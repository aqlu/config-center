package com.qianmi.common.config.loader;

/**
 * 加载器
 * Created by aqlu on 15/5/16.
 */
public interface Loader<T> {
    T load(String path) throws Exception;

    void destroy();
}
