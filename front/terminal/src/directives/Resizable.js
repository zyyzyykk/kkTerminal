const getTransformX = (element) => {
    // 获取计算后的样式
    const style = window.getComputedStyle(element);
    // 从样式中获取transform属性
    const transformValue = style.transform;
    if (transformValue === 'none') return 0;
    // 使用DOMMatrix解析transform字符串
    const matrix = new DOMMatrix(transformValue);
    // 读取m41属性，即translateX的值
    return matrix.m41;
};

/**
 * v-resizable: width resize for Dialog
 */
const resizableDirective = {
    updated(el, binding) {
        setTimeout(() => {
            // 获取必要的DOM元素
            const header = el.querySelector('.el-dialog__header');
            const body = el.querySelector('.el-dialog__body');
            if(!header && !body) return;
            // 防止重复
            const handle = el.querySelector('.resize-handle');
            if(handle) return;
            const dialog = el.querySelector('.el-dialog');
            // 最小/最大宽度
            const { minWidthRate, maxWidthRate } = binding.value || {};
            const minWidth = dialog.offsetWidth * (minWidthRate || 0.5);
            const maxWidth = Math.min(dialog.offsetWidth * (maxWidthRate || 2), document.documentElement.clientWidth * 0.8);
            // 创建拖拽条
            const leftHandle = document.createElement('div');
            leftHandle.classList.add('resize-handle', 'handle-left');
            const rightHandle = document.createElement('div');
            rightHandle.classList.add('resize-handle', 'handle-right');
            // 添加拖拽条
            header.appendChild(leftHandle);
            body.appendChild(leftHandle);
            header.appendChild(rightHandle);
            body.appendChild(rightHandle);
            // 边距
            const vergeWidth = {
                left: 0,
                right: 0,
            };
            // 鼠标事件
            leftHandle.onpointerdown = (event) => {
                // 阻止默认行为和事件冒泡
                event.preventDefault();
                event.stopPropagation();
                leftHandle.setPointerCapture(event.pointerId);
                vergeWidth.left = event.clientX - leftHandle.getBoundingClientRect().left;
                leftHandle.onpointermove = (event) => {
                    // 阻止默认行为和事件冒泡
                    event.preventDefault();
                    event.stopPropagation();
                    // 未按下鼠标主键
                    if(event.buttons !== 1) return;
                    // 可视窗口的宽度
                    const clientWidth = document.documentElement.clientWidth;
                    // 中心位置
                    const centerX = (clientWidth / 2) + getTransformX(dialog);
                    dialog.style.width = Math.min(maxWidth, Math.max(minWidth, ((centerX - event.clientX + vergeWidth.left) * 2))) + 'px';
                };
                leftHandle.onpointerup = () => {
                    leftHandle.onpointermove = null;
                    leftHandle.onpointerup = null;
                };
            };
            rightHandle.onpointerdown = (event) => {
                // 阻止默认行为和事件冒泡
                event.preventDefault();
                event.stopPropagation();
                rightHandle.setPointerCapture(event.pointerId);
                vergeWidth.right = rightHandle.getBoundingClientRect().right - event.clientX;
                rightHandle.onpointermove = (event) => {
                    // 阻止默认行为和事件冒泡
                    event.preventDefault();
                    event.stopPropagation();
                    // 未按下鼠标主键
                    if(event.buttons !== 1) return;
                    // 可视窗口的宽度
                    const clientWidth = document.documentElement.clientWidth;
                    // 中心位置
                    const centerX = (clientWidth / 2) + getTransformX(dialog);
                    dialog.style.width = Math.min(maxWidth, Math.max(minWidth, ((event.clientX - centerX + vergeWidth.right) * 2))) + 'px';
                };
                rightHandle.onpointerup = () => {
                    rightHandle.onpointermove = null;
                    rightHandle.onpointerup = null;
                };
            };
        }, 1);
    },
};

export default resizableDirective;
