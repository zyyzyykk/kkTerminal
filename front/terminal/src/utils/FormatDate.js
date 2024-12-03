import i18n from "@/locales/i18n";

// 将时间戳转为 2024年1月22日，20:40:38 格式
export const formatDate = (time) => {
    if(!time) return "";
    let date = new Date(time*1000);
    let year = date.getFullYear();
    let month = date.getMonth() + 1;
    let day = date.getDate();
    let hour = date.getHours();
    let min = date.getMinutes();
    let seconds = date.getSeconds();

    if(month < 10) month = "0" + month;
    if(day < 10) day = "0" + day;
    if(hour < 10) hour = "0" + hour;
    if(min < 10) min = "0" + min;
    if(seconds < 10) seconds = "0" + seconds;

    let dateArr = null;
    if(i18n.global.locale === "zh") dateArr = [year, month, day];
    else dateArr = [month, day, year];

    return dateArr[0] + i18n.global.t("年") + dateArr[1] + i18n.global.t("月") + dateArr[2] + i18n.global.t("日，") + hour + ":" + min + ":" + seconds;
};