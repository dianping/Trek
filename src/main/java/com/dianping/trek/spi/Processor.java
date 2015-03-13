package com.dianping.trek.spi;

public interface Processor {
    
    public void setApp(Application app);
    
    public String processOneLine(String rawLine);
    
    public void logToDisk(String processedLine);
}
