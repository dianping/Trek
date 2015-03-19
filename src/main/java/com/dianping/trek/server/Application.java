package com.dianping.trek.server;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

import com.dianping.trek.processor.AbstractProcessor;
import com.dianping.trek.util.BizerAppender;
import com.dianping.trek.util.Constants;
import com.dianping.trek.util.ReflectionUtils;

public class Application {
    private final String appName;
    private final String appKey;
    private final BlockingQueue<MessageChunk> messageQueue;
    private final AtomicLong receivedMessageStat;
    private final BizerAppender appender;
    private boolean immediateFlush;
    private int numWorker;
    private AbstractProcessor processor;
    
    public Application(String appName, String appKey,
            Class<? extends AbstractProcessor> processorClass, String basePath,
            int numWorker, boolean immediateFlush) {
        this.appName = appName;
        this.appKey = appKey;
        this.messageQueue = new LinkedBlockingQueue<MessageChunk>(Constants.DEFAULT_QUEUE_SIZE);
        this.receivedMessageStat = new AtomicLong(0);
        this.appender = new BizerAppender(basePath, appName, immediateFlush);
        this.immediateFlush = immediateFlush;
        this.numWorker = numWorker;
        this.processor = ReflectionUtils.newInstance(processorClass);
    }

    public String getAppName() {
        return appName;
    }

    public String getAppKey() {
        return appKey;
    }

    public BlockingQueue<MessageChunk> getMessageQueue() {
        return messageQueue;
    }

    public AtomicLong getReceivedMessageStat() {
        return receivedMessageStat;
    }

    public BizerAppender getAppender() {
        return appender;
    }

    public AbstractProcessor getProcessor() {
        return processor;
    }

    public boolean isImmediateFlush() {
        return immediateFlush;
    }

    public void setImmediateFlush(boolean immediateFlush) {
        this.appender.setImmediateFlush(immediateFlush);
        this.immediateFlush = immediateFlush;
    }

    public int getNumWorker() {
        return numWorker;
    }

    public void setNumWorker(int numWorker) {
        this.numWorker = numWorker;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((appKey == null) ? 0 : appKey.hashCode());
        result = prime * result + ((appName == null) ? 0 : appName.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Application other = (Application) obj;
        if (appKey == null) {
            if (other.appKey != null)
                return false;
        } else if (!appKey.equals(other.appKey))
            return false;
        if (appName == null) {
            if (other.appName != null)
                return false;
        } else if (!appName.equals(other.appName))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "Application [appName=" + appName + ", appKey=" + appKey
                + ", appender=" + appender + "]";
    }
}
