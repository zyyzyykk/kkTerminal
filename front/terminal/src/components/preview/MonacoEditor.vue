<template>
  <div
    id="code-editor"
    ref="codeEditorRef"
    class="kk-code-editor"
  />
</template>

<script>
// 已采用 AceEditor，此组件被废弃

// By default, all languages shipped with the monaco-editor will be included.
// 默认情况下monaco-editor会包含所有的语言文件
import * as monaco from 'monaco-editor/esm/vs/editor/editor.api';
// import * as monaco from 'monaco-editor';
import { ref, toRaw, onMounted, onUnmounted } from "vue";

export default {
  name: 'MonacoEditor',
  components: {
  },
  setup(props,context) {

    const codeEditorRef = ref();
    const codeEditor = ref();

    // 赋值内容
    const setValue = (text) => {
      const model = toRaw(codeEditor.value).getModel();
      const range = model.getFullModelRange();
      model.pushEditOperations(null, [{ range, text: text }]);
    }

    // 修改语言
    const setLanguage = (lang) => {
      monaco.editor.setModelLanguage(toRaw(codeEditor.value).getModel(), lang);
    }

    const CtrlS = (event) => {
      if (event.ctrlKey || event.metaKey) {
        switch (String.fromCharCode(event.which).toLowerCase()) {
          case 's':
            event.preventDefault();
            context.emit('handleSave', toRaw(codeEditor.value).getValue());
            break;
        }
      }
    };

    onMounted(() => {
      if (!codeEditorRef.value) return;
      codeEditor.value = monaco.editor.create(codeEditorRef.value, {
        value: '',
        language: '',
        automaticLayout: true,
        colorDecorators: true,
        minimap: {
          enabled: true,
        },
        readOnly: false,
        theme: "vs",
        // 语言为简体中文
        locale: "zh-cn",
        // lineNumbers: "off",
        // roundedSelection: false,
        // scrollBeyondLastLine: false,
      });

      // 编辑 监听内容变化
      codeEditor.value.onDidChangeModelContent(() => {
        // 当前最新内容
        // console.log(toRaw(codeEditor.value).getValue());
        context.emit('handleChange');
      });

      // 监听保存键 ctrl+s
      codeEditorRef.value.addEventListener('keydown', CtrlS);

    });

    onUnmounted(() => {
      if(codeEditorRef.value) codeEditorRef.value.removeEventListener('keydown', CtrlS);
    });

    return {
      codeEditorRef,
      codeEditor,
      setValue,
      CtrlS,
      setLanguage,

    }
  }
}
</script>

<style scoped>
.kk-code-editor
{
  height: 100%; 
  width: 100%;
  border: 1px solid #ececec;
}
</style>