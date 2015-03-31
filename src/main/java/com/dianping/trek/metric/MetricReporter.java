package com.dianping.trek.metric;

import java.util.HashMap;
import java.util.Map;

import com.dianping.cat.Cat;
import com.dianping.trek.server.TrekContext;
import com.dianping.trek.util.Constants;

public class MetricReporter extends Thread {
    private static final long METRIC_REPORT_DURATION = 5000L;
    private volatile boolean running;
    private TrekContext context;
    private Map<String, Long> lastReportStat;

    public MetricReporter() {
        this.running = true;
        this.context = TrekContext.getInstance();
        this.lastReportStat = new HashMap<String, Long>();
    }
    
    @Override
    public void run() {
        while (running) {
            for (String appName : context.getAllApplicationNames()) {
                long currentCount = context.getReceivedMessageStat(appName);
                Long lastCount = lastReportStat.get(appName);
                long delta = currentCount - (lastCount == null ? 0L : lastCount);
                Cat.logMetricForSum(Constants.CAT_DOMAIN_PREFIX + appName, delta);
                lastReportStat.put(appName, currentCount);
            }
            try {
                Thread.sleep(METRIC_REPORT_DURATION);
            } catch (InterruptedException e) {
                running = false;
            }
        }
    }
}
