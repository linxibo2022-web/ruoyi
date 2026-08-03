package plus.ruoyi.system.config.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.json.JSONUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import plus.ruoyi.common.core.dict.DictEnableStatus;
import plus.ruoyi.common.core.dict.DictNoticeStatus;
import plus.ruoyi.common.core.dict.DictNoticeType;
import plus.ruoyi.common.core.exception.ServiceException;
import plus.ruoyi.common.core.utils.*;
import plus.ruoyi.common.mybatis.core.page.PageQuery;
import plus.ruoyi.common.mybatis.core.page.PageResult;
import plus.ruoyi.common.mybatis.core.query.PlusLambdaQuery;

import plus.ruoyi.common.oss.utils.OssContentUtils;
import plus.ruoyi.common.satoken.utils.LoginHelper;
import plus.ruoyi.common.websocket.dto.WebSocketMessageDto;
import plus.ruoyi.common.websocket.utils.WebSocketUtils;
import plus.ruoyi.system.config.dao.ISysNoticeDao;
import plus.ruoyi.system.config.domain.SysNotice;
import plus.ruoyi.system.config.domain.bo.SysNoticeBo;
import plus.ruoyi.system.config.domain.dto.TargetConfigDto;
import plus.ruoyi.system.config.domain.vo.SysNoticeVo;
import plus.ruoyi.system.config.domain.vo.UserNoticeVo;
import plus.ruoyi.system.config.service.ISysNoticeService;
import plus.ruoyi.system.core.dao.ISysDeptDao;
import plus.ruoyi.system.core.dao.ISysRoleDao;
import plus.ruoyi.system.core.dao.ISysUserDao;
import plus.ruoyi.system.core.dao.ISysUserRoleDao;
import plus.ruoyi.system.core.domain.SysDept;
import plus.ruoyi.system.core.domain.SysRole;
import plus.ruoyi.system.core.domain.SysUser;
import plus.ruoyi.system.core.domain.SysUserRole;

import java.util.*;

/**
 * 公告 服务层实现
 *
 * @author Lion Li
 */
@RequiredArgsConstructor
@Service
public class SysNoticeServiceImpl implements ISysNoticeService {

    private final ISysNoticeDao noticeDao;
    private final ISysUserDao userDao;
    private final ISysDeptDao deptDao;
    private final ISysRoleDao roleDao;
    private final ISysUserRoleDao userRoleDao;

    /**
     * 根据ID查询
     *
     * @param noticeId 主键ID
     * @return 视图对象
     */
    @Override
    public SysNoticeVo get(Long noticeId) {
        SysNotice entity = noticeDao.getById(noticeId);
        return MapstructUtils.convert(entity, SysNoticeVo.class);
    }

    /**
     * 查询列表
     *
     * @param bo 查询参数
     * @return 列表数据
     */
    @Override
    public List<SysNoticeVo> list(SysNoticeBo bo) {
        PlusLambdaQuery<SysNotice> wrapper = noticeDao.buildQueryWrapper(bo);
        List<SysNotice> entities = noticeDao.list(wrapper);
        return MapstructUtils.convert(entities, SysNoticeVo.class);
    }

    /**
     * 分页查询
     *
     * @param bo        查询参数
     * @param pageQuery 分页参数
     * @return 分页结果
     */
    @Override
    public PageResult<SysNoticeVo> page(SysNoticeBo bo, PageQuery pageQuery) {
        // Service层处理：根据创建人名称查询创建人ID
        if (StringUtils.isNotBlank(bo.getCreateByName())) {
            SysUser sysUser = userDao.getByNameKeyword(bo.getCreateByName());
            bo.setCreateBy(ObjectUtils.defaultIfNull(ObjectUtils.getIfNotNull(sysUser, SysUser::getUserId), -1L));
        }

        PlusLambdaQuery<SysNotice> wrapper = noticeDao.buildQueryWrapper(bo);
        PageResult<SysNotice> entityPage = noticeDao.page(wrapper, pageQuery);
        return entityPage.convert(SysNoticeVo.class);
    }

