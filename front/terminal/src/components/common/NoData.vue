<template>
  <div class="no-data" :style="viewStyle" >
    <div style="flex: 1;" ></div>
    <slot name="mySlot" ></slot>
    <div style="display: flex; align-items: center;" >
      <div><img src="@/assets/no_data.png" :alt="$t('暂无数据')" :style="{ width: imgWidth }" ></div>
    </div>
    <div class="msg" :style="{ fontSize: msgSize }" >{{ $t(msg) }}</div>
    <div style="flex: 1;" ></div>
  </div>
</template>

<script>
import { computed } from "vue";
import i18n from "@/locales/i18n";

export default {
  name: 'NoData',
  props: {
    msg: {
      type: String,
      required: false,
      default: i18n.global.k('暂无数据'),
    },
    width: {
      type: String,
      required: false,
    },
    height: {
      type: String,
      required: false,
    },
    minHeight: {
      type: String,
      required: false,
    },
    imgWidth: {
      type: String,
      required: false,
      default: '120px',
    },
    msgSize: {
      type: String,
      required: false,
      default: '14px',
    },
  },
  setup(props) {

    const viewStyle = computed(() => {
      return {
        width: props.width ? props.width : '100%',
        height: props.height ? props.height : '100%',
        minHeight: props.minHeight ? props.minHeight : '',
      }
    });

    return {
      viewStyle,
    }
  },
}
</script>

<style scoped>
.no-data {
  text-align: center;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.msg {
  margin-top: 10px;
  color: #909399;
}
</style>
