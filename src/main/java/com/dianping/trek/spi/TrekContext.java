package com.dianping.trek.spi;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.dianping.trek.api.Application;

public enum TrekContext {
    INSTANCE;
    
    private static Log LOG = LogFactory.getLog(TrekContext.class);
    private Map<String, Application> apps;
    
    private TrekContext() {
        this.apps = new HashMap<String, Application>();
    }
    
    public boolean addApplication(String appName, String appKey) {
        if (apps.containsKey(appName)) {
            LOG.warn("The application name '" + appName + "' has already exist!");
            return false;
        } else {
            apps.put(appName, new Application(appName, appKey));
            return true;
        }
    }
    
    public Set<String> getExistApplicationNames() {
        return new HashSet<String>(apps.keySet());
    }
    
    public Application getApplication(String appName) {
        return apps.get(appName);
    }
    
    public BlockingQueue<String> getApplicationMessageQueue(String appName) {
        Application application = apps.get(appName);
        if (application == null) {
            return null;
        } else {
            return application.getMessageQueue();
        }
    }

    public long updateReceivedMessageStat(String appName, long increase) {
        Application application = apps.get(appName);
        if (application == null) {
            return 0L;
        } else {
            return application.getReceivedMessageStat().addAndGet(increase);
        }
    }
    
    public long getReceivedMessageStat(String appName) {
        Application application = apps.get(appName);
        if (application == null) {
            return 0L;
        } else {
            return application.getReceivedMessageStat().get();
        }
    }
}
