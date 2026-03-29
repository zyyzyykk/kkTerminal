<template>
  <div class="root-view" >
    <!-- 设置栏 -->
    <div class="setting" v-show="isShowSetting && (urlParams.mode !== 'headless' && urlParams.mode !== 'pure')" >
      <div class="setting-menu no-select" @click="doSettings(1)" ><div>{{ $t('连接设置') }}</div></div>
      <div class="setting-menu no-select" @click="doSettings(2)" ><div>{{ $t('偏好设置') }}</div></div>
      <div class="setting-menu no-select" @click="doSettings(4)" ><div>{{ $t('文件管理') }}</div></div>
      <div @mousemove="showAdvance(true)" @mouseleave="showAdvance(false)" :class="['setting-menu', 'no-select', (sshKey && env.advance) ? '':'disabled']" @click="doSettings(5)" >
        <div>{{ $t('高级') }}</div>
        <div style="flex: 1;" ></div>
        <el-icon><ArrowRight /></el-icon>
      </div>
      <div class="setting-menu no-select" @click="doSettings(3)" ><div>{{ $t('重启') }}</div></div>
    </div>
    <div @mousemove="showAdvance(true)" @mouseleave="showAdvance(false)" :class="['advance', (sshKey && env.advance) ? '':'disabled']" v-show="isShowSetting && isShowAdvance && (urlParams.mode !== 'headless' && urlParams.mode !== 'pure')" >
      <div class="setting-menu no-select" @click="doSettings(6)" ><div>{{ $t('协作') }}</div></div>
      <div class="setting-menu no-select" @click="doSettings(7)" ><div>{{ $t('监控') }}</div></div>
      <div class="setting-menu no-select" @click="doSettings(8)" ><div>Docker</div></div>
    </div>
    <div v-if="urlParams.mode !== 'headless'" class="kk-flex bar" >
      <img class="logo no-select" src="@/assets/terminal.svg" alt="terminal"
           @click="isShowSetting = !isShowSetting; isShowAdvance = false; showAdvance(false);" >
      <div class="ellipsis no-select" style="font-size: 14px; margin-right: 10px;" >kk Terminal</div>
      <div style="flex: 1;" ></div>
      <div v-show="urlParams.mode !== 'headless' && urlParams.mode !== 'pure'" class="kk-flex" >
        <div v-if="env.cloud" class="bar-tab no-select" >
          <el-icon @click="startRecord" v-if="!recording" style="font-size: 20px; color: #909399;" ><VideoPlay /></el-icon>
          <el-icon @click="stopRecord" v-else style="font-size: 20px; color: #f56c6c;" ><VideoPause /></el-icon>
        </div>
        <div v-if="env.cloud" class="bar-tab no-select" >
          <el-icon @click="cloudSync" style="font-size: 20px; color: #909399;" ><ChromeFilled /></el-icon>
        </div>
        <div v-if="env.advance" class="bar-tab no-select" >
          <el-tooltip :disabled="!cooperating" :content="onlineNumber" placement="bottom" :show-after="300" >
            <el-icon v-if="!cooperating" @click="doSettings(6)" style="font-size: 20px; color: #909399;" ><UserFilled /></el-icon>
            <el-icon v-else-if="calcType(onlineNumber, maxNumber) === 'success'" @click="doSettings(6)" style="font-size: 20px; color: #67c23a;" ><UserFilled /></el-icon>
            <el-icon v-else-if="calcType(onlineNumber, maxNumber) === 'warning'" @click="doSettings(6)" style="font-size: 20px; color: #e6a23c;" ><UserFilled /></el-icon>
            <el-icon v-else @click="doSettings(6)" style="font-size: 20px; color: #f56c6c;" ><UserFilled /></el-icon>
          </el-tooltip>
        </div>
        <div v-if="env.advance" class="bar-tab no-select" >
          <img @click="doSettings(7)" src="@/assets/monitor.svg" alt="monitor" style="height: 18px;" >
        </div>
        <div v-if="env.advance" class="bar-tab no-select" >
          <img @click="doSettings(8)" src="@/assets/docker.svg" alt="docker" style="height: 18px;" >
        </div>
        <el-dropdown v-if="env.transport" @visible-change="changeDropdownShowStatus" class="bar-tab no-select" hide-timeout="300" trigger="click" >
          <el-badge :hidden="isShowDropdown || !isShowDot" :is-dot="true" :offset="[0, 2]" >
            <span @click="isShowDot = false;" ><img src="@/assets/transport.svg" alt="transport" style="height: 18px;" ></span>
          </el-badge>
          <template #dropdown>
            <el-card class="no-select" style="width: 512px;" :body-style="{padding: '5px 5px'}" >
              <el-tabs stretch type="border-card" class="trans-tabs" >
                <el-tab-pane>
                  <template #label >
                    <el-badge :show-zero="false" :value="Object.keys(waitingList).length" :max="99" :offset="[6, 2]" type="warning" >
                      <div>{{ $t('等待中') }}</div>
                    </el-badge>
                  </template>
                  <div v-if="Object.keys(waitingList).length > 0" class="trans-items no-scrollbar" >
                    <div v-for="item in waitingList" :key="item.id" class="kk-flex trans-item" >
                      <FileIcons :name="item.name" :width="24" :height="24" :isFolder="item.size === -1" />
                      <div style="margin-left: 15px;" ></div>
                      <div class="kk-flex-column" >
                        <ToolTip :content="item.path + item.name" >
                          <template #content>
                            <div class="trans-item-name ellipsis" style="width: 360px;" >{{ item.path + item.name }}</div>
                          </template>
                        </ToolTip>
                        <div class="trans-item-size" >{{ calcSize(item.size) }}</div>
                      </div>
                      <div style="flex: 1;" ></div>
                      <el-progress class="trans-progress" width="48" type="circle" :percentage="item.progress" />
                      <div style="flex: 1;" ></div>
                    </div>
                  </div>
                  <div v-else>
                    <NoData height="256px" ></NoData>
                  </div>
                </el-tab-pane>
                <el-tab-pane>
                  <template #label >
                    <el-badge :show-zero="false" :value="Object.keys(uploadingList).length" :max="99" :offset="[6, 2]" type="danger" >
                      <div>{{ $t('正在上传') }}</div>
                    </el-badge>
                  </template>
                  <div v-if="Object.keys(uploadingList).length > 0" class="trans-items no-scrollbar" >
                    <div v-for="item in uploadingList" :key="item.id" class="kk-flex trans-item" >
                      <FileIcons :name="item.name" :width="24" :height="24" :isFolder="item.size === -1" />
                      <div style="margin-left: 15px;" ></div>
                      <div class="kk-flex-column" >
                        <ToolTip :content="item.path + item.name" >
                          <template #content>
                            <div class="trans-item-name ellipsis" style="width: 360px;" >{{ item.path + item.name }}</div>
                          </template>
                        </ToolTip>
                        <div class="trans-item-size" >{{ calcSize(item.size) }}</div>
                      </div>
                      <div style="flex: 1;" ></div>
                      <el-icon @click="fileBlockView(item.path, item.name)" class="el-icon--left folder-hover" ><FolderOpened /></el-icon>
                      <div style="margin-left: 10px;" ></div>
                      <el-icon @click="updateTransportLists(1, 1, item.id, null)" class="el-icon--left close-hover" ><CircleClose /></el-icon>
                      <div style="flex: 1;" ></div>
                    </div>
                  </div>
                  <div v-else>
                    <NoData height="256px" ></NoData>
                  </div>
                </el-tab-pane>
                <el-tab-pane>
                  <template #label >
                    <el-badge :show-zero="false" :value="Object.keys(downloadingList).length" :max="99" :offset="[6, 2]" type="danger" >
                      <div>{{ $t('正在下载') }}</div>
                    </el-badge>
                  </template>
                  <div v-if="Object.keys(downloadingList).length > 0" class="trans-items no-scrollbar" >
                    <div v-for="item in downloadingList" :key="item.id" class="kk-flex trans-item" >
                      <FileIcons :name="item.name" :width="24" :height="24" :isFolder="item.size === -1" />
                      <div style="margin-left: 15px;" ></div>
                      <div class="kk-flex-column" >
                        <ToolTip :content="item.path + item.name" >
                          <template #content>
                            <div class="trans-item-name ellipsis" style="width: 360px;" >{{ item.path + item.name }}</div>
                          </template>
                        </ToolTip>
                        <div class="trans-item-size" >{{ calcSize(item.size) }}</div>
                      </div>
                      <div style="flex: 1;" ></div>
                      <el-icon @click="fileBlockView(item.path, item.name)" class="el-icon--left folder-hover" ><FolderOpened /></el-icon>
                      <div style="margin-left: 10px;" ></div>
                      <el-icon @click="updateTransportLists(2, 1, item.id, null)" class="el-icon--left close-hover" ><CircleClose /></el-icon>
                      <div style="flex: 1;" ></div>
                    </div>
                  </div>
                  <div v-else>
                    <NoData height="256px" ></NoData>
                  </div>
                </el-tab-pane>
                <el-tab-pane>
                  <template #label >
                    <el-badge :show-zero="false" :value="Object.keys(finishedList).length" :max="99" :offset="[6, 2]" type="success" >
                      <div>{{ $t('已完成') }}</div>
                    </el-badge>
                  </template>
                  <div v-if="Object.keys(finishedList).length > 0" class="trans-items no-scrollbar" >
                    <div v-for="item in finishedList" :key="item.id" class="kk-flex trans-item" >
                      <FileIcons :name="item.name" :width="24" :height="24" :isFolder="item.size === -1" />
                      <div style="margin-left: 15px;" ></div>
                      <div class="kk-flex-column" >
                        <ToolTip :content="item.path + item.name" >
                          <template #content>
                            <div class="trans-item-name ellipsis" style="width: 360px;" >{{ item.path + item.name }}</div>
                          </template>
                        </ToolTip>
                        <div class="trans-item-size" >{{ calcSize(item.size) }}</div>
                      </div>
                      <div style="flex: 1;" ></div>
                      <el-icon v-if="item.status === -1" class="el-icon--left" >
                        <img class="custom-svg" src="@/assets/error_upload.svg" alt="error_upload" >
                      </el-icon>
                      <el-icon v-else-if="item.status === -2" class="el-icon--left" >
                        <img class="custom-svg" src="@/assets/error_download.svg" alt="error_download" >
                      </el-icon>
                      <el-icon v-else @click="fileBlockView(item.path, item.name)" class="el-icon--left folder-hover" ><FolderOpened /></el-icon>
                      <div style="margin-left: 10px;" ></div>
                      <el-icon @click="updateTransportLists(3, 1, item.id, null)" class="el-icon--left close-hover" ><CircleClose /></el-icon>
                      <div style="flex: 1;" ></div>
                    </div>
                  </div>
                  <div v-else>
                    <NoData height="256px" ></NoData>
                  </div>
                </el-tab-pane>
              </el-tabs>
            </el-card>
          </template>
        </el-dropdown>
        <div v-if="env.cmdcode" class="kk-flex" style="margin-right: 10px;" >
          <div style="margin-right: 3px;" >
            <el-input
              v-model="cmdcode"
              :style="{ width: '110px', height: '20px', fontSize: '12px'}"
              @keydown="handleCmdCode"
              maxlength="6"
              :placeholder="$t('命令代码')"
            >
            </el-input>
          </div>
          <el-popover v-if="env.cmdcode" placement="bottom-end" :width="$t('242')" trigger="click" >
            <template #reference>
              <el-icon :style="{ color: '#606266', cursor: 'pointer' }" ><QuestionFilled /></el-icon>
            </template>
            <div v-if="env.cmdcode" class="no-select" style="font-size: 12px; color: #313131;" >
              <div style="font-size: 16px; font-weight: bold;" >{{ $t('什么是命令代码？') }}</div>
              <div style="margin-top: 5px;" >{{ $t('命令代码是用于访问和执行特定操作流程的快捷方式') }}</div>
              <div class="kk-flex" style="margin-top: 5px;" >
                <div>{{ $t('输入') }}&nbsp;</div>
                <div class="cmdcode-name" >SCCC</div>
                <div>&nbsp;{{ $t('并按下回车以查看更多信息') }}</div>
              </div>
              <div class="kk-flex" style="margin-top: 5px;" >
                <div>{{ $t('输入') }}&nbsp;</div>
                <div class="cmdcode-name" >SCCW</div>
                <div>&nbsp;{{ $t('并按下回车以自定义工作流') }}</div>
              </div>
            </div>
          </el-popover>
        </div>
      </div>
    </div>
    <!-- Terminal主体 -->
    <div class="kk-flex terminal-class" :style="{backgroundColor: env.bg}" >
      <div style="margin-left: 5px;" ></div>
      <div ref="terminal" style="flex: 1;" ></div>
    </div>
  </div>

  <!-- 连接设置 -->
  <ConnectSetting ref="connectSettingRef" :env="env" :sshOptions="options" @saveOp="saveOp" @deleteOp="deleteOp" @callback="saveEnv" ></ConnectSetting>
  <!-- 偏好设置 -->
  <PreferenceSetting ref="preferenceSettingRef" :env="env" @callback="saveEnv" :os="osInfo.clientOS" ></PreferenceSetting>
  <!-- 文件管理 -->
  <FileBlock ref="fileBlockRef" :sshKey="sshKey" :os="osInfo.clientOS" @updateTransportLists="updateTransportLists" :uploadingList="uploadingList" ></FileBlock>
  <!-- 命令代码工作流 -->
  <CmdCodeWorkflow ref="cmdCodeWorkflowRef" @importCmdCodes="importCmdCodes" @exportCmdCodes="exportCmdCodes" ></CmdCodeWorkflow>
  <!-- 命令代码中心 -->
  <CmdCodeCenter ref="cmdCodeCenterRef" :userCmdCodes="cmdcodes" @handleSaveCmdCode="handleSaveCmdCode" @handleDeleteCmdCode="handleDeleteCmdCode" ></CmdCodeCenter>
  <!-- 协作 -->
  <CooperateGen ref="cooperateGenRef" :sshKey="sshKey" :advance="env.advance" @handleCooperate="handleCooperate" ></CooperateGen>
  <!-- 监控 -->
  <StatusMonitor ref="statusMonitorRef" :sshKey="sshKey" :advance="env.advance" ></StatusMonitor>
  <!-- Docker -->
  <DockerBlock ref="dockerBlockRef" :sshKey="sshKey" :advance="env.advance" @enable="enableDocker" @deploy="runContainer" ></DockerBlock>

