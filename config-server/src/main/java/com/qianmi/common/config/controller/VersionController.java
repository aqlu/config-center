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
 * Version控制器
 * Created by aqlu on 15/5/26.
 */
@RestController
@RequestMapping("/config")
public class VersionController {

    @Autowired
    private CuratorFramework curatorFramework;
//
//    @RequestMapping(value = "/{appName}/", method = RequestMethod.GET)
//    @Secured({"ROLE_ADMIN", "ROLE_USER"})
//    public List<String> list(@PathVariable("appName") String appName) throws Exception {
//
//        String appPath = ZKPaths.makePath(ConfigSysSettings.CONFIG_ZK_ROOT_PATH, appName);
//
//        if (null == curatorFramework.checkExists().forPath(appPath)) {
//            throw new ConfigException(HttpStatus.NOT_FOUND.value(), String.format("[%s] not exists.", appName));
//        }
//
//        return curatorFramework.getChildren().forPath(appPath);
//    }

    @RequestMapping(value = "/{appName}/{version}/_delete", method = RequestMethod.POST)
    @Secured({"ROLE_ADMIN", "ROLE_USER"})
    public ResultInfo remove(@PathVariable("appName") String appName, @PathVariable("version") String version)
            throws Exception {
        String versionPath = ZKPaths.makePath(ConfigSysSettings.CONFIG_ZK_ROOT_PATH, appName, version);
        if (null != curatorFramework.checkExists().forPath(versionPath)) {
            curatorFramework.inTransaction().delete().forPath(versionPath).and().delete().forPath(versionPath + "$").and().commit();
        }
        return ResultInfo.SUCCESSFUL();
    }

    @RequestMapping(value = "/{appName}/{version}", method = {RequestMethod.PUT, RequestMethod.POST})
    @Secured({"ROLE_ADMIN", "ROLE_USER"})
    public ResultInfo add(@PathVariable("appName") String appName, @PathVariable("version") String version)
            throws Exception {
        String versionPath = ZKPaths.makePath(ConfigSysSettings.CONFIG_ZK_ROOT_PATH, appName, version);
        if (null != curatorFramework.checkExists().forPath(ZKPaths.makePath(ConfigSysSettings.CONFIG_ZK_ROOT_PATH, appName))) {
            curatorFramework.inTransaction().create().forPath(versionPath).and().create().forPath(versionPath + '$').and().commit();
        }
        return ResultInfo.SUCCESSFUL();
    }
}
