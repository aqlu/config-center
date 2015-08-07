package com.qianmi.common.config.security;

import com.qianmi.common.config.ConfigSysSettings;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.utils.ZKPaths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.nio.charset.Charset;

import static org.springframework.security.core.authority.AuthorityUtils.createAuthorityList;

/**
 *
 * Created by aqlu on 15/5/30.
 */
@Component
public class SimpleUserDetailsService implements UserDetailsService {

    private static Logger logger = LoggerFactory.getLogger(SimpleUserDetailsService.class);

    @Autowired
    private CuratorFramework curatorFramework;


    @Value("${config-server.adminUser}")
    private String adminUser;

    @Value("${config-server.adminPassword}")
    private String adminPassword;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        if (adminUser.equals(username)) {
            return new User(username, adminPassword, createAuthorityList("ROLE_ADMIN", "ROLE_USER"));
        }

        String appPath = ZKPaths.makePath(ConfigSysSettings.CONFIG_ZK_ROOT_PATH, username);
        try {
            String password = new String(curatorFramework.getData().forPath(appPath), Charset.forName("UTF-8"));

            return new User(username, password, createAuthorityList("ROLE_USER"));
        } catch (Exception e) {
            logger.warn("get data from [{}] failed. errorMsg:{}", appPath, e.getMessage());
            logger.info("get data from [{}] failed. ex:{}", appPath, e);
            throw new UsernameNotFoundException("[" + username + "] not found.", e);
        }
    }
}
