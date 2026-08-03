<!-- SKU管理 -->
<template>
  <div class="goods-sku-manager">
    <!-- 第一步:规格选择器 -->
    <SkuSpecSelector :sku-list="skuList" @generate="handleGenerateSkus" @clear="handleSpecClear" />

    <!-- 第二步:SKU列表编辑 -->
    <el-card shadow="hover" style="margin-top: 20px" v-if="skuList.length > 0">
      <template #header>
        <div class="card-header">
          <span>SKU列表 ({{ skuList.length }} 个)</span>
        </div>
      </template>

      <el-table :data="skuList" border style="width: 100%" max-height="500">
        <el-table-column type="index" label="序号" width="60" align="center" fixed="left" />

        <el-table-column label="SKU图片" width="100" align="center" fixed="left">
          <template #default="{ row }">
            <AFormImgUpload v-model="row.img" :show-form-item="false" :limit="1" size="small" :is-show-tip="false" />
          </template>
        </el-table-column>

        <el-table-column label="SKU名称" min-width="100" fixed="left">
          <template #default="{ row }">
            <el-tag type="primary">{{ row.skuName }}</el-tag>
          </template>
        </el-table-column>

        <el-table-column label="原价" width="130">
          <template #default="{ row }">
            <el-input-number v-model="row.originalPrice" :precision="2" :step="1" :min="0" controls-position="right" style="width: 100%" />
          </template>
        </el-table-column>

        <el-table-column label="价格" width="130">
          <template #default="{ row }">
            <el-input-number v-model="row.price" :precision="2" :step="1" :min="0" controls-position="right" style="width: 100%" />
          </template>
        </el-table-column>

        <el-table-column label="库存" width="130">
          <template #default="{ row }">
            <el-input-number v-model="row.stock" :step="1" :min="0" controls-position="right" style="width: 100%" />
          </template>
        </el-table-column>

        <el-table-column label="排序" width="120">
          <template #default="{ row }">
            <el-input-number v-model="row.sortOrder" :step="1" :min="0" controls-position="right" style="width: 100%" />
          </template>
        </el-table-column>

        <el-table-column label="是否默认" width="90" align="center">
          <template #default="{ row }">
            <el-radio v-model="defaultSkuId" :label="getSkuTempId(row)" @change="handleDefaultChange(row)">
              <span></span>
            </el-radio>
          </template>
        </el-table-column>

        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-switch v-model="row.status" active-value="1" inactive-value="0" />
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import type { GoodsSkuBo } from '@/api/business/mall/goodsSku/goodsSkuTypes'
import SkuSpecSelector from './SkuSpecSelector.vue'
import { showMsgSuccess, showMsg, showConfirm } from '@/utils/modal'

/** 规格维度类型 */
interface SpecDimension {
  name: string
  values: string[]
}

// 定义Props
interface Props {
  modelValue: GoodsSkuBo[]
}

