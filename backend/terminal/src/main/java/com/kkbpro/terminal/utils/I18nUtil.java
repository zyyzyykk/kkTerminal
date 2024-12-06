package com.kkbpro.terminal.utils;

import com.alibaba.fastjson.JSON;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@Component
public class I18nUtil {

    // 语言
    public static final ThreadLocal<String> locale = new ThreadLocal<>();

    // messages
    private static final Map<String, Map<String, String>> messagesCache = new HashMap<>();

    // load en_US messages
    static {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Path filePath = null;
        try {
            filePath = Paths.get(classLoader.getResource("locales/en_US.json").toURI());
            String jsonContent = new String(Files.readAllBytes(filePath));
            Map<String, String> messages = JSON.parseObject(jsonContent, Map.class);
            messagesCache.put("en", messages);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load en_US messages");
        }
    }

    /**
     * 获取国际化消息
     */
    public String getMessage(String key) {
        return getMessage(key, locale.get());
    }

    public String getMessage(String key, String lang) {
        return messagesCache.get(lang) == null ? key : messagesCache.get(lang).getOrDefault(key, key);
    }

}