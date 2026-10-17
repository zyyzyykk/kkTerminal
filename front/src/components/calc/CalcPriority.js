export const calcPriority = (type,attrs) => {
  return getType(type) + getAttrs(attrs);
};

// 获取所有者、所属组和其他用户的权限数组
export const getPermissionInfo = (attrs) => {
  const permissionString = calcPriority('REGULAR', attrs).slice(1);   // 仅提取权限部分
  return {
    owner: [permissionString[0] === 'r', permissionString[1] === 'w', permissionString[2] === 'x' || permissionString[2] === 's' || permissionString[2] === 'S'],
    group: [permissionString[3] === 'r', permissionString[4] === 'w', permissionString[5] === 'x' || permissionString[5] === 's' || permissionString[5] === 'S'],
    others: [permissionString[6] === 'r', permissionString[7] === 'w', permissionString[8] === 'x' || permissionString[8] === 't' || permissionString[8] === 'T'],
    sub: false,
  };
};

// chmod权限整数值
export const getChmodValue = (permissions) => {
  const calculateSection = (section) => {
    return (section[0] ? 4 : 0) + (section[1] ? 2 : 0) + (section[2] ? 1 : 0);
  };
  const ownerValue = calculateSection(permissions.owner).toString();
  const groupValue = calculateSection(permissions.group).toString();
  const othersValue = calculateSection(permissions.others).toString();

  return ownerValue + groupValue + othersValue;
};

const fileTypeSymbols = {
  'BLOCK_SPECIAL': 'b',
  'CHAR_SPECIAL': 'c',
  'FIFO_SPECIAL': 'p',
  'SOCKET_SPECIAL': 's',
  'REGULAR': '-',
  'DIRECTORY': 'd',
  'SYMLINK': 'l',
  'UNKNOWN': '?'
};

const getType = (type) => {
  return fileTypeSymbols[type];
};

const permissionDict = {
  'USR_R': 256,
  'USR_W': 128,
  'USR_X': 64,
  'GRP_R': 32,
  'GRP_W': 16,
  'GRP_X': 8,
  'OTH_R': 4,
  'OTH_W': 2,
  'OTH_X': 1,
  'SUID': 2048,
  'SGID': 1024,
  'STICKY': 512
};

const getAttrs = (attrs) => {
  let symbol = '---------';
  // 将权限数组转换为对应的权限值总和
  let permissionValue = 0;
  for(let i=0;i<attrs.length;i++) if(permissionDict[attrs[i]]) permissionValue += permissionDict[attrs[i]];

  // 设置用户权限
  symbol = setPermissionSymbol(symbol, 0, permissionValue & permissionDict['USR_R'], 'r');
  symbol = setPermissionSymbol(symbol, 1, permissionValue & permissionDict['USR_W'], 'w');
  symbol = setPermissionSymbol(symbol, 2, permissionValue & permissionDict['USR_X'], 'x', permissionValue & permissionDict['SUID'], 's', 'S');

  // 设置组权限
  symbol = setPermissionSymbol(symbol, 3, permissionValue & permissionDict['GRP_R'], 'r');
  symbol = setPermissionSymbol(symbol, 4, permissionValue & permissionDict['GRP_W'], 'w');
  symbol = setPermissionSymbol(symbol, 5, permissionValue & permissionDict['GRP_X'], 'x', permissionValue & permissionDict['SGID'], 's', 'S');

  // 设置其他权限
  symbol = setPermissionSymbol(symbol, 6, permissionValue & permissionDict['OTH_R'], 'r');
  symbol = setPermissionSymbol(symbol, 7, permissionValue & permissionDict['OTH_W'], 'w');
  symbol = setPermissionSymbol(symbol, 8, permissionValue & permissionDict['OTH_X'], 'x', permissionValue & permissionDict['STICKY'], 't', 'T');

  return symbol;
};

// 设置权限符号
const setPermissionSymbol = (symbol, index, attr, char, specialPermission = 0, specialChar = '', noPermissionChar = '-') => {
  if (specialPermission) symbol = symbol.substring(0, index) + (attr ? specialChar : noPermissionChar) + symbol.substring(index + 1);
  else symbol = symbol.substring(0, index) + (attr ? char : symbol[index]) + symbol.substring(index + 1);
  return symbol;
};
