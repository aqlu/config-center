package com.qianmi.common.config.constants;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;

/**
 *
 * Created by aqlu on 15/5/30.
 */
@ConfigurationProperties(prefix = "config-server", ignoreInvalidFields = false)
@ManagedResource
public class ServiceProperties {

    private String zkRoot = "/config";

    private String adminUser = "admin";

    private String adminPassword = "admin";

    @ManagedAttribute
    public String getZkRoot() {
        return zkRoot;
    }

    public void setZkRoot(String zkRoot) {
        this.zkRoot = zkRoot;
    }

    public String getAdminUser() {
        return adminUser;
    }

    public void setAdminUser(String adminUser) {
        this.adminUser = adminUser;
    }

    public String getAdminPassword() {
        return adminPassword;
    }

    public void setAdminPassword(String adminPassword) {
        this.adminPassword = adminPassword;
    }
}
