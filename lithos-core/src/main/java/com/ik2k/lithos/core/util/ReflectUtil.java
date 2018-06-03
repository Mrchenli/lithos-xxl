package com.ik2k.lithos.core.util;

import java.lang.reflect.Field;
import java.util.Arrays;

public class ReflectUtil {

    public static final String XXL_JOB_REGISTRY = "com.xxl.job.admin.core.model.XxlJobRegistry";

    public static final String XXL_JOB_INFO = "com.xxl.job.admin.core.model.XxlJobInfo";



    public static Class getClass(String path) throws ClassNotFoundException {
        return Class.forName(path);
    }
    public static Object newInstance(String path) throws ClassNotFoundException {
        try {
            return Class.forName(path).newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }
    public static void  replaceField(Object target,String fieldName ,Object b){
        Field[] fields = target.getClass().getDeclaredFields();
        Arrays.stream(fields).forEach(field -> {
            field.setAccessible(true);
            if(fieldName.equals(field.getName())){
                try {
                    field.set(target,b);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
            field.setAccessible(false);
            return;
        });
    }

    public static void  setSuperField(Object target,String fieldName ,Object b){
        Field[] fields = target.getClass().getSuperclass().getDeclaredFields();
        Arrays.stream(fields).forEach(field -> {
            field.setAccessible(true);
            if(fieldName.equals(field.getName())){
                try {
                    field.set(target,b);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
            field.setAccessible(false);
            return;
        });
    }

    public static Field  getSuperField(Object target,String fieldName){
        Field[] fields = target.getClass().getSuperclass().getDeclaredFields();
        for (int i=0;i<fields.length;i++){
            Field field = fields[i];
            if(fieldName.equals(field.getName())){
                return field;
            }
        }
        return null;
    }

    public static <T> T  getFieldValue(Object target,String fieldName){
        Field[] fields = target.getClass().getDeclaredFields();
        for (int i=0;i<fields.length;i++){
            Field field = fields[i];
            if(fieldName.equals(field.getName())){
                field.setAccessible(true);
                try {
                    T t= (T) field.get(target);
                    field.setAccessible(false);
                    return t;
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }
}
