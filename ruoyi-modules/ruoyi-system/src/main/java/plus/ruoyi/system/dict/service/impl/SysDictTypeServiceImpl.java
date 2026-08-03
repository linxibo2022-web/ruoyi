package plus.ruoyi.system.dict.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import plus.ruoyi.common.core.constant.CacheNames;
import plus.ruoyi.common.core.domain.dto.DictDataDTO;
import plus.ruoyi.common.core.domain.dto.DictTypeDTO;
import plus.ruoyi.common.core.exception.ServiceException;
import plus.ruoyi.common.core.utils.MapstructUtils;
import plus.ruoyi.common.core.utils.SpringUtils;
import plus.ruoyi.common.core.utils.StreamUtils;
import plus.ruoyi.common.core.utils.StringUtils;
import plus.ruoyi.common.mybatis.core.page.PageQuery;
import plus.ruoyi.common.mybatis.core.page.PageResult;
import plus.ruoyi.common.mybatis.core.query.PlusLambdaQuery;
import plus.ruoyi.common.redis.utils.CacheUtils;
import plus.ruoyi.common.satoken.utils.LoginHelper;
import plus.ruoyi.system.dict.dao.ISysDictDataDao;
import plus.ruoyi.system.dict.dao.ISysDictTypeDao;
import plus.ruoyi.system.dict.domain.SysDictData;
import plus.ruoyi.system.dict.domain.SysDictType;
import plus.ruoyi.system.dict.domain.bo.SysDictTypeBo;
import plus.ruoyi.system.dict.domain.vo.SysDictDataVo;
import plus.ruoyi.system.dict.domain.vo.SysDictTypeVo;
import plus.ruoyi.system.dict.service.ISysDictTypeService;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 字典类型业务层实现
 * <p>提供字典类型和字典数据的完整业务功能，包括CRUD操作、缓存管理、标签值转换等</p>
 *
 * @author Lion Li
 */
@Service
@RequiredArgsConstructor
public class SysDictTypeServiceImpl implements ISysDictTypeService {

    private final ISysDictTypeDao dictTypeDao;
    private final ISysDictDataDao dictDataDao;

    /**
     * 根据ID查询
     *
     * @param dictId 主键ID
     * @return 视图对象
     */
    @Override
    public SysDictTypeVo get(Long dictId) {
        SysDictType entity = dictTypeDao.getById(dictId);
        return MapstructUtils.convert(entity, SysDictTypeVo.class);
    }

    /**
     * 查询列表
     *
     * @param bo 查询参数
     * @return 列表数据
     */
    @Override
    public List<SysDictTypeVo> list(SysDictTypeBo bo) {
        PlusLambdaQuery<SysDictType> wrapper = dictTypeDao.buildQueryWrapper(bo);
        List<SysDictType> entities = dictTypeDao.list(wrapper);
        return MapstructUtils.convert(entities, SysDictTypeVo.class);
    }

    /**
     * 分页查询
     *
     * @param bo        查询参数
     * @param pageQuery 分页参数
     * @return 分页结果
     */
    @Override
    public PageResult<SysDictTypeVo> page(SysDictTypeBo bo, PageQuery pageQuery) {
        PlusLambdaQuery<SysDictType> wrapper = dictTypeDao.buildQueryWrapper(bo);
        PageResult<SysDictType> entityPage = dictTypeDao.page(wrapper, pageQuery);
        return entityPage.convert(SysDictTypeVo.class);
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
        return dictTypeDao.deleteByIds(ids);
    }

