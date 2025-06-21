<template>
  <el-dialog
      v-model="computedDialogVisible"
      :before-close="closeDialog"
      :width="$t('240')"
      :modal="false"
      :title="$t('协作')"
      modal-class="kk-dialog-class"
      header-class="kk-header-class"
      body-class="kk-body-class-0"
      draggable
  >
    <div class="no-select">
      <div class="kk-flex">
        <div>{{ $t('编辑权限') }}：</div>
        <div style="flex: 1" ></div>
        <el-radio-group v-model="isReadonly" class="ml-4">
          <el-radio :label="false" size="large">{{ $t('是') }}</el-radio>
          <el-radio :label="true" size="large">{{ $t('否') }}</el-radio>
        </el-radio-group>
        <div style="flex: 1" ></div>
      </div>
      <div style="margin-top: 10px" ></div>
      <div class="kk-flex">
        <div>{{ $t('人数上限') }}：</div>
        <div style="flex: 1" ></div>
        <div>
          <el-input-number :style="{width: '80px'}" size="small" v-model="maxHeadCount" :min="1" :max="6" step="1" :step-strictly="true" >
          </el-input-number>
        </div>
        <div style="flex: 1" ></div>
      </div>
      <div style="margin-top: 10px" ></div>
      <div style="display: flex; border-top: 1px solid #f1f2f4;">
        <div style="flex: 1;"></div>
        <el-button size="small" type="primary" @click="confirm" style="margin-top: 10px;">
          {{ $t('确定') }}
        </el-button>
      </div>
    </div>
  </el-dialog>
</template>

<script>
import { ref,computed } from 'vue';
import useClipboard from "vue-clipboard3";
import $ from 'jquery';
import { ElMessage } from 'element-plus';
import { http_base_url } from '@/env/BaseUrl';
import i18n from "@/locales/i18n";
import { getPureUrl } from "@/utils/UrlUtil";

export default {
  name: 'CooperateGen',
  props:['sshKey', 'advance'],
  setup(props, context) {

    // 控制Dialog显示
    const DialogVisible = ref(false);
    const computedDialogVisible = computed(() => {
      return DialogVisible.value && props.advance;
    });

    const isReadonly = ref(true);
    const maxHeadCount = ref(6);

    // 拷贝
    const { toClipboard } = useClipboard();

    // 确定
    const confirm = async () => {
      const maxHC = maxHeadCount.value;
      $.ajax({
        url: http_base_url + '/cooperate/key',
        type:'get',
        data:{
          time:new Date().getTime(),
          sshKey:props.sshKey,
          readOnly:isReadonly.value,
          maxHeadCount:maxHC,
        },
        async success(resp) {
          if(resp.status == 'success') {
            const link = getPureUrl() + '?cooperate=' + resp.data;
            await toClipboard(link);
            ElMessage({
              message: i18n.global.t('协作链接已复制'),
              type: 'success',
              grouping: true,
              repeatNum: Number.MIN_SAFE_INTEGER,
            });
            context.emit('handleCooperate', maxHC);
            closeDialog();
          }
          else {
            ElMessage({
              message: i18n.global.t('协作Key生成失败'),
              type: resp.status,
              grouping: true,
              repeatNum: Number.MIN_SAFE_INTEGER,
            });
          }
        }
      });
    };

    // 重置
    const reset = () => {
      isReadonly.value = true;
      maxHeadCount.value = 6;
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
      computedDialogVisible,
      DialogVisible,
      confirm,
      closeDialog,
      reset,
      isReadonly,
      maxHeadCount,
    }
  }
}
</script>

<style scoped>
.kk-flex {
  display: flex;
  align-items: center;
}

/* 文本不可选中 */
.no-select {
  user-select: none;
}
</style>