</template>

<script>
import browser from "@/utils/Browser";
import { ref, onMounted, onUnmounted, getCurrentInstance } from "vue";
import { secretKeyGetter, aesEncrypt, aesDecrypt, rsaEncrypt } from "@/utils/Encrypt";
import { ElMessage } from "element-plus";

import { Terminal } from "@xterm/xterm";
import { FitAddon } from "@xterm/addon-fit";
import { WebLinksAddon } from "@xterm/addon-web-links";
import "@xterm/xterm/css/xterm.css";

import { request } from "@/utils/Request";
import { default_env } from "@/env/Env";
import { http_base_url, ws_base_url } from "@/env/Base";
import { changeStr, changeBase64Str, changeStrBase64, generateRandomString } from "@/utils/String";

import ConnectSetting from "@/components/connect/ConnectSetting";
import PreferenceSetting from "@/components/preference/PreferenceSetting";
import FileBlock from "@/components/file/FileBlock";
import CmdCodeWorkflow from "@/components/cmdcode/CmdCodeWorkflow";
import CmdCodeCenter from "@/components/cmdcode/CmdCodeCenter";
import CooperateGen from "@/components/advance/CooperateGen";
import StatusMonitor from "@/components/advance/StatusMonitor";
import DockerBlock from "@/components/advance/docker/DockerBlock";
import { getUrlParams, getPureUrl, doUrlDownload } from "@/utils/Url";
import {
  QuestionFilled,
  VideoPlay,
  VideoPause,
  ChromeFilled,
  ArrowRight,
  UserFilled,
  FolderOpened,
  CircleClose,
} from "@element-plus/icons-vue";
import {
  FuncCmdCode,
  SysCmdCode,
  UserCmdCodeExecutor,
  UserCmdCodeHelper,
  CmdCodeReservedVarsSetter,
  historyCmdCode,
} from "@/components/cmdcode/CmdCode";

