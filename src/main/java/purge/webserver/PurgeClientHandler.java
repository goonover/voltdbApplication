package purge.webserver;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * Created by swqsh on 2017/6/19.
 */
public class PurgeClientHandler extends SimpleChannelInboundHandler<PurgeMessage> {

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, PurgeMessage message) throws Exception {

        if(message.type==PurgeMessageType.PLAINTEXT){
            System.out.println(message.value);
        }

        //客户端收到getAll类型的均为服务器对客户端的响应
        if(message.type==PurgeMessageType.GETALL){
            String[] rules=message.value.split(";");
            for(String rule:rules)
                System.out.println(rule);
        }

    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
