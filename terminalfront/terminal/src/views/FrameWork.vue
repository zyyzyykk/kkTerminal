<template>
  <div class="global">
    <div style="flex: 1;"></div>
    <div style="box-shadow: 2px 2px 4px rgba(0, 0, 0, 0.3);">
      <div class="bar">
        <div><img src="../assets/logo.png" alt="终端" style="height: 20px; margin: 0 7px;" ></div>
        <div>kk Terminal</div>
      </div>
      <div ref="cmdBoxRef" class="content" @contextmenu.prevent="" @click="if(connect_status == 1) cmdInputRef.focus();" style="cursor: text; font-family: 'Courier New';">
        <div v-for="(line,index) in serverInfo" :key="line.id" style="margin: 5px 5px;">
          <div v-if="index != serverInfo.length - 1 && line.isHtml == true" v-html="line.content"></div>
          <div v-if="index != serverInfo.length - 1 && line.isHtml == false">
            {{ line.content.replace(/ /g, '&nbsp;') }}
          </div>
        </div>
        <div style="display: flex; align-items: center; margin: 5px 5px;">
          <div v-if="serverInfo[serverInfo.length - 1].isHtml == true" v-html="serverInfo[serverInfo.length - 1].content"></div>
          <div v-if="serverInfo[serverInfo.length - 1].isHtml == false">{{ serverInfo[serverInfo.length - 1].content.replace(/ /g, '&nbsp;') }}</div>
          <div style="flex: 1;" ><input type="text" v-model="now_cmd" ref="cmdInputRef" class="terminal-input" style="color: white;  font-family: 'Courier New';" /></div>
        </div>
      </div>
    </div>
    <div style="flex: 1;"></div>
  </div>

  <LoginSsh ref="loginSshRef" @doSSHCheck="doSSHCheck" ></LoginSsh>

</template>

<script>
import { handleANSI } from "../Utils/HandleANSI";
import { ref,onMounted,onUnmounted,computed } from 'vue';
import { Base64 } from '../Utils/Base64Util';
import LoginSsh from '../components/LoginSsh'
// import $ from "jquery";
import base_url from '../Utils/BaseUrl'
import { generateRandomString } from "../Utils/StringUtil";

