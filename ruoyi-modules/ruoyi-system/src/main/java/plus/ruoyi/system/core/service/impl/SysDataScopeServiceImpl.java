package plus.ruoyi.system.core.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ObjectUtil;
import lombok.RequiredArgsConstructor;
import plus.ruoyi.common.core.constant.CacheNames;
import plus.ruoyi.common.core.utils.StreamUtils;
import plus.ruoyi.system.core.dao.ISysDeptDao;
import plus.ruoyi.system.core.dao.ISysRoleDeptDao;
import plus.ruoyi.system.core.domain.SysRoleDept;
import plus.ruoyi.system.core.service.ISysDataScopeService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 数据权限Service业务层处理 (SysDataScopeServiceImpl)
 * <p>
 * 本类负责处理系统中的数据权限相关业务逻辑，为数据权限注解提供底层支持：
 * <p>
 * 1. 核心功能
 * - 角色自定义权限数据获取：获取指定角色关联的部门ID集合
 * - 部门及子部门权限数据获取：获取指定部门及其所有子部门的ID集合
 * - 权限数据缓存：利用Spring缓存机制提高查询性能
 * <p>
 * 2. 权限范围支持
 * - 角色自定义权限：根据角色ID获取该角色被授权的具体部门列表
 * - 部门及以下权限：根据部门ID获取该部门及其所有子部门的递归列表
 * <p>
 * 3. 缓存策略
 * - 角色自定义权限缓存：CacheNames.SYS_ROLE_CUSTOM
 * - 部门及子部门缓存：CacheNames.SYS_DEPT_AND_CHILD
 * - 支持条件缓存，避免无效数据缓存
 * <p>
 * 4. 特殊说明
 * - 本Service内不允许调用标注数据权限注解的方法
 * - 避免循环解析问题，例如调用deptMapper.selectList等标注了数据权限注解的方法
 * - 返回格式统一为逗号分隔的字符串，便于SQL IN条件使用
 * <p>
 * 5. 异常处理
 * - 参数为null时返回"-1"，表示无权限数据
 * - 查询结果为空时返回"-1"，确保权限控制的安全性
 * <p>
 * 本类是数据权限框架的核心支撑，确保了权限数据的正确获取和高效缓存。
 *
 * @author Lion Li
 */
@RequiredArgsConstructor
@Service("sdss")
public class SysDataScopeServiceImpl implements ISysDataScopeService {

    /**
     * 角色部门关联数据访问层
     * 用于查询角色与部门的关联关系
     */
    private final ISysRoleDeptDao roleDeptDao;

    /**
     * 部门数据访问层
     * 用于查询部门层级结构数据
     */
    private final ISysDeptDao deptDao;

    // ================ 角色权限相关方法 =================

    /**
     * 获取角色自定义权限数据
     * <p>
     * 根据角色ID查询该角色被授权的部门ID集合，用于实现角色级别的数据权限控制。
     * 查询结果会被缓存以提高性能，缓存key为角色ID。
     * <p>
     * 业务逻辑：
     * 1. 校验角色ID有效性，无效时返回"-1"
     * 2. 查询角色关联的部门数据，仅选择部门ID字段优化性能
     * 3. 将部门ID集合转换为逗号分隔的字符串格式
     * 4. 无权限数据时返回"-1"确保安全性
     * <p>
     * 使用场景：
     * - 数据权限注解中角色自定义权限的数据范围控制
     * - 用户查询数据时的权限范围限制
     *
     * @param roleId 角色ID，不能为null
     * @return 部门ID组合字符串，格式："1,2,3"；无权限时返回"-1"
     */
    @Cacheable(cacheNames = CacheNames.SYS_ROLE_CUSTOM, key = "#roleId", condition = "#roleId != null")
    @Override
    public String getRoleCustom(Long roleId) {
        // 参数有效性校验
        if (ObjectUtil.isNull(roleId)) {
            return "-1";
        }

        // 执行查询：仅查询指定角色的部门关联数据，只选择部门ID字段
        List<SysRoleDept> roleDeptList = roleDeptDao.listByRoleId(roleId);

        // 处理查询结果
        if (CollUtil.isNotEmpty(roleDeptList)) {
            // 提取部门ID并转换为逗号分隔的字符串
            return StreamUtils.join(roleDeptList, rd -> Convert.toStr(rd.getDeptId()));
        }

        // 无权限数据时返回-1
        return "-1";
    }

    // ================ 部门权限相关方法 =================

    /**
     * 获取部门及以下权限数据
     * <p>
     * 根据部门ID查询该部门及其所有子部门的ID集合，用于实现部门及以下级别的数据权限控制。
     * 查询结果会被缓存以提高性能，缓存key为部门ID。
     * <p>
     * 业务逻辑：
     * 1. 校验部门ID有效性，无效时返回"-1"
     * 2. 递归查询指定部门的所有子部门
     * 3. 将当前部门ID也加入结果集（包含自身权限）
     * 4. 将部门ID集合转换为逗号分隔的字符串格式
     * 5. 无权限数据时返回"-1"确保安全性
     * <p>
     * 使用场景：
     * - 数据权限注解中部门及以下权限的数据范围控制
     * - 部门管理员查询下属部门数据时的权限范围限制
     * - 组织架构相关的权限控制
     *
     * @param deptId 部门ID，不能为null
     * @return 部门ID组合字符串，格式："1,2,3"；无权限时返回"-1"
     */
    @Cacheable(cacheNames = CacheNames.SYS_DEPT_AND_CHILD, key = "#deptId", condition = "#deptId != null")
    @Override
    public String getDeptAndChild(Long deptId) {
        // 参数有效性校验
        if (ObjectUtil.isNull(deptId)) {
            return "-1";
        }

        // 查询指定部门的所有子部门ID（递归查询）
        List<Long> deptIds = deptDao.listChildrenDeptIds(deptId);

        // 将当前部门ID也加入集合（包含自身权限）
        if (!deptIds.contains(deptId)) {
            deptIds.add(deptId);
        }

        // 处理查询结果
        if (CollUtil.isNotEmpty(deptIds)) {
            // 将部门ID集合转换为逗号分隔的字符串
            return StreamUtils.join(deptIds, Convert::toStr);
        }

        // 无权限数据时返回-1
        return "-1";
    }
}
