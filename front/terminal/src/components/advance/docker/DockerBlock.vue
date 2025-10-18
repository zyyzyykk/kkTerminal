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
      <el-tabs stretch :element-loading-text="$t('加载中...')" v-loading="loading" @tab-click="handleTabClick" type="border-card" >
        <el-tab-pane :label="$t('部署')" >
          <div v-if="noDocker" class="kk-flex-column" style="height: 240px;" >
            <div style="flex: 1;" ></div>
            <div><img style="height: 160px;" src="@/assets/no_docker.png" alt="docker" ></div>
            <div class="kk-flex" style="margin-top: 25px;" >
              <div style="font-size: large;" >{{ $t('Docker未安装?') }}</div>
              <div>
                <el-button type="primary" @click="installDocker" style="margin-left: 15px;" >
                  {{ $t('安装') }}
                </el-button>
              </div>
            </div>
            <div style="flex: 1;" ></div>
          </div>
          <div v-else >
            <div id="docker-appstore" v-if="!deployInfo.isShow" style="height: 240px; overflow-y: scroll;" >
              <div class="kk-flex" >
                <span><img src="@/assets/app_store.svg" alt="appstore" style="height: 30px;" ></span>
                <div style="margin-left: 15px; font-size: 20px; font-weight: bolder;" >{{ $t('Docker应用商店') }}</div>
                <div style="flex: 1;" ></div>
                <div>
                  <el-input v-model="appSearch" class="w-50 m-2" :placeholder="$t('搜索应用')" >
                    <template #suffix>
                      <el-icon class="el-input__icon" ><Search /></el-icon>
                    </template>
                  </el-input>
                </div>
                <el-dropdown style="margin-left: 20px;" hide-timeout="300" >
                  <span class="a-link no-select" >{{ $t(dockerAppTypes[appType]) }}<el-icon class="el-icon--right" ><arrow-down /></el-icon></span>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <template v-for="(type,index) in dockerAppTypes" :key="index" >
                        <el-dropdown-item class="no-select" @click="appType = index" >{{ $t(type) }}</el-dropdown-item>
                      </template>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </div>
              <div class="kk-border" ></div>
              <div v-if="dockerApps.length > 0" class="card-container" >
                <div class="card-item" v-for="(app, index) in dockerApps" :key="index" >
                  <el-card>
                    <template #header>
                      <div class="card-header" >
                        <div class="kk-flex" >
                          <span class="app-title" >{{ app.name }}</span>
                          <div style="flex: 1;" ></div>
                          <el-button size="small" type="primary" @click="handleAppGet(app.deployInfo)" >
                            {{ $t('获取') }}
                          </el-button>
                        </div>
                      </div>
                    </template>
                    <div class="kk-flex" >
                      <div>
                        <div class="app-img-box kk-flex" >
                          <div style="flex: 1;" ></div>
                          <img :src="app.img" :alt="app.name" >
                          <div style="flex: 1;" ></div>
                        </div>
                      </div>
                      <div>{{ $t(app.desc) }}</div>
                    </div>
                  </el-card>
                </div>
              </div>
              <div v-else >
                <NoData height="196px" :msg="i18n.global.k('暂无应用')" ></NoData>
              </div>
            </div>
            <div v-else style="height: 240px; overflow-y: scroll;" >
              <div class="kk-flex" >
                <el-icon @click="handleReset(false)" style="cursor: pointer; font-size: 16px;" ><ArrowLeft /></el-icon>
                <div style="margin-left: 8px;" >{{ $t('返回') }}</div>
              </div>
              <div class="kk-border" ></div>
              <div style="padding: 0 120px;" >
                <div class="kk-flex deploy-item" >
                  <div class="form-width" >{{ $t('镜像') }}</div>
                  <div class="kk-flex" style="flex: 1;" >
                    <el-input style="flex: 1;" v-model="deployInfo.imageName" class="w-50 m-2" :placeholder="$t('镜像名称')" ></el-input>
                    <div style="margin: 0 5px;" >:</div>
                    <el-input v-model="deployInfo.imageVersion" style="width: 80px;" class="w-50 m-2" :placeholder="$t('版本号')" ></el-input>
                  </div>
                </div>
                <div class="kk-flex deploy-item" >
                  <div class="form-width" >{{ $t('容器') }}</div>
                  <div style="flex: 1;" >
                    <el-input v-model="deployInfo.containerName" class="w-50 m-2" :placeholder="$t('容器名称')" ></el-input>
                  </div>
                </div>
                <div class="kk-flex deploy-item" >
                  <div class="form-width" >{{ $t('端口映射') }}</div>
                  <div style="flex: 1;" >
                    <el-input v-model="deployInfo.portMapping" class="w-50 m-2" :placeholder="$t('每行一个，主机端口:容器端口')" type="textarea" ></el-input>
                  </div>
                </div>
                <div class="kk-flex deploy-item" >
                  <div class="form-width" >{{ $t('环境变量') }}</div>
                  <div style="flex: 1;" >
                    <el-input v-model="deployInfo.envVar" class="w-50 m-2" :placeholder="$t('每行一个，环境变量名=值')" type="textarea" ></el-input>
                  </div>
                </div>
                <div class="kk-flex deploy-item" >
                  <div class="form-width" >{{ $t('数据卷挂载') }}</div>
                  <div style="flex: 1;" >
                    <el-input v-model="deployInfo.volumeMounting" class="w-50 m-2" :placeholder="$t('每行一个，主机路径:容器路径')" type="textarea" ></el-input>
                  </div>
                </div>
                <div class="kk-flex deploy-item" >
                  <div class="form-width" >{{ $t('其它命令选项') }}</div>
                  <div style="flex: 1;" >
                    <el-input v-model="deployInfo.paramOptions" class="w-50 m-2" :placeholder="$t('每行一个，-命令选项 值')" type="textarea" ></el-input>
                  </div>
                </div>
                <div style="height: 10px;" ></div>
                <div class="kk-flex deploy-item" >
                  <div style="flex: 1;" ></div>
                  <el-button :disabled="!deployInfo.imageName" type="primary" @click="handleDeploy" >
                    {{ $t('部署') }}
                  </el-button>
                  <div style="flex: 1;" ></div>
                  <el-button text bg @click="handleReset(true)" >
                    {{ $t('清空') }}
                  </el-button>
                  <div style="flex: 1;" ></div>
                </div>
              </div>
            </div>
          </div>
        </el-tab-pane>
        <el-tab-pane :disabled="noDocker" :label="$t('容器')" >
          <el-table @cell-dblclick="tableDataCopy" style="height: 200px; width: 100%;" v-if="dockerInfo.container.length > 0" :data="dockerInfo.container" @selection-change="tableSelect" border stripe table-layout="fixed" >
            <el-table-column type="selection" width="40" fixed="left" />
            <el-table-column show-overflow-tooltip prop="id" label="ID" width="120" />
            <el-table-column show-overflow-tooltip prop="name" :label="$t('名称')" width="120" />
            <el-table-column show-overflow-tooltip prop="status" :label="$t('状态')" width="80" />
            <el-table-column show-overflow-tooltip prop="image" :label="$t('镜像')" width="140" />
            <el-table-column show-overflow-tooltip prop="port" :label="$t('端口')" />
            <el-table-column show-overflow-tooltip width="50" fixed="right" >
              <template #header>
                <el-icon style="font-size: 18px; color: #606266;" ><Operation /></el-icon>
              </template>
              <template #default="scope">
                <el-icon @click="openContainerViewer(scope.row)" style="font-size: 18px; color: #606266; cursor: pointer;" ><Document /></el-icon>
              </template>
            </el-table-column>
          </el-table>
          <div v-else >
            <NoData v-if="!loading" height="240px" ></NoData>
            <div v-else style="height: 240px;" ></div>
          </div>
          <div class="kk-flex" v-if="dockerInfo.container.length > 0" style="height: 35px; margin-top: 5px;" >
            <el-dropdown hide-timeout="300" >
              <span class="a-link no-select" >{{ $t(containerTypeArr[containerType]) }}<el-icon class="el-icon--right" ><arrow-down /></el-icon></span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item class="no-select" @click="containerType = 1" >{{ $t(containerTypeArr[1]) }}</el-dropdown-item>
                  <el-dropdown-item class="no-select" @click="containerType = 2" >{{ $t(containerTypeArr[2]) }}</el-dropdown-item>
                  <el-dropdown-item class="no-select" @click="containerType = 3" >{{ $t(containerTypeArr[3]) }}</el-dropdown-item>
                  <el-dropdown-item class="no-select" @click="containerType = 4" >{{ $t(containerTypeArr[4]) }}</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
            <el-button :disabled="!(selectedItems.length > 0 && containerType !== 0)" size="small" type="primary" @click="operateConfirm" style="margin-left: 10px;" >
              {{ $t('确定') }}
            </el-button>
          </div>
        </el-tab-pane>
        <el-tab-pane :disabled="noDocker" :label="$t('镜像')" >
          <el-table @cell-dblclick="tableDataCopy" style="height: 200px; width: 100%;" v-if="dockerInfo.image.length > 0" :data="dockerInfo.image" @selection-change="tableSelect" border stripe >
            <el-table-column type="selection" width="40" fixed="left" />
            <el-table-column show-overflow-tooltip prop="id" label="ID" width="130" />
            <el-table-column show-overflow-tooltip prop="repository" :label="$t('仓库')" />
            <el-table-column show-overflow-tooltip prop="size" :label="$t('大小')" width="100" />
            <el-table-column show-overflow-tooltip prop="createTime" :label="$t('创建时间')" width="140" sortable />
          </el-table>
          <div v-else >
            <NoData v-if="!loading" height="240px" ></NoData>
            <div v-else style="height: 240px;" ></div>
          </div>
          <div class="kk-flex" v-if="dockerInfo.image.length > 0" style="height: 35px; margin-top: 5px;" >
            <el-dropdown hide-timeout="300" >
              <span class="a-link no-select" >{{ $t(deleteTypeArr[deleteType]) }}<el-icon class="el-icon--right" ><arrow-down /></el-icon></span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item class="no-select" @click="deleteType = 1" >{{ $t(deleteTypeArr[1]) }}</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
            <el-button :disabled="!(selectedItems.length > 0 && deleteType !== 0)" size="small" type="primary" @click="deleteConfirm" style="margin-left: 10px;" >
              {{ $t('确定') }}
            </el-button>
          </div>
        </el-tab-pane>
        <el-tab-pane :disabled="noDocker" :label="$t('网络')" >
          <el-table @cell-dblclick="tableDataCopy" style="height: 200px; width: 100%;" v-if="dockerInfo.network.length > 0" :data="dockerInfo.network" @selection-change="tableSelect" border stripe >
            <el-table-column type="selection" width="40" fixed="left" />
            <el-table-column show-overflow-tooltip prop="name" :label="$t('名称')" width="140" />
            <el-table-column show-overflow-tooltip prop="ip" label="IP" />
            <el-table-column show-overflow-tooltip prop="gateway" :label="$t('网关')" width="120" />
            <el-table-column show-overflow-tooltip prop="createTime" :label="$t('创建时间')" width="140" sortable />
          </el-table>
          <div v-else >
            <NoData v-if="!loading" height="240px" ></NoData>
            <div v-else style="height: 240px;" ></div>
          </div>
          <div class="kk-flex" v-if="dockerInfo.network.length > 0" style="height: 35px; margin-top: 5px;" >
            <el-dropdown hide-timeout="300" >
              <span class="a-link no-select" >{{ $t(deleteTypeArr[deleteType]) }}<el-icon class="el-icon--right" ><arrow-down /></el-icon></span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item class="no-select" @click="deleteType = 2" >{{ $t(deleteTypeArr[2]) }}</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
            <el-button :disabled="!(selectedItems.length > 0 && deleteType !== 0)" size="small" type="primary" @click="deleteConfirm" style="margin-left: 10px;" >
              {{ $t('确定') }}
            </el-button>
          </div>
        </el-tab-pane>
        <el-tab-pane :disabled="noDocker" :label="$t('数据卷')" >
          <el-table @cell-dblclick="tableDataCopy" style="height: 200px; width: 100%;" v-if="dockerInfo.volume.length > 0" :data="dockerInfo.volume" @selection-change="tableSelect" border stripe >
            <el-table-column type="selection" width="40" fixed="left" />
            <el-table-column show-overflow-tooltip prop="name" :label="$t('名称')" width="140" />
            <el-table-column show-overflow-tooltip prop="mount" :label="$t('挂载点')" />
            <el-table-column show-overflow-tooltip prop="createTime" :label="$t('创建时间')" width="140" sortable />
          </el-table>
          <div v-else >
            <NoData v-if="!loading" height="240px" ></NoData>
            <div v-else style="height: 240px;" ></div>
          </div>
          <div class="kk-flex" v-if="dockerInfo.volume.length > 0" style="height: 35px; margin-top: 5px;" >
            <el-dropdown hide-timeout="300" >
              <span class="a-link no-select" >{{ $t(deleteTypeArr[deleteType]) }}<el-icon class="el-icon--right" ><arrow-down /></el-icon></span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item class="no-select" @click="deleteType = 3" >{{ $t(deleteTypeArr[3]) }}</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
            <el-button :disabled="!(selectedItems.length > 0 && deleteType !== 0)" size="small" type="primary" @click="deleteConfirm" style="margin-left: 10px;" >
              {{ $t('确定') }}
            </el-button>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>
  </el-dialog>

  <!-- 容器详细信息 -->
  <DockerContainerViewer ref="dockerContainerViewerRef" :sshKey="sshKey" ></DockerContainerViewer>

