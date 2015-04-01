package com.dianping.trek.handler;

import java.util.concurrent.BlockingQueue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.dianping.trek.decoder.DecodeResult;
import com.dianping.trek.exception.InvalidApplicationException;
import com.dianping.trek.server.Application;
import com.dianping.trek.server.MessageChunk;
import com.dianping.trek.server.TrekContext;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class ApplicationDistributionHandler extends ChannelInboundHandlerAdapter {
    private static Log LOG = LogFactory.getLog(ApplicationDistributionHandler.class);
    private static long ALARM_THRESHOLD = 100L; 
    private long exceptionCount = 0L;
    
    private TrekContext trekCtx;
    
    public ApplicationDistributionHandler() {
        this.trekCtx = TrekContext.getInstance();
    }
    
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg == null) {
            // control message
            return;
        }
        DecodeResult result = (DecodeResult) msg;
        String logname = result.getLogName();
        String appkey = trekCtx.getAppkeyByLogname(logname);
        if (appkey == null) {
            LOG.error("Can not find appkey by " + logname);
            exceptionCount++;
            return;
        }
        BlockingQueue<MessageChunk> appMessageQueue;
        if ((appMessageQueue = trekCtx.getApplicationMessageQueue(appkey)) != null) {
            appMessageQueue.offer(new MessageChunk(ctx, result));
            trekCtx.updateReceivedMessageStat(appkey, result.getLogList().size());
        } else {
            LOG.error("Can not find application by " + appkey);
            exceptionCount++;
        }
    }
    
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
            throws Exception {
        if (exceptionCount++ > ALARM_THRESHOLD) {
            LOG.error("Exceptions have occured more than " + ALARM_THRESHOLD + " times!", new InvalidApplicationException());
            exceptionCount = 0L;
        }
    }
}
