<!-- 通知公告 -->
<template>
  <div>
    <!--  搜索栏  -->
    <ASearchForm ref="queryFormRef" v-model="queryParams" :visible="showSearch">
      <AFormInput label="模糊搜索" prop="searchValue" v-model="queryParams.searchValue" @input="handleQuery"></AFormInput>
      <AFormInput label="创建人" v-model="queryParams.createByName" prop="createByName" @input="handleQuery"></AFormInput>
      <AFormSelect label="类型" v-model="queryParams.noticeType" prop="noticeType" :options="sys_notice_type" @change="handleQuery"></AFormSelect>
    </ASearchForm>

    <el-card shadow="hover">
      <!--   工具栏   -->
      <template #header>
        <el-row :gutter="10" class="mb-2">
          <el-col :span="1.5" v-permi="['system:notice:add']">
            <el-button type="primary" plain icon="Plus" @click="handleAdd">
              {{ t('新增') }}
            </el-button>
          </el-col>
          <el-col :span="1.5" v-permi="['system:notice:update']">
            <el-button type="success" plain icon="Edit" :disabled="selectionItems.length !== 1" @click="handleUpdate()">
              {{ t('修改') }}
            </el-button>
          </el-col>
          <el-col :span="1.5" v-permi="['system:notice:delete']">
            <el-button type="danger" plain icon="Delete" :disabled="selectionItems.length === 0" @click="handleDelete()">
              {{ t('删除') }}
            </el-button>
          </el-col>

          <TableToolbar v-model:showSearch="showSearch" @reset-query="resetQuery" @query-table="getList"></TableToolbar>
        </el-row>
      </template>

      <!--   表格数据  -->
      <el-table ref="noticeTableRef" v-loading="isLoading" :data="noticeList" :height="tableHeight" stripe @selection-change="handleSelectionChange">
        <el-table-column type="selection" align="center" />
        <el-table-column :label="t('noticeTitle', '公告标题')" prop="noticeTitle" align="center" show-overflow-tooltip />
        <el-table-column :label="t('noticeType', '公告类型')" prop="noticeType" align="center">
          <template #default="{ row }">
            <DictTag :options="sys_notice_type" :value="row.noticeType" />
          </template>
        </el-table-column>
        <el-table-column :label="t('Push Scope', '推送范围')" prop="targetText" align="center" show-overflow-tooltip />
        <el-table-column :label="t('Read Status', '已读状态')" align="center">
          <template #default="{ row }">
            <span>{{ row.readCount || 0 }}/{{ row.targetCount || 0 }}</span>
          </template>
        </el-table-column>
        <el-table-column :label="t('status', '状态')" prop="status" align="center">
          <template #default="{ row }">
            <DictTag :options="sys_notice_status" :value="row.status" />
          </template>
        </el-table-column>
        <el-table-column :label="t('createByName', '创建者')" prop="createByName" align="center" />
        <el-table-column :label="t('createTime', '创建时间')" prop="createTime" align="center" />
        <el-table-column :label="t('Operation', '操作')" align="center" fixed="right">
          <template #default="{ row }">
            <el-tooltip :content="t('修改')" placement="top">
              <el-button v-permi="['system:notice:update']" link type="success" icon="Edit" @click="handleUpdate(row)"></el-button>
            </el-tooltip>
            <el-tooltip :content="t('删除')" placement="top">
              <el-button v-permi="['system:notice:delete']" link type="danger" icon="Delete" @click="handleDelete(row)"></el-button>
            </el-tooltip>
          </template>
        </el-table-column>
      </el-table>

      <Pagination v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />
    </el-card>

    <!-- 添加或修改公告对话框 -->
    <AModal v-model="dialog.visible" :title="dialog.title" mode="dialog" size="large" :loading="buttonLoading" @confirm="submitForm" @cancel="cancel">
      <el-form ref="noticeFormRef" :model="form" :rules="rules" label-width="auto">
        <el-row>
          <AFormInput label="公告标题" v-model="form.noticeTitle" prop="noticeTitle" span="auto"></AFormInput>
          <AFormSelect label="公告类型" v-model="form.noticeType" prop="noticeType" :options="sys_notice_type" span="auto"></AFormSelect>

          <!-- 推送设置 -->
          <el-col :span="24">
            <el-form-item :label="t('Push Scope', '推送范围')" prop="pushType">
              <el-radio-group v-model="form.pushType" @change="handlePushTypeChange">
                <el-radio value="all">{{ t('All', '全员') }}</el-radio>
                <el-radio value="dept">{{ t('By Dept', '按部门') }}</el-radio>
                <el-radio value="role">{{ t('By Role', '按角色') }}</el-radio>
                <el-radio value="user">{{ t('Specific Users', '指定用户') }}</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>

          <!-- 部门选择 -->
          <AFormTreeSelect
            :span="24"
            v-if="form.pushType === 'dept'"
            label="推送部门"
            v-model="form.deptIds"
            :data="deptOptions"
            multiple
            show-checkbox
          ></AFormTreeSelect>

          <!-- 角色选择 -->
          <AFormSelect
            :span="24"
            v-if="form.pushType === 'role'"
            label="推送角色"
            v-model="form.roleIds"
            :options="roleOptions"
            value-field="roleId"
            label-field="roleName"
            multiple
          ></AFormSelect>

          <!-- 用户选择 -->
          <el-col :span="24" v-if="form.pushType === 'user'">
            <el-form-item :label="t('Push Users', '推送用户')">
              <UserSelect
                v-model="selectedUsers"
                :multiple="true"
                show-inline-tags
                :initial-user-names="initialUserNames"
                :button-text="t('Select Users', '选择推送用户')"
              />
            </el-form-item>
          </el-col>

          <AFormRadio label="状态" v-model="form.status" prop="status" :options="sys_notice_status" :span="24"></AFormRadio>
          <AFormEditor label="内容" v-model="form.noticeContent" prop="noticeContent" :span="24"></AFormEditor>
        </el-row>
      </el-form>
    </AModal>
  </div>
