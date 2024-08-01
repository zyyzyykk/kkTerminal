### ⌨️ TCode 事务代码

##### Tcode简介

Tcode（事务代码）是用于访问和执行特定事务流程的快捷方式

Tcode 不区分大小写；长度为 2~6 位，其中第一位表示 TCode 类型

##### TCode类型

> 在TCode输入框中输入 `/H` 并按下回车，查看全部TCode的详细信息

- 功能TCode：以 `/` 开头，用于快速执行通用功能
- 系统TCode：以 `S` 开头，用于快速访问系统模块
- 用户TCode：以 `U` 开头，自定义实现类似shell脚本的自动化Workflow

##### 自定义TCode

###### 新建TCode

1. 在TCode输入框中输入 `/A` ，并按下回车
2. 在自定义TCode对话框中，输入TCode名称与描述，并编辑对应的Workflow（js语法）
3. 点击确定，添加 Tcode

###### Workflow

1. Workflow仅支持 javaScript (js) 语法格式

2. Workflow中存在 `kkTerminal` 对象，可以**直接使用**对象中的方法
3.  `kkTerminal` 包含如下方法：
   - 写入内容并等待输出结果：`await kkTerminal.write('写入内容'[, 获取结果延时=200ms])`
   - 获取从上次写入命令开始的全部输出结果数组：`kkTerminal.getOut()`
   - 获取从Workflow执行开始的全部输出结果数组：`kkTerminal.getAllOut()`
   - 在终端中隐藏/显示Workflow执行过程：`kkTerminal.hide()/kkTerminal.show()`
4. 注意事项：
   - Workflow中**不能**添加注释信息
   - Workflow中**不能**双引号 `""` ，**只能**使用单引号 `''`
   - 使用 `kkTerminal.write` 方法时**必须**在前面加 `await` 关键字

###### Workflow示例：使用自定义TCode完成Jar包的启动与部署

```js
await kkTerminal.write('cd /root/terminal');
await kkTerminal.write('lsof -i :3000', 500);
let resultArr = kkTerminal.getOut();
if(resultArr.length >= 3) {
    let pid = resultArr[2].replace(/\s+/g, ' ').split(' ')[1];
	if(pid) await kkTerminal.write('kill -9 ' + pid);
}
let jar = 'kkTerminal.jar';
await kkTerminal.write('java -jar ./' + jar + ' > ./out.log &');
alert('TCode Workflow Over!');
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
        "workflow": "await kkTerminal.write('cd /root/terminal');\nawait kkTerminal.write('lsof -i :3000', 500);\nlet resultArr = kkTerminal.getOut();\nif(resultArr.length >= 3) {\n    let pid = resultArr[2].replace(/\\s+/g, ' ').split(' ')[1];\n\tif(pid) await kkTerminal.write('kill -9 ' + pid);\n}\nlet jar = 'kkTerminal.jar';\nawait kkTerminal.write('java -jar ./' + jar + ' > ./out.log &');\nalert('TCode Workflow Over!');",
        "status": "Not Active"
    }
}
```
