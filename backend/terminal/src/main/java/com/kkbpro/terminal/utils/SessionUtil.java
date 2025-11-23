package com.kkbpro.terminal.utils;

import com.kkbpro.terminal.constant.Constant;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.*;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SessionUtil implements HttpSessionListener {

    private static final ConcurrentHashMap<String, HttpSession> sessionMap = new ConcurrentHashMap<>();

    @Override
    public void sessionCreated(HttpSessionEvent event) {
        HttpSession session = event.getSession();
        sessionMap.put(session.getId(), session);
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent event) {
        HttpSession session = event.getSession();
        sessionMap.remove(session.getId());
    }

    private static HttpSession getSession() {
        HttpServletRequest request = getHttpServletRequest();
        HttpSession session = request.getSession(false);
        // 未携带Cookie
        if (session == null) {
            String key = request.getHeader("X-Cookie");
            if (key != null && !Constant.NULL_STRING.equalsIgnoreCase(key)) {
                session = sessionMap.get(key);
            }
        }
        // 未建立会话
        if (session == null) {
            session = request.getSession(true);
            // 设置Cookie
            HttpServletResponse response = getHttpServletResponse();
            String header = "X-Set-Cookie";
            String cookie = parseCookie(response);
            response.setHeader(header, cookie);
            response.setHeader("Access-Control-Expose-Headers", header);
        }

        return session;
    }

    private static HttpServletRequest getHttpServletRequest() {
        return ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
    }

    private static HttpServletResponse getHttpServletResponse() {
        return ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
    }

    private static String parseCookie(HttpServletResponse response) {
        try {
            Collection<String> setCookies = response.getHeaders("Set-Cookie");
            if (setCookies == null || setCookies.isEmpty()) {
                return null;
            }
            for (String setCookie : setCookies) {
                String[] attributes = setCookie.split(";");
                for (String attribute : attributes) {
                    attribute = attribute.trim();
                    String[] values = attribute.split("=", 2);
                    if (values.length == 2 && Constant.SESSION_NAME.equals(values[0])) {
                        return values[1].isEmpty() ? null : values[1];
                    }
                }
            }
        } catch (Exception e) {
            LogUtil.logException(SessionUtil.class, e);
        }

        return null;
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
