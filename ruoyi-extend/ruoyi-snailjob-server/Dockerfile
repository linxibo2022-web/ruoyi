# 贝尔实验室 Spring 官方推荐镜像 JDK下载地址 https://bell-sw.com/pages/downloads/
FROM bellsoft/liberica-openjdk-rocky:21.0.10-cds
# FROM bellsoft/liberica-openjdk-rocky:17.0.16-cds
LABEL maintainer="抓蛙师"

# 创建目录结构
RUN mkdir -p /ruoyi/snailjob/logs

WORKDIR /ruoyi/snailjob

# 设置环境变量
ENV SERVER_PORT=8800 \
    SNAILJOB_SERVER_PORT=17888 \
    LANG=C.UTF-8 \
    LC_ALL=C.UTF-8 \
    JAVA_OPTS="-Xms512m -Xmx1024m" \
    SPRING_PROFILES_ACTIVE=prod \
    TZ=Asia/Shanghai

# 暴露端口
EXPOSE ${SERVER_PORT}
EXPOSE ${SNAILJOB_SERVER_PORT}

# 复制应用jar包到工作目录
COPY ./target/ruoyi-snailjob-server.jar /ruoyi/snailjob/app.jar

# 启动命令 - 明确切换到工作目录并使用绝对路径
ENTRYPOINT ["sh", "-c", "cd /ruoyi/snailjob && exec java \
    -Dserver.port=${SERVER_PORT} \
    -Dspring.profiles.active=${SPRING_PROFILES_ACTIVE} \
    -Duser.timezone=${TZ} \
    -XX:+HeapDumpOnOutOfMemoryError \
    -XX:HeapDumpPath=/ruoyi/snailjob/logs/ \
    -XX:+UseZGC \
    ${JAVA_OPTS} \
    -jar /ruoyi/snailjob/app.jar"]
