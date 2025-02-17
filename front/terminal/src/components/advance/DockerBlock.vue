<template>
  <el-dialog
      v-model="computedDialogVisible"
      destroy-on-close
      :width="720"
      title="Docker"
      :modal="false"
      modal-class="kk-dialog-class"
      header-class="kk-header-class"
      body-class="kk-body-class-6"
      :before-close="closeDialog"
      draggable
  >
    <div class="no-select" >
      <el-tabs element-loading-text="Loading..." v-loading="loading" @tab-click="handleTabClick" type="border-card" >
        <el-tab-pane :label="$t('容器')">
          <el-table style="height: 200px; width: 100%" v-if="dockerInfo.container.length > 0" :data="dockerInfo.container" border stripe >
            <el-table-column show-overflow-tooltip type="selection" :selectable="selectable" width="40" />
            <el-table-column show-overflow-tooltip prop="id" label="ID" width="130" />
            <el-table-column show-overflow-tooltip prop="name" :label="$t('名称')" width="120" />
            <el-table-column show-overflow-tooltip prop="image" :label="$t('镜像')" width="140" />
            <el-table-column show-overflow-tooltip prop="port" :label="$t('端口')" />
          </el-table>
          <div v-else >
            <NoData height="200px" msg="No Data"></NoData>
          </div>
        </el-tab-pane>
        <el-tab-pane :label="$t('镜像')">
          <el-table style="height: 200px; width: 100%" v-if="dockerInfo.image.length > 0" :data="dockerInfo.image" border stripe >
            <el-table-column type="selection" :selectable="selectable" width="40" />
            <el-table-column show-overflow-tooltip prop="id" label="ID" width="130" />
            <el-table-column show-overflow-tooltip prop="repository" :label="$t('仓库')" />
            <el-table-column show-overflow-tooltip prop="size" :label="$t('大小')" width="100" />
            <el-table-column show-overflow-tooltip prop="createTime" :label="$t('创建时间')" width="140" />
          </el-table>
          <div v-else >
            <NoData height="200px" msg="No Data"></NoData>
          </div>
        </el-tab-pane>
        <el-tab-pane :label="$t('网络')">
          <el-table style="height: 200px; width: 100%" v-if="dockerInfo.network.length > 0" :data="dockerInfo.network" border stripe >
            <el-table-column type="selection" :selectable="selectable" width="40" />
            <el-table-column show-overflow-tooltip prop="name" :label="$t('名称')" width="140" />
            <el-table-column show-overflow-tooltip prop="ip" label="IP" />
            <el-table-column show-overflow-tooltip prop="gateway" :label="$t('网关')" width="120" />
            <el-table-column show-overflow-tooltip prop="createTime" :label="$t('创建时间')" width="140" />
          </el-table>
          <div v-else >
            <NoData height="200px" msg="No Data"></NoData>
          </div>
        </el-tab-pane>
        <el-tab-pane :label="$t('数据卷')">
          <el-table style="height: 200px; width: 100%" v-if="dockerInfo.volume.length > 0" :data="dockerInfo.volume" border stripe >
            <el-table-column type="selection" :selectable="selectable" width="40" />
            <el-table-column show-overflow-tooltip prop="name" :label="$t('名称')" width="140" />
            <el-table-column show-overflow-tooltip prop="mount" :label="$t('挂载点')" />
            <el-table-column show-overflow-tooltip prop="createTime" :label="$t('创建时间')" width="140" />
          </el-table>
          <div v-else >
            <NoData height="200px" msg="No Data"></NoData>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>
  </el-dialog>
</template>

<script>

import NoData from "@/components/NoData.vue";
import { computed, ref } from "vue";
import $ from "jquery";
import { http_base_url } from "@/env/BaseUrl";

export default {
  name: 'DockerBlock',
  components: {
    NoData,
  },
  props:['sshKey', 'advance'],
  setup(props) {

    // 控制Dialog显示
    const DialogVisible = ref(false);
    const computedDialogVisible = computed(() => {
      return DialogVisible.value && props.advance;
    });
    const loading = ref(false);

    // Docker信息
    const isDocker = ref(true);
    const dockerInfo = ref({
      container: [],
      image: [],
      network: [],
      volume: [],
    });
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
            isDocker.value = true;
            let container = dockerInfo.value.container;
            let image = dockerInfo.value.image;
            let network = dockerInfo.value.network;
            let volume = dockerInfo.value.volume;
            // 容器
            if(currentType == 0) {
              const containerArr = resp.data.split('$');
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
              const imageArr = resp.data.split('$');
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
              const networkArr = resp.data.split('$');
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
              const volumeArr = resp.data.split('$');
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
          else isDocker.value = false;
          loading.value = false;
        }
      });
    };
    const handleTabClick = (tab) => {
      loading.value = true;
      getDockerInfo(tab.index);
    };

    // 重置
    const reset = (deep=false) => {
      if(deep) {
        isDocker.value = true;
        dockerInfo.value = {
          container: [],
          image: [],
          network: [],
          volume: [],
        };
      }
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
      dockerInfo,
      getDockerInfo,
      handleTabClick,
      loading,
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

</style>
