import CryptoJS from "crypto-js";
import JSEncrypt from "jsencrypt";

const secretKeys = {
    storage: null,
    socket: null,
    response: null,
    public: null,
};
export const secretKeySetter = {
    storage(key) {
        secretKeys.storage = key;
    },
    socket(key) {
        secretKeys.socket = key;
    },
    response(key) {
        secretKeys.response = key;
    },
    public(key) {
        secretKeys.public = key;
    }
};
export const secretKeyGetter = {
    storage() {
        return secretKeys.storage;
    },
    socket() {
        return secretKeys.socket;
    },
    response() {
        return secretKeys.response;
    },
    public() {
        return secretKeys.public;
    }
};

/**
 * AES Encrypt
 */
export const aesEncrypt = (word, secretKey) => {
    const secret_key = secretKey || secretKeyGetter.storage();
    if(!secret_key || !word) return null;
    const key = CryptoJS.enc.Utf8.parse(secret_key);
    const srcs = CryptoJS.enc.Utf8.parse(word);
    const encrypted = CryptoJS.AES.encrypt(srcs, key, {mode: CryptoJS.mode.ECB, padding: CryptoJS.pad.Pkcs7});
    return encrypted.toString();
};

/**
 * AES Buffer Encrypt
 */
export const aesEncryptBuffer = (arrayBuffer, secretKey) => {
    const secret_key = secretKey || secretKeyGetter.storage();
    if(!secret_key || !arrayBuffer) return null;
    const key = CryptoJS.enc.Utf8.parse(secret_key);
    const uint8Array = new Uint8Array(arrayBuffer);
    const srcs = CryptoJS.lib.WordArray.create(uint8Array);
    const encrypted = CryptoJS.AES.encrypt(srcs, key, {mode: CryptoJS.mode.ECB, padding: CryptoJS.pad.Pkcs7});
    return encrypted.toString();
};

/**
 * AES Decrypt
 */
export const aesDecrypt = (word, secretKey) => {
    const secret_key = secretKey || secretKeyGetter.storage();
    if(!secret_key || !word) return null;
    const key = CryptoJS.enc.Utf8.parse(secret_key);
    const decrypt = CryptoJS.AES.decrypt(word, key, {mode: CryptoJS.mode.ECB, padding: CryptoJS.pad.Pkcs7});
    return CryptoJS.enc.Utf8.stringify(decrypt).toString();
};

/**
 * RSA Encrypt
 */
export const rsaEncrypt = (word, publicKey) => {
    const secret_key = publicKey || secretKeyGetter.public();
    if(!secret_key || !word) return null;
    // 创建对象
    const jsRsa = new JSEncrypt();
    // 设置公钥
    jsRsa.setPublicKey(secret_key);
    // 返回加密后结果
    return jsRsa.encrypt(word);
};