    /**
     * 批量保存
     *
     * @param boList 业务对象集合
     * @return 影响行数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchSave(List<SysDictTypeBo> boList) {
        if (CollUtil.isEmpty(boList)) {
            return 0;
        }
        List<SysDictType> entities = new ArrayList<>(boList.size());
        for (SysDictTypeBo bo : boList) {
            SysDictType entity = MapstructUtils.convert(bo, SysDictType.class);
            beforeSave(entity);
            entities.add(entity);
        }
        return dictTypeDao.batchSave(entities);
    }

    /**
     * 保存前的数据校验
     *
     * @param entity 待保存实体
     */
    protected void beforeSave(SysDictType entity) {
        // 超级管理员可以修改所有字典，包括系统级字典和 isSystem 属性
        if (LoginHelper.isSuperAdmin()) {
            return;
        }

        // 普通用户不能修改系统级字典
        if (entity.getDictId() != null) {
            SysDictType oldEntity = dictTypeDao.getById(entity.getDictId());
            if (oldEntity != null && "1".equals(oldEntity.getIsSystem())) {
                throw ServiceException.of("系统级字典不允许修改");
            }
        }

        // 普通用户不能修改 isSystem 属性，强制清空
        entity.setIsSystem(null);
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

        // 系统级字典不允许删除
        List<SysDictType> dictTypes = dictTypeDao.listByIds(ids);
        for (SysDictType dictType : dictTypes) {
            if ("1".equals(dictType.getIsSystem())) {
                throw ServiceException.of("系统级字典\"{}\"不允许删除", dictType.getDictName());
            }
        }
    }

    /**
     * 根据字典类型查询字典数据
     * <p>示例：{@code List<SysDictDataVo> dataList = selectDictDataByType("sys_user_gender")}</p>
     *
     * @param dictType 字典类型编码
     * @return 字典数据集合，如果不存在返回空列表
     */
    @Cacheable(cacheNames = CacheNames.SYS_DICT, key = "#dictType")
    @Override
    public List<SysDictDataVo> listDictDataByType(String dictType) {
        if (StringUtils.isBlank(dictType)) {
            return Collections.emptyList();
        }

        List<SysDictData> dictDataList = dictDataDao.listDictDataByType(dictType);
        if (CollUtil.isEmpty(dictDataList)) {
            return Collections.emptyList();
        }
        return MapstructUtils.convert(dictDataList, SysDictDataVo.class);
    }

    /**
     * 根据字典类型查询字典类型信息
     * <p>示例：{@code SysDictTypeVo dictType = selectDictTypeByType("sys_user_gender")}</p>
     *
     * @param dictType 字典类型编码
     * @return 字典类型信息，如果不存在返回null
     */
    @Cacheable(cacheNames = CacheNames.SYS_DICT_TYPE, key = "#dictType")
    @Override
    public SysDictTypeVo getDictTypeByType(String dictType) {
        if (StringUtils.isBlank(dictType)) {
            return null;
        }

        SysDictType entity = dictTypeDao.getByDictType(dictType);
        return MapstructUtils.convert(entity, SysDictTypeVo.class);
    }

    /**
     * 批量删除字典类型
     * <p>删除前会检查是否有关联的字典数据，如果有则不允许删除</p>
     *
     * @param dictIds 需要删除的字典类型ID数组
     * @throws ServiceException 当字典类型已分配字典数据时抛出
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDictTypeByIds(List<Long> dictIds) {
        if (CollUtil.isEmpty(dictIds)) {
            return;
        }
        List<SysDictType> dictTypeList = dictTypeDao.listByIds(dictIds);
        dictTypeList.forEach(sysDictType -> {
            boolean assigned = dictDataDao.existsByDictType(sysDictType.getDictType());
            if (assigned) {
                throw ServiceException.of("{}已分配,不能删除", sysDictType.getDictName());
            }
        });
        dictTypeDao.deleteByIds(dictIds);
        dictTypeList.forEach(sysDictType -> {
            evictDictCache(sysDictType.getDictType());
        });
    }

    /**
     * 重置字典缓存
     * <p>清空所有字典相关的缓存数据</p>
     */
    @Override
    public void resetDictCache() {
        CacheUtils.clear(CacheNames.SYS_DICT);
        CacheUtils.clear(CacheNames.SYS_DICT_TYPE);
    }

