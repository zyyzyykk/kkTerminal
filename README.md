# kkTerminal

> A terminal for Web SSH connection
>
> Author: [zyyzyykk](https://github.com/zyyzyykk/)
>
> Source Code: https://github.com/zyyzyykk/kkTerminal
>
> Docker Hub: https://hub.docker.com/repository/docker/zyyzyykk/kkterminal/general
>
> Preview: https://kkterminal.com
>
> Update Time: 2026-10-11
>

<p align="center"><a href="https://kkterminal.com" target="_blank" rel="noopener noreferrer"><img width="100" src="https://kkbapps.oss-cn-shanghai.aliyuncs.com/logo/terminal.svg" alt="kkTerminal"></a></p>

<p align="center">
  <a href="https://hub.docker.com/repository/docker/zyyzyykk/kkterminal/general"><img src="https://img.shields.io/docker/pulls/zyyzyykk/kkterminal?logo=docker" alt="Docker Image"></a>
  <a href="https://www.oracle.com/cn/java/technologies/downloads/#java8-windows"><img src="https://img.shields.io/badge/jdk-21-orange?logo=openjdk&logoColor=%23e3731c" alt="JDK Version"></a>
  <a href="https://spring.io/projects/spring-boot"><img src="https://img.shields.io/badge/springboot-3.5.10-green?color=6db33f&logo=springboot" alt="SpringBoot Version"></a>
  <a href="https://cn.vuejs.org/"><img src="https://img.shields.io/badge/vue-3.x-green?color=42b883&logo=vue.js" alt="Vue Version"></a>
  <a href="https://www.apache.org/licenses/"><img src="https://img.shields.io/badge/licence-Apache-red?logo=apache&logoColor=%23D22128" alt="Apache Licence"></a>
  <a href="https://github.com/zyyzyykk/kkTerminal"><img src="https://img.shields.io/github/stars/zyyzyykk/kkterminal" alt="GitHub"></a>
</p>
<p align="center">English ｜ <a href="./doc/zh_CN/README.md" >简体中文</a></p>

### ⚡ Quick Integration

```html
<iframe src="https://kkterminal.com" height="600px" width="800px" ></iframe>
```

### 🐳 Deploy with Docker

1. Pull the latest image:

   ```bash
   docker pull zyyzyykk/kkterminal:latest
   ```

2. Create and run a container:

   ```bash
   docker run -d --name kkterminal \
   -p 3000:3000 \
   -e PASSWORD="" \
   -v /data/kkterminal:/data \
   zyyzyykk/kkterminal:latest
   ```

3. Configure the Nginx reverse proxy:

   ```nginx
   # Deploy under the root path
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
   # Deploy under a subpath
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

4. Accessing in browser: `http://<server ip>:3000/[subpath]`

### 👀 Preview

Visit this website: https://kkterminal.com

![Connect](https://kkbapps.oss-cn-shanghai.aliyuncs.com/terminal/3.8.8/en/Connect.png)

![Preference](https://kkbapps.oss-cn-shanghai.aliyuncs.com/terminal/3.8.8/en/Preference.png)

![File](https://kkbapps.oss-cn-shanghai.aliyuncs.com/terminal/3.8.8/en/File.png)

![Editor](https://kkbapps.oss-cn-shanghai.aliyuncs.com/terminal/3.8.8/en/Editor.png)

[**More Modules Preview**](./doc/en_US/MODULE.md)

### 💡 Function Description

1. Click the upper-left terminal icon and select `Connect` to establish an SSH connection
2. Use the `<iframe>` tag to quickly embed the terminal into third-party websites
3. Customize terminal settings via [URL Parameters](./doc/en_US/PARAMS.md)
4. Switch between Chinese and English for internationalization
5. Deploy on-premises on local PCs or remote servers
6. Adapt to window sizes and Chinese input
7. Configure preferences such as terminal background/foreground color, font size, cursor style, etc
8. Restart automatically or manually
9. Copy selected text automatically and paste with right-click
10. Manage files by opening the File module to browse, decompress, upload, and download files or folders
11. Perform file operations with shortcut keys, including multi-select, select all, copy, paste, cut, invert selection, open, delete, etc
12. Browse and edit files, then press `Ctrl+S` to save changes to the remote server
13. Execute customized workflows via [Command Code](./doc/en_US/CMDCODE.md)
14. Record and replay terminal operations
15. Synchronize data between different devices
16. Provide advanced features: Cooperate, Monitor and Docker
17. Require password verification for access
18. Enable shell integration

### 👨‍💻 Update Records

##### zyyzyykk/kkterminal:3.8.8

- Add shell integration function
- Support subpath deployment
- File module adds follow terminal directory function
- Update some apps in docker appstore
- Add directory navigation dropdown in File module
- Refactoring cloud data storage path
- Optimize webpage display
- Enhance some functions

##### zyyzyykk/kkterminal:3.8.0

- Docker module adds container details display and app store updates
- Enhance compatibility of non-secure context and lower versions
- Upgrade project jdk and springboot version
- Refactoring some codes and optimizing UI display

##### zyyzyykk/kkterminal:3.7.6

- Support more URL parameters
- Optimize file transfer process
- Add access verify function
- User Command Code file module adds API
- Docker module adds viewing container details
- Fix bug of data request exception in Docker module
- Optimize webpage display
- Enhance some functions

[**History Update Records**](./doc/en_US/UPDATE.md)

### 🧬 Architecture

```markdown
+---------+     http      +-------------+    ssh     +---------------+
| browser | <===========> | kk Terminal | <========> | remote server |
+---------+   websocket   +-------------+    sftp    +---------------+
Vue + Xterm              SpringBoot + SSHJ                Linux OS    
```

### 🛡️ Privacy and Security

> [!Warning]
>
> **🔐 Zero-Knowledge Encryption**
>
> Cloud-synchronized data is protected with zero-knowledge encryption, so the service provider cannot access the original content.
>
> **⏱️ Temporary Storage**
>
> Files and private keys are stored only temporarily and are deleted after transfer or login is completed.
>
> **⚠️ Disclaimer**
>
> This project is provided on an "AS IS" basis for personal learning and research purposes only and any illegal use is prohibited.
