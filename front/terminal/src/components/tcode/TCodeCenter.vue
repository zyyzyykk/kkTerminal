<template>
  <div v-resizable="{ minWidthRate: 1, maxWidthRate: 1.5 }" >
    <el-dialog
        v-model="DialogVisible"
        destroy-on-close
        :width="480"
        :title="$t('终端代码中心')"
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
            <div class="pane-body" >
              <div class="kk-flex" >
                <div>{{ $t('以') }}&nbsp;</div>
                <div style="background-color: #f3f4f4;" >F</div>
                <div>&nbsp;{{ $t('开头，用于执行通用功能') }}</div>
              </div>
              <div class="kk-border" ></div>
              <div v-for="(item, key) in FuncTCode" :key="key" >
                <div class="kk-flex tcode-item" style="padding: 12px 10px;" >
                  <div class="kk-flex form-width" >
                    <div style="background-color: #f3f4f4;" >{{ key }}</div>
                    <div style="flex: 1;" ></div>
                  </div>
                  <div class="ellipsis" style="margin-left: 10px; line-height: 18px;" >{{ $t(item.desc) }}</div>
                </div>
              </div>
            </div>
          </el-tab-pane>
          <el-tab-pane :label="$t('系统')" >
            <div class="pane-body" >
              <div class="kk-flex" >
                <div>{{ $t('以') }}&nbsp;</div>
                <div style="background-color: #f3f4f4;" >S</div>
                <div>&nbsp;{{ $t('开头，用于访问系统模块') }}</div>
              </div>
              <div class="kk-border" ></div>
              <div v-for="(item, key) in SysTCode" :key="key" >
                <div class="kk-flex tcode-item" style="padding: 12px 10px;" >
                  <div class="kk-flex form-width" >
                    <div style="background-color: #f3f4f4;" >{{ key }}</div>
                    <div style="flex: 1;" ></div>
                  </div>
                  <div class="ellipsis" style="margin-left: 10px; line-height: 18px;" >{{ $t(item.desc) }}</div>
                </div>
              </div>
            </div>
          </el-tab-pane>
          <el-tab-pane :label="$t('用户')" >
            <div class="pane-body" >
              <template v-if="userTCodes && Object.keys(userTCodes).length > 0" >
                <template v-if="nowTCode && nowTCode.length >= 2 && nowTCode.length <= 6" >
                  <div class="kk-flex" style="margin-bottom: 10px;" >
                    <el-icon @click="toOverview" style="margin-right: 10px; cursor: pointer; font-size: 16px;" ><ArrowLeft /></el-icon>
                    <div> {{ modifyTag + nowTCode }} </div>
                    <div style="margin-left: 10px;" ></div>
                    <el-tooltip :content="TCodeStatusEnum[userTCodes[nowTCode].status]" placement="top" >
                      <TCodeStatus :style="{fontSize: '18px', cursor: 'pointer'}" :status="userTCodes[nowTCode].status" ></TCodeStatus>
                    </el-tooltip>
                    <div style="flex: 1;" ></div>
                    <el-tooltip v-if="!mode" :content="$t('编辑')" placement="top" >
                      <el-icon @click="doModifyTCode" class="editor-operator" ><Edit /></el-icon>
                    </el-tooltip>
                    <el-tooltip v-if="!mode" :content="$t('删除')" placement="top" >
                      <el-icon @click="confirmDeleteTCode" class="editor-operator" ><Delete /></el-icon>
                    </el-tooltip>
                    <el-tooltip v-if="mode" :content="$t('只读')" placement="top" >
                      <el-icon @click="doOnlyRead" class="editor-operator" ><View /></el-icon>
                    </el-tooltip>
                    <el-tooltip v-if="mode" :content="$t('保存修改')" placement="top" >
                      <el-icon @click="doSaveTCode" class="editor-operator" ><Finished /></el-icon>
                    </el-tooltip>
                  </div>
                  <div style="width: 100%; height: 180px;" >
                    <AceEditor ref="userTCodeEditorRef" @handleChange="handleChange" @handleSave="doSaveTCode" ></AceEditor>
                  </div>
                </template>
                <template v-else >
                  <div class="kk-flex" >
                    <div>{{ $t('以') }}&nbsp;</div>
                    <div style="background-color: #f3f4f4;" >U</div>
                    <div>&nbsp;{{ $t('开头，用于自定义工作流') }}</div>
                  </div>
                  <div class="kk-border" ></div>
                  <div v-for="(item, key) in userTCodes" :key="key" >
                    <div class="kk-flex tcode-item" style="padding: 12px 10px;" >
                      <div class="kk-flex form-width" >
                        <div style="background-color: #f3f4f4;" >{{ key }}</div>
                        <div style="flex: 1;" ></div>
                      </div>
                      <ToolTip :content="item.desc" :delay="1000" >
                        <template #content>
                          <div class="ellipsis" style="margin-left: 10px; line-height: 18px;" >{{ item.desc }}</div>
                        </template>
                      </ToolTip>
                      <div style="flex: 1;" ></div>
                      <div style="margin-left: 10px;" ></div>
                      <el-tooltip :content="TCodeStatusEnum[item.status]" placement="top" >
                        <TCodeStatus :style="{fontSize: '18px', cursor: 'pointer'}" :status="item.status" ></TCodeStatus>
                      </el-tooltip>
                      <el-icon @click="toWorkflow(key)" style="margin-left: 15px; cursor: pointer; font-size: 16px;" ><ArrowRight /></el-icon>
                    </div>
                  </div>
                </template>
              </template>
              <template v-else >
                <div class="kk-flex" >
                  <div>{{ $t('以') }}&nbsp;</div>
                  <div style="background-color: #f3f4f4;" >U</div>
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
import { ref } from 'vue';
import { ElMessage } from 'element-plus';
import { FuncTCode, SysTCode, TCodeStatusEnum } from "@/components/tcode/TCode";
import ToolTip from "@/components/common/ToolTip";
import NoData from '@/components/common/NoData';
import { ArrowRight, ArrowLeft, Edit, View, Finished, Delete } from '@element-plus/icons-vue';
import AceEditor from '@/components/common/AceEditor';
import { aesDecrypt } from '@/utils/Encrypt';
import TCodeStatus from './TCodeStatus';
import { deleteDialog } from '@/utils/DeleteDialog';
import i18n from "@/locales/i18n";
import { localStore } from "@/env/Store";
import { localStoreUtil } from "@/utils/CloudUtil";

