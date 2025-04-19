package org.buaa.project.toolkit.timecost;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class CostTimeAspect {

    @Pointcut("execution(* org.buaa.project.controller..*(..))")
    public void costTime() {
    }

    @Around("costTime()")
    public Object costTimeAround(ProceedingJoinPoint joinPoint) throws Throwable {
        Object obj = null;
        try {
            long beginTime = System.currentTimeMillis();
            obj = joinPoint.proceed();
            String method = joinPoint.getSignature().getName();
            String className = joinPoint.getSignature().getDeclaringTypeName();
            long cost = System.currentTimeMillis() - beginTime;

            log.info("[{}] [Method: {}] - 接口调用耗时: {}ms", className, method, cost);
        } catch (Throwable throwable) {
            throw throwable;
        }
        return obj;
    }
}
