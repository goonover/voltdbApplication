package purge;

import mssql2voltdb.utils.SQLCommandUtils;
import org.apache.log4j.Logger;
import org.voltdb.client.Client;
import org.voltdb.client.ClientResponse;
import org.voltdb.client.ProcedureCallback;
import purge.webserver.PurgeServer;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static java.nio.file.StandardOpenOption.APPEND;

/**
 * 整个服务的串联点，把规则扫描{@link RulesMaintainer},Voltdb资源监视器{@link VoltdbMonitor},
 * 负责与客户端交互的{@link PurgeServer}统一配置，控制开关以及作为中间人提供资源访问
 * 用于方便定义规则自动清除voltdb冗余数据，以免内存耗尽中止服务
 * Created by swqsh on 2017/6/16.
 */
public class PurgeService {

    private Logger logger= Logger.getLogger(PurgeService.class);

    //voltdb服务器列表
    private String servers;

    //开始清理冗余数据的阈值
    private double threshold=0.5;

    //扫描操作列表的频率
    private int updateFrequency=30;

    //进行清理操作的频率
    private int cleanFrequency=60;

    //voltdb的管理员端口
    private int voltadminPort=21211;

    //规则列表的路径
    private String ruleFilePath;

    //监听客户端的服务器
    private PurgeServer webServer;

    private Properties purgeServiceProperties=new Properties();

    //是否开启webServer监听服务器的选项
    private boolean enableWeb=true;

    //在当前路径下创建purgeroot目录，该目录下保存着跟该PurgeService相关的配置文件和规则列表
    private final String currentPath=System.getProperty("user.dir");
    //private final String purgeroot=currentPath+"//purgeroot";
    private final String purgeroot="purgeroot";
    private final String deployment="deployment.properties";
    private final String rules="rules";

    ScheduledExecutorService scheduledExecutorService= Executors.newScheduledThreadPool(2);

    private Client client;
    private VoltdbMonitor voltdbMonitor;
    private RulesMaintainer rulesMaintainer;

    public PurgeService(){
        Path deploymentPath=Paths.get(currentPath,purgeroot,deployment);
        Properties properties=new Properties();
        if(Files.exists(deploymentPath)){
            try {
                properties.load(Files.newInputStream(deploymentPath));
            } catch (IOException e) {
                System.out.println("service can not load properties, exit!");
                System.exit(-1);
            }
        }
        configure(properties);
        init();

    }

    public PurgeService(String propertiesPath){
        try {
            FileInputStream inputStream=new FileInputStream(propertiesPath);
            Properties properties=new Properties();
            properties.load(inputStream);
            configure(properties);
            init();
        }  catch (IOException e) {
            e.printStackTrace();
        }

    }

    public PurgeService(Properties properties){
        configure(properties);
        init();
    }

