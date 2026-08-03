<!-- 关联信息 -->
<template>
  <AModal v-model="visible" :title="t('Connection Info', '关联信息')" size="large" :showFooter="false" @close="handleClose">
    <el-tabs v-model="activeTab" class="connection-info-tabs">
      <el-tab-pane :label="t('Platform Bindings', '平台绑定')" name="bind">
        <el-table v-loading="bindLoading" :data="bindList" stripe height="260">
          <el-table-column :label="t('platformType', '平台类型')" prop="platformType" align="center" min-width="100">
            <template #default="{ row }">
              <DictTag :options="sys_platform_type" :value="row.platformType" />
            </template>
          </el-table-column>
          <el-table-column :label="t('appid', 'AppID')" prop="appid" align="center" min-width="120" show-overflow-tooltip />
          <el-table-column :label="t('openid', 'OpenID')" prop="openid" align="center" min-width="160" show-overflow-tooltip />
          <el-table-column :label="t('unionid', 'UnionID')" prop="unionid" align="center" min-width="160" show-overflow-tooltip />
          <el-table-column :label="t('createTime', '创建时间')" prop="createTime" align="center" min-width="180" />
        </el-table>
      </el-tab-pane>

      <el-tab-pane :label="t('Social Accounts', '社交账号')" name="social">
        <el-table v-loading="socialLoading" :data="socialList" stripe height="260">
          <el-table-column :label="t('source', '来源平台')" prop="source" align="center" min-width="100" />
          <el-table-column :label="t('avatar', '头像')" align="center" width="80">
            <template #default="{ row }">
              <el-avatar v-if="row.avatar" :src="row.avatar" :size="32" />
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column :label="t('nickName', '昵称')" prop="nickName" align="center" min-width="100" show-overflow-tooltip />
          <el-table-column :label="t('openId', 'OpenID')" prop="openId" align="center" min-width="160" show-overflow-tooltip />
          <el-table-column :label="t('unionId', 'UnionID')" prop="unionId" align="center" min-width="160" show-overflow-tooltip />
          <el-table-column :label="t('createTime', '创建时间')" prop="createTime" align="center" min-width="180" />
        </el-table>
      </el-tab-pane>
    </el-tabs>
  </AModal>
</template>

<script setup lang="ts" name="ConnectionInfo">
import { listSocialsByUserId } from '@/api/system/core/social/socialApi'
import { listBindsByUserId } from '@/api/business/base/bind/bindApi'
import type { SysSocialVo } from '@/api/system/core/social/socialTypes'
import type { BindVo } from '@/api/business/base/bind/bindTypes'

const { t } = useI18n()
const { sys_platform_type } = useDict(DictTypes.sys_platform_type)

interface ConnectionInfoProps {
  modelValue: boolean
  userId?: string | number
}

interface ConnectionInfoEmits {
  (e: 'update:modelValue', value: boolean): void
}

const props = withDefaults(defineProps<ConnectionInfoProps>(), {
  modelValue: false,
  userId: undefined
})

const emit = defineEmits<ConnectionInfoEmits>()

// =========== 对话框控制 ===========
const visible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

// =========== 数据相关 ===========
const activeTab = ref('bind')
const bindLoading = ref(false)
const socialLoading = ref(false)
const bindList = ref<BindVo[]>([])
const socialList = ref<SysSocialVo[]>([])

/** 加载平台绑定数据 */
const loadBindList = async () => {
  if (!props.userId) return
  bindLoading.value = true
  const [err, data] = await listBindsByUserId(props.userId)
  if (!err) {
    bindList.value = data
  }
  bindLoading.value = false
}

/** 加载社交账号数据 */
const loadSocialList = async () => {
  if (!props.userId) return
  socialLoading.value = true
  const [err, data] = await listSocialsByUserId(props.userId)
  if (!err) {
    socialList.value = data
  }
  socialLoading.value = false
}

/** 关闭对话框 */
const handleClose = () => {
  visible.value = false
  activeTab.value = 'bind'
  bindList.value = []
  socialList.value = []
}

// =========== 监听变化 ===========
watch(
  () => props.modelValue,
  (newValue) => {
    if (newValue && props.userId) {
      loadBindList()
      loadSocialList()
    }
  }
)
</script>
