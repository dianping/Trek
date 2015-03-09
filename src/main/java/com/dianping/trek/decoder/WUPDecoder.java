package com.dianping.trek.decoder;

import java.net.InetSocketAddress;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

public class WUPDecoder extends LengthFieldBasedFrameDecoder {
    private static final Log LOG = LogFactory.getLog(WUPDecoder.class);
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
        System.out.println("Connected from " + ip);
        ctx.fireChannelActive();
    }
    
    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        System.out.println("test");
        ByteBuf frame = (ByteBuf) super.decode(ctx, in);
        if (frame == null) {
            return null;
        }
        ByteBuf message =  frame.slice();
        message.skipBytes(SIZE_BEFORE_LENGTH_FIELD);
        int lengthFieldValue = message.readInt();
        int inputSize = SIZE_BEFORE_LENGTH_FIELD + lengthFieldValue;
        byte[] inputData = new byte[inputSize];
        message.getBytes(0, inputData);
        if(coder.isValidMsg(inputData)){
            DecodeResult result=coder.decode(inputData);
            System.out.println(result);
            return result;
        } else {
            LOG.error("invlid message");
            return null;
        }
    }
}
