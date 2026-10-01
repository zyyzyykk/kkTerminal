// 生产环境
// import router from "@/router";
//
// const router_base_path = (router.options.history.base + '/').replace(/\/{2,}/g, '/');
// const http_protocol = window.location.protocol + '//';
// export const http_base_url = http_protocol + window.location.host + router_base_path + 'api';
// const ws_protocol = (http_protocol === 'https://') ? 'wss://' : 'ws://';
// export const ws_base_url = ws_protocol + window.location.host + router_base_path + 'api/socket/ssh/';

// 开发环境
export const ws_base_url = 'ws://localhost:3000/api/socket/ssh/';
export const http_base_url = 'http://localhost:3000/api';

// 在线环境
// export const ws_base_url = 'wss://ssh.kkbpro.com/api/socket/ssh/';
// export const http_base_url = 'https://ssh.kkbpro.com/api';