export default {
  name: 'FrameWork',
  components: {
    LoginSsh,
  },
  setup() {

    // cmd区域
    const cmdBoxRef = ref();
    // cmd输入框
    const cmdInputRef = ref();

    // web-socket连接
    const socket = ref(null);
    // 连接状态 -1 连接失败 0 连接中 1 连接成功
    const connect_status = ref(0);
    const connect_status_tip = {
      '-1':'Fail to connect kk server !',
      '0':'Connecting to kk server ...',
      '1':'Connecting success',
    }
    const now_connect_status_tip = computed(() => {
      return connect_status_tip[connect_status.value];
    })

    // 终端服务器结果信息
    const serverInfo = ref([]);
    serverInfo.value.push({id:generateRandomString(64),...handleANSI(now_connect_status_tip.value)});

    // 读取服务器信息
    const loginSshRef = ref();
    const sshInfo = ref({});
    const getSSHInfo = () => {
      if(localStorage.getItem('ssh')) {
        sshInfo.value = JSON.parse(Base64.decode(localStorage.getItem('ssh')));
        connectSSH();
      }
      else loginSshRef.value.DialogVisilble = true;
    }
    const doSSHCheck = (info) => {
      sshInfo.value = info;
      loginSshRef.value.DialogVisilble = false;
      connectSSH();
    }
    const remember = () => {
      localStorage.removeItem('ssh');
      if(sshInfo.value.isRemember == true) {
        localStorage.setItem('ssh',Base64.encode(JSON.stringify(sshInfo.value)))
      }
    }
    // 连接服务器
    const now_cmd = ref('');
    const tag = ref(false);
    const connectSSH = () => {
      socket.value = new WebSocket(base_url + 'websocket/' + sshInfo.value.user_name + '/' + Base64.encode(sshInfo.value.password));
      // 当接收到服务器发送的信息时触发
      socket.value.onmessage = resp => {
        let result = JSON.parse(resp.data);
        // 连接失败
        if(result.code == -1) {
          connect_status.value = -1;
          serverInfo.value.shift();
          serverInfo.value.push({id:generateRandomString(64),...handleANSI(now_connect_status_tip.value)});
          if(socket.value) {
            socket.value.close();
            socket.value = null;
          }
          if(localStorage.getItem('ssh')) sshInfo.value = JSON.parse(Base64.decode(localStorage.getItem('ssh')));
          loginSshRef.value.DialogVisilble = true;
        }
        // 显示在终端里面
        if(result.code >= 1) {
          if(connect_status.value != 1) {
            serverInfo.value.shift();
            connect_status.value = 1;
            // 记住
            remember();
          }

          // kk欢迎语
          if(result.code == 2) {
            serverInfo.value.push({id:generateRandomString(64),...handleANSI(Base64.decode(result.info))});
          }

          // 初始化
          if(result.code == 3) {
            if(socket.value) {
              socket.value.send(Base64.encode(JSON.stringify({type:10,content:"\n"})));
              now_cmd.value = '';
            }
          }

          // shell发来的命令结果
          if(result.code == 10) {
            let arr = Base64.decode(result.info).split('\n');
            // console.log(arr);
            for(let i=0;i<arr.length;i++) {
              if(arr[i] == '') continue;
              if(i == 0 && arr[0] == now_cmd.value + '\r') {
                let info = serverInfo.value[serverInfo.value.length - 1].origin;
                serverInfo.value.pop();
                serverInfo.value.push({id:generateRandomString(64),...handleANSI(info + arr[i])});
              }
              else serverInfo.value.push({id:generateRandomString(64),...handleANSI(arr[i])});
              now_cmd.value = '';
            }
          }
          
          // shell发来的快捷键结果
          if(result.code == 11) {
            let arr = Base64.decode(result.info).split('\n');
            console.log(arr[0] == now_cmd.value,tag.value,arr[0]);
            for(let i=0;i<arr.length;i++) {
              if(arr[i] == '') continue;
              if(i == 0 && arr[0] == now_cmd.value && tag.value == false) {
                tag.value = true;
                continue;
              }
              else if(tag.value == true) {
                now_cmd.value = now_cmd.value + arr[i];
                tag.value = false;
                continue;
              } else if(tag.value == false) {
                serverInfo.value.push({id:generateRandomString(64),...handleANSI(arr[i])});
              }
            }
          }


        }
      }
    }


    // 终端快捷键
    const shortcutKeys = {
      'ctrl+c':67,
      'enter':13,
      'tab':9,
      'ctrl+u':85,
      'ctrl+d':68,
    };


    // 监听cmd输入框事件
    const watchCmdInput = () => {
      // 禁止通过鼠标改变光标位置
      cmdInputRef.value.addEventListener('mousedown', function(event) {
        event.preventDefault();
      });
      // 监听快捷键
      cmdInputRef.value.addEventListener('keydown', function(event) {
        // ctrl+?
        if(event.ctrlKey) {
          event.preventDefault();
        }
        
        // ctrl+c
        if (event.ctrlKey && event.keyCode === shortcutKeys['ctrl+c']) {
          if(socket.value) {
            // socket.value.send(Base64.encode(JSON.stringify({type:12,content:now_cmd.value})));
            socket.value.send(Base64.encode(JSON.stringify({type:13,content:"3"})));
          }
        }
        // enter
        else if(event.keyCode === shortcutKeys['enter']) {
          event.preventDefault();
          if(socket.value) {
            socket.value.send(Base64.encode(JSON.stringify({type:10,content:now_cmd.value + "\n"})));
          }
        }
        // tab
        else if(event.keyCode === shortcutKeys['tab']) {
          event.preventDefault();
          if(socket.value) {
            socket.value.send(Base64.encode(JSON.stringify({type:12,content:now_cmd.value})));
            socket.value.send(Base64.encode(JSON.stringify({type:11,content:"9"})));
          }
        }
        // ctrl+u
        else if(event.ctrlKey && event.keyCode === shortcutKeys['ctrl+u']) {
          now_cmd.value = '';
        }
        // ctrl+u
        else if(event.ctrlKey && event.keyCode === shortcutKeys['ctrl+d']) {
          socket.value.send(Base64.encode(JSON.stringify({type:11,content:"3"})));
        }
      });
    }


    onMounted(() => {
      getSSHInfo();
      // 监听cmd输入框事件
      watchCmdInput();
    });

    onUnmounted(() => {
      // 卸载全部监听事件
      // cmdInputRef.value.removeEventListener();
    });

    return {
      socket,
      getSSHInfo,
      loginSshRef,
      doSSHCheck,
      connectSSH,
      connect_status,
      now_connect_status_tip,
      serverInfo,
      cmdInputRef,
      shortcutKeys,
      now_cmd,
      cmdBoxRef,
      
    }
  }
}
</script>

<style scoped>
.global {
  height: 100vh;
  background-color: #f9f9f9;
  display: flex;
  align-items: center;
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

.content {
  height: 80vh;
  width: 60vw;
  background-color: #000000;
  overflow-y: scroll;
  /* overflow-x: scroll; */
  border-top: 1px solid #d7d7d7;
  border-bottom: 1px solid #d7d7d7;
  border-left: 1px solid #d7d7d7;
  border-right: 1px solid #d7d7d7;
  color: white;
}

.terminal-input {
  border: none; 
  outline: none; 
  min-width: 100%; 
  background-color: transparent; 
  white-space: pre-wrap;
  word-break: break-all;
}

</style>