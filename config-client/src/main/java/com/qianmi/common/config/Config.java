package com.qianmi.common.config;

/**
 * Config
 * Created by aqlu on 15/5/15.
 */
public interface Config {
    Object get(String key);

    Object get(String group, String key);
}
