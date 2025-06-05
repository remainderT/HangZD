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
import lombok.extern.slf4j.Slf4j;
import org.buaa.project.common.convention.exception.ClientException;
import org.buaa.project.common.convention.result.Results;
import org.buaa.project.config.UserFlowRiskControlConfiguration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;

import java.io.IOException;
import java.io.PrintWriter;

import static org.buaa.project.common.enums.ServiceErrorCodeEnum.FLOW_LIMIT_ERROR;

/**
 * 用户操作流量风控过滤器
 */
@Slf4j
@RequiredArgsConstructor
public class UserFlowRiskControlFilter implements Filter {

    private final StringRedisTemplate stringRedisTemplate;
    private final UserFlowRiskControlConfiguration userFlowRiskControlConfiguration;

    private static final String USER_FLOW_RISK_CONTROL_LUA_SCRIPT_PATH = "user_flow_risk_control.lua";

    @SneakyThrows
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String requestURI = httpRequest.getRequestURI();
        
        // 排除WebSocket连接
        if ("/ws".equals(requestURI)) {
            filterChain.doFilter(request, response);
            return;
        }
        
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource(USER_FLOW_RISK_CONTROL_LUA_SCRIPT_PATH)));
        redisScript.setResultType(Long.class);
        String ipAddress = request.getRemoteAddr();
        Long result;
        try {
            result = stringRedisTemplate.execute(redisScript, Lists.newArrayList(ipAddress), userFlowRiskControlConfiguration.getTimeWindow());
        } catch (Throwable ex) {
            log.error("执行用户请求流量限制LUA脚本出错", ex);
            returnJson((HttpServletResponse) response, JSON.toJSONString(Results.failure(new ClientException(FLOW_LIMIT_ERROR))));
            return;
        }
        if (result == null || result > userFlowRiskControlConfiguration.getMaxAccessCount()) {
            log.error("用户请求流量超限: " + ipAddress + " " + UserContext.getUsername());
            returnJson((HttpServletResponse) response, JSON.toJSONString(Results.failure(new ClientException(FLOW_LIMIT_ERROR))));
            return;
        }
        filterChain.doFilter(request, response);
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
