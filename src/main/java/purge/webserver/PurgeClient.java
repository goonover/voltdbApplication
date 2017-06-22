package purge.webserver;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.io.*;

/**
 * exit----->客户端退出
 * shutdown------>关闭服务器
 * show rules------>显示服务器规则列表中所有规则
 * Created by swqsh on 2017/6/19.
 */
public class PurgeClient {

    String ip="127.0.0.1";
    int port=5089;

    //与服务器的channel
    private Channel channel;


    public PurgeClient(){};

    public PurgeClient(String ip,int port){
        this.ip=ip;
        this.port=port;
    }

    /**
     * client开启服务
     * client发起与服务器的连接，建立连接后，读取控制台的命令提供服务
     */
    public void start(){
        EventLoopGroup workerGroup=new NioEventLoopGroup();
        try{
            Bootstrap bootstrap=new Bootstrap();
            bootstrap.group(workerGroup)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            ChannelPipeline pipeline=socketChannel.pipeline();
                            pipeline.addLast(new PurgeMessageEncoder());
                            pipeline.addLast(new PurgeMessageDecoder());
                            pipeline.addLast(new PurgeClientHandler());
                        }
                    });
            ChannelFuture future=bootstrap.connect(ip,port).sync();
            String line;
            this.channel=future.channel();

            try{
                readAndProcessCommandFromConsole();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            workerGroup.shutdownGracefully();
        }
    }

    private void readAndProcessCommandFromConsole() throws IOException, InterruptedException {
        System.out.println("you can enter \"help\" for usage");
        //连接已建立,下面为具体对各个输入的具体处理
        BufferedReader reader=new BufferedReader(new InputStreamReader(System.in));
        ChannelFuture lastFuture=null;
        String line;
        while ((line=reader.readLine())!=null){
            if(line.length()<=0)
                continue;

            if("exit".equals(line.toLowerCase())) {
                exit();
                break;
            }

            PurgeMessage message=new PurgeMessage();
            switch(line.toLowerCase()){
                case "shutdown":
                    shutdown(message);
                    break;
                case "show rules":
                    getAllSqlRules(message);
                    break;
                case "remove rules":
                    removeSqlRules(message,reader);
                    break;
                case "add rules":
                    addSqlRules(message,reader);
                    break;
                case "help":
                    printUsage();
                    break;
                case "add procedures":
                    addProcedureRules(message,reader);
                    break;
                case "remove procedures":
                    removeProcedureRules(message,reader);
                    break;
                case "show procedures":
                    showProcedures(message);
                    break;
                default:
                    message.type(PurgeMessageType.PLAINTEXT);
                    message.value(line);
                    break;
                }

                if(message.type!=PurgeMessageType.PLAINTEXT)
                    lastFuture=channel.writeAndFlush(message);
                else
                    unknownCommand(message);

                if(lastFuture!=null){
                    lastFuture.sync();
                    if(line.toLowerCase().equals("shutdown"))
                        break;
                }
            }
    }

    private void exit(){
        System.out.println("client is ready to exit!");
        channel.close();
    }

    private void shutdown(PurgeMessage message){
        message.type(PurgeMessageType.SHUTDOWN);
    }

    private void getAllSqlRules(PurgeMessage message){
        message.type(PurgeMessageType.GETALL);
    }

    private void removeSqlRules(PurgeMessage message,BufferedReader reader) throws IOException{
        System.out.println("Please enter the sql rule to be removed:");
        String line=reader.readLine();
        if(line.length()<=0)
            return;
        message.type(PurgeMessageType.REMOVESQLS);
        message.value(line);
    }

    private void addSqlRules(PurgeMessage message,BufferedReader reader) throws IOException {
        System.out.println("Please enter the sql rules to be added:");
        String line=reader.readLine();
        if(line.length()<=0)
            return;
        message.type(PurgeMessageType.ADDSQLS);
        message.value(line);
    }

    private void addProcedureRules(PurgeMessage message,BufferedReader reader) throws IOException {
        System.out.println("Please enter the procedures and params to be added");
        String line=reader.readLine();
        if(line.length()<0)
            return;
        message.type(PurgeMessageType.ADDPROCEDURES);
        message.value(line);
    }

    private void removeProcedureRules(PurgeMessage message,BufferedReader reader) throws IOException {
        System.out.println("Please enter the procedures and params to be removed");
        String line=reader.readLine();
        if(line.length()<0)
            return;
        message.type(PurgeMessageType.REMOVEPROCEDURES);
        message.value(line);
    }

    private void showProcedures(PurgeMessage message){
        message.type(PurgeMessageType.SHOWPROCEDURES);
    }

    //PlainText只用于服务器向客户端发送，如客户端想发送PlainText格式的消息，则直接报错
    private void unknownCommand(PurgeMessage message){
        System.out.println("unknown command:"+message.value);
        System.out.println("Please enter the legal command");
    }

    //帮助
    private void printUsage(){
        System.out.println("exit:close the PurgeService Client.");
        System.out.println("shutdown:close the PurgeService and this Client.");
        System.out.println("show rules:try to obtain all sql rules from the server.");
        System.out.println("remove rules:try to remove one or multiple rules from the server.Rules are split by ';'.");
        System.out.println("add rules:try to add one or multiple rules to the server.Rules are split by ';'.");
        System.out.println("show procedures:try to obtain all procedures from the server.");
        System.out.println("add procedures:try to add one or multiple procedure to the server.Rules are split by ';'.");
        System.out.println("remove procedures:try to remove one or multiple procedure to the server.Rules are split by ';'.");
    }

    public static void main(String[] args){
        PurgeClient client=null;
        if(args.length==2){
            client=new PurgeClient(args[0],Integer.parseInt(args[1]));
        }else{
            client=new PurgeClient();
        }
        client.start();
    }

}
