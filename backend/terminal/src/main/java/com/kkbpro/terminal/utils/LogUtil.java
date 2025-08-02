package com.kkbpro.terminal.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LogUtil {

    private static final Map<Class<?>, Logger> loggerCache = new ConcurrentHashMap<>();

    public static Logger getLoggerForClass(Class<?> clazz) {
        return loggerCache.computeIfAbsent(clazz, LoggerFactory::getLogger);
    }

    public static void logException(Class<?> clazz, Exception e) {
        Logger logger = getLoggerForClass(clazz);
        logger.error("Exception occurred \"{}\"", e.getMessage(), e);
    }

    public static void logException(Class<?> clazz, Throwable e) {
        Logger logger = getLoggerForClass(clazz);
        logger.error("Exception occurred \"{}\"", e.getMessage(), e);
    }

}
