<template>
  <el-dialog
    v-model="DialogVisilble"
    width="80%"
    :modal="false"
    modal-class="kk-dialog-class"
    :before-close="closeDialog"
    align-center
    draggable
  >
    <template #title>
      <div v-if="DialogVisilble" class="kk-flex">
        <FileIcons :name="fileName" width="25" height="25" :isFloder="false" />
        <div style="margin: 0 5px; font-size: larger;">{{ modifyTag + fileName }}</div>
      </div>
    </template>
    <div style="margin-top: -28px;"></div>
    <div element-loading-text="Loading..." v-loading="loading" style="padding: 0px 5px; width: 100%; height: 60vh;">
      <AceEditor ref="codeEditorRef" :value="text" @handleChange="handleChange" @handleSave="handleSave" ></AceEditor>
    </div>
    <div style="margin-top: -13px;"></div>
  </el-dialog>
</template>

<script>
import { ref } from 'vue';
import AceEditor from './AceEditor';
import $ from 'jquery';
import { ElMessage } from 'element-plus'

// 引入文件图标组件
import FileIcons from 'file-icons-vue'

export default {
  name: 'TxtPreview',
  components: {
    AceEditor,
    FileIcons,
  },
  setup(props,context) {

    const DialogVisilble = ref(false);
    const loading = ref(false);

    const modifyTag = ref('');
    const fileName = ref('');
    const fileUrl = ref('');

    const codeEditorRef = ref();

    const initText = async () => {
      let url = fileUrl.value;
      await $.ajax({
        url: url,
        method: 'GET',
        dataType: 'text',
        dataFilter(resp) {
          return resp;
        },
        success(resp) {
          if(url == fileUrl.value)
          {
            codeEditorRef.value.setValue(resp);
            modifyTag.value = '';
            loading.value = false;
          }
        },
        error() {
          if(url == fileUrl.value)
          {
            ElMessage({
              message: '文件加载失败',
              type: 'error',
              grouping: true,
            });
            resetEditor();
            DialogVisilble.value = false;
          }
        }
      });
    }

    const handleChange = () => {
      modifyTag.value = '*';
    }

    const handleSave = (text) => {
      if(modifyTag.value != '*') return;
      context.emit('doSave',fileName.value, fileUrl.value, text);
      modifyTag.value = '';
    }

    const resetEditor = () => {
      if(codeEditorRef.value) codeEditorRef.value.setValue('');
      modifyTag.value = '';
    }

    const closeDialog = (done) => {
      setTimeout(() => {
        resetEditor();
      },200);
      done();
    }
    

    return {
      DialogVisilble,
      initText,
      loading,
      fileName,
      fileUrl,
      codeEditorRef,
      modifyTag,
      handleChange,
      handleSave,
      closeDialog,
      resetEditor,
    }
  }
}
</script>

<style scoped>
.kk-flex {
  display: flex;
  align-items: center;
}
</style>