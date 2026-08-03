// 权限指令集合
import { Directive, DirectiveBinding } from 'vue'
import { useAuth } from '@/composables/useAuth'

/**
 * 权限指令集合
 *
 * 提供一系列用于权限控制的自定义指令，优化命名便于记忆和使用。
 *
 * 包含以下指令：
 * - 基础权限指令:
 *   - v-permi: 基于操作权限控制元素显示（OR逻辑）
 *   - v-role: 基于角色权限控制元素显示（OR逻辑）
 *   - v-admin: 仅管理员可见的元素
 *   - v-superadmin: 仅超级管理员可见的元素
 *
 * - 高级权限指令:
 *   - v-permi-all: 必须满足所有权限才显示（AND逻辑）
 *   - v-role-all: 必须满足所有角色才显示（AND逻辑）
 *   - v-tenant: 基于租户操作权限控制元素
 *
 * - 反向权限指令:
 *   - v-no-permi: 与v-permi相反，有权限时隐藏
 *   - v-no-role: 与v-role相反，有角色时隐藏
 *
 * - 自定义控制指令:
 *   - v-auth: 灵活的权限控制指令，支持多种动作
 */

// ===== 基础权限指令 =====

/**
 * v-permi 指令
 *
 * 基于操作权限控制元素的显示，满足任一权限即可显示元素（OR逻辑）。
 *
 * @example
 * // 单个权限
 * <button v-permi="'system:user:add'">添加用户</button>
 *
 * // 多个权限（满足其一即可）
 * <button v-permi="['system:user:add', 'system:user:update']">用户管理</button>
 */
export const permi: Directive = {
  mounted(el: HTMLElement, binding: DirectiveBinding) {
    const { hasPermission } = useAuth()
    const { value } = binding

    if (!value) {
      throw new Error('权限值不能为空')
    }

    if (!hasPermission(value)) {
      el.parentNode && el.parentNode.removeChild(el)
    }
  }
}

/**
 * v-role 指令
 *
 * 基于角色权限控制元素，满足任一角色即可显示元素（OR逻辑）。
 *
 * @example
 * // 单个角色
 * <button v-role="'editor'">编辑内容</button>
 *
 * // 多个角色（满足其一即可）
 * <button v-role="['admin', 'editor']">内容管理</button>
 */
export const role: Directive = {
  mounted(el: HTMLElement, binding: DirectiveBinding) {
    const { hasRole } = useAuth()
    const { value } = binding

    if (!value) {
      throw new Error('角色值不能为空')
    }

    if (!hasRole(value)) {
      el.parentNode && el.parentNode.removeChild(el)
    }
  }
}

/**
 * v-admin 指令
 *
 * 仅管理员可见的元素，包括超级管理员和租户管理员。
 *
 * @example
 * <button v-admin>管理员功能</button>
 */
export const admin: Directive = {
  mounted(el: HTMLElement, binding: DirectiveBinding) {
    const { isAnyAdmin } = useAuth()

    if (!isAnyAdmin) {
      el.parentNode && el.parentNode.removeChild(el)
    }
  }
}

/**
 * v-superadmin 指令
 *
 * 仅超级管理员可见的元素。
 *
 * @example
 * <button v-superadmin>超级管理员功能</button>
 */
export const superadmin: Directive = {
  mounted(el: HTMLElement, binding: DirectiveBinding) {
    const { isSuperAdmin } = useAuth()

    if (!isSuperAdmin) {
      el.parentNode && el.parentNode.removeChild(el)
    }
  }
}

// ===== 高级权限指令 =====

/**
 * v-permi-all 指令
 *
 * 必须满足所有权限才可显示元素（AND逻辑）。
 *
 * @example
 * <button v-permi-all="['system:user:add', 'system:user:update']">高级用户管理</button>
 */
export const permiAll: Directive = {
  mounted(el: HTMLElement, binding: DirectiveBinding) {
    const { hasAllPermissions } = useAuth()
    const { value } = binding

    if (!Array.isArray(value)) {
      throw new Error('权限值必须为数组')
    }

    if (!hasAllPermissions(value)) {
      el.parentNode && el.parentNode.removeChild(el)
    }
  }
}

/**
 * v-role-all 指令
 *
 * 必须满足所有角色才可显示元素（AND逻辑）。
 *
 * @example
 * <button v-role-all="['admin', 'editor']">高级内容管理</button>
 */
export const roleAll: Directive = {
  mounted(el: HTMLElement, binding: DirectiveBinding) {
    const { hasAllRoles } = useAuth()
    const { value } = binding

    if (!Array.isArray(value)) {
      throw new Error('角色值必须为数组')
    }

    if (!hasAllRoles(value)) {
      el.parentNode && el.parentNode.removeChild(el)
    }
  }
}

