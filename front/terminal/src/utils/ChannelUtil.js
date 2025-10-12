import router from "@/router";
import { secretKeySetter } from "@/utils/Encrypt";

// 浏览器窗口广播
let channel = null;

export const getChannel = () => {
    return channel;
};

export const messageDict = {
  'RESPONSE_KEY_UPDATE': 'RESPONSE_KEY_UPDATE',
};

export const initChannel = () => {
    if(channel) return;
    channel = new BroadcastChannel('kkTerminal');
    channel.onmessage = (e) => {
        const { message, data } = JSON.parse(e.data);
        if(message === messageDict['RESPONSE_KEY_UPDATE']) {
            secretKeySetter.response(data);
            const route = router.currentRoute.value;
            if(route.name !== 'terminal') {
                router.push({
                    name: 'terminal',
                    query: {
                        ...route.query,
                    },
                });
            }
        }
    };
};
