export const calcSize = (sizeRaw) => {
    if(sizeRaw == null || sizeRaw == undefined) return "";
    let size = "";
    if (sizeRaw < 1024) {                                                       // B
        size = sizeRaw.toFixed(2) + " B"
    } else if (sizeRaw < 1024 * 1024) {                                         // KB
        size = (sizeRaw / 1024).toFixed(2) + " KB"
    } else if (sizeRaw < 1024 * 1024 * 1024) {                                  // MB
        size = (sizeRaw / (1024 * 1024)).toFixed(2) + " MB"
    } else {                                                                    // GB
        size = (sizeRaw / (1024 * 1024 * 1024)).toFixed(2) + " GB"
    }
    let sizeStr = size + "";                                                    // 转成字符串
    let index = sizeStr.indexOf(".");                                           // 获取小数点处的索引
    let dou = sizeStr.substring(index + 1, index + 3)                           // 获取小数点后两位的值
    if (dou == "00") {                                                          // 判断后两位是否为00，如果是则删除00               
        return sizeStr.substring(0, index) + sizeStr.substring(index + 3)
    }
    return size;
}