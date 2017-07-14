package purge;

import org.apache.log4j.Logger;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

/**
 * 维护规则列表和存储过程列表的抽象类，{@link SQLRulesMaintainer}和{@link PurgeProceduresMaintainer}都必须扩展
 * 该类。该类主要负责一些规则的添加删除
 * Created by swqsh on 2017/6/22.
 */
public abstract class Maintainer<T> extends Thread {

    protected Logger logger= Logger.getLogger(Maintainer.class);

    BlockingQueue<T> rules=new LinkedBlockingQueue<T>();

    //规则列表是否已经更新的标志
    protected boolean modified=false;

    //规则存储文件路径
    protected Path rulePath;

    //允许拥有重复元素
    protected boolean allowDuplicated;

    //采用增量日志标志
    protected boolean aofOn = false;
    protected AOFWriter aofWriter;

    public Maintainer(String filePathName){
       this(filePathName,true);
    }

    public Maintainer(String filePathName,boolean allowDuplicated){
        this(filePathName,allowDuplicated,null);
    }

    public Maintainer(String filePathName,boolean allowDuplicated,AOFWriter writer){
        if(writer!=null){
            aofOn = true;
            aofWriter = writer;
        }
        this.allowDuplicated=allowDuplicated;
        rulePath=Paths.get(filePathName);
        try {
            readRulesFromFile();
        } catch (IOException e) {
            handlerInitException(e);
        }
    }

    /**
     * 快照模式采用，每隔一段时间把当前的规则列表写到文档当中，增量日志模式应该永不调用
     */
    @Override
    public void run(){
        if(modified)
            writeRulesToFile();
    }

    void shutdown(){
        if(modified)
            writeRulesToFile();
    }

    //把当前规则写到存储文件之中
    protected void writeRulesToFile(){
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

    protected abstract boolean addRule(T ruleToBeAdded);

    /**
     * 注意，该函数非原子操作，可能部分成功，部分失败
     * @param rulesToBeAdded
     * @return  不直接返回成功失败，而是返回添加成功的规则列表
     */
    protected List<T> addRules(List<T> rulesToBeAdded){
        List<T> addedList=new ArrayList<>();
        for(T rule:rulesToBeAdded){
            if(addRule(rule))
                addedList.add(rule);
        }
        return addedList;
    }

    protected abstract boolean removeRule(T rule);

    /**
     * 注意，该函数非原子操作，可能部分成功，部分失败
     * @param rulesToBeRemoved
     * @return  不直接返回成功失败，而是返回添加删除的规则列表
     */
    protected List<T> removeRules(List<T> rulesToBeRemoved){
        List<T> removedList=new ArrayList<>();
        for(T rule:rulesToBeRemoved){
            if(removeRule(rule))
                removedList.add(rule);
        }
        return removedList;
    }

    protected List<T> getRules(){
        return new ArrayList<T>(rules);
    }

    /**
     * aof的添加和删除规则只用于{@link AOFWriter#restoreFromLog()}恢复使用
     * 其与{@link Maintainer#addRule(Object)}和{@link Maintainer#removeRule(Object)}
     * 的区别在于在恢复时不会写日志，而普通的添加会写日志
     * @param rule
     */
    protected void aofAddRule(T rule){
        rules.add(rule);
    };

    protected void aofRemoveRule(T rule){
        rules.remove(rule);
    };

    /**
     * 返回容器中拥有的记录数，以免调用{@link Maintainer#getRules()}然后再获取size，减少一次list的构造
     * @return  记录条数
     */
    protected int getSize(){
        return rules.size();
    }

}
