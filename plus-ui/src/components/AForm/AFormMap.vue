<!--
📌 高德地图选点组件 AFormMap

## 特性
- ✅ 使用官方 @amap/amap-jsapi-loader 按需加载
- ✅ 多组件实例共享 AMap 对象（只加载一次）
- ✅ 弹窗级生命周期（打开创建，关闭销毁）
- ✅ 无需全局配置，配置在组件属性上

## 配置说明

1. 获取高德地图密钥
   访问：https://lbs.amap.com/
   - 注册并登录账号
   - 创建应用，选择 Web端(JSAPI) 类型
   - 获取 Key 和安全密钥（推荐）

2. 安装依赖（如未安装）
   npm install @amap/amap-jsapi-loader

3. 在组件上配置
   <AFormMap
     amap-key="你的高德地图Key"
     amap-security-code="你的安全密钥（可选）"
     v-model="form.address"
     label="门店地址"
   />
   或者在组件内配置
   map-key="你的高德地图Key"
   amap-security-code="你的安全密钥（可选）"

## 用法示例

// 基础用法 - 只绑定地址字符串
<AFormMap
  amap-key="你的高德地图Key"
  v-model="form.address"
  label="门店地址"
  prop="address"
  :span="12"
/>

// 完整用法 - 绑定地址 + 完整位置对象
<AFormMap
  amap-key="你的高德地图Key"
  amap-security-code="你的安全密钥"
  v-model="form.address"
  v-model:location-data="form.location"
  label="门店位置"
  prop="address"
  :span="12"
/>

// 行政区域校验（推荐传入 area-name）
<AFormMap
  amap-key="你的高德地图Key"
  v-model="form.storeAddress"
  v-model:location-data="form.storeLocation"
  label="门店位置"
  :area-code="form.areaCode"
  area-name="深圳市南山区"
  :strict-validation="true"
  validation-message="门店位置必须在商家所属的行政区域内"
  :span="12"
/>

// 行政区域校验（不传 area-name）- 警告信息中只显示区域代码
<AFormMap
  amap-key="你的高德地图Key"
  v-model="form.storeAddress"
  v-model:location-data="form.storeLocation"
  label="门店位置"
  :area-code="form.areaCode"
  :strict-validation="true"
  :span="12"
/>

// 自定义地图配置
<AFormMap
  amap-key="你的高德地图Key"
  v-model="form.deliveryAddress"
  label="配送地址"
  :default-center="{ lng: 120.123, lat: 30.456 }"
  :default-zoom="16"
  map-style="dark"
  search-city="杭州"
  dialog-title="选择配送位置"
  map-height="600px"
  :span="24"
/>

// 搜索栏用法
<AFormMap
  amap-key="你的高德地图Key"
  v-model="queryParams.address"
  label="门店地址"
  placeholder="输入或选择地址"
/>

// 搜索栏用法（不含form-item）
<AFormMap
  amap-key="你的高德地图Key"
  v-model="queryParams.address"
  :show-form-item="false"
  placeholder="输入或选择地址"
/>

// 带提示信息
<AFormMap
  amap-key="你的高德地图Key"
  v-model="form.address"
  label="取货地址"
  tooltip="请在地图上精确选择取货位置"
  :span="12"
/>

// 自定义宽度和尺寸
<AFormMap
  amap-key="你的高德地图Key"
  v-model="form.address"
  label="地址"
  :width="500"
  size="large"
  :span="24"
/>

// 自定义按钮文字
<AFormMap
  amap-key="你的高德地图Key"
  v-model="form.address"
  label="门店位置"
  button-text="选择门店位置"
  :span="12"
