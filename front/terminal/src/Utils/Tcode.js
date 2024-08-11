// 功能TCode, 以 / 开头
export const FuncTcode = {
    '/A': {
        desc: '自定义TCode',
        execFlow(context) {
            setTimeout(() => {
                context.proxy.userTcodeRef.initText();
            },1);
            context.proxy.userTcodeRef.DialogVisilble = true;
        }
    },
    '/O': {
        desc: '新建窗口',
        execFlow() {
            let _url = window.location.href;
            window.open(_url, '_blank');
        }
    },
    '/C': {
        desc: '关闭窗口',
        execFlow() {
            window.close();
        }
    },
    '/E': {
        desc: '退出登录',
        execFlow(context) {
            if(context.proxy.socket) context.proxy.socket.close(3131);
        }
    },
    '/R': {
        desc: '刷新页面',
        execFlow() {
            window.location.reload();
        }
    },
    '/H': {
        desc: '帮助',
        execFlow(context) {
            context.proxy.helpTcodeRef.DialogVisilble = true;
        }
    },
}

// 系统TCode, 以 S 开头
export const SysTcode = {
    'SC': {
        desc: '连接设置',
        execFlow(context) {
            context.proxy.isShowSetting = false;
            context.proxy.connectSettingRef.DialogVisilble = true;
        }
    },
    'SP': {
        desc: '偏好设置',
        execFlow(context) {
            context.proxy.isShowSetting = false;
            context.proxy.styleSettingRef.DialogVisilble = true;
        }
    },
    'SF': {
        desc: '文件管理',
        execFlow(context) {
            context.proxy.isShowSetting = false;
            context.proxy.fileBlockRef.DialogVisilble = true;
            context.proxy.fileBlockRef.getInitDir();
        }
    },
    'SS': {
        desc: '重启',
        execFlow(context) {
            context.proxy.isShowSetting = false;
            context.proxy.now_connect_status = context.proxy.connect_status['Connecting'];
            context.proxy.sshKey = '';
            if(context.proxy.socket) context.proxy.socket.close(3333);  // 主动释放资源，必需
            // 进行重启
            context.proxy.closeFileBlock();
            context.proxy.doSSHConnect();
            context.proxy.resetTerminal();
        }
    }
}

// 用户TCode, 以 U 开头
export const UserTcodeExecutor = {
    active: false,
    display: true,
    outArray: [],
    cnt: 0,
    // 变量
    variables: {
        session(key,value) {
            if(value != undefined) sessionStorage.setItem(key,JSON.stringify(value));
            else {
                if(sessionStorage.getItem(key)) return JSON.parse(sessionStorage.getItem(key));
                else return null;
            }
        },
        global(key,value) {
            const storageKey = 'tcode-global-vars';
            let tcodeGlobalVars = {};
            if(localStorage.getItem(storageKey)) {
                tcodeGlobalVars = JSON.parse(localStorage.getItem(storageKey));
            }
            if(value != undefined) {
                tcodeGlobalVars[key] = value;
                localStorage.setItem(storageKey,JSON.stringify(tcodeGlobalVars));
            }
            else return tcodeGlobalVars[key];
        },
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
    read() {
        return filter(this.outArray.slice(this.cnt));
    },
    readAll() {
        return filter(this.outArray.slice(0));
    },
    getAllOut() {
        return filter(this.outArray.slice(0));
    },
    getOut() {
        return filter(this.outArray.slice(this.cnt));
    },
    // 隐藏
    hide() {
        this.display = false;
    },
    // 显示
    show() {
        this.display = true;
    },
}

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
// Error-加载失败: Load Error
// Interrupted-执行失败: Execute Error
// Inactive-未被使用: Not Active
// Success-执行成功: Execute Success
export const TCodeStatusEnum = {
    'Load Error': 'Error',
    'Execute Error':'Interrupted',
    'Not Active': 'Inactive',
    'Execute Success': 'Success',
}