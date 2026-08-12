package plus.ruoyi.common.test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 测试应用启动类
 * <p>为所有模块的 Spring 集成测试提供统一的应用上下文</p>
 *
 * @author 抓蛙师
 */
@SpringBootApplication
public class TestApplication {

    public static void main(String[] args) {
        SpringApplication.run(TestApplication.class, args);
    }
}
