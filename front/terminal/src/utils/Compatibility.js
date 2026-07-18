import browser from "@/utils/Browser";
import { localStore } from "@/env/Store";
import { aesDecrypt, aesEncrypt } from "@/utils/Encrypt";
import { eq, valid, lte, compare } from "semver";

// 最新版本号
const latestVersion = document.querySelector('meta[name="version"]')?.getAttribute('content');

const setupCompatFixes = () => {
    const currentVersion = browser.localStorage.getItem(localStore['version']);
    if(eq(latestVersion, currentVersion)) return;
    for(const fixItem of sortedFixesChain) {
        if(!valid(currentVersion) || lte(currentVersion, fixItem.version)) fixItem.run();
    }
    browser.localStorage.setItem(localStore['version'], latestVersion);
};

export default setupCompatFixes;

// 兼容3.7.6及以下版本
const fixFor376 = () => {
    if(browser.localStorage.getItem(localStore['env'])) {
        const env = JSON.parse(aesDecrypt(browser.localStorage.getItem(localStore['env'])));
        if(!('cmdcode' in env)) {
            if('tCode' in env) env.cmdcode = env.tCode;
            else env.cmdcode = true;
        }
        delete env.tCode;
        browser.localStorage.setItem(localStore['env'], aesEncrypt(JSON.stringify(env)));
    }
    const transItems = [
        { from: 'tcodes', to: 'cmdcodes' },
        { from: 'tcode-local-vars', to: 'cmdcode-vars' },
        { from: 'tcode-draft', to: 'cmdcode-draft' },
    ];
    for(const transItem of transItems) {
        if(browser.localStorage.getItem(transItem.from)) {
            browser.localStorage.setItem(localStore[transItem.to], browser.localStorage.getItem(transItem.from));
            browser.localStorage.removeItem(transItem.from);
        }
    }
};

const sortedFixesChain = [
    { version: "3.7.6", run: fixFor376 },
].sort((a, b) => compare(a.version, b.version));
