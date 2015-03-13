package com.dianping.trek.server;

import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.dianping.trek.decoder.WUPDecoder;
import com.dianping.trek.handler.ApplicationDistributionHandler;
import com.dianping.trek.handler.WorkerThreadPool;
import com.dianping.trek.spi.Processor;
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
    private static Log LOG = LogFactory.getLog(TrekServer.class);
    private int port;
    private WorkerThreadPool workerPool;

    public TrekServer(int port) {
        this.port = port;
    }
    
    public void run() throws Exception {
        workerPool = new WorkerThreadPool();
        workerPool.refresh();
        
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
            ch.pipeline().addLast(new WUPDecoder(1024 * 1024, 8, 4, -4, 0, true));
            ch.pipeline().addLast(new ApplicationDistributionHandler());
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
        TrekContext.SetBasePath(basePath);
        String appJsonStr = prop.getProperty("trek.app");
        
        if (appJsonStr != null) {
            JSONArray appArray = new JSONArray(appJsonStr);
            for (int i = 0; i < appArray.length(); i++) {
                JSONObject appObject = appArray.getJSONObject(i);
                String name = (String)appObject.get("name");
                String key = (String)appObject.get("key");
                try {
                    String processorClassName = (String) appObject.get("processorClass");
                    @SuppressWarnings("unchecked")
                    Class<? extends Processor> processorClass = (Class<? extends Processor>) Class.forName(processorClassName);
                    TrekContext.INSTANCE.addApplication(name, key, processorClass);
                } catch (JSONException e) {
                    TrekContext.INSTANCE.addApplication(name, key);
                } catch (Exception e) {
                    LOG.error("Stop trek server cause fail to load processor", e);
                    System.exit(1);
                }
            }
        }
        new TrekServer(port).run();
    }
}
