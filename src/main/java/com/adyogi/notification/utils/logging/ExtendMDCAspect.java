package com.adyogi.notification.utils.logging;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

import com.adyogi.notification.utils.logging.annotation.ExtendMDC;
import com.adyogi.notification.utils.logging.annotation.MDCValue;
import org.apache.logging.log4j.ThreadContext;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

@Aspect
@Component
public class ExtendMDCAspect {
    public ExtendMDCAspect() {
    }

    @Around("@annotation(com.adyogi.notification.utils.logging.annotation.ExtendMDC)")
    public Object extendMDC(ProceedingJoinPoint pjp) throws Throwable {
        Method method = ((MethodSignature)pjp.getSignature()).getMethod();
        ExtendMDC methodAnnotation = (ExtendMDC)method.getAnnotation(ExtendMDC.class);

        Object var4;
        try {
            this.addMethodDefinedValues(methodAnnotation);
            this.addParameterDefinedValues(method.getParameters(), pjp.getArgs());
            var4 = pjp.proceed();
        } finally {
            this.removeMethodDefinedValues(methodAnnotation);
            this.removeParameterDefinedValues(method.getParameters());
        }

        return var4;
    }

    private void addMethodDefinedValues(ExtendMDC methodAnnotation) {
        MDCValue[] var2 = methodAnnotation.value();
        int var3 = var2.length;

        for(int var4 = 0; var4 < var3; ++var4) {
            MDCValue value = var2[var4];
            ThreadContext.put(value.value(), value.content());
        }

    }

    private void removeMethodDefinedValues(ExtendMDC methodAnnotation) {
        MDCValue[] var2 = methodAnnotation.value();
        int var3 = var2.length;

        for(int var4 = 0; var4 < var3; ++var4) {
            MDCValue value = var2[var4];
            ThreadContext.remove(value.value());
        }

    }

    private void addParameterDefinedValues(Parameter[] parameters, Object[] args) {
        for(int i = 0; i < parameters.length; ++i) {
            Parameter parameter = parameters[i];
            MDCValue value = (MDCValue)parameter.getAnnotation(MDCValue.class);
            if (value != null) {
                ThreadContext.put(value.value(), String.valueOf(args[i]));
            }
        }

    }

    private void removeParameterDefinedValues(Parameter[] parameters) {
        for(int i = 0; i < parameters.length; ++i) {
            Parameter parameter = parameters[i];
            MDCValue value = (MDCValue)parameter.getAnnotation(MDCValue.class);
            if (value != null) {
                ThreadContext.remove(value.value());
            }
        }
    }
}
