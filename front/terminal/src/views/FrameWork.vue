<template>
  <div class="golbal">
    <!-- 设置栏 -->
    <div class="setting" v-show="isShowSetting" >
      <div class="setting-menu no-select" @click="doSettings(1)" ><div>连接设置</div></div>
      <div class="setting-menu no-select" @click="doSettings(2)" ><div>偏好设置</div></div>
      <div class="setting-menu no-select" @click="doSettings(4)" ><div>文件管理</div></div>
      <div class="setting-menu no-select" @click="doSettings(3)" ><div>重启</div></div>
    </div>
    <div class="bar">
      <div style="user-select: none;" @click="showSettings" >
        <img src="../assets/logo.png" alt="终端" style="height: 16px; margin: 0 7px; cursor: pointer;" >
      </div>
      <div style="user-select: none; font-size: 14px;" ><span>kk Terminal</span></div>
      <div style="flex: 1;"></div>
      <div v-if="env.tCode" style="display: flex; align-items: center;" >
        <div style="font-size: 12px; color: #313131; user-select: none;" > TCode </div>
        <div style="margin-left: 7px;" ></div>
        <div>
          <el-input
            v-model="tcode"
            id="kkterminalTcode"
            ref="tCodeInputRef"
            :style="{ width: '100px', height: '20px', fontSize: '12px'}"
            @keydown.enter="handleTcode"
            maxlength="6"
          >
          </el-input>
        </div>
        <div style="cursor: pointer; margin-left: 5px;" >
          <el-popover placement="bottom-end" :width="220" trigger="hover" >
            <template #reference>
              <el-icon :style="{ color: '#606266' }" ><QuestionFilled /></el-icon>
            </template>
            <div style="font-size: 12px; color: #313131;" >
              <div style="user-select: none; font-size: 14px; font-weight: bold;" >什么是 TCode (终端代码) ？</div>
              <div style="user-select: none; margin-top: 5px;">TCode（终端代码）是用于访问和执行特定操作流程的快捷方式</div>
              <div style="user-select: none; margin-top: 5px;">
                输入 
                <span style="background-color: #f3f4f4; user-select: text;" >/H</span>
                并按下回车，查看帮助信息
              </div>
              <div style="user-select: none; margin-top: 5px;">
                输入 
                <span style="background-color: #f3f4f4; user-select: text;" >/A</span>
                并按下回车，自定义TCode
              </div>
            </div>
          </el-popover>
        </div>
        <div style="margin-left: 20px;" ></div>
      </div>
    </div>
    <!-- terminal主体 -->
    <div ref="terminal" class="terminal-class" ></div>
  </div>

  <!-- 连接设置 -->
  <ConnectSetting ref="connectSettingRef" :env="env" :sshOptions="options" @saveOp="saveOp" @callback="saveEnv"></ConnectSetting>
  <!-- 样式设置 -->
  <StyleSetting ref="styleSettingRef" :env="env" @callback="saveEnv" :os="osInfo.clientOS" ></StyleSetting>
  <!-- 文件管理 -->
  <FileBlock ref="fileBlockRef" :sshKey="sshKey" :os="osInfo.clientOS" ></FileBlock>
  <!-- 用户TCode -->
  <UserTcode ref="userTcodeRef" @importTCodes="importTCodes" @exportTcodes="exportTcodes" ></UserTcode>
  <!-- 帮助TCode -->
  <HelpTcode ref="helpTcodeRef" :userTCodes="tcodes" @handleSaveTCode="handleSaveTCode" ></HelpTcode>

</template>

<script>
import useClipboard from "vue-clipboard3";
import { ref, onMounted, onUnmounted, getCurrentInstance } from 'vue';
import { encrypt, decrypt } from '@/Utils/Encrypt';
import { ElMessage } from 'element-plus';

import { Terminal } from 'xterm';
import { FitAddon } from 'xterm-addon-fit';
import "xterm/css/xterm.css";

import $ from 'jquery';
import { default_env } from '@/Utils/Env';
import { ws_base_url } from '@/Utils/BaseUrl';
import { changeStr } from '@/Utils/StringUtil';
import { http_base_url } from '@/Utils/BaseUrl';

