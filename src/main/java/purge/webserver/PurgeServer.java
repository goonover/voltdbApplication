package purge.webserver;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import purge.PurgeService;

/**
 * 用于PurgeService与Client交流的服务器
 * Created by swqsh on 2017/6/19.
 */
public class PurgeServer {

    private PurgeService service;

    private String ip="127.0.0.1";
    private int port=5089;

    public PurgeServer(PurgeService service){
        this.service=service;
    }

    public PurgeServer(PurgeService service,String ip,int port){
        this.service=service;
        this.ip=ip;
        this.port=port;
    }

    public void start(){
        EventLoopGroup bossGroup=new NioEventLoopGroup(1);
        EventLoopGroup workerGroup=new NioEventLoopGroup(3);
        try{
            ServerBootstrap bootstrap=new ServerBootstrap();
            bootstrap.channel(NioServerSocketChannel.class)
                    .group(bossGroup,workerGroup)
                    .handler(new LoggingHandler())
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast(new PurgeMessageEncoder(),
                                    new PurgeMessageDecoder(),new PurgeServerHandler(service));
                        }
                    });
            ChannelFuture future=bootstrap.bind(port).sync();
            //阻塞，直到channel关闭
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
            /**此时，PurgeServer已经关闭，接下来应调用{@link PurgeService#shutdown()}
             *来控制与其相关的{@link purge.WatchDog}和{@link purge.OperationScanner}关闭
             */
            service.shutdown();
        }
    }


}
