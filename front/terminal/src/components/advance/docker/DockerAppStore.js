import i18n from "@/locales/i18n";
import { generateRandomString } from "@/utils/StringUtil";

export const dockerAppTypes = [
    i18n.global.k('全部'),
    i18n.global.k('实用工具'),
    i18n.global.k('开发&运维'),
    i18n.global.k('个人建站'),
    i18n.global.k('数据库'),
    i18n.global.k('网盘存储'),
];

const imgs = require.context('@/assets/apps', false, /.*/);
const base_prefix = './';

export const dockerAppStore = [
    {
        name: 'Custom',
        desc: i18n.global.k('指定任意镜像进行应用部署'),
        type: 0,
        img: imgs(base_prefix + 'custom.svg'),
        deployInfo: {
            imageName: '',
            imageVersion: '',
            containerName: '',
            portMapping: '',
            envVar: '',
            volumeMounting: '',
            paramOptions: '',
        },
    },
    {
        name: 'kkTerminal',
        desc: i18n.global.k('kkTerminal是一个强大的Web SSH连接终端'),
        type: 1,
        img: imgs(base_prefix + 'kkTerminal.svg'),
        deployInfo: {
            imageName: 'zyyzyykk/kkterminal',
            imageVersion: 'latest',
            containerName: 'kkTerminal',
            portMapping: '3000:3000',
            envVar: 'PASSWORD=' + generateRandomString(16),
            volumeMounting: '/data/kkterminal/cloud:/cloud',
            paramOptions: '',
        },
    },
];
