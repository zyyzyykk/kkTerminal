// 计算文件权限
const priorityDic = {
  "U":0,
  "G":1,
  "O":2,
  "R":0,
  "W":1,
  "X":2
}
export const calcPriority = (type,attrs) => {
    let rtype = '';
    switch (type) {
        case "REGULAR":
          rtype = "-";
          break;
        case "DIRECTORY":
          rtype = "d";
          break;
        case "SYMLINK":
          rtype = "l";
          break;
        default:
          rtype = "-";
          break;
    }
    let rattrs = ['-','-','-','-','-','-','-','-','-'];

    for(let i=0;i<attrs.length;i++) {
        let attr = attrs[i];
        let role = priorityDic[attr[0]];
        let name = priorityDic[attr[4]];
        if(role != null && role != undefined 
          && name != null && name != undefined 
          && attr[4] != null && attr[4] != undefined) {
            rattrs[role*3+name] = attr[4].toLowerCase();
        }
    }
    for(let i=0;i<rattrs.length;i++) rtype += rattrs[i];

    return rtype;
}