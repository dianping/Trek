package com.dianping.trek.util;

import java.lang.reflect.Constructor;

public class ReflectionUtils {
    
    public static <T> T newInstance(Class<T> theClass, Object... params) {
        Class<?>[] paramTypeArray = new Class[params.length];
        for (int i = 0; i < params.length; i++) {
            paramTypeArray[i] = params[i].getClass();
        }
        T result;
        try {
            Constructor<T> meth= theClass.getDeclaredConstructor(paramTypeArray);
                meth.setAccessible(true);
            result = meth.newInstance(params);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return result;
    }
}
