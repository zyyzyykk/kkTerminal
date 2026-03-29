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
        <div class="title kk-flex ellipsis" >
          <div v-if="isShowDirInput" style="width: 100%; margin: 1px 0;" >
            <el-autocomplete id="aimDirInput" v-model="dir" :placeholder="$t('输入目录路径')" placement="bottom" popper-class="kk-autocomplete"
                             :trigger-on-focus="false" :fetch-suggestions="doCompgen" :debounce="400" fit-input-width
                             @keydown.enter="dirInputCallback" @blur="dirInputCallback" @mousedown.stop @dblclick.stop >
              <template #default="{ item }">
                <div class="ellipsis" >
                  <ToolTip :isShow="DialogVisible && isShowDirInput" :content="item.value" :parentStyle="{ width: '100%' }" >
                    <template #content>
                      <div class="ellipsis" >{{ item.value }}</div>
                    </template>
                  </ToolTip>
                </div>
              </template>
              <template #loading>
                <div style="height: 100%;" :element-loading-text="$t('加载中...')" v-loading="true" ></div>
              </template>
            </el-autocomplete>
          </div>
          <div v-else class="kk-flex" style="width: 100%; border: 1px solid #efefef;" >
            <button ref="closeDropdownButtonRef" style="display: none;" ></button>
            <div id="dirLevelBar" class="kk-flex level-class no-scrollbar no-select" @click="doShowDirInput" @scroll="handleScroll" >
              <div class="kk-flex" >
                <el-button @click="changeDirByLevel($event, -1)" text class="text-button" ><el-icon style="font-size: 16px;" ><Monitor /></el-icon></el-button>
                <el-dropdown v-show="DialogVisible && !isShowDirInput" placement="bottom-start" size="small" trigger="click" >
                  <el-button @click="getDirFolders(changeDirByLevel($event, -1, false))" text class="icon-button" ><el-icon><ArrowRight /></el-icon></el-button>
                  <template #dropdown>
                    <div :element-loading-text="$t('加载中...')" v-loading="folderLoading" >
                      <div v-if="folderItems.length > 0" class="folder-list" >
                        <div v-for="(item, index) in folderItems" :key="index" >
                          <el-dropdown-item @click="changeDir(folderDir + item)" >
                            <ToolTip :isShow="DialogVisible && !isShowDirInput" :content="item" >
                              <template #content>
                                <div class="ellipsis" >{{ item }}</div>
                              </template>
                            </ToolTip>
                          </el-dropdown-item>
                        </div>
                      </div>
                      <div v-else style="width: 120px; height: 128px;" >
                        <NoData height="128px" imgWidth="64px" msgSize="12px" v-if="!folderLoading" :msg="i18n.global.k('暂无文件夹')" ></NoData>
                      </div>
                    </div>
                  </template>
                </el-dropdown>
              </div>
              <div class="kk-flex" v-for="(item, index) in dirLevels" :key="index" >
                <el-button @click="changeDirByLevel($event, index)" text class="text-button" >{{ item }}</el-button>
                <el-dropdown v-show="DialogVisible && !isShowDirInput" placement="bottom-start" size="small" trigger="click" >
                  <el-button @click="getDirFolders(changeDirByLevel($event, index, false))" text class="icon-button" ><el-icon><ArrowRight /></el-icon></el-button>
                  <template #dropdown>
                    <div :element-loading-text="$t('加载中...')" v-loading="folderLoading" >
                      <div v-if="folderItems.length > 0" class="folder-list" >
                        <div v-for="(item, index) in folderItems" :key="index" >
                          <el-dropdown-item @click="changeDir(folderDir + '/' + item)" >
                            <ToolTip :isShow="DialogVisible && !isShowDirInput" :content="item" >
                              <template #content>
                                <div class="ellipsis" >{{ item }}</div>
                              </template>
                            </ToolTip>
                          </el-dropdown-item>
                        </div>
                      </div>
                      <div v-else style="width: 120px; height: 128px;" >
                        <NoData height="128px" imgWidth="64px" msgSize="12px" v-if="!folderLoading" :msg="i18n.global.k('暂无文件夹')" ></NoData>
                      </div>
                    </div>
                  </template>
                </el-dropdown>
              </div>
            </div>
            <div class="kk-flex" >
              <el-button @click="doShowDirInput" text class="operator-button" ><el-icon style="font-size: 16px;" ><Edit /></el-icon></el-button>
              <el-button @click="doRefresh" text class="operator-button" ><el-icon style="font-size: 16px;" ><Refresh /></el-icon></el-button>
              <el-button v-if="dir && dir !== '/'" @click="doReturn" text class="operator-button" ><el-icon style="font-size: 16px;" ><Fold /></el-icon></el-button>
              <el-button v-else :disabled="true" text class="operator-button" ><el-icon style="font-size: 16px;" ><Fold /></el-icon></el-button>
              <el-button v-if="selectedFiles.length === 1" @click="doDownload" text class="operator-button" ><el-icon style="font-size: 16px;" ><Download /></el-icon></el-button>
              <el-button v-else :disabled="true" text class="operator-button" ><el-icon style="font-size: 16px;" ><Download /></el-icon></el-button>
              <el-dropdown v-if="dirStatus === 0" v-show="DialogVisible" placement="bottom-end" trigger="click" >
                <el-button text class="operator-button" ><el-icon style="font-size: 16px;" ><Upload /></el-icon></el-button>
                <template #dropdown>
                  <el-dropdown-menu v-show="DialogVisible" class="no-select" style="text-align: center;" >
                    <el-dropdown-item @click="fileUploadTypeChoose(0)" >
                      <div class="kk-flex" >
                        <el-icon><DocumentAdd /></el-icon>
                        <div>{{ $t('文件') }}</div>
                      </div>
                    </el-dropdown-item>
                    <el-dropdown-item @click="fileUploadTypeChoose(1)" >
                      <div class="kk-flex" >
                        <el-icon><FolderAdd /></el-icon>
                        <div>{{ $t('文件夹') }}</div>
                      </div>
                    </el-dropdown-item>
                    <el-dropdown-item @click="fileUploadTypeChoose(2)" >
                      <div class="kk-flex" >
                        <el-icon><Link /></el-icon>
                        <div>URL</div>
                      </div>
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
              <el-button v-else :disabled="true" text class="operator-button" ><el-icon style="font-size: 16px;" ><Upload /></el-icon></el-button>
            </div>
          </div>
        </div>
        <div id="fileArea" ref="fileAreaRef" :element-loading-text="$t('加载中...')" v-loading="loading" class="list-class no-select"
             @contextmenu="handleContextMenu" @click="handleClick"
             @scroll="handleScroll" @dragover="stopEvent" @drop="handleFileDrag"
             tabindex="0" @keydown="handleShortcutKeys" >
          <div v-if="files.length > 0" >
            <div v-for="item in files" :key="item.id" >
              <template v-if="item.isDirectory" >
                <div :class="[isSelected(item.id) !== -1 ? 'item-selected' : '', 'item-class']" @click="addSelectFile($event,item)" @dblclick="changeDir(dir + item.name + '/')" @contextmenu="addSelectFile($event,item,false)" >
                  <FileIcons :style="{display: 'flex', alignItems: 'center'}" :iconStyle="{opacity: (item.name[0] === '.' || (isClipboard(item.id) !== -1 && isCtrlX)) ? 0.5 : 1}" :name="item.name" :width="20" :height="20" :isFolder="item.isDirectory" :isLink="item.isSymlink" />
                  <div style="margin: 0 10px;" v-if="isShowRenameInput && renameFile && item.id === renameFile.id" >
                    <el-input id="rename" v-model="renameFile.name" placeholder="" size="small" @keydown.enter="isShowRenameInput = false;" @blur="isShowRenameInput = false;" @keydown.stop @contextmenu.stop @mousedown.stop @dblclick.stop @change="handleRename(item)" />
                  </div>
                  <ToolTip :isShow="!isShowMenu" :content="item.name" >
                    <template #content>
                      <div v-if="!(isShowRenameInput && renameFile && item.id === renameFile.id)" class="ellipsis" style="margin: 0 10px; line-height: 18px;" >
                        {{ item.name }}
                      </div>
                    </template>
                  </ToolTip>
                </div>
              </template>
              <template v-else>
                <div :class="[isSelected(item.id) !== -1 ? 'item-selected' : '', 'item-class']" @click="addSelectFile($event,item)" @dblclick="preViewFile(item.name)" @contextmenu="addSelectFile($event,item,false)" >
                  <FileIcons :style="{display: 'flex', alignItems: 'center'}" :iconStyle="{opacity: (item.name[0] === '.' || (isClipboard(item.id) !== -1 && isCtrlX)) ? 0.5 : 1}" :name="item.name" :width="20" :height="20" :isFolder="item.isDirectory" :isLink="item.isSymlink" />
                  <div style="margin: 0 10px;" v-if="isShowRenameInput && renameFile && item.id === renameFile.id" >
                    <el-input id="rename" v-model="renameFile.name" placeholder="" size="small" @keydown.enter="isShowRenameInput = false;" @blur="isShowRenameInput = false;" @keydown.stop @contextmenu.stop @mousedown.stop @dblclick.stop @change="handleRename(item)" />
                  </div>
                  <ToolTip :isShow="!isShowMenu" :content="item.name" >
                    <template #content>
                      <div v-if="!(isShowRenameInput && renameFile && item.id === renameFile.id)" class="ellipsis" style="margin: 0 10px; line-height: 18px;" >
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
  <FilePreview ref="filePreviewRef" @doSave="doSave" ></FilePreview>
  <MkFile ref="mkFileRef" @callback="handleMkFile" ></MkFile>

  <!-- 文件属性 -->
  <FileAttr :sshKey="sshKey" ref="fileAttrRef" @callback="doRename" @editPermissions="editPermissions" :uploadingList="uploadingList" ></FileAttr>

  <!-- 菜单项 -->
  <div ref="menuBlockRef" @contextmenu="stopEvent" v-show="isShowMenu" class="kk-menu no-select" >
    <div style="border-bottom: 1px solid #ddd;" class="kk-menu-item" @click="handleMenuSelect(1)" key="1" >{{ $t('刷新') }}</div>
    <div :class="['kk-menu-item', selectedFiles.length !== 1 ? 'disabled':'']" @click="handleMenuSelect(2)" key="2" >{{ $t('打开') }}</div>
    <div style="border-bottom: 1px solid #ddd;" :class="['kk-menu-item', selectedFiles.length > 1 ? 'disabled':'']" @click="handleMenuSelect(3)" key="3" >{{ $t('复制路径') }}</div>
    <div style="border-bottom: 1px solid #ddd;" v-show="selectedFiles.length === 1 && isZipFile(selectedFiles[0].name)" :class="['kk-menu-item', !(selectedFiles.length === 1 && isZipFile(selectedFiles[0].name)) ? 'disabled':'']" @click="handleMenuSelect(9)" key="9" >{{ $t('解压') }}</div>
    <div :class="['kk-menu-item', selectedFiles.length !== 1 ? 'disabled':'']" @click="handleMenuSelect(4)" key="4" >{{ $t('下载') }}</div>
    <div :class="['kk-menu-item', dirStatus === 1 ? 'disabled':'']" @click="handleMenuSelect(5)" key="5" >{{ $t('新建') }}</div>
    <div :class="['kk-menu-item', selectedFiles.length !== 1 ? 'disabled':'']" @click="handleMenuSelect(6)" key="6" >{{ $t('重命名') }}</div>
    <el-popconfirm :title="$t('确定删除此文件吗？')"
      :confirm-button-text="$t('确定')" :cancel-button-text="$t('取消')"
      @confirm="confirmPopConfirm" @cancel="cancelPopConfirm"
      :visible="isShowMenu && isShowPop" trigger="click"
      :popper-style="{zIndex: 3466, fontSize: '13px', color: 'black'}" popper-class="confirmPop"
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
import { ref, onUnmounted, onMounted, watch } from "vue";
import browser from "@/utils/Browser";
import { request } from "@/utils/Request";
import { ElMessage } from "element-plus";
import { deleteDialog } from "@/components/common/DeleteDialog";
import { http_base_url } from "@/env/Base";
import { Edit, Refresh, Fold, Download, Upload, DocumentAdd, FolderAdd, Link, ArrowRight, Monitor } from "@element-plus/icons-vue";
import { escapeItem, escapePath, osFileNaturalSort } from "@/utils/String";
import { isZipFile } from "@/components/preview/FileSuffix";
import { getChmodValue } from "@/components/calc/CalcPriority";
import { getUrlParams, doUrlDownload } from "@/utils/Url";
import { CmdCodeReservedVarsSetter } from "@/components/cmdcode/CmdCode";

