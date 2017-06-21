package mssql2voltdb.utils;

import mssql2voltdb.DataMeta.ApplicationMeta;
import mssql2voltdb.DataMeta.ColumnMeta;
import mssql2voltdb.DataMeta.TableMeta;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.sql.*;

/**
 * Created by swqsh on 2017/3/23.
 */
public class GenerateCodeHelper {

    public static String generateAppScript(ApplicationMeta applicationMeta){
        StringBuilder sb=new StringBuilder();
        for(TableMeta tableMeta:applicationMeta.getTables()){
            generateTableScript(tableMeta,sb);
        }
        return sb.toString();
    }

    public static void dumpTable(Connection connection,ApplicationMeta applicationMeta, String tableName){
        TableMeta table=null;
        String columnName="";
        for(TableMeta tableMeta:applicationMeta.getTables()){
            if(tableMeta.getTableName().equalsIgnoreCase(tableName)) {
                table = tableMeta;
                break;
            }
        }

        /*
        如果目标表存在主键，即以第一个主键排序
        否则以第一个键排序
         */
        if(table!=null){
            if(table.getPkNames().size()>0)
                columnName = table.getPkNames().get(0);
            else
                columnName=table.getColumns().get(0).getColumnName();
        }

        dumpTable(connection,tableName,columnName);
    }

    public static void dumpTable(Connection connection,String tableName,String columnName){
        //为了防止内存耗尽，不能一次查出含有大量数据的数据库表
        dumpTable(connection,tableName,columnName,5);
    }

    public static void dumpTable(Connection connection,String tableName,String columnName,int perSize){
        String realFileName=".\\target\\"+tableName+".txt";
        File result=new File(realFileName);
        boolean hasHeaders=false;
        int position=0;
        boolean hasMore=getDataFromDatabases(connection,result,hasHeaders,tableName,columnName,position,perSize);
        position+=perSize;
        hasHeaders=true;
        while(hasMore){
            hasMore=getDataFromDatabases(connection,result,hasHeaders,tableName,columnName,position,perSize);
            position+=perSize;
        }
    }

    private static boolean getDataFromDatabases(Connection connection,File result,boolean hasHeaders,String tableName,
                                         String columnName, int position,int perSize){
        /*
           查询数据库得到一次数据
         */
        boolean hasMore=true;
        Statement statement=null;
        ResultSet resultSet=null;
        String query="select * from "+tableName+" order by "+columnName+" offset "+position+" rows fetch next "
                +perSize+" rows only";
        try{
            statement=connection.createStatement();
            resultSet=statement.executeQuery(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if(resultSet==null)
            return false;

        //第一次查询时要写入头信息
        try {
            if (!hasHeaders)
                writeHeaders(result, resultSet);
            int size=writeDataToCSV(result, resultSet);
            System.out.println("resultSet: "+size);
            hasMore=(size==perSize);
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return hasMore;
    }

    private static void writeHeaders(File result,ResultSet resultSet) throws SQLException, IOException {
        ResultSetMetaData meta = resultSet.getMetaData() ;
        int numberOfColumns = meta.getColumnCount() ;
        String dataHeaders = "" + meta.getColumnName(1) + "" ;
        for (int i = 2 ; i < numberOfColumns + 1 ; i ++ ) {
            dataHeaders += "," + meta.getColumnName(i) ;
        }
        dataHeaders+="\n";
        FileUtils.writeStringToFile(result,dataHeaders,"utf-8");
    }

    private static int writeDataToCSV(File result,ResultSet resultSet) throws SQLException, IOException {
        int rowCount=0;
        while (resultSet.next()){
            int numberOfColumns = resultSet.getMetaData().getColumnCount() ;
            String row=resultSet.getString(1);
            rowCount++;
            for(int i=2;i<=numberOfColumns;i++){
                row+=","+resultSet.getString(i);
            }
            row+="\n";
            FileUtils.writeStringToFile(result,row,"utf-8",true);
        }
        return rowCount;
    }

    private static void generateTableScript(TableMeta tableMeta, StringBuilder sb) {
        sb.append("create table "+tableMeta.getTableName()+"(\n\t");
        for(ColumnMeta columnMeta:tableMeta.getColumns()){
            sb.append(columnMeta.getColumnName()+"\t\t");
            if(columnMeta.getColumnType().equalsIgnoreCase("varchar"))
                sb.append(columnMeta.getColumnType()+"("+columnMeta.getColumnSize()+")\t\t");
            else
                sb.append(columnMeta.getColumnType()+"\t\t");
            sb.append(columnMeta.getNullable()?",":"not null,\t\t");
            sb.append("\n\t");
        }
        if (tableMeta.getPkNames().size()>0){
            String primaryKey="";
            int i=0;
            while(i<tableMeta.getPkNames().size()){
                primaryKey+=tableMeta.getPkNames().get(i)+",";
                i++;
            }
            int last=primaryKey.lastIndexOf(",");
            if(last>0)
                primaryKey=primaryKey.substring(0,last);
            sb.append("primary key("+primaryKey+"));\n\n");
        }
    }


}