// 定义Emits
interface Emits {
  (e: 'update:modelValue', value: GoodsSkuBo[]): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

// SKU列表
const skuList = ref<GoodsSkuBo[]>([])

// 默认SKU的临时ID(用于单选绑定)
const defaultSkuId = ref<string>('')

/**
 * 获取SKU的临时唯一标识
 * 用于单选框绑定(因为新增的SKU没有id)
 */
const getSkuTempId = (sku: GoodsSkuBo): string => {
  return sku.id ? `id_${sku.id}` : `spec_${sku.specValues}`
}

/**
 * 初始化默认SKU选中状态
 */
const initDefaultSku = () => {
  const defaultSku = skuList.value.find((sku) => sku.isDefault === '1')
  if (defaultSku) {
    defaultSkuId.value = getSkuTempId(defaultSku)
  } else {
    defaultSkuId.value = ''
  }
}

/**
 * 处理默认SKU变更
 */
const handleDefaultChange = (selectedSku: GoodsSkuBo) => {
  // 将所有SKU的isDefault设置为'0'
  skuList.value.forEach((sku) => {
    sku.isDefault = '0'
  })
  // 将选中的SKU设置为'1'
  selectedSku.isDefault = '1'
}

// 监听外部数据变化
watch(
  () => props.modelValue,
  (newVal) => {
    skuList.value = newVal || []
    // 初始化默认SKU选中状态
    initDefaultSku()
  },
  { immediate: true, deep: true }
)

// 监听内部数据变化，同步到外部
watch(
  skuList,
  (newVal) => {
    emit('update:modelValue', newVal)
  },
  { deep: true }
)

/**
 * 检测孤儿SKU
 * 孤儿SKU指的是规格值已被删除,但SKU仍然存在的情况
 */
const detectOrphanedSkus = (skus: GoodsSkuBo[], dimensions: SpecDimension[]): GoodsSkuBo[] => {
  // 构建当前有效的规格值集合
  const validSpecs = new Map<string, Set<string>>()
  dimensions.forEach((dim) => {
    validSpecs.set(dim.name, new Set(dim.values))
  })

  // 检查每个SKU的规格值是否都存在于有效集合中
  return skus.filter((sku) => {
    if (!sku.specValues) return false
    try {
      const specObj = JSON.parse(sku.specValues)
      // 检查是否有任何规格值已被删除
      return Object.entries(specObj).some(([key, value]) => {
        const validValues = validSpecs.get(key)
        return !validValues || !validValues.has(value as string)
      })
    } catch {
      return false
    }
  })
}

/**
 * 批量生成SKU
 * 根据规格维度的笛卡尔积生成所有可能的SKU组合
 */
const handleGenerateSkus = async (dimensions: SpecDimension[]) => {
  if (dimensions.length === 0) return

  // 步骤1: 检测孤儿SKU
  const orphanedSkus = detectOrphanedSkus(skuList.value, dimensions)

  // 步骤2: 如果存在孤儿SKU,先提示用户是否删除
  if (orphanedSkus.length > 0) {
    const orphanedNames = orphanedSkus.map((s) => s.skuName).join('、')
    const [err] = await showConfirm(
      `检测到 ${orphanedSkus.length} 个SKU的规格值已被删除:\n${orphanedNames}\n\n是否删除这些SKU?`,
      '提示',
      { type: 'warning' }
    )

    if (!err) {
      // 用户确认删除,从列表中移除孤儿SKU
      skuList.value = skuList.value.filter((sku) => !orphanedSkus.some((orphan) => getSkuTempId(orphan) === getSkuTempId(sku)))
      showMsgSuccess(`已删除 ${orphanedSkus.length} 个无效SKU`)
    } else {
      // 用户取消删除,直接返回,不继续生成
      return
    }
  }

  // 步骤3: 生成笛卡尔积
  const combinations = cartesianProduct(dimensions)

  // 步骤4: 提示用户
  const [err] = await showConfirm(`将生成 ${combinations.length} 个SKU,已存在的SKU将被保留,确认生成吗?`, '提示', {
    type: 'warning'
  })
  if (!err) {
    const newSkus: GoodsSkuBo[] = []

    for (const combination of combinations) {
      // 构建规格JSON对象(按key排序,确保一致性)
      const specObj: Record<string, string> = {}
      const nameArray: string[] = []

      for (const item of combination) {
        specObj[item.name] = item.value
        nameArray.push(item.value)
      }

      // 排序key
      const sortedSpecObj = Object.keys(specObj)
        .sort()
        .reduce(
          (acc, key) => {
            acc[key] = specObj[key]
            return acc
          },
          {} as Record<string, string>
        )

      const specValuesStr = JSON.stringify(sortedSpecObj)

      // 检查是否已存在相同规格的SKU
      const existingSku = skuList.value.find((sku) => {
        if (!sku.specValues) return false
        try {
          const existingSpec = JSON.parse(sku.specValues)
          const existingSorted = JSON.stringify(
            Object.keys(existingSpec)
              .sort()
              .reduce(
                (acc, key) => {
                  acc[key] = existingSpec[key]
                  return acc
                },
                {} as Record<string, string>
              )
          )
          return existingSorted === specValuesStr
        } catch {
          return false
        }
      })

      if (!existingSku) {
        // 生成SKU名称
        const skuName = nameArray.join('-')

        newSkus.push({
          skuName,
          specValues: specValuesStr,
          originalPrice: undefined,
          price: undefined,
          stock: 0,
          salesCount: 0,
          status: '1',
          sortOrder: 999,
          isDefault: '0', // 默认非默认SKU
          remark: ''
        })
      }
    }

    if (newSkus.length > 0) {
      skuList.value.push(...newSkus)
      showMsgSuccess(`成功生成 ${newSkus.length} 个新SKU`)
    } else {
      showMsg('所有SKU组合均已存在')
    }
  }
}

/**
 * 计算规格维度的笛卡尔积
 */
const cartesianProduct = (dimensions: SpecDimension[]): Array<{ name: string; value: string }[]> => {
  if (dimensions.length === 0) return []
  if (dimensions.length === 1) {
    return dimensions[0].values.map((value) => [{ name: dimensions[0].name, value }])
  }

  const result: Array<{ name: string; value: string }[]> = []
  const helper = (index: number, current: { name: string; value: string }[]) => {
    if (index === dimensions.length) {
      result.push([...current])
      return
    }

    for (const value of dimensions[index].values) {
      current.push({ name: dimensions[index].name, value })
      helper(index + 1, current)
      current.pop()
    }
  }

  helper(0, [])
  return result
}

/**
 * 处理规格选择器的清空事件
 * 规格清空时同步清空SKU列表
 */
const handleSpecClear = () => {
  skuList.value = []
  defaultSkuId.value = ''
}
</script>

<style scoped lang="scss">
.goods-sku-manager {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .spec-tag {
    margin-right: 8px;
    margin-bottom: 4px;
  }

  :deep(.el-input-number) {
    width: 100%;
  }
}
</style>
