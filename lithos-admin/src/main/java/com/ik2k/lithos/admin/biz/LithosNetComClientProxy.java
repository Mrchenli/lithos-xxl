package com.ik2k.lithos.admin.biz;

import com.ik2k.lithos.admin.jobbean.LithosRemoteHttpJobBean;
import com.ik2k.lithos.core.model.ExecutorParam;
import com.ik2k.lithos.core.serail.JsonSerializer;
import com.xxl.job.core.biz.model.TriggerParam;
import com.xxl.job.core.rpc.codec.RpcRequest;
import com.xxl.job.core.rpc.codec.RpcResponse;
import com.xxl.job.core.rpc.netcom.jetty.client.JettyClient;
import org.quartz.JobDataMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;

import java.lang.reflect.Proxy;

public class LithosNetComClientProxy implements FactoryBean<Object> {

    private Logger logger= LoggerFactory.getLogger(LithosNetComClientProxy.class);

    private Class<?> iface;
    private String serverAddress;
    private String accessToken;
    private JettyClient client = new JettyClient();

    public LithosNetComClientProxy(Class<?> iface, String serverAddress, String accessToken) {
        this.iface = iface;
        this.serverAddress = serverAddress;
        this.accessToken = accessToken;
    }

    @Override
    public Object getObject() throws Exception {
        return Proxy.newProxyInstance(Thread.currentThread()
                        .getContextClassLoader(), new Class[] { iface },
                (proxy, method, args) -> {

                    // filter method like "Object.toString()"
                    if (Object.class.getName().equals(method.getDeclaringClass().getName())) {
                        logger.error(">>>>>>>>>>> xxl-rpc proxy class-method not support [{}.{}]", method.getDeclaringClass().getName(), method.getName());
                        throw new RuntimeException("xxl-rpc proxy class-method not support");
                    }
                    // request
                    RpcRequest request = new RpcRequest();
                    request.setServerAddress(serverAddress);
                    request.setCreateMillisTime(System.currentTimeMillis());
                    request.setAccessToken(accessToken);
                    request.setClassName(method.getDeclaringClass().getName());
                    request.setMethodName(method.getName());
                    request.setParameterTypes(method.getParameterTypes());
                    JobDataMap jobDataMap = LithosRemoteHttpJobBean.jobDataMapThreadLocal.get();
                    if(jobDataMap!=null){
                        TriggerParam triggerParam = (TriggerParam) args[0];
                        if(jobDataMap==null){
                            jobDataMap = new JobDataMap();
                        }
                        jobDataMap.put(ExecutorParam.DB_EXECUTOR_PARAM,triggerParam.getExecutorParams());
                        String param = JsonSerializer.toString(jobDataMap);
                        triggerParam.setExecutorParams(param);
                        args[0]=triggerParam;
                    }
                    request.setParameters(args);

                    // send
                    RpcResponse response = client.send(request);

                    // valid response
                    if (response == null) {
                        throw new Exception("Network request fail, response not found.");
                    }
                    if (response.isError()) {
                        throw new RuntimeException(response.getError());
                    } else {
                        return response.getResult();
                    }

                });
    }

    @Override
    public Class<?> getObjectType() {
        return iface;
    }
    @Override
    public boolean isSingleton() {
        return false;
    }

}