    /**
     * 新增
     *
     * @param bo 业务对象
     * @return 主键ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long add(SysNoticeBo bo) {
        SysNotice entity = MapstructUtils.convert(bo, SysNotice.class);
        beforeSave(entity);
        noticeDao.insert(entity);
        return entity.getNoticeId();
    }

    /**
     * 修改
     *
     * @param bo 业务对象
     * @return 影响行数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int update(SysNoticeBo bo) {
        if (bo.getNoticeId() == null) {
            throw ServiceException.of("公告ID不能为空");
        }
        if (!noticeDao.exists(bo.getNoticeId())) {
            throw ServiceException.of("公告不存在");
        }
        SysNotice entity = MapstructUtils.convert(bo, SysNotice.class);
        beforeSave(entity);
        return noticeDao.updateById(entity);
    }

    /**
     * 批量删除
     *
     * @param ids ID集合
     * @return 影响行数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchDelete(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            throw ServiceException.of("ID集合不能为空");
        }
        beforeDelete(ids);
        return noticeDao.deleteByIds(ids);
    }

    /**
     * 批量保存
     *
     * @param boList 业务对象集合
     * @return 影响行数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchSave(List<SysNoticeBo> boList) {
        if (CollUtil.isEmpty(boList)) {
            return 0;
        }
        List<SysNotice> entities = new ArrayList<>(boList.size());
        for (SysNoticeBo bo : boList) {
            SysNotice entity = MapstructUtils.convert(bo, SysNotice.class);
            beforeSave(entity);
            entities.add(entity);
        }
        return noticeDao.batchSave(entities);
    }

    /**
     * 保存前的数据处理
     *
     * <p>富文本内容入库前清洗掉私有桶图片 URL 上的预签名参数（X-Amz-* 等），只持久化干净地址；
     * 展示时由 {@link SysNoticeVo}/{@link UserNoticeVo} 的 {@code @SerialMap(PRESIGNED_URL)} 动态重新签名。
     * 这样既避免把 1 小时即过期的临时签名写死进数据库，也兼容旧数据（旧公告再次保存即被清洗）。
     *
     * @param entity 待保存实体
     */
    protected void beforeSave(SysNotice entity) {
        entity.setNoticeContent(OssContentUtils.cleanPresignedUrls(entity.getNoticeContent()));
    }

    /**
     * 通知公告数据删除前的业务规则校验
     *
     * @param ids 待删除数据ID集合
     */
    protected void beforeDelete(Collection<Long> ids) {
        // 删除前校验
    }

    /**
     * 处理推送配置，生成目标配置和用户ID列表
     *
     * @param bo 公告业务对象
     */
    @Override
    public void processTargetConfig(SysNoticeBo bo) {
        if (StringUtils.isBlank(bo.getPushType())) {
            bo.setPushType("all");
        }

        TargetConfigDto config;
        List<Long> targetUserIds = switch (bo.getPushType()) {
            case "dept" -> {
                config = buildDeptConfig(bo.getDeptIds());
                // 使用经过权限校验后的部门ID查询用户
                yield getUserIdsByDeptIds(config.getIds());
            }
            case "role" -> {
                config = buildRoleConfig(bo.getRoleIds());
                // 使用经过权限校验后的角色ID查询用户
                yield getUserIdsByRoleIds(config.getIds());
            }
            case "user" -> {
                config = buildUserConfig(bo.getUserIds());
                yield bo.getUserIds();
            }
            default -> {
                config = TargetConfigDto.ofAll();
                yield getAllUserIds();
            }
        };

        bo.setTargetConfig(JSONUtil.toJsonStr(config));
        bo.setTargetUserIds(StringUtils.join(targetUserIds, ","));
        bo.setReadUserIds(StringUtils.EMPTY);
    }

