package purge;

import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.*;

/**
 * Created by swqsh on 2017/6/25.
 */
public class PurgeVoltProcedureTest {
    @Test
    public void hashCodeTest() throws Exception {
        PurgeVoltProcedure procedure1=new PurgeVoltProcedure();
        procedure1.setProcedureName("hello");
        procedure1.setParams(new String[]{"1","2"});

        PurgeVoltProcedure procedure2=new PurgeVoltProcedure();
        procedure2.setProcedureName("hello");
        procedure2.setParams(new String[]{"1","2"});

        assertTrue(procedure1.equals(procedure2));
        assertTrue(procedure1.hashCode()==procedure2.hashCode());
    }

}