<!-- 注册 -->
<template>
  <div class="register-page">
    <!-- 左侧品牌展示区域 -->
    <AuthLeftView />

    <!-- 右侧表单区域 -->
    <div class="register-right-view">
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

      <div class="register-wrap">
        <!-- 标题和副标题 -->
        <h3 class="title">{{ appTitle }}</h3>
        <p v-if="captcha.tenantEnabled && captcha.tenantTitle" class="sub-title">{{ captcha.tenantTitle }}</p>
        <p v-else class="sub-title">{{ t('register.subtitle') }}</p>

        <!-- 邀请信息显示 -->
        <div v-if="inviteInfo" class="invite-info">
          <el-alert
            :title="`${t('register.invitedRole')}: ${inviteInfo.roleName} | ${t('register.department')}: ${inviteInfo.deptName}`"
            type="success"
            :closable="false"
            show-icon
            class="invite-alert"
          >
            <template #default>
              <div class="invite-detail">
                <p>
                  <strong>{{ t('register.inviteRole') }}:</strong> {{ inviteInfo.roleName }}
                </p>
                <p>
                  <strong>{{ t('register.department') }}:</strong> {{ inviteInfo.deptName }}
                </p>
                <p v-if="inviteInfo.needApproval" class="approval-notice">
                  <el-icon>
                    <InfoFilled />
                  </el-icon>
                  {{ t('register.approvalRequired') }}
                </p>
              </div>
            </template>
          </el-alert>
        </div>

        <el-form class="form" ref="registerFormRef" :model="registerFormData" :rules="registerFormRules" style="margin-top: 25px">
          <!-- 租户id输入区域 - 仅在没有邀请码时显示 -->
          <el-form-item prop="tenantId" v-if="!inviteInfo">
            <el-input
              size="large"
              v-model="registerFormData.tenantId"
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
              v-model="registerFormData.userName"
              type="text"
              auto-complete="off"
              :placeholder="t('register.userName')"
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
              v-model="registerFormData.password"
              type="password"
              auto-complete="off"
              :placeholder="t('register.password')"
              class="form-input"
              show-password
            >
              <template #prefix>
                <Icon code="password" class="input-icon" />
              </template>
            </el-input>
          </el-form-item>

          <!-- 确认密码输入区域 -->
          <el-form-item prop="confirmPassword">
            <el-input
              size="large"
              v-model="registerFormData.confirmPassword"
              type="password"
              auto-complete="off"
              :placeholder="t('register.confirmPassword')"
              @keyup.enter="handleRegisterSubmit"
              class="form-input"
              show-password
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
              v-model="registerFormData.code"
              auto-complete="off"
              :placeholder="t('register.code')"
              class="captcha-input"
              @keyup.enter="handleRegisterSubmit"
            >
              <template #prefix>
                <Icon code="topic" class="input-icon" />
              </template>
            </el-input>
            <div class="captcha-image-container">
              <el-image :src="captcha.img" class="captcha-image" @click="refreshCaptchaImage" />
            </div>
          </el-form-item>

          <!-- 注册按钮 -->
          <div style="margin-top: 30px">
            <el-form-item class="register-button-item">
              <el-button class="register-btn" :loading="isSubmitting" size="large" type="primary" @click.prevent="handleRegisterSubmit">
                <span v-if="!isSubmitting">{{ t('register.register') }}</span>
                <span v-else>{{ t('register.registering') }}</span>
              </el-button>
            </el-form-item>
          </div>

          <!-- 登录链接 -->
          <div class="login-link-section">
            <div class="login-link">
              {{ t('register.hasAccount') }}
              <router-link class="link-type" :to="'/login'">{{ t('register.switchLoginPage') }}</router-link>
            </div>
          </div>

          <!-- 第三方注册按钮区域 -->
          <div v-if="availableSocialLogins.length > 0 && socialAutoRegisterEnabled" class="social-login">
            <div class="social-icons">
              <el-button
                v-for="social in availableSocialLogins"
                :key="social.type"
                circle
                class="social-btn"
                :title="t(`login.social.${social.type}`)"
                :style="{ color: social.color }"
                @click="handleSocialRegister(social.type)"
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
import { InfoFilled } from '@element-plus/icons-vue'
import AuthLeftView from './components/AuthLeftView.vue'
import AThemeColorPicker from '@/components/ATheme/AThemeColorPicker.vue'
import LangeSelect from '@/layouts/components/Navbar/tools/LangSelect.vue'
import { imgCode, userRegister, socialBindUrl } from '@/api/system/auth/authApi'
import { validateRoleInvite } from '@/api/system/core/role/roleApi'
import type { CaptchaVo, RegisterBody } from '@/api/system/auth/authTypes'
import type { RoleInviteVo } from '@/api/system/core/role/roleTypes'
import { SystemConfig } from '@/systemConfig'
import { showAlertSuccess, showMsgError, showMsgWarning } from '@/utils/modal'
import { localCache } from '@/utils/cache'
import { getSocialConfigs, type SocialConfig } from '@/api/system/auth/socialConfig'
import { toggleThemeWithAnimation } from '@/utils/themeAnimation'

