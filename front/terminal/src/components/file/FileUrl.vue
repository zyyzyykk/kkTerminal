<template>
  <el-dialog
    v-model="DialogVisible"
    :before-close="closeDialog"
    :width="320"
    :modal="false"
    :title="$t('URL上传')"
    modal-class="kk-dialog-class"
    header-class="kk-header-class"
    body-class="kk-body-class-0"
    draggable
  >
    <div class="no-select" >
      <div class="kk-flex" >
        <div class="form-width" >URL：</div>
        <div style="flex: 1;" >
          <el-input size="small" v-model="url" class="w-50 m-2" :placeholder="$t('请输入文件URL')" >
            <template #prefix>
              <el-icon class="el-input__icon" ><Connection /></el-icon>
            </template>
          </el-input>
        </div>
      </div>
      <div v-if="err_msg && err_msg.length > 0" class="error-text no-select" > {{ $t(err_msg) }} </div>
      <div v-else style="height: 7px;" ></div>
      <div class="kk-flex" >
        <div class="form-width" >{{ $t('文件名') }}：</div>
        <div style="flex: 1;" >
          <el-input size="small" v-model="name" @keydown.enter="confirm" class="w-50 m-2" :placeholder="$t('请输入文件名')" >
            <template #prefix>
              <el-icon class="el-input__icon" ><Document /></el-icon>
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
import { Connection, Document } from '@element-plus/icons-vue';
import i18n from "@/locales/i18n";

export default {
  name: 'FileUrl',
  components: {
    Connection,
    Document,
  },
  setup(props, context) {
    // 控制Dialog显示
    const DialogVisible = ref(false);
    const err_msg = ref('');
    const name = ref('');
    const url = ref('');

    // 确定
    const confirm = () => {
      // 校验
      err_msg.value = '';
      if(!(url.value && url.value.trim().length > 0)) {
        err_msg.value = i18n.global.k('URL不能为空');
        return;
      }
      if(!(name.value && name.value.trim().length > 0)) {
        err_msg.value = i18n.global.k('文件名不能为空');
        return;
      }
      const invalidNameRe = /[/|]/;
      if(invalidNameRe.test(name.value)) {
        err_msg.value = i18n.global.k("文件名不能含有") + " |,/";
        return;
      }
      context.emit('callback',url.value, name.value);
      closeDialog();
    };

    // 重置
    const reset = () => {
      err_msg.value = '';
      name.value = '';
      url.value = '';
      DialogVisible.value = false;
    };

    // 关闭
    const closeDialog = (done) => {
      setTimeout(() => {
        reset();
      }, 400);
      DialogVisible.value = false;
      if(done) done();
    };

    return {
      DialogVisible,
      err_msg,
      name,
      url,
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

.form-width {
  width: 56px;
}
</style>
