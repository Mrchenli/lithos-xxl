package com.ik2k.lithos.admin.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
@Mapper
public interface LithosXxlJobGroupDao {

    @Select("select id from xxl_job_qrtz_trigger_group where app_name=#{appName}")
    int loadIdByName(@Param("appName") String appName);

}
