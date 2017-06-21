package utils;

import java.io.File;
import java.io.IOException;

/**
 * Created by swqsh on 2017/6/20.
 */
public class MakeDir {
    public static void main(String[] args) throws IOException {
        String currentPath=System.getProperty("user.dir");
        String target=currentPath+"\\purgeroot";
        File directory=new File(target);
        if(!directory.exists()||!directory.isDirectory()){
            boolean createSuccess=directory.mkdir();
            if(!createSuccess)
                System.out.println("create directory:"+target+" failed");
            else
                System.out.println("create directory successfully");
        }
        /*String[] files=directory.list();
        String deploymentFileName=target+"\\deployment";
        //String rules=target+"\\rules";
        boolean contained=false;
        for(String file:files){
            System.out.println(directory.getAbsolutePath());
            System.out.println(file);
        }
        /*
        if(!contained) {
            File deployment = new File(deploymentFileName);
            deployment.createNewFile();
        }*/
    }
}
