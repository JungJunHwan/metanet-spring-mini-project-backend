package com.dashboard.app.global.aop;

import java.util.Arrays;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.AfterReturning;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(LoggingAspect.class);

    @Before("execution(* com.dashboard.app..controller..*(..))")
    public void logBeforeController(JoinPoint joinPoint) {
        log.info("[AOP] Enter {} args={}", joinPoint.getSignature().toShortString(),
                Arrays.toString(joinPoint.getArgs()));
    }

    @AfterReturning(pointcut = "execution(* com.dashboard.app..controller..*(..))", returning = "result")
    public void logAfterReturningController(JoinPoint joinPoint, Object result) {
        log.info("[AOP] Exit {} result={}", joinPoint.getSignature().toShortString(), result);
    }

    @AfterThrowing(pointcut = "execution(* com.dashboard.app..controller..*(..))", throwing = "exception")
    public void logAfterThrowingController(JoinPoint joinPoint, Throwable exception) {
        log.error("[AOP] Exception in {} message={}", joinPoint.getSignature().toShortString(),
                exception.getMessage(), exception);
    }
}
