package purge;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by swqsh on 2017/7/13.
 */
public class PurgeServiceTest {
    @Test
    public void start() throws Exception {
        String path=System.getProperty("user.dir")+"\\src\\main\\resources\\conf.properties";
        PurgeService purgeService = new PurgeService(path);
        purgeService.start();
    }

}