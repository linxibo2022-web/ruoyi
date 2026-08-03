<!-- 个人中心 -->
<template>
  <div>
    <el-row :gutter="20">
      <el-col class="mb-4" :span="6" :lg="6" :md="8" :sm="24" :xs="24">
        <el-card class="h-full">
          <template #header>
            <div class="flex items-center justify-between">
              <span class="font-medium">{{ t('Profile', '个人信息') }}</span>
            </div>
          </template>
          <div>
            <div class="text-center mb-6">
              <UserAvatar />
            </div>
            <div class="space-y-1 text-13px">
              <div class="flex justify-between items-center py-3 border-b border-b-solid border-b-gray-100">
                <div class="flex items-center gap-0.5 flex-shrink-0">
                  <Icon code="user" />
                  <span>{{ t('Username', '用户名称') }}</span>
                </div>
                <span class="ml-2 text-right truncate" :title="state.user.userName">{{ state.user.userName }}</span>
              </div>
              <div class="flex justify-between items-center py-3 border-b border-b-solid border-b-gray-100">
                <div class="flex items-center gap-0.5 flex-shrink-0">
                  <Icon code="phone" />
                  <span>{{ t('Phone', '手机号码') }}</span>
                </div>
                <span class="ml-2 text-right truncate" :title="state.user.phone">{{ state.user.phone }}</span>
              </div>
              <div class="flex justify-between items-center py-3 border-b border-b-solid border-b-gray-100">
                <div class="flex items-center gap-0.5 flex-shrink-0">
                  <Icon code="email" />
                  <span>{{ t('Email', '用户邮箱') }}</span>
                </div>
                <span class="ml-2 text-right truncate" :title="state.user.email">{{ state.user.email }}</span>
              </div>
              <div class="flex justify-between items-center py-3 border-b border-b-solid border-b-gray-100">
                <div class="flex items-center gap-0.5 flex-shrink-0">
                  <Icon code="tree" />
                  <span>{{ t('Department', '所属部门') }}</span>
                </div>
                <span v-if="state.user.deptName" class="ml-2 text-right truncate" :title="`${state.user.deptName} / ${state.postGroup}`"
                  >{{ state.user.deptName }} / {{ state.postGroup }}</span
                >
              </div>
              <div class="flex justify-between items-center py-3 border-b border-b-solid border-b-gray-100">
                <div class="flex items-center gap-0.5 flex-shrink-0">
                  <Icon code="role" />
                  <span>{{ t('Role', '所属角色') }}</span>
                </div>
                <span class="ml-2 text-right truncate" :title="state.roleGroup">{{ state.roleGroup }}</span>
              </div>
              <div class="flex justify-between items-center py-3 border-b border-b-solid border-b-gray-100">
                <div class="flex items-center gap-0.5 flex-shrink-0">
                  <Icon code="date" />
                  <span>{{ t('Created', '创建日期') }}</span>
                </div>
                <span class="ml-2 text-right truncate" :title="state.user.createTime">{{ state.user.createTime }}</span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col class="mb-4" :span="18" :lg="18" :md="16" :sm="24" :xs="24">
        <el-card class="h-full bg-red">
          <template #header>
            <div class="flex items-center justify-between">
              <span class="font-medium">{{ t('Basic Info', '基本资料') }}</span>
            </div>
          </template>
          <el-tabs v-model="activeTab">
            <el-tab-pane :label="t('Basic Info', '基本资料')" name="userinfo">
              <UserInfo :user="userForm" />
            </el-tab-pane>
            <el-tab-pane :label="t('Change Password', '修改密码')" name="resetPwd">
              <resetPwd />
            </el-tab-pane>
            <el-tab-pane :label="t('Third Party', '第三方应用')" name="thirdParty">
              <SocialBinding :auths="state.auths" />
            </el-tab-pane>
            <el-tab-pane :label="t('Online Devices', '在线设备')" name="onlineDevice">
              <onlineDevice :devices="state.devices" />
            </el-tab-pane>
            <!-- 根据权限控制API密钥标签页是否显示 -->
            <el-tab-pane v-if="showApiKeyTab" :label="t('API Key', 'API密钥')" name="apiKey">
              <ApiKeyManage />
            </el-tab-pane>
          </el-tabs>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts" name="Profile">
import UserAvatar from './UserAvatar.vue'
import UserInfo from './UserInfo.vue'
import ResetPwd from './ResetPwd.vue'
import SocialBinding from './SocialBinding.vue'
import OnlineDevice from './OnlineDevice.vue'
import ApiKeyManage from './ApiKeyManage.vue'
import { getSocialBindingList } from '@/api/system/core/social/socialApi'
import { getUserProfile } from '@/api/system/core/user/userApi'
import { listCurrentUserOnlines } from '@/api/system/monitor/online/onlineApi'
import type { SysUserVo } from '@/api/system/core/user/userTypes'
const { t } = useI18n()

// 类型定义
interface State {
  user: Partial<SysUserVo>
  roleGroup: string
  postGroup: string
  auths: any
  devices: any
}

// 响应式数据
const activeTab = ref('userinfo')
const state = ref<State>({
  user: {},
  roleGroup: '',
  postGroup: '',
  auths: [],
  devices: []
})
const userForm = ref<any>({})

// 使用功能配置和用户store
const featureStore = useFeatureStore()
const userStore = useUserStore()

/**
 * 检查是否显示API密钥标签页
 */
const showApiKeyTab = computed(() => {
  // 先检查功能是否启用
  if (!featureStore.features.openApiEnabled) {
    return false
  }

  // 获取当前用户角色编码数组
  const userRoles = userStore.roles

  // 使用 featureStore 的 canUseOpenApi 方法检查权限
  return featureStore.canUseOpenApi(userRoles)
})

/**获取用户个人信息*/
const getUser = async () => {
  const [err, data] = await getUserProfile()
  if (err) return
  state.value.user = data.user
  userForm.value = { ...data.user }
  state.value.roleGroup = data.roleGroup
  state.value.postGroup = data.postGroup
}

/**获取授权列表*/
const getAuths = async () => {
  const [err, data] = await getSocialBindingList()
  if (err) return
  state.value.auths = data
}

/**获取当前用户登录在线设备*/
const getOnlines = async () => {
  const [err, data] = await listCurrentUserOnlines()
  if (err) return
  state.value.devices = data
}

onMounted(() => {
  getUser()
  getAuths()
  getOnlines()
})
</script>