    /**
     * 新增字典类型
     * <p>示例：{@code List<SysDictDataVo> result = insertDictType(dictTypeBo)}</p>
     *
     * @param bo 字典类型业务对象
     * @return 空的字典数据列表（防止缓存穿透）
     * @throws ServiceException 操作失败时抛出
     */
    @CachePut(cacheNames = CacheNames.SYS_DICT, key = "#bo.dictType")
    @Override
    public List<SysDictDataVo> insertDictType(SysDictTypeBo bo) {
        SysDictType dictType = MapstructUtils.convert(bo, SysDictType.class);
        boolean success = dictTypeDao.insert(dictType) > 0;
        if (success) {
            bo.setDictId(dictType.getDictId());
            // 新增类型下无数据，返回空列表防止缓存穿透
            return Collections.emptyList();
        }
        throw ServiceException.of("新增字典类型失败");
    }

    /**
     * 修改字典类型
     * <p>更新字典类型信息，同时更新关联的字典数据的类型编码</p>
     *
     * @param bo 字典类型业务对象
     * @return 更新后的字典数据列表
     * @throws ServiceException 操作失败时抛出
     */
    @CachePut(cacheNames = CacheNames.SYS_DICT, key = "#bo.dictType")
    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<SysDictDataVo> updateDictType(SysDictTypeBo bo) {
        SysDictType newDictType = MapstructUtils.convert(bo, SysDictType.class);
        SysDictType oldDictType = dictTypeDao.getById(newDictType.getDictId());

        if (oldDictType == null) {
            throw ServiceException.of("字典类型不存在");
        }

        // 更新关联的字典数据
        if (!Objects.equals(oldDictType.getDictType(), newDictType.getDictType())) {
            dictDataDao.updateDictType(oldDictType.getDictType(), newDictType.getDictType());
        }

        // 更新字典类型
        boolean success = dictTypeDao.updateById(newDictType) > 0;
        if (success) {
            // 清理旧缓存
            evictDictCache(oldDictType.getDictType());

            // 返回新的字典数据
            List<SysDictData> dictDataList = dictDataDao.listDictDataByType(newDictType.getDictType());
            return MapstructUtils.convert(dictDataList, SysDictDataVo.class);
        }
        throw ServiceException.of("修改字典类型失败");
    }

    /**
     * 校验字典类型编码是否唯一
     * <p>示例：{@code boolean unique = checkDictTypeUnique(dictTypeBo)}</p>
     *
     * @param dictType 字典类型业务对象
     * @return true表示唯一，false表示已存在
     */
    @Override
    public boolean checkDictTypeUnique(SysDictTypeBo dictType) {
        return dictTypeDao.checkDictTypeUnique(dictType.getDictType(), dictType.getDictId());
    }

    /**
     * 根据字典类型和字典值获取字典标签
     * <p>支持单个值和多个值（分隔符分割）的标签获取</p>
     * <p>示例：</p>
     * <ul>
     *     <li>单值：{@code getDictLabel("sys_user_gender", "1", ",") → "男"}</li>
     *     <li>多值：{@code getDictLabel("sys_user_status", "1,2", ",") → "正常,停用"}</li>
     * </ul>
     *
     * @param dictType  字典类型编码
     * @param dictValue 字典值（单个值或多个值用分隔符分割）
     * @param separator 分隔符
     * @return 字典标签（单个标签或多个标签用分隔符连接），无效值返回空字符串
     */
    @Override
    public String getDictLabel(String dictType, String dictValue, String separator) {
        // 参数校验
        if (StringUtils.isBlank(dictType) || StringUtils.isBlank(dictValue)) {
            return StringUtils.EMPTY;
        }

        Map<String, String> valueToLabelMap = getDictValueToLabelMap(dictType);
        return StringUtils.convertWithMapping(dictValue, separator, valueToLabelMap);
    }

