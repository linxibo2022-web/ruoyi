package plus.ruoyi.snailjob;

import cn.hutool.core.thread.ThreadUtil;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;

import java.util.Arrays;

/**
 * 任务调度服务端启动程序
 * <p>
 * SnailJob Server
 *
 * @author opensnail
 * @date 2024-05-17
 */
@SpringBootApplication
public class SnailJobServer {

    public static void main(String[] args) {
        // 启动Spring Boot应用并获取应用上下文
        ConfigurableApplicationContext context = SpringApplication.run(
            com.aizuda.snailjob.server.SnailJobServerApplication.class, args);

        // 获取环境配置
        Environment env = context.getEnvironment();

        ThreadUtil.sleep(1000);
        // 打印简化的启动成功信息
        System.out.printf("\n(✨◠‿◠)ﾉ♪♫ %s 启动成功！环境: %s 地址: http://127.0.0.1:%s%s\n\n",
            env.getProperty("spring.application.name"),
            Arrays.toString(env.getActiveProfiles()),
            env.getProperty("server.port"),
            env.getProperty("server.servlet.context-path"));
    }
}