</template>

<script setup lang="ts" name="Notice">
import { pageNotices, getNotice, deleteNotices, addNotice, updateNotice } from '@/api/system/config/notice/noticeApi'
import type { SysNoticeQuery, SysNoticeBo, SysNoticeVo } from '@/api/system/config/notice/noticeTypes'
import { getRoleOptions } from '@/api/system/core/role/roleApi'
import { getDeptTreeOptions } from '@/api/system/core/dept/deptApi'
import type { SysUserVo } from '@/api/system/core/user/userTypes'
import { toValidate } from '@/utils/to'
import { showMsgSuccess, showConfirm } from '@/utils/modal'

// 使用通知 Store
const noticeStore = useNoticeStore()

const { t } = useI18n()

/**字典数据 */
const { sys_notice_status, sys_notice_type } = useDict('sys_notice_status', 'sys_notice_type')

// 使用表格高度处理钩子
const { tableHeight, queryFormRef, showSearch } = useTableHeight()

// =========== 查询相关 ===========

/**查询参数对象*/
const queryParams = ref<SysNoticeQuery>({
  pageNum: 1,
  pageSize: 10,
  orderByColumn: 'updateTime',
  isAsc: 'desc',
  noticeTitle: '',
  createByName: '',
  status: '',
  noticeType: ''
})

/** 公告搜索按钮操作 */
const handleQuery = () => {
  queryParams.value.pageNum = 1
  getList()
}

/** 公告重置按钮操作 */
const resetQuery = () => {
  queryFormRef.value?.resetFields()
  handleQuery()
}

// =========== 公告表格数据相关 ===========
/**表格加载状态*/
const isLoading = ref(true)
/**数据列表*/
const noticeList = ref<SysNoticeVo[]>([])
/**总记录数*/
const total = ref(0)
/**表格实例*/
const noticeTableRef = ref()
/**选中的数据项*/
const selectionItems = ref<SysNoticeVo[]>([])

/** 表格多选事件处理 */
const handleSelectionChange = (selection: SysNoticeVo[]) => {
  selectionItems.value = selection
}

/** 查询公告列表 */
const getList = async () => {
  isLoading.value = true
  const [err, data] = await pageNotices(queryParams.value)
  if (!err) {
    // 处理显示数据
    noticeList.value = data.records.map((item) => {
      // 解析推送配置显示文本
      if (item.targetConfig) {
        try {
          const config = JSON.parse(item.targetConfig)
          if (config.type === 'all') {
            item.targetText = t('All', '全员')
          } else if (config.names && config.names.length > 0) {
            item.targetText = config.names.join('、')
          }
        } catch (e) {
          item.targetText = t('All', '全员')
        }
      } else {
        item.targetText = t('All', '全员')
      }

      // 计算已读状态
      const readCount = item.readUserIds ? item.readUserIds.split(',').filter((id) => id.trim()).length : 0
      const targetCount = item.targetUserIds ? item.targetUserIds.split(',').filter((id) => id.trim()).length : 0
      item.readCount = readCount
      item.targetCount = targetCount

      return item
    })
    total.value = data.total
  }
  isLoading.value = false
}

/** 删除公告操作 */
const handleDelete = async (row?: SysNoticeVo) => {
  const idsToDelete = row ? [row.noticeId] : selectionItems.value.map((item) => item.noticeId)
  if (idsToDelete.length === 0) return
  const itemsToDelete = row ? row.noticeTitle : selectionItems.value.map((item) => item.noticeTitle).join('、')
  const [confirmErr] = await showConfirm(`${t('Confirm delete', '是否确认删除')}${itemsToDelete}`)
  if (confirmErr) return

  const [deleteErr] = await deleteNotices(idsToDelete)
  if (!deleteErr) {
    showMsgSuccess(t('message.deleteSuccess'))
    await getList()
  }
}

// =========== 公告表单相关 ===========
/**初始表单数据*/
const initFormData: SysNoticeBo = {
  noticeId: undefined,
  noticeTitle: '',
  noticeType: '',
  noticeContent: '',
  status: '1',
  remark: '',
  createByName: '',
  pushType: 'all',
  deptIds: [],
  roleIds: [],
  userIds: []
}

