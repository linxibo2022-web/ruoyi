<!-- 菜单管理 -->
<template>
  <div>
    <!--  搜索栏  -->
    <ASearchForm ref="queryFormRef" v-model="queryParams" :visible="showSearch">
      <AFormInput label="模糊搜索" v-model="queryParams.searchValue" prop="searchValue" @input="handleQuery"></AFormInput>
      <AFormSelect label="状态" v-model="queryParams.status" prop="status" :options="sys_enable_status" @change="handleQuery"></AFormSelect>
    </ASearchForm>

    <el-card shadow="hover">
      <!--   工具栏   -->
      <template #header>
        <el-row :gutter="10" class="mb-2">
          <el-col :span="1.5" v-permi="['system:menu:add']">
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

      <!--   表格数据：用 el-table-v2 虚拟滚动，支撑数百~上千条树形菜单"全部展开"也不卡（仅渲染可视区行）  -->
      <div ref="tableContainerRef" v-loading="isLoading" :style="{ height: `${tableHeight}px` }">
        <el-auto-resizer>
          <template #default="{ height, width }">
            <el-table-v2
              class="menu-table-v2"
              :columns="columns"
              :data="menuList"
              :width="width"
              :height="height"
              :row-height="50"
              row-key="menuId"
              expand-column-key="menuName"
              :expanded-row-keys="expandedRowKeys"
              :row-class="getRowClass"
              @expanded-rows-change="onExpandedRowsChange"
            >
              <template #cell="{ column, rowData }">
                <div v-if="column.key === 'menuName'" class="flex items-center overflow-hidden">
                  <Icon class="mr-1 flex-shrink-0" :code="rowData.icon" />
                  <span class="cursor-pointer truncate" :title="rowData.menuName" @click="copy(rowData.menuName)">{{ rowData.menuName }}</span>
                </div>
                <span v-else-if="column.key === 'orderNum'">{{ rowData.orderNum }}</span>
                <span v-else-if="column.key === 'perms'" class="cursor-pointer truncate" :title="rowData.perms" @click="copy(rowData.perms)">{{
                  rowData.perms
                }}</span>
                <span
                  v-else-if="column.key === 'component'"
                  class="cursor-pointer truncate"
                  :title="rowData.component"
                  @click="copy(rowData.component)"
                  >{{ rowData.component }}</span
                >
                <span v-else-if="column.key === 'i18nKey'" class="cursor-pointer truncate" :title="rowData.i18nKey" @click="copy(rowData.i18nKey)">{{
                  rowData.i18nKey
                }}</span>
                <AFormSwitch
                  v-else-if="column.key === 'visible'"
                  v-model="rowData.visible"
                  :show-form-item="false"
                  @change="handleVisibleChange(rowData)"
                />
                <AFormSwitch
                  v-else-if="column.key === 'status'"
                  v-model="rowData.status"
                  :show-form-item="false"
                  @change="handleStatusChange(rowData)"
                />
                <span v-else-if="column.key === 'createTime'">{{ rowData.createTime }}</span>
                <div v-else-if="column.key === 'operation'" class="menu-actions">
                  <el-tooltip :content="t('修改')" placement="top">
                    <el-button v-permi="['system:menu:update']" text bg type="success" :icon="Edit" @click="handleUpdate(rowData)" />
                  </el-tooltip>
                  <el-tooltip :content="t('新增')" placement="top">
                    <el-button v-permi="['system:menu:add']" text bg type="primary" :icon="Plus" @click="handleAdd(rowData)" />
                  </el-tooltip>
                  <el-tooltip :content="t('Copy', '复制')" placement="top">
                    <el-button v-permi="['system:menu:add']" text bg type="primary" :icon="CopyDocument" @click="handleCopy(rowData)" />
                  </el-tooltip>
                  <el-tooltip :content="t('删除')" placement="top">
                    <el-button v-permi="['system:menu:delete']" text bg type="danger" :icon="Delete" @click="handleDelete(rowData)" />
                  </el-tooltip>
                </div>
              </template>
            </el-table-v2>
          </template>
        </el-auto-resizer>
      </div>
    </el-card>

    <!-- 添加或修改菜单对话框 -->
    <AModal v-model="dialog.visible" :title="dialog.title" :loading="buttonLoading" @confirm="submitForm" @cancel="closeDialog">
      <el-form ref="menuFormRef" :model="form" :rules="rules" label-width="auto">
        <el-row :gutter="10">
          <AFormTreeSelect
            :label="t('Parent Menu', '上级菜单')"
            v-model="form.parentId"
            :data="menuOptions"
            :props="{ value: 'menuId', label: 'menuName', children: 'children' }"
            :placeholder="t('Select Parent Menu', '选择上级菜单')"
            check-strictly
            :span="24"
          ></AFormTreeSelect>

          <AFormRadio :label="t('Menu Type', '菜单类型')" v-model="form.menuType" prop="menuType" :options="menuTypeOptions" :span="24"></AFormRadio>

          <el-col v-if="form.menuType !== 'F'" :span="24">
            <el-form-item :label="t('Menu Icon', '菜单图标')" prop="icon">
              <IconSelect v-model="form.icon" empty-value="#" />
            </el-form-item>
          </el-col>

          <AFormInput :label="t('Menu Name', '菜单名称')" v-model="form.menuName" prop="menuName" span="auto"></AFormInput>

          <AFormInput :label="t('Order', '显示排序')" v-model="form.orderNum" prop="orderNum" type="number" :min="0" span="auto"></AFormInput>

          <AFormRadio
            v-if="form.menuType !== 'F'"
            :label="t('External Link', '是否外链')"
            v-model="form.isExternalLink"
            :options="yesNoOptions"
            :tooltip="t('External link tip', '选择是外链则路由地址需要以 http(s):// 开头')"
            span="auto"
          ></AFormRadio>

          <AFormInput
            v-if="form.menuType !== 'F'"
            :label="t('Route Path', '路由地址')"
            v-model="form.path"
            prop="path"
            :placeholder="t('Enter route path', '请输入路由地址')"
            :tooltip="t('Route path tip', '访问的路由地址，如：user，如外网地址需内链访问则以 http(s):// 开头')"
            span="auto"
          ></AFormInput>

          <AFormInput
            v-if="form.menuType === 'C'"
            :label="t('Component Path', '组件路径')"
            v-model="form.component"
            prop="component"
            :placeholder="t('Enter component path', '请输入组件路径')"
            :tooltip="t('Component path tip', '访问的组件路径，如：system/user/index，默认在 views 目录下')"
            span="auto"
          ></AFormInput>

          <AFormInput
            v-if="form.menuType !== 'M'"
            :label="t('Permission Key', '权限字符')"
            v-model="form.perms"
            :placeholder="t('Enter permission key', '请输入权限标识')"
            :maxlength="100"
            :tooltip="t('Permission key tip', '控制器中定义的权限字符，如：@SaCheckPermission(system:user:view)')"
            span="auto"
          ></AFormInput>

          <AFormInput
            v-if="form.menuType === 'C'"
            :label="t('Route Params', '路由参数')"
            v-model="form.queryParam"
            :placeholder="t('Enter route params', '请输入路由参数')"
            :maxlength="255"
            :tooltip="t('Route params tip', '访问路由的默认传递参数，如：{id: 1, name: ry}')"
            span="auto"
          ></AFormInput>

          <AFormRadio
            v-if="form.menuType === 'C'"
            :label="t('Cache', '是否缓存')"
            v-model="form.isCache"
            :options="cacheOptions"
            :tooltip="t('Cache tip', '选择是则会被 keep-alive 缓存，需要匹配组件的 name 和地址保持一致')"
            span="auto"
          ></AFormRadio>

          <AFormRadio
            v-if="form.menuType !== 'F'"
            :label="t('Visibility', '显示状态')"
            v-model="form.visible"
            :options="sys_display_setting"
            :tooltip="t('Visibility tip', '选择隐藏则路由将不会出现在侧边栏，但仍然可以访问')"
            span="auto"
          ></AFormRadio>

          <AFormRadio
            :label="t('Status', '启用状态')"
            v-model="form.status"
            :options="sys_enable_status"
            :tooltip="t('Status tip', '选择停用则路由将不会出现在侧边栏，也不能被访问')"
            span="auto"
          ></AFormRadio>
        </el-row>
      </el-form>
    </AModal>
  </div>
