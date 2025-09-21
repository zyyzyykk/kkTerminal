<template>
  <div class="no-data" :style="viewStyle" >
    <div style="flex: 1;" ></div>
    <slot name="mySlot" ></slot>
    <div style="display: flex; align-items: center;" >
      <div>
        <img src="../../assets/no_data.png" :alt="$t('暂无数据')" style="width: 120px;" >
      </div>
    </div>
    <div class="msg" >{{ $t(msg) }}</div>
    <div style="flex: 1;" ></div>
  </div>
</template>

<script>
import { computed } from 'vue';
import i18n from "@/locales/i18n";

export default {
  name:'NoData',
  components: {
  },
  props: {
    msg: {
      type:String,
      required:false,
      default: i18n.global.k('暂无数据'),
    },
    width:{
      type:String,
      required:false,
    },
    height:{
      type:String,
      required:false,
    },
    minHeight:{
      type:String,
      required:false,
    },
  },
  setup(props) {

    const viewStyle = computed(() => {
      return {
        width: props.width ? props.width : '100%',
        height: props.height ? props.height : '100%',
        minHeight: props.minHeight ? props.minHeight : '',
      }
    });

    return {
      viewStyle,
    }
  },
}
</script>

<style scoped>
.no-data {
  text-align: center;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.msg {
  margin-top: 10px;
  color: #909399;
  font-size: 14px;
}
</style>
