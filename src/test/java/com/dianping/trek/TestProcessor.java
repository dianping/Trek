package com.dianping.trek;

import com.dianping.trek.processor.AbstractProcessor;

public class TestProcessor extends AbstractProcessor {

    @Override
    public String processOneLine(String rawLine) {
        return "------" + rawLine;
    }
}
