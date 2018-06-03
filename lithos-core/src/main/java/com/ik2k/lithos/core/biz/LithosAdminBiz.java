package com.ik2k.lithos.core.biz;


import com.ik2k.lithos.core.model.ExecutorParam;
import com.ik2k.lithos.core.model.LithosRegistryParam;
import com.xxl.job.core.biz.AdminBiz;
import com.xxl.job.core.biz.model.HandleCallbackParam;
import com.xxl.job.core.biz.model.ReturnT;

import java.util.List;

public interface LithosAdminBiz{

    String MAPPING = "/api";

    String ANNOTATION_REGISTRIED="registered";
    String ANNOTATION_REMOVED="removed";

    ReturnT<String> callback(List<HandleCallbackParam> callbackParamList);


    ReturnT<String> registry(LithosRegistryParam lithosRegistryParam);

    ReturnT<String> registryRemove(LithosRegistryParam lithosRegistryParam);

    ReturnT<String> triggerJob(int jobId);

    ReturnT<String> triggerAnnotationJob(ExecutorParam executorParam) throws Exception;


}
