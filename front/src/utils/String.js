// 是否仅由数字和字母组成
export const isAlphaNumeric = (str) => {
  const regex = /^[A-Za-z0-9]+$/;
  return regex.test(str);
};

// 将 '/' 转为 '@'
export const changeStr = (str) => {
  return str.replace(/\//g, '@');
};

// 将 '@' 转为 '-'
export const changeStr2 = (str) => {
  return str.replace(/@/g, '-');
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
  return str.replace(pathChars, (match) => {
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
