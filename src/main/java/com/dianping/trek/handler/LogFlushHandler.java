package com.dianping.trek.handler;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;

import com.dianping.trek.util.BizerAppender;

public class LogFlushHandler extends AbstractCustomHandler {
    private String basePath;
    private Map<String, BizerAppender> appenders;
    public LogFlushHandler() {
        basePath = trekContext.getBasePath();
        appenders = new HashMap<String, BizerAppender>();
        for (String appName : trekContext.getAllApplicationNames()) {
            appenders.put(appName, new BizerAppender(basePath, appName));
        }
    }
    @Override
    public void custom(String appName, String message) {
        BizerAppender appender = appenders.get(appName);
        if (appender != null) {
            appender.append(
                new LoggingEvent(
                        null,
                        Logger.getLogger(appName),
                        Level.INFO,
                        message,
                        null
                )
            );
        }
    }
}