import i18n from "@/locales/i18n";
import { cloudUpload, cloudDownload, syncUpload, syncDownload } from "@/utils/Cloud";
import { deleteDialog } from "@/components/common/DeleteDialog";
import { calcType } from "@/components/calc/CalcType";
import { calcSize } from "@/components/calc/CalcSize";
import { calcBgColor } from "@/components/calc/CalcColor";
import { getChannel, messageDict } from "@/utils/Channel";
import { localStore } from "@/env/Store";
import NoData from "@/components/common/NoData";
import ToolTip from "@/components/common/ToolTip";
import FileIcons from "file-icons-vue";
import setupCompatFixes from "@/utils/Compatibility";

export default {
  name: 'TerminalView',
  components: {
    FileIcons,
    NoData,
    ToolTip,
    DockerBlock,
    ConnectSetting,
    PreferenceSetting,
    FileBlock,
    CmdCodeWorkflow,
    CmdCodeCenter,
    CooperateGen,
    StatusMonitor,
    QuestionFilled,
    VideoPlay,
    VideoPause,
    ChromeFilled,
    ArrowRight,
    UserFilled,
    FolderOpened,
    CircleClose,
  },
  props: ['osInfo'],
  setup(props) {

    // 兼容性修复
    setupCompatFixes();

    // 浏览器窗口广播
    const channel = getChannel();
    channel.postMessage(JSON.stringify({
      message: messageDict['RESPONSE_KEY_UPDATE'],
      data: secretKeyGetter.response(),
    }));

    // 连接状态
    const connectStatusDict = ref({
      'Fail': 'Fail to connect remote server !\r\n',
      'Success': 'Connecting success !\r\n',
      'Connecting': 'Connecting to remote server ...\r\n',
      'Disconnected': 'Disconnect to remote server.\r\n',
      'End': 'This Cooperation is Ended.\r\n',
    });
    const currentConnectStatus = ref(connectStatusDict.value['Connecting']);

    // 获取当前组件实例
    const instance = getCurrentInstance();

    // 终端插件
    const fitAddon = new FitAddon();              // 宽高自适应
    const webLinksAddon = new WebLinksAddon();    // 网页链接跳转

    // 操作录像
    const recording = ref(false);
    const recordId = ref('');
    const recordInfo = ref([]);
    const startRecord = () => {
      ElMessage({
        message: i18n.global.t('操作录像开始'),
        type: 'success',
        grouping: true,
      });
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
      });
      cloudUpload('record-', recordId.value, JSON.stringify(recordInfo.value)).then(() => {
        browser.navigator.clipboard.writeText(getPureUrl() + '?record=' + recordId.value).then(() => {
          ElMessage({
            message: i18n.global.t('录像链接已复制'),
            type: 'success',
            grouping: true,
          });
        });
      });
    };
    const playRecord = (index) => {
      const record = recordInfo.value[index];
      if(record) termWrite(record.content);
      const nextRecord = recordInfo.value[index + 1];
      if(nextRecord) {
        browser.setTimeout(() => {
          playRecord(index + 1);
        }, nextRecord.time - record.time);
      }
    };

    // 多端同步提示
    const cloudSync = async () => {
      if(urlParams.value.user) deleteDialog(i18n.global.t('提示'), i18n.global.t('确定从云端覆盖本地数据吗？'), doSyncDownload);
      else {
        await syncUpload();
        const link = getPureUrl() + '?user=' + changeBase64Str(browser.localStorage.getItem(localStore['user']));
        await browser.navigator.clipboard.writeText(link);
        ElMessage({
          message: i18n.global.t('多端同步链接已复制'),
          type: 'success',
          grouping: true,
          repeatNum: Number.MIN_SAFE_INTEGER,
        });
      }
    };
    const doSyncDownload = async () => {
      await syncDownload(changeStrBase64(urlParams.value.user));
    };

    // 加载环境变量
    const options = ref({});
    const loadOps = () => {
      if(browser.localStorage.getItem(localStore['options'])) options.value = JSON.parse(aesDecrypt(browser.localStorage.getItem(localStore['options'])));
      else options.value = {};
    };
    loadOps();
    const cmdcodes = ref({});
    const loadCmdCodes = () => {
      cmdcodes.value = {};
      if(browser.localStorage.getItem(localStore['cmdcodes'])) {
        cmdcodes.value = JSON.parse(aesDecrypt(browser.localStorage.getItem(localStore['cmdcodes'])));
      }
      browser.setTimeout(() => {
        cmdCodeCenterRef.value.userCmdCodes = {...cmdcodes.value};
      }, 1);
    };
    loadCmdCodes();
    const env = ref(default_env);
    const urlParams = ref(getUrlParams());
    const loadEnv = () => {
      if(browser.localStorage.getItem(localStore['env'])) env.value = {...env.value, ...JSON.parse(aesDecrypt(browser.localStorage.getItem(localStore['env'])))};
      // login lang
      browser.localStorage.setItem(localStore['lang'], env.value.lang);
      // # bg fg
      if(urlParams.value.bg && urlParams.value.bg[0] !== '#') urlParams.value.bg = '#' + urlParams.value.bg;
      if(urlParams.value.fg && urlParams.value.fg[0] !== '#') urlParams.value.fg = '#' + urlParams.value.fg;
      // true/false cursorBlink cmdcode cloud advance transport
      if(urlParams.value.cursorBlink === 'true') urlParams.value.cursorBlink = true;
      else if(urlParams.value.cursorBlink === 'false') urlParams.value.cursorBlink = false;
      if(urlParams.value.cmdcode === 'true') urlParams.value.cmdcode = true;
      else if(urlParams.value.cmdcode === 'false') urlParams.value.cmdcode = false;
      if(urlParams.value.cloud === 'true') urlParams.value.cloud = true;
      else if(urlParams.value.cloud === 'false') urlParams.value.cloud = false;
      if(urlParams.value.advance === 'true') urlParams.value.advance = true;
      else if(urlParams.value.advance === 'false') urlParams.value.advance = false;
      if(urlParams.value.transport === 'true') urlParams.value.transport = true;
      else if(urlParams.value.transport === 'false') urlParams.value.transport = false;
      // url参数
      for (const key in urlParams.value) {
        if(key in env.value && key.lastIndexOf('_') === -1) env.value[key] = urlParams.value[key];
      }
      // option
      const currentOpInfo = options.value[env.value['option']];
      if(currentOpInfo) env.value = {...env.value, ...currentOpInfo};
      else env.value.option = '';
      urlParams.value.option = env.value.option;
      // record
      if(urlParams.value.record) {
        if(urlParams.value.mode !== 'headless' && urlParams.value.mode !== 'pure') urlParams.value.mode = 'pure';
        currentConnectStatus.value = '';
      }
      else urlParams.value.record = '';
      // cooperate
      if(urlParams.value.cooperate) {
        if(urlParams.value.mode !== 'headless' && urlParams.value.mode !== 'pure') urlParams.value.mode = 'pure';
      }
      else urlParams.value.cooperate = '';
      // lang
      if(!env.value.lang) env.value.lang = 'en';
      i18n.global.locale = env.value.lang;
      // 预留值
      CmdCodeReservedVarsSetter('option', env.value.option);
    };
    loadEnv();

    // 保存更改的配置
    const saveOp = (name, item) => {
      if(name) options.value = {...options.value, [name]: item};
      browser.localStorage.setItem(localStore['options'], aesEncrypt(JSON.stringify(options.value)));
      loadOps();
    };
    // 删除配置
    const deleteOp = (name) => {
      delete options.value[name];
      saveOp(null, null);
      if(env.value.option && env.value.option === name) saveEnv({option: ''}, false);
    };

    // 初始化终端
    const terminal = ref();
    let term = null;
    const initTerminal = () => {
      term = new Terminal({
        convertEol: true,                                     // 设置光标为下一行开头
        scrollback: Number.MAX_SAFE_INTEGER,                  // 终端回滚量
        disableStdin: false,                                  // 是否禁用输入
        cursorStyle: env.value.cursorStyle,                   // 光标样式(默认block)
        cursorInactiveStyle: env.value.cursorStyle === 'block' ? 'outline' : env.value.cursorStyle,   // 光标样式(失去焦点)
        cursorBlink: env.value.cursorBlink,                   // 光标是否闪烁
        theme: {
          foreground: env.value.fg,                           // 终端前景色
          background: env.value.bg,                           // 终端背景色
          cursor: env.value.fg,                               // 光标颜色
          selectionBackground: calcBgColor(env.value.bg),     // 选中文本背景色
        },
        lineHeight: 1.2,                                      // 文本行高
        fontFamily: env.value.fontFamily,                     // 字体(默认Courier New)
        fontSize: env.value.fontSize,                         // 字号(默认16)
      });
      term.loadAddon(fitAddon);
      term.loadAddon(webLinksAddon);
    };

    // 终端窗口自适应
    const termFit = () => {
      terminal.value.style.width = window.innerWidth + 'px';
      terminal.value.style.height = (window.innerHeight - (urlParams.value.mode !== 'headless' ? 25 : 0)) + 'px';
      const newCols = fitAddon.proposeDimensions().cols;
      const newRows = fitAddon.proposeDimensions().rows;
      term.resize(newCols, newRows);
      // 同步修改虚拟终端窗口大小
      if(socket.value && socket.value.readyState === WebSocket.OPEN) {
        socket.value.send(aesEncrypt(JSON.stringify({type: 1, content: "", rows: newRows, cols: newCols}), secretKey.value));
      }
    };
    // 终端写入
    const termWrite = (content) => {
      if(content) {
        term.focus();
        term.write(content);
      }
    };

    // 协作
    const cooperating = ref(false);
    const onlineNumber = ref(0);
    const maxNumber = ref(0);
    const handleCooperate = (num) => {
      onlineNumber.value = 0;
      maxNumber.value = num;
      cooperating.value = true;
    };
    const endCooperate = () => {
      request({
        url: http_base_url + '/advance/cooperate/end',
        type: 'post',
        data: {
          sshKey: sshKey.value,
        },
        success(resp) {
          if(resp.status === 'success') {
            onlineNumber.value = 0;
            maxNumber.value = 0;
            cooperating.value = false;
          }
          ElMessage({
            message: resp.info,
            type: resp.status,
            grouping: true,
          });
        }
      });
    };
    const endCooperateConfirm = () => {
      deleteDialog(i18n.global.t('提示'), i18n.global.t('确定结束此次协作吗？'), endCooperate);
    };

    // websocket连接
    const sshKey = ref('');
    const secretKey = ref('');
    const socket = ref(null);
    const doSSHConnect = () => {
      // ws密钥
      secretKey.value = generateRandomString(16);
      // ws连接信息
      const wsInfo = {};
      wsInfo.secretKey = rsaEncrypt(secretKey.value);
      wsInfo.envInfo = aesEncrypt(JSON.stringify({
        ...env.value,
        cooperateKey: urlParams.value.cooperate,
        cols: term.cols,
        rows: term.rows,
      }), secretKey.value);
      // ws连接
      socket.value = new WebSocket(ws_base_url + changeStr(aesEncrypt(JSON.stringify(wsInfo), secretKeyGetter.socket())));
      // 接收消息
      socket.value.onmessage = resp => {
        const result = JSON.parse(resp.data);
        // 协作失败
        if(result.code === -2) {
          term.clear();
          currentConnectStatus.value = connectStatusDict.value['Fail'];
          termWrite(result.info + ".\n");
        }
        // 连接失败
        else if(result.code === -1) {
          term.clear();
          currentConnectStatus.value = connectStatusDict.value['Fail'];
          termWrite(currentConnectStatus.value);
          browser.setTimeout(() => {
            doSettings(1);
          }, 400);
        }
        // 连接成功
        else if(result.code === 0) {
          term.clear();
          currentConnectStatus.value = connectStatusDict.value['Success'];
          termFit();
          // 协作成功
          if(urlParams.value.cooperate) {
            termWrite(result.info + ".\n");
            return;
          }
          sshKey.value = aesDecrypt(result.data, secretKey.value);
          if(urlParams.value.cmd) {
            // 执行bash命令
            if(urlParams.value.cmd.toLowerCase().startsWith('bash:')) sendMessage(urlParams.value.cmd.substring(5) + "\n");
            // 执行命令代码
            else if(urlParams.value.cmd.toLowerCase().startsWith('code:')) {
              browser.setTimeout(() => {
                cmdcode.value = urlParams.value.cmd.substring(5);
                handleCmdCode({key: 'Enter'});
              }, 400);
            }
          }
          if(env.value.advance && statusMonitorRef.value) statusMonitorRef.value.doMonitor();
          if(env.value.advance && dockerBlockRef.value) dockerBlockRef.value.getDockerVersion(sshKey.value);
        }
        // 输出
        else if(result.code === 1) {
          const output = aesDecrypt(result.data, secretKey.value);
          if(UserCmdCodeHelper.active) UserCmdCodeHelper.outArray.push(output);
          if(recording.value) {
            recordInfo.value.push({
              time: new Date().getTime(),
              content: output,
            });
          }
          if(!(UserCmdCodeHelper.active && !UserCmdCodeHelper.display)) termWrite(output);
        }
        // 更新协作者数量
        else if(result.code === 2) {
          onlineNumber.value = Number(aesDecrypt(result.data, secretKey.value));
        }
        // 更新文件传输列表
        else if(result.code === 3) {
          const fileTransInfo = JSON.parse(aesDecrypt(result.data, secretKey.value));
          const index = parseInt(fileTransInfo.index);
          const id = fileTransInfo.id;
          // 已完成
          if(index === 3) {
            updateTransportLists(1, 1, id, null);
            updateTransportLists(2, 1, id, null);
            updateTransportLists(3, 0, id, fileTransInfo);
          }
          // 正在上传/正在下载
          else {
            updateTransportLists(0, 1, id, null);
            updateTransportLists(index, 0, id, fileTransInfo);
          }
        }
      };
      // 断开连接
      socket.value.onclose = (e) => {
        if(currentConnectStatus.value === connectStatusDict.value['Success'] && e.code !== 3333) {
          sshKey.value = '';
          if(urlParams.value.cooperate) {
            currentConnectStatus.value = connectStatusDict.value['End'];
            termWrite("\r\n" + currentConnectStatus.value);
            return;
          }
          closeBlock();
          currentConnectStatus.value = connectStatusDict.value['Disconnected'];
          termWrite("\r\n" + currentConnectStatus.value);
        }
        UserCmdCodeHelper.reset();
      };
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
      clearTimeout(advanceTimer);
      if(isShowAdvance.value !== newVal) {
        advanceTimer = browser.setTimeout(() => {
          isShowAdvance.value = newVal;
        }, 400);
      }
    };
    const connectSettingRef = ref();
    const preferenceSettingRef = ref();
    const fileBlockRef = ref();
    const cooperateGenRef = ref();
    const statusMonitorRef = ref();
    const dockerBlockRef = ref();
    // 保存更改的环境变量
    const saveEnv = (new_env, restart=true) => {
      let save_env = default_env;
      if(browser.localStorage.getItem(localStore['env'])) save_env = {...save_env, ...JSON.parse(aesDecrypt(browser.localStorage.getItem(localStore['env'])))};
      save_env = {...save_env, ...new_env};
      browser.localStorage.setItem(localStore['env'], aesEncrypt(JSON.stringify(save_env)));
      for (const key in new_env) {
        if(key in urlParams.value) urlParams.value[key] = new_env[key];
      }
      if(restart) doSettings(3);
    };

    // 文本消息发送
    const sendMessage = (text, active=false) => {
      if(socket.value) {
        // 禁止在执行命令代码工作流的过程中进行人为输入
        if(UserCmdCodeHelper.active === active) {
          socket.value.send(aesEncrypt(JSON.stringify({type: 0, content: text, rows: 0, cols: 0}), secretKey.value));
        }
        else {
          ElMessage({
            message: i18n.global.t('命令代码') + ' ' + UserCmdCodeHelper.name + ' ' + i18n.global.t('正在执行'),
            type: 'warning',
            grouping: true,
            repeatNum: Number.MIN_SAFE_INTEGER,
          });
        }
      }
    };

    // 中文
    const putChinese = (event) => {
      event.preventDefault();
      event.stopPropagation();
      sendMessage(event.target.value);
      event.target.value = '';
    };

    // 右键事件函数
    const doPaste = async (event) => {
      event.preventDefault();   // 阻止默认的上下文菜单弹出
      event.stopPropagation();
      const pasteText = await browser.navigator.clipboard.readText();
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
      loadEnv();
      initTerminal();
      term.open(terminal.value);
      termFit();

      // 支持中文输入
      terminal.value.addEventListener('compositionend', putChinese);

      // 全部输入
      term.onData(data => {
        sendMessage(data);
      });

      // 监听选中文本，自动复制
      term.onSelectionChange(async () => {
        if (term.hasSelection()) {
          const copyText = term.getSelection();
          if(copyText.trim()) await browser.navigator.clipboard.writeText(copyText);
        }
      });

      // 右键进行粘贴
      terminal.value.addEventListener('contextmenu', doPaste);

      // 左键单击
      terminal.value.addEventListener('click', doClick);

      termWrite(currentConnectStatus.value);
    };

    // 终端设置
    const doSettings = (type) => {
      // 连接设置
      if(type === 1) {
        showSettings(false);
        connectSettingRef.value.DialogVisible = true;
      }
      // 偏好设置
      else if (type === 2) {
        showSettings(false);
        preferenceSettingRef.value.DialogVisible = true;
      }
      // 重启
      else if (type === 3) {
        showSettings(false);
        currentConnectStatus.value = connectStatusDict.value['Connecting'];
        sshKey.value = '';
        if(socket.value) socket.value.close(3333);  // 主动释放资源，必需
        // 进行重启
        closeBlock();
        resetTerminal();
        doSSHConnect();
      }
      // 文件管理
      else if(type === 4) {
        showSettings(false);
        fileBlockRef.value.getHomeDir();
        fileBlockRef.value.DialogVisible = true;
      }
      // 高级
      else if(type === 5) {
        isShowSetting.value = true;
        isShowAdvance.value = true;
        showAdvance(true);
      }
      // 协作
      else if(type === 6) {
        if(!(sshKey.value && env.value.advance)) return;
        showSettings(false);
        if(!cooperating.value) cooperateGenRef.value.DialogVisible = true;
        else endCooperateConfirm();
      }
      // 监控
      else if(type === 7) {
        if(!(sshKey.value && env.value.advance)) return;
        showSettings(false);
        browser.setTimeout(() => {
          statusMonitorRef.value.updateNetworkData();
          statusMonitorRef.value.updateDiskData();
        }, 1);
        statusMonitorRef.value.DialogVisible = true;
      }
      // Docker
      else if(type === 8) {
        if(!(sshKey.value && env.value.advance)) return;
        showSettings(false);
        dockerBlockRef.value.getDockerVersion(sshKey.value);
        dockerBlockRef.value.DialogVisible = true;
      }
    };

    // websocket心跳续约 (25秒)
    let timer = null;
    const doHeartBeat = () => {
      if(!timer) {
        timer = browser.setInterval(() => {
          if(socket.value && socket.value.readyState === WebSocket.OPEN) {
            socket.value.send(aesEncrypt(JSON.stringify({type: 2, content: "", rows: 0, cols: 0}), secretKey.value));
          }
          // 本地运行
          if(props.osInfo.serverOS !== "Linux") {
            request({
              url: http_base_url + '/system/beat',
              type: 'post',
              data: {
                windowId: props.osInfo.windowId,
              },
              success() {
              }
            });
          }
        }, 25000);
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
      // 传输列表
      resetTransportLists();
    };

    const setCmdCodeStatus = (transCmdCode, state) => {
      cmdcodes.value[transCmdCode].status = state;
      browser.localStorage.setItem(localStore['cmdcodes'], aesEncrypt(JSON.stringify(cmdcodes.value)));
      browser.setTimeout(() => {
        cmdCodeCenterRef.value.userCmdCodes = {...cmdcodes.value};
      }, 1);
    };

    // 处理命令代码
    const cmdcode = ref('');
    const handleCmdCode = async (event) => {
      // 历史命令代码
      if(event.key === 'ArrowUp' || event.key === 'ArrowDown') {
        event.preventDefault();
        event.stopPropagation();
        if(event.key === 'ArrowUp') cmdcode.value = historyCmdCode.up(cmdcode.value);
        else cmdcode.value = historyCmdCode.down(cmdcode.value);
        return;
      }
      if(event.key !== 'Enter') return;
      if(!cmdcode.value || cmdcode.value.length < 2) return;
      const transCmdCode = cmdcode.value.toUpperCase();
      cmdcode.value = '';
      historyCmdCode.add(transCmdCode);
      // 执行命令代码工作流
      // 功能命令代码
      if(transCmdCode[0] === 'F' && FuncCmdCode[transCmdCode]) FuncCmdCode[transCmdCode].execFlow(instance);
      // 系统命令代码
      else if(transCmdCode[0] === 'S' && SysCmdCode[transCmdCode]) SysCmdCode[transCmdCode].execFlow(instance);
      // 用户命令代码
      else if(transCmdCode[0] === 'U' && cmdcodes.value[transCmdCode]) {
        if(!UserCmdCodeHelper.writeNoAwait) UserCmdCodeHelper.writeNoAwait = sendMessage;
        if(!UserCmdCodeHelper.fileBlockRef) UserCmdCodeHelper.fileBlockRef = fileBlockRef.value;
        // 当前未执行任何命令代码工作流
        if(!UserCmdCodeHelper.active) {
          UserCmdCodeHelper.reset();
          UserCmdCodeHelper.name = transCmdCode;
          UserCmdCodeHelper.active = true;
          // 编译: 工作流 => 执行流
          if(!cmdcodes.value[transCmdCode].execFlow || !(cmdcodes.value[transCmdCode].execFlow instanceof Function)) {
            const textFlow = cmdcodes.value[transCmdCode].workflow.toString();
            try {
              cmdcodes.value[transCmdCode].execFlow = new Function('kkTerminal', `return (async () => { ${textFlow} })()`);
            } catch (error) {
              setCmdCodeStatus(transCmdCode, 'Compile Error');
              ElMessage({
                message: i18n.global.t('命令代码') + ' ' + transCmdCode + ' ' + i18n.global.t('编译错误：') + error,
                type: 'error',
                grouping: true,
              });
              UserCmdCodeHelper.reset();
              return;
            }
          }
          // 执行命令代码工作流
          try {
            ElMessage({
              message: i18n.global.t('命令代码') + ' ' + transCmdCode + ' ' + i18n.global.t('工作流开始'),
              type: 'success',
              grouping: true,
            });
            await cmdcodes.value[transCmdCode].execFlow(UserCmdCodeExecutor);
            ElMessage({
              message: i18n.global.t('命令代码') + ' ' + transCmdCode + ' ' + i18n.global.t('工作流结束'),
              type: 'success',
              grouping: true,
            });
            setCmdCodeStatus(transCmdCode, 'Execute Success');
          } catch(error) {
            ElMessage({
              message: i18n.global.t('命令代码') + ' ' + transCmdCode + ' ' + i18n.global.t('执行中断：') + error,
              type: 'warning',
              grouping: true,
            });
            setCmdCodeStatus(transCmdCode, 'Execute Interrupt');
          } finally {
            UserCmdCodeHelper.reset();
          }
        }
        else {
          ElMessage({
            message: i18n.global.t('命令代码') + ' ' + UserCmdCodeHelper.name + ' ' + i18n.global.t('正在执行'),
            type: 'warning',
            grouping: true,
            repeatNum: Number.MIN_SAFE_INTEGER,
          });
        }
      }
      // 错误命令代码
      else {
        if(transCmdCode[0] === 'F' || transCmdCode[0] === 'S' || transCmdCode[0] === 'U') {
          ElMessage({
            message: i18n.global.t('命令代码') + ' ' + transCmdCode + ' ' + i18n.global.t('不存在'),
            type: 'warning',
            grouping: true,
          });
        }
        else {
          ElMessage({
            message: i18n.global.t('命令代码必须以F，S，U开头'),
            type: 'warning',
            grouping: true,
          });
        }
      }
    };
    const cmdCodeWorkflowRef = ref();
    // 批量导入CmdCode
    const importCmdCodes = (data) => {
      const cmdCodeData = {...cmdcodes.value, ...data};
      browser.localStorage.setItem(localStore['cmdcodes'], aesEncrypt(JSON.stringify(cmdCodeData)));
      loadCmdCodes();
    };
    // 批量导出命令代码
    const exportCmdCodes = () => {
      let content = {};
      if(browser.localStorage.getItem(localStore['cmdcodes'])) content = JSON.parse(aesDecrypt(browser.localStorage.getItem(localStore['cmdcodes'])));
      // 创建 Blob 对象
      const blob = new Blob([JSON.stringify(content, null, 4)], { type: 'text/plain' });
      // 创建指向 Blob 的 URL
      const url = URL.createObjectURL(blob);
      doUrlDownload(url, 'CommandCodes.json');
      // 释放 URL 对象
      URL.revokeObjectURL(url);
    };
    // 命令代码中心
    const cmdCodeCenterRef = ref();
    const handleSaveCmdCode = (name, content) => {
      const data = {};
      data[name] = {
        desc: cmdcodes.value[name].desc || '',
        workflow: content || '',
        status: 'Not Active',
      };
      importCmdCodes(data);
    };
    const handleDeleteCmdCode = (name) => {
      delete cmdcodes.value[name];
      importCmdCodes({});
    };

    // 监听窗口大小变化，自动调整终端大小
    const listenResize = () => {
      browser.addEventListener('resize', () => {
        if(fileBlockRef.value) {
          fileBlockRef.value.isShowMenu = false;
          fileBlockRef.value.isShowPop = false;
        }
        termFit();
      });
    };

    // 安装/授权Docker
    const enableDocker = (cmd) => {
      sendMessage(cmd);
    };
    // 部署容器
    const runContainer = (cmd) => {
      sendMessage(cmd);
    };

    // 传输列表
    const waitingList = ref({});
    const uploadingList = ref({});
    const downloadingList = ref({});
    const finishedList = ref({});
    const transportLists = {
      0: waitingList,
      1: uploadingList,
      2: downloadingList,
      3: finishedList
    };
    const resetTransportLists = () => {
      waitingList.value = {};
      uploadingList.value = {};
      downloadingList.value = {};
      finishedList.value = {};
      isShowDot.value = false;
    };
    // type: 0增/改 1删 2查
    const updateTransportLists = (index, type, id, item) => {
      const operateList = transportLists[index];
      // 查询
      if(type === 2) return operateList.value[id];
      // 删除
      if(type === 1) {
        delete operateList.value[id];
        isShowDot.value = true;
      }
      // 新增/修改
      if(type === 0) {
        // 新增
        if(!operateList.value[id]) {
          operateList.value = {...{[id]: item}, ...operateList.value};
          isShowDot.value = true;
        }
        // 修改
        else operateList.value[id] = {...item};
      }
    };
    const isShowDot = ref(false);
    const isShowDropdown = ref(false);
    const changeDropdownShowStatus = (status) => {
      isShowDropdown.value = status;
      isShowDot.value = false;
    };
    const fileBlockView = async (path, name) => {
      await fileBlockRef.value.fileBlockView(path, name);
    };

    onMounted(async () => {
      // 启动终端
      resetTerminal();
      // 监听窗口大小变化
      listenResize();

      // 录像
      if(urlParams.value.record) {
        recordInfo.value = await cloudDownload('record-' + urlParams.value.record);
        if(recordInfo.value) playRecord(0);
        else termWrite('Record ID is Invalid.\r\n');
        return;
      }

      // 连接服务器
      doSSHConnect();
      // 心跳
      doHeartBeat();
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
      currentConnectStatus,
      connectStatusDict,
      terminal,
      doSSHConnect,
      socket,
      showAdvance,
      showSettings,
      isShowSetting,
      isShowAdvance,
      doSettings,
      connectSettingRef,
      preferenceSettingRef,
      fileBlockRef,
      cooperateGenRef,
      statusMonitorRef,
      dockerBlockRef,
      saveEnv,
      sshKey,
      doHeartBeat,
      saveOp,
      deleteOp,
      cmdcode,
      cmdcodes,
      cmdCodeWorkflowRef,
      handleCmdCode,
      importCmdCodes,
      exportCmdCodes,
      cmdCodeCenterRef,
      handleSaveCmdCode,
      handleDeleteCmdCode,
      closeBlock,
      resetTerminal,
      sendMessage,
      cooperating,
      onlineNumber,
      maxNumber,
      handleCooperate,
      endCooperateConfirm,
      calcType,
      calcSize,
      enableDocker,
      runContainer,
      waitingList,
      uploadingList,
      downloadingList,
      finishedList,
      updateTransportLists,
      isShowDot,
      isShowDropdown,
      changeDropdownShowStatus,
      fileBlockView,
    }

  }
}
</script>