// ==================== 系统配置与工具 ====================
const { t } = useI18n()
const appTitle = SystemConfig.app.title
const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const layout = useLayout()
const { setTheme } = useTheme()

// 暗黑模式状态
const isDark = computed(() => layout.dark.value)

// 当前主题色（从布局状态获取）
const currentThemeColor = computed(() => layout.theme.value)

// ==================== 响应式数据 ====================
// 表单引用
const registerFormRef = ref<ElFormInstance>()

// 表单数据
const registerFormData = ref<RegisterBody>({
  tenantId: '',
  authType: 'password',
  userType: 'pc_user',
  userName: '',
  password: '',
  confirmPassword: '',
  code: '',
  uuid: '',
  inviteCode: ''
})

// 验证码信息
const captcha = ref<CaptchaVo>({
  captchaEnabled: true,
  tenantEnabled: true,
  tenantId: '',
  registerEnabled: false,
  tenantTitle: '',
  uuid: '',
  img: ''
})

// 邀请信息
const inviteInfo = ref<RoleInviteVo | null>(null)

// 业务状态
const isSubmitting = ref(false) // 是否正在提交注册

// 页脚配置
const icpRecord = ref('粤ICP备2021091549号-1') // ICP备案号，可以为空
const icpQueryUrl = 'https://beian.miit.gov.cn/' // 备案查询网站

// 可用的社交登录列表
const availableSocialLogins = ref<SocialConfig[]>([])

// 是否开启社交登录自动注册
const socialAutoRegisterEnabled = ref(false)

// ==================== 表单验证规则 ====================
/**
 * 验证确认密码是否与密码一致
 */
const validateConfirmPassword = (rule: any, value: string, callback: any) => {
  if (registerFormData.value.password !== value) {
    callback(new Error(t('register.rule.confirmPassword.equalToPassword')))
  } else {
    callback()
  }
}

