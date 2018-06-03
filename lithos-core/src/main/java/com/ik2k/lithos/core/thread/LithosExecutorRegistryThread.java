package com.ik2k.lithos.core.thread;

import com.ik2k.lithos.core.biz.LithosAdminBiz;
import com.ik2k.lithos.core.model.LithosRegistryParam;
import com.ik2k.lithos.core.model.XxlJobInfo;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.enums.RegistryConfig;
import com.xxl.job.core.util.IpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by xuxueli on 17/3/2.
 */
public class LithosExecutorRegistryThread extends Thread {
    private static Logger logger = LoggerFactory.getLogger(LithosExecutorRegistryThread.class);

    private static List<LithosAdminBiz> adminBizList = new ArrayList<>(0);

    private static final List<XxlJobInfo> xxlJobInfos =new ArrayList<>(0);

    public static List<LithosAdminBiz> getAdminBizList(){
        return adminBizList;
    }

    public static List<XxlJobInfo> getXxlJobInfos(){
        return xxlJobInfos;
    }

    private static LithosExecutorRegistryThread instance = new LithosExecutorRegistryThread();
    public static LithosExecutorRegistryThread getInstance(){
        return instance;
    }

    private Thread registryThread;
    private volatile boolean toStop = false;
    //添加执行器自动启动的时候通过注解来注册任务
    private volatile boolean finshXxlJobs = false;

    public void start(final int port, final String ip, final String appName){

        // valid
        if (appName==null || appName.trim().length()==0) {
            logger.warn(">>>>>>>>>>> xxl-job, executor registry config fail, appName is null.");
            return;
        }
        if (adminBizList == null) {
            logger.warn(">>>>>>>>>>> xxl-job, executor registry config fail, adminAddresses is null.");
            return;
        }

        // executor address (generate addredd = ip:port)
        final String executorAddress;
        if (ip != null && ip.trim().length()>0) {
            executorAddress = ip.trim().concat(":").concat(String.valueOf(port));
        } else {
            executorAddress = IpUtil.getIpPort(port);
        }

        registryThread = new Thread(new Runnable() {
            @Override
            public void run() {

                // registry
                while (!toStop) {
                    try {
                        LithosRegistryParam lithosRegistryParam = new LithosRegistryParam(RegistryConfig.RegistType.EXECUTOR.name(), appName, executorAddress);
                        //todo
                        if(!finshXxlJobs){//如果执行器启动的时候没有完成注解任务的注册就完成注册
                            lithosRegistryParam.setXxlJobs(xxlJobInfos);
                        }
                        for (LithosAdminBiz adminBiz:adminBizList) {
                            try {
                                ReturnT<String> registryResult = adminBiz.registry(lithosRegistryParam);
                                if (registryResult!=null && ReturnT.SUCCESS_CODE == registryResult.getCode()) {
                                    registryResult = ReturnT.SUCCESS;
                                    finshXxlJobs = true;
                                    logger.info(">>>>>>>>>>> xxl-job registry success, lithosRegistryParam:{}, registryResult:{}", new Object[]{lithosRegistryParam, registryResult});
                                    break;
                                } else {
                                    logger.info(">>>>>>>>>>> xxl-job registry fail, lithosRegistryParam:{}, registryResult:{}", new Object[]{lithosRegistryParam, registryResult});
                                }
                            } catch (Exception e) {
                                logger.info(">>>>>>>>>>> xxl-job registry error, lithosRegistryParam:{}", lithosRegistryParam, e);
                            }

                        }
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    }

                    try {
                        TimeUnit.SECONDS.sleep(RegistryConfig.BEAT_TIMEOUT);
                    } catch (InterruptedException e) {
                        logger.error(e.getMessage(), e);
                    }
                }

                // registry remove
                try {
                    LithosRegistryParam lithosRegistryParam = new LithosRegistryParam(RegistryConfig.RegistType.EXECUTOR.name(), appName, executorAddress);
                    for (LithosAdminBiz adminBiz:adminBizList) {
                        try {
                            //todo
                            if(xxlJobInfos.size()>0){//如果执行器退出了 就检查下注解任务有没有其他可用的执行器 如果没有 就暂停任务的调度
                                lithosRegistryParam.setXxlJobs(xxlJobInfos);
                            }
                            ReturnT<String> registryResult = adminBiz.registryRemove(lithosRegistryParam);
                            if (registryResult!=null && ReturnT.SUCCESS_CODE == registryResult.getCode()) {
                                registryResult = ReturnT.SUCCESS;
                                logger.info(">>>>>>>>>>> xxl-job registry-remove success, lithosRegistryParam:{}, registryResult:{}", new Object[]{lithosRegistryParam, registryResult});
                                break;
                            } else {
                                logger.info(">>>>>>>>>>> xxl-job registry-remove fail, lithosRegistryParam:{}, registryResult:{}", new Object[]{lithosRegistryParam, registryResult});
                            }
                        } catch (Exception e) {
                            logger.info(">>>>>>>>>>> xxl-job registry-remove error, lithosRegistryParam:{}", lithosRegistryParam, e);
                        }

                    }
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
                logger.info(">>>>>>>>>>> xxl-job, executor registry thread destory.");

            }
        });
        registryThread.setDaemon(true);
        registryThread.start();
    }

    public void toStop() {
        toStop = true;
        // interrupt and wait
        registryThread.interrupt();
        try {
            registryThread.join();
        } catch (InterruptedException e) {
            logger.error(e.getMessage(), e);
        }
    }

}
