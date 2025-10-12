package com.kkbpro.terminal.interceptor;

import com.kkbpro.terminal.annotation.Log;
import com.kkbpro.terminal.constant.Constant;
import com.kkbpro.terminal.utils.*;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class AccessInterceptor implements HandlerInterceptor {

    /**
     * 在请求到达Controller之前进行拦截
     */
    @Log
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
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
     * 在Controller方法处理完之后执行
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) {
        // 移除存储密钥
        AESUtil.key.remove();
    }

}
