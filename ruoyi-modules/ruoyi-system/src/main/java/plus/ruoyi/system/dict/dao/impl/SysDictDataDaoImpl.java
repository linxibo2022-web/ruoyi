package plus.ruoyi.system.dict.dao.impl;

import cn.hutool.core.util.ObjectUtil;
import org.springframework.stereotype.Repository;
import plus.ruoyi.common.mybatis.core.dao.impl.BaseDaoImpl;
import plus.ruoyi.common.mybatis.core.query.PlusLambdaQuery;
import plus.ruoyi.system.dict.dao.ISysDictDataDao;
import plus.ruoyi.system.dict.domain.SysDictData;
import plus.ruoyi.system.dict.domain.bo.SysDictDataBo;
import plus.ruoyi.system.dict.mapper.SysDictDataMapper;

import java.util.List;

/**
 * 字典数据访问实现
 *
 * @author 抓蛙师
 */
@Repository
public class SysDictDataDaoImpl extends BaseDaoImpl<SysDictDataMapper, SysDictData> implements ISysDictDataDao {

    /**
     * 构建查询条件
     *
     * @param bo 查询参数
     * @return 查询条件
     */
    @Override
    public PlusLambdaQuery<SysDictData> buildQueryWrapper(SysDictDataBo bo) {
        PlusLambdaQuery<SysDictData> lqw = PlusLambdaQuery.of(SysDictData.class);

        // 精确匹配查询条件
        lqw.eq(SysDictData::getDictSort, bo.getDictSort());
        lqw.like(SysDictData::getDictLabel, bo.getDictLabel());
        lqw.eq(SysDictData::getDictType, bo.getDictType());
        lqw.eq(SysDictData::getStatus, bo.getStatus());
        lqw.orderByAsc(SysDictData::getDictSort);

        // 模糊查询（跨数据库兼容：String 类型用 like，非 String 类型用 likeCast）
        String searchValue = bo.getSearchValue();
        if (ObjectUtil.isNotEmpty(searchValue)) {
            lqw.and(w -> w
                .like(SysDictData::getDictLabel, searchValue)            // String
                .or().like(SysDictData::getDictValue, searchValue)       // String
                .or().like(SysDictData::getDictType, searchValue)        // String
                .or().like(SysDictData::getRemark, searchValue)          // String
                .or().likeCast(SysDictData::getDictDataId, searchValue)); // Long
        }
        return lqw;
    }

    /**
     * 根据字典类型和字典键值查询字典数据
     *
     * @param dictType  字典类型
     * @param dictValue 字典键值
     * @return 字典数据
     */
    @Override
    public SysDictData getByTypeAndValue(String dictType, String dictValue) {
        PlusLambdaQuery<SysDictData> lqw = PlusLambdaQuery.of(SysDictData.class)
            .select(SysDictData::getDictLabel)
            .eq(SysDictData::getDictType, dictType)
            .eq(SysDictData::getDictValue, dictValue);
        return getOne(lqw);
    }

    /**
     * 根据字典类型和字典标签查询字典数据
     *
     * @param dictType  字典类型
     * @param dictLabel 字典标签
     * @return 字典数据
     */
    @Override
    public SysDictData getByTypeAndLabel(String dictType, String dictLabel) {
        PlusLambdaQuery<SysDictData> lqw = PlusLambdaQuery.of(SysDictData.class)
            .eq(SysDictData::getDictType, dictType)
            .eq(SysDictData::getDictLabel, dictLabel);
        return getOne(lqw);
    }

    /**
     * 校验字典键值是否唯一
     *
     * @param dictType   字典类型
     * @param dictValue  字典键值
     * @param dictDataId 排除的字典数据ID
     * @return 是否唯一
     */
    @Override
    public boolean checkDictValueUnique(String dictType, String dictValue, Long dictDataId) {
        PlusLambdaQuery<SysDictData> lqw = PlusLambdaQuery.of(SysDictData.class)
            .eq(SysDictData::getDictType, dictType)
            .eq(SysDictData::getDictValue, dictValue)
            .ne(SysDictData::getDictDataId, dictDataId);
        return !exists(lqw);
    }

    /**
     * 根据字典类型查询字典数据列表
     *
     * @param dictType 字典类型
     * @return 字典数据列表
     */
    @Override
    public List<SysDictData> listDictDataByType(String dictType) {
        PlusLambdaQuery<SysDictData> lqw = PlusLambdaQuery.of(SysDictData.class)
            .eq(SysDictData::getDictType, dictType)
            .orderByAsc(SysDictData::getDictSort);
        return list(lqw);
    }

    /**
     * 批量更新字典数据的字典类型
     *
     * @param oldDictType 旧字典类型
     * @param newDictType 新字典类型
     * @return 更新数量
     */
    @Override
    public int updateDictType(String oldDictType, String newDictType) {
        return lambdaUpdate()
            .set(SysDictData::getDictType, newDictType)
            .eq(SysDictData::getDictType, oldDictType)
            .update();
    }

    /**
     * 检查字典类型下是否存在字典数据
     *
     * @param dictType 字典类型
     * @return 是否存在
     */
    @Override
    public boolean existsByDictType(String dictType) {
        PlusLambdaQuery<SysDictData> lqw = PlusLambdaQuery.of(SysDictData.class)
            .eq(SysDictData::getDictType, dictType);
        return exists(lqw);
    }

    /**
     * 根据租户ID查询所有字典数据
     *
     * @param tenantId 租户ID
     * @return 字典数据列表
     */
    @Override
    public List<SysDictData> listByTenantId(String tenantId) {
        PlusLambdaQuery<SysDictData> lqw = PlusLambdaQuery.of(SysDictData.class)
            .eq(SysDictData::getTenantId, tenantId);
        return list(lqw);
    }

}
