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
        Set<String> appkeys = trekContext.getAllAppkeys();
        for (String appkey : appkeys) {
            if (runningWorkers.containsKey(appkey)) {
                continue;
            }
            Application app = trekContext.getApplication(appkey);
            BlockingQueue<MessageChunk> queue = app.getMessageQueue();
            AbstractProcessor processor = app.getProcessor();
            processor.setApp(app);
            int numWorker = app.getNumWorker();
            Set<Worker> workers = new CopyOnWriteArraySet<Worker>();
            for (int i = 0; i < numWorker; i++) {
                Worker worker = new Worker(app.getAlias(), queue, processor);
                worker.setDaemon(true);
                worker.setName(app.getAlias() + "-" + i); 
                worker.start();
                workers.add(worker);
            }
            runningWorkers.putIfAbsent(appkey, workers);
        }
    }
    
    public void shutdown(String appkey) {
        Set<Worker> workers = null;
        if ((workers = runningWorkers.get(appkey)) == null) {
            return;
        } else {
            LOG.info("stopping work thread.");
            for (Worker worker : workers) {
                worker.stopGracefully();
            }
            workers.clear();
        }
        runningWorkers.remove(appkey);
    }
    
    class Worker extends Thread {

        private volatile boolean running;
        private String  alias;
        private BlockingQueue<MessageChunk> queue;
        private AbstractProcessor processor;
        public Worker(String alias, BlockingQueue<MessageChunk> queue, AbstractProcessor processor) {
            this.running = true;
            this.alias = alias;
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
                    LOG.error("Custom handler thread " + alias + " interrupted", e);
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
            LOG.trace("ACK: " + unprocessedChunk.getResult().hashCode() + " " + System.currentTimeMillis());
        }
        
        public void stopGracefully() {
            this.running = false;
        }
    }
}
