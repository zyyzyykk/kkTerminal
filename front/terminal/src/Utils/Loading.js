// 加载中。。。
import { ElLoading } from 'element-plus'
export default function() {
    const loading = ElLoading.service({
        // lock: true,
        text: 'Loading',
        // background: 'rgba(0, 0, 0, 0.7)',
    })

    return loading;
}