package com.kkbpro.terminal.Utils;

import java.util.Base64;

/**
 * BASE64加密解密
 */
public class BASE64Util {

    //JDK1.8及以上可直接使用Base64，JDK1.7及以下可以使用BASE64Encoder
    //Android平台可以使用android.util.Base64

    public static String Base64Encrypt(String message) {
        return new String(Base64.getEncoder().encode(message.getBytes()));
    }

    public static String Base64Decrypt(String message) {
        return new String(Base64.getDecoder().decode(message.getBytes()));
    }

}