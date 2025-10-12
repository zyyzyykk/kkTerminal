package com.kkbpro.terminal.config;

import com.kkbpro.terminal.interceptor.AccessInterceptor;
import com.kkbpro.terminal.interceptor.BusinessInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private AccessInterceptor accessInterceptor;

    @Autowired
    private BusinessInterceptor businessInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册请求前拦截器
        registry.addInterceptor(accessInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/init")
                .excludePathPatterns("/api/access/**");
        registry.addInterceptor(businessInterceptor).addPathPatterns("/**");
    }

}