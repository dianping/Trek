package com.dianping.trek.handler;

import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.dianping.trek.spi.Application;
import com.dianping.trek.spi.Processor;
import com.dianping.trek.spi.TrekContext;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.concurrent.EventExecutor;

public class WorkerThreadPool {
    private static Log LOG = LogFactory.getLog(WorkerThreadPool.class);
    
    private TrekContext trekContext;
    
    ExecutorService pool;
    
    private ConcurrentHashMap<String, Woker> runningWorkers;
    
    public WorkerThreadPool() {
        this.trekContext = TrekContext.INSTANCE;
        this.pool = Executors.newCachedThreadPool();
        this.runningWorkers = new ConcurrentHashMap<String, Woker>();
    }
    
    public void refresh() {
        Set<String> applications = trekContext.getAllApplicationNames();
        for (String appName : applications) {
            Woker worker = new Woker(trekContext.getApplication(appName));
            if(null == runningWorkers.putIfAbsent(appName, worker)) {
                pool.execute(worker);
            }
        }
    }
    
    public void execute(String appName) {
        if (runningWorkers.containsKey(appName)) {
            return;
        } else {
            Woker worker = new Woker(trekContext.getApplication(appName));
            if(null == runningWorkers.putIfAbsent(appName, worker)) {
                pool.execute(worker);
            }
        }
    }
    
    public void shutdown(String appName) {
        Woker worker = null;
        if ((worker = runningWorkers.get(appName)) == null) {
            return;
        } else {
            LOG.info("stopping work thread.");
            worker.stop();
        }
    }
    
    class Woker implements Runnable {

        private boolean running;
        private Application app;
        private BlockingQueue<String> queue;
        private Processor processor;
        public Woker(Application app) {
            this.app = app;
            this.running = true;
            this.queue = app.getMessageQueue();
            this.processor = app.getProcessor();
            this.processor.setApp(app);
        }
        
        @Override
        public void run() {
            while (running) {
                try {
                    String message = queue.take();
                    String handledMessage = processor.processOneLine(message);
                    processor.logToDisk(handledMessage);
                } catch (InterruptedException e) {
                    LOG.error("Custom handler thread " + app.getAppName() + " interrupted", e);
                    running = false;
                } catch (Throwable t) {
                    LOG.error("Oops, worker got an exception!", t);
                }
            }
            queue.clear();
        }
        
        public void stop() {
            this.running = false;
        }
    }
}
