<template>
  <el-dialog
    v-model="DialogVisilble"
    destroy-on-close
    :width="480"
    title="帮助"
    :modal="false"
    modal-class="kk-dialog-class"
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
                    <div style="display: inline-block; margin-bottom: 12px; margin-top: -10px;" >
                      <div @click="toOverview" style="display: inline-block; margin-right: 10px; cursor: pointer; font-size: 16px;" ><el-icon><ArrowLeft /></el-icon></div>
                      <div style="display: inline-block; margin-top: 3px;" > {{ nowTCode }} </div>
                    </div>
                    <div style="width: 100%; height: 168px;">
                      <AceEditor ref="userTcodeEditorRef" ></AceEditor>
                    </div>
                  </template>
                  <template v-else >
                    <div>以 <span style="background-color: #f3f4f4;" >U</span> 开头，自定义实现类似Shell脚本的自动化Workflow</div>
                    <div class="kk-border" ></div>
                    <div v-for="(item, key) in userTCodes" :key="key" >
                      <div class="kk-flex tocde-item" style="padding: 12px 10px;" >
                        <div style="background-color: #f3f4f4;" >{{ key }}</div>
                        <div class="ellipsis" style="margin-left: 25px;" >{{ item.desc }}</div>
                        <div style="flex: 1;" ></div>
                        <div @click="toWorkflow(key)" style="margin-left: 10px; cursor: pointer;font-size: 16px;" ><el-icon><ArrowRight /></el-icon></div>
                      </div>
                    </div>
                  </template>
                </template>
                <template v-else >
                  <div>以 <span style="background-color: #f3f4f4;" >U</span> 开头，自定义实现类似Shell脚本的自动化Workflow</div>
                  <div class="kk-border" ></div>
                  <NoData height="210px" >
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
import { FuncTcode, SysTcode } from "@/Utils/Tcode";
import NoData from '../NoData.vue';
import { ArrowRight, ArrowLeft } from '@element-plus/icons-vue';
import AceEditor from '../preview/AceEditor.vue';
import { decrypt } from '@/Utils/Encrypt';

export default {
  name:'HelpTcode',
  components: {
    NoData,
    AceEditor,
    ArrowRight,
    ArrowLeft,
  },
  setup() {

    // 控制Dialog显示
    const DialogVisilble = ref(false);

    const userTCodes = ref({});
    const nowTCode = ref('');
    const toWorkflow = (tcode) => {
      nowTCode.value = tcode;
      setTimeout(() => {
        initTcodeEditor();
      },1);
    }
    const toOverview = () => {
      nowTCode.value = '';
    }

    // 编辑器(只读)
    const userTcodeEditorRef = ref();
    const initTcodeEditor = () => {
      userTcodeEditorRef.value.setLanguage('kk.js');
      userTcodeEditorRef.value.setValue(JSON.parse(decrypt(localStorage.getItem('tcodes')))[nowTCode.value].workflow || '');
      userTcodeEditorRef.value.reset();
      userTcodeEditorRef.value.setReadOnly(true);
    }

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
}

/* 不换行 */
.nowrap {
  white-space: nowrap;
}
</style>