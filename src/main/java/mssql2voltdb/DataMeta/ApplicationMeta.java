package mssql2voltdb.DataMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by swqsh on 2017/3/21.
 */
public class ApplicationMeta {

    private String ip;

    private String userName;

    private String passWord;

    private String database;

    private List<String> tableNames=new ArrayList<String>();

    //存储目标表的元数据
    private List<TableMeta> tables=new ArrayList<TableMeta>();

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassWord() {
        return passWord;
    }

    public void setPassWord(String passWord) {
        this.passWord = passWord;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public List<String> getTableNames() {
        return tableNames;
    }

    public void setTableNames(List<String> tableNames) {
        this.tableNames = tableNames;
    }

    public List<TableMeta> getTables() {
        return tables;
    }

    public void setTables(List<TableMeta> tables) {
        this.tables = tables;
    }
}