    //初始化voltdb连接、voltdb资源监视器、规则维护
    private void init() {
        try {
            client= SQLCommandUtils.getClient(this.servers,voltadminPort);
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.voltdbMonitor=new VoltdbMonitor(this,client,threshold);
        this.rulesMaintainer=new RulesMaintainer(this.ruleFilePath);
        if(enableWeb){
            webServer=new PurgeServer(this);
        }
    }

    /**
     * PurgeService开始服务
     */
    public void start(){
        scheduledExecutorService.scheduleAtFixedRate(rulesMaintainer,0,updateFrequency, TimeUnit.SECONDS);
        //scheduledExecutorService.scheduleAtFixedRate(voltdbMonitor,3,cleanFrequency,TimeUnit.SECONDS);
        if(webServer!=null)
            webServer.start();
    }

    /**
     * 关闭服务
     * TODO:检测如果网站服务器没有关闭，则关闭网站服务器
     */
    public void shutdown(){
        logger.info("PurgeService is going to shutdown!");
        scheduledExecutorService.shutdown();
        rulesMaintainer.shutdown();
    }

    public List<String> getRules(){
        return rulesMaintainer.getOperations();
    }

    /**
     * 删除规则
     * @param rule
     * @return
     */
    public boolean removeRule(String rule){
        return rulesMaintainer.removeRule(rule);
    }

    public boolean removeRules(List<String> rulesToBeRemoved){
        return rulesMaintainer.removeRules(rulesToBeRemoved);
    }

    public boolean addRules(List<String> rulesToBeAdded){
        return rulesMaintainer.addRules(rulesToBeAdded);
    }


    private void configure(Properties properties){
        String servers=properties.getProperty("servers");
        String threshold=properties.getProperty("threshold");
        String updateFrequency=properties.getProperty("updateFrequency");
        String cleanFrequency=properties.getProperty("cleanFrequency");
        String voltadminPort=properties.getProperty("voltadminPort");
        String ruleFilePath=properties.getProperty("ruleFilePath");

        assert (servers!=null);
        this.servers=servers;
        if(threshold!=null)
            this.threshold=Double.parseDouble(threshold);

        if(updateFrequency!=null)
            this.updateFrequency=Integer.parseInt(updateFrequency);

        if(cleanFrequency!=null)
            this.cleanFrequency=Integer.parseInt(cleanFrequency);

        if(voltadminPort!=null)
            this.voltadminPort=Integer.parseInt(voltadminPort);

        this.ruleFilePath=ruleFilePath;

        //补充完用户配置可能省略掉的部分后，purgeServiceProperties才是实际的配置
        purgeServiceProperties.put("servers",this.servers);
        purgeServiceProperties.put("threshold",String.valueOf(this.threshold));
        purgeServiceProperties.put("updateFrequency",String.valueOf(this.updateFrequency));
        purgeServiceProperties.put("cleanFrequency",String.valueOf(this.cleanFrequency));
        purgeServiceProperties.put("voltadminPort",String.valueOf(this.voltadminPort));

        try {
            fileCheck();
        }catch (IOException e){
            System.out.println("can not create root or files, please start service " +
                    "at the proper location, purge service is going to shutdown!");
            System.exit(-1);
        }

        this.ruleFilePath=Paths.get(currentPath,purgeroot,rules).toString();
    }

    /**
     * 执行规则列表中的所有语句
     * TODO://目前只支持adhoc的sql语句，以后将会支持调用其它存储过程，处理集群已暂停的情况
     */
    public void purge(){
        System.out.println(rulesMaintainer.getOperations());
       for(String command:rulesMaintainer.getOperations()){
            try {
                client.callProcedure(new PurgeCallback(command),"@AdHoc",command);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 检查配置目录以及文件是否存在，如不存在，则创建文件及目录，如果在操作文件时出现异常，
     * 程序直接退出
     */
    private void fileCheck() throws IOException{
        Path purgerootPath= Paths.get(currentPath,purgeroot);
        if(Files.exists(purgerootPath)&&!Files.isDirectory(purgerootPath)) {
            System.out.println("purgeroot exists and it is not a directory, exit!");
            System.exit(-1);
        }
        Path deploymentPath=Paths.get(currentPath,purgeroot,deployment);
        Path rulesPath=Paths.get(currentPath,purgeroot,rules);

       //不存在purgeroot的目录，创建该目录，创建失败即退出
        if(!Files.exists(purgerootPath)) {
            Files.createDirectory(purgerootPath);
        }

        //在第一次创建时，会把配置写到配置文件当中。在此之后，如需改变配置文件，需手动修改
        if(!Files.exists(deploymentPath)) {
            Files.createFile(deploymentPath);
            writePropertiesToDeployment(deploymentPath);
        }
        //对于rules，处理是把配置中rules路径的文档的内容复制到rules文档的末尾，而不是直接将其替代
        if(!Files.exists(rulesPath))
            Files.createFile(rulesPath);
        writeRulesToFile(rulesPath);
    }

    /**
     * 把配置信息写到制定的配置文档之中，在第一次初始化时被调用
     * @param deploymentPath
     * @throws IOException
     */
    private void writePropertiesToDeployment(Path deploymentPath) throws IOException {
        logger.info("writing deployment to purgeroot/deployment.properties");
        OutputStream outputStream=Files.newOutputStream(deploymentPath);
        purgeServiceProperties.store(outputStream,"-----------PurgeService deployment------------");
    }

    //把配置中目录的内容复制到新建的file之中
    public void writeRulesToFile(Path rulesPath) throws IOException {
        logger.info("writing rules to purgeroot/rules");
        List<String> rulesList=RulesMaintainer.parseOperationsFromFile(ruleFilePath);
        BufferedWriter writer=Files.newBufferedWriter(rulesPath,APPEND);
        writer.write("\n");
        for(String aRule:rulesList){
            writer.write(aRule+"\n");
        }
        writer.flush();
        writer.close();
    }

    /**
     * 执行规则语句的回调，当发现SQL语法错误时，即将该语句移除
     */
    class PurgeCallback implements ProcedureCallback{

        private String statement;

        public PurgeCallback(String statement){
            this.statement=statement;
        }

        @Override
        public void clientCallback(ClientResponse clientResponse) throws Exception {
            if(clientResponse.getStatus()!=ClientResponse.SUCCESS){
                String cause=clientResponse.getStatusString();
                logger.warn(cause);
                if(cause.contains("SQL Syntax error")) {
                    rulesMaintainer.remove(statement);
                }
            }
        }
    }

    public static void main(String[] args) throws IOException {
        String path=System.getProperty("user.dir")+"\\utils\\src\\main\\resources";
        Properties pros=new Properties();
        /*pros.put("servers","10.201.64.23");
        pros.put("threshold","0.6");
        pros.put("updateFrequency","30");
        pros.put("cleanFrequency","60");
        pros.put("voltadminPort","21211");
        pros.put("ruleFilePath",path+"\\rules.txt");
        File file=new File(path+"\\conf.properties");
        if(!file.exists())
            file.createNewFile();
        OutputStream outputStream=new FileOutputStream(file);
        pros.store(outputStream,"first generated config file");*/
        FileInputStream inputStream=new FileInputStream(path+"\\conf.properties");
        pros.load(inputStream);

        /*for(String name:pros.stringPropertyNames()){
            System.out.println(name+":"+pros.get(name));
        }
        String hello=null;
        System.out.println(hello==null);
        System.out.println("hello:"+pros.get("hello"));*/
        //PurgeService service=new PurgeService(pros);
        PurgeService service=new PurgeService();
        service.start();
    }

}
