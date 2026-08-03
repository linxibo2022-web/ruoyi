<!-- 角色邀请 -->
<template>
  <!-- 角色邀请管理对话框 -->
  <AModal v-model="visible" :title="`角色邀请管理（${currentRole.roleName}：${currentRole.roleKey}）`" mode="dialog" size="xl" @close="handleClose">
    <div class="role-invite-container">
      <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <!-- 左侧：邀请配置 -->
        <div class="invite-config">
          <h5 class="text-base font-medium text-gray-800 mb-4 border-b pb-2">创建邀请码</h5>
          <el-form ref="inviteFormRef" :model="inviteForm" :rules="inviteRules" label-width="auto" size="default">
            <el-form-item label="选择部门" prop="deptId">
              <el-tree-select
                v-model="inviteForm.deptId"
                :data="deptTreeOptions"
                :props="{ label: 'label', value: 'id' }"
                placeholder="请选择部门"
                check-strictly
                :render-after-expand="false"
                class="w-full"
              />
            </el-form-item>

            <el-form-item label="有效期" prop="validHours">
              <el-select v-model="inviteForm.validHours" placeholder="请选择有效期" class="w-full">
                <el-option label="1小时" :value="1" />
                <el-option label="6小时" :value="6" />
                <el-option label="12小时" :value="12" />
                <el-option label="1天" :value="24" />
                <el-option label="3天" :value="72" />
                <el-option label="7天" :value="168" />
              </el-select>
            </el-form-item>

            <el-form-item label="使用次数" prop="maxUseCount">
              <el-select v-model="inviteForm.maxUseCount" placeholder="请选择使用次数" class="w-full">
                <el-option label="不限制" :value="-1" />
                <el-option label="1次" :value="1" />
                <el-option label="5次" :value="5" />
                <el-option label="10次" :value="10" />
                <el-option label="20次" :value="20" />
                <el-option label="50次" :value="50" />
              </el-select>
            </el-form-item>

            <el-form-item label="审核设置">
              <el-radio-group v-model="inviteForm.needApproval" size="default">
                <el-radio :value="false">免审核</el-radio>
                <el-radio :value="true">需审核</el-radio>
              </el-radio-group>
            </el-form-item>

            <el-form-item label="备注">
              <el-input v-model="inviteForm.remark" type="textarea" :rows="2" placeholder="可选，备注此邀请码的用途" />
            </el-form-item>

            <el-form-item>
              <el-button type="primary" :loading="creating" @click="createInvite" class="w-full"> 创建邀请码</el-button>
            </el-form-item>
          </el-form>
        </div>

        <!-- 右侧：邀请码列表和详情 -->
        <div class="invite-list">
          <div class="flex items-center justify-between mb-4 pb-2 border-b">
            <h5 class="text-base font-medium text-gray-800">邀请码列表</h5>
            <div class="flex items-center gap-2">
              <el-button type="primary" size="small" icon="Refresh" :loading="loading" @click="loadInviteList"> 刷新 </el-button>
            </div>
          </div>

          <!-- 邀请码列表 -->
          <div class="invite-cards max-h-96 overflow-y-auto">
            <div v-if="loading" class="text-center py-8 text-gray-500">
              <el-icon class="animate-spin mr-2">
                <Loading />
              </el-icon>
              加载中...
            </div>

            <div v-else-if="inviteList.length === 0" class="text-center py-8 text-gray-500">暂无邀请码</div>

            <div v-else class="space-y-3 pb-2">
              <div
                v-for="invite in inviteList"
                :key="invite.inviteCode"
                class="invite-card border rounded-lg p-3 hover:shadow-md transition-shadow"
                :class="getCardStatusClass(invite)"
              >
                <div class="flex justify-between">
                  <!-- 邀请码基本信息 -->
                  <div class="text-xs text-gray-600">
                    <div class="flex items-center gap-2">
                      <el-tag type="info" size="small">{{ invite.inviteCode.substring(0, 8) }}...</el-tag>
                      <el-tag :type="getStatusTagType(invite)" size="small">
                        {{ getStatusText(invite) }}
                      </el-tag>
                      <el-button type="danger" size="small" link @click="deleteInvite(invite.inviteCode)"> 删除 </el-button>
                    </div>
                    <div class="mt-2">部门：{{ invite.deptName }}</div>
                    <div class="flex mt-1">
                      <span>使用情况：</span>
                      <span>{{ invite.currentUseCount }}/{{ invite.maxUseCount === -1 ? '∞' : invite.maxUseCount }}</span>
                    </div>
                    <div class="flex mt-1">
                      <span>有效期至：</span>
                      <span>{{ formatDate(invite.validUntil, 'YYYY-MM-DD HH:mm') }}</span>
                    </div>
                    <div class="flex mt-1">
                      <span>需要审核：</span>
                      <span>{{ invite.needApproval ? '是' : '否' }}</span>
                    </div>
                    <div v-if="invite.remark" class="text-gray-600"><span>备注：</span>{{ invite.remark }}</div>
                  </div>
                  <!-- 邀请码详情 -->
                  <div class="flex flex-col text-sm space-y-1">
                    <el-image :src="invite.inviteUrlQrcode" class="w-24 h-24 mt-2" />
                    <el-button type="primary" size="small" @click="copyLink(invite)" class="flex-1"> 复制链接 </el-button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <template #footer>
      <el-button @click="handleClose">关闭</el-button>
    </template>
  </AModal>
</template>

