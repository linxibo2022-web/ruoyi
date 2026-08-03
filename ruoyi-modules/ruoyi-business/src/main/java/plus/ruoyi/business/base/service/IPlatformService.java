package plus.ruoyi.business.base.service;

import plus.ruoyi.business.base.domain.bo.PlatformBo;
import plus.ruoyi.business.base.domain.vo.PlatformVo;
import plus.ruoyi.business.base.domain.vo.TemplateConfig;
import plus.ruoyi.common.mybatis.core.page.PageQuery;
import plus.ruoyi.common.mybatis.core.page.PageResult;

import java.util.Collection;
import java.util.List;

/**
 * 平台配置服务接口
 *
 * @author 抓蛙师
 */
public interface IPlatformService {

    /**
     * 根据ID查询
     *
     * @param id 主键ID
     * @return 视图对象
     */
    PlatformVo get(Long id);

    /**
     * 查询列表
     *
     * @param bo 查询参数
     * @return 列表数据
     */
    List<PlatformVo> list(PlatformBo bo);

    /**
     * 分页查询
     *
     * @param bo        查询参数
     * @param pageQuery 分页参数
     * @return 分页结果
     */
    PageResult<PlatformVo> page(PlatformBo bo, PageQuery pageQuery);

    /**
     * 新增
     *
     * @param bo 业务对象
     * @return 主键ID
     */
    Long add(PlatformBo bo);

    /**
     * 修改
     *
     * @param bo 业务对象
     * @return 影响行数
     */
    int update(PlatformBo bo);

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
    int batchSave(List<PlatformBo> boList);

    /**
     * 根据appid获取订阅消息模板配置
     *
     * @param appid 小程序appid
     * @return 订阅消息模板配置列表（只返回启用的）
     */
    List<TemplateConfig> getTemplateConfigs(String appid);
}
