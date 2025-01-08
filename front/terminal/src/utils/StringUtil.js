// 将 '/' 转为 '@'
export const changeStr = (str) => {
  let result = '';
  for (let i = 0; i < str.length; i++) {
    if(str[i] != '/') result += str[i];
    else result += '@';
  }
  
  return result;
};

// 将 '@' 转为 '-'
export const changeStr2 = (str) => {
  let result = '';
  for (let i = 0; i < str.length; i++) {
    if(str[i] != '@') result += str[i];
    else result += '-';
  }
  
  return result;
};

// 生成随机字符串
export const generateRandomString = (length) => {
  // 字符集合
  const chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
  let result = '';
  for (let i = 0; i < length; i++) {
      const randomIndex = Math.floor(Math.random() * chars.length);
      result += chars[randomIndex];
  }
  return result;
}

// 转义字符串(路径)
const pathChars = /[ !"#$&'()*,:;<=>?@[\\\]^`{}~]/g;
export const escapePath = (str) => {
  return str.replace(pathChars, function(match) {
    return '\\' + match;
  });
};

// 转义字符串(文件项)
export const escapeItem = (str) => {
  return escapePath(str);
};