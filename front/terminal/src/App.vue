<template>
  <FrameWork v-if="isInitialized" ></FrameWork>
</template>

<script>
import FrameWork from "./views/FrameWork";
import $ from 'jquery';
import { http_base_url } from '@/env/BaseUrl';
import { ref, onBeforeMount } from "vue";
import { sessionStore } from "@/env/Store";

export default {
  name: 'App',
  components: {
    FrameWork,
  },
  setup() {

    // 初始化
    const isInitialized = ref(false);
    const needAesKey = !sessionStorage.getItem(sessionStore['aes-key']);
    const needPublicKey = !sessionStorage.getItem(sessionStore['public-key']);

    onBeforeMount(async () => {
      await $.ajax({
        url: http_base_url + '/init',
        type:'post',
        data: {
          aesKey: needAesKey,
          publicKey: needPublicKey,
        },
        success(resp){
          const data = JSON.parse(resp.info);
          if(needAesKey) sessionStorage.setItem(sessionStore['aes-key'], data.aesKey);
          if(needPublicKey) sessionStorage.setItem(sessionStore['public-key'], data.publicKey);
          sessionStorage.setItem(sessionStore['os-info'], JSON.stringify(data.osInfo));
          isInitialized.value = true;
        },
      });
    });

    return {
      isInitialized,
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

/* 隐藏滚动条 */
.xterm-viewport {
  scrollbar-width: none !important; /* Firefox */
  -ms-overflow-style: none !important; /* Internet Explorer 和 Edge */
}

.xterm-viewport::-webkit-scrollbar {
  display: none !important; /* Chrome 和 Safari */
}

</style>
