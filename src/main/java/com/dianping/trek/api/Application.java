package com.dianping.trek.api;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

public class Application {
    private final String appName;
    private final String appKey;
    private final BlockingQueue<String> messageQueue;
    private final AtomicLong receivedMessageStat;
    
    public Application(String appName, String appKey) {
        this.appName = appName;
        this.appKey = appKey;
        this.messageQueue = new LinkedBlockingQueue<String>();
        this.receivedMessageStat = new AtomicLong(0);
    }

    public String getAppName() {
        return appName;
    }

    public String getAppKey() {
        return appKey;
    }

    public BlockingQueue<String> getMessageQueue() {
        return messageQueue;
    }

    public AtomicLong getReceivedMessageStat() {
        return receivedMessageStat;
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
        return "Application [appName=" + appName + ", appKey=" + appKey + "]";
    }
}
