// 上线环境
// const http_protocol = window.location.protocol;
// let ws_protocol = 'ws://';
// if(http_protocol === 'https:') ws_protocol = 'wss://';
// export const ws_base_url = ws_protocol + window.location.host + '/socket/ssh/';
// export const http_base_url = http_protocol + '//' + window.location.host + '/api';

// 本地开发环境
export const ws_base_url = 'ws://localhost:3000/socket/ssh/';
export const http_base_url = 'http://localhost:3000/api';

// 在线环境
// export const ws_base_url = 'wss://ssh.kkbpro.com/socket/ssh/';
// export const http_base_url = 'https://ssh.kkbpro.com/api';
