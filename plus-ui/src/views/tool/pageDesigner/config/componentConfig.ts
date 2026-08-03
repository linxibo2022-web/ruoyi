/**
 * 页面设计器组件配置
 */
import type { ComponentConfig, FormItemType, OptionItem } from '../types'

/** 输入框类型选项 */
const inputTypeOptions: OptionItem[] = [
  { label: '文本', value: 'text' },
  { label: '密码', value: 'password' }
]

/** 日期类型选项 */
const dateTypeOptions: OptionItem[] = [
  { label: '日期', value: 'date' },
  { label: '日期时间', value: 'datetime' },
  { label: '日期范围', value: 'daterange' },
  { label: '日期时间范围', value: 'datetimerange' },
  { label: '月份', value: 'month' },
  { label: '年份', value: 'year' },
  { label: '时间', value: 'time' }
]

/** 尺寸选项 */
const sizeOptions: OptionItem[] = [
  { label: '默认', value: 'default' },
  { label: '小', value: 'small' },
  { label: '大', value: 'large' }
]

/** span选项 */
const spanOptions: OptionItem[] = [
  { label: '自动', value: 'auto' },
  { label: '1/4 (6列)', value: 6 },
  { label: '1/3 (8列)', value: 8 },
  { label: '1/2 (12列)', value: 12 },
  { label: '2/3 (16列)', value: 16 },
  { label: '3/4 (18列)', value: 18 },
  { label: '整行 (24列)', value: 24 }
]

