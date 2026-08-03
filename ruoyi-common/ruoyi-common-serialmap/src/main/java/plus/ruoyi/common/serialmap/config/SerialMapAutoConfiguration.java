package plus.ruoyi.common.serialmap.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import plus.ruoyi.common.core.service.DeptService;
import plus.ruoyi.common.core.service.DictService;
import plus.ruoyi.common.core.service.OssService;
import plus.ruoyi.common.core.service.UserService;
import plus.ruoyi.common.serialmap.core.impl.*;
import plus.ruoyi.common.serialmap.initializer.SerialMapConverterInitializer;


/**
 * 序列化映射模块自动配置类
 *
 * <p>功能说明：
 * <ul>
 *   <li>通过 META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports 自动加载</li>
 *   <li>负责注册序列化映射模块的所有转换器 Bean</li>
 *   <li>为每个转换器自动注入所需的业务服务依赖</li>
 * </ul>
 *
 * <p>工作流程：
 * <ol>
 *   <li>Spring Boot 启动时自动加载本配置类</li>
 *   <li>通过 @Bean 方法创建所有转换器实例并注入依赖</li>
 *   <li>Spring 将转换器实例纳入容器管理</li>
 *   <li>后续由 SerialMapConverterInitializer 完成转换器注册和 Jackson 配置</li>
 * </ol>
 *
 * <p>转换器注册说明：
 * <ul>
 *   <li>所有转换器通过 @Bean 方法显式注册，确保插件环境下正常工作</li>
 *   <li>Bean 名称与方法名一致（首字母小写驼峰命名）</li>
 *   <li>使用 @ConditionalOnMissingBean 允许用户自定义覆盖</li>
 *   <li>依赖的服务（如 UserService）由 Spring 自动注入</li>
 * </ul>
 *
 * @author Lion Li
 * @see SerialMapConverterInitializer 转换器初始化器，负责后续的注册和配置工作
 */
@AutoConfiguration
public class SerialMapAutoConfiguration {

    // ==================== 注册所有转换器 Bean ====================

    /**
     * 注册部门ID转名称转换器
     *
     * <p>支持单个ID或逗号分隔的多个ID转换
     *
     * @param deptService 部门服务
     * @return 转换器实例
     */
    @Bean
    @ConditionalOnMissingBean
    public DeptNameImpl deptNameImpl(DeptService deptService) {
        return new DeptNameImpl(deptService);
    }

    /**
     * 注册字典值转标签转换器
     *
     * <p>根据字典类型和字典值获取对应的字典标签
     *
     * @param dictService 字典服务
     * @return 转换器实例
     */
    @Bean
    @ConditionalOnMissingBean
    public DictTypeImpl dictTypeImpl(DictService dictService) {
        return new DictTypeImpl(dictService);
    }

    /**
     * 注册OSS文件ID转URL转换器
     *
     * <p>将OSS文件ID转换为可访问的URL地址
     *
     * @param ossService OSS服务
     * @return 转换器实例
     */
    @Bean
    @ConditionalOnMissingBean
    public OssUrlImpl ossUrlImpl(OssService ossService) {
        return new OssUrlImpl(ossService);
    }

    /**
     * 注册用户ID转用户名转换器
     *
     * <p>将用户ID转换为用户登录账号
     *
     * @param userService 用户服务
     * @return 转换器实例
     */
    @Bean
    @ConditionalOnMissingBean
    public UserNameImpl userNameImpl(UserService userService) {
        return new UserNameImpl(userService);
    }

    /**
     * 注册用户ID转昵称转换器
     *
     * <p>支持单个ID或逗号分隔的多个ID转换为昵称
     *
     * @param userService 用户服务
     * @return 转换器实例
     */
    @Bean
    @ConditionalOnMissingBean
    public NickNameImpl nickNameImpl(UserService userService) {
        return new NickNameImpl(userService);
    }

    /**
     * 注册用户ID转头像转换器
     *
     * <p>支持单个ID或逗号分隔的多个ID转换为头像URL
     *
     * @param userService 用户服务
     * @return 转换器实例
     */
    @Bean
    @ConditionalOnMissingBean
    public AvatarImpl avatarImpl(UserService userService) {
        return new AvatarImpl(userService);
    }

    /**
     * 注册目录ID转名称转换器
     *
     * <p>将OSS目录ID转换为目录名称
     *
     * @param ossService OSS服务
     * @return 转换器实例
     */
    @Bean
    @ConditionalOnMissingBean
    public DirectoryNameImpl directoryNameImpl(OssService ossService) {
        return new DirectoryNameImpl(ossService);
    }

    /**
     * 注册通用字段映射转换器
     *
     * <p>支持多种映射模式：
     * <ul>
     *   <li>单字段映射：ID → 字段值</li>
     *   <li>对象映射：ID → 实体对象/VO对象</li>
     *   <li>集合映射：ID/ID列表 → 对象列表</li>
     * </ul>
     *
     * <p>具有Redis缓存功能，提升查询性能
     *
     * @return 转换器实例
     */
    @Bean
    @ConditionalOnMissingBean
    public FieldMapImpl fieldMapImpl() {
        return new FieldMapImpl();
    }

    /**
     * 注册国际化翻译转换器
     *
     * <p>基于Spring MessageSource实现多语言翻译，支持：
     * <ul>
     *   <li>简单翻译：直接使用key值查找消息</li>
     *   <li>前缀翻译：prefix.key格式</li>
     *   <li>字典翻译：dict.dictType.value格式</li>
     * </ul>
     *
     * @return 转换器实例
     */
    @Bean
    @ConditionalOnMissingBean
    public I18nTranslateImpl i18nTranslateImpl() {
        return new I18nTranslateImpl();
    }

    /**
     * 注册预签名URL转换器
     *
     * <p>为私有文件URL生成预签名访问URL，支持：
     * <ul>
     *   <li>单个URL和多URL（逗号分隔）处理</li>
     *   <li>富文本HTML中的图片标签加签</li>
     *   <li>可配置签名有效期</li>
     *   <li>自动跳过公有文件URL</li>
     * </ul>
     *
     * @return 转换器实例
     */
    @Bean
    @ConditionalOnMissingBean
    public PresignedUrlImpl presignedUrlImpl() {
        return new PresignedUrlImpl();
    }

    // ==================== 注册初始化器 ====================

    /**
     * 注册序列化映射转换器初始化器
     *
     * <p>负责在 Spring 容器启动完成后执行以下初始化工作：
     * <ul>
     *   <li>扫描并注册所有转换器到全局处理器</li>
     *   <li>配置 Jackson 序列化器以支持字段映射</li>
     * </ul>
     *
     * @param objectMapper Jackson 对象映射器
     * @return 初始化器实例
     */
    @Bean
    @ConditionalOnMissingBean
    public SerialMapConverterInitializer serialMapConverterInitializer(ObjectMapper objectMapper) {
        return new SerialMapConverterInitializer(objectMapper);
    }

}
