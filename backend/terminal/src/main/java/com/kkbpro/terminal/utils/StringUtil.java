package com.kkbpro.terminal.utils;

import java.util.Random;


/**
 * 字符串相关工具类
 */
public class StringUtil {

    /**
     * 判断是否为空
     */
    public static boolean isEmpty(String str) {
        return str == null || str.isEmpty();
    }

    /**
     *  将字符串的'@'转为'/'
     */
    public static String changeStr(String str) {
        return isEmpty(str) ? str : str.replace("@", "/");
    }

    /**
     * 将Base64编码转换为合法的URL参数
     */
    public static String changeBase64ToStr(String str) {
        return isEmpty(str) ? str : str.replace("+", "-").replace("=", "@");
    }

    public static String changeStrToBase64(String str) {
        return isEmpty(str) ? str : str.replace("-", "+").replace("@", "=");
    }

    /**
     * 生成随机字符串
     */
    public static String generateRandomString(int length) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuilder sb = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            sb.append(characters.charAt(random.nextInt(characters.length())));
        }

        return sb.toString();
    }

}
