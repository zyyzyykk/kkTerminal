<template>
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
  width: 100%;
  height: 30vh;
  display: flex;
  align-items: center;
  justify-content: center;
}

.msg {
  margin-top: 10px;
  color: #909399;
  font-size: 14px;
}
</style>