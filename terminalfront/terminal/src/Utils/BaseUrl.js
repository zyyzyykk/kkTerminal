// 上线环境
// let http_protocol = window.location.protocol;
// let ws_protocol = '';
// if(http_protocol == 'https:') ws_protocol = 'wss://';
// else ws_protocol = 'ws://';
// const base_url = ws_protocol + window.location.host + '/socket/ssh/';

// 本地开发环境
const base_url = 'ws://localhost:3000/socket/ssh/';

export default base_url;