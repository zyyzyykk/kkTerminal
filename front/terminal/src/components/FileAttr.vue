<template>
  <el-dialog
    v-model="DialogVisible"
    :before-close="closeDialog"
    :width="400"
    :modal="false"
    modal-class="kk-dialog-class"
    body-class="kk-body-class-12"
    align-center
    draggable
  >
    <template #title>
      <div class="kk-flex-0 nowrap kk-header-class">
        <FileIcons :style="{display: 'flex', alignItems: 'center'}" :name="fileInfo.name" :width="16" :height="16" :isFolder="fileInfo.isDirectory" :isLink="fileInfo.isSymlink" />
        <div class="ellipsis" style="margin: 0 5px; font-size: small;">{{ fileInfo.name }}</div>
        <div style="font-size: small;">{{ $t('属性') }}</div>
      </div>
    </template>
    <div style="margin-top: -32px;"></div>
    <div class="no-select" element-loading-text="Loading..." v-loading="loading" >
      <div class="kk-flex">
        <div style="margin-right: 10px;" ><FileIcons :style="{display: 'flex', alignItems: 'center'}" :name="fileInfo.name" :width="32" :height="32" :isFolder="fileInfo.isDirectory" :isLink="fileInfo.isSymlink" /></div>
        <div>
          <el-input @keydown.enter="confirm" v-model="rename" placeholder="" />
        </div>
      </div>
      <div class="kk-border" ></div>
      <div class="kk-flex nowrap">
        <div class="form-width" >{{ $t('位置') }}：</div>
        <ToolTip :content="fileDir + fileInfo.name" >
          <template #content>
            <div class="ellipsis">
              {{ fileDir + fileInfo.name }}
            </div>
          </template>
        </ToolTip>
        <div style="cursor: pointer; margin-left: 7px;" @click="doCopy(fileDir + fileInfo.name)">
          <el-icon size="18"><DocumentCopy /></el-icon>
        </div>
      </div>
      <div v-if="fileInfo.isDirectory" class="kk-flex">
        <div class="form-width" >{{ $t('包含') }}：</div>
        <div class="ellipsis" >
          {{ includeInfo }}
        </div>
        <div v-if="fileInfo.isSymlink || unreliable" style="margin-left: 10px;" >
          <el-tag size="small" type="danger">unsure</el-tag>
        </div>
        <div style="cursor: pointer; margin-left: 7px;" @click="getFolderInclude" >
          <el-icon size="18"><Refresh /></el-icon>
        </div>
      </div>
      <div v-else class="kk-flex">
        <div class="form-width" >{{ $t('大小') }}：</div>
        <div class="ellipsis" >
          {{ calcSize(fileInfo.attributes.size) }} ({{ fileInfo.attributes.size + ' ' + $t('字节') }})
        </div>
        <div v-if="fileInfo.isSymlink || unreliable" style="margin-left: 10px;" >
          <el-tag size="small" type="danger">unsure</el-tag>
        </div>
        <div style="cursor: pointer; margin-left: 7px;" @click="getFileSize" >
          <el-icon size="18"><Refresh /></el-icon>
        </div>
      </div>
      <div class="kk-border" ></div>
      <div class="kk-flex">
        <div class="form-width" >{{ $t('修改时间') }}：</div>
        <div>
          {{ calcDate(fileInfo.attributes.mtime) }}
        </div>
      </div>
      <div class="kk-flex">
        <div class="form-width" >{{ $t('访问时间') }}：</div>
        <div>
          {{ calcDate(fileInfo.attributes.atime) }}
        </div>
      </div>
      <div class="kk-border" ></div>
      <div class="kk-flex">
        <div class="form-width" >{{ $t('权限') }}：</div>
        <div>
          {{ calcPriority(fileInfo.attributes.mode.type,fileInfo.attributes.permissions) }}
        </div>
        <div style="cursor: pointer; margin-left: 7px;" @click="openEditPermissions">
          <el-icon size="18"><Edit /></el-icon>
        </div>
        <div style="flex: 1;"></div>
        <div>
          <el-button size="small" type="primary" @click="confirm" >{{ $t('确定') }}</el-button>
        </div>
      </div>
    </div>
<!--    <div style="margin-top: -15px;"></div>-->
  </el-dialog>

  <!-- 权限修改 -->
  <PermissionsEdit ref="permissionsEditRef" @editPermissions="editPermissions" ></PermissionsEdit>

</template>

<script>
import { ref } from 'vue';
import $ from 'jquery';
import { http_base_url } from '@/env/BaseUrl';
import { calcDate } from '@/components/calc/CalcDate';
import { calcPriority } from '@/components/calc/CalcPriority';
import { ElMessage } from 'element-plus';
import useClipboard from "vue-clipboard3";
import { DocumentCopy, Refresh, Edit } from '@element-plus/icons-vue';
import { calcSize } from '@/components/calc/CalcSize';
import { escapeItem, escapePath } from '@/utils/StringUtil';
import ToolTip from './ToolTip.vue';
import PermissionsEdit from './PermissionsEdit.vue';
import i18n from "@/locales/i18n";

