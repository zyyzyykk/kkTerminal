import { createI18n } from 'vue-i18n';
import en from './en_US.json';

const i18n = createI18n({
    locale: 'en',
    fallbackLocale: 'zh',
    messages: { en },
    missing: (locale, key) => {
        return key;
    },
});

i18n.global.k = (val) => {
    return val;
};

export default i18n;
