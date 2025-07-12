<template>
  <div v-resizable="{ maxWidthRate: 1 }" >
    <el-dialog
        v-model="DialogVisible"
        width="80%"
        :modal="false"
        modal-class="kk-dialog-class"
        body-class="kk-body-class-12"
        :before-close="closeDialog"
        align-center
        draggable
    >
      <template #title>
        <div class="kk-flex no-select kk-header-class" >
          <FileIcons :style="{display: 'flex', alignItems: 'center'}" :name="fileName" :width="25" :height="25" :isFolder="false" />
          <div class="ellipsis" style="margin: 0 5px; font-size: larger;" >{{ modifyTag + fileName }}</div>
        </div>
      </template>
      <div style="margin-top: -28px;" ></div>
      <div id="editor-bar" v-show="previewInfo.preview === 'editor'" class="kk-flex ellipsis no-select" style="margin-bottom: 5px; overflow-x: auto;" >
        <div class="kk-flex" >
          <div>{{ $t('编码') }}：</div>
          <div class="kk-flex" >
            <el-tag size="small" type="info" style="margin-right: 5px;" >{{ $t('打开') }}</el-tag>
            <el-dropdown size="small" hide-timeout="300" >
              <span class="a-link" >{{ openEncode }}<el-icon class="el-icon--right" ><arrow-down /></el-icon></span>
              <template #dropdown>
                <el-dropdown-menu>
                  <template v-for="(name,index) in encodeSet" :key="index" >
                    <el-dropdown-item class="no-select" @click="changeEncode(0, name)" >{{ name }}</el-dropdown-item>
                  </template>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
          <div class="kk-flex" >
            <el-tag size="small" type="info" style="margin-left: 10px; margin-right: 5px;" >{{ $t('保存') }}</el-tag>
            <el-dropdown size="small" hide-timeout="300" >
              <span class="a-link" >{{ saveEncode }}<el-icon class="el-icon--right" ><arrow-down /></el-icon></span>
              <template #dropdown>
                <el-dropdown-menu>
                  <template v-for="(name,index) in encodeSet" :key="index" >
                    <el-dropdown-item class="no-select" @click="changeEncode(1, name)" >{{ name }}</el-dropdown-item>
                  </template>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
        <div style="margin-right: 20px;" ></div>
        <div class="kk-flex" >
          <div>{{ $t('模式') }}：</div>
          <el-dropdown size="small" hide-timeout="300" >
            <span class="a-link" >{{ mode }}<el-icon class="el-icon--right" ><arrow-down /></el-icon></span>
            <template #dropdown>
              <el-dropdown-menu>
                <template v-for="(name,index) in modeSet" :key="index" >
                  <el-dropdown-item class="no-select" @click="setMode(name)" >{{ name }}</el-dropdown-item>
                </template>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
        <div style="margin-right: 20px;" ></div>
        <div class="kk-flex" >
          <div>{{ $t('缩进') }}：</div>
          <div><el-input-number :style="{width: '80px'}" size="small" v-model="indent" :min="2" :max="4" step="2" :step-strictly="true" @change="setIndent" /></div>
        </div>
        <div style="margin-right: 20px;" ></div>
        <div class="kk-flex" >
          <div>{{ $t('字号') }}：</div>
          <div>
            <el-input-number :style="{width: '100px'}" size="small" v-model="fontSize" :min="12" :max="20" step="2" :step-strictly="true" @change="setFontSize" >
              <template #suffix>
                <span>px</span>
              </template>
            </el-input-number>
          </div>
        </div>
        <div style="margin-right: 20px;" ></div>
        <div class="kk-flex" >
          <el-button size="small" type="primary" @click="doCopy" >
            <el-icon class="el-icon--left" ><DocumentCopy /></el-icon>{{ $t('复制') }}
          </el-button>
        </div>
      </div>
      <div element-loading-text="Loading..." v-loading="loading" style="width: 100%; height: 60vh; position: relative; margin-bottom: 10px" >
        <AceEditor class="preview" v-show="!loading && previewInfo.preview === 'editor'" ref="codeEditorRef" @handleChange="handleChange" @handleSave="handleSave" ></AceEditor>
        <iframe id="imgPreview" class="preview" v-if="!loading && previewInfo.preview === 'iframe' && previewUrl" :src="previewUrl" ></iframe>
        <audio controls class="preview" v-if="!loading && previewInfo.preview === 'audio' && previewUrl" >
          <source :src="previewUrl" :type="previewInfo.type" >
        </audio>
        <video controls class="preview" v-if="!loading && previewInfo.preview === 'video' && previewUrl" >
          <source :src="previewUrl" :type="previewInfo.type" >
        </video>
        <div style="position: absolute; top: 0; right: 30px" v-if="!loading && previewInfo.preview === 'iframe' && previewInfo.type !== 'application/pdf' && previewInfo.type !== 'text/html' && previewUrl" >
          <el-input-number :style="{width: '105px'}" size="small" v-model="percentage" :min="20" :max="200" step="10" :step-strictly="true" @change="setPercentage" >
            <template #suffix>
              <span>%</span>
            </template>
          </el-input-number>
        </div>
      </div>
      <div style="margin-top: -13px;" ></div>
    </el-dialog>
  </div>
