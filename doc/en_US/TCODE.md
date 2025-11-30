### ⌨️ Terminal Code

> Terminal Code is a shortcut used to access and execute specific operational processes
>
> The length of Terminal Code is 2-6 bits, where the first bit represents its type
>
> Terminal Code is not case sensitive

##### Terminal Code Type

> Enter `STC` in the Terminal Code input box and press enter to show more details

- Function Terminal Code: Start with `F` , used to execute common functions
- System Terminal Code: Start with `S` , used to access system modules
- User Terminal Code: Start with `U` , used to customize workflows

##### New User Terminal Code

1. Enter `STW` in the Terminal Code input box and press enter
2. Enter the name and description of Terminal Code
3. Click confirm to add User Terminal Code

##### User Terminal Code Workflow

> [!Warning] 
>
> Precautions when writing workflows:
>
> - `kkTerminal` is a native object in workflow, and you can **directly use** the methods in this object
> - Workflow only supports javascript syntax format
> - Annotation information **cannot** be added in workflow
> - Double quotation marks `""` **cannot** be used in workflow, **only** single quotation marks `''` can be used
> - When using some methods of `kkTerminal` , the keyword `await` **must** be added before it

###### kkTerminal Basic Functions

| Methods | Description                                                  | Example                                                      |
| ------- | ------------------------------------------------------------ | ------------------------------------------------------------ |
| write   | Write content and wait for output results                    | `await kkTerminal.write('content'[, delay for obtaining results=200ms])` |
| read    | Obtain all output result arrays since the last write command | `kkTerminal.read()`                                          |
| readAll | Obtain all output result arrays since the workflow execution | `kkTerminal.readAll()`                                       |
| hide    | Hide workflow execution process in the terminal              | `kkTerminal.hide()`                                          |
| show    | Display workflow execution process in the terminal           | `kkTerminal.show()`                                          |

###### kkTerminal Get/Set Variables

| Methods     | Description                       | Example                                     |
| ----------- | --------------------------------- | ------------------------------------------- |
| var.session | Get/Set session level variables   | `kkTerminal.var.session('key'[, value])`    |
| var.local   | Get/Set local level variables     | `kkTerminal.var.local('key'[, value])`      |
| var.clean   | Batch clear local level variables | `kkTerminal.var.clean(['key1','key2',...])` |

###### kkTerminal Operation File

| Methods       | Description                                     | Example                                                  |
| ------------- | ----------------------------------------------- | -------------------------------------------------------- |
| file.cd       | Open file module and enter specified path       | `await kkTerminal.file.cd('path')`                       |
| file.ls       | Enter specified path and return file attributes | `await kkTerminal.file.ls('path')`                       |
| file.pwd      | Return file module current path                 | `kkTerminal.file.pwd()`                                  |
| file.open     | Open file under specified path in file editor   | `await kkTerminal.file.open('path'[, editor config={}])` |
| file.edit     | Edit content in file editor                     | `kkTerminal.file.edit((editor) => {})`                   |
| file.save     | Save file with specified encoding format        | `kkTerminal.file.save('encode')`                         |
| file.close    | Close file editor/file module                   | `kkTerminal.file.close([close file module=false])`       |
| file.download | Download files/folders under specified path     | `await kkTerminal.file.download('path')`                 |

###### kkTerminal Reserved Values

> The reserved values are system values automatically recorded by `kkTerminal` object, and can be obtained through method `kkTerminal.var.session('reserved value name')`

| Name           | Description                           |
| -------------- | ------------------------------------- |
| CONNECT_OPTION | Option name of current ssh connection |
| HOME_PATH      | Home path of file module              |
| CURRENT_DIR    | Current directory of file module      |

###### Default workflow template (deployment jar package)

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

> Import: Import any number of User Terminal Code (will overwrite Terminal Code with the same name)
>
> Export: Export all existing User Terminal Code
>
> Note: The file format for importing and exporting User Terminal Code is json

Example of User Terminal Code file:

```json
{
    "UJAR": {
        "desc": "start jar",
        "workflow": "const path = '/root/terminal';\nawait kkTerminal.write('cd ' + path, 1200);\nconst port = 3000;\nawait kkTerminal.write('lsof -ti :' + port, 1200);\nconst resultArr = kkTerminal.read();\nif(resultArr.length >= 2) {\n    const pid = resultArr[1];\n\tif(pid && /^\\d+$/.test(pid)) await kkTerminal.write('kill -9 ' + pid, 1200);\n}\nconst jar = 'kkTerminal.jar';\nawait kkTerminal.write('nohup java -jar ./' + jar + ' > ./out.log &', 1200);",
        "status": "Not Active"
    }
}
```

