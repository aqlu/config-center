package com.qianmi.common.config.controller;

import com.qianmi.common.config.ConfigSysSettings;
import com.qianmi.common.config.domain.ResultInfo;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.utils.ZKPaths;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.authentication.encoding.ShaPasswordEncoder;
import org.springframework.web.bind.annotation.*;

/**
 * App控制器
 * Created by aqlu on 15/7/10.
 */
@RestController
@RequestMapping("/config")
public class AppController {

    @Autowired
    private CuratorFramework curatorFramework;


    @RequestMapping(value = "/{appName}/_delete", method = RequestMethod.POST)
    @Secured({"ROLE_ADMIN", "ROLE_USER"})
    public ResultInfo remove(@PathVariable("appName") String appName) throws Exception {
        String appPath = ZKPaths.makePath(ConfigSysSettings.CONFIG_ZK_ROOT_PATH, appName);

        if (null != curatorFramework.checkExists().forPath(appPath)) {
            curatorFramework.delete().deletingChildrenIfNeeded().forPath(appPath);
        }
        return ResultInfo.SUCCESSFUL();
    }

    @RequestMapping(value = "/{appName}", method = { RequestMethod.PUT, RequestMethod.POST })
    @Secured({"ROLE_ADMIN", "ROLE_USER"})
    public ResultInfo add(@PathVariable("appName") String appName, @RequestParam String password) throws Exception {
        String appPath = ZKPaths.makePath(ConfigSysSettings.CONFIG_ZK_ROOT_PATH, appName);

        if (null == curatorFramework.checkExists().forPath(appPath)) {
            curatorFramework.inTransaction().create().forPath(appPath)
                    .and().setData().forPath(appPath, new ShaPasswordEncoder().encodePassword(password, null).getBytes("UTF-8"))
            .and().commit();
        }else{
            curatorFramework.setData().forPath(appPath, new ShaPasswordEncoder().encodePassword(password, null).getBytes("UTF-8"));
        }

        return ResultInfo.SUCCESSFUL();
    }

}
