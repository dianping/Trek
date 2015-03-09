package com.dianping.trek.server.decoder;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import com.dianping.trek.util.CompressUtil;
import com.qq.jce.wup.UniPacket;

public class LogMsgCoderImpl implements  LogMsgCoder {

	private static final int CHARSET_FLAG_GBK=2;
	private static final int REQUEST_MAGIC_NUMBER=0xdeadbeef;
	
	private static final String FIELD_LOG="log";
	private static final int MIN_HEAD_SIZE=12;//magic(4bytes)+charsetFlag(4bytes)+length(4bytes)
	
	private static final Charset CHARSET_GBK=Charset.forName("GBK");
	private static final Charset CHARSET_UTF8=Charset.forName("UTF-8");
	
	public LogMsgCoderImpl(){
		
	}

	private static Charset getCharset(int flag){
		switch (flag) {
		case CHARSET_FLAG_GBK:
			return CHARSET_GBK;
		default:
			return CHARSET_UTF8;
		}
	}
	
	
	private List<String> decodeUnCompressData(ByteBuffer byteBuffer,Charset charset) throws UnsupportedEncodingException{
		short logNum=byteBuffer.getShort();//总的日志条数
		List<String> resultList=new ArrayList<String>(logNum);
		for(int i=0;i<logNum;i++){
			int len=byteBuffer.getInt();//本行日志的长度
			byte[] content=new byte[len];
			byteBuffer.get(content);
			String s=new String(content,charset);
			resultList.add(s);
		}
		return resultList;
	}

	
	@SuppressWarnings("unused")
    @Override
	public boolean isValidMsg(byte[] data){
		if(data==null || data.length<MIN_HEAD_SIZE){
			return false;
		}
		ByteBuffer buffer=ByteBuffer.wrap(data);
		int magicNumber=buffer.getInt();
		if(magicNumber!=REQUEST_MAGIC_NUMBER){
			return false;
		}
		int charsetFlag=buffer.getInt();
		int length=buffer.getInt();
		int actualLen=buffer.remaining()+4;
		if(actualLen!=length){//长度本身占用4个字节
			return false;
		}
		
		return true;
	}


	@SuppressWarnings("unused")
    @Override
	public DecodeResult decode(byte[] data) throws Exception {
		
		ByteBuffer buffer=ByteBuffer.wrap(data);
		int magicNumber=buffer.getInt();
		int charsetFlag=buffer.getInt();
				
		byte[] contentData=new byte[buffer.remaining()];
		buffer.get(contentData);
	
		UniPacket packet=new UniPacket(); //默认用版本2
		packet.decode(contentData);
		
		//LogMsgStruct logMsgStruct=packet.get(FIELD_LOG);
		LogMsgStruct logMsgStruct=packet.get(FIELD_LOG, new LogMsgStruct(), null);

		//根据类型来解压缩数据
		ByteBuffer decompressData=null;
		LogMsgStruct.CompressType type=logMsgStruct.getType();
		switch (type) {
		case EM_ZLIB:{
			decompressData=CompressUtil.zlibDecompress(logMsgStruct.getCompressData());
			break;
		}
		case EM_SNAPPY:{
			decompressData=CompressUtil.snappyDecompress(logMsgStruct.getCompressData());
			break;
		}
		case EM_NONE:{
			break;	
		}
		default:
			throw new RuntimeException("Unknown compress type "+type);
		}
		
		//构造日志数据
		List<String> logList=null;
		if(decompressData!=null){
			logList=decodeUnCompressData(decompressData,getCharset(charsetFlag));
		}
		else {
			logList=logMsgStruct.getLogList();
		}

		//构造返回包
		boolean needBackMsg=logMsgStruct.isNeedBackMsg();
		byte[] returnData=null;
		if(needBackMsg){
			UniPacket backPacket=packet.createResponse();
			returnData=backPacket.encode();
		}

		String logName=logMsgStruct.getName();
		return new DecodeResult(logName, logList, needBackMsg, returnData);
	}

}
