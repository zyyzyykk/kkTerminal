# kkTerminal

> kkTerminal, a web-based terminal for SSH remote server connection.
>
> Author: [zyyzyykk](https://github.com/zyyzyykk/)
>
> Source Code: https://github.com/zyyzyykk/kkTerminal
>
> Docker Hub Address: https://hub.docker.com/repository/docker/zyyzyykk/kkterminal/general
>
> Preview: https://ssh.kkbpro.com/	(For domestic servers)	or	https://ssh.kkbapps.com/	(For foreign servers)
>
> Update Time: 2024-08-14
>

<p align="center"><a href="https://ssh.kkbpro.com/" target="_blank" rel="noopener noreferrer"><img width="100" src="https://img.kkbapps.com/logo/terminal.png" alt="kkterminal logo"></a></p>

<p align="center">
  <a href="https://hub.docker.com/repository/docker/zyyzyykk/kkterminal/general"><img src="https://img.shields.io/docker/pulls/zyyzyykk/kkterminal" alt="Docker Image"></a>
  <a href="https://www.oracle.com/cn/java/technologies/downloads/#java8-windows"><img src="https://img.shields.io/badge/jdk-1.8-orange" alt="JDK Version"></a>
  <a href="https://spring.io/projects/spring-boot"><img src="https://img.shields.io/badge/springboot-2.7.15-green?color=6db33f" alt="SpringBoot Version"></a>
  <a href="https://cn.vuejs.org/"><img src="https://img.shields.io/badge/vue-3.x-green?color=42b883" alt="Vue Version"></a>
  <a href="https://www.apache.org/licenses/"><img src="https://img.shields.io/badge/licence-Apache-red" alt="Apache Licence"></a>
  <a href="https://github.com/zyyzyykk/kkTerminal"><img src="https://img.shields.io/github/stars/zyyzyykk/kkterminal" alt="GitHub"></a>
</p>
<p align="center"><a href="../README.md" >简体中文</a> ｜ English</p>

### **⚡** Quick integration

Using the `iframe` tag in HTML web pages to quick integration:

```html
<iframe src="https://ssh.kkbpro.com/" height="400px" width="600px" ></iframe>
```

### 💪 Deploy with Docker

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

![connect](https://img.kkbapps.com/terminal/3.1.7-1.png)

![prefer](https://img.kkbapps.com/terminal/315-2.png)

![file](https://img.kkbapps.com/terminal/3.2.0-3.png)

![editor](https://img.kkbapps.com/terminal/3.1.2-4.png)

[**More Module Previews**](./MODULE.md)

### 💡 Function Description

1.kkTerminal is a web-based SSH remote connection tool for servers. Click on the terminal icon in the upper left corner and select the connection settings to establish an SSH connection

2.Support the use of `iframe` tags in web pages for quick integration into third-party websites

3.Supports local PC deployment, automatically opens browser window upon startup

4.Support adaptive window size and Chinese input

5.Support custom preference settings, allowing users to choose the background/foreground color, font size, cursor display style and enable TCode for the terminal

6.Support restart: It will automatically restart after modifying SSH connection settings or custom preference settings, or can be manually restarted after SSH connection is disconnected

7.Support copy and paste:

- Copy: Same as `Git` terminal, selecting text will automatically copy it
- Paste: Same as `Cmd` terminal, right-click to paste (requires browser access permission)

8.Support file management, open the File Management Module to view, upload, and download files/folders

9.Support shortcut key operations such as multiple/all file selection, copy and paste and cut

10.Support file browsing and editing, modify the file and save it to a remote server using `ctrl+s`

11.Support [TCode (Terminal Code)](./TCODE.md)，which can achieve automated Workflow similar to Shell scripts through Customized TCode

### 👨‍💻 Update Records

##### zyyzyykk/kkterminal:3.2.2: latest

- TCode supports Get/Set to Session/Local Level variables
- Text editor adds intelligent prompts for TCode native object `kkTerminal`
- File Management Module shortcut keys adapted for MacOS

##### zyyzyykk/kkterminal:3.2.0 :

- The File Management Module supports shortcut key operations such as multiple/all selection, copy and paste, cut, etc
- Support folder download
- Refactoring some logic and optimizing display

##### zyyzyykk/kkterminal:3.1.7 :

- Adapt to default fonts and optimize TCode status display
- Support local PC deployment, automatically open browser window

##### [History Update Records](./UPDATE.md)

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
