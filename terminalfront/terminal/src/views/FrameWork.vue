<template>
  <div class="golbal">
    <div>
      <!-- 设置栏 -->
      <div class="setting" >
        <div class="setting-menu" >连接配置</div>
        <div class="setting-menu" >样式配置</div>
        <div class="setting-menu" >重连</div>
      </div>
    </div>
    <!-- <div class="bar">
      <div><img src="../assets/logo.png" alt="终端" style="height: 20px; margin: 0 7px;" ></div>
      <div>kk Terminal</div>
    </div> -->
    <!-- terminal主体 -->
    <div ref="terminal" id="terminal"></div>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue';
import { encrypt, decrypt } from '@/Utils/Encrypt';
import { Terminal } from 'xterm';
import { default_env } from '@/Utils/Env';
import base_url from '@/Utils/BaseUrl';
import { changeStr } from '@/Utils/StringUtil';
import "xterm/css/xterm.css";
// import { FitAddon } from 'xterm-addon-fit';

export default {
  name: "FrameWork",
  components: {
  },
  setup() {

    // 读取环境变量
    const env = ref(null);
    if(localStorage.getItem('env')) env.value = JSON.parse(decrypt(localStorage.getItem('env')));
    else env.value = default_env;

    // 连接状态
    const connect_status = ref({
      '-1':'Fail to connect kk server !\r\n',
      '0':'Connecting success !\r\n',
      '1':'Connecting to kk server ...\r\n',
    })
    const now_connect_status = ref(connect_status.value['1']);

    // 终端
    const terminal = ref();
    let term = new Terminal({
      rendererType: "canvas",                               // 渲染类型
      rows: 40,                                             // 行数
      cols: 100,                                            // 不指定行数，自动回车后光标从下一行开始
      convertEol: true,                                     // 启用时，光标将设置为下一行的开头
      // scrollback: 50,                                    // 终端中的回滚量
      disableStdin: false,                                  // 是否应禁用输入
      cursorStyle: env.value.cursorStyle,                   // 光标样式
      cursorBlink: env.value.cursorBlink,                   // 光标闪烁

      theme: {
        foreground: env.value.fg,                           // 前景色
        background: env.value.bg,                           // 背景色
        cursor: "help",                                     // 设置光标
        lineHeight: 20,
        fontFamily: env.value.fontFamily,                   // 设置字体为 Consolas
        fontSize: env.value.fontSize                        // 设置字号为 16
      }
    });

    // websocket连接
    const socket = ref(null);
    const doSSHConnect = () => {
      socket.value = new WebSocket(base_url + changeStr(encrypt(JSON.stringify(env.value))));
      socket.value.onmessage = resp => {
        let result = JSON.parse(resp.data);
        // 连接失败
        if(result.code == -1) {
          term.clear();
          now_connect_status.value = connect_status.value[resp.code];
          term.write(now_connect_status.value);
        }
        // 连接成功
        if(result.code == 0) {
          term.clear();
          now_connect_status.value = connect_status.value[resp.code];
          term.write(now_connect_status.value);
        }
        // 输出
        if(result.code == 1) {
          term.write(decrypt(result.info));
        }
      }
    }

    doSSHConnect();

    onMounted(() => {
      term.open(terminal.value);

      // term.prompt = () => {
      //     term.write("\r\n\x1b[33m$\x1b[0m ")
      // }

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


    });

    return {
      terminal,
      doSSHConnect,
      socket,
    }

  }
}
</script>


<style scoped>

.golbal {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100vh;
}

.bar {
  background-color: #f8f8f8;
  color: black;
  width: 60vw;
  height: 25px;
  display: flex;
  align-items: center;
  border-top: 1px solid #d7d7d7;
  border-bottom: 1px solid #d7d7d7;
  border-left: 1px solid #d7d7d7;
  border-right: 1px solid #d7d7d7;
}

#terminal {
  width: 100%;
  height: 100%;
}

.setting {
  position: absolute;
  left: 0;
  top: 0;
  display: none;
}

.setting-menu {
  background-color: #f2f2f2;
}

.setting-menu:hover {
  background-color: #91c9f7;
}


</style>