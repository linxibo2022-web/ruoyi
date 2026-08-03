<!--
表单卡片组件 AFormCard

使用示例:

1. 基本用法
<el-row :gutter="20">
  <el-col :span="24">
    <AFormCard
      title="基本信息"
      :model="{ name: '', email: '' }"
    >
      <el-form-item label="姓名" prop="name">
        <el-input placeholder="请输入姓名" />
      </el-form-item>
      <el-form-item label="邮箱" prop="email">
        <el-input placeholder="请输入邮箱" />
      </el-form-item>
    </AFormCard>
  </el-col>
</el-row>

2. 带副标题和验证规则
<el-row :gutter="20">
  <el-col :span="24">
    <AFormCard
      title="个人资料"
      subtitle="完善您的个人信息以获得更好的服务"
      :model="{ nickname: '', phone: '' }"
      :rules="{
        nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
        phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }]
      }"
    >
      <el-form-item label="昵称" prop="nickname">
        <el-input placeholder="请输入昵称" />
      </el-form-item>
      <el-form-item label="手机" prop="phone">
        <el-input placeholder="请输入手机号" />
      </el-form-item>
    </AFormCard>
  </el-col>
</el-row>

3. 可折叠表单
<el-row :gutter="20">
  <el-col :span="24">
    <AFormCard
      title="高级设置"
      subtitle="配置系统的高级选项"
      :model="{ setting1: '', setting2: '' }"
      collapsible
    >
      <el-form-item label="配置项1">
        <el-input placeholder="请输入配置值" />
      </el-form-item>
      <el-form-item label="配置项2">
        <el-input placeholder="请输入配置值" />
      </el-form-item>
    </AFormCard>
  </el-col>
</el-row>

4. 带操作按钮
<el-row :gutter="20">
  <el-col :span="24">
    <AFormCard
      title="登录"
      :model="{ username: '', password: '' }"
      :rules="{
        username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
        password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
      }"
      show-actions
      submit-text="登录"
      reset-text="清空"
    >
      <el-form-item label="用户名" prop="username">
        <el-input placeholder="请输入用户名" />
      </el-form-item>
      <el-form-item label="密码" prop="password">
        <el-input type="password" placeholder="请输入密码" />
      </el-form-item>
    </AFormCard>
  </el-col>
</el-row>

5. 两列布局
<el-row :gutter="20">
  <el-col :xs="24" :md="12">
    <AFormCard
      title="基础信息"
      :model="{ name: '', age: '' }"
      show-actions
    >
      <el-form-item label="姓名">
        <el-input placeholder="请输入姓名" />
      </el-form-item>
      <el-form-item label="年龄">
        <el-input-number placeholder="请输入年龄" style="width: 100%" />
      </el-form-item>
    </AFormCard>
  </el-col>
  <el-col :xs="24" :md="12">
    <AFormCard
      title="联系方式"
      :model="{ phone: '', address: '' }"
      show-actions
    >
      <el-form-item label="电话">
        <el-input placeholder="请输入电话" />
      </el-form-item>
      <el-form-item label="地址">
        <el-input placeholder="请输入地址" />
      </el-form-item>
    </AFormCard>
  </el-col>
</el-row>
-->
<template>
  <div class="form-card">
    <!-- 卡片头部 -->
    <div class="card-header" @click="handleHeaderClick">
      <div class="header-content">
        <h3 class="card-title">{{ title }}</h3>
        <p v-if="subtitle" class="card-subtitle">{{ subtitle }}</p>
      </div>
      <div class="header-action">
        <slot name="header-action">
          <el-button v-if="collapsible" text @click.stop="toggleCollapse">
            <Icon :code="isCollapsed ? 'down' : 'up'" :size="16" />
          </el-button>
        </slot>
      </div>
    </div>

    <!-- 表单内容 -->
    <el-collapse-transition>
      <div v-show="!isCollapsed" class="card-body">
        <el-form ref="formRef" :model="model" :rules="rules" :label-width="labelWidth" :label-position="labelPosition" :size="size">
          <slot></slot>

          <!-- 操作按钮 -->
          <el-form-item v-if="showActions || $slots.actions" class="form-actions">
            <slot name="actions">
              <el-button type="primary" @click="handleSubmit">
                {{ computedSubmitText }}
              </el-button>
              <el-button @click="handleReset">
                {{ computedResetText }}
              </el-button>
            </slot>
          </el-form-item>
        </el-form>
      </div>
    </el-collapse-transition>
  </div>
</template>

<script setup lang="ts" name="AFormCard">
const { t } = useI18n()