</template>

<script>
import { ref } from 'vue';
import AceEditor from './AceEditor';
import $ from 'jquery';
import useClipboard from "vue-clipboard3";
import { ElMessage } from 'element-plus';
import { previewFileInfo } from '@/components/preview/FileSuffix';
import { changeStr2 } from '@/utils/StringUtil';
import { getUrlParams } from "@/utils/UrlUtil";
import { encodeStrToArray, decodeArrayToStr } from "@/components/preview/EncodeUtil";
import { ArrowDown, DocumentCopy } from '@element-plus/icons-vue';
import i18n from "@/locales/i18n";

// 引入文件图标组件
import FileIcons from 'file-icons-vue';

export default {
  name: 'TxtPreview',
  components: {
    AceEditor,
    FileIcons,
    ArrowDown,
    DocumentCopy,
  },
  setup(props,context) {

    const DialogVisible = ref(false);
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
      const url = fileUrl.value;
      previewInfo.value = previewFileInfo(fileName.value);
      initEncode(url);
      jqXHR.value = $.ajax({
        url: url,
        method: 'GET',
        xhrFields: {
          responseType: 'arraybuffer'
        },
        data: {
          type: 'open',
        },
        dataFilter(resp) {
          return resp;
        },
        success(resp) {
          if(url === fileUrl.value) {
            // 文件可预览
            if(previewInfo.value.preview !== 'editor') {
              const blob = new Blob([resp], {type:previewInfo.value.type});
              if(previewUrl.value) URL.revokeObjectURL(previewUrl.value);
              previewUrl.value = URL.createObjectURL(blob);
              percentage.value = 100;
              imgHeight.value = -1;
            }
            else {
              const text = decodeArrayToStr(new Uint8Array(resp), openEncode.value);
              codeEditorRef.value.setValue(text);
              codeEditorRef.value.resetHistory();
              mode.value = 'auto';
              codeEditorRef.value.setLanguage(fileName.value);
              setIndent();
              setFontSize();
            }
            modifyTag.value = '';
            loading.value = false;
          }
        },
        error() {
          if(url === fileUrl.value) {
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
      if(modifyTag.value !== '*') return;
      context.emit('doSave',fileName.value, fileUrl.value, encodeStrToArray(text, (saveEncode.value || "UTF-8")));
      modifyTag.value = '';
    };

    // 编码
    const openEncode = ref('');
    const saveEncode = ref('');
    const encodeSet = ref(['UTF-8','GBK','ISO-8859-1','Windows-1252']);
    const initEncode = (url) => {
      const urlParams = getUrlParams(url);
      const serverEncode = urlParams.sshKey ? changeStr2(urlParams.sshKey.split('-')[1]) : 'UTF-8';
      encodeSet.value = ['UTF-8','GBK','ISO-8859-1','Windows-1252'];
      // 服务器编码不在默认编码集中
      if(!encodeSet.value.map(item => item.toLowerCase()).includes(serverEncode.toLowerCase())) encodeSet.value.unshift(serverEncode);
      // 设置编码
      if(!openEncode.value) openEncode.value = serverEncode;
      if(!saveEncode.value) saveEncode.value = serverEncode;
    };
    const changeEncode = (type, name) => {
      // 打开编码
      if(type === 0) {
        if(openEncode.value === name) return;
        if(!loading.value) initText();
        openEncode.value = name;
        DialogVisible.value = true;
      }
      // 保存编码
      else if(type === 1) {
        saveEncode.value = name;
        modifyTag.value = '*';
      }
    };

    // 语言模式
    const mode = ref('auto');
    const modeSet = ref(['auto','xml','bash','json']);
    const setMode = (name) => {
      if(mode.value === name) return;
      mode.value = name;
      codeEditorRef.value.setLanguage((name === 'auto') ? fileName.value : ("kk." + name));
    };

    // 缩进
    const indent = ref(4);
    const setIndent = () => {
      codeEditorRef.value.setTabSize(indent.value);
    };

    // 编辑器字号
    const fontSize = ref(14);
    const setFontSize = () => {
      codeEditorRef.value.setFontSize(fontSize.value);
    };

    // 图片百分比
    const percentage = ref(100);
    const imgHeight = ref(-1);
    const getIframeImg = () => {
      const iframe = document.getElementById('imgPreview');
      const iframeDoc = iframe.contentDocument || iframe.contentWindow.document;
      return iframeDoc.querySelector('img');
    };
    const setPercentage = () => {
      const iframeImg = getIframeImg();
      if(imgHeight.value === -1) imgHeight.value = iframeImg.height;
      iframeImg.height = (imgHeight.value * percentage.value) / 100;
    };

    // 拷贝
    const { toClipboard } = useClipboard();

    // 复制
    const doCopy = async () => {
      const content = codeEditorRef.value.getValue();
      if(!(content && content.length > 0)) {
        ElMessage({
          message: i18n.global.t('内容为空'),
          type: 'warning',
          grouping: true,
          repeatNum: Number.MIN_SAFE_INTEGER,
        });
        return;
      }
      await toClipboard(content);
      ElMessage({
        message: i18n.global.t('复制成功'),
        type: 'success',
        grouping: true,
        repeatNum: Number.MIN_SAFE_INTEGER,
      });
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
      if(previewUrl.value) {
        URL.revokeObjectURL(previewUrl.value);
        previewUrl.value = '';
      }
      if(jqXHR.value) {
        jqXHR.value.abort();
        jqXHR.value = null;
      }
      openEncode.value = '';
      saveEncode.value = '';
      encodeSet.value = ['UTF-8','GBK','ISO-8859-1','Windows-1252'];
      mode.value = 'auto';
      // indent.value = 4;
      // fontSize.value = 14;
      percentage.value = 100;
      imgHeight.value = -1;
      DialogVisible.value = false;
    };

    // 关闭
    const closeDialog = (done) => {
      setTimeout(() => {
        reset(true);
      },400);
      DialogVisible.value = false;
      if(done) done();
    };

    return {
      DialogVisible,
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
      openEncode,
      saveEncode,
      changeEncode,
      encodeSet,
      initEncode,
      mode,
      modeSet,
      setMode,
      indent,
      setIndent,
      fontSize,
      setFontSize,
      percentage,
      imgHeight,
      setPercentage,
      doCopy,
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

/* 文本不可选中 */
.no-select {
  user-select: none;
}

.preview {
  width: 100%;
  height: 100%;
}

/* 隐藏滚动条 */
#editor-bar {
  scrollbar-width: none !important; /* Firefox */
  -ms-overflow-style: none !important; /* Internet Explorer 和 Edge */
}

#editor-bar::-webkit-scrollbar {
  display: none !important; /* Chrome 和 Safari */
}
</style>
