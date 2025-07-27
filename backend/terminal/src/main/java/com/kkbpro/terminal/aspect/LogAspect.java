package com.kkbpro.terminal.aspect;

import com.kkbpro.terminal.utils.IpUtil;
import com.kkbpro.terminal.utils.LogUtil;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 操作日志切面
 */
@Aspect
@Component
public class LogAspect {

    /**
     * 方法执行起始时间
     */
    private static final ThreadLocal<Long> startTime = new ThreadLocal<>();

    /**
     * 定义切入点
     */
    @Pointcut("@annotation(com.kkbpro.terminal.annotation.Log)")
    private void logPointCut() {}

    /**
     * 前置通知
     */
    @Before("logPointCut()")
    public void logBefore(JoinPoint joinPoint) {
        String ip = IpUtil.getIpAddress(((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest());
        Logger logger = LogUtil.getLoggerForClass(joinPoint.getTarget().getClass());
        logger.info("Method {} starts execution from IP {}", joinPoint.getSignature().getName(), ip);
        startTime.set(System.currentTimeMillis());
    }

    /**
     * 后置通知
     */
    @After("logPointCut()")
    public void logAfter(JoinPoint joinPoint) {
        Long totalTime = System.currentTimeMillis() - startTime.get();
        startTime.remove();
        Logger logger = LogUtil.getLoggerForClass(joinPoint.getTarget().getClass());
        logger.info("Method {} costs {}ms totally", joinPoint.getSignature().getName(), totalTime);
    }

    /**
     * 异常通知
     */
    @AfterThrowing(value = "logPointCut()", throwing = "e")
    public void logThrowing(JoinPoint joinPoint, Exception e) {
        Logger logger = LogUtil.getLoggerForClass(joinPoint.getTarget().getClass());
        logger.error("Exception occurred {}", e.getMessage(), e);
    }

}
