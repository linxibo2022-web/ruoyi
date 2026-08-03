package plus.ruoyi.system.openapi.service;

import plus.ruoyi.common.core.service.OpenApiService;
import plus.ruoyi.common.mybatis.core.page.PageQuery;
import plus.ruoyi.common.mybatis.core.page.PageResult;
import plus.ruoyi.system.openapi.domain.bo.SysApiKeyBo;
import plus.ruoyi.system.openapi.domain.vo.OpenApiSecretVo;
import plus.ruoyi.system.openapi.domain.vo.SysApiKeyVo;

import java.util.Collection;
import java.util.List;

/**
 * API密钥Service接口
 *
 * @author 抓蛙师
 * @date 2025-10-03
 */
public interface ISysApiKeyService extends OpenApiService {

    /**
     * 根据ID查询
     *
     * @param id 主键ID
     * @return 视图对象
     */
    SysApiKeyVo get(Long id);

    /**
     * 查询列表
     *
     * @param bo 查询参数
     * @return 列表数据
     */
    List<SysApiKeyVo> list(SysApiKeyBo bo);

    /**
     * 分页查询
     *
     * @param bo        查询参数
     * @param pageQuery 分页参数
     * @return 分页结果
     */
    PageResult<SysApiKeyVo> page(SysApiKeyBo bo, PageQuery pageQuery);

    /**
     * 新增
     *
     * @param bo 业务对象
     * @return 主键ID
     */
    Long add(SysApiKeyBo bo);

    /**
     * 修改
     *
     * @param bo 业务对象
     * @return 影响行数
     */
    int update(SysApiKeyBo bo);

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
    int batchSave(List<SysApiKeyBo> boList);

    /**
     * 统计数量
     *
     * @param bo 查询参数
     * @return 统计结果
     */
    Long count(SysApiKeyBo bo);

    /**
     * 生成新的API密钥
     *
     * @param bo 业务对象
     * @return 密钥详情(包含明文Secret)
     */
    OpenApiSecretVo generate(SysApiKeyBo bo);

    /**
     * 重置密钥(重新生成AppSecret)
     *
     * @param id 密钥ID
     * @return 新的密钥详情
     */
    OpenApiSecretVo resetSecret(Long id);
}
