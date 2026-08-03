<!--用法示例
// 基础多选模式
<UserSelect v-model="selectedUsers" :multiple="true" />
// 基础单选模式
<UserSelect v-model="selectedUser" :multiple="false" />
// 显示内部标签（选择用户按钮）
<UserSelect v-model="selectedUser" show-inline-tags/>
// 自定义按钮样式
<UserSelect v-model="selectedUsers" :multiple="true" button-text="选择项目成员" button-type="success" :button-plain="false" />
// 编辑模式，传入初始用户名数据
<UserSelect v-model="userIds" :initial-user-names="userNamesString" :multiple="true" />
// 禁用标签删除功能
<UserSelect v-model="selectedUsers" :multiple="true" :readonly="true" />
// 隐藏内置标签选择器，仅使用弹窗
<UserSelect v-model="selectedUsers" :show-inline-tags="false" ref="userSelectRef" />
// 限制用户范围
<UserSelect v-model="selectedUsers" :multiple="true" :user-ids="allowedUserIds" />
// 使用预设数据
<UserSelect v-model="selectedUsers" :data="presetUsers" :multiple="true" />
// 控制默认返回类型为ID
<UserSelect v-model="selectedUsers" :multiple="true" default-return-type="id" />
// 自定义标签和按钮尺寸
<UserSelect v-model="selectedUsers" :multiple="true" button-size="large" tag-size="default" />
-->
<template>
  <div>
    <!-- 内置标签选择器 -->
    <div v-if="showInlineTags" class="user-selector-container">
      <div class="flex items-start flex-wrap gap-2">
        <!-- 选择按钮 -->
        <el-button :size="buttonSize" :type="buttonType" :plain="buttonPlain" class="flex-shrink-0" @click="open">
          {{ computedButtonText }}
          <span v-if="showCount && displayUsers.length > 0">({{ displayUsers.length }})</span>
        </el-button>

        <!-- 已选用户标签列表 -->
        <div v-if="displayUsers.length > 0" class="flex flex-wrap gap-1 flex-1 min-w-0">
          <el-tag
            v-for="user in displayUsers"
            :key="user.userId"
            :size="tagSize"
            :closable="!disabled && !readonly"
            @close="handleRemoveUser(user.userId)"
          >
            <span :title="getUserDisplayName(user)">
              {{ getUserDisplayName(user) }}
            </span>
          </el-tag>
        </div>
      </div>
    </div>

    <!-- 弹窗选择器 -->
    <AModal v-model="userDialog.visible.value" :title="userDialog.title.value" mode="dialog" size="xl" @confirm="confirm" @cancel="close">
      <!-- 使用可拖拽面板组件 -->
      <AResizablePanels v-model:leftWidth="leftPanelWidth" :min-width="200" :max-width="450" :gutter="20">
        <!-- 左侧面板：部门树 -->
        <template #left>
          <el-card shadow="hover" class="h-full">
            <template #header>
              <span class="font-medium">{{ t('userSelect.deptFilter') }}</span>
            </template>
            <el-input v-model="deptName" :placeholder="t('userSelect.deptPlaceholder')" prefix-icon="Search" clearable class="mb-3" />
            <el-tree
              ref="deptTreeRef"
              :style="{ height: `${treeHeight}px` }"
              class="overflow-y-auto"
              node-key="id"
              :data="deptOptions"
              :props="{ label: 'label', children: 'children' }"
              :expand-on-click-node="false"
              :filter-node-method="filterNode"
              highlight-current
              default-expand-all
              @node-click="handleNodeClick"
            />
          </el-card>
        </template>

        <!-- 右侧面板：用户列表 -->
        <template #right>
          <!-- 搜索表单 -->
          <ASearchForm ref="queryFormRef" v-model="queryParams" :visible="showSearch">
            <AFormInput :label="t('userSelect.userName')" prop="userName" v-model="queryParams.userName" @input="handleQuery" />
            <AFormInput :label="t('userSelect.phone')" prop="phone" v-model="queryParams.phone" @input="handleQuery" />
            <AFormDate v-model="dateRange" type="daterange" :label="t('userSelect.createTime')" @change="handleQuery" />
          </ASearchForm>

          <el-card shadow="hover">
            <!-- 显示已选项 -->
            <template #header>
              <ASelectionTags
                key-field="userId"
                :items="selectionItems"
                :formatter="(user) => getUserDisplayName(user)"
                :on-clear="selectionClear"
                @close="selectionRemove"
              />
            </template>

            <!-- 用户表格数据 -->
            <el-table
              ref="userTableRef"
              v-loading="loading"
              :data="userList"
              :height="tableHeight"
              border
              stripe
              @selection-change="handleSelectionChange"
            >
              <el-table-column v-if="multiple" type="selection" width="50" align="center" />
              <el-table-column v-else width="50" align="center">
                <template #default="{ row }">
                  <el-radio v-model="selectedUserId" :value="row.userId" @change="handleRadioChange(row)">
                    <span></span>
                  </el-radio>
                </template>
              </el-table-column>
              <el-table-column :label="t('userSelect.userId')" prop="userId" align="center" />
              <el-table-column :label="t('userSelect.userName')" prop="userName" align="center" />
              <el-table-column :label="t('userSelect.nickName')" prop="nickName" align="center" />
              <el-table-column :label="t('userSelect.dept')" prop="deptName" align="center" />
              <el-table-column :label="t('userSelect.phone')" prop="phone" align="center" width="120" />
              <el-table-column :label="t('userSelect.status')" prop="status" align="center">
                <template #default="{ row }">
                  <DictTag :options="sys_enable_status" :value="row.status" />
                </template>
              </el-table-column>
              <el-table-column :label="t('userSelect.createTime')" prop="createTime" align="center" width="160" />
            </el-table>

            <Pagination v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />
          </el-card>
        </template>
      </AResizablePanels>
    </AModal>
  </div>
