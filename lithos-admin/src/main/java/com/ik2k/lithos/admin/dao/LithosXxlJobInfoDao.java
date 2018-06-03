package com.ik2k.lithos.admin.dao;

import com.ik2k.lithos.core.model.XxlJobInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface LithosXxlJobInfoDao {

    @Select("select id from xxl_job_qrtz_trigger_info where annotation_identity = #{annotationIdentity}")
    int loadIdByAnnotationIdentity(@Param("annotationIdentity") String annotationIdentity);

    @Select("select " +
            "t.id id," +
            "t.job_group jobGroup," +
            "t.job_cron jobCron," +
            "t.job_desc jobDesc," +
            "t.add_time addTime," +
            "t.update_time updateTime," +
            "t.author author," +
            "t.alarm_email alarmEmail," +
            "t.executor_route_strategy executorRouteStrategy," +
            "t.executor_handler executorHandler," +
            "t.executor_param executorParam," +
            "t.executor_block_strategy executorBlockStrategy," +
            "t.executor_fail_strategy executorFailStrategy," +
            "t.glue_type glueType," +
            "t.glue_source glueSource," +
            "t.glue_remark glueRemark," +
            "t.glue_updatetime glueUpdatetime," +
            "t.child_jobid childJobid," +
            "t.annotation_identity annotationIdentity " +
            "from xxl_job_qrtz_trigger_info t ,xxl_job_qrtz_trigger_group g where t.job_group = g.id and g.app_name =#{groupName} and t.annotation_identity is not null")
    List<XxlJobInfo> loadByGroupName(@Param("groupName") String  groupName);

    @Update("update xxl_job_qrtz_trigger_info t set t.annotation_identity=#{annoIdt} where t.id=#{id}")
    int update(@Param("id") Integer id,@Param("annoIdt") String annotationIdentity);

}
