### 🔗 URL Parameters

| Name        | Description                      | Values                 | Example/Instructions                                         |
| ----------- | -------------------------------- | ---------------------- | ------------------------------------------------------------ |
| option      | login by option                  | option name            | option=kkterminal                                            |
| bg          | background color                 | hexadecimal color code | bg=0C0C0C                                                    |
| fg          | foreground color                 | hexadecimal color code | fg=FFFFFF                                                    |
| fontFamily  | font                             | font name              | fontFamily=Courier%20New                                     |
| fontSize    | size                             | 12/14/16/18/20         | fontSize=18                                                  |
| cursorStyle | cursor style                     | block/underline/bar    | cursorStyle=bar                                              |
| cursorBlink | cursor blink                     | true/false             | cursorBlink=false                                            |
| tCode       | enable Terminal Code             | true/false             | tCode=false                                                  |
| cloud       | enable devices sync              | true/false             | cloud=false                                                  |
| lang        | language                         | en/zh                  | lang=zh                                                      |
| mode        | terminal mode                    | headless/pure/-        | mode=pure                                                    |
| record      | play operation record            | -                      | priority over ssh connection and cooperation                 |
| cooperate   | start team cooperation           | -                      | priority over ssh connection                                 |
| cmd         | initialized command              | bash/Terminal Code     | cmd=bash:ls<br>cmd=tcode:sf                                  |
| user        | user information of devices sync | -                      | devices synchronization process:<br/>1. Click on "Devices Sync" tab to generate the sync link carrying user parameter<br/>2. Use another browser/device to access this sync link<br/>3. Click on "Devices Sync" tab to achieve user data synchronization |