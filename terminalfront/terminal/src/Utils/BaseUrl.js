// 上线环境
// let http_protocol = window.location.protocol;
// let ws_protocol = '';
// if(http_protocol == 'https:') ws_protocol = 'wss://';
// else ws_protocol = 'ws://';
// export const ws_base_url = ws_protocol + window.location.host + '/socket/ssh/';
// export const http_base_url = http_protocol + '//' + window.location.host + '/api';

// 本地开发环境
export const ws_base_url = 'ws://localhost:3000/api/ssh/';
export const http_base_url = 'http://localhost:3000/api';