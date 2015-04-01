package com.dianping.trek.processor;

public class PrintTimeProcessor extends AbstractProcessor {

    
    @Override
    public String processOneLine(String unProcessedLine) {
        return System.currentTimeMillis() + "|" +unProcessedLine;
    }
}
