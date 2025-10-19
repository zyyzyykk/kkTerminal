<template>
  <div class="root-view no-select" :element-loading-text="$t('加载中...')" v-loading="loading" >
    <div class="container" >
      <div class="kk-flex container-header" >
        <div><img src="@/assets/terminal.svg" alt="terminal" style="height: 72px;" ></div>
        <div class="header-text" >
          <div class="title" >{{ $t('欢迎来到kkTerminal') }}</div>
          <div style="height: 8px;" ></div>
          <div class="content" >{{ $t('管理员开启了对kkTerminal的密码校验') }}</div>
        </div>
      </div>
      <div class="container-body" >
        <div class="kk-flex" >
          <div style="flex: 1;" >
            <el-input v-model="password" type="password" @keydown.enter="verify" class="w-50 m-2 body-item" maxlength="16" :placeholder="$t('输入密码以获取访问权限')" >
              <template #prefix>
                <el-icon class="el-input__icon" ><Lock /></el-icon>
              </template>
            </el-input>
          </div>
          <div style="width: 20px;" ></div>
          <div>
            <el-button :disabled="password.length === 0" class="body-item" type="primary" @click="verify" >
              <span style="margin: 0 10px;" >{{ $t('验证') }}</span>
            </el-button>
          </div>
        </div>
        <div class="error-text" style="font-size: 16px;" >{{ $t(err_msg) }}</div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref } from 'vue';
import { useRouter, useRoute } from "vue-router";

import request from "@/utils/RequestUtil";
import { http_base_url } from "@/env/BaseUrl";
import { localStore } from "@/env/Store";
import { getUrlParams } from "@/utils/UrlUtil";
import { syncDownload, localStoreUtil } from "@/utils/CloudUtil";
import { rsaEncrypt, secretKeySetter } from "@/utils/Encrypt";
import { ElMessage } from 'element-plus';
import { Lock } from '@element-plus/icons-vue';
import i18n from "@/locales/i18n";

export default {
  name: 'AccessCheck',
  components: {
    Lock,
  },
  setup() {

    const router = useRouter();
    const route = useRoute();

    const loading = ref(false);
    const err_msg = ref('');

    const password = ref('');
    const verify = () => {
      loading.value = true;
      err_msg.value = '';
      request({
        url: http_base_url + '/access/login',
        type: 'post',
        data: {
          password: rsaEncrypt(password.value),
        },
        success(resp) {
          if(resp.status === 'success') {
            ElMessage({
              message: i18n.global.t('登录成功'),
              type: "success",
              grouping: true,
              repeatNum: Number.MIN_SAFE_INTEGER,
            });
            const data = JSON.parse(resp.info);
            secretKeySetter.response(data.responseKey);
            // 多端同步-下载
            syncDownload().then(() => {
              loading.value = false;
              router.push({
                name: 'terminal',
                query: {
                  ...route.query,
                },
              });
            });
          }
          else {
            err_msg.value = i18n.global.k('密码错误');
            password.value = '';
            loading.value = false;
          }
        },
      });
    };

    // lang
    i18n.global.locale = getUrlParams()['lang'] || localStoreUtil.getItem(localStore['lang']) || 'en';

    return {
      loading,
      err_msg,
      password,
      verify,
    }
  },
}
</script>

<style scoped>
.root-view {
  height: 100vh;
  width: 100%;
  background: #eef2f6 url('@/assets/bg_access_check.png') repeat-x 0 bottom;
  display: flex;
  align-items: center;
  justify-content: center;
}

.container {
  margin-top: -12vh;
  width: 650px;
  background-color: #fcfcfc;
  border-radius: 16px;
  box-shadow: 0 6px 12px 0 rgba(0, 0, 0, 0.15), 0 3px 6px -2px rgba(0, 0, 0, 0.2), 0 8px 16px 4px rgba(0, 0, 0, 0.12);
}

.kk-flex {
  display: flex;
  align-items: center;
}

.container-header {
  height: 120px;
  padding: 30px;
}

.header-text {
  height: 55px;
  margin: 0 20px;
}

.title {
  font-size: 24px;
  line-height: 28px;
  color: #444444;
  font-weight: bolder;
}

.content {
  font-size: 15px;
  line-height: 19px;
  color: #555555;
}

.container-body {
  padding: 40px;
  border-top: 1px solid #dddddd;
}

.body-item {
  height: 52px;
  font-size: 18px;
}
</style>
