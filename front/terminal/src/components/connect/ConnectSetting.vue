<template>
  <el-dialog
    v-model="DialogVisible"
    :destory-on-close="true"
    :before-close="closeDialog"
    :width="360"
    :title="$t('连接设置')"
    :modal="false"
    :close-on-click-modal="false"
    modal-class="kk-dialog-class"
    header-class="kk-header-class"
    body-class="kk-body-class-6"
    align-center
    draggable
  >
    <div>
      <div class="item-class" style="margin-bottom: 15px;" >
        <div class="no-select form-width nowrap" >{{ $t('配\u00A0\u00A0\u00A0置') }}：</div>
        <ToolTip :content="!(setInfo.option && setInfo.option.length > 0) ? $t('新建配置') : setInfo.option" >
          <template #content>
            <div class="ellipsis" style="user-select: none;" :class="!(setInfo.option && setInfo.option.length > 0) ? 'new-option': 'old-option'" >{{ !(setInfo.option && setInfo.option.length > 0) ? $t('新建配置') : setInfo.option }}</div>
          </template>
        </ToolTip>
        <div style="flex: 1;" ></div>
        <div><el-button size="small" type="primary" @click="showOption(0)" style="margin-left: 10px;" ><el-icon class="el-icon--left" ><Switch /></el-icon>{{ $t('切换') }}</el-button></div>
        <div v-if="!isForbidInput" ><el-button size="small" type="primary" @click="showOption(1)" style="margin-left: 10px;" ><el-icon class="el-icon--left" ><Finished /></el-icon>{{ $t('保存') }}</el-button></div>
        <div v-else ><el-button size="small" type="primary" @click="newOp" style="margin-left: 10px;" ><el-icon class="el-icon--left" ><Edit /></el-icon>{{ $t('新建') }}</el-button></div>
      </div>
      <div class="item-class" style="margin-bottom: 15px;" >
        <div class="no-select form-width" >{{ $t('主机IP') }}：</div>
        <div>
          <el-input :disabled="isForbidInput" v-model="setInfo.server_ip" class="w-50 m-2" :placeholder="$t('输入主机IP')" >
            <template #prefix>
              <el-icon class="el-input__icon" ><HomeFilled /></el-icon>
            </template>
          </el-input>
        </div>
        <div class="operator-class" @click="doCopy(setInfo.server_ip)" ><el-icon size="15" ><DocumentCopy /></el-icon></div>
      </div>
      <div class="item-class" style="margin-bottom: 15px;" >
        <div class="no-select form-width" >{{ $t('端口号') }}：</div>
        <div>
          <el-input :disabled="isForbidInput" v-model="setInfo.server_port" class="w-50 m-2" :placeholder="$t('输入端口号')" >
            <template #prefix>
              <el-icon class="el-input__icon" ><Paperclip /></el-icon>
            </template>
          </el-input>
        </div>
        <div class="operator-class" @click="doCopy(setInfo.server_port)" ><el-icon size="15" ><DocumentCopy /></el-icon></div>
      </div>
      <div class="item-class" style="margin-bottom: 15px;" >
        <div class="no-select form-width" >{{ $t('用户名') }}：</div>
        <div>
          <el-input :disabled="isForbidInput" v-model="setInfo.server_user" class="w-50 m-2" :placeholder="$t('输入用户名')" >
            <template #prefix>
              <el-icon class="el-input__icon" ><User /></el-icon>
            </template>
          </el-input>
        </div>
        <div class="operator-class" @click="doCopy(setInfo.server_user)" ><el-icon size="15" ><DocumentCopy /></el-icon></div>
      </div>
      <div class="item-class" style="margin-bottom: 5px;" >
        <el-dropdown :disabled="isForbidInput" class="no-select form-width" hide-timeout="300" >
          <span>{{ setInfo.authType == 0 ? $t('密码') : $t('私钥') }}<el-icon class="el-icon--right" ><arrow-down style="cursor: pointer;" /></el-icon></span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item class="no-select" @click="switchAuthType(0)" >{{ $t('密码') }}</el-dropdown-item>
              <el-dropdown-item class="no-select" @click="switchAuthType(1)" >{{ $t('私钥') }}</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
        <div v-if="setInfo.authType == 0" class="item-class" >
          <div>
            <el-input :disabled="isForbidInput" v-model="setInfo.server_password" :type="isShowPassword ? 'text': 'password'" class="w-50 m-2" :placeholder="$t('输入密码')" >
              <template #prefix>
                <el-icon class="el-input__icon" ><Lock /></el-icon>
              </template>
            </el-input>
          </div>
          <div v-if="isShowPassword" class="operator-class" @click="isShowPassword = false" ><el-icon size="15" ><View /></el-icon></div>
          <div v-else class="operator-class" @click="isShowPassword = true" ><el-icon size="15" ><Hide /></el-icon></div>
        </div>
        <div v-else >
          <el-button :disabled="isForbidInput" type="primary" @click="openPrivateKeyBlock" >
            <el-icon class="el-icon--left" ><Key /></el-icon>{{ $t('导入') }}
          </el-button>
        </div>
      </div>
    </div>
    <div class="error-text no-select" > {{ $t(err_msg) }} </div>
    <div style="margin-bottom: 5px;" ></div>
    <div style="display: flex; border-top: 1px solid #f1f2f4;" >
      <el-checkbox v-if="setInfo.option && setInfo.option.length > 0" v-model="isNewWindow" :label="$t('新窗口打开')" size="small" style="margin-top: 10px;" />
      <div style="flex: 1;" ></div>
      <el-button size="small" type="primary" @click="confirm" style="margin-top: 10px;" >
        {{ $t('确定') }}
      </el-button>
    </div>
  </el-dialog>

  <!-- 配置管理 -->
  <OptionBlock ref="optionBlockRef" @callback="doOption" :opType="optionBlockType" :sshOptions="sshOptions" @handleDeleteOption="handleDeleteOption" ></OptionBlock>
  <!-- 私钥管理 -->
  <PrivateKey ref="privateKeyRef" @callback="savePrivateKey" ></PrivateKey>
