package com.qianmi.common.config;

import com.qianmi.common.config.listener.PropertiesZkListener;
import com.qianmi.common.config.loader.file.FileLoader;
import com.qianmi.common.config.loader.file.PropertiesFileLoader;
import com.qianmi.common.config.loader.zk.PropertiesZkLoader;
import javafx.util.Pair;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.ZKPaths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Properties配置工具类
 * Created by aqlu on 15/5/15.
 */
public class PropertiesConfig extends AbstractConfig {
    private static final String PROPERTIES_EXTENSION = ".properties";

    private static final Logger logger = LoggerFactory.getLogger(PropertiesConfig.class);

    private ConcurrentMap<String, Properties> configs = new ConcurrentHashMap<String, Properties>();

    private FileLoader<Properties> propertiesFileLoader = new PropertiesFileLoader();

    private PropertiesZkLoader propertiesZkLoader;

    private String versionPath;

    private ExecutorService executorService = Executors.newFixedThreadPool(2);

    private Timer timer = new Timer();

    private static class PropertiesConfigHolder {
        private static final PropertiesConfig INSTANCE = new PropertiesConfig();
    }

    public static PropertiesConfig getInstance() {
        return PropertiesConfigHolder.INSTANCE;
    }

    private PropertiesConfig() {
        init();
    }

    public void init() {
        logger.info("PropertiesConfig initializing ...");

        String connectStr = ConfigSysSettings.CONFIG_ZK_CONNECTION_STR;
        int sessionTimeout = ConfigSysSettings.CONFIG_ZK_CONNECTION_SESSION_TIMEOUT;
        int connectTimeout = ConfigSysSettings.CONFIG_ZK_CONNECTION_TIMEOUT;
        int baseInterval = ConfigSysSettings.CONFIG_ZK_RETRY_BASE_INTERVAL;
        int retryCount = ConfigSysSettings.CONFIG_ZK_RETRY_COUNT;
        String rootPath = ConfigSysSettings.CONFIG_ZK_ROOT_PATH;
        String version = ConfigSysSettings.CONFIG_VERSION;
        boolean openLocalCache = ConfigSysSettings.CONFIG_OPEN_LOCAL_CACHE;

        logger.info("begin connect to zookeeper parameters:[connectStr={}, sessionTimeout={}, connectTimeout={}, "
                + "baseInterval={}, retryCount={}, rootPath={}, version={}, openLocalCache={}]", connectStr,
                sessionTimeout, connectTimeout, baseInterval, retryCount, rootPath, version, openLocalCache);

        try {
            // 【step.1】 从zk获取数据
            propertiesZkLoader = new PropertiesZkLoader(connectStr, sessionTimeout, connectTimeout,
                    new ExponentialBackoffRetry(baseInterval, retryCount), new PropertiesZkListener(this));
            this.versionPath = ZKPaths.makePath(rootPath, version);

            loadFromZk();

            // step.2.1 如果获取成功，则刷新本地cache
            cacheToLocal();

            // step.3 启动定时器
            timer.scheduleAtFixedRate(new TimerTask() {
                public void run() {
                    try {
                        logger.debug("consistency check begin");
                        reloadAllGroup();
                        logger.debug("consistency check end");
                    } catch (Exception e) {
                        logger.warn("consistency check failed.", e);
                    }
                }
            }, 60 * 1000, ConfigSysSettings.CONFIG_CHECK_RATE);
        } catch (Throwable throwable) {
            logger.warn("#### load properties from zookeeper failed.", throwable);

            // step.2.2 如果获取失败，则获取本地cache
            if (!loadFromLocal()) {
                // step.2.2.1 如果本地获取失败，抛出异常，终止启动
                logger.error("### load properties failed, System will to exit.");
                System.exit(1);
            }
        }

        logger.info("PropertiesConfig initialized !");
    }

    /**
     * <p>
     * 从zookeeper加载配置文件
     * </p>
     * @throws Exception
     */
    private void loadFromZk() throws Exception {
        logger.info("begin to load properties from zookeeper: [{}]", this.versionPath);

        List<String> groupNames = propertiesZkLoader.getAllGroupNames(versionPath);

        if (groupNames != null) {
            for (String groupName : groupNames) {
                String groupPath = ZKPaths.makePath(versionPath, groupName);
                loadGroupFromZk(groupPath);
            }
        }

        logger.info("load properties from zookeeper finished.");
    }

