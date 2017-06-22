package purge.webserver;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * 消息类型
 * Created by swqsh on 2017/6/19.
 */
public enum  PurgeMessageType {
    GETALL,REMOVESQLS,ADDSQLS,SHOWPROCEDURES,ADDPROCEDURES,REMOVEPROCEDURES,PLAINTEXT,SHUTDOWN;

    private static final Map<Integer,PurgeMessageType> lookup=new HashMap<>();

    static {
        for(PurgeMessageType messageType: EnumSet.allOf(PurgeMessageType.class)){
            lookup.put(messageType.ordinal(),messageType);
        }
    }

    public static PurgeMessageType fromOrdinal(int ordinal){
        return lookup.get(ordinal);
    }

}