</template>

<script setup lang="ts" name="UserSelect">
/**
 * 用户选择组件 (UserSelect)
 *
 * 基于 useSelection 组合函数实现的用户选择器，提供弹窗式的用户选择功能。
 * 支持部门树过滤、搜索筛选、分页查询和单选/多选模式。
 * 内置标签选择器，可以直接在组件上显示已选用户标签。
 *
 * 主要特性：
 * - 🌳 部门树过滤：支持通过部门树快速定位用户
 * - 🔍 搜索筛选：支持通过用户名称、手机号码等条件搜索
 * - 📄 跨页选择：支持在分页中保持选中状态（多选模式）
 * - 🎯 单选/多选：支持单选和多选两种模式
 * - ⚡ 智能绑定：v-model 支持 ID 或用户对象，自动跟踪类型
 * - 📄 预设激活：data 属性可预设激活项，优先级高于 v-model
 * - 🏷️ 内置标签：可直接在组件上显示已选用户的标签列表
 * - 💡 用户名显示：支持传入初始用户名数据，用于编辑时显示
 * - 🔧 高度可配置：按钮样式、标签大小、占位符等均可自定义
 * - 📋 智能返回：当 modelValue 为空时，默认返回用户对象或用户对象数组
 *
 */

import { pageUsers, getUserOptions } from '@/api/system/core/user/userApi'
import { getDeptTreeOptions } from '@/api/system/core/dept/deptApi'
import type { SysUserQuery, SysUserVo } from '@/api/system/core/user/userTypes'
import type { SysDeptTreeVo, SysDeptVo } from '@/api/system/core/dept/deptTypes'
import { addDateRange } from '@/utils/date'

const { t } = useI18n()

/**字典数据*/
const { sys_enable_status } = useDict(DictTypes.sys_enable_status)

// =========== 属性定义 ===========
interface UserSelectProps {
  /** 选中项的双向绑定值 */
  modelValue?: string | number | (string | number)[] | SysUserVo | SysUserVo[] | undefined
  /** 是否为多选模式，默认 true */
  multiple?: boolean
  /** 预设激活的用户数据 */
  data?: string | number | (string | number)[] | SysUserVo | SysUserVo[] | undefined
  /** 限制可选的用户ID范围 */
  userIds?: string | number | (string | number)[] | undefined
  /** 当 modelValue 为空时的默认返回类型，'object' 返回用户对象，'id' 返回用户ID */
  defaultReturnType?: 'object' | 'id'

  // ========== 内置标签选择器相关属性 ==========
  /** 是否显示内置标签选择器，默认 true */
  showInlineTags?: boolean
  /** 选择按钮的文本 */
  buttonText?: string
  /** 选择按钮的类型 */
  buttonType?: 'primary' | 'success' | 'warning' | 'danger' | 'info' | 'text' | ''
  /** 选择按钮是否为朴素按钮 */
  buttonPlain?: boolean
  /** 选择按钮的尺寸 */
  buttonSize?: 'large' | 'default' | 'small'
  /** 标签的尺寸 */
  tagSize?: 'large' | 'default' | 'small'
  /** 是否显示选中数量 */
  showCount?: boolean
  /** 是否禁用（禁用删除标签功能） */
  disabled?: boolean
  /** 是否只读（只读删除标签功能） */
  readonly?: boolean
  /** 初始用户名数据，用于编辑时显示用户名，格式：'用户1,用户2' 或 ['用户1', '用户2'] */
  initialUserNames?: string | string[]
}

