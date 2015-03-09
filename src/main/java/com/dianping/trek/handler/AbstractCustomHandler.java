package com.dianping.trek.handler;

import java.util.Set;
import java.util.concurrent.BlockingQueue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.dianping.trek.api.Application;
import com.dianping.trek.spi.TrekContext;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.concurrent.EventExecutor;

public abstract class AbstractCustomHandler extends ChannelInboundHandlerAdapter {
    private static Log LOG = LogFactory.getLog(AbstractCustomHandler.class);
    
    protected TrekContext trekContext;
    
    public AbstractCustomHandler() {
        this.trekContext = TrekContext.INSTANCE;
    }
    
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        Set<String> applications = trekContext.getAllApplicationNames();
        EventExecutor executor = ctx.executor();
        for (String app : applications) {
            executor.execute(new CustomMessageHandler(trekContext.getApplication(app)));
        }
    }
    
    public abstract void custom(String appName, String message);
    
    class CustomMessageHandler implements Runnable {

        private boolean running;
        private Application app;
        private BlockingQueue<String> queue;
        public CustomMessageHandler(Application app) {
            this.app = app;
            this.running = true;
            this.queue = app.getMessageQueue();
        }
        
        @Override
        public void run() {
            while (running) {
                try {
                    String message = queue.take();
                    custom(app.getAppName(), message);
                } catch (InterruptedException e) {
                    LOG.error("Custom handler thread " + app.getAppName() + " interrupted", e);
                    running = false;
                }
            }
        }
    }
}
