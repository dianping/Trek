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
            for (String appkey : context.getAllAppkeys()) {
                long currentCount = context.getReceivedMessageStat(appkey);
                Long lastCount = lastReportStat.get(appkey);
                long delta = currentCount - (lastCount == null ? 0L : lastCount);
                Cat.logMetricForSum(Constants.CAT_DOMAIN_PREFIX + TrekContext.getInstance().getApplication(appkey).getAlias(), delta);
                lastReportStat.put(appkey, currentCount);
            }
            try {
                Thread.sleep(METRIC_REPORT_DURATION);
            } catch (InterruptedException e) {
                running = false;
            }
        }
    }
}
