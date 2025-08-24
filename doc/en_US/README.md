# kkTerminal

> kkTerminal, a powerful terminal for Web SSH connection
>
> Author: [zyyzyykk](https://github.com/zyyzyykk/)
>
> Source Code: https://github.com/zyyzyykk/kkTerminal
>
> Docker Hub Address: https://hub.docker.com/repository/docker/zyyzyykk/kkterminal/general
>
> Preview: https://ssh.kkbpro.com/
>
> Update Time: 2025-08-24
>

<p align="center"><a href="https://ssh.kkbpro.com/" target="_blank" rel="noopener noreferrer"><img width="100" src="https://kkbapps.oss-cn-shanghai.aliyuncs.com/logo/terminal.svg" alt="kkTerminal"></a></p>

<p align="center">
  <a href="https://hub.docker.com/repository/docker/zyyzyykk/kkterminal/general"><img src="https://img.shields.io/docker/pulls/zyyzyykk/kkterminal?logo=docker" alt="Docker Image"></a>
  <a href="https://www.oracle.com/cn/java/technologies/downloads/#java8-windows"><img src="https://img.shields.io/badge/jdk-1.8-orange?logo=openjdk&logoColor=%23e3731c" alt="JDK Version"></a>
  <a href="https://spring.io/projects/spring-boot"><img src="https://img.shields.io/badge/springboot-2.7.15-green?color=6db33f&logo=springboot" alt="SpringBoot Version"></a>
  <a href="https://cn.vuejs.org/"><img src="https://img.shields.io/badge/vue-3.x-green?color=42b883&logo=vue.js" alt="Vue Version"></a>
  <a href="https://www.apache.org/licenses/"><img src="https://img.shields.io/badge/licence-Apache-red?logo=apache&logoColor=%23D22128" alt="Apache Licence"></a>
  <a href="https://github.com/zyyzyykk/kkTerminal"><img src="https://img.shields.io/github/stars/zyyzyykk/kkterminal" alt="GitHub"></a>
</p>
<p align="center">English ｜ <a href="../zh_CN/README.md" >简体中文</a></p>

### ⚡ Quick integration

Using the `iframe` tag in HTML web pages to quick integration:

```html
<iframe src="https://ssh.kkbpro.com/" height="600px" width="800px" ></iframe>
```

### 🐳 Deploy with Docker

1. Pull image:

```bash
docker pull zyyzyykk/kkterminal
```

2. Create and run a container:

```bash
docker run -d --name kkterminal \
-p 3000:3000 \
-e TITLE="kkTerminal" \
-e AESKEY="P5P1SIqVe6kaOxMX" \
-v /data/kkterminal/cloud:/cloud \
zyyzyykk/kkterminal
```

3. Accessing in browser: `http://server-ip:3000/`

### 👀 Preview

Visit this website: https://ssh.kkbpro.com/

![Connect](https://kkbapps.oss-cn-shanghai.aliyuncs.com/terminal/3.6.8/en/Connect.png)

![Preference](https://kkbapps.oss-cn-shanghai.aliyuncs.com/terminal/3.6.8/en/Preference.png)

![File](https://kkbapps.oss-cn-shanghai.aliyuncs.com/terminal/3.6.8/en/File.png)

![Editor](https://kkbapps.oss-cn-shanghai.aliyuncs.com/terminal/3.6.8/en/Editor.png)

[**More Module Previews**](./MODULE.md)

### 💡 Function Description

1. kkTerminal is a powerful terminal for Web SSH connection; Click on the terminal icon in the upper-left corner and select the connection settings to establish an SSH connection

2. Support the use of `iframe` tags in web pages for quick integration into third-party websites

3. Support customizing terminal configuration through [URL Parameters](./PARAMS.md)

4. Support internationalization and language switching between Chinese and English

5. Support local PC deployment, automatically opens browser window upon startup

6. Support adaptive window size and Chinese input

7. Support preference settings, such as choosing the terminal background/foreground color, font size, cursor display style, etc

8. Support automatically/manually restart

9. Support copy and paste:

   - Copy: Same as `Git` terminal, selecting text will automatically copy it

   - Paste: Same as `Cmd` terminal, right-click to paste (requires browser access permission)


10. Support file management, open the File Management Module to view, decompress, upload and download files/folders

11. Support shortcut key operations such as multiple/all file selection, copy and paste, cut, selection switch, open, etc

12. Support file browsing and editing, modify your file and save it to the remote server using `ctrl+s`

13. Support [Terminal Code](./TCODE.md)，which can execute customized workflow

14. Support operation recording and devices synchronization functions

15. Support Cooperate, Monitor and Docker functions

### 👨‍💻 Update Records

##### zyyzyykk/kkterminal:3.6.8: latest

- Add file transport list
- Add docker appStore
- Add logging function
- Optimize the overall encryption logic
- Some dialogs support left and right stretching to adjust width
- Optimize the encoding format for opening and saving in file editor
- User Terminal Code adds reserved values and file module API
- Refactoring some code and optimizing interface display

##### zyyzyykk/kkterminal:3.6.0: 

- Add Advance Cooperate function
- Add Advance Monitor function
- Add Advance Docker function
- Optimize packaging volume & webpage display

[**History Update Records**](./UPDATE.md)

### 🧬 Architecture

```markdown
+---------+     http      +-------------+    ssh     +---------------+
| browser | <===========> | kk Terminal | <========> | remote server |
+---------+   websocket   +-------------+    sftp    +---------------+
Vue + Xterm              SpringBoot + SSHJ                Linux OS    
```

### 🏘️ About this project

> [!Important]
>
> kkTerminal will not actively record any information such as passwords, files, commands, etc which related to remote servers

Author: [zyyzyykk](https://github.com/zyyzyykk/)

Welcome to provide valuable opinions or suggestions on this project, and you can also join us in maintaining and developing this project together

### 🌟 Stars

[![Stars](https://starchart.cc/zyyzyykk/kkTerminal.svg?variant=adaptive)](https://starchart.cc/zyyzyykk/kkTerminal)
