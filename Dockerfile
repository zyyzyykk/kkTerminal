# basic java image
FROM amazoncorretto:8-alpine-jre

# customize environment variables
ENV BANNER="kkTerminal"
ENV STORAGE="P5P1SIqVe6kaOxMX"
ENV PASSWORD=""

# copy jar to directory
COPY kkTerminal.jar /kkTerminal.jar

# entrance: jar startup command
ENTRYPOINT java -Dfile.encoding=UTF-8 -jar /kkTerminal.jar --kk.app.banner=$BANNER --kk.key.storage=$STORAGE --kk.key.password=$PASSWORD

# image meta
LABEL org.opencontainers.image.authors="zyyzyykk@126.com"
LABEL org.opencontainers.image.source="https://github.com/zyyzyykk/kkTerminal"
LABEL org.opencontainers.image.licenses="Apache"