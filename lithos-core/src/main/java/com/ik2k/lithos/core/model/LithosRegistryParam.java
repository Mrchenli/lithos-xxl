package com.ik2k.lithos.core.model;

import com.xxl.job.core.biz.model.RegistryParam;

import java.io.Serializable;
import java.util.List;

/**
 * Created by xuxueli on 2017-05-10 20:22:42
 */
public class LithosRegistryParam extends RegistryParam implements Serializable {

    private List<XxlJobInfo> xxlJobs;

    public List<XxlJobInfo> getXxlJobs() {
        return xxlJobs;
    }

    public void setXxlJobs(List<XxlJobInfo> xxlJobs) {
        this.xxlJobs = xxlJobs;
    }

    public LithosRegistryParam() {

    }

    public LithosRegistryParam(String registGroup, String registryKey, String registryValue) {
        super(registGroup, registryKey, registryValue);
    }

    public LithosRegistryParam(String registGroup, String registryKey, String registryValue, List<XxlJobInfo> xxlJobs) {
        super(registGroup, registryKey, registryValue);
        this.xxlJobs = xxlJobs;
    }

}
