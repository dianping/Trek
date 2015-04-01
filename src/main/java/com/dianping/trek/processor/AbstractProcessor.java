package com.dianping.trek.processor;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Category;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;

import com.dianping.trek.server.Application;
import com.dianping.trek.server.MessageChunk;

public abstract class AbstractProcessor {
    private static Log LOG = LogFactory.getLog(AbstractProcessor.class);
    private final int NEWLINE = '\n';
    private final int RETURN = '\r';
    
    private Application app;
    Category category;
    private String fqnOfCategoryClass;

    public void setApp(Application app) {
        if (this.app == null) {
            this.app = app;
            this.fqnOfCategoryClass = Logger.class.getName();
            this.category = Logger.getLogger(app.getAlias());
        }
    }
    
    public Application getApp() {
        return app;
    }
    
    public abstract String processOneLine(String unProcessedLine);
    
    public MessageChunk processOneChunk(MessageChunk chunk) {
        List<String> unProcessedList = chunk.getResult().getLogList();
        List<String> processedList = new ArrayList<String>(unProcessedList.size());
        for (String unProcessedLine : unProcessedList) {
            try {
                processedList.add(processOneLine(trimCRNL(unProcessedLine)));
            } catch (Exception e) {
                LOG.error("Exception cached when processing one line, drop it", e);
            }
        }
        chunk.setProcessedMessage(processedList);
        return chunk;
    }
    
    public void logToDisk(MessageChunk processedChunk) {
        List<String> processedLineList;
        if (processedChunk == null) {
            return;
        }
        if ((processedLineList = processedChunk.getProcessedMessage()).size() == 0) {
            return;
        }
        for (String processedLine : processedLineList) {
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
        LOG.trace("DIK: " + processedChunk.getResult().hashCode() + " " + System.currentTimeMillis());
    }
    
    public String trimCRNL(String line) {
        if (line.charAt(line.length() - 1) == NEWLINE) {
            if (line.charAt(line.length() - 2) == RETURN) {
                return line.substring(0, line.length() - 2);
            }
            return line.substring(0, line.length() - 1);
        }
        return line;
    }
}
