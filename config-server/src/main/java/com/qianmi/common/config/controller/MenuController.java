package com.qianmi.common.config.controller;

import com.qianmi.common.config.ConfigSysSettings;
import com.qianmi.common.config.domain.ExtMenu;
import com.qianmi.common.config.domain.Menu;
import com.qianmi.common.config.domain.ResultInfo;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.utils.ZKPaths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

/**
 * Menu控制层
 * Created by aqlu on 15/6/15.
 */
@RestController
@RequestMapping("/menu")
public class MenuController {

    private static Logger logger = LoggerFactory.getLogger(MenuController.class);

    private static final String ID_PREFIX = "ext";
    private static final String ID_APPEND = "-";
    private static final String ROOT_ICONCLS = "icon-setting";
    private static final String APP_ICONCLS = "icon-red";
    private static final String VERSION_ICONCLS = "icon-blue";
    private static final String GROUP_ICONCLS = "icon-yellow";

    @Autowired
    private CuratorFramework curatorFramework;


    @RequestMapping(value = "/tree.do", method = {RequestMethod.GET, RequestMethod.POST})
    @Secured({"ROLE_ADMIN", "ROLE_USER"})
    public
    @ResponseBody
    Iterable<Menu> findMenusTree(HttpServletRequest request) {
        String user = SecurityContextHolder.getContext().getAuthentication().getName();
        boolean isAdmin = SecurityContextHolder.getContext().getAuthentication().getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));

        List<Menu> menuList = new ArrayList<Menu>();

        ExtMenu rootMenu = new ExtMenu("ext-root-menu", "配置中心", ROOT_ICONCLS, null, false, "", "", "ROOT");

        try {
            List<String> appList = curatorFramework.getChildren().forPath(ConfigSysSettings.CONFIG_ZK_ROOT_PATH);

            // 遍历所有app
            for (String app : appList) {

                if (isAdmin || user.equals(app)) {
                    String appId = ID_PREFIX.concat(ID_APPEND).concat(app);
                    ExtMenu appMenu = new ExtMenu(appId, app, APP_ICONCLS, "ext-root-menu", false, appId, app, "APP");

                    String appPath = ZKPaths.makePath(ConfigSysSettings.CONFIG_ZK_ROOT_PATH, app);
                    try {
                        List<String> versionList = curatorFramework.getChildren().forPath(appPath);

                        // 遍历所有version
                        for (String version : versionList) {
                            if(version.endsWith("$")){
                                continue;
                            }

                            String versionId = appId.concat(ID_APPEND).concat(version);
                            String versionIdPath = appId.concat(ID_APPEND).concat(versionId);
                            String versionTextPath = app.concat(">").concat(version);

                            ExtMenu versionMenu = new ExtMenu(versionId, version, VERSION_ICONCLS, appId, false, versionIdPath, versionTextPath, "VERSION");

                            String versionPath = ZKPaths.makePath(appPath, version);
                            try {
                                List<String> groupList = curatorFramework.getChildren().forPath(versionPath);

                                // 遍历所有group
                                for (String group : groupList) {

                                    String groupId = versionId.concat(ID_APPEND).concat(group);
                                    String groupIdPath = versionIdPath.concat(ID_APPEND).concat(groupId);
                                    String groupTextPath = versionTextPath.concat(">").concat(group);

                                    ExtMenu groupMenu = new ExtMenu(groupId, group, GROUP_ICONCLS, versionId, true, groupIdPath, groupTextPath, "GROUP");

                                    versionMenu.addChildren(groupMenu);
                                }
                            } catch (Exception e) {
                                logger.warn("获取[{}]的子节点失败", versionPath, e);
                            }
                            appMenu.addChildren(versionMenu);
                        }

                    } catch (Exception e) {
                        logger.warn("获取[{}]的子节点失败", appPath, e);
                    }
                    rootMenu.addChildren(appMenu);
                }
            }
        } catch (Exception e) {
            logger.warn("获取[{}]的子节点失败", ConfigSysSettings.CONFIG_ZK_ROOT_PATH, e);
        }
        menuList.add(rootMenu);
        return menuList;
    }

    @RequestMapping(value = "/remove.do", method = {RequestMethod.POST, RequestMethod.DELETE})
    @Secured("ROLE_ADMIN")
    public
    @ResponseBody
    ResultInfo removeMenus(@RequestParam Long... ids) {
        return ResultInfo.SUCCESSFUL();
    }

    @RequestMapping(value = "/save.do", method = {RequestMethod.POST})
    @Secured("ROLE_ADMIN")
    public
    @ResponseBody
    ResultInfo saveMenus(@RequestParam Menu menu) {
        return ResultInfo.SUCCESSFUL();
    }
}
