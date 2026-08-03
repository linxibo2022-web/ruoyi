package plus.ruoyi.common.encrypt.core;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ReflectUtil;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.io.Resources;
import plus.ruoyi.common.core.constant.Constants;
import plus.ruoyi.common.core.utils.ObjectUtils;
import plus.ruoyi.common.core.utils.StringUtils;
import plus.ruoyi.common.encrypt.annotation.EncryptField;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.ClassMetadata;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.util.ClassUtils;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 加密器管理器
 *
 * 核心功能：
 * 1. 扫描并缓存带有@EncryptField注解的实体类字段
 * 2. 管理各种加密器实例的创建和缓存
 * 3. 提供统一的加密解密接口
 * 4. 优化性能，避免重复创建加密器实例
 *
 * 缓存策略：
 * - fieldCache: 缓存类->加密字段集合的映射
 * - encryptorMap: 缓存加密上下文->加密器实例的映射
 *
 * @author 老马
 * @version 4.6.0
 */
@Slf4j
@NoArgsConstructor
public class EncryptorManager {

    /**
     * 加密器实例缓存
     *
     * Key: EncryptContext的hashCode，确保相同配置复用实例
     * Value: 对应的加密器实例
     */
    Map<Integer, IEncryptor> encryptorMap = new ConcurrentHashMap<>();

    /**
     * 类的加密字段缓存
     *
     * Key: 实体类的Class对象
     * Value: 该类中标注了@EncryptField注解的字段集合
     */
    Map<Class<?>, Set<Field>> fieldCache = new ConcurrentHashMap<>();

    /**
     * 构造方法，初始化时扫描实体类
     *
     * @param typeAliasesPackage 实体类所在包路径，支持多个包用逗号分隔
     */
    public EncryptorManager(String typeAliasesPackage) {
        scanEncryptClasses(typeAliasesPackage);
    }

    /**
     * 获取指定类的加密字段缓存
     *
     * @param sourceClazz 目标类
     * @return 该类的加密字段集合，如果没有则返回null
     */
    public Set<Field> getFieldCache(Class<?> sourceClazz) {
        return ObjectUtils.getIfNotNull(fieldCache, f -> f.get(sourceClazz));
    }

    /**
     * 注册并获取加密器实例
     *
     * 使用缓存机制，相同配置的加密器会复用实例，提高性能
     *
     * @param encryptContext 加密上下文配置
     * @return 对应的加密器实例
     */
    public IEncryptor registAndGetEncryptor(EncryptContext encryptContext) {
        int key = encryptContext.hashCode();
        if (encryptorMap.containsKey(key)) {
            return encryptorMap.get(key);
        }
        // 使用反射创建加密器实例
        IEncryptor encryptor = ReflectUtil.newInstance(
            encryptContext.getAlgorithm().getClazz(),
            encryptContext
        );
        encryptorMap.put(key, encryptor);
        return encryptor;
    }

    /**
     * 移除缓存中的加密器实例
     *
     * 当配置发生变化时，可以清除对应的缓存
     *
     * @param encryptContext 要移除的加密上下文配置
     */
    public void removeEncryptor(EncryptContext encryptContext) {
        this.encryptorMap.remove(encryptContext.hashCode());
    }

    /**
     * 统一加密接口
     *
     * @param value 待加密的明文
     * @param encryptContext 加密配置上下文
     * @return 加密后的密文
     */
    public String encrypt(String value, EncryptContext encryptContext) {
        if (StringUtils.startsWith(value, Constants.ENCRYPT_HEADER)) {
            return value;
        }
        IEncryptor encryptor = this.registAndGetEncryptor(encryptContext);
        String encrypt = encryptor.encrypt(value, encryptContext.getEncode());
        return Constants.ENCRYPT_HEADER + encrypt;
    }

    /**
     * 统一解密接口
     *
     * @param value 待解密的密文
     * @param encryptContext 加密配置上下文
     * @return 解密后的明文
     */
    public String decrypt(String value, EncryptContext encryptContext) {
        if (!StringUtils.startsWith(value, Constants.ENCRYPT_HEADER)) {
            return value;
        }
        IEncryptor encryptor = this.registAndGetEncryptor(encryptContext);
        String str = StringUtils.removeStart(value, Constants.ENCRYPT_HEADER);
        return encryptor.decrypt(str);
    }

    /**
     * 扫描指定包下的实体类，缓存包含加密字段的类信息
     *
     * 扫描流程：
     * 1. 根据包路径解析所有.class文件
     * 2. 加载类的元数据信息
     * 3. 检查类中是否有@EncryptField注解的字段
     * 4. 将有加密字段的类信息缓存起来
     *
     * @param typeAliasesPackage 要扫描的包路径，支持多个包用逗号分隔
     */
    private void scanEncryptClasses(String typeAliasesPackage) {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        CachingMetadataReaderFactory factory = new CachingMetadataReaderFactory();
        String[] packagePatternArray = StringUtils.splitPreserveAllTokens(
            typeAliasesPackage,
            ConfigurableApplicationContext.CONFIG_LOCATION_DELIMITERS
        );
        String classpath = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX;

        try {
            for (String packagePattern : packagePatternArray) {
                String path = ClassUtils.convertClassNameToResourcePath(packagePattern);
                Resource[] resources = resolver.getResources(classpath + path + "/*.class");

                for (Resource resource : resources) {
                    ClassMetadata classMetadata = factory.getMetadataReader(resource).getClassMetadata();
                    Class<?> clazz = Resources.classForName(classMetadata.getClassName());
                    Set<Field> encryptFieldSet = getEncryptFieldSetFromClazz(clazz);

                    if (CollUtil.isNotEmpty(encryptFieldSet)) {
                        fieldCache.put(clazz, encryptFieldSet);
                    }
                }
            }
        } catch (Exception e) {
            log.error("初始化数据安全缓存时出错:{}", e.getMessage());
        }
    }

    /**
     * 从指定类中提取所有标注了@EncryptField注解的字段
     *
     * 提取规则：
     * 1. 跳过接口、内部类、匿名类
     * 2. 遍历类的继承链，包括父类字段
     * 3. 只处理String类型且标注了@EncryptField注解的字段
     * 4. 设置字段为可访问状态
     *
     * @param clazz 目标类
     * @return 该类中的加密字段集合
     */
    private Set<Field> getEncryptFieldSetFromClazz(Class<?> clazz) {
        Set<Field> fieldSet = new HashSet<>();

        // 跳过接口、内部类、匿名类
        if (clazz.isInterface() || clazz.isMemberClass() || clazz.isAnonymousClass()) {
            return fieldSet;
        }

        // 遍历继承链
        while (clazz != null) {
            Field[] fields = clazz.getDeclaredFields();
            fieldSet.addAll(List.of(fields));
            clazz = clazz.getSuperclass();
        }

        // 过滤出加密字段：必须是String类型且标注了@EncryptField注解
        fieldSet = fieldSet.stream()
            .filter(field -> field.isAnnotationPresent(EncryptField.class) && field.getType() == String.class)
            .collect(Collectors.toSet());

        // 设置字段可访问
        for (Field field : fieldSet) {
            field.setAccessible(true);
        }

        return fieldSet;
    }
}
