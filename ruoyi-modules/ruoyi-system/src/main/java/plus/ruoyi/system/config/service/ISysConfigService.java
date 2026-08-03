package plus.ruoyi.system.config.service;

import plus.ruoyi.common.mybatis.core.page.PageQuery;
import plus.ruoyi.common.mybatis.core.page.PageResult;
import plus.ruoyi.system.config.domain.bo.SysConfigBo;
import plus.ruoyi.system.config.domain.vo.SysConfigVo;

import java.util.Collection;
import java.util.List;

/**
 * 参数配置 服务层
 *
 * @author Lion Li
 */
public interface ISysConfigService {

    /**
     * 根据ID查询
     *
     * @param configId 主键ID
     * @return 视图对象
     */
    SysConfigVo get(Long configId);

    /**
     * 查询列表
     *
     * @param bo 查询参数
     * @return 列表数据
     */
    List<SysConfigVo> list(SysConfigBo bo);

    /**
     * 分页查询
     *
     * @param bo        查询参数
     * @param pageQuery 分页参数
     * @return 分页结果
     */
    PageResult<SysConfigVo> page(SysConfigBo bo, PageQuery pageQuery);

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
    int batchSave(List<SysConfigBo> boList);

    /**
     * 根据键名查询参数配置信息
     *
     * @param configKey 参数键名
     * @return 参数键值
     */
    String getConfigByKey(String configKey);

    /**
     * 获取注册开关
     *
     * @param tenantId 租户id
     * @return true开启，false关闭
     */
    boolean getRegisterEnabled(String tenantId);

    /**
     * 新增参数配置
     *
     * @param bo 参数配置信息
     * @return 结果
     */
    String insertConfig(SysConfigBo bo);

    /**
     * 修改参数配置
     *
     * @param bo 参数配置信息
     * @return 结果
     */
    String updateConfig(SysConfigBo bo);

    /**
     * 批量删除参数信息
     *
     * @param configIds 需要删除的参数ID
     */
    void deleteConfigByIds(List<Long> configIds);

    /**
     * 清除参数缓存数据
     */
    void clearConfigCache();

    /**
     * 校验参数键名是否唯一
     *
     * @param config 参数信息
     * @return 结果
     */
    boolean checkConfigKeyUnique(SysConfigBo config);

}
