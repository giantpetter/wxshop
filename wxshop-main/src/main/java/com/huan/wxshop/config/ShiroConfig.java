package com.huan.wxshop.config;

import com.huan.wxshop.service.ShiroRealm;
import com.huan.wxshop.service.UserLoginInterceptor;
import com.huan.wxshop.service.UserService;
import com.huan.wxshop.service.VerificationCheckCode;
import org.apache.shiro.cache.MemoryConstrainedCacheManager;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.util.ThreadContext;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Primary;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.servlet.Filter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Configuration
@Primary
public class ShiroConfig implements WebMvcConfigurer {
    private final UserService userService;

    @Autowired
    public ShiroConfig(UserService userService) {
        this.userService = userService;
    }

    @Bean
    @DependsOn("shiroLoginFilter")
    public ShiroFilterFactoryBean shiroFilter(SecurityManager securityManager, ShiroLoginFilter shiroLoginFilter) {
        ShiroFilterFactoryBean shiroFilterFactoryBean = new ShiroFilterFactoryBean();
        shiroFilterFactoryBean.setSecurityManager(securityManager);

        Map<String, String> pattern = new HashMap<>();
        pattern.put("/api/v1/code", "anon");
        pattern.put("/api/v1/login", "anon");
        pattern.put("/api/v1/status", "anon");
        pattern.put("/api/v1/test/**", "anon");
        pattern.put("/api/v1/logout", "anon");
        pattern.put("/**", "authc");

        Map<String, Filter> filterMap = new LinkedHashMap<>();
        filterMap.put("shiroLoginFilter", shiroLoginFilter);
        shiroFilterFactoryBean.setFilters(filterMap);
        shiroFilterFactoryBean.setFilterChainDefinitionMap(pattern);

        return shiroFilterFactoryBean;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new UserLoginInterceptor(userService));
    }


    @Bean
    public SecurityManager securityManager(ShiroRealm realm) {
        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
        securityManager.setRealm(realm);
        securityManager.setCacheManager(new MemoryConstrainedCacheManager());
        securityManager.setSessionManager(new DefaultWebSessionManager()); //默认 sessionManager 的设置
        ThreadContext.bind(securityManager); //np
        return securityManager;
    }

    @Bean("realm")
    public ShiroRealm shiroRealm(VerificationCheckCode verificationCheckCode) {

        return new ShiroRealm(verificationCheckCode);
    }


}
