<template>
  <div class="golbal">
    <!-- 设置栏 -->
    <div class="setting" v-show="isShowSetting" >
      <div class="setting-menu" @click="doSettings(1)" ><div>连接设置</div></div>
      <div class="setting-menu" @click="doSettings(2)" ><div>样式设置</div></div>
      <div class="setting-menu" @click="doSettings(3)" ><div>重启</div></div>
    </div>
    <div class="bar">
      <div @click="showSettings" >
        <img src="../assets/logo.png" alt="终端" style="height: 16px; margin: 0 7px; cursor: pointer;" >
      </div>
      <div style="font-size: 14px;" ><span>kk Terminal</span></div>
    </div>
    <!-- terminal主体 -->
    <div ref="terminal" class="terminal-class"></div>
  </div>

  <!-- 连接设置 -->
  <ConnectSetting ref="connectSettingRef" :env="env" @callback="saveEnv" ></ConnectSetting>
  <!-- 样式设置 -->
  <StyleSetting ref="styleSettingRef" :env="env" @callback="saveEnv" ></StyleSetting>

</template>

<script>
import { ref, onMounted } from 'vue';
import { encrypt, decrypt } from '@/Utils/Encrypt';

import { Terminal } from 'xterm';
import { FitAddon } from 'xterm-addon-fit'
import "xterm/css/xterm.css";

import { default_env } from '@/Utils/Env';
import base_url from '@/Utils/BaseUrl';
import { changeStr } from '@/Utils/StringUtil';

import ConnectSetting from '@/components/ConnectSetting.vue';
import StyleSetting from '@/components/StyleSetting.vue';

export default {
  name: "FrameWork",
  components: {
    ConnectSetting,
    StyleSetting,
  },
  setup() {

    const fitAddon = new FitAddon();

    // 加载环境变量
    const env = ref(null);
    const loadEnv = () => {
      if(localStorage.getItem('env')) env.value = JSON.parse(decrypt(localStorage.getItem('env')));
      else env.value = default_env;
    }
    loadEnv();

    // 连接状态
    const connect_status = ref({
      'Fail':'Fail to connect kk server !\r\n',
      'Success':'Connecting success !\r\n',
      'Connecting':'Connecting to kk server ...\r\n',
    })
    const now_connect_status = ref(connect_status.value['Connecting']);

    // 终端
    const terminal = ref();
    let term = null;
    const initTerminal = () => {
      term = new Terminal({
        rendererType: "canvas",                               // 渲染类型
        // rows: 20,                                          // 行数
        // cols: 10,                                          // 不指定行数，自动回车后光标从下一行开始
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
        fontFamily: env.value.fontFamily,                     // 设置字体为 Simsun
        fontSize: env.value.fontSize,                         // 设置字号为 16

      });
      term.loadAddon(fitAddon);
    }

    // 终端视高自适应
    const termFit = () => {
      terminal.value.style.height = (window.innerHeight - 27) + 'px';
      fitAddon.fit();
    }

    // websocket连接
    const socket = ref(null);
    const doSSHConnect = () => {
      socket.value = new WebSocket(base_url + changeStr(encrypt(JSON.stringify(env.value))));
      socket.value.onmessage = resp => {
        let result = JSON.parse(resp.data);
        // 连接失败
        if(result.code == -1) {
          term.clear();
          now_connect_status.value = connect_status.value['Fail'];
          term.write(now_connect_status.value);
          // 第一次使用
          if(env.value.server_ip || env.value.server_ip == '') {
            doSettings(1);
          }
        }
        // 连接成功
        if(result.code == 0) {
          term.clear();
          now_connect_status.value = connect_status.value['Success'];
          // term.write(now_connect_status.value);
        }
        // 输出
        if(result.code == 1) {
          term.write(decrypt(result.info));
          fitAddon.fit();
          // 设置回滚量
          term.options.scrollback += term._core.buffer.lines.length;
        }
      }
    };

    // 终端信息设置
    const isShowSetting = ref(false);
    const showSettings = () => {
      isShowSetting.value = !isShowSetting.value;
    };
    const connectSettingRef = ref();
    const styleSettingRef = ref();
    // 保存更改的环境变量
    const saveEnv = (new_env) => {
      env.value = {...env.value,...new_env};
      localStorage.setItem('env',encrypt(JSON.stringify(env.value)));
      doSettings(3);
    } 
    // 重启终端
    const resetTerminal = () => {
      loadEnv();
      if(term) terminal.value.innerHTML = '';
      initTerminal();
      term.open(terminal.value);
      termFit();

      // 添加事件监听器，支持输入方法
      term.onKey(e => {
        // const printable = !e.domEvent.altKey && !e.domEvent.altGraphKey && !e.domEvent.ctrlKey && !e.domEvent.metaKey
        socket.value.send(e.key);
      })

      // 粘贴的情况 ??
      // term.onData(key => {
      //     // 仅仅这样判断 双 ascii 按键也会触发，不想要
      //     if (key.length > 1) {
      //         for (let i = 0; i < key.length; i++)
      //             socket.value.send(key[i]);
      //     }
      // });

      term.write(now_connect_status.value);
    }
    const doSettings = (type) => {
      // 连接设置
      if(type == 1) {
        isShowSetting.value = false;
        connectSettingRef.value.DialogVisilble = true;
      }
      // 样式设置
      else if (type == 2) {
        isShowSetting.value = false;
        styleSettingRef.value.DialogVisilble = true;
      }
      // 重启
      else if (type == 3) {
        isShowSetting.value = false;
        now_connect_status.value = connect_status.value['Connecting'];
        if(socket.value) socket.value.close();
        doSSHConnect();
        resetTerminal();
      }
    }

    onMounted(() => {
      // 连接服务器
      doSSHConnect();

      // 启动终端
      resetTerminal();

      // 监听窗口大小变化事件，自动调整终端大小
      window.addEventListener('resize', () => {
        termFit();
      });
    });

    return {
      env,
      now_connect_status,
      terminal,
      doSSHConnect,
      socket,
      showSettings,
      isShowSetting,
      doSettings,
      connectSettingRef,
      styleSettingRef,
      saveEnv,
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
  height: calc(100% - 30);
}

.setting {
  position: absolute;
  left: 0;
  top: 25px;
  z-index: 100;
  cursor: pointer;;
}

.setting-menu {
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f2f2f2;
  text-align: center;
  width: 80px;
  height: 25px;
  font-size: 13px;
  color: #555;
}

.setting-menu:hover {
  background-color: #91c9f7;
}


</style>