const props = withDefaults(defineProps<UserSelectProps>(), {
  multiple: false,
  modelValue: undefined,
  data: undefined,
  userIds: undefined,
  defaultReturnType: 'object',
  showInlineTags: false,
  buttonText: undefined,
  buttonType: 'primary',
  buttonPlain: true,
  buttonSize: 'small',
  tagSize: 'small',
  showCount: true,
  disabled: false,
  readonly: false,
  initialUserNames: undefined
})

/**计算属性 - 按钮文本*/
const computedButtonText = computed(() => props.buttonText ?? t('userSelect.selectUser'))

const emit = defineEmits(['update:modelValue', 'confirmCallBack'])

// =========== 可拖拽面板相关 ===========
/**左侧面板宽度*/
const leftPanelWidth = ref(250)

/**树形结构和表格高度计算*/
const treeHeight = computed(() => {
  // 弹窗中的高度计算，考虑搜索表单、卡片头部等
  return 550
})

const tableHeight = computed(() => {
  // 表格高度，与树高度对齐
  return 410
})

// =========== 保持所有原有的工具函数和状态管理 ===========
const isUserObject = (item: any): item is SysUserVo => {
  return typeof item === 'object' && item?.userId
}

const extractUserIds = (data: any): string[] => {
  if (!data) return []
  const items = Array.isArray(data) ? data : [data]
  return items.map((item) => (isUserObject(item) ? String(item.userId) : String(item))).filter(Boolean)
}

const extractUserObjects = (data: any): SysUserVo[] => {
  if (!data) return []
  const items = Array.isArray(data) ? data : [data]
  return items.filter(isUserObject)
}

/**
 * 判断 modelValue 是否为空
 */
const isModelValueEmpty = (value: any): boolean => {
  return value === undefined || value === null || value === '' || (Array.isArray(value) && value.length === 0)
}

/**
 * 检测数据类型偏好
 * 根据现有的 modelValue 判断用户期望的数据类型
 */
const detectDataTypePreference = (): 'object' | 'id' => {
  // 如果 modelValue 为空，使用默认返回类型
  if (isModelValueEmpty(props.modelValue)) {
    return props.defaultReturnType
  }

  // 如果 modelValue 不为空，根据其类型判断
  if (Array.isArray(props.modelValue)) {
    return props.modelValue.length > 0 && isUserObject(props.modelValue[0]) ? 'object' : 'id'
  }

  return isUserObject(props.modelValue) ? 'object' : 'id'
}

/**
 * 解析初始用户名数据
 */
const parseInitialUserNames = (): string[] => {
  if (!props.initialUserNames) return []
  if (Array.isArray(props.initialUserNames)) return props.initialUserNames
  return props.initialUserNames
    .split(',')
    .map((name) => name.trim())
    .filter(Boolean)
}

/**
 * 获取用户显示名称
 */
const getUserDisplayName = (user: SysUserVo): string => {
  return user.nickName || user.userName || t('userSelect.defaultUserName', { id: user.userId })
}

// =========== 状态管理 ===========
const userDialog = useDialog({ title: t('userSelect.title') })
/** 用户列表数据 */
const userList = ref<SysUserVo[]>([])
/** 表格加载状态 */
const loading = ref(true)
/** 是否显示搜索表单 */
const showSearch = ref(true)
/** 总记录数 */
const total = ref(0)
/** 日期范围 */
const dateRange = ref<[ElDateModelType, ElDateModelType]>(['', ''])
/** 部门名称搜索关键词 */
const deptName = ref('')
/** 部门树数据 */
const deptOptions = ref<SysDeptTreeVo[]>([])

// =========== 组件引用 ===========
/** 部门树引用 */
const deptTreeRef = ref<ElTreeInstance>()
/** 查询表单引用 */
const queryFormRef = ref<ElFormInstance>()
/** 表格引用 */
const userTableRef = ref<ElTableInstance>()

// 选择状态管理
const { selectionItems, selectionIsRestoring, selectionChange, selectionSync, selectionRemove, selectionClear, selectionInit } = useSelection(
  'userId',
  userTableRef,
  userList,
  computed(() => props.multiple)
)

