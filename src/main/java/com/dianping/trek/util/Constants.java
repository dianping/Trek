package com.dianping.trek.util;

public class Constants {
    public static final int DEFAULT_QUEUE_SIZE = 1000;
    public static final String CAT_DOMAIN_PREFIX = "Cat-Trek-";
    
    public static final String ALIAS_KEY = "alias";
    public static final String LOGNAMES_KEY = "logNames";
    public static final String APPKEY_KEY = "key";
    
    public static final String PROCESS_CLASS_KEY = "class";
    public static final String DEFAULT_PROCESSOR_CLASS = "com.dianping.trek.processor.SimpleProcessor";
    
    public static final String NUM_WORKER_KEY = "numWorker";
    public static final int DEFAULT_WORKER_NUMBER = 4;
    
    public static final String IMMEDIATE_FLUSH = "flush";
    
    public static final String ENCRYKEY_KEY = "encryKey";
    
    public static final String FLUSH_BUFFER_SIZE_KEY = "bufSize";
    public static final int DEFAULT_FLUSH_BUFFER_SIZE = 4096;
}
