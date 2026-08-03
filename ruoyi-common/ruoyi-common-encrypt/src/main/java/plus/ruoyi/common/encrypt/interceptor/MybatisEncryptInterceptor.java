package plus.ruoyi.common.encrypt.interceptor;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ObjectUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.plugin.*;
import plus.ruoyi.common.core.utils.StringUtils;
import plus.ruoyi.common.encrypt.annotation.EncryptField;
import plus.ruoyi.common.encrypt.core.EncryptContext;
import plus.ruoyi.common.encrypt.core.EncryptorManager;
import plus.ruoyi.common.encrypt.enumd.AlgorithmType;
import plus.ruoyi.common.encrypt.enumd.EncodeType;
import plus.ruoyi.common.encrypt.properties.EncryptorProperties;

import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.util.*;

/**
 * MyBatis入参加密拦截器
 * <p>
 * 拦截MyBatis的参数设置过程，在SQL执行前对标注了@EncryptField注解的字段进行加密
 * <p>
 * 拦截点：ParameterHandler.setParameters方法
 * <p>
 * 处理流程：
 * 1. 获取SQL参数对象
 * 2. 检查参数中是否包含需要加密的字段
 * 3. 对标注了@EncryptField的String字段进行加密
 * 4. 支持Map、List、普通对象等多种参数类型
 * <p>
 * 使用场景：
 * - INSERT语句：插入前加密敏感字段
 * - UPDATE语句：更新前加密敏感字段
 * - 查询条件：WHERE条件中的敏感字段加密
 *
 * @author 老马
 * @version 4.6.0
 */
@Slf4j
@Intercepts({@Signature(
    type = ParameterHandler.class,
    method = "setParameters",
    args = {PreparedStatement.class})
})
@AllArgsConstructor
public class MybatisEncryptInterceptor implements Interceptor {

    private final EncryptorManager encryptorManager;
    private final EncryptorProperties defaultProperties;

    /**
     * 拦截方法
     * <p>
     * 在ParameterHandler.setParameters被调用前，先对参数进行加密处理
     */
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Object target = invocation.getTarget();
        if (target instanceof ParameterHandler parameterHandler) {
            Object parameterObject = parameterHandler.getParameterObject();
            if (ObjectUtil.isNotNull(parameterObject) && !(parameterObject instanceof String)) {
                this.encryptHandler(parameterObject);
            }
        }
        return invocation.proceed();
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    /**
     * 递归处理参数对象中的加密字段
     * <p>
     * 支持的参数类型：
     * - Map: 递归处理Map中的每个值
     * - List: 递归处理List中的每个元素
     * - 普通对象: 处理对象中标注了@EncryptField的字段
     *
     * @param sourceObject 待处理的参数对象
     */
    private void encryptHandler(Object sourceObject) {
        if (ObjectUtil.isNull(sourceObject)) {
            return;
        }

        // 处理Map类型参数
        if (sourceObject instanceof Map<?, ?> map) {
            new HashSet<>(map.values()).forEach(this::encryptHandler);
            return;
        }

        // 处理List类型参数
        if (sourceObject instanceof List<?> list) {
            if (CollUtil.isEmpty(list)) {
                return;
            }
            // 优化：检查第一个元素是否包含加密字段，如果没有则直接返回
            Object firstItem = list.get(0);
            if (ObjectUtil.isNull(firstItem) ||
                CollUtil.isEmpty(encryptorManager.getFieldCache(firstItem.getClass()))) {
                return;
            }
            list.forEach(this::encryptHandler);
            return;
        }

        // 处理普通对象
        Set<Field> fields = encryptorManager.getFieldCache(sourceObject.getClass());
        if (ObjectUtil.isNull(fields)) {
            return;
        }

        try {
            for (Field field : fields) {
                Object fieldValue = field.get(sourceObject);
                String encryptedValue = this.encryptField(Convert.toStr(fieldValue), field);
                field.set(sourceObject, encryptedValue);
            }
        } catch (Exception e) {
            log.error("处理加密字段时出错", e);
        }
    }

    /**
     * 对单个字段进行加密
     * <p>
     * 优先级：字段注解配置 > 全局默认配置
     *
     * @param value 字段值
     * @param field 字段信息（包含@EncryptField注解）
     * @return 加密后的字段值
     */
    private String encryptField(String value, Field field) {
        if (ObjectUtil.isNull(value)) {
            return null;
        }

        EncryptField encryptField = field.getAnnotation(EncryptField.class);
        EncryptContext encryptContext = new EncryptContext();

        // 构建加密上下文，优先使用注解配置，其次使用默认配置
        encryptContext.setAlgorithm(
            encryptField.algorithm() == AlgorithmType.DEFAULT ?
                defaultProperties.getAlgorithm() : encryptField.algorithm()
        );
        encryptContext.setEncode(
            encryptField.encode() == EncodeType.DEFAULT ?
                defaultProperties.getEncode() : encryptField.encode()
        );
        encryptContext.setPassword(
            StringUtils.isBlank(encryptField.password()) ?
                defaultProperties.getPassword() : encryptField.password()
        );
        encryptContext.setPrivateKey(
            StringUtils.isBlank(encryptField.privateKey()) ?
                defaultProperties.getPrivateKey() : encryptField.privateKey()
        );
        encryptContext.setPublicKey(
            StringUtils.isBlank(encryptField.publicKey()) ?
                defaultProperties.getPublicKey() : encryptField.publicKey()
        );

        return this.encryptorManager.encrypt(value, encryptContext);
    }

    @Override
    public void setProperties(Properties properties) {
        // MyBatis插件属性设置（本实现中暂未使用）
    }
}
