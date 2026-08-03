package plus.ruoyi.system.core.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.ArrayUtil;
import plus.ruoyi.common.satoken.utils.LoginHelper;
import plus.ruoyi.common.sensitive.core.SensitiveService;
import plus.ruoyi.common.tenant.helper.TenantHelper;
import org.springframework.stereotype.Service;

/**
 * 数据脱敏业务层处理
 *
 * @author Lion Li
 * @version 3.6.0
 */
@Service
public class SysSensitiveServiceImpl implements SensitiveService {

    /**
     * 判断是否需要脱敏
     * <p>
     * 根据用户角色和权限判断是否对敏感数据进行脱敏处理：
     * 1. 未登录用户一律脱敏
     * 2. 同时配置角色和权限时，需要同时满足才不脱敏
     * 3. 仅配置角色时，拥有任一角色即不脱敏
     * 4. 仅配置权限时，拥有任一权限即不脱敏
     * 5. 管理员用户（超级管理员、租户管理员）默认不脱敏
     *
     * @param roleKey 角色标识数组，可为空
     * @param perms   权限标识数组，可为空
     * @return true表示需要脱敏，false表示不需要脱敏
     */
    @Override
    public boolean isSensitive(String[] roleKey, String[] perms) {
        // 未登录用户一律脱敏
        if (!LoginHelper.isLogin()) {
            return true;
        }

        boolean roleExist = ArrayUtil.isNotEmpty(roleKey);
        boolean permsExist = ArrayUtil.isNotEmpty(perms);

        // 权限检查逻辑
        if (roleExist && permsExist) {
            // 同时配置角色和权限：需要同时满足才不脱敏
            if (StpUtil.hasRoleOr(roleKey) && StpUtil.hasPermissionOr(perms)) {
                return false;
            }
        } else if (roleExist && StpUtil.hasRoleOr(roleKey)) {
            // 仅配置角色：拥有任一角色即不脱敏
            return false;
        } else if (permsExist && StpUtil.hasPermissionOr(perms)) {
            // 仅配置权限：拥有任一权限即不脱敏
            return false;
        }

        // 管理员特权检查
        if (TenantHelper.isEnable()) {
            // 租户环境：超级管理员和租户管理员都不脱敏
            return !LoginHelper.isSuperAdmin() && !LoginHelper.isTenantAdmin();
        }

        // 非租户环境：仅超级管理员不脱敏
        return !LoginHelper.isSuperAdmin();
    }
}
