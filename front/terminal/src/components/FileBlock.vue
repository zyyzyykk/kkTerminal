<template>
  <el-dialog
    v-model="DialogVisilble"
    :before-close="closeDialog"
    title="文件管理"
    width="50%"
    :modal="false"
    modal-class="kk-dialog-class"
    style="position: relative;"
    draggable
  >
    <div style="margin-top: -22px;"></div>
    <div>
        <div class="title" style="display: flex; align-items: center;" >
          <div style="flex: 1; white-space: nowrap; overflow: hidden; text-overflow:ellipsis;">
            <div v-if="isShowDirInput == true" >
              <el-input v-model="dir" placeholder="输入目录路径" size="small" @change="dirInputCallback" />
            </div>
            <div v-else @dblclick="isShowDirInput = true" >{{ dir }}</div>
          </div>
          <div style="display: flex; align-items: center;">
            <div class="hover-class" @click="doRefresh" style="margin-left: 10px; font-size: 18px; cursor: pointer;"><el-icon><Refresh /></el-icon></div>
            <div class="hover-class" @click="doReturn" style="margin-left: 10px; font-size: 18px; cursor: pointer;"><el-icon><Fold /></el-icon></div>
            <div class="hover-class" @click="doDownload" style="margin-left: 10px; font-size: 18px; cursor: pointer;"><el-icon><Download /></el-icon></div>
            <div class="hover-class" @click="doUpload" style="margin-left: 10px; font-size: 18px; cursor: pointer;">
              <el-upload
                :show-file-list="false"
                :with-credentials="true"
                :http-request="doUpload"
                >
                <el-icon><Upload /></el-icon>
              </el-upload>
            </div>
          </div>
        </div>
        <div element-loading-text="Loading..." v-loading="loading" class="list-class no-select">
            <div v-if="files.length != 0">
                <div v-for="item in files" :key="item.name" >
                  <template v-if="item.isDirectory == true">
                    <div :class="['item-class', (aimFileInfo && item.name == aimFileInfo.name) ? 'item-selected' : '']" @click="aimFileInfo = item" @dblclick="changeDir(dir + item.name + '/')" >
                      <FileIcons :name="item.name" width="20" height="20" :isFloder="item.isDirectory" />
                      <div style="margin: 0 10px;">{{ item.name }}</div>
                    </div>
                  </template>
                  <template v-else>
                    <div :class="['item-class', (aimFileInfo && item.name == aimFileInfo.name) ? 'item-selected' : '']" @click="aimFileInfo = item" @dblclick="preViewFile(item.name)" >
                      <FileIcons :name="item.name" width="20" height="20" :isFloder="item.isDirectory" />
                      <div style="margin: 0 10px;">{{ item.name }}</div>
                    </div>
                  </template>
                </div>
            </div>
            <div v-else>
              <NoData v-if="loading == false" :msg="noDataMsg"></NoData>
            </div>
        </div>
    </div>
  </el-dialog>

  <!-- <FileOption ref="fileOptionRef" :file="fileOptionFile" @callback="fileOptionCallback" ></FileOption> -->
  <TxtPreview ref="txtPreviewRef" @doSave="doSave" ></TxtPreview>

</template>

<script>
import { ref } from 'vue';
import $ from 'jquery';
import { ElMessage } from 'element-plus'
import { http_base_url } from '@/Utils/BaseUrl';

import NoData from '@/components/NoData';
// import FileOption from './FileOption';
import TxtPreview from './preview/TxtPreview';

// 引入文件图标组件
import FileIcons from 'file-icons-vue'

