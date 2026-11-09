package com.qiyun.qhfg.config;

import com.qiyun.qhfg.filter.ManageApiFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.servlet.Filter;

/**
 * @PACKAGE_NAME: com.example.fxpgJava.config
 * @NAME: WebAuthFilterConfig 用户管理模块过滤器拦截配置
 * @USER: spike
 * @DATE: 2023/4/12 10:17
 * @PROJECT_NAME: FXPG_Java
 */
@Configuration
public class WebAuthFilterConfig implements WebMvcConfigurer {

    @Bean
    public FilterRegistrationBean webAuthFilterRegistration() {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(manageApiFilter());
        registration.setName("ManageApiFilter");
        //用户管理路径拦截
        registration.addUrlPatterns("/*");
        registration.addInitParameter("exclusions","/,/thumbnailImage*,/webjars*,/definitions*,/v2*,/swagger*,/sysUser/Login,/sysUser/Register");
        registration.setOrder(1);
        return registration;
    }

    @Bean
    public Filter manageApiFilter() {
        return new ManageApiFilter();
    }
}
