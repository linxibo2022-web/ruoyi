<!-- 社交账号绑定 -->
<template>
  <div>
    <!-- 已绑定账号表格 -->
    <el-table :data="auths" stripe row-key="id" :show-header="true">
      <el-table-column type="index" :label="t('index', '序号')" width="105" align="center" />
      <el-table-column :label="t('source', '绑定账号平台')" prop="source" align="center" />
      <el-table-column :label="t('avatar', '头像')" prop="avatar" align="center">
        <template #default="{ row }">
          <ImagePreview :src="row.avatar" />
        </template>
      </el-table-column>
      <el-table-column :label="t('userName', '系统账号')" prop="userName" align="center" />
      <el-table-column :label="t('createTime', '绑定时间')" prop="createTime" align="center" />
      <el-table-column :label="t( '操作')" align="center" fixed="right">
        <template #default="{ row }">
          <el-tooltip :content="t('button.unbind')" placement="top">
            <el-button link type="primary" @click="handleUnlockAuth(row)">{{ t('button.unbind') }}</el-button>
          </el-tooltip>
        </template>
      </el-table-column>
    </el-table>

    <!-- 可绑定的第三方平台 -->
    <div class="mt-6">
      <h4 class="text-lg font-medium mb-4 text-gray-700">{{ t('bindingTip', '你可以绑定以下第三方帐号') }}</h4>
      <div class="flex flex-wrap gap-4">
        <a
          v-for="social in availableSocialLogins"
          :key="social.type"
          href="#"
          :title="t(`login.social.${social.type}`)"
          @click.prevent="handleAuthUrl(social.type)"
          class="flex flex-col items-center min-w-20 p-2 rounded-lg hover:bg-gray-50 transition-colors cursor-pointer no-underline"
          :style="{ color: social.color || '#3b82f6' }"
        >
          <div class="mb-2">
            <Icon :code="social.icon as IconCode" class="text-2xl" />
          </div>
          <span class="text-sm">{{ t(`login.social.${social.type}`) }}</span>
        </a>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts" name="SocialBinding">
import { socialUnbind, socialBindUrl, imgCode } from '@/api/system/auth/authApi'
import { refreshPage } from '@/utils/tab'
import { showMsgSuccess, showConfirm } from '@/utils/modal'
import { getSocialConfigs, type SocialConfig } from '@/api/system/auth/socialConfig'
const { t } = useI18n()

// =========== Props 定义 ===========

/**
 * 第三方授权账号信息接口
 */
interface AuthItem {
  /**
   * 账号ID
   */
  id: string

  /**
   * 绑定账号平台
   */
  source: string

  /**
   * 头像地址
   */
  avatar: string

  /**
   * 系统用户名
   */
  userName: string

  /**
   * 绑定时间
   */
  createTime: string
}

/**
 * 组件Props接口
 */
interface UserAuthBindingProps {
  /**
   * 已绑定的第三方账号列表
   */
  auths: AuthItem[]
}

/**定义 props,提供默认值*/
const props = withDefaults(defineProps<UserAuthBindingProps>(), {
  auths: () => []
})

// =========== 数据定义 ===========
/**响应式处理已绑定账号列表*/
const auths = computed(() => props.auths)

/**可用的社交登录列表*/
const availableSocialLogins = ref<SocialConfig[]>([])

/**初始化可用的社交登录列表*/
const initSocialLogins = async () => {
  const [err, data] = await imgCode()
  if (!err && data.socialTypes) {
    availableSocialLogins.value = getSocialConfigs(data.socialTypes)
  }
}

// 组件挂载时初始化
onMounted(() => {
  initSocialLogins()
})

// =========== 账号操作 ===========
/**
 * 解绑第三方账号
 * @param row 要解绑的账号信息
 */
const handleUnlockAuth = async (row: AuthItem) => {
  const confirmMsg = t(`Are you sure to unbind "${row.source}"?`, `您确定要解除"${row.source}"的账号绑定吗?`)
  const [confirmErr] = await showConfirm(confirmMsg)
  if (confirmErr) return

  const [err] = await socialUnbind(row.id)
  if (!err) {
    showMsgSuccess(t('message.unbindSuccess', '解绑成功'))
    await refreshPage()
  }
}

/**
 * 发起第三方账号授权
 * @param source 授权来源平台
 */
const handleAuthUrl = async (source: string) => {
  const [err, data] = await socialBindUrl(source)
  if (!err) {
    window.location.href = data
  }
}
</script>
