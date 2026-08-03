package plus.ruoyi.system.dict.service.impl;

import cn.hutool.core.collection.CollUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CachePut;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import plus.ruoyi.common.core.constant.CacheNames;
import plus.ruoyi.common.core.exception.ServiceException;
import plus.ruoyi.common.core.utils.MapstructUtils;
import plus.ruoyi.common.mybatis.core.page.PageQuery;
import plus.ruoyi.common.mybatis.core.page.PageResult;
import plus.ruoyi.common.mybatis.core.query.PlusLambdaQuery;
import plus.ruoyi.common.redis.utils.CacheUtils;
import plus.ruoyi.common.satoken.utils.LoginHelper;
import plus.ruoyi.system.dict.dao.ISysDictDataDao;
import plus.ruoyi.system.dict.dao.ISysDictTypeDao;
import plus.ruoyi.system.dict.domain.SysDictData;
import plus.ruoyi.system.dict.domain.SysDictType;
import plus.ruoyi.system.dict.domain.bo.SysDictDataBo;
import plus.ruoyi.system.dict.domain.vo.SysDictDataVo;
import plus.ruoyi.system.dict.service.ISysDictDataService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 字典 业务层处理
 *
 * @author Lion Li
 */
@RequiredArgsConstructor
@Service
public class SysDictDataServiceImpl implements ISysDictDataService {

    private final ISysDictDataDao dictDataDao;
    private final ISysDictTypeDao dictTypeDao;

    /**
     * 根据ID查询
     *
     * @param dictDataId 主键ID
     * @return 视图对象
     */
    @Override
    public SysDictDataVo get(Long dictDataId) {
        SysDictData entity = dictDataDao.getById(dictDataId);
        return MapstructUtils.convert(entity, SysDictDataVo.class);
    }

    /**
     * 查询列表
     *
     * @param bo 查询参数
     * @return 列表数据
     */
    @Override
    public List<SysDictDataVo> list(SysDictDataBo bo) {
        PlusLambdaQuery<SysDictData> wrapper = dictDataDao.buildQueryWrapper(bo);
        List<SysDictData> entities = dictDataDao.list(wrapper);
        return MapstructUtils.convert(entities, SysDictDataVo.class);
    }

    /**
     * 分页查询
     *
     * @param bo        查询参数
     * @param pageQuery 分页参数
     * @return 分页结果
     */
    @Override
    public PageResult<SysDictDataVo> page(SysDictDataBo bo, PageQuery pageQuery) {
        PlusLambdaQuery<SysDictData> wrapper = dictDataDao.buildQueryWrapper(bo);
        PageResult<SysDictData> entityPage = dictDataDao.page(wrapper, pageQuery);
        return entityPage.convert(SysDictDataVo.class);
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
        return dictDataDao.deleteByIds(ids);
    }

    /**
     * 批量保存
     *
     * @param boList 业务对象集合
     * @return 影响行数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchSave(List<SysDictDataBo> boList) {
        if (CollUtil.isEmpty(boList)) {
            return 0;
        }
        List<SysDictData> entities = new ArrayList<>(boList.size());
        for (SysDictDataBo bo : boList) {
            SysDictData entity = MapstructUtils.convert(bo, SysDictData.class);
            beforeSave(entity);
            entities.add(entity);
        }
        return dictDataDao.batchSave(entities);
    }

    /**
     * 保存前的数据校验
     *
     * @param entity 待保存实体
     */
    protected void beforeSave(SysDictData entity) {
        // 超级管理员可以修改所有字典数据，包括系统级字典的数据
        if (LoginHelper.isSuperAdmin()) {
            return;
        }

        // 普通用户不能修改系统级字典的数据
        SysDictType dictType = dictTypeDao.getByDictType(entity.getDictType());
        if (dictType != null && "1".equals(dictType.getIsSystem())) {
            throw ServiceException.of("系统级字典\"{}\"不允许修改数据", dictType.getDictName());
        }
    }

    /**
     * 删除前的业务规则校验
     *
     * @param ids 待删除数据ID集合
     */
    protected void beforeDelete(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return;
        }
        // 系统级字典数据不允许删除
        List<SysDictData> dictDataList = dictDataDao.listByIds(ids);
        for (SysDictData dictData : dictDataList) {
            SysDictType dictType = dictTypeDao.getByDictType(dictData.getDictType());
            if (dictType != null && "1".equals(dictType.getIsSystem())) {
                throw ServiceException.of("系统级字典\"{}\"数据不允许删除", dictType.getDictName());
            }
        }
    }

    /**
     * 根据字典类型和字典键值查询字典数据信息
     *
     * @param dictType  字典类型
     * @param dictValue 字典键值
     * @return 字典标签
     */
    @Override
    public String getDictLabel(String dictType, String dictValue) {
        SysDictData dictData = dictDataDao.getByTypeAndValue(dictType, dictValue);
        return dictData != null ? dictData.getDictLabel() : null;
    }

    /**
     * 批量删除字典数据信息
     *
     * @param dictDataIds 需要删除的字典数据ID
     */
    @Override
    public void deleteDictDataByIds(List<Long> dictDataIds) {
        List<SysDictData> dictDataList = dictDataDao.listByIds(dictDataIds);
        dictDataDao.deleteByIds(dictDataIds);
        dictDataList.forEach(dictData -> CacheUtils.evict(CacheNames.SYS_DICT, dictData.getDictType()));
    }

    /**
     * 新增保存字典数据信息
     *
     * @param bo 字典数据信息
     * @return 结果
     */
    @CachePut(cacheNames = CacheNames.SYS_DICT, key = "#bo.dictType")
    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<SysDictDataVo> insertDictData(SysDictDataBo bo) {
        SysDictData data = MapstructUtils.convert(bo, SysDictData.class);
        beforeSave(data);
        boolean success = dictDataDao.insert(data) > 0;
        if (success) {
            List<SysDictData> dictDataList = dictDataDao.listDictDataByType(data.getDictType());
            return MapstructUtils.convert(dictDataList, SysDictDataVo.class);
        }
        throw ServiceException.of("操作失败");
    }

    /**
     * 修改保存字典数据信息
     *
     * @param bo 字典数据信息
     * @return 结果
     */
    @CachePut(cacheNames = CacheNames.SYS_DICT, key = "#bo.dictType")
    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<SysDictDataVo> updateDictData(SysDictDataBo bo) {
        SysDictData data = MapstructUtils.convert(bo, SysDictData.class);
        beforeSave(data);
        boolean success = dictDataDao.updateById(data) > 0;
        if (success) {
            List<SysDictData> dictDataList = dictDataDao.listDictDataByType(data.getDictType());
            return MapstructUtils.convert(dictDataList, SysDictDataVo.class);
        }
        throw ServiceException.of("操作失败");
    }

    /**
     * 校验字典键值是否唯一
     *
     * @param dict 字典数据
     * @return 结果
     */
    @Override
    public boolean checkDictDataUnique(SysDictDataBo dict) {
        return dictDataDao.checkDictValueUnique(dict.getDictType(), dict.getDictValue(), dict.getDictDataId());
    }

    /**
     * 根据字典类型和字典标签查询字典数据
     *
     * @param dictType  字典类型
     * @param dictLabel 字典标签
     * @return 字典数据
     */
    @Override
    public SysDictDataVo getDictDataByTypeAndLabel(String dictType, String dictLabel) {
        SysDictData entity = dictDataDao.getByTypeAndLabel(dictType, dictLabel);
        return MapstructUtils.convert(entity, SysDictDataVo.class);
    }
}
