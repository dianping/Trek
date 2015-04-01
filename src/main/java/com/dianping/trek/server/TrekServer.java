package com.dianping.trek.server;

import java.io.IOException;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.dianping.trek.decoder.WUPDecoder;
import com.dianping.trek.handler.ApplicationDistributionHandler;
import com.dianping.trek.metric.MetricReporter;
import com.dianping.trek.processor.AbstractProcessor;
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
import io.netty.handler.codec.bytes.ByteArrayEncoder;

public class TrekServer extends Thread {
    private static Log LOG = LogFactory.getLog(TrekServer.class);
    private int port;
    private WorkerThreadManager workerManger;
    private MetricReporter metricReporter;

    public void run() {
        LOG.info("Start trek server on port " + port);
        workerManger = new WorkerThreadManager();
        workerManger.startAll();
        metricReporter = new MetricReporter();
        metricReporter.start();
        
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
             .channel(NioServerSocketChannel.class)
             .childHandler(new FilterChannelChain())
             .option(ChannelOption.SO_BACKLOG, 128)
             .option(ChannelOption.SO_KEEPALIVE, true)
             .childOption(ChannelOption.SO_KEEPALIVE, false);

            // Bind and start to accept incoming connections.
            ChannelFuture f = b.bind(port).sync();

            // Wait until the server socket is closed.
            // In this example, this does not happen, but you can do that to gracefully
            // shut down your server.
            f.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            LOG.error("netty loop interrupted", e);;
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
    
    class FilterChannelChain extends ChannelInitializer<SocketChannel> {

        @Override
        protected void initChannel(SocketChannel ch) throws Exception {
            ch.pipeline().addLast(new WUPDecoder(1024 * 1024, 8, 4, -4, 0, true));
            ch.pipeline().addLast(new ByteArrayEncoder());
            ch.pipeline().addLast(new ApplicationDistributionHandler());
        }
    }

    public void initParam() throws IOException, ClassNotFoundException {
        Properties prop = new Properties();
        prop.load(TrekServer.class.getClassLoader().getResourceAsStream("config.properties"));
        this.port = Integer.parseInt(prop.getProperty("trek.port", "8090"));
        String basePath = prop.getProperty("trek.basePath", "/tmp");
        TrekContext.getInstance().setDefaultLogBaseDir(basePath);
        String encryKey = prop.getProperty("trek.encryKey");
        if (encryKey == null) {
            System.exit(1);
        }
        TrekContext.getInstance().setEncryKey(encryKey);
        
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
                String processorClassName = CommonUtil.getString(appObject, Constants.PROCESS_CLASS_KEY, Constants.DEFAULT_PROCESSOR_CLASS);
                @SuppressWarnings("unchecked")
                Class<? extends AbstractProcessor> processorClass = (Class<? extends AbstractProcessor>) Class.forName(processorClassName);
                int numWorker = CommonUtil.getInteger(appObject, Constants.NUM_WORKER_KEY, Constants.DEFAULT_WORKER_NUMBER);
                TrekContext.getInstance().addApplication(name, key, processorClass, numWorker, immediateFlush);
            }
        }
    }
    
    public static void main(String[] args) throws Exception {
        TrekServer server = new TrekServer();
        server.initParam();
        server.run();
    }
}
