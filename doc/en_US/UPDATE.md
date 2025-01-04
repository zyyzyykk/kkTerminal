### 👨‍💻 History Update Records

##### zyyzyykk/kkterminal:3.4.0: 

- Add SSH Option and custom TCode deletion function
- Fixed folder pasting failure bug in File Module
- Optimized network resources and page display

##### zyyzyykk/kkterminal:3.3.7: 

- Adaptive remote server encoding format
- Expand highlighting file types
- Add file item keyboard selection switch and open
- Optimization of hidden file icon style

##### zyyzyykk/kkterminal:3.3.3: 

- Add compressed file decompression function
- Fixed the bug of file loss caused by folder drag upload
- Added preview for browser native supported format files
- Optimize the interface display of File Management Module

##### zyyzyykk/kkterminal:3.2.9: 

- Add file URL upload
- Fixed bug where canceling folder downloads caused disconnection
- Optimize page display and code logic, optimize packaging volume

##### zyyzyykk/kkterminal:3.2.6:

- Fix bugs related to special character operations in files
- Optimize file upload and download
- File Attribute Module: Folder addition includes attributes, file size attributes, and real-time refresh
- Optimize display logic and standardize coding

##### zyyzyykk/kkterminal:3.2.2:

- TCode supports Get/Set to Session/Local Level variables
- Text editor adds intelligent prompts for TCode native object `kkTerminal`
- File Management Module shortcut keys adapted for MacOS

##### zyyzyykk/kkterminal:3.2.0 :

- The File Management Module supports shortcut key operations such as multiple/all selection, copy and paste, cut, etc
- Support folder download
- Refactoring some logic and optimizing display

##### zyyzyykk/kkterminal:3.1.7 :

- Adapt to default fonts and optimize TCode status display
- Support local PC deployment, automatically open browser window

##### zyyzyykk/kkterminal:3.1.5 :

- Fix bug where files cannot be uploaded through the input box
- Support folder upload and optimize file batch upload logic
- Add TCode status information and optimize TCode display

##### zyyzyykk/kkterminal:3.1.2 :

- The file editor supports code highlighting and intelligent prompts for common file types
- Add [TCode (Terminal Code)](./TCODE.md) to enable quick access to functional modules and execution of specific operational processes

##### zyyzyykk/kkterminal:3.0.9 :

- The Connection Setting Module has added functions such as host IP replication, password display and hiding, etc
- The File Management Module has added new prompts for file upload and other related information

##### zyyzyykk/kkterminal:3.0.7 :

- File icon lib [file-icons-vue](https://github.com/zyyzyykk/file-icons-vue) updated to version `1.2.6`  , no further updates will be made in the future

##### zyyzyykk/kkterminal:3.0.6 :

- Solved the issue of unable to recognize folders during drag and drop upload, and ultimately modified it to not support drag and drop upload of folders or empty files

##### zyyzyykk/kkterminal:3.0.5 :

- Optimize text editor to undo history
- Improve the handling of request exceptions and errors

##### zyyzyykk/kkterminal:3.0.3 :

- Modify the scrollbar style and optimize the display of file size attributes
- Fix bug: Some file permission attributes are incorrect, and Chinese input on the terminal is confusing

##### zyyzyykk/kkterminal:3.0.1 :

- Optimize styles such as text overflow and omission
- Fix bug where some files cannot display attribute modules

##### zyyzyykk/kkterminal:3.0.0 :

- Optimize file upload logic, compress packaging volume
- Implement file drag and drop upload, drag the file to the display area to upload
- Add file menu, right-click on file or blank area to view menu items

##### zyyzyykk/kkterminal:2.9.7 :

- Optimize display details
- The text editor has been replaced by the lighter `Ace` instead of `Monaco`

##### zyyzyykk/kkterminal:2.9.5 :

- Fixed known bugs
- Support Chinese input
- Add file browsing and editing functions, modify files and save them to a remote server using `ctrl+s`

##### zyyzyykk/kkterminal:2.9.2 :

- Modify the prompt content
- Optimize the pop-up module so that you can continue operating the terminal after opening the pop-up
- New connection configuration for saving and importing, allowing for quick switching between connecting to multiple remote servers

##### zyyzyykk/kkterminal:2.9.0 :

- Modified some default styles, added webpage descriptions and detail handling
- File icon lib [file-icons-vue](https://github.com/zyyzyykk/file-icons-vue) updated to version `1.1.3` , the lib will be updated synchronously in subsequent versions, and no further explanation will be provided

##### zyyzyykk/kkterminal:2.8.8 :

- Fixed a bug where file and folder categories were displayed incorrectly in some cases
- File icon lib [file-icons-vue](https://github.com/zyyzyykk/file-icons-vue) update to version `1.0.4`
- Optimized some details

##### zyyzyykk/kkterminal:2.8.3 :

- Optimize file upload function to achieve backend file upload
- Optimize the display details of the File Management Module
- Fixed the bug of data confusion caused by unverified SSH connection

##### zyyzyykk/kkterminal:2.8.0 :

- Add WebSocket heartbeat renewal to ensure uninterrupted WS connections
- Add File Management Module to enable file viewing, uploading and downloading

##### zyyzyykk/kkterminal:2.5 :

- Prompt for disconnection due to lack of interaction during new growth time
- Fixed display issue caused by excessively long commands when terminal window size changes

##### zyyzyykk/kkterminal:2.2 :

- Fix the paste bug that occurs after restarting the terminal
- Changed the selectable font styles

##### zyyzyykk/kkterminal:2.0 :

Add copy and paste function within the terminal:
- Copy: Same as Git terminal, selecting text will automatically copy it
- Paste: Same as Cmd terminal, right-click to paste (requires browser access permission)

##### zyyzyykk/kkterminal:1.0 :

Submit official image