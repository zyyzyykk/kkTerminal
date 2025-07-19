<template>
  <div v-resizable="{ maxWidthRate: 1.2 }" >
    <el-dialog
        v-model="DialogVisible"
        :before-close="closeDialog"
        :title="$t('文件管理')"
        width="50%"
        :modal="false"
        modal-class="kk-dialog-class"
        header-class="kk-header-class"
        body-class="kk-body-class-0"
        align-center
        draggable
        style="position: relative;"
    >
      <div>
        <div class="title kk-flex" >
          <div class="ellipsis" style="flex: 1;" >
            <div v-if="isShowDirInput" >
              <el-input id="aimDirInput" v-model="dir" :placeholder="$t('输入目录路径')" size="small" @keydown.enter="isShowDirInput = false;" @blur="isShowDirInput = false;" @mousedown.stop @dblclick.stop @change="dirInputCallback" />
            </div>
            <div class="kk-flex no-select ellipsis" v-else @dblclick="doShowDirInput" >
              <div v-for="(item, index) in dirLevels" :key="index" class="kk-flex" >
                <div>/</div>
                <div class="dir-level" @click="changeDirByLevel($event,index)" >{{ item }}</div>
              </div>
              <div>/</div>
            </div>
          </div>
          <div class="kk-flex" >
            <div class="hover-class operate-icon" @click="doRefresh" ><el-icon><Refresh /></el-icon></div>
            <div v-if="dir && dir !== '/'" class="hover-class operate-icon" @click="doReturn" ><el-icon><Fold /></el-icon></div>
            <div v-else class="disabled-function operate-icon" ><el-icon><Fold /></el-icon></div>
            <div v-if="selectedFiles.length === 1" class="hover-class operate-icon" @click="doDownload" ><el-icon><Download /></el-icon></div>
            <div v-else class="disabled-function operate-icon" ><el-icon><Download /></el-icon></div>
            <div v-if="dirStatus === 0" class="hover-class operate-icon" >
              <el-dropdown v-show="DialogVisible" style="line-height: unset;" placement="bottom-end" size="small" trigger="click" >
                <div class="hover-class" style="font-size: 18px; cursor: pointer;" ><el-icon><Upload /></el-icon></div>
                <template #dropdown>
                  <el-dropdown-menu v-show="DialogVisible" class="no-select" style="text-align: center;" >
                    <el-dropdown-item @click="fileUploadTypeChoose(0)" >
                      <div class="kk-flex" >
                        <div><el-icon><DocumentAdd /></el-icon></div>
                        <div>{{ $t('文件') }}</div>
                      </div>
                    </el-dropdown-item>
                    <el-dropdown-item @click="fileUploadTypeChoose(1)" >
                      <div class="kk-flex" >
                        <div><el-icon><FolderAdd /></el-icon></div>
                        <div>{{ $t('文件夹') }}</div>
                      </div>
                    </el-dropdown-item>
                    <el-dropdown-item @click="fileUploadTypeChoose(2)" >
                      <div class="kk-flex" >
                        <div><el-icon><Link /></el-icon></div>
                        <div>URL</div>
                      </div>
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
            <div v-else class="disabled-function operate-icon" ><el-icon><Upload /></el-icon></div>
          </div>
        </div>
        <div id="fileArea" ref="fileAreaRef" element-loading-text="Loading..." v-loading="loading" class="list-class no-select"
             @contextmenu="handleContextMenu" @scroll="handleScroll"
             @dragover="preventDefault" @drop="handleFileDrag"
             tabindex="0" @keydown="handleShortcutKeys" >
          <div v-if="files.length > 0" >
            <div v-for="item in files" :key="item.id" >
              <template v-if="item.isDirectory" >
                <div :class="[isSelected(item.id) !== -1 ? 'item-selected' : '', 'item-class']" @click="addSelectFile($event,item)" @dblclick="changeDir(dir + item.name + '/')" @contextmenu="addSelectFile($event,item,false)" >
                  <FileIcons :style="{display: 'flex', alignItems: 'center'}" :iconStyle="{opacity: (item.name[0] === '.' || (isClipboard(item.id) !== -1 && isCtrlx)) ? 0.5 : 1}" :name="item.name" :width="20" :height="20" :isFolder="item.isDirectory" :isLink="item.isSymlink" />
                  <div style="margin: 0 10px;" v-if="isShowRenameInput && renameFile && item.id === renameFile.id" >
                    <el-input id="rename" v-model="renameFile.name" placeholder="" size="small" @keydown.enter="isShowRenameInput = false;" @blur="isShowRenameInput = false;" @keydown.stop @contextmenu.stop @mousedown.stop @dblclick.stop @change="handleRename(item)" />
                  </div>
                  <ToolTip :isShow="!isShowMenu" :content="item.name" :delay="1000" >
                    <template #content>
                      <div v-if="!(isShowRenameInput && renameFile && item.id === renameFile.id)" class="ellipsis" style="margin: 0 10px;" >
                        {{ item.name }}
                      </div>
                    </template>
                  </ToolTip>
                </div>
              </template>
              <template v-else>
                <div :class="[isSelected(item.id) !== -1 ? 'item-selected' : '', 'item-class']" @click="addSelectFile($event,item)" @dblclick="preViewFile(item.name)" @contextmenu="addSelectFile($event,item,false)" >
                  <FileIcons :style="{display: 'flex', alignItems: 'center'}" :iconStyle="{opacity: (item.name[0] === '.' || (isClipboard(item.id) !== -1 && isCtrlx)) ? 0.5 : 1}" :name="item.name" :width="20" :height="20" :isFolder="item.isDirectory" :isLink="item.isSymlink" />
                  <div style="margin: 0 10px;" v-if="isShowRenameInput && renameFile && item.id === renameFile.id" >
                    <el-input id="rename" v-model="renameFile.name" placeholder="" size="small" @keydown.enter="isShowRenameInput = false;" @blur="isShowRenameInput = false;" @keydown.stop @contextmenu.stop @mousedown.stop @dblclick.stop @change="handleRename(item)" />
                  </div>
                  <ToolTip :isShow="!isShowMenu" :content="item.name" :delay="1000" >
                    <template #content>
                      <div v-if="!(isShowRenameInput && renameFile && item.id === renameFile.id)" class="ellipsis" style="margin: 0 10px;" >
                        {{ item.name }}
                      </div>
                    </template>
                  </ToolTip>
                </div>
              </template>
            </div>
          </div>
          <div v-else>
            <NoData height="248px" @contextmenu="selectedFiles = []" v-if="!loading" :msg="noDataMsg" ></NoData>
          </div>
        </div>
      </div>
      <div style="margin-top: -12px;" ></div>
    </el-dialog>
  </div>

  <!-- 文件上传 -->
  <el-upload
    v-show="false"
    :show-file-list="false"
    :with-credentials="true"
    :http-request="doUpload"
    :multiple="true"
  >
    <el-button v-show="false" size="small" type="primary" id="fileUploadInputButton" ></el-button>
  </el-upload>
  <!-- 文件夹上传 -->
  <input
    v-show="false"
    type="file"
    webkitdirectory
    @change="folderInputUploadPrehandle"
    id="folderUploadInput"
  />
  <!-- 文件URL上传 -->
  <FileUrl ref="fileUrlRef" @callback="fileUrlUpload" ></FileUrl>
  <TxtPreview ref="txtPreviewRef" @doSave="doSave" ></TxtPreview>
  <MkFile ref="mkFileRef" @callback="handleMkFile" ></MkFile>

  <!-- 文件属性 -->
  <FileAttr :sshKey="sshKey" ref="fileAttrRef" @callback="doRename" @editPermissions="editPermissions" :uploadingList="uploadingList" ></FileAttr>

  <!-- 菜单项 -->
  <div ref="menuBlockRef" @contextmenu="preventDefault" v-show="isShowMenu" class="kk-menu no-select" >
    <div style="border-bottom: 1px solid #ddd;" class="kk-menu-item" @click="handleMenuSelect(1)" key="1" >{{ $t('刷新') }}</div>
    <div :class="['kk-menu-item', selectedFiles.length !== 1 ? 'disabled':'']" @click="handleMenuSelect(2)" key="2" >{{ $t('打开') }}</div>
    <div style="border-bottom: 1px solid #ddd;" :class="['kk-menu-item', selectedFiles.length > 1 ? 'disabled':'']" @click="handleMenuSelect(3)" key="3" >{{ $t('复制路径') }}</div>
    <div style="border-bottom: 1px solid #ddd;" v-show="selectedFiles.length === 1 && isZipFile(selectedFiles[0].name)" :class="['kk-menu-item', !(selectedFiles.length === 1 && isZipFile(selectedFiles[0].name)) ? 'disabled':'']" @click="handleMenuSelect(9)" key="9" >{{ $t('解压') }}</div>
    <div :class="['kk-menu-item', selectedFiles.length !== 1 ? 'disabled':'']" @click="handleMenuSelect(4)" key="4" >{{ $t('下载') }}</div>
    <div :class="['kk-menu-item', dirStatus === 1 ? 'disabled':'']" @click="handleMenuSelect(5)" key="5" >{{ $t('新建') }}</div>
    <div :class="['kk-menu-item', selectedFiles.length !== 1 ? 'disabled':'']" @click="handleMenuSelect(6)" key="6" >{{ $t('重命名') }}</div>
    <el-popconfirm :title="$t('确定删除此文件吗?')"
      :confirm-button-text="$t('确定')" :cancel-button-text="$t('取消')"
      @confirm="confirmPopConfirm" @cancel="cancelPopConfirm"
      :visible="isShowMenu && isShowPop" trigger="click"
      :popper-style="{zIndex: 3466,fontSize:'13px',color:'black'}" popper-class="confirmPop"
      placement="right" confirm-button-type="danger" >
      <template #reference>
        <div :class="['kk-menu-item', selectedFiles.length === 0 ? 'disabled':'']" key="7" >
          <div @click="handleMenuSelect(7)" >{{ $t('删除') }}</div>
        </div>
      </template>
    </el-popconfirm>
    <div style="border-top: 1px solid #ddd;" :class="['kk-menu-item', selectedFiles.length !== 1 ? 'disabled':'']" @click="handleMenuSelect(8)" key="8" >{{ $t('属性') }}</div>
  </div>

