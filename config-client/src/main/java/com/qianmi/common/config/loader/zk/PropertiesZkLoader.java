package com.qianmi.common.config.loader.zk;

import javafx.util.Pair;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.CuratorListener;
import org.apache.curator.utils.ZKPaths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * <p>
 * 基于Zookeeper存储的Properties加载类；
 * </p>
 * 
 * <pre>
 * zookeeper存储设计示例如下：
 * /config(root path)
 * ├── 1.0(version)
 * │   ├── default(group name, each group as a file)
 * │   │   ├─── propName(key name)  --> value1
 * │   │   └─── propName2 --> value2
 * │   └── dbconfig
 * │       ├─── url --> jdbc:mysql://127.0.0.1:3306/test?characterEncoding=UTF-8
 * │       ├─── username --> root
 * │       ├─── password --> root
 * │       └─── dirver --> com.mysql.jdbc.Driver
 * ├── 2.0
 * │   └── ...
 * └── ...
 * </pre>
 * 
 * Created by aqlu on 15/5/16.
 */
public class PropertiesZkLoader implements ZkLoader<Properties> {
    protected Logger logger = LoggerFactory.getLogger(getClass());

    private CuratorFramework curator;

    private CuratorListener listener;

    public PropertiesZkLoader(String connectStr, int sessionTimeout, int connectTimeout, RetryPolicy retryPolicy,
            CuratorListener listener) {
        this.curator = CuratorFrameworkFactory.newClient(connectStr, sessionTimeout, connectTimeout, retryPolicy);
        this.listener = listener;
        curator.getCuratorListenable().addListener(this.listener);
        curator.start();
    }

    public List<String> getAllGroupNames(final String versionPath) throws Exception {
        // 获取所有的group节点，并监控version节点下的变化
        return curator.getChildren().watched().forPath(versionPath);
    }

    /**
     * 加载所有group节点数据
     * @param versionPath version节点路径
     * @return 配置集合Map
     * @throws Exception
     */
    public Map<String, String> loadAllGroup(final String versionPath) throws Exception {
        Assert.hasText(versionPath, "[versionPath] must not be null or empty");

        Map<String, String> configs = new HashMap<String, String>();

        List<String> groupNodes = null;
        try {
            // 获取所有的group节点，并监控version节点下的变化
            groupNodes = curator.getChildren().watched().forPath(versionPath);
        } catch (Exception e) {
            logger.warn("**** get children from [{}] failed!", versionPath);
            throw e;
        }
        if (groupNodes != null) {
            for (String groupNode : groupNodes) {
                configs.putAll(loadGroup(ZKPaths.makePath(versionPath, groupNode)));
            }
        }
        return configs;
    }

    /**
     * 加载Group节点数据
     * @param groupPath group节点路径
     * @return 配置集合Map
     * @throws Exception
     */
    public Map<String, String> loadGroup(final String groupPath) throws Exception {
        Assert.hasText(groupPath, "[groupPath] must not be null or empty");

        Map<String, String> configs = new HashMap<String, String>();

        // 获取group的子节点列表，并监控子节点的变化
        List<String>  keyNodes = curator.getChildren().watched().forPath(groupPath);

        if (keyNodes != null) {
            for (String keyNode : keyNodes) {
                Pair<String, String> keyValue = loadKey(ZKPaths.makePath(groupPath, keyNode));
                if (keyValue != null) {
                    configs.put(keyValue.getKey(), keyValue.getValue());
                }
            }
        }

        return configs;
    }

    /**
     * 获取子节点上的数据，并返回节点名与节点值的key->value对
     * @param keyPath Key节点的完整路径
     * @return 节点名与节点值的key->value对
     * @throws Exception
     */
    public Pair<String, String> loadKey(final String keyPath) throws Exception {
        Assert.hasText(keyPath, "[keyPath] must not be null or empty");

        byte[] data = new byte[0];
        try {
            // 获取节点上的数据，并监控节点值的变化
            data = curator.getData().watched().forPath(keyPath);
        } catch (Exception e) {
            logger.warn("**** load value from [{}] failed! ", keyPath);
            throw e;
        }

        String value = new String(data, Charset.forName("UTF-8"));
        String key = ZKPaths.getNodeFromPath(keyPath);// 获取key名称

        return new Pair<String, String>(key, value);
    }

    /**
     * 从zookeeper加载配置数据
     * @param groupPath group节点路径，若为空表示加载所有group节点；
     * @return
     */
    public Properties load(String groupPath) throws Exception {
        Assert.hasText(groupPath, "[groupPath] must not be null or empty");

        Properties properties = new Properties();
        properties.putAll(loadGroup(groupPath));

        return properties;
    }

    public void destroy() {
        if (curator != null) {
            curator.getCuratorListenable().removeListener(listener);
            curator.close();
        }
    }

    public void save(String path, String value, String comment) throws Exception {

    }

}
