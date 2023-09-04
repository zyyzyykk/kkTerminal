<template>
  <el-dialog
    v-model="DialogVisilble"
    :before-close="closeDialog"
    title="连接服务器设置"
    width="30%"
  >
    <div style="margin-top: -15px;"></div>
    <div class="item-class">
      <div style="display: flex; align-items: center; margin-bottom: 15px;">
        <div>用户名：</div>
        <div>
          <el-input v-model="setInfo.user_name" class="w-50 m-2" placeholder="请输入用户名">
            <template #prefix>
              <el-icon class="el-input__icon"><User /></el-icon>
            </template>
          </el-input>
        </div>
      </div>
      <div style="display: flex; align-items: center; margin-bottom: 5px;">
        <div>密 &nbsp; 码：</div>
        <div>
          <el-input v-model="setInfo.password" show-password class="w-50 m-2" placeholder="请输入密码">
            <template #prefix>
              <el-icon class="el-input__icon"><Lock /></el-icon>
            </template>
          </el-input>
        </div>
      </div>
      <div><el-checkbox class="a-link" v-model="setInfo.isRemember">记住</el-checkbox></div>
    </div>
    <div class="errInfo"> {{ err_msg }} </div>
    <div style="margin-bottom: 15px;"></div>
    <div style="display: flex; border-top: 1px solid #f1f2f4;">
      <div style="flex: 1;"></div>
      <el-button type="primary" @click="setSSH" style="margin-bottom: -15px; margin-top: 10px;">
        确定
      </el-button>
    </div>
  </el-dialog>
</template>

<script>
import { ref } from 'vue';

export default {
  name:'LoginSsh',
  components: {
  },
  setup(props,context) {

    // 控制Dialog显示
    const DialogVisilble = ref(false);
    const err_msg = ref('');

    // 设置信息
    const setInfo = ref({});
    const setSSH = () => {
      // 校验参数
      if(!(setInfo.value.user_name && setInfo.value.user_name != '')) {
        err_msg.value = '用户名不能为空';
        return;
      }
      if(!(setInfo.value.password && setInfo.value.password != '')) {
        err_msg.value = '密码不能为空';
        return;
      }
      context.emit('doSSHCheck',setInfo.value);
    }

    // 关闭
    const closeDialog = (done) => {
      err_msg.value = '';
      setInfo.value = {};
      done();
    }

    return {
      DialogVisilble,
      err_msg,
      closeDialog,
      setSSH,
      setInfo,

    }
  }


}
</script>

<style scoped>
.errInfo{
  font-size: 12px;
  color: rgb(234, 80, 80);
  margin-top: 5px;
}
</style>