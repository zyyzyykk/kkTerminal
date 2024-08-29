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
    <div>
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
      <div class="kk-flex">
        <div class="no-select" style="text-align: left; width: 100px;">大小：</div>
        <div>
          {{ calcSize(fileInfo.attributes.size) }} ({{ fileInfo.attributes.size + ' 字节' }})
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
import { formatDate } from '../Utils/FormatDate';
import { calcPriority } from '../Utils/CalcPriority';
import { ElMessage } from 'element-plus';
import useClipboard from "vue-clipboard3";
import { DocumentCopy } from '@element-plus/icons-vue';
import { calcSize } from '../Utils/CalcSize';

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