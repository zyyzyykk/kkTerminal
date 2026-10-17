<template>
  <el-icon :style="statusStyle" >
    <CircleCloseFilled v-if="statusType === -2" />
    <WarningFilled v-else-if="statusType === -1" />
    <SuccessFilled v-else-if="statusType === 1" />
    <InfoFilled v-else />
  </el-icon>
</template>

<script>
import { computed } from "vue";
import {
  SuccessFilled,
  WarningFilled,
  CircleCloseFilled,
  InfoFilled,
} from "@element-plus/icons-vue";
import { CmdCodeStatusEnum } from "@/components/cmdcode/CmdCode";

export default {
  name: 'CmdCodeStatus',
  components: {
    SuccessFilled,
    WarningFilled,
    CircleCloseFilled,
    InfoFilled,
  },
  props: {
    status: {
      type: String,
      default: 'success',
    },
    style: {
      type: Object,
      default: (() => {}),
    },
  },
  setup(props) {

    const statusColor = ['#f56c6c', '#e6a23c', '#909399', '#67c23a'];

    const statusType = computed(() => {
      return CmdCodeStatusEnum[props.status].type || 0;
    });

    const statusStyle = computed(() => {
      return {
        fontSize: '16px',
        ...props.style,
        color: statusColor[statusType.value + 2],
      }
    });

    return {
      statusType,
      statusStyle,
    }
  }
}
</script>

<style scoped>
</style>
