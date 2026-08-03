<!-- 规格选择器 -->
<template>
  <div class="sku-spec-selector">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>规格设置</span>
          <div class="header-actions">
            <el-button type="primary" size="small" icon="Plus" @click="handleAddSpecDimension" :disabled="specDimensions.length >= 3">
              添加规格维度
            </el-button>
            <el-button size="small" icon="Delete" @click="handleClearAll" v-if="specDimensions.length > 0"> 清空所有 </el-button>
          </div>
        </div>
      </template>

      <!-- 规格维度列表 -->
      <div v-if="specDimensions.length === 0" class="empty-tip">
        <el-empty description="请先添加规格维度(如: 颜色、尺码)" :image-size="80" />
      </div>

      <div v-for="(dimension, dimIndex) in specDimensions" :key="dimIndex" class="spec-dimension">
        <div class="dimension-header">
          <el-input v-model="dimension.name" placeholder="规格名称(如:颜色)" style="width: 200px" @blur="handleDimensionNameChange">
            <template #prepend>
              <el-icon><Tickets /></el-icon>
            </template>
          </el-input>

          <el-button link type="danger" icon="Delete" @click="handleRemoveSpecDimension(dimIndex)"> 删除维度 </el-button>
        </div>

        <!-- 规格值列表 -->
        <div class="dimension-values">
          <el-tag
            v-for="(value, valueIndex) in dimension.values"
            :key="valueIndex"
            closable
            @close="handleRemoveSpecValue(dimIndex, valueIndex)"
            class="spec-value-tag"
          >
            {{ value }}
          </el-tag>

          <!-- 添加规格值输入框 -->
          <el-input
            v-if="dimension.showInput"
            ref="specValueInputRef"
            v-model="dimension.inputValue"
            size="small"
            style="width: 100px"
            @keyup.enter="handleAddSpecValue(dimIndex)"
            @blur="handleAddSpecValue(dimIndex)"
          />
          <el-button v-else size="small" @click="showSpecValueInput(dimIndex)" icon="Plus"> 添加值 </el-button>
        </div>
      </div>

      <!-- 批量生成SKU按钮 -->
      <div v-if="specDimensions.length > 0" class="generate-actions">
        <el-button type="success" @click="handleGenerateSkus">
          <Icon name="magic-wand" />
          批量生成SKU ({{ totalSkuCount }} 个)
        </el-button>
        <el-text type="info" size="small"> 将根据规格组合自动生成 {{ totalSkuCount }} 个SKU </el-text>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { showMsgWarning, showMsgSuccess, showConfirm } from '@/utils/modal'
import { Tickets } from '@element-plus/icons-vue'

/** 规格维度类型 */
interface SpecDimension {
  name: string // 维度名称(如"颜色")
  values: string[] // 维度值列表(如["红色","蓝色"])
  showInput: boolean // 是否显示输入框
  inputValue: string // 输入框的值
}

/** 组件Props */
interface Props {
  /** 已有的SKU列表,用于初始化规格维度 */
  skuList?: Array<{ specValues?: string }>
}

