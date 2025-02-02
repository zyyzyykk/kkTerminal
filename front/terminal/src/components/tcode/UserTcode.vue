<template>
  <el-dialog
    v-model="DialogVisible"
    :before-close="closeDialog"
    destroy-on-close
    :width="550"
    :title="$t('自定义TCode')"
    :modal="false"
    modal-class="kk-dialog-class"
    header-class="kk-header-class"
    body-class="kk-body-class-0"
    align-center
    draggable
  >
    <div>
      <div class="kk-flex">
        <div class="no-select nowrap form-width" >TCode：</div>
        <div class="no-select nowrap" style="background-color: #f3f4f4; margin-right: 8px;" >U</div>
        <el-input size="small" :style="{width: '130px'}" v-model="userTcodeInfo.name" class="w-50 m-2" :placeholder="$t('输入TCode')" maxlength="5" minlength="1" >
          <template #prefix>
            <el-icon><CollectionTag /></el-icon>
          </template>
        </el-input>
        <div style="flex: 1;" ></div>
        <div>
          <el-upload
            :show-file-list="false"
            :with-credentials="false"
            :http-request="importTcodes"
            :multiple="false"
            >
            <el-button size="small" type="primary" >
              <el-icon class="el-icon--left"><Upload /></el-icon> {{ $t('导入') }}
            </el-button>
          </el-upload>
        </div>
      </div>
      <div class="kk-flex">
        <div class="no-select nowrap form-width" style="margin-right: 2px;" >{{ $t('描\u00A0\u00A0\u00A0述') }}：</div>
        <el-input size="small" :style="{width: '146px'}" v-model="userTcodeInfo.desc" class="w-50 m-2" :placeholder="$t('输入TCode描述')" >
          <template #prefix>
            <el-icon><EditPen /></el-icon>
          </template>
        </el-input>
        <div style="flex: 1;" ></div>
        <div>
          <el-button size="small" type="primary" @click="exportTcodes" >
            <el-icon class="el-icon--left"><Download /></el-icon> {{ $t('导出') }}
          </el-button>
        </div>
      </div>
      <div class="kk-border" ></div>
      <div class="kk-flex" style="margin: 7px 0;" >
        <div class="no-select nowrap">Workflow</div>
        <div style="flex: 1;" ></div>
        <div @click="workflowTab(1)" style="font-size: 18px; cursor: pointer; margin-left: 15px;" >
          <el-tooltip :content="$t('默认模板')" placement="top">
            <el-icon><Refresh /></el-icon>
          </el-tooltip>
        </div>
        <div @click="workflowTab(2)" style="font-size: 18px; cursor: pointer; margin-left: 15px;" >
          <el-tooltip :content="$t('清空')" placement="top">
            <el-icon><DocumentDelete /></el-icon>
          </el-tooltip>
        </div>
        <div @click="workflowTab(3)" style="font-size: 18px; cursor: pointer; margin-left: 15px;" >
          <el-tooltip :content="$t('保存')" placement="top">
            <el-icon><Finished /></el-icon>
          </el-tooltip>
        </div>
      </div>
      <div element-loading-text="Loading..." v-loading="loading" style="padding: 0px 5px; width: 100%; height: 30vh;">
        <AceEditor ref="userTcodeEditorRef" @handleSave="handleSave" ></AceEditor>
      </div>
    </div>
    <div style="display: flex;">
      <div style="flex: 1;"></div>
      <el-button size="small" type="primary" @click="confirm" style="margin-top: 10px;">
        {{ $t('确定') }}
      </el-button>
    </div>
  </el-dialog>
</template>

<script>
import { ref } from 'vue';
import { ElMessage } from 'element-plus';
import AceEditor from '../preview/AceEditor';
import { CollectionTag, EditPen, Upload, Download, Refresh, Finished, DocumentDelete } from '@element-plus/icons-vue';
import i18n from "@/locales/i18n";

