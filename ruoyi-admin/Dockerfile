# 贝尔实验室 Spring 官方推荐镜像 JDK下载地址 https://bell-sw.com/pages/downloads/
FROM bellsoft/liberica-openjdk-rocky:21.0.10-cds
# FROM bellsoft/liberica-openjdk-rocky:17.0.16-cds
LABEL maintainer="抓蛙师"

# 创建目录结构
RUN mkdir -p /ruoyi/server/logs \
    /ruoyi/server/upload \
    /ruoyi/server/temp

WORKDIR /ruoyi/server

# 设置环境变量
ENV SERVER_PORT=8080 \
    SNAIL_PORT=28080 \
    DEBUG_PORT=5005 \
    DEBUG_ARGS="" \
    LANG=C.UTF-8 \
    LC_ALL=C.UTF-8 \
    JAVA_OPTS="" \
    SPRING_PROFILES_ACTIVE=prod \
    TZ=Asia/Shanghai

# 暴露端口
EXPOSE ${SERVER_PORT}
# 暴露 snail job 客户端端口 用于定时任务调度中心通信
EXPOSE ${SNAIL_PORT}
# 调试端口（仅在需要时使用）
EXPOSE ${DEBUG_PORT}

# 复制应用jar包到工作目录
COPY ./target/ryplus_uni.jar /ruoyi/server/app.jar

# 使用 shell 形式的 ENTRYPOINT 以便环境变量替换
# 明确 cd 到工作目录并使用绝对路径启动应用
ENTRYPOINT ["sh", "-c", "cd /ruoyi/server && exec java \
    -Dserver.port=${SERVER_PORT} \
    -Dsnail-job.port=${SNAIL_PORT} \
    -Dspring.profiles.active=${SPRING_PROFILES_ACTIVE} \
    -Duser.timezone=${TZ} \
    ${DEBUG_ARGS} \
    -XX:+HeapDumpOnOutOfMemoryError \
    -XX:HeapDumpPath=/ruoyi/server/logs/ \
    -XX:+UseZGC \
    ${JAVA_OPTS} \
    -jar /ruoyi/server/app.jar"]

#IDEA中创建远程调试配置(注意远程调试运行到断点其他接口也会无法访问 所以已上线项目慎用或者不影响运营的时间段再调试或者另外启动一个服务)
#设置之前需要先通过docker-compose容器编排的时候开启调试参数 DEBUG_ARGS 还需要开启5005端口,建议使用ssh隧道
## 本地执行，建立SSH隧道
#ssh -L 5005:127.0.0.1:5005 user@your-server-ip
## 然后IDEA连接localhost:5005即可 如果未开启ssh隧道则临时开放5005端口,调试完需要注释掉DEBUG_ARGS重启编排并关闭端口
#
#接着: 打开Run -> Edit Configurations -> 点击左上角的 + 号 -> 选择 Remote JVM Debug
#配置参数
#Name: ryplus_uni-远程调试
#Debugger mode: Attach to remote JVM
#Host: 你的服务器IP地址(如果开启了ssh隧道则直接连localhost:5005)
#Port: 5005
#Command line arguments for remote JVM:
#-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005
#Use module classpath: 选择你的项目模块(可以不选)
#点击OK保存
#设置断点 点击调试 即可进行调试
