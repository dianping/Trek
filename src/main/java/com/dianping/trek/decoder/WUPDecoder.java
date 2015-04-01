package com.dianping.trek.decoder;

import java.net.InetSocketAddress;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.dianping.trek.exception.InvalidMessageException;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.TooLongFrameException;

public class WUPDecoder extends LengthFieldBasedFrameDecoder {
    private static final Log LOG = LogFactory.getLog(WUPDecoder.class);
    private static final int SUBMIT_MAGIC_NUMBER = 0xfeedcafe;
    private static final int REQUEST_MAGIC_NUMBER=0xdeadbeef;
    private static final int SIZE_BEFORE_LENGTH_FIELD = 8;//magic(4bytes)+charsetFlag(4bytes)
    private LogMsgCoder coder;
    
    public WUPDecoder(int maxFrameLength, int lengthFieldOffset,
            int lengthFieldLength, int lengthAdjustment,
            int initialBytesToStrip, boolean failFast) {
        super(maxFrameLength, lengthFieldOffset, lengthFieldLength, lengthAdjustment,
                initialBytesToStrip, failFast);
        coder = new LogMsgCoderImpl();
    }
    
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        InetSocketAddress address = (InetSocketAddress) ctx.channel().remoteAddress();
        String ip = address.getAddress().getHostAddress();
        LOG.trace("Connected from " + ip);
        ctx.fireChannelActive();
    }
    
    @SuppressWarnings("unused")
    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        ByteBuf frame = (ByteBuf) super.decode(ctx, in);
        if (frame == null) {
            return null;
        }
        ByteBuf message =  frame.slice();
        int magic = message.readInt();
        try {
            if (SUBMIT_MAGIC_NUMBER == magic) {
                int type = message.readInt();
                int lengthFieldValue = message.readInt();
                //TODO handle control message
                return null;
            } else if (REQUEST_MAGIC_NUMBER == magic){
                int charset = message.readInt();
                int lengthFieldValue = message.readInt();
                int inputSize = SIZE_BEFORE_LENGTH_FIELD + lengthFieldValue;
                byte[] inputData = new byte[inputSize];
                message.getBytes(0, inputData);
                if(coder.isValidMsg(inputData)){
                    DecodeResult result = coder.decode(inputData);
                    return result;
                } else {
                    LOG.error("invalid message", new InvalidMessageException());
                    return null;
                }
            } else {
                LOG.warn("invalid message!");
                return null;
            }
        }finally {
            frame.release();
        }
    }
    
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
            throws Exception {
        if (!(cause instanceof TooLongFrameException)) {
            LOG.error("decode exception", cause);
        }
    }
}
