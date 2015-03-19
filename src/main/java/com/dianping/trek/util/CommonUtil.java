package com.dianping.trek.util;

import org.json.JSONException;
import org.json.JSONObject;

public class CommonUtil {
    
    public static String getString(JSONObject jsonObject, String key, String defaultValue) {
        try {
            return jsonObject.getString(key);
        } catch (JSONException e) {
            return defaultValue;
        }
    }
    
    public static Integer getInteger(JSONObject jsonObject, String key, Integer defaultValue) {
        try {
            return jsonObject.getInt(key);
        } catch (JSONException e) {
            return defaultValue;
        }
    }
    
    public static Boolean getBoolean(JSONObject jsonObject, String key, Boolean defaultValue) {
        try {
            return jsonObject.getBoolean(key);
        } catch (JSONException e) {
            return defaultValue;
        }
    }
    
    public static byte[] int2byte(int res) {
        byte[] targets = new byte[4];
        targets[0] = (byte) (res & 0xff);// 最低位 
        targets[1] = (byte) ((res >> 8) & 0xff);// 次低位 
        targets[2] = (byte) ((res >> 16) & 0xff);// 次高位 
        targets[3] = (byte) (res >>> 24);// 最高位,无符号右移。 
        return targets; 
    }
}
