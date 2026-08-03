<!-- 登录 -->
<template>
  <div class="login-page">
    <!-- 左侧品牌展示区域 -->
    <AuthLeftView />

    <!-- 右侧表单区域 -->
    <div class="login-right-view">
      <!-- 右上角工具栏 -->
      <div class="top-right-wrap">
        <!-- 主题色选择器 -->
        <AThemeColorPicker :current-color="currentThemeColor" @change="handleThemeColorChange" />

        <!-- 语言选择 -->
        <div class="btn language-btn">
          <LangeSelect :show-tooltip="false" :show-animate="false" :show-background="false" />
        </div>

        <!-- 暗黑模式切换 -->
        <div class="btn theme-btn" @click="toggleDarkModeWithAnimation">
          <Icon size="20px" :code="isDark ? 'sun' : 'moon'" />
        </div>
      </div>

      <div class="login-wrap">
        <!-- 标题和副标题 -->
        <h3 class="title">{{ appTitle }}</h3>
        <p v-if="captcha.tenantEnabled && captcha.tenantTitle" class="sub-title">{{ captcha.tenantTitle }}</p>
        <p v-else class="sub-title">{{ t('login.ruoyiStudio') }}</p>

        <el-form class="form" ref="loginFormRef" :model="loginFormData" :rules="loginFormRules" style="margin-top: 25px">
          <!-- 租户id输入区域 -->
          <el-form-item prop="tenantId" v-if="true">
            <el-input
              size="large"
              v-model="loginFormData.tenantId"
              type="text"
              auto-complete="off"
              :placeholder="t('login.tenantId')"
              class="form-input"
              @blur="handleTenantIdBlur"
            >
              <template #prefix>
                <Icon code="company" class="input-icon" />
              </template>
            </el-input>
          </el-form-item>

          <!-- 用户名输入区域 -->
          <el-form-item prop="userName">
            <el-input
              size="large"
              v-model="loginFormData.userName"
              type="text"
              auto-complete="off"
              :placeholder="t('login.userName')"
              class="form-input"
            >
              <template #prefix>
                <Icon code="user" class="input-icon" />
              </template>
            </el-input>
          </el-form-item>

          <!-- 密码输入区域 -->
          <el-form-item prop="password">
            <el-input
              size="large"
              v-model="loginFormData.password"
              type="password"
              auto-complete="off"
              :placeholder="t('login.password')"
              @keyup.enter="handleLoginSubmit"
              class="form-input"
            >
              <template #prefix>
                <Icon code="password" class="input-icon" />
              </template>
            </el-input>
          </el-form-item>

          <!-- 验证码输入区域 -->
          <el-form-item v-if="captcha.captchaEnabled" prop="code" class="captcha-form-item">
            <el-input
              size="large"
              v-model="loginFormData.code"
              auto-complete="off"
              :placeholder="t('login.code')"
              class="captcha-input"
              @keyup.enter="handleLoginSubmit"
            >
              <template #prefix>
                <Icon code="topic" class="input-icon" />
              </template>
            </el-input>
            <div class="captcha-image-container">
              <el-image :src="captcha.img" class="captcha-image" @click="refreshCaptchaImage" />
            </div>
          </el-form-item>

          <!-- 记住密码和忘记密码链接 -->
          <div class="remember-forgot-section">
            <el-checkbox v-model="loginFormData.rememberMe" class="remember-checkbox">
              {{ t('login.rememberPassword') }}
            </el-checkbox>
            <div class="forgot-link">
              <router-link class="link-type" :to="'/forgotPassword'">{{ t('login.forgotPassword') }}</router-link>
            </div>
          </div>

          <!-- 登录按钮 -->
          <div style="margin-top: 30px">
            <el-form-item class="login-button-item">
              <el-button class="login-btn" :loading="isSubmitting" size="large" type="primary" @click.prevent="handleLoginSubmit">
                <span v-if="!isSubmitting">{{ t('login.login') }}</span>
                <span v-else>{{ t('login.logging') }}</span>
              </el-button>
            </el-form-item>
          </div>

          <!-- 注册入口 -->
          <div v-if="captcha.registerEnabled" class="register-section">
            <span class="register-text">{{ t('login.noAccount') }}</span>
            <router-link class="register-link" :to="'/register'">{{ t('login.registerNow') }}</router-link>
          </div>

          <!-- 第三方登录按钮区域 -->
          <div v-if="availableSocialLogins.length > 0" class="social-login">
            <div class="social-icons">
              <el-button
                v-for="social in availableSocialLogins"
                :key="social.type"
                circle
                class="social-btn"
                :title="t(`login.social.${social.type}`)"
                :style="{ color: social.color }"
                @click="handleSocialLogin(social.type)"
              >
                <Icon :code="social.icon as IconCode" />
              </el-button>
            </div>
          </div>
        </el-form>
      </div>

      <!-- 页面底部版权信息 -->
      <div class="footer">
        <div class="footer-content">
          <span class="copyright">Copyright © 2025-{{ new Date().getFullYear() }} 抓蛙师(qq:770492966) All Rights Reserved.</span>
          <a v-if="icpRecord" :href="icpQueryUrl" target="_blank" class="icp-link" rel="noopener noreferrer">
            {{ icpRecord }}
          </a>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import AuthLeftView from './components/AuthLeftView.vue'
