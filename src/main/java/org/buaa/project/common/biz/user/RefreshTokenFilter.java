package org.buaa.project.common.biz.user;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.buaa.project.dao.entity.UserDO;
import org.buaa.project.dao.mapper.UserMapper;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.io.IOException;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static org.buaa.project.common.consts.RedisCacheConstants.USER_INFO_KEY;
import static org.buaa.project.common.consts.RedisCacheConstants.USER_LOGIN_EXPIRE_KEY;
import static org.buaa.project.common.consts.RedisCacheConstants.USER_LOGIN_KEY;

/**
 * 刷新 Token 过滤器
 */
@RequiredArgsConstructor
public class RefreshTokenFilter implements Filter {

    private final StringRedisTemplate stringRedisTemplate;

    private final UserMapper userMapper;

    @SneakyThrows
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        String username = httpServletRequest.getHeader("username");
        String requestURI = httpServletRequest.getRequestURI();
        String token = httpServletRequest.getHeader("token");
        //System.out.println(username);
        //System.out.println(token);
        if(requestURI.equals("/api/hangzd/user/last-active-time")) {
            //System.out.println("active time");
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }
        if (StrUtil.isBlank(token) || StrUtil.isBlank(username)) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }
        String hasLogin = stringRedisTemplate.opsForValue().get(USER_LOGIN_KEY + username);
        if (StrUtil.isBlank(hasLogin) || !Objects.equals(hasLogin, token)) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }
        UserDO userDO = JSON.parseObject(stringRedisTemplate.opsForValue().get(USER_INFO_KEY + username), UserDO.class);
        if (userDO == null) {
            userDO = userMapper.selectOne(new QueryWrapper<UserDO>().eq("username", username));
            stringRedisTemplate.opsForValue().set(USER_INFO_KEY + username, JSON.toJSONString(userDO), USER_LOGIN_EXPIRE_KEY, TimeUnit.DAYS);
        }
        UserInfoDTO userInfoDTO = UserInfoDTO.builder().
                userId(String.valueOf(userDO.getId())).
                username(username).
                userType(userDO.getUserType()).
                salt(userDO.getSalt()).
                token(token).
                build();
        UserContext.setUser(userInfoDTO);

        //更新数据库中last_active_time字段
        userDO.setLastActiveTime(new Date(System.currentTimeMillis()));
        LambdaUpdateWrapper<UserDO> updateWrapper = Wrappers.lambdaUpdate(UserDO.class)
                .eq(UserDO::getUsername, username)
                .set(UserDO::getLastActiveTime, userDO.getLastActiveTime());
        userMapper.update(userDO, updateWrapper);

        stringRedisTemplate.expire(USER_LOGIN_KEY + username, USER_LOGIN_EXPIRE_KEY, TimeUnit.DAYS);
        
        try {
            filterChain.doFilter(servletRequest, servletResponse);
        } finally {
            UserContext.removeUser();
        }
    }

}