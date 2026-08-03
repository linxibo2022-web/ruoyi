package plus.ruoyi.system.oss.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import plus.ruoyi.common.core.constant.CacheNames;
import plus.ruoyi.common.core.dict.DictEnableStatus;
import plus.ruoyi.common.core.exception.ServiceException;
import plus.ruoyi.common.core.utils.MapstructUtils;
import plus.ruoyi.common.core.utils.StringUtils;
import plus.ruoyi.common.json.utils.JsonUtils;
import plus.ruoyi.common.mybatis.core.page.PageQuery;
import plus.ruoyi.common.mybatis.core.page.PageResult;
import plus.ruoyi.common.mybatis.core.query.PlusLambdaQuery;
import plus.ruoyi.common.oss.constant.OssConstant;
import plus.ruoyi.common.redis.utils.CacheUtils;
import plus.ruoyi.common.redis.utils.RedisUtils;
import plus.ruoyi.system.oss.dao.ISysOssConfigDao;
import plus.ruoyi.system.oss.domain.SysOssConfig;
import plus.ruoyi.system.oss.domain.bo.SysOssConfigBo;
import plus.ruoyi.system.oss.domain.vo.SysOssConfigVo;
import plus.ruoyi.system.oss.service.ISysOssConfigService;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 对象存储配置Service业务层处理
 *
 * @author Lion Li
 * @author 孤舟烟雨
 * @date 2021-08-13
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class SysOssConfigServiceImpl implements ISysOssConfigService {

    private final ISysOssConfigDao ossConfigDao;

    /**
     * 根据ID查询
     *
     * @param ossConfigId 主键ID
     * @return 视图对象
     */
    @Override
    public SysOssConfigVo get(Long ossConfigId) {
        SysOssConfig entity = ossConfigDao.getById(ossConfigId);
        return MapstructUtils.convert(entity, SysOssConfigVo.class);
    }

    /**
     * 查询列表
     *
     * @param bo 查询参数
     * @return 列表数据
     */
    @Override
    public List<SysOssConfigVo> list(SysOssConfigBo bo) {
        PlusLambdaQuery<SysOssConfig> wrapper = ossConfigDao.buildQueryWrapper(bo);
        List<SysOssConfig> entities = ossConfigDao.list(wrapper);
        return MapstructUtils.convert(entities, SysOssConfigVo.class);
    }

    /**
     * 分页查询
     *
     * @param bo        查询参数
     * @param pageQuery 分页参数
     * @return 分页结果
     */
    @Override
    public PageResult<SysOssConfigVo> page(SysOssConfigBo bo, PageQuery pageQuery) {
        PlusLambdaQuery<SysOssConfig> wrapper = ossConfigDao.buildQueryWrapper(bo);
        PageResult<SysOssConfig> entityPage = ossConfigDao.page(wrapper, pageQuery);
        return entityPage.convert(SysOssConfigVo.class);
    }

    /**
     * 新增
     *
     * @param bo 业务对象
     * @return 主键ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long add(SysOssConfigBo bo) {
        SysOssConfig entity = MapstructUtils.convert(bo, SysOssConfig.class);
        beforeSave(entity);
        ossConfigDao.insert(entity);
        return entity.getOssConfigId();
    }

    /**
     * 修改
     *
     * @param bo 业务对象
     * @return 影响行数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int update(SysOssConfigBo bo) {
        if (bo.getOssConfigId() == null) {
            throw ServiceException.of("配置ID不能为空");
        }
        if (!ossConfigDao.exists(bo.getOssConfigId())) {
            throw ServiceException.of("配置不存在");
        }
        SysOssConfig entity = MapstructUtils.convert(bo, SysOssConfig.class);
        beforeSave(entity);
        return ossConfigDao.updateById(entity);
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
        return ossConfigDao.deleteByIds(ids);
    }

    /**
     * 批量保存
     *
     * @param boList 业务对象集合
     * @return 影响行数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchSave(List<SysOssConfigBo> boList) {
        if (CollUtil.isEmpty(boList)) {
            return 0;
        }
        List<SysOssConfig> entities = new ArrayList<>(boList.size());
        for (SysOssConfigBo bo : boList) {
            SysOssConfig entity = MapstructUtils.convert(bo, SysOssConfig.class);
            beforeSave(entity);
            entities.add(entity);
        }
        return ossConfigDao.batchSave(entities);
    }

    /**
     * 根据ID集合查询
     */
    @Override
    public List<SysOssConfigVo> listByIds(Collection<Long> ids) {
        List<SysOssConfig> entities = ossConfigDao.listByIds(ids);
        return MapstructUtils.convert(entities, SysOssConfigVo.class);
    }

    /**
     * 保存前的数据校验
     *
     * @param entity 待保存实体
     */
    protected void beforeSave(SysOssConfig entity) {
        if (ObjectUtil.isNotNull(entity) && StringUtils.isNotEmpty(entity.getConfigKey())) {
            boolean unique = ossConfigDao.checkConfigKeyUnique(entity.getConfigKey(),
                ObjectUtil.defaultIfNull(entity.getOssConfigId(), -1L));
            if (!unique) {
                throw ServiceException.of("操作配置'{}'失败, 配置key已存在!", entity.getConfigKey());
            }
        }
    }

    /**
     * 删除前的业务规则校验
     *
     * @param ids 待删除数据ID集合
     */
    protected void beforeDelete(Collection<Long> ids) {
        if (CollUtil.containsAny(ids, OssConstant.SYSTEM_DATA_IDS)) {
            throw ServiceException.of("系统内置, 不可删除!");
        }
    }

    /**
     * 项目启动时，初始化参数到缓存，加载配置类
     */
    @Override
    public void initOssConfig() {
        List<SysOssConfig> list = ossConfigDao.listAll();
        // 加载OSS初始化配置
        for (SysOssConfig config : list) {
            String configKey = config.getConfigKey();
            if (DictEnableStatus.ENABLE.getValue().equals(config.getStatus())) {
                RedisUtils.setCacheObject(OssConstant.DEFAULT_CONFIG_KEY, configKey, Duration.ofDays(10));
            }
            CacheUtils.put(CacheNames.SYS_OSS_CONFIG, configKey, JsonUtils.toJsonString(config));
        }
    }

    /**
     * 启用禁用状态
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateOssConfigStatus(SysOssConfigBo bo) {
        SysOssConfig sysOssConfig = MapstructUtils.convert(bo, SysOssConfig.class);
        // 先禁用所有配置
        int row = ossConfigDao.disableAllConfigs();
        // 再启用指定配置
        row += ossConfigDao.updateById(sysOssConfig);
        if (row > 0) {
            RedisUtils.setCacheObject(OssConstant.DEFAULT_CONFIG_KEY, sysOssConfig.getConfigKey(), Duration.ofDays(10));
        }
        return row > 0;
    }

}
