<template>
  <el-dialog
    v-model="DialogVisilble"
    :before-close="closeDialog"
    title="文件管理"
    width="50%"
    draggable
  >
    <div style="margin-top: -15px;"></div>
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
            <div class="hover-class" @click="doUpload" style="margin-left: 10px; font-size: 18px; cursor: pointer;"><el-icon><Upload /></el-icon></div>
          </div>
        </div>
        <div class="list-class">
            <div v-if="files.length != 0">
                <div v-for="item in files" :key="item.name" >
                  <template v-if="item.type == true">
                    <div class="item-class" @click="changeDir(dir + item.name + '/')" >
                      <!-- <FileIcons :name="item.name" width="20" height="20" :isFloder="item.type" /> -->
                      <div style="margin: 0 10px;">{{ item.name }}</div>
                    </div>
                  </template>
                  <template v-else>
                    <div :class="['item-class', item.name == aimFileName ? 'item-selected' : '']" @click="aimFileName = item.name" @dblclick="downloadFile(item.name)" >
                      <!-- <FileIcons :name="item.name" width="20" height="20" :isFloder="item.type" /> -->
                      <div style="margin: 0 10px;">{{ item.name }}</div>
                    </div>
                  </template>
                </div>
            </div>
            <div v-else>
              <NoData :msg="noDataMsg"></NoData>
            </div>
        </div>
    </div>
  </el-dialog>
</template>

<script>
import { ref } from 'vue';
import $ from 'jquery';
import { http_base_url } from '@/Utils/BaseUrl';

import NoData from '@/components/NoData';

// 引入文件图标组件
// import FileIcons from 'vue-file-icons'

export default {
  name:'FileBlock',
  components: {
    NoData,
    // FileIcons,
  },
  props:['sshKey'],
  setup(props,context) {

    const aimFileName = ref('');

    // 获取初始目录
    const isShowDirInput = ref(false);
    const dir = ref('/');
    const getInitDir = () => {
      $.ajax({
        url: http_base_url + '/pwd',
        type:'get',
        data:{
          sshKey:props.sshKey,
        },
        success(resp){
          dir.value = resp.data.path;
          if(dir.value[dir.value.length - 1] != '/') dir.value = dir.value + '/';
          aimFileName.value = '';
          getDirList();
        }
      });
    }
    
    // 获取当前路径下的文件列表
    const files = ref([]);
    const noDataMsg = ref('暂无文件');
    const getDirList = () => {
      $.ajax({
        url: http_base_url + '/ls',
        type:'get',
        data:{
          sshKey:props.sshKey,
          path:dir.value,
        },
        success(resp){
          files.value = resp.data.files;
          noDataMsg.value = '暂无文件';
        },
        error(){
          files.value = [];
          noDataMsg.value = '目录不存在';
        }
      });
    }

    // 下载文件
    const downloadFile = (name) => {
      if(isShowDirInput.value == true) return;
      let a = document.createElement('a');
      a.href = http_base_url + '/download/' + name + '?sshKey=' + props.sshKey + '&path=' + dir.value;
      document.body.appendChild(a);
      context.emit('doHeartBeat');
      a.click();
      let timer = setInterval(() => {
        context.emit('doHeartBeat');
        console.log('kk');
      },180000);
      document.body.removeChild(a);
      clearInterval(timer);
    }

    // 更新目录路径
    const changeDir = (new_dir) => {
      if(isShowDirInput.value == true) return;
      dir.value = new_dir;
      aimFileName.value = '';
      getDirList();
    }

    // 更改路径回调
    const dirInputCallback = () => {
      isShowDirInput.value = false;
      if(dir.value[dir.value.length - 1] != '/') dir.value = dir.value + '/';
      aimFileName.value = '';
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
      aimFileName.value = '';
      doRefresh();
    }
    // 下载文件
    const doDownload = () => {
      if(isShowDirInput.value == true) return;
      if(aimFileName.value != '') downloadFile(aimFileName.value);
    }
    // 上传文件
    const doUpload = () => {
      if(isShowDirInput.value == true) return;
    }

    // 控制Dialog显示
    const DialogVisilble = ref(false);

    // 关闭
    const closeDialog = (done) => {
      dir.value = '/';
      aimFileName.value = '';
      files.value = [];
      done();
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
      aimFileName,

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
  margin-bottom: 10px;
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


</style>