// 单选模式下当前选中的用户ID
const selectedUserId = computed({
  get: () => (selectionItems.value.length > 0 ? selectionItems.value[0].userId : ''),
  set: (value) => {
    if (!props.multiple && value) {
      const user = userList.value.find((u) => u.userId === value)
      if (user) selectionChange(user)
    }
  }
})

// 查询参数
const queryParams = ref<SysUserQuery>({
  pageNum: 1,
  pageSize: 10,
  userName: '',
  phone: '',
  status: '',
  deptId: '',
  roleId: '',
  userIds: ''
})

// =========== 计算属性 ===========
const sourceData = computed(() => (props.data !== undefined ? props.data : props.modelValue))
const sourceUserIds = computed(() => extractUserIds(sourceData.value))
const sourceUserObjects = computed(() => extractUserObjects(sourceData.value))

// 显示用户列表（用于内置标签选择器）
const displayUsers = computed(() => {
  // 优先使用选择状态中的用户对象
  if (selectionItems.value.length > 0) {
    return selectionItems.value
  }

  // 如果有源用户对象，直接使用
  if (sourceUserObjects.value.length > 0) {
    return sourceUserObjects.value
  }

  // 如果只有用户ID，尝试构建显示用的用户对象
  if (sourceUserIds.value.length > 0) {
    const initialNames = parseInitialUserNames()
    return sourceUserIds.value.map(
      (userId, index) =>
        ({
          userId: userId,
          nickName: initialNames[index] || '',
          userName: initialNames[index] || t('userSelect.defaultUserName', { id: userId }),
          // 其他字段使用默认值
          deptName: '',
          phone: '',
          status: '1',
          createTime: ''
        }) as SysUserVo
    )
  }

  return []
})

// =========== 方法 ===========

/**
 * 部门树过滤方法
 * 根据输入的部门名称过滤部门树节点
 *
 * @param value - 过滤关键字
 * @param data - 部门树节点数据
 * @returns 是否显示该节点
 */
const filterNode = (value: string, data: any): boolean => {
  if (!value) return true
  return data.label.includes(value)
}

// =========== 数据加载方法 ===========

/**
 * 获取部门树结构数据
 */
const getTreeSelect = async () => {
  const [err, data] = await getDeptTreeOptions()
  if (!err) deptOptions.value = data
}

/**
 * 查询用户列表数据
 * 加载完成后自动同步选中状态
 */
const getList = async () => {
  loading.value = true
  // 设置恢复标志，防止在数据加载和恢复期间的事件干扰
  selectionIsRestoring.value = true

  queryParams.value.userIds = props.userIds as any
  queryParams.value.params = {}
  addDateRange(queryParams.value, dateRange.value, 'createTime')

  const [err, data] = await pageUsers(queryParams.value)
  if (!err) {
    userList.value = data.records || []
    total.value = data.total
    await nextTick()
    await selectionSync()
  }

  loading.value = false

  //数据加载和恢复完成，重置标志
  selectionIsRestoring.value = false
}

/**
 * 智能初始化默认选中用户
 */
const initSelectedUsers = async () => {
  if (!sourceData.value) return

  await selectionInit(async () => {
    // 如果已经有用户对象，直接使用
    if (sourceUserObjects.value.length > 0) {
      return sourceUserObjects.value
    }

    // 如果只有用户ID，通过API获取用户信息
    if (sourceUserIds.value.length > 0) {
      const [err, data] = await getUserOptions(sourceUserIds.value)
      return err ? [] : data || []
    }

    return []
  })
}

// =========== 事件处理方法 ===========

/**
 * 部门树节点点击事件处理
 *
 * @param data - 被点击的部门树节点数据
 */
const handleNodeClick = (data: SysDeptVo) => {
  queryParams.value.deptId = data.id
  handleQuery()
}

/**
 * 搜索按钮操作
 * 重置页码并执行查询
 */
const handleQuery = () => {
  queryParams.value.pageNum = 1
  getList()
}

/**
 * 重置查询条件
 *
 * @param refresh - 是否在重置后刷新数据
 */
const resetQuery = (refresh = true) => {
  dateRange.value = ['', '']
  queryFormRef.value?.resetFields()
  queryParams.value.pageNum = 1
  queryParams.value.deptId = undefined
  deptTreeRef.value?.setCurrentKey(undefined)
  if (refresh) handleQuery()
}

/**
 * 表格选择变化事件处理
 * 完全按照原版本的处理逻辑
 */
const handleSelectionChange = (selection: SysUserVo[]) => {
  if (props.multiple && !selectionIsRestoring.value) {
    selectionChange(selection)
  }
}

/**
 * 单选radio变化事件处理
 */
