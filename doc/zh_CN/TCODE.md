### ⌨️ 终端代码

> 终端代码是用于访问和执行特定操作流程的快捷方式
>
> 终端代码长度为 2~6 位，其中第一位表示终端代码类型
>
> 终端代码不区分大小写

##### 终端代码类型

在终端代码输入框中输入 `STC` 并按下回车以查看更多信息

- 功能终端代码：以 `F` 开头，用于执行通用功能
- 系统终端代码：以 `S` 开头，用于访问系统模块
- 用户终端代码：以 `U` 开头，用于自定义工作流

##### 新建用户终端代码

1. 在终端代码输入框中输入 `STW` 并按下回车
2. 输入终端代码名称与描述，并编写相应的工作流
3. 点击确定添加用户终端代码

##### 用户终端代码工作流

> [!Warning]
>
> 编写工作流时的注意事项：
>
> - `kkTerminal` 是工作流中的原生对象，可以**直接使用**对象中的方法
>
> - 工作流中**仅支持** javascript 语法格式
> - 工作流中**不能**添加注释信息
> - 工作流中**不能**使用双引号 `""` ，**只能**使用单引号 `''`
> - 使用 `kkTerminal` 对象的某些方法时**必须**在前面添加 `await` 关键字

###### kkTerminal 基础功能

| 方法名  | 说明                                     | 示例                                                       |
| ------- | ---------------------------------------- | ---------------------------------------------------------- |
| write   | 写入内容并等待输出结果                   | `await kkTerminal.write('写入内容'[, 获取结果延时=200ms])` |
| read    | 获取从上次写入命令开始的全部输出结果数组 | `kkTerminal.read()`                                        |
| readAll | 获取从工作流执行开始的全部输出结果数组   | `kkTerminal.readAll()`                                     |
| hide    | 在终端中隐藏工作流执行过程               | `kkTerminal.hide()`                                        |
| show    | 在终端中显示工作流执行过程               | `kkTerminal.show()`                                        |

###### kkTerminal 存取变量

| 方法名      | 说明                | 示例                                        |
| ----------- | ------------------- | ------------------------------------------- |
| var.session | 获取/设置会话级变量 | `kkTerminal.var.session('key'[, value])`    |
| var.local   | 获取/设置本地级变量 | `kkTerminal.var.local('key'[, value])`      |
| var.clean   | 批量清除本地级变量  | `kkTerminal.var.clean(['key1','key2',...])` |

###### kkTerminal 操作文件

| 方法名        | 说明                               | 示例                                                  |
| ------------- | ---------------------------------- | ----------------------------------------------------- |
| file.cd       | 打开文件模块并进入指定路径         | `await kkTerminal.file.cd('path')`                    |
| file.open     | 在文件编辑器中打开指定路径下的文件 | `await kkTerminal.file.open('path'[, 编辑器配置={}])` |
| file.edit     | 在文件编辑器中编辑文件内容         | `kkTerminal.file.edit((editor) => {})`                |
| file.save     | 以指定编码格式保存文件             | `kkTerminal.file.save('encode')`                      |
| file.close    | 关闭文件编辑器/文件模块            | `kkTerminal.file.close([是否关闭文件模块=false])`     |
| file.download | 下载指定路径下的文件/文件夹        | `await kkTerminal.file.download('path')`              |

###### kkTerminal 保留值

> 保留值是由 kkTerminal对象 自动记录的系统值，通过 `kkTerminal.var.session('保留值名称')` 方法获取

| 名称           | 说明                |
| -------------- | ------------------- |
| CONNECT_OPTION | 当前ssh连接的配置名 |
| HOME_PATH      | 文件模块家路径      |
| CURRENT_DIR    | 文件模块当前目录    |

###### 默认工作流模版（部署jar包）

```js
const path = '/root/terminal';
await kkTerminal.write('cd ' + path, 1200);
const port = 3000;
await kkTerminal.write('lsof -ti :' + port, 1200);
const resultArr = kkTerminal.read();
if(resultArr.length >= 2) {
    const pid = resultArr[1];
    if(pid && /^\d+$/.test(pid)) await kkTerminal.write('kill -9 ' + pid, 1200);
}
const jar = 'kkTerminal.jar';
await kkTerminal.write('nohup java -jar ./' + jar + ' > ./out.log &', 1200);
```

###### 导入导出

> 导入：导入任意数量的用户终端代码（会覆盖同名的终端代码）
>
> 导出：导出当前已存在的全部用户终端代码
>
> 注意：导入导出用户终端代码的文件格式均为 json

用户终端代码文件示例：

```json
{
    "UJAR": {
        "desc": "start jar",
        "workflow": "const path = '/root/terminal';\nawait kkTerminal.write('cd ' + path, 1200);\nconst port = 3000;\nawait kkTerminal.write('lsof -ti :' + port, 1200);\nconst resultArr = kkTerminal.read();\nif(resultArr.length >= 2) {\n    const pid = resultArr[1];\n\tif(pid && /^\\d+$/.test(pid)) await kkTerminal.write('kill -9 ' + pid, 1200);\n}\nconst jar = 'kkTerminal.jar';\nawait kkTerminal.write('nohup java -jar ./' + jar + ' > ./out.log &', 1200);",
        "status": "Not Active"
    }
}
```