    /**
     * <p>
     * 获取本地文件
     * </p>
     * @return 是否成功
     */
    private boolean loadFromLocal() {
        String configHome = ConfigSysSettings.CONFIG_HOME;
        String version = ConfigSysSettings.CONFIG_VERSION;
        String cacheSuffix = ConfigSysSettings.CACHE_FILE_SUFFIX;
        final String filterRegex = String.format(".*.(%s)(%s)[%s]*$", version, PROPERTIES_EXTENSION, cacheSuffix);

        File configHomeDir = new File(configHome);
        File[] configFiles = configHomeDir.listFiles(new FileFilter() {
            public boolean accept(File file) {
                return file.isFile() && file.getName().matches(filterRegex);
            }
        });

        if (configFiles != null) {
            for (File configFile : configFiles) {
                try {
                    loadGroupFromFile(configFile);
                } catch (Exception e) {
                    logger.warn("#### failed to load properties from file: [{}]", configFile.getPath());
                    return false;
                }
            }
        }

        logger.info("load properties from local finished.");
        return true;
    }

    /**
     * <p>
     * 从zookeeper加载group
     * </p>
     * @param groupPath 完整group路径
     * @throws Exception
     */
    private void loadGroupFromZk(String groupPath) throws Exception {
        logger.info("load properties from zookeeper: [{}]", groupPath);

        configs.put(ZKPaths.getNodeFromPath(groupPath), propertiesZkLoader.load(groupPath));
    }

    /**
     * <p>
     * 从文件加载group
     * </p>
     * @param configFile 配置文件
     * @throws Exception
     */
    private void loadGroupFromFile(File configFile) throws Exception {
        logger.info("load properties from file: [{}]", configFile.getPath());

        configs.put(getGroupNameFromFileName(configFile.getName()), propertiesFileLoader.load(configFile.getPath()));
    }

    /**
     * 缓存配置到问本地；文件名为：groupName.version.properties.cache
     */
    private void cacheToLocal() {
        Set<Map.Entry<String, Properties>> set = configs.entrySet();

        logger.info("begin to cache properties to local");

        for (Map.Entry<String, Properties> entry : set) {
            String groupName = entry.getKey();
            Properties properties = entry.getValue();

            if (StringUtils.hasText(groupName) && properties != null) {
                Properties propertiesCopy = new Properties();
                propertiesCopy.putAll(properties);
                cacheGroupToLocal(groupName, propertiesCopy);
            }
        }
        logger.info("cache to local finished");

    }

    private void cacheGroupToLocal(String groupName, Properties properties) {
        if (ConfigSysSettings.CONFIG_OPEN_LOCAL_CACHE) {

            String comments = String.format("Cached from zookeeper configuration group: [%s/%s]", versionPath,
                    groupName);
            String filePath = generateCacheFilePath(groupName);

            try {
                propertiesFileLoader.save(filePath, properties, comments);
                logger.info("cached group[{}] to [{}]", groupName, filePath);
            } catch (Exception e) {
                logger.warn("cache file to local failed! filePath:{}", filePath, e);
            }
        }
    }

    /**
     * <p>
     * 删除group缓存
     * </p>
     * @param groupName group名
     */
    private void deleteCache(String groupName) {
        String filePath = generateCacheFilePath(groupName);
        logger.info("delete cache file: [{}]", filePath);
        try {
            Files.deleteIfExists(Paths.get(filePath));
        } catch (IOException e) {
            logger.warn("delete cache file failed. filePath: [{}]", filePath, e);
        }
    }

    /**
     * 根据配置文件名获取group名
     * @param fileName 文件名称
     * @return group名称
     */
    private String getGroupNameFromFileName(String fileName) {
        return fileName.substring(
                0,
                fileName.lastIndexOf("." + ConfigSysSettings.CONFIG_VERSION + PROPERTIES_EXTENSION
                        + ConfigSysSettings.CACHE_FILE_SUFFIX));
    }

