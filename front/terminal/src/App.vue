<template>
  <FrameWork></FrameWork>
</template>

<script>
import FrameWork from "./views/FrameWork";

export default {
  name: 'App',
  components: {
    FrameWork,
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

.kk-dialog-class {
  pointer-events: none;
  z-index: 3456;
}

.kk-header-class {
  padding-top: 4px;
  padding-bottom: 8px !important;
  padding-left: 4px;
  margin-right: 16px;
  user-select: none;
}

.kk-body-class-0 {
  padding: 0 4px;
}

.kk-body-class-6 {
  padding-top: 6px;
  padding-bottom: 0;
  padding-left: 4px;
  margin-right: 4px;
}

.kk-body-class-12 {
  padding-top: 12px;
  padding-bottom: 0;
  padding-left: 4px;
  margin-right: 4px;
}

.el-dialog {
  pointer-events: auto;
}

.el-popconfirm__main {
  user-select: none;
  margin-top: 2px;
}

/* 文本不可选中 */
.no-select {
  user-select: none;
}

</style>