import AThemeColorPicker from '@/components/ATheme/AThemeColorPicker.vue'
import LangeSelect from '@/layouts/components/Navbar/tools/LangSelect.vue'
import { imgCode, socialBindUrl } from '@/api/system/auth/authApi'
import type { PasswordLoginBody, CaptchaVo } from '@/api/system/auth/authTypes'
import { SystemConfig } from '@/systemConfig'
import { localCache } from '@/utils/cache'
import { encryptLocal, decryptLocal } from '@/utils/crypto'
import { showMsgError } from '@/utils/modal'
import { getSocialConfigs, type SocialConfig } from '@/api/system/auth/socialConfig'
import { toggleThemeWithAnimation } from '@/utils/themeAnimation'

// ==================== 系统配置与工具 ====================
const { t } = useI18n()

const appTitle = SystemConfig.app.title
const userStore = useUserStore()
const router = useRouter()
const layout = useLayout()
const { setTheme } = useTheme()

// 暗黑模式状态
const isDark = computed(() => layout.dark.value)

// 当前主题色（从布局状态获取）
const currentThemeColor = computed(() => layout.theme.value)

// ==================== 响应式数据 ====================
// 表单引用
const loginFormRef = ref<ElFormInstance>()

// 表单数据（包含记住密码字段）
const loginFormData = ref<PasswordLoginBody>({
  tenantId: '',
  authType: 'password',
  userName: '',
  password: '',
  code: '',
  uuid: '',
  rememberMe: false
})

// 验证码信息
const captcha = ref<CaptchaVo>({
  captchaEnabled: true,
  tenantEnabled: true,
  tenantId: '',
  tenantTitle: '',
  registerEnabled: false,
  uuid: '',
  img: ''
})

// 业务状态
const isSubmitting = ref(false) // 是否正在提交登录
const redirectUrl = ref('/') // 登录成功后的重定向URL

// 页脚配置
const icpRecord = ref('粤ICP备2021091549号-1') // ICP备案号，可以为空
const icpQueryUrl = 'https://beian.miit.gov.cn/' // 备案查询网站

// 可用的社交登录列表
const availableSocialLogins = ref<SocialConfig[]>([])

// ==================== 表单验证规则 ====================
const loginFormRules: ElFormRules = {
  userName: [{ required: true, trigger: 'blur', message: t('login.rule.userName.required') }],
  password: [{ required: true, trigger: 'blur', message: t('login.rule.password.required') }],
  code: [{ required: true, trigger: 'change', message: t('login.rule.code.required') }]
}

// ==================== 路由监听 ====================
/**
 * 监听路由变化，更新重定向URL
 */
watch(
  () => router.currentRoute.value,
  (newRoute: any) => {
    const queryRedirect = newRoute.query?.redirect
    if (queryRedirect) {
      redirectUrl.value = decodeURIComponent(queryRedirect)
    }
  },
  { immediate: true }
)

// ==================== 数据初始化方法 ====================

/** 初始化表单默认值的方法 */
const initializeFormData = () => {
  setTimeout(() => {
    const isDevelopment = SystemConfig.app.env === 'development'
    const cachedTenantId = localCache.get('tenantId')

    // 只在有缓存时才设置，否则等待验证码接口返回
    if (cachedTenantId) {
      loginFormData.value.tenantId = cachedTenantId

      // 只有是默认租户且开发环境才填充测试账号
      if (isDevelopment && cachedTenantId === '000000') {
        loginFormData.value.userName = 'superadmin'
        loginFormData.value.password = 'admin123'
      }
    }
  }, 500)
}

