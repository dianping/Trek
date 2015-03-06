package com.dianping.trek.server.decoder;

import java.nio.ByteBuffer;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

public class WUPDecoder extends ByteToMessageDecoder {
    private static final Log LOG = LogFactory.getLog(WUPDecoder.class);
    private static final int MIN_HEAD_SIZE=12;//magic(4bytes)+charsetFlag(4bytes)+length(4bytes)
    private static final int REQUEST_MAGIC_NUMBER=0xdeadbeef;
    private LogMsgCoder coder = new LogMsgCoderImpl();
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in,
            List<Object> out) throws Exception {
        // TODO Auto-generated method stub
        if (in.readableBytes() < MIN_HEAD_SIZE) {
            return;
        }
        // TODO 不要做byte转化。直接用in来验算。
        byte[] inputData = new byte[in.readableBytes()];
        in.getBytes(0, inputData);
        
        ByteBuffer buffer=ByteBuffer.wrap(inputData);
        int magicNumber=buffer.getInt();
        boolean toDiscard = false;
        if(magicNumber != REQUEST_MAGIC_NUMBER){
            LOG.error("Message is invalid");
            toDiscard = true;
            return;
        }
        int charsetFlag = buffer.getInt();
        int length = buffer.getInt();
        
        int actualLen = buffer.remaining() + 4;
        if(actualLen != length){//长度本身占用4个字节
            return;
        }
        
        byte[] inputData = new byte[in.readableBytes()];
        in.getBytes(0, inputData);
        if(coder.isValidMsg(inputData)){
            DecodeResult result=coder.decode(inputData);
        } else {
            return;
        }
    }

}
