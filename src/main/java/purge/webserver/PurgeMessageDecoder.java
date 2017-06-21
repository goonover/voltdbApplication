package purge.webserver;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * Created by swqsh on 2017/6/19.
 */
public class PurgeMessageDecoder extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf in, List<Object> list) throws Exception {
        if(in.readableBytes()<4)
            return;
        int size=in.readInt();
        if(in.readableBytes()<size) {
            in.resetReaderIndex();
            return;
        }
        byte[] temp=new byte[size];
        in.readBytes(temp);
        PurgeMessage purgeMessage=new PurgeMessage();
        purgeMessage.initFormByteArray(temp);
        list.add(purgeMessage);
    }
}