</template>

<script setup lang="ts" name="Menu">
import type { Column } from 'element-plus'
import { CopyDocument, Delete, Edit, Plus } from '@element-plus/icons-vue'
import { addMenu, deleteMenu, getMenu, listMenus, updateMenu } from '@/api/system/core/menu/menuApi'
import { type SysMenuQuery, type SysMenuBo, type SysMenuVo, MenuType } from '@/api/system/core/menu/menuTypes'
import { buildTree } from '@/utils/tree'
import { copy } from '@/utils/function'
import { isTrue, toggleStatus } from '@/utils/boolean'
import { toValidate } from '@/utils/to'
import { showMsgSuccess, showConfirm } from '@/utils/modal'

const { t } = useI18n()

/**字典数据 */
const { sys_display_setting, sys_enable_status } = useDict(DictTypes.sys_display_setting, DictTypes.sys_enable_status)

// =========== 类型定义 ===========
interface MenuOptionsType {
  menuId: number
  menuName: string
  children: MenuOptionsType[] | undefined
}

// 使用表格高度处理钩子（优化：不监听侧边栏状态，避免不必要的重排）
const { tableHeight, queryFormRef, showSearch } = useTableHeight(-32)

// =========== 宽度锁定（侧边栏动画期间锁住容器宽度，避免 el-auto-resizer 逐帧触发 el-table-v2 重渲染）===========
const tableContainerRef = ref<HTMLElement>()
const layout = useLayout()

