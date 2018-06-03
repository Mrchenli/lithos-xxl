package com.ik2k.lithos.core.util;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@Component
public class AdminSpringContext implements ApplicationContextAware{

    private static ApplicationContext ctx;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        ctx = applicationContext;
    }

    public static <T> T getBean(String name) {
        return (T) ctx.getBean(name);
    }

    public static Method getMethod(Object target,String methodName,Class<?>... paramTypes) throws NoSuchMethodException, NoSuchFieldException {
        if(paramTypes.length==0){
            return target.getClass().getDeclaredMethod(methodName);
        }else{
            return target.getClass().getDeclaredMethod(methodName,paramTypes);
        }
    }

    public static <T> T execute(Object target,Method method,Object[] args) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        return (T) method.invoke(target,args);
    }


}
