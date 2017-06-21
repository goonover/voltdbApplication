package purge.webserver;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import purge.PurgeService;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by swqsh on 2017/6/19.
 */
public class PurgeServerHandler extends SimpleChannelInboundHandler<PurgeMessage>{

    private PurgeService purgeService;
    private ChannelHandlerContext ctx;

    public PurgeServerHandler(PurgeService service){
        this.purgeService=service;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        PurgeMessage message=new PurgeMessage();
        this.ctx=ctx;
        message.type(PurgeMessageType.PLAINTEXT);
        message.value("Welcome to VoltDb Purge Service");
        ctx.writeAndFlush(message);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext,
                                PurgeMessage message) throws Exception {
        /**
         * 普通文本消息，只是打印出来，用于测试
         */
        if(message.type==PurgeMessageType.PLAINTEXT){
            printPlainText(message);
        }
        /**
         * 关闭类型消息，关闭服务器
         */
        if(message.type==PurgeMessageType.SHUTDOWN){
            shutdown();
        }

        //把所有规则发回到客户端
        if(message.type==PurgeMessageType.GETALL){
            sendAllRulesToClient(message);
        }

        //移除规则
        if(message.type==PurgeMessageType.REMOVE){
            removeRules(message);
        }

        //添加规则
        if(message.type==PurgeMessageType.ADD){
            addRules(message);
        }
    }

    //处理普通文本类型
    private void printPlainText(PurgeMessage message){
        System.out.println(message.value);
    }

    private void shutdown(){
        System.out.println("server is shutdown by remote address:["+
                ctx.channel().remoteAddress()+"]");
        ctx.channel().close();
        ctx.channel().parent().close();
    }

    //把所有的规则返回到客户端
    private void sendAllRulesToClient(PurgeMessage message){
        String res="";
        for(String aRule:purgeService.getRules()){
            if(!aRule.endsWith(";"))
                aRule+=";";
            res+=aRule;
        }
        message.value(res);
        ctx.writeAndFlush(message);
    }

    private void removeRules(PurgeMessage message){
        String rule=message.value.trim();
        //boolean removed=purgeService.removeRules(Arrays.asList(rule.split(";")));
        boolean removed=purgeService.removeRule(rule);
        message.type(PurgeMessageType.PLAINTEXT);
        if(removed){
            message.value("rule:\""+rule+"\" has been removed successfully!");
        }else{
            message.value("rule:\""+rule+"\" can't be found, please check if the spelling is correct");
        }
        ctx.writeAndFlush(message);
    }

    private void addRules(PurgeMessage message){
        String source=message.value.trim();
        String[] temp=source.split(";");
        List<String> rulesToBeAdded=new ArrayList<>();
        for(String aRule:temp){
            rulesToBeAdded.add(aRule+";");
        }
        boolean added=purgeService.addRules(rulesToBeAdded);
        message.type(PurgeMessageType.PLAINTEXT);
        if(added){
            message.value("rules:\""+source+"\" have been added successfully");
        }else{
            message.value("try to add rules:\""+source+"\" failed,please try again");
        }
        ctx.writeAndFlush(message);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
