package mssql2voltdb.utils;

import mssql2voltdb.DataMeta.VoltClientConfig;
import mssql2voltdb.DataMeta.WriteBackConfig;
import org.apache.log4j.Logger;
import org.voltdb.VoltTable;
import org.voltdb.VoltTableRow;
import org.voltdb.VoltType;
import org.voltdb.client.*;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * file encode is gbk,if you find some character is shown as ????,
 * please just change the encode to gbk
 * 该类用于把voltdb某一个表的所有记录全部查出来，并生成sql语句，倒入传统数据库
 * 该类适用于元组数量较少的表格，如元组数量大的表格请采用快照及其他持久化方式
 * 使用方法：
 * 填写配置格式为{@link WriteBackConfig}的应用配置，然后调用{@code writeBack}即可
 *
 * WriteBackConfig config=new WriteBackConfig(...);
 * WritebackHelper helper=new WritebackHelper();
 * helper.writeBack(config);
 *
 * Created by swqsh on 2017/4/10.
 * @see #writeBack(WriteBackConfig)
 */
public class WritebackHelper {

    private Logger logger=Logger.getLogger(WritebackHelper.class);

    /**
     * 回写主方法
     * @param writeBackConfig
     */
    public void writeBack(WriteBackConfig writeBackConfig){
        Client client=connect(writeBackConfig.getVoltClientConfig());
        VoltTable table=null;
        try {
            ClientResponse clientResponse=client.callProcedure("@AdHoc","select * from users");
            table=clientResponse.getResults()[0];
        } catch (NoConnectionsException e) {
            logger.error("连接错误，请检查服务器配置是否正确");
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ProcCallException e) {
            logger.error("不能查到相关表的数据，请检查相关配置");
            e.printStackTrace();
        }
        VoltTable.ColumnInfo[] columnInfos=table.getTableSchema();
        List<ColumnInfoAdapter> columnInfoAdapterList=new ArrayList<>();
        for(VoltTable.ColumnInfo columnInfo:columnInfos){
            columnInfoAdapterList.add(new ColumnInfoAdapter(columnInfo));
        }

        List<String> sqlScripts=generateSQLScript(table,columnInfoAdapterList);
        Connection connection=TransformerHelper.createConnection(writeBackConfig.getJdbcUrl(),
                writeBackConfig.getJdbcUserName(),writeBackConfig.getJdbcPassword(),writeBackConfig.getJdbcDriver());
        try {
            writeToDatabases(connection,sqlScripts);
        } catch (SQLException e) {
            logger.error("向数据库回写过程中发生错误\n");
            e.printStackTrace();
        }
        releaseClient(client);
        TransformerHelper.close(connection);
    }

    /**
     * 创建与voltdb的连接
     * @param voltClientConfig
     * @return
     */
    public static Client connect(VoltClientConfig voltClientConfig){
        ClientConfig clientConfig=new ClientConfig(voltClientConfig.getUserName(),voltClientConfig.getPassWord());
        clientConfig.setTopologyChangeAware(voltClientConfig.isTopologyChangeAware());
        Client client=ClientFactory.createClient(clientConfig);
        try {
            for (String server : voltClientConfig.getServers())
                client.createConnection(server.trim(),21212);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return client;
    }

    /**
     * 执行插入数据的sql语句，向传统数据库写入数据
     * @param connection
     * @param sqlScirpts
     * @throws SQLException
     */
    private void writeToDatabases(Connection connection,List<String> sqlScirpts) throws SQLException {
        Statement statement=connection.createStatement();
        connection.setAutoCommit(false);
        for(String script:sqlScirpts){
            statement.addBatch(script);
        }
        statement.executeBatch();
        connection.commit();
    }

    /**
     * 产生插入代码
     * @param table
     * @param columnInfoAdapterList
     * @return
     */
    private List<String> generateSQLScript(VoltTable table,List<ColumnInfoAdapter> columnInfoAdapterList){
        List<String> result=new ArrayList<>();
        table.advanceRow();
        int tableSize=table.getRowCount();

        for(int i=0;i<tableSize;i++) {
            VoltTableRow row=table.fetchRow(i);
            String res="insert into users (";
            String name="";
            String value="";
            for (ColumnInfoAdapter adapter : columnInfoAdapterList) {
                name += adapter.getName() + ",";
                value += "'"+row.get(adapter.getName(), adapter.getType()) + "',";
            }
            name = name.substring(0, name.lastIndexOf(","));
            value = value.substring(0, value.lastIndexOf(","));
            res += name + ") values (" + value + ");";
            result.add(res);
        }
        return  result;
    }

    private void releaseClient(Client client){
        try {
            client.drain();
            client.close();
        } catch (NoConnectionsException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }



}

class ColumnInfoAdapter{

    String name;
    VoltType type;

    public ColumnInfoAdapter(VoltTable.ColumnInfo columnInfo){
        for(Field field:ColumnInfoAdapter.class.getDeclaredFields()){
            try {
                String name=field.getName();
                Field columnInfoField = columnInfo.getClass().getDeclaredField(name);
                field.setAccessible(true);
                columnInfoField.setAccessible(true);
                field.set(this, columnInfoField.get(columnInfo));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public VoltType getType() {
        return type;
    }

    public void setType(VoltType type) {
        this.type = type;
    }

}
