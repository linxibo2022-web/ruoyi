package plus.ruoyi.system.config.service;

import plus.ruoyi.common.mybatis.core.page.PageQuery;
import plus.ruoyi.common.mybatis.core.page.PageResult;
import plus.ruoyi.system.config.domain.bo.SysNoticeBo;
import plus.ruoyi.system.config.domain.vo.SysNoticeVo;
import plus.ruoyi.system.config.domain.vo.UserNoticeVo;

import java.util.Collection;
import java.util.List;

/**
 * 公告 服务层
 *
 * @author Lion Li
 */
public interface ISysNoticeService {

    /**
     * 根据ID查询
     *
     * @param noticeId 主键ID
     * @return 视图对象
     */
    SysNoticeVo get(Long noticeId);

    /**
     * 查询列表
     *
     * @param bo 查询参数
     * @return 列表数据
     */
    List<SysNoticeVo> list(SysNoticeBo bo);

    /**
     * 分页查询
     *
     * @param bo        查询参数
     * @param pageQuery 分页参数
     * @return 分页结果
     */
    PageResult<SysNoticeVo> page(SysNoticeBo bo, PageQuery pageQuery);

    /**
     * 新增
     *
     * @param bo 业务对象
     * @return 主键ID
     */
    Long add(SysNoticeBo bo);

    /**
     * 修改
     *
     * @param bo 业务对象
     * @return 影响行数
     */
    int update(SysNoticeBo bo);

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
    int batchSave(List<SysNoticeBo> boList);

    /**
     * 处理推送配置，生成目标配置和用户ID列表
     *
     * @param bo 业务对象
     */
    void processTargetConfig(SysNoticeBo bo);

    /**
     * 发送公告通知
     *
     * @param bo 公告业务对象
     */
    void sendNoticeNotification(SysNoticeBo bo);

    /**
     * 分页获取用户的公告列表
     *
     * @param userId    用户ID
     * @param pageQuery 分页参数
     * @return 分页结果
     */
    PageResult<UserNoticeVo> pageNoticesByUserId(Long userId, PageQuery pageQuery);

    /**
     * 获取用户公告详情
     *
     * @param noticeId 公告ID
     * @param userId   用户ID
     * @return 公告详情
     */
    UserNoticeVo getUserNoticeDetail(Long noticeId, Long userId);

    /**
     * 获取用户未读通知数量
     *
     * @param userId 用户ID
     * @return 未读数量
     */
    Long getUnreadCountByUserId(Long userId);

    /**
     * 标记公告为已读
     *
     * @param noticeId 公告ID
     * @param userId   用户ID
     * @return 是否成功
     */
    boolean markAsRead(Long noticeId, Long userId);

    /**
     * 标记所有公告为已读
     *
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean markAllAsRead(Long userId);
}
