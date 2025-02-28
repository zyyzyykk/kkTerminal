<template>
  <div class="golbal">
    <!-- 设置栏 -->
    <div class="setting" v-show="isShowSetting && (urlParams.mode != 'headless' && urlParams.mode != 'pure')" >
      <div class="setting-menu no-select" @click="doSettings(1)" ><div>{{ $t('连接设置') }}</div></div>
      <div class="setting-menu no-select" @click="doSettings(2)" ><div>{{ $t('偏好设置') }}</div></div>
      <div class="setting-menu no-select" @click="doSettings(4)" ><div>{{ $t('文件管理') }}</div></div>
      <div @mousemove="showAdvance(true)" @mouseleave="showAdvance(false)" :class="['setting-menu', 'no-select', (sshKey && env.advance) ? '':'disabled']" @click="doSettings(5)" >
        <div style="flex: 2" ></div>
        <div>{{ $t('高级') }}</div>
        <div style="flex: 1" ></div>
        <el-icon style="margin-right: 2px" ><ArrowRight /></el-icon>
      </div>
      <div class="setting-menu no-select" @click="doSettings(3)" ><div>{{ $t('重启') }}</div></div>
    </div>
    <div @mousemove="showAdvance(true)" @mouseleave="showAdvance(false)" :class="['advance', (sshKey && env.advance) ? '':'disabled']" v-show="isShowSetting && isShowAdvance && (urlParams.mode != 'headless' && urlParams.mode != 'pure')" >
      <div class="setting-menu no-select" @click="doSettings(6)" ><div>{{ $t('协作') }}</div></div>
      <div :class="['setting-menu', 'no-select', (env.server_user === 'root') ? '':'disabled']" @click="doSettings(7)" ><div>{{ $t('监控') }}</div></div>
      <div class="setting-menu no-select" @click="doSettings(8)" ><div>Docker</div></div>
    </div>
    <div v-if="urlParams.mode != 'headless'" class="kk-flex bar">
      <div style="user-select: none;" @click="isShowSetting = !isShowSetting; isShowAdvance = false; showAdvance(false);" >
        <img src="../assets/logo.png" :alt="$t('终端')" style="height: 16px; margin: 0 7px; cursor: pointer;" >
      </div>
      <div class="ellipsis no-select" style="font-size: 14px;" ><span>kk Terminal</span></div>
      <div style="flex: 1;"></div>
      <div v-show="urlParams.mode != 'headless' && urlParams.mode != 'pure'" class="kk-flex" >
        <div v-if="cooperating" style="margin-left: 10px; cursor: pointer;" >
          <el-tag size="small" @click="endCooperateConfirm" :type="calcType(onlineNumber, maxNumber)" effect="plain" class="kk-flex no-select" ><el-icon class="el-icon--left" ><UserFilled /></el-icon>{{ onlineNumber }}</el-tag>
        </div>
        <div v-if="env.cloud" style="margin-left: 10px; cursor: pointer;" >
          <el-tag v-if="recording == false" @click="startRecord" size="small" type="info" effect="plain" class="kk-flex no-select" style="color: #313131;" ><el-icon class="el-icon--left" ><VideoPlay /></el-icon>{{ $t('开始录制') }}</el-tag>
          <el-tag v-else size="small" @click="stopRecord" type="danger" effect="plain" class="kk-flex no-select" ><el-icon class="el-icon--left" ><VideoPause /></el-icon>{{ $t('录制中') }}</el-tag>
        </div>
        <div v-if="env.cloud" style="margin-left: 10px; cursor: pointer;" >
          <el-tag @click="cloudSync" size="small" type="info" effect="plain" class="kk-flex no-select" style="color: #313131;" ><el-icon class="el-icon--left" ><MostlyCloudy /></el-icon>{{ $t('云端同步') }}</el-tag>
        </div>
        <div v-if="env.tCode" class="kk-flex" style="margin-left: 10px;" >
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
            <el-popover v-if="env.tCode" placement="bottom-end" :width="$t('220')" trigger="click" >
              <template #reference>
                <el-icon :style="{ color: '#606266' }" ><QuestionFilled /></el-icon>
              </template>
              <div v-if="env.tCode" style="font-size: 12px; color: #313131;" >
                <div style="user-select: none; font-size: 14px; font-weight: bold;" >{{ $t('什么是 TCode (终端代码) ？') }}</div>
                <div style="user-select: none; margin-top: 5px;">{{ $t('TCode（终端代码）是用于访问和执行特定操作流程的快捷方式') }}</div>
                <div style="user-select: none; margin-top: 5px;">
                  {{ $t('输入') }}
                  <span style="background-color: #f3f4f4; user-select: text;" >/H</span>
                  {{ $t('并按下回车，查看帮助信息') }}
                </div>
                <div style="user-select: none; margin-top: 5px;">
                  {{ $t('输入') }}
                  <span style="background-color: #f3f4f4; user-select: text;" >/A</span>
                  {{ $t('并按下回车，自定义TCode') }}
                </div>
              </div>
            </el-popover>
          </div>
          <div style="margin-left: 20px;" ></div>
        </div>
        <div v-else style="margin-left: 10px;" ></div>
      </div>
    </div>
    <!-- terminal主体 -->
    <div ref="terminal" class="terminal-class" ></div>
  </div>

  <!-- 连接设置 -->
  <ConnectSetting ref="connectSettingRef" :env="env" :sshOptions="options" @saveOp="saveOp" @deleteOp="deleteOp" @callback="saveEnv"></ConnectSetting>
  <!-- 样式设置 -->
  <StyleSetting ref="styleSettingRef" :env="env" @callback="saveEnv" :os="osInfo.clientOS" ></StyleSetting>
  <!-- 文件管理 -->
  <FileBlock ref="fileBlockRef" :sshKey="sshKey" :os="osInfo.clientOS" ></FileBlock>
  <!-- 用户TCode -->
  <UserTcode ref="userTcodeRef" @importTCodes="importTCodes" @exportTcodes="exportTcodes" ></UserTcode>
  <!-- 帮助TCode -->
  <HelpTcode ref="helpTcodeRef" :userTCodes="tcodes" @handleSaveTCode="handleSaveTCode" @handleDeleteTCode="handleDeleteTCode" ></HelpTcode>
  <!--协作-->
  <CooperateGen ref="cooperateGenRef" :sshKey="sshKey" :advance="env.advance" @handleCooperate="handleCooperate" ></CooperateGen>
  <!--监控-->
  <StatusMonitor ref="statusMonitorRef" :sshKey="sshKey" :advance="env.advance" ></StatusMonitor>
  <!--Docker-->
  <DockerBlock ref="dockerBlockRef" :sshKey="sshKey" :advance="env.advance" @install="installDocker" ></DockerBlock>

