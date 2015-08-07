package com.qianmi.common.config;

import com.qianmi.common.config.convert.PropertyConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Config抽象类
 * Created by aqlu on 15/5/16.
 */
public abstract class AbstractConfig implements Config {

    private static final String SIMPLE_DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    protected Logger logger = LoggerFactory.getLogger(getClass());

    public Object get(String key, Object defaultValue) {
        Object val = get(key);
        if (val == null) {
            logger.debug("Can not get value of [{}], use default [{}]", key, defaultValue);
            return defaultValue;
        } else {
            return val;
        }
    }

    public String getString(String key) {
        return String.valueOf(get(key));
    }

    public String getString(String key, String defaultValue) {
        return String.valueOf(get(key, defaultValue));
    }

    public int getInt(String key) {
        return PropertyConverter.toInteger(get(key));
    }

    public int getInt(String key, String defaultValue) {
        return PropertyConverter.toInteger(get(key, defaultValue));
    }

    public boolean getBoolean(String key) {
        return PropertyConverter.toBoolean(get(key));
    }

    public boolean getBoolean(String key, String defaultValue) {
        return PropertyConverter.toBoolean(get(key, defaultValue));
    }

    public Date getDate(String key) {
        return PropertyConverter.toDate(get(key), SIMPLE_DATE_TIME_FORMAT);
    }

    public Date getDate(String key, String format) {
        return PropertyConverter.toDate(get(key), format);
    }

    public Date getDate(String key, String defaultValue, String format) {
        return PropertyConverter.toDate(get(key, defaultValue), format);
    }

    public Locale getLocale(String key) {
        return PropertyConverter.toLocale(get(key));
    }

    public Locale getLocale(String key, String defaultValue) {
        return PropertyConverter.toLocale(get(key, defaultValue));
    }

    public URL getURL(String key) {
        return PropertyConverter.toURL(get(key));
    }

    public URL getURL(String key, String defaultValue) {
        return PropertyConverter.toURL(get(key, defaultValue));
    }

    protected String getCurrentDateTimeStr() {
        return new SimpleDateFormat(SIMPLE_DATE_TIME_FORMAT).format(new Date());
    }
}
