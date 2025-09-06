<template>
  <div class="global" >
    <!-- 设置栏 -->
    <div class="setting" v-show="isShowSetting && (urlParams.mode !== 'headless' && urlParams.mode !== 'pure')" >
      <div class="setting-menu no-select" @click="doSettings(1)" ><div>{{ $t('连接设置') }}</div></div>
      <div class="setting-menu no-select" @click="doSettings(2)" ><div>{{ $t('偏好设置') }}</div></div>
      <div class="setting-menu no-select" @click="doSettings(4)" ><div>{{ $t('文件管理') }}</div></div>
      <div @mousemove="showAdvance(true)" @mouseleave="showAdvance(false)" :class="['setting-menu', 'no-select', (sshKey && env.advance) ? '':'disabled']" @click="doSettings(5)" >
        <div>{{ $t('高级') }}</div>
        <div style="flex: 1" ></div>
        <el-icon><ArrowRight /></el-icon>
      </div>
      <div class="setting-menu no-select" @click="doSettings(3)" ><div>{{ $t('重启') }}</div></div>
    </div>
    <div @mousemove="showAdvance(true)" @mouseleave="showAdvance(false)" :class="['advance', (sshKey && env.advance) ? '':'disabled']" v-show="isShowSetting && isShowAdvance && (urlParams.mode !== 'headless' && urlParams.mode !== 'pure')" >
      <div class="setting-menu no-select" @click="doSettings(6)" ><div>{{ $t('协作') }}</div></div>
      <div :class="['setting-menu', 'no-select', (env.server_user === 'root') ? '':'disabled']" @click="doSettings(7)" ><div>{{ $t('监控') }}</div></div>
      <div class="setting-menu no-select" @click="doSettings(8)" ><div>Docker</div></div>
    </div>
    <div v-if="urlParams.mode !== 'headless'" class="kk-flex bar" >
      <div class="no-select" @click="isShowSetting = !isShowSetting; isShowAdvance = false; showAdvance(false);" >
        <img src="@/assets/terminal.svg" alt="terminal" style="height: 16px; margin: 0 7px; cursor: pointer;" >
      </div>
      <div class="ellipsis no-select" style="font-size: 14px; line-height: 18px;" ><span>kk Terminal</span></div>
      <div style="flex: 1;" ></div>
      <div v-show="urlParams.mode !== 'headless' && urlParams.mode !== 'pure'" class="kk-flex" >
        <div v-if="cooperating" class="bar-tag" >
          <el-tag size="small" @click="endCooperateConfirm" :type="calcType(onlineNumber, maxNumber)" effect="plain" >
            <div class="kk-flex no-select" >
              <el-icon class="el-icon--left" style="font-size: 18px;" ><UserFilled /></el-icon>
              <div>{{ onlineNumber }}</div>
            </div>
          </el-tag>
        </div>
        <div v-if="env.cloud" class="bar-tag" >
          <el-tag @click="recording ? stopRecord() : startRecord()" size="small" :type="recording ? 'danger' : 'info'" effect="plain" >
            <div v-if="!recording" class="kk-flex no-select" style="color: #313131;" >
              <el-icon class="el-icon--left" style="font-size: 18px;" ><VideoPlay /></el-icon>
              <div>{{ $t('开始录制') }}</div>
            </div>
            <div v-else class="kk-flex no-select" >
              <el-icon class="el-icon--left" style="font-size: 18px;" ><VideoPause /></el-icon>
              <div>{{ $t('录制中') }}</div>
            </div>
          </el-tag>
        </div>
        <div v-if="env.cloud" class="bar-tag" >
          <el-tag @click="cloudSync" size="small" type="info" effect="plain" >
            <div class="kk-flex no-select" style="color: #313131;" >
              <el-icon class="el-icon--left" style="font-size: 18px;" ><ChromeFilled /></el-icon>
              <div>{{ $t('多端同步') }}</div>
            </div>
          </el-tag>
        </div>
        <el-dropdown v-if="env.transport" @visible-change="changeDropdownShowStatus" class="bar-tag" hide-timeout="300" trigger="click" >
          <el-badge :hidden="isShowDropdown || !isShowDot" :is-dot="true" :offset="[0, 4]" >
            <span @click="isShowDot = false;" ><img src="@/assets/transport.svg" alt="transport" style="height: 20px;" ></span>
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
                  <div v-if="Object.keys(waitingList).length > 0" class="trans-items" >
                    <div v-for="item in waitingList" :key="item.id" class="kk-flex trans-item" style="height: 64px; width: 512px;" >
                      <FileIcons :name="item.name" :width="24" :height="24" :isFolder="item.size == -1" />
                      <div style="margin-left: 15px;" ></div>
                      <div class="kk-flex-column" >
                        <ToolTip :content="item.path + item.name" :delay="1000" >
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
                  <div v-if="Object.keys(uploadingList).length > 0" class="trans-items" >
                    <div v-for="item in uploadingList" :key="item.id" class="kk-flex trans-item" style="height: 64px; width: 512px;" >
                      <FileIcons :name="item.name" :width="24" :height="24" :isFolder="item.size == -1" />
                      <div style="margin-left: 15px;" ></div>
                      <div class="kk-flex-column" >
                        <ToolTip :content="item.path + item.name" :delay="1000" >
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
                  <div v-if="Object.keys(downloadingList).length > 0" class="trans-items" >
                    <div v-for="item in downloadingList" :key="item.id" class="kk-flex trans-item" style="height: 64px; width: 512px;" >
                      <FileIcons :name="item.name" :width="24" :height="24" :isFolder="item.size == -1" />
                      <div style="margin-left: 15px;" ></div>
                      <div class="kk-flex-column" >
                        <ToolTip :content="item.path + item.name" :delay="1000" >
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
                  <div v-if="Object.keys(finishedList).length > 0" class="trans-items" >
                    <div v-for="item in finishedList" :key="item.id" class="kk-flex trans-item" style="height: 64px; width: 512px;" >
                      <FileIcons :name="item.name" :width="24" :height="24" :isFolder="item.size == -1" />
                      <div style="margin-left: 15px;" ></div>
                      <div class="kk-flex-column" >
                        <ToolTip :content="item.path + item.name" :delay="1000" >
                          <template #content>
                            <div class="trans-item-name ellipsis" style="width: 360px;" >{{ item.path + item.name }}</div>
                          </template>
                        </ToolTip>
                        <div class="trans-item-size" >{{ calcSize(item.size) }}</div>
                      </div>
                      <div style="flex: 1;" ></div>
                      <el-icon @click="fileBlockView(item.path, item.name)" class="el-icon--left folder-hover" ><FolderOpened /></el-icon>
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
        <div v-if="env.tCode" class="kk-flex" style="margin-right: 10px;" >
          <div style="margin-right: 3px;" >
            <el-input
              v-model="tcode"
              :style="{ width: '100px', height: '20px', fontSize: '12px'}"
              @keydown="handleTCode"
              maxlength="6"
              :placeholder="$t('终端代码')"
            >
            </el-input>
          </div>
          <el-popover v-if="env.tCode" placement="bottom-end" :width="$t('232')" trigger="click" >
            <template #reference>
              <el-icon :style="{ color: '#606266', cursor: 'pointer' }" ><QuestionFilled /></el-icon>
            </template>
            <div v-if="env.tCode" class="no-select" style="font-size: 12px; color: #313131;" >
              <div style="font-size: 16px; font-weight: bold;" >{{ $t('什么是终端代码？') }}</div>
              <div style="margin-top: 5px;" >{{ $t('终端代码是用于访问和执行特定操作流程的快捷方式') }}</div>
              <div class="kk-flex" style="margin-top: 5px;" >
                <div>{{ $t('输入') }}&nbsp;</div>
                <div style="background-color: #f3f4f4; user-select: text;" >STC</div>
                <div>&nbsp;{{ $t('并按下回车以查看更多信息') }}</div>
              </div>
              <div class="kk-flex" style="margin-top: 5px;" >
                <div>{{ $t('输入') }}&nbsp;</div>
                <div style="background-color: #f3f4f4; user-select: text;" >STW</div>
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
  <!-- 终端代码工作流 -->
  <TCodeWorkflow ref="tCodeWorkflowRef" @importTCodes="importTCodes" @exportTCodes="exportTCodes" ></TCodeWorkflow>
  <!-- 终端代码中心 -->
  <TCodeCenter ref="tCodeCenterRef" :userTCodes="tcodes" @handleSaveTCode="handleSaveTCode" @handleDeleteTCode="handleDeleteTCode" ></TCodeCenter>
  <!-- 协作 -->
  <CooperateGen ref="cooperateGenRef" :sshKey="sshKey" :advance="env.advance" @handleCooperate="handleCooperate" ></CooperateGen>
  <!-- 监控 -->
  <StatusMonitor ref="statusMonitorRef" :sshKey="sshKey" :advance="env.advance" ></StatusMonitor>
  <!-- Docker -->
  <DockerBlock ref="dockerBlockRef" :sshKey="sshKey" :advance="env.advance" @install="installDocker" @deploy="runContainer" ></DockerBlock>

