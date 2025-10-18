import { aesEncrypt, aesDecrypt } from '@/utils/Encrypt';
import { localStore, sessionStore } from "@/env/Store";
import { localStoreUtil } from "@/utils/CloudUtil";
import { calcPriority } from '@/components/calc/CalcPriority';
import { toRaw } from 'vue';
import i18n from "@/locales/i18n";

const storageLocalKey = localStore['tcode-vars'];
const storageSessionPrefix = sessionStore['tcode-vars'];

// 功能终端代码, 以 F 开头
export const FuncTCode = {
    'FS': {
        desc: i18n.global.k('重启终端'),
        execFlow(context) {
            context.proxy.doSettings(3);
        }
    },
    'FL': {
        desc: i18n.global.k('刷新页面'),
        execFlow() {
            window.location.reload();
        }
    },
    'FE': {
        desc: i18n.global.k('用户退出登录'),
        execFlow(context) {
            if(context.proxy.socket) context.proxy.socket.close(3131);
        }
    },
    'FO': {
        desc: i18n.global.k('新建终端窗口'),
        execFlow() {
            const _url = window.location.href;
            window.open(_url, '_blank');
        }
    },
    'FC': {
        desc: i18n.global.k('关闭终端窗口'),
        execFlow() {
            window.close();
        }
    },
};

// 系统终端代码, 以 S 开头
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
    'STC': {
        desc: i18n.global.k('终端代码中心'),
        execFlow(context) {
            context.proxy.tCodeCenterRef.DialogVisible = true;
        }
    },
    'STW': {
        desc: i18n.global.k('终端代码工作流'),
        execFlow(context) {
            setTimeout(() => {
                context.proxy.tCodeWorkflowRef.initText();
            }, 1);
            context.proxy.tCodeWorkflowRef.DialogVisible = true;
        }
    },
};

