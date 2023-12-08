<template>
  <slot name="forMid"></slot>
  <div class="no-data">
    <div>
      <div>
        <img src="../assets/no_data.png" alt="暂无数据" style="width: 120px;">
      </div>
      <div class="msg">{{ msg }}</div>
    </div>
  </div>
</template>

<script>
export default {
  name:'NoData',
  components: {
  },
  props:{
    msg: String,
  },

}
</script>

<style scoped>
.no-data {
  text-align: center;
  padding: 10px 0px;
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  align-content: center;
  justify-content: center;
}
.icon-empty {
  font-size: 50px;
  color: #bbb;
}
.msg {
  margin-top: 10px;
  color: #909399;
  font-size: 14px;
}
</style>