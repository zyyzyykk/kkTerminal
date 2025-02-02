const typeArr = ["success", "warning", "danger"];

export const calcType = (a, b) => {
    if(b === 0) return typeArr[2];
    const r = a / b;
    if(r < 0.34) return typeArr[0];
    else if(r < 0.67) return typeArr[1];
    else return typeArr[2];
};
