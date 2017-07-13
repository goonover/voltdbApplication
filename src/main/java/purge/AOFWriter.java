package purge;

import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 增量日志的读写都是通过该类完成，日志格式为：
 * timestamp,{@link AOFOperation#ordinal()},value
 * Created by swqsh on 2017/7/13.
 */
public class AOFWriter {

    private SQLRulesMaintainer sqlRulesMaintainer;
    private PurgeProceduresMaintainer purgeProceduresMaintainer;
    //是否已经经过初始化
    private AtomicBoolean restored = new AtomicBoolean(false);
    private Logger logger = Logger.getLogger(AOFWriter.class);

    //所有跟增量日志有关的写操作都应该由fileWriter来完成
    private BufferedWriter fileWriter;

    //增量日志路径
    private Path aofPath;

    public AOFWriter(String pathName){
        aofPath = Paths.get(pathName);
    }


    public void setSqlRulesMaintainer(SQLRulesMaintainer sqlRulesMaintainer) {
        this.sqlRulesMaintainer = sqlRulesMaintainer;
    }

    public void setPurgeProceduresMaintainer(PurgeProceduresMaintainer purgeProceduresMaintainer) {
        this.purgeProceduresMaintainer = purgeProceduresMaintainer;
    }

    /**
     * 从增量日志中恢复，用{@link AOFWriter#restored}防止该方法被调用多次
     */
    public void restoreFromLog() throws IOException{
        if(!restored.getAndSet(true))
           logger.info("ready to restoreFormLog");
        if(purgeProceduresMaintainer==null||sqlRulesMaintainer==null){
            logger.error("restore failed:maintainer must not be null, exit!");
            System.exit(-1);
        }

        //第一次初始化，新建日志文档
        if(!Files.exists(aofPath))
                Files.createFile(aofPath);
        BufferedReader reader = Files.newBufferedReader(aofPath);
        String perLine;
        while ((perLine = reader.readLine())!=null){
            String[] params = perLine.split(",");
            int ordinal = Integer.parseInt(params[1]);
            switch (AOFOperation.fromOrdinal(ordinal)){
                case ADDPROCEDURE:
                    PurgeVoltProcedure procedureToBeAdd = PurgeVoltProcedure.parseProcedureFromStr(params[2]);
                    purgeProceduresMaintainer.aofAddRule(procedureToBeAdd);
                    break;
                case REMOVEPROCEDURE:
                    PurgeVoltProcedure procedureToBeMoved = PurgeVoltProcedure.parseProcedureFromStr(params[2]);
                    purgeProceduresMaintainer.aofRemoveRule(procedureToBeMoved);
                    break;
                case ADDSQL:
                    SQLRule sqlToBeAdd = new SQLRule(params[2]);
                    sqlRulesMaintainer.aofAddRule(sqlToBeAdd);
                    break;
                case REMOVESQL:
                    SQLRule sqlToBeRemoved = new SQLRule(params[2]);
                    sqlRulesMaintainer.aofRemoveRule(sqlToBeRemoved);
                    break;
                default:
                    break;
            }
        }
        reader.close();
    }

    /**
     * 把规则写到增量日志中
     * @param operationType     要添加规则的类型
     * @param rule      规则的String类型表示
     */
    public void recordLog(AOFOperation operationType,String rule) throws IOException {
        if(fileWriter == null)
            fileWriter = Files.newBufferedWriter(aofPath);
        String record = System.currentTimeMillis()+","+operationType.ordinal()+","+rule+"\n";
        fileWriter.write(record);
        fileWriter.flush();
    }

    //TODO:移除所有getter，只是方便测试用
    public SQLRulesMaintainer getSqlRulesMaintainer() {
        return sqlRulesMaintainer;
    }

    public PurgeProceduresMaintainer getPurgeProceduresMaintainer() {
        return purgeProceduresMaintainer;
    }
}

/**
 * 增量日志的操作类型
 */
enum AOFOperation{

    ADDSQL,REMOVESQL,ADDPROCEDURE,REMOVEPROCEDURE;

    private static final Map<Integer,AOFOperation> lookup = new HashMap<>();

    static{
        for(AOFOperation operation: EnumSet.allOf(AOFOperation.class)){
            lookup.put(operation.ordinal(),operation);
        }
    }

    public static AOFOperation fromOrdinal(int ordinal){
        return lookup.get(ordinal);
    }

}
