package com.dianping.trek.util;

import java.io.File;

import org.apache.log4j.DailyRollingFileAppender;

public class BizerAppender extends DailyRollingFileAppender {
    
    private String basePath;
    private String appName;
    
    public BizerAppender(String basePath, String appName) {
        this.basePath = basePath;
        this.appName = appName;
    }
    
    @Override
    public void activateOptions() {
        super.setFile(basePath + File.separator + appName);
        super.setDatePattern("'.'yyyy-MM-dd.HH");
        super.activateOptions();
    }
}