    /**
     * 根据groupName生成缓存路径
     * @param groupName group名称
     * @return 缓存路径
     */
    private String generateCacheFilePath(String groupName) {
        String configHome = ConfigSysSettings.CONFIG_HOME;
        String version = ConfigSysSettings.CONFIG_VERSION;
        String cacheSuffix = ConfigSysSettings.CACHE_FILE_SUFFIX;
        String fileName = groupName.concat(".").concat(version).concat(PROPERTIES_EXTENSION).concat(cacheSuffix);

        return configHome + ConfigSysSettings.SYSTEM_SEPARATOR + fileName;
    }

    /**
     * 获取配置值
     * @param key 属性键
     * @return 属性值
     */
    public Object get(String key) {
        Set<Map.Entry<String, Properties>> set = configs.entrySet();

        for (Map.Entry<String, Properties> entry : set) {
            Properties properties = entry.getValue();
            if (properties != null) {
                if (properties.getProperty(key) != null) {
                    return properties.getProperty(key);
                }
            }
        }

        logger.info("can not found key: [{}]", key);
        return null;
    }

    /**
     * <p>
     * 获取指定group的配置值
     * </p>
     * @param group group名
     * @param key 属性键
     * @return 属性值
     */
    public Object get(String group, String key) {
        Properties properties = configs.get(group);
        return properties == null ? null : properties.get(key);
    }

    /**
     * <p>
     * 重新加载所有Group
     * </p>
     * @throws Exception
     */
    public void reloadAllGroup() throws Exception {
        // 重新加载所有group
        loadFromZk();

        // 缓存到本地
        if (ConfigSysSettings.CONFIG_OPEN_LOCAL_CACHE) {
            executorService.execute(new Runnable() {
                public void run() {
                    cacheToLocal();
                }
            });
        }
    }

    /**
     * <p>
     * 重新加载group
     * </p>
     * @param groupPath groupPath
     * @throws Exception
     */
    public void reloadGroup(String groupPath) throws Exception {
        // 重新加载group
        Properties properties = propertiesZkLoader.load(groupPath);
        final String groupName = ZKPaths.getNodeFromPath(groupPath);
        configs.put(groupName, properties);

        // 重新缓存group到本地
        if (ConfigSysSettings.CONFIG_OPEN_LOCAL_CACHE) {
            final Properties newCopyProperties = new Properties();
            newCopyProperties.putAll(properties);

            executorService.execute(new Runnable() {
                public void run() {
                    cacheGroupToLocal(groupName, newCopyProperties);
                }
            });
        }
    }

    /**
     * <p>
     * 重置指定group的属性值
     * </p>
     * @param keyPath key节点路径
     * @throws Exception
     */
    public boolean reloadKey(String keyPath) throws Exception {

        final String groupPath = ZKPaths.getPathAndNode(keyPath).getPath();
        final String groupName = ZKPaths.getNodeFromPath(groupPath);

        Pair<String, String> keyValue = propertiesZkLoader.loadKey(keyPath);

        // properties.setProperty(key, value);
        Properties properties = configs.get(groupName);

        if (properties == null) {
            logger.warn("can not found group:[{}]", groupName);
            return false;
        } else {
            properties.setProperty(keyValue.getKey(), keyValue.getValue());

            // 缓存到本地
            if (ConfigSysSettings.CONFIG_OPEN_LOCAL_CACHE) {
                final Properties newCopyProperties = new Properties();
                newCopyProperties.putAll(properties);
                executorService.execute(new Runnable() {
                    public void run() {
                        cacheGroupToLocal(groupName, newCopyProperties);
                    }
                });
            }
            return true;
        }
    }

    public String getVersionPath(){
        return this.versionPath;
    }

    /**
     * <p>
     * 删除某个group的所有配置
     * </p>
     * @param group group名称
     */
    public void removeGroup(final String group) {
        Assert.hasText(group, "argument [group] must not be null, empty, or blank");
        configs.remove(group);

        // 清空缓存
        if (ConfigSysSettings.CONFIG_OPEN_LOCAL_CACHE) {
            executorService.execute(new Runnable() {
                public void run() {
                    deleteCache(group);
                }
            });
        }
    }

    /**
     * 资源销毁
     */
    public void destroy() {
        timer.cancel();

        if (propertiesFileLoader != null) {
            propertiesFileLoader.destroy();
        }
        if (propertiesZkLoader != null) {
            propertiesZkLoader.destroy();
        }
    }

}
