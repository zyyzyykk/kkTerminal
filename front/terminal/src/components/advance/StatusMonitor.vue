<template>
  <el-dialog
      v-model="computedDialogVisible"
      destroy-on-close
      :width="720"
      :title="$t('监控')"
      :modal="false"
      modal-class="kk-dialog-class"
      header-class="kk-header-class"
      body-class="kk-body-class-6"
      :before-close="closeDialog"
      draggable
  >
    <div class="no-select" >
      <el-tabs stretch type="border-card" @tab-click="handleTabClick" >
        <el-tab-pane :label="$t('概览')" >
          <div class="kk-flex" style="height: 200px;" >
            <div style="margin-left: 5px;" ></div>
            <div class="kk-flex-column" >
              <div style="flex: 1;" ></div>
              <div>{{ $t('负载') }}</div>
              <div style="margin-top: 10px;" ></div>
              <el-progress :color="calcStatus(statusInfo.overview.load)" type="circle" :percentage="statusInfo.overview.load" />
              <div style="margin-top: 10px;" ></div>
              <div v-if="statusInfo.overview.load < 33.4" >{{ $t('运行流畅') }}</div>
              <div v-else-if="statusInfo.overview.load < 66.7" >{{ $t('运行缓慢') }}</div>
              <div v-else >{{ $t('运行堵塞') }}</div>
              <div style="flex: 1;" ></div>
            </div>
            <div style="flex: 1;" ></div>
            <div class="kk-flex-column" >
              <div style="flex: 1;" ></div>
              <div>{{ $t('CPU使用率') }}</div>
              <div style="margin-top: 10px;" ></div>
              <el-progress :color="calcStatus(statusInfo.overview.cpu)" type="circle" :percentage="statusInfo.overview.cpu" />
              <div style="margin-top: 10px;" ></div>
              <div>{{ statusInfo.overview.core }} {{ $t('核心') }}</div>
              <div style="flex: 1;" ></div>
            </div>
            <div style="flex: 1;" ></div>
            <div class="kk-flex-column" >
              <div style="flex: 1;" ></div>
              <div>{{ $t('内存使用率') }}</div>
              <div style="margin-top: 10px;" ></div>
              <el-progress :color="calcStatus(statusInfo.overview.usedMemory * 100 / statusInfo.overview.totalMemory)" type="circle" :percentage="calcNumber(statusInfo.overview.usedMemory * 100 / statusInfo.overview.totalMemory)" />
              <div style="margin-top: 10px;" ></div>
              <div>{{ statusInfo.overview.usedMemory }} / {{ statusInfo.overview.totalMemory }} (MB)</div>
              <div style="flex: 1;" ></div>
            </div>
            <div style="flex: 1;" ></div>
            <div class="kk-flex-column" >
              <div style="flex: 1;" ></div>
              <div>/</div>
              <div style="margin-top: 10px;" ></div>
              <el-progress :color="calcStatus(statusInfo.overview.usedRoot * 100 / statusInfo.overview.totalRoot)" type="circle" :percentage="calcNumber(statusInfo.overview.usedRoot * 100 / statusInfo.overview.totalRoot)" />
              <div style="margin-top: 10px;" ></div>
              <div>{{ calcNumber(statusInfo.overview.usedRoot / 1024, 2) }}G / {{ calcNumber(statusInfo.overview.totalRoot / 1024, 2) }}G</div>
              <div style="flex: 1;" ></div>
            </div>
            <div style="margin-left: 5px;" ></div>
          </div>
        </el-tab-pane>
        <el-tab-pane :label="$t('Top进程')" >
          <el-table @cell-dblclick="tableDataCopy" style="height: 200px; width: 100%;" v-if="statusInfo.processes.length > 0" :data="statusInfo.processes" border stripe >
            <el-table-column prop="pid" label="PID" width="80" />
            <el-table-column prop="user" label="User" width="100" />
            <el-table-column prop="cpu" label="%CPU" width="100" />
            <el-table-column prop="mem" label="%MEM" width="100" />
            <el-table-column prop="cmd" label="Command" />
          </el-table>
          <div v-else >
            <NoData height="200px" ></NoData>
          </div>
        </el-tab-pane>
        <el-tab-pane :label="$t('网络')" >
          <div style="position: relative;" >
            <EChart v-show="statusInfo.time.length > 1"
              style="width: 650px; height: 200px;" ref="networkEChartRef"
              :legend="[$t('上行'),$t('下行')]" yName="KB/s" >
            </EChart>
            <div v-show="statusInfo.time.length > 1" style="position: absolute; top: 0; right: 0;" >
              <el-dropdown size="small" style="margin-top: 2px; margin-right: 25px;" hide-timeout="300" >
                <span class="a-link no-select" >{{ selectedNetwork }}<el-icon class="el-icon--right" ><arrow-down /></el-icon></span>
                <template #dropdown>
                  <el-dropdown-menu>
                    <template v-for="(key, val) in statusInfo.network" :key="key" >
                      <el-dropdown-item class="no-select" @click="selectedNetwork = val; updateNetworkData();" >{{ val }}</el-dropdown-item>
                    </template>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
            <div v-show="statusInfo.time.length > 1" style="position: absolute; top: 0; left: 0;" >
              <div class="kk-flex" >
                <div class="kk-flex" style="color: #5a6fc0;" >
                  <el-icon><SortUp /></el-icon>
                  <div>{{ (networkSeries[0].data[networkSeries[0].data.length - 1] || 0) + 'K' }}</div>
                </div>
                <div style="margin-left: 10px;" ></div>
                <div class="kk-flex" style="color: #9eca7f;" >
                  <el-icon><SortDown /></el-icon>
                  <div>{{ (networkSeries[1].data[networkSeries[1].data.length - 1] || 0) + 'K' }}</div>
                </div>
              </div>
            </div>
          </div>
          <div v-if="statusInfo.time.length <= 1" >
            <NoData height="200px" ></NoData>
          </div>
        </el-tab-pane>
        <el-tab-pane :label="$t('磁盘')" >
          <div style="position: relative;" >
            <EChart v-show="statusInfo.time.length > 1"
                    style="width: 650px; height: 200px;" ref="diskEChartRef"
                    :legend="[$t('读'),$t('写')]" yName="KB/s" >
            </EChart>
            <div v-show="statusInfo.time.length > 1" style="position: absolute; top: 0; right: 0;" >
              <el-dropdown size="small" style="margin-top: 2px; margin-right: 25px;" hide-timeout="300" >
                <span class="a-link no-select" >{{ selectedDisk }}<el-icon class="el-icon--right" ><arrow-down /></el-icon></span>
                <template #dropdown>
                  <el-dropdown-menu>
                    <template v-for="(key, val) in statusInfo.disk" :key="key" >
                      <el-dropdown-item class="no-select" @click="selectedDisk = val; updateDiskData();" >{{ val }}</el-dropdown-item>
                    </template>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
            <div v-show="statusInfo.time.length > 1" style="position: absolute; top: 0; left: 0;" >
              <div class="kk-flex" >
                <div class="kk-flex" style="color: #5a6fc0;" >
                  <el-icon><SortUp /></el-icon>
                  <div>{{ (diskSeries[0].data[diskSeries[0].data.length - 1] || 0) + 'K' }}</div>
                </div>
                <div style="margin-left: 10px;" ></div>
                <div class="kk-flex" style="color: #9eca7f;" >
                  <el-icon><SortDown /></el-icon>
                  <div>{{ (diskSeries[1].data[diskSeries[1].data.length - 1] || 0) + 'K' }}</div>
                </div>
              </div>
            </div>
          </div>
          <div v-if="statusInfo.time.length <= 1" >
            <NoData height="200px" ></NoData>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>
  </el-dialog>
