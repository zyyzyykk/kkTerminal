<template>
  <FrameWork v-if="isInitialized" :osInfo="osInfo" ></FrameWork>
</template>

<script>
import FrameWork from "./views/FrameWork";
import $ from 'jquery';
import { http_base_url } from '@/env/BaseUrl';
import { ref, onMounted } from "vue";
import { syncDownload } from "@/utils/CloudUtil";
import { encryptKeySetter } from "@/utils/Encrypt";

export default {
  name: 'App',
  components: {
    FrameWork,
  },
  setup() {

    // 初始化
    const isInitialized = ref(false);
    const osInfo = ref(null);

    onMounted(() => {
      setTimeout(() => {
        $.ajax({
          url: http_base_url + '/init',
          type: 'post',
          success(resp) {
            const data = JSON.parse(resp.info);
            encryptKeySetter.aes(data.aesKey);
            encryptKeySetter.rsa(data.publicKey);
            osInfo.value = data.osInfo;
            // 多端同步-下载
            syncDownload().then(() => {
              isInitialized.value = true;
            });
          },
        });
      }, 1);
    });

    return {
      isInitialized,
      osInfo,
    }
  },
}
</script>

<style>
/* 禁止图片拖拽 */
img {
  -webkit-user-drag: none; /* Safari */
  -khtml-user-drag: none; /* Konqueror HTML */
  -moz-user-drag: none; /* Firefox */
  -o-user-drag: none; /* Opera */
}

.kk-dialog-class {
  pointer-events: none;
  z-index: 3456;
}

.kk-header-class {
  padding-top: 4px;
  padding-bottom: 8px !important;
  padding-left: 4px;
  margin-right: 16px;
  user-select: none;
}

.kk-body-class-0 {
  padding: 0 4px;
}

.kk-body-class-6 {
  padding-top: 6px;
  padding-bottom: 0;
  padding-left: 4px;
  margin-right: 4px;
}

.kk-body-class-12 {
  padding-top: 12px;
  padding-bottom: 0;
  padding-left: 4px;
  margin-right: 4px;
}

.el-dialog {
  pointer-events: auto;
}

.el-popconfirm__main {
  user-select: none;
  margin-top: 2px;
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

/* 隐藏滚动条 */
.xterm-viewport {
  scrollbar-width: none !important; /* Firefox */
  -ms-overflow-style: none !important; /* Internet Explorer 和 Edge */
}

.xterm-viewport::-webkit-scrollbar {
  display: none !important; /* Chrome 和 Safari */
}

/* 左右拉伸 */
.resize-handle {
  position: absolute;
  top: 0;
  width: 6px;
  height: 100%;
  background-color: transparent;
  color: transparent;
  cursor: ew-resize;
}

.handle-left {
  left: 0;
}

.handle-right {
  right: 0;
}
</style>
