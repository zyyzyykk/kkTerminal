# 基础java镜像
FROM openjdk:8-alpine

# 自定义环境变量
ENV TITLE="kkbapps"

# 将Java应用程序复制到目录
COPY terminal.jar /terminal.jar

# 入口，java项目的启动命令
ENTRYPOINT java -jar /terminal.jar --kk.title=$TITLE

# 添加镜像元数据
LABEL org.opencontainers.image.authors="zyyzyykk@126.com"
LABEL org.opencontainers.image.source="https://github.com/zyyzyykk/kkTerminal"
LABEL org.opencontainers.image.licenses="Apache"