</template>

<script>
import { ref, onUnmounted, onMounted, watch } from 'vue';
import useClipboard from "vue-clipboard3";
import $ from 'jquery';
import { ElMessage } from 'element-plus';
import { http_base_url } from '@/env/BaseUrl';
import { Refresh, Fold, Download, Upload, DocumentAdd, FolderAdd, Link } from '@element-plus/icons-vue';
import { escapeItem, escapePath, generateRandomString, osFileNaturalSort } from '@/utils/StringUtil';
import { isZipFile } from '@/components/preview/FileSuffix';
import { getChmodValue } from '@/components/calc/CalcPriority';
import { getUrlParams } from "@/utils/UrlUtil";
import { aesEncryptBuffer, rsaEncrypt } from "@/utils/Encrypt";
import { encodeStrToArray } from "@/components/preview/EncodeUtil";

import ToolTip from '@/components/ToolTip';
import NoData from '@/components/NoData';
import TxtPreview from '../preview/TxtPreview';
import MkFile from './MkFile';
import FileAttr from './FileAttr';
import FileUrl from './FileUrl';

import i18n from "@/locales/i18n";

// 引入文件图标组件
import FileIcons from 'file-icons-vue';

export default {
  name:'FileBlock',
  components: {
    ToolTip,
    NoData,
    FileIcons,
    TxtPreview,
    MkFile,
    FileAttr,
    FileUrl,
    Refresh,
    Fold,
    Download,
    Upload,
    DocumentAdd,
    FolderAdd,
    Link,
  },
  props:['sshKey','os','uploadingList'],
  setup(props, context) {

    // 加载
    const loading = ref(true);

    // 拷贝
    const { toClipboard } = useClipboard();

    const files = ref([]);
    const selectedFiles = ref([]);
    let lastSelectedIndex = -1;
    const addSelectFile = (event, item, click=true) => {
      event.preventDefault();
      const index = isSelected(item.id);
      // 右键
      if(!click) {
        if(index === -1) {
          lastSelectedIndex = item.index;
          selectedFiles.value = [];
          selectedFiles.value.push(item);
        }
        return;
      }
      // 单击
      // shift
      if(event.shiftKey) {
        if(selectedFiles.value.length === 0 || lastSelectedIndex === -1 || lastSelectedIndex >= files.value.length) {
          selectedFiles.value = [];
          for(let i=0;i<=item.index;i++) selectedFiles.value.push({...files.value[i]});
          lastSelectedIndex = -1;
        }
        else {
          selectedFiles.value = [];
          const start = Math.min(item.index, lastSelectedIndex);
          const end = Math.max(item.index, lastSelectedIndex);
          for(let i=start;i<=end;i++) selectedFiles.value.push({...files.value[i]});
        }
      }
      // ctrl
      else if((props.os === "Windows" && event.ctrlKey) || ((props.os === "Mac" || props.os === "iOS") && event.metaKey)) {
        lastSelectedIndex = item.index;
        if(index !== -1) selectedFiles.value.splice(index, 1);
        else selectedFiles.value.push(item);
      }
      else {
        lastSelectedIndex = item.index;
        selectedFiles.value = [];
        selectedFiles.value.push(item);
      }
    };
    const isSelected = (id) => {
      for(let i=0;i<selectedFiles.value.length;i++) {
        if(id === selectedFiles.value[i].id) return i;
      }
      return -1;
    };
    const isClipboard = (id) => {
      for(let i=0;i<fileClipboard.value.files.length;i++) {
        if(id === fileClipboard.value.files[i].id) return i;
      }
      return -1;
    };
    const getFileInfoByName = (name) => {
      for(let i=0;i<files.value.length;i++) {
        if(files.value[i].name === name) return files.value[i];
      }
      return null;
    };

    const dir = ref('');
    const dirLevels = ref([]);
    // 保证路径正确
    const confirmDirCorrect = () => {
      if(dir.value === '' || dir.value[0] !== '/') dir.value = '/' + dir.value;
      if(dir.value[dir.value.length - 1] !== '/') dir.value = dir.value + '/';
      dir.value = dir.value.replace(/\/{2,}/g, '/');
      // 更新路径显示
      calcDirLevel(dir.value);
    };
    const calcDirLevel = (fullPath) => {
      if(fullPath === '/') dirLevels.value = [];
      else dirLevels.value = fullPath.substring(1,fullPath.length - 1).split('/');
    };
    const changeDirByLevel = (event,index) => {
      // not ctrl
      if(!((props.os === "Windows" && event.ctrlKey) || ((props.os === "Mac" || props.os === "iOS") && event.metaKey))) return;
      event.preventDefault();
      let aimDir = '/';
      for(let i=0;i<=index;i++) {
        aimDir += dirLevels.value[i];
        aimDir += '/';
      }
      changeDir(aimDir);
    };

    // 获取初始家目录
    const isShowDirInput = ref(false);
    const getInitDir = () => {
      if(dir.value) return;
      $.ajax({
        url: http_base_url + '/home',
        type: 'get',
        data: {
          time: new Date().getTime(),
          sshKey: props.sshKey,
        },
        success(resp) {
          if(resp.status === 'success') {
            noDataMsg.value = i18n.global.k('暂无文件');
            dirStatus.value = 0;
            dir.value = resp.data;
            confirmDirCorrect();
            selectedFiles.value = [];
            files.value = [];
            getDirList();
          }
          else {
            noDataMsg.value = resp.info;
            dirStatus.value = 1;
            loading.value = false;
          }
        }
      });
    };

    // 获取当前路径下的文件列表
    const noDataMsg = ref(i18n.global.k('暂无文件'));
    // 目录状态：0 正常 / 1 目录不存在、无权限等
    const dirStatus = ref(0);
    const getDirList = async () => {
      if(!dir.value) {
        getInitDir();
        return;
      }
      const now_dir = dir.value;
      await $.ajax({
        url: http_base_url + '/ls',
        type: 'get',
        data: {
          time: new Date().getTime(),
          sshKey: props.sshKey,
          path: now_dir,
        },
        beforeSend: function() { // 发送请求前执行的方法
          loading.value = true;
          files.value = [];
        },
        success(resp) {
          if(now_dir === dir.value) {
            selectedFiles.value = [];
            if(resp.status === 'success') {
              files.value = osFileNaturalSort(resp.data);
              noDataMsg.value = i18n.global.k('暂无文件');
              dirStatus.value = 0;
              lastSelectedIndex = -1;
              setTimeout(() => {
                fileAreaRef.value.tabindex = '0';
                fileAreaRef.value.focus();
              }, 1);
              if(fileAttrRef.value && fileAttrRef.value.DialogVisible) {
                const nowFileInfo = getFileInfoByName(fileAttrRef.value.fileInfo.name);
                if(nowFileInfo) {
                  fileAttrRef.value.reset();
                  fileAttrRef.value.DialogVisible = true;
                  fileAttrRef.value.fileInfo = nowFileInfo;
                  fileAttrRef.value.fileDir = dir.value;
                  fileAttrRef.value.rename = nowFileInfo.name;
                  if(nowFileInfo.isDirectory) fileAttrRef.value.getFolderInclude();
                  else fileAttrRef.value.getFileSize();
                }
                else fileAttrRef.value.closeDialog();
              }
            }
            else {
              files.value = [];
              noDataMsg.value = resp.info;
              dirStatus.value = 1;
              lastSelectedIndex = -1;
              setTimeout(() => {
                fileAreaRef.value.tabindex = '0';
                fileAreaRef.value.focus();
              }, 1);
            }
          }
        },
        complete: function() { // 发送请求完成后执行的方法
          if(now_dir === dir.value) loading.value = false;
        }
      });
    };
    const fileBlockView = async (path, name) => {
      dir.value = path;
      confirmDirCorrect();
      DialogVisible.value = true;
      await getDirList();
      const fileInfo = getFileInfoByName(name);
      if(fileInfo) {
        lastSelectedIndex = fileInfo.index;
        selectedFiles.value = [];
        selectedFiles.value.push(fileInfo);
        setTimeout(() => {
          fileAreaRef.value.scrollTop = 31 * fileInfo.index;
        }, 1);
      }
      return fileInfo;
    };

    // 获取远程文件url
    const getRemoteFileUrl = (name, path) => {
      return http_base_url + '/download/remote/file' + '?time=' + new Date().getTime() + '&fileName=' + encodeURIComponent(name) + '&sshKey=' + props.sshKey + '&path=' + encodeURIComponent(path ? path : dir.value) + '&type=download';
    };
    // 获取远程文件夹url
    const getRemoteFolderUrl = (name, path) => {
      return http_base_url + '/download/remote/folder' + '?time=' + new Date().getTime() + '&folderName=' + encodeURIComponent(escapeItem(name)) + '&sshKey=' + props.sshKey + '&path=' + encodeURIComponent(escapePath(path ? path : dir.value));
    };

    // 下载远程文件
    const downloadRemoteFile = (name) => {
      if(isShowDirInput.value) return;
      const a = document.createElement('a');
      a.href = getRemoteFileUrl(name);
      document.body.appendChild(a);
      a.click();
      document.body.removeChild(a);
    };

    // 下载文件夹
    const downloadDir = (name) => {
      if(isShowDirInput.value) return;
      const a = document.createElement('a');
      a.href = getRemoteFolderUrl(name);
      document.body.appendChild(a);
      a.click();
      document.body.removeChild(a);
    };

    // 更新目录路径
    const changeDir = (new_dir) => {
      if(isShowDirInput.value) return;
      dir.value = new_dir;
      confirmDirCorrect();
      selectedFiles.value = [];
      getDirList();
    };

    // 更改路径回调
    const dirInputCallback = () => {
      isShowDirInput.value = false;
      confirmDirCorrect();
      selectedFiles.value = [];
      getDirList();
    };

    // 刷新文件列表
    const doRefresh = () => {
      isShowDirInput.value = false;
      getDirList();
    };
    // 返回上一级
    const doReturn = () => {
      if(isShowDirInput.value) return;
      if(dir.value === '/') return;
      if(dir.value[dir.value.length - 1] === '/') dir.value = dir.value.substring(0,dir.value.length - 1);
      const index = dir.value.lastIndexOf('/');
      if(index !== -1) dir.value = dir.value.substring(0, index + 1);
      confirmDirCorrect();
      selectedFiles.value = [];
      doRefresh();
    };
    // 下载文件/文件夹
    const doDownload = () => {
      if(isShowDirInput.value) return;
      if(selectedFiles.value.length === 1 && selectedFiles.value[0].name && selectedFiles.value[0].isDirectory) downloadDir(selectedFiles.value[0].name);
      if(selectedFiles.value.length === 1 && selectedFiles.value[0].name && !selectedFiles.value[0].isDirectory) downloadRemoteFile(selectedFiles.value[0].name);
    };
    // 上传文件
    const chunkSize = 1024 * 2173;   // 每一片大小2173kB
    const doUpload = async (fileData, data={}) => {
      try {
        if(isShowDirInput.value) return;
        const file = fileData.file;
        if(!file) return;
        // 文件切片
        const fileName = file.name;
        const fileSize = file.size;
        // 允许上传空文件
        const chunks = (Math.ceil(fileSize / chunkSize) === 0) ? 1 : Math.ceil(fileSize / chunkSize);
        // 文件id统一为UUID
        file.uid = crypto.randomUUID();
        const fileId = file.uid;
        const chunkIndex = 1;
        const path = data.pathVal ? data.pathVal : dir.value;

        // 添加到传输列表（等待中）
        const fileTransInfo = {
          id: fileId,
          path: path,
          name: fileName,
          size: fileSize,
          progress: 0,
        };
        context.emit('updateTransportLists', 0, 0, fileId, fileTransInfo);

        // 大文件开始上传提示
        if(fileSize > 20*1024*1024 && !data.noStartUpLoad) {
          ElMessage({
            message: data.startUpLoad ? data.startUpLoad : i18n.global.t('开始上传'),
            type: 'success',
            grouping: true,
          });
        }
        // 分片上传
        for(let chunk=chunkIndex;chunk<=chunks;chunk++) {
          // 计算分片
          const start = (chunk-1) * chunkSize;
          const end = start + chunkSize >= fileSize ? fileSize : start + chunkSize;
          // 加密文件片
          const aesKey = generateRandomString(16);
          const chunkBuffer = await file.slice(start, end).arrayBuffer();
          const encryptedBuffer = encodeStrToArray(aesEncryptBuffer(chunkBuffer, aesKey), "UTF-8");
          const chunkFile = new File([new Blob([encryptedBuffer])], fileName);
          // 上传文件片
          const formData = new FormData();
          formData.append('aesKey',rsaEncrypt(aesKey));
          formData.append('file',chunkFile);
          formData.append('fileName',fileName);
          formData.append('chunks',chunks);
          formData.append('chunk',chunk);
          formData.append('totalSize',fileSize);
          formData.append('id',fileId);
          formData.append('sshKey',props.sshKey);
          formData.append('path',path);
          await $.ajax({
            url: http_base_url + '/upload',
            type: 'post',
            data: formData,
            contentType : false,
            processData : false,
            success(resp) {
              if(resp.status === 'success') {
                // 文件后台上传中
                if(resp.code == 202) {
                  ElMessage({
                    message: data.alert ? data.alert : resp.info,
                    type: resp.status,
                    grouping: true,
                  });
                  if(path === dir.value) {
                    setTimeout(() => {
                      getDirList();
                    }, Math.min(1000, 500 + chunks * 10));
                  }
                }
                else {
                  // 更新传输列表（等待中）进度
                  fileTransInfo.progress = Math.floor((chunk / chunks) * 100);
                  context.emit('updateTransportLists', 0, 0, fileId, fileTransInfo);
                }
              }
              else {
                ElMessage({
                  message: resp.info,
                  type: resp.status,
                  grouping: true,
                })
                chunk = chunks + 10;
                // 更新传输列表（等待中）状态
                fileTransInfo.status = 1;
                context.emit('updateTransportLists', 0, 1, fileId, null);
                context.emit('updateTransportLists', 3, 0, fileId, fileTransInfo);
              }
            },
          });
        }
      } catch (error) {
        ElMessage({
          message: i18n.global.t("文件上传失败"),
          type: "error",
          grouping: true,
        });
      }
    };

    const doShowDirInput = (event) => {
      // ctrl
      if((props.os === "Windows" && event.ctrlKey) || ((props.os === "Mac" || props.os === "iOS") && event.metaKey)) return;
      isShowDirInput.value = true;
      setTimeout(() => {
        document.querySelector('#aimDirInput').focus();
      },1);
    };

    // 控制Dialog显示
    const DialogVisible = ref(false);

    // 文本文件编辑
    const txtPreviewRef = ref();
    const preViewFile = async (name, config={}) => {
      txtPreviewRef.value.fileName = name;
      txtPreviewRef.value.fileUrl = getRemoteFileUrl(name);
      txtPreviewRef.value.loading = true;
      txtPreviewRef.value.DialogVisible = true;
      await txtPreviewRef.value.initText(config);
    };
    // 保存文本，写回服务器
    const doSave = (name, url, arrayBuffer) => {
      const urlParams = getUrlParams(url);
      if(urlParams.sshKey !== props.sshKey) return;
      // 创建Blob对象
      const blob = new Blob([arrayBuffer], { type: 'application/octet-stream' });
      // 创建File对象
      const file = new File([blob], name);
      doUpload({file:file}, {pathVal: urlParams.path, startUpLoad:i18n.global.t("修改保存中"),alert:i18n.global.t('文件后台保存中')});
    };

    // 文件/文件夹拖拽
    const fileAreaRef = ref();
    const preventDefault = (event) => {
      event.preventDefault();
    };
    const handleFileDrag = (event) => {
      event.preventDefault();
      // 文件/文件夹项
      const items = event.dataTransfer.items;
      if(!(items && items.length > 0)) return;
      const basePath = dir.value;
      for (let i = 0; i < items.length; i++) {
        const item = items[i].webkitGetAsEntry();
        // 文件类型
        if(item.isFile && !item.isDirectory) {
          item.file(file => {
            doUpload({file:file}, {pathVal: basePath});
          });
        }
        // 文件夹类型
        else if(!item.isFile && item.isDirectory) {
          const nowPath = basePath + item.name;
          $.ajax({
            url: http_base_url + '/mkdir',
            type: 'post',
            data: {
              sshKey: props.sshKey,
              path: basePath,
              item: item.name,
            },
            success(resp) {
              if(resp.status === 'success') {
                getDirList();
                folderUpload(item, nowPath + '/');
              }
              else {
                ElMessage({
                  message: resp.info,
                  type: resp.status,
                  grouping: true,
                });
              }
            }
          });
        }
      }
    };
    // 文件夹拖拽递归上传
    const folderUpload = (directoryEntry, basePath) => {
      const reader = directoryEntry.createReader();
      reader.readEntries((entries) => {
        for (let i = 0; i < entries.length; i++) {
          const item = entries[i];
          // 文件类型
          if(item.isFile && !item.isDirectory) {
            item.file(file => {
              doUpload({file:file}, {pathVal: basePath, noStartUpLoad: true});
            });
          }
          // 文件夹类型
          else if(!item.isFile && item.isDirectory) {
            const nowPath = basePath + item.name;
            $.ajax({
              url: http_base_url + '/mkdir',
              type: 'post',
              data: {
                sshKey: props.sshKey,
                path: basePath,
                item: item.name,
              },
              success(resp) {
                if(resp.status === 'success') {
                  folderUpload(item, nowPath + '/');
                }
                else {
                  ElMessage({
                    message: resp.info,
                    type: resp.status,
                    grouping: true,
                  });
                }
              }
            });
          }
       }
      });
    };

    // 菜单项
    const isShowMenu = ref(false);
    const isShowPop = ref(false);
    const isShowRenameInput = ref(false);
    const renameFile = ref({});
    const fileAttrRef = ref();
    // 菜单选择
    const handleMenuSelect = async (type) => {
      switch (type) {
        // 刷新
        case 1:
          doRefresh();
          break;
        // 打开
        case 2:
          if(selectedFiles.value.length === 1 && selectedFiles.value[0].isDirectory) changeDir(dir.value + selectedFiles.value[0].name + '/');
          else if(selectedFiles.value.length === 1 && !selectedFiles.value[0].isDirectory) preViewFile(selectedFiles.value[0].name);
          break;
        // 复制路径
        case 3:
          if(!(dir.value && dir.value.length > 0)) {
            ElMessage({
              message: i18n.global.t('内容为空'),
              type: 'warning',
              grouping: true,
              repeatNum: Number.MIN_SAFE_INTEGER,
            });
          }
          else {
            const path = dir.value + (selectedFiles.value.length === 1 ? selectedFiles.value[0].name : '');
            await toClipboard(path);
            ElMessage({
              message: i18n.global.t('复制成功'),
              type: 'success',
              grouping: true,
              repeatNum: Number.MIN_SAFE_INTEGER,
            });
          }
          break;
        // 下载
        case 4:
          if(selectedFiles.value.length === 1) doDownload();
          break;
        // 新建
        case 5:
          mkFileRef.value.nowDir = dir.value;
          mkFileRef.value.DialogVisible = true;
          break;
        // 重命名
        case 6:
          if(selectedFiles.value.length === 1) {
            renameFile.value = {...selectedFiles.value[0]};
            isShowRenameInput.value = true;
            setTimeout(() => {
              document.querySelector('#rename').focus();
            },1);
          }
          break;
        // 删除
        case 7:
          isShowPop.value = true;
          break;
        // 属性
        case 8:
          if(selectedFiles.value.length === 1) {
            fileAttrRef.value.reset();
            fileAttrRef.value.fileInfo = {...selectedFiles.value[0]};
            fileAttrRef.value.fileDir = dir.value;
            fileAttrRef.value.rename = selectedFiles.value[0].name;
            if(selectedFiles.value[0].isDirectory) fileAttrRef.value.getFolderInclude();
            else fileAttrRef.value.getFileSize();
            fileAttrRef.value.DialogVisible = true;
          }
          break;
        // 解压
        case 9:
          if(selectedFiles.value.length === 1 && isZipFile(selectedFiles.value[0].name)) {
            untar();
          }
          break;
        default:
          break;
      }
      if(type !== 7) {
        isShowMenu.value = false;
        isShowPop.value = false;
      }
    };
    const handleScroll = () => {
      isShowMenu.value = false;
      isShowPop.value = false;
    };
    const menuBlockRef = ref();
    // 右键显示
    const handleContextMenu = (event) => {
      // 点击空白处
      if(event.target.id === 'fileArea') selectedFiles.value = [];
      menuBlockRef.value.style.top = event.clientY - 135 + 'px';
      menuBlockRef.value.style.left = event.clientX + 1 + 'px';
      isShowMenu.value = true;
      isShowPop.value = false;
      event.preventDefault();
    };
    // 重命名文件
    const handleRename = (item) => {
      isShowRenameInput.value = false;
      // 校验
      if(item.name === renameFile.value.name) {
        renameFile.value = {};
        return;
      }
      if(!(renameFile.value.name && renameFile.value.name.trim().length > 0)) {
        ElMessage({
          message: i18n.global.t("文件名不能为空"),
          type: "warning",
          grouping: true,
        })
        renameFile.value = {};
        return;
      }
      const invalidNameRe = /[/|]/;
      if(invalidNameRe.test(renameFile.value.name)) {
        ElMessage({
          message: i18n.global.t("文件名不能含有") + " |,/",
          type: "warning",
          grouping: true,
        });
        renameFile.value = {};
        return;
      }
      const oldPath = dir.value + item.name;
      const newPath = dir.value + renameFile.value.name;
      renameFile.value = {};
      doRename(oldPath,newPath);
    };
    const doRename = (oldPath,newPath) => {
      $.ajax({
        url: http_base_url + '/rename',
        type: 'post',
        data: {
          sshKey: props.sshKey,
          oldPath: oldPath,
          newPath: newPath,
        },
        success(resp) {
          ElMessage({
            message: resp.info,
            type: resp.status,
            grouping: true,
          });
          getDirList();
        }
      });
    };
    // 批量删除文件/文件夹
    const confirmPopConfirm = () => {
      isShowMenu.value = false;
      isShowPop.value = false;
      if(selectedFiles.value.length === 0) return;
      $.ajax({
        url: http_base_url + '/rm-rf',
        type: 'post',
        data: {
          sshKey: props.sshKey,
          path: escapePath(dir.value),
          items: selectedFiles.value.map(e => escapeItem(e.name)).join(' '),
        },
        success(resp) {
          ElMessage({
            message: resp.info,
            type: resp.status,
            grouping: true,
          });
          getDirList();
        }
      });
    };
    const cancelPopConfirm = () => {
      isShowPop.value = false;
    };
    // 新建文件/文件夹
    const mkFileRef = ref();
    const handleMkFile = (isFolder, name, nowDir) => {
      $.ajax({
        url: http_base_url + (isFolder ? '/mkdir' : '/touch'),
        type: 'post',
        data: {
          sshKey: props.sshKey,
          path: isFolder ? nowDir : escapePath(nowDir),
          item: isFolder ? name : escapeItem(name),
        },
        success(resp) {
          ElMessage({
            message: resp.info,
            type: resp.status,
            grouping: true,
          });
          getDirList();
        }
      });
    };

    watch(dir,() => {
      if(mkFileRef.value) mkFileRef.value.closeDialog();
      if(fileUrlRef.value) fileUrlRef.value.closeDialog();
      if(fileAttrRef.value) fileAttrRef.value.closeDialog();
    });

    // 滚动到可视区
    const scrollToView = (index) => {
      const itemWidth = 31;
      const upDistance = index * itemWidth;
      const downDistance = upDistance + itemWidth;
      // 获取可视区域的高度
      const viewHeight = fileAreaRef.value.clientHeight;
      // 获取当前滚动位置
      const currentScrollTop = fileAreaRef.value.scrollTop;
      // 当前项不完全在可视区内
      if(upDistance < currentScrollTop) fileAreaRef.value.scrollTop = upDistance;
      else if(downDistance > currentScrollTop + viewHeight) fileAreaRef.value.scrollTop = downDistance - viewHeight;
    };

    // 文件快捷键操作
    const fileClipboard = ref({
      path:'/',
      files:[],
    });
    const isCtrlx = ref(false);
    const handleShortcutKeys = (event) => {
      const renameDom = document.querySelector('#rename');
      if(renameDom && renameDom.contains(event.target)) return;
      event.preventDefault();
      // 上下箭头
      if(event.key === 'ArrowUp' || event.key === 'ArrowDown') {
        // 上箭头
        if(event.key === 'ArrowUp') {
          if(lastSelectedIndex === -1 || lastSelectedIndex > files.value.length) lastSelectedIndex = files.value.length;
          lastSelectedIndex--;
          lastSelectedIndex = Math.max(0, lastSelectedIndex);
        }
        // 下箭头
        else if(event.key === 'ArrowDown') {
          lastSelectedIndex++;
          lastSelectedIndex = Math.min(files.value.length - 1, lastSelectedIndex);
        }
        isShowMenu.value = false;
        isShowPop.value = false;
        selectedFiles.value = [];
        selectedFiles.value.push(files.value[lastSelectedIndex]);
        scrollToView(lastSelectedIndex);
      }
      // 回车键
      if(event.key === 'Enter') {
        if(isShowMenu.value || isShowPop.value) {
          isShowMenu.value = false;
          isShowPop.value = false;
          return;
        }
        if(selectedFiles.value.length === 1) {
          const item = selectedFiles.value[0];
          if(item.isDirectory) changeDir(dir.value + item.name + '/');
          else preViewFile(item.name);
        }
      }
      // ctrl
      if ((props.os === "Windows" && event.ctrlKey) || ((props.os === "Mac" || props.os === "iOS") && event.metaKey)) {
        switch (String.fromCharCode(event.which).toLowerCase()) {
          // 全选
          case 'a':
            selectedFiles.value = [];
            for(let i=0;i<files.value.length;i++) selectedFiles.value.push({...files.value[i]});
            break;
          // 复制
          case 'c':
            if(selectedFiles.value.length > 0) {
              fileClipboard.value.path = dir.value;
              fileClipboard.value.files = [...selectedFiles.value];
              isCtrlx.value = false;
            }
            break;
          // 粘贴
          case 'v':
            if(fileClipboard.value.files.length === 0) return;
            fileCopyMove(isCtrlx.value ? 'mv' : 'cp');
            if(isCtrlx.value) {
              isCtrlx.value = false;
              fileClipboard.value = {
                path:'/',
                files:[],
              };
            }
            break;
          // 剪切
          case 'x':
            if(selectedFiles.value.length > 0) {
              fileClipboard.value.path = dir.value;
              fileClipboard.value.files = [...selectedFiles.value];
              isCtrlx.value = true;
            }
            break;
        }
      }
    };

    // 文件复制/剪切
    const fileCopyMove = (mode) => {
      if(fileClipboard.value.path === dir.value) return;
      $.ajax({
        url: http_base_url + '/' + mode,
        type: 'post',
        data: {
          sshKey: props.sshKey,
          src: escapePath(fileClipboard.value.path),
          dst: escapePath(dir.value),
          items: fileClipboard.value.files.map(e => escapeItem(e.name)).join(' '),
        },
        success(resp) {
          if(resp.status === 'success') getDirList();
          // 复制剪切失败
          else {
            ElMessage({
              message: resp.info,
              type: resp.status,
              grouping: true,
            });
            fileClipboard.value = {
              path:'/',
              files:[],
            };
          }
        }
      });
    };

    // 文件上传类型
    const fileUploadTypeChoose = (type) => {
      // 文件上传
      if(type === 0) {
        document.querySelector('#fileUploadInputButton').click();
      }
      // 文件夹上传
      else if(type === 1) {
        document.querySelector('#folderUploadInput').value = '';
        document.querySelector('#folderUploadInput').click();
      }
      // URL上传
      else if(type === 2) {
        fileUrlRef.value.DialogVisible = true;
      }
    };
    // 文件夹input框上传预处理
    const folderInputUploadPrehandle = (event) => {
      const basePath = dir.value;
      const filesGroupByPath = {};
      // 根据文件路径进行分类
      for(let i=0;i<event.target.files['length'];i++) {
        const file = event.target.files[i];
        const fullPath = file['webkitRelativePath'];
        const index = fullPath.lastIndexOf('/');
        const filePath = fullPath.substring(0, index + 1);
        if(!filesGroupByPath[filePath]) {
          filesGroupByPath[filePath] = {
            path:filePath,
            files:[],
          }
        }
        filesGroupByPath[filePath].files.push(file);
      }
      // 根据文件路径长度进行升序排序，确保文件父目录存在
      const filesArr = [];
      for(const key in filesGroupByPath) {
        filesArr.push(filesGroupByPath[key]);
      }
      filesArr.sort((a, b) => a.path.length - b.path.length);
      folderInputUpload(basePath,filesArr,0);
    };
    // 文件夹input框上传
    const folderInputUpload = (basePath,filesArr,now) => {
      // 父目录创建
      const fileObj = filesArr[now];
      $.ajax({
        url: http_base_url + '/mkdir',
        type: 'post',
        data: {
          sshKey: props.sshKey,
          path: basePath,
          item: fileObj.path,
        },
        success(resp) {
          if(resp.status === 'success') {
            if(now === 0) getDirList();
            if(now < filesArr.length - 1) folderInputUpload(basePath,filesArr, now+1);
            // 子文件上传
            for(let i=0;i<fileObj.files.length;i++) {
              const file = fileObj.files[i];
              doUpload({file:file}, {pathVal: basePath + fileObj.path, noStartUpLoad: true});
            }
          }
          else {
            ElMessage({
              message: i18n.global.t("文件夹上传失败"),
              type: resp.status,
              grouping: true,
              repeatNum: Number.MIN_SAFE_INTEGER,
            });
          }
        }
      });
    };

    // 文件URL上传
    const fileUrlRef = ref();
    const fileUrlUpload = (url,fileName) => {
      $.ajax({
        url: http_base_url + '/wget',
        type: 'post',
        data: {
          sshKey: props.sshKey,
          path: escapePath(dir.value),
          item: escapeItem(fileName),
          url: encodeURI(url),
        },
        success(resp) {
          ElMessage({
            message: resp.info,
            type: resp.status,
            grouping: true,
          });
          getDirList();
        }
      });
    };

    // 解压文件
    const untar = () => {
      $.ajax({
        url: http_base_url + '/untar',
        type: 'post',
        data: {
          sshKey: props.sshKey,
          path: escapePath(dir.value),
          item: escapeItem(selectedFiles.value[0].name),
        },
        success(resp) {
          ElMessage({
            message: resp.info,
            type: resp.status,
            grouping: true,
          });
          getDirList();
        }
      });
    };

    // 修改权限
    const editPermissions = (path,item, permissionsInfo) => {
      $.ajax({
        url: http_base_url + '/chmod',
        type: 'post',
        data: {
          sshKey: props.sshKey,
          path: escapePath(path),
          item: escapeItem(item),
          perms: getChmodValue(permissionsInfo),
          sub: permissionsInfo.sub || false,
        },
        success(resp) {
          ElMessage({
            message: resp.info,
            type: resp.status,
            grouping: true,
          });
          if(resp.status === 'success') {
            if(fileAttrRef.value && fileAttrRef.value.permissionsEditRef) fileAttrRef.value.permissionsEditRef.closeDialog();
            getDirList();
          }
        }
      });
    };

    // 重置
    const reset = (deep=false) => {
      if(deep) {
        loading.value = true;
        files.value = [];
        dir.value = '';
        dirLevels.value = [];
        isShowDirInput.value = false;
        noDataMsg.value = i18n.global.k('暂无文件');
        dirStatus.value = 0;
        fileClipboard.value = {
          path:'/',
          files:[],
        };
        isCtrlx.value = false;
      }
      selectedFiles.value = [];
      lastSelectedIndex = -1;
      isShowMenu.value = false;
      isShowPop.value = false;
      isShowRenameInput.value = false;
      renameFile.value = {};
      DialogVisible.value = false;
    };

    // 关闭
    const closeDialog = (done) => {
      if(txtPreviewRef.value && txtPreviewRef.value.DialogVisible) txtPreviewRef.value.closeDialog();
      if(mkFileRef.value && mkFileRef.value.DialogVisible) mkFileRef.value.closeDialog();
      if(fileAttrRef.value && fileAttrRef.value.DialogVisible) fileAttrRef.value.closeDialog();
      if(fileUrlRef.value && fileUrlRef.value.DialogVisible) fileUrlRef.value.closeDialog();
      setTimeout(() => {
        reset();
      },400);
      DialogVisible.value = false;
      if(done) done();
    };

    // 深度关闭
    const deepCloseDialog = () => {
      if(txtPreviewRef.value) txtPreviewRef.value.closeDialog();
      if(mkFileRef.value) mkFileRef.value.closeDialog();
      if(fileAttrRef.value) fileAttrRef.value.closeDialog();
      if(fileUrlRef.value) fileUrlRef.value.closeDialog();
      setTimeout(() => {
        reset(true);
      },400);
      DialogVisible.value = false;
    };

    onMounted(() => {
      document.addEventListener('mousedown', (event) => {
        if(fileAreaRef.value && fileAreaRef.value.contains(event.target)) {
          fileAreaRef.value.tabindex = '0';
          fileAreaRef.value.focus();
        }
        if (menuBlockRef.value && menuBlockRef.value.contains(event.target)) return;
        const confirmPop = document.querySelector('.confirmPop');
        if (confirmPop && confirmPop.contains(event.target)) return;
        isShowMenu.value = false;
        isShowPop.value = false;
      });
    });

    onUnmounted(() => {
      if(fileAreaRef.value) {
        fileAreaRef.value.removeEventListener('dragover', preventDefault);
        fileAreaRef.value.removeEventListener('drop', handleFileDrag);
        fileAreaRef.value.removeEventListener('scroll', handleScroll);
        fileAreaRef.value.removeEventListener('keydown', handleShortcutKeys);
      }
    });

    return {
      DialogVisible,
      isShowDirInput,
      confirmDirCorrect,
      dir,
      files,
      getInitDir,
      getDirList,
      fileBlockView,
      downloadRemoteFile,
      downloadDir,
      changeDir,
      dirInputCallback,
      noDataMsg,
      doRefresh,
      doReturn,
      doDownload,
      doUpload,
      selectedFiles,
      loading,
      txtPreviewRef,
      preViewFile,
      doSave,
      fileAreaRef,
      handleMenuSelect,
      isShowMenu,
      preventDefault,
      handleFileDrag,
      handleScroll,
      handleContextMenu,
      isShowRenameInput,
      handleRename,
      renameFile,
      menuBlockRef,
      doShowDirInput,
      isShowPop,
      confirmPopConfirm,
      cancelPopConfirm,
      mkFileRef,
      handleMkFile,
      doRename,
      fileAttrRef,
      folderUpload,
      dirStatus,
      handleShortcutKeys,
      fileClipboard,
      addSelectFile,
      isSelected,
      isClipboard,
      isCtrlx,
      fileCopyMove,
      fileUploadTypeChoose,
      folderInputUploadPrehandle,
      fileUrlRef,
      fileUrlUpload,
      reset,
      closeDialog,
      deepCloseDialog,
      isZipFile,
      untar,
      editPermissions,
      dirLevels,
      changeDirByLevel,
    }
  }
}
</script>

