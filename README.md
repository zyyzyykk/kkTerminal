# kkTerminal

> kkTerminal, a terminal for Web SSH connection
>
> Author: [zyyzyykk](https://github.com/zyyzyykk/)
>
> Source Code: https://github.com/zyyzyykk/kkTerminal
>
> Docker Hub Address: https://hub.docker.com/repository/docker/zyyzyykk/kkterminal/general
>
> Preview: https://ssh.kkbpro.com/
>
> Update Time: 2025-03-15
>

<p align="center"><a href="https://ssh.kkbpro.com/" target="_blank" rel="noopener noreferrer"><img width="100" src="https://kkbapps.oss-cn-shanghai.aliyuncs.com/logo/terminal.png" alt="kkTerminal"></a></p>

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

1. Pull image:

```bash
docker pull zyyzyykk/kkterminal
```

2. Create and run a container for port mapping: `-p port:3000`

```bash
docker run -d --name kkterminal -p 3000:3000 zyyzyykk/kkterminal
```

3. Custom art word: `-e TITLE="ArtWord"`

```bash
docker run -d --name kkterminal -p 3000:3000 -e TITLE="kkbpro" zyyzyykk/kkterminal
```

4. Accessing in browser: `http://server-ip:3000/`

### 🛸 Preview

Visit the following website: https://ssh.kkbpro.com/

![Connect](https://kkbapps.oss-cn-shanghai.aliyuncs.com/terminal/3.6.0/en/Connect.png)

![Preference](https://kkbapps.oss-cn-shanghai.aliyuncs.com/terminal/3.6.0/en/Preference.png)

![File](https://kkbapps.oss-cn-shanghai.aliyuncs.com/terminal/3.6.0/en/File.png)

![Editor](https://kkbapps.oss-cn-shanghai.aliyuncs.com/terminal/3.6.0/en/Editor.png)

[**More Module Previews**](./doc/en_US/MODULE.md)

### 💡 Function Description

1. kkTerminal is a terminal for Web SSH connection. Click on the terminal icon in the upper-left corner and select the connection settings to establish an SSH connection

2. Support the use of `iframe` tags in web pages for quick integration into third-party websites

3. Support customizing terminal configuration through [URL Parameters](./doc/en_US/PARAMS.md)

4. Support internationalization and language switching between Chinese and English

5. Support local PC deployment, automatically opens browser window upon startup

6. Support adaptive window size and Chinese input

7. Support custom preference settings, allowing users to choose the background/foreground color, font size, cursor display style, enable TCode for the terminal, etc

8. Support restart: It will automatically restart after modifying SSH connection settings or custom preference settings, or can be manually restarted after SSH connection is disconnected

9. Support copy and paste:

   - Copy: Same as `Git` terminal, selecting text will automatically copy it

   - Paste: Same as `Cmd` terminal, right-click to paste (requires browser access permission)


9. Support file management, open the File Management Module to view, decompress, upload and download files/folders

10. Support shortcut key operations such as multiple/all file selection, copy and paste, cut, selection switch, open, etc

11. Support file browsing and editing, modify the file and save it to a remote server using `ctrl+s`

12. Support [TCode (Terminal Code)](./doc/en_US/TCODE.md)，which can achieve automated Workflow similar to Shell scripts through Customized TCode

13. Support operation recording and cloud synchronization functions

14. Support Cooperate, Monitor and Docker functions

### 👨‍💻 Update Records

##### zyyzyykk/kkterminal:3.6.0: latest

- Add Advance Cooperate function
- Add Advance Monitor function
- Add Advance Docker function
- Optimize packaging volume & webpage display

##### zyyzyykk/kkterminal:3.5.6: 

- Support more URL parameters
- Add editor text & image size percentage
- Optimize webpage display

##### zyyzyykk/kkterminal:3.5.3: 

- Add URL parameter to customize terminal configuration
- Fix bug of multi window restart
- Add operation recording and cloud synchronization functions
- Refactoring some code logic

[**History Update Records**](./doc/en_US/UPDATE.md)

### 🧬 Architecture

```markdown
+---------+     http      +-------------+    ssh     +---------------+
| browser | <===========> | kk Terminal | <========> | remote server |
+---------+   websocket   +-------------+    sftp    +---------------+
Vue + Xterm              SpringBoot + SSHJ                Linux OS    
```

### 🏘️ About this project

Dear Users,

I regret to announce that due to limited time and energy, I will not be able to maintenance on `kkTerminal` in the future.

If you are interested in `kkTerminal` or would like to continue maintaining, please feel free to contact me or raise an issue.

If you discover bugs or wish to develop new features, please also raise an issue.

Finally, thank you for your support of kkTerminal and I sincerely hope that kkTerminal can really help you.

Best Regards,

[zyyzyykk](https://github.com/zyyzyykk/)

2025.03.15

### 🌟 Stars

[![Stars](https://starchart.cc/zyyzyykk/kkTerminal.svg?variant=adaptive)](https://starchart.cc/zyyzyykk/kkTerminal)
