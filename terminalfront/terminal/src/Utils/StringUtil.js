// 生成随机字符串
export function generateRandomString(length) {
  let result = '';
  const characters = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
  const charactersLength = characters.length;

  for (let i = 0; i < length; i++) {
    result += characters.charAt(Math.floor(Math.random() * charactersLength));
  }

  return result;
}

// 将 '/' 转为 '@'
export function changeStr(str) {
  let result = '';
  for (let i = 0; i < str.length; i++) {
    if(str[i] != '/') result += str[i];
    else result += '@';
  }
  
  return result;
}