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
import org.buaa.project.common.enums.UserErrorCodeEnum;
import org.buaa.project.dao.entity.MessageDO;
import org.buaa.project.dao.entity.QuestionDO;
import org.buaa.project.dao.entity.UserDO;
import org.buaa.project.dao.mapper.QuestionMapper;
import org.buaa.project.dao.mapper.UserMapper;
import org.buaa.project.dao.mapper.MessageMapper;
import org.buaa.project.dto.req.user.UserLoginReqDTO;
import org.buaa.project.dto.req.user.UserRegisterReqDTO;
import org.buaa.project.dto.req.user.UserUpdateReqDTO;
import org.buaa.project.dto.resp.MessageRespDTO;
import org.buaa.project.dto.resp.QuestionRespDTO;
import org.buaa.project.dto.resp.UserLoginRespDTO;
import org.buaa.project.dto.resp.UserRespDTO;
import org.buaa.project.service.UserService;
import org.buaa.project.toolkit.RandomGenerator;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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

    @Value("${spring.mail.username}")
    private String from;

    @Autowired
    private MessageMapper messageMapper;
    @Autowired
    private QuestionMapper questionMapper;

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
    public UserRespDTO getUserById(Long id) {
        LambdaQueryWrapper<UserDO> queryWrapper = Wrappers.lambdaQuery(UserDO.class)
                .eq(UserDO::getId, id);
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
        String emailkey = mail.replace(EMAIL_SUFFIX,"");
        String frequentKey = USER_CODE_TO_FREQUENT+ emailkey;
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
        } catch (DuplicateKeyException ex) {
            throw new ClientException(USER_EXIST);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public UserLoginRespDTO login(UserLoginReqDTO requestParam, ServletRequest request) {
        //验证码还没搞
        /*HttpServletRequest httpRequest = (HttpServletRequest) request;
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
*/
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
    public void logout(String username, String token) {
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
        String password = DigestUtils.md5DigestAsHex((requestParam.getPassword() + UserContext.getSalt()).getBytes());
        UserDO userDO = UserDO.builder()
                        .username(requestParam.getNewUsername())
                        .password(password)
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
    public void likeUser(String username, Integer increment) {
        if (increment == null || increment < 0) {
            throw new ClientException(USER_INVALID_DATA);
        }
        if (!hasUsername(username)) {
            throw new ClientException(USER_NAME_NULL);
        }
        LambdaQueryWrapper<UserDO> queryWrapper = Wrappers.lambdaQuery(UserDO.class)
                .eq(UserDO::getUsername, username);
        UserDO userDO = baseMapper.selectOne(queryWrapper);
        if (userDO == null ) {
            throw new ClientException(USER_NULL);
        } else if(userDO.getLikeCount() == null || userDO.getLikeCount() < 0) {
            throw new ClientException(USER_INVALID_DATA);
        }
        LambdaUpdateWrapper<UserDO> updateWrapper = Wrappers.lambdaUpdate(UserDO.class)
                .eq(UserDO::getUsername, username)
                .setSql("like_count = like_count + " + increment);
        baseMapper.update(null, updateWrapper);

        /*
          更新redis缓存
         */

        String key = USER_INFO_KEY + username;
        String userInfo = stringRedisTemplate.opsForValue().get(key);
        if (StrUtil.isNotEmpty(userInfo)) {
            UserDO user = JSON.parseObject(userInfo, UserDO.class);
            user.setLikeCount(user.getLikeCount() + increment);
            stringRedisTemplate.opsForValue().set(key, JSON.toJSONString(user));
        }
    }

    @Override
    public void dislikeUser(String username, Integer decrement) {
        if (decrement == null || decrement < 0) {
            throw new ClientException(USER_INVALID_DATA);
        }
        if (!hasUsername(username)) {
            throw new ClientException(USER_NAME_NULL);
        }
        LambdaQueryWrapper<UserDO> queryWrapper = Wrappers.lambdaQuery(UserDO.class)
                .eq(UserDO::getUsername, username);
        UserDO userDO = baseMapper.selectOne(queryWrapper);
        if (userDO == null ) {
            throw new ClientException(USER_NULL);
        } else if(userDO.getLikeCount() == null || userDO.getLikeCount() < 0) {
            throw new ClientException(USER_INVALID_DATA);
        }
        //检查减去后的值是否小于0
        if (userDO.getLikeCount() - decrement < 0) {
            throw new ClientException(USER_INVALID_DECREMENT);
        }
        LambdaUpdateWrapper<UserDO> updateWrapper = Wrappers.lambdaUpdate(UserDO.class)
                .eq(UserDO::getUsername, username)
                .setSql("like_count = like_count - " + decrement);
        baseMapper.update(null, updateWrapper);
        /*
          更新redis缓存
         */
        String key = USER_INFO_KEY + username;
        String userInfo = stringRedisTemplate.opsForValue().get(key);
        if (StrUtil.isNotEmpty(userInfo)) {
            UserDO user = JSON.parseObject(userInfo, UserDO.class);
            user.setLikeCount(user.getLikeCount() - decrement);
            stringRedisTemplate.opsForValue().set(key, JSON.toJSONString(user));
        }
    }

    @Override
    public void collectUpdate(Boolean collect) {
        Integer change = collect ? 1 : -1;
        String username = UserContext.getUsername();
        LambdaQueryWrapper<UserDO> queryWrapper = Wrappers.lambdaQuery(UserDO.class)
                .eq(UserDO::getUsername, username);
        UserDO userDO = baseMapper.selectOne(queryWrapper);
        if (userDO == null ) {
            throw new ClientException(USER_NULL);
        } else if(userDO.getCollectCount() == null || userDO.getCollectCount() < 0) {
            throw new ClientException(USER_INVALID_DATA);
        }
        if(userDO.getCollectCount() + change < 0) {
            throw new ClientException(USER_INVALID_DECREMENT);
        }
        LambdaUpdateWrapper<UserDO> updateWrapper = Wrappers.lambdaUpdate(UserDO.class)
                .eq(UserDO::getUsername, username)
                .setSql("collect_count = collect_count + " + change);
        baseMapper.update(null, updateWrapper);

        /*
          更新redis缓存
         */

        String key = USER_INFO_KEY + username;
        String userInfo = stringRedisTemplate.opsForValue().get(key);
        if (StrUtil.isNotEmpty(userInfo)) {
            UserDO user = JSON.parseObject(userInfo, UserDO.class);
            user.setCollectCount(user.getCollectCount() + change);
            stringRedisTemplate.opsForValue().set(key, JSON.toJSONString(user));
        }
    }

    @Override
    public void usefulUpdate(String username,Boolean useful) {
        Integer change = useful ? 1 : -1;
        LambdaQueryWrapper<UserDO> queryWrapper = Wrappers.lambdaQuery(UserDO.class)
                .eq(UserDO::getUsername, username);
        UserDO userDO = baseMapper.selectOne(queryWrapper);
        if (userDO == null ) {
            throw new ClientException(USER_NULL);
        } else if(userDO.getUsefulCount() == null || userDO.getUsefulCount() < 0) {
            throw new ClientException(USER_INVALID_DATA);
        }
        if(userDO.getUsefulCount() + change < 0) {
            throw new ClientException(USER_INVALID_DECREMENT);
        }
        LambdaUpdateWrapper<UserDO> updateWrapper = Wrappers.lambdaUpdate(UserDO.class)
                .eq(UserDO::getUsername, username)
                .setSql("useful_count = useful_count + " + change);
        baseMapper.update(null, updateWrapper);

        /*
          更新redis缓存
         */

        String key = USER_INFO_KEY + username;
        String userInfo = stringRedisTemplate.opsForValue().get(key);
        if (StrUtil.isNotEmpty(userInfo)) {
            UserDO user = JSON.parseObject(userInfo, UserDO.class);
            user.setUsefulCount(user.getUsefulCount() + change);
            stringRedisTemplate.opsForValue().set(key, JSON.toJSONString(user));
        }

    }

    @Override
    public List<MessageRespDTO> getActiveAnswers(String username) {
        // 根据用户名查询用户信息
        LambdaQueryWrapper<UserDO> userQueryWrapper = Wrappers.lambdaQuery(UserDO.class)
                .eq(UserDO::getUsername, username);
        UserDO userDO = baseMapper.selectOne(userQueryWrapper);
        if (userDO == null) {
            throw new ClientException(USER_NULL);
        }

        // 从Message表中查询该用户的所有活跃回答（del_flag = 0）
        LambdaQueryWrapper<MessageDO> messageQueryWrapper = Wrappers.lambdaQuery(MessageDO.class)
                .eq(MessageDO::getFromId, userDO.getId())
                .eq(MessageDO::getDelFlag, 0);
        List<MessageDO> messageDOList = messageMapper.selectList(messageQueryWrapper);

        // 将MessageDO转换为MessageRespDTO
        List<MessageRespDTO> messageRespDTOS = new ArrayList<>();
        for (MessageDO messageDO : messageDOList) {
            MessageRespDTO messageRespDTO = new MessageRespDTO();
            BeanUtils.copyProperties(messageDO, messageRespDTO);
            messageRespDTOS.add(messageRespDTO);
        }

        return messageRespDTOS;
    }

    @Override
    public List<QuestionRespDTO> getActiveQuestions(String username) {
        // 根据用户名查询用户信息
        LambdaQueryWrapper<UserDO> userQueryWrapper = Wrappers.lambdaQuery(UserDO.class)
                .eq(UserDO::getUsername, username);
        UserDO userDO = baseMapper.selectOne(userQueryWrapper);
        if (userDO == null) {
            throw new ClientException(USER_NULL);
        }

        // 从Question表中查询该用户的所有活跃回答（del_flag = 0）

        LambdaQueryWrapper<QuestionDO> questionQueryWrapper = Wrappers.lambdaQuery(QuestionDO.class)
                .eq(QuestionDO::getUserId, userDO.getId())
                .eq(QuestionDO::getDelFlag, 0);
        //System.out.println("1");
        List<QuestionDO> questionDOList = questionMapper.selectList(questionQueryWrapper);
        //System.out.println("2");
        List<QuestionRespDTO> questionRespDTOS = new ArrayList<>();
        for (QuestionDO questionDO : questionDOList) {
            QuestionRespDTO questionRespDTO = new QuestionRespDTO();
            BeanUtils.copyProperties(questionDO, questionRespDTO);
            questionRespDTOS.add(questionRespDTO);
        }

        return questionRespDTOS;
    }
    
    @Override
    public void updateTags(String username, String tags) {
        if (!Objects.equals(username, UserContext.getUsername())) {
            throw new ClientException(USER_UPDATE_ERROR);
        }
        
        if (tags == null) {
            throw new ClientException(USER_INVALID_DATA);
        }
        if (!hasUsername(username)) {
            throw new ClientException(USER_NAME_NULL);
        }
        
        LambdaQueryWrapper<UserDO> queryWrapper = Wrappers.lambdaQuery(UserDO.class)
                .eq(UserDO::getUsername, username);
        UserDO userDO = baseMapper.selectOne(queryWrapper);
        if (userDO == null) {
            throw new ClientException(USER_NULL);
        }
        
        LambdaUpdateWrapper<UserDO> updateWrapper = Wrappers.lambdaUpdate(UserDO.class)
                .eq(UserDO::getUsername, username)
                .set(UserDO::getTags, tags);
        baseMapper.update(null, updateWrapper);
    }
    
}
