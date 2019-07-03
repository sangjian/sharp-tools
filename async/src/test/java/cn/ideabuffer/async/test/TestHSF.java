package cn.ideabuffer.async.test;



/**
 * @author sangjian.sj
 * @date 2019/07/03
 */

public class TestHSF {

    //static{
    //    // Sar包自动下载、解压缩到Temp目录下的 2_7_151228/taobao-hsf.sar
    //    // 比如/home/admin/appname/ 会下载到/home/admin/appname/2_7_151228/taobao-hsf.sar/
    //    // 如果下载不可用，可以手动放置sar包。
    //    HSFEasyStarter.start("/home/admin/sharp-tools", "2019-04-stable");
    //    // 或者是 ServiceFactory.getInstance();
    //}
    //
    //private SettleGroupCheckService settleGroupCheckService;
    //
    //private AsyncTemplate asyncTemplate;
    //
    //@Before
    //public void before() {
    //    String springResourcePath = "spring-context.xml";
    //    ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(springResourcePath);
    //    settleGroupCheckService = (SettleGroupCheckService) ctx.getBean("settleGroupCheckService");
    //
    //    // 等待服务的地址，非必须。
    //    // 如果不加这句，调得太快，可能会出现找不到地址的异常。简单的Sleep一会儿，3秒左右。
    //    try {
    //        ServiceUtil.waitServiceReady(settleGroupCheckService);
    //    } catch (Exception e) {
    //
    //    }
    //    asyncTemplate = ctx.getBean("asyncTemplate", AsyncTemplate.class);
    //}
    //
    //
    //@Test
    //public void testHSFAsync() {
    //    SettleQuery query = new SettleQuery();
    //    query.setPage(1);
    //    query.setPageSize(200);
    //    System.out.println(settleGroupCheckService.getClass().getName());
    //    System.out.println("start...");
    //    long start = System.currentTimeMillis();
    //    Result<List<SettleGroupCheckDTO>> result = asyncTemplate.submit(() -> settleGroupCheckService.query(query), Result.class);
    //    System.out.println("cost:" + (System.currentTimeMillis() - start));
    //    System.out.println(result.getData());
    //}

}
