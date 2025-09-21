<template>
  <div v-resizable="{ minWidthRate: 1, maxWidthRate: 1.5 }" >
    <el-dialog
        v-model="DialogVisible"
        :before-close="closeDialog"
        destroy-on-close
        :width="550"
        :title="$t('终端代码工作流')"
        :modal="false"
        modal-class="kk-dialog-class"
        header-class="kk-header-class"
        body-class="kk-body-class-0"
        align-center
        draggable
    >
      <div>
        <div class="kk-flex" >
          <div class="no-select nowrap form-width" >{{ $t('名称') }}：</div>
          <div class="no-select nowrap" style="background-color: #f3f4f4; margin-right: 8px;" >U</div>
          <el-input size="small" :style="{width: '190px'}" v-model="userTCodeInfo.name" class="w-50 m-2" :placeholder="$t('输入终端代码名称')" maxlength="5" minlength="1" >
            <template #prefix>
              <el-icon><CollectionTag /></el-icon>
            </template>
          </el-input>
          <div style="flex: 1;" ></div>
          <div>
            <el-upload
                :show-file-list="false"
                :with-credentials="false"
                :http-request="importTCodes"
                :multiple="false"
            >
              <el-button size="small" type="primary" >
                <el-icon class="el-icon--left" ><Upload /></el-icon> {{ $t('导入') }}
              </el-button>
            </el-upload>
          </div>
        </div>
        <div class="kk-flex" >
          <div class="no-select nowrap form-width" style="margin-right: 2px;" >{{ $t('描述') }}：</div>
          <el-input size="small" :style="{width: '206px'}" v-model="userTCodeInfo.desc" class="w-50 m-2" :placeholder="$t('输入终端代码描述')" >
            <template #prefix>
              <el-icon><EditPen /></el-icon>
            </template>
          </el-input>
          <div style="flex: 1;" ></div>
          <div>
            <el-button size="small" type="primary" @click="exportTCodes" >
              <el-icon class="el-icon--left" ><Download /></el-icon> {{ $t('导出') }}
            </el-button>
          </div>
        </div>
        <div class="kk-border" ></div>
        <div class="kk-flex" style="margin: 7px 0;" >
          <div class="no-select nowrap" >Workflow</div>
          <div style="flex: 1;" ></div>
          <div @click="workflowTab(1)" style="font-size: 18px; cursor: pointer; margin-left: 15px;" >
            <el-tooltip :content="$t('默认模板')" placement="top" >
              <el-icon><Refresh /></el-icon>
            </el-tooltip>
          </div>
          <div @click="workflowTab(2)" style="font-size: 18px; cursor: pointer; margin-left: 15px;" >
            <el-tooltip :content="$t('清空')" placement="top" >
              <el-icon><DocumentDelete /></el-icon>
            </el-tooltip>
          </div>
          <div @click="workflowTab(3)" style="font-size: 18px; cursor: pointer; margin-left: 15px;" >
            <el-tooltip :content="$t('保存')" placement="top" >
              <el-icon><Finished /></el-icon>
            </el-tooltip>
          </div>
        </div>
        <div element-loading-text="Loading..." v-loading="loading" style="width: 100%; height: 30vh;" >
          <AceEditor ref="userTCodeEditorRef" @handleSave="handleSave" ></AceEditor>
        </div>
      </div>
      <div style="display: flex;" >
        <div style="flex: 1;" ></div>
        <el-button size="small" type="primary" @click="confirm" style="margin-top: 10px;" >
          {{ $t('确定') }}
        </el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { ref } from 'vue';
import { ElMessage } from 'element-plus';
import AceEditor from '../preview/AceEditor';
import { CollectionTag, EditPen, Upload, Download, Refresh, Finished, DocumentDelete } from '@element-plus/icons-vue';
import i18n from "@/locales/i18n";
import { localStore } from "@/env/Store";
import { localStoreUtil } from "@/utils/CloudUtil";

