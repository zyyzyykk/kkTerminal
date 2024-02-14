<template>
  <el-dialog
    v-model="DialogVisilble"
    :before-close="closeDialog"
    :width="300"
    :modal="false"
    title="新建"
    modal-class="kk-dialog-class"
    draggable
  >
    <div style="margin-top: -32px;"></div>
    <div class="no-select">
      <el-radio-group v-model="isDirectory" class="ml-4">
        <el-radio :label="false" size="large">文件</el-radio>
        <el-radio :label="true" size="large">文件夹</el-radio>
      </el-radio-group>
      <div class="errInfo no-select"> {{ err_msg }} </div>
      <div class="kk-flex">
        <div>
          <FileIcons width="24" height="24" v-if="isDirectory == true" name="kk.txt" :isFloder="true" />
          <FileIcons width="24" height="24" v-else name="kk.txt" :isFloder="false" />
        </div>
        <div style="width: 10px;"></div>
        <div style="flex: 1;">
          <el-input size="small" v-model="name" class="w-50 m-2" placeholder="">
          </el-input>
        </div>
        <div style="margin-left: 10px;">
          <el-button size="small" type="primary" @click="confirm" >确定</el-button>
        </div>
      </div>
    </div>
    <div style="margin-top: -10px;"></div>
  </el-dialog>
</template>

<script>
import { ref } from 'vue';
// 引入文件图标组件
import FileIcons from 'file-icons-vue';

export default {
  name: 'MkFile',
  components: {
    FileIcons,
  },
  setup(props,context)
  {
    // 控制Dialog显示
    const DialogVisilble = ref(false);
    const err_msg = ref('');
    const isDirectory = ref(false);
    const name = ref(''); 
    const nowDir = ref('');
    
    // 确定
    const confirm = () => {
      // 校验
      err_msg.value = '';
      if(!(name.value && name.value.trim().length > 0)) {
        err_msg.value = '文件名不能为空';
        return;
      }
      if(name.value.indexOf('/') != -1) {
        err_msg.value = '文件名不能含有 /';
        return;
      }
      context.emit('callback', isDirectory.value, name.value, nowDir.value);
      reset();
    }

    // 关闭
    const closeDialog = (done) => {
      setTimeout(() => {
        reset();
      },200);
      done();
    };

    const reset = () => {
      err_msg.value = '';
      isDirectory.value = false;
      name.value = '';
      nowDir.value = '';
      DialogVisilble.value = false;
    }

    return {
      DialogVisilble,
      err_msg,
      isDirectory,
      name,
      nowDir,
      confirm,
      closeDialog,
      reset,

    }
  }
}
</script>

<style scoped>
.kk-flex {
  display: flex; 
  align-items: center;
  margin-top: 10px;
}

/* 文本不可选中 */
.no-select {
  user-select: none;
}

.errInfo{
  font-size: 12px;
  color: rgb(234, 80, 80);
  margin-top: 8px;
}
</style>