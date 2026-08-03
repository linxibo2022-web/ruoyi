package plus.ruoyi.business.base.service.impl;

import cn.hutool.core.collection.CollUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import plus.ruoyi.business.base.dao.IPlatformDao;
import plus.ruoyi.business.base.domain.Platform;
import plus.ruoyi.business.base.domain.bo.PlatformBo;
import plus.ruoyi.business.base.domain.vo.PlatformVo;
import plus.ruoyi.business.base.domain.vo.TemplateConfig;
import plus.ruoyi.business.base.service.IPlatformService;
import plus.ruoyi.common.core.dict.DictEnableStatus;
import plus.ruoyi.common.core.domain.dto.PlatformDTO;
import plus.ruoyi.common.core.exception.ServiceException;
import plus.ruoyi.common.core.service.PlatformService;
import plus.ruoyi.common.core.utils.MapstructUtils;
import plus.ruoyi.common.core.utils.StringUtils;
import plus.ruoyi.common.json.utils.JsonUtils;
import plus.ruoyi.common.mybatis.core.page.PageQuery;
import plus.ruoyi.common.mybatis.core.page.PageResult;
import plus.ruoyi.common.mybatis.core.query.PlusLambdaQuery;
import plus.ruoyi.common.tenant.helper.TenantHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 平台配置服务实现
 *
 * @author 抓蛙师
 */
@Service
@RequiredArgsConstructor
public class PlatformServiceImpl implements IPlatformService, PlatformService {

    private final IPlatformDao platformDao;

    /**
     * 根据ID查询
     *
     * @param id 主键ID
     * @return 视图对象
     */
    @Override
    public PlatformVo get(Long id) {
        Platform entity = platformDao.getById(id);
        return MapstructUtils.convert(entity, PlatformVo.class);
    }

    /**
     * 查询列表
     *
     * @param bo 查询参数
     * @return 列表数据
     */
    @Override
    public List<PlatformVo> list(PlatformBo bo) {
        PlusLambdaQuery<Platform> wrapper = platformDao.buildQueryWrapper(bo);
        List<Platform> entities = platformDao.list(wrapper);
        return MapstructUtils.convert(entities, PlatformVo.class);
    }

    /**
     * 分页查询
     *
     * @param bo        查询参数
     * @param pageQuery 分页参数
     * @return 分页结果
     */
    @Override
    public PageResult<PlatformVo> page(PlatformBo bo, PageQuery pageQuery) {
        PlusLambdaQuery<Platform> wrapper = platformDao.buildQueryWrapper(bo);
        PageResult<Platform> entityPage = platformDao.page(wrapper, pageQuery);
        return entityPage.convert(PlatformVo.class);
    }

    /**
     * 新增
     *
     * @param bo 业务对象
     * @return 主键ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long add(PlatformBo bo) {
        Platform entity = MapstructUtils.convert(bo, Platform.class);
        beforeSave(entity);
        platformDao.insert(entity);
        return entity.getId();
    }

    /**
     * 修改
     *
     * @param bo 业务对象
     * @return 影响行数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int update(PlatformBo bo) {
        if (bo.getId() == null) {
            throw ServiceException.of("平台配置ID不能为空");
        }
        if (!platformDao.exists(bo.getId())) {
            throw ServiceException.of("平台配置不存在");
        }
        Platform entity = MapstructUtils.convert(bo, Platform.class);
        beforeSave(entity);
        return platformDao.updateById(entity);
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
        return platformDao.deleteByIds(ids);
    }

    /**
     * 批量保存
     *
     * @param boList 业务对象集合
     * @return 影响行数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchSave(List<PlatformBo> boList) {
        if (CollUtil.isEmpty(boList)) {
            return 0;
        }
        List<Platform> entities = new ArrayList<>(boList.size());
        for (PlatformBo bo : boList) {
            Platform entity = MapstructUtils.convert(bo, Platform.class);
            beforeSave(entity);
            entities.add(entity);
        }
        return platformDao.batchSave(entities);
    }

    /**
     * 保存前钩子方法
     * 子类可重写此方法实现数据校验、默认值设置等逻辑
     *
     * @param entity 实体对象
     */
    protected void beforeSave(Platform entity) {
        // 默认实现为空,子类按需重写
    }

    /**
     * 删除前钩子方法
     * 子类可重写此方法实现关联数据校验、清理等逻辑
     *
     * @param ids 待删除的ID集合
     */
    protected void beforeDelete(Collection<Long> ids) {
        // 默认实现为空,子类按需重写
    }

    /**
     * 根据平台类型获取平台配置列表
     * 忽略租户中执行
     *
     * @param type     平台类型
     * @param tenantId 租户id
     * @return 平台配置列表
     */
    @Override
    public List<PlatformDTO> listPlatformsByType(String type, String tenantId) {
        List<Platform> list;
        if (StringUtils.isBlank(tenantId)) {
            list = TenantHelper.ignore(() ->
                platformDao.listByTypeAndStatus(type, DictEnableStatus.ENABLE.getValue())
            );
        } else {
            list = TenantHelper.dynamic(tenantId, () ->
                platformDao.listByTypeAndStatus(type, DictEnableStatus.ENABLE.getValue())
            );
        }
        return MapstructUtils.convert(list, PlatformDTO.class);
    }

    /**
     * 根据appid和平台类型获取平台配置
     *
     * @param appid appid
     * @param type  平台类型
     * @return 平台配置
     */
    @Override
    public PlatformDTO getPlatformByAppidAndType(String appid, String type) {
        Platform platform = platformDao.getByAppidAndType(appid, type, DictEnableStatus.ENABLE.getValue());
        return MapstructUtils.convert(platform, PlatformDTO.class);
    }

    /**
     * 根据appid获取平台配置
     * 用于检查appid的全局唯一性
     *
     * @param appid    appid
     * @param tenantId 租户id
     * @return 平台配置,如果不存在返回null
     */
    @Override
    public PlatformDTO getPlatformByAppid(String appid, String tenantId) {
        if (StringUtils.isBlank(appid)) {
            return null;
        }

        Platform platform;
        if (StringUtils.isBlank(tenantId)) {
            // 使用TenantHelper.ignore()进行跨租户查询
            platform = TenantHelper.ignore(() -> platformDao.getByAppid(appid));
        } else {
            platform = TenantHelper.dynamic(tenantId, () -> platformDao.getByAppid(appid));
        }
        return MapstructUtils.convert(platform, PlatformDTO.class);
    }

    /**
     * 根据appid获取订阅消息模板配置
     *
     * @param appid 小程序appid
     * @return 订阅消息模板配置列表（只返回启用的）
     */
    @Override
    public List<TemplateConfig> getTemplateConfigs(String appid) {
        if (StringUtils.isBlank(appid)) {
            return Collections.emptyList();
        }

        // 忽略租户查询平台配置
        Platform platform = TenantHelper.ignore(() -> platformDao.getByAppid(appid));

        if (platform == null || StringUtils.isBlank(platform.getTemplateConfigs())) {
            return Collections.emptyList();
        }

        // 解析JSON字符串
        List<TemplateConfig> configs = JsonUtils.parseArray(
            platform.getTemplateConfigs(),
            TemplateConfig.class
        );

        if (CollUtil.isEmpty(configs)) {
            return Collections.emptyList();
        }

        // 只返回启用的配置
        return configs.stream()
            .filter(config -> DictEnableStatus.ENABLE.getValue().equals(config.getStatus()))
            .collect(Collectors.toList());
    }
}
