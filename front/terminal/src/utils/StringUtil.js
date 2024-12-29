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