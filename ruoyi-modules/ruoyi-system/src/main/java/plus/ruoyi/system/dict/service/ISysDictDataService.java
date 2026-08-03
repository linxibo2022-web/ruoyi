package plus.ruoyi.system.dict.service;

import plus.ruoyi.common.mybatis.core.page.PageQuery;
import plus.ruoyi.common.mybatis.core.page.PageResult;
import plus.ruoyi.system.dict.domain.bo.SysDictDataBo;
import plus.ruoyi.system.dict.domain.vo.SysDictDataVo;

import java.util.Collection;
import java.util.List;

/**
 * 字典 业务层
 *
 * @author Lion Li
 */
public interface ISysDictDataService {

    /**
     * 根据ID查询
     *
     * @param dictDataId 主键ID
     * @return 视图对象
     */
    SysDictDataVo get(Long dictDataId);

    /**
     * 查询列表
     *
     * @param bo 查询参数
     * @return 列表数据
     */
    List<SysDictDataVo> list(SysDictDataBo bo);

    /**
     * 分页查询
     *
     * @param bo        查询参数
     * @param pageQuery 分页参数
     * @return 分页结果
     */
    PageResult<SysDictDataVo> page(SysDictDataBo bo, PageQuery pageQuery);

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
    int batchSave(List<SysDictDataBo> boList);

    /**
     * 根据字典类型和字典键值查询字典数据信息
     *
     * @param dictType  字典类型
     * @param dictValue 字典键值
     * @return 字典标签
     */
    String getDictLabel(String dictType, String dictValue);

    /**
     * 批量删除字典数据信息
     *
     * @param dictDataIds 需要删除的字典数据ID
     */
    void deleteDictDataByIds(List<Long> dictDataIds);

    /**
     * 新增保存字典数据信息
     *
     * @param bo 字典数据信息
     * @return 结果
     */
    List<SysDictDataVo> insertDictData(SysDictDataBo bo);

    /**
     * 修改保存字典数据信息
     *
     * @param bo 字典数据信息
     * @return 结果
     */
    List<SysDictDataVo> updateDictData(SysDictDataBo bo);

    /**
     * 校验字典键值是否唯一
     *
     * @param dict 字典数据
     * @return 结果
     */
    boolean checkDictDataUnique(SysDictDataBo dict);

    /**
     * 根据字典类型和字典标签查询字典数据
     *
     * @param dictType  字典类型
     * @param dictLabel 字典标签
     * @return 字典数据
     */
    SysDictDataVo getDictDataByTypeAndLabel(String dictType, String dictLabel);
}
