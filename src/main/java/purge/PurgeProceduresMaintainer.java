package purge;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;

import static purge.PurgeVoltProcedure.parseProcedureFromStr;

/**
 * 解析存储清理服务在清理时应该调用的存储过程，并定期将期更新数据回写到存储过程存储列表中
 * Created by swqsh on 2017/6/22.
 */
public class PurgeProceduresMaintainer extends Maintainer<PurgeVoltProcedure>{

    public PurgeProceduresMaintainer(String proceduresPathName){
        super(proceduresPathName);
    }

    public PurgeProceduresMaintainer(String proceduresPathName,boolean allowDuplicated){
        super(proceduresPathName,allowDuplicated);
    }

    /**
     * 从文档读取相关存储过程信息，如在此过程出现异常，系统直接退出
     * @throws IOException
     */
    @Override
    void readRulesFromFile() throws IOException {
        BufferedReader reader= Files.newBufferedReader(rulePath);
        String line;
        while ((line=reader.readLine())!=null){
            PurgeVoltProcedure procedure=parseProcedureFromStr(line);
            if(procedure!=null){
                rules.add(procedure);
            }
        }
        reader.close();
    }

    @Override
    public boolean addRule(PurgeVoltProcedure procedureToBeAdded) {
        if(procedureToBeAdded.getProcedureName().contains(" "))
            return false;
        if(!allowDuplicated&&rules.contains(procedureToBeAdded))
            return false;
        boolean addSuccessfully=rules.add(procedureToBeAdded);
        modified|=addSuccessfully;
        return addSuccessfully;
    }

    @Override
    public boolean removeRule(PurgeVoltProcedure procedureToBeRemoved) {
        boolean removedSuccessfully=false;
        PurgeVoltProcedure target=null;
        for(PurgeVoltProcedure aProcedure:rules){
            if(aProcedure.equals(procedureToBeRemoved)){
                target=aProcedure;
                break;
            }
        }
        if(target!=null)
            removedSuccessfully=rules.remove(target);
        modified|=removedSuccessfully;
        return removedSuccessfully;
    }

}