export default {
  name:'FileBlock',
  components: {
    NoData,
    FileIcons,
    // FileOption,
    TxtPreview,
  },
  props:['sshKey'],
  setup(props) {

    // 加载
    const loading = ref(true);

    const aimFileInfo = ref(null);

    // 获取初始目录
    const isShowDirInput = ref(false);
    const dir = ref('');
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
          if(dir.value == '' || dir.value[0] != '/') dir.value = '/' + dir.value;
          if(dir.value[dir.value.length - 1] != '/') dir.value = dir.value + '/';
          dir.value = dir.value.replace(/\/{2,}/g, '/');
          aimFileInfo.value = null;
          getDirList();
        }
      });
    }
    
    // 获取当前路径下的文件列表
    const files = ref([]);
    const noDataMsg = ref('暂无文件');
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
            aimFileInfo.value = null;
            if(resp.status == 'success') {
              files.value = resp.data.files;
              noDataMsg.value = '暂无文件';
            }
            else {
              files.value = [];
              noDataMsg.value = resp.info;
            }
          }
        },
        complete: function() { // 发送请求完成后执行的方法
          if(now_dir == dir.value) loading.value = false;
        }
      });
    }

    // 获取文件url
    const getFileUrl = (name) => {
      return http_base_url + '/download/' + name + '?time=' + new Date().getTime() + '&sshKey=' + props.sshKey + '&path=' + dir.value;
    }

    // 解析url的path
    const parseUrl = (url) => {
      let urlParams = {key:'', path:null};
      let indexKey = url.indexOf('&sshKey=');
      let indexPath = url.indexOf('&path=');
      if(indexKey != -1 && indexPath != -1) urlParams.key = url.substring(indexKey + 8, indexPath);
      if(indexPath != -1) urlParams.path = url.substring(indexPath + 6);
      return urlParams;
    }

    // 下载文件
    const downloadFile = (name) => {
      if(isShowDirInput.value == true) return;
      let a = document.createElement('a');
      a.href = getFileUrl(name);
      document.body.appendChild(a);
      a.click();
      document.body.removeChild(a);
    }

    // 更新目录路径
    const changeDir = (new_dir) => {
      if(isShowDirInput.value == true) return;
      dir.value = new_dir;
      aimFileInfo.value = null;
      getDirList();
    }

    // 更改路径回调
    const dirInputCallback = () => {
      isShowDirInput.value = false;
      if(dir.value == '' || dir.value[0] != '/') dir.value = '/' + dir.value;
      if(dir.value[dir.value.length - 1] != '/') dir.value = dir.value + '/';
      dir.value = dir.value.replace(/\/{2,}/g, '/');
      aimFileInfo.value = null;
      getDirList();
    }

    // 刷新文件列表
    const doRefresh = () => {
      isShowDirInput.value = false;
      getDirList();
    }
    // 返回上一级
    const doReturn = () => {
      if(isShowDirInput.value == true) return;
      if(dir.value == '/') return;
      if(dir.value[dir.value.length - 1] == '/') dir.value = dir.value.substring(0,dir.value.length - 1);
      let index = dir.value.lastIndexOf('/');
      if(index != -1) dir.value = dir.value.substring(0,index + 1);
      aimFileInfo.value = null;
      doRefresh();
    }
    // 下载文件
    const doDownload = () => {
      if(isShowDirInput.value == true) return;
      if(aimFileInfo.value && aimFileInfo.value.name && !aimFileInfo.value.isDirectory) downloadFile(aimFileInfo.value.name);
    }
    // 上传文件
    const chunkSize = 1024 * 517;   // 每一片大小517kB
    const doUpload = async (fileData, pathVal) => {
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
      const path = pathVal ? pathVal : dir.value;

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
                message: resp.info,
                type: resp.status,
                grouping: true,
              })
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
    }

    // 控制Dialog显示
    const DialogVisilble = ref(false);

    // 关闭
    const closeDialog = (done) => {
      // dir.value = '/';
      // aimFileInfo.value = null;
      // files.value = [];

      // fileOptionRef.value.isShow = false;
      txtPreviewRef.value.DialogVisilble = false;
      done();
    }

    // 右键
    // const fileOptionRef = ref();
    // const fileOptionFile = ref(null);
    // const doContextmenu = (event,item) => {
    //   event.preventDefault();
    //   fileOptionRef.value.fileOptionBody.style.top = event.pageY + 'px';
    //   fileOptionRef.value.fileOptionBody.style.left = event.pageX + 'px';
    //   fileOptionFile.value = item;
    //   fileOptionRef.value.isShow = true;
    // }
    // const fileOptionCallback = () => {

    // }

    // 文本文件编辑
    const txtPreviewRef = ref();
    const preViewFile = (name) => {
      txtPreviewRef.value.fileName = name;
      txtPreviewRef.value.fileUrl = getFileUrl(name);
      txtPreviewRef.value.initText();
      txtPreviewRef.value.loading = true;
      txtPreviewRef.value.DialogVisilble = true;
    }
    // 保存文本，写回服务器
    const doSave = (name, url, text) => {
      let urlParams = parseUrl(url);
      if(urlParams.key != props.sshKey) return;
      // 创建Blob对象
      const blob = new Blob([text], { type: 'text/plain' });
      // 创建File对象
      const file = new File([blob], name);
      file.uid = Math.random().toString(36).substring(2);
      doUpload({file:file}, urlParams.path);
    }

    return {
      DialogVisilble,
      closeDialog,
      isShowDirInput,
      dir,
      files,
      getInitDir,
      getDirList,
      downloadFile,
      changeDir,
      dirInputCallback,
      noDataMsg,
      doRefresh,
      doReturn,
      doDownload,
      doUpload,
      aimFileInfo,
      // fileOptionRef,
      // doContextmenu,
      // fileOptionFile,
      // fileOptionCallback,
      loading,
      txtPreviewRef,
      preViewFile,
      doSave,

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
  border-bottom: 1px solid #ececec;
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
  background-color: #f3f3f3;
}

/* 文本不可选中 */
.no-select {
  user-select: none;
}


</style>