package org.buaa.project.common.biz.user;

import com.alibaba.fastjson2.JSON;
import com.google.common.collect.Lists;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.buaa.project.common.convention.exception.ClientException;
import org.buaa.project.common.convention.result.Results;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Locale;

import static org.buaa.project.common.enums.UserErrorCodeEnum.USER_TOKEN_NULL;

/**
 * 登录检验过滤器
 */
@RequiredArgsConstructor
public class
LoginCheckFilter implements Filter {

    private static final List<String> IGNORE_URI = Lists.newArrayList(
            "/api/hangzd/user/login", //登录
            "/api/hangzd/user/send-code", //注册时发送验证码
            "/api/hangzd/user/captcha", //登录时获取验证码
            "/api/hangzd/user/last-active-time"
            /*"/api/answerly/v1/user/send-code",
            "/api/answerly/v1/user/login"*/
    );

    private boolean requireLogin(String URI, String method) {
        if (IGNORE_URI.contains(URI)) {
            return false;
        }
        // 注册用户
        if (URI.equals("/api/hangzd/user") && method.equals("POST")) {
            return false;
        }
        return true;
    }

    @SneakyThrows
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        String requestURI = httpServletRequest.getRequestURI();
        String method = httpServletRequest.getMethod();
        if (requireLogin(requestURI, method) && UserContext.getUsername() == null) {
            returnJson((HttpServletResponse) servletResponse, JSON.toJSONString(Results.failure(new ClientException(USER_TOKEN_NULL))));
            return;
        }

        filterChain.doFilter(servletRequest, servletResponse);
    }

    private void returnJson(HttpServletResponse response, String json) throws Exception {
        PrintWriter writer = null;
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=utf-8");
        try {
            writer = response.getWriter();
            writer.print(json);

        } catch (IOException e) {
        } finally {
            if (writer != null)
                writer.close();
        }
    }


}
