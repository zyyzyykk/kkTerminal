<template>
  <el-dialog
    v-model="DialogVisilble"
    :destory-on-close="true"
    :before-close="closeDialog"
    :width="360"
    title="连接设置"
    :modal="false"
    :close-on-click-modal="false"
    modal-class="kk-dialog-class"
    align-center
    draggable
  >
    <div style="margin-top: -25px;"></div>
    <div>
      <div class="item-class" style="margin-bottom: 15px;">
        <div class="no-select nowrap">配 &nbsp; 置：</div>
        <div class="ellipsis" style="user-select: none;" :class="!(setInfo.option && setInfo.option.length > 0) ? 'new-option': 'old-option'" >{{ !(setInfo.option && setInfo.option.length > 0) ? '新建配置' : setInfo.option }}</div>
        <div style="flex: 1;"></div>
        <div><el-button size="small" type="primary" @click="showOption(0)" style="margin-left: 10px;" ><el-icon class="el-icon--left"><Switch /></el-icon>切换</el-button></div>
        <div><el-button v-if="isForbidInput == false" size="small" type="primary" @click="showOption(1)" style="margin-left: 10px;" ><el-icon class="el-icon--left"><Finished /></el-icon>保存</el-button></div>
        <div><el-button v-if="isForbidInput == true" size="small" type="primary" @click="newOp" style="margin-left: 10px;" ><el-icon class="el-icon--left"><Edit /></el-icon>新建</el-button></div>
      </div>
      <div class="item-class" style="margin-bottom: 15px;">
        <div class="no-select">主机ip：</div>
        <div>
          <el-input :disabled="isForbidInput" v-model="setInfo.server_ip" class="w-50 m-2" placeholder="输入主机ip">
            <template #prefix>
              <el-icon><HomeFilled /></el-icon>
            </template>
          </el-input>
        </div>
        <div style="cursor: pointer; margin-left: 10px;" @click="doCopy(setInfo.server_ip)"><el-icon size="15"><DocumentCopy /></el-icon></div>
      </div>
      <div class="item-class" style="margin-bottom: 15px;">
        <div class="no-select">端口号：</div>
        <div>
          <el-input :disabled="isForbidInput" v-model="setInfo.server_port" class="w-50 m-2" placeholder="输入端口号">
            <template #prefix>
              <el-icon><Paperclip /></el-icon>
            </template>
          </el-input>
        </div>
        <div style="cursor: pointer; margin-left: 10px;" @click="doCopy(setInfo.server_port)"><el-icon size="15"><DocumentCopy /></el-icon></div>
      </div>
      <div class="item-class" style="margin-bottom: 15px;">
        <div class="no-select">用户名：</div>
        <div>
          <el-input :disabled="isForbidInput" v-model="setInfo.server_user" class="w-50 m-2" placeholder="输入用户名">
            <template #prefix>
              <el-icon class="el-input__icon"><User /></el-icon>
            </template>
          </el-input>
        </div>
        <div style="cursor: pointer; margin-left: 10px;" @click="doCopy(setInfo.server_user)"><el-icon size="15"><DocumentCopy /></el-icon></div>
      </div>
      <div class="item-class" style="margin-bottom: 5px;">
        <div class="no-select">密 &nbsp; 码：</div>
        <div>
          <el-input :disabled="isForbidInput" v-model="setInfo.server_password" :type="isShowPassword ? 'text': 'password'" class="w-50 m-2" placeholder="输入密码">
            <template #prefix>
              <el-icon class="el-input__icon"><Lock /></el-icon>
            </template>
          </el-input>
        </div>
        <div v-if="isShowPassword == true" style="cursor: pointer; margin-left: 10px;" @click="isShowPassword = false"><el-icon size="15"><View /></el-icon></div>
        <div v-else style="cursor: pointer; margin-left: 10px;" @click="isShowPassword = true"><el-icon size="15"><Hide /></el-icon></div>
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

  <!-- 配置管理 -->
  <OptionBlock ref="optionBlockRef" @callback="doOption" :opType="optionBlockType" :sshOptions="sshOptions" ></OptionBlock>
</template>

