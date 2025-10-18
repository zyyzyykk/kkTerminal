const BYTES_PER_KILOBYTE = 1024;
const B = 1;
const KB = BYTES_PER_KILOBYTE * B;
const MB = BYTES_PER_KILOBYTE * KB;
const GB = BYTES_PER_KILOBYTE * MB;

export const calcSize = (size) => {
    if(!size) return "";
    const _size = Math.max(0, parseInt(size));

    let value;
    let unit;
    if (_size < KB) {                                                       // B
        value = _size / B;
        unit = "B";
    }
    else if (_size < MB) {                                                  // KB
        value = _size / KB;
        unit = "KB";
    }
    else if (_size < GB) {                                                  // MB
        value = _size / MB;
        unit = "MB";
    }
    else {                                                                  // GB
        value = _size / GB;
        unit = "GB";
    }

    let formattedValue = value.toFixed(2);
    if (formattedValue.endsWith(".00")) {                                   // 移除 ".00"
        formattedValue = formattedValue.substring(0, formattedValue.length - 3);
    }

    return `${formattedValue} ${unit}`;
};