watch(
  () => layout.sidebar.value.opened,
  () => {
    if (tableContainerRef.value) {
      const currentWidth = tableContainerRef.value.offsetWidth
      tableContainerRef.value.style.width = `${currentWidth}px`
      setTimeout(() => {
        if (tableContainerRef.value) {
          tableContainerRef.value.style.width = ''
        }
      }, 300)
    }
  }
)

// =========== 查询相关 ===========

/**查询参数对象*/
const queryParams = ref<SysMenuQuery>({
  menuName: undefined,
  status: undefined
})

/** 菜单搜索按钮操作 */
const handleQuery = () => {
  getList()
}

/** 菜单重置按钮操作 */
const resetQuery = () => {
  queryFormRef.value?.resetFields()
  handleQuery()
}

// =========== 菜单表格数据相关 ===========
/**表格加载状态*/
const isLoading = ref(true)
/**数据列表（树形）*/
const menuList = ref<SysMenuVo[]>([])
/**是否展开所有*/
const isAllExpanded = ref(false)
/**当前展开的行 key 列表（el-table-v2 通过该列表决定哪些子节点可见）*/
const expandedRowKeys = ref<Array<string | number>>([])
/**最近一次成功加载的时间戳，用于 onActivated 时判断是否需要刷新*/
let lastLoadTime = 0

/** 收集树中所有节点的 menuId（叶子节点的 key 放进去无副作用，仅用于"展开全部"）*/
const collectAllMenuIds = (list: SysMenuVo[]): Array<string | number> => {
  const ids: Array<string | number> = []
  const walk = (nodes: SysMenuVo[]) => {
    for (const node of nodes) {
      ids.push(node.menuId)
      const children = (node as SysMenuVo & { children?: SysMenuVo[] }).children
      if (children?.length) walk(children)
    }
  }
  walk(list)
  return ids
}

/** 查询菜单列表 */
const getList = async () => {
  isLoading.value = true
  const [err, data] = await listMenus(queryParams.value)
  if (!err) {
    const tree = buildTree<SysMenuVo>(data, { id: 'menuId' })
    menuList.value = tree || []
    // 数据刷新后，按当前"展开全部"标记同步展开列表
    expandedRowKeys.value = isAllExpanded.value ? collectAllMenuIds(menuList.value) : []
    lastLoadTime = Date.now()
  }
  isLoading.value = false
}

/** 切换全部展开/折叠：直接替换 expandedRowKeys，el-table-v2 仅渲染可视区行，几百上千条也不卡 */
const toggleAllExpansion = () => {
  isAllExpanded.value = !isAllExpanded.value
  expandedRowKeys.value = isAllExpanded.value ? collectAllMenuIds(menuList.value) : []
}

/** el-table-v2 行展开状态变化（用户手动点开/收起箭头时） */
const onExpandedRowsChange = (keys: Array<string | number>) => {
  expandedRowKeys.value = keys
  // 仅用于按钮文案：展开数量达到全部可展开节点数时视为"已全部展开"
  isAllExpanded.value = keys.length > 0 && keys.length >= collectAllMenuIds(menuList.value).length
}

