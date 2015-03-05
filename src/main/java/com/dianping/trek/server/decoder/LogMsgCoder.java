package com.dianping.trek.server.decoder;

public interface LogMsgCoder {

	public DecodeResult decode(byte[] data) throws Exception;
	
	public boolean isValidMsg(byte[] data) throws Exception;
	
}