/** 组件配置列表 */
export const componentList: ComponentConfig[] = [
  // ==================== 基础组件 ====================
  {
    type: 'input',
    name: '单行输入',
    icon: 'input',
    category: '基础组件',
    defaultProps: {
      maxlength: 255,
      clearable: true,
      showWordLimit: false
    },
    propsConfig: [
      { prop: 'placeholder', label: '占位符', type: 'input' },
      { prop: 'prefixIcon', label: '前缀图标', type: 'icon-select' },
      { prop: 'suffixIcon', label: '后缀图标', type: 'icon-select' },
      { prop: 'maxlength', label: '最大长度', type: 'number', min: 1, max: 9999 },
      { prop: 'clearable', label: '可清空', type: 'switch', defaultValue: true },
      { prop: 'showWordLimit', label: '字数统计', type: 'switch', defaultValue: false },
      { prop: 'disabled', label: '禁用', type: 'switch', defaultValue: false }
    ]
  },
  {
    type: 'textarea',
    name: '多行文本',
    icon: 'textarea',
    category: '基础组件',
    defaultProps: {
      maxlength: 500,
      rows: 3,
      showWordLimit: true
    },
    propsConfig: [
      { prop: 'placeholder', label: '占位符', type: 'input' },
      { prop: 'rows', label: '行数', type: 'number', min: 2, max: 20, defaultValue: 3 },
      { prop: 'maxlength', label: '最大长度', type: 'number', min: 1, max: 9999 },
      { prop: 'showWordLimit', label: '字数统计', type: 'switch', defaultValue: true },
      { prop: 'disabled', label: '禁用', type: 'switch', defaultValue: false }
    ]
  },
  {
    type: 'password',
    name: '密码输入',
    icon: 'password',
    category: '基础组件',
    defaultProps: {
      maxlength: 128,
      showPassword: true,
      clearable: true
    },
    propsConfig: [
      { prop: 'placeholder', label: '占位符', type: 'input' },
      { prop: 'maxlength', label: '最大长度', type: 'number', min: 1, max: 128 },
      { prop: 'showPassword', label: '显示密码', type: 'switch', defaultValue: true },
      { prop: 'clearable', label: '可清空', type: 'switch', defaultValue: true },
      { prop: 'disabled', label: '禁用', type: 'switch', defaultValue: false }
    ]
  },
  {
    type: 'number',
    name: '数字输入',
    icon: 'number',
    category: '基础组件',
    defaultProps: {
      controls: true,
      controlsPosition: ''
    },
    propsConfig: [
      { prop: 'placeholder', label: '占位符', type: 'input' },
      { prop: 'min', label: '最小值', type: 'number' },
      { prop: 'max', label: '最大值', type: 'number' },
      { prop: 'step', label: '步长', type: 'number', min: 0.01, defaultValue: 1 },
      { prop: 'precision', label: '精度', type: 'number', min: 0, max: 10 },
      { prop: 'controls', label: '控制按钮', type: 'switch', defaultValue: true },
      {
        prop: 'controlsPosition',
        label: '按钮位置',
        type: 'select',
        options: [
          { label: '两侧', value: '' },
          { label: '右侧', value: 'right' }
        ],
        defaultValue: ''
      },
      { prop: 'disabled', label: '禁用', type: 'switch', defaultValue: false }
    ]
  },

  // ==================== 选择组件 ====================
  {
    type: 'select',
    name: '下拉选择',
    icon: 'select',
    category: '选择组件',
    defaultProps: {
      clearable: true,
      filterable: false,
      multiple: false
    },
    propsConfig: [
      { prop: 'placeholder', label: '占位符', type: 'input' },
      { prop: 'dictType', label: '字典类型', type: 'dict-select' },
      {
        prop: 'options',
        label: '选项配置',
        type: 'options-editor',
        showCondition: (item) => !item.props?.dictType
      },
      { prop: 'multiple', label: '多选', type: 'switch', defaultValue: false },
      { prop: 'filterable', label: '可筛选', type: 'switch', defaultValue: false },
      { prop: 'clearable', label: '可清空', type: 'switch', defaultValue: true },
      {
        prop: 'multipleLimit',
        label: '多选数量限制',
        type: 'number',
        min: 0,
        showCondition: (item) => item.props?.multiple === true
      },
      { prop: 'disabled', label: '禁用', type: 'switch', defaultValue: false }
    ]
  },
  {
    type: 'radio',
    name: '单选框',
    icon: 'selected',
    category: '选择组件',
    defaultProps: {
      type: 'radio'
    },
    propsConfig: [
      { prop: 'dictType', label: '字典类型', type: 'dict-select' },
      {
        prop: 'options',
        label: '选项配置',
        type: 'options-editor',
        showCondition: (item) => !item.props?.dictType
      },
      {
        prop: 'type',
        label: '显示风格',
        type: 'select',
        options: [
          { label: '单选框', value: 'radio' },
          { label: '按钮', value: 'button' }
        ],
        defaultValue: 'radio'
      },
      { prop: 'border', label: '显示边框', type: 'switch', defaultValue: false },
      { prop: 'disabled', label: '禁用', type: 'switch', defaultValue: false }
    ]
  },
  {
    type: 'checkbox',
    name: '复选框',
    icon: 'checkbox',
    category: '选择组件',
    defaultProps: {
      type: 'checkbox'
    },
    propsConfig: [
      { prop: 'dictType', label: '字典类型', type: 'dict-select' },
      {
        prop: 'options',
        label: '选项配置',
        type: 'options-editor',
        showCondition: (item) => !item.props?.dictType
      },
      {
        prop: 'type',
        label: '显示风格',
        type: 'select',
        options: [
          { label: '复选框', value: 'checkbox' },
          { label: '按钮', value: 'button' }
        ],
        defaultValue: 'checkbox'
      },
      { prop: 'border', label: '显示边框', type: 'switch', defaultValue: false },
      { prop: 'min', label: '最少选择', type: 'number', min: 0 },
      { prop: 'max', label: '最多选择', type: 'number', min: 0 },
      { prop: 'disabled', label: '禁用', type: 'switch', defaultValue: false }
    ]
  },
  {
    type: 'switch',
    name: '开关',
    icon: 'switch',
    category: '选择组件',
    defaultProps: {
      activeValue: true,
      inactiveValue: false
    },
    propsConfig: [
      { prop: 'activeText', label: '开启文本', type: 'input' },
      { prop: 'inactiveText', label: '关闭文本', type: 'input' },
      {
        prop: 'valueType',
        label: '值类型',
        type: 'select',
        options: [
          { label: '布尔值', value: 'boolean' },
          { label: '字符串', value: 'string' },
          { label: '数字', value: 'number' }
        ],
        defaultValue: 'boolean'
      },
      { prop: 'disabled', label: '禁用', type: 'switch', defaultValue: false }
    ]
  },
  {
    type: 'cascader',
    name: '级联选择',
    icon: 'cascader',
    category: '选择组件',
    defaultProps: {
      clearable: true,
      filterable: false,
      showAllLevels: true
    },
    propsConfig: [
      { prop: 'placeholder', label: '占位符', type: 'input' },
      { prop: 'options', label: '选项配置', type: 'options-editor' },
      { prop: 'filterable', label: '可筛选', type: 'switch', defaultValue: false },
      { prop: 'clearable', label: '可清空', type: 'switch', defaultValue: true },
      { prop: 'showAllLevels', label: '显示完整路径', type: 'switch', defaultValue: true },
      { prop: 'disabled', label: '禁用', type: 'switch', defaultValue: false }
    ]
  },
  {
    type: 'treeSelect',
    name: '树形选择',
    icon: 'tree',
    category: '选择组件',
    defaultProps: {
      clearable: true,
      filterable: false,
      checkStrictly: false
    },
    propsConfig: [
      { prop: 'placeholder', label: '占位符', type: 'input' },
      { prop: 'multiple', label: '多选', type: 'switch', defaultValue: false },
      { prop: 'filterable', label: '可筛选', type: 'switch', defaultValue: false },
      { prop: 'clearable', label: '可清空', type: 'switch', defaultValue: true },
      { prop: 'checkStrictly', label: '父子不关联', type: 'switch', defaultValue: false },
      { prop: 'disabled', label: '禁用', type: 'switch', defaultValue: false }
    ]
  },

  // ==================== 日期组件 ====================
  {
    type: 'date',
    name: '日期选择',
    icon: 'date',
    category: '日期组件',
    defaultProps: {
      type: 'date',
      format: 'YYYY-MM-DD',
      valueFormat: 'YYYY-MM-DD',
      clearable: true
    },
    propsConfig: [
      { prop: 'placeholder', label: '占位符', type: 'input' },
      { prop: 'format', label: '显示格式', type: 'input', defaultValue: 'YYYY-MM-DD' },
      { prop: 'valueFormat', label: '值格式', type: 'input', defaultValue: 'YYYY-MM-DD' },
      { prop: 'clearable', label: '可清空', type: 'switch', defaultValue: true },
      { prop: 'disabled', label: '禁用', type: 'switch', defaultValue: false }
    ]
  },
  {
    type: 'datetime',
    name: '日期时间',
    icon: 'time',
    category: '日期组件',
    defaultProps: {
      type: 'datetime',
      format: 'YYYY-MM-DD HH:mm:ss',
      valueFormat: 'YYYY-MM-DD HH:mm:ss',
      clearable: true
    },
    propsConfig: [
      { prop: 'placeholder', label: '占位符', type: 'input' },
      { prop: 'format', label: '显示格式', type: 'input', defaultValue: 'YYYY-MM-DD HH:mm:ss' },
      { prop: 'valueFormat', label: '值格式', type: 'input', defaultValue: 'YYYY-MM-DD HH:mm:ss' },
      { prop: 'clearable', label: '可清空', type: 'switch', defaultValue: true },
      { prop: 'disabled', label: '禁用', type: 'switch', defaultValue: false }
    ]
  },
  {
    type: 'daterange',
    name: '日期范围',
    icon: 'date-range',
    category: '日期组件',
    defaultProps: {
      type: 'daterange',
      format: 'YYYY-MM-DD',
      valueFormat: 'YYYY-MM-DD',
      rangeSeparator: ' - ',
      clearable: true
    },
    propsConfig: [
      { prop: 'startPlaceholder', label: '开始占位符', type: 'input', defaultValue: '开始日期' },
      { prop: 'endPlaceholder', label: '结束占位符', type: 'input', defaultValue: '结束日期' },
      { prop: 'rangeSeparator', label: '分隔符', type: 'input', defaultValue: ' - ' },
      { prop: 'format', label: '显示格式', type: 'input', defaultValue: 'YYYY-MM-DD' },
      { prop: 'valueFormat', label: '值格式', type: 'input', defaultValue: 'YYYY-MM-DD' },
      { prop: 'clearable', label: '可清空', type: 'switch', defaultValue: true },
      { prop: 'disabled', label: '禁用', type: 'switch', defaultValue: false }
    ]
  },
  {
    type: 'datetimerange',
    name: '时间范围',
    icon: 'time-range',
    category: '日期组件',
    defaultProps: {
      type: 'datetimerange',
      format: 'YYYY-MM-DD HH:mm:ss',
      valueFormat: 'YYYY-MM-DD HH:mm:ss',
      rangeSeparator: ' - ',
      clearable: true
    },
    propsConfig: [
      { prop: 'startPlaceholder', label: '开始占位符', type: 'input', defaultValue: '开始时间' },
      { prop: 'endPlaceholder', label: '结束占位符', type: 'input', defaultValue: '结束时间' },
      { prop: 'rangeSeparator', label: '分隔符', type: 'input', defaultValue: ' - ' },
      { prop: 'format', label: '显示格式', type: 'input', defaultValue: 'YYYY-MM-DD HH:mm:ss' },
      { prop: 'valueFormat', label: '值格式', type: 'input', defaultValue: 'YYYY-MM-DD HH:mm:ss' },
      { prop: 'clearable', label: '可清空', type: 'switch', defaultValue: true },
      { prop: 'disabled', label: '禁用', type: 'switch', defaultValue: false }
    ]
  },
  {
    type: 'time',
    name: '时间选择',
    icon: 'timer',
    category: '日期组件',
    defaultProps: {
      format: 'HH:mm:ss',
      valueFormat: 'HH:mm:ss',
      clearable: true
    },
    propsConfig: [
      { prop: 'placeholder', label: '占位符', type: 'input' },
      { prop: 'format', label: '显示格式', type: 'input', defaultValue: 'HH:mm:ss' },
      { prop: 'valueFormat', label: '值格式', type: 'input', defaultValue: 'HH:mm:ss' },
      { prop: 'clearable', label: '可清空', type: 'switch', defaultValue: true },
      { prop: 'disabled', label: '禁用', type: 'switch', defaultValue: false }
    ]
  },

  // ==================== 上传组件 ====================
  {
    type: 'imgUpload',
    name: '图片上传',
    icon: 'image',
    category: '上传组件',
    defaultProps: {
      limit: 1,
      listType: 'picture-card',
      fileSize: 5
    },
    propsConfig: [
      { prop: 'limit', label: '数量限制', type: 'number', min: 1, max: 20, defaultValue: 1 },
      { prop: 'fileSize', label: '大小限制(MB)', type: 'number', min: 1, max: 100, defaultValue: 5 },
      {
        prop: 'listType',
        label: '列表类型',
        type: 'select',
        options: [
          { label: '卡片', value: 'picture-card' },
          { label: '图片', value: 'picture' },
          { label: '文本', value: 'text' }
        ],
        defaultValue: 'picture-card'
      },
      { prop: 'disabled', label: '禁用', type: 'switch', defaultValue: false }
    ]
  },
  {
    type: 'fileUpload',
    name: '文件上传',
    icon: 'upload',
    category: '上传组件',
    defaultProps: {
      limit: 5,
      fileSize: 10
    },
    propsConfig: [
      { prop: 'limit', label: '数量限制', type: 'number', min: 1, max: 20, defaultValue: 5 },
      { prop: 'fileSize', label: '大小限制(MB)', type: 'number', min: 1, max: 500, defaultValue: 10 },
      { prop: 'fileType', label: '文件类型', type: 'input', placeholder: '如: pdf,doc,docx' },
      { prop: 'disabled', label: '禁用', type: 'switch', defaultValue: false }
    ]
  },

  // ==================== 高级组件 ====================
  {
    type: 'editor',
    name: '富文本',
    icon: 'edit',
    category: '高级组件',
    defaultProps: {
      height: 300
    },
    propsConfig: [
      { prop: 'height', label: '编辑器高度', type: 'number', min: 200, max: 800, defaultValue: 300 },
      { prop: 'placeholder', label: '占位符', type: 'input' },
      { prop: 'disabled', label: '禁用', type: 'switch', defaultValue: false }
    ]
  },

  // ==================== 卡片组件 - 统计类 ====================
  {
    type: 'statsCard',
    name: '统计卡片',
    icon: 'dashboard',
    category: '卡片组件',
    defaultProps: {
      title: '总用户数',
      value: 8520,
      unit: '人',
      icon: 'user',
      trend: { value: 12.5, isUp: true },
      description: '较昨日'
    },
    propsConfig: [
      { prop: 'title', label: '标题', type: 'input', defaultValue: '总用户数' },
      { prop: 'value', label: '数值', type: 'number', defaultValue: 8520 },
      { prop: 'unit', label: '单位', type: 'input', defaultValue: '人' },
      { prop: 'icon', label: '图标', type: 'icon-select', defaultValue: 'user' },
      { prop: 'description', label: '描述', type: 'input', defaultValue: '较昨日' },
      { prop: 'showProgress', label: '显示进度条', type: 'switch', defaultValue: false },
      { prop: 'target', label: '目标值', type: 'number' },
      { prop: 'showAnimation', label: '数字动画', type: 'switch', defaultValue: true }
    ]
  },
  {
    type: 'lineStatsCard',
    name: '折线统计卡片',
    icon: 'trend',
    category: '卡片组件',
    defaultProps: {
      title: '访问量趋势',
      description: '较上周 <span class="trend-up">+18.5%</span>',
      subtitle: '本周数据',
      chartData: [120, 150, 180, 160, 200, 190, 220],
      stats: [
        { label: '今日访问', value: '8.2k' },
        { label: '本周访问', value: '52k' }
      ]
    },
    propsConfig: [
      { prop: 'title', label: '标题', type: 'input', defaultValue: '访问量趋势' },
      { prop: 'description', label: '描述', type: 'input' },
      { prop: 'subtitle', label: '副标题', type: 'input' }
    ]
  },
  {
    type: 'barStatsCard',
    name: '柱状统计卡片',
    icon: 'chart',
    category: '卡片组件',
    defaultProps: {
      title: '用户增长',
      description: '比上周 <span class="trend-up">+23%</span>',
      chartData: [160, 100, 150, 80, 190, 100, 175],
      stats: [
        { label: '今日新增', value: '1.2k' },
        { label: '本月新增', value: '32k' }
      ]
    },
    propsConfig: [
      { prop: 'title', label: '标题', type: 'input', defaultValue: '用户增长' },
      { prop: 'description', label: '描述', type: 'input' },
      { prop: 'barWidth', label: '柱宽度', type: 'number', defaultValue: 8 }
    ]
  },

  // ==================== 卡片组件 - 图表类 ====================
  {
    type: 'pieChartCard',
    name: '饼图卡片',
    icon: 'chart',
    category: '卡片组件',
    defaultProps: {
      title: '销售分布',
      subtitle: '按渠道统计',
      data: [
        { name: '直接访问', value: 335 },
        { name: '邮件营销', value: 310 },
        { name: '联盟广告', value: 234 },
        { name: '搜索引擎', value: 435 }
      ],
      height: 280
    },
    propsConfig: [
      { prop: 'title', label: '标题', type: 'input', defaultValue: '销售分布' },
      { prop: 'subtitle', label: '副标题', type: 'input' },
      { prop: 'height', label: '高度', type: 'number', min: 200, max: 500, defaultValue: 280 },
      { prop: 'showLegend', label: '显示图例', type: 'switch', defaultValue: true },
      { prop: 'centerText', label: '中心文字', type: 'input' }
    ]
  },
  {
    type: 'barChartCard',
    name: '柱图卡片',
    icon: 'chart',
    category: '卡片组件',
    defaultProps: {
      title: '月度销售额',
      value: 125000,
      unit: '元',
      trend: { value: 12.5, isUp: true },
      data: [120, 200, 150, 180, 220, 190, 240],
      height: 280
    },
    propsConfig: [
      { prop: 'title', label: '标题', type: 'input', defaultValue: '月度销售额' },
      { prop: 'value', label: '数值', type: 'number' },
      { prop: 'unit', label: '单位', type: 'input' },
      { prop: 'height', label: '高度', type: 'number', min: 200, max: 500, defaultValue: 280 },
      { prop: 'barWidth', label: '柱宽度', type: 'number', defaultValue: 20 }
    ]
  },
  {
    type: 'lineChartCard',
    name: '折线图卡片',
    icon: 'trend',
    category: '卡片组件',
    defaultProps: {
      title: '本月访问量',
      value: 8520,
      unit: '次',
      trend: { value: 12.5, isUp: true },
      data: [800, 900, 850, 920, 880, 950, 1020],
      height: 280,
      smooth: true,
      showAreaColor: true
    },
    propsConfig: [
      { prop: 'title', label: '标题', type: 'input', defaultValue: '本月访问量' },
      { prop: 'value', label: '数值', type: 'number' },
      { prop: 'unit', label: '单位', type: 'input' },
      { prop: 'height', label: '高度', type: 'number', min: 200, max: 500, defaultValue: 280 },
      { prop: 'smooth', label: '平滑曲线', type: 'switch', defaultValue: true },
      { prop: 'showAreaColor', label: '显示面积', type: 'switch', defaultValue: true }
    ]
  },
  {
    type: 'radarChartCard',
    name: '雷达图卡片',
    icon: 'monitor',
    category: '卡片组件',
    defaultProps: {
      title: '能力评估',
      indicator: [
        { name: '销售', max: 100 },
        { name: '管理', max: 100 },
        { name: '技术', max: 100 },
        { name: '客服', max: 100 },
        { name: '研发', max: 100 }
      ],
      data: [{ name: '当前', value: [80, 90, 85, 75, 88] }],
      height: 280
    },
    propsConfig: [
      { prop: 'title', label: '标题', type: 'input', defaultValue: '能力评估' },
      { prop: 'height', label: '高度', type: 'number', min: 200, max: 500, defaultValue: 280 },
      { prop: 'showLegend', label: '显示图例', type: 'switch', defaultValue: true }
    ]
  },
  {
    type: 'mapChartCard',
    name: '地图卡片',
    icon: 'location',
    category: '卡片组件',
    defaultProps: {
      title: '全国销售分布',
      totalValue: 8520,
      unit: '万元',
      height: 350
    },
    propsConfig: [
      { prop: 'title', label: '标题', type: 'input', defaultValue: '全国销售分布' },
      { prop: 'totalValue', label: '总数值', type: 'number' },
      { prop: 'unit', label: '单位', type: 'input' },
      { prop: 'height', label: '高度', type: 'number', min: 300, max: 600, defaultValue: 350 },
      { prop: 'showLabels', label: '显示标签', type: 'switch', defaultValue: true },
      { prop: 'showScatter', label: '显示散点', type: 'switch', defaultValue: false }
    ]
  },

  // ==================== 卡片组件 - 数据展示类 ====================
  {
    type: 'dataCard',
    name: '数据对比卡片',
    icon: 'data',
    category: '卡片组件',
    defaultProps: {
      title: '销售数据',
      iconCode: 'chart',
      dataList: [
        { label: '今日销售', value: '¥8,520', percent: 12.5 },
        { label: '昨日', value: '¥7,580', percent: -2.1 },
        { label: '本周', value: '¥45,600', percent: 8.9 }
      ]
    },
    propsConfig: [
      { prop: 'title', label: '标题', type: 'input', defaultValue: '销售数据' },
      { prop: 'iconCode', label: '图标', type: 'icon-select', defaultValue: 'chart' }
    ]
  },
  {
    type: 'tableCard',
    name: '表格卡片',
    icon: 'table',
    category: '卡片组件',
    defaultProps: {
      title: '最新订单',
      columns: [
        { prop: 'id', label: '订单号', width: 100 },
        { prop: 'name', label: '商品名称' },
        { prop: 'price', label: '价格', width: 80 }
      ],
      data: [
        { id: 'ORD001', name: '商品A', price: '¥199' },
        { id: 'ORD002', name: '商品B', price: '¥299' },
        { id: 'ORD003', name: '商品C', price: '¥399' }
      ]
    },
    propsConfig: [
      { prop: 'title', label: '标题', type: 'input', defaultValue: '最新订单' },
      { prop: 'showHeader', label: '显示表头', type: 'switch', defaultValue: true },
      { prop: 'border', label: '显示边框', type: 'switch', defaultValue: false },
      { prop: 'showActions', label: '显示操作', type: 'switch', defaultValue: false }
    ]
  },
  {
    type: 'dataListCard',
    name: '数据列表卡片',
    icon: 'list',
    category: '卡片组件',
    defaultProps: {
      title: '待办事项',
      list: [
        { title: '完成项目报告', status: '进行中', time: '2小时前', icon: 'document' },
        { title: '客户会议', status: '待处理', time: '今天 14:00', icon: 'date' },
        { title: '代码审查', status: '已完成', time: '昨天', icon: 'finished' }
      ],
      maxCount: 5,
      showMoreButton: true
    },
    propsConfig: [
      { prop: 'title', label: '标题', type: 'input', defaultValue: '待办事项' },
      { prop: 'maxCount', label: '最大显示数', type: 'number', min: 3, max: 10, defaultValue: 5 },
      { prop: 'showMoreButton', label: '显示更多', type: 'switch', defaultValue: true }
    ]
  },
  {
    type: 'activityCard',
    name: '活动时间轴卡片',
    icon: 'timer',
    category: '卡片组件',
    defaultProps: {
      title: '最近活动',
      activities: [
        { id: 1, user: '张三', action: '创建了', target: '新项目', time: '5分钟前', type: 'create' },
        { id: 2, user: '李四', action: '更新了', target: '文档', time: '10分钟前', type: 'update' },
        { id: 3, user: '王五', action: '删除了', target: '文件', time: '30分钟前', type: 'delete' }
      ],
      showAvatar: true
    },
    propsConfig: [
      { prop: 'title', label: '标题', type: 'input', defaultValue: '最近活动' },
      { prop: 'showAvatar', label: '显示头像', type: 'switch', defaultValue: true },
      { prop: 'showTypeIcon', label: '显示类型图标', type: 'switch', defaultValue: false }
    ]
  },
  {
    type: 'timelineListCard',
    name: '时间轴列表卡片',
    icon: 'timer',
    category: '卡片组件',
    defaultProps: {
      title: '最近交易',
      list: [
        { time: '上午 09:30', content: '收到支付 385.90 元', status: 'success' },
        { time: '上午 10:00', content: '新销售记录', code: 'ML-3467', status: 'info' },
        { time: '下午 02:30', content: '退款处理中', status: 'warning' }
      ],
      maxCount: 5
    },
    propsConfig: [
      { prop: 'title', label: '标题', type: 'input', defaultValue: '最近交易' },
      { prop: 'maxCount', label: '最大显示数', type: 'number', min: 3, max: 10, defaultValue: 5 }
    ]
  },

  // ==================== 卡片组件 - 用户信息类 ====================
  {
    type: 'userCard',
    name: '用户卡片',
    icon: 'user',
    category: '卡片组件',
    defaultProps: {
      name: '张三',
      role: '产品经理',
      description: '负责产品设计与规划',
      stats: { posts: 128, followers: 1234, following: 567 },
      tags: ['Vue', 'React', 'Node.js'],
      showStatus: true
    },
    propsConfig: [
      { prop: 'name', label: '用户名', type: 'input', defaultValue: '张三' },
      { prop: 'role', label: '角色', type: 'input', defaultValue: '产品经理' },
      { prop: 'description', label: '描述', type: 'input' },
      { prop: 'avatar', label: '头像URL', type: 'input' },
      { prop: 'showStatus', label: '显示状态', type: 'switch', defaultValue: true }
    ]
  },
  {
    type: 'profileCard',
    name: '个人资料卡片',
    icon: 'user',
    category: '卡片组件',
    defaultProps: {
      profile: {
        name: '张三',
        title: '高级工程师',
        company: '某某科技公司',
        email: 'zhangsan@example.com',
        phone: '138****8888',
        location: '北京市'
      },
      stats: [
        { label: '项目', value: 28 },
        { label: '粉丝', value: 1234 },
        { label: '关注', value: 567 }
      ]
    },
    propsConfig: [
      { prop: 'showContact', label: '显示联系方式', type: 'switch', defaultValue: true },
      { prop: 'coverImage', label: '封面图URL', type: 'input' }
    ]
  },
  {
    type: 'socialCard',
    name: '社交卡片',
    icon: 'chat',
    category: '卡片组件',
    defaultProps: {
      content: {
        user: '张三',
        text: '今天天气真好！出去走走~',
        images: [],
        time: '5分钟前'
      },
      stats: { likes: 128, comments: 45, shares: 12 },
      showActions: true
    },
    propsConfig: [
      { prop: 'showActions', label: '显示操作', type: 'switch', defaultValue: true }
    ]
  },

  // ==================== 卡片组件 - 特殊功能类 ====================
  {
    type: 'formCard',
    name: '表单卡片',
    icon: 'form',
    category: '卡片组件',
    defaultProps: {
      title: '基本信息',
      collapsible: false,
      showActions: true
    },
    propsConfig: [
      { prop: 'title', label: '标题', type: 'input', defaultValue: '基本信息' },
      { prop: 'collapsible', label: '可折叠', type: 'switch', defaultValue: false },
      { prop: 'showActions', label: '显示操作', type: 'switch', defaultValue: true }
    ]
  },
  {
    type: 'pricingCard',
    name: '价格卡片',
    icon: 'goods',
    category: '卡片组件',
    defaultProps: {
      plan: '专业版',
      price: 199,
      period: '月',
      features: ['所有基础功能', '无限项目', '优先支持', 'API访问'],
      disabledFeatures: ['定制开发'],
      recommended: true
    },
    propsConfig: [
      { prop: 'plan', label: '方案名称', type: 'input', defaultValue: '专业版' },
      { prop: 'price', label: '价格', type: 'number', defaultValue: 199 },
      { prop: 'period', label: '周期', type: 'input', defaultValue: '月' },
      { prop: 'originalPrice', label: '原价', type: 'number' },
      { prop: 'recommended', label: '推荐', type: 'switch', defaultValue: false }
    ]
  },
  {
    type: 'imageCard',
    name: '图片卡片',
    icon: 'image',
    category: '卡片组件',
    defaultProps: {
      title: 'Vue 3 深度解析',
      description: '深入探讨Vue 3的响应式原理与组合式API',
      category: '前端开发',
      author: '张三',
      date: '2024-03-20',
      layout: 'vertical'
    },
    propsConfig: [
      { prop: 'title', label: '标题', type: 'input', defaultValue: 'Vue 3 深度解析' },
      { prop: 'description', label: '描述', type: 'input' },
      { prop: 'imageUrl', label: '图片URL', type: 'input' },
      { prop: 'category', label: '分类', type: 'input' },
      { prop: 'author', label: '作者', type: 'input' },
      {
        prop: 'layout',
        label: '布局',
        type: 'select',
        options: [
          { label: '垂直', value: 'vertical' },
          { label: '水平', value: 'horizontal' }
        ],
        defaultValue: 'vertical'
      }
    ]
  },
  {
    type: 'infoCard',
    name: '信息提示卡片',
    icon: 'info',
    category: '卡片组件',
    defaultProps: {
      title: '系统通知',
      type: 'info',
      closable: true,
      simple: false
    },
    propsConfig: [
      { prop: 'title', label: '标题', type: 'input', defaultValue: '系统通知' },
      {
        prop: 'type',
        label: '类型',
        type: 'select',
        options: [
          { label: '信息', value: 'info' },
          { label: '成功', value: 'success' },
          { label: '警告', value: 'warning' },
          { label: '错误', value: 'error' }
        ],
        defaultValue: 'info'
      },
      { prop: 'icon', label: '自定义图标', type: 'icon-select' },
      { prop: 'closable', label: '可关闭', type: 'switch', defaultValue: true },
      { prop: 'simple', label: '简洁模式', type: 'switch', defaultValue: false }
    ]
  },
  {
    type: 'weatherCard',
    name: '天气卡片',
    icon: 'sun',
    category: '卡片组件',
    defaultProps: {
      weather: {
        city: '北京',
        temperature: 25,
        condition: '晴',
        humidity: 60,
        windSpeed: 12,
        airQuality: 85
      },
      showDetails: true
    },
    propsConfig: [
      { prop: 'showDetails', label: '显示详情', type: 'switch', defaultValue: true }
    ]
  },
  {
    type: 'notificationCard',
    name: '通知卡片',
    icon: 'notification',
    category: '卡片组件',
    defaultProps: {
      title: '通知消息',
      notifications: [
        { id: 1, title: '新消息', content: '您有一条新消息', read: false, type: 'info' },
        { id: 2, title: '系统通知', content: '系统将于今晚维护', read: true, type: 'warning' },
        { id: 3, title: '订单完成', content: '您的订单已完成', read: false, type: 'success' }
      ],
      showUnreadCount: true
    },
    propsConfig: [
      { prop: 'title', label: '标题', type: 'input', defaultValue: '通知消息' },
      { prop: 'showUnreadCount', label: '显示未读数', type: 'switch', defaultValue: true },
      { prop: 'showTypeIcon', label: '显示类型图标', type: 'switch', defaultValue: true }
    ]
  },
  {
    type: 'emptyCard',
    name: '空状态卡片',
    icon: 'folder',
    category: '卡片组件',
    defaultProps: {
      icon: 'folder',
      title: '暂无数据',
      description: '还没有任何内容，快来创建第一条吧'
    },
    propsConfig: [
      { prop: 'icon', label: '图标', type: 'icon-select', defaultValue: 'folder' },
      { prop: 'title', label: '标题', type: 'input', defaultValue: '暂无数据' },
      { prop: 'description', label: '描述', type: 'input', defaultValue: '还没有任何内容，快来创建第一条吧' },
      { prop: 'image', label: '自定义图片', type: 'input' }
    ]
  },

  // ==================== 图表组件 ====================
  {
    type: 'lineChart',
    name: '折线图',
    icon: 'trend',
    category: '图表组件',
    defaultProps: {
      title: '折线图',
      height: 300,
      smooth: true
    },
    propsConfig: [
      { prop: 'title', label: '标题', type: 'input', defaultValue: '折线图' },
      { prop: 'height', label: '高度', type: 'number', min: 200, max: 600, defaultValue: 300 },
      { prop: 'smooth', label: '平滑曲线', type: 'switch', defaultValue: true },
      { prop: 'showArea', label: '显示面积', type: 'switch', defaultValue: false },
      { prop: 'showLegend', label: '显示图例', type: 'switch', defaultValue: true }
    ]
  },
  {
    type: 'barChart',
    name: '柱状图',
    icon: 'chart',
    category: '图表组件',
    defaultProps: {
      title: '柱状图',
      height: 300,
      horizontal: false
    },
    propsConfig: [
      { prop: 'title', label: '标题', type: 'input', defaultValue: '柱状图' },
      { prop: 'height', label: '高度', type: 'number', min: 200, max: 600, defaultValue: 300 },
      { prop: 'horizontal', label: '横向显示', type: 'switch', defaultValue: false },
      { prop: 'showLegend', label: '显示图例', type: 'switch', defaultValue: true },
      { prop: 'stack', label: '堆叠显示', type: 'switch', defaultValue: false }
    ]
  },
  {
    type: 'pieChart',
    name: '饼图',
    icon: 'chart',
    category: '图表组件',
    defaultProps: {
      title: '饼图',
      height: 300,
      radius: ['40%', '70%']
    },
    propsConfig: [
      { prop: 'title', label: '标题', type: 'input', defaultValue: '饼图' },
      { prop: 'height', label: '高度', type: 'number', min: 200, max: 600, defaultValue: 300 },
      { prop: 'showLegend', label: '显示图例', type: 'switch', defaultValue: true },
      { prop: 'roseType', label: '南丁格尔图', type: 'switch', defaultValue: false }
    ]
  },
  {
    type: 'radarChart',
    name: '雷达图',
    icon: 'monitor',
    category: '图表组件',
    defaultProps: {
      title: '雷达图',
      height: 300
    },
    propsConfig: [
      { prop: 'title', label: '标题', type: 'input', defaultValue: '雷达图' },
      { prop: 'height', label: '高度', type: 'number', min: 200, max: 600, defaultValue: 300 },
      { prop: 'showLegend', label: '显示图例', type: 'switch', defaultValue: true },
      { prop: 'shape', label: '形状', type: 'select', options: [{ label: '多边形', value: 'polygon' }, { label: '圆形', value: 'circle' }], defaultValue: 'polygon' }
    ]
  },
  {
    type: 'scatterChart',
    name: '散点图',
    icon: 'data',
    category: '图表组件',
    defaultProps: {
      title: '散点图',
      height: 300
    },
    propsConfig: [
      { prop: 'title', label: '标题', type: 'input', defaultValue: '散点图' },
      { prop: 'height', label: '高度', type: 'number', min: 200, max: 600, defaultValue: 300 },
      { prop: 'showLegend', label: '显示图例', type: 'switch', defaultValue: true },
      { prop: 'symbolSize', label: '点大小', type: 'number', min: 4, max: 30, defaultValue: 10 }
    ]
  },
  {
    type: 'mapChart',
    name: '地图',
    icon: 'location',
    category: '图表组件',
    defaultProps: {
      title: '地图',
      height: 400,
      mapType: 'china'
    },
    propsConfig: [
      { prop: 'title', label: '标题', type: 'input', defaultValue: '地图' },
      { prop: 'height', label: '高度', type: 'number', min: 300, max: 800, defaultValue: 400 },
      { prop: 'mapType', label: '地图类型', type: 'select', options: [{ label: '中国', value: 'china' }, { label: '世界', value: 'world' }], defaultValue: 'china' },
      { prop: 'showLabel', label: '显示标签', type: 'switch', defaultValue: false },
      { prop: 'showVisualMap', label: '显示色阶', type: 'switch', defaultValue: true }
    ]
  },

  // ==================== 布局组件 ====================
  {
    type: 'row',
    name: '行容器',
    icon: 'rows',
    category: '布局组件',
    defaultProps: {
      gutter: 20,
      justify: 'start',
      align: 'top'
    },
    propsConfig: [
      { prop: 'gutter', label: '栅格间隔', type: 'number', min: 0, max: 100, defaultValue: 20 },
      {
        prop: 'justify',
        label: '水平排列',
        type: 'select',
        options: [
          { label: '左对齐', value: 'start' },
          { label: '居中', value: 'center' },
          { label: '右对齐', value: 'end' },
          { label: '两端对齐', value: 'space-between' },
          { label: '均匀分布', value: 'space-around' },
          { label: '平均分布', value: 'space-evenly' }
        ],
        defaultValue: 'start'
      },
      {
        prop: 'align',
        label: '垂直对齐',
        type: 'select',
        options: [
          { label: '顶部', value: 'top' },
          { label: '居中', value: 'middle' },
          { label: '底部', value: 'bottom' }
        ],
        defaultValue: 'top'
      }
    ]
  },
  {
    type: 'col',
    name: '列容器',
    icon: 'columns',
    category: '布局组件',
    defaultProps: {
      span: 12,
      offset: 0
    },
    propsConfig: [
      {
        prop: 'span',
        label: '栅格占位',
        type: 'select',
        options: [
          { label: '1/4 (6列)', value: 6 },
          { label: '1/3 (8列)', value: 8 },
          { label: '1/2 (12列)', value: 12 },
          { label: '2/3 (16列)', value: 16 },
          { label: '3/4 (18列)', value: 18 },
          { label: '整行 (24列)', value: 24 }
        ],
        defaultValue: 12
      },
      { prop: 'offset', label: '偏移量', type: 'number', min: 0, max: 24, defaultValue: 0 },
      { prop: 'push', label: '右移格数', type: 'number', min: 0, max: 24, defaultValue: 0 },
      { prop: 'pull', label: '左移格数', type: 'number', min: 0, max: 24, defaultValue: 0 }
    ]
  },
  {
    type: 'divider',
    name: '分割线',
    icon: 'minus',
    category: '布局组件',
    defaultProps: {
      content: '',
      contentPosition: 'center'
    },
    propsConfig: [
      { prop: 'content', label: '文本内容', type: 'input' },
      {
        prop: 'contentPosition',
        label: '文本位置',
        type: 'select',
        options: [
          { label: '左侧', value: 'left' },
          { label: '居中', value: 'center' },
          { label: '右侧', value: 'right' }
        ],
        defaultValue: 'center'
      },
      {
        prop: 'direction',
        label: '方向',
        type: 'select',
        options: [
          { label: '水平', value: 'horizontal' },
          { label: '垂直', value: 'vertical' }
        ],
        defaultValue: 'horizontal'
      }
    ]
  },
  {
    type: 'alert',
    name: '提示框',
    icon: 'warning',
    category: '布局组件',
    defaultProps: {
      title: '提示信息',
      type: 'info',
      closable: true
    },
    propsConfig: [
      { prop: 'title', label: '标题', type: 'input', defaultValue: '提示信息' },
      { prop: 'description', label: '描述', type: 'input' },
      {
        prop: 'type',
        label: '类型',
        type: 'select',
        options: [
          { label: '成功', value: 'success' },
          { label: '警告', value: 'warning' },
          { label: '信息', value: 'info' },
          { label: '错误', value: 'error' }
        ],
        defaultValue: 'info'
      },
      { prop: 'closable', label: '可关闭', type: 'switch', defaultValue: true },
      { prop: 'showIcon', label: '显示图标', type: 'switch', defaultValue: true }
    ]
  },
  {
    type: 'collapse',
    name: '折叠面板',
    icon: 'collapse',
    category: '布局组件',
    defaultProps: {
      title: '折叠面板',
      accordion: false
    },
    propsConfig: [
      { prop: 'title', label: '标题', type: 'input', defaultValue: '折叠面板' },
      { prop: 'accordion', label: '手风琴模式', type: 'switch', defaultValue: false }
    ]
  },

  // ==================== 展示组件 ====================
  {
    type: 'icon',
    name: '图标',
    icon: 'star',
    category: '展示组件',
    defaultProps: {
      code: 'star',
      size: 'lg',
      color: ''
    },
    propsConfig: [
      { prop: 'code', label: '选择图标', type: 'icon-select', defaultValue: 'star' },
      {
        prop: 'size',
        label: '图标大小',
        type: 'select',
        options: [
          { label: '超小 (12px)', value: 'xs' },
          { label: '小 (16px)', value: 'sm' },
          { label: '中 (20px)', value: 'md' },
          { label: '大 (24px)', value: 'lg' },
          { label: '超大 (32px)', value: 'xl' },
          { label: '特大 (40px)', value: '2xl' }
        ],
        defaultValue: 'lg'
      },
      { prop: 'color', label: '图标颜色', type: 'input', placeholder: '如: #409eff, red' },
      {
        prop: 'animate',
        label: '动画效果',
        type: 'select',
        options: [
          { label: '无', value: '' },
          { label: '抖动', value: 'shake' },
          { label: '旋转', value: 'rotate180' },
          { label: '上移', value: 'moveUp' },
          { label: '放大', value: 'expand' },
          { label: '缩小', value: 'shrink' },
          { label: '呼吸', value: 'breathing' }
        ],
        defaultValue: ''
      }
    ]
  }
]

/** 获取组件配置 */
export function getComponentConfig(type: FormItemType): ComponentConfig | undefined {
  return componentList.find((c) => c.type === type)
}

/** 按分类获取组件列表 */
export function getComponentsByCategory(): Record<string, ComponentConfig[]> {
  const result: Record<string, ComponentConfig[]> = {}
  componentList.forEach((component) => {
    if (!result[component.category]) {
      result[component.category] = []
    }
    result[component.category].push(component)
  })
  return result
}

/** 获取所有分类 */
export function getCategories(): string[] {
  const categories = new Set<string>()
  componentList.forEach((c) => categories.add(c.category))
  return Array.from(categories)
}

/** 默认页面配置 */
export const defaultFormSchema = (): import('../types').FormSchema => ({
  name: 'index',
  description: '',
  labelWidth: '100px',
  labelPosition: 'right',
  layout: 'dialog',
  dialogSize: 'medium',
  items: [],
  gutter: 20
})
