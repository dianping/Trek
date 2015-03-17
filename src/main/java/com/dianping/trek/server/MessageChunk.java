package com.dianping.trek.server;

import java.util.List;

import io.netty.channel.ChannelHandlerContext;

import com.dianping.trek.decoder.DecodeResult;

public class MessageChunk {

    private final ChannelHandlerContext ctx;

    private final DecodeResult result;
    
    private final int unprocessedMessageCount;
    
    private List<String> processedMessage;
    
    public MessageChunk(ChannelHandlerContext ctx, DecodeResult result) {
        this.ctx = ctx;
        this.result = result;
        this.unprocessedMessageCount = result.getLogList().size();
    }

    public ChannelHandlerContext getCtx() {
        return ctx;
    }

    public DecodeResult getResult() {
        return result;
    }
    
    /**
     * suggest to clear after logged to disk
     */
    public void clearUnprocessedMessage() {
        this.result.getLogList().clear();
    }

    public int getUnprocessedMessageCount() {
        return unprocessedMessageCount;
    }
    
    public int getProcessedMessageCount() {
        return processedMessage.size();
    }

    public List<String> getProcessedMessage() {
        return processedMessage;
    }

    public void setProcessedMessage(List<String> processedMessage) {
        this.processedMessage = processedMessage;
    }
}
