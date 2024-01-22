<template>
  <el-dialog
    v-model="DialogVisilble"
    destroy-on-close
    :width="360"
    title="连接设置"
    :modal="false"
    :append-to-body="true"
    :close-on-click-modal="false"
    draggable
  >
    <div style="margin-top: -15px;"></div>
    <div>
      <div class="item-class" style="margin-bottom: 15px;">
        <div class="no-select">配 &nbsp; 置：</div>
        <div :class="!(option && option.length > 0) ? 'new-option': 'old-option'" >{{ !(option && option.length > 0) ? '新建配置' : option }}</div>
        <div style="flex: 1;"></div>
        <div><el-button size="small" type="primary" @click="confirm" >导入</el-button></div>
        <div><el-button size="small" type="primary" @click="confirm" style="margin-left: 10px;" >保存</el-button></div>
      </div>
      <div class="item-class" style="margin-bottom: 15px;">
        <div class="no-select">主机ip：</div>
          <div>
            <el-input v-model="setInfo.server_ip" class="w-50 m-2" placeholder="输入主机ip">
              <template #prefix>
                <el-icon><HomeFilled /></el-icon>
              </template>
            </el-input>
        </div>
      </div>
      <div class="item-class" style="margin-bottom: 15px;">
        <div class="no-select">端口号：</div>
        <div>
          <el-input v-model="setInfo.server_port" class="w-50 m-2" placeholder="输入端口号">
            <template #prefix>
              <el-icon><Paperclip /></el-icon>
            </template>
          </el-input>
        </div>
      </div>
      <div class="item-class" style="margin-bottom: 15px;">
        <div class="no-select">用户名：</div>
        <div>
          <el-input v-model="setInfo.server_user" class="w-50 m-2" placeholder="输入用户名">
            <template #prefix>
              <el-icon class="el-input__icon"><User /></el-icon>
            </template>
          </el-input>
        </div>
      </div>
      <div class="item-class" style="margin-bottom: 5px;">
        <div class="no-select">密 &nbsp; 码：</div>
        <div>
          <el-input v-model="setInfo.server_password" type="password" class="w-50 m-2" placeholder="输入密码">
            <template #prefix>
              <el-icon class="el-input__icon"><Lock /></el-icon>
            </template>
          </el-input>
        </div>
      </div>
    </div>
    <div class="errInfo no-select"> {{ err_msg }} </div>
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

    // 配置选项
    const option = ref(props.option);
    const useOption = () => {
      if(option.value != null && localStorage.getItem("option-" + option.value)) {
        setInfo.value = JSON.parse(localStorage.getItem("option-" + option.value));
      }
    }
    useOption();


    const confirm = () => {
      // 校验参数
      err_msg.value = '';
      // 验证IP地址
      const ipRegex = /^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$/;
      if (!ipRegex.test(setInfo.value.server_ip)) {
        err_msg.value = "主机ip地址无效";
        return;
      }
      // 校验端口号
      if (isNaN(setInfo.value.server_port) || setInfo.value.server_port < 0 || setInfo.value.server_port > 65535) {
        err_msg.value = "端口号无效";
        return;
      }
      // 校验用户名
      if (!(setInfo.value.server_user && setInfo.value.server_user != '')) {
        err_msg.value = "用户名不能为空";
        return;
      }
      // 校验密码
      if (!(setInfo.value.server_password && setInfo.value.server_password != '')) {
        err_msg.value = "密码不能为空";
        return;
      }
      context.emit('callback',setInfo.value);
      DialogVisilble.value = false;
    }

    return {
      setInfo,
      DialogVisilble,
      err_msg,
      confirm,
      option,

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
  margin-top: 8px;
}

/* 文本不可选中 */
.no-select {
  user-select: none;
}

.old-option {
  color: #67c23a;
}

.new-option {
  color: #f56c6c;
}
</style>