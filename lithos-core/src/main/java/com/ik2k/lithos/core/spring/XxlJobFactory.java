package com.ik2k.lithos.core.spring;


import com.ik2k.lithos.core.biz.LithosAdminBiz;
import com.ik2k.lithos.core.model.ExecutorParam;
import com.ik2k.lithos.core.thread.LithosExecutorRegistryThread;
import com.xxl.job.core.biz.AdminBiz;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.executor.XxlJobExecutor;
import com.xxl.job.core.thread.ExecutorRegistryThread;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;

public class XxlJobFactory implements InvocationHandler {

    private static final XxlJobFactory xxlJobFactory = new XxlJobFactory();

    public static <T> T newProxy(Class<T> interfaceType){
        return (T) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),new Class[]{interfaceType},xxlJobFactory);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        List<LithosAdminBiz> adminBizs = LithosExecutorRegistryThread.getAdminBizList();
        ReturnT ret=null;
        for (LithosAdminBiz adminBiz : adminBizs){
            String identity = method.getDeclaringClass().getCanonicalName()+"."+method.getName();
            ExecutorParam executorParam =null;
            if(args.length==1){
                 executorParam = (ExecutorParam) args[0];
                 executorParam.getParam().put(ExecutorParam.ANNOTATION_IDENTITY,identity);

                 ret = ((LithosAdminBiz) adminBiz).triggerAnnotationJob(executorParam);
                if(ReturnT.SUCCESS_CODE==ret.getCode()){
                    break;
                }
            }else{
                throw new RuntimeException("xxlJob proxy 参数必须为ExecutorParam");
            }
        }
        return ret;
    }
}
