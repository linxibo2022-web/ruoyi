package plus.ruoyi.system.config.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.lang.Dict;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.dynamic.datasource.annotation.DS;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import plus.ruoyi.common.core.constant.CacheNames;
import plus.ruoyi.common.core.dict.DictBooleanFlag;
import plus.ruoyi.common.core.exception.ServiceException;
import plus.ruoyi.common.core.service.ConfigService;
import plus.ruoyi.common.core.utils.MapstructUtils;
import plus.ruoyi.common.core.utils.ObjectUtils;
import plus.ruoyi.common.core.utils.SpringUtils;
import plus.ruoyi.common.core.utils.StringUtils;
import plus.ruoyi.common.satoken.utils.LoginHelper;
import plus.ruoyi.common.mybatis.core.page.PageQuery;
import plus.ruoyi.common.mybatis.core.page.PageResult;
import plus.ruoyi.common.mybatis.core.query.PlusLambdaQuery;
import plus.ruoyi.common.redis.utils.CacheUtils;
import plus.ruoyi.common.json.utils.JsonUtils;
import plus.ruoyi.common.tenant.helper.TenantHelper;
import plus.ruoyi.system.config.dao.ISysConfigDao;
import plus.ruoyi.system.config.domain.SysConfig;
import plus.ruoyi.system.config.domain.bo.SysConfigBo;
import plus.ruoyi.system.config.domain.vo.SysConfigVo;
import plus.ruoyi.system.config.service.ISysConfigService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 参数配置 服务层实现
 *
 * @author Lion Li
 */
@RequiredArgsConstructor
@Service
public class SysConfigServiceImpl implements ISysConfigService, ConfigService {

    private final ISysConfigDao configDao;

    /**
     * 查询参数配置信息
     *
     * @param configId 参数配置ID
     * @return 参数配置信息
     */
    @Override
    @DS("master")
    public SysConfigVo get(Long configId) {
        SysConfig entity = configDao.getById(configId);
        return MapstructUtils.convert(entity, SysConfigVo.class);
    }

    /**
     * 查询列表
     *
     * @param bo 查询参数
     * @return 列表数据
     */
    @Override
    public List<SysConfigVo> list(SysConfigBo bo) {
        PlusLambdaQuery<SysConfig> wrapper = configDao.buildQueryWrapper(bo);
        List<SysConfig> entities = configDao.list(wrapper);
        return MapstructUtils.convert(entities, SysConfigVo.class);
    }

    /**
     * 分页查询
     *
     * @param bo        查询参数
     * @param pageQuery 分页参数
     * @return 分页结果
     */
    @Override
    public PageResult<SysConfigVo> page(SysConfigBo bo, PageQuery pageQuery) {
        PlusLambdaQuery<SysConfig> wrapper = configDao.buildQueryWrapper(bo);
        PageResult<SysConfig> entityPage = configDao.page(wrapper, pageQuery);
        return entityPage.convert(SysConfigVo.class);
    }

    /**
     * 批量删除
     *
     * @param ids ID集合
     * @return 影响行数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchDelete(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            throw ServiceException.of("ID集合不能为空");
        }
        beforeDelete(ids);
        return configDao.deleteByIds(ids);
    }

    /**
     * 批量保存
     *
     * @param boList 业务对象集合
     * @return 影响行数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchSave(List<SysConfigBo> boList) {
        if (CollUtil.isEmpty(boList)) {
            return 0;
        }
        List<SysConfig> entities = new ArrayList<>(boList.size());
        for (SysConfigBo bo : boList) {
            SysConfig entity = MapstructUtils.convert(bo, SysConfig.class);
            beforeSave(entity);
            entities.add(entity);
        }
        return configDao.batchSave(entities);
    }

    /**
     * 保存前的数据校验
     *
     * @param entity 待保存实体
     */
    protected void beforeSave(SysConfig entity) {
        // 超级管理员可以修改所有参数，包括系统内置参数
        if (LoginHelper.isSuperAdmin()) {
            return;
        }

        // 普通用户不能修改系统内置参数
        if (entity.getConfigId() != null) {
            SysConfig oldEntity = configDao.getById(entity.getConfigId());
            if (oldEntity != null && DictBooleanFlag.YES.getValue().equals(oldEntity.getConfigType())) {
                throw ServiceException.of("系统内置参数\"{}\"不允许修改", oldEntity.getConfigKey());
            }
        }

        // 普通用户不能修改 configType 属性，强制清空
        entity.setConfigType(null);
    }

