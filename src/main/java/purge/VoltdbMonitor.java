package purge;

import org.apache.log4j.Logger;
import org.voltdb.VoltTable;
import org.voltdb.client.Client;
import org.voltdb.client.ClientResponse;
import org.voltdb.client.ProcCallException;

import java.io.IOException;

/**
 * 监视voltdb集群中各个服务器的资源使用情况，当目前已占用内存已超过阈值的时候，就报警
 * Created by swqsh on 2017/6/16.
 */
public class VoltdbMonitor implements Runnable{

    private Client client;
    private double threshold;
    private Logger logger=Logger.getLogger(VoltdbMonitor.class);
    private boolean dangerous=false;
    private PurgeService purgeService;

    public VoltdbMonitor(PurgeService purgeService,Client client,double threshold){
        this.purgeService=purgeService;
        this.client=client;
        if(threshold>=0)
            this.threshold=threshold;
        else
            this.threshold=0.6;
    }

    @Override
    public void run() {
        try {
            ClientResponse response=client.callProcedure("@Statistics",
                    "memory",1);
            if(response.getStatus()!=ClientResponse.SUCCESS){
                logger.info(response.getStatusString());
            }else{
                for(VoltTable t:response.getResults()){
                    t.advanceRow();
                    int rss= (int) t.getLong("RSS");
                    int physical=(int) t.getLong("physicalmemory");
                    if(rss>physical*threshold) {
                        dangerous = true;
                        break;
                    }
                }
            }
            if(dangerous){
                purge();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ProcCallException e) {
            e.printStackTrace();
        }
    }

    /**
     * 当内存报警时，调用{@link PurgeService#purge()}进行内存清理
     */
    public void purge(){
        purgeService.purge();
    }

}
