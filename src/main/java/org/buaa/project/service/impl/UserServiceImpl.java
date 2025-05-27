package org.buaa.project.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.buaa.project.common.biz.user.UserContext;
import org.buaa.project.common.consts.MailSendConstants;
import org.buaa.project.common.convention.exception.ClientException;
import org.buaa.project.common.convention.exception.ServiceException;
import org.buaa.project.common.enums.EntityTypeEnum;
import org.buaa.project.common.enums.UserActionTypeEnum;
import org.buaa.project.common.enums.UserErrorCodeEnum;
import org.buaa.project.dao.entity.UserDO;
import org.buaa.project.dao.mapper.UserMapper;
import org.buaa.project.dto.req.user.*;
import org.buaa.project.dto.resp.UserLoginRespDTO;
import org.buaa.project.dto.resp.UserRespDTO;
import org.buaa.project.service.UserActionService;
import org.buaa.project.service.UserService;
import org.buaa.project.toolkit.RandomGenerator;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.Arrays;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static org.buaa.project.common.consts.MailSendConstants.EMAIL_SUFFIX;
import static org.buaa.project.common.consts.RedisCacheConstants.*;
import static org.buaa.project.common.enums.ServiceErrorCodeEnum.MAIL_SEND_ERROR;
import static org.buaa.project.common.enums.UserErrorCodeEnum.*;

