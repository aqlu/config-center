package com.qianmi.common.config.listener;

import com.qianmi.common.config.PropertiesConfig;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.CuratorEvent;
import org.apache.curator.framework.api.CuratorListener;
import org.apache.curator.utils.ZKPaths;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Properties配置ZK监听器
 * Created by aqlu on 15/5/22.
 */
public class PropertiesZkListener implements CuratorListener {

    private static final Logger logger = LoggerFactory.getLogger(PropertiesZkListener.class);

    private PropertiesConfig propertiesConfig;

    public PropertiesZkListener(PropertiesConfig propertiesConfig){
        this.propertiesConfig = propertiesConfig;
    }

    public void eventReceived(CuratorFramework client, CuratorEvent event) throws Exception {
        final WatchedEvent watchedEvent = event.getWatchedEvent();
        logger.debug("event: {}", event);

        if (watchedEvent != null) {

            if (watchedEvent.getState() == Watcher.Event.KeeperState.SyncConnected) {
                String watchPath = watchedEvent.getPath();

                switch (watchedEvent.getType()) {
                    case NodeDeleted: // 节点删除事件，对应场景：删除group、删除key
                        ZKPaths.PathAndNode pathAndNode = ZKPaths.getPathAndNode(watchPath);

                        String parentPath = pathAndNode.getPath();
                        if (propertiesConfig.getVersionPath().equals(parentPath)) {
                            // 删除group
                            String groupName = pathAndNode.getNode();
                            propertiesConfig.removeGroup(groupName);
                        }
                        break;
                    case NodeChildrenChanged: // 子节点变化事件，对应场景：新增或删除group、新增或删除key
                        if (propertiesConfig.getVersionPath().equals(watchPath)) {
                            try {
                                propertiesConfig.reloadAllGroup();
                            } catch (Exception e) {
                                logger.warn("catch event[NodeChildrenChanged] on path [{}], but load failed.", watchPath, e);
                            }
                        } else {
                            // 重新加载某个group (对应场景：新增或删除某个key)
                            try {
                                propertiesConfig.reloadGroup(watchPath);
                            }
                            catch (KeeperException.NoNodeException e){
                                // group节点不存在，删除对应的group配置
                                String groupName = ZKPaths.getNodeFromPath(watchPath);
                                logger.info("catch event[NodeChildrenChanged] on path [{}], but this path not exists, so begin remove group [{}]", groupName);
                                propertiesConfig.removeGroup(groupName);
                            }
                            catch (Exception e) {
                                logger.warn("catch event[NodeChildrenChanged] on path [{}], but load failed.", watchPath, e);
                            }
                        }

                        break;
                    case NodeDataChanged: // 节点数据变化事件，对应场景：修改key节点数据
                        // 修改配置数据（对应场景：key节点数据修改）
                        try {
                            propertiesConfig.reloadKey(watchPath);
                        } catch (Exception e) {
                            logger.warn("catch event[NodeDataChanged] on path [{}], but load failed.", watchPath, e);
                        }

                        break;
                    default:
                        break;
                }
            }
        }
    }
}
