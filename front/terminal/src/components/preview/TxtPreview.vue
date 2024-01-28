<template>
  <el-dialog
    v-model="DialogVisilble"
    destroy-on-close
    width="80%"
    :title="fileName"
    :modal="false"
    modal-class="kk-dialog-class"
    top="10vh"
    draggable
  >
    <template #title>
      <div class="kk-flex">
        <FileIcons :name="fileName" width="20" height="20" :isFloder="false" />
        <div style="margin: 0 5px;">{{ fileName }}</div>
      </div>
    </template>
    <div style="margin-top: -15px;"></div>
    <div element-loading-text="Loading..." v-loading="loading" style="padding: 0px 5px; width: 100%; height: 60vh;">
      <CodeEditor ref="codeEditorRef" :value="text" ></CodeEditor>
    </div>
  </el-dialog>
</template>

<script>
import { ref } from 'vue';
import CodeEditor from './CodeEditor';
import $ from 'jquery';

// 引入文件图标组件
import FileIcons from 'file-icons-vue'

export default {
  name: 'TxtPreview',
  components: {
    CodeEditor,
    FileIcons,
  },
  setup() {

    const DialogVisilble = ref(false);
    const loading = ref(false);

    const fileName = ref('');
    const fileUrl = ref('');

    const codeEditorRef = ref();

    const initText = async () => {
      await $.ajax({
        url: fileUrl.value,
        method: 'GET',
        xhrFields: {
          responseType: "text",
        },
        dataFilter(resp) {
          return resp;
        },
        success(resp) {
          codeEditorRef.value.setValue(resp);
          loading.value = false;
        },
        error() {
          console.log('error');
        }
      });
    }
    

    return {
      DialogVisilble,
      initText,
      loading,
      fileName,
      fileUrl,
      codeEditorRef,
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