/>
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

      <el-input :model-value="modelValue" :placeholder="placeholder || `${t('placeholder.select')}${computedLabel}`"
                :size="size" :disabled="disabled" :clearable="clearable" :style="{ width: computedWidth }" readonly
                @clear="handleClear">
        <template #suffix>
          <el-button text @click="openMapDialog" :disabled="disabled">
            {{ buttonText }}
          </el-button>
        </template>
      </el-input>
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

    <el-input :model-value="modelValue" :placeholder="placeholder || `${t('placeholder.select')}${computedLabel}`"
              :size="size" :disabled="disabled" :clearable="clearable" :style="{ width: computedWidth }" readonly
              @clear="handleClear">
      <template #suffix>
        <el-button text @click="openMapDialog" :disabled="disabled">
          {{ buttonText }}
        </el-button>
      </template>
    </el-input>
  </el-form-item>

  <!--showFormItem为false 只显示输入框-->
  <template v-else>
    <el-input :model-value="modelValue" :placeholder="placeholder || `${t('placeholder.select')}${computedLabel}`"
              :size="size" :disabled="disabled" :clearable="clearable" :style="{ width: computedWidth }" readonly
              @clear="handleClear">
      <template #suffix>
        <el-button text @click="openMapDialog" :disabled="disabled">
          {{ buttonText }}
        </el-button>
      </template>
    </el-input>
  </template>

  <!-- 地图选点弹窗 -->
  <el-dialog v-model="dialogVisible" :title="dialogTitle" :width="dialogWidth" :close-on-click-modal="false"
             @open="handleOpen" @close="handleClose" destroy-on-close append-to-body :z-index="3000">
    <!-- 搜索框（输入即出候选下拉，点选直接定位；保留搜索按钮做兜底） -->
    <div class="map-search" style="margin-bottom: 15px">
      <el-autocomplete
        v-model="searchKeyword"
        class="amap-suggest-input"
        :fetch-suggestions="fetchSuggestions"
        :placeholder="t('formMap.searchPlaceholder')"
        :debounce="300"
        :trigger-on-focus="false"
        clearable
        value-key="name"
        popper-class="amap-suggest-popper"
        style="width: 100%"
        @select="handleSuggestionSelect"
        @keyup.enter="handleSearch"
      >
        <template #append>
          <el-button icon="Search" @click="handleSearch">{{ t('formMap.searchButton') }}</el-button>
        </template>
        <template #default="{ item }">
          <div class="amap-suggest-item">
            <div class="amap-suggest-name">{{ item.name }}</div>
            <div class="amap-suggest-addr">{{ item.address }}</div>
          </div>
        </template>
      </el-autocomplete>
      <el-alert v-if="areaCode" :title="t('formMap.areaHint', { code: areaCode })" type="info" style="margin-top: 10px"
                :closable="false" />
    </div>

    <!-- 地图容器 -->
    <div id="amap-container" v-loading="mapLoading" :element-loading-text="t('formMap.mapLoading')"
         :style="{ height: mapHeight, margin: '20px 0', border: '1px solid #dcdfe6', borderRadius: '4px' }"></div>

    <!-- 行政区域校验警告 -->
    <el-alert v-if="showWarning && validationWarning" type="warning" style="margin: 10px 0" show-icon :closable="false"
              effect="dark">{{ validationWarning }}
    </el-alert>

    <!-- 位置信息面板 -->
    <el-descriptions :column="2" border size="default" v-if="currentLocation.longitude">
      <el-descriptions-item :label="t('formMap.longitude')" label-align="right">
        <el-tag>{{ currentLocation.longitude }}</el-tag>
      </el-descriptions-item>
      <el-descriptions-item :label="t('formMap.latitude')" label-align="right">
        <el-tag>{{ currentLocation.latitude }}</el-tag>
      </el-descriptions-item>
      <el-descriptions-item :label="t('formMap.province')" label-align="right">
        {{ currentLocation.province || '-' }}
      </el-descriptions-item>
      <el-descriptions-item :label="t('formMap.city')" label-align="right">
        {{ currentLocation.city || '-' }}
      </el-descriptions-item>
      <el-descriptions-item :label="t('formMap.district')" label-align="right">
        {{ currentLocation.district || '-' }}
      </el-descriptions-item>
      <el-descriptions-item :label="t('formMap.adcode')" label-align="right">
        <el-tag :type="isValidArea ? 'success' : 'danger'">{{ currentLocation.adcode || '-' }}</el-tag>
      </el-descriptions-item>
      <el-descriptions-item :label="t('formMap.detailAddress')" :span="2" label-align="right">
        {{ currentLocation.address || '-' }}
      </el-descriptions-item>
    </el-descriptions>
    <el-empty v-else :description="t('formMap.emptyTip')" :image-size="100" />

    <template #footer>
      <el-button @click="handleClose">{{ t('button.cancel') }}</el-button>
      <el-button type="primary" :disabled="!currentLocation.longitude || (strictValidation && !isValidArea)"
                 @click="handleConfirm">
        {{ t('formMap.confirmButton') }}
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts" name="AFormMap">
import { showMsgError, showMsgWarning, showMsgSuccess } from '@/utils/modal'

