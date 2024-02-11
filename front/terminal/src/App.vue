<template>
  <FrameWork></FrameWork>
  <div class=""></div>
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

.el-dialog {
  pointer-events: auto;
}

.ant-dropdown {
  margin: 0 0;
  padding: 0 0;
  border-radius: 0;
}

.ant-dropdown-menu {
  margin: 0 0;
  padding: 0 0;
  border-radius: 0;
}

/* :where.ant-dropdown-menu-item {
  height: 30px;
  color: #000;
  background-color: #fff;
} */

.ant-dropdown-menu-item {
  height: 30px;
  color: #000;
  background-color: #fff;
  margin:  0 0 !important;
  padding: 0 0 !important;
}

.ant-dropdown-menu-item-active {
  background-color: #000;
}

.ant-dropdown-menu-title-content {
  font-size: 13px;
  border-radius: 0;
  margin:  0 0 !important;
  padding: 0 0 !important;
}

</style>