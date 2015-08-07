package com.qianmi.common.config.loader.file;

import com.qianmi.common.config.loader.Loader;

/**
 * 文件加载接口
 * Created by aqlu on 15/5/15.
 */
public interface FileLoader<T> extends Loader<T> {
    void save(String path, T datas, String comments) throws Exception;
}
