# 基础java镜像
FROM openjdk:8-alpine

# 自定义环境变量
ENV TITLE="kkbapps"

# 将Java应用程序复制到目录
COPY terminal.jar /data/terminal.jar

# 入口，java项目的启动命令
ENTRYPOINT java -jar /kkbpro/backend/terminal.jar --kk.title=$TITLE