/** el-table-v2 没有内置斑马纹，按奇偶行号下发自定义 class 由样式补齐 */
const getRowClass = ({ rowIndex }: { rowIndex: number }) => (rowIndex % 2 === 1 ? 'is-stripe' : '')

/** 删除菜单操作 */
const handleDelete = async (row: SysMenuVo) => {
  const [confirmErr] = await showConfirm(`${t('是否确认删除')}${row.menuName}?`)
  if (confirmErr) return

  const [deleteErr] = await deleteMenu(row.menuId)
  if (!deleteErr) {
    showMsgSuccess(t('message.deleteSuccess'))
    await getList()
  }
}

// =========== 菜单表单相关 ===========
/**上级菜单选项*/
const menuOptions = ref<MenuOptionsType[]>([])
/**表单引用*/
const menuFormRef = ref<ElFormInstance>()
/**表单提交按钮加载状态*/
const buttonLoading = ref(false)
/**对话框配置对象*/
const dialog = reactive<DialogState>({
  visible: false,
  title: ''
})

/**初始表单数据*/
const initFormData: SysMenuBo = {
  path: '',
  menuId: undefined,
  parentId: 0,
  menuName: '',
  icon: '',
  menuType: MenuType.M,
  orderNum: 1,
  isExternalLink: '0',
  isCache: '1',
  visible: '1',
  status: '1'
}

/**表单数据对象*/
const form = ref<SysMenuBo>({ ...initFormData })

/**表单校验规则*/
const rules = computed<ElFormRules>(() => ({
  menuName: [{ required: true, message: t('Menu name required', '菜单名称不能为空'), trigger: 'blur' }],
  orderNum: [{ required: true, message: t('Order required', '菜单顺序不能为空'), trigger: 'blur' }],
  path: [{ required: true, message: t('Route path required', '路由地址不能为空'), trigger: 'blur' }]
}))

// =========== 选项数据 ===========
/**菜单类型选项*/
const menuTypeOptions = computed(() => [
  { label: t('Directory', '目录'), value: 'M' },
  { label: t('Menu', '菜单'), value: 'C' },
  { label: t('Button', '按钮'), value: 'F' }
])

/**是否选项*/
const yesNoOptions = computed(() => [
  { label: t('No', '否'), value: '0' },
  { label: t('Yes', '是'), value: '1' }
])

/**缓存选项*/
const cacheOptions = computed(() => [
  { label: t('No Cache', '不缓存'), value: '0' },
  { label: t('Cache', '缓存'), value: '1' }
])

/** 查询菜单下拉树结构 */
const getTreeselect = async () => {
  const [err, data] = await listMenus()
  if (!err && data) {
    menuOptions.value = []
    const menu: MenuOptionsType = { menuId: 0, menuName: t('Root Category', '主类目'), children: [] }
    menu.children = buildTree<MenuOptionsType>(data, { id: 'menuId' })
    menuOptions.value.push(menu)
  }
}

/** 菜单表单重置 */
const reset = () => {
  form.value = { ...initFormData }
  menuFormRef.value?.resetFields()
}

/** 取消菜单编辑 */
const cancel = () => {
  dialog.visible = false
  reset()
}

/** 关闭对话框 */
const closeDialog = () => {
  reset()
}

/** 新增菜单操作 */
const handleAdd = async (row?: SysMenuVo) => {
  reset()
  await getTreeselect()
  if (row && row.menuId) {
    form.value.parentId = row.menuId
  } else {
    form.value.parentId = 0
  }
  dialog.visible = true
  dialog.title = `${t('新增')}${t('menu', '菜单')}`
}

/** 修改菜单操作 */
const handleUpdate = async (row: SysMenuVo) => {
  reset()
  await getTreeselect()

  if (row.menuId) {
    const [err, data] = await getMenu(row.menuId)
    if (!err) {
      Object.assign(form.value, data)
      dialog.visible = true
      dialog.title = `${t('修改')}${t('menu', '菜单')}`
    }
  }
}

/** 复制菜单操作 */
const handleCopy = async (row: SysMenuVo) => {
  reset()
  await getTreeselect()

  if (row.menuId) {
    const [err, data] = await getMenu(row.menuId)
    if (!err) {
      // 复制所有属性，但清空menuId
      Object.assign(form.value, data)
      form.value.menuId = undefined
      // 修改菜单名称，添加"复制"后缀
      form.value.menuName = form.value.menuName + t('_Copy', '_复制')

      dialog.visible = true
      dialog.title = `${t('Copy', '复制')}${t('menu', '菜单')}`
    }
  }
}

