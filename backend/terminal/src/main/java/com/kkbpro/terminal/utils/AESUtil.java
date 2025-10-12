package com.kkbpro.terminal.utils;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.SecretKeySpec;

public class AESUtil {

    /**
     * 加密密钥
     */
    public static final ThreadLocal<String> key = new ThreadLocal<>();

    /**
     * 加密算法
     */
    private static final String ALGORITHM = "AES/ECB/PKCS5Padding";

    /**
     * aes解密
     * @param encryptStr 内容
     */
    public static String decrypt(String encryptStr) throws Exception {
        return decrypt(encryptStr, key.get());
    }

    /**
     * aes加密
     * @param content 内容
     */
    public static String encrypt(String content) throws Exception {
        return encrypt(content, key.get());
    }

    /**
     * base64 encode
     * @param bytes 待编码的byte[]
     * @return 编码后的base64 code
     */
    public static String base64Encode(byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes);
    }

    /**
     * base64 decode
     * @param base64Code 待解码的base64 code
     * @return 解码后的byte[]
     */
    public static byte[] base64Decode(String base64Code) {
        return StringUtil.isEmpty(base64Code) ? null : Base64.getDecoder().decode(base64Code);
    }


    /**
     * AES加密
     * @param content 待加密的内容
     * @param encryptKey 加密密钥
     * @return 加密后的byte[]
     */
    public static byte[] encryptToBytes(String content, String encryptKey) throws Exception {
        KeyGenerator kgen = KeyGenerator.getInstance("AES");
        kgen.init(128);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(encryptKey.getBytes(), "AES"));

        return cipher.doFinal(content.getBytes(StandardCharsets.UTF_8));
    }


    /**
     * AES加密为base64 code
     * @param content 待加密的内容
     * @param encryptKey 加密密钥
     * @return 加密后的base64 code
     */
    public static String encrypt(String content, String encryptKey) throws Exception {
        return StringUtil.isEmpty(content) ? null : base64Encode(encryptToBytes(content, encryptKey));
    }

    /**
     * AES解密
     * @param encryptBytes 待解密的byte[]
     * @param decryptKey 解密密钥
     * @return 解密后的byte[]
     */
    public static byte[] decryptByBytes(byte[] encryptBytes, String decryptKey) throws Exception {
        KeyGenerator kgen = KeyGenerator.getInstance("AES");
        kgen.init(128);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(decryptKey.getBytes(), "AES"));

        return cipher.doFinal(encryptBytes);
    }

    public static byte[] decryptToBytes(String encryptStr, String decryptKey) throws Exception {
        return StringUtil.isEmpty(encryptStr) ? null : decryptByBytes(base64Decode(encryptStr), decryptKey);
    }

    /**
     * 将base64 code AES解密
     * @param encryptStr 待解密的base64 code
     * @param decryptKey 解密密钥
     * @return 解密后的string
     */
    public static String decrypt(String encryptStr, String decryptKey) throws Exception {
        return StringUtil.isEmpty(encryptStr) ? null : new String(decryptToBytes(encryptStr, decryptKey));
    }

}

