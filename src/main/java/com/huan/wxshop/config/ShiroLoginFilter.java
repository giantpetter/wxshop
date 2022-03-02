package com.huan.wxshop.config;

import org.apache.shiro.web.filter.authc.FormAuthenticationFilter;
import org.springframework.stereotype.Component;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

@Component
public class ShiroLoginFilter extends FormAuthenticationFilter {
    public ShiroLoginFilter() {
        System.out.println("\nShiroLoginFilter 初始化\n");
    }

    /**
     * 在访问 controller 前判断是否登录，返回401，不进行重定向
     *
     * @returning true-继续往下执行 false-过滤器已处理，不进行其他过滤。
     */
    @Override
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response, Object mappedValue) throws Exception {
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        httpServletResponse.setStatus(401);
        httpServletResponse.setContentType("application/json");
        httpServletResponse.setCharacterEncoding("UTF-8");
        return false;
    }
}
