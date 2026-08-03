<!-- 岗位管理 -->
<template>
  <div>
    <!-- 使用可拖拽面板组件 -->
    <AResizablePanels v-model:leftWidth="leftPanelWidth" :min-width="200" :max-width="450" :gutter="20">
      <!-- 左侧面板：部门树（虚拟滚动 + 搜索防抖，解决大数据量卡死） -->
      <template #left>
        <ADeptTree ref="deptTreeRef" :data="deptOptions" :height="deptTreeHeight" @node-click="handleNodeClick" />
      </template>

      <!-- 右侧面板：岗位列表 -->
      <template #right>
        <!-- 岗位搜索栏 -->
        <ASearchForm ref="queryFormRef" v-model="queryParams" :visible="showSearch">
          <AFormInput label="模糊搜索" prop="searchValue" v-model="queryParams.searchValue" @input="handleQuery"></AFormInput>
          <AFormTreeSelect label="所在部门" v-model="queryParams.deptId" prop="deptId" :data="deptOptions" @change="handleQuery"></AFormTreeSelect>
          <AFormSelect label="状态" v-model="queryParams.status" prop="status" :options="sys_enable_status" @change="handleQuery"></AFormSelect>
        </ASearchForm>

        <el-card shadow="hover">
          <!-- 岗位工具栏 -->
          <template #header>
            <el-row :gutter="10" class="mb-2">
              <el-col :span="1.5" v-permi="['system:post:add']">
                <el-button type="primary" plain icon="Plus" @click="handleAdd">
                  {{ t('新增') }}
                </el-button>
              </el-col>
              <el-col :span="1.5" v-permi="['system:post:update']">
                <el-button type="success" plain icon="Edit" :disabled="selectionItems.length !== 1" @click="handleUpdate()">
                  {{ t('修改') }}
                </el-button>
              </el-col>
              <el-col :span="1.5" v-permi="['system:post:delete']">
                <el-button type="danger" plain icon="Delete" :disabled="selectionItems.length === 0" @click="handleDelete()">
                  {{ t('删除') }}
                </el-button>
              </el-col>
              <el-col :span="1.5" v-permi="['system:post:export']">
                <el-button type="warning" plain icon="Download" @click="handleExport">
                  {{ t('导出') }}
                </el-button>
              </el-col>

              <TableToolbar v-model:showSearch="showSearch" @reset-query="resetQuery" @query-table="getList"></TableToolbar>
            </el-row>
          </template>

          <!-- 岗位表格数据 -->
          <el-table ref="postTableRef" v-loading="isLoading" :data="postList" :height="tableHeight" stripe @selection-change="handleSelectionChange">
            <el-table-column type="selection" width="50" align="center" />
            <el-table-column v-if="false" :label="t('postId', '岗位编号')" prop="postId" align="center" />
            <el-table-column :label="t('postName', '岗位名称')" prop="postName" align="center" />
            <el-table-column :label="t('postCode', '岗位编码')" prop="postCode" align="center" />
            <el-table-column :label="t('postCategory', '类别编码')" prop="postCategory" align="center" />
            <el-table-column :label="t('deptName', '所在部门')" prop="deptName" align="center" />
            <el-table-column :label="t('postSort', '排序')" prop="postSort" align="center" />
            <el-table-column :label="t('status', '状态')" prop="status" align="center">
              <template #default="{ row }">
                <AFormSwitch v-model="row.status" @change="handleStatusChange(row)" />
              </template>
            </el-table-column>
            <el-table-column :label="t('createTime', '创建时间')" prop="createTime" align="center" />
            <el-table-column :label="t('操作')" align="center" fixed="right">
              <template #default="{ row }">
                <el-tooltip :content="t('修改')" placement="top">
                  <el-button v-permi="['system:post:update']" link type="success" icon="Edit" @click="handleUpdate(row)"></el-button>
                </el-tooltip>
                <el-tooltip :content="t('删除')" placement="top">
                  <el-button v-permi="['system:post:delete']" link type="danger" icon="Delete" @click="handleDelete(row)"></el-button>
                </el-tooltip>
              </template>
            </el-table-column>
          </el-table>

          <Pagination v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />
        </el-card>

        <!-- 添加或修改岗位对话框 -->
        <AModal v-model="dialog.visible" :title="dialog.title" size="small" :loading="buttonLoading" @confirm="submitForm" @cancel="cancel">
          <el-form ref="postFormRef" :model="form" :rules="rules" label-width="auto">
            <el-row :gutter="10">
              <AFormInput label="岗位名称" v-model="form.postName" prop="postName" span="auto"></AFormInput>
              <AFormTreeSelect
                label="部门"
                v-model="form.deptId"
                prop="deptId"
                :data="deptOptions"
                :props="{ value: 'id', label: 'label', children: 'children' }"
                span="auto"
              ></AFormTreeSelect>
              <AFormInput label="岗位编码" v-model="form.postCode" prop="postCode" span="auto"></AFormInput>
              <AFormInput label="类别编码" v-model="form.postCategory" prop="postCategory" span="auto"></AFormInput>
              <AFormInput type="number" label="岗位顺序" v-model="form.postSort" prop="postSort" :min="0" span="auto"></AFormInput>
              <AFormRadio label="岗位状态" v-model="form.status" prop="status" :options="sys_enable_status" span="auto"></AFormRadio>
              <AFormInput type="textarea" label="备注" v-model="form.remark" prop="remark" span="auto"></AFormInput>
            </el-row>
          </el-form>
        </AModal>
      </template>
    </AResizablePanels>
  </div>
