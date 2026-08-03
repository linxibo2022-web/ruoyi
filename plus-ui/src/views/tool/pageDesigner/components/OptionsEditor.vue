<!-- 选项编辑器 -->
<template>
  <div class="options-editor">
    <div v-for="(option, index) in options" :key="index" class="option-item">
      <el-input v-model="option.label" placeholder="显示文本" size="small" @change="handleChange" />
      <el-input v-model="option.value" placeholder="值" size="small" @change="handleChange" />
      <el-button :icon="Delete" size="small" type="danger" plain @click="removeOption(index)" />
    </div>
    <el-button :icon="Plus" size="small" type="primary" plain class="add-btn" @click="addOption">
      添加选项
    </el-button>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { Plus, Delete } from '@element-plus/icons-vue'
import type { OptionItem } from '../types'

defineOptions({ name: 'OptionsEditor' })

const props = defineProps<{
  modelValue?: OptionItem[]
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: OptionItem[]): void
  (e: 'change'): void
}>()

const options = ref<OptionItem[]>([])

// 监听外部值变化
watch(
  () => props.modelValue,
  (val) => {
    if (val) {
      options.value = [...val]
    }
  },
  { immediate: true, deep: true }
)

// 添加选项
function addOption() {
  const index = options.value.length + 1
  options.value.push({
    label: `选项${index}`,
    value: String(index)
  })
  handleChange()
}

// 删除选项
function removeOption(index: number) {
  options.value.splice(index, 1)
  handleChange()
}

// 触发更新
function handleChange() {
  emit('update:modelValue', [...options.value])
  emit('change')
}
</script>

<style scoped lang="scss">
.options-editor {
  .option-item {
    display: flex;
    gap: 8px;
    margin-bottom: 8px;

    .el-input {
      flex: 1;
    }
  }

  .add-btn {
    width: 100%;
  }
}
</style>
