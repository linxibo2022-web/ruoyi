<!-- 基本信息配置 -->
<template>
  <el-form ref="basicInfoFormRef" :model="infoForm" :rules="rules" label-width="auto">
    <el-row>
      <AFormInput v-model="infoForm.tableName" :label="t('Table Name', '表名称')" prop="tableName" :span="12"></AFormInput>

      <AFormInput v-model="infoForm.tableComment" :label="t('Table Comment', '表描述')" prop="tableComment" :span="12"></AFormInput>

      <AFormInput v-model="infoForm.className" :label="t('Class Name', '实体类名称')" prop="className" :span="12"></AFormInput>

      <AFormInput v-model="infoForm.functionAuthor" :label="t('Author', '作者')" prop="functionAuthor" :span="12"></AFormInput>

      <AFormInput type="textarea" v-model="infoForm.remark" :label="t('Remark', '备注')" prop="remark" :rows="3" :span="24"></AFormInput>
    </el-row>
  </el-form>
</template>

<script setup lang="ts" name="BasicInfoForm">
const { t } = useI18n()

// =========== 类型定义 ===========
/**
 * 基础信息表单组件Props接口
 */
interface BasicInfoFormProps {
  /** 表单信息对象 - 接受DbTableVO的部分属性 */
  info: Partial<{
    tableName: string
    tableComment: string
    className: string
    functionAuthor: string
    remark: string
    [key: string]: any
  }>
}

// =========== Props 定义 ===========
const props = withDefaults(defineProps<BasicInfoFormProps>(), {
  info: () => ({})
})

// =========== 表单相关 ===========
/**表单引用*/
const basicInfoFormRef = ref<ElFormInstance>()

/**响应式表单数据*/
const infoForm = computed(() => props.info || {})

/**表单校验规则*/
const rules = computed<ElFormRules>(() => ({
  tableName: [{ required: true, message: t('Table name is required', '表名称不能为空'), trigger: 'blur' }],
  tableComment: [{ required: true, message: t('Table comment is required', '表描述不能为空'), trigger: 'blur' }],
  className: [{ required: true, message: t('Class name is required', '实体类名称不能为空'), trigger: 'blur' }],
  functionAuthor: [{ required: true, message: t('Author is required', '作者不能为空'), trigger: 'blur' }]
}))

// =========== 暴露方法 ===========
defineExpose({
  $refs: {
    basicInfoForm: basicInfoFormRef
  }
})
</script>
