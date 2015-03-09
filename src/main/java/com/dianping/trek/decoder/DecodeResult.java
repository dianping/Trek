package com.dianping.trek.decoder;

import java.util.Arrays;
import java.util.List;

public class DecodeResult {
	
	private String logName;
	
	private List<String> logList;
	
	private boolean needBackMsg; //是否需要回包
	
	private byte[] returnData;

	public DecodeResult(String logName, List<String> logList,
			boolean needBackMsg, byte[] returnData) {
		this.logName = logName;
		this.logList = logList;
		this.needBackMsg = needBackMsg;
		this.returnData = returnData;
	}

	public String getLogName() {
		return logName;
	}

	public void setLogName(String logName) {
		this.logName = logName;
	}

	public List<String> getLogList() {
		return logList;
	}

	public void setLogList(List<String> logList) {
		this.logList = logList;
	}

	

	public boolean isNeedBackMsg() {
		return needBackMsg;
	}

	public void setNeedBackMsg(boolean needBackMsg) {
		this.needBackMsg = needBackMsg;
	}

	public byte[] getReturnData() {
		return returnData;
	}

	public void setReturnData(byte[] returnData) {
		this.returnData = returnData;
	}

	@Override
	public String toString() {
		return "DecodeResult [logName=" + logName + ", logList=" + logList
				+ ", needBackMsg=" + needBackMsg + ", returnData="
				+ Arrays.toString(returnData) + "]";
	}
	

}
