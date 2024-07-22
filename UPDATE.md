### 👨‍💻 历史更新记录

##### zyyzyykk/kkterminal:3.0.7：

- 文件图标库 [file-icons-vue](https://github.com/zyyzyykk/file-icons-vue) 更新至 `1.2.6` 版本，后续不再进行更新

##### zyyzyykk/kkterminal:3.0.6：

- 解决拖拽上传无法识别文件夹的问题，最终修改为不支持拖拽上传文件夹或空文件

##### zyyzyykk/kkterminal:3.0.5：

- 优化文本编辑器撤销历史
- 完善请求异常错误处理

##### zyyzyykk/kkterminal:3.0.3：

- 修改滚动条样式，优化文件大小属性展示
- 修复bug：部分文件权限属性错误、终端中文输入混乱

##### zyyzyykk/kkterminal:3.0.1：

- 优化文字溢出省略等样式
- 修复部分文件无法显示属性模块的bug

##### zyyzyykk/kkterminal:3.0.0：

- 优化文件上传逻辑，压缩打包体积
- 实现文件拖拽上传，将文件拖拽至显示区即可上传
- 新增文件菜单，右键文件或空白区查看菜单项

##### zyyzyykk/kkterminal:2.9.7：

- 优化显示细节
- 文本编辑器由 `Monaco` 替换为更轻量的 `Ace`

##### zyyzyykk/kkterminal:2.9.5：

- 修复了已知bug
- 支持中文输入
- 新增文件的浏览与编辑功能，修改文件后使用 `ctrl+s` 保存至远程服务器

##### zyyzyykk/kkterminal:2.9.2：

- 修改提示语内容
- 对弹窗模块进行优化，打开弹窗后可继续操作终端
- 新增连接配置的保存与导入，可快速切换连接多个远程服务器

##### zyyzyykk/kkterminal:2.9.0：

- 修改了一些默认样式，增加了网页描述与细节处理
- 文件图标库 [file-icons-vue](https://github.com/zyyzyykk/file-icons-vue) 更新至 `1.1.3` 版本，后续版本会同步更新图标库，不再进行说明

##### zyyzyykk/kkterminal:2.8.8：

- 修复了部分情况下文件与文件夹类别展示错误的bug
- 文件图标库 [file-icons-vue](https://github.com/zyyzyykk/file-icons-vue) 更新至 `1.0.4` 版本
- 优化了一些细节

##### zyyzyykk/kkterminal:2.8.3：

- 优化文件上传功能，实现文件后台上传
- 优化文件模块的展示细节
- 修复了因ssh连接未校验导致数据混乱的bug

##### zyyzyykk/kkterminal:2.8.0：

- 新增websocket心跳续约，保证ws连接不断开

- 新增文件管理模块，实现文件的查看、上传与下载

##### zyyzyykk/kkterminal:2.5：

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