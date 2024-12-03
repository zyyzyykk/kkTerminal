<template>
  <el-dialog
    v-model="DialogVisilble"
    destroy-on-close
    :width="426"
    :title="$t('偏好设置')"
    :modal="false"
    modal-class="kk-dialog-class"
    :before-close="closeDialog"
    draggable
  >
    <div style="margin-top: -27px;"></div>
    <div class="no-select">
      <div class="item-class" style="margin-bottom: 13px;">
        <div class="form-width" >{{ $t('颜色') }}：</div>
        <div class="item-class">
          <div>{{ $t('背景色') }}</div>
          <div style="margin: 0 20px;" >
            <el-color-picker v-model="setInfo.bg" />
          </div>
        </div>
        <div style="width: 20px;" ></div>
        <div class="item-class">
          <div>{{ $t('前景色') }}</div>
          <div style="margin: 0 20px;" >
            <el-color-picker v-model="setInfo.fg" />
          </div>
        </div>
      </div>
      <div class="item-class" style="margin-bottom: 13px;">
        <div class="form-width" >{{ $t('文本') }}：</div>
        <div class="item-class">
          <div>{{ $t('字体') }}</div>
          <el-dropdown style="margin: 0 20px;" >
            <span class="a-link" >{{ setInfo.fontFamily }}<el-icon class="el-icon--right"><arrow-down /></el-icon></span>
            <template #dropdown>
              <el-dropdown-menu>
                <template v-for="(font,index) in fontFamilyList" :key="index" >
                  <el-dropdown-item @click="setInfo.fontFamily = font" >{{ font }}</el-dropdown-item>
                </template>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
        <div class="item-class">
          <div>{{ $t('字号') }}</div>
          <el-dropdown style="margin: 0 20px;" >
            <span class="a-link" >{{ setInfo.fontSize }}<el-icon class="el-icon--right"><arrow-down /></el-icon></span>
            <template #dropdown>
              <el-dropdown-menu>
                <template v-for="(size,index) in fontSizeList" :key="index" >
                  <el-dropdown-item @click="setInfo.fontSize = size" >{{ size }}</el-dropdown-item>
                </template>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
      <div class="item-class" style="margin-bottom: 8px;">
        <div class="form-width" >{{ $t('光标') }}：</div>
        <div class="item-class">
          <div>{{ $t('样式') }}</div>
          <el-dropdown style="margin: 0 20px;" >
            <span class="a-link" >{{ setInfo.cursorStyle }}<el-icon class="el-icon--right"><arrow-down /></el-icon></span>
            <template #dropdown>
              <el-dropdown-menu>
                <template v-for="(style,index) in cursorStyleList" :key="index" >
                  <el-dropdown-item @click="setInfo.cursorStyle = style" >{{ style }}</el-dropdown-item>
                </template>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
        <div style="width: 15px;" ></div>
        <div class="item-class">
          <div>{{ $t('闪烁') }}</div>
          <div style="margin: 0 20px;" >
            <el-switch v-model="setInfo.cursorBlink" />
          </div>
        </div>
      </div>
      <div class="item-class" style="margin-bottom: 13px;">
        <div class="form-width" >{{ $t('功能') }}：</div>
        <div class="item-class">
          <div>TCode</div>
          <div style="margin: 0 20px;" >
            <el-switch v-model="setInfo.tCode" />
          </div>
        </div>
      </div>
      <div class="item-class" style="margin-bottom: 5px;">
        <div class="form-width" >{{ $t('其它') }}：</div>
        <div class="item-class">
          <div>{{ $t('语言') }}</div>
          <el-dropdown style="margin: 0 20px;" >
            <span class="a-link" >{{ mapValueToLabel(setInfo.lang) }}<el-icon class="el-icon--right"><arrow-down /></el-icon></span>
            <template #dropdown>
              <el-dropdown-menu>
                <template v-for="(lang,index) in langList" :key="index" >
                  <el-dropdown-item @click="setInfo.lang = lang.value" >{{ lang.label }}</el-dropdown-item>
                </template>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </div>
    <div style="margin-bottom: 5px;"></div>
    <div style="display: flex; border-top: 1px solid #f1f2f4;">
      <div style="flex: 1;"></div>
      <el-button size="small" type="primary" @click="confirm" style="margin-bottom: -15px; margin-top: 10px;">
        {{ $t('确定') }}
      </el-button>
    </div>
  </el-dialog>
</template>

<script>
import { ref, computed } from 'vue';
import { ArrowDown } from '@element-plus/icons-vue';

export default {
  name:'StyleSetting',
  components: {
    ArrowDown,
  },
  props:['env','os'],
  setup(props,context) {

    // 控制Dialog显示
    const DialogVisilble = ref(false);

    // 语言列标
    const langList = [{label:"English",value:"en"}, {label:"Chinese",value:"zh"}];
    const mapValueToLabel = (lang) => {
      const item = langList.find(item => item.value === lang);
      return item ? item.label : "English";
    };

    // 字体列表
    const fontFamilyList = computed(() => {
      if(props.os == "Windows") {
        return ['Courier New','Consolas','Monospace','Lucida Console'];
      }
      else if(props.os == "Mac" || props.os == "iOS") {
        return ['Courier New','Menlo','Monaco','Courier'];
      }
      else return ['Courier New','Consolas','Monospace','Lucida Console'];
    });
    // 字号列表
    const fontSizeList = [12,14,16,18,20];
    // 光标样式列表
    const cursorStyleList = ['block','underline','bar'];

    // 设置信息
    const setInfo = ref({
      lang: props.env.lang,
      bg: props.env.bg,
      fg: props.env.fg,
      fontFamily: props.env.fontFamily,
      fontSize: props.env.fontSize,
      cursorStyle: props.env.cursorStyle,
      cursorBlink: props.env.cursorBlink,
      tCode: props.env.tCode,
    });
    const confirm = () => {
      context.emit('callback',setInfo.value);
      closeDialog();
    };

    // 重置
    const reset = () => {
      setInfo.value = {
        lang: props.env.lang,
        bg: props.env.bg,
        fg: props.env.fg,
        fontFamily: props.env.fontFamily,
        fontSize: props.env.fontSize,
        cursorStyle: props.env.cursorStyle,
        cursorBlink: props.env.cursorBlink,
        tCode: props.env.tCode,
      };
      DialogVisilble.value = false;
    };

    // 关闭
    const closeDialog = (done) => {
      setTimeout(() => {
        reset();
      },400);
      DialogVisilble.value = false;
      if(done) done();
    };

    return {
      langList,
      mapValueToLabel,
      fontFamilyList,
      fontSizeList,
      cursorStyleList,
      setInfo,
      DialogVisilble,
      confirm,
      closeDialog,
    }
  }


}
</script>

<style scoped>
.item-class {
  display: flex; 
  align-items: center; 
}

/* 文本不可选中 */
.no-select {
  user-select: none;
}

.form-width {
  width: 60px;
}

</style>