<!-- 规格编辑器 -->
<template>
  <div class="sku-spec-editor">
    <div v-for="(item, index) in specItems" :key="index" class="spec-item">
      <el-input
        v-model="item.name"
        placeholder="规格名(如:颜色)"
        style="width: 100px"
        size="small"
        @input="handleChange"
      />
      <span class="separator">:</span>
      <el-input
        v-model="item.value"
        placeholder="规格值(如:红色)"
        style="width: 100px"
        size="small"
        @input="handleChange"
      />
      <el-button
        link
        type="danger"
        icon="Close"
        size="small"
        @click="removeSpec(index)"
        :disabled="specItems.length === 1"
      />
    </div>
    <el-button link type="primary" icon="Plus" size="small" @click="addSpec">添加规格</el-button>
  </div>
</template>

<script setup lang="ts">
/** 规格项类型 */
interface SpecItem {
  name: string
  value: string
}

/** 组件Props */
interface Props {
  modelValue?: string
}

/** 组件Emits */
interface Emits {
  (e: 'update:modelValue', value: string): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

/** 规格项列表 */
const specItems = ref<SpecItem[]>([{ name: '', value: '' }])

/** 监听外部值变化，解析JSON为规格项 */
watch(
  () => props.modelValue,
  (newVal) => {
    if (!newVal) {
      specItems.value = [{ name: '', value: '' }]
      return
    }

    try {
      const jsonObj = JSON.parse(newVal)
      const items: SpecItem[] = []
      for (const [name, value] of Object.entries(jsonObj)) {
        items.push({ name, value: String(value) })
      }
      if (items.length > 0) {
        specItems.value = items
      }
    } catch (e) {
      // 解析失败，保持默认
      console.warn('规格值JSON解析失败:', e)
    }
  },
  { immediate: true }
)

/** 添加规格项 */
const addSpec = () => {
  specItems.value.push({ name: '', value: '' })
}

/** 删除规格项 */
const removeSpec = (index: number) => {
  if (specItems.value.length > 1) {
    specItems.value.splice(index, 1)
    handleChange()
  }
}

/** 规格项变化处理，转换为JSON并同步到外部 */
const handleChange = () => {
  const jsonObj: Record<string, string> = {}
  let hasValue = false

  for (const item of specItems.value) {
    if (item.name && item.value) {
      jsonObj[item.name] = item.value
      hasValue = true
    }
  }

  emit('update:modelValue', hasValue ? JSON.stringify(jsonObj) : '')
}
</script>

<style scoped lang="scss">
.sku-spec-editor {
  display: flex;
  flex-direction: column;
  gap: 8px;

  .spec-item {
    display: flex;
    align-items: center;
    gap: 8px;

    .separator {
      color: #909399;
      font-weight: bold;
    }
  }
}
</style>
