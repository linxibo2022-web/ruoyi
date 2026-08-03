package plus.ruoyi.system.dict.dao;

import plus.ruoyi.common.mybatis.core.dao.IBaseDao;
import plus.ruoyi.common.mybatis.core.query.PlusLambdaQuery;
import plus.ruoyi.system.dict.domain.SysDictData;
import plus.ruoyi.system.dict.domain.bo.SysDictDataBo;

import java.util.List;

/**
 * 字典数据DAO接口
 *
 * @author 抓蛙师
 */
public interface ISysDictDataDao extends IBaseDao<SysDictData> {

    /**
     * 根据业务对象构建查询条件
     */
    PlusLambdaQuery<SysDictData> buildQueryWrapper(SysDictDataBo bo);

    /**
     * 根据字典类型和字典键值查询字典数据
     *
     * @param dictType  字典类型
     * @param dictValue 字典键值
     * @return 字典数据
     */
    SysDictData getByTypeAndValue(String dictType, String dictValue);

    /**
     * 根据字典类型和字典标签查询字典数据
     *
     * @param dictType  字典类型
     * @param dictLabel 字典标签
     * @return 字典数据
     */
    SysDictData getByTypeAndLabel(String dictType, String dictLabel);

    /**
     * 校验字典键值是否唯一
     *
     * @param dictType   字典类型
     * @param dictValue  字典键值
     * @param dictDataId 排除的字典数据ID
     * @return 是否唯一
     */
    boolean checkDictValueUnique(String dictType, String dictValue, Long dictDataId);

    /**
     * 根据字典类型查询字典数据列表
     *
     * @param dictType 字典类型
     * @return 字典数据列表
     */
    List<SysDictData> listDictDataByType(String dictType);

    /**
     * 批量更新字典数据的字典类型
     *
     * @param oldDictType 旧字典类型
     * @param newDictType 新字典类型
     * @return 更新数量
     */
    int updateDictType(String oldDictType, String newDictType);

    /**
     * 检查字典类型下是否存在字典数据
     *
     * @param dictType 字典类型
     * @return 是否存在
     */
    boolean existsByDictType(String dictType);

    /**
     * 根据租户ID查询所有字典数据
     *
     * @param tenantId 租户ID
     * @return 字典数据列表
     */
    List<SysDictData> listByTenantId(String tenantId);
}
