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
    <div v-show="previewInfo.preview == 'editor'" class="kk-flex" style="margin-bottom: 5px;" >
      <div class="kk-flex" >
        <div>{{ $t('保存编码') }}：</div>
        <el-dropdown size="small" hide-timeout="400" >
          <span class="a-link" >{{ encode }}<el-icon class="el-icon--right"><arrow-down /></el-icon></span>
          <template #dropdown>
            <el-dropdown-menu>
              <template v-for="(name,index) in encodeSet" :key="index" >
                <el-dropdown-item @click="encode = name; modifyTag = '*';" >{{ name }}</el-dropdown-item>
              </template>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>
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
import { changeStr2 } from '@/utils/StringUtil';
import { detectEncoding, encodeStrToArray, decodeArrayToStr } from "@/components/preview/EncodeUtil";
import { ArrowDown } from '@element-plus/icons-vue';
import i18n from "@/locales/i18n";

// 引入文件图标组件
import FileIcons from 'file-icons-vue';

export default {
  name: 'TxtPreview',
  components: {
    AceEditor,
    FileIcons,
    ArrowDown,
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
      previewInfo.value = previewFileInfo(fileName.value);
      initEncode(url);
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
            // 文件可预览
            if(previewInfo.value.preview != 'editor') {
              const blob = new Blob([resp], {type:previewInfo.value.type});
              if(previewUrl.value != '') URL.revokeObjectURL(previewUrl.value);
              previewUrl.value = URL.createObjectURL(blob);
            }
            else {
              encode.value = detectEncoding(String.fromCharCode(...new Uint8Array(resp.slice(0,100*1024))));
              initEncode(url);
              const text = decodeArrayToStr(new Uint8Array(resp),encode.value);
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
              message: i18n.global.t('文件加载失败'),
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
      context.emit('doSave',fileName.value, fileUrl.value, encodeStrToArray(text, (encode.value || "UTF-8")));
      modifyTag.value = '';
    };

    // 编码
    const encode = ref('');
    const encodeSet = ref(['UTF-8','GBK','ISO-8859-1','Windows-1252']);
    const initEncode = (url) => {
      let serverEncode = 'UTF-8';
      let indexKey = url.indexOf('&sshKey=');
      let indexPath = url.indexOf('&path=');
      if(indexKey != -1 && indexPath != -1) serverEncode = changeStr2(url.substring(indexKey + 8, indexPath).split('-')[1]);
      encodeSet.value = ['UTF-8','GBK','ISO-8859-1','Windows-1252'];
      if(!encodeSet.value.map(item => item.toLowerCase()).includes(serverEncode.toLowerCase())) encodeSet.value.unshift(serverEncode);
      if(!encode.value) encode.value = serverEncode;
      else if(!encodeSet.value.map(item => item.toLowerCase()).includes(encode.value.toLowerCase())) encodeSet.value.unshift(encode.value);
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
      encode.value = '';
      encodeSet.value = ['UTF-8','GBK','ISO-8859-1','Windows-1252'];
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
      encode,
      encodeSet,
      initEncode,
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
