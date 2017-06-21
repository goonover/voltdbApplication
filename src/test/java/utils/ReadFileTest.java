package utils;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by swqsh on 2017/6/15.
 */
public class ReadFileTest {
    public static void main(String[] args) throws IOException {
        //String path=System.getProperty("user.dir")+"\\utils\\src\\main\\resources\\demo.properties";
       //String path="D:\\helloWorld.txt";
        //ScheduledExecutorService executorService= Executors.newScheduledThreadPool(1);
        String source="hello;kitty;world;";
        String[] target=source.split(";");
        System.out.println(target.length);
        for(String per:target){
            System.out.println(per);
        }
    }
}
