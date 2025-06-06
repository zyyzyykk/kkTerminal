import CryptoJS from "crypto-js";
import JSEncrypt from "jsencrypt";
import { sessionStore } from "@/env/Store";

/**
 * AES Encrypt
 */
export const aesEncrypt = (word, secretKey) => {
    const secret_key = secretKey || sessionStorage.getItem(sessionStore['aes-key']);
    if(!secret_key) return null;
    const key = CryptoJS.enc.Utf8.parse(secret_key);
    const srcs = CryptoJS.enc.Utf8.parse(word);
    const encrypted = CryptoJS.AES.encrypt(srcs, key, {mode:CryptoJS.mode.ECB,padding: CryptoJS.pad.Pkcs7});
    return encrypted.toString();
};

/**
 * AES Decrypt
 */
export const aesDecrypt = (word, secretKey) => {
    const secret_key = secretKey || sessionStorage.getItem(sessionStore['aes-key']);
    if(!secret_key) return null;
    const key = CryptoJS.enc.Utf8.parse(secret_key);
    const decrypt = CryptoJS.AES.decrypt(word, key, {mode:CryptoJS.mode.ECB,padding: CryptoJS.pad.Pkcs7});
    return CryptoJS.enc.Utf8.stringify(decrypt).toString();
};

/**
 * RSA Encrypt
 */
export const rsaEncrypt = (word, publicKey) => {
    const secret_key = publicKey || sessionStorage.getItem(sessionStore['public-key']);
    if(!secret_key) return null;
    // 创建对象
    const jsRsa = new JSEncrypt();
    // 设置公钥
    jsRsa.setPublicKey(secret_key);
    // 返回加密后结果
    return jsRsa.encrypt(word);
};
