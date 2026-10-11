# kkTerminal

> 一个Web SSH连接终端
>
> 作者：[zyyzyykk](https://github.com/zyyzyykk/)
>
> 源代码：https://github.com/zyyzyykk/kkTerminal
>
> Docker仓库：https://hub.docker.com/repository/docker/zyyzyykk/kkterminal/general
>
> 预览：https://kkterminal.com
>
> 更新时间：2026-10-11
>

<p align="center"><a href="https://kkterminal.com" target="_blank" rel="noopener noreferrer"><img width="100" src="https://kkbapps.oss-cn-shanghai.aliyuncs.com/logo/terminal.svg" alt="kkterminal"></a></p>

<p align="center">
  <a href="https://hub.docker.com/repository/docker/zyyzyykk/kkterminal/general"><img src="https://img.shields.io/docker/pulls/zyyzyykk/kkterminal?logo=docker" alt="Docker Image"></a>
  <a href="https://www.oracle.com/cn/java/technologies/downloads/#java8-windows"><img src="https://img.shields.io/badge/jdk-21-orange?logo=openjdk&logoColor=%23e3731c" alt="JDK Version"></a>
  <a href="https://spring.io/projects/spring-boot"><img src="https://img.shields.io/badge/springboot-3.5.10-green?color=6db33f&logo=springboot" alt="SpringBoot Version"></a>
  <a href="https://cn.vuejs.org/"><img src="https://img.shields.io/badge/vue-3.x-green?color=42b883&logo=vue.js" alt="Vue Version"></a>
  <a href="https://www.apache.org/licenses/"><img src="https://img.shields.io/badge/licence-Apache-red?logo=apache&logoColor=%23D22128" alt="Apache Licence"></a>
  <a href="https://github.com/zyyzyykk/kkTerminal"><img src="https://img.shields.io/github/stars/zyyzyykk/kkterminal" alt="GitHub"></a>
</p>
<p align="center">简体中文 ｜ <a href="../en_US/README.md" >English</a></p>

### ⚡ 快速集成

```html
<iframe src="https://kkterminal.com" height="600px" width="800px" ></iframe>
```

### 🐳 Docker部署

1. 拉取最新镜像：

   ```bash
   docker pull zyyzyykk/kkterminal:latest
   ```

2. 创建并运行容器：

   ```bash
   docker run -d --name kkterminal \
   -p 3000:3000 \
   -e PASSWORD="" \
   -v /data/kkterminal:/data \
   zyyzyykk/kkterminal:latest
   ```

3. 配置Nginx反向代理：

   ```nginx
   # 部署在根路径
   location ^~ / {
   	proxy_pass http://127.0.0.1:3000;
   	proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
   	proxy_set_header X-Real-IP $remote_addr;
   	proxy_set_header Host $http_host;
   
   	proxy_http_version 1.1;
   	proxy_set_header Upgrade $http_upgrade;
   	proxy_set_header Connection "upgrade";
   	proxy_read_timeout 36000s;
   }
   ```

   ```nginx
   # 部署在子路径
   location ^~ /subpath/ {
   	proxy_pass http://127.0.0.1:3000/;
   	proxy_set_header X-Forwarded-Prefix /subpath;
   	proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
   	proxy_set_header X-Real-IP $remote_addr;
   	proxy_set_header Host $http_host;
   
   	proxy_http_version 1.1;
   	proxy_set_header Upgrade $http_upgrade;
   	proxy_set_header Connection "upgrade";
   	proxy_read_timeout 36000s;
   }
   ```

4. 在浏览器中访问：`http://<服务器IP>:3000/[子路径]`

### 👀 预览

访问此网址：https://kkterminal.com

![Connect](https://kkbapps.oss-cn-shanghai.aliyuncs.com/terminal/3.8.8/zh/Connect.png)

![Preference](https://kkbapps.oss-cn-shanghai.aliyuncs.com/terminal/3.8.8/zh/Preference.png)

![File](https://kkbapps.oss-cn-shanghai.aliyuncs.com/terminal/3.8.8/zh/File.png)

![Editor](https://kkbapps.oss-cn-shanghai.aliyuncs.com/terminal/3.8.8/zh/Editor.png)

[**更多模块预览**](./MODULE.md)

### 💡 功能说明

1. 点击左上角的终端图标，选择 `Connect` 以建立SSH连接
2. 使用 `<iframe>` 标签将终端快速嵌入第三方网站
3. 通过 [URL参数](./PARAMS.md) 自定义终端设置
4. i18n国际化中/英文切换
5. 可部署在本地电脑或远程服务器
6. 自适配窗口尺寸和中文输入
7. 配置终端背景色/前景色、字体大小、光标样式等偏好
8. 自动/手动重启
9. 文本选中自动复制和单击右键粘贴
10. 打开文件管理模块查看、解压、上传和下载文件/文件夹
11. 使用快捷键执行文件操作，包括多选、全选、复制、粘贴、剪切、反选、打开、删除等
12. 浏览并编辑文件，按 ``Ctrl+S`` 将更改保存到远程服务器
13. 通过 [命令代码](./CMDCODE.md) 执行自定义工作流
14. 记录和回放终端操作
15. 在不同设备间同步数据
16. 提供高级功能：协作、监控、Docker
17. 需要密码验证才可访问
18. 启用终端集成

### 👨‍💻 更新记录

##### zyyzyykk/kkterminal:3.8.8

- 新增终端集成功能
- 支持子路径部署
- 文件管理模块新增跟随终端目录功能
- 更新Docker应用商店
- 文件管理模块新增目录导航下拉菜单
- 重构云端数据存储路径
- 优化界面显示
- 增强部分功能

##### zyyzyykk/kkterminal:3.8.0

- Docker模块新增容器详情显示和应用商店更新
- 增强非安全上下文和低版本的兼容性
- 升级项目jdk和springboot版本
- 重构部分代码与优化界面显示

##### zyyzyykk/kkterminal:3.7.6

- 支持更多URL参数
- 优化文件传输流程
- 新增访问认证功能
- 用户命令代码文件模块新增API
- Docker模块新增查看容器详细信息功能
- 修复Docker模块数据请求异常的bug
- 优化界面显示
- 增强部分功能

[**历史更新记录**](./UPDATE.md)

### 🧬 架构

```markdown
+---------+     http      +-------------+    ssh     +---------------+
| browser | <===========> | kk Terminal | <========> | remote server |
+---------+   websocket   +-------------+    sftp    +---------------+
Vue + Xterm              SpringBoot + SSHJ                Linux OS    
```

### 🛡️ 隐私与安全

> [!Warning]
>
> **🔐 零知识加密**
>
> 云端同步数据采用零知识加密进行保护，因此服务端无法获取原始内容
>
> **⏱️ 临时存储**
>
> 文件和私钥仅被临时存储，并在传输或登录完成后删除
>
> **⚠️ 免责声明**
>
> 本项目按”原样“提供，仅用于个人学习和研究，严禁任何非法使用
