<!--
表格卡片组件 ATableCard

使用示例：

1. 基本用法
<el-row :gutter="20">
  <el-col :span="24">
    <ATableCard
      title="最新订单"
      :columns="[
        { prop: 'id', label: '订单号', width: 120 },
        { prop: 'name', label: '商品名称' },
        { prop: 'price', label: '价格' },
        { prop: 'status', label: '状态' }
      ]"
      :data="[
        { id: 'ORD001', name: '商品A', price: '¥199', status: '已完成' },
        { id: 'ORD002', name: '商品B', price: '¥299', status: '进行中' },
        { id: 'ORD003', name: '商品C', price: '¥399', status: '已完成' }
      ]"
    />
  </el-col>
</el-row>

2. 带分页
<el-row :gutter="20">
  <el-col :span="24">
    <ATableCard
      title="用户列表"
      :columns="[
        { prop: 'name', label: '姓名' },
        { prop: 'email', label: '邮箱' },
        { prop: 'role', label: '角色' }
      ]"
      :data="[
        { name: '张三', email: 'zhangsan@example.com', role: '管理员' },
        { name: '李四', email: 'lisi@example.com', role: '用户' }
      ]"
      :pagination="{ total: 100, pageSize: 10, currentPage: 1 }"
    />
  </el-col>
</el-row>

3. 带操作列
<el-row :gutter="20">
  <el-col :span="24">
    <ATableCard
      title="商品管理"
      :columns="[
        { prop: 'name', label: '商品名称' },
        { prop: 'category', label: '分类' },
        { prop: 'price', label: '价格' }
      ]"
      :data="[
        { name: 'iPhone 15', category: '手机', price: '¥5999' },
        { name: 'MacBook Pro', category: '电脑', price: '¥12999' }
      ]"
      :show-actions="true"
    />
  </el-col>
</el-row>

4. 两列布局
<el-row :gutter="20">
  <el-col :xs="24" :md="12">
    <ATableCard
      title="待处理订单"
      :columns="[
        { prop: 'orderNo', label: '订单号' },
        { prop: 'amount', label: '金额' }
      ]"
      :data="[
        { orderNo: 'ORD001', amount: '¥199' },
        { orderNo: 'ORD002', amount: '¥299' }
      ]"
    />
  </el-col>
  <el-col :xs="24" :md="12">
    <ATableCard
      title="已完成订单"
      :columns="[
        { prop: 'orderNo', label: '订单号' },
        { prop: 'amount', label: '金额' }
      ]"
      :data="[
        { orderNo: 'ORD003', amount: '¥399' },
        { orderNo: 'ORD004', amount: '¥499' }
      ]"
    />
  </el-col>
</el-row>
-->
<template>
  <div class="table-card">
    <!-- 头部 -->
    <div v-if="title || $slots.header || showHeaderAction" class="card-header">
      <slot name="header">
        <div class="header-content">
          <div class="header-left">
            <h3 class="card-title">{{ title }}</h3>
            <p v-if="subtitle" class="card-subtitle">{{ subtitle }}</p>
          </div>
          <div v-if="showHeaderAction" class="header-right">
            <slot name="header-action">
              <el-button type="primary" size="small" @click="handleAdd">
                <Icon code="add" :size="14" />
                <span style="margin-left: 4px">{{ t('card.table.add') }}</span>
              </el-button>
            </slot>
          </div>
        </div>
      </slot>
    </div>

    <!-- 表格 -->
    <div class="card-body">
      <el-table
        v-loading="loading"
        :data="data"
        :height="tableHeight"
        :max-height="maxHeight"
        :stripe="stripe"
        :border="border"
        style="width: 100%"
        @selection-change="handleSelectionChange"
        @row-click="handleRowClick"
      >
        <!-- 选择列 -->
        <el-table-column v-if="showSelection" type="selection" width="55" />

        <!-- 数据列 -->
        <el-table-column
          v-for="column in columns"
          :key="column.prop"
          :prop="column.prop"
          :label="column.label"
          :width="column.width"
          :min-width="column.minWidth"
          :align="column.align || 'left'"
          :fixed="column.fixed"
        >
          <template #default="scope">
            <slot :name="`column-${column.prop}`" :row="scope.row" :column="column">
              {{ scope.row[column.prop] }}
            </slot>
          </template>
        </el-table-column>

        <!-- 操作列 -->
        <el-table-column v-if="showActions || $slots.actions" :label="t('card.table.actions')" :width="actionWidth" :fixed="actionFixed" align="center">
          <template #default="scope">
            <slot name="actions" :row="scope.row">
              <el-button link type="primary" size="small" @click.stop="handleEdit(scope.row)"> {{ t('card.table.edit') }} </el-button>
              <el-button link type="danger" size="small" @click.stop="handleDelete(scope.row)"> {{ t('card.table.delete') }} </el-button>
            </slot>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div v-if="pagination" class="card-pagination">
        <el-pagination
          :current-page="pagination.currentPage"
          :page-size="pagination.pageSize"
          :total="pagination.total"
          :page-sizes="pagination.pageSizes || [10, 20, 50, 100]"
          :layout="paginationLayout"
          background
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts" name="ATableCard">
const { t } = useI18n()

