<template>
  <div class="global">
    <div style="flex: 1;"></div>
    <div>
      <div class="bar">
        <div><img src="../assets/logo.png" alt="终端" style="height: 20px; margin: 0 7px;" ></div>
        <div>kk Terminal</div>
      </div>
      <div class="content" @click="cmdInputRef.focus();" style="cursor: text; font-family: 'Courier New';">
        <div v-for="(line,index) in serverInfo" :key="index" style="margin: 5px 5px;">
          {{ line.replace(/ /g, '&nbsp;') }}
        </div>
        <div style="display: flex; align-items: center;">
          <div>{{ cmdPrefix.replace(/ /g, '&nbsp;') }}</div>
          <div><input type="text" ref="cmdInputRef" style="background-color: black; color: white;"></div>
        </div>
      </div>
    </div>
    <div style="flex: 1;"></div>
  </div>

  <LoginSsh ref="loginSshRef" @doSSHCheck="doSSHCheck" ></LoginSsh>

</template>

<script>
import { ref,onMounted,computed } from 'vue';
import { Base64 } from '../Utils/Base64Util';
import LoginSsh from '../components/LoginSsh'
// import $ from "jquery";
import base_url from '../Utils/BaseUrl'

export default {
  name: 'FrameWork',
  components: {
    LoginSsh,
  },
  setup() {

    // cmd输入框
    const cmdInputRef = ref();
    // 监听cmd输入框事件
    const watchCmdInput = () => {
      
    }

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
    serverInfo.value.push(now_connect_status_tip.value);

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
      if(sshInfo.value.isRemember == true) {
        localStorage.setItem('ssh',Base64.encode(JSON.stringify(sshInfo.value)))
      }
    }
    // 连接服务器
    const cmdPrefix = ref('');
    const connectSSH = () => {
      socket.value = new WebSocket(base_url + 'websocket/' + sshInfo.value.user_name + '/' + sshInfo.value.password);
      // 当接收到服务器发送的信息时触发
      socket.value.onmessage = resp => {
        let result = JSON.parse(resp.data);
        // 连接失败
        if(result.code == -1) {
          connect_status.value = -1;
          serverInfo.value.shift();
          serverInfo.value.push(now_connect_status_tip.value);
          if(socket.value) {
            socket.value.close();
            socket.value = null;
          }
          loginSshRef.value.DialogVisilble = true;
        }
        // 显示在终端里面
        if(result.code == 1) {
          if(connect_status.value != 1) {
            serverInfo.value.shift();
            connect_status.value = 1;
            // 记住
            remember();
          }
          serverInfo.value.push(result.info);
        }
        // 命令行前缀
        if(result.code == 2) {
          cmdPrefix.value = result.info;
          cmdInputRef.value.focus();
        }
      }
    }



    onMounted(() => {
      getSSHInfo();
      // 监听cmd输入框事件
      watchCmdInput();
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
      cmdPrefix,
      cmdInputRef,
      
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
  overflow-x: scroll;
  border-top: 1px solid #d7d7d7;
  border-bottom: 1px solid #d7d7d7;
  border-left: 1px solid #d7d7d7;
  border-right: 1px solid #d7d7d7;
  color: white;
}

</style>