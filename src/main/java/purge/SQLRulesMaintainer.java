package purge;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 存储所有的规则，当规则被客户端修改时，记录下规则已被更新，每隔一段时间重新把规则写到规则列表
 * 的文件之中
 * Created by swqsh on 2017/6/22.
 */
public class SQLRulesMaintainer extends Maintainer<SQLRule> {

    public SQLRulesMaintainer(String filePathName) {
        super(filePathName);
    }

    @Override
    void readRulesFromFile() throws IOException {
        rules=parseOperationsFromFile(rulePath.toString());
    }

    /**
     * 读取存储着规则的文件,并将其每条规则独立提取出来，方便统一格式以及处理
     */
    public static List<SQLRule> parseOperationsFromFile(String filePath) throws RuntimeException{
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
     * 把SQL规则加到规则列表中，请注意，相同的SQL语句可以重复添加
     * TODO:添加开关选项判断是否需要禁止重复添加
     * @param ruleToBeAdded
     * @return
     */
    @Override
    public boolean addRule(SQLRule ruleToBeAdded) {
        boolean added=rules.add(ruleToBeAdded);
        modified|=added;
        return added;
    }

    @Override
    public boolean removeRule(SQLRule rule) {
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
        modified|=removed;
        return removed;
    }
}
