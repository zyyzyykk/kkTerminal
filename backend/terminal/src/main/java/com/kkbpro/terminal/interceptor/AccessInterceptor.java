package com.kkbpro.terminal.interceptor;

import com.kkbpro.terminal.constant.Constant;
import com.kkbpro.terminal.utils.*;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class AccessInterceptor implements HandlerInterceptor {

    /**
     * 在请求到达Controller之前进行拦截
     */
    @Override
    public boolean preHandle(@Nullable HttpServletRequest request, @NonNull HttpServletResponse response, @Nullable Object handler) {
        // 访问权限校验
        String responseKey = (String) SessionUtil.getAttribute(Constant.LOGIN_SESSION);
        if (StringUtil.isEmpty(responseKey)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }
        AESUtil.key.set(responseKey);

        return true;
    }

    /**
     * 在整个请求完成后执行
     */
    @Override
    public void afterCompletion(@Nullable HttpServletRequest request, @Nullable HttpServletResponse response, @Nullable Object handler, Exception ex) {
        // 移除存储密钥
        AESUtil.key.remove();
    }

}
