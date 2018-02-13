package org.fpwei.line.crawler.aop;


import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class TaskAspect {

    @Pointcut("execution(public * org.fpwei.line.crawler.task.*.*(..)) && @annotation(org.springframework.scheduling.annotation.Scheduled)")
    public void taskMethod() {

    }

    @Before("taskMethod()")
    public void beforeTaskExecute(JoinPoint joinPoint) {
        String method = joinPoint.getSignature().toString();
        log.info("Starting [{}] ...", method);
    }


    @After("taskMethod()")
    public void afterTaskExecute(JoinPoint joinPoint) {
        String method = joinPoint.getSignature().toString();
        log.info("[{}] finish ...", method);
    }

}
