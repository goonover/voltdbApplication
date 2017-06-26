package purge;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by swqsh on 2017/6/26.
 */
public class SQLRuleTest {
    @Test
    public void toStringTest() throws Exception {
        SQLRule rule=new SQLRule("hello");
        System.out.println(rule);
    }

}