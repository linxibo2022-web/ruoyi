package plus.ruoyi;

import cn.hutool.core.thread.ThreadUtil;
import plus.ruoyi.common.core.utils.ClientLicenseModuleValidator;
import plus.ruoyi.common.core.utils.SpringUtils;
import plus.ruoyi.common.core.utils.StringUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.metrics.buffering.BufferingApplicationStartup;

import java.util.Arrays;

/**
 *  主应用服务启动类
 * <p>
 * 应用程序的主入口点，负责初始化Spring Boot应用上下文并配置必要的启动参数。
 * 在应用启动完成后，输出友好的启动成功消息，包含应用名称、激活的配置文件和服务端口信息。
 * <p>
 * 该启动类使用了以下技术和功能：
 * 1. Spring Boot自动配置 - 通过@SpringBootApplication注解启用自动配置
 * 2. 启动性能监控 - 配置BufferingApplicationStartup监控应用启动性能
 * 3. 应用信息展示 - 在应用启动完成后显示友好的成功消息
 *
 * @author Lion Li
 */
@SpringBootApplication
public class RuoyiPlus {

    /**
     * 应用程序入口方法
     * <p>
     * 该方法完成以下工作：
     * 1. 创建Spring应用实例
     * 2. 配置应用启动性能监控
     * 3. 启动Spring应用并初始化上下文
     * 4. 显示应用启动成功的友好提示
     * <p>
     *
     * @param args 命令行参数数组，可用于传递应用配置
     */
    public static void main(String[] args) {
        // 创建Spring应用实例，指定主配置类
        SpringApplication application = new SpringApplication(RuoyiPlus.class);

        // 配置应用启动性能监控，设置缓冲区大小为2048
        // 这有助于收集和分析应用启动过程中的性能指标
        application.setApplicationStartup(new BufferingApplicationStartup(2048));

        // 运行Spring应用，初始化应用上下文
        // 此调用会完成所有Bean的创建、自动配置和依赖注入
        application.run(args);

        ThreadUtil.sleep(1000);
        // 打印简化的启动成功信息
        System.out.println(StringUtils.format("\n(✨◠‿◠)ﾉ♪♫ {} 启动成功！环境: {} 地址: http://127.0.0.1:{}{}\n",
            SpringUtils.getApplicationName(),
            Arrays.toString(SpringUtils.getActiveProfiles()),
            SpringUtils.getProperty("server.port"),
            SpringUtils.getProperty("server.servlet.context-path")));
        // 需要授权验证 则打开此注释
        //ClientLicenseModuleValidator.check();
    }
}
