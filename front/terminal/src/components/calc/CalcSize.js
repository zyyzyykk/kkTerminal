const BYTES_PER_KILOBYTE = 1024;
const B = 1;
const KB = BYTES_PER_KILOBYTE * B;
const MB = BYTES_PER_KILOBYTE * KB;
const GB = BYTES_PER_KILOBYTE * MB;

export const calcSize = (size) => {
    if(!size) return "";
    if(size < 0) size = 0;

    let value;
    let unit;
    if (size < KB) {                                                         // B
        value = size / B;
        unit = "B";
    }
    else if (size < MB) {                                                   // KB
        value = size / KB;
        unit = "KB";
    }
    else if (size < GB) {                                                   // MB
        value = size / MB;
        unit = "MB";
    }
    else {                                                                  // GB
        value = size / GB;
        unit = "GB";
    }

    let formattedValue = value.toFixed(2);
    if (formattedValue.endsWith(".00")) {                                   // 移除 ".00"
        formattedValue = formattedValue.substring(0, formattedValue.length - 3);
    }

    return `${formattedValue} ${unit}`;
};
