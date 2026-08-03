package plus.ruoyi.common.serialmap.core.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.toolkit.Db;
import lombok.extern.slf4j.Slf4j;
import plus.ruoyi.common.core.constant.CacheNames;
import plus.ruoyi.common.redis.utils.CacheUtils;
import plus.ruoyi.common.serialmap.annotation.SerialMapType;
import plus.ruoyi.common.serialmap.constant.SerialMapConstant;
import plus.ruoyi.common.serialmap.core.SerialMapInterface;
import plus.ruoyi.common.serialmap.core.context.SerialMapContext;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 通用字段映射转换器
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
 * <p>使用示例：
 * <pre>
 * // 字段映射
 * {@code @SerialMap(converter = FIELD_MAP, source = "userId", entityClass = SysUser.class)}
 * private String nickName;
 *
 * // 对象映射
 * {@code @SerialMap(converter = FIELD_MAP, source = "deptId", entityClass = SysDept.class)}
 * private SysDeptVo deptVo;
 *
 * // 集合映射
 * {@code @SerialMap(converter = FIELD_MAP, source = "roleIds", entityClass = SysRole.class)}
 * private List<SysRoleVo> roles;
 * </pre>
 *
 * @author 抓蛙师
 */
@Slf4j
@SerialMapType(type = SerialMapConstant.FIELD_MAP)
public class FieldMapImpl implements SerialMapInterface<Object> {

    @Override
    public Object convert(Object key, String param) {
        if (key == null) {
            return null;
        }

        try {
            // 获取当前上下文
            SerialMapContext context = SerialMapContext.current();
            if (context == null) {
                log.warn("上下文未找到，无法执行字段映射");
                return key;
            }

            // 获取实体类
            Class<?> entityClass = context.getAnnotation().entityClass();
            if (entityClass == void.class) {
                log.warn("实体类未指定，无法执行字段映射");
                return key;
            }

            // 根据字段类型处理不同的映射逻辑
            Class<?> fieldType = context.getFieldType();
            String propertyName = context.getPropertyName();

            // 获取目标字段名，如果指定了targetField则使用，否则使用当前属性名
            String targetField = context.getAnnotation().targetField();
            if (StrUtil.isBlank(targetField)) {
                targetField = propertyName;
            }

            // 根据字段类型选择转换策略
            if (String.class.isAssignableFrom(fieldType)) {
                // 字符串类型 - 简单字段映射
                return convertSingleField(key, entityClass, targetField);
            } else if (Collection.class.isAssignableFrom(fieldType)) {
                // 集合类型 - 对象列表映射
                return convertList(key, entityClass, fieldType);
            } else {
                // 对象类型 - 单个对象映射
                return convertObject(key, entityClass, fieldType);
            }
        } catch (Exception e) {
            log.error("字段映射转换异常: 键值={}, 错误信息={}", key, e.getMessage(), e);
            return key;
        }
    }

    /**
     * 单字段转换 - 返回指定字段的值
     */
    private Object convertSingleField(Object key, Class<?> entityClass, String targetField) {
        if (!(key instanceof Serializable)) {
            log.warn("键值不是Serializable类型，无法通过ID查询数据库");
            return key;
        }

        try {
            // 构建缓存键
            String cacheKey = buildCacheKey(key, entityClass, targetField);

            // 尝试从缓存获取
            Object cachedValue = CacheUtils.get(CacheNames.FIELD_MAP, cacheKey);
            if (cachedValue != null) {
                log.debug("字段值缓存命中: 键={}, 字段={}", key, targetField);
                return cachedValue;
            }

            // 缓存未命中，从数据库查询
            Object entity = getEntityById((Serializable) key, entityClass);
            if (entity == null) {
                log.warn("未找到ID为 {} 的 {} 实体", key, entityClass.getSimpleName());
                return key;
            }

            // 智能获取字段值
            Object value = getFieldValue(entity, targetField);
            if (value != null) {
                // 将结果放入缓存
                CacheUtils.put(CacheNames.FIELD_MAP, cacheKey, value);
                return value;
            }

            return key;
        } catch (Exception e) {
            log.error("单字段转换失败: ID={}, 实体类={}, 字段={}, 错误信息={}",
                key, entityClass.getSimpleName(), targetField, e.getMessage());
            return key;
        }
    }

