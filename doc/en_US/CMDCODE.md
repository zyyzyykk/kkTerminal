### ⌨️ Command Code

> Command Code is a shortcut used to access and execute specific operational processes
>
> The length of Command Code is 2-6 bits, where the first bit represents its type
>
> Command Code is not case sensitive

##### Command Code Type

Enter `SCCC` in the Command Code input box and press enter to show more details

- Function Command Code: Start with `F` , used to execute common functions
- System Command Code: Start with `S` , used to access system modules
- User Command Code: Start with `U` , used to customize workflows

##### New User Command Code

1. Enter `SCCW` in the Command Code input box and press enter
2. Enter the name and description of Command Code
3. Click confirm to add User Command Code

##### User Command Code Workflow

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

| Methods | Description                                                 | Example                                                 |
| ------- | ----------------------------------------------------------- | ------------------------------------------------------- |
| write   | Write content to the terminal and wait for output results   | `await kkTerminal.write('content'[, wait timeout=10s])` |
| read    | Return the output result array of the k-th previous command | `kkTerminal.read(-k)`                                   |
| readAll | Return all output result arrays since the workflow started  | `kkTerminal.readAll()`                                  |
| hide    | Hide workflow execution process in the terminal             | `kkTerminal.hide()`                                     |
| show    | Display workflow execution process in the terminal          | `kkTerminal.show()`                                     |

###### kkTerminal Get/Set Variables

| Methods     | Description                       | Example                                       |
| ----------- | --------------------------------- | --------------------------------------------- |
| var.session | Get/Set session level variables   | `kkTerminal.var.session('key'[, value])`      |
| var.local   | Get/Set local level variables     | `kkTerminal.var.local('key'[, value])`        |
| var.clean   | Batch clear local level variables | `kkTerminal.var.clean(['key1', 'key2', ...])` |

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

> The reserved values are system values automatically recorded by `kkTerminal` object, and can be obtained via method `kkTerminal.var.session('reserved value name')`

| Name           | Description                               |
| -------------- | ----------------------------------------- |
| CONNECT_OPTION | Option name of the current SSH connection |
| HOME_DIRECTORY | Home directory of the current user        |
| FILE_DIRECTORY | Current directory in the File module      |
| WORK_DIRECTORY | Current working directory of the terminal |

###### Default workflow template (deployment jar package)

```js
const port = 3000;
await kkTerminal.write('lsof -ti :' + port);
const pid = kkTerminal.read()[0];
if(pid && /^\d+$/.test(pid)) await kkTerminal.write('kill -9 ' + pid);
const path = '/root/terminal';
await kkTerminal.write('cd ' + path);
const jar = 'kkTerminal.jar';
await kkTerminal.write('nohup java -jar ./' + jar + ' > ./out.log &');
```

###### Import/Export

> Import: Import any number of User Command Code (will overwrite Command Code with the same name)
>
> Export: Export all existing User Command Code
>
> Note: The file format for importing and exporting User Command Code is json

Example of User Command Code file:

```json
{
    "UJAR": {
        "desc": "Execute Jar",
        "workflow": "const port = 3000;\nawait kkTerminal.write('lsof -ti :' + port);\nconst pid = kkTerminal.read()[0];\nif(pid && /^\\d+$/.test(pid)) await kkTerminal.write('kill -9 ' + pid);\nconst path = '/root/terminal';\nawait kkTerminal.write('cd ' + path);\nconst jar = 'kkTerminal.jar';\nawait kkTerminal.write('nohup java -jar ./' + jar + ' > ./out.log &');",
        "status": "Not Active"
    }
}
```
