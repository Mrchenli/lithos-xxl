package com.ik2k.lithos.admin.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
@Mapper
public interface LithosXxlJobRegistryDao {

    @Select("SELECT t.* FROM XXL_JOB_QRTZ_TRIGGER_REGISTRY AS t WHERE t.registry_group =#{registryGroup} and t.registry_key = #{registryKey} AND t.update_time <![CDATA[ > ]]> DATE_ADD(NOW(),INTERVAL -#{timeout} SECOND)")
    List findByGroupAndRkey(@Param("registryGroup") String registryGroup, @Param("registryKey") String registryKey);

}
