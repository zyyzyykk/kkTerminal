<template>
  <el-dialog
    v-model="DialogVisilble"
    :before-close="closeDialog"
    :width="300"
    :modal="false"
    :title="$t('新建')"
    modal-class="kk-dialog-class"
    header-class="kk-header-class"
    body-class="kk-body-class-0"
    draggable
  >
    <div class="no-select">
      <el-radio-group v-model="isDirectory" class="ml-4">
        <el-radio :label="false" size="large">{{ $t('文件') }}</el-radio>
        <el-radio :label="true" size="large">{{ $t('文件夹') }}</el-radio>
      </el-radio-group>
      <div class="errInfo no-select"> {{ err_msg }} </div>
      <div class="kk-flex">
        <div>
          <FileIcons :style="{display: 'flex', alignItems: 'center'}" :width="24" :height="24" v-if="isDirectory == true" name="kk.txt" :isFolder="true" />
          <FileIcons :style="{display: 'flex', alignItems: 'center'}" :width="24" :height="24" v-else name="kk.txt" :isFolder="false" />
        </div>
        <div style="width: 10px;"></div>
        <div style="flex: 1;">
          <el-input size="small" v-model="name" @keydown.enter="confirm" class="w-50 m-2" placeholder="">
          </el-input>
        </div>
        <div style="margin-left: 10px;">
          <el-button size="small" type="primary" @click="confirm" >{{ $t('确定') }}</el-button>
        </div>
      </div>
    </div>
  </el-dialog>
</template>

<script>
import { ref } from 'vue';
import i18n from "@/locales/i18n";
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
        err_msg.value = i18n.global.t('文件名不能为空');
        return;
      }
      const invalidNameRe = /[/|]/;
      if(invalidNameRe.test(name.value)) {
        err_msg.value = i18n.global.t("文件名不能含有") + " |,/";
        return;
      }
      context.emit('callback', isDirectory.value, name.value, nowDir.value);
      closeDialog();
    };

    // 重置
    const reset = () => {
      err_msg.value = '';
      isDirectory.value = false;
      name.value = '';
      nowDir.value = '';
      DialogVisilble.value = false;
    };

    // 关闭
    const closeDialog = (done) => {
      setTimeout(() => {
        reset();
      },400);
      DialogVisilble.value = false;
      if(done) done();
    };

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
