/**
 * 认证与授权钩子 (useAuth)
 *
 * 提供用户认证与权限检查功能，包括权限字符串检查、角色校验和路由访问控制。
 * 此钩子集成了用户状态管理，简化了权限相关操作。
 *
 * 包含以下功能：
 * - 用户状态: 登录状态、管理员类型判断 (isLoggedIn, isSuperAdmin, isTenantAdmin)
 * - 权限检查: 单个或多个权限检查，支持OR和AND逻辑 (hasPermission, hasAllPermissions)
 * - 角色检查: 单个或多个角色检查，支持OR和AND逻辑 (hasRole, hasAllRoles)
 * - 租户权限: 租户上下文中的权限检查 (hasTenantPermission)
 * - 路由控制: 基于权限的路由访问控制 (canAccessRoute, filterAuthorizedRoutes)
 *
 * @returns 返回认证与授权相关的状态和方法
 *
 * @example
 * // 在组件中使用
 * import { useAuth } from '@/hooks/useAuth';
 *
 * export default defineComponent({
 *   setup() {
 *     const { hasPermission, hasRole, isSuperAdmin, isTenantAdmin } = useAuth();
 *
 *     // 检查特定权限
 *     const canAddUser = hasPermission('system:user:add');
 *
 *     // 检查多个权限（满足任一权限）
 *     const canManageUsers = hasPermission(['system:user:add', 'system:user:update']);
 *
 *     // 检查角色
 *     const isEditor = hasRole('editor');
 *
 *     // 检查是否为超级管理员（使用缓存中的角色）
 *     const isSuperAdminFromCache = isSuperAdmin();
 *
 *     // 检查指定角色是否为超级管理员
 *     const isSpecificRoleSuperAdmin = isSuperAdmin('customsuperadmin');
 *
 *     return {
 *       canAddUser,
 *       canManageUsers,
 *       isEditor,
 *       isSuperAdminFromCache,
 *       isSpecificRoleSuperAdmin
 *     };
 *   }
 * });
 */
