import { encrypt, decrypt } from '@/utils/Encrypt';
const storageLocalKey = 'tcode-local-vars';
const storageSessionPrefix = 'tcode-session-vars-';

import i18n from "@/locales/i18n";

// 功能TCode, 以 / 开头
export const FuncTcode = {
    '/A': {
        desc: i18n.global.t('自定义TCode'),
        execFlow(context) {
            setTimeout(() => {
                context.proxy.userTcodeRef.initText();
            },1);
            context.proxy.userTcodeRef.DialogVisilble = true;
        }
    },
    '/O': {
        desc: i18n.global.t('新建窗口'),
        execFlow() {
            let _url = window.location.href;
            window.open(_url, '_blank');
        }
    },
    '/C': {
        desc: i18n.global.t('关闭窗口'),
        execFlow() {
            window.close();
        }
    },
    '/E': {
        desc: i18n.global.t('退出登录'),
        execFlow(context) {
            if(context.proxy.socket) context.proxy.socket.close(3131);
        }
    },
    '/R': {
        desc: i18n.global.t('刷新页面'),
        execFlow() {
            window.location.reload();
        }
    },
    '/H': {
        desc: i18n.global.t('帮助'),
        execFlow(context) {
            context.proxy.helpTcodeRef.DialogVisilble = true;
        }
    },
};

// 系统TCode, 以 S 开头
export const SysTcode = {
    'SC': {
        desc: i18n.global.t('连接设置'),
        execFlow(context) {
            context.proxy.isShowSetting = false;
            context.proxy.connectSettingRef.DialogVisilble = true;
        }
    },
    'SP': {
        desc: i18n.global.t('偏好设置'),
        execFlow(context) {
            context.proxy.isShowSetting = false;
            context.proxy.styleSettingRef.DialogVisilble = true;
        }
    },
    'SF': {
        desc: i18n.global.t('文件管理'),
        execFlow(context) {
            context.proxy.isShowSetting = false;
            context.proxy.fileBlockRef.DialogVisilble = true;
            context.proxy.fileBlockRef.getInitDir();
        }
    },
    'SS': {
        desc: i18n.global.t('重启'),
        execFlow(context) {
            context.proxy.isShowSetting = false;
            context.proxy.now_connect_status = context.proxy.connect_status['Connecting'];
            context.proxy.sshKey = '';
            if(context.proxy.socket) context.proxy.socket.close(3333);  // 主动释放资源，必需
            // 进行重启
            context.proxy.closeFileBlock();
            context.proxy.resetTerminal();
            context.proxy.doSSHConnect();
        }
    }
};

// 用户TCode, 以 U 开头
export const UserTcodeExecutor = {
    active: false,
    display: true,
    outArray: [],
    cnt: 0,
    // 变量
    variables: {
        session(key,value) {
            if(value != undefined) sessionStorage.setItem(storageSessionPrefix + key,JSON.stringify(value));
            else {
                if(sessionStorage.getItem(storageSessionPrefix + key)) return JSON.parse(sessionStorage.getItem(storageSessionPrefix + key));
                else return null;
            }
        },
        local(key,value) {
            let tcodeLocalVars = {};
            if(localStorage.getItem(storageLocalKey)) {
                tcodeLocalVars = JSON.parse(decrypt(localStorage.getItem(storageLocalKey)));
            }
            if(value != undefined) {
                tcodeLocalVars[key] = value;
                localStorage.setItem(storageLocalKey,encrypt(JSON.stringify(tcodeLocalVars)));
            }
            else return tcodeLocalVars[key];
        },
        clean() {
            if(arguments.length == 0) {
                localStorage.removeItem(storageLocalKey);
                return;
            }
            let tcodeLocalVars = {};
            if(localStorage.getItem(storageLocalKey)) {
                tcodeLocalVars = JSON.parse(decrypt(localStorage.getItem(storageLocalKey)));
            }
            for (let i = 0; i < arguments.length; i++) {
                delete tcodeLocalVars[arguments[i]];
            }
            localStorage.setItem(storageLocalKey,encrypt(JSON.stringify(tcodeLocalVars)));
        }
    },
    // 仅向terminal写入，不等待
    writeOnly: null,
    // 向terminal写入并等待
    async writeAndWait(content, time = 200) {
        if(content && content.length > 0 && content[content.length - 1] != '\n' && content[content.length - 1] != '\r') content += '\n';
        this.cnt = this.outArray.length;
        this.writeOnly(content,true);
        await new Promise(resolve => setTimeout(resolve, time > 0 ? time : 0));
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
    let ret = [];
    let tmp = filterRN(arr);
    for(let i = 0; i < tmp.length; i++) {
        let str = filterANSI(tmp[i]);
        if(str && str != '') ret.push(str);
    }
    return ret;
};

// 以 \r\n 分割
const filterRN = (arr) => {
    let ret = [];
    for(let i = 0; i < arr.length; i++) {
        let tmp = arr[i].split("\r\n");
        for(let j = 0; j < tmp.length; j++) {
            if (tmp[j] && tmp[j] != '') ret.push(tmp[j]);
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
export const userTcodeExecutorCompleter = {
  getCompletions: function(editor, session, pos, prefix, callback) {
    const userTcodeExecutorCompletions = [
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

    callback(null, userTcodeExecutorCompletions.map((completion) => {
      return {
        caption: completion.caption || completion.name,
        value: completion.value,
        meta: completion.meta,
        description: completion.description || ''
      };
    }));
  }
};
