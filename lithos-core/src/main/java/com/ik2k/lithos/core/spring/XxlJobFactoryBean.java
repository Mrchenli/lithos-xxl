package com.ik2k.lithos.core.spring;

import org.springframework.beans.factory.FactoryBean;

public class XxlJobFactoryBean implements FactoryBean {

    private final Class<?> interfaceType;

    public XxlJobFactoryBean(Class<?> interfaceType) {
        this.interfaceType = interfaceType;
    }

    @Override
    public Object getObject() throws Exception {
        return XxlJobFactory.newProxy(interfaceType);
    }

    @Override
    public Class<?> getObjectType() {
        return interfaceType;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}
