package plus.ruoyi.system.config.dao;

import plus.ruoyi.common.mybatis.core.dao.IBaseDao;
import plus.ruoyi.common.mybatis.core.page.PageQuery;
import plus.ruoyi.common.mybatis.core.page.PageResult;
import plus.ruoyi.common.mybatis.core.query.PlusLambdaQuery;
import plus.ruoyi.system.config.domain.SysNotice;
import plus.ruoyi.system.config.domain.bo.SysNoticeBo;

import java.util.List;

/**
 * 通知公告DAO接口
 *
 * @author 抓蛙师
 */
public interface ISysNoticeDao extends IBaseDao<SysNotice> {

    /**
     * 根据业务对象构建查询条件
     */
    PlusLambdaQuery<SysNotice> buildQueryWrapper(SysNoticeBo bo);

    /**
     * 分页获取用户的公告列表
     *
     * @param userId    用户ID
     * @param pageQuery 分页参数
     * @return 分页结果（Entity）
     */
    PageResult<SysNotice> pageNoticesByUserId(Long userId, PageQuery pageQuery);

    /**
     * 获取用户某公告详情
     *
     * @param noticeId 公告ID
     * @param userId   用户ID
     * @return 公告实体
     */
    SysNotice getUserNoticeDetail(Long noticeId, Long userId);

    /**
     * 获取用户未读数量
     *
     * @param userId 用户ID
     * @return 未读数量
     */
    Long getUnreadCountByUserId(Long userId);

    /**
     * 获取用户的所有未读公告
     *
     * @param userId 用户ID
     * @return 未读公告列表
     */
    List<SysNotice> listUnreadNoticesByUserId(Long userId);
}
