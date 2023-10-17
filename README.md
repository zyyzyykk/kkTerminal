# kkTerminal

> web终端实现ssh远程连接
>
> 作者：zyyzyykk
>
> 源码：http://git.kkbapps.com/kk/kkTerminal
>
> 预览：https://ssh.kkbapps.com/	或	https://ssh.kkbpro.com/	（第一次访问会有点慢）
>
> 更新时间：2023-10-17

### 使用docker部署

1.拉取镜像：

```sh
docker pull zyyzyykk/kkterminal:2.0
```

2.创建并运行容器，默认监听3000端口：

```sh
docker run -d --name kkterminal -p 3000:3000 zyyzyykk/kkterminal:2.0
```

3.在浏览器中访问：`http://你的服务器ip:3000/`

4.自定义艺术字：`-e TITLE="自定义艺术字"`

```sh
docker run -d --name kkterminal -p 3000:3000 -e TITLE="kkbapps" zyyzyykk/kkterminal:2.0
```

### 预览：

访问以下网址：

- https://ssh.kkbapps.com/
- https://ssh.kkbpro.com/     （第一次访问会有点慢）

![kkterminal](https://img.kkbapps.com/kkterminal-show.png)

### 更新记录

##### zyyzyykk/kkterminal:2.0 ：

增加在终端内复制粘贴功能：

- 复制：类似git终端，选中文本即自动进行复制
- 粘贴：鼠标右键单击进行粘贴（需要权限）

##### zyyzyykk/kkterminal:1.0 ：

提交官方镜像

