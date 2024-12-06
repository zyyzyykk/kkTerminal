package com.kkbpro.terminal.interceptor;

import com.kkbpro.terminal.utils.I18nUtil;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class RequestInterceptor implements HandlerInterceptor {

    /**
     * 在请求到达Controller之前进行拦截
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 获取请求的参数 sshKey
        String sshKey = request.getParameter("sshKey");
        // 获取语言
        String lang = (sshKey != null && !sshKey.isEmpty()) ? sshKey.split("-")[0] : "en";
        I18nUtil.locale.set(lang);

        return true;
    }

    /**
     * 在Controller方法处理完之后执行
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        // 移除语言
        I18nUtil.locale.remove();
    }

}
