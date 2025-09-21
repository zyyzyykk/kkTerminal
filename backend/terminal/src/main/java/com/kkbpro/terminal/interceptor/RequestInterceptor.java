package com.kkbpro.terminal.interceptor;

import com.kkbpro.terminal.annotation.Log;
import com.kkbpro.terminal.utils.I18nUtil;
import com.kkbpro.terminal.utils.LogUtil;
import com.kkbpro.terminal.utils.SSHUtil;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;

@Component
public class RequestInterceptor implements HandlerInterceptor {

    /**
     * 在请求到达Controller之前进行拦截
     */
    @Log
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 获取请求的参数 sshKey
        String sshKey = request.getParameter("sshKey");
        // 获取语言
        String lang = "en";
        try {
            lang = sshKey.split("-")[0];
        } catch (Exception e) {
            LogUtil.logException(this.getClass(), e);
        } finally {
            I18nUtil.locale.set(lang);
        }
        // 获取服务器编码集
        String encode = StandardCharsets.UTF_8.name();
        try {
            encode = sshKey.split("-")[1].replace("@","-");
        } catch (Exception e) {
            LogUtil.logException(this.getClass(), e);
        } finally {
            SSHUtil.charset.set(encode);
        }

        return true;
    }

    /**
     * 在Controller方法处理完之后执行
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) {
        // 移除语言
        I18nUtil.locale.remove();
        // 移除服务器编码集
        SSHUtil.charset.remove();
    }

}
