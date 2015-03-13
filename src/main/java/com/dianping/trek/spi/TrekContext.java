package com.dianping.trek.spi;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public enum TrekContext {
    INSTANCE;
    
    private static Log LOG = LogFactory.getLog(TrekContext.class);
    private Map<String, Application> apps;
    private static String DEFALUT_BASE_PATH;
    
    private TrekContext() {
        this.apps = new ConcurrentHashMap<String, Application>();
    }
    
    public boolean addApplication(String appName, String appKey, Class<? extends Processor> processorClass, String outputDir) {
        if (apps.containsKey(appName)) {
            LOG.warn("The application name '" + appName + "' has already exist!");
            return false;
        } else {
            Application application = new Application(appName, appKey, processorClass, outputDir);
            apps.put(appName, application);
            return true;
        }
    }
    
    public boolean addApplication(String appName, String appKey) {
        return addApplication(appName, appKey, BasicProcessor.class, DEFALUT_BASE_PATH);
    }

    public boolean addApplication(String appName, String appKey, Class<? extends Processor> processorClass) {
        return addApplication(appName, appKey, processorClass, DEFALUT_BASE_PATH);
    }
    
    public boolean addApplication(String appName, String appKey, String outputDir) {
        return addApplication(appName, appKey, BasicProcessor.class, outputDir);
    }
    
    public Set<String> getAllApplicationNames() {
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
    
    public static String getDefaultBasePath() {
        return DEFALUT_BASE_PATH;
    }
    
    public static void SetBasePath(String defaultBasePath) {
        DEFALUT_BASE_PATH = defaultBasePath;
    }
}