    /**
     * 对象转换 - 返回完整对象或转换后的VO
     */
    private Object convertObject(Object key, Class<?> entityClass, Class<?> fieldType) {
        if (!(key instanceof Serializable)) {
            log.warn("键值不是Serializable类型，无法通过ID查询数据库");
            return null;
        }

        try {
            // 构建缓存键
            String cacheKey = buildCacheKey(key, entityClass, "object");

            // 尝试从缓存获取
            Object cachedObject = CacheUtils.get(CacheNames.FIELD_MAP, cacheKey);
            if (fieldType.isInstance(cachedObject)) {
                log.debug("对象缓存命中: 键={}, 类型={}", key, fieldType.getSimpleName());
                return cachedObject;
            }

            // 缓存未命中，从数据库查询
            Object entity = getEntityById((Serializable) key, entityClass);
            if (entity == null) {
                log.warn("未找到ID为 {} 的 {} 实体", key, entityClass.getSimpleName());
                return null;
            }

            Object result;
            // 如果目标类型与实体类型相同，直接返回
            if (fieldType.isAssignableFrom(entityClass)) {
                result = entity;
            } else {
                // 否则，创建目标类型实例并复制属性
                Object targetObject = fieldType.getDeclaredConstructor().newInstance();
                BeanUtil.copyProperties(entity, targetObject);
                result = targetObject;
            }

            // 将结果放入缓存
            CacheUtils.put(CacheNames.FIELD_MAP, cacheKey, result);

            return result;
        } catch (Exception e) {
            log.error("对象转换失败: ID={}, 实体类={}, 字段类型={}, 错误信息={}",
                key, entityClass.getSimpleName(), fieldType.getSimpleName(), e.getMessage());
            return null;
        }
    }

    /**
     * 集合转换 - 返回对象列表
     */
    @SuppressWarnings("unchecked")
    private Object convertList(Object key, Class<?> entityClass, Class<?> fieldType) {
        try {
            List<Serializable> idList = null;

            // 处理不同类型的key
            if (key instanceof Collection<?> collection) {
                // 如果key已经是集合类型
                idList = collection.stream()
                    .filter(id -> id instanceof Serializable)
                    .map(id -> (Serializable) id)
                    .collect(Collectors.toList());
            } else if (key instanceof Serializable) {
                // 单个ID转列表
                idList = CollUtil.newArrayList((Serializable) key);
            }

            if (CollUtil.isEmpty(idList)) {
                log.warn("ID列表为空，无法进行集合转换");
                return CollUtil.newArrayList();
            }

            // 构建缓存键
            String cacheKey = buildCacheKey(idList, entityClass, "list");

            // 尝试从缓存获取
            Object cachedList = CacheUtils.get(CacheNames.FIELD_MAP, cacheKey);
            if (cachedList instanceof List) {
                log.debug("列表缓存命中: 键={}, 类型={}", idList, "List");
                return cachedList;
            }

            // 获取泛型类型
            Type genericType = SerialMapContext.current().getGenericType();
            Class<?> elementClass = Object.class;

            if (genericType instanceof Class) {
                elementClass = (Class<?>) genericType;
            } else if (genericType instanceof ParameterizedType paramType) {
                Type[] typeArguments = paramType.getActualTypeArguments();
                if (typeArguments.length > 0 && typeArguments[0] instanceof Class) {
                    elementClass = (Class<?>) typeArguments[0];
                }
            }

            // 批量查询实体
            List<Object> entityList = (List<Object>) Db.listByIds(idList, entityClass);
            if (CollUtil.isEmpty(entityList)) {
                log.warn("未找到指定ID列表对应的实体记录");
                return CollUtil.newArrayList();
            }

            // 如果目标类型与实体类型相同，直接返回
            if (elementClass.isAssignableFrom(entityClass)) {
                // 缓存结果
                CacheUtils.put(CacheNames.FIELD_MAP, cacheKey, entityList);
                return entityList;
            }

            // 转换为目标类型列表
            List<Object> resultList = new ArrayList<>(entityList.size());
            for (Object entity : entityList) {
                try {
                    Object targetObj = elementClass.getDeclaredConstructor().newInstance();
                    BeanUtil.copyProperties(entity, targetObj);
                    resultList.add(targetObj);
                } catch (Exception e) {
                    log.error("集合元素转换失败: 目标类={}, 错误信息={}",
                        elementClass.getSimpleName(), e.getMessage());
                }
            }

            // 缓存结果
            CacheUtils.put(CacheNames.FIELD_MAP, cacheKey, resultList);
            return resultList;
        } catch (Exception e) {
            log.error("集合转换失败: 键值={}, 实体类={}, 错误信息={}",
                key, entityClass.getSimpleName(), e.getMessage());
            return CollUtil.newArrayList();
        }
    }

