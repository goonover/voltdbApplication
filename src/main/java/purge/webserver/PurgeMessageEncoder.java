package purge.webserver;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * Created by swqsh on 2017/6/19.
 */
public class PurgeMessageEncoder extends MessageToByteEncoder<PurgeMessage> {

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext,
                          PurgeMessage purgeMessage, ByteBuf byteBuf) throws Exception {
        ByteBuf buf= Unpooled.buffer();
        buf.writeBytes(purgeMessage.flattenToByteArray());
        channelHandlerContext.writeAndFlush(buf);
    }
}
