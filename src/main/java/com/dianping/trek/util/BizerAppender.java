package com.dianping.trek.util;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.DailyRollingFileAppender;
import org.apache.log4j.PatternLayout;

import com.dianping.trek.server.TrekContext;

public class BizerAppender extends DailyRollingFileAppender {
    private String basePath;
    private String alias;
    
    public BizerAppender(String basePath, String alias, boolean immediateFlush, int flushBufferSize) {
        this.basePath = basePath;
        this.alias = alias;
        try {
            super.setFile(basePath + File.separator + alias, true, false, flushBufferSize);
        } catch (IOException e) {
            try {
                super.setFile(TrekContext.getInstance().getDefaultLogBaseDir() + File.separator + alias, true, false, 1024);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
        super.setDatePattern("'.'yyyy-MM-dd.HH");
        super.setLayout(new PatternLayout());
        super.activateOptions();
        super.setImmediateFlush(immediateFlush);
    }

    @Override
    public String toString() {
        return "BizerAppender [basePath=" + basePath + ", alias=" + alias
                + "]";
    }
}
