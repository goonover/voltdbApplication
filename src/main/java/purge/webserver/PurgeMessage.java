package purge.webserver;

import java.nio.ByteBuffer;

/**
 * Created by swqsh on 2017/6/19.
 */
public class PurgeMessage implements Message{

    PurgeMessageType type;

    String value;

    public PurgeMessage(){
        type=PurgeMessageType.PLAINTEXT;
        value="";
    }

    public PurgeMessage(PurgeMessageType type,String value){
        this.type=type;
        this.value=value;
    }

    public PurgeMessage type(PurgeMessageType type){
        this.type=type;
        return this;
    }

    public PurgeMessage value(String value){
        this.value=value;
        return this;
    }

    /**
     * 消息戳的byte数组长度，
     * 总长度------>4
     * type:PurgeMessageType.ordinal---->4
     * value长度------->4
     * value长度----->value.size()
     * 4+4+4+value.size()
     * @return
     */
    public int getSerializeSize(){
        return 4+4+4+value.length();
    }

    /**
     * src的格式应该与发送的消息戳格式一致
     * @param src
     */
    @Override
    public void initFormByteArray(byte[] src) {
        ByteBuffer byteBuffer= ByteBuffer.wrap(src);
        //byteBuffer.getInt();
        //解析类型
        int typeNum=byteBuffer.getInt();
        type=PurgeMessageType.fromOrdinal(typeNum);
        //解析String
        int size=byteBuffer.getInt();
        byte[] temp=new byte[size];
        byteBuffer.get(temp);
        value=new String(temp);
    }

    @Override
    public byte[] flattenToByteArray() {
        ByteBuffer buffer=ByteBuffer.allocate(getSerializeSize());
        buffer.putInt(getSerializeSize()-4);
        buffer.putInt(type.ordinal());
        buffer.putInt(value.length());
        buffer.put(value.getBytes());
        return buffer.array();
    }
}

