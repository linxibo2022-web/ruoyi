<!--用法示例
//一般用于搜索栏
<AFormSwitch v-model="queryParams.status" label="状态" prop="status" @change="handleQuery"></AFormSwitch>
//一般用于表单
<AFormSwitch v-model="form.status" label="状态" prop="status" :span="12"></AFormSwitch>
//带提示信息的开关
<AFormSwitch v-model="form.isPublic" label="是否公开" prop="isPublic" tooltip="公开后所有用户可见" :span="12"></AFormSwitch>
//不含el-form-item容器的简单开关
<AFormSwitch v-model="form.enabled" label="启用状态" :show-form-item="false"></AFormSwitch>
//自定义文本的开关
<AFormSwitch v-model="form.autoSave" label="自动保存" active-text="开启" inactive-text="关闭" :span="12"></AFormSwitch>
//布尔值类型的开关
<AFormSwitch v-model="form.isEnabled" label="功能开关" :active-value="true" :inactive-value="false" :span="12"></AFormSwitch>
//自定义颜色的开关
<AFormSwitch v-model="form.isDanger" label="危险模式" active-color="#ff4949" inactive-color="#13ce66" :span="12"></AFormSwitch>
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

      <el-switch
        v-model="switchValue"
        :active-value="activeValue"
        :inactive-value="inactiveValue"
        :active-text="activeText"
        :inactive-text="inactiveText"
        :active-color="activeColor"
        :inactive-color="inactiveColor"
        :disabled="disabled"
        :loading="loading"
        :size="size"
        :width="width"
        :inline-prompt="inlinePrompt"
        :before-change="beforeChange"
        :validate-event="validateEvent"
        @change="handleChange"
        @focus="handleFocus"
        @blur="handleBlur"
      />
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

    <el-switch
      v-model="switchValue"
      :active-value="activeValue"
      :inactive-value="inactiveValue"
      :active-text="activeText"
      :inactive-text="inactiveText"
      :active-color="activeColor"
      :inactive-color="inactiveColor"
      :disabled="disabled"
      :loading="loading"
      :size="size"
      :width="width"
      :inline-prompt="inlinePrompt"
      :before-change="beforeChange"
      :validate-event="validateEvent"
      @change="handleChange"
      @focus="handleFocus"
      @blur="handleBlur"
    />
  </el-form-item>

  <!--showFormItem为false 只显示开关-->
  <template v-else>
    <el-switch
      v-model="switchValue"
      :active-value="activeValue"
      :inactive-value="inactiveValue"
      :active-text="activeText"
      :inactive-text="inactiveText"
      :active-color="activeColor"
      :inactive-color="inactiveColor"
      :disabled="disabled"
      :loading="loading"
      :size="size"
      :width="width"
      :inline-prompt="inlinePrompt"
      :before-change="beforeChange"
      :validate-event="validateEvent"
      @change="handleChange"
      @focus="handleFocus"
      @blur="handleBlur"
    />
  </template>
</template>

<script setup lang="ts" name="AFormSwitch">
const { t, te, isChinese } = useI18n()

/**
 * 开关组件的Props接口定义
 * @description 定义了开关组件的所有属性和类型
 */
interface AFormSwitchProps {
  /**
   * 绑定值，支持字符串、数字或布尔值
   * @default undefined
   */
  modelValue?: string | number | boolean | undefined

  /**
   * 标签文本
   * @default ''
   */
  label?: string

  /**
   * 标签宽度，支持数字或字符串
   * @default undefined
   */
  labelWidth?: number | string

  /**
   * 表单域model数据字段名
   * @default ''
   */
  prop?: string

  /**
   * 栅格占据的列数，支持数字、数字字符串、响应式对象或预设字符串
   * - 数字：固定span值，如 12
   * - 数字字符串：如 "12"，会自动转换为数字
   * - 响应式对象：{ xs: 24, sm: 24, md: 12, lg: 8, xl: 6 }
   * - 预设字符串：'auto' - 自动响应式布局
   * @default undefined
   */
  span?: SpanType

  /**
   * 是否显示表单项
   * @default true
   */
  showFormItem?: boolean

