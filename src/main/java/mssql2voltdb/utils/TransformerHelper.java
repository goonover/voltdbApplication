package mssql2voltdb.utils;

import mssql2voltdb.DataMeta.ApplicationMeta;
import mssql2voltdb.DataMeta.ColumnMeta;
import mssql2voltdb.DataMeta.TableMeta;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by swqsh on 2017/3/21.
 */
public final class TransformerHelper {


    public static  Connection createConnection(ApplicationMeta applicationMeta){
        String url="jdbc:sqlserver://"+applicationMeta.getIp()+";databaseName="+applicationMeta.getDatabase();
        String userName=applicationMeta.getUserName();
        String passWord=applicationMeta.getPassWord();
        String jdbcDriver="com.microsoft.sqlserver.jdbc.SQLServerDriver";
        return createConnection(url,userName,passWord,jdbcDriver);
    }

    public static Connection createConnection(String url,String userName,String passWord,String jdbcDriver){
        Connection connection=null;
        try{
            Class.forName(jdbcDriver);
            connection=DriverManager.getConnection(url,userName,passWord);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return connection;
    }

    public static  void close(Connection connection) {
        if(connection != null) {
            try {
                connection.close() ;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void fillApplicationMeta(ApplicationMeta applicationMeta,Connection conn){
        DatabaseMetaData metaData=null;
        String catalog=null;
        try {
            metaData=conn.getMetaData();
            catalog=conn.getCatalog();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        for(String tableName:applicationMeta.getTableNames()){
            TableMeta tableMeta=new TableMeta();
            tableMeta.setTableName(tableName);
            fillTable(tableMeta,metaData,catalog);
            applicationMeta.getTables().add(tableMeta);
        }
    }

    public static final ArrayList<String> getAllProcedureNames(Connection connection) throws SQLException {
        Statement statement=connection.createStatement();
        ResultSet resultSet=statement.executeQuery("select * from sys.procedures");
        ArrayList<String> res=new ArrayList<String>();
        while (resultSet.next()){
            res.add(resultSet.getString("name"));
        }
        resultSet.close();
        statement.close();
        return res;
    }

    public static final String getProcedureDefinition(Connection connection,String procedureName) throws SQLException {
        CallableStatement callableStatement=connection.prepareCall("{call sp_helptext(?)}");
        callableStatement.setString("objname",procedureName);
        boolean results=callableStatement.execute();
        ResultSet resultSet=null;
        String res="";
        if(results){
            resultSet=callableStatement.getResultSet();
            if(resultSet.next()){
                res=resultSet.getString("Text");
            }
        }
        resultSet.close();
        callableStatement.close();
        return res;
    }

    private static void fillTable(TableMeta tableMeta,DatabaseMetaData metaData,String catalog){
        try {
            fillColumns(tableMeta,metaData,catalog);
            fillPrimaryKeys(tableMeta,metaData,catalog);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void fillPrimaryKeys(TableMeta tableMeta, DatabaseMetaData metaData, String catalog) throws SQLException {
        ResultSet rs=metaData.getPrimaryKeys(catalog,null,tableMeta.getTableName());
        List<String> pkNames=tableMeta.getPkNames();
        while (rs.next()){
            String primaryKey=rs.getString("COLUMN_NAME");
            pkNames.add(primaryKey);
            for(ColumnMeta columnMeta:tableMeta.getColumns()){
                if(columnMeta.getColumnName().equalsIgnoreCase(primaryKey)){
                    columnMeta.setPrimaryKey(true);
                }
            }
        }
        tableMeta.setPkNames(pkNames);
    }

    private static void fillColumns(TableMeta tableMeta, DatabaseMetaData metaData,String catalog) throws SQLException {
        ResultSet rs=metaData.getColumns(catalog,"%",tableMeta.getTableName(),"%");
        List<ColumnMeta> columnMetas=tableMeta.getColumns();
        while (rs.next()){
            ColumnMeta columnMeta=new ColumnMeta();
            columnMeta.setColumnName(rs.getString("COLUMN_NAME"));
            columnMeta.setColumnSize(rs.getString("COLUMN_SIZE"));
            columnMeta.setColumnType(rs.getString("TYPE_NAME"));
            columnMeta.setNullable(rs.getBoolean("IS_NULLABLE"));
            columnMeta.setOrdinalPosition(rs.getInt("ORDINAL_POSITION"));
            columnMetas.add(columnMeta);
        }
        tableMeta.setColumns(columnMetas);
    }

}
