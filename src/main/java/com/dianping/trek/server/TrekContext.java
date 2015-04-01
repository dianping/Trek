package com.dianping.trek.server;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.dianping.trek.processor.AbstractProcessor;

public class TrekContext {
    private static Log LOG = LogFactory.getLog(TrekContext.class);
    private Map<String, String> logname2appkeyMap; //logname -> appkey  n:1
    private Map<String, Application> apps; //appkey -> application  1:1
    private String defaultLogBaseDir;
    private String encryKey;
    
    private static TrekContext instance = new TrekContext();
    
    public static TrekContext getInstance() {
        return instance;
    }
    
    private TrekContext() {
        this.apps = new ConcurrentHashMap<String, Application>();
        this.logname2appkeyMap = new ConcurrentHashMap<String, String>();
    }
    
    public boolean addApplication(String alias, String appKey,
            Class<? extends AbstractProcessor> processorClass, String outputDir,
            int numWorker, boolean immediateFlush, int flushBufferSize) {
        if (apps.containsKey(appKey)) {
            LOG.warn("The application alias '" + alias + "' with key '" + appKey + "' has already exist!");
            return false;
        } else {
            Application application = new Application(alias, appKey, processorClass, outputDir, numWorker, immediateFlush, flushBufferSize);
            apps.put(appKey, application); 
            return true;
        }
    }
    
    public Set<String> getAllAppkeys() {
        return new HashSet<String>(apps.keySet());
    }
    
    public Application getApplication(String appkey) {
        return apps.get(appkey);
    }
    
    public BlockingQueue<MessageChunk> getApplicationMessageQueue(String appkey) {
        Application application = apps.get(appkey);
        if (application == null) {
            return null;
        } else {
            return application.getMessageQueue();
        }
    }

    public long updateReceivedMessageStat(String appkey, long increase) {
        Application application = apps.get(appkey);
        if (application == null) {
            return 0L;
        } else {
            return application.getReceivedMessageStat().addAndGet(increase);
        }
    }
    
    public long getReceivedMessageStat(String appkey) {
        Application application = apps.get(appkey);
        if (application == null) {
            return 0L;
        } else {
            return application.getReceivedMessageStat().get();
        }
    }
    
    public String getDefaultLogBaseDir() {
        return defaultLogBaseDir;
    }
    
    public void setDefaultLogBaseDir(String defaultBasePath) {
        this.defaultLogBaseDir = defaultBasePath;
    }

    public String getEncryKey() {
        return encryKey;
    }

    public void setEncryKey(String key) {
        this.encryKey = key;
    }

    public String getAppkeyByLogname(String logname) {
        return logname2appkeyMap.get(logname);
    }
    
    public void setLognameMapping(String logname, String appkey) {
        logname2appkeyMap.put(logname, appkey);
    }
}