  /**
   * 提示信息
   * @default ''
   */
  tooltip?: string

  /**
   * 开关打开时的值
   * @default '1'
   */
  activeValue?: string | number | boolean

  /**
   * 开关关闭时的值
   * @default '0'
   */
  inactiveValue?: string | number | boolean

  /**
   * 开关打开时显示的文字
   * @default ''
   */
  activeText?: string

  /**
   * 开关关闭时显示的文字
   * @default ''
   */
  inactiveText?: string

  /**
   * 开关打开时的背景色
   * @default '#409eff'
   */
  activeColor?: string

  /**
   * 开关关闭时的背景色
   * @default '#dcdfe6'
   */
  inactiveColor?: string

  /**
   * 是否禁用
   * @default false
   */
  disabled?: boolean

  /**
   * 是否显示加载中
   * @default false
   */
  loading?: boolean

  /**
   * 组件尺寸
   * @default ''
   */
  size?: '' | 'default' | 'small' | 'large'

  /**
   * 开关的宽度
   * @default undefined
   */
  width?: number

  /**
   * 是否在按钮内显示文字
   * @default false
   */
  inlinePrompt?: boolean

  /**
   * 切换前的钩子函数，返回 false 或者返回 Promise 且被 reject 则停止切换
   * @default undefined
   */
  beforeChange?: () => Promise<boolean> | boolean

  /**
   * 是否触发表单验证
   * @default true
   */
  validateEvent?: boolean
  /**
   * 是否禁用初始化时的 change 事件
   * @default true
   */
  preventInitialChange?: boolean

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

// 使用 withDefaults 定义 props，提供默认值
const props = withDefaults(defineProps<AFormSwitchProps>(), {
  modelValue: undefined,
  label: '',
  labelWidth: undefined,
  prop: '',
  span: undefined,
  showFormItem: false,
  tooltip: '',
  activeValue: '1',
  inactiveValue: '0',
  activeText: '',
  inactiveText: '',
  activeColor: '#409eff',
  inactiveColor: '#dcdfe6',
  disabled: false,
  loading: false,
  size: '',
  width: undefined,
  inlinePrompt: false,
  beforeChange: undefined,
  validateEvent: true,
  preventInitialChange: true
})

const emit = defineEmits(['update:modelValue', 'change', 'focus', 'blur'])

// ========== 根据响应式模式选择对应的组合函数 ==========
const { computedSpan, shouldUseCol } = useResponsiveSpan(toRef(props, 'span'), {
  mode: props.responsiveMode,
  modalSize: toRef(props, 'modalSize')
})

// 初始化标识
const isInitialized = ref(false)

// 使用计算属性实现双向绑定
const switchValue = computed({
  get() {
    return props.modelValue
  },
  set(value) {
    emit('update:modelValue', value)
  }
})

// 使用计算属性计算label标签显示国际化
const computedLabel = computed(() => {
  // 未显式传 label：用 prop 作为 key 自动从词典国际化（兼容原有行为）
  if (!props.label) return t(props.prop, props.label)
  // 显式传了 label：中文环境直接用 label，保证 label 可控；
  // 其它语言优先查 prop 词典翻译，命中用词典，否则回退 label
  if (isChinese.value) return props.label
  return te(props.prop) ? t(props.prop) : props.label
})

/**
 * 处理变化事件
 * @param val 变化后的值
 */
const handleChange = (val: any) => {
  // 如果启用了防止初始化触发，且还未初始化完成，则不触发 change 事件
  if (props.preventInitialChange && !isInitialized.value) {
    return
  }
  emit('change', val)
}

/**
 * 处理获得焦点事件
 * @param event 焦点事件
 */
const handleFocus = (event: FocusEvent) => {
  emit('focus', event)
}

/**
 * 处理失去焦点事件
 * @param event 焦点事件
 */
const handleBlur = (event: FocusEvent) => {
  emit('blur', event)
}

// 组件挂载后延迟设置初始化完成标识
onMounted(async () => {
  await nextTick()
  // 延迟设置，确保初始渲染完成
  setTimeout(() => {
    isInitialized.value = true
  }, 100)
})
</script>
