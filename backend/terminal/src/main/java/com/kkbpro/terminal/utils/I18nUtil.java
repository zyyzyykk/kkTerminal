package com.kkbpro.terminal.utils;

import com.alibaba.fastjson.JSON;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class I18nUtil {

    // 语言
    public static final ThreadLocal<String> locale = new ThreadLocal<>();

    // messages
    private static final Map<String, Map<String, String>> messagesCache = new HashMap<>();

    // load en_US messages
    static {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        // 使用 getResourceAsStream() 从类路径加载资源
        try(InputStream inputStream = classLoader.getResourceAsStream("locales/en_US.json")) {
            // 手动读取输入流内容
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, bytesRead);
            }

            // 将字节流转换为字符串
            String jsonContent = byteArrayOutputStream.toString(StandardCharsets.UTF_8.name());
            Map<String, String> messages = JSON.parseObject(jsonContent, Map.class);
            messagesCache.put("en", messages);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load en_US messages");
        }
    }

    /**
     * 获取国际化消息
     */
    public static String getMessage(String key) {
        return getMessage(key, locale.get());
    }

    public static String getMessage(String key, String lang) {
        return messagesCache.get(lang) == null ? key : messagesCache.get(lang).getOrDefault(key, key);
    }

}