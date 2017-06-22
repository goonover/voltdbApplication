package purge;

import org.apache.log4j.Logger;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

/**
 * 维护规则列表和存储过程列表的抽象类，{@link SQLRulesMaintainer}和{@link PurgeProceduresMaintainer}都必须扩展
 * 该类。该类主要负责一些规则的添加删除
 * Created by swqsh on 2017/6/22.
 */
public abstract class Maintainer<T> extends Thread {

    private Logger logger= Logger.getLogger(Maintainer.class);

    List<T> rules=new ArrayList<T>();

    //规则列表是否已经更新的标志
    protected boolean modified=false;

    //规则存储文件路径
    protected Path rulePath;

    public Maintainer(String filePathName){
        rulePath=Paths.get(filePathName);
        try {
            readRulesFromFile();
        } catch (IOException e) {
            handlerInitException(e);
        }
    }

    @Override
    public void run(){
        if(modified)
            writeRulesToFile();
    }

    public void shutdown(){
        if(modified)
            writeRulesToFile();
    }

    //把当前规则写到存储文件之中
    public void writeRulesToFile(){
        String fileTemp=rulePath.toString()+"_temp";
        Path fileTempPath= Paths.get(fileTemp);
        try {
            BufferedWriter writer= Files.newBufferedWriter(fileTempPath);
            for(T rule:rules){
                writer.write(rule.toString()+"\n");
            }
            writer.flush();
            writer.close();
            Files.move(fileTempPath,rulePath,REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
        modified=false;
    }

    //读取文档内容，生成规则列表
    abstract void readRulesFromFile() throws IOException;

    //初始化失败，直接退出
    private void handlerInitException(Exception e){
        logger.error("cann't read rules from "+rulePath.toString()+", exit");
        logger.error(e);
        System.exit(-1);
    }

    public abstract boolean addRule(T ruleToBeAdded);

    /**
     * 注意，该函数非原子操作，可能部分成功，部分失败
     * @param rulesToBeAdded
     * @return  不直接返回成功失败，而是返回添加成功的规则列表
     */
    public List<T> addRules(List<T> rulesToBeAdded){
        List<T> addedList=new ArrayList<>();
        for(T rule:rulesToBeAdded){
            if(addRule(rule))
                addedList.add(rule);
        }
        return addedList;
    }

    public abstract boolean removeRule(T rule);

    /**
     * 注意，该函数非原子操作，可能部分成功，部分失败
     * @param rulesToBeRemoved
     * @return  不直接返回成功失败，而是返回添加删除的规则列表
     */
    public List<T> removeRules(List<T> rulesToBeRemoved){
        List<T> removedList=new ArrayList<>();
        for(T rule:rulesToBeRemoved){
            if(removeRule(rule))
                removedList.add(rule);
        }
        return removedList;
    }

    public List<T> getRules(){
        return rules;
    }

}