</template>

<script setup lang="ts" name="Post">
import { pagePosts, getPost, addPost, updatePost, deletePosts } from '@/api/system/core/post/postApi'
import { getDeptTreeOptions } from '@/api/system/core/dept/deptApi'
import type { SysPostQuery, SysPostBo, SysPostVo } from '@/api/system/core/post/postTypes'
import type { SysDeptTreeVo } from '@/api/system/core/dept/deptTypes'
import { isTrue, toggleStatus } from '@/utils/boolean'
import { toValidate } from '@/utils/to'
import { showMsgSuccess, showConfirm } from '@/utils/modal'

const { t } = useI18n()

/**字典数据 */
const { sys_enable_status } = useDict(DictTypes.sys_enable_status)

// 使用表格高度处理钩子
const { tableHeight, queryFormRef, showSearch } = useTableHeight()

// =========== 可拖拽面板相关 ===========
/**左侧面板宽度*/
const leftPanelWidth = ref(250)

// =========== 部门树相关 ===========

/** 部门树相关 */
const deptOptions = ref<SysDeptTreeVo[]>([])
const deptTreeRef = ref()

/** 部门树虚拟滚动高度（px）：跟随搜索栏与表格高度联动，供 el-tree-v2 虚拟滚动使用 */
const deptTreeHeight = computed(() => (queryFormRef.value?.$el?.offsetHeight || 0) + tableHeight.value + 82)

/** 节点单击事件 */
const handleNodeClick = (data: SysDeptTreeVo) => {
  queryParams.value.belongDeptId = data.id
  queryParams.value.deptId = undefined
  handleQuery()
}

// =========== 查询相关 ===========

/**查询参数对象*/
const queryParams = ref<SysPostQuery>({
  pageNum: 1,
  pageSize: 10,
  postCode: undefined,
  postName: undefined,
  postCategory: undefined,
  deptId: undefined,
  belongDeptId: undefined,
  status: undefined
})

/** 岗位搜索按钮操作 */
const handleQuery = () => {
  queryParams.value.pageNum = 1
  if (queryParams.value.deptId) {
    queryParams.value.belongDeptId = undefined
  }
  getList()
}

/** 岗位重置按钮操作 */
const resetQuery = () => {
  queryFormRef.value?.resetFields()
  queryParams.value.pageNum = 1
  queryParams.value.deptId = undefined
  deptTreeRef.value?.setCurrentKey(undefined)
  /** 清空左边部门树选中值 */
  queryParams.value.belongDeptId = undefined
  handleQuery()
}

// =========== 岗位表格数据相关 ===========
/**表格加载状态*/
const isLoading = ref(true)
/**数据列表*/
const postList = ref<SysPostVo[]>([])
/**总记录数*/
const total = ref(0)
/**表格实例*/
const postTableRef = ref()
/**选中的数据项*/
const selectionItems = ref<SysPostVo[]>([])

/** 表格多选事件处理 */
const handleSelectionChange = (selection: SysPostVo[]) => {
  selectionItems.value = selection
}

/** 查询岗位列表 */
const getList = async () => {
  isLoading.value = true
  const [err, data] = await pagePosts(queryParams.value)
  if (!err) {
    postList.value = data.records
    total.value = data.total
  }
  isLoading.value = false
}

