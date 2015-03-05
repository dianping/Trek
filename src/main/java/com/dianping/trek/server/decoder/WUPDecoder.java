package com.dianping.trek.server.decoder;

import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

public class WUPDecoder extends ByteToMessageDecoder {

    private LogMsgCoder coder = new LogMsgCoderImpl();
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in,
            List<Object> out) throws Exception {
        // TODO Auto-generated method stub
        byte[] inputData = new byte[in.readableBytes()];
        in.getBytes(0, inputData);
        if(coder.isValidMsg(inputData)){
            DecodeResult result=coder.decode(inputData);
        } else {
            return;
        }
    }

}
