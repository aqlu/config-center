package com.qianmi.common.config.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.encoding.ShaPasswordEncoder;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;

/**
 * 鉴权
 * Created by aqlu on 15/5/30.
 */
@Configuration
@Order(SecurityProperties.ACCESS_OVERRIDE_ORDER)
public class ApplicationSecurity extends WebSecurityConfigurerAdapter {

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    private UserDetailsService userDetailsService;

    public void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService).passwordEncoder(new ShaPasswordEncoder());
    }


    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable().authorizeRequests()
                .antMatchers("/favicon.ico").permitAll()
                .antMatchers("/app/**").permitAll()
                .antMatchers("/css/**").permitAll()
                .antMatchers("/extjs/**").permitAll()
                .antMatchers("/images/**").permitAll()
                .antMatchers("/js/**").permitAll()
                .anyRequest().fullyAuthenticated()
                .and().formLogin().loginPage("/login").failureUrl("/login?error").permitAll()
                .and().logout().permitAll()
                .and().exceptionHandling().accessDeniedPage("/access?error");
//
//        http.authorizeRequests().antMatchers("/login").permitAll().antMatchers("/index").permitAll().anyRequest().fullyAuthenticated().and().formLogin()
//                .loginPage("/login").failureUrl("/login?error").and().logout()
//                .logoutRequestMatcher(new AntPathRequestMatcher("/logout")).and().exceptionHandling()
//                .accessDeniedPage("/access?error");
    }
}
