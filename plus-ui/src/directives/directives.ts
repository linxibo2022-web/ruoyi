import { App } from 'vue'
import {
  // 基础指令
  permi,
  role,
  admin,
  superadmin,
  // 高级指令
  permiAll,
  roleAll,
  tenant,
  // 反向指令
  noPermi,
  noRole,
  // 灵活控制指令
  auth
} from './permission'

/**
 * 全局注册自定义指令
 *
 * 注册系统中所有的自定义指令，包括复制文本指令和一系列权限控制指令。
 * 权限指令基于useAuth钩子提供的权限检查功能，用于控制UI元素的显示与隐藏。
 *
 * @param app Vue应用实例
 */
export default (app: App) => {
  // ===== 基础权限指令 =====

  /**
   * 基于权限控制元素显示(OR逻辑)
   * 满足任一权限即可显示元素
   */
  app.directive('permi', permi)

  /**
   * 基于角色控制元素显示(OR逻辑)
   * 满足任一角色即可显示元素
   */
  app.directive('role', role)

  /**
   * 仅管理员可见
   * 包括超级管理员和租户管理员
   */
  app.directive('admin', admin)

  /**
   * 仅超级管理员可见
   * 系统最高权限角色专属
   */
  app.directive('superadmin', superadmin)

  // ===== 高级权限指令 =====

  /**
   * 必须满足所有权限才显示(AND逻辑)
   * 用于需要多重权限验证的场景
   */
  app.directive('permiAll', permiAll)

  /**
   * 必须满足所有角色才显示(AND逻辑)
   * 用于需要多重角色验证的场景
   */
  app.directive('roleAll', roleAll)

  /**
   * 基于租户权限控制元素显示
   * 可指定检查特定租户下的权限
   */
  app.directive('tenant', tenant)

  // ===== 反向权限指令 =====

  /**
   * 反向权限控制
   * 当用户拥有指定权限时，元素将被移除
   */
  app.directive('noPermi', noPermi)

  /**
   * 反向角色控制
   * 当用户拥有指定角色时，元素将被移除
   */
  app.directive('noRole', noRole)

  // ===== 自定义控制指令 =====

  /**
   * 高级权限控制
   * 支持自定义处理方式，如禁用、隐藏、添加类名等
   */
  app.directive('auth', auth)
}