export const useAuth = () => {
  // 获取用户状态管理
  const userStore = useUserStore()

  // 超级管理员角色标识
  const SUPER_ADMIN = 'superadmin'

  // 租户管理员角色标识
  const TENANT_ADMIN = 'admin'

  // 通配符权限标识
  const ALL_PERMISSION = '*:*:*'

  /**
   * 当前用户登录状态
   * @description 表示用户是否已登录系统
   */
  const isLoggedIn = computed(() => {
    return userStore.token && userStore.token.length > 0
  })

  /**
   * 检查当前用户是否为超级管理员
   * @param roleToCheck 可选，要检查的超级管理员角色标识，不传则使用默认值
   * @returns 是否为超级管理员
   * @description 超级管理员具有所有权限，支持动态指定角色标识
   */
  const isSuperAdmin = (roleToCheck?: string): boolean => {
    const targetRole = roleToCheck || SUPER_ADMIN
    return userStore.roles.includes(targetRole)
  }

  /**
   * 检查当前用户是否为租户管理员
   * @param roleToCheck 可选，要检查的租户管理员角色标识，不传则使用默认值
   * @returns 是否为租户管理员
   * @description 租户管理员具有其租户内的管理权限，支持动态指定角色标识
   */
  const isTenantAdmin = (roleToCheck?: string): boolean => {
    const targetRole = roleToCheck || TENANT_ADMIN
    return userStore.roles.includes(targetRole)
  }

  /**
   * 检查当前用户是否为任意级别的管理员
   * @param roleKey 角色标识
   * @returns 是否为任意级别的管理员
   * @description 包括超级管理员和租户管理员，支持动态指定角色标识
   */
  const isAnyAdmin = (roleKey?: string): boolean => {
    if (roleKey) {
      return roleKey === SUPER_ADMIN || roleKey === TENANT_ADMIN
    }
    return isSuperAdmin() || isTenantAdmin()
  }

  /**
   * 检查是否拥有指定权限
   * @param permission 权限标识或权限标识数组
   * @param superAdminRole 可选，超级管理员角色标识，用于权限豁免检查
   * @returns 是否拥有权限
   * @description
   * 检查当前用户是否拥有指定的权限标识。
   * 支持单个权限字符串或多个权限数组，数组情况下只需满足其中一个权限即可返回true。
   */
  const hasPermission = (permission: string | string[], superAdminRole?: string): boolean => {
    // 未定义权限或空权限
    if (!permission || permission.length === 0) {
      console.warn('权限参数不能为空')
      return false
    }

    // 获取当前用户权限列表
    const userPermissions = userStore.permissions

    // 超级管理员拥有所有权限
    if (isSuperAdmin(superAdminRole)) {
      return true
    }

    // 当前用户拥有所有权限标识
    if (userPermissions.includes(ALL_PERMISSION)) {
      return true
    }

    // 处理权限参数为数组的情况
    if (Array.isArray(permission)) {
      return permission.some((perm) => {
        return userPermissions.includes(perm)
      })
    }

    // 单个权限的检查
    return userPermissions.includes(permission)
  }

  /**
   * 检查是否拥有指定权限（租户范围内）
   * @param permission 权限标识或权限标识数组
   * @param tenantId 租户ID，不提供则使用当前用户的租户ID
   * @param superAdminRole 可选，超级管理员角色标识
   * @param tenantAdminRole 可选，租户管理员角色标识
   * @returns 是否在指定租户内拥有权限
   * @description
   * 检查当前用户是否在指定租户范围内拥有权限。
   * 租户管理员仅在其管理的租户内有权限。
   */
  const hasTenantPermission = (permission: string | string[], tenantId?: string, superAdminRole?: string, tenantAdminRole?: string): boolean => {
    // 获取要检查的租户ID，如果未提供则使用当前用户的租户ID
    const targetTenantId = tenantId || userStore.userInfo?.tenantId

    // 超级管理员拥有所有租户的所有权限
    if (isSuperAdmin(superAdminRole)) {
      return true
    }

    // 检查是否为当前用户所属租户
    // 无论是租户管理员还是普通用户，都只能操作自己所属租户的数据
    if (targetTenantId !== userStore.userInfo?.tenantId) {
      return false
    }

    // 如果是租户管理员，在自己的租户内拥有所有权限
    if (isTenantAdmin(tenantAdminRole)) {
      return true
    }

    // 普通用户在自己的租户内，按正常权限检查
    return hasPermission(permission, superAdminRole)
  }

  /**
   * 检查是否拥有指定角色
   * @param role 角色标识或角色标识数组
   * @param superAdminRole 可选，超级管理员角色标识，用于角色豁免检查
   * @returns 是否拥有角色
   * @description
   * 检查当前用户是否拥有指定的角色。注意：租户管理员admin也属于一种角色
   * 支持单个角色字符串或多个角色数组，数组情况下只需满足其中一个角色即可返回true。
   */
  const hasRole = (role: string | string[], superAdminRole?: string): boolean => {
    // 未定义角色或空角色
    if (!role || role.length === 0) {
      console.warn('角色参数不能为空')
      return false
    }

    // 获取当前用户角色列表
    const userRoles = userStore.roles

    // 超级管理员默认拥有所有角色
    if (isSuperAdmin(superAdminRole)) {
      return true
    }

    // 处理角色参数为数组的情况
    if (Array.isArray(role)) {
      return role.some((r) => {
        return userRoles.includes(r)
      })
    }

    // 单个角色的检查
    return userRoles.includes(role)
  }

  /**
   * 检查是否拥有指定权限（满足全部）
   * @param permissions 权限标识数组
   * @param superAdminRole 可选，超级管理员角色标识
   * @returns 是否拥有所有指定权限
   * @description
   * 与hasPermission不同，此方法要求用户必须拥有数组中的所有权限。
   */
  const hasAllPermissions = (permissions: string[], superAdminRole?: string): boolean => {
    // 超级管理员拥有所有权限
    if (isSuperAdmin(superAdminRole)) {
      return true
    }

    // 获取当前用户权限列表
    const userPermissions = userStore.permissions

    // 当前用户拥有所有权限标识
    if (userPermissions.includes(ALL_PERMISSION)) {
      return true
    }

    // 检查是否拥有数组中的所有权限
    return permissions.every((perm) => {
      return userPermissions.includes(perm)
    })
  }

  /**
   * 检查是否拥有指定角色（满足全部）
   * @param roles 角色标识数组
   * @param superAdminRole 可选，超级管理员角色标识
   * @returns 是否拥有所有指定角色
   * @description
   * 与hasRole不同，此方法要求用户必须拥有数组中的所有角色。
   */
  const hasAllRoles = (roles: string[], superAdminRole?: string): boolean => {
    // 超级管理员拥有所有角色
    if (isSuperAdmin(superAdminRole)) {
      return true
    }

    // 获取当前用户角色列表
    const userRoles = userStore.roles

    // 检查是否拥有数组中的所有角色
    return roles.every((role) => {
      return userRoles.includes(role)
    })
  }

  /**
   * 检查是否有权限访问某个路由
   * @param route 路由对象
   * @param superAdminRole 可选，超级管理员角色标识
   * @returns 是否有权限访问
   * @description
   * 根据路由的meta信息检查用户是否有权限访问该路由。
   */
  const canAccessRoute = (route: any, superAdminRole?: string): boolean => {
    if (!route) {
      return false
    }

    // 如果路由没有meta或者没有权限要求，则允许访问
    if (!route.meta || (!route.meta.roles && !route.meta.permissions)) {
      return true
    }

    // 超级管理员可以访问任何路由
    if (isSuperAdmin(superAdminRole)) {
      return true
    }

    // 检查角色权限
    if (route.meta.roles && route.meta.roles.length > 0) {
      if (!hasRole(route.meta.roles, superAdminRole)) {
        return false
      }
    }

    // 检查操作权限
    if (route.meta.permissions && route.meta.permissions.length > 0) {
      if (!hasPermission(route.meta.permissions, superAdminRole)) {
        return false
      }
    }

    return true
  }

  /**
   * 过滤有权限访问的路由
   * @param routes 路由数组
   * @param superAdminRole 可选，超级管理员角色标识
   * @returns 过滤后的路由数组
   * @description
   * 从路由数组中过滤出当前用户有权限访问的路由。
   */
  const filterAuthorizedRoutes = (routes: any[], superAdminRole?: string): any[] => {
    if (!routes || routes.length === 0) {
      return []
    }

    return routes.filter((route) => {
      // 检查当前路由是否可访问
      const hasAccess = canAccessRoute(route, superAdminRole)

      // 如果有子路由，递归处理
      if (hasAccess && route.children && route.children.length > 0) {
        route.children = filterAuthorizedRoutes(route.children, superAdminRole)
      }

      return hasAccess
    })
  }

  return {
    // 状态
    isLoggedIn,
    isSuperAdmin,
    isTenantAdmin,
    isAnyAdmin,

    // 权限检查方法
    hasPermission,
    hasTenantPermission,
    hasRole,
    hasAllPermissions,
    hasAllRoles,

    // 路由访问控制
    canAccessRoute,
    filterAuthorizedRoutes
  }
}
