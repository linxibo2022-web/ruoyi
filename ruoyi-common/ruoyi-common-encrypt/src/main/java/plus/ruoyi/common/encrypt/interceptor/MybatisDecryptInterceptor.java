package plus.ruoyi.common.encrypt.interceptor;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ObjectUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.executor.resultset.ResultSetHandler;
import org.apache.ibatis.plugin.*;
import plus.ruoyi.common.core.utils.StringUtils;
import plus.ruoyi.common.encrypt.annotation.EncryptField;
import plus.ruoyi.common.encrypt.core.EncryptContext;
import plus.ruoyi.common.encrypt.core.EncryptorManager;
import plus.ruoyi.common.encrypt.enumd.AlgorithmType;
import plus.ruoyi.common.encrypt.enumd.EncodeType;
import plus.ruoyi.common.encrypt.properties.EncryptorProperties;

import java.lang.reflect.Field;
import java.sql.Statement;
import java.util.*;

/**
 * MyBatis出参解密拦截器
 * <p>
 * 拦截MyBatis的结果集处理过程，在返回结果前对标注了@EncryptField注解的字段进行解密
 * <p>
 * 拦截点：ResultSetHandler.handleResultSets方法
 * <p>
 * 处理流程：
 * 1. 获取查询结果
 * 2. 递归遍历结果中的对象
 * 3. 对标注了@EncryptField的String字段进行解密
 * 4. 支持Map、List、普通对象等多种结果类型
 * <p>
 * 使用场景：
 * - SELECT查询：返回结果前解密敏感字段
 * - 分页查询：解密分页结果中的敏感字段
 * - 关联查询：解密关联对象中的敏感字段
 *
 * @author 老马
 * @version 4.6.0
 */
@Slf4j
@Intercepts({@Signature(
    type = ResultSetHandler.class,
    method = "handleResultSets",
    args = {Statement.class})
})
@AllArgsConstructor
public class MybatisDecryptInterceptor implements Interceptor {

    private final EncryptorManager encryptorManager;
    private final EncryptorProperties defaultProperties;

    /**
     * 拦截方法
     * <p>
     * 处理流程：
     * 1. 首先对查询参数进行解密处理，防止参数被多次加密
     * 2. 执行原始的结果集处理
     * 3. 对查询结果进行解密处理
     *
     * @param invocation 方法调用信息
     * @return 解密后的结果
     */
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        // 步骤1：处理查询参数解密，防止重复加密
        handleParameterDecryption(invocation);

        // 步骤2：执行原始的结果集处理
        Object result = invocation.proceed();
        if (result == null) {
            return null;
        }

