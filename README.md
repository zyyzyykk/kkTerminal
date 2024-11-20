# kkTerminal

> kkTerminal, a terminal for Web SSH connection
>
> Author: [zyyzyykk](https://github.com/zyyzyykk/)
>
> Source Code: https://github.com/zyyzyykk/kkTerminal
>
> Docker Hub Address: https://hub.docker.com/repository/docker/zyyzyykk/kkterminal/general
>
> Preview: https://ssh.kkbpro.com/	(For domestic servers)	or	https://ssh.kkbapps.com/	(For foreign servers)
>
> Update Time: 2024-11-20
>

<p align="center"><a href="https://ssh.kkbpro.com/" target="_blank" rel="noopener noreferrer"><img width="100" src="http://img.kkbapps.com/logo/terminal.png" alt="kkTerminal"></a></p>

<p align="center">
  <a href="https://hub.docker.com/repository/docker/zyyzyykk/kkterminal/general"><img src="https://img.shields.io/docker/pulls/zyyzyykk/kkterminal?logo=docker" alt="Docker Image"></a>
  <a href="https://www.oracle.com/cn/java/technologies/downloads/#java8-windows"><img src="https://img.shields.io/badge/jdk-1.8-orange?logo=openjdk&logoColor=%23e3731c" alt="JDK Version"></a>
  <a href="https://spring.io/projects/spring-boot"><img src="https://img.shields.io/badge/springboot-2.7.15-green?color=6db33f&logo=springboot" alt="SpringBoot Version"></a>
  <a href="https://cn.vuejs.org/"><img src="https://img.shields.io/badge/vue-3.x-green?color=42b883&logo=vue.js" alt="Vue Version"></a>
  <a href="https://www.apache.org/licenses/"><img src="https://img.shields.io/badge/licence-Apache-red?logo=apache&logoColor=%23D22128" alt="Apache Licence"></a>
  <a href="https://github.com/zyyzyykk/kkTerminal"><img src="https://img.shields.io/github/stars/zyyzyykk/kkterminal" alt="GitHub"></a>
</p>
<p align="center">English ｜ <a href="./doc/zh_CN/README.md" >简体中文</a></p>

### ⚡ Quick integration

Using the `iframe` tag in HTML web pages to quick integration:

```html
<iframe src="https://ssh.kkbpro.com/" height="400px" width="600px" ></iframe>
```

### 🐳 Deploy with Docker

1.Pull image:

```bash
docker pull zyyzyykk/kkterminal
```

2.Create and run a container for port mapping: `-p port:3000`

```bash
docker run -d --name kkterminal -p 3000:3000 zyyzyykk/kkterminal
```

3.Mount data volume: `-v path:/data`

```bash
docker run -d --name kkterminal -p 3000:3000 -v /data:/data zyyzyykk/kkterminal
```

4.Custom art word: `-e TITLE="ArtWord"`

```bash
docker run -d --name kkterminal -p 3000:3000 -e TITLE="kkbpro" zyyzyykk/kkterminal
```

5.Accessing in browser: `http://server-ip:3000/`

### 🛸 Preview

Visit the following website:

- 🌐 For domestic servers: https://ssh.kkbpro.com/
- 🌍 For foreign servers: https://ssh.kkbapps.com/

![Connect](http://img.kkbapps.com/terminal/Connect-3.3.3.png)

![Preference](http://img.kkbapps.com/terminal/Preference-3.3.3.png)

![File](http://img.kkbapps.com/terminal/File-3.3.7.png)

![Editor](http://img.kkbapps.com/terminal/Editor-3.3.3.png)

[**More Module Previews**](./doc/en_US/MODULE.md)

### 💡 Function Description

1.kkTerminal is a terminal for Web SSH connection. Click on the terminal icon in the upper left corner and select the connection settings to establish an SSH connection

2.Support the use of `iframe` tags in web pages for quick integration into third-party websites

3.Supports local PC deployment, automatically opens browser window upon startup

4.Support adaptive window size and Chinese input

5.Support custom preference settings, allowing users to choose the background/foreground color, font size, cursor display style and enable TCode for the terminal

6.Support restart: It will automatically restart after modifying SSH connection settings or custom preference settings, or can be manually restarted after SSH connection is disconnected

7.Support copy and paste:

- Copy: Same as `Git` terminal, selecting text will automatically copy it
- Paste: Same as `Cmd` terminal, right-click to paste (requires browser access permission)

8.Support file management, open the File Management Module to view, decompress, upload and download files/folders

9.Support shortcut key operations such as multiple/all file selection, copy and paste, cut, selection switch, open

10.Support file browsing and editing, modify the file and save it to a remote server using `ctrl+s`

11.Support [TCode (Terminal Code)](./doc/en_US/TCODE.md)，which can achieve automated Workflow similar to Shell scripts through Customized TCode

### 👨‍💻 Update Records

##### zyyzyykk/kkterminal:3.3.7: latest

- Adaptive remote server encoding format
- Expand highlighting file types
- Add file item keyboard selection switch and open
- Optimization of hidden file icon style

##### zyyzyykk/kkterminal:3.3.3: 

- Add compressed file decompression function
- Fixed the bug of file loss caused by folder drag upload
- Added preview for browser native supported format files
- Optimize the interface display of File Management Module

##### zyyzyykk/kkterminal:3.2.9: 

- Add file URL upload
- Fixed bug where canceling folder downloads caused disconnection
- Optimize page display and code logic, optimize packaging volume

[**History Update Records**](./doc/en_US/UPDATE.md)

### 🧬 Architecture

```markdown
+---------+     http      +-------------+    ssh     +---------------+
| browser | <===========> | kk Terminal | <========> | remote server |
+---------+   websocket   +-------------+    sftp    +---------------+
Vue + Xterm              SpringBoot + SSHJ                Linux OS    
```

### 🏘️ About this project

Author: [zyyzyykk](https://github.com/zyyzyykk/)

Welcome to provide valuable opinions or suggestions on this project, and you can also join us in maintaining and developing this project together

### 🌟 Stars

[![Stars](https://starchart.cc/zyyzyykk/kkTerminal.svg?variant=adaptive)](https://starchart.cc/zyyzyykk/kkTerminal)