<script>
import { ref } from 'vue';
import useClipboard from "vue-clipboard3";
import { ElMessage } from 'element-plus';
import OptionBlock from './OptionBlock';
import { HomeFilled, Paperclip, User, Lock, DocumentCopy, View, Hide, Edit, Finished, Switch } from '@element-plus/icons-vue';

export default {
  name:'ConnectSetting',
  components: {
    OptionBlock,
    HomeFilled,
    Paperclip,
    User,
    Lock,
    DocumentCopy,
    View,
    Hide,
    Edit,
    Finished,
    Switch,
  },
  props:['env','sshOptions'],
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
      option: props.env.option,
    });

    // 校验参数
    const verifyParams = () => {
      err_msg.value = '';
      // 验证IP地址
      const ipRegex = /^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$/;
      if (!ipRegex.test(setInfo.value.server_ip)) {
        err_msg.value = "主机ip地址无效";
        return false;
      }
      // 校验端口号
      if (isNaN(setInfo.value.server_port) || setInfo.value.server_port < 0 || setInfo.value.server_port > 65535) {
        err_msg.value = "端口号无效";
        return false;
      }
      // 校验用户名
      if (!(setInfo.value.server_user && setInfo.value.server_user != '')) {
        err_msg.value = "用户名不能为空";
        return false;
      }
      // 校验密码
      if (!(setInfo.value.server_password && setInfo.value.server_password != '')) {
        err_msg.value = "密码不能为空";
        return false;
      }

      return true;
    };

    // 配置选项
    const optionBlockRef = ref();
    const optionBlockType = ref(0);

    const showOption = (type) => {
      if(type == 1) {
        if(verifyParams() == false) return;
      }
      optionBlockType.value = type;
      optionBlockRef.value.aimOption = '';
      optionBlockRef.value.DialogVisilble = true;
    };

    const isForbidInput = ref(false);
    if(setInfo.value.option != '') isForbidInput.value = true;
    const doOption = (option) => {
      // 切换
      if(optionBlockType.value == 0)
      {
        setInfo.value = {...setInfo.value, ...props.sshOptions[option]};
        isForbidInput.value = true;
      }
      // 保存
      else if(optionBlockType.value == 1)
      {
        setInfo.value.option = option;
        context.emit('saveOp', option, setInfo.value);
        isForbidInput.value = true;
      }
    };

    const newOp = () => {
      isForbidInput.value = false;
      setInfo.value.option = '';
    };

    const confirm = () => {
      if(verifyParams() == false) return;
      context.emit('callback',setInfo.value);
      closeDialog();
    };

    // 拷贝
    const { toClipboard } = useClipboard();

    // 复制
    const doCopy = async (content) => {
      content += '';
      if(!(content && content.length > 0)) {
        ElMessage({
          message: '内容为空',
          type: 'warning',
          grouping: true,
          repeatNum: Number.MIN_SAFE_INTEGER,
        });
        return;
      }
      await toClipboard(content);
      ElMessage({
        message: '复制成功',
        type: 'success',
        grouping: true,
        repeatNum: Number.MIN_SAFE_INTEGER,
      });
    };

    const isShowPassword = ref(false);

    // 重置
    const reset = () => {
      err_msg.value = '';
      isShowPassword.value = false;
      setInfo.value = {
        server_ip: props.env.server_ip,
        server_port: props.env.server_port,
        server_user: props.env.server_user,
        server_password:props.env.server_password,
        option: props.env.option,
      };
      if(setInfo.value.option != '') isForbidInput.value = true;
      else isForbidInput.value = false;
      DialogVisilble.value = false;
    };

    // 关闭
    const closeDialog = (done) => {
      if(optionBlockRef.value && optionBlockRef.value.DialogVisilble) optionBlockRef.value.closeDialog();
      setTimeout(() => {
        reset();
      },400);
      DialogVisilble.value = false;
      if(done) done();
    };

    return {
      setInfo,
      DialogVisilble,
      err_msg,
      confirm,
      optionBlockRef,
      showOption,
      optionBlockType,
      doOption,
      isForbidInput,
      newOp,
      reset,
      closeDialog,
      doCopy,
      isShowPassword,
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

/* 文本溢出省略 */
.ellipsis {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.nowrap {
  white-space: nowrap;
}

.old-option {
  color: #67c23a;
}

.new-option {
  color: #f56c6c;
}
</style>