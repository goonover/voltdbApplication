package utils;

import mssql2voltdb.utils.SQLCommandUtils;
import org.voltdb.VoltTable;
import org.voltdb.client.Client;
import org.voltdb.client.ClientResponse;
import org.voltdb.client.ProcCallException;

import java.io.IOException;

/**
 * Created by swqsh on 2017/6/22.
 */
public class Spacesplit {

    public static void main(String[] args) throws IOException, InterruptedException {
        /*String value="     ";
        String[] res=value.split("\\s+");
        for(String per:res){
            System.out.println(per);
        }
        System.out.println(res.length);*/
        String hello="hello";
        System.out.println(hello.toString());
       /* Client client= SQLCommandUtils.getClient("10.201.64.23");
        try {
            ClientResponse response=client.callProcedure("getcompany",new Object[0]);
            System.out.println(response.getStatusString());
            if(response.getStatus()==ClientResponse.SUCCESS){
                for(VoltTable table:response.getResults()){
                    System.out.println(table.toJSONString());
                }
            }
        } catch (ProcCallException e) {
            e.printStackTrace();
        }finally {
            client.drain();
            client.close();
        }*/
    }
}
