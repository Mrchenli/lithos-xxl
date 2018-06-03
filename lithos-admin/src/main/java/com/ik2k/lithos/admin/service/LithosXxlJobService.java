package com.ik2k.lithos.admin.service;

import com.ik2k.lithos.core.model.XxlJobInfo;
import com.xxl.job.core.biz.model.ReturnT;

import java.util.List;

public interface LithosXxlJobService {

    List<XxlJobInfo> loadByGroupName(String groupName);


    ReturnT<String> addList(List<XxlJobInfo> jobInfos,int groupId);

}
