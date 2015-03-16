package com.dianping.trek.server;

import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.dianping.trek.decoder.WUPDecoder;
import com.dianping.trek.handler.ApplicationDistributionHandler;
import com.dianping.trek.spi.Processor;
import com.dianping.trek.spi.TrekContext;
import com.dianping.trek.util.CommonUtil;
import com.dianping.trek.util.Constants;

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
    private WorkerThreadManager workerManger;

    public TrekServer(int port) {
        this.port = port;
    }
    
    public void run() throws Exception {
        workerManger = new WorkerThreadManager();
        workerManger.startAll();
        
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
        TrekContext.SetDefaultLogBaseDir(basePath);
        String appJsonStr = prop.getProperty("trek.app.json");
        
        if (appJsonStr != null) {
            JSONArray appArray = new JSONArray(appJsonStr);
            for (int i = 0; i < appArray.length(); i++) {
                JSONObject appObject;
                String name;
                String key;
                try {
                    appObject = appArray.getJSONObject(i);
                    name = appObject.getString(Constants.APPNAME_KEY);
                    key = appObject.getString(Constants.APPKEY_KEY);
                } catch (JSONException e) {
                    continue;
                }
                boolean immediateFlush = CommonUtil.getBoolean(appObject, Constants.IMMEDIATE_FLUSH, false);
                try {
                    String processorClassName = CommonUtil.getString(appObject, Constants.PROCESS_CLASS_KEY, Constants.DEFAULT_PROCESSOR_CLASS);
                    @SuppressWarnings("unchecked")
                    Class<? extends Processor> processorClass = (Class<? extends Processor>) Class.forName(processorClassName);
                    int numWorker = CommonUtil.getInteger(appObject, Constants.NUM_WORKER_KEY, Constants.DEFAULT_WORKER_NUMBER);
                    TrekContext.INSTANCE.addApplication(name, key, processorClass, numWorker, immediateFlush);
                } catch (Exception e) {
                    LOG.error("Stop trek server cause fail to load processor", e);
                    System.exit(1);
                }
            }
        }
        new TrekServer(port).run();
    }
}
