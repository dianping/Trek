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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((logList == null) ? 0 : logList.hashCode());
        result = prime * result + ((logName == null) ? 0 : logName.hashCode());
        result = prime * result + (needBackMsg ? 1231 : 1237);
        result = prime * result + Arrays.hashCode(returnData);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        DecodeResult other = (DecodeResult) obj;
        if (logList == null) {
            if (other.logList != null)
                return false;
        } else if (!logList.equals(other.logList))
            return false;
        if (logName == null) {
            if (other.logName != null)
                return false;
        } else if (!logName.equals(other.logName))
            return false;
        if (needBackMsg != other.needBackMsg)
            return false;
        if (!Arrays.equals(returnData, other.returnData))
            return false;
        return true;
    }
}
