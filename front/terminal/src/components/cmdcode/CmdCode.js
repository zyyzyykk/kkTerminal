import browser from "@/utils/Browser";
import { aesEncrypt, aesDecrypt } from "@/utils/Encrypt";
import { localStore, sessionStore } from "@/env/Store";
import { calcPriority } from "@/components/calc/CalcPriority";
import { toRaw } from "vue";
import i18n from "@/locales/i18n";

const storageLocalKey = localStore['cmdcode-vars'];
const storageSessionPrefix = sessionStore['cmdcode-vars'];

// 功能命令代码, 以 F 开头
export const FuncCmdCode = {
    'FS': {
        desc: i18n.global.k('重启终端'),
        execFlow(context) {
            context.proxy.doSettings(3);
        },
    },
    'FL': {
        desc: i18n.global.k('刷新页面'),
        execFlow() {
            browser.location.reload();
        },
    },
    'FE': {
        desc: i18n.global.k('用户退出登录'),
        execFlow(context) {
            if(context.proxy.socket) context.proxy.socket.close(3131);
        },
    },
    'FO': {
        desc: i18n.global.k('新建终端窗口'),
        execFlow() {
            const _url = window.location.href;
            browser.open(_url, '_blank');
        },
    },
    'FC': {
        desc: i18n.global.k('关闭终端窗口'),
        execFlow() {
            browser.close();
        },
    },
};

// 系统命令代码, 以 S 开头
export const SysCmdCode = {
    'SC': {
        desc: i18n.global.k('连接设置'),
        execFlow(context) {
            context.proxy.doSettings(1);
        },
    },
    'SP': {
        desc: i18n.global.k('偏好设置'),
        execFlow(context) {
            context.proxy.doSettings(2);
        },
    },
    'SF': {
        desc: i18n.global.k('文件管理'),
        execFlow(context) {
            context.proxy.doSettings(4);
        },
    },
    'SAC': {
        desc: i18n.global.k('高级-协作'),
        execFlow(context) {
            context.proxy.doSettings(6);
        },
    },
    'SAM': {
        desc: i18n.global.k('高级-监控'),
        execFlow(context) {
            context.proxy.doSettings(7);
        },
    },
    'SAD': {
        desc: i18n.global.k('高级-Docker'),
        execFlow(context) {
            context.proxy.doSettings(8);
        },
    },
    'SCCC': {
        desc: i18n.global.k('命令代码中心'),
        execFlow(context) {
            context.proxy.cmdCodeCenterRef.DialogVisible = true;
        },
    },
    'SCCW': {
        desc: i18n.global.k('命令代码工作流'),
        execFlow(context) {
            browser.setTimeout(() => {
                context.proxy.cmdCodeWorkflowRef.initText();
            }, 1);
            context.proxy.cmdCodeWorkflowRef.DialogVisible = true;
        },
    },
};

