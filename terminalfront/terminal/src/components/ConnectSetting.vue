<template>
  <el-dialog
    v-model="DialogVisilble"
    destroy-on-close
    :width="360"
    title="连接设置"
    :modal="false"
    draggable
  >
    <div style="margin-top: -15px;"></div>
    <div>
      <div class="item-class" style="margin-bottom: 15px;">
        <div>主机ip：</div>
        <div>
          <el-input v-model="setInfo.server_ip" class="w-50 m-2" placeholder="输入主机ip">
            <template #prefix>
              <el-icon><HomeFilled /></el-icon>
            </template>
          </el-input>
        </div>
      </div>
      <div class="item-class" style="margin-bottom: 15px;">
        <div>端口号：</div>
        <div>
          <el-input v-model="setInfo.server_port" class="w-50 m-2" placeholder="输入端口号">
            <template #prefix>
              <el-icon><Paperclip /></el-icon>
            </template>
          </el-input>
        </div>
      </div>
      <div class="item-class" style="margin-bottom: 15px;">
        <div>用户名：</div>
        <div>
          <el-input v-model="setInfo.server_user" class="w-50 m-2" placeholder="输入用户名">
            <template #prefix>
              <el-icon class="el-input__icon"><User /></el-icon>
            </template>
          </el-input>
        </div>
      </div>
      <div class="item-class" style="margin-bottom: 5px;">
        <div>密 &nbsp; 码：</div>
        <div>
          <el-input v-model="setInfo.server_password" type="password" class="w-50 m-2" placeholder="输入密码">
            <template #prefix>
              <el-icon class="el-input__icon"><Lock /></el-icon>
            </template>
          </el-input>
        </div>
      </div>
    </div>
    <div class="errInfo"> {{ err_msg }} </div>
    <div style="margin-bottom: 5px;"></div>
    <div style="display: flex; border-top: 1px solid #f1f2f4;">
      <div style="flex: 1;"></div>
      <el-button size="small" type="primary" @click="confirm" style="margin-bottom: -15px; margin-top: 10px;">
        确定
      </el-button>
    </div>
  </el-dialog>
</template>

<script>
import { ref } from 'vue';

export default {
  name:'ConnectSetting',
  components: {
  },
  props:['env'],
  setup(props,context) {

    // 控制Dialog显示
    const DialogVisilble = ref(false);
    const err_msg = ref('');

    // 设置信息
    const setInfo = ref({
      server_ip: props.env.server_ip,
      server_port: props.env.server_port,
      server_user: props.env.server_user,
      server_password:props.env.server_password,
    });
    const confirm = () => {
      context.emit('callback',setInfo.value);
      DialogVisilble.value = false;
    }

    return {
      setInfo,
      DialogVisilble,
      err_msg,
      confirm,

    }
  }


}
</script>

<style scoped>

.item-class {
  display: flex; 
  align-items: center; 
}

.errInfo{
  font-size: 12px;
  color: rgb(234, 80, 80);
  margin-top: 5px;
}
</style>