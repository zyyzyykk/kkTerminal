# kkTerminal

> web终端实现ssh远程连接
>
> 作者：zyyzyykk
>
> 源码：http://git.kkbapps.com/kk/kkTerminal
>
> 预览：https://ssh.kkbpro.com/	或	https://ssh.kkbapps.com/
>
> 更新时间：2023-10-14

### 使用docker部署

1.拉取镜像：

```sh
docker pull zyyzyykk/kkterminal:1.0
```

2.创建并运行容器，默认监听3000端口：

```sh
docker run -d --name kkterminal -p 3000:3000 zyyzyykk/kkterminal:1.0
```

3.在浏览器中访问：`http://你的服务器ip:3000/`

### 预览：

访问一下网址：

- https://ssh.kkbpro.com/ 

- https://ssh.kkbapps.com/

![image-20231014200543821](C:\Users\86185\AppData\Roaming\Typora\typora-user-images\image-20231014200543821.png)

### 更新记录

##### zyyzyykk/kkterminal:1.0 ：

提交官方镜像

