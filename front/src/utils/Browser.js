import { syncUpload } from "@/utils/Cloud";
import useClipboard from "vue-clipboard3";
import { v4 as uuidv4 } from "uuid";

const { toClipboard } = useClipboard();
let clipboardText = "";

// Overwrite all used functions in the BOM window
const browser = {
    localStorage: {
        setItem(key, value, sync = true) {
            window.localStorage.setItem(key, value);
            // 多端同步-上传
            if(sync) syncUpload([key]);
        },
        getItem(key) {
            return window.localStorage.getItem(key);
        },
        removeItem(key) {
            window.localStorage.removeItem(key);
        },
    },
    sessionStorage: {
        setItem(key, value) {
            window.sessionStorage.setItem(key, value);
        },
        getItem(key) {
            return window.sessionStorage.getItem(key);
        },
        removeItem(key) {
            window.sessionStorage.removeItem(key);
        },
    },
    setTimeout(handler, timeout, ...args) {
        return window.setTimeout(handler, timeout, ...args);
    },
    setInterval(handler, timeout, ...args) {
        return window.setInterval(handler, timeout, ...args);
    },
    location: {
        reload() {
            window.location.reload();
        },
    },
    navigator: {
        clipboard: {
            async writeText(text) {
                clipboardText = text;
                return toClipboard(text);
            },
            async readText() {
                return window.isSecureContext ? window.navigator.clipboard.readText() : clipboardText;
            },
        },
    },
    open(url, features) {
        return window.open(url, features);
    },
    close() {
        window.close();
    },
    addEventListener(type, listener, options) {
        window.addEventListener(type, listener, options);
    },
    crypto: {
        randomUUID() {
            return window.isSecureContext ? window.crypto.randomUUID() : uuidv4();
        },
    },
    getComputedStyle(elt, pseudoElt) {
        return window.getComputedStyle(elt, pseudoElt);
    },
};

export default browser;
