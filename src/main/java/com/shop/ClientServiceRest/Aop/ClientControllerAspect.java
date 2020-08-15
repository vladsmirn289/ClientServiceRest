package com.shop.ClientServiceRest.Aop;

import com.shop.ClientServiceRest.Controller.ClientController;
import com.shop.ClientServiceRest.Model.Client;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@Aspect
public class ClientControllerAspect extends TemplateAdvice {
    private final static Logger logger = LoggerFactory.getLogger(ClientController.class);

    @Around("@annotation(com.shop.ClientServiceRest.Aop.NoSuchClientPointcut)")
    public Object onThrowNoClient(ProceedingJoinPoint joinPoint) throws Throwable {
        return onThrowNoElement(joinPoint);
    }

    @Around("@annotation(com.shop.ClientServiceRest.Aop.BadRequestClientPointcut)")
    public Object onBadRequestClient(ProceedingJoinPoint joinPoint) throws Throwable {
        return onBadRequest(joinPoint);
    }

    @Override
    void onThrowNoElementLog(Long id, Throwable ex) {
        logger.warn("Client with id - " + id + " not found");
        logger.error(ex.toString());
    }

    @Override
    void onBadRequestLog() {
        logger.info("Bad request on client information");
    }

    @Override
    String getClassName() {
        return Client.class.getSimpleName();
    }

    @Override
    String getIdName() {
        return "id";
    }
}
