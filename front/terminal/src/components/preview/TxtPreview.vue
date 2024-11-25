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
      <div class="kk-flex">
        <FileIcons :style="{display: 'flex', alignItems: 'center'}" :name="fileName" :width="25" :height="25" :isFolder="false" />
        <div class="ellipsis" style="margin: 0 5px; font-size: larger;">{{ modifyTag + fileName }}</div>
      </div>
    </template>
    <div style="margin-top: -28px;"></div>
    <div element-loading-text="Loading..." v-loading="loading" style="padding: 0px 5px; width: 100%; height: 60vh;">
      <AceEditor class="preview" v-show="!loading && previewInfo.preview == 'editor'" ref="codeEditorRef" @handleChange="handleChange" @handleSave="handleSave" ></AceEditor>
      <iframe class="preview" v-if="!loading && previewInfo.preview == 'iframe' && previewUrl != ''" :src="previewUrl" ></iframe>
      <audio controls class="preview" v-if="!loading && previewInfo.preview == 'audio' && previewUrl != ''" >
        <source :src="previewUrl" :type="previewInfo.type" >
      </audio>
      <video controls class="preview" v-if="!loading && previewInfo.preview == 'video' && previewUrl != ''" >
        <source :src="previewUrl" :type="previewInfo.type" >
      </video>
    </div>
    <div style="margin-top: -13px;"></div>
  </el-dialog>
</template>

<script>
import { ref } from 'vue';
import AceEditor from './AceEditor';
import $ from 'jquery';
import { ElMessage } from 'element-plus';
import { previewFileInfo } from '@/utils/FileSuffix';

// 引入文件图标组件
import FileIcons from 'file-icons-vue';

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

    const previewInfo = ref({
      preview:'editor',
      type:'text',
    });
    const previewUrl = ref('');

    const codeEditorRef = ref();

    const jqXHR = ref(null);

    const initText = async () => {
      loading.value = true;
      reset();
      let url = fileUrl.value;
      jqXHR.value = $.ajax({
        url: url,
        method: 'GET',
        xhrFields: {
          responseType: 'arraybuffer'
        },
        dataFilter(resp) {
          return resp;
        },
        success(resp) {
          if(url == fileUrl.value)
          {
            previewInfo.value = previewFileInfo(fileName.value);
            // 文件可预览
            if(previewInfo.value.preview != 'editor') {
              const blob = new Blob([resp], {type:previewInfo.value.type});
              if(previewUrl.value != '') URL.revokeObjectURL(previewUrl.value);
              previewUrl.value = URL.createObjectURL(blob);
            }
            else {
              const decoder = new TextDecoder('utf-8');
              const text = decoder.decode(new Uint8Array(resp));
              codeEditorRef.value.setValue(text);
              codeEditorRef.value.resetHistory();
              codeEditorRef.value.setLanguage(fileName.value);
            }
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
            reset();
            closeDialog();
          }
        }
      });
      await jqXHR.value;
    };

    const handleChange = () => {
      modifyTag.value = '*';
    };

    const handleSave = (text) => {
      if(modifyTag.value != '*') return;
      context.emit('doSave',fileName.value, fileUrl.value, text);
      modifyTag.value = '';
    };

    // 重置
    const reset = (deep=false) => {
      if(deep) {
        fileName.value = '';
        fileUrl.value = '';
        loading.value = false;
      }
      if(codeEditorRef.value) codeEditorRef.value.reset();
      modifyTag.value = '';
      previewInfo.value = {
        preview:'editor',
        type:'text',
      };
      if(previewUrl.value != '') {
        URL.revokeObjectURL(previewUrl.value);
        previewUrl.value = '';
      }
      if(jqXHR.value) {
        jqXHR.value.abort();
        jqXHR.value = null;
      }
      DialogVisilble.value = false;
    };

    // 关闭
    const closeDialog = (done) => {
      setTimeout(() => {
        reset(true);
      },400);
      DialogVisilble.value = false;
      if(done) done();
    };
    

    return {
      DialogVisilble,
      initText,
      loading,
      fileName,
      fileUrl,
      previewInfo,
      previewUrl,
      codeEditorRef,
      modifyTag,
      handleChange,
      handleSave,
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
}

/* 文本溢出省略 */
.ellipsis {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  line-height: 22px;
}

.preview {
  width: 100%;
  height: 100%;
}

</style>