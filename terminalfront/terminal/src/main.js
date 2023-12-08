import { createApp } from 'vue'
import App from './App.vue'

const app = createApp(App);

// 引入element UI
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import zhCn from 'element-plus/es/locale/lang/zh-cn'  // 中文

app.use(ElementPlus, {
  locale: zhCn,
});

app.config.globalProperties.$ELEMENT = { size: 'small' };

// 引入element icon
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.mount('#app');

// 引入全局样式
import './assets/base.css'


// jQuery配置Ajax全局响应拦截，进行数据解密
import $ from 'jquery';
import { decrypt } from './Utils/Encrypt';

$.ajaxSetup({
    processData: true,
    // 携带cookie
    xhrFields:{
      withCredentials: true,
    },
    dataFilter(resp) {
      resp = JSON.parse(resp);
      let data = resp.data;
      if(data) resp.data = JSON.parse(decrypt(resp.data));
      return JSON.stringify(resp);
    },
});

//用于去掉eazyplayer警告,开发时禁用，打包开启
app.config.warnHandler = () => {}
