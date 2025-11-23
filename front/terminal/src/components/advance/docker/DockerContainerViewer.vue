<template>
  <div v-resizable="{ minWidthRate: 0.8, maxWidthRate: 1.2 }" >
    <el-dialog
        v-model="DialogVisible"
        width="60%"
        :modal="false"
        modal-class="kk-dialog-class"
        body-class="kk-body-class-12"
        :before-close="closeDialog"
        align-center
        draggable
    >
      <template #title>
        <div class="kk-flex kk-header-class no-select" >
          <FileIcons :style="{display: 'flex', alignItems: 'center'}" name="kk.json" :width="25" :height="25" :isFolder="false" />
          <div class="ellipsis" style="margin: 0 5px; font-size: larger; line-height: 22px;" >{{ containerName }}</div>
        </div>
      </template>
      <div style="margin-top: -28px;" ></div>
      <div :element-loading-text="$t('加载中...')" v-loading="loading" style="width: 100%; height: 80vh; position: relative; margin-bottom: 10px;" >
        <AceEditor class="preview" v-show="!loading" ref="containerViewerRef" ></AceEditor>
      </div>
      <div style="margin-top: -13px;" ></div>
    </el-dialog>
  </div>
</template>

<script>
import { ref } from "vue";
import { request } from "@/utils/RequestUtil";
import { http_base_url } from "@/env/BaseUrl";
import AceEditor from '@/components/common/AceEditor';
import { ElMessage } from "element-plus";
import FileIcons from "file-icons-vue";
import i18n from "@/locales/i18n";

export default {
  name: 'DockerContainerViewer',
  components: {
    FileIcons,
    AceEditor,
  },
  props: ['sshKey'],
  setup(props) {

    const DialogVisible = ref(false);
    const loading = ref(false);

    const containerName = ref('');
    const containerViewerRef = ref();
    const initText = async (item) => {
      loading.value = true;
      containerName.value = item.name;
      await request({
        url: http_base_url + '/advance/docker/container',
        type: 'post',
        data: {
          sshKey: props.sshKey,
          type: 4,
          items: item.id,
        },
        success(resp) {
          if(resp.status === 'success') initViewer(resp.data);
          else {
            ElMessage({
              message: i18n.global.t('内容加载失败'),
              type: 'error',
              grouping: true,
            });
            reset();
            closeDialog();
          }
          loading.value = false;
        }
      });
    };
    const initViewer = (content) => {
      containerViewerRef.value.setReadOnly(true);
      containerViewerRef.value.setValue(content);
      containerViewerRef.value.removeEndEmptyLine();
      containerViewerRef.value.resetHistory();
      containerViewerRef.value.setLanguage("kk.json");
    };

    // 重置
    const reset = () => {
      loading.value = false;
      containerName.value = '';
      if(containerViewerRef.value) containerViewerRef.value.reset();
    };

    // 关闭
    const closeDialog = (done) => {
      setTimeout(() => {
        reset();
      }, 400);
      DialogVisible.value = false;
      if(done) done();
    };

    return {
      DialogVisible,
      loading,
      containerName,
      containerViewerRef,
      initText,
      reset,
      closeDialog,
    }
  },
}
</script>

<style scoped>
.kk-flex {
  display: flex;
  align-items: center;
}

.preview {
  width: 100%;
  height: 100%;
}
</style>
