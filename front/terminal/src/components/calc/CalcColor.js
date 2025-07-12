// 亮度阈值
const threshold = 128;

export const calcBgColor = (hexColor) => {
    // 去掉 #
    const hex = hexColor.replace('#', '');

    // 解析 R/G/B
    const r = parseInt(hex.substr(0, 2), 16);
    const g = parseInt(hex.substr(2, 2), 16);
    const b = parseInt(hex.substr(4, 2), 16);

    // 计算亮度值
    const luminance = 0.2126 * r + 0.7152 * g + 0.0722 * b;

    return luminance > threshold ? '#bad6fc' : '#555555';
};
