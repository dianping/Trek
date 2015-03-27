package com.dianping.trek.server;

import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.dianping.trek.processor.AbstractProcessor;

public class WorkerThreadManager {
    private static Log LOG = LogFactory.getLog(WorkerThreadManager.class);
    
    private TrekContext trekContext;
    
    private ConcurrentHashMap<String, Set<Worker>> runningWorkers;
    
    public WorkerThreadManager() {
        this.trekContext = TrekContext.getInstance();
        this.runningWorkers = new ConcurrentHashMap<String, Set<Worker>>();
    }
    
    public void startAll() {
        Set<String> applications = trekContext.getAllApplicationNames();
        for (String appName : applications) {
            if (runningWorkers.containsKey(appName)) {
                continue;
            }
            Application app = trekContext.getApplication(appName);
            BlockingQueue<MessageChunk> queue = app.getMessageQueue();
            AbstractProcessor processor = app.getProcessor();
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

        private volatile boolean running;
        private String  appName;
        private BlockingQueue<MessageChunk> queue;
        private AbstractProcessor processor;
        public Worker(String appName, BlockingQueue<MessageChunk> queue, AbstractProcessor processor) {
            this.running = true;
            this.appName = appName;
            this.queue = queue;
            this.processor = processor;
        }
        
        @Override
        public void run() {
            while (running) {
                try {
                    MessageChunk unprocessedChunk = queue.take();
                    processAndAck(unprocessedChunk, processor);
                } catch (InterruptedException e) {
                    LOG.error("Custom handler thread " + appName + " interrupted", e);
                    running = false;
                } catch (Throwable t) {
                    LOG.error("Oops, worker got an exception!", t);
                }
            }
            for (MessageChunk unprocessedChunk : queue) {
                processAndAck(unprocessedChunk, processor);
            }
        }

        private void processAndAck(MessageChunk unprocessedChunk, AbstractProcessor processor) {
            MessageChunk processedChunk = processor.processOneChunk(unprocessedChunk);
            processor.logToDisk(processedChunk);
            if (processedChunk.getResult().isNeedBackMsg()) {
                byte[] src = processedChunk.getResult().getReturnData();
                byte[] ack = Arrays.copyOf(src, src.length);
                processedChunk.getCtx().writeAndFlush(ack);
            }
            LOG.debug("ACK: " + unprocessedChunk.getResult().hashCode() + " " + System.currentTimeMillis());
        }
        
        public void stopGracefully() {
            this.running = false;
        }
    }
}
