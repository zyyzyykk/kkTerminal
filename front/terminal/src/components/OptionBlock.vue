<template>
  <el-dialog
    v-model="DialogVisilble"
    destroy-on-close
    :width="420"
    :modal="false"
    modal-class="kk-dialog-class"
    draggable
  >
    <div style="margin-top: -15px;"></div>
    <div class="no-select">
        <div v-if="files.length != 0">
            <div v-for="item in files" :key="item.name" >
              <template v-if="item.isDirectory == true">
                <div :class="['item-class', (aimFileInfo && item.name == aimFileInfo.name) ? 'item-selected' : '']" @click="aimFileInfo = item" @dblclick="changeDir(dir + item.name + '/')" >
                  <FileIcons :name="item.name" width="20" height="20" :isFloder="item.isDirectory" />
                  <div style="margin: 0 10px;">{{ item.name }}</div>
                </div>
              </template>
              <template v-else>
                <div :class="['item-class', (aimFileInfo && item.name == aimFileInfo.name) ? 'item-selected' : '']" @click="aimFileInfo = item" @dblclick="downloadFile(item.name)" >
                  <FileIcons :name="item.name" width="20" height="20" :isFloder="item.isDirectory" />
                  <div style="margin: 0 10px;">{{ item.name }}</div>
                </div>
              </template>
            </div>
        </div>
        <div v-else>
          <NoData v-if="loading == false" :msg="noDataMsg"></NoData>
        </div>
    </div>
  </el-dialog>
</template>

<script>

export default {
  name: 'OptionBlock',
  components: {

  },
  setup(props)
  {

    return {
      
    }
  }
}
</script>

<style scoped>

</style>