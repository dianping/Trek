package com.dianping.trek.util;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.DailyRollingFileAppender;
import org.apache.log4j.PatternLayout;

public class BizerAppender extends DailyRollingFileAppender {
    
    public BizerAppender(String basePath, String appName) throws IOException {
        super.setFile(basePath + File.separator + appName, true, false, 1024);
        super.setDatePattern("'.'yyyy-MM-dd.HH");
        super.setLayout(new PatternLayout());
        super.activateOptions();
    }
}
