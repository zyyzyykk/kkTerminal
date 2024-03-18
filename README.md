# kkTerminal

> kkTerminal，一个web终端实现ssh远程连接服务器
>
> kkTerminal, a web-based terminal for SSH remote server connection.
>
> 作者：[zyyzyykk](https://github.com/zyyzyykk/)
>
> 源代码：https://github.com/zyyzyykk/kkTerminal
>
> docker仓库地址：https://hub.docker.com/repository/docker/zyyzyykk/kkterminal/general
>
> 预览：https://ssh.kkbpro.com/	(国内服务器访问)	或	https://ssh.kkbapps.com/	(国外服务器访问)
>
> 更新时间：2024-03-12
>

### **⚡** 快速引入

在html网页中使用 `iframe` 标签实现快速引入：

```html
<iframe src="https://ssh.kkbpro.com/" height="400px" width="600px" ></iframe>
```

### 💪 使用docker部署

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

- 🌐 国内服务器访问：https://ssh.kkbpro.com/
- 🌍 国外服务器访问：https://ssh.kkbapps.com/

![kkterminal](https://img.kkbapps.com/terminal/303-1.png)

![kkterminal](https://img.kkbapps.com/terminal/303-2.png)

![kkterminal](https://img.kkbapps.com/terminal/303-3.png)

![kkterminal](https://img.kkbapps.com/terminal/303-4.png)

### 💡 功能说明

1.kkTerminal是一个web端ssh远程连接服务器的工具，点击左上角的终端图标，选择连接设置进行ssh连接

2.支持在网页中使用 `iframe` 标签引入，可快速集成到第三方网站中

3.支持窗口大小自适应，支持中文输入

4.支持自定义偏好设置，可选择终端的背/前景色、字体字号、光标显示样式

5.支持重启：当修改ssh连接设置或自定义偏好设置后会自动重启，也可在ssh连接断开后手动进行重启

6.支持复制粘贴：

- 复制同 `git` 终端，选中文本会自动进行复制
- 粘贴同 `cmd` 终端，单击鼠标右键进行粘贴（需要浏览器打开权限）

7.支持文件管理，打开文件管理模块进行文件的查看、上传与下载

8.支持文件的浏览与编辑，修改文件后使用 `ctrl+s` 保存至远程服务器

### 👨‍💻 更新记录

> 核心功能全部开发完成，后续更新主要针对bug修复、逻辑优化等方面

##### zyyzyykk/kkterminal:3.0.6：latest

- 解决拖拽上传无法识别文件夹的问题，最终修改为不支持拖拽上传文件夹或空文件

##### zyyzyykk/kkterminal:3.0.5：

- 优化文本编辑器撤销历史
- 完善请求异常错误处理

##### zyyzyykk/kkterminal:3.0.3：

- 修改滚动条样式，优化文件大小属性展示
- 修复bug：部分文件权限属性错误、终端中文输入混乱

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
