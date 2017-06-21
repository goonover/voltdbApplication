package purge;

import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

/**
 * 存储所有的规则，当规则被客户端修改时，记录下规则已被更新，每隔一段时间重新把规则写到规则列表
 * 的文件之中
 * Created by swqsh on 2017/6/15.
 */
public class RulesMaintainer extends Thread{

    private static Logger logger= Logger.getLogger(RulesMaintainer.class);

    private volatile List<String> rules=new ArrayList<>();
    
    //规则文件保存的路径
    private String rulePathStr;

    //列表是否已经被更新
    boolean modified=false;
    
    
    public RulesMaintainer(String filePath){
        rulePathStr=filePath;
        rules=parseOperationsFromFile(filePath);
        this.modified=false;
        logger.info("RulesMaintainer start");
    }

    @Override
    public void run(){
        if(modified)
            updateRulesFile();
        modified=false;
    }

    public void shutdown(){
        if(modified)
            updateRulesFile();;
    }

    /**
     * 在{@link RulesMaintainer#rules}已被修改的情况下，把其规则重新写到规则列表中
     */
    private void updateRulesFile() {
        Path rulePath=Paths.get(rulePathStr);
        Path temp=Paths.get(rulePathStr+"temp");
        try {
            if(Files.exists(temp))
                Files.delete(temp);
            BufferedWriter writer = Files.newBufferedWriter(temp);
            System.out.println("rules size:"+rules.size());
            for (String aRule : rules) {
                System.out.println(aRule);
                writer.write(aRule + "\n");
            }
            writer.flush();
            Files.move(temp, rulePath, REPLACE_EXISTING);
            writer.close();
        }catch (IOException e){
            logger.warn(e);
        }
    }

    /**
     * 读取存储着规则的文件,并将其每条规则独立提取出来，方便统一格式以及处理
     */
    public static List<String> parseOperationsFromFile(String filePath){
        ArrayList<String> res=new ArrayList<>();
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
                    res.add(legacy.trim());
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
                res.add(legacy);
            }
            reader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return res;
    }

    public List<String> getOperations(){
        return this.rules;
    }

    /**
     * 当发现语句违反SQL语法时，由{@link PurgeService.PurgeCallback}调用该方法移除该规则
     * @param statement
     * @return
     */
    public boolean remove(String statement){
        System.out.println("remove statement:"+statement);
        boolean removedIt=rules.remove(statement);
        modified|=removedIt;
        return removedIt;
    }

    /**
     * 通过客户端移除规则，与remove的差别在于调用对象不一样，而且rule不一定完全一样
     * tip:不能直接在for里面直接调用remove，否则iterator会抛出并发异常
     * @param rule
     * @return
     */
    public boolean removeRule(String rule){
        boolean removed=false;
        String operation="";
        for(String aRule:rules){
            if(aRule.equals(rule)||aRule.equals(rule+";"))
                operation=aRule;
        }
        if(!operation.equals(""))
            removed=remove(operation);

        return removed;
    }

    public boolean addRules(List<String> rulesToBeAdd){
        boolean addSuccessfully=this.rules.addAll(rulesToBeAdd);
        modified|=addSuccessfully;
        return addSuccessfully;
    }

    public boolean removeRules(List<String> rulesToBeRemoved){
        boolean removeSuccessfully=this.rules.removeAll(rulesToBeRemoved);
        modified|=removeSuccessfully;
        return removeSuccessfully;
    }


}
