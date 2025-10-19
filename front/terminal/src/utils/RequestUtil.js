import $ from "jquery";
import i18n from "@/locales/i18n";

const request = (options) => {
    options.headers = {
        'X-Accept-Language': i18n.global.locale,
        ...options.headers,
    };
    return $.ajax(options);
};

export default request;