export default {
  name: 'TCodeWorkflow',
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

    const userTCodeEditorRef = ref();
    const userTCodeInfo = ref({
      name:'',
      desc:'',
    });

    // 保存
    const handleSave = (text) => {
      localStoreUtil.setItem(localStore['tcode-draft'], text);
      ElMessage({
        message: i18n.global.t('保存成功'),
        type: 'success',
        grouping: true,
        repeatNum: Number.MIN_SAFE_INTEGER,
      });
    };
    const setValue = (text) => {
      userTCodeEditorRef.value.setValue(text);
    };
    const initText = () => {
      // 工作流仅支持JS语法
      userTCodeEditorRef.value.setLanguage('kk.js');
      // 加载Draft
      if(localStoreUtil.getItem(localStore['tcode-draft'])) {
        userTCodeEditorRef.value.setValue(localStoreUtil.getItem(localStore['tcode-draft']));
      }
    };
    // 仅有数字字母组成
    const isAlphaNumeric = (str) => {
      const regex = /^[A-Za-z0-9]+$/;
      return regex.test(str);
    };

    // 导入导出终端代码
    const importTCodes = (data) => {
      const file = data.file;
      const fileReader = new FileReader();
      fileReader.onload = () => {
        try {
          const tcodes = JSON.parse(fileReader.result);
          const data = {};
          // 统计成功/失败数
          let scnt = 0;
          let fcnt = 0;
          for (const key in tcodes) {
            if(key && key.length >= 2 && key.length <= 6 && (key[0] === 'U' || key[0] === 'u') && isAlphaNumeric(key)) {
              data[key.toUpperCase()] = {
                desc: tcodes[key].desc || '',
                workflow:  tcodes[key].workflow || '',
                status: 'Not Active',
              };
              scnt++;
            }
            else {
              ElMessage({
                message: i18n.global.t('终端代码') + ' ' + key + ' ' + i18n.global.t('名称非法'),
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
              message: i18n.global.t('导入失败：无合法终端代码'),
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
    const exportTCodes = () => {
      context.emit('exportTCodes');
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
          localStoreUtil.removeItem(localStore['tcode-draft']);
          setValue(workflowTemplate);
          break;
        case 2:
          // 删除
          localStoreUtil.removeItem(localStore['tcode-draft']);
          setValue('');
          break;
        case 3:
          // 保存
          handleSave(userTCodeEditorRef.value.getValue());
          break;
      }
    };

    // 添加终端代码确定
    const confirm = () => {
      if(!(userTCodeInfo.value.name && userTCodeInfo.value.name.length >= 1 && userTCodeInfo.value.name.length <= 5)) {
        ElMessage({
          message: i18n.global.t('终端代码名称不能为空'),
          type: 'error',
          grouping: true,
          repeatNum: Number.MIN_SAFE_INTEGER,
        });
        return;
      }
      if(!isAlphaNumeric(userTCodeInfo.value.name)) {
        ElMessage({
          message: i18n.global.t('终端代码只能由字母和数字组成'),
          type: 'error',
          grouping: true,
          repeatNum: Number.MIN_SAFE_INTEGER,
        });
        return;
      }
      const data = {};
      data['U' + userTCodeInfo.value.name.toUpperCase()] = {
        desc: userTCodeInfo.value.desc,
        workflow: userTCodeEditorRef.value.getValue(),
        status: 'Not Active',
      };
      context.emit('importTCodes', data);
      ElMessage({
        message: i18n.global.t('添加成功'),
        type: 'success',
        grouping: true,
      });
      localStoreUtil.removeItem(localStore['tcode-draft']);
      closeDialog();
    };

    // 重置
    const reset = () => {
      if(userTCodeEditorRef.value) userTCodeEditorRef.value.reset();
      loading.value = false;
      userTCodeInfo.value.name = '';
      userTCodeInfo.value.desc = '';
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
      userTCodeEditorRef,
      handleSave,
      initText,
      loading,
      userTCodeInfo,
      importTCodes,
      exportTCodes,
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

/* 不换行 */
.nowrap {
  white-space: nowrap;
}

.form-width {
  width: 50px;
}
</style>
