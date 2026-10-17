// 获取url参数
export const getUrlParams = (url) => {
  const _url = url || window.location.href;
  const urlParamsStr = doUrlSplit(_url)[1];
  const urlParams = new URLSearchParams(urlParamsStr).entries();
  const params = {};
  for (const [key, value] of urlParams) {
    params[key] = value;
  }
  return params;
};

// 获取无参url
export const getPureUrl = (url) => {
  const _url = url || window.location.href;
  return doUrlSplit(_url)[0];
};

const doUrlSplit = (url) => {
  const _url = url || window.location.href;
  return _url.split('?');
};

export const doUrlDownload = (url, name) => {
  const a = document.createElement('a');
  a.href = url;
  if(name) a.download = name;
  document.body.appendChild(a);
  a.click();
  document.body.removeChild(a);
};
