# 贝尔实验室 Spring 官方推荐镜像 JDK下载地址 https://bell-sw.com/pages/downloads/
FROM bellsoft/liberica-openjdk-rocky:21.0.10-cds
# FROM bellsoft/liberica-openjdk-rocky:17.0.16-cds
LABEL maintainer="抓蛙师"

# 创建目录结构
RUN mkdir -p /ruoyi/monitor/logs

WORKDIR /ruoyi/monitor

# 设置环境变量
ENV SERVER_PORT=9090 \
    LANG=C.UTF-8 \
    LC_ALL=C.UTF-8 \
    JAVA_OPTS="-Xms256m -Xmx512m" \
    SPRING_PROFILES_ACTIVE=prod \
    TZ=Asia/Shanghai

# 暴露端口
EXPOSE ${SERVER_PORT}

# 复制应用jar包到工作目录
COPY ./target/ruoyi-monitor-admin.jar /ruoyi/monitor/app.jar

# 启动命令 - 明确切换到工作目录并使用绝对路径
ENTRYPOINT ["sh", "-c", "cd /ruoyi/monitor && exec java \
    -Dserver.port=${SERVER_PORT} \
    -Dspring.profiles.active=${SPRING_PROFILES_ACTIVE} \
    -Duser.timezone=${TZ} \
    -XX:+HeapDumpOnOutOfMemoryError \
    -XX:HeapDumpPath=/ruoyi/monitor/logs/ \
    -XX:+UseZGC \
    ${JAVA_OPTS} \
    -jar /ruoyi/monitor/app.jar"]
