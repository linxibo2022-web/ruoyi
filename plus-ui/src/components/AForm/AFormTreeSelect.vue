<!--用法示例
//一般用于搜索栏
<AFormTreeSelect label="父级" v-model="queryParams.parentId" :data="treeOptions" prop="parentId" @change="handleQuery"></AFormTreeSelect>
//一般用于表单
<AFormTreeSelect label="父级" v-model="queryParams.parentId" :data="treeOptions" prop="parentId"
:props="{value: 'deptId', label: 'deptName', children: 'children'}" :span="12"></AFormTreeSelect>
//带提示信息的树形选择器
<AFormTreeSelect label="父级" v-model="queryParams.parentId" :data="treeOptions" prop="parentId"
tooltip="请选择上级部门" :span="12"></AFormTreeSelect>
//不含el-form-item容器的简单树形选择器
<AFormTreeSelect label="父级" v-model="queryParams.parentId" :data="treeOptions" :show-form-item="false"></AFormTreeSelect>
//多选模式
<AFormTreeSelect label="权限" v-model="form.permissionIds" :data="permissionTree" multiple
show-checkbox :span="12"></AFormTreeSelect>
-->
<template>
  <!--span有值且showFormItem为true 显示ElCol ElFormItem-->
  <el-col :span="computedSpan" v-if="shouldUseCol && showFormItem">
    <el-form-item :label-width="labelWidth" :prop="prop">
      <template #label>
        <span>{{ computedLabel }}</span>
        <el-tooltip v-if="tooltip" :content="tooltip" placement="top" class="ml-1">
          <el-icon>
            <QuestionFilled />
          </el-icon>
        </el-tooltip>
      </template>

      <el-tree-select
        v-model="treeSelectValue"
        :data="data"
        :props="nodeProps"
        :value-key="valueKey"
        :node-key="nodeKey"
        :placeholder="placeholder || `${t('placeholder.select')}${computedLabel}`"
        :size="size"
        :clearable="clearable"
        :disabled="disabled"
        :check-strictly="checkStrictly"
        :filterable="filterable"
        :multiple="multiple"
        :show-checkbox="showCheckbox"
        :accordion="accordion"
        :indent="indent"
        :icon="icon"
        :expand-on-click-node="expandOnClickNode"
        :check-on-click-node="checkOnClickNode"
        :auto-expand-parent="autoExpandParent"
        :default-checked-keys="defaultCheckedKeys"
        :default-expanded-keys="defaultExpandedKeys"
        :current-node-key="currentNodeKey"
        :render-after-expand="renderAfterExpand"
        :load="load"
        :render-content="renderContent"
        :highlight-current="highlightCurrent"
        :default-expand-all="defaultExpandAll"
        :check-descendants="checkDescendants"
        :only-check-children="onlyCheckChildren"
        :empty-text="computedEmptyText"
        :style="{ width: '100%' }"
        @change="handleChange"
        @visible-change="handleVisibleChange"
        @clear="handleClear"
        @remove-tag="handleRemoveTag"
        @focus="handleFocus"
        @blur="handleBlur"
      >
        <!-- 自定义节点内容插槽 -->
        <template v-if="$slots.default" #default="scope">
          <slot :node="scope.node" :data="scope.data"></slot>
        </template>
      </el-tree-select>
    </el-form-item>
  </el-col>

  <!--span无值且showFormItem为true 显示ElFormItem-->
  <el-form-item :label-width="labelWidth" :prop="prop" v-else-if="!shouldUseCol && showFormItem">
    <template #label>
      <span>{{ computedLabel }}</span>
      <el-tooltip v-if="tooltip" :content="tooltip" placement="top" class="ml-1">
        <el-icon>
          <QuestionFilled />
        </el-icon>
      </el-tooltip>
    </template>

    <el-tree-select
      v-model="treeSelectValue"
      :data="data"
      :props="nodeProps"
      :value-key="valueKey"
      :node-key="nodeKey"
      :placeholder="placeholder || `${t('placeholder.select')}${computedLabel}`"
      :size="size"
      :clearable="clearable"
      :disabled="disabled"
      :check-strictly="checkStrictly"
      :filterable="filterable"
      :multiple="multiple"
      :show-checkbox="showCheckbox"
      :accordion="accordion"
      :indent="indent"
      :icon="icon"
      :expand-on-click-node="expandOnClickNode"
      :check-on-click-node="checkOnClickNode"
      :auto-expand-parent="autoExpandParent"
      :default-checked-keys="defaultCheckedKeys"
      :default-expanded-keys="defaultExpandedKeys"
      :current-node-key="currentNodeKey"
      :render-after-expand="renderAfterExpand"
      :load="load"
      :render-content="renderContent"
      :highlight-current="highlightCurrent"
      :default-expand-all="defaultExpandAll"
      :check-descendants="checkDescendants"
      :only-check-children="onlyCheckChildren"
      :empty-text="computedEmptyText"
      :style="{ width: formatUnit(width || 240) }"
      @change="handleChange"
      @visible-change="handleVisibleChange"
      @clear="handleClear"
      @remove-tag="handleRemoveTag"
      @focus="handleFocus"
      @blur="handleBlur"
    >
      <!-- 自定义节点内容插槽 -->
      <template v-if="$slots.default" #default="scope">
        <slot :node="scope.node" :data="scope.data"></slot>
      </template>
    </el-tree-select>
  </el-form-item>

  <!--showFormItem为false 只显示树形选择器-->
  <template v-else>
    <el-tree-select
      v-model="treeSelectValue"
      :data="data"
      :props="nodeProps"
      :value-key="valueKey"
      :node-key="nodeKey"
      :placeholder="placeholder || `${t('placeholder.select')}${computedLabel}`"
      :size="size"
      :clearable="clearable"
      :disabled="disabled"
      :check-strictly="checkStrictly"
      :filterable="filterable"
      :multiple="multiple"
      :show-checkbox="showCheckbox"
      :accordion="accordion"
      :indent="indent"
      :icon="icon"
      :expand-on-click-node="expandOnClickNode"
      :check-on-click-node="checkOnClickNode"
      :auto-expand-parent="autoExpandParent"
      :default-checked-keys="defaultCheckedKeys"
      :default-expanded-keys="defaultExpandedKeys"
      :current-node-key="currentNodeKey"
      :render-after-expand="renderAfterExpand"
      :load="load"
      :render-content="renderContent"
      :highlight-current="highlightCurrent"
      :default-expand-all="defaultExpandAll"
      :check-descendants="checkDescendants"
      :only-check-children="onlyCheckChildren"
      :empty-text="computedEmptyText"
      :style="{ width: formatUnit(width || 240) }"
      @change="handleChange"
      @visible-change="handleVisibleChange"
      @clear="handleClear"
      @remove-tag="handleRemoveTag"
      @focus="handleFocus"
      @blur="handleBlur"
    >
      <!-- 自定义节点内容插槽 -->
      <template v-if="$slots.default" #default="scope">
        <slot :node="scope.node" :data="scope.data"></slot>
      </template>
    </el-tree-select>
  </template>
