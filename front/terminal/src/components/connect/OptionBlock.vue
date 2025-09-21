<template>
  <el-dialog
    v-model="DialogVisible"
    :before-close="closeDialog"
    destroy-on-close
    :width="420"
    :modal="false"
    :title="$t('全部配置')"
    modal-class="kk-dialog-class"
    header-class="kk-header-class"
    body-class="kk-body-class-6"
    draggable
  >
    <div class="no-select" >
      <div v-if="Object.keys(sshOptions).length > 0" class="kk-border" >
        <div v-for="(value, key) in sshOptions" :key="key" >
          <div :class="['item-class', (aimOption == key) ? 'item-selected' : '']" @click="aimOption = key" >
            <FileIcons :style="{display: 'flex', alignItems: 'center'}" name="kk.ini" :width="20" :height="20" :isFolder="false" />
            <div class="ellipsis" style="margin: 0 10px; line-height: 18px;" >{{ key }}</div>
            <div style="flex: 1;" ></div>
            <div @click="confirmDeleteOption(key)" ><el-icon><CircleClose /></el-icon></div>
          </div>
        </div>
      </div>
      <div v-else class="kk-border" >
        <NoData :msg="i18n.global.k('暂无配置')" ></NoData>
      </div>
      <div class="kk-flex" >
        <div>{{ $t('配置名') }}：</div>
        <div style="flex: 1;" >
          <el-input size="small" v-model="aimOption" :disabled="opType == 0" @keydown.enter="confirm" class="w-50 m-2" placeholder="" >
          </el-input>
        </div>
        <div style="margin-left: 10px;" >
          <el-button size="small" type="primary" @click="confirm" >
            {{ opType ? $t('保存') : $t('选择') }}
          </el-button>
        </div>
      </div>
    </div>
  </el-dialog>
</template>

<script>
import { ref } from 'vue';
import { CircleClose } from '@element-plus/icons-vue';
import { ElMessage } from 'element-plus';
import { deleteDialog } from '@/utils/DeleteDialog';
import NoData from '@/components/common/NoData';
import FileIcons from 'file-icons-vue';
import i18n from "@/locales/i18n";

export default {
  name: 'OptionBlock',
  components: {
    FileIcons,
    NoData,
    CircleClose,
  },
  props:['opType','sshOptions'],
  setup(props,context) {
    // 控制Dialog显示
    const DialogVisible = ref(false);

    // 目标配置
    const aimOption = ref('');

    // 确定
    const confirm = () => {
      if(aimOption.value == '' || aimOption.value.trim() == '') return;
      context.emit('callback',aimOption.value.trim());
      closeDialog();
    };

    // 删除配置
    const deleteOption = ref('');
    const confirmDeleteOption = (name) => {
      deleteOption.value = name;
      deleteDialog(i18n.global.t('提示'), i18n.global.t('确定删除此配置吗?'), doDeleteOption);
    };
    const doDeleteOption = () => {
      context.emit('handleDeleteOption', deleteOption.value);
      ElMessage({
        message: i18n.global.t('删除成功'),
        type: 'success',
        grouping: true,
      });
      if(aimOption.value == deleteOption.value) aimOption.value = '';
    };

    // 重置
    const reset = () => {
      aimOption.value = '';
      deleteOption.value = '';
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
      i18n,
      DialogVisible,
      aimOption,
      confirm,
      confirmDeleteOption,
      doDeleteOption,
      reset,
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

.kk-border
{
  height: 30.1vh;
  overflow-y: scroll;
  width: 100%;
  border-bottom: 1px solid #ececec;
}
</style>
