<template>
  <el-dialog
    v-model="DialogVisible"
    :before-close="closeDialog"
    :width=" $t('360')"
    :modal="false"
    modal-class="kk-dialog-class"
    body-class="kk-body-class-12"
    align-center
    draggable
  >
    <template #title>
      <div class="kk-flex-0 nowrap kk-header-class" >
        <FileIcons :style="{display: 'flex', alignItems: 'center'}" :name="fileInfo.name" :width="16" :height="16" :isFolder="fileInfo.isDirectory" :isLink="fileInfo.isSymlink" />
        <div class="ellipsis" style="margin: 0 5px; font-size: small; line-height: 18px;" >{{ fileInfo.name }}</div>
        <div style="font-size: small;" >{{ $t('权限修改') }}</div>
      </div>
    </template>
    <div style="margin-top: -25px;" ></div>
    <div class="kk-flex" >
      <div class="form-width no-select" >{{ $t('所有者') }}{{ $t('：') }}</div>
      <el-checkbox v-model="permissionsInfo.owner[0]" :label="$t('读取')" size="large" />
      <el-checkbox v-model="permissionsInfo.owner[1]" :label="$t('写入')" size="large" />
      <el-checkbox v-model="permissionsInfo.owner[2]" :label="$t('执行')" size="large" />
    </div>
    <div class="kk-flex" >
      <div class="form-width no-select" >{{ $t('所属组') }}{{ $t('：') }}</div>
      <el-checkbox v-model="permissionsInfo.group[0]" :label="$t('读取')" size="large" />
      <el-checkbox v-model="permissionsInfo.group[1]" :label="$t('写入')" size="large" />
      <el-checkbox v-model="permissionsInfo.group[2]" :label="$t('执行')" size="large" />
    </div>
    <div class="kk-flex" >
      <div class="form-width no-select" >{{ $t('其他用户') }}{{ $t('：') }}</div>
      <el-checkbox v-model="permissionsInfo.others[0]" :label="$t('读取')" size="large" />
      <el-checkbox v-model="permissionsInfo.others[1]" :label="$t('写入')" size="large" />
      <el-checkbox v-model="permissionsInfo.others[2]" :label="$t('执行')" size="large" />
    </div>
    <div style="margin-bottom: 5px;" ></div>
    <div style="display: flex; align-items: center; border-top: 1px solid #f1f2f4;" >
      <el-checkbox v-if="fileInfo.isDirectory" v-model="permissionsInfo.sub" :label="$t('应用到子目录和文件')" size="small" style="margin-top: 10px;" />
      <div style="flex: 1;" ></div>
      <el-button size="small" type="primary" @click="confirm" style="margin-top: 10px;" >
        {{ $t('确定') }}
      </el-button>
    </div>
  </el-dialog>
</template>

<script>
import { ref } from 'vue';
import { getPermissionInfo } from '@/components/calc/CalcPriority';

// 引入文件图标组件
import FileIcons from 'file-icons-vue';

export default {
  name: 'PermissionsEdit',
  components: {
    FileIcons,
  },
  setup(props, context) {

    // 控制Dialog显示
    const DialogVisible = ref(false);

    // 文件信息
    const fileDir = ref('');
    const fileInfo = ref({
      attributes: {
        mode: {
          type: '',
        },
        permissions: '',
      },
    });

    const permissionsInfo = ref({
      owner: [false,false,false],
      group: [false,false,false],
      others: [false,false,false],
      sub: false,
    });

    // 初始化，获取文件权限信息
    const init = () => {
      permissionsInfo.value = getPermissionInfo(fileInfo.value.attributes.permissions);
    };

    // 修改权限
    const confirm = () => {
      context.emit('editPermissions', fileDir.value, fileInfo.value.name, permissionsInfo.value);
    };

    // 重置
    const reset = () => {
      fileDir.value = '';
      fileInfo.value = {
        attributes: {
          mode: {
            type: '',
          },
          permissions: '',
        },
      };
      permissionsInfo.value = {
        owner: [false,false,false],
        group: [false,false,false],
        others: [false,false,false],
        sub: false,
      };
      DialogVisible.value = false;
    };

    // 关闭
    const closeDialog = (done) => {
      setTimeout(() => {
        reset();
      }, 400);
      DialogVisible.value = false;
      if(done) done();
    };

    return {
      fileDir,
      fileInfo,
      DialogVisible,
      closeDialog,
      init,
      permissionsInfo,
      getPermissionInfo,
      confirm,
    }
  }
}
</script>

<style scoped>
.kk-flex-0 {
  display: flex;
  align-items: center;
}

.kk-flex {
  display: flex;
  align-items: center;
  margin-top: 15px;
}

.nowrap {
  white-space: nowrap;
}

.form-width {
  width: 72px;
}
</style>
