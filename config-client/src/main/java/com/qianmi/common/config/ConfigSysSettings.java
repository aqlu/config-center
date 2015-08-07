package com.qianmi.common.config;

import org.apache.curator.retry.ExponentialBackoffRetry;

/**
 * 系统设置常量类
 * Created by aqlu on 15/5/16.
 */
public final class ConfigSysSettings {
    private ConfigSysSettings() {
    }

    public static final String SYSTEM_SEPARATOR = System.getProperty("file.separator");

    /**
     * <p>Config缓存文件存放目录地址，默认为user.home目录下的config目录；设置方式：-Dconfig.home=/path/to/cache/config/file</p>
     */
    public static final String CONFIG_HOME = System.getProperty("config.home", System.getProperty("user.home") + SYSTEM_SEPARATOR + "config");

    /**
     * <p>Config缓存文件的后缀名，默认为.cache；设置方式：-Dconfig.cacheFileSuffix=.cache</p>
     */
    public static final String CACHE_FILE_SUFFIX = System.getProperty("config.cacheFileSuffix", ".cache");

    /**
     * <p>Config文件是否缓存到本地，默认为true；设置方式：-Dconfig.openLocalCache=true</p>
     */
    public static final boolean CONFIG_OPEN_LOCAL_CACHE = Boolean.parseBoolean(System.getProperty("config.openLocalCache", "true"));

    /**
     * <p>Config版本号，用来区分不同版本的配置，默认为1.0；设置方式：-Dconfig.version=1.0</p>
     */
    public static final String CONFIG_VERSION = System.getProperty("config.version", "1.0");

    /**
     * <p>zookeeper服务器连接地址，可以是多台，默认为：127.0.0.1:2181；设置方式：-Dconfig.zk.connection.str=127.0.0.1:2181</p>
     */
    public static final String CONFIG_ZK_CONNECTION_STR = System.getProperty("config.zk.connection.str", "127.0.0.1:2181");

    /**
     * <p>zookeeper连接超时时间，单位毫秒，默认为2000（即2秒）；设置方式：-Dconfig.zk.connection.timeout=2000</p>
     */
    public static final int CONFIG_ZK_CONNECTION_TIMEOUT = Integer.parseInt(System.getProperty("config.zk.connection.timeout", "2000"));

    /**
     * <p>zookeeper会话超时时间，单位毫秒，默认为60000（即1分钟）；设置方式：-Dconfig.zk.connection.sessionTimeout=60000</p>
     */
    public static final int CONFIG_ZK_CONNECTION_SESSION_TIMEOUT = Integer.parseInt(System.getProperty("config.zk.connection.sessionTimeout", "60000"));

    /**
     * <p>zookeeper连接失败重试次数，默认3次；设置方式：-Dconfig.zk.retry.count=3</p>
     * 注：zookeeper连接失败策略使用：{@link ExponentialBackoffRetry}
     */
    public static final int CONFIG_ZK_RETRY_COUNT = Integer.parseInt(System.getProperty("config.zk.retry.count", "3"));

    /**
     * <p>zookeeper连接失败重试时间间隔基数，单位毫秒，默认1000（即1秒）；设置方式：-Dconfig.zk.retry.baseInterval=1000</p>
     * 注：zookeeper连接失败策略使用：{@link ExponentialBackoffRetry}
     */
    public static final int CONFIG_ZK_RETRY_BASE_INTERVAL = Integer.parseInt(System.getProperty("config.zk.retry.baseInterval", "1000"));

    /**
     * <p>zookeeper根路径地址，建议采用/config/{appName}方式设置，默认为：/config；设置方式：-Dconfig.zk.rootPath=/config</p>
     */
    public static final String CONFIG_ZK_ROOT_PATH = System.getProperty("config.zk.rootPath", "/config");

    /**
     * <p>配置同步检查速率，单位毫秒，默认为60000（即1分钟）；设置方式：-Dconfig.checkRate=60000</p>
     */
    public static final int CONFIG_CHECK_RATE = Integer.parseInt(System.getProperty("config.checkRate", "60000"));

    public static void main(String[] args) {
        System.out.println(ConfigSysSettings.class.getClassLoader().getResource("").getPath());
        System.out.println(System.getProperty("user.home"));
        System.out.println(System.getProperty("user.dir"));
    }
}