<style scoped>
.root-view {
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

.kk-flex-column {
  display: flex;
  flex-direction: column;
  align-items: start;
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

.bar-tab {
  margin-right: 10px;
  cursor: pointer;
  display: inline-flex;
}

.logo {
  height: 25px;
  padding: 4.5px 0;
  margin: 0 7px;
  cursor: pointer;
}

.terminal-class {
  width: 100%;
  height: 100%;
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
  justify-content: start;
  background-color: #f2f2f2;
  padding-left: 8px;
  width: 84px;
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
  left: 88px;
  top: 100px;
  z-index: 100;
  cursor: pointer;
  border: 2px solid #f2f2f2;
  border-left: 2px solid #efefef;
}

.disabled {
  pointer-events: none;
  color: #a8abb2;
  background-color: #f5f7fa;
}

.cmdcode-name {
  background-color: #f3f4f4;
  user-select: text;
}

.trans-items {
  height: 256px;
  overflow-y: scroll;
}

.trans-item {
  height: 64px;
  width: 100%;
  padding: 10px 0 10px 15px;
  border-bottom: 1px solid #efefef;
}

.trans-item:hover {
  background-color: #f3f3f3;
}

.folder-hover {
  font-size: 20px;
  cursor: pointer;
  color: #606266;
}

.folder-hover:hover {
  color: #409eff;
}

.close-hover {
  font-size: 20px;
  cursor: pointer;
  color: #606266;
}

.close-hover:hover {
  color: #F56C6C;
}

.trans-item-name {
  color: #4f4f4f;
  font-weight: bold;
  font-size: 14px;
}

.trans-item-size {
  color: #474747;
  font-size: 12px;
}

.custom-svg {
  height: 20px !important;
  width: 20px !important;
}
</style>

<style>
.trans-tabs .el-tabs__content {
  padding: 0 0 !important;
}

.trans-progress .el-progress__text {
  font-size: 12px !important;
}
</style>
