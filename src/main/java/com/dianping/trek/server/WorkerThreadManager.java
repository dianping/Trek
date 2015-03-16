package com.dianping.trek.server;

import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.dianping.trek.spi.Application;
import com.dianping.trek.spi.Processor;
import com.dianping.trek.spi.TrekContext;

public class WorkerThreadManager {
    private static Log LOG = LogFactory.getLog(WorkerThreadManager.class);
    
    private TrekContext trekContext;
    
    private ConcurrentHashMap<String, Set<Worker>> runningWorkers;
    
    public WorkerThreadManager() {
        this.trekContext = TrekContext.INSTANCE;
        this.runningWorkers = new ConcurrentHashMap<String, Set<Worker>>();
    }
    
    public void startAll() {
        Set<String> applications = trekContext.getAllApplicationNames();
        for (String appName : applications) {
            if (runningWorkers.containsKey(appName)) {
                continue;
            }
            Application app = trekContext.getApplication(appName);
            BlockingQueue<String> queue = app.getMessageQueue();
            Processor processor = app.getProcessor();
            processor.setApp(app);
            int numWorker = app.getNumWorker();
            Set<Worker> workers = new CopyOnWriteArraySet<Worker>();
            for (int i = 0; i < numWorker; i++) {
                Worker worker = new Worker(appName, queue, processor);
                worker.setDaemon(true);
                worker.setName(appName + "-" + i); 
                worker.start();
                workers.add(worker);
            }
            runningWorkers.putIfAbsent(appName, workers);
        }
    }
    
    public void shutdown(String appName) {
        Set<Worker> workers = null;
        if ((workers = runningWorkers.get(appName)) == null) {
            return;
        } else {
            LOG.info("stopping work thread.");
            for (Worker worker : workers) {
                worker.stopGracefully();
            }
            workers.clear();
        }
        runningWorkers.remove(appName);
    }
    
    class Worker extends Thread {

        private boolean running;
        private String  appName;
        private BlockingQueue<String> queue;
        private Processor processor;
        public Worker(String appName, BlockingQueue<String> queue, Processor processor) {
            this.running = true;
            this.appName = appName;
            this.queue = queue;
            this.processor = processor;
        }
        
        @Override
        public void run() {
            while (running) {
                try {
                    String message = queue.take();
                    String handledMessage = processor.processOneLine(message);
                    processor.logToDisk(handledMessage);
                } catch (InterruptedException e) {
                    LOG.error("Custom handler thread " + appName + " interrupted", e);
                    running = false;
                } catch (Throwable t) {
                    LOG.error("Oops, worker got an exception!", t);
                }
            }
            queue.clear();
        }
        
        public void stopGracefully() {
            this.running = false;
        }
    }
}
