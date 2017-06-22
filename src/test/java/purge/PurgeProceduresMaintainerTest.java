package purge;

import org.apache.log4j.Logger;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by swqsh on 2017/6/22.
 */
public class PurgeProceduresMaintainerTest {

    String currentPath="D:\\repository\\workspace\\voltdbApplication\\";
    Path proceduresPath= Paths.get(currentPath,"purgeroot","procedures");
    Logger logger=Logger.getLogger(PurgeProceduresMaintainerTest.class);

    @Test
    public void writePurgeProceduresToFile() throws Exception {
        assertTrue(Files.exists(proceduresPath));
        PurgeProceduresMaintainer maintainer=new PurgeProceduresMaintainer(proceduresPath.toString());
        PurgeVoltProcedure procedure=new PurgeVoltProcedure();
        procedure.setProcedureName("who");
        procedure.setParams(new String[]{"are","you"});
        maintainer.addRule(procedure);
        maintainer.writeRulesToFile();
    }


    @org.junit.Test
    public void getPurgeProcedures() throws Exception {


        logger.info(proceduresPath.toString());
        logger.info(Files.exists(proceduresPath));
        assertTrue(Files.exists(proceduresPath));

        PurgeProceduresMaintainer maintainer=new PurgeProceduresMaintainer(proceduresPath.toString());
        logger.info(maintainer.getRules());
    }

    @Test
    public void removePurgeVoltProcedure() throws Exception {
        assertTrue(Files.exists(proceduresPath));
        PurgeProceduresMaintainer maintainer=new PurgeProceduresMaintainer(proceduresPath.toString());
        PurgeVoltProcedure procedure=new PurgeVoltProcedure();
        procedure.setProcedureName("who");
        procedure.setParams(new String[]{"are","you"});
        boolean removed=maintainer.removeRule(procedure);
        logger.info(removed);
        maintainer.writeRulesToFile();
    }

    @Test
    public void addPurgeVoltProcedures() throws Exception {
        PurgeProceduresMaintainer maintainer=new PurgeProceduresMaintainer(proceduresPath.toString());
        List<PurgeVoltProcedure> procedures=new ArrayList<>();
        procedures.add(PurgeVoltProcedure.parseProcedureFromStr("shut up"));
        procedures.add(PurgeVoltProcedure.parseProcedureFromStr("i like eatting meat"));
        List<PurgeVoltProcedure> added=maintainer.addRules(procedures);
        logger.info(added);
        maintainer.writeRulesToFile();
    }

    @Test
    public void removePurgeVoltProcedures() throws Exception {
        PurgeProceduresMaintainer maintainer=new PurgeProceduresMaintainer(proceduresPath.toString());
        List<PurgeVoltProcedure> procedures=new ArrayList<>();
        procedures.add(PurgeVoltProcedure.parseProcedureFromStr("shut up"));
        procedures.add(PurgeVoltProcedure.parseProcedureFromStr("i like eatting meat"));
        List<PurgeVoltProcedure> removed=maintainer.removeRules(procedures);
        logger.info(removed);
        maintainer.writeRulesToFile();
    }

}