    /**
     * 根据字典类型和字典标签获取字典值
     * <p>示例：{@code getDictValue("sys_user_gender", "男", ",") → "1"}</p>
     *
     * @param dictType  字典类型编码
     * @param dictLabel 字典标签（单个标签或多个标签用分隔符分割）
     * @param separator 分隔符
     * @return 字典值（单个值或多个值用分隔符连接），无效标签返回空字符串
     */
    @Override
    public String getDictValue(String dictType, String dictLabel, String separator) {
        // 参数校验
        if (StringUtils.isBlank(dictType) || StringUtils.isBlank(dictLabel)) {
            return StringUtils.EMPTY;
        }

        Map<String, String> labelToValueMap = getDictLabelToValueMap(dictType);
        return StringUtils.convertWithMapping(dictLabel, separator, labelToValueMap);
    }

    /**
     * 获取字典类型下所有的字典值与标签映射
     * <p>示例：{@code Map<String, String> dictMap = getAllDictByDictType("sys_user_gender")}</p>
     * <p>返回格式：{"1": "男", "2": "女"}</p>
     *
     * @param dictType 字典类型编码
     * @return 字典值为key，字典标签为value的有序映射
     */
    @Override
    public Map<String, String> getAllDictByDictType(String dictType) {
        return getDictValueToLabelMap(dictType);
    }

    /**
     * 根据字典类型查询字典类型详细信息
     * <p>示例：{@code DictTypeDTO dictType = getDictType("sys_user_gender")}</p>
     *
     * @param dictType 字典类型编码
     * @return 字典类型详细信息，如果不存在返回null
     */
    @Override
    public DictTypeDTO getDictType(String dictType) {
        SysDictTypeVo vo = SpringUtils.getAopProxy(this).getDictTypeByType(dictType);
        return vo != null ? BeanUtil.toBean(vo, DictTypeDTO.class) : null;
    }

    /**
     * 根据字典类型查询字典数据列表
     * <p>示例：{@code List<DictDataDTO> dataList = getDictData("sys_user_gender")}</p>
     *
     * @param dictType 字典类型编码
     * @return 字典数据列表，如果不存在返回空列表
     */
    @Override
    public List<DictDataDTO> getDictData(String dictType) {
        List<SysDictDataVo> dataList = SpringUtils.getAopProxy(this).listDictDataByType(dictType);
        return BeanUtil.copyToList(dataList, DictDataDTO.class);
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 获取字典值到标签的映射（有序）
     *
     * @param dictType 字典类型编码
     * @return 字典值为key，字典标签为value的有序映射
     */
    private Map<String, String> getDictValueToLabelMap(String dictType) {
        List<SysDictDataVo> dataList = SpringUtils.getAopProxy(this).listDictDataByType(dictType);
        if (CollUtil.isEmpty(dataList)) {
            return Collections.emptyMap();
        }

        // 使用LinkedHashMap保持顺序
        return dataList.stream()
            .collect(Collectors.toMap(
                SysDictDataVo::getDictValue,
                SysDictDataVo::getDictLabel,
                // 保留第一个值
                (existing, replacement) -> existing,
                LinkedHashMap::new
            ));
    }

    /**
     * 获取字典标签到值的映射
     *
     * @param dictType 字典类型编码
     * @return 字典标签为key，字典值为value的映射
     */
    private Map<String, String> getDictLabelToValueMap(String dictType) {
        List<SysDictDataVo> dataList = SpringUtils.getAopProxy(this).listDictDataByType(dictType);
        return StreamUtils.toMap(dataList,
            SysDictDataVo::getDictLabel,
            SysDictDataVo::getDictValue);
    }

    /**
     * 清理指定字典类型的缓存
     *
     * @param dictType 字典类型编码
     */
    private void evictDictCache(String dictType) {
        CacheUtils.evict(CacheNames.SYS_DICT, dictType);
        CacheUtils.evict(CacheNames.SYS_DICT_TYPE, dictType);
    }

}
