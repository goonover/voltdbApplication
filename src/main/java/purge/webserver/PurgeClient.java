package purge.webserver;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * exit----->客户端退出
 * shutdown------>关闭服务器
 * show rules------>显示服务器规则列表中所有规则
 * Created by swqsh on 2017/6/19.
 */
public class PurgeClient {

    String ip="127.0.0.1";
    int port=5089;

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
            ChannelFuture lastFuture=null;
            Channel channel=future.channel();

            System.out.println("you can enter \"help\" for usage");
            //连接已建立,下面为具体对各个输入的具体处理
            BufferedReader reader=new BufferedReader(new InputStreamReader(System.in));
            try {
                while ((line=reader.readLine())!=null){
                    if(line.length()<=0)
                        continue;

                    if("exit".equals(line.toLowerCase())) {
                        channel.close();
                        break;
                    }

                    PurgeMessage message=new PurgeMessage();
                    switch(line.toLowerCase()){
                        case "shutdown":
                            message.type(PurgeMessageType.SHUTDOWN);
                            break;
                        case "show rules":
                            message.type(PurgeMessageType.GETALL);
                            break;
                        case "remove rule":
                            System.out.println("Please enter the rule to be removed:");
                            line=reader.readLine();
                            if(line.length()<=0)
                                continue;
                            message.type(PurgeMessageType.REMOVE);
                            message.value(line);
                            break;
                        case "add rule":
                            System.out.println("Please enter the rule to be added:");
                            line=reader.readLine();
                            if(line.length()<=0)
                                continue;
                            message.type(PurgeMessageType.ADD);
                            message.value(line);
                            break;
                        case "help":
                            printUsage();
                        default:
                            message.type(PurgeMessageType.PLAINTEXT);
                            message.value(line);
                            break;
                    }

                    lastFuture=channel.writeAndFlush(message);
                    if(lastFuture!=null){
                        lastFuture.sync();
                        if(line.toLowerCase().equals("shutdown"))
                            break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            workerGroup.shutdownGracefully();
        }
    }

    //帮助
    public void printUsage(){
        System.out.println("exit:close the PurgeService Client.");
        System.out.println("shutdown:close the PurgeService and this Client.");
        System.out.println("show rules:try to obtain all rules from the server.");
        System.out.println("remove rule:try to remove a rule from the server.");
        System.out.println("add rule:try to add a rule to the server.");
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