/** 提交菜单表单 */
const submitForm = async () => {
  const [validateErr] = await toValidate(menuFormRef)
  if (validateErr) return

  buttonLoading.value = true
  let err: Error | null
  if (form.value.menuId) {
    ;[err] = await updateMenu(form.value)
  } else {
    ;[err] = await addMenu(form.value)
  }

  if (!err) {
    showMsgSuccess(form.value.menuId ? t('message.updateSuccess') : t('message.addSuccess'))
    dialog.visible = false
    await getList()
  }
  buttonLoading.value = false
}

/** 显示设置修改 */
const handleVisibleChange = async (row: SysMenuBo) => {
  const text = row.visible === '1' ? t('message.visible', '显示') : t('message.invisible', '隐藏')
  const [confirmErr] = await showConfirm(`${t('是否确认')}${text}${row.menuName}?`)
  if (confirmErr) {
    row.visible = row.visible === '1' ? '0' : '1'
    return
  }
  const [updateErr] = await updateMenu(row)
  if (updateErr) {
    row.visible = row.visible === '1' ? '0' : '1'
    return
  }
  // row 即 menuList 中的响应式对象，开关已通过 v-model 更新，无需 getList() 整表重建（也避免丢失展开状态）
  showMsgSuccess(`${text}${t('成功')}`)
}

/** 菜单启用禁用状态修改 */
const handleStatusChange = async (row: SysMenuBo) => {
  const text = isTrue(row.status) ? t('启用') : t('停用')
  const [confirmErr] = await showConfirm(`${t('是否确认')}${text}${row.menuName}?`)
  if (confirmErr) {
    row.status = toggleStatus(row.status)
    return
  }
  const [updateErr] = await updateMenu(row)
  if (updateErr) {
    row.status = toggleStatus(row.status)
    return
  }
  // 同上：本地状态已更新，无需重新拉取并重建整表
  showMsgSuccess(`${text}${t('成功')}`)
}

// =========== el-table-v2 列定义 ===========
const columns = computed<Column<any>[]>(() => [
  { key: 'menuName', dataKey: 'menuName', title: t('menuName', '菜单名称'), width: 240, minWidth: 200, flexGrow: 1, align: 'left' },
  { key: 'orderNum', dataKey: 'orderNum', title: t('orderNum', '显示排序'), width: 100, align: 'center' },
  { key: 'perms', dataKey: 'perms', title: t('perms', '权限标识'), width: 170, align: 'center' },
  { key: 'component', dataKey: 'component', title: t('component', '组件路径'), width: 200, align: 'center' },
  { key: 'i18nKey', dataKey: 'i18nKey', title: t('i18nKey', '国际化键'), width: 180, align: 'center' },
  { key: 'visible', dataKey: 'visible', title: t('visible', '显示设置'), width: 100, align: 'center' },
  { key: 'status', dataKey: 'status', title: t('status', '状态'), width: 80, align: 'center' },
  { key: 'createTime', dataKey: 'createTime', title: t('createTime', '创建时间'), width: 170, align: 'center' },
  { key: 'operation', title: t('操作'), width: 170, align: 'center', fixed: 'right' }
])

/**初始化菜单数据列表*/
onMounted(() => {
  getList()
})

/**页面激活时刷新菜单列表（30 秒内不重复拉取，避免 keep-alive 切回时频繁全量查询）*/
onActivated(() => {
  if (isLoading.value) return
  if (Date.now() - lastLoadTime < 30_000) return
  getList()
})
</script>
<style lang="scss" scoped>
/* el-table-v2 表头文字防止换行（如多语言翻译变长时仍保持单行，溢出由列宽兜底） */
:deep(.el-table-v2__header-cell-text) {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* el-table-v2 无内置斑马纹，自行补齐：奇数行底色 + hover 高亮 */
.menu-table-v2 {
  :deep(.el-table-v2__row.is-stripe) {
    background-color: var(--el-fill-color-lighter);
  }
  :deep(.el-table-v2__row:hover) {
    background-color: var(--el-table-row-hover-bg-color);
  }
}

/* 操作列容器:具体样式由全局 _element-plus.scss 的 .menu-actions 规则提供
   (复刻 .el-table__fixed-right 下原有的彩色浅底按钮视觉) */
.menu-actions {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
}
</style>
