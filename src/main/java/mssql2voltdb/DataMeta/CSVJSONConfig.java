package mssql2voltdb.DataMeta;

import mssql2voltdb.utils.MyCSVLoader;
import org.voltdb.client.Client;
import org.voltdb.utils.CSVLoader;

import java.lang.reflect.Field;

import static mssql2voltdb.utils.MyCSVLoader.*;

/**
 * Created by swqsh on 2017/4/6.
 */
public class CSVJSONConfig {

    public String file = "";

    String procedure = "";

    int limitrows = Integer.MAX_VALUE;

    String reportdir = System.getProperty("user.dir");

    int maxerrors = 100;

    String blank = "null";

    char separator = DEFAULT_SEPARATOR;

    char quotechar = DEFAULT_QUOTE_CHARACTER;

    char escape = DEFAULT_ESCAPE_CHARACTER;

    boolean strictquotes = DEFAULT_STRICT_QUOTES;

    long skip = DEFAULT_SKIP_LINES;

    boolean nowhitespace = DEFAULT_NO_WHITESPACE;

    long columnsizelimit = DEFAULT_COLUMN_LIMIT_SIZE;

    String servers = "localhost";

    String user = "";

    String password = "";

    int port = Client.VOLTDB_SERVER_PORT;

    String timezone = "";

    String customNullString = "";

    boolean noquotechar = false;

    public int batch = 200;

    boolean header = DEFAULT_HEADER;

    public String table = "";

    boolean useSuppliedProcedure = false;

    boolean update = DEFAULT_UPSERT_MODE;

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public String getProcedure() {
        return procedure;
    }

    public void setProcedure(String procedure) {
        this.procedure = procedure;
    }

    public int getLimitrows() {
        return limitrows;
    }

    public void setLimitrows(int limitrows) {
        this.limitrows = limitrows;
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

    public String getBlank() {
        return blank;
    }

    public void setBlank(String blank) {
        this.blank = blank;
    }

    public char getSeparator() {
        return separator;
    }

    public void setSeparator(char separator) {
        this.separator = separator;
    }

    public char getQuotechar() {
        return quotechar;
    }

    public void setQuotechar(char quotechar) {
        this.quotechar = quotechar;
    }

    public char getEscape() {
        return escape;
    }

    public void setEscape(char escape) {
        this.escape = escape;
    }

    public boolean isStrictquotes() {
        return strictquotes;
    }

    public void setStrictquotes(boolean strictquotes) {
        this.strictquotes = strictquotes;
    }

    public long getSkip() {
        return skip;
    }

    public void setSkip(long skip) {
        this.skip = skip;
    }

    public boolean isNowhitespace() {
        return nowhitespace;
    }

    public void setNowhitespace(boolean nowhitespace) {
        this.nowhitespace = nowhitespace;
    }

    public long getColumnsizelimit() {
        return columnsizelimit;
    }

    public void setColumnsizelimit(long columnsizelimit) {
        this.columnsizelimit = columnsizelimit;
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

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public String getCustomNullString() {
        return customNullString;
    }

    public void setCustomNullString(String customNullString) {
        this.customNullString = customNullString;
    }

    public boolean isNoquotechar() {
        return noquotechar;
    }

    public void setNoquotechar(boolean noquotechar) {
        this.noquotechar = noquotechar;
    }

    public int getBatch() {
        return batch;
    }

    public void setBatch(int batch) {
        this.batch = batch;
    }

    public boolean isHeader() {
        return header;
    }

    public void setHeader(boolean header) {
        this.header = header;
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

    public CSVConfig toCSVConfig(){
        CSVConfig csvConfig=new CSVConfig();
        for(Field field:getClass().getDeclaredFields()){
            String name=field.getName();
            try {
                Field field1=csvConfig.getClass().getDeclaredField(name);
                field1.setAccessible(true);
                field1.set(csvConfig,field.get(this));
            }  catch (NoSuchFieldException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        csvConfig.validate();
        return csvConfig;
    }
}
