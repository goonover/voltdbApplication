package mssql2voltdb.DataMeta;

import mssql2voltdb.utils.JDBCLoader;
import org.voltdb.client.Client;

import java.lang.reflect.Field;

/**
 * Created by swqsh on 2017/4/7.
 */
public class JDBCJSONConfig {

    String procedure = "";

    String reportdir = System.getProperty("user.dir");

    int maxerrors = 100;

    String servers = "localhost";

    String user = "";

    String password = "";

    int port = Client.VOLTDB_SERVER_PORT;

    String jdbcdriver = "";

    String jdbcurl = "";

    String jdbcuser = "";

    String jdbcpassword = "";

    String jdbctable = "";

    int fetchsize = 100;

    public int batch = 200;

    public String table = "";

    boolean useSuppliedProcedure = false;

    boolean update = false;

    public String getProcedure() {
        return procedure;
    }

    public void setProcedure(String procedure) {
        this.procedure = procedure;
    }

    public String getReportdir() {
        return reportdir;
    }

    public void setReportdir(String reportdir) {
        this.reportdir = reportdir;
    }

    public int getMaxerrors() {
        return maxerrors;
    }

    public void setMaxerrors(int maxerrors) {
        this.maxerrors = maxerrors;
    }

    public String getServers() {
        return servers;
    }

    public void setServers(String servers) {
        this.servers = servers;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getJdbcdriver() {
        return jdbcdriver;
    }

    public void setJdbcdriver(String jdbcdriver) {
        this.jdbcdriver = jdbcdriver;
    }

    public String getJdbcurl() {
        return jdbcurl;
    }

    public void setJdbcurl(String jdbcurl) {
        this.jdbcurl = jdbcurl;
    }

    public String getJdbcuser() {
        return jdbcuser;
    }

    public void setJdbcuser(String jdbcuser) {
        this.jdbcuser = jdbcuser;
    }

    public String getJdbcpassword() {
        return jdbcpassword;
    }

    public void setJdbcpassword(String jdbcpassword) {
        this.jdbcpassword = jdbcpassword;
    }

    public String getJdbctable() {
        return jdbctable;
    }

    public void setJdbctable(String jdbctable) {
        this.jdbctable = jdbctable;
    }

    public int getFetchsize() {
        return fetchsize;
    }

    public void setFetchsize(int fetchsize) {
        this.fetchsize = fetchsize;
    }

    public int getBatch() {
        return batch;
    }

    public void setBatch(int batch) {
        this.batch = batch;
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public boolean isUseSuppliedProcedure() {
        return useSuppliedProcedure;
    }

    public void setUseSuppliedProcedure(boolean useSuppliedProcedure) {
        this.useSuppliedProcedure = useSuppliedProcedure;
    }

    public boolean isUpdate() {
        return update;
    }

    public void setUpdate(boolean update) {
        this.update = update;
    }

    public JDBCLoader.JDBCLoaderConfig toJDBCLoaderConfig(){
        JDBCLoader.JDBCLoaderConfig jdbcLoaderConfig=new JDBCLoader.JDBCLoaderConfig();
        for(Field field:getClass().getDeclaredFields()){
            try {
                Field jdbcField = jdbcLoaderConfig.getClass().getDeclaredField(field.getName());
                jdbcField.setAccessible(true);
                jdbcField.set(jdbcLoaderConfig, field.get(this));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }
        }
        jdbcLoaderConfig.validate();
        return jdbcLoaderConfig;
    }
}
