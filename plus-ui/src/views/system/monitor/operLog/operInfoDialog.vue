<!-- 操作日志详情 -->
<template>
  <AModal
    v-model="open"
    :title="t('Oper Log Detail', '操作日志详细')"
    mask-closable
    footer-type="close-only"
    @cancel="closeDialog"
    @closed="info = null"
  >
    <el-descriptions v-if="info" :column="1" border>
      <!-- 操作结果 -->
      <el-descriptions-item :label="t('Oper Result', '操作结果')">
        <template #default>
          <DictTag :value="info?.status" :options="sys_oper_result"></DictTag>
        </template>
      </el-descriptions-item>

      <!-- 登录信息 -->
      <el-descriptions-item :label="t('Login Info', '登录信息')">
        <template #default> {{ info.operName }} / {{ info.deptName }} / {{ info.operIp }} / {{ info.operLocation }} </template>
      </el-descriptions-item>
      <el-descriptions-item :label="t('Request Info', '请求信息')">
        <template #default> {{ info.requestMethod }} {{ info.operUrl }}</template>
      </el-descriptions-item>
      <el-descriptions-item :label="t('Oper Module', '操作模块')">
        <template #default> {{ info.title }} / {{ typeFormat(info.operType) }}</template>
      </el-descriptions-item>
      <el-descriptions-item :label="t('Oper Method', '操作方法')">
        <template #default>
          {{ info.method }}
        </template>
      </el-descriptions-item>
      <el-descriptions-item :label="t('Request Params', '请求参数')">
        <template #default>
          <div class="max-h-300px overflow-y-auto">
            <VueJsonPretty :data="formatToJsonObject(info.operParam)" />
          </div>
        </template>
      </el-descriptions-item>
      <el-descriptions-item :label="t('Response Params', '返回参数')">
        <template #default>
          <div class="max-h-300px overflow-y-auto">
            <VueJsonPretty :data="formatToJsonObject(info.jsonResult)" />
          </div>
        </template>
      </el-descriptions-item>
      <el-descriptions-item :label="t('Cost Time', '消耗时间')">
        <template #default>
          <span> {{ info.costTime }}ms </span>
        </template>
      </el-descriptions-item>
      <el-descriptions-item :label="t('Oper Time', '操作时间')">
        <template #default> {{ formatDate(info.operTime) }}</template>
      </el-descriptions-item>
      <el-descriptions-item v-if="info.status === '1'" :label="t('Error Info', '异常信息')">
        <template #default>
          <span class="text-danger"> {{ info.errorMsg }}</span>
        </template>
      </el-descriptions-item>
    </el-descriptions>
  </AModal>
</template>

<script setup lang="ts">
import type { SysOperLogBo } from '@/api/system/monitor/operLog/operLogTypes'
import VueJsonPretty from 'vue-json-pretty'
import 'vue-json-pretty/lib/styles.css'
import { useDictStore } from '@/stores/modules/dict'
import { useDict } from '@/composables/useDict'

const { t } = useI18n()
const { sys_oper_result } = useDict('sys_oper_result')
import { formatDate } from '@/utils/date'
// 对话框状态
const open = ref(false)
const info = ref<SysOperLogBo | null>(null)
/**
 * 打开对话框
 * @param row 操作日志数据
 */
const openDialog = (row: SysOperLogBo) => {
  info.value = row
  open.value = true
}

/**
 * 关闭对话框
 */
const closeDialog = () => {
  open.value = false
}

// 对外暴露方法
defineExpose({
  openDialog,
  closeDialog
})

/**
 * 将JSON字符串转换为对象
 * @param data 原始JSON字符串
 * @returns 解析后的对象或原始字符串
 */
function formatToJsonObject(data: string) {
  if (!data) return ''

  try {
    return JSON.parse(data)
  } catch (error) {
    return data
  }
}

/**
 * 字典信息
 */
const dictStore = useDictStore()

/**
 * 获取操作类型标签
 * @param operType 业务操作类型
 * @returns 操作类型标签文本
 */
const typeFormat = (operType: string): string => {
  return dictStore.getDictLabel('sys_oper_type', operType)
}
</script>

<style lang="scss" scoped>
/**
label宽度固定
*/
:deep(.el-descriptions__label) {
  min-width: 100px;
}

/**
文字超过 换行显示
*/
:deep(.el-descriptions__content) {
  max-width: 300px;
}
</style>
