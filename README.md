# kkTerminal

> kkTerminal，一个web终端实现ssh远程连接服务器
>
> kkTerminal, a web-based terminal for SSH remote server connection.
>
> 作者：[zyyzyykk](https://github.com/zyyzyykk/)
>
> 源码：http://git.kkbapps.com/kk/kkTerminal
>
> docker仓库地址：https://hub.docker.com/repository/docker/zyyzyykk/kkterminal/general
>
> 预览：https://ssh.kkbpro.com/	(国内服务器访问)	或	https://ssh.kkbapps.com/	(国外服务器访问)
>
> 更新时间：2024-01-29
>

### **⚡** 快速引入

在html网页中使用 `iframe` 标签实现快速引入：

```html
<iframe src="https://ssh.kkbpro.com/" height="400px" width="600px" ></iframe>
```

### 💪 使用docker部署

1.拉取镜像：

```sh
docker pull zyyzyykk/kkterminal
```

2.创建并运行容器，默认监听3000端口：

```sh
docker run -d --name kkterminal -p 3000:3000 zyyzyykk/kkterminal
```

3.在浏览器中访问：`http://你的服务器ip:3000/`

4.自定义艺术字：`-e TITLE="自定义艺术字"`

```sh
docker run -d --name kkterminal -p 3000:3000 -e TITLE="kkbpro" zyyzyykk/kkterminal
```

### 🛸 预览

访问以下网址：

- 🌐 国内服务器访问：https://ssh.kkbpro.com/
- 🌍 国外服务器访问：https://ssh.kkbapps.com/

![kkterminal](https://img.kkbapps.com/terminal/2.png)

![kkterminal](https://img.kkbapps.com/terminal/3.png)

![kkterminal](https://img.kkbapps.com/terminal/4.png)

![kkterminal](https://img.kkbapps.com/terminal/5.png)

### 💡 功能说明

1.kkTerminal是一个web端ssh远程连接服务器的工具，点击右上角的终端图标，打开连接设置进行ssh连接设置

2.支持在网页中使用 `iframe` 标签引入，可快速集成到第三方网站中

3.支持窗口大小自适应，支持中文输入

4.支持自定义偏好设置，可选择终端的背/前景色、字体字号、光标显示样式

5.支持重启，当修改ssh配置或自定义样式配置后，会自动重启；也可手动重启，适用于ssh连接断开的情况

6.支持复制粘贴：复制同 `git` 终端，选中文本会进行自动复制；粘贴同 `cmd` 终端，鼠标右键单击进行粘贴（需要浏览器打开权限）

7.支持文件管理，打开文件管理模块进行文件的查看、上传与下载

8.支持文件的浏览与编辑，修改文件后使用 `ctrl+s` 保存至远程服务器

### 👨‍💻 更新记录

##### zyyzyykk/kkterminal:2.9.5：latest

- 修复了已知bug
- 支持中文输入
- 新增文件的浏览与编辑功能，修改文件后使用 `ctrl+s` 保存至远程服务器

##### zyyzyykk/kkterminal:2.9.2：

- 修改提示语内容
- 对弹窗模块进行优化，打开弹窗后可继续操作终端
- 新增连接配置的保存与导入，可快速切换连接多个远程服务器

##### [历史更新记录](./UPDATE.md)

### 🧬 架构

```markdown
+---------+     http      +-------------+    ssh     +---------------+
| browser | <===========> | kk Terminal | <========> | remote server |
+---------+   websocket   +-------------+    sftp    +---------------+
Vue + xterm              SpringBoot + sshj
```

### 🏘️ 关于此项目

作者：[zyyzyykk](https://github.com/zyyzyykk/)

欢迎对此项目提出宝贵的意见或建议，也可以加入我们一起进行此项目的维护与开发