</template>

<script>
import { ref, computed } from 'vue';
import $ from 'jquery';
import { ArrowDown, SortUp, SortDown } from '@element-plus/icons-vue';
import { http_base_url } from "@/env/BaseUrl";
import { calcNumber, calcStatus } from "@/components/calc/CalcType";
import NoData from '@/components/common/NoData';
import EChart from "@/components/common/EChart";
import { ElMessage } from "element-plus";
import useClipboard from "vue-clipboard3";
import { calcTime } from "@/components/calc/CalcDate";
import i18n from "@/locales/i18n";

export default {
  name: 'StatusMonitor',
  methods: { calcNumber, calcStatus, calcTime },
  components: {
    EChart,
    NoData,
    ArrowDown,
    SortUp,
    SortDown,
  },
  props: ['sshKey', 'advance'],
  setup(props) {
    // 拷贝
    const { toClipboard } = useClipboard();

    // 控制Dialog显示
    const DialogVisible = ref(false);
    const computedDialogVisible = computed(() => {
      return DialogVisible.value && props.advance;
    });

    // 状态信息
    const initStatusInfo = {
      overview: {
        load: 0,
        cpu: 0,
        core: 0,
        usedMemory: 0,
        totalMemory: 1,
        usedRoot: 0,
        totalRoot: 1,
      },
      processes: [],
      network: {},
      disk: {},
      time: [],
    }
    const statusInfo = ref({...initStatusInfo});
    const selectedNetwork = ref('all');
    const selectedDisk = ref('all');
    const getStatusInfo = () => {
      $.ajax({
        url: http_base_url + '/monitor',
        type: 'post',
        data: {
          sshKey: props.sshKey,
        },
        success(resp) {
          if(resp.status === 'success') {
            const statusArr = resp.data.split('^');
            const overviewArr = statusArr[0].split('@').map(item => {return calcNumber(item)});
            const top4Process = statusArr[1].split('$');
            const networksArr = statusArr[2].split('$');
            const disksArr = statusArr[3].split('$');
            const newTime = Number(statusArr[4]);
            // NetWork
            const network = {...statusInfo.value.network};
            for(let i = 0; i < networksArr.length; i++) {
              const networkInfo = networksArr[i].split('@');
              const networkName = networkInfo[0].substring(0, networkInfo[0].length - 1);
              if(!network[networkName]) {
                network[networkName] = [];
                for(let j = 0; j < statusInfo.value.time.length; j++) {
                  network[networkName].push({
                    totalUp: Number(networkInfo[1]),
                    totalDown: Number(networkInfo[2]),
                  });
                }
              }
              network[networkName].push({
                totalUp: Number(networkInfo[1]),
                totalDown: Number(networkInfo[2]),
              });
            }
            if(!network[selectedNetwork.value]) selectedNetwork.value = "all";
            // Disk
            const disk = {...statusInfo.value.disk};
            for(let i = 0; i < disksArr.length; i++) {
              const diskInfo = disksArr[i].split('@');
              const diskName = diskInfo[0];
              if(!disk[diskName]) {
                disk[diskName] = [];
                for(let j = 0; j < statusInfo.value.time.length; j++) {
                  disk[diskName].push({
                    totalRead: Number(diskInfo[1]),
                    totalWrite: Number(diskInfo[2]),
                  });
                }
              }
              disk[diskName].push({
                totalRead: Number(diskInfo[1]),
                totalWrite: Number(diskInfo[2]),
              });
            }
            if(!disk[selectedDisk.value]) selectedDisk.value = "all";
            // Time
            const time = statusInfo.value.time;
            time.push(newTime);

            statusInfo.value = {
              overview: {
                load: overviewArr[0],
                cpu: overviewArr[1],
                core: overviewArr[2],
                usedMemory: overviewArr[3],
                totalMemory: overviewArr[4],
                usedRoot: overviewArr[5],
                totalRoot: overviewArr[6],
              },
              processes: top4Process.map(item => {
                const processArr = item.split('@');
                return {
                  pid: processArr[0],
                  user: processArr[1],
                  cpu: processArr[2],
                  mem: processArr[3],
                  cmd: processArr[4],
                }
              }),
              network: network,
              disk: disk,
              time: time,
            };
            updateNetworkData();
            updateDiskData();
          }
        }
      });
    };

    // 网络IO series
    const networkSeries = computed(() => {
      const time = statusInfo.value.time;
      const networkInfo = statusInfo.value.network[selectedNetwork.value];
      const totalUp = networkInfo ? networkInfo.map(item => {return item.totalUp}) : [];
      const totalDown = networkInfo ? networkInfo.map(item => {return item.totalDown}) : [];
      return [
        {
            name: i18n.global.t('上行'),
            type: 'line',
            data: totalUp.slice(1).map((value, index) => {return calcNumber(Math.max(0, (value - totalUp[index])) * 1000 / ((time[index + 1] - time[index]) * 1024), 3)}),
        },
        {
          name: i18n.global.t('下行'),
          type: 'line',
          data: totalDown.slice(1).map((value, index) => {return calcNumber(Math.max(0, (value - totalDown[index])) * 1000 / ((time[index + 1] - time[index]) * 1024), 3)}),
        },
      ];
    });
    const updateNetworkData = () => {
      if(networkEChartRef.value) {
        const time = statusInfo.value.time;
        networkEChartRef.value.update(time.slice(1).map((value, index) => {return calcTime(Math.floor((value + time[index]) / 2))}), networkSeries.value);
      }
    };

    // 磁盘IO series
    const diskSeries = computed(() => {
      const time = statusInfo.value.time;
      const diskInfo = statusInfo.value.disk[selectedDisk.value];
      const totalRead = diskInfo ? diskInfo.map(item => {return item.totalRead}) : [];
      const totalWrite = diskInfo ? diskInfo.map(item => {return item.totalWrite}) : [];
      return [
        {
          name: i18n.global.t('读'),
          type: 'line',
          data: totalRead.slice(1).map((value, index) => {return calcNumber(Math.max(0, (value - totalRead[index])) * 1000 / ((time[index + 1] - time[index]) * 2), 3)}),
        },
        {
          name: i18n.global.t('写'),
          type: 'line',
          data: totalWrite.slice(1).map((value, index) => {return calcNumber(Math.max(0, (value - totalWrite[index])) * 1000 / ((time[index + 1] - time[index]) * 2), 3)}),
        },
      ];
    });
    const updateDiskData = () => {
      if(diskEChartRef.value) {
        const time = statusInfo.value.time;
        diskEChartRef.value.update(time.slice(1).map((value, index) => {return calcTime(Math.floor((value + time[index]) / 2))}), diskSeries.value);
      }
    };

    // Echarts
    const networkEChartRef = ref();
    const diskEChartRef = ref();

    const timer = ref(null);
    const interval = 6000;
    const doMonitor = () => {
      if(timer.value == null) {
        setTimeout(() => {
          getStatusInfo();
        }, 1);
        timer.value = setInterval(() => {
          getStatusInfo();
        }, interval);
      }
    };

    const stopMonitor = () => {
      if(timer.value != null) {
        clearInterval(timer.value);
        timer.value = null;
      }
    };

    const handleTabClick = (tab) => {
      const index = parseInt(tab.index);
      if(index === 2 && !networkEChartRef.value.isFinished) networkEChartRef.value.loading = true;
      if(index === 3 && !diskEChartRef.value.isFinished) diskEChartRef.value.loading = true;
    };

    // 复制表格数据
    const tableDataCopy = async (row, column) => {
      if(!row[column.property]) return;
      await toClipboard(row[column.property]);
      ElMessage({
        message: i18n.global.t('复制成功'),
        type: 'success',
        grouping: true,
        repeatNum: Number.MIN_SAFE_INTEGER,
      });
    };

    // 重置
    const reset = (deep=false) => {
      if(deep) {
        statusInfo.value = {...initStatusInfo};
        selectedNetwork.value = 'all';
        selectedDisk.value = 'all';
      }
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
    // 深度关闭
    const deepCloseDialog = (done) => {
      setTimeout(() => {
        reset(true);
      }, 400);
      stopMonitor();
      DialogVisible.value = false;
      if(done) done();
    };

    return {
      computedDialogVisible,
      DialogVisible,
      reset,
      closeDialog,
      deepCloseDialog,
      doMonitor,
      stopMonitor,
      statusInfo,
      networkEChartRef,
      diskEChartRef,
      selectedNetwork,
      selectedDisk,
      updateNetworkData,
      networkSeries,
      updateDiskData,
      diskSeries,
      handleTabClick,
      tableDataCopy,
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
</style>
