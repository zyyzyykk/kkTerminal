<template>
  <div element-loading-text="Loading..." v-loading="loading" ref="eChartRef" ></div>
</template>

<script>
import { onMounted, ref } from 'vue';
import * as echarts from 'echarts';

export default {
  name: 'EChart',
  props: ['legend', 'yName'],
  setup(props) {

    const loading = ref(true);
    const isFinished = ref(false);
    const eChartRef = ref();
    const option = {
      title: {
        text: '',
      },
      tooltip: {
        trigger: 'axis'
      },
      legend: {
        data: props.legend,
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '3%',
        containLabel: true
      },
      xAxis: {
        type: 'category',
        boundaryGap: false,
        data: [],
      },
      yAxis: {
        name : props.yName,
        type: 'value'
      },
      series: [],
    };

    let chart = null;
    const update = (time,series) => {
      option.xAxis.data = time;
      option.series = series;
      // 更新图表
      if (chart) {
        isFinished.value = false;
        chart.setOption(option);
      }
    };

    onMounted(() => {
      // 初始化ECharts实例并应用配置项
      chart = echarts.init(eChartRef.value);
      chart.setOption(option);
      chart.resize();
      chart.on('finished', function() {
        isFinished.value = true;
        loading.value = false;
      });
    });

    return {
      eChartRef,
      update,
      loading,
      isFinished,
    }
  }
}
</script>

<style scoped >

/* 文本不可选中 */
.no-select {
  user-select: none;
}

</style>