const { t, te, isChinese } = useI18n()

/**
 * 位置信息数据结构
 */
export interface LocationData {
  /** 经度 */
  longitude: string
  /** 纬度 */
  latitude: string
  /** 详细地址 */
  address: string
  /** 省份 */
  province: string
  /** 城市 */
  city: string
  /** 区县 */
  district: string
  /** 行政区划代码 */
  adcode: string
}

/**
 * 地图组件的Props接口定义
 */
interface AFormMapProps {

  // ========== 地图配置属性（有默认值，可覆盖）==========
  /**
   * 高德地图 Key（必填）
   * 获取地址：https://lbs.amap.com/
   * @example ''
   */
  amapKey?: string

  /**
   * 高德地图安全密钥（可选，但推荐配置）
   * @example ''
   */
  amapSecurityCode?: string
  // ========== 表单基础属性 ==========
  /**
   * v-model 绑定值：地址字符串
   * @example "北京市东城区天安门广场"
   */
  modelValue?: string

  /**
   * 完整位置信息对象绑定（可选）
   * 选点确认后，完整信息会更新到此对象
   */
  locationData?: LocationData

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
   * 占位符文本
   * @default ''
   */
  placeholder?: string

  /**
   * 表单域model数据字段名
   * @default ''
   */
  prop?: string

  /**
   * 是否显示表单项
   * @default true
   */
  showFormItem?: boolean

  /**
   * 是否禁用
   * @default false
   */
  disabled?: boolean

  /**
   * 是否显示清除按钮
   * @default true
   */
  clearable?: boolean

  /**
   * 组件尺寸
   * @default ''
   */
  size?: '' | 'default' | 'small' | 'large'

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
   * 提示信息
   * @default ''
   */
  tooltip?: string

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

  /**
   * 输入框宽度，支持数字或字符串
   * - 数字：自动添加 px 单位，如 300 -> '300px'
   * - 字符串：直接使用，如 '100%', '20rem'
   * @default undefined - 使用默认宽度
   */
  width?: number | string

  /**
   * 选择位置按钮文字
   * @default '选择位置'
   */
  buttonText?: string

  /**
   * API 版本
   * @default '2.0'
   */
  mapVersion?: string

  /**
   * 默认地图中心点（经纬度）
   * @default { lng: 116.397428, lat: 39.90923 } // 北京天安门
   */
  defaultCenter?: {
    lng: number
    lat: number
  }

  /**
   * 默认缩放级别 (3-18)
   * @default 15
   */
  defaultZoom?: number

  /**
   * 地图样式
   * @default 'normal'
   */
  mapStyle?: 'normal' | 'dark' | 'light' | 'whitesmoke' | 'fresh'

  /**
   * 是否显示3D楼块
   * @default true
   */
  showBuildingBlock?: boolean

  /**
   * 搜索默认城市
   * @default '全国'
   */
  searchCity?: string

  /**
   * 搜索结果每页数量
   * @default 10
   */
  searchPageSize?: number

  /**
   * 弹窗标题
   * @default '选择位置'
   */
  dialogTitle?: string

  /**
   * 弹窗宽度
   * @default '60%'
   */
  dialogWidth?: string

  /**
   * 地图容器高度
   * @default '500px'
   */
  mapHeight?: string

  // ========== 行政区域校验配置 ==========
  /**
   * 行政区域代码（用于校验）
   * @example '440305' // 深圳南山区
   */
  areaCode?: string

  /**
   * 行政区域名称（用于显示，推荐传入）
   * 传入后，校验警告信息会显示区域名称，用户体验更好
   * 如果不传入，警告信息中只显示区域代码
   * @example '深圳市南山区'
   */
  areaName?: string

  /**
   * 是否启用行政区域校验
   * @default true
   */
  enableValidation?: boolean

  /**
   * 严格模式：校验不通过时禁用确认按钮
   * @default false
   */
  strictValidation?: boolean

