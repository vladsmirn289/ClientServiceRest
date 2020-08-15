package com.shop.ClientServiceRest.Aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.NoSuchElementException;

@Component
public abstract class TemplateAdvice {
    public Object onThrowNoElement(ProceedingJoinPoint joinPoint) throws Throwable {
        Long id = null;
        MethodSignature methodSignature = (MethodSignature)joinPoint.getSignature();
        Method method = methodSignature.getMethod();
        Object[] args = joinPoint.getArgs();
        Parameter[] params = methodSignature.getMethod().getParameters();

        for (int i = 0; i < method.getParameterCount(); ++i) {
            for (Annotation ann : params[i].getAnnotations()) {
                if (ann instanceof PathVariable && ((PathVariable)ann).value().equals(getIdName())) {
                    id = (Long)args[i];
                    break;
                }
            }

            if (id != null) {
                break;
            }
        }

        try {
            return joinPoint.proceed();
        } catch (NoSuchElementException | NullPointerException ex) {
            onThrowNoElementLog(id, ex);

            Class<?> returnValue = methodSignature.getMethod().getReturnType();
            if (returnValue != Void.class) {
                return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
            } else {
                return Void.class;
            }
        }
    }

    public Object onBadRequest(ProceedingJoinPoint joinPoint) throws Throwable {
        BindingResult bindingResult = null;
        Object object = null;

        for (Object arg : joinPoint.getArgs()) {
            if (arg.getClass().getSimpleName().contains("BindingResult")) {
                bindingResult = (BindingResult)arg;
            }

            if (arg.getClass().getSimpleName().equals(getClassName())) {
                object = arg;
            }
        }

        if (bindingResult == null || bindingResult.hasErrors()) {
            onBadRequestLog();

            return new ResponseEntity<>(object, HttpStatus.BAD_REQUEST);
        } else {
            return joinPoint.proceed();
        }
    }

    abstract void onThrowNoElementLog(Long id, Throwable ex);
    abstract void onBadRequestLog();
    abstract String getClassName();
    abstract String getIdName();
}