// 用户终端代码, 以 U 开头
export const UserTCodeExecutor = {
    // 文件
    file: {
        async cd(dir) {
            await UserTCodeHelper.fileBlockRef.fileBlockView(dir);
        },
        async ls(dir) {
            await this.cd(dir);
            return toRaw(UserTCodeHelper.fileBlockRef.files).map((file) => {
                return {
                    permission: calcPriority(file.attributes.mode.type, file.attributes.permissions),
                    uID: file.attributes.uID,
                    gID: file.attributes.gID,
                    size: file.attributes.size,
                    mtime: file.attributes.mtime,
                    name: file.name,
                }
            });
        },
        pwd() {
            return toRaw(UserTCodeHelper.fileBlockRef.dir);
        },
        async open(path, config={}) {
            const { dir, name } = UserTCodeHelper.parsePath(path);
            const fileInfo = await UserTCodeHelper.fileBlockRef.fileBlockView(dir, name);
            if(fileInfo && !fileInfo.isDirectory) await UserTCodeHelper.fileBlockRef.preViewFile(name, config);
            else throw new Error(i18n.global.t('无法打开文件：') + name);
        },
        edit(editFlow) {
            if(editFlow && editFlow instanceof Function) editFlow(UserTCodeHelper.fileBlockRef.filePreviewRef.codeEditorRef.aceEditor);
        },
        save(encode) {
            const filePreviewInstance = UserTCodeHelper.fileBlockRef.filePreviewRef;
            if(encode) filePreviewInstance.saveEncode = encode;
            filePreviewInstance.handleSave(filePreviewInstance.codeEditorRef.getValue());
        },
        close(block=false) {
            if(block) UserTCodeHelper.fileBlockRef.closeDialog();
            else UserTCodeHelper.fileBlockRef.filePreviewRef.closeDialog();
        },
        async download(path) {
            const { dir, name } = UserTCodeHelper.parsePath(path);
            const fileInfo = await UserTCodeHelper.fileBlockRef.fileBlockView(dir, name);
            if(fileInfo) {
                if(fileInfo.isDirectory) UserTCodeHelper.fileBlockRef.downloadDir(name);
                else UserTCodeHelper.fileBlockRef.downloadRemoteFile(name);
            }
            else throw new Error(i18n.global.t('无法下载文件：') + name);
        },
    },
    // 变量
    var: {
        session(key, value) {
            if(value) sessionStorage.setItem(storageSessionPrefix + key, JSON.stringify(value));
            else {
                if(sessionStorage.getItem(storageSessionPrefix + key)) return JSON.parse(sessionStorage.getItem(storageSessionPrefix + key));
                else return null;
            }
        },
        local(key, value) {
            let tCodeLocalVars = {};
            if(localStoreUtil.getItem(storageLocalKey)) {
                tCodeLocalVars = JSON.parse(aesDecrypt(localStoreUtil.getItem(storageLocalKey)));
            }
            if(value) {
                tCodeLocalVars[key] = value;
                localStoreUtil.setItem(storageLocalKey, aesEncrypt(JSON.stringify(tCodeLocalVars)));
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
    // 写入后等待
    async write(content, time = 200) {
        content = content.toString();
        if(!content) content = '\n';
        else if(!content.endsWith('\n') && !content.endsWith('\r')) content += '\n';
        UserTCodeHelper.cnt = UserTCodeHelper.outArray.length;
        UserTCodeHelper.writeNoAwait(content, true);
        await new Promise(resolve => setTimeout(resolve, Math.max(200, time)));
        return this.read();
    },
    // 读取输出
    read() {
        return filter(UserTCodeHelper.outArray.slice(UserTCodeHelper.cnt));
    },
    // 读取全部输出
    readAll() {
        return filter(UserTCodeHelper.outArray.slice(0));
    },
    // 隐藏
    hide() {
        UserTCodeHelper.display = false;
    },
    // 显示
    show() {
        UserTCodeHelper.display = true;
    },
};
export const UserTCodeHelper = {
    name: '',
    active: false,
    display: true,
    outArray: [],
    cnt: 0,
    fileBlockRef: null,
    writeNoAwait: null,
    parsePath(path) {
        if(path.endsWith('/')) path = path.slice(0, -1);
        const index = path.lastIndexOf('/');
        const dir = path.substring(0, index) || this.fileBlockRef.dir;
        const name = path.substring(index + 1);
        return { dir, name };
    },
    reset() {
        this.name = '';
        this.active = false;
        this.display = true;
        this.outArray = [];
        this.cnt = 0;
    },
};

const TCodeReservedVarsDict = {
    'option': 'CONNECT_OPTION',
    'home': 'HOME_PATH',
    'dir': 'CURRENT_DIR',
};
export const TCodeReservedVarsSetter = (key, val) => {
    UserTCodeExecutor.var.session(TCodeReservedVarsDict[key], val);
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

// 用户终端代码状态枚举
// Error-编译失败: Compile Error
// Interrupted-执行中断: Execute Interrupt
// Inactive-未被使用: Not Active
// Success-执行成功: Execute Success
export const TCodeStatusEnum = {
    'Compile Error': 'Error',
    'Execute Interrupt': 'Interrupted',
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
            name: "file",
            value: "file",
            meta: "kkTerminal",
            description: "operate file module",
            score: 1000,
        },
        {
            name: "cd",
            value: "cd()",
            meta: "kkTerminal",
            description: "change directory",
            score: 1000,
        },
        {
            name: "ls",
            value: "ls()",
            meta: "kkTerminal",
            description: "list files",
            score: 1000,
        },
        {
            name: "pwd",
            value: "pwd()",
            meta: "kkTerminal",
            description: "print working directory",
            score: 1000,
        },
        {
            name: "open",
            value: "open()",
            meta: "kkTerminal",
            description: "open file editor",
            score: 1000,
        },
        {
            name: "edit",
            value: "edit((editor) => {\n\n})",
            meta: "kkTerminal",
            description: "edit file content",
            score: 1000,
        },
        {
            name: "save",
            value: "save()",
            meta: "kkTerminal",
            description: "save file changes",
            score: 1000,
        },
        {
            name: "close",
            value: "close()",
            meta: "kkTerminal",
            description: "close editor or file module",
            score: 1000,
        },
        {
            name: "download",
            value: "download()",
            meta: "kkTerminal",
            description: "download file or folder",
            score: 1000,
        },
        {
            name: "var",
            value: "var",
            meta: "kkTerminal",
            description: "operate variables",
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
            description: "read latest from terminal",
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
            description: "hide Terminal Code display",
            score: 1000,
        },
        {
            name: "show",
            value: "show()",
            meta: "kkTerminal",
            description: "show Terminal Code display",
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

// 历史终端代码
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