</template>

<script>
import useClipboard from "vue-clipboard3";
import { ref, onMounted, onUnmounted, getCurrentInstance } from 'vue';
import { encrypt, decrypt } from '@/utils/Encrypt';
import { ElMessage } from 'element-plus';

import { Terminal } from 'xterm';
import { FitAddon } from 'xterm-addon-fit';
import "xterm/css/xterm.css";

import $ from 'jquery';
import { default_env } from '@/env/Env';
import { ws_base_url } from '@/env/BaseUrl';
import { changeStr } from '@/utils/StringUtil';
import { http_base_url } from '@/env/BaseUrl';

import ConnectSetting from '@/components/ConnectSetting';
import StyleSetting from '@/components/StyleSetting';
import FileBlock from "@/components/FileBlock";
import UserTcode from '@/components/tcode/UserTcode';
import HelpTcode from "@/components/tcode/HelpTcode";
import CooperateGen from '@/components/advance/CooperateGen';
import StatusMonitor from '@/components/advance/StatusMonitor'
import DockerBlock from "@/components/advance/DockerBlock";
import { getUrlParams, getPureUrl } from '@/utils/UrlUtil';
import { QuestionFilled, VideoPlay, VideoPause, MostlyCloudy, ArrowRight, UserFilled } from '@element-plus/icons-vue';
import { FuncTcode, SysTcode, UserTcodeExecutor } from "@/components/tcode/Tcode";

import i18n from "@/locales/i18n";
import { cloud, load, syncUpload, syncDownload } from "@/utils/CloudUtil";
import { deleteDialog } from "@/utils/DeleteDialog";
import { calcType } from "@/components/calc/CalcType"