<style scoped>
.title {
  background-color: #efefef;
  padding: 4px 10px;
  font-size: 13px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow:ellipsis;
  margin-bottom: 15px;
}

.kk-flex {
  display: flex;
  align-items: center;
}

.list-class {
  margin-bottom: 10px;
  height: 248px;
  overflow-y: scroll;
  width: 100%;
}

.item-class {
  display: flex;
  align-items: center;
  padding: 5px 10px;
  border-bottom: 1px solid #efefef;
  cursor: pointer;
  width: 100%;
}

.item-class:hover {
  background-color: #f3f3f3;
}

.operate-icon {
  margin-left: 10px;
  font-size: 18px;
  cursor: pointer;
}

.hover-class:hover {
  color: #409eff;
}

.item-selected
{
  background-color: #efefef !important;
  border-bottom: 1px solid #d8d8d8;
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

.kk-menu
{
  position: absolute;
  z-index: 3466;
  text-align: left;
  border-radius: 8px;
  border-top: 8px solid #fff;
  border-bottom: 8px solid #fff;
  box-shadow: 0 6px 12px 0 rgba(0, 0, 0, 0.15), 0 3px 6px -2px rgba(0, 0, 0, 0.2), 0 8px 16px 4px rgba(0, 0, 0, 0.12);
}

.kk-menu-item {
  padding-left: 15px;
  height: 30px;
  font-size: 13px;
  line-height: 30px;
  width: 110px;
  color: #383838;
  background-color: #fff;
  cursor: pointer;
}

.kk-menu-item:hover
{
  background-color: #efefef;
}

.disabled {
  background-color: #f5f7fa;
  color: #a8abb2;
  pointer-events: none;
}

.disabled-function {
  color: #a8abb2;
  pointer-events: none;
}

.confirmPop {
  /* 定位删除确认框 */
  user-select: none;
}

.dir-level:hover {
  text-decoration: none;
  color: var(--link);
  cursor: pointer;
}
</style>