// 引入文件图标组件
import FileIcons from 'file-icons-vue';

export default {
  name: 'FileAttr',
  components: {
    ToolTip,
    FileIcons,
    DocumentCopy,
    Refresh,
    Edit,
    PermissionsEdit,
  },
  props:['sshKey'],
  setup(props,context) {
    // 拷贝
    const { toClipboard } = useClipboard();

    // 控制Dialog显示
    const DialogVisible = ref(false);
    const fileInfo = ref({});
    const fileDir = ref('');
    const rename = ref('');

    // 加载
    const loading = ref(true);
    // 不可靠标识
    const unreliable = ref(false);
    // 包含信息
    const includeInfo = ref(i18n.global.t('0 个文件，0 个文件夹'));

    // 获取文件大小
    const getFileSize = () => {
      $.ajax({
        url: http_base_url + '/du',
        type:'get',
        data:{
          time:new Date().getTime(),
          sshKey:props.sshKey,
          path:escapePath(fileDir.value),
          item:escapeItem(fileInfo.value.name),
        },
        beforeSend: function() { // 发送请求前执行的方法
          loading.value = true;
        },
        success(resp){
          if(resp.status == 'success') fileInfo.value.attributes.size = parseInt(resp.data, 10);
          else unreliable.value = true;
        },
        complete: function() { // 发送请求完成后执行的方法
          loading.value = false;
        }
      });
    };

    // 获取文件夹包含信息
    const getFolderInclude = () => {
      $.ajax({
        url: http_base_url + '/find',
        type:'get',
        data:{
          time:new Date().getTime(),
          sshKey:props.sshKey,
          path:escapePath(fileDir.value),
          item:escapeItem(fileInfo.value.name),
        },
        beforeSend: function() { // 发送请求前执行的方法
          loading.value = true;
        },
        success(resp){
          if(resp.status == 'success') {
            includeInfo.value = resp.data[0] + i18n.global.t(' 个文件，') + (Math.max(0,parseInt(resp.data[1], 10) - 1)) + i18n.global.t(' 个文件夹');
          }
          else unreliable.value = true;
        },
        complete: function() { // 发送请求完成后执行的方法
          loading.value = false;
        }
      });
    };

    // 确定
    const confirm = () => {
      // 校验
      if(fileInfo.value.name == rename.value) {
        closeDialog();
        return;
      }
      if(!(rename.value && rename.value.trim().length > 0)) {
        ElMessage({
          message: i18n.global.t("文件名不能为空"),
          type: "warning",
          grouping: true,
        })
        return;
      }
      const invalidNameRe = /[/|]/;
      if(invalidNameRe.test(rename.value)) {
        ElMessage({
          message: i18n.global.t("文件名不能含有") + " |,/",
          type: "warning",
          grouping: true,
        })
        return;
      }
      let oldPath = fileDir.value + fileInfo.value.name;
      let newPath = fileDir.value + rename.value;
      context.emit('callback',oldPath,newPath);
      closeDialog();
    };

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

    const editPermissions = (path,item, permissionsInfo) => {
      context.emit('editPermissions', path, item, permissionsInfo);
    };

    // 重置
    const reset = () => {
      fileInfo.value = {
        attributes:{
          mode:{
            type:'',
          },
          permissions:'',
        },
      };
      rename.value = '';
      fileDir.value = '';
      loading.value = false;
      unreliable.value = false;
      includeInfo.value = i18n.global.t('0 个文件，0 个文件夹');
      DialogVisible.value = false;
    };

    // 权限修改
    const permissionsEditRef = ref();
    const openEditPermissions = () => {
      permissionsEditRef.value.fileDir = fileDir.value;
      permissionsEditRef.value.fileInfo = fileInfo.value;
      permissionsEditRef.value.DialogVisible = true;
      permissionsEditRef.value.init();
    };

    // 关闭
    const closeDialog = (done) => {
      if(permissionsEditRef.value) permissionsEditRef.value.closeDialog();
      setTimeout(() => {
        reset();
      },400);
      DialogVisible.value = false;
      if(done) done();
    };

    return {
      DialogVisible,
      fileInfo,
      confirm,
      closeDialog,
      reset,
      fileDir,
      rename,
      calcDate,
      calcPriority,
      calcSize,
      doCopy,
      loading,
      unreliable,
      includeInfo,
      getFileSize,
      getFolderInclude,
      permissionsEditRef,
      openEditPermissions,
      editPermissions,
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

.kk-border {
  padding-bottom: 10px;
  border-bottom: 1px solid #ececec;
}

.no-select {
  user-select: none;
}

/* 文本溢出省略 */
.ellipsis {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  line-height: 18px;
}

.nowrap {
  white-space: nowrap;
}

.form-width {
  text-align: left;
  width: 100px;
}
</style>
