package plus.ruoyi.common.mybatis.config;

import cn.hutool.core.net.NetUtil;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.core.handlers.PostInitTableInfoHandler;
import com.baomidou.mybatisplus.core.incrementer.DefaultIdentifierGenerator;
import com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.TenantLineInnerInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.anyline.data.datasource.DataSourceMonitor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.BeansException;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import plus.ruoyi.common.core.factory.YmlPropertySourceFactory;
import plus.ruoyi.common.core.utils.SpringUtils;
import plus.ruoyi.common.mybatis.aspect.DataPermissionAspect;
import plus.ruoyi.common.mybatis.handler.InjectionMetaObjectHandler;
import plus.ruoyi.common.mybatis.handler.MybatisExceptionHandler;
import plus.ruoyi.common.mybatis.handler.PlusPostInitTableInfoHandler;
import plus.ruoyi.common.mybatis.interceptor.PlusDataPermissionInterceptor;

/**
 * MyBatis-Plus 自动配置
 * <p>
 * 配置 MyBatis-Plus 的核心功能和插件，包括：
 * <ul>
 * <li>拦截器插件（多租户、数据权限、分页、乐观锁）</li>
 * <li>元数据处理器（自动填充字段）</li>
 * <li>ID 生成器（雪花算法）</li>
 * <li>异常处理器</li>
 * <li>数据源监控器（Anyline 框架集成）</li>
 * <li>事务管理</li>
 * </ul>
 * <p>
 * 插件执行顺序很重要，多租户插件必须放在第一位
 *
 * @author Lion Li
 */
@Slf4j
@AutoConfiguration
@EnableTransactionManagement(proxyTargetClass = true)
@MapperScan("${mybatis-plus.mapperPackage}")
@PropertySource(value = "classpath:common-mybatis.yml", factory = YmlPropertySourceFactory.class)
public class MybatisAutoConfiguration {

    /**
     * MyBatis-Plus 拦截器配置
     * <p>
     * 注册各种内置拦截器，执行顺序：
     * 1. 多租户插件（必须第一位）
     * 2. 数据权限拦截器
     * 3. 分页插件
     * 4. 乐观锁插件
     *
     * @return 配置完成的拦截器
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        // 多租户插件 - 必须放到第一位，确保其他插件在多租户环境下正常工作
        try {
            TenantLineInnerInterceptor tenant = SpringUtils.getBean(TenantLineInnerInterceptor.class);
            interceptor.addInnerInterceptor(tenant);
        } catch (BeansException ignore) {
            // 多租户插件未配置时忽略
        }

        // 数据权限处理 - 基于用户角色过滤数据
        interceptor.addInnerInterceptor(dataPermissionInterceptor());
        // 分页插件 - 自动识别数据库类型并生成分页SQL
        interceptor.addInnerInterceptor(paginationInnerInterceptor());
        // 乐观锁插件 - 防止并发更新冲突
        interceptor.addInnerInterceptor(optimisticLockerInnerInterceptor());

        return interceptor;
    }

    /**
     * 数据权限拦截器
     * <p>
     * 根据用户权限自动在 SQL 中添加数据过滤条件
     *
     * @return 数据权限拦截器实例
     */
    public PlusDataPermissionInterceptor dataPermissionInterceptor() {
        return new PlusDataPermissionInterceptor(SpringUtils.getProperty("mybatis-plus.mapperPackage"));
    }

    /**
     * 数据权限切面处理器
     * <p>
     * 配合数据权限拦截器，处理方法级别的权限控制
     *
     * @return 数据权限切面处理器
     */
    @Bean
    public DataPermissionAspect dataPermissionAspect() {
        return new DataPermissionAspect();
    }

    /**
     * 分页插件配置
     * <p>
     * 自动识别数据库类型，生成对应的分页 SQL
     *
     * @return 分页拦截器实例
     */
    public PaginationInnerInterceptor paginationInnerInterceptor() {
        PaginationInnerInterceptor paginationInnerInterceptor = new PaginationInnerInterceptor();
        // 分页合理化：当页码超出范围时不报错，返回空结果
        paginationInnerInterceptor.setOverflow(false);
        return paginationInnerInterceptor;
    }

