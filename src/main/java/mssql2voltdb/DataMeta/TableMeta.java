package mssql2voltdb.DataMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by swqsh on 2017/3/21.
 */
public class TableMeta {

    private String tableName;

    private List<ColumnMeta> columns=new ArrayList<ColumnMeta>();

    //表中所含有的主键
    private List<String> pkNames=new ArrayList<String>();

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public List<ColumnMeta> getColumns() {
        return columns;
    }

    public void setColumns(List<ColumnMeta> columns) {
        this.columns = columns;
    }

    public List<String> getPkNames() {
        return pkNames;
    }

    public void setPkNames(List<String> pkNames) {
        this.pkNames = pkNames;
    }
}