/** 获取并刷新验证码图片 */
const refreshCaptchaImage = async (updateTenantId = false) => {
  const [err, data] = await imgCode()

  if (err) {
    console.error('获取验证码失败:', err)
    return
  }

  // 更新验证码信息
  captcha.value = {
    captchaEnabled: data.captchaEnabled ?? true,
    tenantEnabled: data.tenantEnabled ?? true,
    tenantId: data.tenantId || '',
    registerEnabled: data.registerEnabled ?? true,
    tenantTitle: data.tenantTitle || '',
    uuid: data.uuid,
    img: data.img,
    socialTypes: data.socialTypes
  }

  // 更新可用的社交登录列表
  if (data.socialTypes) {
    availableSocialLogins.value = getSocialConfigs(data.socialTypes)
  } else {
    availableSocialLogins.value = []
  }

  // 更新表单中的验证码UUID，并清空验证码输入框
  if (captcha.value.captchaEnabled) {
    loginFormData.value.uuid = captcha.value.uuid
    loginFormData.value.code = ''
  }

  // 根据参数决定是否更新租户ID
  if (updateTenantId && captcha.value.tenantId) {
    updateTenantInfo(captcha.value.tenantId)
  }
}

/** 更新租户信息（表单、缓存、URL） */
const updateTenantInfo = (tenantId: string) => {
  // 更新表单
  loginFormData.value.tenantId = tenantId

  // 更新缓存
  localCache.set('tenantId', tenantId)

  // 更新URL参数
  const url = new URL(window.location.href)
  url.searchParams.set('tenantId', tenantId)
  window.history.replaceState(window.history.state, '', url.toString())
}

/** 从本地缓存恢复登录数据（密码解密读取） */
const restoreLoginDataFromCache = () => {
  try {
    const cachedLoginData = localCache.getJSON<PasswordLoginBody>('loginData')

    if (cachedLoginData && cachedLoginData.rememberMe) {
      loginFormData.value.userName = cachedLoginData.userName || ''
      loginFormData.value.password = decryptLocal(cachedLoginData.password || '') || cachedLoginData.password || ''
      loginFormData.value.rememberMe = true
    }
  } catch (error) {
    console.error('恢复登录数据失败:', error)
  }
}

// ==================== 缓存管理方法 ====================

/** 保存登录数据到本地缓存（密码加密存储） */
const saveLoginDataToCache = () => {
  try {
    if (loginFormData.value.rememberMe) {
      const loginDataToCache: PasswordLoginBody = {
        authType: 'password',
        userName: loginFormData.value.userName,
        password: encryptLocal(loginFormData.value.password),
        rememberMe: true
      }
      localCache.setJSON('loginData', loginDataToCache)
    } else {
      clearLoginDataFromCache()
    }
  } catch (error) {
    console.error('保存登录数据失败:', error)
  }
}

/** 清除本地缓存的登录数据 */
const clearLoginDataFromCache = () => {
  try {
    localCache.remove('loginData')
  } catch (error) {
    console.error('清除登录数据失败:', error)
  }
}

// ==================== 业务处理方法 ====================
/** 处理用户登录提交 */
const handleLoginSubmit = () => {
  loginFormRef.value?.validate(async (isValid: boolean, fields: any) => {
    if (isValid) {
      isSubmitting.value = true
      // 处理记住密码功能
      saveLoginDataToCache()

      // 准备提交的登录数据（移除前端专用字段）
      const submitData: PasswordLoginBody = {
        authType: loginFormData.value.authType,
        userName: loginFormData.value.userName,
        password: loginFormData.value.password,
        code: loginFormData.value.code,
        uuid: loginFormData.value.uuid
      }

      // 执行登录
      const [error] = await userStore.loginUser(submitData)
      if (!error) {
        // 登录成功，跳转到目标页面
        const targetUrl = redirectUrl.value || '/'
        await router.push(targetUrl)
      } else {
        // 登录失败，刷新验证码（不更新租户ID）
        if (captcha.value.captchaEnabled) {
          await refreshCaptchaImage(false)
        }
      }
      isSubmitting.value = false
    } else {
      console.log('表单验证失败:', fields)
    }
  })
}

/**
 * 处理第三方社交登录
 * @param socialType 社交登录类型 (wechat_open, maxkey, topiam, gitee, github)
 */
const handleSocialLogin = async (socialType: string) => {
  const [err, data] = await socialBindUrl(socialType)

  if (err) {
    showMsgError(t('login.thirdPartyConfigError'))
  } else {
    // 跳转到第三方授权页面
    window.location.href = data
  }
}

// 租户id失去焦点 - 简化逻辑，避免循环调用
const handleTenantIdBlur = async () => {
  // 更新租户信息
  updateTenantInfo(loginFormData.value.tenantId)

  // 刷新验证码（不更新租户ID，避免循环）
  // refreshCaptchaImage 会自动清空验证码输入框
  await refreshCaptchaImage(false)
}

