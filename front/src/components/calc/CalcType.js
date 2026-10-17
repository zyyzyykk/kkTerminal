const typeArr = ["success", "warning", "danger"];

export const calcType = (a, b) => {
    if(b === 0) return typeArr[2];
    const r = a / b;
    if(r < 0.34) return typeArr[0];
    else if(r < 0.67) return typeArr[1];
    else return typeArr[2];
};

const statusArr = ["#67c23a", "#e6a23c", "#f56c6c"];

export const calcStatus = (str) => {
    const num = calcNumber(str);
    if(num < 33.4) return statusArr[0];
    else if(num < 66.7) return statusArr[1];
    else return statusArr[2];
};

export const calcNumber = (str, fixed=1) => {
    return parseFloat(Number(str).toFixed(fixed));
};
