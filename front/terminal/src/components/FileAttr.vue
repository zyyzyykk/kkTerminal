<template>
  <el-dialog
    v-model="DialogVisilble"
    :before-close="closeDialog"
    :width="400"
    :modal="false"
    modal-class="kk-dialog-class"
    align-center
    draggable
  >
    <template #title>
      <div style="margin-top: -15px;"></div>
      <div class="kk-flex nowrap">
        <FileIcons :name="fileInfo.name" width="16" height="16" :isFolder="fileInfo.isDirectory" />
        <div class="ellipsis" style="margin: 0 5px; font-size: small;">{{ fileInfo.name }}</div>
        <div style="font-size: small;">属性</div>
      </div>
    </template>
    <div style="margin-top: -32px;"></div>
    <div element-loading-text="Loading..." v-loading="loading" >
      <div class="kk-flex">
        <div style="margin-right: 10px;" ><FileIcons :name="fileInfo.name" width="32" height="32" :isFolder="fileInfo.isDirectory" /></div>
        <div>
          <el-input v-model="rename" placeholder="" />
        </div>
      </div>
      <div class="kk-border" ></div>
      <div class="kk-flex nowrap">
        <div class="no-select" style="text-align: left; width: 100px;">位置：</div>
        <div class="ellipsis">
          {{ fileDir + fileInfo.name }}
        </div>
        <div style="cursor: pointer; margin-left: 5px;" @click="doCopy(fileDir + fileInfo.name)"><el-icon size="15"><DocumentCopy /></el-icon></div>
      </div>
      <div v-if="fileInfo.isDirectory" class="kk-flex">
        <div class="no-select" style="text-align: left; width: 100px;">包含：</div>
        <div>
          {{ includeInfo }}
        </div>
        <div v-if="unreliable" style="margin-left: 10px;" >
          <el-tag size="small" type="danger">unsure</el-tag>
        </div>
      </div>
      <div v-else class="kk-flex">
        <div class="no-select" style="text-align: left; width: 100px;">大小：</div>
        <div>
          {{ calcSize(fileInfo.attributes.size) }} ({{ fileInfo.attributes.size + ' 字节' }})
        </div>
        <div v-if="unreliable" style="margin-left: 10px;" >
          <el-tag size="small" type="danger">unsure</el-tag>
        </div>
      </div>
      <div class="kk-border" ></div>
      <div class="kk-flex">
        <div class="no-select" style="text-align: left; width: 100px;">修改时间：</div>
        <div>
          {{ formatDate(fileInfo.attributes.mtime) }}
        </div>
      </div>
      <div class="kk-flex">
        <div class="no-select" style="text-align: left; width: 100px;">访问时间：</div>
        <div>
          {{ formatDate(fileInfo.attributes.atime) }}
        </div>
      </div>
      <div class="kk-border" ></div>
      <div class="kk-flex">
        <div class="no-select" style="text-align: left; width: 100px;">权限：</div>
        <div>
          {{ calcPriority(fileInfo.attributes.mode.type,fileInfo.attributes.permissions) }}
        </div>
        <div style="flex: 1;"></div>
        <div>
          <el-button size="small" type="primary" @click="confirm" >确定</el-button>
        </div>
      </div>
    </div>
    <div style="margin-top: -15px;"></div>
  </el-dialog>
</template>

<script>
import { ref } from 'vue';
import $ from 'jquery';
import { http_base_url } from '@/utils/BaseUrl';
import { formatDate } from '@/utils/FormatDate';
import { calcPriority } from '@/utils/CalcPriority';
import { ElMessage } from 'element-plus';
import useClipboard from "vue-clipboard3";
import { DocumentCopy } from '@element-plus/icons-vue';
import { calcSize } from '@/utils/CalcSize';
import { escapeItem, escapePath } from '@/utils/StringUtil';

// 引入文件图标组件
import FileIcons from 'file-icons-vue';

