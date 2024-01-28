<template>
  <div ref="fileOptionBody" v-show="isShow == true" class="file-option">
    <div class="li-class">刷新</div>
    <div class="li-class">打开</div>
    <div class="li-class">复制路径</div>
    <div class="li-class">下载</div>
    <div class="li-class">新建</div>
    <div class="li-class">重命名</div>
    <div class="li-class">删除</div>
    <div class="li-class">属性</div>
  </div>
</template>

<script>
import { ref } from 'vue';

export default {
  name: 'FileOption',
  components: {
  },
  props:['file'],
  setup()
  {
    const isShow = ref(false);
    const fileOptionBody = ref();

    return {
      isShow,
      fileOptionBody,

    }
  }
}
</script>

<style scoped>
.file-option {
  position: absolute;
  z-index: 9999;
}

.li-class {
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f2f2f2;
  text-align: center;
  width: 80px;
  height: 25px;
  font-size: 13px;
  color: #666;
  cursor: pointer;
  padding: 3px 3px;
}

.li-class:hover {
  background-color: #91c9f7;
}
</style>