import { aesEncrypt, aesDecrypt } from '@/utils/Encrypt';
import { localStore, sessionStore } from "@/env/Store";
import { localStoreUtil } from "@/utils/CloudUtil";
const storageLocalKey = localStore['tcode-vars'];
const storageSessionPrefix = sessionStore['tcode-vars'];

import i18n from "@/locales/i18n";

// 功能TCode, 以 / 开头
export const FuncTCode = {
    '/A': {
        desc: i18n.global.k('自定义TCode'),
        execFlow(context) {
            setTimeout(() => {
                context.proxy.userTCodeRef.initText();
            },1);
            context.proxy.userTCodeRef.DialogVisible = true;
        }
    },
    '/O': {
        desc: i18n.global.k('新建窗口'),
        execFlow() {
            const _url = window.location.href;
            window.open(_url, '_blank');
        }
    },
    '/C': {
        desc: i18n.global.k('关闭窗口'),
        execFlow() {
            window.close();
        }
    },
    '/E': {
        desc: i18n.global.k('退出登录'),
        execFlow(context) {
            if(context.proxy.socket) context.proxy.socket.close(3131);
        }
    },
    '/R': {
        desc: i18n.global.k('刷新页面'),
        execFlow() {
            window.location.reload();
        }
    },
    '/H': {
        desc: i18n.global.k('帮助'),
        execFlow(context) {
            context.proxy.helpTCodeRef.DialogVisible = true;
        }
    },
};

// 系统TCode, 以 S 开头
export const SysTCode = {
    'SC': {
        desc: i18n.global.k('连接设置'),
        execFlow(context) {
            context.proxy.doSettings(1);
        }
    },
    'SP': {
        desc: i18n.global.k('偏好设置'),
        execFlow(context) {
            context.proxy.doSettings(2);
        }
    },
    'SF': {
        desc: i18n.global.k('文件管理'),
        execFlow(context) {
            context.proxy.doSettings(4);
        }
    },
    'SAC': {
        desc: i18n.global.k('高级-协作'),
        execFlow(context) {
            if(!(context.proxy.sshKey && context.proxy.env.advance)) return;
            context.proxy.doSettings(6);
        }
    },
    'SAM': {
        desc: i18n.global.k('高级-监控'),
        execFlow(context) {
            if(!(context.proxy.sshKey && context.proxy.env.advance && context.proxy.env.server_user === 'root')) return;
            context.proxy.doSettings(7);
        }
    },
    'SAD': {
        desc: i18n.global.k('高级-Docker'),
        execFlow(context) {
            if(!(context.proxy.sshKey && context.proxy.env.advance)) return;
            context.proxy.doSettings(8);
        }
    },
    'SS': {
        desc: i18n.global.k('重启'),
        execFlow(context) {
            context.proxy.doSettings(3);
        }
    },
};

// 用户TCode, 以 U 开头
export const UserTCodeExecutor = {
    active: false,
    display: true,
    outArray: [],
    cnt: 0,
    // 变量
    variables: {
        session(key,value) {
            if(value) sessionStorage.setItem(storageSessionPrefix + key,JSON.stringify(value));
            else {
                if(sessionStorage.getItem(storageSessionPrefix + key)) return JSON.parse(sessionStorage.getItem(storageSessionPrefix + key));
                else return null;
            }
        },
        local(key,value) {
            let tCodeLocalVars = {};
            if(localStoreUtil.getItem(storageLocalKey)) {
                tCodeLocalVars = JSON.parse(aesDecrypt(localStoreUtil.getItem(storageLocalKey)));
            }
            if(value) {
                tCodeLocalVars[key] = value;
                localStoreUtil.setItem(storageLocalKey,aesEncrypt(JSON.stringify(tCodeLocalVars)));
            }
            else return tCodeLocalVars[key];
        },
        clean() {
            if(arguments.length === 0) {
                localStoreUtil.removeItem(storageLocalKey);
                return;
            }
            let tCodeLocalVars = {};
            if(localStoreUtil.getItem(storageLocalKey)) {
                tCodeLocalVars = JSON.parse(aesDecrypt(localStoreUtil.getItem(storageLocalKey)));
            }
            for (let i = 0; i < arguments.length; i++) {
                delete tCodeLocalVars[arguments[i]];
            }
            localStoreUtil.setItem(storageLocalKey,aesEncrypt(JSON.stringify(tCodeLocalVars)));
        }
    },
    // 仅向terminal写入，不等待
    writeOnly: null,
    // 向terminal写入并等待
    async writeAndWait(content, time = 200) {
        if(!content) content = '\n';
        else if(content[content.length - 1] != '\n' && content[content.length - 1] != '\r') content += '\n';
        this.cnt = this.outArray.length;
        this.writeOnly(content, true);
        await new Promise(resolve => setTimeout(resolve, Math.max(0, time)));
    },
    // writeAndWait简写
    write(content, time = 200) {
        return this.writeAndWait(content, time);
    },
    // 读取输出
    read() {
        return filter(this.outArray.slice(this.cnt));
    },
    // 读取全部输出
    readAll() {
        return filter(this.outArray.slice(0));
    },
    // 隐藏
    hide() {
        this.display = false;
    },
    // 显示
    show() {
        this.display = true;
    },
};

