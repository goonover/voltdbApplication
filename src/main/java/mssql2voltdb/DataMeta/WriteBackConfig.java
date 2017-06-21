package mssql2voltdb.DataMeta;

import java.util.List;

/**
 * 用于执行{@link mssql2voltdb.utils.WritebackHelper#writeBack(WriteBackConfig)}的配置格式
 * Created by swqsh on 2017/4/10.
 */
public class WriteBackConfig {

    /**
     * voltdb client连接相关配置
     */

    private VoltClientConfig voltClientConfig;

    private String procedure="";

    /**
     * jdbc 连接相关配置
     */
    private String jdbcDriver="";

    private String jdbcUrl="";

    private String jdbcUserName="";

    private String jdbcPassword="";

    private String jdbcTable="";

    private String table="";

    public VoltClientConfig getVoltClientConfig() {
        return voltClientConfig;
    }

    public void setVoltClientConfig(VoltClientConfig voltClientConfig) {
        this.voltClientConfig = voltClientConfig;
    }

    public String getProcedure() {
        return procedure;
    }

    public void setProcedure(String procedure) {
        this.procedure = procedure;
    }

    public String getJdbcDriver() {
        return jdbcDriver;
    }

    public void setJdbcDriver(String jdbcDriver) {
        this.jdbcDriver = jdbcDriver;
    }

    public String getJdbcUrl() {
        return jdbcUrl;
    }

    public void setJdbcUrl(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
    }

    public String getJdbcUserName() {
        return jdbcUserName;
    }

    public void setJdbcUserName(String jdbcUserName) {
        this.jdbcUserName = jdbcUserName;
    }

    public String getJdbcPassword() {
        return jdbcPassword;
    }

    public void setJdbcPassword(String jdbcPassword) {
        this.jdbcPassword = jdbcPassword;
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public String getJdbcTable() {
        return jdbcTable;
    }

    public void setJdbcTable(String jdbcTable) {
        this.jdbcTable = jdbcTable;
    }
}