/** 组件Emits */
interface Emits {
  (e: 'generate', dimensions: SpecDimension[]): void
  (e: 'clear'): void // 清空规格时通知父组件
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

/** 规格维度列表 */
const specDimensions = ref<SpecDimension[]>([])

/** 输入框引用 */
const specValueInputRef = ref()

/**
 * 从SKU列表中反向解析规格维度
 * 用于编辑商品时初始化规格设置
 */
const initDimensionsFromSkuList = (skuList: Array<{ specValues?: string }>) => {
  if (!skuList || skuList.length === 0) {
    specDimensions.value = []
    return
  }

  // 用 Map 来收集所有规格维度及其值
  const dimensionMap = new Map<string, Set<string>>()

  // 遍历所有SKU,收集规格维度信息
  for (const sku of skuList) {
    if (!sku.specValues) continue

    try {
      const specObj = JSON.parse(sku.specValues)
      // specObj 格式: { "颜色": "红色", "尺码": "M" }
      for (const [dimName, dimValue] of Object.entries(specObj)) {
        if (!dimensionMap.has(dimName)) {
          dimensionMap.set(dimName, new Set())
        }
        dimensionMap.get(dimName)!.add(dimValue as string)
      }
    } catch (e) {
      console.error('解析SKU规格值失败:', e)
    }
  }

  // 转换为组件需要的格式
  specDimensions.value = Array.from(dimensionMap.entries()).map(([name, values]) => ({
    name,
    values: Array.from(values),
    showInput: false,
    inputValue: ''
  }))
}

/** 监听SKU列表变化,自动初始化规格维度 */
watch(
  () => props.skuList,
  (newSkuList) => {
    // 如果SKU列表为空,清空规格维度
    if (!newSkuList || newSkuList.length === 0) {
      specDimensions.value = []
      return
    }

    // 只在规格维度为空且有SKU数据时才初始化
    if (specDimensions.value.length === 0) {
      initDimensionsFromSkuList(newSkuList)
    }
  },
  { immediate: true, deep: true }
)

/** 计算总SKU数量 */
const totalSkuCount = computed(() => {
  if (specDimensions.value.length === 0) return 0

  let count = 1
  for (const dimension of specDimensions.value) {
    if (dimension.values.length > 0) {
      count *= dimension.values.length
    } else {
      return 0 // 如果有维度没有值,则无法生成
    }
  }
  return count
})

/** 添加规格维度 */
const handleAddSpecDimension = () => {
  if (specDimensions.value.length >= 3) {
    showMsgWarning('最多支持3个规格维度')
    return
  }

  specDimensions.value.push({
    name: '',
    values: [],
    showInput: false,
    inputValue: ''
  })
}

/** 删除规格维度 */
const handleRemoveSpecDimension = (index: number) => {
  specDimensions.value.splice(index, 1)
}

/** 规格维度名称变化 */
const handleDimensionNameChange = () => {
  // 校验维度名称不能重复
  const names = specDimensions.value.map((d) => d.name.trim()).filter((n) => n)
  const uniqueNames = new Set(names)
  if (names.length !== uniqueNames.size) {
    showMsgWarning('规格维度名称不能重复')
  }
}

/** 显示添加规格值的输入框 */
const showSpecValueInput = (dimIndex: number) => {
  specDimensions.value[dimIndex].showInput = true
  nextTick(() => {
    specValueInputRef.value?.[0]?.focus()
  })
}

/** 添加规格值 */
const handleAddSpecValue = (dimIndex: number) => {
  const dimension = specDimensions.value[dimIndex]
  const value = dimension.inputValue?.trim()

  if (value) {
    // 检查是否重复
    if (dimension.values.includes(value)) {
      showMsgWarning('规格值已存在')
    } else {
      dimension.values.push(value)
    }
  }

  dimension.inputValue = ''
  dimension.showInput = false
}

/** 删除规格值 */
const handleRemoveSpecValue = (dimIndex: number, valueIndex: number) => {
  specDimensions.value[dimIndex].values.splice(valueIndex, 1)
}

/** 批量生成SKU */
const handleGenerateSkus = () => {
  // 校验
  for (const dimension of specDimensions.value) {
    if (!dimension.name.trim()) {
      showMsgWarning('请填写规格维度名称')
      return
    }
    if (dimension.values.length === 0) {
      showMsgWarning(`请为"${dimension.name}"添加至少一个规格值`)
      return
    }
  }

  // 检查维度名称是否重复
  const names = specDimensions.value.map((d) => d.name.trim())
  const uniqueNames = new Set(names)
  if (names.length !== uniqueNames.size) {
    showMsgWarning('规格维度名称不能重复')
    return
  }

  emit('generate', JSON.parse(JSON.stringify(specDimensions.value)))
}

/** 清空所有规格 */
const handleClearAll = async () => {
  const [err] = await showConfirm('确认清空所有规格设置吗？这也会清空已生成的SKU列表。', '提示', {
    type: 'warning',
    confirmButtonText: '确认清空',
    cancelButtonText: '取消'
  })
  if (err) return

  specDimensions.value = []
  emit('clear') // 通知父组件清空SKU列表
  showMsgSuccess('已清空')
}
</script>

<style scoped lang="scss">
.sku-spec-selector {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;

    .header-actions {
      display: flex;
      gap: 8px;
    }
  }

  .empty-tip {
    padding: 20px;
    text-align: center;
  }

  .spec-dimension {
    border: 1px solid #e4e7ed;
    border-radius: 4px;
    padding: 16px;
    margin-bottom: 16px;

    &:last-child {
      margin-bottom: 0;
    }

    .dimension-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 12px;
    }

    .dimension-values {
      display: flex;
      flex-wrap: wrap;
      gap: 8px;
      align-items: center;

      .spec-value-tag {
        height: 32px;
        line-height: 30px;
      }
    }
  }

  .generate-actions {
    margin-top: 20px;
    padding-top: 20px;
    border-top: 1px dashed #e4e7ed;
    display: flex;
    align-items: center;
    gap: 12px;
  }
}
</style>
