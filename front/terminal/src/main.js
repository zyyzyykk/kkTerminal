// 浏览器窗口广播
import { initChannel } from "@/utils/ChannelUtil";
initChannel();

// jQuery配置Ajax全局响应拦截，进行数据解密
import $ from 'jquery';
import { secretKeyGetter, aesDecrypt } from '@/utils/Encrypt';
import { ElMessage } from 'element-plus';

$.ajaxSetup({
    cache: false,                   // 禁用缓存
    processData: true,
    xhrFields:{
      withCredentials: true,        // 携带cookie
    },
    statusCode: {                   // 响应码
        401() {
            ElMessage({
                message: i18n.global.t('会话已过期'),
                type: "warning",
                grouping: true,
                repeatNum: Number.MIN_SAFE_INTEGER,
            });
            setTimeout(() => {
                window.location.reload();
            }, 1000);
            return false;
        },
    },
    dataFilter(resp) {
      resp = JSON.parse(resp);
      if(resp.data) resp.data = JSON.parse(aesDecrypt(resp.data, secretKeyGetter.response()));
      return JSON.stringify(resp);
    },
});

import { createApp } from 'vue';
import App from './App.vue';

const app = createApp(App);

// Vue Router
import router from '@/router';
app.use(router);

// 自定义指令
import resizableDirective from '@/directives/Resizable';
app.directive('resizable', resizableDirective);

// 按需引入element-plus
import 'element-plus/dist/index.css';
import { ElButton, ElDialog, ElIcon, ElInput, ElUpload, ElColorPicker,ElDropdown, ElDropdownMenu, ElDropdownItem, ElSwitch, ElLoading, ElRadioGroup, ElRadio, ElPopover, ElTabs, ElTabPane, ElResult, ElTooltip, ElTag, ElPopconfirm, ElCheckbox, ElInputNumber, ElProgress, ElTable, ElCard, ElBadge } from 'element-plus';
app.use(ElButton).use(ElDialog).use(ElIcon).use(ElInput).use(ElUpload).use(ElColorPicker).use(ElTabs).use(ElTabPane).use(ElResult).use(ElTooltip).use(ElCheckbox).use(ElProgress).use(ElTable).use(ElBadge);
app.use(ElDropdown).use(ElDropdownMenu).use(ElDropdownItem).use(ElSwitch).use(ElLoading).use(ElRadioGroup).use(ElRadio).use(ElPopover).use(ElTag).use(ElPopconfirm).use(ElInputNumber).use(ElCard);

// i18n 国际化
import i18n from '@/locales/i18n';
app.use(i18n);

app.mount('#app');

// 引入全局样式
import '@/assets/base.css';

// 防抖: 解决 ElTable 自动宽度高度导致的「ResizeObserver loop limit exceeded」问题
const debounce = (fn, delay) => {
    let timer = null;
    return function () {
        const context = this;
        const args = arguments;
        clearTimeout(timer);
        timer = setTimeout(function () {
            fn.apply(context, args);
        }, delay);
    }
}
const _ResizeObserver = window.ResizeObserver;
window.ResizeObserver = class ResizeObserver extends _ResizeObserver {
    constructor(callback) {
        callback = debounce(callback, 16);
        super(callback);
    }
};

// 禁用警告输出
app.config.warnHandler = () => {};