// ==================== 主题切换 ====================
/** 切换暗黑模式 (带圆形扩散动画) */
const toggleDarkModeWithAnimation = (event: MouseEvent) => {
  toggleThemeWithAnimation(event, isDark.value)
}

/** 处理主题色变化 */
const handleThemeColorChange = (color: string) => {
  setTheme(color)
}

// ==================== 生命周期钩子 ====================
/** 组件挂载时初始化数据 */
onMounted(async () => {
  // 先等待路由完全加载
  await nextTick()

  // 初始化表单数据（从缓存获取租户ID）
  initializeFormData()

  // 首次刷新验证码，如果没有缓存租户ID则使用接口返回的
  const hasCachedTenantId = !!localCache.get('tenantId')
  await refreshCaptchaImage(!hasCachedTenantId)

  // 恢复记住的登录数据
  restoreLoginDataFromCache()
})
</script>

<style lang="scss" scoped>
@use '@/assets/styles/abstracts/variables' as *;

// 登录页面主容器 - 左右分栏布局
.login-page {
  display: flex;
  width: 100vw;
  height: 100vh;
  min-width: 320px; // 设置最小宽度,防止极小屏幕下布局崩溃
  background: var(--bg-base);
  overflow: hidden; // 禁止滚动条
}

// 右侧表单区域
.login-right-view {
  position: relative;
  flex: 1;
  height: 100%;

  .top-right-wrap {
    position: fixed;
    top: 23px;
    right: 30px;
    z-index: 100;
    display: flex;
    gap: 10px;
    align-items: center;
    justify-content: flex-end;

    .btn {
      display: inline-flex;
      align-items: center;
      justify-content: center;
      width: 32px;
      height: 32px;
      cursor: pointer;
      user-select: none;
      transition: all 0.3s;
      font-size: 18px;
      color: var(--el-text-color-regular);

      &:hover {
        color: var(--el-color-primary);
      }

      // 语言选择器样式调整
      &.language-btn {
        :deep(.el-dropdown) {
          display: flex;
          align-items: center;
        }
      }
    }
  }

  .login-wrap {
    position: absolute;
    inset: 0;
    width: 440px;
    height: 610px;
    padding: 40px 30px 0;
    margin: auto;
    overflow: hidden;
    background-size: cover;
    border-radius: 5px;
    opacity: 0;
    transform: translateX(30px);
    animation: slideInRight 0.6s cubic-bezier(0.25, 0.46, 0.45, 0.94) forwards;

    .title {
      margin-left: -2px;
      font-size: 34px;
      font-weight: 600;
      color: var(--app-text);
    }

    .sub-title {
      margin-top: 10px;
      font-size: 14px;
      color: var(--el-text-color-secondary);
    }

    .form {
      box-sizing: border-box;
      width: 100%;
    }
  }

  // 验证码区域
  .captcha-form-item {
    :deep(.el-form-item__content) {
      display: flex;
      gap: 12px;
      align-items: center;
    }

    .captcha-input {
      flex: 1;
    }

    .captcha-image-container {
      width: 120px;
      height: 40px;
      display: flex;
      align-items: center;
      justify-content: center;

      .captcha-image {
        width: 100%;
        height: 100%;
        border-radius: 6px;
        cursor: pointer;
      }
    }
  }

  // 记住密码和忘记密码区域
  .remember-forgot-section {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;

    .remember-checkbox {
      :deep(.el-checkbox__label) {
        font-size: 14px;
        color: #4a5568;
      }
    }

    .forgot-link {
      .link-type {
        color: #5a67d8;
        text-decoration: none;
        font-size: 14px;

        &:hover {
          text-decoration: underline;
        }
      }
    }
  }

  // 注册入口区域
  .register-section {
    text-align: left;
    margin-top: 12px;
    font-size: 14px;

    .register-text {
      color: var(--el-text-color-regular);
      margin-right: 4px;
    }

    .register-link {
      color: var(--el-color-primary);
      text-decoration: none;
      font-weight: 500;
      transition: all 0.3s ease;

      &:hover {
        color: var(--el-color-primary-light-3);
        text-decoration: underline;
      }
    }
  }

  // 登录按钮
  .login-button-item {
    margin-bottom: 16px;

    .login-btn {
      width: 100%;
      border-radius: var(--radius-md);
      font-size: 16px;
      font-weight: 500;
      padding: 12px;
      transition: all var(--duration-normal) ease;

      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 8px 20px rgba(var(--el-color-primary-rgb, 64, 158, 255), 0.3);
      }

      &:active {
        transform: translateY(0);
      }
    }
  }

  // 社交登录
  .social-login {
    text-align: center;
    margin-top: 10px;

    .social-icons {
      display: flex;
      justify-content: center;
      gap: 12px;
      flex-wrap: wrap; // 允许换行,避免在小屏幕上溢出

      .social-btn {
        width: 40px;
        height: 40px;
        border-radius: 50%;
        background: linear-gradient(135deg, rgba(255, 255, 255, 0.9) 0%, rgba(255, 255, 255, 0.7) 100%);
        border: 1px solid rgba(0, 0, 0, 0.08);
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
        transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
        position: relative;
        overflow: hidden;

        // 添加光泽效果
        &::before {
          content: '';
          position: absolute;
          top: -50%;
          left: -50%;
          width: 200%;
          height: 200%;
          background: linear-gradient(45deg, transparent 30%, rgba(255, 255, 255, 0.3) 50%, transparent 70%);
          transform: translateX(-100%);
          transition: transform 0.6s;
        }

        &:hover {
          transform: translateY(-4px) scale(1.05);
          box-shadow:
            0 8px 16px rgba(0, 0, 0, 0.12),
            0 0 0 4px rgba(var(--el-color-primary-rgb, 64, 158, 255), 0.1);
          border-color: var(--el-color-primary-light-5);

          &::before {
            transform: translateX(100%);
          }
        }

        &:active {
          transform: translateY(-2px) scale(1.02);
        }

        // 图标样式
        :deep(.icon-font) {
          font-size: 20px;
          transition: all 0.3s ease;
        }

        &:hover :deep(.icon-font) {
          filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.2));
        }
      }
    }
  }

  // 暗黑模式下的社交登录按钮样式
  :deep(.dark) .social-login {
    .social-btn {
      background: linear-gradient(135deg, rgba(255, 255, 255, 0.08) 0%, rgba(255, 255, 255, 0.04) 100%);
      border-color: rgba(255, 255, 255, 0.1);
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);

      &:hover {
        background: linear-gradient(135deg, rgba(255, 255, 255, 0.12) 0%, rgba(255, 255, 255, 0.08) 100%);
        border-color: var(--el-color-primary);
      }
    }
  }
}

