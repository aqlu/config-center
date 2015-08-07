package com.qianmi.common.config;

import com.qianmi.common.config.constants.ServiceProperties;
import com.qianmi.common.config.security.ApplicationSecurity;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * Startup
 * Created by aqlu on 15/5/25.
 */
@EnableConfigurationProperties(ServiceProperties.class)
@EnableAutoConfiguration
@SpringBootApplication
@EnableGlobalMethodSecurity(securedEnabled = true)
public class Application extends WebMvcConfigurerAdapter {

    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/login").setViewName("login");
        registry.addViewController("/index").setViewName("index");
        registry.addViewController("/welcome").setViewName("welcome");
        registry.addViewController("/access").setViewName("access");
    }

    @Bean
    public ApplicationSecurity applicationSecurity() {
        return new ApplicationSecurity();
    }

    @Bean
    public CuratorFramework getCuratorFramework() {
        CuratorFramework curatorFramework = CuratorFrameworkFactory.newClient(
                ConfigSysSettings.CONFIG_ZK_CONNECTION_STR, ConfigSysSettings.CONFIG_ZK_CONNECTION_SESSION_TIMEOUT,
                ConfigSysSettings.CONFIG_ZK_CONNECTION_TIMEOUT, new ExponentialBackoffRetry(
                        ConfigSysSettings.CONFIG_ZK_RETRY_BASE_INTERVAL, ConfigSysSettings.CONFIG_ZK_RETRY_COUNT));
        curatorFramework.start();
        return curatorFramework;
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}