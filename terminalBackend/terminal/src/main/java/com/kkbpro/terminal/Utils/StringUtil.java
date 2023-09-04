package com.kkbpro.terminal.Utils;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.util.DigestUtils;

/**
 * 字符串相关工具类
 */
public class StringUtil {

    /**
     * 生成随机字符与数字
     */
    public static final String getRandomStr(Integer count)
    {
        return RandomStringUtils.random(count,true,true);
    }

    /**
     * 生成随机数
     */
    public static final String getRandomNumber(Integer count)
    {
        return RandomStringUtils.random(count,false,true);
    }

    /**
     * 判断是否为空
     */
    public static boolean isEmpty(String str) {

        if (null == str || "".equals(str) || "null".equals(str) || "\u0000".equals(str)) {
            return true;
        } else if ("".equals(str.trim())) {
            return true;
        }
        return false;
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
     * 获取最后一个'/'后面的字串
     */
    public static String getEndStr(String str) {
        int lastIndex = str.lastIndexOf("/");
        if (lastIndex != -1) {
            return str.substring(lastIndex + 1);
        } else {
            return str;
        }
    }


}