import ToolTip from "@/components/common/ToolTip";
import NoData from "@/components/common/NoData";
import FilePreview from "@/components/preview/FilePreview";
import MkFile from "./MkFile";
import FileAttr from "./FileAttr";
import FileUrl from "./FileUrl";

import i18n from "@/locales/i18n";
import FileIcons from "file-icons-vue";
import PQueue from "p-queue";

export default {
  name: 'FileBlock',
  components: {
    ToolTip,
    NoData,
    FileIcons,
    FilePreview,
    MkFile,
    FileAttr,
    FileUrl,
    Edit,
    Refresh,
    Fold,
    Download,
    Upload,
    DocumentAdd,
    FolderAdd,
    Link,
    ArrowRight,
    Monitor,
  },
  props: ['sshKey', 'os', 'uploadingList'],
  setup(props, context) {

    // 加载
    const loading = ref(true);

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
      if(dir.value === '') dir.value = homeDir.value;
      if(dir.value[0] !== '/') dir.value = '/' + dir.value;
      if(dir.value[dir.value.length - 1] !== '/') dir.value = dir.value + '/';
      dir.value = dir.value.replace(/\/{2,}/g, '/');
      // 更新路径显示
      calcDirLevel(dir.value);
    };
    const calcDirLevel = (fullPath) => {
      if(fullPath === '/') dirLevels.value = [];
      else dirLevels.value = fullPath.substring(1, fullPath.length - 1).split('/');
    };
    const changeDirByLevel = (event, index, change=true) => {
      stopEvent(event);
      let aimDir = '';
      for(let i=0;i<=index;i++) {
        aimDir += '/';
        aimDir += dirLevels.value[i];
      }
      aimDir += '/';
      if(change) changeDir(aimDir);
      return aimDir;
    };

    // 获取初始家目录
    const isShowDirInput = ref(false);
    const homeDir = ref('');
    const getHomeDir = () => {
      if(dir.value) return;
      request({
        url: http_base_url + '/file/home',
        type: 'get',
        data: {
          sshKey: props.sshKey,
        },
        success(resp) {
          if(resp.status === 'success') {
            noDataMsg.value = i18n.global.k('暂无文件');
            dirStatus.value = 0;
            homeDir.value = resp.data;
            dir.value = homeDir.value;
            confirmDirCorrect();
            selectedFiles.value = [];
            files.value = [];
            getDirList();
            // 预留值
            CmdCodeReservedVarsSetter('home', homeDir.value);
          }
          else {
            noDataMsg.value = resp.info;
            dirStatus.value = 1;
            loading.value = false;
          }
        }
      });
    };

    // 文件名称补全
    const doCompgen = (queryString, callback) => {
      const currentDir = dir.value;
      request({
        url: http_base_url + '/file/compgen',
        type: 'get',
        data: {
          sshKey: props.sshKey,
          path: currentDir,
        },
        success(resp) {
          if(currentDir === dir.value) {
            if(resp.status === 'success') {
              if(!isShowDirInput.value) return;
              const compgenItems = resp.data.split('|').filter(Boolean)
                  .filter(item => item.toLowerCase().startsWith(queryString.toLowerCase()))
                  .map(item => {return { value: item }});
              callback(compgenItems);
            }
          }
        },
      });
    };
    // ls文件夹
    const folderDir = ref('');
    const folderItems = ref([]);
    const folderLoading = ref(true);
    const getDirFolders = (path) => {
      folderDir.value = path;
      const currentFolder = folderDir.value;
      request({
        url: http_base_url + '/file/ls/folders',
        type: 'get',
        data: {
          sshKey: props.sshKey,
          path: currentFolder,
        },
        beforeSend() {      // 发送请求前执行的方法
          folderLoading.value = true;
          folderItems.value = [];
        },
        success(resp) {
          if(currentFolder === folderDir.value) {
            if(resp.status === 'success') {
              folderItems.value = resp.data.split('/').filter(Boolean);
            }
            folderLoading.value = false;
          }
        },
      });
    };
    // ls所有文件
    const dirStatus = ref(0);   // 目录状态: 0 正常 / 1 目录不存在、无权限等
    const noDataMsg = ref(i18n.global.k('暂无文件'));
    const getDirList = async () => {
      if(!dir.value) {
        getHomeDir();
        return;
      }
      const currentDir = dir.value;
      await request({
        url: http_base_url + '/file/ls/all',
        type: 'get',
        data: {
          sshKey: props.sshKey,
          path: currentDir,
        },
        beforeSend() {      // 发送请求前执行的方法
          loading.value = true;
          files.value = [];
        },
        success(resp) {
          if(currentDir === dir.value) {
            selectedFiles.value = [];
            if(resp.status === 'success') {
              dir.value = resp.data.path;
              confirmDirCorrect();
              files.value = osFileNaturalSort(resp.data.fileInfoList);
              noDataMsg.value = i18n.global.k('暂无文件');
              dirStatus.value = 0;
              lastSelectedIndex = -1;
              browser.setTimeout(() => {
                fileAreaRef.value.tabindex = '0';
                fileAreaRef.value.focus();
              }, 1);
              if(resp.data.fileName) preViewFile(resp.data.fileName);
              if(fileAttrRef.value && fileAttrRef.value.DialogVisible) {
                const currentFileInfo = getFileInfoByName(fileAttrRef.value.fileInfo.name);
                if(currentFileInfo) {
                  fileAttrRef.value.reset();
                  fileAttrRef.value.DialogVisible = true;
                  fileAttrRef.value.fileInfo = currentFileInfo;
                  fileAttrRef.value.fileDir = dir.value;
                  fileAttrRef.value.rename = currentFileInfo.name;
                  if(currentFileInfo.isDirectory) fileAttrRef.value.getFolderInclude();
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
              browser.setTimeout(() => {
                fileAreaRef.value.tabindex = '0';
                fileAreaRef.value.focus();
              }, 1);
            }
            loading.value = false;
          }
        },
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
        browser.setTimeout(() => {
          fileAreaRef.value.scrollTop = 31 * fileInfo.index;
        }, 1);
      }
      return fileInfo;
    };

    // 获取远程文件url
    const getRemoteFileUrl = (name, path) => {
      return http_base_url + '/file/download/remote/file' + '?time=' + new Date().getTime() + '&fileName=' + encodeURIComponent(name) + '&sshKey=' + props.sshKey + '&path=' + encodeURIComponent(path ? path : dir.value) + '&trigger=browser';
    };
    // 获取远程文件夹url
    const getRemoteFolderUrl = (name, path) => {
      return http_base_url + '/file/download/remote/folder' + '?time=' + new Date().getTime() + '&folderName=' + encodeURIComponent(escapeItem(name)) + '&sshKey=' + props.sshKey + '&path=' + encodeURIComponent(escapePath(path ? path : dir.value));
    };

    // 下载远程文件
    const downloadRemoteFile = (name) => {
      if(isShowDirInput.value) return;
      doUrlDownload(getRemoteFileUrl(name));
    };

    // 下载文件夹
    const downloadDir = (name) => {
      if(isShowDirInput.value) return;
      doUrlDownload(getRemoteFolderUrl(name));
    };

    // 更新目录路径
    const changeDir = (new_dir) => {
      if(isShowDirInput.value) return;
      dir.value = new_dir;
      dirInputCallback();
    };

    // 更改路径回调
    const dirInputCallback = () => {
      isShowDirInput.value = false;
      confirmDirCorrect();
      selectedFiles.value = [];
      getDirList();
      browser.setTimeout(() => {
        const element = document.querySelector('#dirLevelBar');
        if(element) element.scrollLeft = element.scrollWidth;
      }, 1);
    };

    // 刷新文件列表
    const doRefresh = () => {
      isShowDirInput.value = false;
      getDirList();
    };
    // 返回上一级
    const doReturn = () => {
      if(isShowDirInput.value) return;
      let currentDir = dir.value;
      if(currentDir === '/') return;
      if(currentDir[currentDir.length - 1] === '/') currentDir = currentDir.substring(0, currentDir.length - 1);
      const index = currentDir.lastIndexOf('/');
      if(index !== -1) currentDir = currentDir.substring(0, index + 1);
      changeDir(currentDir);
    };
    // 下载文件/文件夹
    const doDownload = () => {
      if(isShowDirInput.value) return;
      if(selectedFiles.value.length === 1 && selectedFiles.value[0].name && selectedFiles.value[0].isDirectory) downloadDir(selectedFiles.value[0].name);
      if(selectedFiles.value.length === 1 && selectedFiles.value[0].name && !selectedFiles.value[0].isDirectory) downloadRemoteFile(selectedFiles.value[0].name);
    };
    // 上传文件
    let globalUploadQueue = null;
    let globalUploadProgress = null;  // 字节级进度追踪
    const resetGlobalUpload = () => {
      if(globalUploadQueue) {
        globalUploadQueue.pause();
        globalUploadQueue.clear();
      }
      globalUploadQueue = new PQueue({
        concurrency: window.isSecureContext ? 24 : 5,
        interval: 200,
        intervalCap: 1,
      });
      globalUploadProgress = {};
    };
    resetGlobalUpload();
    const chunkSize = 4 * 1024 * 1024;   // 文件片大小
    const doUpload = async (fileData, config={}) => {
      try {
        if(isShowDirInput.value) return;
        const file = fileData.file;
        if(!file) return;
        // 文件切片
        const fileName = file.name;
        const fileSize = file.size;
        // 允许上传空文件
        const chunks = Math.max(1, Math.ceil(fileSize / chunkSize));
        // 文件ID
        const fileId = browser.crypto.randomUUID();
        file.uid = fileId;
        const path = config.path ? config.path : dir.value;
        const key = props.sshKey;

        // 添加到传输列表（等待中）
        const fileTransInfo = {
          id: fileId,
          path: path,
          name: fileName,
          size: fileSize,
          status: 0,
          progress: 0,
        };
        context.emit('updateTransportLists', 0, 0, fileId, fileTransInfo);

        // 大文件开始上传提示
        if(fileSize > 20 * 1024 * 1024 && !config.noBeforeAlert) {
          ElMessage({
            message: config.beforeAlert ? config.beforeAlert : i18n.global.t('开始上传'),
            type: 'success',
            grouping: true,
          });
        }
        // 分片上传
        globalUploadProgress[fileId] = {};
        const uploadTasks = [];
        for(let chunk=1;chunk<=chunks;chunk++) {
          // 添加任务至上传队列
          const task = globalUploadQueue.add(() => {
            // 计算分片
            const currentChunk = chunk;
            const start = (currentChunk - 1) * chunkSize;
            const end = start + chunkSize >= fileSize ? fileSize : start + chunkSize;
            const fileChunk = file.slice(start, end);
            // 上传文件片
            const formData = new FormData();
            formData.append('file', fileChunk);
            formData.append('chunk', currentChunk);
            formData.append('id', fileId);
            formData.append('sshKey', key);
            return new Promise((resolve, reject) => {
              // 上传已失败
              if(globalUploadProgress[fileId] === null) {
                reject();
                return;
              }
              request({
                url: http_base_url + '/file/chunk/upload',
                type: 'post',
                data: formData,
                contentType: false,
                processData: false,
                xhr() {
                  const xhr = new XMLHttpRequest();
                  xhr.upload.onprogress = (e) => {
                    // 更新传输列表（等待中）进度
                    globalUploadProgress[fileId][currentChunk.toString()] = Math.min(e.loaded, end - start);
                    const uploadedSize = Object.values(globalUploadProgress[fileId]).reduce((a, b) => a + b, 0);
                    fileTransInfo.progress = Math.min(Math.floor((uploadedSize / fileSize) * 100), 99);
                    context.emit('updateTransportLists', 0, 0, fileId, fileTransInfo);
                  };
                  return xhr;
                },
                success(resp) {
                  // 上传已失败
                  if(globalUploadProgress[fileId] === null) {
                    reject();
                    return;
                  }
                  // 文件片上传成功
                  if(resp.status === 'success') {
                    resolve();
                    return;
                  }
                  // 文件片上传失败
                  globalUploadProgress[fileId] = null;
                  chunk = chunks + 10;
                  // 更新传输列表（等待中）状态
                  fileTransInfo.status = -1;
                  context.emit('updateTransportLists', 0, 1, fileId, null);
                  context.emit('updateTransportLists', 3, 0, fileId, fileTransInfo);
                  ElMessage({
                    message: resp.info,
                    type: resp.status,
                    grouping: true,
                  });
                  reject();
                },
                error() {
                  // 上传已失败
                  if(globalUploadProgress[fileId] === null) {
                    reject();
                    return;
                  }
                  globalUploadProgress[fileId] = null;
                  chunk = chunks + 10;
                  // 更新传输列表（等待中）状态
                  fileTransInfo.status = -1;
                  context.emit('updateTransportLists', 0, 1, fileId, null);
                  context.emit('updateTransportLists', 3, 0, fileId, fileTransInfo);
                  reject();
                },
              });
            });
          }, {priority: -(chunk + Math.random())});
          uploadTasks.push(task);
        }
        // 合并文件片
        Promise.allSettled(uploadTasks).then(() => {
          const fileUploadProgress = globalUploadProgress[fileId];
          delete globalUploadProgress[fileId];
          // 上传已失败
          if(fileUploadProgress === null) return;
          // 上传成功
          // 更新传输列表（等待中）进度
          fileTransInfo.progress = 100;
          context.emit('updateTransportLists', 0, 0, fileId, fileTransInfo);
          const formData = new FormData();
          formData.append('fileName', fileName);
          formData.append('chunks', chunks);
          formData.append('totalSize', fileSize);
          formData.append('id', fileId);
          formData.append('path', path);
          formData.append('sshKey', key);
          request({
            url: http_base_url + '/file/chunk/merge',
            type: 'post',
            data: formData,
            contentType: false,
            processData: false,
            success(resp) {
              if(resp.status === 'success') {
                ElMessage({
                  message: config.afterAlert ? config.afterAlert : resp.info,
                  type: resp.status,
                  grouping: true,
                });
                if(path === dir.value) {
                  browser.setTimeout(() => {
                    getDirList();
                  }, Math.min(1000, 500 + chunks * 10));
                }
              }
            },
          });
        });
      } catch (error) {
        ElMessage({
          message: i18n.global.t("文件上传失败"),
          type: "error",
          grouping: true,
        });
      }
    };

    const doShowDirInput = (event) => {
      stopEvent(event);
      isShowDirInput.value = true;
      browser.setTimeout(() => {
        document.querySelector('#aimDirInput').focus();
      }, 1);
    };

    // 控制Dialog显示
    const DialogVisible = ref(false);

    // 文本文件编辑
    const filePreviewRef = ref();
    const preViewFile = async (name, config={}) => {
      filePreviewRef.value.fileName = name;
      filePreviewRef.value.fileUrl = getRemoteFileUrl(name);
      filePreviewRef.value.loading = true;
      filePreviewRef.value.DialogVisible = true;
      await filePreviewRef.value.initText(config);
    };
    // 保存文本，写回服务器
    const doSave = (name, url, arrayBuffer) => {
      const urlParams = getUrlParams(url);
      if(urlParams.sshKey !== props.sshKey) return;
      // 创建Blob对象
      const blob = new Blob([arrayBuffer], { type: 'application/octet-stream' });
      // 创建File对象
      const file = new File([blob], name);
      doUpload({file: file}, {path: urlParams.path, beforeAlert: i18n.global.t('修改保存中'), afterAlert: i18n.global.t('文件后台保存中')});
    };

    // 文件/文件夹拖拽
    const fileAreaRef = ref();
    // 阻止默认行为和事件冒泡
    const stopEvent = (event) => {
      event.preventDefault();
      event.stopPropagation();
    };
    const handleFileDrag = (event) => {
      stopEvent(event);
      // 文件/文件夹项
      const items = event.dataTransfer.items;
      if(!(items && items.length > 0)) return;
      const basePath = dir.value;
      for (let i = 0; i < items.length; i++) {
        const item = items[i].webkitGetAsEntry();
        // 文件类型
        if(item.isFile && !item.isDirectory) {
          item.file(file => {
            doUpload({file:file}, {path: basePath});
          });
        }
        // 文件夹类型
        else if(!item.isFile && item.isDirectory) {
          const currentPath = basePath + item.name;
          request({
            url: http_base_url + '/file/mkdir',
            type: 'post',
            data: {
              sshKey: props.sshKey,
              path: basePath,
              item: item.name,
            },
            success(resp) {
              if(resp.status === 'success') {
                getDirList();
                folderUpload(item, currentPath + '/');
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
              doUpload({file:file}, {path: basePath, noBeforeAlert: true});
            });
          }
          // 文件夹类型
          else if(!item.isFile && item.isDirectory) {
            const currentPath = basePath + item.name;
            request({
              url: http_base_url + '/file/mkdir',
              type: 'post',
              data: {
                sshKey: props.sshKey,
                path: basePath,
                item: item.name,
              },
              success(resp) {
                if(resp.status === 'success') {
                  folderUpload(item, currentPath + '/');
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
            await browser.navigator.clipboard.writeText(path);
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
          mkFileRef.value.currentDir = dir.value;
          mkFileRef.value.DialogVisible = true;
          break;
        // 重命名
        case 6:
          if(selectedFiles.value.length === 1) {
            renameFile.value = {...selectedFiles.value[0]};
            isShowRenameInput.value = true;
            browser.setTimeout(() => {
              document.querySelector('#rename').focus();
            }, 1);
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
    const closeDropdownButtonRef = ref();
    const handleScroll = () => {
      isShowMenu.value = false;
      isShowPop.value = false;
      if(closeDropdownButtonRef.value) closeDropdownButtonRef.value.click();
    };
    const menuBlockRef = ref();
    // 右键显示
    const handleContextMenu = (event) => {
      // 右键点击空白处
      if(event.target.id === 'fileArea') selectedFiles.value = [];
      menuBlockRef.value.style.top = event.clientY - 135 + 'px';
      menuBlockRef.value.style.left = event.clientX + 1 + 'px';
      isShowMenu.value = true;
      isShowPop.value = false;
      stopEvent(event);
    };
    // 左键点击空白处
    const handleClick = (event) => {
      if(event.target.id === 'fileArea') selectedFiles.value = [];
      stopEvent(event);
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
          message: i18n.global.t("文件名不能含有") + " |" + i18n.global.t('，') + "/",
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
      request({
        url: http_base_url + '/file/rename',
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
      request({
        url: http_base_url + '/file/rm',
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
    const handleMkFile = (isFolder, name, currentDir) => {
      request({
        url: http_base_url + (isFolder ? '/file/mkdir' : '/file/touch'),
        type: 'post',
        data: {
          sshKey: props.sshKey,
          path: isFolder ? currentDir : escapePath(currentDir),
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

    watch(dir,(newVal) => {
      if(mkFileRef.value) mkFileRef.value.closeDialog();
      if(fileUrlRef.value) fileUrlRef.value.closeDialog();
      if(fileAttrRef.value) fileAttrRef.value.closeDialog();
      // 预留值
      CmdCodeReservedVarsSetter('dir', newVal);
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
      path: '/',
      files: [],
    });
    const isCtrlX = ref(false);
    const handleShortcutKeys = (event) => {
      const renameDom = document.querySelector('#rename');
      if(renameDom && renameDom.contains(event.target)) return;
      stopEvent(event);
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
        switch (event.key.toLowerCase()) {
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
              isCtrlX.value = false;
            }
            break;
          // 粘贴
          case 'v':
            if(fileClipboard.value.files.length === 0) return;
            fileCopyMove(isCtrlX.value ? 'mv' : 'cp');
            if(isCtrlX.value) {
              isCtrlX.value = false;
              fileClipboard.value = {
                path: '/',
                files: [],
              };
            }
            break;
          // 剪切
          case 'x':
            if(selectedFiles.value.length > 0) {
              fileClipboard.value.path = dir.value;
              fileClipboard.value.files = [...selectedFiles.value];
              isCtrlX.value = true;
            }
            break;
          // 删除
          case 'backspace':
            if(selectedFiles.value.length > 0) {
              isShowMenu.value = false;
              isShowPop.value = false;
              deleteDialog(i18n.global.t('提示'), i18n.global.t('确定删除此文件吗？'), confirmPopConfirm);
            }
            break;
        }
      }
    };

    // 文件复制/剪切
    const fileCopyMove = (mode) => {
      if(fileClipboard.value.path === dir.value) return;
      request({
        url: http_base_url + '/file/' + mode,
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
              path: '/',
              files: [],
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
    const folderInputUpload = (basePath, filesArr, current) => {
      // 父目录创建
      const fileObj = filesArr[current];
      request({
        url: http_base_url + '/file/mkdir',
        type: 'post',
        data: {
          sshKey: props.sshKey,
          path: basePath,
          item: fileObj.path,
        },
        success(resp) {
          if(resp.status === 'success') {
            if(current === 0) getDirList();
            if(current < filesArr.length - 1) folderInputUpload(basePath,filesArr, current + 1);
            // 子文件上传
            for(let i=0;i<fileObj.files.length;i++) {
              const file = fileObj.files[i];
              doUpload({file:file}, {path: basePath + fileObj.path, noBeforeAlert: true});
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
      request({
        url: http_base_url + '/file/wget',
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
      request({
        url: http_base_url + '/file/untar',
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
      request({
        url: http_base_url + '/file/chmod',
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
        homeDir.value = '';
        folderLoading.value = true;
        folderItems.value = [];
        folderDir.value = '';
        dirLevels.value = [];
        isShowDirInput.value = false;
        noDataMsg.value = i18n.global.k('暂无文件');
        dirStatus.value = 0;
        fileClipboard.value = {
          path: '/',
          files:[],
        };
        isCtrlX.value = false;
        resetGlobalUpload();
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
      if(filePreviewRef.value && filePreviewRef.value.DialogVisible) filePreviewRef.value.closeDialog();
      if(mkFileRef.value && mkFileRef.value.DialogVisible) mkFileRef.value.closeDialog();
      if(fileAttrRef.value && fileAttrRef.value.DialogVisible) fileAttrRef.value.closeDialog();
      if(fileUrlRef.value && fileUrlRef.value.DialogVisible) fileUrlRef.value.closeDialog();
      browser.setTimeout(() => {
        reset();
      }, 400);
      DialogVisible.value = false;
      if(done) done();
    };

    // 深度关闭
    const deepCloseDialog = () => {
      if(filePreviewRef.value) filePreviewRef.value.closeDialog();
      if(mkFileRef.value) mkFileRef.value.closeDialog();
      if(fileAttrRef.value) fileAttrRef.value.closeDialog();
      if(fileUrlRef.value) fileUrlRef.value.closeDialog();
      browser.setTimeout(() => {
        reset(true);
      }, 400);
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
        fileAreaRef.value.removeEventListener('dragover', stopEvent);
        fileAreaRef.value.removeEventListener('drop', handleFileDrag);
        fileAreaRef.value.removeEventListener('scroll', handleScroll);
        fileAreaRef.value.removeEventListener('keydown', handleShortcutKeys);
      }
    });

    return {
      i18n,
      DialogVisible,
      isShowDirInput,
      confirmDirCorrect,
      dir,
      files,
      getHomeDir,
      folderDir,
      folderItems,
      folderLoading,
      getDirFolders,
      getDirList,
      fileBlockView,
      downloadRemoteFile,
      downloadDir,
      changeDir,
      dirInputCallback,
      doCompgen,
      noDataMsg,
      doRefresh,
      doReturn,
      doDownload,
      doUpload,
      selectedFiles,
      loading,
      filePreviewRef,
      preViewFile,
      doSave,
      fileAreaRef,
      handleMenuSelect,
      isShowMenu,
      stopEvent,
      handleFileDrag,
      closeDropdownButtonRef,
      handleScroll,
      handleContextMenu,
      handleClick,
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
      isCtrlX,
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
  font-size: 14px;
  margin-bottom: 15px;
}

.icon-button {
  padding: 0 2px;
}

.text-button {
  padding: 0 8px;
}

.operator-button {
  padding: 0 6px;
}

.level-class {
  flex: 1;
  overflow-x: scroll;
}

.folder-list {
  width: 120px;
  max-height: 192px;
  overflow-y: scroll;
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

.item-selected {
  background-color: #efefef !important;
  border-bottom: 1px solid #e8e8e8;
}

.kk-menu {
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

.kk-menu-item:hover {
  background-color: #efefef;
}

.disabled {
  pointer-events: none;
  color: #a8abb2;
  background-color: #f5f7fa;
}

/* 定位删除确认框 */
.confirmPop {
  user-select: none;
}
</style>

<style>
.kk-autocomplete .el-autocomplete-suggestion__wrap {
  padding: 0 0;
}

.kk-autocomplete ul {
  max-height: 160px;
  overflow-y: scroll;
}

.kk-autocomplete li {
  height: 32px;
  line-height: 32px;
  padding: 0 10px !important;
}

.kk-autocomplete .is-loading li {
  height: 160px !important;
  line-height: unset !important;
}
</style>
