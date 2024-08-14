<template>
  <el-dialog
    v-model="DialogVisilble"
    :before-close="closeDialog"
    title="文件管理"
    width="50%"
    :modal="false"
    modal-class="kk-dialog-class"
    draggable
    style="position: relative;"
  >
    <div style="margin-top: -27px;"></div>
    <div>
      <div class="title" style="display: flex; align-items: center;" >
        <div class="ellipsis" style="flex: 1;">
          <div v-if="isShowDirInput == true" >
            <el-input id="aimDirInput" v-model="dir" placeholder="输入目录路径" size="small" @blur="dirInputCallback" />
          </div>
          <div class="no-select ellipsis" v-else @dblclick="doShowDirInput" >{{ dir }}</div>
        </div>
        <div style="display: flex; align-items: center;">
          <div class="hover-class" @click="doRefresh" style="margin-left: 10px; font-size: 18px; cursor: pointer;"><el-icon><Refresh /></el-icon></div>
          <div v-if="dir && dir != '/'" class="hover-class" @click="doReturn" style="margin-left: 10px; font-size: 18px; cursor: pointer;"><el-icon><Fold /></el-icon></div>
          <div v-else class="disabled-function" style="margin-left: 10px; font-size: 18px; cursor: pointer;"><el-icon><Fold /></el-icon></div>
          <div v-if="selectedFiles.length == 1" class="hover-class" @click="doDownload" style="margin-left: 10px; font-size: 18px; cursor: pointer;"><el-icon><Download /></el-icon></div>
          <div v-else class="disabled-function" style="margin-left: 10px; font-size: 18px; cursor: pointer;"><el-icon><Download /></el-icon></div>
          <div v-if="dirStatus == 0" class="hover-class" @click="doUpload" style="margin-left: 10px; font-size: 18px; cursor: pointer;">
            <el-upload
              :show-file-list="false"
              :with-credentials="true"
              :http-request="doUpload"
              :multiple="true"
              >
              <el-icon><Upload /></el-icon>
            </el-upload>
          </div>
          <div v-else class="disabled-function" style="margin-left: 10px; font-size: 18px; cursor: pointer;"><el-icon><Upload /></el-icon></div>
        </div>
      </div>
      <div id="fileArea" ref="fileAreaRef" element-loading-text="Loading..." v-loading="loading" class="list-class no-select" 
         @contextmenu="handleContextMenu" @scroll="handleScroll" 
         @dragover="preventDefault" @drop="handleFileDrag" 
         tabindex="0" @keydown="handleShortcutKeys" >
          <div v-if="files.length != 0" >
              <div v-for="item in files" :key="item.id" >
                <template v-if="item.isDirectory == true">
                  <div :class="[isSelected(item.id) != -1 ? 'item-selected' : '', 'item-class']" @click="addSelectFile($event,item)" @dblclick="changeDir(dir + item.name + '/')" @contextmenu="addSelectFile($event,item,false)" >
                    <FileIcons :style="{opacity: isClipboard(item.id) != -1 && isCtrlx ? 0.5 : 1}" :name="item.name" width="20" height="20" :isFolder="item.isDirectory" />
                    <div style="margin: 0 10px;" v-if="isShowRenameInput == true && renameFile && item.id == renameFile.id" >
                      <el-input id="rename" v-model="renameFile.name" placeholder="" size="small" @blur="handleRename(item)" />
                    </div>
                    <div v-else class="ellipsis" style="margin: 0 10px;">{{ item.name }}</div>
                  </div>
                </template>
                <template v-else>
                  <div :class="[isSelected(item.id) != -1 ? 'item-selected' : '', 'item-class']" @click="addSelectFile($event,item)" @dblclick="preViewFile(item.name)" @contextmenu="addSelectFile($event,item,false)" >
                    <FileIcons :style="{opacity: isClipboard(item.id) != -1 && isCtrlx ? 0.5 : 1}" :name="item.name" width="20" height="20" :isFolder="item.isDirectory" />
                    <div style="margin: 0 10px;" v-if="isShowRenameInput == true && renameFile && item.id == renameFile.id" >
                      <el-input id="rename" v-model="renameFile.name" placeholder="" size="small" @blur="handleRename(item)" />
                    </div>
                    <div v-else class="ellipsis" style="margin: 0 10px;">{{ item.name }}</div>
                  </div>
                </template>
              </div>
          </div>
          <div v-else>
            <NoData @contextmenu="selectedFiles = []" v-if="loading == false" :msg="noDataMsg"></NoData>
          </div>
      </div>
    </div>
    <div style="margin-top: -12px;"></div>
  </el-dialog>

  <TxtPreview ref="txtPreviewRef" @doSave="doSave" ></TxtPreview>
  <MkFile ref="mkFileRef" @callback="handleMkFile" ></MkFile>
  <FileAttr ref="fileAttrRef" @callback="doRename" ></FileAttr>

  <div ref="menuBlockRef" @contextmenu="preventDefault" v-show="isShowMenu" class="kk-menu no-select">
    <div style="border-bottom: 1px solid #ddd;" class="kk-menu-item" @click="handleMenuSelect(1)" key="1" >刷新</div>
    <div :class="['kk-menu-item', selectedFiles.length != 1 ? 'disabled':'']" @click="handleMenuSelect(2)" key="2" >打开</div>
    <div style="border-bottom: 1px solid #ddd;" :class="['kk-menu-item', selectedFiles.length > 1 ? 'disabled':'']" @click="handleMenuSelect(3)" key="3" >复制路径</div>
    <div :class="['kk-menu-item', selectedFiles.length != 1 ? 'disabled':'']" @click="handleMenuSelect(4)" key="4" >下载</div>
    <div :class="['kk-menu-item', dirStatus == 1 ? 'disabled':'']" @click="handleMenuSelect(5)" key="5" >新建</div>
    <div :class="['kk-menu-item', selectedFiles.length != 1 ? 'disabled':'']" @click="handleMenuSelect(6)" key="6" >重命名</div>
    <a-popconfirm :open="isShowMenu && isShowPop" :overlayStyle="{zIndex: 3466,marginLeft: '10px'}" placement="rightBottom" ok-text="确定" cancel-text="取消" >
      <template #title>
        <div class="no-select" style="font-size: 13px; margin-top: 4px;" >确定删除此文件/文件夹吗?</div>
      </template>
      <template #okButton>
        <el-button id="sureDelFileBtn" size="small" type="primary" @click="handlePopConfirm" >确定</el-button>
      </template>
      <template #cancelButton>
        <el-button size="small" text >取消</el-button>
      </template>
      <div :class="['kk-menu-item', selectedFiles.length == 0 ? 'disabled':'']" key="7" >
        <div @click="handleMenuSelect(7)" >删除</div>
      </div>
    </a-popconfirm>
    <div style="border-top: 1px solid #ddd;" :class="['kk-menu-item', selectedFiles.length != 1 ? 'disabled':'']" @click="handleMenuSelect(8)" key="8" >属性</div>
  </div>

