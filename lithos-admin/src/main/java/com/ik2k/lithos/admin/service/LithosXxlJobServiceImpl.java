package com.ik2k.lithos.admin.service;

import com.ik2k.lithos.admin.dao.LithosXxlJobInfoDao;
import com.ik2k.lithos.core.util.Execute;
import com.ik2k.lithos.core.util.ReflectUtil;
import com.ik2k.lithos.core.model.XxlJobInfo;
import com.ik2k.lithos.core.serail.JsonSerializer;
import com.xxl.job.core.biz.model.ReturnT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class LithosXxlJobServiceImpl implements LithosXxlJobService {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private LithosXxlJobInfoDao lithosXxlJobInfoDao;

    @Override
    public List<XxlJobInfo> loadByGroupName(String groupName) {
        return lithosXxlJobInfoDao.loadByGroupName(groupName);
    }

    @Override
    public ReturnT<String> addList(List<XxlJobInfo> jobInfos, int groupId) {
        try{
            Class jobInfoClzz = ReflectUtil.getClass(ReflectUtil.XXL_JOB_INFO);
            Map<XxlJobInfo,String> failJobs = new HashMap<>();
            for (XxlJobInfo job:jobInfos){
                job.setJobGroup(groupId);
                String jobStr = JsonSerializer.toString(job);
                Object jobInfo = JsonSerializer.deserialize(jobStr.getBytes(),jobInfoClzz);
                ReturnT returnT = Execute.newExecute()
                        .target("xxlJobServiceImpl")
                        .method("add",jobInfoClzz)
                        .execute(jobInfo);
                Integer group = ReflectUtil.getFieldValue(jobInfo,"jobGroup");
                Integer id = ReflectUtil.getFieldValue(jobInfo,"id");
                //然后更新插入进去的annotation identity
                lithosXxlJobInfoDao.update(id,job.getAnnotationIdentity());
                if(!job.isOnStart()){
                    boolean flag =Execute.newExecute()
                            .target("xxlJobDynamicScheduler")
                            .method("pauseJob",String.class,String.class)
                            .execute(id+"",group+"");
                    if(flag){
                        returnT.setCode(ReturnT.SUCCESS_CODE);
                    }else{
                        returnT.setCode(ReturnT.FAIL_CODE);
                    }

                }
                if(!(returnT.getCode()==ReturnT.SUCCESS_CODE)){
                    failJobs.put(job,returnT.getMsg());
                }
            }
            return new ReturnT<>(JsonSerializer.toString(failJobs));
        }catch (Exception e){
            logger.error("annotation registry failed e:"+e.getMessage()+e);
        }
       return ReturnT.FAIL;
    }




}
