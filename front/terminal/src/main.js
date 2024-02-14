import { createApp } from 'vue'
import App from './App.vue'

const app = createApp(App);

// 按需引入element-plus
import 'element-plus/dist/index.css';
import { ElButton, ElDialog, ElIcon, ElInput, ElUpload, ElColorPicker,ElDropdown, ElDropdownMenu, ElDropdownItem, ElSwitch, ElLoading, ElRadioGroup, ElRadio } from 'element-plus';
app.use(ElButton).use(ElDialog).use(ElIcon).use(ElInput).use(ElUpload).use(ElColorPicker);
app.use(ElDropdown).use(ElDropdownMenu).use(ElDropdownItem).use(ElSwitch).use(ElLoading).use(ElRadioGroup).use(ElRadio);

// 按需引入ant-design
import 'ant-design-vue/dist/reset.css';
import { Popconfirm } from 'ant-design-vue';
app.use(Popconfirm);

app.mount('#app');

// 引入全局样式
import './assets/base.css';

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
