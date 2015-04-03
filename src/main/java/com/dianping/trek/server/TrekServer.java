package com.dianping.trek.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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
            LOG.error("netty loop interrupted", e);
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
            throw new IOException("Can not find encry key");
        }
        TrekContext.getInstance().setEncryKey(encryKey);
        
        String appJsonStr = prop.getProperty("trek.app.json");
        if (appJsonStr != null) {
            JSONArray appArray = new JSONArray(appJsonStr);
            for (int i = 0; i < appArray.length(); i++) {
                JSONObject appObject;
                String alias;
                JSONArray lognameArray;
                String key;
                List<String> lognames;
                try {
                    appObject = appArray.getJSONObject(i);
                    alias = appObject.getString(Constants.ALIAS_KEY);
                    key = appObject.getString(Constants.APPKEY_KEY);
                    lognameArray = appObject.getJSONArray(Constants.LOGNAMES_KEY);
                    lognames = new ArrayList<String>();
                    for (int j = 0; j < lognameArray.length(); j++) {
                        lognames.add(lognameArray.getString(j));
                    }
                } catch (JSONException e) {
                    LOG.warn("find invalid json", e);
                    continue;
                }
                //bind vary logname to one key
                for (String logname : lognames) {
                    TrekContext.getInstance().setLognameMapping(logname, key);
                }
                int numWorker = CommonUtil.getInteger(appObject, Constants.NUM_WORKER_KEY, Constants.DEFAULT_WORKER_NUMBER);
                boolean immediateFlush = CommonUtil.getBoolean(appObject, Constants.IMMEDIATE_FLUSH, false);
                int flushBufferSize = CommonUtil.getInteger(appObject, Constants.FLUSH_BUFFER_SIZE_KEY, Constants.DEFAULT_FLUSH_BUFFER_SIZE);
                String processorClassName = CommonUtil.getString(appObject, Constants.PROCESS_CLASS_KEY, Constants.DEFAULT_PROCESSOR_CLASS);
                @SuppressWarnings("unchecked")
                Class<? extends AbstractProcessor> processorClass = (Class<? extends AbstractProcessor>) Class.forName(processorClassName);
                TrekContext.getInstance().addApplication(alias, key, processorClass, basePath,  numWorker, immediateFlush, flushBufferSize);
            }
        }
    }
    
    public static void main(String[] args) throws Exception {
        TrekServer server = new TrekServer();
        server.initParam();
        server.run();
    }
}
