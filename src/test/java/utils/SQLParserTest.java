package utils;

import org.voltdb.parser.SQLParser;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by swqsh on 2017/6/15.
 */
public class SQLParserTest {

    public static void main(String[] args){
        String ddl0="create table company(name varchar);";
        String ddl1="select * from hello;";
        String ddl2="who you are";
        String ddl3="delete from cookie where id=3";
        String ddl4="update me set cokk='me' where id =5;";
        ArrayList<String> src=new ArrayList<>();
        src.add(ddl0);
        src.add(ddl1);
        src.add(ddl2);
        src.add(ddl3);
        src.add(ddl4);
        for(String str:src){
            List temp=SQLParser.parseQuery(str);
            System.out.println(temp);
        }
        System.out.println("ddl1:"+SQLParser.queryIsDDL(ddl0));
        System.out.println("ddl2:"+SQLParser.queryIsDDL(ddl2));
        System.out.println("ddl3:"+SQLParser.queryIsDDL(ddl3));
        System.out.println("ddl4:"+SQLParser.queryIsDDL(ddl4));
    }

}
