package plus.ruoyi.system.oss.service;

import plus.ruoyi.common.core.service.OssConfigService;
import plus.ruoyi.common.mybatis.core.page.PageQuery;
import plus.ruoyi.common.mybatis.core.page.PageResult;
import plus.ruoyi.system.oss.domain.bo.SysOssConfigBo;
import plus.ruoyi.system.oss.domain.vo.SysOssConfigVo;

import java.util.Collection;
import java.util.List;

/**
 * 对象存储配置Service接口
 *
 * @author Lion Li
 * @author 孤舟烟雨
 * @date 2021-08-13
 */
public interface ISysOssConfigService extends OssConfigService {

    /**
     * 根据ID查询
     *
     * @param ossConfigId 主键ID
     * @return 视图对象
     */
    SysOssConfigVo get(Long ossConfigId);

    /**
     * 查询列表
     *
     * @param bo 查询参数
     * @return 列表数据
     */
    List<SysOssConfigVo> list(SysOssConfigBo bo);

    /**
     * 分页查询
     *
     * @param bo        查询参数
     * @param pageQuery 分页参数
     * @return 分页结果
     */
    PageResult<SysOssConfigVo> page(SysOssConfigBo bo, PageQuery pageQuery);

    /**
     * 新增
     *
     * @param bo 业务对象
     * @return 主键ID
     */
    Long add(SysOssConfigBo bo);

    /**
     * 修改
     *
     * @param bo 业务对象
     * @return 影响行数
     */
    int update(SysOssConfigBo bo);

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
    int batchSave(List<SysOssConfigBo> boList);

    /**
     * 根据ID集合查询
     *
     * @param ids ID集合
     * @return 视图对象列表
     */
    List<SysOssConfigVo> listByIds(Collection<Long> ids);

    /**
     * 启用停用状态
     */
    boolean updateOssConfigStatus(SysOssConfigBo bo);

}
