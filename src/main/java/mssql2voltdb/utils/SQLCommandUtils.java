package mssql2voltdb.utils;

import org.apache.commons.io.IOUtils;
import org.voltdb.client.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;

/**
 * voltdb的常用方法
 * Created by swqsh on 2017/4/19.
 */
public class SQLCommandUtils {

    /**
     * 用于快速建立连接
     * @param servers 服务器ip地址列表，用逗号","作为分隔符
     * @return
     * @throws IOException
     */
    public static Client getClient(String servers) throws IOException{
        return  getClient(servers,21212);
    }

    public static Client getClient(String servers,int port) throws IOException{
        return getClient(servers,port,"","",true);
    }


    public static Client getClient(String servers,int port,String userName,String passWord,Boolean isTopologyChangeAware)
            throws IOException {
        ClientConfig clientConfig=new ClientConfig(userName,passWord);
        clientConfig.setTopologyChangeAware(isTopologyChangeAware);
        String[] serverList=servers.split(",");
        Client client=ClientFactory.createClient(clientConfig);
        for(String server:serverList){
            client.createConnection(server,port);
        }
        return  client;
    };

    /**
     * 用于voltdb连接到集群，获取Client，等到所有的连接建立后才返回
     * @param servers
     * @param userName
     * @param passWord
     * @return
     * @throws InterruptedException
     */
    public static Client getClientWithMultiConn(String servers,String userName,String passWord) throws InterruptedException {
        ClientConfig clientConfig=new ClientConfig(userName,passWord);
        clientConfig.setTopologyChangeAware(true);
        String[] serverList=servers.split(",");
        Client client=ClientFactory.createClient(clientConfig);
        CountDownLatch connections=new CountDownLatch(serverList.length);
        for(String server:serverList){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        client.createConnection(server);
                        connections.countDown();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
        connections.await();
        return client;
    }

    /**
     * 把jar包加载到client中，跟sqlcmd的load Classes from  xxx.jar功能一样
     * @param client
     * @param jarPath   jar包的所在路径
     */
    public static void loadClasses(Client client,String jarPath){
        File jarFile=new File(jarPath);
        try {
            client.updateClasses(jarFile,null);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ProcCallException e) {
            e.printStackTrace();
        }
    }

    /**
     * 把已经加载的class移除，功能跟sqlcmd的remove className一样
     * @param client
     * @param className
     */
    public static void removeClasses(Client client,String className){
        try {
            client.updateClasses(null,className);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ProcCallException e) {
            e.printStackTrace();
        }
    }


    /**
     * 可以直接执行ddl语句，如create table之类
     * @param client
     * @param statement
     */
    public static void executeDDLStatement(Client client,String statement){
        try {
            client.callProcedure("@AdHoc",statement);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ProcCallException e) {
            e.printStackTrace();
        }
    }

    /**
     * 执行sql文件
     * @param client
     * @param filePath
     */
    public static void executeDDLFile(Client client,String filePath) {
        try {
            FileInputStream inputStream = new FileInputStream(filePath);
            String ddlSet = IOUtils.toString(inputStream, "utf-8");
            executeDDLStatement(client, ddlSet);
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    /**
     * 从jarPath所在的jar包中创建新的存储过程
     * @param client
     * @param jarPath
     * @param procedureName
     */
    public static void createProcedure(Client client,String jarPath,String procedureName){
        loadClasses(client,jarPath);
        String statement="create procedure from class "+procedureName;
        executeDDLStatement(client,statement);
    }

    public static void updateProcedure(Client client,String jarPath,String procedureName){
        String statement="drop procedure "+procedureName;
        executeDDLStatement(client,statement);
        createProcedure(client,jarPath,procedureName);
    }

    /**
     * 关闭连接
     * @param client
     * @throws NoConnectionsException
     * @throws InterruptedException
     */
    public static void closeClient(Client client) throws NoConnectionsException, InterruptedException {
        client.drain();
        client.close();
    }

    /**
     * 在集群暂停时，调用该存储过程恢复服务状态
     * @param client
     * @throws IOException
     * @throws ProcCallException
     */
    public static void resume(Client client) throws IOException, ProcCallException {
        client.callProcedure("@Resume");
    }

    public static void main(String[] args){
        try {
            Client client = getClient("10.201.64.23");
            //executeDDLFile(client,"./logV3.sql");
            //executeDDLStatement(client,"insert into person values('hello',1);" +
             //       "insert into person values ('world','2');");
            //loadClasses(client,"./storeprocs.jar");
            //executeDDLStatement(client,"create procedure from class Register_User");
            updateProcedure(client,"./storeprocs.jar","Register_User");
            closeClient(client);
        }catch (Exception e){
            e.printStackTrace();
        }
    }


}
