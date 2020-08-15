package com.shop.ClientServiceRest.Aop;

import com.shop.ClientServiceRest.Controller.BasketController;
import com.shop.ClientServiceRest.Model.ClientItem;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@Aspect
public class BasketControllerAspect extends TemplateAdvice {
    private final static Logger logger = LoggerFactory.getLogger(BasketController.class);

    @Around("@annotation(com.shop.ClientServiceRest.Aop.NoSuchClientItemPointcut)")
    public Object onThrowNoClientItem(ProceedingJoinPoint joinPoint) throws Throwable {
        return onThrowNoElement(joinPoint);
    }

    @Around("@annotation(com.shop.ClientServiceRest.Aop.BadRequestClientItemPointcut)")
    public Object onBadRequestClientItem(ProceedingJoinPoint joinPoint) throws Throwable {
        return onBadRequest(joinPoint);
    }

    @Override
    void onThrowNoElementLog(Long itemId, Throwable ex) {
        logger.warn("ClientItem with id - " + itemId + " not found");
        logger.error(ex.toString());
    }

    @Override
    void onBadRequestLog() {
        logger.info("Bad request on clientItem information");
    }

    @Override
    String getClassName() {
        return ClientItem.class.getSimpleName();
    }

    @Override
    String getIdName() {
        return "item_id";
    }
}
