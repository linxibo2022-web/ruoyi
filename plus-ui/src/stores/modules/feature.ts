/**
 * 系统功能配置管理 (useFeatureStore)
 *
 * 基于 Pinia 的系统功能配置管理模块，提供统一的功能开关管理。
 *
 * 包含以下功能：
 * - 功能配置: 管理应用中的各项功能开关 (features)
 * - 配置初始化: 从服务端获取功能配置 (initFeatures)
 * - openapi是否启用:  (canUseOpenApi)
 */

import type { SystemFeature } from '@/api/common/system/feature/featureApi'
import { getSystemFeatures } from '@/api/common/system/feature/featureApi'

/** 应用模块名称 */
const FEATURE_MODULE = 'feature'

export const useFeatureStore = defineStore(FEATURE_MODULE, () => {
  /**
   * 系统功能配置
   * @description 存储所有功能的启用状态
   */
  const features = ref<SystemFeature>({
    langchain4jEnabled: false,
    langchain4jThinkingEnabled: false,
    websocketEnabled: false,
    sseEnabled: false,
    openApiEnabled: false,
    openApiAccessMode: 'ALL',
    openApiAllowedRoles: []
  })

  /**
   * 配置是否已初始化
   */
  const initialized = ref(false)

  /**
   * 初始化功能配置
   * @description 从服务端获取功能配置，应在应用启动时调用一次
   * @returns {Promise<void>}
   * @example
   * const featureStore = useFeatureStore()
   * await featureStore.initFeatures()
   */
  const initFeatures = async (): Promise<void> => {
    if (initialized.value) {
      return
    }

    const [err, data] = await getSystemFeatures()
    if (err) {
      features.value = {
        langchain4jEnabled: false,
        websocketEnabled: false,
        sseEnabled: false,
        openApiEnabled: false,
        openApiAccessMode: 'ALL',
        openApiAllowedRoles: []
      }
    } else {
      features.value = data
    }
    initialized.value = true
  }

  /**
   * 检查当前用户是否可以使用开放API
   * @param userRoles 用户角色数组
   * @returns 是否可以使用
   */
  const canUseOpenApi = (userRoles: string[]): boolean => {
    if (!features.value.openApiEnabled) {
      return false
    }

    const mode = features.value.openApiAccessMode
    const allowedRoles = features.value.openApiAllowedRoles || []

    switch (mode) {
      case 'ALL':
        return true

      case 'SUPER_ADMIN':
        // 检查是否有超管角色
        return userRoles.includes('superadmin')

      case 'ADMIN':
        // 检查是否有管理员角色（超管或admin）
        return userRoles.includes('superadmin') || userRoles.includes('admin')

      case 'ROLES':
        // 检查是否有任一允许的角色
        if (allowedRoles.length === 0) {
          return false
        }
        return userRoles.some((role) => allowedRoles.includes(role))

      default:
        return false
    }
  }

  return {
    features,
    initialized,
    initFeatures,
    canUseOpenApi
  }
})
