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
</style>