const handleRadioChange = (row: SysUserVo) => {
  if (!props.multiple) selectionChange(row)
}

/**
 * 移除用户标签
 */
const handleRemoveUser = (userId: string | number) => {
  if (props.disabled || props.readonly) return
  // 如果有选中列表，从选中列表中移除
  const userInSelection = selectionItems.value.find((user) => user.userId === userId)
  if (userInSelection) {
    if (selectionItems.value.length === 1) {
      selectionClear() // 清空内部选择状态
      emit('update:modelValue', props.multiple ? [] : null)
      emit('confirmCallBack', props.multiple ? [] : null)
      return
    }
    selectionRemove(userInSelection)

    // 立即更新 modelValue
    const remainingUsers = selectionItems.value.filter((user) => user.userId !== userId)
    const dataTypePreference = detectDataTypePreference()
    const shouldReturnObject = dataTypePreference === 'object'

    const result = props.multiple
      ? shouldReturnObject
        ? remainingUsers
        : remainingUsers.map((u) => u.userId)
      : shouldReturnObject
        ? remainingUsers[0] || null
        : remainingUsers[0]?.userId || ''

    emit('update:modelValue', result)
    emit('confirmCallBack', result)
    return
  }
  // 如果没有选中列表（初始化状态），直接操作源数据
  if (displayUsers.value.length > 0 && selectionItems.value.length === 0) {
    const currentIndex = displayUsers.value.findIndex((user) => String(user.userId) === String(userId))
    if (currentIndex === -1) return

    const dataTypePreference = detectDataTypePreference()
    const shouldReturnObject = dataTypePreference === 'object'

    let result: any

    if (props.multiple) {
      // 多选模式：过滤掉要删除的项
      if (shouldReturnObject) {
        // 返回对象数组
        const currentObjects = sourceUserObjects.value.length > 0 ? sourceUserObjects.value : displayUsers.value
        result = currentObjects.filter((user) => String(user.userId) !== String(userId))
      } else {
        // 返回ID数组
        result = sourceUserIds.value.filter((id) => String(id) !== String(userId))
      }
    } else {
      // 单选模式：清空选择
      result = shouldReturnObject ? null : ''
    }

    emit('update:modelValue', result)
    emit('confirmCallBack', result)
  }
}

/**
 * 确认选择，智能返回数据
 */
const confirm = () => {
  const selectedUsers = selectionItems.value

  if (selectedUsers.length === 0) {
    const result = props.multiple ? [] : null
    emit('update:modelValue', result)
    emit('confirmCallBack', result)
    userDialog.closeDialog()
    return
  }

  // 检测数据类型偏好
  const dataTypePreference = detectDataTypePreference()
  const shouldReturnObject = dataTypePreference === 'object'

  const result = props.multiple
    ? shouldReturnObject
      ? selectedUsers
      : selectedUsers.map((u) => u.userId)
    : shouldReturnObject
      ? selectedUsers[0]
      : selectedUsers[0].userId

  emit('update:modelValue', result)
  emit('confirmCallBack', result)
  userDialog.closeDialog()
}

const close = () => {
  userDialog.closeDialog()
}

const open = () => {
  userDialog.openDialog()
}

// =========== 监听器 ===========

/**
 * 监听部门名称变化，实时过滤部门树
 */
watchEffect(() => deptTreeRef.value?.filter(deptName.value), { flush: 'post' })

/**
 * 监听对话框状态变化
 * 处理对话框打开和关闭时的逻辑
 */
watch(
  () => userDialog.visible.value,
  async (newValue: boolean) => {
    if (newValue) {
      // 对话框打开时初始化数据
      await getTreeSelect()
      await getList()
      await initSelectedUsers()
    } else {
      // 对话框关闭时重置状态
      resetQuery(false)
      selectionClear()
    }
  }
)

/**
 * 监听源数据变化，自动同步选择状态
 */
watch(
  () => sourceData.value,
  async () => {
    if (sourceData.value) {
      await initSelectedUsers()
    } else {
      selectionClear()
    }
  },
  { deep: true }
)

// =========== 组件导出 ===========

defineExpose({
  open,
  close: userDialog.closeDialog
})
</script>

<style scoped lang="scss">
.user-selector-container {
  width: 100%;

  .el-tag {
    margin-right: 4px;
    margin-top: 0;
  }
}

// 响应式处理
@media (max-width: 768px) {
  .user-selector-container {
    .flex {
      flex-direction: column;
      align-items: stretch;
    }

    .el-button {
      margin-bottom: 8px;
    }
  }
}
</style>
