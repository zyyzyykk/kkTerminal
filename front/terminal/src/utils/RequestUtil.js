import $ from "jquery";
import { secretKeyGetter, aesDecrypt } from '@/utils/Encrypt';
import { ElMessage } from 'element-plus';
import i18n from "@/locales/i18n";

let cookie = null;

export const request = (options) => {
    options.headers = {
        'X-Accept-Language': i18n.global.locale,
        'X-Cookie': cookie,
        ...options.headers,
    };
    const success = options.success;
    options.success = (resp, textStatus, jqXHR) => {
        cookie = jqXHR.getResponseHeader('X-Set-Cookie') || cookie;
        if(success) success(resp, textStatus, jqXHR);
    };
    return $.ajax(options);
};

export const ajaxSetup = () => {
    $.ajaxSetup({
        cache: false,                   // 禁用缓存
        processData: true,
        xhrFields:{
            withCredentials: true,      // 携带cookie
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
};
