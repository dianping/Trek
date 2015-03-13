package com.dianping.trek.spi;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;

public class BasicProcessor implements Processor {
    private static Log LOG = LogFactory.getLog(BasicProcessor.class);
    protected Application app;
    
    @Override
    public void setApp(Application app) {
        this.app = app;
    }
    
    @Override
    public String processOneLine(String rawLine) {
        return rawLine;
    }
    
    @Override
    public void logToDisk(String processedLine) {
        if (processedLine == null || processedLine.length() == 0) {
            return;
        }
        LOG.trace("proceesed: " + processedLine);
        app.getAppender().append(
            new LoggingEvent(
                    Logger.class.getName(),
                    Logger.getLogger(app.getAppName()),
                    Level.INFO,
                    processedLine,
                    null
            )
        );
    }
}
