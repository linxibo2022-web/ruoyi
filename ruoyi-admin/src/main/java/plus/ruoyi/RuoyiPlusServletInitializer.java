package plus.ruoyi;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * web容器中进行部署的初始化类
 * <p>
 * 该类用于在外部Servlet容器(如Tomcat、Jetty等)中部署SpringBoot应用时的初始化配置
 * 扩展SpringBootServletInitializer可使SpringBoot应用支持传统WAR包部署模式
 * <p>
 * 主要作用:
 * 1. 继承SpringBootServletInitializer使应用可以作为WAR包部署到外部Servlet容器
 * 2. 重写configure方法指定应用的主启动类(RuoyiPlus)
 * 3. 当应用在外部Servlet容器启动时,会调用此类而非直接调用主启动类的main方法
 *
 * @author Lion Li
 */
public class RuoyiPlusServletInitializer extends SpringBootServletInitializer {

    /**
     * 配置SpringBoot应用源
     * <p>
     * 重写父类configure方法,指定启动类RuoyiPlusApplication作为配置源
     * 当外部Servlet容器启动Web应用时,会调用此方法加载SpringBoot应用的上下文
     *
     * @param application SpringApplicationBuilder对象,用于构建SpringApplication
     * @return 配置好源的SpringApplicationBuilder实例
     */
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(RuoyiPlus.class);
    }

}
