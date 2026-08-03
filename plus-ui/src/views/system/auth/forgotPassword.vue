<!-- 忘记密码 -->
<template>
  <div class="forgot-password-page">
    <!-- 左侧品牌展示区域 -->
    <AuthLeftView />

    <!-- 右侧表单区域 -->
    <div class="forgot-password-right-view">
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

      <div class="forgot-password-wrap">
        <!-- 标题和副标题 -->
        <h3 class="title">{{ t('forgotPassword.title') }}</h3>
        <p class="sub-title">{{ t('forgotPassword.subtitle') }}</p>

        <el-form class="form" ref="forgotPasswordFormRef" :model="forgotPasswordFormData" :rules="forgotPasswordFormRules" style="margin-top: 35px">
          <!-- 邮箱输入区域 -->
          <el-form-item prop="email">
            <el-input
              size="large"
              v-model="forgotPasswordFormData.email"
              type="text"
              auto-complete="off"
              :placeholder="t('forgotPassword.emailPlaceholder')"
              class="form-input"
              @keyup.enter="handleForgotPasswordSubmit"
            >
              <template #prefix>
                <Icon code="email" class="input-icon" />
              </template>
            </el-input>
          </el-form-item>

          <!-- 验证码输入区域 -->
          <el-form-item prop="code" class="captcha-form-item">
            <el-input
              size="large"
              v-model="forgotPasswordFormData.code"
              auto-complete="off"
              :placeholder="t('forgotPassword.codePlaceholder')"
              class="captcha-input"
              @keyup.enter="handleForgotPasswordSubmit"
            >
              <template #prefix>
                <Icon code="topic" class="input-icon" />
              </template>
            </el-input>
            <div class="captcha-image-container">
              <el-image :src="captcha.img" class="captcha-image" @click="refreshCaptchaImage" />
            </div>
          </el-form-item>

          <!-- 提交按钮 -->
          <div style="margin-top: 30px">
            <el-form-item class="submit-button-item">
              <el-button class="submit-btn" :loading="isSubmitting" size="large" type="primary" @click.prevent="handleForgotPasswordSubmit">
                <span v-if="!isSubmitting">{{ t('forgotPassword.submitBtn') }}</span>
                <span v-else>{{ t('forgotPassword.submitting') }}</span>
              </el-button>
            </el-form-item>
          </div>

          <!-- 返回登录链接 -->
          <div class="back-to-login-section">
            <router-link class="back-link" :to="'/login'">
              <Icon code="left" size="14px" />
              <span>{{ t('forgotPassword.backToLogin') }}</span>
            </router-link>
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
import { imgCode } from '@/api/system/auth/authApi'
import type { CaptchaVo } from '@/api/system/auth/authTypes'
import { SystemConfig } from '@/systemConfig'
import { showMsgSuccess, showMsgError } from '@/utils/modal'
import { toggleThemeWithAnimation } from '@/utils/themeAnimation'

// ==================== 系统配置与工具 ====================
const { t } = useI18n()
const router = useRouter()
const layout = useLayout()
const { setTheme } = useTheme()

// 暗黑模式状态
const isDark = computed(() => layout.dark.value)

// 当前主题色（从布局状态获取）
const currentThemeColor = computed(() => layout.theme.value)

// ==================== 响应式数据 ====================
// 表单引用
const forgotPasswordFormRef = ref<ElFormInstance>()

// 表单数据
const forgotPasswordFormData = ref({
  email: '',
  code: '',
  uuid: ''
})

// 验证码信息
const captcha = ref<CaptchaVo>({
  captchaEnabled: true,
  tenantEnabled: false,
  tenantId: '',
  registerEnabled: false,
  tenantTitle: '',
  uuid: '',
  img: ''
})

// 业务状态
const isSubmitting = ref(false) // 是否正在提交

