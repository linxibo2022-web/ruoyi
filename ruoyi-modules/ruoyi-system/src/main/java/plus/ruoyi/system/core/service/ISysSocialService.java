package plus.ruoyi.system.core.service;

import plus.ruoyi.common.mybatis.core.page.PageQuery;
import plus.ruoyi.common.mybatis.core.page.PageResult;
import plus.ruoyi.system.core.domain.bo.SysSocialBo;
import plus.ruoyi.system.core.domain.vo.SysSocialVo;

import java.util.Collection;
import java.util.List;

/**
 * 社会化关系Service接口
 *
 * @author thiszhc
 */
public interface ISysSocialService {

    /**
     * 根据ID查询
     *
     * @param id 主键ID
     * @return 社会化关系VO
     */
    SysSocialVo get(Long id);

    /**
     * 查询列表
     *
     * @param bo 业务对象
     * @return 社会化关系VO列表
     */
    List<SysSocialVo> list(SysSocialBo bo);

    /**
     * 分页查询
     *
     * @param bo 业务对象
     * @param pageQuery 分页参数
     * @return 分页结果
     */
    PageResult<SysSocialVo> page(SysSocialBo bo, PageQuery pageQuery);

    /**
     * 新增
     *
     * @param bo 业务对象
     * @return 主键ID
     */
    Long add(SysSocialBo bo);

    /**
     * 修改
     *
     * @param bo 业务对象
     * @return 影响行数
     */
    int update(SysSocialBo bo);

    /**
     * 批量删除
     *
     * @param ids 主键ID集合
     * @return 影响行数
     */
    int batchDelete(Collection<Long> ids);

    /**
     * 批量保存
     *
     * @param boList 业务对象列表
     * @return 影响行数
     */
    int batchSave(List<SysSocialBo> boList);

    /**
     * 根据用户ID查询社会化账号绑定列表
     *
     * @param userId 用户ID
     * @return 用户的社会化账号绑定列表
     */
    List<SysSocialVo> listSocialsByUserId(Long userId);

    /**
     * 根据第三方平台认证ID查询社会化关系列表
     *
     * @param authId 第三方平台的认证ID（如微信openId、QQ openId等）
     * @return 社会化关系列表
     */
    List<SysSocialVo> listSocialsByAuthId(String authId);

}
