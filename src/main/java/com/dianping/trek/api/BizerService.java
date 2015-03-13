package com.dianping.trek.api;

import com.dianping.trek.spi.Processor;

public class BizerService {
    
    private String serviceName;
    private String appKey;
    private String fileOutputDir;
    private Class<?> processorClass;
    
    public BizerService(String name, String appKey) {
        this.serviceName = name;
        this.appKey = appKey;
    }

    public BizerService(String name, String appKey, String fileOutputDir) {
        this.serviceName = name;
        this.appKey = appKey;
        this.fileOutputDir = fileOutputDir;
    }
    
    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getAppKey() {
        return appKey;
    }

    public void setAppKey(String appKey) {
        this.appKey = appKey;
    }

    public String getFileOutputDir() {
        return fileOutputDir;
    }

    public void setFileOutputDir(String fileOutputDir) {
        this.fileOutputDir = fileOutputDir;
    }

    public Class<?> getProcessorClass() {
        return processorClass;
    }
    
    public void setProcessorClass(Class<? extends Processor> processorClass) {
        this.processorClass = processorClass;
    }
}
