package plus.ruoyi.monitor.admin;

import cn.hutool.core.thread.ThreadUtil;
import de.codecentric.boot.admin.server.config.EnableAdminServer;
import plus.ruoyi.common.core.utils.SpringUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Arrays;

/**
 * 监控服务启动类
 * <p>
 * Spring Boot Admin
 * 主要功能：
 * 1. 监控各个微服务的健康状态
 * 2. 提供Web界面查看服务状态
 * 3. 支持邮件和WebHook通知
 *
 * @author Lion Li
 */
@EnableAdminServer
@SpringBootApplication
public class MonitorAdmin {

    public static void main(String[] args) {
        SpringApplication.run(MonitorAdmin.class, args);

        ThreadUtil.sleep(1000);
        // 打印简化的启动成功信息
        System.out.printf("\n(✨◠‿◠)ﾉ♪♫ %s 启动成功！环境: %s 地址: http://127.0.0.1:%s%s\n\n",
            SpringUtils.getApplicationName(),
            Arrays.toString(SpringUtils.getActiveProfiles()),
            SpringUtils.getProperty("server.port"),
            SpringUtils.getProperty("spring.boot.admin.context-path"));
    }
}