  /**
   * 校验失败提示文字
   * @default '所选位置不在指定行政区域内，请重新选择！'
   */
  validationMessage?: string

  /**
   * 校验模式
   * - strict: 严格模式，必须完全匹配
   * - prefix: 前缀模式，允许下级行政区域（推荐）
   * - city: 城市模式，只校验到市级
   * @default 'prefix'
   */
  validateMode?: 'strict' | 'prefix' | 'city'

  /**
   * 是否显示校验警告
   * @default true
   */
  showWarning?: boolean
}

// 使用 withDefaults 定义 props，提供默认值
const props = withDefaults(defineProps<AFormMapProps>(), {

  // 地图配置默认值
  amapKey: '',
  amapSecurityCode: '',
  mapVersion: '2.0',
  defaultCenter: () => ({ lng: 116.397428, lat: 39.90923 }), // 北京天安门
  defaultZoom: 15,
  mapStyle: 'normal',
  showBuildingBlock: true,
  searchCity: '全国',
  searchPageSize: 10,
  dialogTitle: '', // 默认为空，使用 computedDialogTitle
  dialogWidth: '60%',
  mapHeight: '500px',

  // 表单基础属性默认值
  modelValue: '',
  locationData: undefined,
  label: '',
  labelWidth: undefined,
  placeholder: '',
  prop: '',
  showFormItem: true,
  disabled: false,
  clearable: true,
  size: '',
  span: undefined,
  tooltip: '',
  modalSize: undefined,
  width: undefined,
  buttonText: '', // 默认为空，使用 computedButtonText

  // 行政区域校验默认值
  areaCode: '',
  areaName: '',
  enableValidation: true,
  strictValidation: false,
  validationMessage: '', // 默认为空，使用 computedValidationMessage
  validateMode: 'prefix',
  showWarning: true
})

const emit = defineEmits<{
  'update:modelValue': [value: string]
  'update:locationData': [value: LocationData | undefined]
  'change': [value: string]
  'clear': []
  'confirm': [value: LocationData]
}>()

// ========== 使用智能响应式逻辑 ==========
const { computedSpan, shouldUseCol } = useResponsiveSpan(toRef(props, 'span'), {
  mode: props.responsiveMode,
  modalSize: toRef(props, 'modalSize')
})

/**
 * 计算属性：处理宽度值
 * 支持数字（自动添加px）和字符串（直接使用）
 */