const registerFormRules: ElFormRules = {
  tenantId: [{ required: true, trigger: 'blur', message: t('register.rule.tenantId.required') }],
  userName: [
    { required: true, trigger: 'blur', message: t('register.rule.userName.required') },
    { min: 2, max: 20, message: t('register.rule.userName.length', { min: 2, max: 20 }), trigger: 'blur' }
  ],
  password: [
    { required: true, trigger: 'blur', message: t('register.rule.password.required') },
    { min: 5, max: 20, message: t('register.rule.password.length', { min: 5, max: 20 }), trigger: 'blur' },
    {
      pattern: /^[^<>"'|\\]+$/,
      message: t('register.rule.password.pattern', { strings: '< > " \' \\ |' }),
      trigger: 'blur'
    }
  ],
  confirmPassword: [
    { required: true, trigger: 'blur', message: t('register.rule.confirmPassword.required') },
    { required: true, validator: validateConfirmPassword, trigger: 'blur' }
  ],
  code: [{ required: true, trigger: 'change', message: t('register.rule.code.required') }]
}

// ==================== 数据初始化方法 ====================

/** 初始化表单默认值的方法 */
const initializeFormData = async () => {
  // 从URL参数获取邀请码
  const inviteCode = route.query.inviteCode as string
  if (inviteCode) {
    registerFormData.value.inviteCode = inviteCode
    if (userStore.token) {
      await userStore.logoutUser()
    }
    await validateInviteCode(inviteCode)
  }

  // 租户ID优先级设置：
  // 1. 邀请码存在时：优先使用inviteInfo中的租户ID（验证邀请码时会设置）
  // 2. 没有邀请码时：URL参数 > 缓存 > 等待验证码接口返回
  if (!inviteInfo.value) {
    const tenantId = (route.query.tenantId as string) || localCache.get('tenantId')
    if (tenantId) {
      registerFormData.value.tenantId = tenantId
    }
    // 如果都没有，等待 refreshCaptchaImage 从验证码接口获取
  }
}

/** 验证邀请码 */
const validateInviteCode = async (inviteCode: string) => {
  const [err, data] = await validateRoleInvite(inviteCode)
  if (!err && data) {
    inviteInfo.value = data
    // 邀请注册时，租户ID使用邀请信息中的租户ID（优先级最高）
    if (data.tenantId) {
      registerFormData.value.tenantId = data.tenantId
      updateTenantInfo(data.tenantId)
    }
    showMsgWarning(`${t('register.inviteCodeSuccess')}: ${data.roleName}`)
  } else {
    showMsgError(t('register.inviteCodeInvalid'))
    registerFormData.value.inviteCode = ''
    inviteInfo.value = null
  }
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

  // 更新社交登录自动注册开关
  socialAutoRegisterEnabled.value = data.socialAutoRegisterEnabled ?? false

  // 更新表单中的验证码UUID
  if (captcha.value.captchaEnabled) {
    registerFormData.value.uuid = captcha.value.uuid
  }

  // 租户ID更新逻辑：
  // 1. 有邀请码时：不更新租户ID（邀请码中的租户ID优先级最高）
  // 2. 无邀请码且允许更新时：使用验证码返回的租户ID
  if (updateTenantId && captcha.value.tenantId && !inviteInfo.value) {
    updateTenantInfo(captcha.value.tenantId)
  }
}

/** 更新租户信息（表单、缓存、URL） */
const updateTenantInfo = (tenantId: string) => {
  // 更新表单
  registerFormData.value.tenantId = tenantId

  // 更新缓存
  localCache.set('tenantId', tenantId)

  // 更新URL参数
  const url = new URL(window.location.href)
  url.searchParams.set('tenantId', tenantId)
  window.history.replaceState(window.history.state, '', url.toString())
}

// ==================== 业务处理方法 ====================
/**
 * 处理用户注册提交
 */
const handleRegisterSubmit = () => {
  registerFormRef.value?.validate(async (isValid: boolean) => {
    if (isValid) {
      isSubmitting.value = true

      // 准备提交的注册数据（移除前端专用字段）
      const submitData: RegisterBody = {
        authType: registerFormData.value.authType,
        userType: registerFormData.value.userType,
        userName: registerFormData.value.userName,
        password: registerFormData.value.password,
        code: registerFormData.value.code,
        uuid: registerFormData.value.uuid
      }

      // 如果有邀请码，传递邀请码
      if (inviteInfo.value) {
        submitData.inviteCode = registerFormData.value.inviteCode
        submitData.tenantId = registerFormData.value.tenantId
      } else {
        // 普通注册需要租户ID
        submitData.tenantId = registerFormData.value.tenantId
      }

      const [error] = await userRegister(submitData)

      if (!error) {
        // 注册成功，显示成功消息并跳转到登录页
        const userName = registerFormData.value.userName
        let successMsg = ''

        if (inviteInfo.value) {
          if (inviteInfo.value.needApproval) {
            successMsg = `${t('register.congratsInvite')}<br/>${t('register.usernameLabel')}：<strong>${userName}</strong><br/>${t('register.roleLabel')}：<strong>${inviteInfo.value.roleName}</strong><br/><span style="color: #E6A23C;">${t('register.waitApproval')}</span>`
          } else {
            successMsg = `${t('register.congratsInvite')}<br/>${t('register.usernameLabel')}：<strong>${userName}</strong><br/>${t('register.roleLabel')}：<strong>${inviteInfo.value.roleName}</strong><br/><span style="color: #67C23A;">${t('register.loginImmediately')}</span>`
          }
        } else {
          successMsg = `${t('register.registrationSuccess')}<br/>${t('register.usernameLabel')}：<strong>${userName}</strong>`
        }

        await showAlertSuccess({
          message: successMsg,
          title: t('register.systemNotice'),
          dangerouslyUseHTMLString: true
        })

        // 跳转到登录页面
        await router.push(`/login?tenantId=${registerFormData.value.tenantId}`)
      } else {
        // 注册失败，刷新验证码（不更新租户ID）
        if (captcha.value.captchaEnabled) {
          await refreshCaptchaImage(false)
          registerFormData.value.code = ''
        }
      }
      isSubmitting.value = false
    }
  })
}

// 租户id失去焦点 - 简化逻辑，避免循环调用
const handleTenantIdBlur = async () => {
  if (!inviteInfo.value) {
    // 只有非邀请注册才处理租户ID变更
    updateTenantInfo(registerFormData.value.tenantId)

    // 刷新验证码（不更新租户ID，避免循环）
    await refreshCaptchaImage(false)

    // 清空验证码输入
    registerFormData.value.code = ''
  }
}

/**
 * 处理第三方社交注册
 * @param socialType 社交登录类型
 */
const handleSocialRegister = async (socialType: string) => {
  // 传递邀请码(如果有)
  const inviteCode = registerFormData.value.inviteCode || undefined
  const [err, data] = await socialBindUrl(socialType, inviteCode)

  if (err) {
    showMsgError(t('register.thirdPartyConfigError'))
  } else {
    // 跳转到第三方授权页面进行注册
    window.location.href = data
  }
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

  // 初始化表单数据（处理邀请码和租户ID优先级）
  await initializeFormData()

  // 请求验证码：
  // - 有邀请码时：不允许更新租户ID（inviteCode中的租户ID优先级最高）
  // - 无邀请码时：允许更新租户ID（使用验证码返回的租户ID）
  await refreshCaptchaImage(!inviteInfo.value)
})
</script>

