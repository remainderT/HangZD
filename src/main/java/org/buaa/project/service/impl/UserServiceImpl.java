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
import org.buaa.project.dao.entity.AnswerDO;
import org.buaa.project.dao.entity.MessageDO;
import org.buaa.project.dao.entity.QuestionDO;
import org.buaa.project.dao.entity.UserDO;
import org.buaa.project.dao.mapper.AnswerMapper;
import org.buaa.project.dao.mapper.QuestionMapper;
import org.buaa.project.dao.mapper.UserMapper;
import org.buaa.project.dao.mapper.MessageMapper;
import org.buaa.project.dto.req.question.QuestionUploadReqDTO;
import org.buaa.project.dto.req.user.AskUsersReqDTO;
import org.buaa.project.dto.req.user.UserLoginReqDTO;
import org.buaa.project.dto.req.user.UserRegisterReqDTO;
import org.buaa.project.dto.req.user.UserUpdateReqDTO;
import org.buaa.project.dto.resp.*;
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

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.buaa.project.common.consts.MailSendConstants.EMAIL_SUFFIX;
import static org.buaa.project.common.consts.RedisCacheConstants.*;
import static org.buaa.project.common.enums.AnswerErrorCodeEnum.ANSWER_POST_FAIL;
import static org.buaa.project.common.enums.QuestionErrorCodeEnum.QUESTION_NULL;
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
    private AnswerMapper answerMapper;
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

        //数据库中last_active_time字段更新
        //
        userDO.setLastActiveTime(new Date(System.currentTimeMillis()));
        LambdaUpdateWrapper<UserDO> updateWrapper = Wrappers.lambdaUpdate(UserDO.class)
                .eq(UserDO::getUsername, requestParam.getUsername())
                .set(UserDO::getLastActiveTime, userDO.getLastActiveTime());
        baseMapper.update(null, updateWrapper);
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
    public void changePassword(String oldPassword, String newPassword) {
        LambdaQueryWrapper<UserDO> queryWrapper = Wrappers.lambdaQuery(UserDO.class)
                .eq(UserDO::getUsername, UserContext.getUsername());
        UserDO userDO = baseMapper.selectOne(queryWrapper);
        
        String password = DigestUtils.md5DigestAsHex((oldPassword + userDO.getSalt()).getBytes());
        if (!Objects.equals(userDO.getPassword(), password)) {
            throw new ClientException(USER_PASSWORD_ERROR);
        }
        
        newPassword = DigestUtils.md5DigestAsHex((newPassword + UserContext.getSalt()).getBytes());
        
        LambdaUpdateWrapper<UserDO> updateWrapper = Wrappers.lambdaUpdate(UserDO.class)
                .eq(UserDO::getUsername, UserContext.getUsername())
                .set(UserDO::getPassword, newPassword);
        baseMapper.update(null, updateWrapper);
        logout(UserContext.getUsername(), UserContext.getToken());
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
        //取出likeCount，更改后填回
        Integer updated = userDO.getLikeCount() + increment;
        UserDO updatedUserDO = UserDO.builder()
                .likeCount(updated)
                .build();
        LambdaUpdateWrapper<UserDO> updateWrapper = Wrappers.lambdaUpdate(UserDO.class)
                .eq(UserDO::getId, userDO.getId());
        baseMapper.update(updatedUserDO, updateWrapper);

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
        Integer updated = userDO.getLikeCount() - decrement;
        UserDO updatedUserDO = UserDO.builder()
                .likeCount(updated)
                .build();
        LambdaUpdateWrapper<UserDO> updateWrapper = Wrappers.lambdaUpdate(UserDO.class)
                .eq(UserDO::getId, userDO.getId());
        baseMapper.update(updatedUserDO, updateWrapper);
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
    public List<AnswerRespDTO> getActiveAnswers(String username) {
        // 根据用户名查询用户信息
        LambdaQueryWrapper<UserDO> userQueryWrapper = Wrappers.lambdaQuery(UserDO.class)
                .eq(UserDO::getUsername, username);
        UserDO userDO = baseMapper.selectOne(userQueryWrapper);
        if (userDO == null) {
            throw new ClientException(USER_NULL);
        }

        // 从Answer表中查询该用户的所有活跃回答（del_flag = 0）
        LambdaQueryWrapper<AnswerDO> answerQueryWrapper = Wrappers.lambdaQuery(AnswerDO.class)
                .eq(AnswerDO::getUserId, userDO.getId())
                .eq(AnswerDO::getDelFlag, 0);
        List<AnswerDO> answerDOList = answerMapper.selectList(answerQueryWrapper);
        //System.out.println(answerDOList);
        // 将AnswerDO转换为AnswerRespDTO
        List<AnswerRespDTO> answerRespDTOS = new ArrayList<>();
        for (AnswerDO answerDO : answerDOList) {
            AnswerRespDTO answerRespDTO = new AnswerRespDTO();
            BeanUtils.copyProperties(answerDO, answerRespDTO);
            answerRespDTOS.add(answerRespDTO);
            //System.out.println(answerRespDTO);
        }

        return answerRespDTOS;
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
        List<QuestionDO> questionDOList = questionMapper.selectList(questionQueryWrapper);
        //System.out.println(questionDOList);
        List<QuestionRespDTO> questionRespDTOS = new ArrayList<>();
        for (QuestionDO questionDO : questionDOList) {
            QuestionRespDTO questionRespDTO = new QuestionRespDTO();
            BeanUtils.copyProperties(questionDO, questionRespDTO);
            questionRespDTOS.add(questionRespDTO);
            //System.out.println(questionRespDTO);
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

    @Override
    public void askUsers(AskUsersReqDTO requestParam) {
        Long aid = requestParam.getAid();
        Long qid = requestParam.getQid();
        List<Long> ids = requestParam.getIds();
        System.out.println(aid);
        System.out.println(qid);
        System.out.println(ids);
        // 检查提问者是否存在
        LambdaQueryWrapper<UserDO> askerQueryWrapper = Wrappers.lambdaQuery(UserDO.class)
                .eq(UserDO::getId, aid);
        UserDO asker = baseMapper.selectOne(askerQueryWrapper);
        System.out.println(asker);
        if (asker == null) {
            throw new ClientException(USER_NULL);
        }
        // 检查问题是否存在
        LambdaQueryWrapper<QuestionDO> questionQueryWrapper = Wrappers.lambdaQuery(QuestionDO.class)
                .eq(QuestionDO::getId, qid);
        QuestionDO question = questionMapper.selectOne(questionQueryWrapper);
        if (question == null) {
            throw new ClientException(QUESTION_NULL);
        }

        for(Long answerid: ids) {
            // 检查回答者是否存在
            LambdaQueryWrapper<UserDO> answererQueryWrapper = Wrappers.lambdaQuery(UserDO.class)
                    .eq(UserDO::getId, answerid);
            UserDO answerer = baseMapper.selectOne(answererQueryWrapper);
            if (answerer == null) {
                throw new ClientException(USER_NULL);
            }

            //根据回答者id和提问生成answerDO
            AnswerDO answerDO = new AnswerDO();
            answerDO.setUserId(answerid);
            answerDO.setUsername(answerer.getUsername());
            answerDO.setQuestionId(qid);
            //answerDO.setContent(question.getContent());
            //插入Answer表
            int inserted = answerMapper.insert(answerDO);
            if (inserted < 1) {
                throw new ClientException(ANSWER_POST_FAIL);
            }
        }

    }

    @Override
    public Date getLastActiveTime(Long id) {
        LambdaQueryWrapper<UserDO> queryWrapper = Wrappers.lambdaQuery(UserDO.class)
                .eq(UserDO::getId, id);
        UserDO userDO = baseMapper.selectOne(queryWrapper);
        if (userDO == null) {
            throw new ClientException(USER_NULL);
        }
        //改为东八区时区返回
        Instant utcInstant = userDO.getLastActiveTime().toInstant();
        ZonedDateTime beijingTime = utcInstant.atZone(ZoneId.of("Asia/Shanghai"));
        Date beijingDate = Date.from(beijingTime.toInstant());
        //System.out.println(beijingDate);
        return beijingDate;
    }

}
