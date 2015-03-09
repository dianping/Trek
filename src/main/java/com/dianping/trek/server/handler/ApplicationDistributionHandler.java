package com.dianping.trek.server.handler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.dianping.trek.server.decoder.DecodeResult;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class ApplicationDistributionHandler extends ChannelInboundHandlerAdapter {
    private static Log LOG = LogFactory.getLog(ApplicationDistributionHandler.class);
    private static long ALARM_THRESHOLD = 100L; 
    private long exceptionCount = 0L;
    
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        DecodeResult result = (DecodeResult) msg;
        if (result.isNeedBackMsg()) {
            ctx.writeAndFlush(result.getReturnData());
        }
        result.getLogName();
    }
    
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
            throws Exception {
        if (exceptionCount++ > ALARM_THRESHOLD) {
            LOG.equals("Exceptions have occured more than " + ALARM_THRESHOLD + " times!");
        }
        ctx.fireExceptionCaught(cause);
    }
    
}
