package com.qianmi.common.config.controller;

import com.qianmi.common.config.ConfigSysSettings;
import com.qianmi.common.config.domain.ResultInfo;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.utils.ZKPaths;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Group控制器
 * Created by aqlu on 15/5/26.
 */
@RestController
@RequestMapping("/config")
public class GroupController {

    @Autowired
    private CuratorFramework curatorFramework;

//    @RequestMapping(value = "/{appName}/{version}", method = RequestMethod.GET)
//    @Secured({"ROLE_ADMIN", "ROLE_USER"})
//    public List<Group> list(@PathVariable("appName") String appName, @PathVariable("version") String version)
//            throws Exception {
//        String versionPath = ZKPaths.makePath(ConfigSysSettings.CONFIG_ZK_ROOT_PATH, appName, version);
//
//        if (null == curatorFramework.checkExists().forPath(versionPath)) {
//            throw new ConfigException(HttpStatus.NOT_FOUND.value(), String.format("[%s/%s] not exists.", appName,
//                    version));
//        }
//
//        List<String> children = curatorFramework.getChildren().forPath(versionPath);
//
//        List<Group> groups = new ArrayList<Group>();
//        if (children != null) {
//            for (String child : children) {
//                groups.add(new Group(child));
//            }
//        }
//        return groups;
//    }

    @RequestMapping(value = "/{appName}/{version}/{groupName}/_delete", method = RequestMethod.POST)
    @Secured({"ROLE_ADMIN", "ROLE_USER"})
    public ResultInfo remove(@PathVariable("appName") String appName, @PathVariable("version") String version,
                             @PathVariable("groupName") String groupName) throws Exception {
        String groupPath = ZKPaths.makePath(ConfigSysSettings.CONFIG_ZK_ROOT_PATH, appName, version, groupName);
        String groupDescPath = ZKPaths.makePath(ConfigSysSettings.CONFIG_ZK_ROOT_PATH, appName, version + "$", groupName);
        if (null != curatorFramework.checkExists().forPath(groupPath)) {

            curatorFramework.delete().deletingChildrenIfNeeded().forPath(groupDescPath);
            curatorFramework.delete().deletingChildrenIfNeeded().forPath(groupPath);
//            curatorFramework.inTransaction().delete().forPath(groupPath)
//                    .and().delete().forPath(groupDescPath)
//                    .and().commit();
        }

        return ResultInfo.SUCCESSFUL();
    }

    @RequestMapping(value = "/{appName}/{version}/{groupName}", method = {RequestMethod.PUT, RequestMethod.POST})
    @Secured({"ROLE_ADMIN", "ROLE_USER"})
    public ResultInfo add(@PathVariable("appName") String appName, @PathVariable("version") String version,
                          @PathVariable("groupName") String groupName) throws Exception {
        String groupPath = ZKPaths.makePath(ConfigSysSettings.CONFIG_ZK_ROOT_PATH, appName, version, groupName);
        String groupDescPath = ZKPaths.makePath(ConfigSysSettings.CONFIG_ZK_ROOT_PATH, appName, version + "$", groupName);
        curatorFramework.inTransaction().create().forPath(groupPath)
                .and().create().forPath(groupDescPath)
                .and().commit();

        return ResultInfo.SUCCESSFUL();
    }
}