export default {
  name: 'FileAttr',
  components: {
    FileIcons,
    DocumentCopy,
  },
  setup(props,context)
  {
    // 拷贝
    const { toClipboard } = useClipboard();

    // 控制Dialog显示
    const DialogVisilble = ref(false);
    const fileInfo = ref({});
    const fileDir = ref('');
    const rename = ref('');

    // 加载
    const loading = ref(true);
    // 不可靠标识
    const unreliable = ref(false);
    // 包含信息
    const includeInfo = ref('0 个文件，0 个文件夹');

    // 获取文件大小
    const getFileSize = (sshKey) => {
      $.ajax({
        url: http_base_url + '/du',
        type:'get',
        data:{
          time:new Date().getTime(),
          sshKey:sshKey,
          path:escapePath(fileDir.value),
          item:escapeItem(fileInfo.value.name),
        },
        beforeSend: function() { // 发送请求前执行的方法
          loading.value = true;
        },
        success(resp){
          if(resp.status == 'success') fileInfo.value.attributes.size = parseInt(resp.data, 10);
          else unreliable.value = true;
        },
        complete: function() { // 发送请求完成后执行的方法
          loading.value = false;
        }
      });
    };

    // 获取文件夹包含信息
    const getFolderInclude = (sshKey) => {
      $.ajax({
        url: http_base_url + '/find',
        type:'get',
        data:{
          time:new Date().getTime(),
          sshKey:sshKey,
          path:escapePath(fileDir.value),
          item:escapeItem(fileInfo.value.name),
        },
        beforeSend: function() { // 发送请求前执行的方法
          loading.value = true;
        },
        success(resp){
          if(resp.status == 'success') {
            includeInfo.value = resp.data[0] + ' 个文件，' + (Math.max(0,parseInt(resp.data[1], 10) - 1)) + ' 个文件夹';
          }
          else unreliable.value = true;
        },
        complete: function() { // 发送请求完成后执行的方法
          loading.value = false;
        }
      });
    };
    
    // 确定
    const confirm = () => {
      // 校验
      if(fileInfo.value.name == rename.value) {
        DialogVisilble.value = false;
        return;
      }
      if(!(rename.value && rename.value.trim().length > 0)) {
        ElMessage({
          message: "文件名不能为空",
          type: "warning",
          grouping: true,
        })
        return;
      }
      if(rename.value.indexOf('/') != -1) {
        ElMessage({
          message: "文件名不能含有 /",
          type: "warning",
          grouping: true,
        })
        return;
      }
      let oldPath = fileDir.value + fileInfo.value.name;
      let newPath = fileDir.value + rename.value;
      context.emit('callback',oldPath,newPath);
      DialogVisilble.value = false;
    };

    // 关闭
    const closeDialog = (done) => {
      done();
    };

    const reset = () => {
      fileInfo.value = {};
      rename.value = '';
      fileDir.value = '';
      loading.value = false;
      unreliable.value = false;
      includeInfo.value = '0 个文件，0 个文件夹';
      DialogVisilble.value = false;
    };

    // 复制
    const doCopy = async (content) => {
      content += '';
      if(!(content && content.length > 0)) {
        ElMessage({
          message: '内容为空',
          type: 'warning',
          grouping: true,
          repeatNum: Number.MIN_SAFE_INTEGER,
        });
        return;
      }
      await toClipboard(content);
      ElMessage({
        message: '复制成功',
        type: 'success',
        grouping: true,
        repeatNum: Number.MIN_SAFE_INTEGER,
      });
    };

    return {
      DialogVisilble,
      fileInfo,
      confirm,
      closeDialog,
      reset,
      fileDir,
      rename,
      formatDate,
      calcPriority,
      calcSize,
      doCopy,
      loading,
      unreliable,
      includeInfo,
      getFileSize,
      getFolderInclude,

    }
  }
}
</script>

<style scoped>
.kk-flex {
  display: flex; 
  align-items: center;
  margin-top: 15px;
}

.kk-border {
  padding-bottom: 10px;
  border-bottom: 1px solid #ececec;
}

.no-select {
  user-select: none;
}

/* 文本溢出省略 */
.ellipsis {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.nowrap {
  white-space: nowrap;
}
</style>