package com.kkbpro.terminal.utils;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class SessionUtil {

    private static HttpSession getSession() {
        HttpServletRequest httpServletRequest = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        return httpServletRequest.getSession();
    }

    public static Object getAttribute(String key) {
        return getSession().getAttribute(key);
    }

    public static void setAttribute(String key, Object value) {
        getSession().setAttribute(key, value);
    }

    public static void removeAttribute(String key) {
        getSession().removeAttribute(key);
    }

}