        // 步骤3：对查询结果进行解密处理
        decryptHandler(result);
        return result;
    }

    /**
     * 处理查询参数解密
     * <p>
     * 通过反射获取ParameterHandler中的参数对象，对其进行解密处理
     * 主要解决以下问题：
     * 1. 防止业务层已加密的参数被再次加密
     * 2. 确保查询条件与数据库存储的加密格式一致
     *
     * @param invocation 方法调用信息
     */
    private void handleParameterDecryption(Invocation invocation) {
        try {
            ResultSetHandler resultSetHandler = (ResultSetHandler) invocation.getTarget();

            // 通过反射获取ParameterHandler
            Field parameterHandlerField = resultSetHandler.getClass().getDeclaredField("parameterHandler");
            parameterHandlerField.setAccessible(true);
            Object target = parameterHandlerField.get(resultSetHandler);

            if (target instanceof ParameterHandler parameterHandler) {
                Object parameterObject = parameterHandler.getParameterObject();

                // 处理参数对象（排除简单的String类型参数）
                if (ObjectUtil.isNotNull(parameterObject) && !(parameterObject instanceof String)) {
                    log.debug("开始处理查询参数解密，参数类型: {}", parameterObject.getClass().getSimpleName());
                    decryptHandler(parameterObject);
                }
            }
        } catch (NoSuchFieldException e) {
            log.warn("无法获取ParameterHandler字段，跳过参数解密处理: {}", e.getMessage());
        } catch (Exception e) {
            log.error("处理查询参数解密时出错", e);
        }
    }

    /**
     * 递归处理对象中的解密字段
     * <p>
     * 支持的数据类型：
     * - Map: 递归处理Map中的每个值
     * - List: 递归处理List中的每个元素
     * - 普通对象: 处理对象中标注了@EncryptField的字段
     * <p>
     * 性能优化：
     * - 对于List类型，先检查第一个元素是否包含加密字段
     * - 使用字段缓存避免重复的反射操作
     *
     * @param sourceObject 待处理的对象（参数对象或结果对象）
     */
    private void decryptHandler(Object sourceObject) {
        if (ObjectUtil.isNull(sourceObject)) {
            return;
        }

        // 处理Map类型数据
        if (sourceObject instanceof Map<?, ?> map) {
            // 使用HashSet避免ConcurrentModificationException
            new HashSet<>(map.values()).forEach(this::decryptHandler);
            return;
        }

        // 处理List类型数据
        if (sourceObject instanceof List<?> list) {
            if (CollUtil.isEmpty(list)) {
                return;
            }

            // 性能优化：检查第一个元素是否包含加密字段，如果没有则直接返回
            Object firstItem = list.get(0);
            if (ObjectUtil.isNull(firstItem) ||
                CollUtil.isEmpty(encryptorManager.getFieldCache(firstItem.getClass()))) {
                return;
            }

            // 递归处理列表中的每个元素
            list.forEach(this::decryptHandler);
            return;
        }

        // 处理普通对象
        processObjectFields(sourceObject);
    }

    /**
     * 处理普通对象中的加密字段
     * <p>
     * 获取对象中所有标注了@EncryptField注解的字段，对其进行解密处理
     *
     * @param sourceObject 待处理的对象
     */
    private void processObjectFields(Object sourceObject) {
        // 获取缓存的加密字段
        Set<Field> fields = encryptorManager.getFieldCache(sourceObject.getClass());
        if (ObjectUtil.isNull(fields)) {
            return;
        }

        try {
            for (Field field : fields) {
                Object fieldValue = field.get(sourceObject);
                String decryptedValue = decryptField(Convert.toStr(fieldValue), field);
                field.set(sourceObject, decryptedValue);
            }
        } catch (IllegalAccessException e) {
            log.error("访问字段时出错，对象类型: {}", sourceObject.getClass().getSimpleName(), e);
        } catch (Exception e) {
            log.error("处理解密字段时出错，对象类型: {}", sourceObject.getClass().getSimpleName(), e);
        }
    }

    /**
     * 对单个字段进行解密
     * <p>
     * 根据字段上的@EncryptField注解配置构建解密上下文，执行解密操作
     * 解密配置优先级：字段注解配置 > 默认全局配置
     *
     * @param value 待解密的字段值
     * @param field 字段信息（包含@EncryptField注解）
     * @return 解密后的字段值
     */
    private String decryptField(String value, Field field) {
        if (ObjectUtil.isNull(value)) {
            return null;
        }

        EncryptField encryptField = field.getAnnotation(EncryptField.class);
        if (encryptField == null) {
            log.warn("字段 {} 没有@EncryptField注解", field.getName());
            return value;
        }

        // 构建解密上下文
        EncryptContext encryptContext = buildEncryptContext(encryptField);

        try {
            return encryptorManager.decrypt(value, encryptContext);
        } catch (Exception e) {
            log.error("解密字段 {} 时出错，值: {}", field.getName(), value, e);
            return value; // 解密失败时返回原值
        }
    }

    /**
     * 构建加密上下文
     * <p>
     * 根据字段注解和默认配置构建解密所需的上下文信息
     * 配置优先级：注解配置 > 默认配置
     *
     * @param encryptField 加密字段注解
     * @return 加密上下文
     */
    private EncryptContext buildEncryptContext(EncryptField encryptField) {
        EncryptContext encryptContext = new EncryptContext();

        // 算法配置：注解指定 > 默认配置
        encryptContext.setAlgorithm(
            encryptField.algorithm() == AlgorithmType.DEFAULT ?
                defaultProperties.getAlgorithm() : encryptField.algorithm()
        );

        // 编码配置：注解指定 > 默认配置
        encryptContext.setEncode(
            encryptField.encode() == EncodeType.DEFAULT ?
                defaultProperties.getEncode() : encryptField.encode()
        );

        // 密码配置：注解指定 > 默认配置
        encryptContext.setPassword(
            StringUtils.isBlank(encryptField.password()) ?
                defaultProperties.getPassword() : encryptField.password()
        );

        // 私钥配置：注解指定 > 默认配置
        encryptContext.setPrivateKey(
            StringUtils.isBlank(encryptField.privateKey()) ?
                defaultProperties.getPrivateKey() : encryptField.privateKey()
        );

        // 公钥配置：注解指定 > 默认配置
        encryptContext.setPublicKey(
            StringUtils.isBlank(encryptField.publicKey()) ?
                defaultProperties.getPublicKey() : encryptField.publicKey()
        );

        return encryptContext;
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
        // MyBatis插件属性设置（本实现中暂未使用）
        // 可以在这里添加插件的自定义配置
    }
}
