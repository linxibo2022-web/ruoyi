<!-- 部门管理 -->
<template>
  <div>
    <!--  搜索栏  -->
    <ASearchForm ref="queryFormRef" v-model="queryParams" :visible="showSearch">
      <AFormInput label="模糊搜索" v-model="queryParams.searchValue" prop="searchValue" @input="handleQuery"></AFormInput>
      <AFormInput label="类别编码" v-model="queryParams.deptCategory" prop="deptCategory" @input="handleQuery"></AFormInput>
      <AFormCascader v-model="queryParams.areaCode" mode="region" label="地区" prop="areaCode" @change="handleQuery"></AFormCascader>
      <AFormSelect label="状态" v-model="queryParams.status" prop="status" :options="sys_enable_status" @change="handleQuery"></AFormSelect>
    </ASearchForm>

    <el-card shadow="hover">
      <!--   工具栏   -->
      <template #header>
        <el-row :gutter="10" class="mb-2">
          <el-col :span="1.5" v-permi="['system:dept:add']">
            <el-button type="primary" plain icon="Plus" @click="handleAdd()">
              {{ t('新增') }}
            </el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button type="info" plain icon="Sort" @click="toggleAllExpansion">
              {{ isAllExpanded ? t('button.collapse') : t('button.expand') }}
            </el-button>
          </el-col>

          <TableToolbar v-model:showSearch="showSearch" @reset-query="resetQuery" @query-table="getList"></TableToolbar>
        </el-row>
      </template>

      <!--   表格数据  -->
      <el-table
        ref="deptTableRef"
        v-loading="isLoading"
        :data="deptList"
        :height="tableHeight"
        row-key="deptId"
        :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
        :default-expand-all="isAllExpanded"
        stripe
      >
        <el-table-column :label="t('deptName', '部门名称')" prop="deptName" align="left" />
        <el-table-column :label="t('deptCategory', '类别编码')" prop="deptCategory" align="center" />
        <el-table-column :label="t('areaCode', '地区')" prop="areaCode" align="center" min-width="120px">
          <template #default="{ row }">
            <DictTag mode="region" :value="row.areaCode" />
          </template>
        </el-table-column>
        <el-table-column :label="t('orderNum', '排序')" prop="orderNum" align="center" />
        <el-table-column :label="t('status', '状态')" prop="status" align="center">
          <template #default="{ row }">
            <AFormSwitch v-model="row.status" @change="handleStatusChange(row)" />
          </template>
        </el-table-column>
        <el-table-column :label="t('createTime', '创建时间')" prop="createTime" align="center" />
        <el-table-column :label="t('操作')" align="center" fixed="right">
          <template #default="{ row }">
            <el-tooltip :content="t('修改')" placement="top">
              <el-button v-permi="['system:dept:update']" link type="success" icon="Edit" @click="handleUpdate(row)"></el-button>
            </el-tooltip>
            <el-tooltip :content="t('新增')" placement="top">
              <el-button v-permi="['system:dept:add']" link type="primary" icon="Plus" @click="handleAdd(row)"></el-button>
            </el-tooltip>
            <el-tooltip :content="t('删除')" placement="top">
              <el-button v-permi="['system:dept:delete']" link type="danger" icon="Delete" @click="handleDelete(row)"></el-button>
            </el-tooltip>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 添加或修改部门对话框 -->
    <AModal v-model="dialog.visible" :title="dialog.title" :loading="buttonLoading" @confirm="submitForm" @cancel="cancel">
      <el-form ref="SysDeptBoRef" :model="form" :rules="rules" label-width="auto">
        <el-row :gutter="10">
          <el-col v-if="form.parentId !== 0" :span="24">
            <AFormTreeSelect
              label="上级部门"
              v-model="form.parentId"
              prop="parentId"
              :data="deptOptions"
              :props="{ value: 'deptId', label: 'deptName', children: 'children' }"
              check-strictly
              :span="24"
            ></AFormTreeSelect>
          </el-col>
          <AFormInput label="部门名称" v-model="form.deptName" prop="deptName" span="auto"></AFormInput>
          <AFormInput label="类别编码" v-model="form.deptCategory" prop="deptCategory" span="auto"></AFormInput>
          <AFormCascader
            style="width: 100%"
            v-model="form.areaCode"
            mode="region"
            label="地区"
            prop="areaCode"
            span="auto"
            check-strictly
          ></AFormCascader>
          <el-col :span="12">
            <el-form-item :label="t('orderNum', '显示排序')" prop="orderNum">
              <el-input-number v-model="form.orderNum" controls-position="right" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="t('leader', '负责人')" prop="leader">
              <el-select v-model="form.leader" :placeholder="t('placeholder.select', '请选择') + t('leader', '负责人')" style="width: 100%">
                <el-option v-for="item in deptUserList" :key="item.userId" :label="item.userName" :value="item.userId" />
              </el-select>
            </el-form-item>
          </el-col>
          <AFormInput label="联系电话" v-model="form.phone" prop="phone" span="auto"></AFormInput>
          <AFormInput label="邮箱" v-model="form.email" prop="email" span="auto"></AFormInput>
          <AFormRadio label="部门状态" v-model="form.status" prop="status" :options="sys_enable_status" span="auto"></AFormRadio>
        </el-row>
      </el-form>
    </AModal>
  </div>