    /**
     * 参数配置数据删除前的业务规则校验
     *
     * @param ids 待删除数据ID集合
     */
    protected void beforeDelete(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return;
        }
        // 系统内置参数不允许删除
        List<SysConfig> configList = configDao.listByIds(ids);
        for (SysConfig config : configList) {
            if (DictBooleanFlag.YES.getValue().equals(config.getConfigType())) {
                throw ServiceException.of("系统内置参数\"{}\"不允许删除", config.getConfigKey());
            }
        }
    }

    /**
     * 根据键名查询参数配置信息
     *
     * @param configKey 参数key
     * @return 参数键值
     */
    @Cacheable(cacheNames = CacheNames.SYS_CONFIG, key = "#configKey")
    @Override
    public String getConfigByKey(String configKey) {
        SysConfig retConfig = configDao.getByConfigKey(configKey);
        return ObjectUtils.getIfNotNull(retConfig, SysConfig::getConfigValue, StringUtils.EMPTY);
    }

    /**
     * 获取注册开关
     *
     * @param tenantId 租户id
     * @return true开启，false关闭
     */
    @Override
    public boolean getRegisterEnabled(String tenantId) {
        SysConfig retConfig = TenantHelper.dynamic(tenantId, () ->
            configDao.getByConfigKey("system.account.register-enabled"));
        if (ObjectUtil.isNull(retConfig)) {
            return false;
        }
        return Convert.toBool(retConfig.getConfigValue());
    }

    /**
     * 新增参数配置
     *
     * @param bo 参数配置信息
     * @return 结果
     */
    @CachePut(cacheNames = CacheNames.SYS_CONFIG, key = "#bo.configKey")
    @Override
    public String insertConfig(SysConfigBo bo) {
        SysConfig config = MapstructUtils.convert(bo, SysConfig.class);
        boolean success = configDao.insert(config) > 0;
        if (success) {
            return config.getConfigValue();
        }
        throw ServiceException.of("操作失败");
    }

    /**
     * 修改参数配置
     *
     * @param bo 参数配置信息
     * @return 结果
     */
    @CachePut(cacheNames = CacheNames.SYS_CONFIG, key = "#bo.configKey")
    @Override
    public String updateConfig(SysConfigBo bo) {
        boolean success = false;
        SysConfig config = MapstructUtils.convert(bo, SysConfig.class);
        if (config.getConfigId() != null) {
            SysConfig temp = configDao.getById(config.getConfigId());
            if (!StringUtils.equals(temp.getConfigKey(), config.getConfigKey())) {
                CacheUtils.evict(CacheNames.SYS_CONFIG, temp.getConfigKey());
            }
            success = configDao.updateById(config) > 0;
        } else {
            CacheUtils.evict(CacheNames.SYS_CONFIG, config.getConfigKey());
            success = configDao.updateByConfigKey(config, config.getConfigKey());
        }
        if (success) {
            return config.getConfigValue();
        }
        throw ServiceException.of("操作失败");
    }

    /**
     * 批量删除参数信息
     *
     * @param configIds 需要删除的参数ID
     */
    @Override
    public void deleteConfigByIds(List<Long> configIds) {
        List<SysConfig> list = configDao.listByIds(configIds);
        list.forEach(config -> {
            CacheUtils.evict(CacheNames.SYS_CONFIG, config.getConfigKey());
        });
        // 删除时会调用 beforeDelete() 方法进行权限校验
        batchDelete(configIds);
    }

    /**
     * 清除参数缓存数据
     */
    @Override
    public void clearConfigCache() {
        CacheUtils.clear(CacheNames.SYS_CONFIG);
    }

    /**
     * 校验参数键名是否唯一
     *
     * @param config 参数配置信息
     * @return 结果
     */
    @Override
    public boolean checkConfigKeyUnique(SysConfigBo config) {
        long configId = ObjectUtil.defaultIfNull(config.getConfigId(), -1L);
        SysConfig info = configDao.getByConfigKey(config.getConfigKey());
        if (ObjectUtil.isNotNull(info) && info.getConfigId() != configId) {
            return false;
        }
        return true;
    }

    /**
     * 根据参数 key 获取 Map 类型的配置
     *
     * @param configKey 参数 key
     * @return Dict 对象，如果配置为空或无法解析，返回空 Dict
     */
    @Override
    public Dict getConfigMap(String configKey) {
        String configValue = getConfigValue(configKey);
        return JsonUtils.parseMap(configValue);
    }

    /**
     * 根据参数 key 获取 Map 类型的配置列表
     *
     * @param configKey 参数 key
     * @return Dict 列表，如果配置为空或无法解析，返回空列表
     */
    @Override
    public List<Dict> getConfigArrayMap(String configKey) {
        String configValue = getConfigValue(configKey);
        return JsonUtils.parseArrayMap(configValue);
    }

    /**
     * 根据参数 key 获取指定类型的配置对象
     *
     * @param configKey 参数 key
     * @param clazz     目标对象类型
     * @return 对象实例，如果配置为空或无法解析，返回 null
     */
    @Override
    public <T> T getConfigObject(String configKey, Class<T> clazz) {
        String configValue = getConfigValue(configKey);
        return JsonUtils.parseObject(configValue, clazz);
    }

    /**
     * 根据参数 key 获取指定类型的配置列表
     *
     * @param configKey 参数 key
     * @param clazz     目标元素类型
     * @return 指定类型列表，如果配置为空或无法解析，返回空列表
     */
    @Override
    public <T> List<T> getConfigArray(String configKey, Class<T> clazz) {
        String configValue = getConfigValue(configKey);
        return JsonUtils.parseArray(configValue, clazz);
    }

    /**
     * 根据参数 key 获取参数值
     *
     * @param configKey 参数 key
     * @return 参数值
     */
    @Override
    public String getConfigValue(String configKey) {
        return SpringUtils.getAopProxy(this).getConfigByKey(configKey);
    }
}