</template>

<script>
import { ref, onUnmounted, onMounted, watch } from 'vue';
import useClipboard from "vue-clipboard3";
import $ from 'jquery';
import { ElMessage } from 'element-plus';
import { http_base_url } from '@/Utils/BaseUrl';
import { Refresh, Fold, Download, Upload } from '@element-plus/icons-vue';

import NoData from '@/components/NoData';
import TxtPreview from './preview/TxtPreview';
import MkFile from './MkFile';
import FileAttr from './FileAttr';

// 引入文件图标组件
import FileIcons from 'file-icons-vue';

export default {
  name:'FileBlock',
  components: {
    NoData,
    FileIcons,
    TxtPreview,
    MkFile,
    FileAttr,
    Refresh,
    Fold,
    Download,
    Upload,
  },
  props:['sshKey','os'],
  setup(props) {

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
        if(index == -1) {
          lastSelectedIndex = item.index;
          selectedFiles.value = [];
          selectedFiles.value.push(item);
        }
        return;
      }      
      // 单击
      // shift
      if(event.shiftKey) {
        if(selectedFiles.value.length == 0 || lastSelectedIndex == -1 || lastSelectedIndex >= files.value.length) {
          selectedFiles.value = [];
          for(let i=0;i<=item.index;i++) selectedFiles.value.push({...files.value[i]});
          lastSelectedIndex = -1;
        }
        else {
          selectedFiles.value = [];
          const start = Math.min(item.index,lastSelectedIndex);
          const end = Math.max(item.index,lastSelectedIndex);
          for(let i=start;i<=end;i++) selectedFiles.value.push({...files.value[i]});
        }
      }
      // ctrl
      else if((props.os == "Windows" && event.ctrlKey) || ((props.os == "Mac" || props.os == "iOS") && event.metaKey)) {
        lastSelectedIndex = item.index;
        if(index != -1) selectedFiles.value.splice(index, 1);
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
        if(id == selectedFiles.value[i].id) return i;
      }
      return -1;
    };
    const isClipboard = (id) => {
      for(let i=0;i<fileClipboard.value.files.length;i++) {
        if(id == fileClipboard.value.files[i].id) return i;
      }
      return -1;
    };

    const dir = ref('');
    // 保证路径正确
    const confirmDirCorrect = () => {
      if(dir.value == '' || dir.value[0] != '/') dir.value = '/' + dir.value;
      if(dir.value[dir.value.length - 1] != '/') dir.value = dir.value + '/';
      dir.value = dir.value.replace(/\/{2,}/g, '/');
    };

    // 获取初始目录
    const isShowDirInput = ref(false);
    const getInitDir = () => {
      if(dir.value != '') return;
      $.ajax({
        url: http_base_url + '/pwd',
        type:'get',
        data:{
          sshKey:props.sshKey,
        },
        success(resp){
          dir.value = resp.data.path;
          confirmDirCorrect();
          selectedFiles.value = [];
          files.value = [];
          getDirList();
        }
      });
    };
    
    // 获取当前路径下的文件列表
    const noDataMsg = ref('暂无文件');
    // 目录状态：0 正常 / 1 目录不存在
    const dirStatus = ref(0);
    const getDirList = () => {
      let now_dir = dir.value;
      $.ajax({
        url: http_base_url + '/ls',
        type:'get',
        data:{
          sshKey:props.sshKey,
          path:now_dir,
        },
        beforeSend: function() { // 发送请求前执行的方法
          loading.value = true;
          files.value = [];
        },
        success(resp){
          if(now_dir == dir.value) {
            selectedFiles.value = [];
            if(resp.status == 'success') {
              files.value = resp.data.files;
              noDataMsg.value = '暂无文件';
              dirStatus.value = 0;
            }
            else {
              files.value = [];
              noDataMsg.value = resp.info;
              dirStatus.value = 1;
            }
          }
        },
        complete: function() { // 发送请求完成后执行的方法
          if(now_dir == dir.value) loading.value = false;
        }
      });
    };

    // 获取远程文件url
    const getRemoteFileUrl = (name, path) => {
      return http_base_url + '/download/remote/file/' + name + '?time=' + new Date().getTime() + '&sshKey=' + props.sshKey + '&path=' + (path ? path : dir.value);
    };
    // 获取远程文件夹url
    const getRemoteFolderUrl = (name, path) => {
      return http_base_url + '/download/remote/folder/' + name + '?time=' + new Date().getTime() + '&sshKey=' + props.sshKey + '&path=' + (path ? path : dir.value);
    };

    // 解析url的path
    const parseUrl = (url) => {
      let urlParams = {key:'', path:null};
      let indexKey = url.indexOf('&sshKey=');
      let indexPath = url.indexOf('&path=');
      if(indexKey != -1 && indexPath != -1) urlParams.key = url.substring(indexKey + 8, indexPath);
      if(indexPath != -1) urlParams.path = url.substring(indexPath + 6);
      return urlParams;
    };

    // 下载远程文件
    const downloadRemoteFile = (name) => {
      if(isShowDirInput.value == true) return;
      let a = document.createElement('a');
      a.href = getRemoteFileUrl(name);
      document.body.appendChild(a);
      a.click();
      document.body.removeChild(a);
    };

    // 下载文件夹
    const downloadDir = (name) => {
      if(isShowDirInput.value == true) return;
      let a = document.createElement('a');
      a.href = getRemoteFolderUrl(name);
      document.body.appendChild(a);
      a.click();
      document.body.removeChild(a);
    };

    // 更新目录路径
    const changeDir = (new_dir) => {
      if(isShowDirInput.value == true) return;
      dir.value = new_dir;
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
      if(isShowDirInput.value == true) return;
      if(dir.value == '/') return;
      if(dir.value[dir.value.length - 1] == '/') dir.value = dir.value.substring(0,dir.value.length - 1);
      let index = dir.value.lastIndexOf('/');
      if(index != -1) dir.value = dir.value.substring(0,index + 1);
      selectedFiles.value = [];
      doRefresh();
    };
    // 下载文件/文件夹
    const doDownload = () => {
      if(isShowDirInput.value == true) return;
      if(selectedFiles.value.length == 1 && selectedFiles.value[0].name && selectedFiles.value[0].isDirectory) downloadDir(selectedFiles.value[0].name);
      if(selectedFiles.value.length == 1 && selectedFiles.value[0].name && !selectedFiles.value[0].isDirectory) downloadRemoteFile(selectedFiles.value[0].name);
    };
    // 上传文件
    const chunkSize = 1024 * 517;   // 每一片大小517kB
    const doUpload = async (fileData, data={}) => {
      try {
        if(isShowDirInput.value == true) return;
        let file = fileData.file;
        if(!file) return;
        // 文件切片
        const fileName = file.name;
        const fileSize = file.size;
        // 允许上传空文件
        const chunks = parseInt(Math.ceil(fileSize / chunkSize)) == 0 ? 1 : parseInt(Math.ceil(fileSize / chunkSize));
        const fileId = file.uid;
        let chunkIndex = 1;
        const path = data.pathVal ? data.pathVal : dir.value;

        // 大文件开始上传提示
        if(fileSize > 20*1024*1024) {
          ElMessage({
            message: data.startUpLoad ? data.startUpLoad : '开始上传',
            type: 'success',
            grouping: true,
          });
        }

        // 分片上传
        for(let chunk=chunkIndex;chunk<=chunks;chunk++) {
          // 上传逻辑
          let start = (chunk-1) * chunkSize;
          let end = start + chunkSize >= fileSize ? fileSize : start + chunkSize;
          let chunkFile = file.slice(start, end);
          let formData = new FormData();
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
            type:'post',
            data: formData,
            contentType : false,
            processData : false,
            success(resp){
              // 文件后台上传中
              if(resp.code == 202) {
                ElMessage({
                  message: data.alert ? data.alert : resp.info,
                  type: resp.status,
                  grouping: true,
                });
                if(path == dir.value) {
                  setTimeout(() => {
                    getDirList();
                  }, Math.min(1000, 500 + chunks * 10));
                }
              }
              // 文件片上传成功
              // else if(resp.code == 203) {

              // }
              // 文件片上传失败
              else if(resp.code == 502) {
                ElMessage({
                  message: resp.info,
                  type: resp.status,
                  grouping: true,
                })
                chunk = chunks + 1;
              }
              // 文件片缺失
              else if(resp.code == 503) {
                ElMessage({
                  message: resp.info,
                  type: resp.status,
                  grouping: true,
                })
                chunk = chunks + 1;
              }
              // 上传文件大小不一致
              else if(resp.code == 504) {
                ElMessage({
                  message: resp.info,
                  type: resp.status,
                  grouping: true,
                })
                chunk = chunks + 1;
              }
              // ssh连接断开
              else if(resp.code == 602) {
                ElMessage({
                  message: resp.info,
                  type: resp.status,
                  grouping: true,
                })
                chunk = chunks + 1;
              }
            },
          });
        }
      } catch (error) {
        ElMessage({
          message: "文件上传失败",
          type: "error",
          grouping: true,
        })
      }
      
    };

    const doShowDirInput = () => {
      isShowDirInput.value = true;
      setTimeout(() => {
        document.querySelector('#aimDirInput').focus();
      },1);
    };

    // 控制Dialog显示
    const DialogVisilble = ref(false);

    // 关闭
    const closeDialog = (done) => {
      selectedFiles.value = [];
      renameFile.value = {};
      txtPreviewRef.value.DialogVisilble = false;
      mkFileRef.value.DialogVisilble = false;
      mkFileRef.value.reset();
      fileAttrRef.value.DialogVisilble = false;
      done();
    };

    // 文本文件编辑
    const txtPreviewRef = ref();
    const preViewFile = (name) => {
      txtPreviewRef.value.fileName = name;
      txtPreviewRef.value.fileUrl = getRemoteFileUrl(name);
      txtPreviewRef.value.loading = true;
      txtPreviewRef.value.initText();
      txtPreviewRef.value.DialogVisilble = true;
    };
    // 保存文本，写回服务器
    const doSave = (name, url, text) => {
      let urlParams = parseUrl(url);
      if(urlParams.key != props.sshKey) return;
      // 创建Blob对象
      const blob = new Blob([text], { type: 'text/plain' });
      // 创建File对象
      const file = new File([blob], name);
      file.uid = Math.random().toString(36).substring(2);
      doUpload({file:file}, {pathVal: urlParams.path, startUpLoad:"修改保存中",alert:'文件后台保存中'});
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
      for (let i = 0; i < items.length; i++) {
        let item = items[i].webkitGetAsEntry();
        // 文件类型
        if(item.isFile && !item.isDirectory) {
          item.file(file => {
            file.uid = Math.random().toString(36).substring(2);
            doUpload({file:file}, {pathVal: dir.value});
          });
        }
        // 文件夹类型
        else if(!item.isFile && item.isDirectory) {
          const nowPath = dir.value + item.name;
          $.ajax({
            url: http_base_url + '/mkdir',
            type:'post',
            data:{
              sshKey:props.sshKey,
              path: nowPath,
            },
            success(resp){
              if(resp.status == 'success') {
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
    const folderUpload = (directoryEntry, basePath) => {
      const reader = directoryEntry.createReader();
      reader.readEntries((entries) => {
        for (let i = 0; i < entries.length; i++) {
          const item = entries[i];
          // 文件类型
          if(item.isFile && !item.isDirectory) {
            item.file(file => {
              file.uid = Math.random().toString(36).substring(2);
              doUpload({file:file}, {pathVal: basePath});
            });
          }
          // 文件夹类型
          else if(!item.isFile && item.isDirectory) {
            const nowPath = basePath + item.name;
            $.ajax({
              url: http_base_url + '/mkdir',
              type:'post',
              data:{
                sshKey:props.sshKey,
                path: nowPath,
              },
              success(resp){
                if(resp.status == 'success') {
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
          if(selectedFiles.value.length == 1 && selectedFiles.value[0].isDirectory == true) changeDir(dir.value + selectedFiles.value[0].name + '/');
          else if(selectedFiles.value.length == 1 && selectedFiles.value[0].isDirectory == false) preViewFile(selectedFiles.value[0].name);
          break;
        // 复制路径
        case 3:
          if(!(dir.value && dir.value.length > 0)) {
            ElMessage({
              message: '内容为空',
              type: 'warning',
              grouping: true,
              repeatNum: Number.MIN_SAFE_INTEGER,
            });
          }
          else {
            const path = dir.value + (selectedFiles.value.length == 1 ? selectedFiles.value[0].name : '');
            await toClipboard(path);
            ElMessage({
              message: '复制成功',
              type: 'success',
              grouping: true,
              repeatNum: Number.MIN_SAFE_INTEGER,
            });
          }
          break;
        // 下载
        case 4:
          if(selectedFiles.value.length == 1) doDownload();
          break;
        // 新建
        case 5:
          mkFileRef.value.DialogVisilble = true;
          mkFileRef.value.nowDir = dir.value;
          break;
        // 重命名
        case 6:
          if(selectedFiles.value.length == 1) {
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
          if(selectedFiles.value.length == 1) {
            fileAttrRef.value.fileInfo = {...selectedFiles.value[0]};
            fileAttrRef.value.fileDir = dir.value;
            fileAttrRef.value.rename = selectedFiles.value[0].name;
            fileAttrRef.value.DialogVisilble = true;
          }
          break;
        default:
          break;
      }
      if(type != 7) {
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
      if(event.target.id == 'fileArea') selectedFiles.value = [];
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
      if(item.name == renameFile.value.name) {
        renameFile.value = {};
        return;
      }
      if(!(renameFile.value.name && renameFile.value.name.trim().length > 0)) {
        ElMessage({
          message: "文件名不能为空",
          type: "warning",
          grouping: true,
        })
        renameFile.value = {};
        return;
      }
      if(renameFile.value.name.indexOf('/') != -1) {
        ElMessage({
          message: "文件名不能含有 /",
          type: "warning",
          grouping: true,
        });
        renameFile.value = {};
        return;
      }
      let oldPath = dir.value + item.name;
      let newPath = dir.value + renameFile.value.name;
      renameFile.value = {};
      doRename(oldPath,newPath);
    };
    const doRename = (oldPath,newPath) => {
      $.ajax({
        url: http_base_url + '/rename',
        type:'post',
        data:{
          sshKey:props.sshKey,
          oldPath:oldPath,
          newPath:newPath,
        },
        success(resp){
          ElMessage({
            message: resp.info,
            type: resp.status,
            grouping: true,
          });
          getDirList();
        }
      });
    }
    // 批量删除文件/文件夹
    const handlePopConfirm = () => {
      isShowMenu.value = false;
      isShowPop.value = false;
      if(selectedFiles.value.length == 0) return;
      $.ajax({
        url: http_base_url + '/rm-rf',
        type:'post',
        data:{
          sshKey:props.sshKey,
          path:dir.value,
          items: selectedFiles.value.map(e => e.name).join(' '),
        },
        success(resp){
          ElMessage({
            message: resp.info,
            type: resp.status,
            grouping: true,
          });
          getDirList();
        }
      });
    };
    // 新建文件/文件夹
    const mkFileRef = ref();
    const handleMkFile = (isFolder, name, nowDir) => {      
      $.ajax({
        url: http_base_url + (isFolder ? '/mkdir' : '/touch'),
        type:'post',
        data:{
          sshKey:props.sshKey,
          path:nowDir + name,
        },
        success(resp){
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
      if(mkFileRef.value) mkFileRef.value.reset();
    });

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
      if ((props.os == "Windows" && event.ctrlKey) || ((props.os == "Mac" || props.os == "iOS") && event.metaKey)) {
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
              fileClipboard.value.files = selectedFiles.value.slice(0);
              isCtrlx.value = false;
            }
            break;
          // 粘贴
          case 'v':
            if(fileClipboard.value.files.length == 0) return;
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
            fileClipboard.value.path = dir.value;
            fileClipboard.value.files = selectedFiles.value.slice(0);
            isCtrlx.value = true;
            break;
        }
      }
    };

    // 文件复制/剪切
    const fileCopyMove = (mode) => {
      if(fileClipboard.value.path == dir.value) return;
      $.ajax({
        url: http_base_url + '/' + mode,
        type:'post',
        data:{
          sshKey:props.sshKey,
          src:fileClipboard.value.path,
          dst:dir.value,
          items: fileClipboard.value.files.map(e => e.name).join(' '),
        },
        success(resp){
          if(resp.status == 'success') getDirList();
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

    onMounted(() => {
      document.addEventListener('mousedown', (event) => {
        if(fileAreaRef.value && fileAreaRef.value.contains(event.target)) {
          fileAreaRef.value.tabindex = '0';
          fileAreaRef.value.focus();
        }
        if (menuBlockRef.value && menuBlockRef.value.contains(event.target)) return;
        const sureDelFileBtn = document.querySelector('#sureDelFileBtn');
        if (sureDelFileBtn && sureDelFileBtn.contains(event.target)) return;
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
      DialogVisilble,
      closeDialog,
      isShowDirInput,
      dir,
      files,
      getInitDir,
      getDirList,
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
      handlePopConfirm,
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

.list-class {
  height: 30vh;
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

.hover-class:hover
{
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
}

.kk-menu
{
  position: absolute;
  z-index: 3466;
  text-align: center;
  border-radius: 8px;
  border-top: 8px solid #fff;
  border-bottom: 8px solid #fff;
  box-shadow: 0 6px 12px 0 rgba(0, 0, 0, 0.15), 0 3px 6px -2px rgba(0, 0, 0, 0.2), 0 8px 16px 4px rgba(0, 0, 0, 0.12);
}

.kk-menu-item {
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


</style>