</template>

<script setup lang="ts" name="Dept">
import { listDepts, getDept, deleteDept, addDept, updateDept, listDeptsExcludeChild } from '@/api/system/core/dept/deptApi'
import type { SysDeptQuery, SysDeptBo, SysDeptVo } from '@/api/system/core/dept/deptTypes'
import type { SysUserVo } from '@/api/system/core/user/userTypes'
import { listUsersByDeptId } from '@/api/system/core/user/userApi'
import { buildTree } from '@/utils/tree'
import { isTrue, toggleStatus } from '@/utils/boolean'
import { toValidate } from '@/utils/to'
import { showMsgSuccess, showConfirm } from '@/utils/modal'

const { t } = useI18n()

interface DeptOptionsType {
  deptId: number | string
  deptName: string
  children: DeptOptionsType[]
}

/**字典数据 */
const { sys_enable_status } = useDict(DictTypes.sys_enable_status)

// 使用表格高度处理钩子
const { tableHeight, queryFormRef, showSearch } = useTableHeight()

// =========== 查询相关 ===========

/**查询参数对象*/
const queryParams = ref<SysDeptQuery>({
  pageNum: undefined,
  pageSize: undefined,
  deptName: undefined,
  deptCategory: undefined,
  status: undefined
})

/** 部门搜索按钮操作 */
const handleQuery = () => {
  getList()
}

/** 部门重置按钮操作 */
const resetQuery = () => {
  queryFormRef.value?.resetFields()
  handleQuery()
}

// =========== 部门表格数据相关 ===========
/**表格加载状态*/
const isLoading = ref(true)
/**数据列表*/
const deptList = ref<SysDeptVo[]>([])
/**表格实例*/
const deptTableRef = ref()
/**是否展开所有*/
const isAllExpanded = ref(true)

/** 查询部门列表 */
const getList = async () => {
  isLoading.value = true
  const [err, data] = await listDepts(queryParams.value)
  if (!err) {
    const tree = buildTree<SysDeptVo>(data, { id: 'deptId', parentId: 'parentId' })
    if (tree) {
      deptList.value = tree
    }
  }
  isLoading.value = false
}

/** 切换全部展开/折叠状态 */
const toggleAllExpansion = () => {
  isAllExpanded.value = !isAllExpanded.value
  setAllRowsExpansion(deptList.value, isAllExpanded.value)
}

/** 递归设置所有行的展开/折叠状态 */
const setAllRowsExpansion = (data: SysDeptVo[], shouldExpand: boolean) => {
  data.forEach((item: SysDeptVo) => {
    deptTableRef.value?.toggleRowExpansion(item, shouldExpand)
    if (item.children?.length) {
      setAllRowsExpansion(item.children, shouldExpand)
    }
  })
}

/** 删除部门操作 */
const handleDelete = async (row: SysDeptVo) => {
  const [confirmErr] = await showConfirm(`${t('是否确认删除')}${row.deptName}?`)
  if (confirmErr) return

  const [deleteErr] = await deleteDept(row.deptId)
  if (!deleteErr) {
    showMsgSuccess(t('message.deleteSuccess'))
    await getList()
  }
}

// =========== 部门表单相关 ===========
/**上级部门选项*/
const deptOptions = ref<DeptOptionsType[]>([])
/**部门用户列表*/
const deptUserList = ref<SysUserVo[]>([])
/**初始表单数据*/
const initFormData: SysDeptBo = {
  deptId: undefined,
  parentId: undefined,
  deptName: undefined,
  deptCategory: undefined,
  orderNum: 0,
  leader: undefined,
  phone: undefined,
  email: undefined,
  status: '1'
}

