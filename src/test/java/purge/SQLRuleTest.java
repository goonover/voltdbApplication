package purge;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

/**
 * Created by swqsh on 2017/6/26.
 */
public class SQLRuleTest {
    @Test
    public void toStringTest() throws Exception {
        String sql=SQLGenerator.deleteElderThan("company","create_date","second","30");
        System.out.println(sql);
    }

    @Test
    public void convertWithStr(){
        SQLRule rule = new SQLRule("delete * from company;");
        String val = rule.toString();
        SQLRule rule1 = new SQLRule(val);
        assertTrue(rule.equals(rule1));
    }

}