/**表单引用*/
const noticeFormRef = ref<ElFormInstance>()
/**表单提交按钮加载状态*/
const buttonLoading = ref(false)
/**对话框配置对象*/
const dialog = ref<DialogState>({
  visible: false,
  title: ''
})
/**表单数据对象*/
const form = ref<SysNoticeBo>({ ...initFormData })
/**表单校验规则*/
const rules = ref<ElFormRules>({
  noticeTitle: [{ required: true, message: t('Notice title required', '公告标题不能为空'), trigger: 'blur' }],
  noticeType: [{ required: true, message: t('Notice type required', '公告类型不能为空'), trigger: 'change' }]
})

// =========== 推送相关选项 ===========
const deptOptions = ref([])
const roleOptions = ref([])
const selectedUsers = ref<SysUserVo[]>([]) // 选中的用户列表
const initialUserNames = ref<string[]>([]) // 初始用户名列表（用于编辑时显示）

/** 加载部门数据 */
const loadDeptOptions = async () => {
  const [err, data] = await getDeptTreeOptions()
  if (!err) {
    deptOptions.value = data
  }
}

/** 加载角色数据 */
const loadRoleOptions = async () => {
  const [err, data] = await getRoleOptions()
  if (!err) {
    roleOptions.value = data
  }
}

/** 推送类型变化处理 */
const handlePushTypeChange = (value: string) => {
  // 清空之前的选择
  form.value.deptIds = []
  form.value.roleIds = []
  form.value.userIds = []
  selectedUsers.value = [] // 清空选中的用户
  initialUserNames.value = [] // 清空初始用户名

  // 按需加载数据
  if (value === 'dept' && deptOptions.value.length === 0) {
    loadDeptOptions()
  } else if (value === 'role' && roleOptions.value.length === 0) {
    loadRoleOptions()
  }
}

/** 公告表单重置 */
const reset = () => {
  form.value = { ...initFormData }
  selectedUsers.value = [] // 重置选中用户
  initialUserNames.value = [] // 重置初始用户名
  noticeFormRef.value?.resetFields()
}

/** 取消公告编辑 */
const cancel = () => {
  reset()
  dialog.value.visible = false
}

/** 新增公告操作 */
const handleAdd = () => {
  reset()
  dialog.value.visible = true
  dialog.value.title = `${t('新增')}${t('Notice', '公告')}`
}

/** 修改公告操作 */
const handleUpdate = async (row?: SysNoticeVo) => {
  reset()
  const itemToEdit = row || selectionItems.value[0]
  const [err, data] = await getNotice(itemToEdit.noticeId)
  if (!err) {
    form.value = { ...data }

    // 解析推送配置
    if (data.targetConfig) {
      try {
        const config = JSON.parse(data.targetConfig)
        form.value.pushType = config.type || 'all'
        if (config.ids) {
          if (config.type === 'dept') {
            form.value.deptIds = config.ids
            loadDeptOptions()
          } else if (config.type === 'role') {
            form.value.roleIds = config.ids
            loadRoleOptions()
          } else if (config.type === 'user') {
            form.value.userIds = config.ids
            // 从配置中恢复用户信息
            if (config.names && config.ids) {
              // 设置初始用户名，用于UserSelect组件显示
              initialUserNames.value = config.names
              // 设置选中的用户对象
              selectedUsers.value = config.ids.map((id: number, index: number) => ({
                userId: id,
                nickName: config.names[index] || `用户${id}`,
                userName: config.names[index] || `用户${id}`
              }))
            }
          }
        }
      } catch (e) {
        form.value.pushType = 'all'
      }
    }

    dialog.value.visible = true
    dialog.value.title = `${t('修改')}${t('Notice', '公告')}`
  }
}

/** 提交公告表单 */
const submitForm = async () => {
  const [validateErr] = await toValidate(noticeFormRef)
  if (validateErr) return

  // 同步用户选择到表单
  if (form.value.pushType === 'user') {
    form.value.userIds = selectedUsers.value.map((user) => user.userId)
  }

  buttonLoading.value = true
  let err: Error | null, msg: string | null
  if (form.value.noticeId) {
    ;[err] = await updateNotice(form.value)
  } else {
    ;[err] = await addNotice(form.value)
  }

  if (!err) {
    showMsgSuccess(form.value.noticeId ? t('message.updateSuccess') : t('message.addSuccess'))
    dialog.value.visible = false
    await getList()
    // 刷新全局未读数量角标
    noticeStore.refreshUnreadCount()
  }
  buttonLoading.value = false
}

// =========== 监听器 ===========
/**
 * 监听选中用户变化，同步到表单数据
 */
watch(
  selectedUsers,
  (newUsers) => {
    if (form.value.pushType === 'user') {
      form.value.userIds = newUsers.map((user) => user.userId)
    }
  },
  { deep: true }
)

// =========== 生命周期 ===========
/**初始化公告数据列表*/
onMounted(() => {
  getList()
})

/**页面激活时刷新公告列表*/
onActivated(() => {
  if (isLoading.value) return
  getList()
})
</script>