// 页脚配置
const icpRecord = ref('粤ICP备2021091549号-1') // ICP备案号
const icpQueryUrl = 'https://beian.miit.gov.cn/' // 备案查询网站

// ==================== 表单验证规则 ====================
const forgotPasswordFormRules = computed<ElFormRules>(() => ({
  email: [
    { required: true, trigger: 'blur', message: t('forgotPassword.rule.emailRequired') },
    { type: 'email', message: t('forgotPassword.rule.emailFormat'), trigger: 'blur' }
  ],
  code: [{ required: true, trigger: 'change', message: t('forgotPassword.rule.codeRequired') }]
}))

// ==================== 数据初始化方法 ====================

/** 获取并刷新验证码图片 */
const refreshCaptchaImage = async () => {
  const [err, data] = await imgCode()

  if (err) {
    console.error('获取验证码失败:', err)
    return
  }

  // 更新验证码信息
  captcha.value = {
    captchaEnabled: data.captchaEnabled ?? true,
    tenantEnabled: data.tenantEnabled ?? false,
    tenantId: data.tenantId || '',
    registerEnabled: data.registerEnabled ?? false,
    tenantTitle: data.tenantTitle || '',
    uuid: data.uuid,
    img: data.img
  }

  // 更新表单中的验证码UUID
  if (captcha.value.captchaEnabled) {
    forgotPasswordFormData.value.uuid = captcha.value.uuid
  }
}

// ==================== 业务处理方法 ====================
/** 处理重置密码提交 */
const handleForgotPasswordSubmit = () => {
  forgotPasswordFormRef.value?.validate(async (isValid: boolean) => {
    if (isValid) {
      isSubmitting.value = true

      // TODO: 调用重置密码接口
      // 模拟异步请求
      setTimeout(async () => {
        // 假设成功
        await showMsgSuccess(t('forgotPassword.success'))

        // 跳转回登录页
        await router.push('/login')

        isSubmitting.value = false
      }, 1000)

      // 刷新验证码
      // await refreshCaptchaImage()
    }
  })
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

  // 刷新验证码
  await refreshCaptchaImage()
})
</script>

<style lang="scss" scoped>
@use '@/assets/styles/abstracts/variables' as *;

// 重置密码页面主容器 - 左右分栏布局
.forgot-password-page {
  display: flex;
  width: 100vw;
  height: 100vh;
  min-width: 320px; // 设置最小宽度,防止极小屏幕下布局崩溃
  background: var(--bg-base);
  overflow: hidden; // 禁止滚动条
}

// 右侧表单区域
.forgot-password-right-view {
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

  .forgot-password-wrap {
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
      line-height: 1.6;
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

  // 提交按钮
  .submit-button-item {
    margin-bottom: 16px;

    .submit-btn {
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

  // 返回登录区域
  .back-to-login-section {
    text-align: center;
    margin-top: 20px;

    .back-link {
      display: inline-flex;
      align-items: center;
      gap: 4px;
      color: var(--el-text-color-regular);
      text-decoration: none;
      font-size: 14px;
      transition: all 0.3s ease;

      &:hover {
        color: var(--el-color-primary);

        :deep(.icon-font) {
          transform: translateX(-2px);
        }
      }

      :deep(.icon-font) {
        transition: transform 0.3s ease;
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
  .forgot-password-page {
    // 小屏下去除默认背景,让 AuthLeftView 的渐变背景显示
    background: transparent;
  }

  .forgot-password-right-view {
    position: relative; // 改为相对定位
    z-index: 1; // 在 AuthLeftView (z-index: 0) 之上
    margin: auto;

    .forgot-password-wrap {
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
  .forgot-password-page {
    position: fixed;
    top: 0;
    background: transparent; // 小屏下去除默认背景
  }

  .forgot-password-right-view {
    position: relative;
    z-index: 1;
    box-sizing: border-box;
    width: 100% !important;
    padding: 0 30px;
    margin: auto;

    .forgot-password-wrap {
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
