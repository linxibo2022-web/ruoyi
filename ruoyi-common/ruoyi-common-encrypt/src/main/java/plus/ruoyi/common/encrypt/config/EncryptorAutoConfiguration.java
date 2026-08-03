package plus.ruoyi.common.encrypt.config;

import com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration;
import com.baomidou.mybatisplus.autoconfigure.MybatisPlusProperties;
import lombok.extern.slf4j.Slf4j;
import plus.ruoyi.common.encrypt.core.EncryptorManager;
import plus.ruoyi.common.encrypt.interceptor.MybatisDecryptInterceptor;
import plus.ruoyi.common.encrypt.interceptor.MybatisEncryptInterceptor;
import plus.ruoyi.common.encrypt.properties.EncryptorProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * MyBatis加解密自动配置类
 * <p>
 * 负责注册MyBatis拦截器，实现数据库字段的自动加解密
 * <p>
 * 配置条件：
 * - mybatis-encryptor.enable=true
 * <p>
 * 注册的Bean：
 * - EncryptorManager: 加密器管理器，负责缓存和调度各种加密器
 * - MybatisEncryptInterceptor: 入参加密拦截器，在数据入库前加密
 * - MybatisDecryptInterceptor: 出参解密拦截器，在查询结果返回前解密
 * <p>
 * 配置示例：
 * <pre>
 * mybatis-encryptor:
 *   enable: true
 *   algorithm: AES
 *   password: "1234567890123456"
 *   encode: BASE64
 * </pre>
 *
 * @author 老马
 * @version 4.6.0
 */
@AutoConfiguration(after = MybatisPlusAutoConfiguration.class)
@EnableConfigurationProperties(EncryptorProperties.class)
@ConditionalOnProperty(value = "mybatis-encryptor.enable", havingValue = "true")
@Slf4j
public class EncryptorAutoConfiguration {

    @Autowired
    private EncryptorProperties properties;

    /**
     * 创建加密器管理器
     * <p>
     * 扫描指定包下的实体类，缓存包含@EncryptField注解的字段
     *
     * @param mybatisPlusProperties MyBatis-Plus配置
     * @return 加密器管理器实例
     */
    @Bean
    public EncryptorManager encryptorManager(MybatisPlusProperties mybatisPlusProperties) {
        return new EncryptorManager(mybatisPlusProperties.getTypeAliasesPackage());
    }

    /**
     * 创建MyBatis加密拦截器
     * <p>
     * 拦截ParameterHandler.setParameters方法，在参数设置前对标注了
     * @EncryptField注解的字段进行加密
     *
     * @param encryptorManager 加密器管理器
     * @return 加密拦截器实例
     */
    @Bean
    public MybatisEncryptInterceptor mybatisEncryptInterceptor(EncryptorManager encryptorManager) {
        return new MybatisEncryptInterceptor(encryptorManager, properties);
    }

    /**
     * 创建MyBatis解密拦截器
     *
     * 拦截ResultSetHandler.handleResultSets方法，在结果集处理后对标注了
     * @EncryptField注解的字段进行解密
     *
     * @param encryptorManager 加密器管理器
     * @return 解密拦截器实例
     */
    @Bean
    public MybatisDecryptInterceptor mybatisDecryptInterceptor(EncryptorManager encryptorManager) {
        return new MybatisDecryptInterceptor(encryptorManager, properties);
    }
}