.footer {
  position: absolute;
  bottom: 30px;
  left: 50%;
  transform: translateX(-50%);
  color: var(--el-text-color-secondary);
  font-size: 12px;
  text-align: center;
  letter-spacing: 0.5px;

  // 当视口高度不足时自动隐藏footer
  @media (max-height: 700px) {
    display: none;
  }

  .footer-content {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 8px;

    .copyright {
      margin: 0;
    }

    .icp-link {
      color: var(--el-color-primary);
      text-decoration: none;
      transition: all var(--duration-normal) ease;
      padding: 2px 8px;
      border-radius: var(--radius-sm);
      font-size: 11px;

      &:hover {
        background: var(--bg-level-2);
        text-decoration: underline;
      }
    }
  }
}

// 动画定义
@keyframes slideInRight {
  from {
    opacity: 0;
    transform: translateX(30px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

// 响应式设计
@media (max-width: $device-ipad-pro) {
  .login-page {
    // 小屏下去除默认背景,让 AuthLeftView 的渐变背景显示
    background: transparent;
  }

  .login-right-view {
    position: relative; // 改为相对定位
    z-index: 1; // 在 AuthLeftView (z-index: 0) 之上
    margin: auto;

    .login-wrap {
      position: relative;
      width: 440px;
      height: auto;
      padding: 0 5px; // 保持少量左右内边距
      border-radius: 0;
      box-shadow: none;
      opacity: 1;
      transform: translateX(0);
      animation: none !important;
      // 小屏下背景透明,显示 AuthLeftView 的渐变背景
      background: transparent;

      // 使用 padding-top 代替 margin-top,确保表单不会太靠上
      padding-top: 15vh;
    }
  }
}

@media (max-width: $device-phone) {
  .login-page {
    position: fixed;
    top: 0;
    background: transparent; // 小屏下去除默认背景
  }

  .login-right-view {
    position: relative;
    z-index: 1;
    box-sizing: border-box;
    width: 100% !important;
    padding: 0 30px;
    margin: auto;

    .login-wrap {
      width: 100%;
      padding: 0; // 手机模式下完全移除内边距,因为外层有 30px
      padding-top: 18vh; // 手机模式下顶部间距更大
      background: transparent; // 背景透明
    }

    .top-right-wrap {
      right: 24px;
    }
  }
}
</style>
