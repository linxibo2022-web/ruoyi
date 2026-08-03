<!-- 租户选择 -->
<template>
  <!-- 租户选择下拉框 - 仅对超级管理员且启用租户功能时显示 -->
  <el-select
    class="min-w-200px mr-2"
    v-if="isSuperAdmin && tenantEnabled"
    v-model="selectedTenantId"
    filterable
    reserve-keyword
    :placeholder="t('navbar.selectTenant')"
    @change="handleTenantSelect"
  >
    <!-- 租户选项列表 -->
    <el-option v-for="tenant in tenantOptions" :key="tenant.tenantId" :label="tenant.companyName" :value="tenant.tenantId" />

    <!-- 选择框前缀图标 -->
    <template #prefix>
      <Icon code="company" class="el-input__icon input-icon" animate="shake" />
    </template>
  </el-select>
</template>

<script setup lang="ts" name="TenantSelect">
import { clearDynamicTenant, setDynamicTenant } from '@/api/system/tenant/tenant/tenantApi'
import { getTenantConfig } from '@/api/system/auth/authApi'
import { closeAllPage, refreshPage } from '@/utils/tab'
import { type TenantOptionVo } from '@/api/system/auth/authTypes'

/**
 * ===== 常量定义 =====
 */

// 超级管理员用户ID
const SUPER_ADMIN_USER_ID = 1

/**
 * ===== 依赖注入 =====
 */

const { t } = useI18n()
const router = useRouter()
const userStore = useUserStore()

/**
 * ===== 组件事件定义 =====
 */

const emit = defineEmits<{
  'tenant-change': [isDynamicMode: boolean]
}>()

/**
 * ===== 响应式状态 =====
 */

// 当前选中的租户ID
const selectedTenantId = ref<string | undefined>(undefined)

// 可用租户列表
const tenantOptions = ref<TenantOptionVo[]>([])

// 租户功能是否启用
const tenantEnabled = ref<boolean>(true)

// 是否处于动态租户模式
const isDynamicTenantMode = ref<boolean>(false)

/**
 * ===== 计算属性 =====
 */

// 判断当前用户是否为超级管理员
const isSuperAdmin = computed(() => userStore.userInfo?.userId === SUPER_ADMIN_USER_ID)

/**
 * ===== 数据获取方法 =====
 */

/**
 * ===== 租户操作方法 =====
 */

/**
 * 处理租户选择事件
 * 用户选择租户时切换到动态租户模式
 * @param {string} tenantId - 选中的租户ID
 */
const handleTenantSelect = async (tenantId: string): Promise<void> => {
  if (!tenantId) return

  const [switchErr] = await switchToDynamicTenant(tenantId)
  if (switchErr) {
    console.error('切换租户失败:', switchErr)
    // 重置选择状态
    selectedTenantId.value = undefined
    return
  }
  await refreshPage()
}

/**
 * ===== 租户模式切换方法 =====
 */

/**
 * 切换到动态租户模式
 * @param {string} tenantId - 目标租户ID
 */
const switchToDynamicTenant = async (tenantId: string): Result<any> => {
  const [err, data] = await setDynamicTenant(tenantId)
  if (err) {
    return [err, null]
  }

  updateTenantModeStatus(true)
  return [null, data]
}

/**
 * 退出动态租户模式
 */
const exitDynamicTenant = async (): Result<any> => {
  const [err, data] = await clearDynamicTenant()
  if (err) {
    return [err, null]
  }

  updateTenantModeStatus(false)
  return [null, data]
}

/**
 * 更新租户模式状态并通知父组件
 * @param {boolean} isDynamic - 是否为动态模式
 */
const updateTenantModeStatus = (isDynamic: boolean): void => {
  isDynamicTenantMode.value = isDynamic
  emit('tenant-change', isDynamic)
}

/**
 * ===== 组件初始化方法 =====
 */

/**
 * 初始化组件
 * 组件挂载时自动获取租户数据
 */
const initializeComponent = async (): Promise<void> => {
  // 检查当前用户是否为超级管理员
  if (!isSuperAdmin.value) {
    // 非超级管理员禁用租户功能
    tenantEnabled.value = false
    return
  }
  const [err, data] = await getTenantConfig()
  if (err) {
    console.error('获取租户列表失败:', err.message)
    // 出错时禁用租户功能
    tenantEnabled.value = false
    tenantOptions.value = []
    return
  }

  // 更新租户功能开关状态
  tenantEnabled.value = data.tenantEnabled ?? true

  // 如果租户功能启用，加载租户选项
  if (tenantEnabled.value) {
    tenantOptions.value = data.voList || []

    // 默认显示当前用户的租户
    const currentTenantId = userStore.userInfo?.tenantId
    if (currentTenantId) {
      selectedTenantId.value = currentTenantId
    }
  }
}

/**
 * ===== 生命周期钩子 =====
 */

onMounted(() => {
  initializeComponent()
})
</script>
