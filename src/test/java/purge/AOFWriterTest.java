package purge;

import org.junit.Before;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by swqsh on 2017/7/13.
 */
public class AOFWriterTest {

    private AOFWriter aofWriter;
    private SQLRulesMaintainer sqlRulesMaintainer;
    private PurgeProceduresMaintainer proceduresMaintainer;

    @Before
    public void init(){
        System.out.println(System.getProperty("user.dir"));
        Path path = Paths.get(System.getProperty("user.dir"),"\\src\\main\\resources","aof.log");
        aofWriter = new AOFWriter(path.toString());
        sqlRulesMaintainer = new SQLRulesMaintainer("",false,aofWriter);
        proceduresMaintainer = new PurgeProceduresMaintainer("",false,aofWriter);
        aofWriter.setPurgeProceduresMaintainer(proceduresMaintainer);
        aofWriter.setSqlRulesMaintainer(sqlRulesMaintainer);
    }

    @Test
    public void restoreFromLog() throws Exception {
        aofWriter.restoreFromLog();
        assert (aofWriter.getPurgeProceduresMaintainer().getRules().size()==0);
        assert (aofWriter.getSqlRulesMaintainer().getRules().size() == 2);
    }

    @Test
    public void recordLog() throws Exception {
        sqlRulesMaintainer.addRule(new SQLRule("select * from company;"));
        sqlRulesMaintainer.removeRule(new SQLRule("select * from yoyo"));
        sqlRulesMaintainer.addRule(new SQLRule("delete from company;"));
        proceduresMaintainer.addRule(PurgeVoltProcedure.parseProcedureFromStr("get 1 2 3"));
        proceduresMaintainer.removeRule(PurgeVoltProcedure.parseProcedureFromStr("insert 3 2 1"));
        proceduresMaintainer.removeRule(PurgeVoltProcedure.parseProcedureFromStr("get 1 2 3"));
    }

}