// 将 '/' 转为 '@'
export const changeStr = (str) => {
  let result = '';
  for (let i = 0; i < str.length; i++) {
    if(str[i] !== '/') result += str[i];
    else result += '@';
  }

  return result;
};

// 将 '@' 转为 '-'
export const changeStr2 = (str) => {
  let result = '';
  for (let i = 0; i < str.length; i++) {
    if(str[i] !== '@') result += str[i];
    else result += '-';
  }

  return result;
};

// 将Base64编码转换为合法的URL参数
export const changeBase64Str = (str) => {
  return str.replace(/\+/g, '-').replace(/=/g, '@');
};

export const changeStrBase64 = (str) => {
  return str.replace(/-/g, '+').replace(/@/g, '=');
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

// 文件自然排序
export const osFileNaturalSort = (files) => {
  const naturalSortedFiles = files.sort((a, b) => {
    const nameA = a.name;
    const nameB = b.name;
    // 隐藏文件优先
    if(nameA[0] === '.' && nameB[0] !== '.') return -1;
    if(nameA[0] !== '.' && nameB[0] === '.') return 1;
    // 启用数字感知并忽略大小写和变音符号
    return nameA.localeCompare(nameB, undefined, { numeric: true, sensitivity: 'base' });
  });
  for(let i = 0; i < naturalSortedFiles.length; i++) naturalSortedFiles[i].index = i;
  return naturalSortedFiles;
};