import ConnectSetting from '@/components/ConnectSetting.vue';
import StyleSetting from '@/components/StyleSetting.vue';
import FileBlock from "@/components/FileBlock.vue";
import UserTcode from '@/components/tcode/UserTcode.vue';
import HelpTcode from "@/components/tcode/HelpTcode.vue";

import { QuestionFilled } from '@element-plus/icons-vue';

import { FuncTcode, SysTcode, UserTcodeExecutor } from "@/Utils/Tcode";

export default {
  name: "FrameWork",
  components: {
    ConnectSetting,
    StyleSetting,
    FileBlock,
    UserTcode,
    HelpTcode,
    QuestionFilled,
  },
  setup() {

    // 获取当前组件实例
    const instance = getCurrentInstance();

    // 拷贝
    const { toClipboard } = useClipboard();

    // 终端自适应
    const fitAddon = new FitAddon();

    // 加载环境变量
    const osInfo = ref({});
    const options = ref({});
    const loadOps = () => {
      if(localStorage.getItem('options')) options.value = JSON.parse(decrypt(localStorage.getItem('options')));
      else options.value = {};
    };
    loadOps();
    const tcodes = ref({});
    const loadTCodes = () => {
      tcodes.value = {};
      if(localStorage.getItem('tcodes')) {
        tcodes.value = JSON.parse(decrypt(localStorage.getItem('tcodes')));
      }
      setTimeout(() => {
        helpTcodeRef.value.userTCodes = {...tcodes.value};
      },1);
    }
    loadTCodes();
    const env = ref(null);
    const loadEnv = () => {
      if(localStorage.getItem('env')) {
        env.value = JSON.parse(decrypt(localStorage.getItem('env')));
        let nowOpInfo = options.value[env.value['option']];
        if(nowOpInfo) env.value = {...env.value,...nowOpInfo};
        else env.value.option = '';
      }
      else env.value = default_env;
    };
    loadEnv();

    // 保存更改的配置
    const saveOp = (name,item) => {
      options.value = {...options.value,[name]:item};
      localStorage.setItem('options',encrypt(JSON.stringify(options.value)));
      loadOps();
    };

    // 连接状态
    const connect_status = ref({
      'Fail':'Fail to connect remote server !\r\n',
      'Success':'Connecting success !\r\n',
      'Connecting':'Connecting to remote server ...\r\n',
      'Disconnected':'Disconnect to remote server.\r\n',
    });
    const now_connect_status = ref(connect_status.value['Connecting']);

    // 初始化终端
    const terminal = ref();
    let term = null;
    const isFirst = ref(true);
    const initTerminal = () => {
      term = new Terminal({
        rendererType: "canvas",                               // 渲染类型
        convertEol: true,                                     // 启用时，光标将设置为下一行的开头
        scrollback: 0,                                        // 终端中的回滚量
        disableStdin: false,                                  // 是否应禁用输入
        cursorStyle: env.value.cursorStyle,                   // 光标样式 block
        cursorBlink: env.value.cursorBlink,                   // 光标闪烁
        theme:{
          foreground: env.value.fg,                           // 前景色
          background: env.value.bg,                           // 背景色
          cursor: "help",                                     // 设置光标
        },
        lineHeight: 1.2,
        fontFamily: env.value.fontFamily,                     // 设置字体为 Consolas
        fontSize: env.value.fontSize,                         // 设置字号为 16
      });
      term.loadAddon(fitAddon);
    }

    // 终端视高自适应
    const termFit = () => {
      terminal.value.style.height = (window.innerHeight - 25) + 'px';
      fitAddon.fit();
      // 修改虚拟终端行列大小
      if(socket.value && socket.value.readyState == WebSocket.OPEN && term) {
        let new_rows = fitAddon.proposeDimensions().rows;
        let new_cols = fitAddon.proposeDimensions().cols;
        socket.value.send(encrypt(JSON.stringify({type:1,content:"",rows:new_rows,cols:new_cols})));
        term.resize(new_cols,new_rows);
      }
    };

    // websocket连接
    const sshKey = ref('');
    const socket = ref(null);
    const doSSHConnect = () => {
      socket.value = new WebSocket(ws_base_url + changeStr(encrypt(JSON.stringify(env.value))));
      socket.value.onopen = () => {
        termFit();
      }
      socket.value.onmessage = resp => {
        let result = JSON.parse(resp.data);
        // 连接失败
        if(result.code == -1) {
          term.clear();
          now_connect_status.value = connect_status.value['Fail'];
          term.write(now_connect_status.value);
          doSettings(1);
        }
        // 连接成功
        if(result.code == 0) {
          term.clear();
          now_connect_status.value = connect_status.value['Success'];
          sshKey.value = decrypt(result.info);
          setTimeout(() => {
            termFit();
          },1);
        }
        // 输出
        if(result.code == 1) {
          let output = decrypt(result.info);
          if(UserTcodeExecutor.active) UserTcodeExecutor.outArray.push(output);
          if(!(UserTcodeExecutor.active && !UserTcodeExecutor.display)) term.write(output);
          // 设置回滚量
          term.options.scrollback += term._core.buffer.lines.length;
        }
      }
      socket.value.onclose = (e) => {
        if(now_connect_status.value == connect_status.value['Success'] && e.code != 3333) {
          sshKey.value = '';
          closeFileBlock();
          now_connect_status.value = connect_status.value['Disconnected'];
          term.write("\r\n" + now_connect_status.value);
        }
        userTcodeExecutorReset();
      }
    };

    // 终端信息设置
    const isShowSetting = ref(false);
    const showSettings = () => {
      isShowSetting.value = !isShowSetting.value;
    };
    const connectSettingRef = ref();
    const styleSettingRef = ref();
    const fileBlockRef = ref();
    // 保存更改的环境变量
    const saveEnv = (new_env) => {
      env.value = {...env.value,...new_env};
      localStorage.setItem('env',encrypt(JSON.stringify(env.value)));
      doSettings(3);
    };

    // 文本消息发送
    const sendMessage = (text, active=false) => {
      if(socket.value) {
        // 重启后第一次输入
        if(isFirst.value) {
          termFit();
          isFirst.value = false;
        }
        if(UserTcodeExecutor.active === active) socket.value.send(encrypt(JSON.stringify({type:0,content:text,rows:0,cols:0})));
      }
    };

    // 中文
    const putChinese = (event) => {
      event.preventDefault();
      sendMessage(event.target.value);
      event.target.value = '';
    };

    // 右键事件函数
    const doPaste = async function(event) {
      event.preventDefault();   // 阻止默认的上下文菜单弹出
      let pasteText = await navigator.clipboard.readText();
      sendMessage(pasteText);
    };

    // 单击事件
    const doClick = () => {
      isShowSetting.value = false;
    };

    // 重启终端
    const resetTerminal = () => {
      if(term && terminal.value) {
        terminal.value.removeEventListener('contextmenu', doPaste);
        terminal.value.removeEventListener('compositionend', putChinese);
        terminal.value.removeEventListener('click', doClick);
      }
      terminal.value.innerHTML = '';
      isFirst.value = true;
      loadEnv();
      initTerminal();
      term.open(terminal.value);
      termFit();

      // 支持中文输入
      terminal.value.addEventListener('compositionend', putChinese);

      // 正常输入
      term.onKey(e => {
        sendMessage(e.key);
      });

      // 监听选中文本，自动复制
      term.onSelectionChange(async () => {
        if (term.hasSelection()) {
          let copyText = term.getSelection();
          let copyTextTrim = copyText.trim();
          if(copyTextTrim && copyTextTrim != '') await toClipboard(copyText);
        }
      });

      // 右键进行粘贴
      terminal.value.addEventListener('contextmenu', doPaste);

      // 左键单击
      terminal.value.addEventListener('click', doClick);

      term.write(now_connect_status.value);
    };
    
    // 终端设置
    const doSettings = (type) => {
      // 连接设置
      if(type == 1) {
        isShowSetting.value = false;
        connectSettingRef.value.DialogVisilble = true;
      }
      // 偏好设置
      else if (type == 2) {
        isShowSetting.value = false;
        styleSettingRef.value.DialogVisilble = true;
      }
      // 重启
      else if (type == 3) {
        isShowSetting.value = false;
        now_connect_status.value = connect_status.value['Connecting'];
        sshKey.value = '';
        if(socket.value) socket.value.close(3333);  // 主动释放资源，必需
        // 进行重启
        closeFileBlock();
        doSSHConnect();
        resetTerminal();
      }
      // 文件管理
      else if(type == 4) {
        isShowSetting.value = false;
        fileBlockRef.value.DialogVisilble = true;
        fileBlockRef.value.getInitDir();
      }
    };

    // websocket心跳续约 (25秒)
    let timer = null;
    const doHeartBeat = () => {
      if(timer == null) {
        timer = setInterval(() => {
          if(socket.value && socket.value.readyState == WebSocket.OPEN) {
            socket.value.send(encrypt(JSON.stringify({type:2,content:"",rows:0,cols:0})));
          }
          // PC端
          if(osInfo.value.serverOS != "Linux") {
            $.ajax({
              url: http_base_url + '/beat',
              type:'post',
              data:{
                windowId:osInfo.value.windowId,
              },
              success(){
              }
            });
          }
        },25000);
      }
    };

    // 关闭文件模块
    const closeFileBlock = () => {
      fileBlockRef.value.txtPreviewRef.DialogVisilble = false;
      fileBlockRef.value.txtPreviewRef.resetEditor();
      fileBlockRef.value.mkFileRef.DialogVisilble = false;
      fileBlockRef.value.fileAttrRef.DialogVisilble = false;
      fileBlockRef.value.DialogVisilble = false;
      fileBlockRef.value.dir = '';
      fileBlockRef.value.selectedFiles = [];
      fileBlockRef.value.renameFile = null;
      fileBlockRef.value.fileClipboard = {
        path:'/',
        files:[],
      };
      fileBlockRef.value.isCtrlx = false;
    };

    const setTcodeStatus = (transTcode, state) => {
      tcodes.value[transTcode].status = state;
      localStorage.setItem('tcodes',encrypt(JSON.stringify(tcodes.value)));
      setTimeout(() => {
        helpTcodeRef.value.userTCodes = {...tcodes.value};
      },1);
    }

    // 处理终端代码
    const tcode = ref('');
    const handleTcode = async () => {
      if(!tcode.value || tcode.value.length < 2) return;
      let transTcode = tcode.value.toUpperCase();
      tcode.value = '';
      // 功能TCode
      if(transTcode[0] == '/' && FuncTcode[transTcode]) FuncTcode[transTcode].execFlow(instance);
      // 系统TCode
      else if(transTcode[0] == 'S' && SysTcode[transTcode]) SysTcode[transTcode].execFlow(instance);
      // 用户TCode
      else if(transTcode[0] == 'U' && tcodes.value[transTcode]) {
        if(!UserTcodeExecutor.writeOnly) UserTcodeExecutor.writeOnly = sendMessage;
        // 未激活
        if(UserTcodeExecutor.active == false) {
          userTcodeExecutorReset();
          UserTcodeExecutor.active = true;
          // 执行流未被定义
          if(!tcodes.value[transTcode].execFlow || !(tcodes.value[transTcode].execFlow instanceof Function)) {
            let textflow = tcodes.value[transTcode].workflow.toString();
            try {
              tcodes.value[transTcode].execFlow = new Function('kkTerminal', `return (async function() { ${textflow} })()`);
            } catch (error) {
              setTcodeStatus(transTcode, 'Load Error');
              ElMessage({
                message: 'TCode-' + transTcode + ' Load Error: ' + error,
                type: 'error',
                grouping: true,
              });
              userTcodeExecutorReset();
              return;
            }
          }
          // 执行Workflow
          try {
            await tcodes.value[transTcode].execFlow(UserTcodeExecutor);
            ElMessage({
              message: 'TCode-' + transTcode + ' Workflow Over',
              type: 'success',
              grouping: true,
            });
            setTcodeStatus(transTcode, 'Execute Success');
          } catch(error) {
            ElMessage({
              message: 'TCode-' + transTcode + ' Execute Error: ' + error,
              type: 'warning',
              grouping: true,
            });
            setTcodeStatus(transTcode, 'Execute Error');
          } finally {
            userTcodeExecutorReset();
          }
        }
        else {
          ElMessage({
            message: 'Another TCode is executing',
            type: 'warning',
            grouping: true,
          });
        }
      }
      else {
        ElMessage({
          message: 'TCode-' + transTcode + ' 不存在',
          type: 'warning',
          grouping: true,
        });
      }
    }
    // 重置用户TCode执行器
    const userTcodeExecutorReset = () => {
      UserTcodeExecutor.active = false;
      UserTcodeExecutor.display = true;
      UserTcodeExecutor.outArray = [];
      UserTcodeExecutor.cnt = 0;
    }
    const userTcodeRef = ref();
    // 批量导入TCode
    const importTCodes = (data) => {
      let tCodeData = {};
      tCodeData = {...tcodes.value,...data};
      localStorage.setItem('tcodes',encrypt(JSON.stringify(tCodeData)));
      loadTCodes();
    };
    // 批量导出TCode
    const exportTcodes = () => {
      let content = {};
      if(localStorage.getItem('tcodes')) content = JSON.parse(decrypt(localStorage.getItem('tcodes')));
      // 创建 Blob 对象
      const blob = new Blob([JSON.stringify(content, null, 4)], { type: 'text/plain' });
      // 创建指向 Blob 的 URL
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = 'TCode.json';
      document.body.appendChild(a);
      a.click();
      document.body.removeChild(a);
      // 释放 URL 对象
      URL.revokeObjectURL(url);
    }
    // 帮助
    const helpTcodeRef = ref();
    const handleSaveTCode = (name, content) => {
      let data = {};
      data[name] = {
        desc: tcodes.value[name].desc || '',
        workflow: content || '',
        status: 'Not Active',
      };
      importTCodes(data);
    }

    onMounted(() => {

      // 启动终端
      resetTerminal();

      $.ajax({
        url: http_base_url + '/init',
        type:'get',
        data: {
          time: new Date().getTime(),
        },
        success(resp){
          osInfo.value = {...resp.data};
          // 连接服务器
          doSSHConnect();
          // 监听窗口大小变化事件，自动调整终端大小
          window.addEventListener('resize', () => {
            if(fileBlockRef.value) {
              fileBlockRef.value.isShowMenu = false;
              fileBlockRef.value.isShowPop = false;
            }
            termFit();
          });
          // 心跳
          doHeartBeat();
        },
      });
    });

    onUnmounted(() => {
      if(term && terminal.value) {
        terminal.value.removeEventListener('contextmenu', doPaste);
        terminal.value.removeEventListener('compositionend', putChinese);
        terminal.value.removeEventListener('click', doClick);
      }
      if(timer) clearInterval(timer);
    });

    return {
      env,
      options,
      now_connect_status,
      connect_status,
      terminal,
      doSSHConnect,
      socket,
      showSettings,
      isShowSetting,
      doSettings,
      connectSettingRef,
      styleSettingRef,
      fileBlockRef,
      saveEnv,
      sshKey,
      doHeartBeat,
      saveOp,
      tcode,
      handleTcode,
      closeFileBlock,
      resetTerminal,
      tcodes,
      userTcodeRef,
      sendMessage,
      importTCodes,
      exportTcodes,
      helpTcodeRef,
      handleSaveTCode,
      osInfo,
    }

  }
}
</script>


<style scoped>

.golbal {
  position: relative;
  width: 100%;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.bar {
  display: flex;
  align-items: center;
  background-color: #f5f5f5;
  color: black;
  width: 100%;
  height: 25px;
  border-top: 1px solid #d7d7d7;
  border-bottom: 1px solid #d7d7d7;
  border-left: 1px solid #d7d7d7;
  border-right: 1px solid #d7d7d7;
}

.terminal-class {
  width: 100%;
}

.setting {
  position: absolute;
  left: 0;
  top: 25px;
  z-index: 100;
  cursor: pointer;
  border: 2px solid #f2f2f2;
}

.setting-menu {
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f2f2f2;
  text-align: center;
  width: 80px;
  height: 25px;
  line-height: 25px;
  font-size: 13px;
  color: #383838;
}

.setting-menu:hover {
  background-color: #91c9f7;
}

/* 文本不可选中 */
.no-select {
  user-select: none;
}
</style>