/** 导出岗位数据 */
const handleExport = () => {
  useDownload().exportExcel('岗位信息', '/system/post/exportPosts', queryParams.value)
}

/** 删除岗位操作 */
const handleDelete = async (row?: SysPostVo) => {
  const idsToDelete = row ? [row.postId] : selectionItems.value.map((item) => item.postId)
  if (idsToDelete.length === 0) return
  const itemsToDelete = row ? `${row.postName}(${row.postCode})` : selectionItems.value.map((item) => `${item.postName}(${item.postCode})`).join(', ')
  const [confirmErr] = await showConfirm(`${t('是否确认删除')}${itemsToDelete}`)
  if (confirmErr) return

  const [deleteErr] = await deletePosts(idsToDelete)
  if (!deleteErr) {
    showMsgSuccess(t('message.deleteSuccess'))
    await getList()
  }
}

// =========== 岗位表单相关 ===========
/**初始表单数据*/
const initFormData: SysPostBo = {
  postId: undefined,
  deptId: undefined,
  postCode: undefined,
  postName: undefined,
  postCategory: undefined,
  postSort: 0,
  status: '1',
  remark: undefined
}

/**表单引用*/
const postFormRef = ref<ElFormInstance>()
/**表单提交按钮加载状态*/
const buttonLoading = ref(false)
/**对话框配置对象*/
const dialog = ref<DialogState>({
  visible: false,
  title: ''
})
/**表单数据对象*/
const form = ref<SysPostBo>({ ...initFormData })
/**表单校验规则*/
const rules = ref<ElFormRules>({
  postName: [{ required: true, message: t('postName cannot be empty', '岗位名称不能为空'), trigger: 'blur' }],
  postCode: [{ required: true, message: t('postCode cannot be empty', '岗位编码不能为空'), trigger: 'blur' }],
  deptId: [{ required: true, message: t('deptId cannot be empty', '部门不能为空'), trigger: 'blur' }],
  postSort: [{ required: true, message: t('postSort cannot be empty', '岗位顺序不能为空'), trigger: 'blur' }]
})

/** 岗位表单重置 */
const reset = () => {
  form.value = { ...initFormData }
  postFormRef.value?.resetFields()
}

/** 取消岗位编辑 */
const cancel = () => {
  reset()
  dialog.value.visible = false
}

/** 新增岗位操作 */
const handleAdd = () => {
  reset()
  dialog.value.visible = true
  dialog.value.title = `${t('新增')}${t('post', '岗位')}`
}

/** 修改岗位操作 */
const handleUpdate = async (row?: SysPostVo) => {
  reset()
  const itemToEdit = row || selectionItems.value[0]
  const [err, data] = await getPost(itemToEdit.postId)
  if (!err) {
    Object.assign(form.value, data)
    dialog.value.visible = true
    dialog.value.title = `${t('修改')}${t('post', '岗位')}`
  }
}

/** 提交岗位表单 */
const submitForm = async () => {
  const [validateErr] = await toValidate(postFormRef)
  if (validateErr) return

  buttonLoading.value = true
  let err: Error | null
  if (form.value.postId) {
    ;[err] = await updatePost(form.value)
  } else {
    ;[err] = await addPost(form.value)
  }
  if (!err) {
    showMsgSuccess(form.value.postId ? t('message.updateSuccess') : t('message.addSuccess'))
    dialog.value.visible = false
    await getList()
  }
  buttonLoading.value = false
}

/** 岗位启用禁用状态修改 */
const handleStatusChange = async (row: SysPostVo) => {
  const text = isTrue(row.status) ? t('启用') : t('停用')
  const [confirmErr] = await showConfirm(`${t('是否确认')}${text}${row.postName}?`)
  if (confirmErr) {
    row.status = toggleStatus(row.status)
    return
  }
  const [updateErr] = await updatePost(row)
  if (updateErr) {
    row.status = toggleStatus(row.status)
    return
  }
  await getList()
  showMsgSuccess(`${text}${t('成功')}`)
}

/** 查询部门下拉树结构 */
const getTreeSelect = async () => {
  const [err, data] = await getDeptTreeOptions()
  if (!err) {
    deptOptions.value = data
  }
}

// =========== 生命周期 ===========
/**初始化岗位数据列表*/
onMounted(() => {
  getTreeSelect()
  getList()
})
/**页面激活时刷新岗位列表*/
onActivated(() => {
  if (isLoading.value) return
  getList()
})
</script>
