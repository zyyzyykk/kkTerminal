package com.kkbpro.terminal.interceptor;

import com.kkbpro.terminal.utils.I18nUtil;
import com.kkbpro.terminal.utils.LogUtil;
import com.kkbpro.terminal.utils.SSHUtil;
import com.kkbpro.terminal.utils.StringUtil;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;

@Component
public class BusinessInterceptor implements HandlerInterceptor {

    /**
     * 在请求到达Controller之前进行拦截
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 获取请求头 X-Accept-Language
        String acceptLang = request.getHeader("X-Accept-Language");
        // 获取请求参数 sshKey
        String sshKey = request.getParameter("sshKey");
        // 获取语言
        String lang = "en";
        try {
            if (!StringUtil.isEmpty(acceptLang)) lang = acceptLang;
            else if (!StringUtil.isEmpty(sshKey)) lang = sshKey.split("-")[0];
        } catch (Exception e) {
            LogUtil.logException(this.getClass(), e);
        } finally {
            I18nUtil.locale.set(lang);
        }
        // 获取服务器编码集
        String encode = StandardCharsets.UTF_8.name();
        try {
            if (!StringUtil.isEmpty(sshKey)) encode = sshKey.split("-")[1].replace("@","-");
        } catch (Exception e) {
            LogUtil.logException(this.getClass(), e);
        } finally {
            SSHUtil.charset.set(encode);
        }

        return true;
    }

    /**
     * 在整个请求完成后执行
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // 移除语言
        I18nUtil.locale.remove();
        // 移除服务器编码集
        SSHUtil.charset.remove();
    }

}
