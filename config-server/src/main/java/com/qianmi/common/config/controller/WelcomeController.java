package com.qianmi.common.config.controller;

import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Date;

/**
 *
 * Created by aqlu on 15/5/28.
 */
@Controller
public class WelcomeController {

    @RequestMapping({"/"})
    @Secured({"ROLE_ADMIN", "ROLE_USER"})
    public String welcome(Map<String, Object> model, HttpServletRequest request) {
        SecurityContextImpl securityContextImpl = (SecurityContextImpl) request.getSession().getAttribute("SPRING_SECURITY_CONTEXT");
        String userName = securityContextImpl.getAuthentication().getName();

        model.put("date", new Date());
        model.put("userName", userName);

        return "index";
    }
}
