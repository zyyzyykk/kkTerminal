<template>
  <div class="app-class" :element-loading-text="$t('加载中...')" v-loading="loading" >
    <template v-if="isInitialized" >
      <RouterView v-slot="{ Component }" >
        <component :is="Component" :osInfo="osInfo" />
      </RouterView>
    </template>
  </div>
</template>

<script>
import { ref, onMounted } from "vue";
import { useRouter, useRoute } from "vue-router";

import $ from 'jquery';
import { http_base_url } from '@/env/BaseUrl';
import { syncDownload } from "@/utils/CloudUtil";
import { secretKeySetter } from "@/utils/Encrypt";

export default {
  name: 'App',
  components: {
  },
  setup() {

    const router = useRouter();
    const route = useRoute();

    // 初始化
    const isInitialized = ref(false);
    const loading = ref(true);
    const osInfo = ref(null);

    onMounted(() => {
      setTimeout(() => {
        $.ajax({
          url: http_base_url + '/init',
          type: 'post',
          success(resp) {
            const data = JSON.parse(resp.info);
            osInfo.value = data.osInfo;
            secretKeySetter.storage(data.storageKey);
            secretKeySetter.socket(data.socketKey);
            secretKeySetter.public(data.publicKey);
            if(data.responseKey) {
              secretKeySetter.response(data.responseKey);
              // 多端同步-下载
              syncDownload().then(() => {
                router.push({
                  name: 'terminal',
                  query: {
                    ...route.query,
                  },
                });
                isInitialized.value = true;
                loading.value = false;
              });
            }
            else {
              router.push({
                name: 'login',
                query: {
                  ...route.query,
                },
              });
              setTimeout(() => {
                isInitialized.value = true;
              }, 1);
              loading.value = false;
            }
          },
        });
      }, 1);
    });

    return {
      isInitialized,
      loading,
      osInfo,
    }
  },
}
</script>

<style>
.app-class {
  height: 100vh;
  width: 100vw;
}

.a-link {
  text-decoration: none;
  color: var(--link);
  cursor: pointer;
}

.error-text {
  font-size: 12px;
  color: var(--error);
  margin-top: 8px;
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

.el-loading-text {
  user-select: none;
}

.el-dialog {
  pointer-events: auto;
}

.el-popconfirm__main {
  user-select: none;
  margin-top: 2px;
}

/* 隐藏滚动条 */
.xterm-viewport {
  scrollbar-width: none !important; /* Firefox */
  -ms-overflow-style: none !important; /* IE 和 Edge */
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
