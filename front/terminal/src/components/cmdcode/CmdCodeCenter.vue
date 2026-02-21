<template>
  <div v-resizable="{ minWidthRate: 1, maxWidthRate: 1.5 }" >
    <el-dialog
        v-model="DialogVisible"
        destroy-on-close
        :width="480"
        :title="$t('命令代码中心')"
        :modal="false"
        modal-class="kk-dialog-class"
        header-class="kk-header-class"
        body-class="kk-body-class-6"
        :before-close="closeDialog"
        draggable
    >
      <div class="no-select" >
        <el-tabs stretch type="border-card" >
          <el-tab-pane :label="$t('功能')" >
            <div class="pane-body no-scrollbar" >
              <div class="kk-flex" >
                <div>{{ $t('以') }}&nbsp;</div>
                <div class="cmdcode-name" >F</div>
                <div>&nbsp;{{ $t('开头，用于执行通用功能') }}</div>
              </div>
              <div class="kk-border" ></div>
              <div v-for="(item, key) in FuncCmdCode" :key="key" >
                <div class="kk-flex cmdcode-item" style="padding: 12px 10px;" >
                  <div class="kk-flex form-width" >
                    <div class="cmdcode-name" >{{ key }}</div>
                    <div style="flex: 1;" ></div>
                  </div>
                  <div class="ellipsis" style="margin-left: 10px; line-height: 18px;" >{{ $t(item.desc) }}</div>
                </div>
              </div>
            </div>
          </el-tab-pane>
          <el-tab-pane :label="$t('系统')" >
            <div class="pane-body no-scrollbar" >
              <div class="kk-flex" >
                <div>{{ $t('以') }}&nbsp;</div>
                <div class="cmdcode-name" >S</div>
                <div>&nbsp;{{ $t('开头，用于访问系统模块') }}</div>
              </div>
              <div class="kk-border" ></div>
              <div v-for="(item, key) in SysCmdCode" :key="key" >
                <div class="kk-flex cmdcode-item" style="padding: 12px 10px;" >
                  <div class="kk-flex form-width" >
                    <div class="cmdcode-name" >{{ key }}</div>
                    <div style="flex: 1;" ></div>
                  </div>
                  <div class="ellipsis" style="margin-left: 10px; line-height: 18px;" >{{ $t(item.desc) }}</div>
                </div>
              </div>
            </div>
          </el-tab-pane>
          <el-tab-pane :label="$t('用户')" >
            <div class="pane-body no-scrollbar" >
              <template v-if="userCmdCodes && Object.keys(userCmdCodes).length > 0" >
                <template v-if="currentCmdCode && currentCmdCode.length >= 2 && currentCmdCode.length <= 6" >
                  <div class="kk-flex" style="margin-bottom: 10px;" >
                    <el-icon @click="gotoOverview" style="margin-right: 10px; cursor: pointer; font-size: 16px;" ><ArrowLeft /></el-icon>
                    <div> {{ modifyTag + currentCmdCode }} </div>
                    <div style="margin-left: 10px;" ></div>
                    <el-tooltip :content="CmdCodeStatusEnum[userCmdCodes[currentCmdCode].status]" placement="top" :show-after="300" >
                      <CmdCodeStatus :style="{fontSize: '18px', cursor: 'pointer'}" :status="userCmdCodes[currentCmdCode].status" ></CmdCodeStatus>
                    </el-tooltip>
                    <div style="flex: 1;" ></div>
                    <el-tooltip v-if="!mode" :content="$t('编辑')" placement="top" :show-after="300" >
                      <el-icon @click="doModifyCmdCode" class="editor-operator" ><Edit /></el-icon>
                    </el-tooltip>
                    <el-tooltip v-if="!mode" :content="$t('删除')" placement="top" :show-after="300" >
                      <el-icon @click="confirmDeleteCmdCode" class="editor-operator" ><Delete /></el-icon>
                    </el-tooltip>
                    <el-tooltip v-if="mode" :content="$t('只读')" placement="top" :show-after="300" >
                      <el-icon @click="doOnlyRead" class="editor-operator" ><View /></el-icon>
                    </el-tooltip>
                    <el-tooltip v-if="mode" :content="$t('保存修改')" placement="top" :show-after="300" >
                      <el-icon @click="doSaveCmdCode" class="editor-operator" ><Finished /></el-icon>
                    </el-tooltip>
                  </div>
                  <div style="width: 100%; height: 180px;" >
                    <AceEditor ref="userCmdCodeEditorRef" @handleChange="handleChange" @handleSave="doSaveCmdCode" ></AceEditor>
                  </div>
                </template>
                <template v-else >
                  <div class="kk-flex" >
                    <div>{{ $t('以') }}&nbsp;</div>
                    <div class="cmdcode-name" >U</div>
                    <div>&nbsp;{{ $t('开头，用于自定义工作流') }}</div>
                  </div>
                  <div class="kk-border" ></div>
                  <div v-for="(item, key) in userCmdCodes" :key="key" >
                    <div class="kk-flex cmdcode-item" style="padding: 12px 10px;" >
                      <div class="kk-flex form-width" >
                        <div class="cmdcode-name" >{{ key }}</div>
                        <div style="flex: 1;" ></div>
                      </div>
                      <ToolTip :content="item.desc" :delay="1000" >
                        <template #content>
                          <div class="ellipsis" style="margin-left: 10px; line-height: 18px;" >{{ item.desc }}</div>
                        </template>
                      </ToolTip>
                      <div style="flex: 1;" ></div>
                      <div style="margin-left: 10px;" ></div>
                      <el-tooltip :content="CmdCodeStatusEnum[item.status]" placement="top" :show-after="300" >
                        <CmdCodeStatus :style="{fontSize: '18px', cursor: 'pointer'}" :status="item.status" ></CmdCodeStatus>
                      </el-tooltip>
                      <el-icon @click="gotoWorkflow(key)" style="margin-left: 15px; cursor: pointer; font-size: 16px;" ><ArrowRight /></el-icon>
                    </div>
                  </div>
                </template>
              </template>
              <template v-else >
                <div class="kk-flex" >
                  <div>{{ $t('以') }}&nbsp;</div>
                  <div class="cmdcode-name" >U</div>
                  <div>&nbsp;{{ $t('开头，用于自定义工作流') }}</div>
                </div>
                <div class="kk-border" ></div>
                <NoData height="160px" >
                  <template #mySlot>
                    <div style="margin-top: 20px;" ></div>
                  </template>
                </NoData>
              </template>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import browser from "@/utils/Browser";
