package plus.ruoyi.system.config.dao.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.stereotype.Repository;
import plus.ruoyi.common.core.dict.DictNoticeStatus;
import plus.ruoyi.common.core.utils.StringUtils;
import plus.ruoyi.common.mybatis.annotation.DataColumn;
import plus.ruoyi.common.mybatis.annotation.DataPermission;
import plus.ruoyi.common.mybatis.core.dao.impl.BaseDaoImpl;
import plus.ruoyi.common.mybatis.core.page.PageQuery;
import plus.ruoyi.common.mybatis.core.page.PageResult;
import plus.ruoyi.common.mybatis.core.query.PlusLambdaQuery;
import plus.ruoyi.common.mybatis.core.query.PlusQuery;
import plus.ruoyi.common.mybatis.helper.DataBaseHelper;
import plus.ruoyi.system.config.dao.ISysNoticeDao;
import plus.ruoyi.system.config.domain.SysNotice;
import plus.ruoyi.system.config.domain.bo.SysNoticeBo;
import plus.ruoyi.system.config.mapper.SysNoticeMapper;

import java.util.List;

/**
 * 通知公告数据访问实现
 *
 * @author 抓蛙师
 */
@Repository
public class SysNoticeDaoImpl extends BaseDaoImpl<SysNoticeMapper, SysNotice> implements ISysNoticeDao {

    /**
     * 构建查询条件
     *
     * @param bo 查询参数
     * @return 查询条件
     */
    @Override
    public PlusLambdaQuery<SysNotice> buildQueryWrapper(SysNoticeBo bo) {
        PlusLambdaQuery<SysNotice> lqw = PlusLambdaQuery.of(SysNotice.class);

        // 精确匹配查询条件
        lqw.like(SysNotice::getNoticeTitle, bo.getNoticeTitle());
        lqw.eq(SysNotice::getNoticeType, bo.getNoticeType());
        lqw.eq(SysNotice::getTargetUserIds, bo.getTargetUserIds());
        lqw.eq(SysNotice::getReadUserIds, bo.getReadUserIds());
        // 根据创建人ID查询（由Service层转换createByName为createById）
        lqw.eq(SysNotice::getCreateBy, bo.getCreateBy());

        // 模糊查询（跨数据库兼容：String 类型用 like，非 String 类型用 likeCast）
        String searchValue = bo.getSearchValue();
        if (StringUtils.isNotBlank(searchValue)) {
            lqw.and(w -> w
                .like(SysNotice::getNoticeTitle, searchValue)      // String
                .or().like(SysNotice::getNoticeContent, searchValue) // String
                .or().like(SysNotice::getTargetUserIds, searchValue) // String
                .or().like(SysNotice::getReadUserIds, searchValue)   // String
                .or().likeCast(SysNotice::getCreateBy, searchValue)  // Long
                .or().likeCast(SysNotice::getNoticeId, searchValue)  // Long
            );
        }
        return lqw;
    }

    /**
     * 分页获取用户的公告列表
     *
     * @param userId    用户ID
     * @param pageQuery 分页参数
     * @return 分页结果（Entity）
     */
    @Override
    public PageResult<SysNotice> pageNoticesByUserId(Long userId, PageQuery pageQuery) {
        PlusQuery<SysNotice> wrapper = PlusQuery.of(SysNotice.class)
            .eq("status", DictNoticeStatus.SEND_IMMEDIATELY.getValue())
            .apply(DataBaseHelper.findInSet(userId, "target_user_ids"))
            .orderByDesc("create_time");

        return page(wrapper.lambda(), pageQuery);
    }

    /**
     * 获取用户某公告详情
     *
     * @param noticeId 公告ID
     * @param userId   用户ID
     * @return 公告实体
     */
    @Override
    public SysNotice getUserNoticeDetail(Long noticeId, Long userId) {
        PlusQuery<SysNotice> wrapper = PlusQuery.of(SysNotice.class)
            .eq("notice_id", noticeId)
            .eq("status", DictNoticeStatus.SEND_IMMEDIATELY.getValue())
            .apply(DataBaseHelper.findInSet(userId, "target_user_ids"));

        return baseMapper.selectOne(wrapper);
    }

    /**
     * 获取用户未读数量
     *
     * @param userId 用户ID
     * @return 未读数量
     */
    @Override
    public Long getUnreadCountByUserId(Long userId) {
        PlusQuery<SysNotice> wrapper = PlusQuery.of(SysNotice.class)
            .eq("status", DictNoticeStatus.SEND_IMMEDIATELY.getValue())
            .apply(DataBaseHelper.findInSet(userId, "target_user_ids"))
            .apply(DataBaseHelper.findNotInSet(userId, "read_user_ids"));
        return baseMapper.selectCount(wrapper);
    }

    /**
     * 获取用户的所有未读公告
     *
     * @param userId 用户ID
     * @return 未读公告列表
     */
    @Override
    public List<SysNotice> listUnreadNoticesByUserId(Long userId) {
        PlusQuery<SysNotice> wrapper = PlusQuery.of(SysNotice.class)
            .eq("status", DictNoticeStatus.SEND_IMMEDIATELY.getValue())
            .apply(DataBaseHelper.findInSet(userId, "target_user_ids"))
            .apply(DataBaseHelper.findNotInSet(userId, "read_user_ids"));
        return baseMapper.selectList(wrapper);
    }

    // ==================== 带数据权限的通用方法（重写父类方法） ====================

    /**
     * 条件查询列表（带数据权限）
     * <p>
     * 默认带数据权限过滤，需要忽略权限时请使用 DataPermissionHelper.ignore() 包装调用
     * <p>
     * 数据权限说明：
     * - deptName -> create_dept: 限制只能查看自己部门创建的公告
     * - userName -> create_by: 限制只能查看自己创建的公告
     *
     * @param wrapper 查询条件
     * @return 公告列表
     */
    @DataPermission({
        @DataColumn(key = "deptName", value = "create_dept"),
        @DataColumn(key = "userName", value = "create_by")
    })
    @Override
    public List<SysNotice> list(PlusLambdaQuery<SysNotice> wrapper) {
        return baseMapper.selectList(wrapper);
    }

    /**
     * 分页查询（带数据权限）
     * <p>
     * 默认带数据权限过滤，需要忽略权限时请使用 DataPermissionHelper.ignore() 包装调用
     *
     * @param wrapper   查询条件
     * @param pageQuery 分页参数
     * @return 分页结果
     */
    @DataPermission({
        @DataColumn(key = "deptName", value = "create_dept"),
        @DataColumn(key = "userName", value = "create_by")
    })
    @Override
    public PageResult<SysNotice> page(PlusLambdaQuery<SysNotice> wrapper, PageQuery pageQuery) {
        return PageResult.of(baseMapper.selectPage(pageQuery.build(), wrapper));
    }

}