/**表单引用*/
const SysDeptBoRef = ref<ElFormInstance>()
/**表单提交按钮加载状态*/
const buttonLoading = ref(false)
/**对话框配置对象*/
const dialog = ref<DialogState>({
  visible: false,
  title: ''
})
/**表单数据对象*/
const form = ref<SysDeptBo>({ ...initFormData })
/**表单校验规则*/
const rules = computed<ElFormRules>(() => ({
  parentId: [{ required: true, message: t('validation.parentIdRequired', '上级部门不能为空'), trigger: 'blur' }],
  deptName: [{ required: true, message: t('validation.deptNameRequired', '部门名称不能为空'), trigger: 'blur' }],
  orderNum: [{ required: true, message: t('validation.orderNumRequired', '显示排序不能为空'), trigger: 'blur' }],
  email: [{ type: 'email', message: t('validation.emailInvalid', '请输入正确的邮箱地址'), trigger: ['blur', 'change'] }],
  phone: [{ pattern: /^1[3|4|5|6|7|8|9][0-9]\d{8}$/, message: t('validation.phoneInvalid', '请输入正确的手机号码'), trigger: 'blur' }]
}))

/** 查询当前部门的所有用户 */
const getDeptAllUser = async (deptId: any) => {
  if (deptId !== null && deptId !== '' && deptId !== undefined) {
    const [err, data] = await listUsersByDeptId(deptId)
    if (!err) {
      deptUserList.value = data
    }
  }
}

/** 部门表单重置 */
const reset = () => {
  form.value = { ...initFormData }
  SysDeptBoRef.value?.resetFields()
}

/** 取消部门编辑 */
const cancel = () => {
  reset()
  dialog.value.visible = false
}

/** 新增部门操作 */
const handleAdd = async (row?: SysDeptVo) => {
  reset()
  const [err, data] = await listDepts()
  if (!err) {
    const tree = buildTree<DeptOptionsType>(data, { id: 'deptId', parentId: 'parentId' })
    if (tree) {
      deptOptions.value = tree
      if (row && row.deptId) {
        form.value.parentId = row?.deptId
      }
      dialog.value.visible = true
      dialog.value.title = `${t('新增')}${t('dept', '部门')}`
    }
  }
}

/** 修改部门操作 */
const handleUpdate = async (row: SysDeptVo) => {
  reset()
  // 查询当前部门所有用户
  await getDeptAllUser(row.deptId)

  const [deptErr, deptData] = await getDept(row.deptId)
  if (!deptErr) {
    const [excludeErr, excludeData] = await listDeptsExcludeChild(row.deptId)
    if (!excludeErr) {
      const tree = buildTree<DeptOptionsType>(excludeData, { id: 'deptId', parentId: 'parentId' })
      if (tree) {
        deptOptions.value = tree
        if (tree.length === 0) {
          const noResultsOptions: DeptOptionsType = {
            deptId: deptData.parentId,
            deptName: deptData.parentName,
            children: []
          }
          deptOptions.value.push(noResultsOptions)
        }
      }
    }

    dialog.value.visible = true
    dialog.value.title = `${t('修改')}${t('dept', '部门')}`

    // AFormCascader 组件已优化：支持异步数据加载后自动重新绑定
    // 无需 setTimeout 延迟，直接赋值即可
    form.value = deptData
  }
}

/** 提交部门表单 */
const submitForm = async () => {
  const [validateErr] = await toValidate(SysDeptBoRef)
  if (validateErr) return

  buttonLoading.value = true
  let err: Error | null
  if (form.value.deptId) {
    ;[err] = await updateDept(form.value)
  } else {
    ;[err] = await addDept(form.value)
  }

  if (!err) {
    showMsgSuccess(form.value.deptId ? t('message.updateSuccess') : t('message.addSuccess'))
    dialog.value.visible = false
    await getList()
  }
  buttonLoading.value = false
}

/** 部门启用禁用状态修改 */
const handleStatusChange = async (row: SysDeptVo) => {
  const text = isTrue(row.status) ? t('启用') : t('停用')
  const [confirmErr] = await showConfirm(`${t('是否确认')}${text}${row.deptName}?`)
  if (confirmErr) {
    row.status = toggleStatus(row.status)
    return
  }
  const [updateErr] = await updateDept(row)
  if (updateErr) {
    row.status = toggleStatus(row.status)
    return
  }
  await getList()
  showMsgSuccess(`${text}${t('成功')}`)
}

// =========== 生命周期 ===========
/**初始化部门数据列表*/
onMounted(() => {
  getList()
})

/**页面激活时刷新部门列表*/
onActivated(() => {
  if (isLoading.value) return
  getList()
})
</script>

<style lang="scss" scoped>
/* 解决树形数据展开箭头与文本不在同一行的问题 */
:deep(.el-table__body tr td:first-child .cell) {
  display: flex;
  align-items: center;
}
</style>
