package com.dianping.trek.server;

import java.util.Map;
import java.util.Properties;

import com.dianping.trek.decoder.WUPDecoder;
import com.dianping.trek.handler.ApplicationDistributionHandler;
import com.dianping.trek.handler.LogFlushHandler;
import com.dianping.trek.spi.Application;
import com.dianping.trek.spi.TrekContext;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class TrekServer {
    
    private int port;

    public TrekServer(int port) {
        this.port = port;
    }
    
    public void run() throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup(); // (1)
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap(); // (2)
            b.group(bossGroup, workerGroup)
             .channel(NioServerSocketChannel.class) // (3)
             .childHandler(new FilterChannelChain())
             .option(ChannelOption.SO_BACKLOG, 128)          // (5)
             .childOption(ChannelOption.SO_KEEPALIVE, true); // (6)

            // Bind and start to accept incoming connections.
            ChannelFuture f = b.bind(port).sync(); // (7)

            // Wait until the server socket is closed.
            // In this example, this does not happen, but you can do that to gracefully
            // shut down your server.
            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
    
    class FilterChannelChain extends ChannelInitializer<SocketChannel> {

        @Override
        protected void initChannel(SocketChannel ch) throws Exception {
            // TODO Auto-generated method stub
            ch.pipeline().addLast(new WUPDecoder(1024 * 1024, 8, 4, -4, 0, true));
            ch.pipeline().addLast(new ApplicationDistributionHandler());
            ch.pipeline().addLast(new LogFlushHandler());
        }
    }

    public static void main(String[] args) throws Exception {
        Properties prop = new Properties();
        prop.load(TrekServer.class.getClassLoader().getResourceAsStream("config.properties"));
        int port;
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        } else {
            port = Integer.parseInt(prop.getProperty("trek.port", "8080"));
        }
        String basePath = prop.getProperty("trek.basePath", "/tmp");
        TrekContext.INSTANCE.addApplication("user_event", "appKey");
        TrekContext.INSTANCE.SetBasePath(basePath);
        new TrekServer(port).run();
    }
}
