<template>
  <div ref="decorationWrapperRef" class="decoration-wrapper" >
    <CircleCloseFilled v-if="statusType === -1" @click="clickDecoration" @mouseleave="showMenu(false)" :style="statusStyle" />
    <SuccessFilled v-else-if="statusType === 1" @click="clickDecoration" @mouseleave="showMenu(false)" :style="statusStyle" />
    <Remove v-else :style="statusStyle" />
    <div ref="decorationMenuRef" v-if="isShowMenu"
         @mousemove="showMenu(true)" @mouseleave="showMenu(false)"
         class="kk-menu no-select"
         :style="{ width: i18n.global.t('130') + 'px', top: menuTop + 'px' }" >
      <div :class="['kk-menu-item', isDisabled ? 'disabled-text':'']"
           @click="isDisabled ? $event.stopPropagation() : handleMenuSelect(1)"
           style="border-bottom: 1px solid #ddd;" key="1" >
        {{ i18n.global.t('重新运行此命令') }}
      </div>
      <div class="kk-menu-item"
           @click="handleMenuSelect(2)" key="2" >
        {{ i18n.global.t('复制此命令') }}
      </div>
      <div class="kk-menu-item"
           @click="handleMenuSelect(3)"
           style="border-bottom: 1px solid #ddd;" key="3" >
        {{ i18n.global.t('复制命令输出') }}
      </div>
      <div :class="['kk-menu-item', isDisabled ? 'disabled-text':'']"
           @click="isDisabled ? $event.stopPropagation() : handleMenuSelect(4)" key="4" >
        {{ i18n.global.t('打开运行目录') }}
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed } from "vue";
import i18n from "@/locales/i18n";
import browser from "@/utils/Browser";
import {
  SuccessFilled,
  CircleCloseFilled,
  Remove,
} from "@element-plus/icons-vue";

export default {
  name: 'ShellStatus',
  components: {
    SuccessFilled,
    CircleCloseFilled,
    Remove,
  },
  props: {
    data: {
      type: Object,
      default: (() => {}),
    },
    isDisabled: {
      type: Boolean,
      default: false,
    },
  },
  setup(props, context) {

    const statusColor = ['#f53f3f', '#73767a', '#00b42a'];

    const statusType = computed(() => {
      if(props.data.exitCode === 0) return 1;
      else if(!props.data.exitCode) return 0;
      else return -1;
    });

    const statusStyle = computed(() => {
      return {
        color: statusColor[statusType.value + 1],
        padding: '2px 2px',
        cursor: statusType.value !== 0 ? 'pointer' : '',
      }
    });

    const isShowMenu = ref(false);
    const decorationWrapperRef = ref();
    const decorationMenuRef = ref();
    const menuTop = ref(0);
    const clickDecoration = () => {
      const viewportHeight = window.innerHeight;
      const wrapperTop = decorationWrapperRef.value.getBoundingClientRect().top;
      const menuHeight = 8 * 2 + 30 * 4;
      // 防止超出下边界
      menuTop.value = Math.min(0, viewportHeight - wrapperTop - menuHeight);
      isShowMenu.value = true;
    };
    let timer = null;
    const showMenu = (newVal) => {
      clearTimeout(timer);
      if(isShowMenu.value !== newVal) {
        timer = browser.setTimeout(() => {
          isShowMenu.value = newVal;
        }, 400);
      }
    };
    const handleMenuSelect = (type) => {
      context.emit('handleMenuSelect', type, props.data);
      clearTimeout(timer);
      isShowMenu.value = false;
    };

    return {
      i18n,
      statusType,
      statusStyle,
      isShowMenu,
      decorationWrapperRef,
      decorationMenuRef,
      menuTop,
      clickDecoration,
      showMenu,
      handleMenuSelect,
    }
  }
}
</script>

<style scoped>
.decoration-wrapper {
  position: relative;
}

.kk-menu {
  position: absolute;
  left: 16px;
  z-index: 3466;
  text-align: left;
  border-radius: 8px;
  border-top: 8px solid #f2f2f2;
  border-bottom: 8px solid #f2f2f2;
  box-shadow: 0 6px 12px 0 rgba(0, 0, 0, 0.15), 0 3px 6px -2px rgba(0, 0, 0, 0.2), 0 8px 16px 4px rgba(0, 0, 0, 0.12);
}

.kk-menu-item {
  background-color: #f2f2f2;
  padding-left: 10px;
  width: 100%;
  height: 30px;
  line-height: 30px;
  font-size: 13px;
  color: #383838;
  cursor: pointer;
}

.kk-menu-item:hover {
  background-color: #91c9f7;
}
</style>

<style>
.xterm-decoration-container .xterm-decoration {
  width: 16px !important;
  left: -16px !important;
}
</style>
