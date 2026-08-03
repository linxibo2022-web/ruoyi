package plus.ruoyi.system.dict.service;

import plus.ruoyi.common.core.service.DictService;
import plus.ruoyi.common.mybatis.core.page.PageQuery;
import plus.ruoyi.common.mybatis.core.page.PageResult;
import plus.ruoyi.system.dict.domain.bo.SysDictTypeBo;
import plus.ruoyi.system.dict.domain.vo.SysDictDataVo;
import plus.ruoyi.system.dict.domain.vo.SysDictTypeVo;

import java.util.Collection;
import java.util.List;

/**
 * 字典 业务层
 *
 * @author Lion Li
 */
public interface ISysDictTypeService extends DictService {

    /**
     * 根据ID查询
     *
     * @param dictId 主键ID
     * @return 视图对象
     */
    SysDictTypeVo get(Long dictId);

    /**
     * 查询列表
     *
     * @param bo 查询参数
     * @return 列表数据
     */
    List<SysDictTypeVo> list(SysDictTypeBo bo);

    /**
     * 分页查询
     *
     * @param bo        查询参数
     * @param pageQuery 分页参数
     * @return 分页结果
     */
    PageResult<SysDictTypeVo> page(SysDictTypeBo bo, PageQuery pageQuery);

    /**
     * 批量删除
     *
     * @param ids ID集合
     * @return 影响行数
     */
    int batchDelete(Collection<Long> ids);

    /**
     * 批量保存
     *
     * @param boList 业务对象集合
     * @return 影响行数
     */
    int batchSave(List<SysDictTypeBo> boList);

    /**
     * 根据字典类型查询字典数据
     *
     * @param dictType 字典类型
     * @return 字典数据集合信息
     */
    List<SysDictDataVo> listDictDataByType(String dictType);

    /**
     * 根据字典类型查询信息
     *
     * @param dictType 字典类型
     * @return 字典类型
     */
    SysDictTypeVo getDictTypeByType(String dictType);

    /**
     * 批量删除字典信息
     *
     * @param dictIds 需要删除的字典ID
     */
    void deleteDictTypeByIds(List<Long> dictIds);

    /**
     * 重置字典缓存数据
     */
    void resetDictCache();

    /**
     * 新增保存字典类型信息
     *
     * @param bo 字典类型信息
     * @return 结果
     */
    List<SysDictDataVo> insertDictType(SysDictTypeBo bo);

    /**
     * 修改保存字典类型信息
     *
     * @param bo 字典类型信息
     * @return 结果
     */
    List<SysDictDataVo> updateDictType(SysDictTypeBo bo);

    /**
     * 校验字典类型称是否唯一
     *
     * @param dictType 字典类型
     * @return 结果
     */
    boolean checkDictTypeUnique(SysDictTypeBo dictType);
}