    /**
     * 乐观锁插件配置
     * <p>
     * 通过版本号字段实现乐观锁，防止并发更新时的数据覆盖
     *
     * @return 乐观锁拦截器实例
     */
    public OptimisticLockerInnerInterceptor optimisticLockerInnerInterceptor() {
        return new OptimisticLockerInnerInterceptor();
    }

    /**
     * 元对象字段填充控制器
     * <p>
     * 自动填充创建时间、更新时间、创建人、更新人等公共字段
     *
     * @return 元对象字段填充处理器
     */
    @Bean
    public MetaObjectHandler metaObjectHandler() {
        return new InjectionMetaObjectHandler();
    }

    /**
     * 雪花算法 ID 生成器
     * <p>
     * 使用网卡信息绑定雪花生成器，确保集群环境下 ID 不重复
     * 基于本机网卡信息生成唯一的工作节点 ID
     *
     * @return ID 生成器实例
     */
    @Bean
    public IdentifierGenerator idGenerator() {
        return new DefaultIdentifierGenerator(NetUtil.getLocalhost());
    }

    /**
     * MyBatis 异常处理器
     * <p>
     * 统一处理 MyBatis 相关异常，提供友好的错误信息
     *
     * @return 异常处理器实例
     */
    @Bean
    public MybatisExceptionHandler mybatisExceptionHandler() {
        return new MybatisExceptionHandler();
    }

    /**
     * 表信息初始化后处理器
     * <p>
     * 在表信息初始化完成后执行自定义逻辑
     *
     * @return 表信息处理器实例
     */
    @Bean
    public PostInitTableInfoHandler postInitTableInfoHandler() {
        return new PlusPostInitTableInfoHandler();
    }

    /**
     * MyBatis 动态数据源监控器
     * <p>
     * 用于 Anyline 框架与动态数据源的集成，提供：
     * - 数据源特征识别
     * - 数据源唯一标识
     * - 适配器生命周期管理
     *
     * @return 数据源监控器实例
     */
    @Bean
    public DataSourceMonitor dataSourceMonitor() {
        return new MyBatisDataSourceMonitor();
    }

    /**
     * MyBatis-Plus 插件说明和文档链接
     * <p>
     * 以下是各个插件的详细文档链接：
     * <ul>
     * <li>PaginationInnerInterceptor 分页插件，自动识别数据库类型
     *     <a href="https://baomidou.com/pages/97710a/">文档链接</a></li>
     * <li>OptimisticLockerInnerInterceptor 乐观锁插件
     *     <a href="https://baomidou.com/pages/0d93c0/">文档链接</a></li>
     * <li>MetaObjectHandler 元对象字段填充控制器
     *     <a href="https://baomidou.com/pages/4c6bcf/">文档链接</a></li>
     * <li>ISqlInjector sql注入器
     *     <a href="https://baomidou.com/pages/42ea4a/">文档链接</a></li>
     * <li>BlockAttackInnerInterceptor 如果是对全表的删除或更新操作，就会终止该操作
     *     <a href="https://baomidou.com/pages/f9a237/">文档链接</a></li>
     * <li>IllegalSQLInnerInterceptor sql性能规范插件(垃圾SQL拦截)</li>
     * <li>IdentifierGenerator 自定义主键策略
     *     <a href="https://baomidou.com/pages/568eb2/">文档链接</a></li>
     * <li>TenantLineInnerInterceptor 多租户插件
     *     <a href="https://baomidou.com/pages/aef2f2/">文档链接</a></li>
     * <li>DynamicTableNameInnerInterceptor 动态表名插件
     *     <a href="https://baomidou.com/pages/2a45ff/">文档链接</a></li>
     * </ul>
     */
}
