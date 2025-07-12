<template>
  <el-dialog
    v-model="DialogVisible"
    :before-close="closeDialog"
    :width="320"
    :modal="false"
    :title="$t('导入私钥')"
    modal-class="kk-dialog-class"
    header-class="kk-header-class"
    body-class="kk-body-class-0"
    draggable
  >
    <div class="no-select" >
      <div class="kk-flex" >
        <div class="form-width" >{{ $t('私钥') }}：</div>
        <el-upload
          :show-file-list="false"
          :with-credentials="true"
          :http-request="readPrivateKeyContent"
          :multiple="false"
        >
          <el-button size="small" type="primary" ><el-icon class="el-icon--left" ><Lock /></el-icon>{{ $t('选择') }}</el-button>
        </el-upload>
      </div>
      <div v-if="err_msg && err_msg.length > 0" class="errInfo no-select" > {{ err_msg }} </div>
      <div v-else style="height: 7px;" ></div>
      <div class="kk-flex" >
        <div class="form-width" >{{ $t('密码') }}：</div>
        <div style="flex: 1;" >
          <el-input size="small" v-model="passphrase" @keydown.enter="confirm" class="w-50 m-2" :placeholder="$t('请输入私钥密码')" >
            <template #prefix>
              <el-icon><Key /></el-icon>
            </template>
          </el-input>
        </div>
        <div style="margin-left: 10px;" >
          <el-button size="small" type="primary" @click="confirm" >{{ $t('确定') }}</el-button>
        </div>
      </div>
    </div>
  </el-dialog>
</template>

<script>
import { ref } from 'vue';
import { ElMessage } from 'element-plus';
import { Lock, Key } from '@element-plus/icons-vue';
import i18n from "@/locales/i18n";

export default {
  name: 'PrivateKey',
  components: {
    Lock,
    Key,
  },
  setup(props,context) {
    // 控制Dialog显示
    const DialogVisible = ref(false);
    const err_msg = ref('');
    const content = ref('');
    const passphrase = ref('');

    // 确定
    const confirm = () => {
      context.emit('callback',content.value, passphrase.value);
      closeDialog();
    };

    // 读取私钥文件
    const readPrivateKeyContent = (file) => {
      const reader = new FileReader();
      reader.onload = () => {
        if(reader.result.length > 1024*1024) {
          ElMessage({
            message: i18n.global.t('内容过长'),
            type: 'warning',
            grouping: true,
          });
          return;
        }
        content.value = reader.result;
        ElMessage({
          message: i18n.global.t('读取成功'),
          type: 'success',
          grouping: true,
        });
      };
      reader.onerror = () => {
        ElMessage({
          message: i18n.global.t('读取失败'),
          type: 'error',
          grouping: true,
        });
      };
      reader.readAsText(file.file);
    };

    // 重置
    const reset = () => {
      err_msg.value = '';
      content.value = '';
      passphrase.value = '';
      DialogVisible.value = false;
    };

    // 关闭
    const closeDialog = (done) => {
      setTimeout(() => {
        reset();
      },400);
      DialogVisible.value = false;
      if(done) done();
    };

    return {
      DialogVisible,
      err_msg,
      content,
      passphrase,
      readPrivateKeyContent,
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

.form-width {
  width: 56px;
}
</style>
