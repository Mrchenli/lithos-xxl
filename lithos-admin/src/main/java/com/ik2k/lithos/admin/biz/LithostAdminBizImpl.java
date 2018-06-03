package com.ik2k.lithos.admin.biz;

import com.ik2k.lithos.admin.dao.LithosXxlJobGroupDao;
import com.ik2k.lithos.admin.dao.LithosXxlJobInfoDao;
import com.ik2k.lithos.admin.dao.LithosXxlJobRegistryDao;
import com.ik2k.lithos.admin.jobbean.LithosRemoteHttpJobBean;
import com.ik2k.lithos.admin.service.LithosXxlJobService;
import com.ik2k.lithos.core.util.Execute;
import com.ik2k.lithos.core.biz.LithosAdminBiz;
import com.ik2k.lithos.core.enums.ExecutorType;
import com.ik2k.lithos.core.model.ExecutorParam;
import com.ik2k.lithos.core.model.LithosRegistryParam;
import com.ik2k.lithos.core.model.XxlJobInfo;
import com.ik2k.lithos.core.serail.JsonSerializer;
import com.ik2k.lithos.core.util.ReflectUtil;
import com.xxl.job.core.biz.AdminBiz;
import com.xxl.job.core.biz.model.HandleCallbackParam;
import com.xxl.job.core.biz.model.ReturnT;
import org.quartz.*;
import org.quartz.impl.JobDetailImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
public class LithostAdminBizImpl implements LithosAdminBiz{

    private static Logger logger = LoggerFactory.getLogger(LithosAdminBiz.class);

    @Autowired
    private AdminBiz adminBiz;
    @Resource(name = "quartzScheduler")
    private Scheduler scheduler;

    @Autowired
    private LithosXxlJobService lithosXxlJobService;
    @Autowired
    private LithosXxlJobGroupDao lithosXxlJobGroupDao;
    @Autowired
    private LithosXxlJobRegistryDao lithosXxlJobRegistryDao;
    @Autowired
    private LithosXxlJobInfoDao lithosXxlJobInfoDao;

    @Override
    public ReturnT<String> callback(List<HandleCallbackParam> callbackParamList) {
        return adminBiz.callback(callbackParamList);
    }

    @Override
    public ReturnT<String> registry(LithosRegistryParam lithosRegistryParam) {
        ReturnT returnT = adminBiz.registry(lithosRegistryParam);
        if(returnT.getCode()==returnT.SUCCESS_CODE){
            if(lithosRegistryParam.getXxlJobs()!=null&& lithosRegistryParam.getXxlJobs().size()>0){//先注册了任务了后才开始 注册执行器
                //1.先查询出这个group所对应的所有注解注册的任务
                String groupName = lithosRegistryParam.getRegistryKey();
                int groupId = lithosXxlJobGroupDao.loadIdByName(groupName);
                List<XxlJobInfo> jobInfos = lithosXxlJobService.loadByGroupName(groupName);//从数据库查询出来的
                List<XxlJobInfo> xxlJobs = lithosRegistryParam.getXxlJobs();//从注解读取来的
                List<XxlJobInfo> left = new ArrayList<>();
                for (XxlJobInfo jobinfo : xxlJobs){
                    boolean exist = false;
                    for (XxlJobInfo job:jobInfos){
                        if(jobinfo.getAnnotationIdentity().equals(job.getAnnotationIdentity())){//存在了
                            exist = true;
                            break;
                        }
                    }
                    if(!exist){
                        left.add(jobinfo);
                    }
                }
                lithosXxlJobService.addList(left,groupId);
                returnT.setContent(LithosAdminBiz.ANNOTATION_REGISTRIED);
            }
        }
        return returnT;
    }

    @Override
    public ReturnT<String> registryRemove(LithosRegistryParam lithosRegistryParam) {
        ReturnT returnT = adminBiz.registryRemove(lithosRegistryParam);
        List registries = lithosXxlJobRegistryDao.findByGroupAndRkey(lithosRegistryParam.getRegistGroup(), lithosRegistryParam.getRegistryKey());
        if(returnT.getCode()==ReturnT.SUCCESS_CODE){
            if(registries.size()==0){//如果没有执行器了就停掉所有的任务
                List<XxlJobInfo> list = lithosRegistryParam.getXxlJobs();
                for (XxlJobInfo jobInfo:list){
                    int id = lithosXxlJobInfoDao.loadIdByAnnotationIdentity(jobInfo.getAnnotationIdentity());
                    try {
                        Execute.newExecute()
                                .target("xxlJobServiceImpl")
                                .method("pause",int.class)
                                .execute(id);
                    } catch (Exception e) {
                        logger.error("xxl job info pause exception e:"+e);
                    }
                }
                returnT.setContent(LithosAdminBiz.ANNOTATION_REMOVED);
            }
        }
        return returnT;
    }

    @Override
    public ReturnT<String> triggerJob(int jobId) {
       return adminBiz.triggerJob(jobId);
    }

    public ReturnT<String> triggerAnnotationJob(ExecutorParam executorParam) throws Exception {
        String annotationIdentity = (String) executorParam.getParam().get(ExecutorParam.ANNOTATION_IDENTITY);
        int id = lithosXxlJobInfoDao.loadIdByAnnotationIdentity(annotationIdentity);
        if(ExecutorType.PAUSE.equals(executorParam.getExecutorType())){
            try {
                Execute.newExecute()
                        .target("xxlJobServiceImpl")
                        .method("pause",int.class)
                        .execute(id);
                return ReturnT.SUCCESS;
            } catch (Exception e) {
                logger.error("annotation pause job failed e :"+e);
                return ReturnT.FAIL;
            }
        }else if(ExecutorType.TRIGGER_ONCE.equals(executorParam.getExecutorType())){
            Object xji = Execute.newExecute().target("xxlJobInfoDao").method("loadById",int.class).execute(id);
            String jobStr=JsonSerializer.toString(xji);
            XxlJobInfo xxlJobInfo=JsonSerializer.deserialize(jobStr.getBytes(),XxlJobInfo.class);
            if (xxlJobInfo == null) {
                return ReturnT.FAIL;
            }
            String group = String.valueOf(xxlJobInfo.getJobGroup());
            String name = String.valueOf(xxlJobInfo.getId());
            try {
                JobKey jobKey = new JobKey(name, group);
                JobDetail jobDetail = scheduler.getJobDetail(jobKey);
                ReflectUtil.replaceField(jobDetail,"jobClass", LithosRemoteHttpJobBean.class);
                if(jobDetail instanceof JobDetailImpl){
                    JobDetailImpl jb = (JobDetailImpl) jobDetail;
                    jb.setDurability(true);
                    scheduler.addJob(jobDetail,true);//替换掉最开始注册的jobDetail(兼容)
                }
                TriggerKey triggerKey = TriggerKey.triggerKey(name, group);
                if (scheduler.checkExists(triggerKey)) {
                    JobDataMap jobDataMap = new JobDataMap(executorParam.getParam());
                    scheduler.triggerJob(jobKey,jobDataMap);
                    logger.info(">>>>>>>>>>> runJob success, jobKey:{}", jobKey);
                    return ReturnT.SUCCESS;
                } else {
                    logger.info(">>>>>>>>>>> runJob fail, jobKey:{}", jobKey);
                    return ReturnT.FAIL;
                }
            } catch (SchedulerException e) {
                logger.error(e.getMessage(), e);
                return new ReturnT<String>(ReturnT.FAIL_CODE, e.getMessage());
            }
        }
        return new ReturnT<>(ReturnT.FAIL_CODE,"no such annotation trigger operation");
    }
}
