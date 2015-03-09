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

public class DefaultCustomHandler extends ChannelInboundHandlerAdapter {
    private static Log LOG = LogFactory.getLog(DefaultCustomHandler.class);
    
    private TrekContext trekContext;
    
    public DefaultCustomHandler() {
        this.trekContext = TrekContext.INSTANCE;
    }
    
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Set<String> applications = trekContext.getExistApplicationNames();
        EventExecutor executor = ctx.executor();
        for (String app : applications) {
            executor.execute(new LoggerHandler(trekContext.getApplication(app)));
        }
    }
    
    class LoggerHandler implements Runnable {

        private boolean running;
        private Application app;
        private BlockingQueue<String> queue;
        public LoggerHandler(Application app) {
            this.app = app;
            this.running = true;
            this.queue = app.getMessageQueue();
        }
        
        @Override
        public void run() {
            while (running) {
                try {
                    String message = queue.take();
                    app.getAppName();
                    //TODO log to disk
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    running = false;
                }
            }
            
        }
        
    }
}