import type { FormInstance, FormRules } from 'element-plus'

/**表单卡片属性接口*/
interface AFormCardProps {
  /**标题*/
  title: string
  /**副标题*/
  subtitle?: string
  /**表单数据模型*/
  model: Record<string, any>
  /**表单验证规则*/
  rules?: FormRules
  /**标签宽度*/
  labelWidth?: string | number
  /**标签位置*/
  labelPosition?: 'left' | 'right' | 'top'
  /**表单尺寸*/
  size?: 'large' | 'default' | 'small'
  /**是否可折叠*/
  collapsible?: boolean
  /**默认是否折叠*/
  defaultCollapsed?: boolean
  /**是否显示操作按钮*/
  showActions?: boolean
  /**提交按钮文本*/
  submitText?: string
  /**重置按钮文本*/
  resetText?: string
}

/**组件属性定义*/
const props = withDefaults(defineProps<AFormCardProps>(), {
  subtitle: '',
  labelWidth: '100px',
  labelPosition: 'right',
  size: 'default',
  collapsible: false,
  defaultCollapsed: false,
  showActions: false,
  submitText: '提交',
  resetText: '重置'
})

/**组件事件定义*/
const emit = defineEmits<{
  /**提交事件*/
  submit: [valid: boolean, model: Record<string, any>]
  /**重置事件*/
  reset: []
}>()

/**计算提交按钮文本*/
const computedSubmitText = computed(() => props.submitText || t('card.form.submit'))

/**计算重置按钮文本*/
const computedResetText = computed(() => props.resetText || t('card.form.reset'))

/**表单引用*/
const formRef = ref<FormInstance>()

/**是否折叠*/
const isCollapsed = ref(props.defaultCollapsed)

/**切换折叠状态*/
const toggleCollapse = () => {
  isCollapsed.value = !isCollapsed.value
}

/**处理头部点击*/
const handleHeaderClick = () => {
  if (props.collapsible) {
    toggleCollapse()
  }
}

/**处理提交*/
const handleSubmit = async () => {
  if (!formRef.value) return

  try {
    const valid = await formRef.value.validate()
    emit('submit', valid, props.model)
  } catch (error) {
    emit('submit', false, props.model)
  }
}

/**处理重置*/
const handleReset = () => {
  formRef.value?.resetFields()
  emit('reset')
}

/**验证表单*/
const validate = async (): Promise<boolean> => {
  if (!formRef.value) return false

  try {
    await formRef.value.validate()
    return true
  } catch (error) {
    return false
  }
}

/**验证指定字段*/
const validateField = async (prop: string): Promise<boolean> => {
  if (!formRef.value) return false

  try {
    await formRef.value.validateField(prop)
    return true
  } catch (error) {
    return false
  }
}

/**清空验证*/
const clearValidate = (props?: string | string[]) => {
  formRef.value?.clearValidate(props)
}

/**暴露方法*/
defineExpose({
  /**表单实例*/
  formInstance: formRef,
  /**验证表单*/
  validate,
  /**验证字段*/
  validateField,
  /**清空验证*/
  clearValidate,
  /**重置表单*/
  resetFields: () => formRef.value?.resetFields(),
  /**折叠*/
  collapse: () => {
    isCollapsed.value = true
  },
  /**展开*/
  expand: () => {
    isCollapsed.value = false
  },
  /**切换折叠*/
  toggleCollapse
})
</script>

<style lang="scss" scoped>
.form-card {
  background-color: var(--bg-level-1);
  border-radius: var(--radius-md);
  border: 1px solid var(--el-border-color);

  .card-header {
    display: flex;
    align-items: flex-start;
    justify-content: space-between;
    padding: 20px;
    border-bottom: 1px solid var(--el-border-color);
    cursor: default;

    .header-content {
      flex: 1;
    }

    .card-title {
      margin: 0 0 4px;
      font-size: 16px;
      font-weight: 500;
      color: var(--el-text-color-primary);
    }

    .card-subtitle {
      margin: 0;
      font-size: 13px;
      color: var(--el-text-color-secondary);
    }

    .header-action {
      margin-left: 16px;

      :deep(.el-button) {
        color: var(--el-text-color-regular);

        &:hover {
          color: var(--el-color-primary);
        }
      }
    }
  }

  .card-body {
    padding: 20px;

    :deep(.el-form) {
      .form-actions {
        margin-top: 20px;
        margin-bottom: 0;

        .el-form-item__content {
          justify-content: flex-end;
        }
      }
    }
  }
}
</style>