<style lang="scss" scoped>
@use '@/assets/styles/abstracts/variables' as *;

// 注册页面主容器 - 左右分栏布局
.register-page {
  display: flex;
  width: 100vw;
  height: 100vh;
  min-width: 320px; // 设置最小宽度,防止极小屏幕下布局崩溃
  background: var(--bg-base);
  overflow: hidden; // 禁止滚动条
}

// 右侧表单区域
.register-right-view {
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

  .register-wrap {
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

    // 邀请信息样式
    .invite-info {
      margin: 15px 0 0;

      .invite-alert {
        :deep(.el-alert__content) {
          text-align: left;
        }

        :deep(.el-alert__title) {
          font-size: 13px;
          line-height: 1.4;
          margin-bottom: 8px;
        }

        .invite-detail {
          p {
            margin: 4px 0;
            font-size: 12px;
            line-height: 1.4;
          }

          .approval-notice {
            color: #e6a23c;
            font-weight: 500;
            display: flex;
            align-items: center;
            gap: 4px;
            margin-top: 8px;
          }
        }
      }
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

  // 登录链接区域
  .login-link-section {
    display: flex;
    justify-content: flex-start;
    align-items: center;
    margin-top: 12px;

    .login-link {
      font-size: 14px;
      color: var(--el-text-color-regular);

      .link-type {
        color: var(--el-color-primary);
        text-decoration: none;
        font-size: 14px;
        font-weight: 500;
        margin-left: 4px;
        transition: all 0.3s ease;

        &:hover {
          color: var(--el-color-primary-light-3);
          text-decoration: underline;
        }
      }
    }
  }

  // 注册按钮
  .register-button-item {
    margin-bottom: 16px;

    .register-btn {
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
  .register-page {
    // 小屏下去除默认背景,让 AuthLeftView 的渐变背景显示
    background: transparent;
  }

  .register-right-view {
    position: relative; // 改为相对定位
    z-index: 1; // 在 AuthLeftView (z-index: 0) 之上
    margin: auto;

    .register-wrap {
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
  .register-page {
    position: fixed;
    top: 0;
    background: transparent; // 小屏下去除默认背景
  }

  .register-right-view {
    position: relative;
    z-index: 1;
    box-sizing: border-box;
    width: 100% !important;
    padding: 0 30px;
    margin: auto;

    .register-wrap {
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
