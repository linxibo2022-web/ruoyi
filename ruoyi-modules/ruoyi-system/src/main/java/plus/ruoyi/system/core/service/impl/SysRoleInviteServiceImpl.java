package plus.ruoyi.system.core.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import plus.ruoyi.common.core.dict.DictEnableStatus;
import plus.ruoyi.common.core.exception.ServiceException;
import plus.ruoyi.common.core.service.ConfigService;
import plus.ruoyi.common.core.utils.MapstructUtils;
import plus.ruoyi.common.redis.utils.RedisUtils;
import plus.ruoyi.common.satoken.utils.LoginHelper;
import plus.ruoyi.common.tenant.helper.TenantHelper;
import plus.ruoyi.system.core.dao.ISysDeptDao;
import plus.ruoyi.system.core.dao.ISysRoleDao;
import plus.ruoyi.system.core.domain.SysDept;
import plus.ruoyi.system.core.domain.SysRole;
import plus.ruoyi.system.core.domain.bo.CreateRoleInviteBo;
import plus.ruoyi.system.core.domain.bo.RoleInviteQueryBo;
import plus.ruoyi.system.core.domain.vo.RoleInviteVo;
import plus.ruoyi.system.core.domain.vo.SysRoleVo;
import plus.ruoyi.system.core.service.ISysRoleInviteService;
import plus.ruoyi.system.core.service.ISysRoleService;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;

@Slf4j
@RequiredArgsConstructor
@Service
public class SysRoleInviteServiceImpl implements ISysRoleInviteService {

    private final ISysRoleDao roleDao;
    private final ISysDeptDao deptDao;

    private static final String INVITE_KEY_PREFIX = "user:invite:";

    /**
     * 生成安全的邀请码
     */
    private String generateSecureInviteCode() {
        // 生成12位安全邀请码，避免特殊字符
        String uuid = IdUtil.randomUUID().replace("-", "");
        return uuid.substring(0, 12).toUpperCase();
    }

    @Override
    public RoleInviteVo createRoleInvite(CreateRoleInviteBo bo) {
        // 移除注册开关检查 - 邀请码生成不应受此限制
        // 邀请码本身就是为了在关闭公开注册时，允许特定人员注册

        Long currentUserId = LoginHelper.getUserId();
        String currentUserName = LoginHelper.getUserName();
        String currentTenantId = LoginHelper.getTenantId();

        // 验证角色是否存在
        SysRole role = roleDao.getById(bo.getRoleId());
        if (ObjectUtil.isNull(role)) {
            throw ServiceException.of("角色不存在");
        }

        // 验证角色是否属于当前租户
        if (!currentTenantId.equals(role.getTenantId()) && !LoginHelper.isSuperAdmin()) {
            throw ServiceException.of("无权为其他租户的角色创建邀请");
        }

        if (ObjectUtil.isNotNull(role.getRoleId()) && LoginHelper.isSuperAdmin(role.getRoleId())) {
            throw ServiceException.of("不允许邀请超级管理员角色");
        }
        if (ObjectUtil.isNotNull(role.getRoleId()) && LoginHelper.isTenantAdmin(Set.of(role.getRoleKey()))) {
            throw ServiceException.of("不允许邀请管理员角色");
        }

        // 验证部门是否存在
        SysDept dept = deptDao.getById(bo.getDeptId());
        if (ObjectUtil.isNull(dept)) {
            throw ServiceException.of("部门不存在");
        }

        // 验证部门是否属于当前租户
        if (!currentTenantId.equals(dept.getTenantId()) && !LoginHelper.isSuperAdmin()) {
            throw ServiceException.of("无权为其他租户的部门创建邀请");
        }

        // 生成唯一邀请码，确保不重复
        String inviteCode;
        String testKey;
        int maxRetries = 10;
        int retries = 0;

        do {
            inviteCode = generateSecureInviteCode();
            testKey = INVITE_KEY_PREFIX + "*:*:" + inviteCode;
            Collection<String> existingKeys = RedisUtils.keys(testKey);
            if (existingKeys.isEmpty()) {
                break;
            }
            retries++;
        } while (retries < maxRetries);

        if (retries >= maxRetries) {
            throw ServiceException.of("生成邀请码失败，请重试");
        }

        // 计算过期时间
        long validUntil = System.currentTimeMillis() + (bo.getValidHours() * 60 * 60 * 1000L);

        // 构建邀请数据
        RoleInviteVo inviteVo = new RoleInviteVo();
        if (LoginHelper.isSuperAdmin()) {
            inviteVo.setTenantId(role.getTenantId());
        } else {
            inviteVo.setTenantId(LoginHelper.getTenantId());
        }
        inviteVo.setInviteCode(inviteCode);
        inviteVo.setUserId(currentUserId);
        inviteVo.setUserName(currentUserName);
        inviteVo.setRoleId(bo.getRoleId());
        inviteVo.setRoleName(role.getRoleName());
        inviteVo.setDeptId(bo.getDeptId());
        inviteVo.setDeptName(dept.getDeptName());
        inviteVo.setMaxUseCount(bo.getMaxUseCount());
        inviteVo.setCurrentUseCount(0);
        inviteVo.setValidUntil(validUntil);
        inviteVo.setCreateTime(System.currentTimeMillis());
        inviteVo.setRemark(bo.getRemark());
        inviteVo.setNeedApproval(bo.getNeedApproval() != null ? bo.getNeedApproval() : false);
        inviteVo.setInviteStatus(DictEnableStatus.ENABLE.getValue());

        // 存储到Redis
        String redisKey = INVITE_KEY_PREFIX + currentUserId + ":" + bo.getRoleId() + ":" + inviteCode;
        RedisUtils.setCacheObject(redisKey, inviteVo, Duration.ofHours(bo.getValidHours()));

        log.info("用户 {} 为角色 {} 创建了邀请码 {}", currentUserName, role.getRoleName(), inviteCode);

        return inviteVo;
    }