/**表格列配置接口*/
interface TableColumn {
  /**属性名*/
  prop: string
  /**列标题*/
  label: string
  /**宽度*/
  width?: number
  /**最小宽度*/
  minWidth?: number
  /**对齐方式*/
  align?: 'left' | 'center' | 'right'
  /**固定列*/
  fixed?: 'left' | 'right' | boolean
}

/**分页配置接口*/
interface PaginationConfig {
  /**总条数*/
  total: number
  /**每页条数*/
  pageSize: number
  /**当前页*/
  currentPage: number
  /**可选的每页条数*/
  pageSizes?: number[]
}

/**表格卡片属性接口*/
interface ATableCardProps {
  /**标题*/
  title?: string
  /**副标题*/
  subtitle?: string
  /**列配置*/
  columns: TableColumn[]
  /**数据*/
  data: any[]
  /**是否加载中*/
  loading?: boolean
  /**表格高度*/
  tableHeight?: string | number
  /**表格最大高度*/
  maxHeight?: string | number
  /**是否斑马纹*/
  stripe?: boolean
  /**是否边框*/
  border?: boolean
  /**是否显示选择列*/
  showSelection?: boolean
  /**是否显示操作列*/
  showActions?: boolean
  /**操作列宽度*/
  actionWidth?: number
  /**操作列固定位置*/
  actionFixed?: 'left' | 'right' | boolean
  /**是否显示头部操作*/
  showHeaderAction?: boolean
  /**分页配置*/
  pagination?: PaginationConfig
  /**分页布局*/
  paginationLayout?: string
}

/**组件属性定义*/
const props = withDefaults(defineProps<ATableCardProps>(), {
  title: '',
  subtitle: '',
  loading: false,
  stripe: true,
  border: false,
  showSelection: false,
  showActions: false,
  actionWidth: 150,
  actionFixed: 'right',
  showHeaderAction: false,
  paginationLayout: 'total, sizes, prev, pager, next, jumper'
})

/**组件事件定义*/
const emit = defineEmits<{
  /**新增*/
  add: []
  /**编辑*/
  edit: [row: any]
  /**删除*/
  delete: [row: any]
  /**行点击*/
  rowClick: [row: any]
  /**选择改变*/
  selectionChange: [selection: any[]]
  /**页码改变*/
  pageChange: [page: number]
  /**每页条数改变*/
  sizeChange: [size: number]
}>()

/**处理新增*/
const handleAdd = () => {
  emit('add')
}

/**处理编辑*/
const handleEdit = (row: any) => {
  emit('edit', row)
}

/**处理删除*/
const handleDelete = (row: any) => {
  emit('delete', row)
}

/**处理行点击*/
const handleRowClick = (row: any) => {
  emit('rowClick', row)
}

/**处理选择改变*/
const handleSelectionChange = (selection: any[]) => {
  emit('selectionChange', selection)
}

/**处理页码改变*/
const handleCurrentChange = (page: number) => {
  emit('pageChange', page)
}

/**处理每页条数改变*/
const handleSizeChange = (size: number) => {
  emit('sizeChange', size)
}

/**暴露方法*/
defineExpose({
  /**数据条数*/
  dataCount: computed(() => props.data?.length || 0)
})
</script>

<style lang="scss" scoped>
.table-card {
  background-color: var(--bg-level-1);
  border-radius: var(--radius-md);
  border: 1px solid var(--el-border-color);

  .card-header {
    padding: 16px;
    border-bottom: 1px solid var(--el-border-color-lighter);

    .header-content {
      display: flex;
      align-items: center;
      justify-content: space-between;
    }

    .header-left {
      flex: 1;
    }

    .card-title {
      margin: 0;
      font-size: 18px;
      font-weight: 500;
      color: var(--el-text-color-primary);
    }

    .card-subtitle {
      margin: 4px 0 0;
      font-size: 14px;
      color: var(--el-text-color-regular);
    }

    .header-right {
      margin-left: 16px;
    }
  }

  .card-body {
    padding: 16px;
    :deep(.el-table) {
      --el-table-border-color: var(--el-border-color-lighter);
    }
  }

  .card-pagination {
    display: flex;
    justify-content: flex-end;
    margin-top: 16px;
  }
}
</style>