</template>

<script setup lang="ts" name="AFormTreeSelect">
import { formatUnit } from '@/utils/format'

const { t, te, isChinese } = useI18n()

/**
 * 树形选择器节点属性接口
 */
interface TreeNodeProps {
  /** 节点值字段名 */
  value?: string
  /** 节点标签字段名 */
  label?: string
  /** 子节点字段名 */
  children?: string
  /** 节点禁用字段名 */
  disabled?: string
  /** 叶子节点字段名 */
  isLeaf?: string

  /** 其他自定义属性 */
  [key: string]: any
}

/**
 * 树形选择器组件属性接口定义
 */
interface AFormTreeSelectProps {
  /** 绑定值 */
  modelValue?: string | number | any[] | null

  /** 树形数据源 */
  data?: any[]

  /** 节点属性配置 */
  props?: TreeNodeProps

  /** 唯一标识 */
  valueKey?: string

  /** 节点唯一标识 */
  nodeKey?: string

  /** 表单标签文本 */
  label?: string

  /** 表单标签宽度，可以是数字或字符串 */
  labelWidth?: string | number

  /** 组件宽度，可以是数字或字符串 */
  width?: string | number

  /** 占位符文本 */
  placeholder?: string

  /** 表单域model数据字段名 */
  prop?: string

  /** 是否显示为表单项，false则不使用ElFormItem包装 */
  showFormItem?: boolean

  /** 组件尺寸 */
  size?: '' | 'default' | 'small' | 'large'

  /** 栅格占据的列数，支持数字、数字字符串、响应式对象或预设字符串
   * - 数字：固定span值，如 12
   * - 数字字符串：如 "12"，会自动转换为数字
   * - 响应式对象：{ xs: 24, sm: 24, md: 12, lg: 8, xl: 6 }
   * - 预设字符串：'auto' - 自动响应式布局
   * @default undefined
   */
  span?: SpanType

  /** 是否显示清除按钮 */
  clearable?: boolean

  /** 是否禁用 */
  disabled?: boolean

  /** 是否严格模式 */
  checkStrictly?: boolean

  /** 是否可过滤 */
  filterable?: boolean

  /** 提示信息 */
  tooltip?: string

  /** 是否多选 */
  multiple?: boolean

  /** 是否显示复选框 */
  showCheckbox?: boolean

  /** 是否每次只打开一个同级树节点展开 */
  accordion?: boolean

  /** 相邻级节点间的水平缩进，单位为像素 */
  indent?: number

  /** 自定义树节点的图标 */
  icon?: string

  /** 是否在点击节点的时候展开或者收缩节点 */
  expandOnClickNode?: boolean

  /** 是否在点击节点的时候选中节点 */
  checkOnClickNode?: boolean

  /** 展开子节点的时候是否自动展开父节点 */
  autoExpandParent?: boolean

  /** 默认勾选的节点的 key 的数组 */
  defaultCheckedKeys?: any[]

  /** 默认展开的节点的 key 的数组 */
  defaultExpandedKeys?: any[]

