<template>
  <el-dialog
    v-model="DialogVisilble"
    :before-close="closeDialog"
    destroy-on-close
    :width="420"
    :modal="false"
    title="全部配置"
    modal-class="kk-dialog-class"
    draggable
  >
    <div style="margin-top: -15px;"></div>
    <div class="no-select">
      <div v-if="Object.keys(sshOptions).length > 0" class="kk-border">
        <div v-for="(value, key) in sshOptions" :key="key" >
          <div :class="['item-class', (aimOption == key) ? 'item-selected' : '']" @click="aimOption = key">
            <FileIcons name="kk.txt" width="20" height="20" :isFloder="false" />
            <div style="margin: 0 10px;">{{ key }}</div>
          </div>
        </div>
      </div>
      <div v-else class="kk-border">
        <NoData :msg="noDataMsg"></NoData>
      </div>
      <div class="kk-flex">
        <div>配置名：</div>
        <div style="flex: 1;">
          <el-input size="small" v-model="aimOption" :disabled="opType == 0" class="w-50 m-2" placeholder="">
          </el-input>
        </div>
        <div style="margin-left: 10px;">
          <el-button size="small" type="primary" @click="confirm" >
            {{ opType ? '保存' : '导入'}}
          </el-button>
        </div>
      </div>
    </div>
  </el-dialog>
</template>

<script>
import { ref } from 'vue';

import NoData from '@/components/NoData';
// 引入文件图标组件
import FileIcons from 'file-icons-vue'

export default {
  name: 'OptionBlock',
  components: {
    FileIcons,
    NoData,
  },
  props:['opType','sshOptions'],
  setup(props,context)
  {
    // 控制Dialog显示
    const DialogVisilble = ref(false);
    const err_msg = ref('');
    const noDataMsg = ref('暂无配置');
    
    // 目标配置
    const aimOption = ref('');
    
    // 确定
    const confirm = () => {
      if(aimOption.value == '' || aimOption.value.trim() == '') return;
      context.emit('callback',aimOption.value.trim());
      DialogVisilble.value = false;
    }

    // 关闭
    const closeDialog = (done) => {
      aimOption.value = '';
      done();
    }

    return {
      DialogVisilble,
      err_msg,
      noDataMsg,
      aimOption,
      confirm,
      closeDialog,

    }
  }
}
</script>

<style scoped>
.item-class {
  display: flex;
  align-items: center;
  padding: 5px 10px;
  border-bottom: 1px solid #ececec;
  cursor: pointer;
  width: 100%;
}

.item-class:hover {
  background-color: #f3f3f3;
}

.item-selected {
  background-color: #f3f3f3;
}

.kk-flex {
  display: flex; 
  align-items: center;
  margin-top: 10px;
}

/* 文本不可选中 */
.no-select {
  user-select: none;
}

.kk-border
{
  height: 30.1vh;
  overflow-y: scroll;
  width: 100%;
  border-bottom: 1px solid #ececec;
}
</style>