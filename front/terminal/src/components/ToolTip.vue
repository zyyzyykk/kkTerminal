<template>
  <el-tooltip popper-class="no-select" :content="content" :disabled="!(isShow && isShowTooltip)" :show-after="delay" placement="top">
    <div ref="tooltipContent" class="tooltip-content">
      <slot name="content"></slot>
    </div>
  </el-tooltip>
</template>

<script>
import { ref, onMounted, onBeforeUnmount } from 'vue';

export default {
  name: 'ToolTip',
  props: {
    content: {
      type:String,
      required:true,
      default:'',
    },
    delay: {
      type:Number,
      required:false,
      default:200,
    },
    isShow: {
      type:Boolean,
      required:false,
      default:true,
    }
  },
  setup() {

    const tooltipContent = ref(null);
    const isShowTooltip = ref(false);

    // 检查内容是否溢出
    const checkOverflow = () => {
      setTimeout(() => {
        if(tooltipContent.value && tooltipContent.value.firstElementChild) {
          isShowTooltip.value = tooltipContent.value.firstElementChild.scrollWidth > tooltipContent.value.firstElementChild.offsetWidth;
        }
        else isShowTooltip.value = false;
      },1);
    };

    // 使用ResizeObserver监听尺寸变化
    let resizeObserver = null;

    const observeSizeChanges = () => {
      if(tooltipContent.value) {
        resizeObserver = new ResizeObserver(() => {
          checkOverflow();
        });
        resizeObserver.observe(tooltipContent.value);
      }
    };

    onMounted(() => {
      checkOverflow();
      observeSizeChanges();
    });

    onBeforeUnmount(() => {
      if(resizeObserver && tooltipContent.value) {
        resizeObserver.unobserve(tooltipContent.value);
        resizeObserver.disconnect();
      }
    });

    return {
      tooltipContent,
      isShowTooltip,
      checkOverflow,
    };
  },
};
</script>

<style scoped>
.tooltip-content {
  display: inline-block;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
</style>  