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
}
