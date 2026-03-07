### 🔗 URL参数

| 参数名      | 含义             | 可选值              | 示例/说明                                                    |
| ----------- | ---------------- | ------------------- | ------------------------------------------------------------ |
| option      | 使用配置登录     | 配置名              | option=kkterminal                                            |
| bg          | 背景色           | 十六进制颜色代码    | bg=0C0C0C                                                    |
| fg          | 前景色           | 十六进制颜色代码    | fg=FFFFFF                                                    |
| fontFamily  | 字体             | 字体名              | fontFamily=Courier%20New                                     |
| fontSize    | 字号             | 12/14/16/18/20      | fontSize=18                                                  |
| cursorStyle | 光标样式         | block/underline/bar | cursorStyle=bar                                              |
| cursorBlink | 光标闪烁         | true/false          | cursorBlink=false                                            |
| cmdcode     | 是否显示命令代码 | true/false          | cmdcode=false                                                |
| cloud       | 是否显示多端同步 | true/false          | cloud=false                                                  |
| advance     | 是否显示高级功能 | true/false          | advance=false                                                |
| transport   | 是否显示传输列表 | true/false          | transport=false                                              |
| lang        | 语言             | en/zh               | lang=zh                                                      |
| mode        | 终端模式         | headless/pure/-     | mode=pure                                                    |
| record      | 播放操作录像     | -                   | 优先级高于ssh连接和协作                                      |
| cooperate   | 开启团队协作     | -                   | 优先级高于ssh连接                                            |
| cmd         | 初始化命令       | bash命令/命令代码   | cmd=bash:ls<br>cmd=code:sf                                   |
| user        | 多端同步用户信息 | -                   | 多端同步流程：<br/>1. 点击“多端同步”标签生成携带user参数的同步链接<br/>2. 使用其它的浏览器/设备访问此同步链接<br/>3. 点击“多端同步”标签以实现用户数据同步 |
