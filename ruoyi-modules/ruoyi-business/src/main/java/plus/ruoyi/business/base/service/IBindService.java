package plus.ruoyi.business.base.service;

import plus.ruoyi.business.base.domain.bo.BindBo;
import plus.ruoyi.business.base.domain.vo.BindVo;
import plus.ruoyi.business.base.domain.vo.PlatformUserInfoVo;
import plus.ruoyi.common.mybatis.core.page.PageQuery;
import plus.ruoyi.common.mybatis.core.page.PageResult;

import java.util.Collection;
import java.util.List;

/**
 * 账号绑定服务接口
 *
 * @author 抓蛙师
 */
public interface IBindService {

    /**
     * 根据ID查询
     *
     * @param id 主键ID
     * @return 视图对象
     */
    BindVo get(Long id);

    /**
     * 查询列表
     *
     * @param bo 查询参数
     * @return 列表数据
     */
    List<BindVo> list(BindBo bo);

    /**
     * 分页查询
     *
     * @param bo        查询参数
     * @param pageQuery 分页参数
     * @return 分页结果
     */
    PageResult<BindVo> page(BindBo bo, PageQuery pageQuery);

    /**
     * 新增
     *
     * @param bo 业务对象
     * @return 主键ID
     */
    Long add(BindBo bo);

    /**
     * 修改
     *
     * @param bo 业务对象
     * @return 影响行数
     */
    int update(BindBo bo);

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
    int batchSave(List<BindBo> boList);

    /**
     * 根据小程序用户信息获取或者新建绑定信息
     *
     * @param platformUserInfoVo 小程序用户信息
     * @return 绑定信息
     */
    BindVo getOrCreateBind(PlatformUserInfoVo platformUserInfoVo);

    /**
     * 更新绑定信息
     *
     * @param bindBo 绑定信息
     * @param userId 用户ID
     */
    void updateBindUserId(BindBo bindBo, Long userId);
}