import { ref } from "vue";
import { ElMessage } from "element-plus";
import { FuncCmdCode, SysCmdCode, CmdCodeStatusEnum } from "@/components/cmdcode/CmdCode";
import ToolTip from "@/components/common/ToolTip";
import NoData from "@/components/common/NoData";
import { ArrowRight, ArrowLeft, Edit, View, Finished, Delete } from "@element-plus/icons-vue";
import AceEditor from "@/components/common/AceEditor";
import { aesDecrypt } from "@/utils/Encrypt";
import CmdCodeStatus from "./CmdCodeStatus";
import { deleteDialog } from "@/components/common/DeleteDialog";
import i18n from "@/locales/i18n";
import { localStore } from "@/env/Store";

export default {
  name: 'CmdCodeCenter',
  components: {
    ToolTip,
    NoData,
    CmdCodeStatus,
    AceEditor,
    ArrowRight,
    ArrowLeft,
    Edit,
    View,
    Finished,
    Delete,
  },
  setup(props, context) {

    // 控制Dialog显示
    const DialogVisible = ref(false);
    const modifyTag = ref('');

    const userCmdCodes = ref({});
    const currentCmdCode = ref('');
    const mode = ref(false);
    // 查看工作流
    const gotoWorkflow = (cmdcode) => {
      currentCmdCode.value = cmdcode;
      mode.value = false;
      browser.setTimeout(() => {
        initCmdCodeEditor(true);
      }, 1);
    };
    // 返回
    const gotoOverview = () => {
      mode.value = false;
      currentCmdCode.value = '';
      modifyTag.value = '';
    };

    // 编辑器(只读)
    const userCmdCodeEditorRef = ref();
    const initCmdCodeEditor = (mode) => {
      userCmdCodeEditorRef.value.setLanguage('kk.js');
      userCmdCodeEditorRef.value.setValue(JSON.parse(aesDecrypt(browser.localStorage.getItem(localStore['cmdcodes'])))[currentCmdCode.value].workflow || '');
      userCmdCodeEditorRef.value.setReadOnly(mode);
      modifyTag.value = '';
    };

    // 启用编辑
    const doModifyCmdCode = () => {
      mode.value = true;
      initCmdCodeEditor(false);
    };
    // 只读模式
    const doOnlyRead = () => {
      mode.value = false;
      initCmdCodeEditor(true);
    };

    // 修改命令代码工作流
    const doSaveCmdCode = () => {
      if(modifyTag.value !== '*') return;
      context.emit('handleSaveCmdCode', currentCmdCode.value, userCmdCodeEditorRef.value.getValue());
      doOnlyRead();
      ElMessage({
        message: i18n.global.t('修改成功'),
        type: 'success',
        grouping: true,
      });
      modifyTag.value = '';
    };

    // 删除命令代码
    const confirmDeleteCmdCode = () => {
      deleteDialog(i18n.global.t('提示'), i18n.global.t('确定删除此命令代码吗？'), doDeleteCmdCode);
    };
    const doDeleteCmdCode = () => {
      context.emit('handleDeleteCmdCode', currentCmdCode.value);
      ElMessage({
        message: i18n.global.t('删除成功'),
        type: 'success',
        grouping: true,
      });
      gotoOverview();
    };

    const handleChange = () => {
      modifyTag.value = '*';
    };

    // 重置
    const reset = () => {
      if(userCmdCodeEditorRef.value) userCmdCodeEditorRef.value.reset();
      mode.value = false;
      currentCmdCode.value = '';
      modifyTag.value = '';
      DialogVisible.value = false;
    };

    // 关闭
    const closeDialog = (done) => {
      browser.setTimeout(() => {
        reset();
      }, 400);
      DialogVisible.value = false;
      if(done) done();
    };

    return {
      DialogVisible,
      FuncCmdCode,
      SysCmdCode,
      CmdCodeStatusEnum,
      currentCmdCode,
      gotoWorkflow,
      gotoOverview,
      userCmdCodes,
      userCmdCodeEditorRef,
      initCmdCodeEditor,
      mode,
      doModifyCmdCode,
      doOnlyRead,
      doSaveCmdCode,
      confirmDeleteCmdCode,
      doDeleteCmdCode,
      handleChange,
      modifyTag,
      reset,
      closeDialog,
    }
  }


}
</script>

<style scoped>
.kk-flex {
  display: flex;
  align-items: center;
}

.kk-border {
  padding-bottom: 10px;
  border-bottom: 1px solid #ddd;
}

.cmdcode-item:hover {
  background-color: #efefef;
}

.cmdcode-name {
  background-color: #f3f4f4;
  user-select: text;
}

.editor-operator {
  margin-left: 15px;
  font-size: 18px;
  cursor: pointer;
}

.form-width {
  min-width: 80px;
  max-width: 80px;
}

.pane-body {
  height: 210px;
  overflow-y: auto;
}
</style>
