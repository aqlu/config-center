package com.qianmi.common.config.controller;

import com.qianmi.common.config.ConfigSysSettings;
import com.qianmi.common.config.domain.Property;
import com.qianmi.common.config.domain.ResultInfo;
import com.qianmi.common.config.exception.ConfigException;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.transaction.CuratorTransactionFinal;
import org.apache.curator.utils.ZKPaths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * Property控制器
 * Created by aqlu on 15/5/26.
 */
@RestController
@RequestMapping("/config")
public class PropertyController {

    private static Logger logger = LoggerFactory.getLogger(PropertyController.class);

    @Autowired
    private CuratorFramework curatorFramework;

    @RequestMapping(value = "/{appName}/{version}/{groupName}/_list", method = RequestMethod.GET)
    @Secured({"ROLE_ADMIN", "ROLE_USER"})
    public ResultInfo list(@PathVariable("appName") String appName, @PathVariable("version") String version,
                           @PathVariable("groupName") String groupName) throws Exception {
        String groupPath = ZKPaths.makePath(ConfigSysSettings.CONFIG_ZK_ROOT_PATH, appName, version, groupName);

        if (null == curatorFramework.checkExists().forPath(groupPath)) {
            throw new ConfigException(HttpStatus.NOT_FOUND.value(), String.format("[%s/%s/%s] not exists.", appName,
                    version, groupName));
        }

        List<String> children = curatorFramework.getChildren().forPath(groupPath);

        List<Property> properties = new ArrayList<Property>();
        if (children != null) {
            for (String child : children) {
                String value = new String(curatorFramework.getData().forPath(
                        ZKPaths.makePath(groupPath, child)), Charset.forName("UTF-8"));
                String description = new String(curatorFramework.getData().forPath(
                        ZKPaths.makePath(ConfigSysSettings.CONFIG_ZK_ROOT_PATH, appName, version + "$", groupName,
                                child)), Charset.forName("UTF-8"));
                properties.add(new Property(child, value, description));
            }
        }
        ResultInfo resultInfo = ResultInfo.SUCCESSFUL();
        resultInfo.setDatas(properties);
        return resultInfo;
    }

    @RequestMapping(value = "/{appName}/{version}/{groupName}/{propNames}/_delete", method = RequestMethod.POST)
    @Secured({"ROLE_ADMIN", "ROLE_USER"})
    public ResultInfo remove(@PathVariable("appName") String appName, @PathVariable("version") String version,
                             @PathVariable("groupName") String groupName, @PathVariable("propNames") String[] propNames) throws Exception {
        CuratorTransactionFinal transaction = (CuratorTransactionFinal)curatorFramework.inTransaction();
        for (String propName : propNames) {
            String propPath = ZKPaths
                    .makePath(ConfigSysSettings.CONFIG_ZK_ROOT_PATH, appName, version, groupName, propName);
            String propDescPath = ZKPaths.makePath(ConfigSysSettings.CONFIG_ZK_ROOT_PATH, appName, version + "$",
                    groupName, propName);

            try {
                transaction.delete().forPath(propPath).and().delete().forPath(propDescPath);
            } catch (Exception e) {
                //ignore
                logger.info("remove path failed.", e);
            }
        }
        transaction.commit();

        return ResultInfo.SUCCESSFUL();
    }

    @RequestMapping(value = "/{appName}/{version}/{groupName}/{propName}", method = {RequestMethod.PUT,
            RequestMethod.POST})
    @Secured({"ROLE_ADMIN", "ROLE_USER"})
    public ResultInfo add(@PathVariable("appName") String appName, @PathVariable("version") String version,
                          @PathVariable("groupName") String groupName, @PathVariable("propName") String propName, Property property)
            throws Exception {

        String propPath = ZKPaths
                .makePath(ConfigSysSettings.CONFIG_ZK_ROOT_PATH, appName, version, groupName, propName);
        String propDescPath = ZKPaths.makePath(ConfigSysSettings.CONFIG_ZK_ROOT_PATH, appName, version + "$",
                groupName, propName);

        CuratorTransactionFinal transaction = (CuratorTransactionFinal)curatorFramework.inTransaction();

        if(null == curatorFramework.checkExists().forPath(propPath)) {
            transaction.create().forPath(propPath);
        }
        if(null == curatorFramework.checkExists().forPath(propDescPath)) {
            transaction.create().forPath(propDescPath);
        }

        transaction.setData().forPath(propPath, property.getValue().getBytes("UTF-8"))
                .and().setData().forPath(propDescPath, property.getDescription().getBytes("UTF-8"))
                .and().commit();

        return ResultInfo.SUCCESSFUL();
    }
}
