package com.ik2k.lithos.core.executor;

import com.ik2k.lithos.core.biz.LithosAdminBiz;
import com.ik2k.lithos.core.jetty.LithosJettyServer;
import com.ik2k.lithos.core.thread.LithosExecutorRegistryThread;
import com.ik2k.lithos.core.util.Execute;
import com.ik2k.lithos.core.util.ReflectUtil;
import com.xxl.job.core.biz.AdminBiz;
import com.xxl.job.core.executor.XxlJobExecutor;
import com.xxl.job.core.log.XxlJobFileAppender;
import com.xxl.job.core.rpc.netcom.NetComClientProxy;
import com.xxl.job.core.thread.JobLogFileCleanThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Created by xuxueli on 2016/3/2 21:14.
 */
public class LithosXxlJobExecutor extends XxlJobExecutor implements ApplicationContextAware {
    private static final Logger logger = LoggerFactory.getLogger(LithosXxlJobExecutor.class);

    // ---------------------- param ----------------------
    private String adminAddresses;
    private String appName;
    private String ip;
    private int port;
    private String accessToken;
    private String logPath;
    private int logRetentionDays;

    public void setAdminAddresses(String adminAddresses) {
        this.adminAddresses = adminAddresses;
    }
    public void setAppName(String appName) {
        this.appName = appName;
    }
    public void setIp(String ip) {
        this.ip = ip;
    }
    public void setPort(int port) {
        this.port = port;
    }
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
    public void setLogPath(String logPath) {
        this.logPath = logPath;
    }
    public void setLogRetentionDays(int logRetentionDays) {
        this.logRetentionDays = logRetentionDays;
    }

    // ---------------------- applicationContext ----------------------
    private static ApplicationContext applicationContext;
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }


    // ---------------------- start + stop ----------------------
    public void start() throws Exception {

        ReflectUtil.setSuperField(this,"adminAddresses",adminAddresses);
        ReflectUtil.setSuperField(this,"appName",appName);
        ReflectUtil.setSuperField(this,"ip",ip);
        ReflectUtil.setSuperField(this,"port",port);
        ReflectUtil.setSuperField(this,"accessToken",accessToken);
        ReflectUtil.setSuperField(this,"logPath",logPath);
        ReflectUtil.setSuperField(this,"logRetentionDays",logRetentionDays);
        Execute.newExecute().target(this).superMethod("initAdminBizList",String.class,String.class)
                .execute(adminAddresses,accessToken);
        // init admin-client
        initAdminBizList(adminAddresses, accessToken);

        Execute.newExecute().target(this).superMethod("initJobHandlerRepository",ApplicationContext.class)
                .execute(applicationContext);
        // init logpath
        XxlJobFileAppender.initLogPath(logPath);
        // init executor-server
        initExecutorServer(port, ip, appName, accessToken);
        // init JobLogFileCleanThread
        JobLogFileCleanThread.getInstance().start(logRetentionDays);
    }
    public void destroy(){
       super.destroy();
    }
    // ---------------------- LithosRegistryThread admin-client ----------------------
    private static void initAdminBizList(String adminAddresses, String accessToken) throws Exception {
        if (adminAddresses!=null && adminAddresses.trim().length()>0) {
            for (String address: adminAddresses.trim().split(",")) {
                if (address!=null && address.trim().length()>0) {
                    String addressUrl = address.concat(AdminBiz.MAPPING);
                    LithosAdminBiz adminBiz = (LithosAdminBiz) new NetComClientProxy(LithosAdminBiz.class, addressUrl, accessToken).getObject();
                    LithosExecutorRegistryThread.getAdminBizList().add(adminBiz);
                }
            }
        }
    }

    private void initExecutorServer(int port, String ip, String appName, String accessToken) throws Exception {
        Field field = ReflectUtil.getSuperField(this,"serverFactory");
        field.setAccessible(true);
        Object target = field.get(this);
        ReflectUtil.replaceField(target,"server",new LithosJettyServer());
        field.setAccessible(false);
        Execute.newExecute()
                .target(this)
                .superMethod("initExecutorServer",int.class,String.class, String.class,String.class)
                .execute(port,ip,appName,accessToken);

    }

}