</template>

<script>
import useClipboard from "vue-clipboard3";
import { ref, onMounted, onUnmounted, getCurrentInstance } from 'vue';
import { aesEncrypt, aesDecrypt, rsaEncrypt } from '@/utils/Encrypt';
import { ElMessage } from 'element-plus';

import { Terminal } from 'xterm';
import { FitAddon } from 'xterm-addon-fit';
import { WebLinksAddon } from '@xterm/addon-web-links';
import "xterm/css/xterm.css";

import $ from 'jquery';
import { default_env } from '@/env/Env';
import { ws_base_url } from '@/env/BaseUrl';
import { changeStr, changeBase64Str, changeStrBase64, generateRandomString } from '@/utils/StringUtil';
import { http_base_url } from '@/env/BaseUrl';

import ConnectSetting from '@/components/connect/ConnectSetting';
import PreferenceSetting from '@/components/PreferenceSetting';
import FileBlock from "@/components/file/FileBlock";
import TCodeWorkflow from '@/components/tcode/TCodeWorkflow';
import TCodeCenter from "@/components/tcode/TCodeCenter";
import CooperateGen from '@/components/advance/CooperateGen';
import StatusMonitor from '@/components/advance/StatusMonitor'
import DockerBlock from "@/components/advance/DockerBlock";
import { getUrlParams, getPureUrl } from '@/utils/UrlUtil';
import { QuestionFilled, VideoPlay, VideoPause, ChromeFilled, ArrowRight, UserFilled, FolderOpened, CircleClose } from '@element-plus/icons-vue';
import {
  FuncTCode,
  SysTCode,
  UserTCodeExecutor,
  UserTCodeHelper,
  TCodeReservedVarsSetter,
  historyTCode,
} from "@/components/tcode/TCode";

