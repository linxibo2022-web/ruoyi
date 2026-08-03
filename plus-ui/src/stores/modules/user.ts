// 用户状态
import { userLogin, userLogout } from '@/api/system/auth/authApi'
import { getUserInfo } from '@/api/system/core/user/userApi'
import type { SysUserVo } from '@/api/system/core/user/userTypes'
import type { LoginRequest } from '@/api/system/auth/authTypes'
import defAva from '@/assets/images/profile.jpg'
import { sessionCache } from '@/utils/cache'

/**
 * 用户认证管理 (useUserStore)
 *
 * 基于 Pinia 的用户认证与权限管理模块，提供完整的用户身份验证和授权功能。
 *
 * 包含以下功能：
 * - 用户认证: 提供登录、注销和身份验证功能 (login, logout, getInfo)
 * - 权限管理: 存储和提供用户角色与权限信息 (roles, permissions)
 * - 用户信息: 管理用户基本资料和个人信息 (userInfo)
 * - 身份令牌: 安全存储和管理访问令牌 (token)
 * - 资料更新: 支持更新用户头像等个人信息 (updateAvatar)
 */

/**
 * 用户模块名称
 */
const USER_MODULE = 'user'
const LAST_LOGIN_USER_KEY = 'last-login-user'

/**
 * 用户状态管理
 * @description 管理用户认证、权限和个人信息等状态。
 * 此Store负责用户登录、注销、获取用户信息等操作，
 * 同时维护用户的角色和权限数据，用于权限控制。
 *
 * @example
 * // 在组件中使用
 * import { useUserStore } from '@/stores/modules/user';
 *
 * export default defineComponent({
 *   setup() {
 *     const userStore = useUserStore();
 *
 *     // 获取用户信息
 *     onMounted(async () => {
 *       await userStore.fetchUserInfo();
 *     });
 *
 *     return {
 *       // 获取用户基本信息
 *       nickname: computed(() => userStore.userInfo?.nickName || ''),
 *       avatar: computed(() => userStore.userInfo?.avatar || '')
 *     }
 *   }
 * });
 */
export const useUserStore = defineStore(USER_MODULE, () => {
  // 获取token工具，避免重复创建实例
  const tokenUtils = useToken()

  /**
   * 用户令牌
   * @description 用户登录后的访问令牌，用于API请求认证
   */
  const token = ref(tokenUtils.getToken())

  /**
   * 用户基本信息
   * @description 用户的基本信息（账号、昵称、头像等）
   */
  const userInfo = ref<SysUserVo | null>(null)

  /**
   * 用户角色编码集合
   * @description 用于判断路由权限和功能权限
   */
  const roles = ref<Array<string>>([])

  /**
   * 用户权限编码集合
   * @description 用于判断按钮权限等细粒度权限控制
   */
  const permissions = ref<Array<string>>([])

  /**
   * 用户登录
   * @param loginRequest 登录信息，包含用户名和密码
   * @returns Promise 登录成功时resolve，失败时reject错误信息
   * @description 调用登录API，成功后保存token
   * @example
   * // 用户登录
   * const [err, data] = await userStore.loginUser({
   *   userName: 'admin',
   *   password: '123456',
   *   code: '1234',
   *   uuid: 'uuid'
   * })
   */
  const loginUser = async (loginRequest: LoginRequest): Result<void> => {
    const [err, data] = await userLogin(loginRequest)
    if (err) {
      return [err, null]
    }
    // 保存token到localStorage和store，使用已缓存的tokenUtils实例
    tokenUtils.setToken(data.access_token, data.expire_in)
    token.value = data.access_token
    return [null, null]
  }

  /**
   * 获取用户信息
   * @returns Result
   * @description 调用获取用户信息API，获取用户基本信息、角色和权限
   * @example
   * // 在路由守卫中使用
   * router.beforeEach(async (to, from, next) => {
   *   if (userStore.token && !userStore.roles.length) {
   *     await userStore.fetchUserInfo();
   *     next();
   *   }
   * });
   */
  const fetchUserInfo = async (): Result<void> => {
    const [err, data] = await getUserInfo()
    if (err) {
      return [err, null]
    }

    const user = data.user
    const currentUserKey = String(user.userId)
    const lastUserKey = sessionCache.get(LAST_LOGIN_USER_KEY)
    if (lastUserKey && lastUserKey !== currentUserKey) {
      await useLayout().delAllViews()
    }
    sessionCache.set(LAST_LOGIN_USER_KEY, currentUserKey)
    // 处理用户头像
    if (!user.avatar) {
      user.avatar = defAva
    }

    // 设置用户基本信息
    userInfo.value = user

    // 设置角色和权限
    roles.value = data.roles || []
    permissions.value = data.permissions || []

    return [null, null]
  }

  /**
   * 用户注销
   * @returns Result
   * @description 调用注销API，清除用户状态和token
   * @example
   * // 用户注销
   * userStore.logoutUser().then(() => {
   *   // 重定向到登录页
   *   router.push('/login');
   * });
   */
  const logoutUser = async (): Result<void> => {
    // 调用注销API
    const [err] = await userLogout()

    // 清除状态
    token.value = ''
    userInfo.value = null
    roles.value = []
    permissions.value = []

    // 移除localStorage中的token，使用已缓存的tokenUtils实例
    tokenUtils.removeToken()
    return [err, null]
  }

  /**
   * 更新用户头像
   * @param avatarUrl 新头像的URL地址
   * @description 更新用户头像
   * @example
   * // 修改用户头像
   * userStore.updateAvatar('https://example.com/avatar.jpg');
   */
  const updateAvatar = (avatarUrl: string): void => {
    if (userInfo.value) {
      userInfo.value.avatar = avatarUrl
    }
  }

  return {
    // 状态
    token,
    userInfo,
    roles,
    permissions,

    // 方法
    fetchUserInfo,
    updateAvatar,
    loginUser,
    logoutUser
  }
})
