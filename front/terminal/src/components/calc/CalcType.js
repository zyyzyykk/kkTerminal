const typeArr = ["success", "warning", "danger"];

export const calcType = (a, b) => {
    if(b === 0) return typeArr[2];
    const r = a / b;
    if(r < 0.34) return typeArr[0];
    else if(r < 0.67) return typeArr[1];
    else return typeArr[2];
};

const statuArr = ["#5fcb71", "#dca550", "#eb5851"];

export const calcStatus = (str) => {
    const num = calcNumber(str);
    if(num < 33.4) return statuArr[0];
    else if(num < 66.7) return statuArr[1];
    else return statuArr[2];
};

export const calcNumber = (str, fixed=1) => {
    return parseFloat(Number(str).toFixed(fixed));
};