<script setup lang="ts">
import { Loading } from '@element-plus/icons-vue'
import { createRoleInvite, listRoleInvites, deleteRoleInvite } from '@/api/system/core/role/roleApi'
import { getDeptTreeOptions } from '@/api/system/core/dept/deptApi'
import type { CreateRoleInviteBo, RoleInviteVo, RoleInviteQueryBo, SysRoleVo } from '@/api/system/core/role/roleTypes'
import type { SysDeptTreeVo } from '@/api/system/core/dept/deptTypes'
import { toValidate } from '@/utils/to'
import { showMsgSuccess, showConfirm } from '@/utils/modal'
import { useClipboard } from '@vueuse/core'
import { formatDate } from '@/utils/date'
import QRCode from 'qrcode'
import { SystemConfig } from '@/systemConfig'

// Emits
const emit = defineEmits<{
  'success': []
}>()

// =========== 邀请功能相关 ===========
const visible = ref(false)

// 当前角色信息
const currentRole = ref<SysRoleVo>({} as SysRoleVo)

/**邀请表单数据*/
const inviteForm = ref<CreateRoleInviteBo>({
  roleId: 0,
  deptId: null,
  validHours: 24,
  maxUseCount: -1,
  remark: '',
  needApproval: false
})

/**邀请表单验证规则*/
const inviteRules = ref<ElFormRules>({
  deptId: [{ required: true, message: '请选择部门', trigger: 'change' }],
  validHours: [{ required: true, message: '请选择有效期', trigger: 'change' }],
  maxUseCount: [{ required: true, message: '请选择使用次数', trigger: 'change' }]
})

/**邀请表单引用*/
const inviteFormRef = ref<ElFormInstance>()
const creating = ref(false)

// 部门树
const deptTreeOptions = ref<SysDeptTreeVo[]>([])

// 邀请码列表
const inviteList = ref<RoleInviteVo[]>([])
const loading = ref(false)
const listQuery = ref<RoleInviteQueryBo>({
  roleId: undefined,
  status: ''
})

// 剪贴板
const { copy } = useClipboard()

// 方法
const openInviteDialog = async (role: SysRoleVo) => {
  currentRole.value = role

  // 重置表单
  inviteForm.value = {
    roleId: role.roleId,
    deptId: null as any, // 重置为空
    validHours: 24,
    maxUseCount: -1,
    remark: '',
    needApproval: false
  }

  listQuery.value.roleId = role.roleId

  // 加载数据
  await Promise.all([loadDeptTree(), loadInviteList()])

  visible.value = true
}

const loadDeptTree = async () => {
  const [err, data] = await getDeptTreeOptions()
  if (!err && data) {
    deptTreeOptions.value = data
  }
}

const loadInviteList = async () => {
  loading.value = true
  const [err, data] = await listRoleInvites(listQuery.value)
  if (!err && data) {
    for (let i = 0; i < data.length; i++) {
      const invite = data[i]
      const inviteUrl = generateInviteLink(invite)
      invite.inviteUrlQrcode = await QRCode.toDataURL(inviteUrl, {
        width: 200,
        margin: 2,
        color: {
          dark: '#000000',
          light: '#FFFFFF'
        }
      })
    }
    inviteList.value = data
  }
  loading.value = false
}

const createInvite = async () => {
  const [validateErr] = await toValidate(inviteFormRef)
  if (validateErr) return

  creating.value = true

  const [err, data] = await createRoleInvite(inviteForm.value)
  if (!err && data) {
    showMsgSuccess('邀请码创建成功')
    // 重置表单（保留角色ID和部门ID）
    const currentDeptId = inviteForm.value.deptId
    inviteForm.value = {
      roleId: currentRole.value.roleId,
      deptId: currentDeptId, // 保留已选择的部门
      validHours: 24,
      maxUseCount: -1,
      remark: '',
      needApproval: false
    }
    inviteFormRef.value?.clearValidate()

    // 刷新列表
    await loadInviteList()
    emit('success')
  }

  creating.value = false
}

const deleteInvite = async (inviteCode: string) => {
  const [confirmErr] = await showConfirm('确认删除此邀请码吗？删除后将无法使用。')
  if (confirmErr) return

  const [err] = await deleteRoleInvite(inviteCode)
  if (!err) {
    showMsgSuccess('删除成功')
    await loadInviteList()
  }
}

// 生成邀请链接
const generateInviteLink = (invite: RoleInviteVo) => {
  return `${window.location.origin}${SystemConfig.app.contextPath}register?tenantId=${invite.tenantId}&inviteCode=${invite.inviteCode}`
}

const copyLink = async (invite: RoleInviteVo) => {
  const inviteUrl = generateInviteLink(invite)
  await copy(inviteUrl)
  showMsgSuccess(`邀请链接已复制：${inviteUrl}`)
}

const handleClose = () => {
  visible.value = false
  inviteFormRef.value?.resetFields()
}

// 工具方法
const getStatusText = (invite: RoleInviteVo) => {
  if (invite.isValid) return '有效'
  if (invite.validUntil < Date.now()) return '过期'
  return '用尽'
}

const getStatusTagType = (invite: RoleInviteVo): any => {
  if (invite.isValid) return 'success'
  if (invite.validUntil < Date.now()) return 'danger'
  return 'warning'
}

const getCardStatusClass = (invite: RoleInviteVo) => {
  if (invite.isValid) return 'border-green-200 bg-green-50'
  if (invite.validUntil < Date.now()) return 'border-red-200 bg-red-50'
  return 'border-yellow-200 bg-yellow-50'
}

const formatDateTime = (timestamp: number) => {
  return new Date(timestamp).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 暴露方法
defineExpose({
  openInviteDialog
})
</script>

<style scoped>
.role-invite-container {
  max-height: 600px;
}

.invite-cards {
  scrollbar-width: thin;
  scrollbar-color: #d1d5db transparent;
}

.invite-cards::-webkit-scrollbar {
  width: 6px;
}

.invite-cards::-webkit-scrollbar-track {
  background: transparent;
}
</style>