    /**
     * 获取所有用户ID（带数据权限）
     * 只能向自己权限范围内的用户发送公告
     */
    private List<Long> getAllUserIds() {
        List<SysUser> userList = userDao.listByStatus(DictEnableStatus.ENABLE.getValue());
        return StreamUtils.toList(userList, SysUser::getUserId);
    }

    /**
     * 根据部门ID获取用户ID列表（带数据权限）
     * 只能向自己权限范围内的用户发送公告
     */
    private List<Long> getUserIdsByDeptIds(List<Long> deptIds) {
        if (CollUtil.isEmpty(deptIds)) {
            return new ArrayList<>();
        }

        List<SysUser> userList = userDao.listByDeptIdsAndStatus(deptIds, DictEnableStatus.ENABLE.getValue());
        return StreamUtils.toList(userList, SysUser::getUserId);
    }

    /**
     * 根据角色ID获取用户ID列表
     */
    private List<Long> getUserIdsByRoleIds(List<Long> roleIds) {
        if (CollUtil.isEmpty(roleIds)) {
            return new ArrayList<>();
        }

        List<SysUserRole> userRoleList = userRoleDao.listByRoleIds(roleIds);

        List<Long> userIds = StreamUtils.toList(userRoleList, SysUserRole::getUserId);

        if (CollUtil.isEmpty(userIds)) {
            return new ArrayList<>();
        }

        // 确保用户状态正常，并过滤掉当前用户无权限管理的用户
        List<SysUser> userList = userDao.listByUserIdsAndStatus(userIds, DictEnableStatus.ENABLE.getValue());

        return StreamUtils.toList(userList, SysUser::getUserId);
    }

    /**
     * 构建部门配置（带数据权限校验）
     * 只返回用户有权限管理的部门，防止越权发送公告
     */
    private TargetConfigDto buildDeptConfig(List<Long> deptIds) {
        if (CollUtil.isEmpty(deptIds)) {
            return TargetConfigDto.ofAll();
        }
        // 使用带数据权限的查询，只返回用户有权限的部门
        List<SysDept> deptList = deptDao.listByDeptIds(deptIds);
        if (CollUtil.isEmpty(deptList)) {
            throw ServiceException.of("您没有权限向所选部门发送公告");
        }
        // 只使用有权限的部门ID
        List<Long> authorizedDeptIds = StreamUtils.toList(deptList, SysDept::getDeptId);
        return TargetConfigDto.of("dept", authorizedDeptIds, StreamUtils.toList(deptList, SysDept::getDeptName));
    }

    /**
     * 构建角色配置（带数据权限校验）
     * 只返回用户有权限管理的角色，防止越权发送公告
     */
    private TargetConfigDto buildRoleConfig(List<Long> roleIds) {
        if (CollUtil.isEmpty(roleIds)) {
            return TargetConfigDto.ofAll();
        }
        // 使用带数据权限的查询，只返回用户有权限的角色
        List<SysRole> roles = roleDao.listByRoleIds(roleIds);
        if (CollUtil.isEmpty(roles)) {
            throw ServiceException.of("您没有权限向所选角色发送公告");
        }
        // 只使用有权限的角色ID
        List<Long> authorizedRoleIds = StreamUtils.toList(roles, SysRole::getRoleId);
        return TargetConfigDto.of("role", authorizedRoleIds, StreamUtils.toList(roles, SysRole::getRoleName));
    }

    /**
     * 构建用户配置
     */
    private TargetConfigDto buildUserConfig(List<Long> userIds) {
        if (CollUtil.isEmpty(userIds)) {
            return TargetConfigDto.ofAll();
        }

        List<SysUser> userList = userDao.listByUserIdsAndStatus(userIds, DictNoticeStatus.SEND_IMMEDIATELY.getValue());

        return TargetConfigDto.of("user", userIds, StreamUtils.toList(userList, SysUser::getUserName));
    }

