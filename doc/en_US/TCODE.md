### ⌨️ TCode (Terminal Code)

##### TCode Introduction

TCode (Terminal Code) is a shortcut used to access and execute specific operational processes

TCode is not case sensitive; The length is 2-6 bits, where the first bit represents the TCode type

##### TCode Type

> Enter `/H` in the TCode input box and press enter to view detailed information of all TCodes

- Function TCode: Starting with `/` , used to quickly execute common functions
- System TCode: Starting with `S` , used for quick access to system modules
- User TCode: Starting with `U` , customize the implementation of automated Workflow similar to shell scripts

##### Customized TCode

###### New TCode

1. Enter `/A` in the TCode input box and press Enter
2. In the custom TCode dialog box, enter the TCode name and description, then edit the corresponding Workflow (JS syntax)
3. Click OK to add TCode

###### Workflow

1. Workflow only supports JavaScript (js) syntax format

2. There is a `kkTerminal` object in Workflow, and you can **directly use** the methods in the object

3. `kkTerminal` contains the following methods:

   | Msethod | Description                                                  | Example                                                      |
   | ------- | ------------------------------------------------------------ | ------------------------------------------------------------ |
   | write   | Write content and wait for output results                    | `await kkTerminal.write('content'[, Delay in obtaining results=200ms])` |
   | read    | Obtain all output result arrays since the last write command | `kkTerminal.read()`                                          |
   | readAll | Obtain an array of all output results from the start of Workflow execution | `kkTerminal.readAll()`                                       |
   | session | Get/Set Session Level variables                              | `kkTerminal.variables.session('key'[, value])`               |
   | local   | Get/Set Local Level variables                                | `kkTerminal.variables.local('key'[, value])`                 |
   | clean   | Batch Clear Local Level variables                            | `kkTerminal.variables.clean(['key1','key2',...])`            |
   | hide    | Hide Workflow execution process in the terminal              | `kkTerminal.hide()`                                          |
   | show    | Display Workflow execution process in the terminal           | `kkTerminal.show()`                                          |

4. Matters needing attention:
   - Annotation information **cannot** be added in Workflow
   - Double quotation marks `""` **cannot** be used in Workflow, **only** single quotation marks `''` can be used
   - When using the method `kkTerminal.write()` , the keyword `await` **must** be added before it

###### Workflow example: Using Customized TCode to start and deploy Jar packages

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

###### Import/Export

- Import: Importing any number of Customized TCodes will overwrite TCodes with the same name
- Export: Export all existing Customized TCodes

Note: The file format for importing and exporting Customized TCodes is JSON

Example:

```json
{
    "UJAR": {
        "desc": "start jar",
        "workflow": "const path = '/root/terminal';\nawait kkTerminal.write('cd ' + path, 1200);\nconst port = 3000;\nawait kkTerminal.write('lsof -ti :' + port, 1200);\nconst resultArr = kkTerminal.read();\nif(resultArr.length >= 2) {\n    const pid = resultArr[1];\n\tif(pid && /^\\d+$/.test(pid)) await kkTerminal.write('kill -9 ' + pid, 1200);\n}\nconst jar = 'kkTerminal.jar';\nawait kkTerminal.write('nohup java -jar ./' + jar + ' > ./out.log &', 1200);",
        "status": "Not Active"
    }
}
```

