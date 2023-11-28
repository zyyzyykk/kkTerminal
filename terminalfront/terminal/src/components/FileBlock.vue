<template>
  <button @click="demo" >点我下载</button>
  <button @click="demo2" >点我列出文件</button>
</template>

<script>
// import { ref } from 'vue';
import $ from 'jquery';

export default {
  name:'FileBlock',
  components: {
  },
  props:['sshKey'],
  setup(props) {

    const demo = () => {
    //     $.ajax({
    //     url: 'http://localhost:3000/api/downloadFile',
    //     type:'get',
    //     data:{
    //         key:props.sshKey,
    //         fileName:'ffmpeg.7z'
    //     },
    //     success(data) {
    //       const filename = 'ffmpeg.7z';

    //       const blob = new Blob([data], { type: 'application/octet-stream' });
    //       const url = window.URL.createObjectURL(blob);

    //       const a = document.createElement('a');
    //       a.href = url;
    //       a.download = filename;

    //       document.body.appendChild(a);
    //       a.click();
    //       document.body.removeChild(a);
    //     }
    //   });

        // const a = document.createElement('a');
        //   a.href = 'http://localhost:3000/api/daaaaaFile?sshKey=' + props.sshKey + '&fileName=terminal.jar';
        //   a.download = "terminal.jar";

        //   document.body.appendChild(a);
        //   console.log('开始');
        //   a.click();
        //   document.body.removeChild(a);


        // 创建一个新的 <a> 元素
        var a = document.createElement('a');

        // 设置 href 和 download 属性
        a.href = 'http://localhost:3000/api/download/ffmpeg.7z' + '?sshKey=' + props.sshKey + '&fileName=terminal.jar';
        a.download = "terminal.jar";

        // 添加 click 事件监听器以在下载开始时执行自定义语句
        a.addEventListener('click', function () {
            console.log('下载开始');
        });

        // 添加 load 事件监听器以在下载完成时执行自定义语句
        a.addEventListener('load', function () {
            console.log('下载完成');
            // 下载完成后从 body 中移除 <a> 元素
            document.body.removeChild(a);
        });

        // 将 <a> 元素附加到 body
        document.body.appendChild(a);

        // 通过编程方式触发 <a> 元素上的 click 事件
        a.click();



    }

    const demo2 = () => {
        $.ajax({
        url: 'http://localhost:3000/api/ls',
        type:'get',
        data:{
            sshKey:props.sshKey,
        },
        success(){
          
        }
      });
    }

    return {
        demo,
        demo2,

    }
  }


}
</script>

<style scoped>
.item-class {
  display: flex; 
  align-items: center; 
}

.errInfo{
  font-size: 12px;
  color: rgb(234, 80, 80);
  margin-top: 5px;
}
</style>