import i18n from "@/locales/i18n";
import { cloud, load, syncUpload, syncDownload, localStoreUtil } from "@/utils/CloudUtil";
import { deleteDialog } from "@/utils/DeleteDialog";
import { calcType } from "@/components/calc/CalcType";
import { calcSize } from '@/components/calc/CalcSize';
import { calcBgColor } from "@/components/calc/CalcColor";
import { localStore } from "@/env/Store";
import NoData from "@/components/NoData";
import ToolTip from "@/components/ToolTip";
import FileIcons from "file-icons-vue";

export default {
  name: "FrameWork",
  components: {
    FileIcons,
    NoData,
    ToolTip,
    DockerBlock,
    ConnectSetting,
    PreferenceSetting,
    FileBlock,
    TCodeWorkflow,
    TCodeCenter,
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

    // 终端插件
    const fitAddon = new FitAddon();              // 宽高自适应
    const webLinksAddon = new WebLinksAddon();    // 网页链接跳转

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

    // 多端同步提示
    const cloudSync = async () => {
      if(urlParams.value.user) deleteDialog(i18n.global.t('提示'), i18n.global.t('确定从云端覆盖本地数据吗?'), doSyncDownload);
      else {
        await syncUpload();
        const link = getPureUrl() + '?user=' + changeBase64Str(localStoreUtil.getItem(localStore['user']));
        await toClipboard(link);
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
      if(localStoreUtil.getItem(localStore['options'])) options.value = JSON.parse(aesDecrypt(localStoreUtil.getItem(localStore['options'])));
      else options.value = {};
    };
    loadOps();
    const tcodes = ref({});
    const loadTCodes = () => {
      tcodes.value = {};
      if(localStoreUtil.getItem(localStore['tcodes'])) {
        tcodes.value = JSON.parse(aesDecrypt(localStoreUtil.getItem(localStore['tcodes'])));
      }
      setTimeout(() => {
        tCodeCenterRef.value.userTCodes = {...tcodes.value};
      },1);
    };
    loadTCodes();
    const env = ref(default_env);
    const urlParams = ref(getUrlParams());
    const loadEnv = () => {
      if(localStoreUtil.getItem(localStore['env'])) env.value = {...env.value, ...JSON.parse(aesDecrypt(localStoreUtil.getItem(localStore['env'])))};
      // # bg fg
      if(urlParams.value.bg && urlParams.value.bg[0] !== '#') urlParams.value.bg = '#' + urlParams.value.bg;
      if(urlParams.value.fg && urlParams.value.fg[0] !== '#') urlParams.value.fg = '#' + urlParams.value.fg;
      // true/false cursorBlink tCode cloud advance transport
      if(urlParams.value.cursorBlink === 'true') urlParams.value.cursorBlink = true;
      else if(urlParams.value.cursorBlink === 'false') urlParams.value.cursorBlink = false;
      if(urlParams.value.tCode === 'true') urlParams.value.tCode = true;
      else if(urlParams.value.tCode === 'false') urlParams.value.tCode = false;
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
      const nowOpInfo = options.value[env.value['option']];
      if(nowOpInfo) env.value = {...env.value,...nowOpInfo};
      else env.value.option = '';
      urlParams.value.option = env.value.option;
      // record
      if(urlParams.value.record) {
        if(urlParams.value.mode !== 'headless' && urlParams.value.mode !== 'pure') urlParams.value.mode = 'pure';
        now_connect_status.value = '';
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
      TCodeReservedVarsSetter('option', env.value.option);
    };
    loadEnv();

    // 保存更改的配置
    const saveOp = (name,item) => {
      if(name) options.value = {...options.value,[name]:item};
      localStoreUtil.setItem(localStore['options'], aesEncrypt(JSON.stringify(options.value)));
      loadOps();
    };
    // 删除配置
    const deleteOp = (name) => {
      delete options.value[name];
      saveOp(null,null);
      if(env.value.option && env.value.option === name) saveEnv({option:''},false);
    };

    // 初始化终端
    const terminal = ref();
    let term = null;
    const isFirst = ref(true);
    const initTerminal = () => {
      term = new Terminal({
        convertEol: true,                                     // 设置光标为下一行开头
        scrollback: Number.MAX_SAFE_INTEGER,                  // 终端回滚量
        disableStdin: false,                                  // 是否禁用输入
        cursorStyle: env.value.cursorStyle,                   // 光标样式(默认block)
        cursorInactiveStyle: 'outline',                       // 光标样式(失去焦点)
        cursorBlink: env.value.cursorBlink,                   // 光标是否闪烁
        theme: {
          foreground: env.value.fg,                           // 终端前景色
          background: env.value.bg,                           // 终端背景色
          cursor: env.value.fg,                               // 光标颜色
          selectionBackground: calcBgColor(env.value.bg),     // 选中文本背景色
        },
        lineHeight: 1.2,                                      // 文本行高
        fontFamily: env.value.fontFamily,                     // 字体(默认Consolas)
        fontSize: env.value.fontSize,                         // 字号(默认16)
      });
      term.loadAddon(fitAddon);
      term.loadAddon(webLinksAddon);
    };

    // 终端视高自适应
    const termFit = () => {
      terminal.value.style.height = (window.innerHeight - (urlParams.value.mode !== 'headless' ? 25 : 0)) + 'px';
      fitAddon.fit();
      // 修改虚拟终端行列大小
      if(socket.value && socket.value.readyState == WebSocket.OPEN && term) {
        const new_rows = fitAddon.proposeDimensions().rows;
        const new_cols = fitAddon.proposeDimensions().cols;
        socket.value.send(aesEncrypt(JSON.stringify({type:1,content:"",rows:new_rows,cols:new_cols}), secretKey.value));
        term.resize(new_cols,new_rows);
      }
    };
    // 终端写入
    const termWrite = (content) => {
      if(content) term.write(content);
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
        type: 'post',
        data: {
          sshKey: sshKey.value,
        },
        async success(resp) {
          if(resp.status === 'success') {
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
        cooperateKey: urlParams.value.cooperate
      }), secretKey.value);
      // ws连接
      socket.value = new WebSocket(ws_base_url + changeStr(aesEncrypt(JSON.stringify(wsInfo))));
      // 连接建立
      socket.value.onopen = () => {
        termFit();
      };
      // 接收消息
      socket.value.onmessage = resp => {
        const result = JSON.parse(resp.data);
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
          sshKey.value = aesDecrypt(aesDecrypt(result.data), secretKey.value);
          if(urlParams.value.cmd) {
            // bash命令
            if(urlParams.value.cmd.toLowerCase().startsWith('bash:')) sendMessage(urlParams.value.cmd.substring(5) + "\n");
            // 终端代码命令
            else if(urlParams.value.cmd.toLowerCase().startsWith('tcode:')) {
              setTimeout(() => {
                tcode.value = urlParams.value.cmd.substring(6);
                handleTCode({key: 'Enter'});
              }, 400);
            }
          }
          if(env.value.advance && env.value.server_user === 'root' && statusMonitorRef.value) statusMonitorRef.value.doMonitor();
          if(env.value.advance && dockerBlockRef.value) dockerBlockRef.value.getDockerVersion(sshKey.value);
        }
        // 输出
        else if(result.code == 1) {
          const output = aesDecrypt(aesDecrypt(result.data), secretKey.value);
          if(UserTCodeHelper.active) UserTCodeHelper.outArray.push(output);
          if(recording.value) {
            recordInfo.value.push({
              time: new Date().getTime(),
              content: output,
            });
          }
          if(!(UserTCodeHelper.active && !UserTCodeHelper.display)) termWrite(output);
        }
        // 更新协作者数量
        else if(result.code == 2) {
          onlineNumber.value = Number(aesDecrypt(aesDecrypt(result.data), secretKey.value));
        }
        // 更新文件传输列表
        else if(result.code == 3) {
          const fileTransInfo = JSON.parse(aesDecrypt(aesDecrypt(result.data), secretKey.value));
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
        if(now_connect_status.value === connect_status.value['Success'] && e.code != 3333) {
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
        UserTCodeHelper.reset();
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
      if(advanceTimer) clearTimeout(advanceTimer);
      if(isShowAdvance.value !== newVal) {
        advanceTimer = setTimeout(() => {
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
    const saveEnv = (new_env,restart=true) => {
      let save_env = default_env;
      if(localStoreUtil.getItem(localStore['env'])) save_env = {...save_env,...JSON.parse(aesDecrypt(localStoreUtil.getItem(localStore['env'])))};
      save_env = {...save_env,...new_env};
      localStoreUtil.setItem(localStore['env'], aesEncrypt(JSON.stringify(save_env)));
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
        if(UserTCodeHelper.active === active) socket.value.send(aesEncrypt(JSON.stringify({type:0,content:text,rows:0,cols:0}), secretKey.value));
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
      const pasteText = await navigator.clipboard.readText();
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
          const copyText = term.getSelection();
          if(copyText.trim()) await toClipboard(copyText);
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
        now_connect_status.value = connect_status.value['Connecting'];
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
        fileBlockRef.value.getInitDir();
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
        showSettings(false);
        cooperateGenRef.value.DialogVisible = true;
      }
      // 监控
      else if(type === 7) {
        showSettings(false);
        setTimeout(() => {
          statusMonitorRef.value.updateNetworkData();
          statusMonitorRef.value.updateDiskData();
        }, 1);
        statusMonitorRef.value.DialogVisible = true;
      }
      // Docker
      else if(type === 8) {
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
            socket.value.send(aesEncrypt(JSON.stringify({type:2,content:"",rows:0,cols:0}), secretKey.value));
          }
          // PC端
          if(props.osInfo.serverOS !== "Linux") {
            $.ajax({
              url: http_base_url + '/beat',
              type: 'post',
              data: {
                windowId: props.osInfo.windowId,
              },
              success() {
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
      // 传输列表
      resetTransportLists();
    };

    const setTCodeStatus = (transTCode, state) => {
      tcodes.value[transTCode].status = state;
      localStoreUtil.setItem(localStore['tcodes'], aesEncrypt(JSON.stringify(tcodes.value)));
      setTimeout(() => {
        tCodeCenterRef.value.userTCodes = {...tcodes.value};
      },1);
    };

    // 处理终端代码
    const tcode = ref('');
    const handleTCode = async (event) => {
      // 历史终端代码
      if(event.key === 'ArrowUp' || event.key === 'ArrowDown') {
        event.preventDefault();
        if(event.key === 'ArrowUp') tcode.value = historyTCode.up(tcode.value);
        else tcode.value = historyTCode.down(tcode.value);
        return;
      }
      if(event.key !== 'Enter') return;
      if(!tcode.value || tcode.value.length < 2) return;
      const transTCode = tcode.value.toUpperCase();
      tcode.value = '';
      historyTCode.add(transTCode);
      // 功能终端代码
      if(transTCode[0] === 'F' && FuncTCode[transTCode]) FuncTCode[transTCode].execFlow(instance);
      // 系统终端代码
      else if(transTCode[0] === 'S' && SysTCode[transTCode]) SysTCode[transTCode].execFlow(instance);
      // 用户终端代码
      else if(transTCode[0] === 'U' && tcodes.value[transTCode]) {
        if(!UserTCodeHelper.writeNoAwait) UserTCodeHelper.writeNoAwait = sendMessage;
        if(!UserTCodeHelper.context) UserTCodeHelper.fileBlockRef = fileBlockRef.value;
        // 未激活
        if(!UserTCodeHelper.active) {
          UserTCodeHelper.reset();
          UserTCodeHelper.active = true;
          // 执行流未被定义
          if(!tcodes.value[transTCode].execFlow || !(tcodes.value[transTCode].execFlow instanceof Function)) {
            const textFlow = tcodes.value[transTCode].workflow.toString();
            try {
              tcodes.value[transTCode].execFlow = new Function('kkTerminal', `return (async function() { ${textFlow} })()`);
            } catch (error) {
              setTCodeStatus(transTCode, 'Compile Error');
              ElMessage({
                message: i18n.global.t('终端代码') + ' ' + transTCode + ' ' + i18n.global.t('编译错误：') + error,
                type: 'error',
                grouping: true,
              });
              UserTCodeHelper.reset();
              return;
            }
          }
          // 执行Workflow
          try {
            await tcodes.value[transTCode].execFlow(UserTCodeExecutor);
            ElMessage({
              message: i18n.global.t('终端代码') + ' ' + transTCode + ' ' + i18n.global.t('工作流结束'),
              type: 'success',
              grouping: true,
            });
            setTCodeStatus(transTCode, 'Execute Success');
          } catch(error) {
            ElMessage({
              message: i18n.global.t('终端代码') + ' ' + transTCode + ' ' + i18n.global.t('执行中断：') + error,
              type: 'warning',
              grouping: true,
            });
            setTCodeStatus(transTCode, 'Execute Interrupt');
          } finally {
            UserTCodeHelper.reset();
          }
        }
        else {
          ElMessage({
            message: i18n.global.t('其它终端代码正在执行'),
            type: 'warning',
            grouping: true,
          });
        }
      }
      else {
        if(transTCode[0] === 'F' || transTCode[0] === 'S' || transTCode[0] === 'U') {
          ElMessage({
            message: i18n.global.t('终端代码') + ' ' + transTCode + ' ' + i18n.global.t('不存在'),
            type: 'warning',
            grouping: true,
          });
        }
        else {
          ElMessage({
            message: i18n.global.t('终端代码必须以 F,S,U 开头'),
            type: 'warning',
            grouping: true,
          });
        }
      }
    };
    const tCodeWorkflowRef = ref();
    // 批量导入TCode
    const importTCodes = (data) => {
      const tCodeData = {...tcodes.value,...data};
      localStoreUtil.setItem(localStore['tcodes'], aesEncrypt(JSON.stringify(tCodeData)));
      loadTCodes();
    };
    // 批量导出终端代码
    const exportTCodes = () => {
      let content = {};
      if(localStoreUtil.getItem(localStore['tcodes'])) content = JSON.parse(aesDecrypt(localStoreUtil.getItem(localStore['tcodes'])));
      // 创建 Blob 对象
      const blob = new Blob([JSON.stringify(content, null, 4)], { type: 'text/plain' });
      // 创建指向 Blob 的 URL
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = 'TerminalCode.json';
      document.body.appendChild(a);
      a.click();
      document.body.removeChild(a);
      // 释放 URL 对象
      URL.revokeObjectURL(url);
    };
    // 终端代码中心
    const tCodeCenterRef = ref();
    const handleSaveTCode = (name, content) => {
      const data = {};
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
    // 部署容器
    const runContainer = (cmd) => {
      sendMessage(cmd);
    };

    // 传输列表
    const waitingList = ref({});
    const uploadingList = ref({});
    const downloadingList = ref({});
    const finishedList = ref({});       // status: 0成功 1失败
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
      const operateList = transportLists[index % 4];
      // 查询
      if(type === 2) return operateList.value[id];
      // 删除
      delete operateList.value[id];
      // 新增/修改
      if(type === 0) {
        const delta = {};
        delta[id] = item;
        operateList.value = {...delta, ...operateList.value};
      }
      isShowDot.value = true;
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

      // 录像
      if(urlParams.value.record) {
        listenResize();
        recordInfo.value = await load('record-' + urlParams.value.record);
        if(recordInfo.value) playRecord(0);
        return;
      }

      // 连接服务器
      doSSHConnect();
      // 监听窗口大小变化
      listenResize();
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
      tcode,
      handleTCode,
      closeBlock,
      resetTerminal,
      tcodes,
      tCodeWorkflowRef,
      sendMessage,
      importTCodes,
      exportTCodes,
      tCodeCenterRef,
      handleSaveTCode,
      handleDeleteTCode,
      cooperating,
      onlineNumber,
      maxNumber,
      handleCooperate,
      endCooperateConfirm,
      calcType,
      calcSize,
      installDocker,
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
.global {
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

.bar-tag {
  margin-right: 10px;
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

/* 文本不可选中 */
.no-select {
  user-select: none;
}

/* 文本溢出省略 */
.ellipsis {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.disabled {
  background-color: #f5f7fa;
  color: #a8abb2;
  pointer-events: none;
}

.trans-items {
  height: 256px;
  overflow-y: scroll;
  scrollbar-width: none !important; /* Firefox */
  -ms-overflow-style: none !important; /* Internet Explorer 和 Edge */
}

/* 隐藏滚动条 */
.trans-items::-webkit-scrollbar {
  display: none !important; /* Chrome 和 Safari */
}

.trans-item {
  padding: 10px 15px;
  border-bottom: 1px solid #efefef;
  width: 100%;
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
</style>

<style>
.trans-tabs .el-tabs__content {
  padding: 0 0 !important;
}

.trans-progress .el-progress__text {
  font-size: 12px !important;
}
</style>