export default {
  name: "FrameWork",
  components: {
    DockerBlock,
    ConnectSetting,
    StyleSetting,
    FileBlock,
    UserTcode,
    HelpTcode,
    CooperateGen,
    StatusMonitor,
    QuestionFilled,
    VideoPlay,
    VideoPause,
    MostlyCloudy,
    ArrowRight,
    UserFilled,
  },
  setup() {

    // 连接状态
    const connect_status = ref({
      'Fail':'Fail to connect remote server !\r\n',
      'Success':'Connecting success !\r\n',
      'Connecting':'Connecting to remote server ...\r\n',
      'Disconnected':'Disconnect to remote server.\r\n',
      'End':'This Cooperation is Ended.\r\n',
    });
    const now_connect_status = ref(connect_status.value['Connecting']);

    // 获取当前组件实例
    const instance = getCurrentInstance();

    // 拷贝
    const { toClipboard } = useClipboard();

    // 终端自适应
    const fitAddon = new FitAddon();

    // 操作录像
    const recording = ref(false);
    const recordId = ref('');
    const recordInfo = ref([]);
    const startRecord = () => {
      recordId.value = crypto.randomUUID();
      recordInfo.value = [];
      recordInfo.value.push({
        time: new Date().getTime(),
        content: 'Record ' + recordId.value + ' Start.\r\n',
      });
      recording.value = true;
    };
    const stopRecord = async () => {
      recording.value = false;
      recordInfo.value.push({
        time: new Date().getTime(),
        content: '\r\nRecord ' + recordId.value + ' Over.',
      })
      await cloud('record-', recordId.value, JSON.stringify(recordInfo.value));
      await toClipboard(getPureUrl() + '?record=' + recordId.value);
      ElMessage({
        message: i18n.global.t('录像链接已复制'),
        type: 'success',
        grouping: true,
      });
    };
    const playRecord = (index) => {
      const record = recordInfo.value[index];
      if(record) termWrite(record.content);
      const nextRecord = recordInfo.value[index + 1];
      if(nextRecord) {
        setTimeout(() => {
          playRecord(index + 1);
        }, nextRecord.time - record.time);
      }
    };

    // 云端同步
    const cloudSync = () => {
      deleteDialog(i18n.global.t('提示'), i18n.global.t('确定从云端覆盖本地数据吗?'), syncDownload);
    };

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
    };
    loadTCodes();
    const env = ref(default_env);
    const urlParams = ref(getUrlParams());
    const loadEnv = () => {
      if(localStorage.getItem('env')) env.value = {...env.value, ...JSON.parse(decrypt(localStorage.getItem('env')))};
      // bg fg
      if(urlParams.value.bg && urlParams.value.bg[0] != '#') urlParams.value.bg = '#' + urlParams.value.bg;
      if(urlParams.value.fg && urlParams.value.fg[0] != '#') urlParams.value.fg = '#' + urlParams.value.fg;
      // cursorBlink tCode cloud
      if(urlParams.value.cursorBlink === 'true') urlParams.value.cursorBlink = true;
      else if(urlParams.value.cursorBlink === 'false') urlParams.value.cursorBlink = false;
      if(urlParams.value.tCode === 'true') urlParams.value.tCode = true;
      else if(urlParams.value.tCode === 'false') urlParams.value.tCode = false;
      if(urlParams.value.cloud === 'true') urlParams.value.cloud = true;
      else if(urlParams.value.cloud === 'false') urlParams.value.cloud = false;
      // url参数
      for (const key in urlParams.value) {
        if(key in env.value && key.lastIndexOf('_') == -1) env.value[key] = urlParams.value[key];
      }
      // option
      let nowOpInfo = options.value[env.value['option']];
      if(nowOpInfo) env.value = {...env.value,...nowOpInfo};
      else env.value.option = '';
      urlParams.value.option = env.value.option;
      // record
      if(urlParams.value.record) {
        if(urlParams.value.mode != 'headless' && urlParams.value.mode != 'pure') urlParams.value.mode = 'pure';
        now_connect_status.value = '';
      }
      else urlParams.value.record = '';
      // cooperate
      if(urlParams.value.cooperate) {
        if(urlParams.value.mode != 'headless' && urlParams.value.mode != 'pure') urlParams.value.mode = 'pure';
      }
      else urlParams.value.cooperate = '';
      // lang
      i18n.global.locale = env.value.lang || 'en';
    };
    loadEnv();

    // 保存更改的配置
    const saveOp = (name,item) => {
      if(name) options.value = {...options.value,[name]:item};
      localStorage.setItem('options',encrypt(JSON.stringify(options.value)));
      loadOps();
    };
    // 删除配置
    const deleteOp = (name) => {
      delete options.value[name];
      saveOp(null,null);
      if(env.value.option && env.value.option == name) saveEnv({option:''},false);
    };

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
    };

    // 终端视高自适应
    const termFit = () => {
      terminal.value.style.height = (window.innerHeight - (urlParams.value.mode != 'headless' ? 25 : 0)) + 'px';
      fitAddon.fit();
      // 修改虚拟终端行列大小
      if(socket.value && socket.value.readyState == WebSocket.OPEN && term) {
        const new_rows = fitAddon.proposeDimensions().rows;
        const new_cols = fitAddon.proposeDimensions().cols;
        socket.value.send(encrypt(JSON.stringify({type:1,content:"",rows:new_rows,cols:new_cols})));
        term.resize(new_cols,new_rows);
      }
    };
    // 终端写入
    const termWrite = (content) => {
      term.write(content);
      // 设置回滚量
      term.options.scrollback += term._core.buffer.lines.length;
    };

    // 协作
    const cooperating = ref(false);
    const onlineNumber = ref(0);
    const maxNumber = ref(0);
    const handleCooperate = (num) => {
      maxNumber.value = num;
      cooperating.value = true;
    };
    const endCooperate = () => {
      $.ajax({
        url: http_base_url + '/cooperate/end',
        type:'post',
        data:{
          sshKey:sshKey.value,
        },
        async success(resp) {
          if(resp.status == 'success') {
            maxNumber.value = 0;
            cooperating.value = false;
          }
          ElMessage({
            message: resp.info,
            type: resp.status,
          });
        }
      });
    };
    const endCooperateConfirm = () => {
      deleteDialog(i18n.global.t('提示'), i18n.global.t('确定结束此次协作吗?'), endCooperate);
    };

    // websocket连接
    const sshKey = ref('');
    const socket = ref(null);
    const doSSHConnect = () => {
      socket.value = new WebSocket(ws_base_url + changeStr(encrypt(JSON.stringify({...env.value, cooperateKey: urlParams.value.cooperate}))));
      socket.value.onopen = () => {
        termFit();
      }
      socket.value.onmessage = resp => {
        let result = JSON.parse(resp.data);
        // 协作失败
        if(result.code == -2) {
          term.clear();
          now_connect_status.value = connect_status.value['Fail'];
          termWrite(result.info + ".\n");
        }
        // 连接失败
        else if(result.code == -1) {
          term.clear();
          now_connect_status.value = connect_status.value['Fail'];
          termWrite(now_connect_status.value);
          setTimeout(() => {
            doSettings(1);
          },400);
        }
        // 连接成功
        else if(result.code == 0) {
          term.clear();
          now_connect_status.value = connect_status.value['Success'];
          setTimeout(() => {
            termFit();
          },1);
          if(urlParams.value.cooperate) {
            termWrite(result.info + ".\n");
            return;
          }
          sshKey.value = decrypt(result.data);
          if(urlParams.value.cmd) sendMessage(urlParams.value.cmd + "\n");
          if(env.value.advance && env.value.server_user === 'root' && statusMonitorRef.value) statusMonitorRef.value.doMonitor();
          if(env.value.advance && dockerBlockRef.value) dockerBlockRef.value.getDockerVersion(sshKey.value);
        }
        // 输出
        else if(result.code == 1) {
          let output = decrypt(result.data);
          if(UserTcodeExecutor.active) UserTcodeExecutor.outArray.push(output);
          if(recording.value) {
            recordInfo.value.push({
              time: new Date().getTime(),
              content: output,
            });
          }
          if(!(UserTcodeExecutor.active && !UserTcodeExecutor.display)) termWrite(output);
        }
        // 更新协作者数量
        else if(result.code == 2) {
          onlineNumber.value = Number(decrypt(result.data));
        }
      }
      socket.value.onclose = (e) => {
        if(now_connect_status.value == connect_status.value['Success'] && e.code != 3333) {
          sshKey.value = '';
          if(urlParams.value.cooperate) {
            now_connect_status.value = connect_status.value['End'];
            termWrite("\r\n" + now_connect_status.value);
            return;
          }
          closeBlock();
          now_connect_status.value = connect_status.value['Disconnected'];
          termWrite("\r\n" + now_connect_status.value);
        }
        userTcodeExecutorReset();
      }
    };

    // 终端信息设置
    const isShowSetting = ref(false);
    const isShowAdvance = ref(false);
    const showSettings = (newVal) => {
      isShowSetting.value = newVal;
      isShowAdvance.value = false;
      showAdvance(false);
    };
    let advanceTimer = null;
    const showAdvance = (newVal) => {
      if(advanceTimer) clearTimeout(advanceTimer);
      if(isShowAdvance.value != newVal) {
        advanceTimer = setTimeout(() => {
          isShowAdvance.value = newVal;
        }, 400);
      }
    };
    const connectSettingRef = ref();
    const styleSettingRef = ref();
    const fileBlockRef = ref();
    const cooperateGenRef = ref();
    const statusMonitorRef = ref();
    const dockerBlockRef = ref();
    // 保存更改的环境变量
    const saveEnv = (new_env,restart=true) => {
      let save_env = default_env;
      if(localStorage.getItem('env')) save_env = {...save_env,...JSON.parse(decrypt(localStorage.getItem('env')))};
      save_env = {...save_env,...new_env};
      localStorage.setItem('env',encrypt(JSON.stringify(save_env)));
      for (const key in new_env) {
        if(key in urlParams.value) urlParams.value[key] = new_env[key];
      }
      if(restart) doSettings(3);
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
      showSettings(false);
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

      termWrite(now_connect_status.value);
    };

    // 终端设置
    const doSettings = (type) => {
      // 连接设置
      if(type == 1) {
        showSettings(false);
        connectSettingRef.value.DialogVisible = true;
      }
      // 偏好设置
      else if (type == 2) {
        showSettings(false);
        styleSettingRef.value.DialogVisible = true;
      }
      // 重启
      else if (type == 3) {
        showSettings(false);
        now_connect_status.value = connect_status.value['Connecting'];
        sshKey.value = '';
        if(socket.value) socket.value.close(3333);  // 主动释放资源，必需
        // 进行重启
        closeBlock();
        resetTerminal();
        doSSHConnect();
      }
      // 文件管理
      else if(type == 4) {
        showSettings(false);
        fileBlockRef.value.getInitDir();
        fileBlockRef.value.DialogVisible = true;
      }
      // 高级
      else if(type == 5) {
        isShowSetting.value = true;
        isShowAdvance.value = true;
        showAdvance(true);
      }
      // 协作
      else if(type == 6) {
        showSettings(false);
        cooperateGenRef.value.DialogVisible = true;
      }
      // 监控
      else if(type == 7) {
        showSettings(false);
        statusMonitorRef.value.DialogVisible = true;
      }
      // Docker
      else if(type == 8) {
        showSettings(false);
        dockerBlockRef.value.DialogVisible = true;
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

    // 关闭相关模块
    const closeBlock = () => {
      // 文件模块
      if(fileBlockRef.value) fileBlockRef.value.deepCloseDialog();
      // 高级-协作模块
      if(cooperateGenRef.value) cooperateGenRef.value.closeDialog();
      cooperating.value = false;
      onlineNumber.value = 0;
      maxNumber.value = 0;
      // 高级-监控模块
      if(statusMonitorRef.value) statusMonitorRef.value.deepCloseDialog();
      // 高级-Docker模块
      if(dockerBlockRef.value) dockerBlockRef.value.deepCloseDialog();
    };

    const setTcodeStatus = (transTcode, state) => {
      tcodes.value[transTcode].status = state;
      localStorage.setItem('tcodes',encrypt(JSON.stringify(tcodes.value)));
      setTimeout(() => {
        helpTcodeRef.value.userTCodes = {...tcodes.value};
      },1);
    };

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
              setTcodeStatus(transTcode, 'Compile Error');
              ElMessage({
                message: 'TCode-' + transTcode + ' ' + i18n.global.t('编译错误：') + error,
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
              message: 'TCode-' + transTcode + ' ' + i18n.global.t('工作流结束'),
              type: 'success',
              grouping: true,
            });
            setTcodeStatus(transTcode, 'Execute Success');
          } catch(error) {
            ElMessage({
              message: 'TCode-' + transTcode + ' ' + i18n.global.t('执行中断：') + error,
              type: 'warning',
              grouping: true,
            });
            setTcodeStatus(transTcode, 'Execute Interrupt');
          } finally {
            userTcodeExecutorReset();
          }
        }
        else {
          ElMessage({
            message: i18n.global.t('其它TCode正在执行'),
            type: 'warning',
            grouping: true,
          });
        }
      }
      else {
        if(transTcode[0] == '/' || transTcode[0] == 'S' || transTcode[0] == 'U') {
          ElMessage({
            message: 'TCode-' + transTcode + i18n.global.t('不存在'),
            type: 'warning',
            grouping: true,
          });
        }
        else {
          ElMessage({
            message: i18n.global.t('TCode必须以 /,S,U 开头'),
            type: 'warning',
            grouping: true,
          });
        }
      }
    };
    // 重置用户TCode执行器
    const userTcodeExecutorReset = () => {
      UserTcodeExecutor.active = false;
      UserTcodeExecutor.display = true;
      UserTcodeExecutor.outArray = [];
      UserTcodeExecutor.cnt = 0;
    };
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
    };
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
    };
    const handleDeleteTCode = (name) => {
      delete tcodes.value[name];
      importTCodes({});
    };

    // 监听窗口大小变化，自动调整终端大小
    const listenResize = () => {
      window.addEventListener('resize', () => {
        if(fileBlockRef.value) {
          fileBlockRef.value.isShowMenu = false;
          fileBlockRef.value.isShowPop = false;
        }
        termFit();
      });
    };

    // 安装Docker
    const installDocker = (cmd) => {
      sendMessage(cmd);
    };

    onMounted(async () => {
      // 启动终端
      resetTerminal();

      // 录像
      if(urlParams.value.record) {
        listenResize();
        recordInfo.value = await load('record-' + urlParams.value.record);
        if(recordInfo.value) playRecord(0);
        return;
      }

      // 云端同步
      if(env.value.cloud) syncUpload();

      // 初始化
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
          // 监听窗口大小变化
          listenResize();
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
      recording,
      startRecord,
      stopRecord,
      cloudSync,
      env,
      urlParams,
      options,
      now_connect_status,
      connect_status,
      terminal,
      doSSHConnect,
      socket,
      showAdvance,
      showSettings,
      isShowSetting,
      isShowAdvance,
      doSettings,
      connectSettingRef,
      styleSettingRef,
      fileBlockRef,
      cooperateGenRef,
      statusMonitorRef,
      dockerBlockRef,
      saveEnv,
      sshKey,
      doHeartBeat,
      saveOp,
      deleteOp,
      tcode,
      handleTcode,
      closeBlock,
      resetTerminal,
      tcodes,
      userTcodeRef,
      sendMessage,
      importTCodes,
      exportTcodes,
      helpTcodeRef,
      handleSaveTCode,
      handleDeleteTCode,
      osInfo,
      cooperating,
      onlineNumber,
      maxNumber,
      handleCooperate,
      endCooperateConfirm,
      calcType,
      installDocker,
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

.kk-flex {
  display: flex;
  align-items: center;
}

.bar {
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

.advance {
  position: absolute;
  left: 84px;
  top: 100px;
  z-index: 100;
  cursor: pointer;
  border: 2px solid #f2f2f2;
  border-left: 2px solid #efefef;
}

/* 文本不可选中 */
.no-select {
  user-select: none;
}

/* 文本溢出省略 */
.ellipsis {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  line-height: 18px;
}

.disabled {
  background-color: #f5f7fa;
  color: #a8abb2;
  pointer-events: none;
}
</style>