</template>

<script>
import { ref } from 'vue';
import useClipboard from "vue-clipboard3";
import { ElMessage } from 'element-plus';
import OptionBlock from './OptionBlock';
import PrivateKey from './PrivateKey';
import ToolTip from '@/components/common/ToolTip';
import { getPureUrl } from '@/utils/UrlUtil';
import { HomeFilled, Paperclip, User, Lock, DocumentCopy, View, Hide, Edit, Finished, Switch, ArrowDown, Key } from '@element-plus/icons-vue';
import i18n from "@/locales/i18n";
import { isIP, isFQDN } from '@/utils/IPUtil';

export default {
  name:'ConnectSetting',
  components: {
    ToolTip,
    OptionBlock,
    PrivateKey,
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
    ArrowDown,
    Key,
  },
  props:['env','sshOptions'],
  setup(props,context) {

    // 控制Dialog显示
    const DialogVisible = ref(false);
    const err_msg = ref('');
    const isNewWindow = ref(false);

    // 设置信息
    const setInfo = ref({
      server_ip: props.env.server_ip,
      server_port: props.env.server_port,
      server_user: props.env.server_user,
      server_password:props.env.server_password,
      server_key: props.env.server_key || null,
      authType: props.env.authType || 0,
      option: props.env.option,
    });

    // 校验参数
    const verifyParams = () => {
      err_msg.value = '';
      // 验证IP地址/域名
      if (!(isIP(setInfo.value.server_ip) || isFQDN(setInfo.value.server_ip))) {
        err_msg.value = i18n.global.k("主机IP地址无效");
        return false;
      }
      // 校验端口号
      if (isNaN(setInfo.value.server_port) || setInfo.value.server_port < 0 || setInfo.value.server_port > 65535) {
        err_msg.value = i18n.global.k("端口号无效");
        return false;
      }
      // 校验用户名
      if (!setInfo.value.server_user) {
        err_msg.value = i18n.global.k("用户名不能为空");
        return false;
      }
      // 校验密码
      if (setInfo.value.authType == 0 && !setInfo.value.server_password) {
        err_msg.value = i18n.global.k("密码不能为空");
        return false;
      }
      // 校验私钥
      if(setInfo.value.authType == 1 && !(setInfo.value.server_key && setInfo.value.server_key.content)) {
        err_msg.value = i18n.global.k("私钥不能为空");
        return false;
      }

      return true;
    };

    // 配置选项
    const optionBlockRef = ref();
    const optionBlockType = ref(0);

    const showOption = (type) => {
      if(type == 1 && !verifyParams()) return;
      optionBlockType.value = type;
      optionBlockRef.value.aimOption = '';
      optionBlockRef.value.DialogVisible = true;
    };

    const isForbidInput = ref(false);
    if(setInfo.value.option) isForbidInput.value = true;
    const doOption = (option) => {
      // 切换
      if(optionBlockType.value == 0)
      {
        if(!props.sshOptions[option].authType) setInfo.value.authType = 0;
        setInfo.value = {...setInfo.value, ...props.sshOptions[option]};
        isForbidInput.value = true;
        err_msg.value = '';
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

    // 删除配置
    const handleDeleteOption = (name) => {
      context.emit('deleteOp', name);
      if(setInfo.value.option && setInfo.value.option == name) {
        setInfo.value.option = '';
        isForbidInput.value = false;
      }
    };

    // 私钥相关
    const privateKeyRef = ref();
    const openPrivateKeyBlock = () => {
      privateKeyRef.value.content = setInfo.value.server_key ? setInfo.value.server_key.content : '';
      privateKeyRef.value.passphrase = setInfo.value.server_key ? setInfo.value.server_key.passphrase : '';
      privateKeyRef.value.DialogVisible = true;
    };
    const savePrivateKey = (content,passphrase) => {
      setInfo.value.server_key = {
        content:content,
        passphrase:passphrase,
      };
    };
    const switchAuthType = (type) => {
      setInfo.value.authType = type;
      // 选择密码登录
      if(type == 0) privateKeyRef.value.closeDialog();
    };

    const confirm = () => {
      if(!verifyParams()) return;
      if(isNewWindow.value && (setInfo.value.option && setInfo.value.option.length > 0)) {
        window.open(getPureUrl() + '?option=' + setInfo.value.option, '_blank');
        return;
      }
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
          message: i18n.global.t('内容为空'),
          type: 'warning',
          grouping: true,
          repeatNum: Number.MIN_SAFE_INTEGER,
        });
        return;
      }
      await toClipboard(content);
      ElMessage({
        message: i18n.global.t('复制成功'),
        type: 'success',
        grouping: true,
        repeatNum: Number.MIN_SAFE_INTEGER,
      });
    };

    const isShowPassword = ref(false);

    // 重置
    const reset = () => {
      err_msg.value = '';
      isNewWindow.value = false;
      isShowPassword.value = false;
      setInfo.value = {
        server_ip: props.env.server_ip,
        server_port: props.env.server_port,
        server_user: props.env.server_user,
        server_password:props.env.server_password,
        server_key: props.env.server_key || null,
        authType: props.env.authType || 0,
        option: props.env.option,
      };
      if(setInfo.value.option) isForbidInput.value = true;
      else isForbidInput.value = false;
      DialogVisible.value = false;
    };

    // 关闭
    const closeDialog = (done) => {
      if(optionBlockRef.value && optionBlockRef.value.DialogVisible) optionBlockRef.value.closeDialog();
      if(privateKeyRef.value && privateKeyRef.value.DialogVisible) privateKeyRef.value.closeDialog();
      setTimeout(() => {
        reset();
      }, 400);
      DialogVisible.value = false;
      if(done) done();
    };

    return {
      setInfo,
      DialogVisible,
      err_msg,
      isNewWindow,
      confirm,
      optionBlockRef,
      showOption,
      optionBlockType,
      doOption,
      isForbidInput,
      newOp,
      handleDeleteOption,
      reset,
      closeDialog,
      doCopy,
      isShowPassword,
      privateKeyRef,
      openPrivateKeyBlock,
      savePrivateKey,
      switchAuthType,
    }
  }


}
</script>

<style scoped>
.item-class {
  display: flex;
  align-items: center;
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

.form-width {
  width: 56px;
}

.operator-class {
  margin-left: 10px;
  cursor: pointer;
}
</style>
