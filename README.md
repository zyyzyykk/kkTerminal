# kkTerminal

> kkTerminal，一个web终端实现ssh远程连接服务器
>
> kkTerminal, a web-based terminal for SSH remote server connection.
>
> 作者：[zyyzyykk](https://github.com/zyyzyykk/)
>
> 源码：http://git.kkbapps.com/kk/kkTerminal
>
> 预览：https://ssh.kkbpro.com/	(国内服务器访问)	或	https://ssh.kkbapps.com/	(国外服务器访问)
>
> 更新时间：2023-11-14
>

### **⚡** 快速引入

在html网页中使用 iframe 标签实现快速引入：

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
docker run -d --name kkterminal -p 3000:3000 -e TITLE="kkbapps" zyyzyykk/kkterminal
```

### 🛸 预览：

访问以下网址：

- 🌐 国内服务器访问：https://ssh.kkbpro.com/
- 🌍 国外服务器访问：https://ssh.kkbapps.com/

![kkterminal-概览](https://img.kkbapps.com/kkterminal-show.png)

![kkterminal-功能](https://img.kkbapps.com/kkterminal-2.jpg)

![kkterminal-样式设置](https://img.kkbapps.com/kkterminal-3.jpg)

### 💡 功能说明

1.kkTerminal是一个web端ssh远程连接服务器的工具，点击右上角的终端图标，打开连接设置进行ssh连接设置

2.支持在网页中使用 iframe 标签引入，可快速集成到第三方网站中；

3.支持窗口大小自适应

4.支持自定义偏好设置，可选择终端的背/前景色，字体字号，光标显示样式

5.支持重启，当修改ssh配置或自定义样式配置后，会自动重启；也可手动重启，适用于长时间无交互致使连接断开的情况

6.支持复制粘贴：复制同git终端，选中文本会进行自动复制；粘贴同cmd终端，鼠标右键单击进行粘贴（需要浏览器打开权限）

7.文件上传下载的功能（**后续，暂未实现**）

### 👨‍💻 更新记录

##### zyyzyykk/kkterminal:2.5：latest

- 新增长时间无交互导致断开连接的提示

- 修复了终端窗口大小变化时由于命令过长导致的展示问题

##### zyyzyykk/kkterminal:2.2 ：

- 修复重启终端后出现的粘贴bug

- 更改了可选择的字体样式

##### zyyzyykk/kkterminal:2.0 ：

增加在终端内复制粘贴功能：

- 复制：同git终端，选中文本会进行自动复制
- 粘贴：同cmd终端，鼠标右键单击进行粘贴（需要浏览器打开权限）

##### zyyzyykk/kkterminal:1.0 ：

提交官方镜像

### 🏘️ 关于此项目

作者：[zyyzyykk](https://github.com/zyyzyykk/)

欢迎对此项目提出宝贵的意见或建议，也可以加入我们一起进行此项目的维护与开发