    @Override
    public List<RoleInviteVo> listRoleInvites(RoleInviteQueryBo queryBo) {
        Long currentUserId = LoginHelper.getUserId();
        String pattern;

        if (ObjectUtil.isNotNull(queryBo.getRoleId())) {
            // 查询用户某个角色的邀请
            pattern = INVITE_KEY_PREFIX + currentUserId + ":" + queryBo.getRoleId() + ":*";
        } else {
            // 查询用户所有邀请
            pattern = INVITE_KEY_PREFIX + currentUserId + ":*:*";
        }

        Collection<String> keys = RedisUtils.keys(pattern);
        List<RoleInviteVo> result = new ArrayList<>();

        for (String key : keys) {
            RoleInviteVo invite = RedisUtils.getCacheObject(key);
            if (ObjectUtil.isNotNull(invite)) {
                // 计算动态字段
                calculateDynamicFields(invite);

                // 状态过滤
                if (ObjectUtil.isNotNull(queryBo.getStatus()) && !matchesStatus(invite, queryBo.getStatus())) {
                    continue;
                }

                result.add(invite);
            }
        }

        // 按创建时间倒序排列
        result.sort((a, b) -> Long.compare(b.getCreateTime(), a.getCreateTime()));
        return result;
    }

    @Override
    public RoleInviteVo validateRoleInvite(String inviteCode) {
        String currentTenantId = TenantHelper.getTenantId();

        // 通过邀请码查找
        String pattern = INVITE_KEY_PREFIX + "*:*:" + inviteCode;
        Collection<String> keys = RedisUtils.keys(pattern);

        if (keys.isEmpty()) {
            throw ServiceException.of("邀请码不存在或已过期");
        }

        String key = keys.iterator().next();
        RoleInviteVo invite = RedisUtils.getCacheObject(key);

        if (ObjectUtil.isNull(invite)) {
            throw ServiceException.of("邀请码不存在或已过期");
        }

        // 验证邀请码是否属于当前租户
        if (!currentTenantId.equals(invite.getTenantId())) {
            throw ServiceException.of("邀请码不属于当前租户");
        }

        // 计算动态字段
        calculateDynamicFields(invite);

        // 检查是否有效
        if (!invite.getIsValid()) {
            String reason = "";
            if (System.currentTimeMillis() > invite.getValidUntil()) {
                reason = "邀请码已过期";
            } else if (invite.getMaxUseCount() > 0 && invite.getCurrentUseCount() >= invite.getMaxUseCount()) {
                reason = "邀请码使用次数已用完";
            } else {
                reason = "邀请码已失效";
            }
            throw ServiceException.of(reason);
        }

        return invite;
    }

