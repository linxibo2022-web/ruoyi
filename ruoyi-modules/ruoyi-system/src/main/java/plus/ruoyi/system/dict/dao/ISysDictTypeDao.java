package plus.ruoyi.system.dict.dao;

import plus.ruoyi.common.mybatis.core.dao.IBaseDao;
import plus.ruoyi.common.mybatis.core.query.PlusLambdaQuery;
import plus.ruoyi.system.dict.domain.SysDictType;
import plus.ruoyi.system.dict.domain.bo.SysDictTypeBo;

import java.util.List;

/**
 * 字典类型DAO接口
 *
 * @author 抓蛙师
 */
public interface ISysDictTypeDao extends IBaseDao<SysDictType> {

    /**
     * 根据业务对象构建查询条件
     */
    PlusLambdaQuery<SysDictType> buildQueryWrapper(SysDictTypeBo bo);

    /**
     * 根据字典类型查询字典类型信息
     *
     * @param dictType 字典类型
     * @return 字典类型实体
     */
    SysDictType getByDictType(String dictType);

    /**
     * 校验字典类型是否唯一
     *
     * @param dictType 字典类型
     * @param dictId   排除的字典类型ID
     * @return 是否唯一
     */
    boolean checkDictTypeUnique(String dictType, Long dictId);

    /**
     * 根据租户ID查询所有字典类型
     *
     * @param tenantId 租户ID
     * @return 字典类型列表
     */
    List<SysDictType> listByTenantId(String tenantId);
}
