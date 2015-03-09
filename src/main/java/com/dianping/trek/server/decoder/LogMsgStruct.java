package com.dianping.trek.server.decoder;

import java.util.ArrayList;
import java.util.List;

import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceOutputStream;
import com.qq.taf.jce.JceStruct;

public final class LogMsgStruct extends JceStruct {

	private static final long serialVersionUID = -7568647850051045326L;
	
	private String stub="log";
	
	private String name;
	
    private CompressType type;
    
    private boolean needBackMsg;
    
    private byte[] compressData;
    
    private List<String> logList;
	
	
    public LogMsgStruct(){
    	super();
    	this.name="";
    	this.type=CompressType.EM_NONE;
    	this.needBackMsg=false;
    	this.compressData=new byte[0];
    	this.logList=new ArrayList<String>();
    }
    
    
    
	public LogMsgStruct(String name, CompressType type,
			boolean needBackMsg, byte[] compressData, List<String> logList) {
		super();
		this.name = name;
		this.type = type;
		this.needBackMsg = needBackMsg;
		this.compressData = (compressData==null ? new byte[0]:compressData);
		this.logList = (logList==null ? new ArrayList<String>():logList);
	}

	public enum CompressType{
        EM_ZLIB(0),
        EM_SNAPPY(1),
        EM_NONE(2);
        int value;
        
        CompressType(int v){
        	this.value=v;
        }
        
        public static CompressType toType(int v){
        	switch (v) {
			case 0:
				return EM_ZLIB;
			case 1:
				return EM_SNAPPY;
			case 2:
				return EM_NONE;
			default:
				throw new RuntimeException("Unknow compress type "+v);
			}
        }
        
    }
	
	@Override
	public void writeTo(JceOutputStream os) {
		os.write(stub, 0);
		os.write(name, 1);
		os.write(type.value, 2);
		os.write(needBackMsg, 3);
		os.write(compressData, 4);
		os.write(logList, 5);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void readFrom(JceInputStream is) {
		stub=is.read(stub, 0, true);
		name=is.read(name, 1, true);
		
		int typeValue=CompressType.EM_NONE.value;
		typeValue=is.read(typeValue,2,true);
		type=CompressType.toType(typeValue);
		
		needBackMsg=is.read(needBackMsg, 3, false);
		compressData=is.read(compressData, 4, false);
		
		logList.add("type");//这个是底层的库判断类型用的
		logList=(List<String>) is.read(logList, 5,false);

	}



	public String getStub() {
		return stub;
	}

	public String getName() {
		return name;
	}
	
	public CompressType getType() {
		return type;
	}

	public boolean isNeedBackMsg() {
		return needBackMsg;
	}

	public byte[] getCompressData() {
		return compressData;
	}

	public List<String> getLogList() {
		return logList;
	}

	
}
