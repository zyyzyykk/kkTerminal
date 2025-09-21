<template>
  <div ref="aceEditorRef" class="kk-code-editor" ></div>
</template>

<script>
import { ref, onMounted, onUnmounted } from 'vue';
import ace from 'ace-builds/src-noconflict/ace';
import 'ace-builds/src-noconflict/ext-searchbox';
import 'ace-builds/src-noconflict/theme-chrome';


// 引入语言高亮样式
import 'ace-builds/src-noconflict/mode-c_cpp';
import 'ace-builds/src-noconflict/mode-csharp';
import 'ace-builds/src-noconflict/mode-css';
import 'ace-builds/src-noconflict/mode-gitignore';
import 'ace-builds/src-noconflict/mode-golang';
import 'ace-builds/src-noconflict/mode-html';
import 'ace-builds/src-noconflict/mode-ini';
import 'ace-builds/src-noconflict/mode-java';
import 'ace-builds/src-noconflict/mode-javascript';
import 'ace-builds/src-noconflict/mode-json';
import 'ace-builds/src-noconflict/mode-jsp';
import 'ace-builds/src-noconflict/mode-markdown';
import 'ace-builds/src-noconflict/mode-properties';
import 'ace-builds/src-noconflict/mode-python';
import 'ace-builds/src-noconflict/mode-sh';
import 'ace-builds/src-noconflict/mode-sql';
import 'ace-builds/src-noconflict/mode-typescript';
import 'ace-builds/src-noconflict/mode-text';
import 'ace-builds/src-noconflict/mode-xml';
import 'ace-builds/src-noconflict/mode-yaml';

// 引入智能提示
import 'ace-builds/src-noconflict/ext-language_tools';
import 'ace-builds/src-noconflict/snippets/c_cpp';
import 'ace-builds/src-noconflict/snippets/csharp';
import 'ace-builds/src-noconflict/snippets/css';
import 'ace-builds/src-noconflict/snippets/gitignore';
import 'ace-builds/src-noconflict/snippets/golang';
import 'ace-builds/src-noconflict/snippets/html';
import 'ace-builds/src-noconflict/snippets/ini';
import 'ace-builds/src-noconflict/snippets/java';
import 'ace-builds/src-noconflict/snippets/javascript';
import 'ace-builds/src-noconflict/snippets/json';
import 'ace-builds/src-noconflict/snippets/jsp';
import 'ace-builds/src-noconflict/snippets/markdown';
import 'ace-builds/src-noconflict/snippets/properties';
import 'ace-builds/src-noconflict/snippets/python';
import 'ace-builds/src-noconflict/snippets/sh';
import 'ace-builds/src-noconflict/snippets/sql';
import 'ace-builds/src-noconflict/snippets/typescript';
import 'ace-builds/src-noconflict/snippets/text';
import 'ace-builds/src-noconflict/snippets/xml';
import 'ace-builds/src-noconflict/snippets/yaml';

import langToMode from '../preview/Lang';
import { userTCodeExecutorCompleter } from '@/components/tcode/TCode';

export default {
  name: 'AceEditor',
  setup(props, context) {
    const aceEditorRef = ref();
    const aceEditor = ref(null);

    // 赋值内容
    const setValue = (text) => {
      if(aceEditor.value) {
        aceEditor.value.setValue(text);
        aceEditor.value.selection.clearSelection();
        resetEditorScroll();
        resetHistory();
      }
    };
    // 重置滚动位置（左上角）
    const resetEditorScroll = () => {
      if(aceEditor.value) {
        // 滚动到最顶部
        aceEditor.value.scrollToLine(0, true, true, () => {});
        // 滚动到最左边
        aceEditor.value.renderer.scrollToX(0);
      }
    };
    // 删除末尾空行
    const removeEndEmptyLine = () => {
      if(aceEditor.value) {
        const aceDocument = aceEditor.value.getSession().getDocument();
        const lastLineNumber = aceDocument.getLength() - 1;
        const lastLineContent = aceDocument.getLine(lastLineNumber);
        if(!lastLineContent) aceDocument.removeFullLines(lastLineNumber, lastLineNumber + 1);
      }
    };

    // 获取内容
    const getValue = () => {
      if(aceEditor.value) return aceEditor.value.getValue();
      else return '';
    };

    // 清空撤销历史
    const resetHistory = () => {
      if(aceEditor.value) aceEditor.value.getSession().getUndoManager().reset();
    };
    // 设置语言
    const setLanguage = (name) => {
      if(aceEditor.value) aceEditor.value.getSession().setMode(`ace/mode/${langToMode(name)}`);
    };
    // 设置只读模式
    const setReadOnly = (isReadOnly) => {
      if (aceEditor.value) {
        aceEditor.value.setReadOnly(isReadOnly);
        // 只读时设置背景色，非只读时恢复默认
        aceEditorRef.value.style.backgroundColor = isReadOnly ? '#f0f0f0' : '';
      }
    };
    // 设置缩进
    const setTabSize = (tabSize) => {
      if(aceEditor.value) aceEditor.value.setOptions({
        tabSize: tabSize,     // 缩进空格数
        useSoftTabs: false    // 使用Tab
      });
    };
    // 设置字号
    const setFontSize = (fontSize) => {
      if(aceEditor.value) aceEditor.value.setOptions({
        fontSize: fontSize,
      })
    };

    // 保存代码
    const CtrlS = (event) => {
      if (event.ctrlKey || event.metaKey) {
        switch (String.fromCharCode(event.which).toLowerCase()) {
          case 's':
            event.preventDefault();
            context.emit('handleSave', aceEditor.value.getValue());
            break;
        }
      }
    };

    // 重置编辑器
    const reset = () => {
      setValue('');
    };

    // 实例化编辑器
    onMounted(() => {
      aceEditor.value = ace.edit(aceEditorRef.value, {
        autoScrollEditorIntoView: false,
        copyWithEmptySelection: false,
        showPrintMargin: false,
        highlightActiveLine: true,
        // fontFamily: 'monospace',
        enableBasicAutocompletion: true,                      // 启用基本自动补全
        enableSnippets: true,                                 // 启用代码片段
        enableLiveAutocompletion: true,                       // 启用实时自动补全
        theme: 'ace/theme/chrome',                            // 主题
        mode: 'ace/mode/text',                                // 高亮
      });
      // 自定义智能提示
      aceEditor.value.completers.push(userTCodeExecutorCompleter);

      // 禁用 Web Workers
      aceEditor.value.session.setOption("useWorker", false);

      // 编辑 监听内容变化
      aceEditor.value.on('change', () => {
        context.emit('handleChange');
      });

      // 查找替换
      aceEditor.value.find('needle',{
        backwards: false,
        wrap: false,
        caseSensitive: false,
        wholeWord: false,
        regExp: false
      });
      aceEditor.value.findNext();
      aceEditor.value.findPrevious();

      // 监听保存键 ctrl+s
      aceEditorRef.value.addEventListener('keydown', CtrlS);
    });

    onUnmounted(() => {
      if(aceEditorRef.value) aceEditorRef.value.removeEventListener('keydown', CtrlS);
    });

    return {
      aceEditorRef,
      aceEditor,
      setValue,
      resetEditorScroll,
      removeEndEmptyLine,
      resetHistory,
      reset,
      setLanguage,
      getValue,
      setReadOnly,
      setTabSize,
      setFontSize,
    }
  }
}
</script>

<style scoped>
.kk-code-editor {
  height: 100%;
  width: 100%;
  border: 1px solid #ececec;
}
</style>
