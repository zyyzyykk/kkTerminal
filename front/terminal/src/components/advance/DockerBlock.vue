<template>
  <el-dialog
      v-model="computedDialogVisible"
      destroy-on-close
      :width="750"
      title="Docker"
      :modal="false"
      modal-class="kk-dialog-class"
      header-class="kk-header-class"
      body-class="kk-body-class-6"
      :before-close="closeDialog"
      draggable
  >
    <div class="no-select" >
      <el-tabs v-if="noDocker === false" element-loading-text="Loading..." v-loading="loading" @tab-click="handleTabClick" type="border-card" >
        <el-tab-pane :label="$t('容器')">
          <el-table style="height: 200px; width: 100%" v-if="dockerInfo.container.length > 0" :data="dockerInfo.container" @selection-change="tableSelect" border stripe >
            <el-table-column show-overflow-tooltip type="selection" :selectable="selectable" width="40" />
            <el-table-column show-overflow-tooltip prop="id" label="ID" width="130" />
            <el-table-column show-overflow-tooltip prop="name" :label="$t('名称')" width="120" />
            <el-table-column show-overflow-tooltip prop="status" :label="$t('状态')" width="80" />
            <el-table-column show-overflow-tooltip prop="image" :label="$t('镜像')" width="140" />
            <el-table-column show-overflow-tooltip prop="port" :label="$t('端口')" />
          </el-table>
          <div v-else >
            <NoData height="240px" msg="No Data"></NoData>
          </div>
          <div class="kk-flex" v-if="dockerInfo.container.length > 0" style="height: 35px; margin-top: 5px;" >
            <el-dropdown hide-timeout="300" >
              <span class="a-link no-select" >{{ $t(containerTypeArr[containerType % containerTypeArr.length]) }}<el-icon class="el-icon--right"><arrow-down /></el-icon></span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item class="no-select" @click="containerType = 1" >{{ $t(containerTypeArr[1]) }}</el-dropdown-item>
                  <el-dropdown-item class="no-select" @click="containerType = 2" >{{ $t(containerTypeArr[2]) }}</el-dropdown-item>
                  <el-dropdown-item class="no-select" @click="containerType = 3" >{{ $t(containerTypeArr[3]) }}</el-dropdown-item>
                  <el-dropdown-item class="no-select" @click="containerType = 4" >{{ $t(containerTypeArr[4]) }}</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
            <el-button :disabled="!(selectedItems.length > 0 && containerType != 0)" size="small" type="primary" @click="operateConfirm" style="margin-left: 10px;" >
              {{ $t('确定') }}
            </el-button>
          </div>
        </el-tab-pane>
        <el-tab-pane :label="$t('镜像')">
          <el-table style="height: 200px; width: 100%" v-if="dockerInfo.image.length > 0" :data="dockerInfo.image" @selection-change="tableSelect" border stripe >
            <el-table-column type="selection" :selectable="selectable" width="40" />
            <el-table-column show-overflow-tooltip prop="id" label="ID" width="130" />
            <el-table-column show-overflow-tooltip prop="repository" :label="$t('仓库')" />
            <el-table-column show-overflow-tooltip prop="size" :label="$t('大小')" width="100" />
            <el-table-column show-overflow-tooltip prop="createTime" :label="$t('创建时间')" width="140" sortable />
          </el-table>
          <div v-else >
            <NoData height="240px" msg="No Data"></NoData>
          </div>
          <div class="kk-flex" v-if="dockerInfo.image.length > 0" style="height: 35px; margin-top: 5px;" >
            <el-dropdown hide-timeout="300" >
              <span class="a-link no-select" >{{ $t(deleteTypeArr[deleteType % deleteTypeArr.length]) }}<el-icon class="el-icon--right"><arrow-down /></el-icon></span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item class="no-select" @click="deleteType = 1" >{{ $t(deleteTypeArr[1]) }}</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
            <el-button :disabled="!(selectedItems.length > 0 && deleteType != 0)" size="small" type="primary" @click="deleteConfirm" style="margin-left: 10px;" >
              {{ $t('确定') }}
            </el-button>
          </div>
        </el-tab-pane>
        <el-tab-pane :label="$t('网络')">
          <el-table style="height: 200px; width: 100%" v-if="dockerInfo.network.length > 0" :data="dockerInfo.network" @selection-change="tableSelect" border stripe >
            <el-table-column type="selection" :selectable="selectable" width="40" />
            <el-table-column show-overflow-tooltip prop="name" :label="$t('名称')" width="140" />
            <el-table-column show-overflow-tooltip prop="ip" label="IP" />
            <el-table-column show-overflow-tooltip prop="gateway" :label="$t('网关')" width="120" />
            <el-table-column show-overflow-tooltip prop="createTime" :label="$t('创建时间')" width="140" sortable />
          </el-table>
          <div v-else >
            <NoData height="240px" msg="No Data"></NoData>
          </div>
          <div class="kk-flex" v-if="dockerInfo.network.length > 0" style="height: 35px; margin-top: 5px;" >
            <el-dropdown hide-timeout="300" >
              <span class="a-link no-select" >{{ $t(deleteTypeArr[deleteType % deleteTypeArr.length]) }}<el-icon class="el-icon--right"><arrow-down /></el-icon></span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item class="no-select" @click="deleteType = 2" >{{ $t(deleteTypeArr[2]) }}</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
            <el-button :disabled="!(selectedItems.length > 0 && deleteType != 0)" size="small" type="primary" @click="deleteConfirm" style="margin-left: 10px;" >
              {{ $t('确定') }}
            </el-button>
          </div>
        </el-tab-pane>
        <el-tab-pane :label="$t('数据卷')">
          <el-table style="height: 200px; width: 100%" v-if="dockerInfo.volume.length > 0" :data="dockerInfo.volume" @selection-change="tableSelect" border stripe >
            <el-table-column type="selection" :selectable="selectable" width="40" />
            <el-table-column show-overflow-tooltip prop="name" :label="$t('名称')" width="140" />
            <el-table-column show-overflow-tooltip prop="mount" :label="$t('挂载点')" />
            <el-table-column show-overflow-tooltip prop="createTime" :label="$t('创建时间')" width="140" sortable />
          </el-table>
          <div v-else >
            <NoData height="240px" msg="No Data"></NoData>
          </div>
          <div class="kk-flex" v-if="dockerInfo.volume.length > 0" style="height: 35px; margin-top: 5px;" >
            <el-dropdown hide-timeout="300" >
              <span class="a-link no-select" >{{ $t(deleteTypeArr[deleteType % deleteTypeArr.length]) }}<el-icon class="el-icon--right"><arrow-down /></el-icon></span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item class="no-select" @click="deleteType = 3" >{{ $t(deleteTypeArr[3]) }}</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
            <el-button :disabled="!(selectedItems.length > 0 && deleteType != 0)" size="small" type="primary" @click="deleteConfirm" style="margin-left: 10px;" >
              {{ $t('确定') }}
            </el-button>
          </div>
        </el-tab-pane>
      </el-tabs>
      <div v-else element-loading-text="Loading..." v-loading="loading" >
        <div class="kk-flex-column" style="height: 311px" >
          <div style="flex: 1" ></div>
          <div><img style="height: 160px" src="@/assets/no_docker.png" ></div>
          <div class="kk-flex" style="margin-top: 25px;" >
            <div>{{ $t('Docker未安装?') }}</div>
            <div>
              <el-button size="small" type="primary" @click="installDocker" style="margin-left: 15px;">
                {{ $t('安装') }}
              </el-button>
            </div>
          </div>
          <div style="flex: 1" ></div>
        </div>
      </div>
    </div>
  </el-dialog>
