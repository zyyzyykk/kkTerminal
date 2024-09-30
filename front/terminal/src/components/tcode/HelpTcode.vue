<template>
  <el-dialog
    v-model="DialogVisilble"
    destroy-on-close
    :width="480"
    title="帮助"
    :modal="false"
    modal-class="kk-dialog-class"
    :before-close="closeDialog"
    draggable
  >
    <div style="margin-top: -27px;"></div>
    <div class="no-select">
        <el-tabs type="border-card">
            <el-tab-pane label="功能TCode">
              <div style="height: 210px; overflow-y: auto; padding: 10px 0" >
                  <div>以 <span style="background-color: #f3f4f4;" >/</span> 开头，用于快速执行通用功能</div>
                  <div class="kk-border" ></div>
                  <div v-for="(item, key) in FuncTcode" :key="key" >
                    <div class="kk-flex tocde-item" style="padding: 12px 10px;" >
                      <div style="background-color: #f3f4f4;" >{{ key }}</div>
                      <div style="margin-left: 25px;" >{{ item.desc }}</div>
                    </div>
                  </div>
              </div>
            </el-tab-pane>
            <el-tab-pane label="系统TCode">
              <div style="height: 210px; overflow-y: auto; padding: 10px 0" >
                  <div>以 <span style="background-color: #f3f4f4;" >S</span> 开头，用于快速访问系统模块</div>
                  <div class="kk-border" ></div>
                  <div v-for="(item, key) in SysTcode" :key="key" >
                    <div class="kk-flex tocde-item" style="padding: 12px 10px;" >
                      <div style="background-color: #f3f4f4;" >{{ key }}</div>
                      <div style="margin-left: 25px;" >{{ item.desc }}</div>
                    </div>
                  </div>
              </div>
            </el-tab-pane>
            <el-tab-pane label="用户TCode">
              <div style="height: 210px; overflow-y: auto; padding: 10px 0" >
                <template v-if="userTCodes && Object.keys(userTCodes).length > 0" >
                  <template v-if="nowTCode && nowTCode.length >= 2 && nowTCode.length <= 6" >
                    <div class="kk-flex" style="margin-bottom: 12px; margin-top: -10px;" >
                      <div @click="toOverview" style="margin-right: 10px; cursor: pointer; font-size: 16px;" ><el-icon><ArrowLeft /></el-icon></div>
                      <div style="margin-top: 3px;" > {{ modifyTag + nowTCode }} </div>
                      <div style="cursor: pointer; margin-left: 10px;" >
                        <el-tooltip :content="TCodeStatusEnum[userTCodes[nowTCode].status]" placement="top">
                          <TcodeStatus :style="{fontSize: '18px'}" :status="userTCodes[nowTCode].status" ></TcodeStatus>
                        </el-tooltip>
                      </div>
                      <div style="flex: 1;" ></div>
                      <div @click="doModifyTCode" v-if="mode == false" style="margin-top: -3px; font-size: 18px; cursor: pointer; margin-left: 15px;" >
                        <el-tooltip content="编辑" placement="top">
                          <el-icon><Edit /></el-icon>
                        </el-tooltip>
                      </div>
                      <div @click="doOnlyRead" v-if="mode == true" style="margin-top: -3px; font-size: 18px; cursor: pointer; margin-left: 15px;" >
                        <el-tooltip content="只读" placement="top">
                          <el-icon><View /></el-icon>
                        </el-tooltip>
                      </div>
                      <div @click="doSaveTCode" v-if="mode == true" style="margin-top: -3px; font-size: 18px; cursor: pointer; margin-left: 15px;" >
                        <el-tooltip content="保存修改" placement="top">
                          <el-icon><Finished /></el-icon>
                        </el-tooltip>
                      </div>
                      <div style="margin-left: 10px;" ></div>
                    </div>
                    <div style="width: 100%; height: 166px;">
                      <AceEditor ref="userTcodeEditorRef" @handleChange="handleChange" @handleSave="doSaveTCode" ></AceEditor>
                    </div>
                  </template>
                  <template v-else >
                    <div>以 <span style="background-color: #f3f4f4;" >U</span> 开头，自定义实现类似Shell脚本的自动化Workflow</div>
                    <div class="kk-border" ></div>
                    <div v-for="(item, key) in userTCodes" :key="key" >
                      <div class="kk-flex tocde-item" style="padding: 12px 10px;" >
                        <div class="kk-flex" style="width: 60px;">
                          <div style="background-color: #f3f4f4;" >{{ key }}</div>
                          <div style="flex: 1;" ></div>
                        </div>
                        <div class="ellipsis" style="margin-left: 10px;" >{{ item.desc }}</div>
                        <div style="flex: 1;" ></div>
                        <div style="cursor: pointer; margin-left: 10px;" >
                          <el-tooltip :content="TCodeStatusEnum[item.status]" placement="top">
                            <TcodeStatus :style="{fontSize: '18px'}" :status="item.status" ></TcodeStatus>
                          </el-tooltip>
                        </div>
                        <div @click="toWorkflow(key)" style="margin-left: 15px; cursor: pointer; font-size: 16px;" ><el-icon><ArrowRight /></el-icon></div>
                      </div>
                    </div>
                  </template>
                </template>
                <template v-else >
                  <div>以 <span style="background-color: #f3f4f4;" >U</span> 开头，自定义实现类似Shell脚本的自动化Workflow</div>
                  <div class="kk-border" ></div>
                  <NoData height="160px" >
                    <template #myslot>
                      <div style="margin-top: 20px;"></div>
                    </template>
                  </NoData>
                </template>
              </div>
            </el-tab-pane>
        </el-tabs>
    </div>
  </el-dialog>
