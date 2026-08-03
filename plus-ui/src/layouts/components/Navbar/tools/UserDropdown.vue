<!-- 用户下拉菜单 -->
<template>
  <div class="mx-4">
    <!-- 用户头像下拉菜单 -->
    <el-dropdown trigger="hover" @command="handleDropdownCommand">
      <!-- 触发器：用户头像 + 昵称 -->
      <div class="flex items-center cursor-pointer gap-2 user-info-container">
        <img class="w-9 h-9 rounded-full object-cover flex-shrink-0" :src="userAvatar" :alt="t('navbar.userAvatar')" />
        <span class="user-nickname text-3.5 font-500 text-gray-700 dark:text-gray-300 truncate max-w-24" :title="userNickname">
          {{ userNickname }}
        </span>
      </div>

      <!-- 下拉菜单内容 -->
      <template #dropdown>
        <el-dropdown-menu>
          <!-- 个人中心 - 非动态租户模式时显示 -->
          <el-dropdown-item v-if="!isDynamicTenant" command="profile">
            <Icon code="user" class="mr-2" />
            {{ t('navbar.personalCenter') }}
          </el-dropdown-item>

          <!-- 项目文档 - 根据配置显示 -->
          <el-dropdown-item v-if="SystemConfig.services.docUrl" command="document">
            <Icon code="book" class="mr-2" />
            {{ t('navbar.document') }}
          </el-dropdown-item>

          <!-- Git仓库 - 根据配置显示 -->
          <el-dropdown-item v-if="SystemConfig.services.gitUrl" command="git">
            <Icon code="git" class="mr-2" />
            {{ t('navbar.git') }}
          </el-dropdown-item>

          <!-- 退出登录 - 分割线分隔 -->
          <el-dropdown-item divided command="logout">
            <Icon code="logout" class="mr-2" />
            {{ t('navbar.logout') }}
          </el-dropdown-item>
        </el-dropdown-menu>
      </template>
    </el-dropdown>
  </div>
</template>

<script setup lang="ts" name="UserDropdown">
import { to } from '@/utils/to'
import { showMsgError, showConfirm, showLoading, hideLoading } from '@/utils/modal'
import { SystemConfig } from '@/systemConfig'
/**
 * ===== 组件Props定义 =====
 */

interface UserDropdownProps {
  /** 是否处于动态租户模式 */
  isDynamicTenant?: boolean
}

const props = withDefaults(defineProps<UserDropdownProps>(), {
  isDynamicTenant: false
})

const { t } = useI18n()
const router = useRouter()
const userStore = useUserStore()

/**
 * ===== 计算属性 =====
 */

// 用户头像URL
const userAvatar = computed(() => userStore.userInfo?.avatar)

// 用户昵称
const userNickname = computed(() => userStore.userInfo?.nickName || userStore.userInfo?.userName || '用户')

/**
 * ===== 菜单命令处理器映射 =====
 */

// 下拉菜单命令与处理方法的映射关系
const commandHandlers: Record<string, () => void | Promise<void>> = {
  profile: handleProfileNavigation,
  document: handleDocumentNavigation,
  git: handleGitNavigation,
  logout: handleUserLogout
}

/**
 * ===== 用户操作处理方法 =====
 */

/**
 * 处理个人中心导航
 * 跳转到用户个人资料页面
 */
function handleProfileNavigation(): void {
  router.push('/user/profile')
}

/**
 * 处理用户退出登录
 * 显示确认对话框，确认后执行退出操作并跳转到登录页
 */
async function handleUserLogout(): Promise<void> {
  // 显示退出确认对话框
  const [confirmErr] = await showConfirm({
    message: t('Confirm logout?', '确定注销并退出系统吗？'),
    title: t('Tip', '提示'),
    confirmButtonText: t('确定'),
    cancelButtonText: t('取消'),
    type: 'warning'
  })

  // 用户取消退出
  if (confirmErr) {
    console.debug('用户取消退出')
    return
  }

  // 用户确认退出，显示加载状态
  showLoading('正在退出系统...')

  // 执行退出登录
  const [logoutErr] = await userStore.logoutUser()

  hideLoading()

  if (logoutErr) {
    showMsgError(`退出失败: ${logoutErr.message}`)
    return
  }
  // 跳转到登录页，保存当前页面路径用于登录后重定向
  const [routerErr] = await to(
    router.replace({
      path: '/login',
      query: {
        redirect: encodeURIComponent(router.currentRoute.value.fullPath || '/')
      }
    })
  )

  if (routerErr) {
    console.error('跳转到登录页失败:', routerErr)
  }
}

/**
 * 处理文档导航
 * 打开项目文档页面
 */
function handleDocumentNavigation(): void {
  window.open(SystemConfig.services.docUrl)
}

/**
 * 处理Git仓库导航
 * 打开Git仓库页面
 */
function handleGitNavigation(): void {
  window.open(SystemConfig.services.gitUrl)
}

/**
 * ===== 事件处理器 =====
 */

/**
 * 处理下拉菜单命令选择
 * 根据命令类型调用对应的处理方法
 * @param {string} command - 下拉菜单命令标识
 */
function handleDropdownCommand(command: string): void {
  const handler = commandHandlers[command]
  if (handler) {
    handler()
  } else {
    console.warn(`未知的下拉菜单命令: ${command}`)
  }
}
</script>

<style lang="scss" scoped>
.user-info-container {
  transition: all 0.2s ease;

  &:hover {
    .user-nickname {
      color: var(--el-color-primary);
    }
  }
}

.user-nickname {
  transition: color 0.2s ease;
}

/* 响应式处理 - 小屏幕隐藏昵称 */
@media (max-width: 768px) {
  .user-nickname {
    display: none;
  }
}
</style>
