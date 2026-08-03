package plus.ruoyi.common.core.utils;

import cn.hutool.extra.spring.SpringUtil;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.boot.autoconfigure.thread.Threading;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * Spring工具类
 * <p>基于HuTool的SpringUtil进行扩展，提供Bean管理、Spring上下文访问、环境信息获取等功能</p>
 * <p>主要用于简化Spring框架的常见操作，如Bean查找、类型判断、代理对象获取等</p>
 *
 * @author Lion Li
 */
@Component
public final class SpringUtils extends SpringUtil {

    /**
     * 判断BeanFactory中是否包含指定名称的Bean定义
     * <p>示例：{@code boolean exists = SpringUtils.containsBean("userService")}</p>
     *
     * @param name Bean名称
     * @return true表示包含该Bean定义，false表示不包含
     */
    public static boolean containsBean(String name) {
        return getBeanFactory().containsBean(name);
    }

    /**
     * 判断指定名称的Bean是否为单例模式
     * <p>示例：{@code boolean isSingle = SpringUtils.isSingleton("userService")}</p>
     * <p>如果与给定名字相应的Bean定义没有被找到，将会抛出NoSuchBeanDefinitionException异常</p>
     *
     * @param name Bean名称
     * @return true表示是单例，false表示是原型（prototype）
     * @throws NoSuchBeanDefinitionException 当Bean定义不存在时抛出
     */
    public static boolean isSingleton(String name) throws NoSuchBeanDefinitionException {
        return getBeanFactory().isSingleton(name);
    }

    /**
     * 获取指定名称Bean的注册类型
     * <p>示例：{@code Class<?> type = SpringUtils.getType("userService")}</p>
     *
     * @param name Bean名称
     * @return Bean的注册类型，如果Bean不存在则返回null
     * @throws NoSuchBeanDefinitionException 当Bean定义不存在时抛出
     */
    public static Class<?> getType(String name) throws NoSuchBeanDefinitionException {
        return getBeanFactory().getType(name);
    }

    /**
     * 获取指定Bean名称的所有别名
     * <p>示例：{@code String[] aliases = SpringUtils.getAliases("userService")}</p>
     * <p>返回格式：["userServiceImpl", "userManager"]</p>
     *
     * @param name Bean名称
     * @return Bean的别名数组，如果没有别名则返回空数组
     * @throws NoSuchBeanDefinitionException 当Bean定义不存在时抛出
     */
    public static String[] getAliases(String name) throws NoSuchBeanDefinitionException {
        return getBeanFactory().getAliases(name);
    }

    /**
     * 获取AOP代理对象
     * <p>示例：{@code UserService proxy = SpringUtils.getAopProxy(userServiceImpl)}</p>
     * <p>通过传入的对象实例，返回其对应的Spring管理的代理对象</p>
     *
     * @param <T> 对象类型
     * @param invoker 原始对象实例
     * @return 对应的AOP代理对象
     */
    @SuppressWarnings("unchecked")
    public static <T> T getAopProxy(T invoker) {
        return (T) getBean(invoker.getClass());
    }

    /**
     * 获取Spring应用上下文
     * <p>示例：{@code ApplicationContext ctx = SpringUtils.context()}</p>
     *
     * @return Spring应用上下文对象
     */
    public static ApplicationContext context() {
        return getApplicationContext();
    }

    /**
     * 判断当前应用是否运行在虚拟线程模式
     * <p>示例：{@code boolean virtual = SpringUtils.isVirtual()}</p>
     * <p>基于Spring Boot的Threading配置判断是否启用了虚拟线程</p>
     *
     * @return true表示启用了虚拟线程，false表示使用传统线程模型
     */
    public static boolean isVirtual() {
        return Threading.VIRTUAL.isActive(getBean(Environment.class));
    }

}
