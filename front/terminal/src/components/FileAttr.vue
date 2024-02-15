<template>
  <el-dialog
    v-model="DialogVisilble"
    :before-close="closeDialog"
    :width="350"
    :modal="false"
    modal-class="kk-dialog-class"
    align-center
    draggable
  >
    <template #title>
      <div class="kk-flex">
        <FileIcons v-if="DialogVisilble" :name="fileInfo.name" width="16" height="16" :isFloder="fileInfo.isDirectory" />
        <div style="margin: 0 5px; font-size: small;">{{ fileInfo.name }}</div>
        <div style="font-size: small;">属性</div>
      </div>
    </template>
    <div style="margin-top: -32px;"></div>
    <div>
      <div class="kk-flex">
        <div v-if="DialogVisilble" style="margin-right: 10px;" ><FileIcons :name="fileInfo.name" width="32" height="32" :isFloder="fileInfo.isDirectory" /></div>
        <div>
          <el-input v-model="rename" placeholder="" />
        </div>
      </div>
      <div class="kk-border" ></div>
      <div class="kk-flex">
        <div style="text-align: left; width: 100px;">位置：</div>
        <div>
          {{ fileDir + fileInfo.name }}
        </div>
      </div>
      <div class="kk-flex">
        <div style="text-align: left; width: 100px;">大小：</div>
        <div>
          {{ fileInfo.attributes.size + ' 字节' }}
        </div>
      </div>
      <div class="kk-border" ></div>
      <div class="kk-flex">
        <div style="text-align: left; width: 100px;">修改时间：</div>
        <div>
          {{ formatDate(fileInfo.attributes.mtime) }}
        </div>
      </div>
      <div class="kk-flex">
        <div style="text-align: left; width: 100px;">访问时间：</div>
        <div>
          {{ formatDate(fileInfo.attributes.atime) }}
        </div>
      </div>
      <div class="kk-border" ></div>
      <div class="kk-flex">
        <div style="text-align: left; width: 100px;">权限：</div>
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
import { formatDate } from '../Utils/FormatDate';
import { calcPriority } from '../Utils/CalcPriority';
import { ElMessage } from 'element-plus';

// 引入文件图标组件
import FileIcons from 'file-icons-vue';

export default {
  name: 'FileAttr',
  components: {
    FileIcons,
  },
  setup(props,context)
  {
    // 控制Dialog显示
    const DialogVisilble = ref(false);
    const fileInfo = ref({});
    const fileDir = ref('');
    const rename = ref('');
    
    // 确定
    const confirm = () => {
      // 校验
      if(fileInfo.value.name == rename.value) {
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
    }

    // 关闭
    const closeDialog = (done) => {
      done();
    }

    const reset = () => {
      fileInfo.value = {};
      rename.value = '';
      fileDir.value = '';
      DialogVisilble.value = false;
    }

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
</style>