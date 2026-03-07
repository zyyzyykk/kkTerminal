import browser from "@/utils/Browser";
import { localStore } from "@/env/Store";
import { aesDecrypt, aesEncrypt } from "@/utils/Encrypt";

// 版本号
const current = document.querySelector('meta[name="version"]')?.getAttribute('content');
const previous = browser.localStorage.getItem(localStore['version']);

// 3.8.0及以上版本新增
const setupCompatFixes = () => {
    if(current === previous) return;
    for(const fix of fixesChain) {
        if(!previous || previous <= fix.version) fix.action();
    }
    browser.localStorage.setItem(localStore['version'], current);
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

const fixesChain = [
    { version: "3.7.6", action: fixFor376 },
];
