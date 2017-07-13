package purge;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * 存储所有的规则，当规则被客户端修改时，记录下规则已被更新，每隔一段时间重新把规则写到规则列表
 * 的文件之中
 * Created by swqsh on 2017/6/22.
 */
public class SQLRulesMaintainer extends Maintainer<SQLRule> {

    public SQLRulesMaintainer(String filePathName){
        super(filePathName);
    }

    public SQLRulesMaintainer(String filePathName,boolean allowDuplicated) {
        super(filePathName,allowDuplicated);
    }

    public SQLRulesMaintainer(String filePathName,boolean allowDuplicated,AOFWriter writer){
        super(filePathName,allowDuplicated,writer);
    }

    @Override
    void readRulesFromFile() throws IOException {
        if(aofOn){
            aofWriter.restoreFromLog();
        }else {
            List<SQLRule> rulesParsed = parseOperationsFromFile(rulePath.toString());
            for (SQLRule rule : rulesParsed) {
                rules.add(rule);
            }
        }
    }

    /**
     * 读取存储着规则的文件,并将其每条规则独立提取出来，方便统一格式以及处理
     */
    protected static List<SQLRule> parseOperationsFromFile(String filePath) throws RuntimeException{
        ArrayList<SQLRule> res=new ArrayList<>();
        if(filePath==null)
            return res;
        Path sourceFilePath= Paths.get(filePath);
        if(!Files.exists(sourceFilePath))
            return res;
        try {
            BufferedReader reader= Files.newBufferedReader(sourceFilePath);
            String line="";
            String legacy="";
            while ((line=reader.readLine())!=null){
                int index;
                while ((index=line.indexOf(";"))!=-1){
                    legacy+=line.substring(0,index+1);
                    res.add(new SQLRule(legacy.trim()));
                    legacy="";
                    line=line.substring(index+1);
                }
                legacy+=line+" ";
            }
            //处理最后一条，去除空格以及如最后一条不存在";"，则给它加上";"
            if(legacy.matches(".*[a-zA-Z].*")){
                legacy=legacy.trim();
                if(!legacy.endsWith(";"))
                    legacy+=";";
                res.add(new SQLRule(legacy));
            }
            reader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return res;
    }

    /**
     * 把SQL规则加到规则列表中，请注意，相同的SQL语句是否可以重复添加取决于allowDuplicated
     * 在aofOn为true时，添加成功时，必须在写入增量日志成功后才能返回
     * @param ruleToBeAdded
     * @return
     */
    @Override
    protected boolean addRule(SQLRule ruleToBeAdded) {
        if(!allowDuplicated&&rules.contains(ruleToBeAdded))
            return false;
        boolean added=rules.add(ruleToBeAdded);
        //如果要改为多线程，必须将写日志和放进hashmap整个代码块加锁，否则有可能出现日志与当前数据不一致
        if(aofOn&&(added==true)){
            try {
                aofWriter.recordLog(AOFOperation.ADDSQL,ruleToBeAdded.toString());
            } catch (IOException e) {
                logger.warn(e);
                rules.remove(ruleToBeAdded);
                return false;
            }
        }
        modified|=added;
        return added;
    }

    @Override
    protected boolean removeRule(SQLRule rule) {
        boolean removed=false;
        SQLRule operation=null;
        for(SQLRule aRule:rules){
            if(aRule.equals(rule)||aRule.equals(rule+";")) {
                operation = aRule;
                break;
            }
        }
        if(operation!=null)
            removed=rules.remove(operation);
        if(aofOn&&removed){
            try {
                aofWriter.recordLog(AOFOperation.REMOVESQL,rule.toString());
            } catch (IOException e) {
                logger.warn(e);
                rules.add(rule);
                return false;
            }
        }
        modified|=removed;
        return removed;
    }
}
