package plus.ruoyi.system.dict.dao.impl;

import org.springframework.stereotype.Repository;
import plus.ruoyi.common.core.utils.StringUtils;
import plus.ruoyi.common.mybatis.core.dao.impl.BaseDaoImpl;
import plus.ruoyi.common.mybatis.core.query.PlusLambdaQuery;
import plus.ruoyi.system.dict.dao.ISysDictTypeDao;
import plus.ruoyi.system.dict.domain.SysDictType;
import plus.ruoyi.system.dict.domain.bo.SysDictTypeBo;
import plus.ruoyi.system.dict.mapper.SysDictTypeMapper;

import java.util.List;
import java.util.Map;

/**
 * 字典类型数据访问实现
 *
 * @author 抓蛙师
 */
@Repository
public class SysDictTypeDaoImpl extends BaseDaoImpl<SysDictTypeMapper, SysDictType> implements ISysDictTypeDao {

    /**
     * 构建查询条件
     *
     * @param bo 查询参数
     * @return 查询条件
     */
    @Override
    public PlusLambdaQuery<SysDictType> buildQueryWrapper(SysDictTypeBo bo) {
        Map<String, Object> params = bo.getParams();
        PlusLambdaQuery<SysDictType> lqw = PlusLambdaQuery.of(SysDictType.class);

        // 精确匹配查询条件
        lqw.like(SysDictType::getDictName, bo.getDictName());
        lqw.like(SysDictType::getDictType, bo.getDictType());
        lqw.eq(SysDictType::getStatus, bo.getStatus());
        lqw.between(params.get("beginTime") != null && params.get("endTime") != null,
            SysDictType::getCreateTime, params.get("beginTime"), params.get("endTime"));
        lqw.orderByAsc(SysDictType::getDictId);

        // 模糊查询（跨数据库兼容：String 类型用 like，非 String 类型用 likeCast）
        String searchValue = bo.getSearchValue();
        if (StringUtils.isNotBlank(searchValue)) {
            lqw.and(w -> w
                .like(SysDictType::getDictName, searchValue)         // String
                .or().like(SysDictType::getDictType, searchValue)    // String
                .or().like(SysDictType::getRemark, searchValue)      // String
                .or().likeCast(SysDictType::getDictId, searchValue)); // Long
        }
        return lqw;
    }

    /**
     * 根据字典类型查询字典类型信息
     *
     * @param dictType 字典类型
     * @return 字典类型实体
     */
    @Override
    public SysDictType getByDictType(String dictType) {
        PlusLambdaQuery<SysDictType> lqw = PlusLambdaQuery.of(SysDictType.class)
            .eq(SysDictType::getDictType, dictType);
        return getOne(lqw);
    }

    /**
     * 校验字典类型是否唯一
     *
     * @param dictType 字典类型
     * @param dictId   排除的字典类型ID
     * @return 是否唯一
     */
    @Override
    public boolean checkDictTypeUnique(String dictType, Long dictId) {
        PlusLambdaQuery<SysDictType> lqw = PlusLambdaQuery.of(SysDictType.class)
            .eq(SysDictType::getDictType, dictType)
            .ne(SysDictType::getDictId, dictId);
        return !exists(lqw);
    }

    /**
     * 根据租户ID查询所有字典类型
     *
     * @param tenantId 租户ID
     * @return 字典类型列表
     */
    @Override
    public List<SysDictType> listByTenantId(String tenantId) {
        PlusLambdaQuery<SysDictType> lqw = PlusLambdaQuery.of(SysDictType.class)
            .eq(SysDictType::getTenantId, tenantId);
        return list(lqw);
    }

}