</template>

<script>
import DockerContainerViewer from "./DockerContainerViewer";
import NoData from "@/components/common/NoData";
import { computed, ref } from "vue";
import $ from "jquery";
import { http_base_url } from "@/env/BaseUrl";
import { ArrowDown, ArrowLeft, Search, Document, Operation } from '@element-plus/icons-vue';
import i18n from "@/locales/i18n";
import { ElMessage } from "element-plus";
import { deleteDialog } from "@/components/common/DeleteDialog";
import useClipboard from "vue-clipboard3";
import { dockerAppTypes, dockerAppStore } from "./DockerAppStore";
import { generateRandomString } from "@/utils/StringUtil";

export default {
  name: 'DockerBlock',
  components: {
    DockerContainerViewer,
    NoData,
    ArrowDown,
    ArrowLeft,
    Search,
    Document,
    Operation,
  },
  props: ['sshKey', 'advance'],
  setup(props, context) {
    // 拷贝
    const { toClipboard } = useClipboard();

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
    const initDockerInfo = {
      container: [],
      image: [],
      network: [],
      volume: [],
    }
    const dockerInfo = ref({...initDockerInfo});
    const getDockerVersion = (sshKey) => {
      loading.value = true;
      $.ajax({
        url: http_base_url + '/docker/version',
        type: 'post',
        data: {
          sshKey: sshKey,
        },
        success(resp) {
          loading.value = false;
          if(resp.status === 'success') {
            noDocker.value = false;
            handleTabClick({index: 0});
          }
          else noDocker.value = true;
        }
      });
    };
    const getDockerInfo = (type) => {
      loading.value = true;
      const currentType = type || 0;
      $.ajax({
        url: http_base_url + '/docker/info',
        type: 'post',
        data: {
          sshKey: props.sshKey,
          type: currentType,
        },
        success(resp) {
          if(resp.status === 'success') {
            noDocker.value = false;
            let container = dockerInfo.value.container;
            let image = dockerInfo.value.image;
            let network = dockerInfo.value.network;
            let volume = dockerInfo.value.volume;
            // 容器
            if(currentType === 0) {
              const containerArr = (resp.data && resp.data !== 'null') ? resp.data.split('$') : [];
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
            else if(currentType === 1) {
              const imageArr = (resp.data && resp.data !== 'null') ? resp.data.split('$') : [];
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
            else if(currentType === 2) {
              const networkArr = (resp.data && resp.data !== 'null') ? resp.data.split('$') : [];
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
            else if(currentType === 3) {
              const volumeArr = (resp.data && resp.data !== 'null') ? resp.data.split('$') : [];
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
      selectedItems.value = [];
      containerType.value = 0;
      deleteType.value = 0;
      const index = parseInt(tab.index);
      if(index > 0) getDockerInfo(index - 1);
    };

    // Docker 容器操作 删除
    const containerType = ref(0);
    const deleteType = ref(0);
    const containerTypeArr = [
      i18n.global.k('选择批量操作'),
      i18n.global.k('启动容器'),
      i18n.global.k('停止容器'),
      i18n.global.k('重启容器'),
      i18n.global.k('删除容器'),
    ];
    const deleteTypeArr = [
      i18n.global.k('选择批量操作'),
      i18n.global.k('删除镜像'),
      i18n.global.k('删除网络'),
      i18n.global.k('删除数据卷'),
    ];
    const containerOperate = () => {
      loading.value = true;
      const type = containerType.value - 1;
      $.ajax({
        url: http_base_url + '/docker/container',
        type: 'post',
        data: {
          sshKey: props.sshKey,
          type: type,
          items: selectedItems.value.map(item => {return (item.id || item.name)}).join(' '),
        },
        success(resp) {
          ElMessage({
            message: resp.info,
            type: resp.status,
            grouping: true,
          });
          selectedItems.value = [];
          loading.value = false;
          handleTabClick({index: 1});
        }
      });
    };
    const dockerDelete = () => {
      loading.value = true;
      const type = deleteType.value;
      $.ajax({
        url: http_base_url + '/docker/delete',
        type: 'post',
        data: {
          sshKey: props.sshKey,
          type: type,
          items: selectedItems.value.map(item => {return (item.id || item.name)}).join(' '),
        },
        success(resp) {
          ElMessage({
            message: resp.info,
            type: resp.status,
            grouping: true,
          });
          selectedItems.value = [];
          loading.value = false;
          handleTabClick({index: type + 1});
        }
      });
    };
    const operateConfirm = () => {
      deleteDialog(i18n.global.t('提示'), i18n.global.t('确定进行此操作吗?'), containerOperate);
    };
    const deleteConfirm = () => {
      deleteDialog(i18n.global.t('提示'), i18n.global.t('确定进行此操作吗?'), dockerDelete);
    };

    // 查看容器详细信息
    const dockerContainerViewerRef = ref();
    const openContainerViewer = (row) => {
      dockerContainerViewerRef.value.initText(row);
      dockerContainerViewerRef.value.DialogVisible = true;
    };

    // 安装Docker
    const installDocker = () => {
      ElMessage({
        message: i18n.global.t('开始安装'),
        type: 'success',
        grouping: true,
        repeatNum: Number.MIN_SAFE_INTEGER,
      });
      context.emit('install', "curl -fsSL https://get.docker.com -o get-docker.sh && sudo sh get-docker.sh\n");
      deepCloseDialog();
    };

    // 复制表格数据
    const tableDataCopy = async (row, column) => {
      if(column.property === 'createTime' || !row[column.property]) return;
      await toClipboard(row[column.property]);
      ElMessage({
        message: i18n.global.t('复制成功'),
        type: 'success',
        grouping: true,
        repeatNum: Number.MIN_SAFE_INTEGER,
      });
    };

    // 部署
    const initDeployInfo = {
      isShow: false,
      imageName: '',
      imageVersion: '',
      containerName: '',
      portMapping: '',
      envVar: '',
      volumeMounting: '',
      paramOptions: '',
    };
    const deployInfo = ref({...initDeployInfo});
    const handleAppGet = (appInfo) => {
      deployInfo.value = {...initDeployInfo, ...appInfo};
      if(deployInfo.value.containerName) deployInfo.value.containerName += "_" + generateRandomString(4);
      deployInfo.value.isShow = true;
    };
    const handleReset = (isShow=true) => {
      deployInfo.value = {...initDeployInfo};
      deployInfo.value.isShow = isShow;
    };
    const handleDeploy = () => {
      let deployCmd = "docker run -dit";
      if(deployInfo.value.containerName) deployCmd += " --name " + deployInfo.value.containerName;
      if(deployInfo.value.portMapping) {
        deployCmd += deployInfo.value.portMapping.split("\n").filter(port => port).map(port => ` -p ${port}`).join("");
      }
      if(deployInfo.value.envVar) {
        deployCmd += deployInfo.value.envVar.split("\n").filter(env => env).map(env => ` -e ${env}`).join("");
      }
      if(deployInfo.value.volumeMounting) {
        deployCmd += deployInfo.value.volumeMounting.split("\n").filter(volume => volume).map(volume => ` -v ${volume}`).join("");
      }
      if(deployInfo.value.paramOptions) {
        deployCmd += deployInfo.value.paramOptions.split("\n").filter(option => option).map(option => ` ${option}`).join("");
      }
      deployCmd += " --restart=unless-stopped";
      deployCmd += " " + deployInfo.value.imageName + ":";
      deployCmd += deployInfo.value.imageVersion || "latest";
      deployCmd += "\n";
      ElMessage({
        message: i18n.global.t('开始部署'),
        type: 'success',
        grouping: true,
        repeatNum: Number.MIN_SAFE_INTEGER,
      });
      handleReset(false);
      context.emit('deploy', deployCmd);
    };
    // 应用商店
    const appSearch = ref('');
    const appType = ref(0);
    const dockerApps = computed(() => {
      return dockerAppStore.filter(app => {
        if(!app.name.toLowerCase().includes(appSearch.value.toLowerCase())
            && !i18n.global.t(app.desc).toLowerCase().includes(appSearch.value.toLowerCase())) {
          return false;
        }
        if(appType.value !== 0 && app.type !== appType.value) return false;
        else return true;
      });
    });

    // 重置
    const reset = (deep=false) => {
      if(deep) {
        noDocker.value = true;
        dockerInfo.value = {...initDockerInfo};
        deployInfo.value = {...initDeployInfo};
      }
      containerType.value = 0;
      deleteType.value = 0;
      selectedItems.value = [];
      appSearch.value = '';
      appType.value = 0;
      loading.value = false;
      DialogVisible.value = false;
    };

    // 关闭
    const closeDialog = (done) => {
      if(dockerContainerViewerRef.value) dockerContainerViewerRef.value.closeDialog();
      setTimeout(() => {
        reset();
      }, 400);
      DialogVisible.value = false;
      if(done) done();
    };
    // 深度关闭
    const deepCloseDialog = (done) => {
      if(dockerContainerViewerRef.value) dockerContainerViewerRef.value.closeDialog();
      setTimeout(() => {
        reset(true);
      }, 400);
      DialogVisible.value = false;
      if(done) done();
    };

    return {
      i18n,
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
      dockerContainerViewerRef,
      openContainerViewer,
      tableDataCopy,
      deployInfo,
      dockerAppTypes,
      dockerApps,
      appSearch,
      appType,
      handleAppGet,
      handleReset,
      handleDeploy,
    }
  }
}
</script>

<style scoped>
.kk-flex {
  display: flex;
  align-items: center;
}

.kk-flex-column {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.kk-border {
  padding-bottom: 10px;
  border-bottom: 1px solid #ddd;
}

.el-card__header {
  padding-top: 8px !important;
  padding-bottom: 8px !important;
}

.app-title {
  color: #666;
  font-weight: bold;
  font-size: 16px;
}

.app-img-box {
  width: 48px;
  height: 48px;
  margin-right: 12px;
  --un-border-opacity: 1;
  box-shadow: rgba(0, 0, 0, 0.15) 0 0 6px;
  border-color: rgb(238 238 238 / var(--un-border-opacity));
  border-radius: 100%;
  border-style: solid;
}

.card-container {
  display: flex;
  flex-wrap: wrap;
  gap: 20px; /* 卡片间距 */
  padding: 0 5px;
  justify-content: space-between;
  margin-top: 20px;
}

.card-item {
  width: calc(50% - 10px);
}

.deploy-item {
  margin-top: 20px;
}

.form-width {
  width: 140px;
}

/* 隐藏滚动条 */
#docker-appstore {
  scrollbar-width: none !important; /* Firefox */
  -ms-overflow-style: none !important; /* IE 和 Edge */
}

#docker-appstore::-webkit-scrollbar {
  display: none !important; /* Chrome 和 Safari */
}
</style>
