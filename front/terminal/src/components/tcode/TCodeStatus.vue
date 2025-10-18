<template>
  <el-icon v-if="statusType === -2" :style="statusStyle" ><CircleCloseFilled /></el-icon>
  <el-icon v-else-if="statusType === -1" :style="statusStyle" ><WarningFilled /></el-icon>
  <el-icon v-else-if="statusType === 1" :style="statusStyle" ><SuccessFilled /></el-icon>
  <el-icon v-else :style="statusStyle" ><InfoFilled /></el-icon>
</template>

<script>
import { computed } from 'vue';

import { SuccessFilled, WarningFilled, CircleCloseFilled, InfoFilled } from '@element-plus/icons-vue'

export default {
  name: 'TCodeStatus',
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

    const statusTypeMap = {
      'Compile Error': -2,
      'Execute Interrupt': -1,
      'Not Active': 0,
      'Execute Success': 1,
    };

    const statusColor = ['#f56c6c','#e6a23c','#909399','#67c23a'];

    const statusType = computed(() => {
      return statusTypeMap[props.status] || 0;
    });

    const statusStyle = computed(() => {
      return {
        fontSize: '16px',
        ...props.style,
        color: statusColor[(statusTypeMap[props.status] || 0) + 2],
      }
    });

    return {
      statusStyle,
      statusType,
    }
  }
}
</script>

<style>
</style>
