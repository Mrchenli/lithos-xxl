# lithos-xxl
demo : https://github.com/Mrchenli/xxl-job
lithos-xxl
step1:
  XXL_JOB_QRTZ_TRIGGER_INFO 这个表的建表语句加一个: `annotation_identity` varchar(255) DEFAULT NULL COMMENT '注解任务注册的唯一标识', 

step2:
  admin resource下面的 spring包下 applicationcontext-base.xml <context:componet-scan>加一个: com.xxl.job.admin.dao,com.ik2k.lithos 扫描路径
  admin resource下面的 spring包下 applicationcontext-xxl-job-admin.xml mapperscan加一个: com.ik2k.lithos.admin.dao 扫描路径

step3:
  admin pom.xml添加依赖
  	<dependency>
			<groupId>com.ik2k.lithos</groupId>
			<artifactId>lithos-admin</artifactId>
			<version>1.0-SNAPSHOT</version>
		</dependency>
    
step4：
  admin下面的XxlJobDynamicSchedule(是一个final的)：
  1.添加属性
  public static LithosAdminBiz lithosAdminBiz;
  2.这个方法里面添加一行代码
     @Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		XxlJobDynamicScheduler.xxlJobLogDao = applicationContext.getBean(XxlJobLogDao.class)
    XxlJobDynamicScheduler.xxlJobInfoDao = applicationContext.getBean(XxlJobInfoDao.class);
    XxlJobDynamicScheduler.xxlJobRegistryDao = applicationContext.getBean(XxlJobRegistryDao.class);
    XxlJobDynamicScheduler.xxlJobGroupDao = applicationContext.getBean(XxlJobGroupDao.class);
    XxlJobDynamicScheduler.adminBiz = applicationContext.getBean(AdminBiz.class);
    //添加一行代码
    XxlJobDynamicScheduler.lithosAdminBiz = applicationContext.getBean(LithosAdminBiz.class);
	}
  3.init方法里面添加一行代码NetComServerFactory.putService(LithosAdminBiz.class, XxlJobDynamicScheduler.lithosAdminBiz);
    public void init() throws Exception {
        // admin registry monitor run
        JobRegistryMonitorHelper.getInstance().start();
        // admin monitor run
        JobFailMonitorHelper.getInstance().start();
        // admin-server(spring-mvc)
        NetComServerFactory.putService(AdminBiz.class, XxlJobDynamicScheduler.adminBiz);
        //添加一行代码
        NetComServerFactory.putService(LithosAdminBiz.class, XxlJobDynamicScheduler.lithosAdminBiz);
        NetComServerFactory.setAccessToken(accessToken);
        // init i18n
        initI18n();
        // valid
        Assert.notNull(scheduler, "quartz scheduler is null");
        logger.info(">>>>>>>>> init xxl-job admin success.");
    }
    4.修改NetComClientProxy 修改成LithosNetComClientProxy
    public static ExecutorBiz getExecutorBiz(String address) throws Exception {
        // valid
        if (address==null || address.trim().length()==0) {
            return null;
        }
        // load-cache
        address = address.trim();
        ExecutorBiz executorBiz = executorBizRepository.get(address);
        if (executorBiz != null) {
            return executorBiz;
        }
        // set-cache  
        //修改NetComClientProxy 修改成LithosNetComClientProxy
        executorBiz = (ExecutorBiz) new LithosNetComClientProxy(ExecutorBiz.class, address, accessToken).getObject();
        executorBizRepository.put(address, executorBiz);
        return executorBiz;
    }

step5:
  spring boot example pom.xml 下面添加如下依赖 可以去掉原来的xxl-core依赖
        <dependency>
            <groupId>com.ik2k.lithos</groupId>
            <artifactId>lithos-core</artifactId>
            <version>1.0-SNAPSHOT</version>
        </dependency>
  XxlJobConfig 类：XxlJobExecutor 替换成 LithosXxlJobExecutor
    public LithosXxlJobExecutor xxlJobExecutor() {
        logger.info(">>>>>>>>>>> xxl-job config init.");
        LithosXxlJobExecutor xxlJobExecutor = new LithosXxlJobExecutor();
        xxlJobExecutor.setAdminAddresses(adminAddresses);
        xxlJobExecutor.setAppName(appName);
        xxlJobExecutor.setIp(ip);
        xxlJobExecutor.setPort(port);
        xxlJobExecutor.setAccessToken(accessToken);
        xxlJobExecutor.setLogPath(logPath);
        xxlJobExecutor.setLogRetentionDays(logRetentionDays);
        return xxlJobExecutor;
    }
  
