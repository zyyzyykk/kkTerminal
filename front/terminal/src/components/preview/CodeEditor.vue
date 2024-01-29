<template>
  <div
    id="code-editor"
    ref="codeEditorRef"
    class="kk-code-editor"
  />
</template>

<script>
import * as monaco from "monaco-editor";
import { ref, toRaw, onMounted, onUnmounted } from "vue";

export default {
  name: 'CodeEditor',
  components: {
  },
  setup(props,context) {

    const codeEditorRef = ref();
    const codeEditor = ref();

    const setValue = (text) => {
      const model = toRaw(codeEditor.value).getModel();
      const range = model.getFullModelRange();
      model.pushEditOperations(null, [{ range, text: text }]);
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