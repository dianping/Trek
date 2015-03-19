package com.dianping.trek.example;

import com.dianping.trek.processor.AbstractProcessor;

public class MobileApplogProcessor extends AbstractProcessor {

    @Override
    public String processOneLine(String rawLine) {
        return rawLine.split("\\|")[20];
    }
}
