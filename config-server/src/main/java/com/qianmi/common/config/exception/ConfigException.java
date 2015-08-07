package com.qianmi.common.config.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * Created by aqlu on 15/5/27.
 */
public class ConfigException extends RuntimeException {
    private static Logger logger = LoggerFactory.getLogger(ConfigException.class);

    private int code;

    public ConfigException(int code, String message) {
        super(message);
        this.code = code;
        logger.warn("Occured exception, code:{}, message:{}", code, this.getMessage());
    }


    public ConfigException(int code, Throwable cause) {
        this.code = code;
        logger.warn("Occured exception, code:{}", code,  cause);
    }

    public int getCode() {
        return code;
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return null;
    }
}
