package com.dianping.trek.spi;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Category;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;

public class BasicProcessor implements Processor {
    private static Log LOG = LogFactory.getLog(BasicProcessor.class);
    private Application app;
    Category category;
    private String fqnOfCategoryClass;

    @Override
    public void setApp(Application app) {
        if (this.app == null) {
            this.app = app;
            this.fqnOfCategoryClass = Logger.class.getName();
            this.category = Logger.getLogger(app.getAppName());
        }
    }
    
    public Application getApp() {
        return app;
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
        synchronized (category) {
        app.getAppender().append(
            new LoggingEvent(
                    fqnOfCategoryClass,
                    category,
                    Level.INFO,
                    processedLine,
                    null
            )
        );
        }
    }
}
