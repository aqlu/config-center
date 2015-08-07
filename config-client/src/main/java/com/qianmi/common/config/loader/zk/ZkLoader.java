package com.qianmi.common.config.loader.zk;

import com.qianmi.common.config.loader.Loader;

/**
 * Zookeeper Loader
 * Created by aqlu on 15/5/16.
 */
public interface ZkLoader<T> extends Loader<T> {
    void save(String path, String value, String comment) throws Exception;
}
