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

    // 使用MutationObserver监听DOM变化
    let mutationObserver = null;

    const observeTextChanges = () => {
      if(tooltipContent.value) {
        mutationObserver = new MutationObserver(() => {
          checkOverflow();
        });
        mutationObserver.observe(tooltipContent.value, {
          childList: true,          // 监听子节点的变化
          subtree: true,            // 监听所有后代节点的变化
          characterData: true,      // 监听文本节点的变化
        });
      }
    };

    onMounted(() => {
      checkOverflow();
      observeTextChanges();
    });

    onBeforeUnmount(() => {
      if(mutationObserver) mutationObserver.disconnect();
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