export default {
  name: 'TCodeCenter',
  components: {
    ToolTip,
    NoData,
    TCodeStatus,
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

    const userTCodes = ref({});
    const nowTCode = ref('');
    const mode = ref(false);
    // 查看工作流
    const toWorkflow = (tcode) => {
      nowTCode.value = tcode;
      mode.value = false;
      setTimeout(() => {
        initTCodeEditor(true);
      },1);
    };
    // 返回
    const toOverview = () => {
      mode.value = false;
      nowTCode.value = '';
      modifyTag.value = '';
    };

    // 编辑器(只读)
    const userTCodeEditorRef = ref();
    const initTCodeEditor = (mode) => {
      userTCodeEditorRef.value.setLanguage('kk.js');
      userTCodeEditorRef.value.setValue(JSON.parse(aesDecrypt(localStoreUtil.getItem(localStore['tcodes'])))[nowTCode.value].workflow || '');
      userTCodeEditorRef.value.setReadOnly(mode);
      modifyTag.value = '';
    };

    // 启用编辑
    const doModifyTCode = () => {
      mode.value = true;
      initTCodeEditor(false);
    };
    // 只读模式
    const doOnlyRead = () => {
      mode.value = false;
      initTCodeEditor(true);
    };

    // 修改终端代码工作流
    const doSaveTCode = () => {
      if(modifyTag.value != '*') return;
      context.emit('handleSaveTCode', nowTCode.value, userTCodeEditorRef.value.getValue());
      doOnlyRead();
      ElMessage({
        message: i18n.global.t('修改成功'),
        type: 'success',
        grouping: true,
      });
      modifyTag.value = '';
    };

    // 删除终端代码
    const confirmDeleteTCode = () => {
      deleteDialog(i18n.global.t('提示'), i18n.global.t('确定删除此终端代码吗?'), doDeleteTCode);
    };
    const doDeleteTCode = () => {
      context.emit('handleDeleteTCode', nowTCode.value);
      ElMessage({
        message: i18n.global.t('删除成功'),
        type: 'success',
        grouping: true,
      });
      toOverview();
    };

    const handleChange = () => {
      modifyTag.value = '*';
    };

    // 重置
    const reset = () => {
      if(userTCodeEditorRef.value) userTCodeEditorRef.value.reset();
      mode.value = false;
      nowTCode.value = '';
      modifyTag.value = '';
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
      FuncTCode,
      SysTCode,
      TCodeStatusEnum,
      nowTCode,
      toWorkflow,
      toOverview,
      userTCodes,
      userTCodeEditorRef,
      initTCodeEditor,
      mode,
      doModifyTCode,
      doOnlyRead,
      doSaveTCode,
      confirmDeleteTCode,
      doDeleteTCode,
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

.tcode-item:hover {
  background-color: #efefef;
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
  /* 隐藏滚动条 */
  scrollbar-width: none !important; /* Firefox */
  -ms-overflow-style: none !important; /* Internet Explorer 和 Edge */
}

.pane-body::-webkit-scrollbar {
  display: none !important; /* Chrome 和 Safari */
}
</style>
