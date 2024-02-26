export const calcSize = (sizeRaw) => {
    if(sizeRaw == null || sizeRaw == undefined) return "";
    let size = "";
    if (sizeRaw < 0.1 * 1024) {                                                 // 小于0.1KB，则转化成B
        size = sizeRaw.toFixed(2) + " B"
    } else if (sizeRaw < 0.1 * 1024 * 1024) {                                   // 小于0.1MB，则转化成KB
        size = (sizeRaw / 1024).toFixed(2) + " KB"
    } else if (sizeRaw < 0.1 * 1024 * 1024 * 1024) {                            // 小于0.1GB，则转化成MB
        size = (sizeRaw / (1024 * 1024)).toFixed(2) + " MB"
    } else {                                                                    // 其他转化成GB
        size = (sizeRaw / (1024 * 1024 * 1024)).toFixed(2) + " GB"
    }
    let sizeStr = size + "";                                                    // 转成字符串
    let index = sizeStr.indexOf(".");                                           // 获取小数点处的索引
    let dou = sizeStr.substr(index + 1, 2)                                      // 获取小数点后两位的值
    if (dou == "00") {                                                          // 判断后两位是否为00，如果是则删除00               
        return sizeStr.substring(0, index) + sizeStr.substr(index + 3, 2)
    }
    return size;
}