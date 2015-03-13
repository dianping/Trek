package com.dianping.trek.example;

import com.dianping.trek.spi.BasicProcessor;

public class MobileApplogProcessor extends BasicProcessor {

    @Override
    public String processOneLine(String rawLine) {
        return rawLine.split("\\|")[20];
    }
}
