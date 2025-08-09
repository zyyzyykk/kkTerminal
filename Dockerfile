# basic java image
FROM openjdk:8-alpine

# customize environment variables
ENV TITLE="kkTerminal"
ENV AESKEY="P5P1SIqVe6kaOxMX"

# copy jar to directory
COPY terminal.jar /terminal.jar

# entrance: jar startup command
ENTRYPOINT java -Dfile.encoding=UTF-8 -jar /terminal.jar --kk.title=$TITLE --kk.aes.key=$AESKEY

# image meta
LABEL org.opencontainers.image.authors="zyyzyykk@126.com"
LABEL org.opencontainers.image.source="https://github.com/zyyzyykk/kkTerminal"
LABEL org.opencontainers.image.licenses="Apache"