    /**
     * 发送公告通知
     *
     * @param bo 公告业务对象
     */
    @Override
    public void sendNoticeNotification(SysNoticeBo bo) {
        // 如果目标用户ID列表不为空且为可用则发送WebSocket消息
        if (StringUtils.isNotBlank(bo.getTargetUserIds()) && DictEnableStatus.isEnabled(bo.getStatus())) {
            String type = DictNoticeType.getByValue(bo.getNoticeType()).getLabel();
            String message = "[" + type + "] " + bo.getNoticeTitle();

            List<Long> userIds = StringUtils.splitToList(bo.getTargetUserIds(), Convert::toLong);
            WebSocketUtils.publishMessage(WebSocketMessageDto.of(userIds, message));
        }
    }

    /**
     * 分页获取用户的公告列表
     *
     * @param userId    用户ID
     * @param pageQuery 分页参数
     * @return 分页结果
     */
    @Override
    public PageResult<UserNoticeVo> pageNoticesByUserId(Long userId, PageQuery pageQuery) {
        // DAO 层返回 Entity
        PageResult<SysNotice> pageResult = noticeDao.pageNoticesByUserId(userId, pageQuery);

        // Service 层做类型转换
        return pageResult.map(notice -> {
            UserNoticeVo vo = MapstructUtils.convert(notice, UserNoticeVo.class);
            // Service 层处理业务逻辑：设置已读状态
            if (vo != null) {
                vo.setIsRead(StringUtils.contains(notice.getReadUserIds(), userId.toString()));
            }
            return vo;
        });
    }

    /**
     * 获取用户某公告详情
     *
     * @param noticeId 公告ID
     * @param userId   用户ID
     * @return 用户公告详情
     */
    @Override
    public UserNoticeVo getUserNoticeDetail(Long noticeId, Long userId) {
        // DAO 层返回 Entity
        SysNotice notice = noticeDao.getUserNoticeDetail(noticeId, userId);

        // Service 层做类型转换
        UserNoticeVo vo = MapstructUtils.convert(notice, UserNoticeVo.class);

        // Service 层处理业务逻辑：设置已读状态
        if (vo != null && notice != null) {
            vo.setIsRead(StringUtils.contains(notice.getReadUserIds(), userId.toString()));
        }

        return vo;
    }

    /**
     * 获取用户未读数量
     *
     * @param userId 用户ID
     * @return 未读数量
     */
    @Override
    public Long getUnreadCountByUserId(Long userId) {
        return noticeDao.getUnreadCountByUserId(userId);
    }

    /**
     * 标记已读
     *
     * @param noticeId 公告ID
     * @param userId   用户ID
     * @return 是否成功
     */
    @Override
    public boolean markAsRead(Long noticeId, Long userId) {
        SysNotice notice = noticeDao.getById(noticeId);
        if (notice == null) {
            return false;
        }
        if (!DictNoticeStatus.SEND_IMMEDIATELY.getValue().equals(notice.getStatus())) {
            // 只有已发布的公告才能被标记为已读
            return false;
        }
        // 检查用户是否有权限查看此公告
        if (!StringUtils.contains(notice.getTargetUserIds(), userId.toString())) {
            return false;
        }
        // 添加用户ID到已读用户列表
        notice.setReadUserIds(StringUtils.addToCommaString(notice.getReadUserIds(), userId.toString()));
        // 更新公告状态为已读
        return noticeDao.updateById(notice) > 0;
    }

    /**
     * 用户标记全部已读
     *
     * @param userId 用户ID
     * @return 是否成功
     */
    @Override
    public boolean markAllAsRead(Long userId) {
        // 获取用户的所有未读公告
        List<SysNotice> unreadNotices = noticeDao.listUnreadNoticesByUserId(userId);

        if (CollUtil.isEmpty(unreadNotices)) {
            return true;
        }

        // 批量更新已读状态
        for (SysNotice notice : unreadNotices) {
            notice.setReadUserIds(StringUtils.addToCommaString(notice.getReadUserIds(), userId.toString()));
        }

        return noticeDao.batchSave(unreadNotices) > 0;
    }
}