  /** 当前选中的节点 */
  currentNodeKey?: string | number

  /** 是否在第一次展开某个树节点后才渲染其子节点 */
  renderAfterExpand?: boolean

  /** 加载子树数据的方法，仅当 lazy 属性为true 时生效 */
  load?: () => {}

  /** 树节点的内容区的渲染 Function */
  renderContent?: () => {}

  /** 是否高亮当前选中节点 */
  highlightCurrent?: boolean

  /** 是否默认展开所有节点 */
  defaultExpandAll?: boolean

  /** 在显示复选框的情况下，是否严格的遵循父子不互相关联的做法 */
  checkDescendants?: boolean

  /** 在显示复选框的情况下，是否严格的遵循父子不互相关联的做法 */
  onlyCheckChildren?: boolean

  /** 内容为空的时候展示的文本 */
  emptyText?: string

  /**
   * 响应式模式
   * - 'screen': 基于屏幕尺寸
   * - 'container': 基于容器尺寸（弹窗场景推荐）
   * - 'modal-size': 基于 AModal 的 size 属性
   * @default 'screen'
   */
  responsiveMode?: 'screen' | 'container' | 'modal-size'

  /**
   * 当 responsiveMode 为 'modal-size' 时使用
   * 应该传入 AModal 的 size 属性值
   */
  modalSize?: 'small' | 'medium' | 'large' | 'xl'
}

/**
 * 定义组件属性并设置默认值
 */
const props = withDefaults(defineProps<AFormTreeSelectProps>(), {
  modelValue: null,
  data: () => [],
  props: () => ({ value: 'id', label: 'label', children: 'children' }),
  valueKey: 'id',
  nodeKey: 'id',
  label: '',
  labelWidth: undefined,
  width: undefined,
  placeholder: '',
  prop: '',
  showFormItem: true,
  size: '',
  span: undefined,
  clearable: true,
  disabled: false,
  checkStrictly: true,
  filterable: false,
  tooltip: '',
  multiple: false,
  showCheckbox: false,
  accordion: false,
  indent: 18,
  icon: undefined,
  expandOnClickNode: true,
  checkOnClickNode: false,
  autoExpandParent: true,
  defaultCheckedKeys: () => [],
  defaultExpandedKeys: () => [],
  currentNodeKey: undefined,
  renderAfterExpand: true,
  load: undefined,
  renderContent: undefined,
  highlightCurrent: false,
  defaultExpandAll: false,
  checkDescendants: false,
  onlyCheckChildren: false,
  emptyText: ''
})

/** 组件事件定义 */
const emit = defineEmits(['update:modelValue', 'change', 'blur', 'focus', 'clear', 'visible-change', 'remove-tag'])

// ========== 根据响应式模式选择对应的组合函数 ==========
const { computedSpan, shouldUseCol } = useResponsiveSpan(toRef(props, 'span'), {
  mode: props.responsiveMode,
  modalSize: toRef(props, 'modalSize')
})

/** 组件数据值，用于双向绑定 */
const treeSelectValue = computed({
  get() {
    return props.modelValue
  },
  set(value) {
    emit('update:modelValue', value)
  }
})

/** 使用计算属性计算label标签显示国际化 */
const computedLabel = computed(() => {
  // 未显式传 label：用 prop 作为 key 自动从词典国际化（兼容原有行为）
  if (!props.label) return t(props.prop, props.label)
  // 显式传了 label：中文环境直接用 label，保证 label 可控；
  // 其它语言优先查 prop 词典翻译，命中用词典，否则回退 label
  if (isChinese.value) return props.label
  return te(props.prop) ? t(props.prop) : props.label
})

/** 空数据文本（支持国际化） */
const computedEmptyText = computed(() => {
  return props.emptyText || t('common.noData')
})

/** 节点属性对象 */
const nodeProps = computed(() => {
  return props.props
})

/**
 * 选择变化处理函数
 * @param val 变化后的值
 */
const handleChange = (val: any) => {
  emit('change', val)
}

/**
 * 下拉框显示/隐藏状态变化
 * @param visible 是否可见
 */
const handleVisibleChange = (visible: boolean) => {
  emit('visible-change', visible)
}

/**
 * 清除选择
 */
const handleClear = () => {
  emit('clear')
}

/**
 * 多选模式下移除标签
 * @param value 移除的值
 */
const handleRemoveTag = (value: any) => {
  emit('remove-tag', value)
}

/**
 * 获得焦点
 */
const handleFocus = (event: FocusEvent) => {
  emit('focus', event)
}

/**
 * 失去焦点
 */
const handleBlur = (event: FocusEvent) => {
  emit('blur', event)
}
</script>

<style>
/* 只针对 tree-select 下拉菜单中的 tree，不影响其他独立的 el-tree 组件 */
.el-select-dropdown .el-tree {
  max-height: 360px !important;
  overflow-y: auto !important;
}

/* 或者更精确的选择器 */
.el-tree-select + .el-select-dropdown .el-tree {
  max-height: 360px !important;
  overflow-y: auto !important;
}
</style>
