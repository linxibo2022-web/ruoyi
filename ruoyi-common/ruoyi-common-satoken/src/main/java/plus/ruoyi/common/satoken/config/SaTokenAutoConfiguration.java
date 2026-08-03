package plus.ruoyi.common.satoken.config;

import cn.dev33.satoken.dao.SaTokenDao;
import cn.dev33.satoken.jwt.StpLogicJwtForSimple;
import cn.dev33.satoken.stp.StpInterface;
import cn.dev33.satoken.stp.StpLogic;
import plus.ruoyi.common.core.factory.YmlPropertySourceFactory;
import plus.ruoyi.common.satoken.core.dao.PlusSaTokenDao;
import plus.ruoyi.common.satoken.core.service.SaPermissionImpl;
import plus.ruoyi.common.satoken.handler.SaTokenExceptionHandler;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;

/**
 * Sa-Token权限认证自动配置类
 *
 * <p>配置Sa-Token框架的核心组件：
 * <ul>
 *   <li>JWT简单模式认证逻辑</li>
 *   <li>权限和角色接口实现</li>
 *   <li>自定义Token存储层</li>
 *   <li>统一异常处理</li>
 * </ul>
 *
 * <p>加载配置文件：common-satoken.yml
 *
 * @author Lion Li
 */
@AutoConfiguration
@PropertySource(value = "classpath:common-satoken.yml", factory = YmlPropertySourceFactory.class)
public class SaTokenAutoConfiguration {

    /**
     * 配置Sa-Token的JWT认证逻辑
     *
     * <p>使用JWT简单模式，Token无状态化存储
     *
     * @return JWT认证逻辑实例
     */
    @Bean
    public StpLogic getStpLogicJwt() {
        // Sa-Token 整合 jwt (简单模式)
        return new StpLogicJwtForSimple();
    }

    /**
     * 权限接口实现
     *
     * <p>提供用户权限和角色的查询接口，支持Bean替换方式自定义实现
     *
     * @return 权限接口实现
     */
    @Bean
    public StpInterface stpInterface() {
        return new SaPermissionImpl();
    }

    /**
     * 自定义Token存储层
     *
     * <p>增强的Token存储实现，支持Redis等持久化存储
     *
     * @return Token存储层实例
     */
    @Bean
    public SaTokenDao saTokenDao() {
        return new PlusSaTokenDao();
    }

    /**
     * Sa-Token异常处理器
     *
     * <p>统一处理认证授权相关异常，提供友好的错误响应
     *
     * @return 异常处理器实例
     */
    @Bean
    public SaTokenExceptionHandler saTokenExceptionHandler() {
        return new SaTokenExceptionHandler();
    }

}