export default {
  name: 'UserTcode',
  components: {
    AceEditor,
    CollectionTag,
    EditPen,
    Upload,
    Download,
    Refresh,
    Finished,
    DocumentDelete,
  },
  setup(props,context)
  {
    // 控制Dialog显示
    const DialogVisible = ref(false);
    const loading = ref(false);

    const workflowTemplate = `const path = '/root/terminal';
await kkTerminal.write('cd ' + path, 1200);
const port = 3000;
await kkTerminal.write('lsof -ti :' + port, 1200);
const resultArr = kkTerminal.read();
if(resultArr.length >= 2) {
    const pid = resultArr[1];
	if(pid && /^\\d+$/.test(pid)) await kkTerminal.write('kill -9 ' + pid, 1200);
}
const jar = 'kkTerminal.jar';
await kkTerminal.write('nohup java -jar ./' + jar + ' > ./out.log &', 1200);`;

    const userTcodeEditorRef = ref();
    const userTcodeInfo = ref({
      name:'',
      desc:'',
    });

    // 保存
    const handleSave = (text) => {
      localStorage.setItem('tcode-draft', text);
      ElMessage({
        message: i18n.global.t('保存成功'),
        type: 'success',
        grouping: true,
        repeatNum: Number.MIN_SAFE_INTEGER,
      });
    };
    const setValue = (text) => {
      userTcodeEditorRef.value.setValue(text);
      userTcodeEditorRef.value.resetHistory();
    };
    const initText = () => {
      // Workflow仅支持JS语法
      userTcodeEditorRef.value.setLanguage('kk.js');
      // 加载Draft
      if(localStorage.getItem('tcode-draft')) {
        userTcodeEditorRef.value.setValue(localStorage.getItem('tcode-draft'));
        userTcodeEditorRef.value.resetHistory();
      }
    };
    // 仅有数字字母组成
    const isAlphaNumeric = (str) => {
      const regex = /^[A-Za-z0-9]+$/;
      return regex.test(str);
    };

    // 导入导出Tcode
    const importTcodes = (data) => {
      let file = data.file;
      const fileReader = new FileReader();
      fileReader.onload = () => {
        try {
          const tcodes = JSON.parse(fileReader.result);
          let data = {};
          // 统计成功/失败数
          let scnt = 0;
          let fcnt = 0;
          for (const key in tcodes) {
            if(key && key.length >= 2 && key.length <= 6 && (key[0] == 'U' || key[0] == 'u') && isAlphaNumeric(key)) {
              data[key.toUpperCase()] = {
                desc: tcodes[key].desc || '',
                workflow:  tcodes[key].workflow || '',
                status: 'Not Active',
              };
              scnt++;
            }
            else {
              ElMessage({
                message: 'TCode-' + key + i18n.global.t('：名称非法'),
                type: 'error',
                grouping: true,
              });
              fcnt++;
            }
          }
          // 全部成功
          if(fcnt == 0) {
            context.emit('importTCodes', data);
            ElMessage({
              message: i18n.global.t('导入成功'),
              type: 'success',
              grouping: true,
            });
            closeDialog();
          }
          // 全部失败
          else if(scnt == 0) {
            ElMessage({
              message: i18n.global.t('导入失败：无合法TCode'),
              type: 'error',
              grouping: true,
            });
          }
          // 部分成功
          else {
            context.emit('importTCodes', data);
            ElMessage({
              message: i18n.global.t('导入成功：') + scnt + i18n.global.t('个') + i18n.global.t('，导入失败：') + fcnt + i18n.global.t('个'),
              type: 'warning',
              grouping: true,
            });
          }
        } catch(error) {
          ElMessage({
            message: i18n.global.t('导入失败：json格式错误'),
            type: 'error',
            grouping: true,
          });
        } finally {
          loading.value = false;
        }
      };
      fileReader.onerror = () => {
        ElMessage({
          message: i18n.global.t('导入失败：文件读取失败'),
          type: 'error',
          grouping: true,
        });
        loading.value = false;
      };
      loading.value = true;
      fileReader.readAsText(file);
    };
    const exportTcodes = () => {
      context.emit('exportTcodes');
      ElMessage({
        message: i18n.global.t('导出成功'),
        type: 'success',
        grouping: true,
      });
    };

    // 处理workflowTab
    const workflowTab = (type) => {
      switch(type) {
        case 1:
          // 刷新
          localStorage.removeItem('tcode-draft');
          setValue(workflowTemplate);
          break;
        case 2:
          // 删除
          localStorage.removeItem('tcode-draft');
          setValue('');
          break;
        case 3:
          // 保存
          handleSave(userTcodeEditorRef.value.getValue());
          break;
      }
    };

    // 确定(添加TCode)
    const confirm = () => {
      if(!(userTcodeInfo.value.name && userTcodeInfo.value.name.length >= 1 && userTcodeInfo.value.name.length <= 5)) {
        ElMessage({
          message: i18n.global.t('TCode不能为空'),
          type: 'error',
          grouping: true,
          repeatNum: Number.MIN_SAFE_INTEGER,
        });
        return;
      }
      if(!isAlphaNumeric(userTcodeInfo.value.name)) {
        ElMessage({
          message: i18n.global.t('TCode只能由字母和数字组成'),
          type: 'error',
          grouping: true,
          repeatNum: Number.MIN_SAFE_INTEGER,
        });
        return;
      }
      let data = {};
      data['U' + userTcodeInfo.value.name.toUpperCase()] = {
        desc: userTcodeInfo.value.desc,
        workflow: userTcodeEditorRef.value.getValue(),
        status: 'Not Active',
      };
      context.emit('importTCodes', data);
      ElMessage({
        message: i18n.global.t('添加成功'),
        type: 'success',
        grouping: true,
      });
      localStorage.removeItem('tcode-draft');
      closeDialog();
    };

    // 重置
    const reset = () => {
      if(userTcodeEditorRef.value) userTcodeEditorRef.value.reset();
      loading.value = false;
      userTcodeInfo.value.name = '';
      userTcodeInfo.value.desc = '';
      DialogVisible.value = false;
    };

    // 关闭
    const closeDialog = (done) => {
      setTimeout(() => {
        reset();
      },400);
      DialogVisible.value = false;
      if(done) done();
    };

    return {
      DialogVisible,
      confirm,
      closeDialog,
      reset,
      userTcodeEditorRef,
      handleSave,
      initText,
      loading,
      userTcodeInfo,
      importTcodes,
      exportTcodes,
      setValue,
      workflowTab,
    }
  }
}
</script>

<style scoped>
.kk-flex {
  display: flex;
  align-items: center;
  margin-top: 15px;
}

.kk-border {
  margin-top: 3px;
  margin-bottom: 3px;
  padding-bottom: 5px;
  border-bottom: 1px solid #ececec;
}

.no-select {
  user-select: none;
}
/* 不换行 */
.nowrap {
  white-space: nowrap;
}

.form-width {
  width: 56px;
}

</style>
