<template>
  <div
    id="code-editor"
    ref="codeEditorRef"
    class="kk-code-editor"
  />
</template>

<script>
import * as monaco from "monaco-editor";
import { ref, toRaw, onMounted } from "vue";

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
        console.log(toRaw(codeEditor.value));
        context.emit('handleChange', toRaw(codeEditor.value).getValue());
      });
    });

    return {
      codeEditorRef,
      codeEditor,
      setValue,

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