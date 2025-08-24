# kkTerminal

> kkTerminal，一个强大的Web SSH连接终端
>
> 作者：[zyyzyykk](https://github.com/zyyzyykk/)
>
> 源代码：https://github.com/zyyzyykk/kkTerminal
>
> Docker仓库地址：https://hub.docker.com/repository/docker/zyyzyykk/kkterminal/general
>
> 预览：https://ssh.kkbpro.com/
>
> 更新时间：2025-08-24
>

<p align="center"><a href="https://ssh.kkbpro.com/" target="_blank" rel="noopener noreferrer"><img width="100" src="https://kkbapps.oss-cn-shanghai.aliyuncs.com/logo/terminal.svg" alt="kkterminal"></a></p>

<p align="center">
  <a href="https://hub.docker.com/repository/docker/zyyzyykk/kkterminal/general"><img src="https://img.shields.io/docker/pulls/zyyzyykk/kkterminal?logo=docker" alt="Docker Image"></a>
  <a href="https://www.oracle.com/cn/java/technologies/downloads/#java8-windows"><img src="https://img.shields.io/badge/jdk-1.8-orange?logo=openjdk&logoColor=%23e3731c" alt="JDK Version"></a>
  <a href="https://spring.io/projects/spring-boot"><img src="https://img.shields.io/badge/springboot-2.7.15-green?color=6db33f&logo=springboot" alt="SpringBoot Version"></a>
  <a href="https://cn.vuejs.org/"><img src="https://img.shields.io/badge/vue-3.x-green?color=42b883&logo=vue.js" alt="Vue Version"></a>
  <a href="https://www.apache.org/licenses/"><img src="https://img.shields.io/badge/licence-Apache-red?logo=apache&logoColor=%23D22128" alt="Apache Licence"></a>
  <a href="https://github.com/zyyzyykk/kkTerminal"><img src="https://img.shields.io/github/stars/zyyzyykk/kkterminal" alt="GitHub"></a>
</p>
<p align="center">简体中文 ｜ <a href="../en_US/README.md" >English</a></p>

### ⚡ 快速集成

在html网页中使用 `iframe` 标签实现快速集成：

```html
<iframe src="https://ssh.kkbpro.com/" height="600px" width="800px" ></iframe>
```

### 🐳 Docker部署

1. 拉取镜像：

```bash
docker pull zyyzyykk/kkterminal
```

2. 创建并运行容器：

```bash
docker run -d --name kkterminal \
-p 3000:3000 \
-e TITLE="kkTerminal" \
-e AESKEY="P5P1SIqVe6kaOxMX" \
-v /data/kkterminal/cloud:/cloud \
zyyzyykk/kkterminal
```

3. 在浏览器中访问：`http://服务器ip:3000/`

### 👀 预览

访问此网址：https://ssh.kkbpro.com/

![Connect](https://kkbapps.oss-cn-shanghai.aliyuncs.com/terminal/3.6.8/zh/Connect.png)

![Preference](https://kkbapps.oss-cn-shanghai.aliyuncs.com/terminal/3.6.8/zh/Preference.png)

![File](https://kkbapps.oss-cn-shanghai.aliyuncs.com/terminal/3.6.8/zh/File.png)

![Editor](https://kkbapps.oss-cn-shanghai.aliyuncs.com/terminal/3.6.8/zh/Editor.png)

[**更多模块预览**](./MODULE.md)

### 💡 功能说明

1. kkTerminal是一个强大的Web SSH连接终端工具，点击左上角的终端图标，选择连接设置进行ssh连接
2. 支持在网页中使用 `iframe` 标签引入，可快速集成到第三方网站中
3. 支持通过 [URL参数](./PARAMS.md) 自定义终端配置
4. 支持i18n国际化，支持中/英文语言切换
5. 支持本地PC端部署，启动时会自动打开浏览器窗口
6. 支持窗口大小自适应，支持中文输入
7. 支持偏好设置，例如终端的背/前景色、字体字号、光标显示样式等
8. 支持自动/手动重启
9. 支持复制粘贴：
   - 复制同 `git` 终端，选中文本会自动进行复制
   - 粘贴同 `cmd` 终端，单击鼠标右键进行粘贴（需要浏览器打开权限）
10. 支持文件管理，打开文件管理模块查看、解压、上传与下载文件/文件夹
11. 支持文件多选/全选、复制粘贴、剪切、选择切换、打开等快捷键操作
12. 支持文件的浏览与编辑，修改文件后使用 `ctrl+s` 保存至远程服务器
13. 支持 [终端代码](./TCODE.md) 执行自定义工作流
14. 支持操作录像、多端同步功能
15. 支持协作、监控、Docker功能

### 👨‍💻 更新记录

##### zyyzyykk/kkterminal:3.6.8：latest

- 新增文件传输列表
- 新增Docker应用商店
- 新增日志记录功能
- 优化整体加密逻辑
- 部分对话框支持左右拉伸调整宽度
- 优化编辑器打开与保存文件的编码格式
- 用户终端代码新增保留值与文件模块API
- 重构部分代码与优化界面显示

##### zyyzyykk/kkterminal:3.6.0：

- 新增高级——协作功能
- 新增高级——监控功能
- 新增高级——Docker功能
- 优化打包体积与界面显示

[**历史更新记录**](./UPDATE.md)

### 🧬 架构

```markdown
+---------+     http      +-------------+    ssh     +---------------+
| browser | <===========> | kk Terminal | <========> | remote server |
+---------+   websocket   +-------------+    sftp    +---------------+
Vue + Xterm              SpringBoot + SSHJ                Linux OS    
```

### 🏘️ 关于此项目

> [!Important] 
>
> kkTerminal不会主动记录密码、文件、命令等任何与远程服务器相关的信息

作者：[zyyzyykk](https://github.com/zyyzyykk/)

欢迎对此项目提出宝贵的意见或建议，也可以加入我们一起进行此项目的维护与开发

### 🌟 点赞

[![Stars](https://starchart.cc/zyyzyykk/kkTerminal.svg?variant=adaptive)](https://starchart.cc/zyyzyykk/kkTerminal)
