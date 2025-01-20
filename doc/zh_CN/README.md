# kkTerminal

> kkTerminal，一个Web SSH连接终端
>
> 作者：[zyyzyykk](https://github.com/zyyzyykk/)
>
> 源代码：https://github.com/zyyzyykk/kkTerminal
>
> docker仓库地址：https://hub.docker.com/repository/docker/zyyzyykk/kkterminal/general
>
> 预览：https://ssh.kkbpro.com/
>
> 更新时间：2025-01-10
>

<p align="center"><a href="https://ssh.kkbpro.com/" target="_blank" rel="noopener noreferrer"><img width="100" src="https://kkbapps.oss-cn-shanghai.aliyuncs.com/logo/terminal.png" alt="kkterminal"></a></p>

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
<iframe src="https://ssh.kkbpro.com/" height="400px" width="600px" ></iframe>
```

### 🐳 使用docker部署

1. 拉取镜像：

```bash
docker pull zyyzyykk/kkterminal
```

2. 创建并运行容器，进行端口映射：`-p 端口号:3000`

```bash
docker run -d --name kkterminal -p 3000:3000 zyyzyykk/kkterminal
```

3. 自定义艺术字：`-e TITLE="自定义艺术字"`

```bash
docker run -d --name kkterminal -p 3000:3000 -e TITLE="kkbpro" zyyzyykk/kkterminal
```

4. 在浏览器中访问：`http://服务器ip:3000/`

### 🛸 预览

访问以下网址：https://ssh.kkbpro.com/

![Connect](https://kkbapps.oss-cn-shanghai.aliyuncs.com/terminal/3.5.3/zh/Connect.png)

![Preference](https://kkbapps.oss-cn-shanghai.aliyuncs.com/terminal/3.5.3/zh/Preference.png)

![File](https://kkbapps.oss-cn-shanghai.aliyuncs.com/terminal/3.5.3/zh/File.png)

![Editor](https://kkbapps.oss-cn-shanghai.aliyuncs.com/terminal/3.5.3/zh/Editor.png)

[**更多模块预览**](./MODULE.md)

### 💡 功能说明

1. kkTerminal是一个Web SSH连接终端工具，点击左上角的终端图标，选择连接设置进行ssh连接

2. 支持在网页中使用 `iframe` 标签引入，可快速集成到第三方网站中

3. 支持通过 [URL参数](./PARAMS.md) 自定义终端配置

4. 支持i18n国际化，支持中/英文语言切换

5. 支持本地PC端部署，启动时会自动打开浏览器窗口

6. 支持窗口大小自适应，支持中文输入

7. 支持自定义偏好设置，可选择终端的背/前景色、字体字号、光标显示样式、启用TCode等

8. 支持重启：当修改ssh连接设置或自定义偏好设置后会自动重启，也可在ssh连接断开后手动进行重启

9. 支持复制粘贴：

   - 复制同 `git` 终端，选中文本会自动进行复制

   - 粘贴同 `cmd` 终端，单击鼠标右键进行粘贴（需要浏览器打开权限）


9. 支持文件管理，打开文件管理模块进行文件/文件夹的查看、解压、上传与下载等
10. 支持文件多选/全选、复制粘贴、剪切、选择切换、打开等快捷键操作
11. 支持文件的浏览与编辑，修改文件后使用 `ctrl+s` 保存至远程服务器
12. 支持 [TCode (终端代码)](./TCODE.md)，能够通过自定义TCode实现类似Shell脚本的自动化Workflow
13. 支持操作录像、云端同步功能

### 👨‍💻 更新记录

##### zyyzyykk/kkterminal:3.5.3：latest

- 新增url参数自定义终端配置
- 修复多窗口重启的bug
- 新增操作录像和云端同步功能
- 重构部分代码逻辑

##### zyyzyykk/kkterminal:3.4.9：

- 新增私钥方式登录
- 新增文件编码自动识别与保存切换
- 新增文件语言模式选择
- 新增文件缩进选择和内容复制

##### zyyzyykk/kkterminal:3.4.5：

- 新增文件权限编辑功能
- 修复了输入框上传时更改目录导致文件夹上传错误的bug
- 优化界面显示

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

赞助商：[<img src="https://api.gitsponsors.com/api/badge/img?id=704828551" height="20">](https://api.gitsponsors.com/api/badge/link?p=ZeY5IHF8NCpCNujm6upYNHtOtIhaIz5VvgUnIlPWJR9Ta0EgrNeq3P+SXzdv9I03XbxmzJe/sogYZPuCzSoEB2C+x9YgzNCl+5N/hx6Bn7wY7k/ajlj7EJJwQAJoiR3uK9o26so7BlUobWU0htzAlg==)

欢迎对此项目提出宝贵的意见或建议，也可以加入我们一起进行此项目的维护与开发

### 🌟 点赞

[![Stars](https://starchart.cc/zyyzyykk/kkTerminal.svg?variant=adaptive)](https://starchart.cc/zyyzyykk/kkTerminal)