    /**
     * 智能获取字段值（尝试多种命名规则）
     */
    private Object getFieldValue(Object entity, String fieldName) {
        // 获取当前处理的上下文
        SerialMapContext context = SerialMapContext.current();

        // 处理targetField，如果有指定则使用
        String targetField = context.getAnnotation().targetField();
        if (StrUtil.isNotBlank(targetField)) {
            // 使用指定的targetField
            fieldName = targetField;
        }

        // 尝试多种命名规则
        String[] possibleFields = {
            // 获取getter方法
            "get" + StrUtil.upperFirst(fieldName),
            // 原始名称
            fieldName,
            // 下划线转驼峰
            StrUtil.toCamelCase(fieldName),
            // 驼峰转下划线
            StrUtil.toUnderlineCase(fieldName),
        };

        // 尝试直接获取字段值
        for (String field : possibleFields) {
            try {
                Object value = BeanUtil.getFieldValue(entity, field);
                if (value != null) {
                    return value;
                }
            } catch (Exception ignored) {
                // 忽略异常，继续尝试下一种命名规则
            }
        }

        // 如果仍然获取不到值，尝试通过反射获取所有字段并模糊匹配
        try {
            for (Field field : ClassUtil.getDeclaredFields(entity.getClass())) {
                String name = field.getName().toLowerCase();
                String fieldNameLower = fieldName.toLowerCase();
                if (name.contains(fieldNameLower) || fieldNameLower.contains(name)) {
                    field.setAccessible(true);
                    Object val = field.get(entity);
                    if (val != null) {
                        return val;
                    }
                }
            }
        } catch (Exception ignored) {
            // 忽略异常
        }

        log.debug("无法找到匹配的字段值: 实体={}, 字段={}",
            entity.getClass().getSimpleName(), fieldName);
        return null;
    }

    /**
     * 构建缓存键
     */
    private String buildCacheKey(Object key, Class<?> entityClass, String suffix) {
        return entityClass.getSimpleName() + ":" + key + ":" + suffix;
    }

    /**
     * 根据ID获取实体对象，带缓存
     */
    private Object getEntityById(Serializable id, Class<?> entityClass) {
        // 构建缓存键
        String cacheKey = entityClass.getName() + ":entity:" + id;

        // 尝试从缓存获取
        Object cachedEntity = CacheUtils.get(CacheNames.FIELD_MAP, cacheKey);
        if (cachedEntity != null) {
            return cachedEntity;
        }

        // 从数据库查询
        Object entity = Db.getById(id, entityClass);
        if (entity != null) {
            // 将结果放入缓存
            CacheUtils.put(CacheNames.FIELD_MAP, cacheKey, entity);
        }

        return entity;
    }
}
