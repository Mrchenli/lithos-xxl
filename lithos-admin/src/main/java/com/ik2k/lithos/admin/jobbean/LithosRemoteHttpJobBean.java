package com.ik2k.lithos.admin.jobbean;

import com.ik2k.lithos.core.util.Execute;
import com.ik2k.lithos.core.model.ExecutorParam;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.quartz.QuartzJobBean;

/**
 * http job bean
 * “@DisallowConcurrentExecution” diable concurrent, thread size can not be only one, better given more
 * @author xuxueli 2015-12-17 18:20:34
 */
//@DisallowConcurrentExecution
public class LithosRemoteHttpJobBean extends QuartzJobBean {

	private static Logger logger = LoggerFactory.getLogger(LithosRemoteHttpJobBean.class);

	public static ThreadLocal<JobDataMap> jobDataMapThreadLocal = new ThreadLocal<>();


	@Override
	protected void executeInternal(JobExecutionContext context)
			throws JobExecutionException {

		// load jobId
		JobKey jobKey = context.getTrigger().getJobKey();
		Integer jobId = Integer.valueOf(jobKey.getName());
		Trigger trigger = context.getTrigger();
		JobDataMap jobDataMap = trigger.getJobDataMap();
		jobDataMap.remove(ExecutorParam.ANNOTATION_IDENTITY);
		jobDataMapThreadLocal.set(jobDataMap);
		// trigger
		try {
			Execute.newExecute()
					.targetForName("com.xxl.job.admin.core.trigger.XxlJobTrigger")
					.method("trigger",int.class)
					.execute(jobId);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}