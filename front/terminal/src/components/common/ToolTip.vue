<template>
  <el-tooltip popper-class="no-select" :content="content" :disabled="!(isShow && isShowTooltip)" :show-after="delay" placement="top" >
    <div ref="toolTipContentRef" class="tooltip-content ellipsis" :style="parentStyle" >
      <slot name="content" ></slot>
    </div>
  </el-tooltip>
</template>

<script>
import { ref, onMounted, onBeforeUnmount } from "vue";
import browser from "@/utils/Browser";

export default {
  name: 'ToolTip',
  props: {
    content: {
      type: String,
      required: true,
      default: '',
    },
    delay: {
      type: Number,
      required: false,
      default: 500,
    },
    isShow: {
      type: Boolean,
      required: false,
      default: true,
    },
    parentStyle: {
      type: Object,
      required: false,
      default: (() => {}),
    },
  },
  setup() {

    const toolTipContentRef = ref(null);
    const isShowTooltip = ref(false);

    // 检查内容是否溢出
    const checkOverflow = () => {
      browser.setTimeout(() => {
        if(toolTipContentRef.value && toolTipContentRef.value.firstElementChild) {
          isShowTooltip.value = toolTipContentRef.value.firstElementChild.scrollWidth > toolTipContentRef.value.firstElementChild.offsetWidth;
        }
        else isShowTooltip.value = false;
      }, 1);
    };

    // 使用ResizeObserver监听尺寸变化
    let resizeObserver = null;

    const observeSizeChanges = () => {
      if(toolTipContentRef.value) {
        resizeObserver = new ResizeObserver(() => {
          checkOverflow();
        });
        resizeObserver.observe(toolTipContentRef.value);
      }
    };

    onMounted(() => {
      checkOverflow();
      observeSizeChanges();
    });

    onBeforeUnmount(() => {
      if(resizeObserver && toolTipContentRef.value) {
        resizeObserver.unobserve(toolTipContentRef.value);
        resizeObserver.disconnect();
      }
    });

    return {
      toolTipContentRef,
      isShowTooltip,
      checkOverflow,
    };
  },
};
</script>

<style scoped>
.tooltip-content {
  display: inline-block;
}
</style>