// 处理过滤输出
const filter = (arr) => {
    const ret = [];
    const tmp = filterRN(arr);
    for(let i = 0; i < tmp.length; i++) {
        const str = filterANSI(tmp[i]);
        if(str) ret.push(str);
    }
    return ret;
};

// 以 \r\n 分割
const filterRN = (arr) => {
    const ret = [];
    for(let i = 0; i < arr.length; i++) {
        const tmp = arr[i].split("\r\n");
        for(let j = 0; j < tmp.length; j++) {
            if (tmp[j]) ret.push(tmp[j]);
        }
    }
    return ret;
};
// 过滤 ANSI 等终端字符
import stripAnsi from 'strip-ansi';
const filterANSI = (str) => {
    // eslint-disable-next-line
    return stripAnsi(str).replace(/[\x00-\x1F\x7F]/g, '');
};

// 用户TCode状态枚举
// Error-编译失败: Compile Error
// Interrupted-执行中断: Execute Interrupt
// Inactive-未被使用: Not Active
// Success-执行成功: Execute Success
export const TCodeStatusEnum = {
    'Compile Error': 'Error',
    'Execute Interrupt':'Interrupted',
    'Not Active': 'Inactive',
    'Execute Success': 'Success',
};


// 编辑器添加kkTerminal智能提示
export const userTCodeExecutorCompleter = {
  getCompletions: function(editor, session, pos, prefix, callback) {
    const userTCodeExecutorCompletions = [
      {
        name: "kkTerminal",
        value: "kkTerminal",
        meta: "kkTerminal",
        description: "kkTerminal API",
        score: 1000,
      },
      {
        name: "variables",
        value: "variables",
        meta: "kkTerminal",
        description: "access variables",
        score: 1000,
      },
      {
        name: "session",
        value: "session()",
        meta: "kkTerminal",
        description: "get or set session variables",
        score: 1000,
      },
      {
          name: "local",
          value: "local()",
          meta: "kkTerminal",
          description: "get or set local variables",
          score: 1000,
      },
      {
        name: "clean",
        value: "clean()",
        meta: "kkTerminal",
        description: "remove local variables",
        score: 1000,
      },
      {
        name: "write",
        value: "write()",
        meta: "kkTerminal",
        description: "write to terminal",
        score: 1000,
      },
      {
        name: "read",
        value: "read()",
        meta: "kkTerminal",
        description: "read lastest from terminal",
        score: 1000,
      },
      {
        name: "readAll",
        value: "readAll()",
        meta: "kkTerminal",
        description: "read all from terminal",
        score: 1000,
      },
      {
        name: "hide",
        value: "hide()",
        meta: "kkTerminal",
        description: "hide TCode display",
        score: 1000,
      },
      {
        name: "show",
        value: "show()",
        meta: "kkTerminal",
        description: "show TCode display",
        score: 1000,
      }
    ];

    callback(null, userTCodeExecutorCompletions.map((completion) => {
      return {
        caption: completion.caption || completion.name,
        value: completion.value,
        meta: completion.meta,
        description: completion.description || ''
      };
    }));
  }
};

// 历史TCode
export const historyTCode = {
    tCodes: [],
    index: 0,
    add(latestTCode) {
        this.tCodes.push(latestTCode);
        this.index = this.tCodes.length;
    },
    up(currentTCode) {
        if(this.tCodes.length === 0) return currentTCode;
        this.index--;
        if(this.index < 0) this.index = 0;
        return this.tCodes[this.index];
    },
    down(currentTCode) {
        if(this.tCodes.length === 0) return currentTCode;
        this.index++;
        if(this.index >= this.tCodes.length) {
            this.index = this.tCodes.length;
            return '';
        }
        return this.tCodes[this.index];
    },
};