// 用户命令代码, 以 U 开头
export const UserCmdCodeExecutor = {
    // 文件
    file: {
        async cd(dir) {
            await UserCmdCodeHelper.fileBlockRef.fileBlockView(dir);
        },
        async ls(dir) {
            await this.cd(dir);
            return toRaw(UserCmdCodeHelper.fileBlockRef.files).map((file) => {
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
            return toRaw(UserCmdCodeHelper.fileBlockRef.dir);
        },
        async open(path, config={}) {
            const { dir, name } = UserCmdCodeHelper.parsePath(path);
            const fileInfo = await UserCmdCodeHelper.fileBlockRef.fileBlockView(dir, name);
            if(fileInfo && !fileInfo.isDirectory) await UserCmdCodeHelper.fileBlockRef.preViewFile(name, config);
            else throw new Error(i18n.global.t('无法打开文件：') + name);
        },
        edit(editFlow) {
            if(editFlow && editFlow instanceof Function) editFlow(UserCmdCodeHelper.fileBlockRef.filePreviewRef.codeEditorRef.aceEditor);
        },
        save(encode) {
            const filePreviewInstance = UserCmdCodeHelper.fileBlockRef.filePreviewRef;
            if(encode) filePreviewInstance.saveEncode = encode;
            filePreviewInstance.handleSave(filePreviewInstance.codeEditorRef.getValue());
        },
        close(block=false) {
            if(block) UserCmdCodeHelper.fileBlockRef.closeDialog();
            else UserCmdCodeHelper.fileBlockRef.filePreviewRef.closeDialog();
        },
        async download(path) {
            const { dir, name } = UserCmdCodeHelper.parsePath(path);
            const fileInfo = await UserCmdCodeHelper.fileBlockRef.fileBlockView(dir, name);
            if(fileInfo) {
                if(fileInfo.isDirectory) UserCmdCodeHelper.fileBlockRef.downloadDir(name);
                else UserCmdCodeHelper.fileBlockRef.downloadRemoteFile(name);
            }
            else throw new Error(i18n.global.t('无法下载文件：') + name);
        },
    },
    // 变量
    var: {
        session(key, value) {
            if(value) browser.sessionStorage.setItem(storageSessionPrefix + key, JSON.stringify(value));
            else {
                if(browser.sessionStorage.getItem(storageSessionPrefix + key)) return JSON.parse(browser.sessionStorage.getItem(storageSessionPrefix + key));
                else return null;
            }
        },
        local(key, value) {
            let cmdCodeLocalVars = {};
            if(browser.localStorage.getItem(storageLocalKey)) {
                cmdCodeLocalVars = JSON.parse(aesDecrypt(browser.localStorage.getItem(storageLocalKey)));
            }
            if(value) {
                cmdCodeLocalVars[key] = value;
                browser.localStorage.setItem(storageLocalKey, aesEncrypt(JSON.stringify(cmdCodeLocalVars)));
            }
            else return cmdCodeLocalVars[key];
        },
        clean() {
            if(arguments.length === 0) {
                browser.localStorage.removeItem(storageLocalKey);
                return;
            }
            let cmdCodeLocalVars = {};
            if(browser.localStorage.getItem(storageLocalKey)) {
                cmdCodeLocalVars = JSON.parse(aesDecrypt(browser.localStorage.getItem(storageLocalKey)));
            }
            for (let i = 0; i < arguments.length; i++) {
                delete cmdCodeLocalVars[arguments[i]];
            }
            browser.localStorage.setItem(storageLocalKey, aesEncrypt(JSON.stringify(cmdCodeLocalVars)));
        },
    },
    // 写入后等待
    async write(content, timeout = 10000) {
        return new Promise((resolve, reject) => {
            if(content === null || content === undefined) content = '';
            content = content.toString();
            if(!content.endsWith('\n') && !content.endsWith('\r')) content += '\n';
            const timer = browser.setTimeout(() => {
                UserCmdCodeHelper.resolve = null;
                UserCmdCodeHelper.outputs.push([]);
                reject();
            }, timeout);
            UserCmdCodeHelper.resolve = (output) => {
                clearTimeout(timer);
                UserCmdCodeHelper.resolve = null;
                UserCmdCodeHelper.outputs.push(output);
                resolve(output);
            };
            UserCmdCodeHelper.writeNoAwait(content, true);
        });
    },
    // 读取输出
    read(index = -1) {
        const len = UserCmdCodeHelper.outputs.length;
        return UserCmdCodeHelper.outputs[(index + len) % len] || [];
    },
    // 读取全部输出
    readAll() {
        return UserCmdCodeHelper.outputs.slice(0);
    },
    // 隐藏
    hide() {
        UserCmdCodeHelper.display = false;
    },
    // 显示
    show() {
        UserCmdCodeHelper.display = true;
    },
};
export const UserCmdCodeHelper = {
    name: '',
    active: false,
    display: true,
    outputs: [],
    resolve: null,
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
        this.outputs = [];
        this.resolve = null;
    },
};

const CmdCodeReservedVarsDict = {
    'option': 'CONNECT_OPTION',
    'home': 'HOME_DIRECTORY',
    'fdir': 'FILE_DIRECTORY',
    'wdir': 'WORK_DIRECTORY',
};
export const CmdCodeReservedVarsHelper = {
    set(key, val) {
        UserCmdCodeExecutor.var.session(CmdCodeReservedVarsDict[key], val);
    },
    clean() {
        for(const key in CmdCodeReservedVarsDict) {
            browser.sessionStorage.removeItem(storageSessionPrefix + CmdCodeReservedVarsDict[key]);
        }
    },
};

// 用户命令代码状态枚举
// Error-编译失败: Compile Error
// Interrupted-执行中断: Execute Interrupt
// Inactive-未被使用: Not Active
// Success-执行成功: Execute Success
export const CmdCodeStatusEnum = {
    'Compile Error': { type: -2, desc: 'Error' },
    'Execute Interrupt': { type: -1, desc: 'Interrupted' },
    'Not Active': { type: 0, desc: 'Inactive' },
    'Execute Success': { type: 1, desc: 'Success' },
};

// 编辑器添加kkTerminal智能提示
export const userCmdCodeExecutorCompleter = {
  getCompletions(editor, session, pos, prefix, callback) {
    const userCmdCodeExecutorCompletions = [
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
            description: "hide Command Code display",
            score: 1000,
        },
        {
            name: "show",
            value: "show()",
            meta: "kkTerminal",
            description: "show Command Code display",
            score: 1000,
        },
    ];

    callback(null, userCmdCodeExecutorCompletions.map((completion) => {
      return {
        caption: completion.caption || completion.name,
        value: completion.value,
        meta: completion.meta,
        description: completion.description || ''
      };
    }));
  }
};

// 历史命令代码
export const historyCmdCode = {
    cmdCodes: [],
    index: 0,
    add(latestCmdCode) {
        this.cmdCodes.push(latestCmdCode);
        this.index = this.cmdCodes.length;
    },
    up(currentCmdCode) {
        if(this.cmdCodes.length === 0) return currentCmdCode;
        this.index--;
        if(this.index < 0) this.index = 0;
        return this.cmdCodes[this.index];
    },
    down(currentCmdCode) {
        if(this.cmdCodes.length === 0) return currentCmdCode;
        this.index++;
        if(this.index >= this.cmdCodes.length) {
            this.index = this.cmdCodes.length;
            return '';
        }
        return this.cmdCodes[this.index];
    },
};