const computedWidth = computed(() => {
  if (props.width === undefined) {
    return undefined
  }
  return typeof props.width === 'number' ? `${props.width}px` : props.width
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

// 弹窗标题（支持国际化）
const dialogTitle = computed(() => {
  return props.dialogTitle || t('formMap.dialogTitle')
})

// 按钮文字（支持国际化）
const buttonText = computed(() => {
  return props.buttonText || t('formMap.buttonText')
})

// 校验失败提示文字（支持国际化）
const computedValidationMessage = computed(() => {
  return props.validationMessage || t('formMap.validationMessage')
})

// ========== 地图相关状态 ==========
const dialogVisible = ref(false)
const mapLoading = ref(true)
const searchKeyword = ref('')
const currentLocation = ref<Partial<LocationData>>({})
const isValidArea = ref(true) // 是否在有效区域内
const validationWarning = ref('') // 校验警告信息

// 地图实例（组件级变量，每个组件独立）
let map: any = null
let marker: any = null
let geocoder: any = null
// POI 搜索实例：弹窗级生命周期，搜索建议下拉与"搜索"按钮共用同一实例，避免重复创建
let placeSearch: any = null

/**
 * 组件内部缓存最后一次选择的位置（用于回显）
 * 即使用户没有绑定 v-model:location-data，也能回显上次选择
 */
let lastSelectedLocation: Partial<LocationData> = {}

/**
 * 高德地图全局加载管理（模块级变量）
 * 所有组件实例共享 AMap 对象，只加载一次
 */
let AMap: any = null // AMap 全局对象缓存
let isAmapLoading = false // 是否正在加载中
let isAmapLoaded = false // 是否已加载完成
let loadCallbacks: Array<(amap: any) => void> = [] // 等待加载完成的回调队列
let errorCallbacks: Array<(error: Error) => void> = [] // 等待加载失败的回调队列

/**
 * 检查高德地图配置
 * @returns 配置是否有效
 */
const checkAmapConfig = (): boolean => {
  if (!props.amapKey) {
    console.warn('⚠️ 高德地图未配置')
    console.warn('请在组件配置 amap-key 属性：')
    console.warn('')
    console.warn('获取密钥步骤：')
    console.warn('  1. 访问 https://lbs.amap.com/')
    console.warn('  2. 注册并登录账号')
    console.warn('  3. 创建应用，选择 Web端(JSAPI) 类型')
    console.warn('  4. 获取 Key 和安全密钥')
    console.warn('')
    console.warn('可选配置安全密钥：')
    console.warn('  <AFormMap amap-key="..." amap-security-code="..." />')

    showMsgError(t('formMap.mapNotConfigured'))
    return false
  }
  return true
}

/**
 * 动态加载高德地图 API（使用 @amap/amap-jsapi-loader）
 * 所有组件实例共享 AMap 对象，只加载一次
 * @returns Promise<any> AMap 实例
 */
const loadAmapScript = async (): Promise<any> => {
  // 1. 检查配置
  if (!checkAmapConfig()) {
    throw new Error('高德地图配置缺失')
  }

  // 2. 如果已经加载成功，直接返回
  if (isAmapLoaded && AMap) {
    console.log('📦 高德地图 API 已加载，直接使用')
    return AMap
  }

  // 3. 如果正在加载中，加入回调队列等待
  if (isAmapLoading) {
    console.log('⏳ 高德地图 API 正在加载中，加入等待队列...')
    return new Promise((resolve, reject) => {
      loadCallbacks.push(resolve)
      errorCallbacks.push(reject)
    })
  }

  // 4. 开始新的加载流程
  isAmapLoading = true
  console.log('🔄 开始加载高德地图 API...')
  // console.log(`📍 Key: ${props.amapKey.substring(0, 10)}...`)
  console.log(`📍 Version: ${props.mapVersion}`)

  try {
    // 5. 动态导入 @amap/amap-jsapi-loader
    const AMapLoader = (await import('@amap/amap-jsapi-loader')).default

    // 6. 设置安全密钥（必须在 load 之前设置）
    if (props.amapSecurityCode) {
      ; (window as any)._AMapSecurityConfig = {
        securityJsCode: props.amapSecurityCode
      }
      console.log('🔐 安全密钥已设置')
    }

    // 7. 使用 AMapLoader 加载
    AMap = await AMapLoader.load({
      key: props.amapKey,
      version: props.mapVersion,
      plugins: ['AMap.Geocoder', 'AMap.PlaceSearch', 'AMap.AutoComplete']
    })

    isAmapLoaded = true
    isAmapLoading = false

    console.log('✅ 高德地图 API 加载成功')
    console.log(`📦 AMap 版本: ${AMap?.version || '未知'}`)

    // 8. 执行所有等待队列中的回调
    loadCallbacks.forEach((callback) => callback(AMap))
    loadCallbacks = []
    errorCallbacks = []

    return AMap
  } catch (error) {
    isAmapLoading = false

    console.error('❌ 高德地图 API 加载失败:', error)
    console.error('请检查：')
    console.error('  1. 网络连接是否正常')
    console.error('  2. amap-key 是否正确')
    console.error('  3. Key 是否已过期或被限制')
    console.error('  4. @amap/amap-jsapi-loader 依赖是否已安装')

    showMsgError(t('formMap.mapLoadFailed'))

    // 9. 执行所有等待队列中的错误回调
    errorCallbacks.forEach((callback) => callback(error as Error))
    loadCallbacks = []
    errorCallbacks = []

    throw error
  }
}

// 注：由于高德地图 API 的 DistrictSearch 不支持直接通过 adcode 查询区域名称，
// 建议用户在使用组件时直接传入 area-name 属性以获得最佳用户体验

// 监听 locationData 变化，同步到 currentLocation
watch(
  () => props.locationData,
  (newVal) => {
    if (newVal) {
      currentLocation.value = { ...newVal }
    }
  },
  { immediate: true, deep: true }
)

/**
 * 打开地图选点弹窗
 */
const openMapDialog = () => {
  if (props.disabled) return

  // ✅ 优先使用 props.locationData（用户绑定了 v-model:location-data）
  if (props.locationData) {
    currentLocation.value = { ...props.locationData }
  }
  // ✅ 其次使用 lastSelectedLocation（用户没绑定，但之前选择过）
  else if (lastSelectedLocation.longitude) {
    currentLocation.value = { ...lastSelectedLocation }
  }

  dialogVisible.value = true
}

/**
 * 校验行政区域
 * @param selectedAdcode 选择的位置的行政区划代码
 * @param locationName 位置名称（省市区）
 * @returns 是否通过校验
 */
const validateAreaCode = (selectedAdcode: string, locationName: string = ''): boolean => {
  // 如果未启用校验，直接返回true
  if (!props.enableValidation || !props.areaCode) {
    isValidArea.value = true
    validationWarning.value = ''
    return true
  }

  let isValid = false

  // 根据 validateMode 进行不同校验
  switch (props.validateMode) {
    case 'strict':
      // 严格模式：必须完全匹配
      isValid = selectedAdcode === props.areaCode
      break
    case 'prefix':
      // 前缀模式：允许下级行政区域
      isValid = selectedAdcode.startsWith(props.areaCode)
      break
    case 'city':
      // 城市模式：只校验到市级（前4位）
      isValid = selectedAdcode.substring(0, 4) === props.areaCode.substring(0, 4)
      break
    default:
      isValid = true
  }

  isValidArea.value = isValid

  if (!isValid) {
    // 构建详细的警告信息
    const warningLines: string[] = []

    // 第一行：主要提示信息
    warningLines.push(`⚠️ ${computedValidationMessage.value}`)

    // 第二行：要求区域
    if (props.areaName) {
      // 如果用户传入了 areaName，显示完整信息
      warningLines.push(`• ${t('formMap.requiredLocation', { name: props.areaName, code: props.areaCode })}`)
    } else {
      // 如果没有传入 areaName，只显示代码
      warningLines.push(`• ${t('formMap.requiredAreaCode', { code: props.areaCode })}`)
    }

    // 第三行：当前位置
    if (locationName) {
      warningLines.push(`• ${t('formMap.currentLocation', { name: locationName, code: selectedAdcode })}`)
    } else {
      warningLines.push(`• ${t('formMap.currentAreaCode', { code: selectedAdcode })}`)
    }

    validationWarning.value = warningLines.join('\n')
  } else {
    validationWarning.value = ''
  }

  return isValid
}

/**
 * 初始化地图
 */
const initMap = async () => {
  try {
    mapLoading.value = true

    // 动态加载高德地图 API
    const AMap = await loadAmapScript()
    if (!AMap) {
      mapLoading.value = false
      return
    }

    // ✅ 确定初始中心点（优先级：props.locationData > lastSelectedLocation > defaultCenter）
    let center = [props.defaultCenter.lng, props.defaultCenter.lat]
    if (props.locationData?.longitude && props.locationData?.latitude) {
      center = [Number(props.locationData.longitude), Number(props.locationData.latitude)]
    } else if (lastSelectedLocation.longitude && lastSelectedLocation.latitude) {
      center = [Number(lastSelectedLocation.longitude), Number(lastSelectedLocation.latitude)]
    }

    // 创建地图实例
    map = new AMap.Map('amap-container', {
      zoom: props.defaultZoom,
      center: center,
      resizeEnable: true,
      mapStyle: `amap://styles/${props.mapStyle}`,
      showBuildingBlock: props.showBuildingBlock
    })

    // 创建标记点
    marker = new AMap.Marker({
      position: center,
      draggable: true,
      cursor: 'move',
      raiseOnDrag: true,
      animation: 'AMAP_ANIMATION_DROP'
    })

    // 监听标记点拖拽结束事件
    marker.on('dragend', (e: any) => {
      const { lng, lat } = e.lnglat
      updateLocation(lng, lat)
    })

    map.add(marker)

    // 监听地图点击事件
    map.on('click', (e: any) => {
      const { lng, lat } = e.lnglat
      updateLocation(lng, lat)
    })

    // 同时加载地理编码 + POI 搜索插件，初始化两个实例（搜索建议与搜索按钮复用 placeSearch）
    ;(window as any).AMap.plugin(['AMap.Geocoder', 'AMap.PlaceSearch'], () => {
      geocoder = new (window as any).AMap.Geocoder({
        city: props.searchCity
      })
      placeSearch = new (window as any).AMap.PlaceSearch({
        city: props.searchCity,
        pageSize: props.searchPageSize
      })

      // ✅ 插件加载完成后，初始化位置数据（回显上次选择）
      // 优先级：props.locationData > lastSelectedLocation > currentLocation
      if (props.locationData?.longitude && props.locationData?.latitude) {
        updateLocation(Number(props.locationData.longitude), Number(props.locationData.latitude))
      } else if (lastSelectedLocation.longitude && lastSelectedLocation.latitude) {
        updateLocation(Number(lastSelectedLocation.longitude), Number(lastSelectedLocation.latitude))
      } else if (currentLocation.value?.longitude && currentLocation.value?.latitude) {
        updateLocation(Number(currentLocation.value.longitude), Number(currentLocation.value.latitude))
      }
    })

    mapLoading.value = false
  } catch (error) {
    console.error('地图初始化失败:', error)
    // showMsgError('地图加载失败，请刷新页面重试')
    mapLoading.value = false
  }
}

/**
 * 更新位置信息（逆地理编码）
 */
const updateLocation = (lng: number, lat: number) => {
  if (!geocoder) {
    showMsgWarning(t('formMap.geocoderNotInit'))
    return
  }

  // 更新标记点位置
  marker.setPosition([lng, lat])
  map.setCenter([lng, lat])

  // 显示加载状态
  mapLoading.value = true

  // 逆地理编码
  geocoder.getAddress([lng, lat], (status: string, result: any) => {
    mapLoading.value = false

    if (status === 'complete' && result.info === 'OK') {
      const regeocode = result.regeocode
      const addressComponent = regeocode.addressComponent

      currentLocation.value = {
        longitude: lng.toFixed(6),
        latitude: lat.toFixed(6),
        address: regeocode.formattedAddress,
        province: addressComponent.province || '',
        city: addressComponent.city || addressComponent.province || '',
        district: addressComponent.district || '',
        adcode: addressComponent.adcode || ''
      }

      // 进行行政区域校验，传入位置名称用于显示
      const locationName = `${addressComponent.province}${addressComponent.city}${addressComponent.district}`
      validateAreaCode(addressComponent.adcode || '', locationName)
    } else {
      showMsgError(t('formMap.getAddressFailed'))
      console.error('逆地理编码失败:', result)
    }
  })
}

/**
 * 把高德 POI 转为下拉项
 * 过滤掉无坐标的条目（如纯行政区类提示），避免点选后无法定位
 */
const mapPoiToSuggestion = (poi: any) => ({
  name: poi.name,
  address: `${poi.pname || ''}${poi.cityname || ''}${poi.adname || ''}${poi.address || ''}`,
  location: poi.location
})

/**
 * el-autocomplete 的建议数据源
 * 复用弹窗级 placeSearch 实例（避免每次输入都新建），自带 300ms 防抖足以扛住 QPS
 */
const fetchSuggestions = (queryString: string, cb: (results: any[]) => void) => {
  if (!queryString || !queryString.trim() || !placeSearch) {
    cb([])
    return
  }
  placeSearch.search(queryString, (status: string, result: any) => {
    if (status === 'complete' && result.info === 'OK' && result.poiList?.pois?.length) {
      const items = result.poiList.pois
        .filter((p: any) => p.location && typeof p.location.lng === 'number')
        .map(mapPoiToSuggestion)
      cb(items)
    } else {
      cb([])
    }
  })
}

/**
 * 选中下拉候选项 → 直接更新地图位置，无需再调用 PlaceSearch
 */
const handleSuggestionSelect = (item: any) => {
  if (item?.location) {
    updateLocation(item.location.lng, item.location.lat)
  }
}

/**
 * 地点搜索（兜底）：用户没点选下拉项而直接按按钮/回车时，取第一条结果定位
 */
const handleSearch = () => {
  if (!searchKeyword.value.trim()) {
    showMsgWarning(t('formMap.searchKeywordRequired'))
    return
  }
  if (!placeSearch) {
    showMsgWarning(t('formMap.geocoderNotInit'))
    return
  }

  mapLoading.value = true
  placeSearch.search(searchKeyword.value, (status: string, result: any) => {
    mapLoading.value = false
    if (status === 'complete' && result.info === 'OK' && result.poiList?.pois?.length) {
      const poi = result.poiList.pois[0]
      const { lng, lat } = poi.location
      updateLocation(lng, lat)
      showMsgSuccess(t('formMap.searchLocated', { name: poi.name }))
    } else {
      showMsgWarning(t('formMap.searchNoResult'))
    }
  })
}

/**
 * 对话框打开时初始化地图
 */
const handleOpen = () => {
  // 使用 setTimeout 确保 DOM 已渲染
  setTimeout(() => {
    initMap()
  }, 100)
}

/**
 * 关闭对话框
 */
const handleClose = () => {
  dialogVisible.value = false
  searchKeyword.value = ''

  // 恢复为原始的 locationData（取消时不保存更改）
  if (props.locationData) {
    currentLocation.value = { ...props.locationData }
  } else {
    currentLocation.value = {}
  }

  // 重置校验状态
  isValidArea.value = true
  validationWarning.value = ''

  // 销毁地图实例
  if (map) {
    map.destroy()
    map = null
  }
  marker = null
  geocoder = null
  placeSearch = null
}

/**
 * 确认选择
 */
const handleConfirm = () => {
  if (!currentLocation.value.longitude || !currentLocation.value.latitude) {
    showMsgWarning(t('formMap.selectFirst'))
    return
  }

  // 严格模式下，如果校验不通过则阻止确认
  if (props.strictValidation && !isValidArea.value) {
    return
  }

  const locationData = currentLocation.value as LocationData

  // ✅ 保存到组件内部缓存（用于下次回显，即使用户没绑定 location-data）
  lastSelectedLocation = { ...locationData }

  // 1. 更新 v-model（地址字符串）
  emit('update:modelValue', locationData.address)

  // 2. 更新完整位置对象（无论是否绑定都 emit）
  emit('update:locationData', locationData)

  // 3. 触发事件
  emit('change', locationData.address)
  emit('confirm', locationData)

  // 4. 关闭弹窗
  handleClose()
}

/**
 * 清空处理函数
 */
const handleClear = () => {
  // 1. 清空地址
  emit('update:modelValue', '')

  // 2. 清空 locationData
  emit('update:locationData', undefined)

  // 3. 清空当前位置
  currentLocation.value = {}

  // 4. ✅ 清空内部缓存
  lastSelectedLocation = {}

  // 5. 触发清空事件
  emit('clear')
}

/**
 * 组件挂载时的生命周期
 * 注：地图在弹窗打开时才初始化，这里无需操作
 */
onMounted(() => {
  // 无需操作，地图在 handleOpen 中初始化
})

/**
 * 组件销毁时的生命周期
 * 注：地图实例在弹窗关闭时已销毁，这里只做保险检查
 */
onUnmounted(() => {
  // 保险起见，检查并清理地图实例
  // 通常在 handleClose 中已经清理过了
  if (map) {
    map.destroy()
    map = null
  }
  marker = null
  geocoder = null
  placeSearch = null

  // 注：AMap 全局对象无需清理
  // 因为它是所有组件实例共享的，永久有效
})
</script>

<style scoped>
.map-search :deep(.el-input-group__append) {
  padding: 0;
}

.map-search :deep(.el-input-group__append .el-button) {
  margin: 0;
}
</style>

<style>
/* el-autocomplete 的 popper 通过 teleport 渲染到 body，scoped 样式无法作用，故用 popper-class 做命名空间隔离 */
/* z-index 必须高于宿主 el-dialog（本组件 dialog 设了 3000），否则下拉会被弹窗遮挡看不见 */
.amap-suggest-popper {
  z-index: 3100 !important;
}

.amap-suggest-popper .amap-suggest-item {
  padding: 4px 0;
  line-height: 1.4;
}

.amap-suggest-popper .amap-suggest-name {
  font-size: 14px;
  color: var(--el-text-color-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.amap-suggest-popper .amap-suggest-addr {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