</template>

<script>

import NoData from "@/components/NoData.vue";
import { computed, ref } from "vue";
import $ from "jquery";
import { http_base_url } from "@/env/BaseUrl";
import { ArrowDown } from '@element-plus/icons-vue';
import i18n from "@/locales/i18n";
import {ElMessage} from "element-plus";
import { deleteDialog } from "@/utils/DeleteDialog";

export default {
  name: 'DockerBlock',
  components: {
    NoData,
    ArrowDown,
  },
  props:['sshKey', 'advance'],
  setup(props, context) {

    // 控制Dialog显示
    const DialogVisible = ref(false);
    const computedDialogVisible = computed(() => {
      return DialogVisible.value && props.advance;
    });
    const loading = ref(false);

    // 表格选择
    const selectedItems = ref([]);
    const tableSelect = (itemsArr) => {
      selectedItems.value = itemsArr;
    };

    // Docker信息
    const noDocker = ref(true);
    const dockerInfo = ref({
      container: [],
      image: [],
      network: [],
      volume: [],
    });
    const getDockerVersion = (sshKey) => {
      loading.value = true;
      $.ajax({
        url: http_base_url + '/docker/version',
        type:'post',
        data:{
          sshKey:sshKey,
        },
        success(resp) {
          if(resp.status == 'success') {
            noDocker.value = false;
            handleTabClick({index: 0});
          }
          else {
            noDocker.value = true;
            loading.value = false;
          }
        }
      });
    };
    const getDockerInfo = (type) => {
      const currentType = (type || 0) % 4;
      $.ajax({
        url: http_base_url + '/docker/info',
        type:'post',
        data:{
          sshKey:props.sshKey,
          type:currentType,
        },
        success(resp) {
          if(resp.status == 'success') {
            noDocker.value = false;
            let container = dockerInfo.value.container;
            let image = dockerInfo.value.image;
            let network = dockerInfo.value.network;
            let volume = dockerInfo.value.volume;
            // 容器
            if(currentType == 0) {
              const containerArr = (resp.data && resp.data != 'null') ? resp.data.split('$') : [];
              container = [];
              for(let i = 0; i < containerArr.length; i++) {
                const containerInfo = containerArr[i].split('@');
                container.push({
                  id: containerInfo[0],
                  name: containerInfo[1],
                  status: containerInfo[2],
                  image: containerInfo[3],
                  port: containerInfo[4],
                });
              }
            }
            // 镜像
            else if(currentType == 1) {
              const imageArr = (resp.data && resp.data != 'null') ? resp.data.split('$') : [];
              image = [];
              for(let i = 0; i < imageArr.length; i++) {
                const imageInfo = imageArr[i].split('@');
                image.push({
                  id: imageInfo[0],
                  repository: imageInfo[1],
                  size: imageInfo[2],
                  createTime: imageInfo[3].split(' ')[0].substring(0, 10),
                });
              }
            }
            // 网络
            else if(currentType == 2) {
              const networkArr = (resp.data && resp.data != 'null') ? resp.data.split('$') : [];
              network = [];
              for(let i = 0; i < networkArr.length; i++) {
                const networkInfo = networkArr[i].split('@');
                network.push({
                  name: networkInfo[0],
                  ip: networkInfo[1],
                  gateway: networkInfo[2],
                  createTime: networkInfo[3].split(' ')[0].substring(0, 10),
                });
              }
            }
            // 数据卷
            else if(currentType == 3) {
              const volumeArr = (resp.data && resp.data != 'null') ? resp.data.split('$') : [];
              volume = [];
              for(let i = 0; i < volumeArr.length; i++) {
                const volumeInfo = volumeArr[i].split('@');
                volume.push({
                  name: volumeInfo[0],
                  mount: volumeInfo[1],
                  createTime: volumeInfo[2].split(' ')[0].substring(0, 10),
                });
              }
            }
            dockerInfo.value = {
              container: container,
              image: image,
              network: network,
              volume: volume,
            };
          }
          loading.value = false;
        }
      });
    };
    const handleTabClick = (tab) => {
      loading.value = true;
      selectedItems.value = [];
      containerType.value = 0;
      deleteType.value = 0;
      getDockerInfo(tab.index);
    };

    // Docker 容器操作 删除
    const containerType = ref(0);
    const deleteType = ref(0);
    const containerTypeArr = [
      i18n.global.k('选择批量操作'),
      i18n.global.k('启动容器'),
      i18n.global.k('停止容器'),
      i18n.global.k('重启容器'),
      i18n.global.k('删除容器')
    ];
    const deleteTypeArr = [
      i18n.global.k('选择批量操作'),
      i18n.global.k('删除镜像'),
      i18n.global.k('删除网络'),
      i18n.global.k('删除数据卷')
    ];
    const containerOperate = () => {
      loading.value = true;
      const type = containerType.value - 1;
      $.ajax({
        url: http_base_url + '/docker/container',
        type:'post',
        data:{
          sshKey:props.sshKey,
          type: type,
          items: selectedItems.value.map(item => {return (item.id || item.name)}).join(' '),
        },
        success(resp) {
          ElMessage({
            message: resp.info,
            type: resp.status,
            grouping: true,
          });
          handleTabClick({index: 0});
          selectedItems.value = [];
          loading.value = false;
        }
      });
    };
    const dockerDelete = () => {
      loading.value = true;
      const type = deleteType.value;
      $.ajax({
        url: http_base_url + '/docker/delete',
        type:'post',
        data:{
          sshKey:props.sshKey,
          type: type,
          items: selectedItems.value.map(item => {return (item.id || item.name)}).join(' '),
        },
        success(resp) {
          ElMessage({
            message: resp.info,
            type: resp.status,
            grouping: true,
          });
          handleTabClick({index: type});
          selectedItems.value = [];
          loading.value = false;
        }
      });
    };
    const operateConfirm = () => {
      deleteDialog(i18n.global.t('提示'), i18n.global.t('确定进行此操作吗?'), containerOperate);
    };
    const deleteConfirm = () => {
      deleteDialog(i18n.global.t('提示'), i18n.global.t('确定进行此操作吗?'), dockerDelete);
    };

    // 安装Docker
    const installDocker = () => {
      context.emit('install', "curl -fsSL https://get.docker.com -o get-docker.sh && sudo sh get-docker.sh\n");
      deepCloseDialog();
    };

    // 重置
    const reset = (deep=false) => {
      if(deep) {
        noDocker.value = true;
        dockerInfo.value = {
          container: [],
          image: [],
          network: [],
          volume: [],
        };
      }
      containerType.value = 0;
      deleteType.value = 0;
      selectedItems.value = [];
      loading.value = false;
      DialogVisible.value = false;
    };

    // 关闭
    const closeDialog = (done) => {
      setTimeout(() => {
        reset();
      },400);
      DialogVisible.value = false;
      if(done) done();
    };
    // 深度关闭
    const deepCloseDialog = (done) => {
      setTimeout(() => {
        reset(true);
      },400);
      DialogVisible.value = false;
      if(done) done();
    };

    return {
      computedDialogVisible,
      DialogVisible,
      reset,
      closeDialog,
      deepCloseDialog,
      noDocker,
      getDockerVersion,
      dockerInfo,
      getDockerInfo,
      handleTabClick,
      loading,
      installDocker,
      tableSelect,
      containerType,
      deleteType,
      selectedItems,
      containerTypeArr,
      deleteTypeArr,
      operateConfirm,
      deleteConfirm,
    }
  }
}
</script>

<style>
/* 禁止图片拖拽 */
img {
  -webkit-user-drag: none; /* Safari */
  -khtml-user-drag: none; /* Konqueror HTML */
  -moz-user-drag: none; /* Firefox */
  -o-user-drag: none; /* Opera */
}

/* 文本不可选中 */
.no-select {
  user-select: none;
}

.kk-flex {
  display: flex;
  align-items: center;
}

.kk-flex-column {
  display: flex;
  flex-direction: column;
  align-items: center;
}

</style>
