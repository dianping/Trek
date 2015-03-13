package com.dianping.trek;

import com.dianping.trek.spi.BasicProcessor;

public class MyProcessor extends BasicProcessor {

    @Override
    public String processOneLine(String rawLine) {
        return "------" + rawLine;
    }
}