/**
 * v-tenant 指令
 *
 * 基于租户操作权限控制元素。
 *
 * @example
 * // 当前租户下的权限
 * <button v-tenant="'system:user:add'">添加用户</button>
 *
 * // 指定租户下的权限
 * <button v-tenant="{ permi: 'system:user:add', tenantId: '12345' }">添加用户</button>
 */
export const tenant: Directive = {
  mounted(el: HTMLElement, binding: DirectiveBinding) {
    const { hasTenantPermission } = useAuth()
    const { value } = binding

    if (!value) {
      throw new Error('租户权限值不能为空')
    }

    // 处理字符串或数组（使用当前用户的租户ID）
    if (typeof value === 'string' || Array.isArray(value)) {
      if (!hasTenantPermission(value)) {
        el.parentNode && el.parentNode.removeChild(el)
      }
      return
    }

    // 处理对象格式 { permi: string | string[], tenantId: string }
    if (typeof value === 'object' && 'permi' in value) {
      const { permi, tenantId } = value
      if (!hasTenantPermission(permi, tenantId)) {
        el.parentNode && el.parentNode.removeChild(el)
      }
    }
  }
}

// ===== 反向权限指令 =====

/**
 * v-no-permi 指令
 *
 * 与v-permi相反，当用户拥有指定权限时，元素将被移除。
 *
 * @example
 * // 有此权限则隐藏
 * <button v-no-permi="'system:user:add'">普通用户操作</button>
 */
export const noPermi: Directive = {
  mounted(el: HTMLElement, binding: DirectiveBinding) {
    const { hasPermission } = useAuth()
    const { value } = binding

    if (!value) {
      throw new Error('权限值不能为空')
    }

    // 与permi指令逻辑相反
    if (hasPermission(value)) {
      el.parentNode && el.parentNode.removeChild(el)
    }
  }
}

/**
 * v-no-role 指令
 *
 * 与v-role相反，当用户拥有指定角色时，元素将被移除。
 *
 * @example
 * // 有此角色则隐藏
 * <button v-no-role="'admin'">普通用户功能</button>
 */
export const noRole: Directive = {
  mounted(el: HTMLElement, binding: DirectiveBinding) {
    const { hasRole } = useAuth()
    const { value } = binding

    if (!value) {
      throw new Error('角色值不能为空')
    }

    // 与role指令逻辑相反
    if (hasRole(value)) {
      el.parentNode && el.parentNode.removeChild(el)
    }
  }
}

// ===== 自定义控制指令 =====

/**
 * v-auth 指令
 *
 * 灵活的权限控制指令，支持权限类型和动作。
 *
 * @example
 * // 权限控制，禁用元素
 * <button v-auth="{ permi: 'system:user:add', action: 'disable' }">添加用户</button>
 *
 * // 角色控制，隐藏元素
 * <button v-auth="{ role: 'editor', action: 'hide' }">编辑内容</button>
 */
export const auth: Directive = {
  mounted(el: HTMLElement, binding: DirectiveBinding) {
    const { hasPermission, hasRole } = useAuth()
    const { value } = binding

    if (!value || typeof value !== 'object') {
      throw new Error('权限值格式不正确')
    }

    let hasAuth = true

    // 检查权限
    if ('permi' in value) {
      hasAuth = hasPermission(value.permi)
    }
    // 检查角色
    else if ('role' in value) {
      hasAuth = hasRole(value.role)
    } else {
      throw new Error('必须指定 permi 或 role')
    }

    // 如果没有权限，根据指定的动作进行处理
    if (!hasAuth) {
      const { action, className } = value
      applyAction(el, action, className)
    }
  }
}

/**
 * 应用权限动作到元素
 */
const applyAction = (el: HTMLElement, action?: string, className?: string): void => {
  switch (action) {
    case 'remove':
      // 移除元素
      el.parentNode && el.parentNode.removeChild(el)
      break

    case 'hide':
      // 隐藏元素
      el.style.display = 'none'
      break

    case 'disable':
      // 禁用元素
      el.setAttribute('disabled', 'disabled')
      el.classList.add('is-disabled')
      // 阻止点击事件
      const stopClick = (e: Event) => {
        e.stopPropagation()
        e.preventDefault()
      }
      el.addEventListener('click', stopClick, true)
      break

    case 'class':
      // 添加指定类名
      if (className) {
        el.classList.add(className)
      } else {
        el.classList.add('no-auth')
      }
      break

    default:
      // 默认移除元素
      el.parentNode && el.parentNode.removeChild(el)
  }
}