/**
 * 用户接口实现层
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, UserDO> implements UserService {

    private final JavaMailSender mailSender;

    private final RedissonClient redissonClient;

    private final StringRedisTemplate stringRedisTemplate;

    private final UserActionService userActionService;

    @Value("${spring.mail.username}")
    private String from;

    @Override
    public UserRespDTO getUserByUsername(String username) {
        LambdaQueryWrapper<UserDO> queryWrapper = Wrappers.lambdaQuery(UserDO.class)
                .eq(UserDO::getUsername, username);
        UserDO userDO = baseMapper.selectOne(queryWrapper);
        if (userDO == null) {
            throw new ServiceException(UserErrorCodeEnum.USER_NULL);
        }
        UserRespDTO result = new UserRespDTO();
        BeanUtils.copyProperties(userDO, result);
        return result;
    }


    @Override
    public Boolean hasUsername(String username) {
        LambdaQueryWrapper<UserDO> queryWrapper = Wrappers.lambdaQuery(UserDO.class)
                .eq(UserDO::getUsername, username);
        UserDO userDO = baseMapper.selectOne(queryWrapper);
        return userDO != null;
    }

    @Override
    public Boolean sendCode(String mail) {
        String emailKey = mail.replace(EMAIL_SUFFIX,"");
        String frequentKey = USER_CODE_TO_FREQUENT+ emailKey;
        Boolean acquired = stringRedisTemplate.opsForValue()
                .setIfAbsent(frequentKey, "1", 1, TimeUnit.MINUTES);
        if(Boolean.FALSE.equals(acquired)) {
            throw new ClientException(UserErrorCodeEnum.USER_CODE_TOO_FREQUENT);
        }
        SimpleMailMessage message = new SimpleMailMessage();
        String code = RandomGenerator.generateSixDigitCode();
        message.setFrom(from);
        message.setText(String.format(MailSendConstants.TEXT, code));
        message.setTo(mail);
        message.setSubject(MailSendConstants.SUBJECT);
        try {
            mailSender.send(message);
            String key = USER_REGISTER_CODE_KEY + mail.replace(EMAIL_SUFFIX,"");
            stringRedisTemplate.opsForValue().set(key, code, USER_REGISTER_CODE_EXPIRE_KEY, TimeUnit.MINUTES);
            return true;
        } catch (Exception e) {
            throw new ServiceException(MAIL_SEND_ERROR);
        }
    }

    @Override
    public void register(UserRegisterReqDTO requestParam) {
        String code = requestParam.getCode();
        String key = USER_REGISTER_CODE_KEY + requestParam.getMail().replace(EMAIL_SUFFIX,"");
        String cacheCode = stringRedisTemplate.opsForValue().get(key);
        if (!code.equals(cacheCode)) {
            throw new ClientException(USER_CODE_ERROR);
        }
        if (hasUsername(requestParam.getUsername())) {
            throw new ClientException(USER_NAME_EXIST);
        }
        RLock lock = redissonClient.getLock(USER_REGISTER_LOCK_KEY + requestParam.getUsername());
        if (!lock.tryLock()) {
            throw new ClientException(USER_NAME_EXIST);
        }
        try {
            UserDO userDO = BeanUtil.toBean(requestParam, UserDO.class);
            userDO.setSalt(UUID.randomUUID().toString().substring(0, 5));
            userDO.setPassword(DigestUtils.md5DigestAsHex((userDO.getPassword() + userDO.getSalt()).getBytes()));
            int inserted = -1;
            try {
                inserted = baseMapper.insert(userDO);
            } catch (Exception e){
              throw new ClientException(USER_INVALID_DATA);
            }
            if (inserted < 1) {
                throw new ClientException(USER_SAVE_ERROR);
            }
            userDO = baseMapper.selectOne(Wrappers.lambdaQuery(UserDO.class)
                    .eq(UserDO::getUsername, requestParam.getUsername()));
            stringRedisTemplate.opsForValue().set(USER_INFO_KEY + requestParam.getUsername(), JSON.toJSONString(userDO));
            //删除redis的验证码
            stringRedisTemplate.delete(key);
            //删除redis的频繁发送验证码的key
            String frequentKey = USER_CODE_TO_FREQUENT+ requestParam.getMail().replace(EMAIL_SUFFIX,"");
            stringRedisTemplate.delete(frequentKey);
        } catch (DuplicateKeyException ex) {
            throw new ClientException(USER_EXIST);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public UserLoginRespDTO login(UserLoginReqDTO requestParam, ServletRequest request) {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        Cookie[] cookies = httpRequest.getCookies();
        String captchaOwner = "";
        if (cookies != null) {
            captchaOwner = Arrays.stream(cookies)
                    .filter(cookie -> "CaptchaOwner".equals(cookie.getName()))
                    .findFirst()
                    .map(Cookie::getValue)
                    .orElse(null);
        }

        String code = stringRedisTemplate.opsForValue().get(USER_LOGIN_CAPTCHA_KEY + captchaOwner);
        if (StrUtil.isBlank(code) || !code.equalsIgnoreCase(requestParam.getCode())) {
            throw new ClientException(USER_LOGIN_CAPTCHA_ERROR);
        }

        if (!hasUsername(requestParam.getUsername())) {
            throw new ClientException(USER_NAME_NULL);
        }
        LambdaQueryWrapper<UserDO> queryWrapper = Wrappers.lambdaQuery(UserDO.class)
                .eq(UserDO::getUsername, requestParam.getUsername());
        UserDO userDO = baseMapper.selectOne(queryWrapper);

        String password = DigestUtils.md5DigestAsHex((requestParam.getPassword() + userDO.getSalt()).getBytes());
        if (!Objects.equals(userDO.getPassword(), password)) {
            throw new ClientException(USER_PASSWORD_ERROR);
        }

        //数据库中last_active_time字段更新
        userDO.setLastActiveTime(new Date(System.currentTimeMillis()));
        baseMapper.updateById(userDO);

        /**
         * String
         * Key：user:login:username
         * Value: token标识
         */
        String hasLogin = stringRedisTemplate.opsForValue().get(USER_LOGIN_KEY + requestParam.getUsername());
        if (StrUtil.isNotEmpty(hasLogin)) {
            // throw new ClientException(USER_REPEATED_LOGIN);
            return new UserLoginRespDTO(hasLogin);
        }
        String uuid = UUID.randomUUID().toString();
        stringRedisTemplate.opsForValue().set(USER_LOGIN_KEY + requestParam.getUsername(), uuid, USER_LOGIN_EXPIRE_KEY, TimeUnit.DAYS);
        return new UserLoginRespDTO(uuid);
    }

    @Override
    public Boolean checkLogin(String username, String token) {
        String hasLogin = stringRedisTemplate.opsForValue().get(USER_LOGIN_KEY + username);
        if (StrUtil.isEmpty(hasLogin)) {
            return false;
        }
        return Objects.equals(hasLogin, token);
    }

    @Override
    public void logout() {
        String username = UserContext.getUsername();
        String token = UserContext.getToken();
        if (checkLogin(username, token)) {
            stringRedisTemplate.delete(USER_LOGIN_KEY + username);
            return;
        }
        throw new ClientException(USER_TOKEN_NULL);
    }

    @Override
    public void update(UserUpdateReqDTO requestParam) {
        if (!Objects.equals(requestParam.getOldUsername(), UserContext.getUsername())) {
            throw new ClientException(USER_UPDATE_ERROR);
        }
        if (!requestParam.getOldUsername().equals(requestParam.getNewUsername()) && hasUsername(requestParam.getNewUsername())) {
            throw new ClientException(USER_NAME_EXIST);
        }
        
        UserDO userDO = UserDO.builder()
                        .username(requestParam.getNewUsername())
                        .avatar(requestParam.getAvatar())
                        .phone(requestParam.getPhone())
                        .introduction(requestParam.getIntroduction())
                        .build();
        LambdaUpdateWrapper<UserDO> updateWrapper = Wrappers.lambdaUpdate(UserDO.class)
            .eq(UserDO::getUsername, requestParam.getOldUsername());
        baseMapper.update(userDO, updateWrapper);
        /**
         * 更新redis缓存
         */
        if (!requestParam.getOldUsername().equals(requestParam.getNewUsername())) {
            stringRedisTemplate.delete(USER_INFO_KEY + requestParam.getOldUsername());
            stringRedisTemplate.opsForValue().set(USER_LOGIN_KEY + requestParam.getNewUsername(), UserContext.getToken(), USER_LOGIN_EXPIRE_KEY, TimeUnit.DAYS);
        }
        UserDO newUserDO = baseMapper.selectOne(Wrappers.lambdaQuery(UserDO.class)
                .eq(UserDO::getUsername, requestParam.getNewUsername()));
        stringRedisTemplate.opsForValue().set(USER_INFO_KEY + requestParam.getNewUsername(), JSON.toJSONString(newUserDO));
    }
    
    @Override
    public void changePassword(ChangePasswordReqDTO requestParam) {
        LambdaQueryWrapper<UserDO> queryWrapper = Wrappers.lambdaQuery(UserDO.class)
                .eq(UserDO::getUsername, UserContext.getUsername());
        UserDO userDO = baseMapper.selectOne(queryWrapper);
        
        String password = DigestUtils.md5DigestAsHex((requestParam.getOldPassword() + userDO.getSalt()).getBytes());
        if (!Objects.equals(userDO.getPassword(), password)) {
            throw new ClientException(USER_PASSWORD_ERROR);
        }
        
        String newPassword = DigestUtils.md5DigestAsHex((requestParam.getNewPassword() + UserContext.getSalt()).getBytes());
        
        LambdaUpdateWrapper<UserDO> updateWrapper = Wrappers.lambdaUpdate(UserDO.class)
                .eq(UserDO::getUsername, UserContext.getUsername())
                .set(UserDO::getPassword, newPassword);
        baseMapper.update(null, updateWrapper);
        if (checkLogin(UserContext.getUsername(), UserContext.getToken())) {
            stringRedisTemplate.delete(USER_LOGIN_KEY + UserContext.getUsername());
            return;
        }
        throw new ClientException(USER_TOKEN_NULL);
    }
    
    @Override
    public void likeUser(LikeUserReqDTO requestParam) {
        Long id = requestParam.getId();
        userActionService.collectAndLike(EntityTypeEnum.USER, id, UserActionTypeEnum.LIKE);
    }
    
    @Override
    public void updateTags(UpdateUserTagsReqDTO requestParam) {
        String username = UserContext.getUsername();
        String tags = requestParam.getTags();
        
        if (!Objects.equals(username, UserContext.getUsername())) {
            throw new ClientException(USER_UPDATE_ERROR);
        }
        
        if (tags == null) {
            throw new ClientException(USER_INVALID_DATA);
        }
        
        LambdaQueryWrapper<UserDO> queryWrapper = Wrappers.lambdaQuery(UserDO.class)
                .eq(UserDO::getUsername, username);
        UserDO userDO = baseMapper.selectOne(queryWrapper);

        if (userDO == null) {
            throw new ClientException(USER_NULL);
        }

        userDO.setTags(tags);
        baseMapper.updateById(userDO);
        
        stringRedisTemplate.delete(USER_INFO_KEY + username);
        UserDO updatedUser = baseMapper.selectOne(queryWrapper);
        stringRedisTemplate.opsForValue().set(
                USER_INFO_KEY + username,
                JSON.toJSONString(updatedUser)
        );
    }

    @Override
    public UserDO getUserById(Long id) {
        UserDO userDO = baseMapper.selectById(id);
        if (userDO == null) {
            throw new ClientException(USER_NULL);
        }
        return userDO;
    }

}
