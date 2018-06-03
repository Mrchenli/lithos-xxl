package com.ik2k.lithos.core.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;

public class Execute {

    private Object target;
    private Method method;
    private Object result;
    private Field field;
    private Object value;


    public static Execute newExecute(){
        return new Execute();
    }

    public Execute target(String beanName){
        this.target = AdminSpringContext.getBean(beanName);
        return this;
    }

    public Execute target(Object target){
        this.target = target;
        return this;
    }

    public Execute targetForName(String path) throws Exception{
        this.target = Class.forName(path).newInstance();
        return this;
    }

    public Execute method(String methodName,Class<?>... paramType) throws Exception {
        method=AdminSpringContext.getMethod(target,methodName,paramType);
        return this;
    }

    public Execute superMethod(String methodName,Class<?>... paramType) throws Exception {
        method=target.getClass().getSuperclass().getDeclaredMethod(methodName,paramType);
        return this;
    }

    public Execute field(String fieldName) throws Exception {
        Arrays.stream(target.getClass().getDeclaredFields()).forEach(field1 -> {
            if(field1.getName().equals(fieldName)){
                this.field = field1;
            }
        });
        return this;
    }

    public <T> T value() throws Exception {
       return (T) field.get(target);
    }


    public  <T> T execute(Object... args) throws Exception{
        method.setAccessible(true);
        T  t=(T) method.invoke(target,args);
        method.setAccessible(false);
        return t;
    }

    public Object getTarget() {
        return target;
    }

    public void setTarget(Object target) {
        this.target = target;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public Field getField() {
        return field;
    }

    public void setField(Field field) {
        this.field = field;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }
}
