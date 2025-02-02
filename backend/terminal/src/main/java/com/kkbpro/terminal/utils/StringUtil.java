package com.kkbpro.terminal.utils;


/**
 * 字符串相关工具类
 */
public class StringUtil {

    /**
     * 判断是否为空
     */
    public static boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }

    /**
     * 判断参数1是否为参数2的前缀
     */
    public static boolean isPrefix(String a, String b) {
        if(a == null) return true;
        if(b == null) return false;

        int len_a = a.length();
        int len_b = b.length();

        if(len_a > len_b) return false;

        for(int i=len_a-1;i>=0;i--) if(a.charAt(i) != b.charAt(i)) return false;

        return true;
    }

    /**
     *  将字符串的'@'转为'/'
     */
    public static String changeStr(String str) {
        StringBuilder result = new StringBuilder();
        for (int i=0;i<str.length();i++) {
            if(str.charAt(i) != '@') result.append(str.charAt(i));
            else result.append('/');
        }

        return result.toString();
    }

    // 将Base64编码转换为合法的URL参数
    public static String changeBase64Str(String str) {
        return str.replace("+", "-").replace("=", "@");
    }

    public static String changeStrBase64(String str) {
        return str.replace("-", "+").replace("@", "=");
    }

}
