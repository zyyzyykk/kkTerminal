package com.kkbpro.terminal.utils;

import javax.crypto.Cipher;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class RSAUtil {

    /**
     * 密钥长度 越长速度越慢
     */
    private final static int KEY_SIZE = 1024;

    /**
     * 公钥
     */
    public static final String PUBLIC_KEY;

    /**
     * 私钥
     */
    private static final String PRIVATE_KEY;

    static {
        try {
            String[] keyPair = genKeyPair();
            PUBLIC_KEY = keyPair[0];
            PRIVATE_KEY = keyPair[1];
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 随机生成密钥对
     */
    public static String[] genKeyPair() throws Exception {
        // KeyPairGenerator类用于生成公钥和私钥对，基于RSA算法生成对象
        KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");
        // 初始化密钥对生成器
        keyPairGen.initialize(KEY_SIZE, new SecureRandom());
        // 生成一个密钥对，保存在keyPair中
        KeyPair keyPair = keyPairGen.generateKeyPair();
        // 得到公钥
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        // 得到私钥
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
        // 得到公钥字符串
        String publicKeyString = encryptBASE64(publicKey.getEncoded());
        // 得到私钥字符串
        String privateKeyString = encryptBASE64(privateKey.getEncoded());

        return new String[] { publicKeyString, privateKeyString };
    }

    /**
     * RSA公钥加密
     *
     * @param str       明文
     * @param publicKey 公钥
     * @return 密文
     * @throws Exception 加密过程中的异常信息
     */
    public static String encrypt(String str, String publicKey) throws Exception {
        // base64编码的公钥
        byte[] decoded = decryptBASE64(publicKey);
        RSAPublicKey pubKey = (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(decoded));
        // RSA加密
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, pubKey);

        return encryptBASE64(cipher.doFinal(str.getBytes(StandardCharsets.UTF_8)));
    }

    public static String encrypt(String str) throws Exception {
        return encrypt(str, PUBLIC_KEY);
    }

    /**
     * RSA私钥解密
     *
     * @param str        密文
     * @param privateKey 私钥
     * @return 明文
     * @throws Exception 解密过程中的异常信息
     */
    public static String decrypt(String str, String privateKey) throws Exception {
        // 64位解码加密后的字符串
        byte[] inputByte = decryptBASE64(str);
        // base64编码的私钥
        byte[] decoded = decryptBASE64(privateKey);
        RSAPrivateKey priKey = (RSAPrivateKey) KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(decoded));
        // RSA解密
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, priKey);

        return new String(cipher.doFinal(inputByte));
    }

    public static String decrypt(String str) throws Exception {
        return decrypt(str, PRIVATE_KEY);
    }

    // 编码返回字符串
    public static String encryptBASE64(byte[] key) {
        return Base64.getEncoder().encodeToString(key);
    }

    // 解码返回byte
    public static byte[] decryptBASE64(String key) {
        return Base64.getDecoder().decode(key);
    }

}