</template>

<script>
import { ref } from 'vue';
import { ElMessage } from 'element-plus';
import { FuncTcode, SysTcode, TCodeStatusEnum } from "@/utils/Tcode";
import NoData from '../NoData.vue';
import { ArrowRight, ArrowLeft, Edit, View, Finished } from '@element-plus/icons-vue';
import AceEditor from '../preview/AceEditor.vue';
import { decrypt } from '@/utils/Encrypt';
import TcodeStatus from './TcodeStatus.vue';

export default {
  name:'HelpTcode',
  components: {
    NoData,
    TcodeStatus,
    AceEditor,
    ArrowRight,
    ArrowLeft,
    Edit,
    View,
    Finished,
  },
  setup(props, context) {

    // 控制Dialog显示
    const DialogVisilble = ref(false);
    const modifyTag = ref('');

    const userTCodes = ref({});
    const nowTCode = ref('');
    const mode = ref(false);
    // 查看Workflow
    const toWorkflow = (tcode) => {
      nowTCode.value = tcode;
      mode.value = false;
      setTimeout(() => {
        initTcodeEditor(true);
      },1);
    };
    // 返回
    const toOverview = () => {
      mode.value = false;
      nowTCode.value = '';
      modifyTag.value = '';
    };

    // 编辑器(只读)
    const userTcodeEditorRef = ref();
    const initTcodeEditor = (mode) => {
      userTcodeEditorRef.value.setLanguage('kk.js');
      userTcodeEditorRef.value.setValue(JSON.parse(decrypt(localStorage.getItem('tcodes')))[nowTCode.value].workflow || '');
      userTcodeEditorRef.value.reset();
      userTcodeEditorRef.value.setReadOnly(mode);
      modifyTag.value = '';
    };

    // 启用编辑
    const doModifyTCode = () => {
      mode.value = true;
      initTcodeEditor(false);
    };
    // 只读模式
    const doOnlyRead = () => {
      mode.value = false;
      initTcodeEditor(true);
    };

    // 修改TCode的Workflow
    const doSaveTCode = () => {
      if(modifyTag.value != '*') return;
      context.emit('handleSaveTCode', nowTCode.value, userTcodeEditorRef.value.getValue());
      doOnlyRead();
      ElMessage({
        message: '修改成功',
        type: 'success',
        grouping: true,
      });
      modifyTag.value = '';
    };

    const handleChange = () => {
      modifyTag.value = '*';
    };

    // 关闭
    const closeDialog = (done) => {
      setTimeout(() => {
        toOverview();
      },300);
      done();
    };

    return {
      DialogVisilble,
      FuncTcode,
      SysTcode,
      nowTCode,
      toWorkflow,
      toOverview,
      userTCodes,
      userTcodeEditorRef,
      initTcodeEditor,
      TCodeStatusEnum,
      mode,
      doModifyTCode,
      doOnlyRead,
      doSaveTCode,
      handleChange,
      modifyTag,
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

.tocde-item:hover {
  background-color: #efefef;
}

/* 文本不可选中 */
.no-select {
  user-select: none;
}

/* 文本溢出省略 */
.ellipsis {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  line-height: 18px;
}

/* 不换行 */
.nowrap {
  white-space: nowrap;
}
</style>