    @Override
    public boolean useRoleInvite(String inviteCode) {
        String currentTenantId = TenantHelper.getTenantId();
        String pattern = INVITE_KEY_PREFIX + "*:*:" + inviteCode;
        Collection<String> keys = RedisUtils.keys(pattern);

        if (keys.isEmpty()) {
            return false;
        }

        String key = keys.iterator().next();
        RoleInviteVo invite = RedisUtils.getCacheObject(key);

        if (ObjectUtil.isNull(invite)) {
            return false;
        }

        // 验证租户匹配
        if (!currentTenantId.equals(invite.getTenantId())) {
            log.warn("邀请码 {} 租户不匹配，当前租户: {}, 邀请码租户: {}",
                inviteCode, currentTenantId, invite.getTenantId());
            return false;
        }

        // 增加使用次数
        invite.setCurrentUseCount(invite.getCurrentUseCount() + 1);

        // 更新Redis
        long remainingTtl = RedisUtils.getTimeToLive(key);
        if (remainingTtl > 0) {
            RedisUtils.setCacheObject(key, invite, Duration.ofMillis(remainingTtl));
        }

        log.info("邀请码 {} 被使用，当前使用次数: {}/{}",
            inviteCode, invite.getCurrentUseCount(),
            invite.getMaxUseCount() == -1 ? "无限制" : invite.getMaxUseCount());

        return true;
    }

    @Override
    public boolean deleteRoleInvite(String inviteCode) {
        Long currentUserId = LoginHelper.getUserId();
        String currentUserName = LoginHelper.getUserName();
        String currentTenantId = LoginHelper.getTenantId();

        // 验证邀请码所有权
        if (!checkRoleInviteOwnership(inviteCode)) {
            throw ServiceException.of("无权删除此邀请码");
        }

        String pattern = INVITE_KEY_PREFIX + currentUserId + ":*:" + inviteCode;
        Collection<String> keys = RedisUtils.keys(pattern);

        if (!keys.isEmpty()) {
            String key = keys.iterator().next();

            // 获取邀请信息用于日志记录
            RoleInviteVo invite = RedisUtils.getCacheObject(key);

            boolean deleted = RedisUtils.deleteObject(key);

            if (deleted && ObjectUtil.isNotNull(invite)) {
                log.info("用户 {} 删除了邀请码 {}", currentUserName, inviteCode);
            }

            return deleted;
        }

        return false;
    }

    @Override
    public boolean checkRoleInviteOwnership(String inviteCode) {
        Long currentUserId = LoginHelper.getUserId();
        String pattern = INVITE_KEY_PREFIX + currentUserId + ":*:" + inviteCode;
        Collection<String> keys = RedisUtils.keys(pattern);
        return !keys.isEmpty();
    }

    /**
     * 计算动态字段
     */
    private void calculateDynamicFields(RoleInviteVo invite) {
        long currentTime = System.currentTimeMillis();

        // 计算是否过期
        boolean isExpired = currentTime > invite.getValidUntil();

        // 计算是否用尽
        boolean isExhausted = invite.getMaxUseCount() > 0 &&
            invite.getCurrentUseCount() >= invite.getMaxUseCount();

        // 计算剩余次数
        if (invite.getMaxUseCount() == -1) {
            invite.setRemainingCount(-1); // 无限制
        } else {
            invite.setRemainingCount(Math.max(0, invite.getMaxUseCount() - invite.getCurrentUseCount()));
        }

        // 计算是否有效
        invite.setIsValid(!"0".equals(invite.getInviteStatus()) && !isExpired && !isExhausted);
    }

    /**
     * 状态匹配
     */
    private boolean matchesStatus(RoleInviteVo invite, String status) {
        return switch (status) {
            case "valid" -> invite.getIsValid();
            case "expired" -> System.currentTimeMillis() > invite.getValidUntil();
            case "exhausted" -> invite.getMaxUseCount() > 0 &&
                invite.getCurrentUseCount() >= invite.getMaxUseCount();
            default -> true;
        };
    }
}
