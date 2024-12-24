# kkTerminal

> kkTerminal，一个Web SSH连接终端
>
> 作者：[zyyzyykk](https://github.com/zyyzyykk/)
>
> 源代码：https://github.com/zyyzyykk/kkTerminal
>
> docker仓库地址：https://hub.docker.com/repository/docker/zyyzyykk/kkterminal/general
>
> 预览：http://ssh.kkbpro.com/	(国内服务器访问)	或	http://ssh.kkbapps.com/	(国外服务器访问)
>
> 更新时间：2024-12-24
>

<p align="center"><a href="https://ssh.kkbpro.com/" target="_blank" rel="noopener noreferrer"><img width="100" src="http://img.kkbapps.com/logo/terminal.png" alt="kkterminal"></a></p>

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
<iframe src="http://ssh.kkbpro.com/" height="400px" width="600px" ></iframe>
```

### 🐳 使用docker部署

1.拉取镜像：

```bash
docker pull zyyzyykk/kkterminal
```

2.创建并运行容器，进行端口映射：`-p 端口号:3000`

```bash
docker run -d --name kkterminal -p 3000:3000 zyyzyykk/kkterminal
```

3.挂载数据卷：`-v 数据卷路径:/data`

```bash
docker run -d --name kkterminal -p 3000:3000 -v /data:/data zyyzyykk/kkterminal
```

4.自定义艺术字：`-e TITLE="自定义艺术字"`

```bash
docker run -d --name kkterminal -p 3000:3000 -e TITLE="kkbpro" zyyzyykk/kkterminal
```

5.在浏览器中访问：`http://服务器ip:3000/`

### 🛸 预览

访问以下网址：

- 🌐 国内服务器访问：http://ssh.kkbpro.com/
- 🌍 国外服务器访问：http://ssh.kkbapps.com/

![Connect](http://img.kkbapps.com/terminal/Connect-zh-3.4.2.png)

![Preference](http://img.kkbapps.com/terminal/Preference-zh-3.4.2.png)

![File](http://img.kkbapps.com/terminal/File-zh-3.4.2.png)

![Editor](http://img.kkbapps.com/terminal/Editor-zh-3.4.2.png)

[**更多模块预览**](./MODULE.md)

### 💡 功能说明

1. kkTerminal是一个Web SSH连接终端工具，点击左上角的终端图标，选择连接设置进行ssh连接

2. 支持在网页中使用 `iframe` 标签引入，可快速集成到第三方网站中

3. 支持i18n国际化，支持中/英文语言切换

4. 支持本地PC端部署，启动时会自动打开浏览器窗口

5. 支持窗口大小自适应，支持中文输入

6. 支持自定义偏好设置，可选择终端的背/前景色、字体字号、光标显示样式、启用TCode

7. 支持重启：当修改ssh连接设置或自定义偏好设置后会自动重启，也可在ssh连接断开后手动进行重启

8. 支持复制粘贴：

   - 复制同 `git` 终端，选中文本会自动进行复制

   - 粘贴同 `cmd` 终端，单击鼠标右键进行粘贴（需要浏览器打开权限）


9. 支持文件管理，打开文件管理模块进行文件/文件夹的查看、解压、上传与下载

10. 支持文件多选/全选、复制粘贴、剪切、选择切换、打开等快捷键操作

11. 支持文件的浏览与编辑，修改文件后使用 `ctrl+s` 保存至远程服务器

12. 支持 [TCode (终端代码)](./TCODE.md)，能够通过自定义TCode实现类似Shell脚本的自动化Workflow

### 👨‍💻 更新记录

##### zyyzyykk/kkterminal:3.4.5：latest

- 新增文件权限编辑功能
- 修复了输入框上传时更改目录导致文件夹上传错误的bug
- 优化界面显示

##### zyyzyykk/kkterminal:3.4.2：

- 实现i18n国际化，支持中/英文语言切换
- 重构了部分代码逻辑

##### zyyzyykk/kkterminal:3.4.0：

- 新增SSH连接配置与自定义TCode的删除功能
- 修复了文件模块文件夹粘贴失败的bug
- 优化了网络资源与页面显示

[**历史更新记录**](./UPDATE.md)

### 🧬 架构

```markdown
+---------+     http      +-------------+    ssh     +---------------+
| browser | <===========> | kk Terminal | <========> | remote server |
+---------+   websocket   +-------------+    sftp    +---------------+
Vue + Xterm              SpringBoot + SSHJ                Linux OS    
```

### 🏘️ 关于此项目

作者：[zyyzyykk](https://github.com/zyyzyykk/)

欢迎对此项目提出宝贵的意见或建议，也可以加入我们一起进行此项目的维护与开发

### 🌟 点赞

[![Stars](https://starchart.cc/zyyzyykk/kkTerminal.svg?variant=adaptive)](https://starchart.cc/zyyzyykk/kkTerminal)
