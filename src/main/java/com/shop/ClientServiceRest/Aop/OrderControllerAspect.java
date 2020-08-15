package com.shop.ClientServiceRest.Aop;

import com.shop.ClientServiceRest.Controller.OrderController;
import com.shop.ClientServiceRest.Model.Order;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@Aspect
public class OrderControllerAspect extends TemplateAdvice {
    private final static Logger logger = LoggerFactory.getLogger(OrderController.class);

    @Around("@annotation(com.shop.ClientServiceRest.Aop.NoSuchOrderPointcut)")
    public Object onThrowNoOrder(ProceedingJoinPoint joinPoint) throws Throwable {
        return onThrowNoElement(joinPoint);
    }

    @Around("@annotation(com.shop.ClientServiceRest.Aop.BadRequestOrderPointcut)")
    public Object onBadRequestOrder(ProceedingJoinPoint joinPoint) throws Throwable {
        return onBadRequest(joinPoint);
    }

    @Override
    void onThrowNoElementLog(Long orderId, Throwable ex) {
        logger.warn("Order with id - " + orderId + " not found");
        logger.error(ex.toString());
    }

    @Override
    void onBadRequestLog() {
        logger.info("Bad request on order information");
    }

    @Override
    String getClassName() {
        return Order.class.getSimpleName();
    }

    @Override
    String getIdName() {
        return "order_id";
    }
}
