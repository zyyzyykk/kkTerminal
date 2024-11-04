### ⌨️ TCode 终端代码

##### TCode简介

TCode（Terminal Code, 终端代码）是用于访问和执行特定操作流程的快捷方式

TCode 不区分大小写；长度为 2~6 位，其中第一位表示 TCode 类型

##### TCode类型

> 在TCode输入框中输入 `/H` 并按下回车，查看全部TCode的详细信息

- 功能TCode：以 `/` 开头，用于快速执行通用功能
- 系统TCode：以 `S` 开头，用于快速访问系统模块
- 用户TCode：以 `U` 开头，自定义实现类似shell脚本的自动化Workflow

##### 自定义TCode

###### 新建TCode

1. 在TCode输入框中输入 `/A` ，并按下回车
2. 在自定义TCode对话框中，输入TCode名称与描述，并编辑对应的Workflow（js语法）
3. 点击确定，添加 TCode

###### Workflow

1. Workflow仅支持 javaScript (js) 语法格式

2. Workflow中存在 `kkTerminal` 对象，可以**直接使用**对象中的方法

3. `kkTerminal` 包含如下方法：

   | 方法名  | 说明                                     | 示例                                                       |
   | ------- | ---------------------------------------- | ---------------------------------------------------------- |
   | write   | 写入内容并等待输出结果                   | `await kkTerminal.write('写入内容'[, 获取结果延时=200ms])` |
   | read    | 获取从上次写入命令开始的全部输出结果数组 | `kkTerminal.read()`                                        |
   | readAll | 获取从Workflow执行开始的全部输出结果数组 | `kkTerminal.readAll()`                                     |
   | session | 获取/设置会话级变量                      | `kkTerminal.variables.session('key'[, value])`             |
   | local   | 获取/设置本地级变量                      | `kkTerminal.variables.local('key'[, value])`               |
   | clean   | 批量清除本地级变量                       | `kkTerminal.variables.clean(['key1','key2',...])`          |
   | hide    | 在终端中隐藏Workflow执行过程             | `kkTerminal.hide()`                                        |
   | show    | 在终端中显示Workflow执行过程             | `kkTerminal.show()`                                        |

4. 注意事项：
   - Workflow中**不能**添加注释信息
   - Workflow中**不能**使用双引号 `""` ，**只能**使用单引号 `''`
   - 使用 `kkTerminal.write()` 方法时**必须**在前面加 `await` 关键字

###### Workflow示例：使用自定义TCode完成Jar包的启动与部署

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

- 导入：导入任意数量的自定义TCode，会覆盖同名的TCode
- 导出：导出当前已存在的全部自定义TCode

注意：导入导出自定义TCode的文件格式均为 json

示例：

```json
{
    "UJAR": {
        "desc": "start jar",
        "workflow": "const path = '/root/terminal';\nawait kkTerminal.write('cd ' + path, 1200);\nconst port = 3000;\nawait kkTerminal.write('lsof -ti :' + port, 1200);\nconst resultArr = kkTerminal.read();\nif(resultArr.length >= 2) {\n    const pid = resultArr[1];\n\tif(pid && /^\\d+$/.test(pid)) await kkTerminal.write('kill -9 ' + pid, 1200);\n}\nconst jar = 'kkTerminal.jar';\nawait kkTerminal.write('nohup java -jar ./' + jar + ' > ./out.log &', 1200);",
        "status": "Not Active"
    }
}
```

