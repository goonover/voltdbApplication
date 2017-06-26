package purge.webserver;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import purge.PurgeService;
import purge.PurgeVoltProcedure;
import purge.SQLRule;

import java.util.ArrayList;
import java.util.Arrays;
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
        switch (message.type){
            /**
             * 普通文本消息，只是打印出来，用于测试
             */
            case PLAINTEXT:
                printPlainText(message);
                break;
            //添加sql规则
            case ADDSQLS:
                addSqlRules(message);
                break;
            //删除sql规则
            case REMOVESQLS:
                removeSqlRules(message);
            //把所有规则发回到客户端
            case GETALL:
                sendAllRulesToClient(message);
                break;
            //把所有存储过程及参数发回到客户端
            case SHOWPROCEDURES:
                sendAllProceduresToClient(message);
                break;
            //添加存储过程
            case ADDPROCEDURES:
                addProcedures(message);
                break;
            case REMOVEPROCEDURES:
                removeProcedures(message);
                break;
            case SHUTDOWN:
                shutdown();
                break;
            default:
                break;
        }

    }

    //处理普通文本类型，应该永远不会被调用
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
        String res="All the sql rules are shown below:\n";
        for(SQLRule aRule:purgeService.getSqlRules()){
            String statement=aRule.getStatement();
            if(!statement.endsWith(";"))
                statement+=";";
            res+=statement+"\n";
        }
        message.type(PurgeMessageType.PLAINTEXT);
        message.value(res);
        ctx.writeAndFlush(message);
    }

    private void sendAllProceduresToClient(PurgeMessage message){
        String res="ProcedureName           params\n";
        for(PurgeVoltProcedure procedure:purgeService.getProcedureRules()){
            res+=procedure.getProcedureName()+"         "+Arrays.asList(procedure.getParams())+"\n";
        }
        message.type(PurgeMessageType.PLAINTEXT);
        message.value(res);
        ctx.writeAndFlush(message);
    }

    private void removeSqlRules(PurgeMessage message){
        String rule=message.value.trim();
        String[] temp=rule.split(";");
        List<SQLRule> sqlRulesToBeRemoved=new ArrayList<>();
        for(String aRule:temp){
            sqlRulesToBeRemoved.add(new SQLRule(aRule.trim()+";"));
        }
        List<SQLRule> removed=purgeService.removeSqlRules(sqlRulesToBeRemoved);
        message.type(PurgeMessageType.PLAINTEXT);
        message.value("rule:\""+removed+"\" has been removed successfully!");

        ctx.writeAndFlush(message);
    }

    private void addSqlRules(PurgeMessage message){
        String source=message.value.trim();
        String[] temp=source.split(";");
        List<SQLRule> rulesToBeAdded=new ArrayList<>();
        for(String aRule:temp){
            rulesToBeAdded.add(new SQLRule(aRule.trim()+";"));
        }
        List<SQLRule> added=purgeService.addSqlRules(rulesToBeAdded);
        message.type(PurgeMessageType.PLAINTEXT);
        message.value("rules:\""+added+"\" have been added successfully");

        ctx.writeAndFlush(message);
    }

    private void addProcedures(PurgeMessage message){
        List<PurgeVoltProcedure> rulesToBeAdded=getProcedureListFromMessage(message);
        List<PurgeVoltProcedure> added=purgeService.addProcedureRules(rulesToBeAdded);
        message.type(PurgeMessageType.PLAINTEXT);
        message.value("procedures:\""+added+"\" have been added successfully");
        ctx.writeAndFlush(message);
    }

    private void removeProcedures(PurgeMessage message){
        List<PurgeVoltProcedure> rulesToBeRemoved=getProcedureListFromMessage(message);
        List<PurgeVoltProcedure> removed=purgeService.removeProcedureRules(rulesToBeRemoved);
        message.type(PurgeMessageType.PLAINTEXT);
        message.value("procedures:\""+removed+"\" have been removed successfully");
        ctx.writeAndFlush(message);
    }

    private List<PurgeVoltProcedure> getProcedureListFromMessage(PurgeMessage message){
        String source=message.value.trim();
        String[] temp=source.split(";");
        List<PurgeVoltProcedure> result=new ArrayList<>();
        for(String aRule:temp){
            result.add(PurgeVoltProcedure.parseProcedureFromStr(aRule.trim()));
        }
        return result;
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
