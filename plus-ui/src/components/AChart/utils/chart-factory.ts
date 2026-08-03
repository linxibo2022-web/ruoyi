/**
 * 图表配置生成工厂
 *
 * 提供静态方法用于生成各种图表的ECharts配置
 * 这是一个辅助工具类，主要用于复杂配置的预处理和验证
 */

import type { EChartsOption } from 'echarts'
import type {
  // BarChartProps,
  BarHorizontalChartProps,
  BarBidirectionalChartProps,
  LineChartProps,
  PieChartProps,
  RadarChartProps,
  ScatterChartProps,
  CandlestickChartProps,
  MapChartProps,
  SeriesDataItem,
  LineDataItem,
  ChartDataItem,
  RadarDataItem,
  ScatterDataItem,
  CandlestickDataItem,
  MapDataItem
} from '../types/chart'

/* 独立导入组件props类型 */
import type { ABarChartProps as BarChartProps } from '../ABarChart.vue'

/**
 * 图表配置生成工厂类
 * 提供静态方法生成各种图表配置
 */
export class ChartConfigFactory {
  /**
   * 验证数据是否为空
   * @param data 数据
   * @returns 是否为空
   */
  private static isEmpty(data: any): boolean {
    if (!data) return true
    if (Array.isArray(data)) {
      return (
        data.length === 0 ||
        data.every((item) =>
          // 兼容数字 / 数字字符串（后端 BigDecimal 经全局 Jackson 配置会序列化为字符串）
          this.isNumericValue(item)
            ? this.toNumber(item) === 0
            : typeof item === 'object' && item?.data
              ? this.toNumberArray(item.data).every((val) => val === 0)
              : false
        )
      )
    }
    return false
  }

  /**
   * 判断值是否为数值（含可安全转换的数字字符串）
   *
   * 后端 BigDecimal 会被框架全局 Jackson 配置（ToStringSerializer）序列化为字符串，
   * 导致图表收到的是 ["12.5","30.2"] 而非 [12.5,30.2]，故类型判断必须兼容数字字符串。
   * @param val 待判断的值
   * @returns 是否为有效数值
   */
  static isNumericValue(val: any): boolean {
    if (typeof val === 'number') return !Number.isNaN(val)
    if (typeof val === 'string' && val.trim() !== '') return !Number.isNaN(Number(val))
    return false
  }

  /**
   * 将值安全转换为数字
   * @param val 待转换的值
   * @returns 转换后的数字，无法转换时返回 0
   */
  static toNumber(val: any): number {
    const num = Number(val)
    return Number.isNaN(num) ? 0 : num
  }

  /**
   * 将数字 / 数字字符串数组归一化为数字数组
   * @param arr 原始数组
   * @returns 归一化后的数字数组
   */
  static toNumberArray(arr: any[]): number[] {
    return Array.isArray(arr) ? arr.map((v) => this.toNumber(v)) : []
  }

  /**
   * 验证柱状图数据
   * @param props 柱状图属性
   * @returns 验证结果和处理后的数据
   */
  static validateBarData(props: BarChartProps) {
    const isEmpty = this.isEmpty(props.data)

    // 检查单数据情况（兼容数字字符串，如后端 BigDecimal 序列化结果）
    if (Array.isArray(props.data) && this.isNumericValue(props.data[0])) {
      const singleData = this.toNumberArray(props.data as any[])
      return {
        isEmpty: isEmpty || singleData.every((val) => val === 0),
        isMultiple: false,
        data: singleData
      }
    }

    // 检查多数据情况
    if (Array.isArray(props.data) && typeof props.data[0] === 'object') {
      const multiData = props.data as SeriesDataItem[]
      return {
        isEmpty: isEmpty || multiData.every((item) => !item.data?.length || item.data.every((val) => val === 0)),
        isMultiple: true,
        data: multiData
      }
    }

    return {
      isEmpty: true,
      isMultiple: false,
      data: []
    }
  }

  /**
   * 验证折线图数据
   * @param props 折线图属性
   * @returns 验证结果和处理后的数据
   */
  static validateLineData(props: LineChartProps) {
    if (props.isEmpty) return { isEmpty: true, isMultiple: false, data: [] }

    // 检查单数据情况（兼容数字字符串，如后端 BigDecimal 序列化结果）
    if (Array.isArray(props.data) && this.isNumericValue(props.data[0])) {
      const singleData = this.toNumberArray(props.data as any[])
      return {
        isEmpty: !singleData.length || singleData.every((val) => val === 0),
        isMultiple: false,
        data: singleData
      }
    }

    // 检查多数据情况
    if (Array.isArray(props.data) && typeof props.data[0] === 'object') {
      const multiData = props.data as LineDataItem[]
      return {
        isEmpty: !multiData.length || multiData.every((item) => !item.data?.length || this.toNumberArray(item.data).every((val) => val === 0)),
        isMultiple: true,
        data: multiData
      }
    }

    return { isEmpty: true, isMultiple: false, data: [] }
  }

  /**
   * 验证双向柱状图数据
   * @param props 双向柱状图属性
   * @returns 验证结果
   */
  static validateBidirectionalBarData(props: BarBidirectionalChartProps) {
    return {
      isEmpty:
        props.isEmpty ||
        !props.positiveData?.length ||
        !props.negativeData?.length ||
        // 归一化后再判空，兼容后端 BigDecimal 序列化为字符串的场景
        (this.toNumberArray(props.positiveData).every((val) => val === 0) && this.toNumberArray(props.negativeData).every((val) => val === 0))
    }
  }

  /**
   * 验证饼图数据
   * @param props 饼图属性
   * @returns 验证结果
   */
  static validatePieData(props: PieChartProps) {
    return {
      isEmpty: !props.data?.length || props.data.every((item) => item.value === 0)
    }
  }

  /**
   * 验证雷达图数据
   * @param props 雷达图属性
   * @returns 验证结果
   */
  static validateRadarData(props: RadarChartProps) {
    return {
      isEmpty: !props.data?.length || props.data.every((item) => item.value.every((val) => val === 0))
    }
  }

  /**
   * 验证散点图数据
   * @param props 散点图属性
   * @returns 验证结果
   */
  static validateScatterData(props: ScatterChartProps) {
    return {
      isEmpty: !props.data?.length || props.data.every((item) => item.value.every((val) => val === 0))
    }
  }

  /**
   * 验证K线图数据
   * @param props K线图属性
   * @returns 验证结果
   */
  static validateCandlestickData(props: CandlestickChartProps) {
    return {
      isEmpty: !props.data?.length || props.data.every((item) => item.open === 0 && item.close === 0 && item.high === 0 && item.low === 0)
    }
  }

  /**
   * 验证地图数据
   * @param props 地图属性
   * @returns 验证结果
   */
  static validateMapData(props: MapChartProps) {
    return {
      isEmpty: props.isEmpty || !props.mapData?.length
    }
  }

  /**
   * 预处理折线图动画数据
   * @param props 折线图属性
   * @param isMultiple 是否为多数据
   * @returns 初始化的动画数据
   */
  static initLineAnimationData(props: LineChartProps, isMultiple: boolean) {
    if (isMultiple) {
      const multiData = props.data as LineDataItem[]
      return multiData.map((item) => ({
        ...item,
        data: new Array(item.data.length).fill(0)
      }))
    } else {
      const singleData = props.data as number[]
      return new Array(singleData.length).fill(0)
    }
  }

  /**
   * 复制真实数据（用于动画）
   * @param data 原始数据
   * @param isMultiple 是否为多数据
   * @returns 复制后的数据
   */
  static copyRealData(data: any, isMultiple: boolean) {
    return isMultiple ? [...(data as LineDataItem[])] : [...(data as number[])]
  }

  /**
   * 计算折线图最大值
   * @param props 折线图属性
   * @param isMultiple 是否为多数据
   * @returns 最大值
   */
  static calculateLineMaxValue(props: LineChartProps, isMultiple: boolean): number {
    if (isMultiple) {
      const multiData = props.data as LineDataItem[]
      return multiData.reduce((max, item) => {
        if (item.data?.length) {
          const itemMax = Math.max(...this.toNumberArray(item.data))
          return Math.max(max, itemMax)
        }
        return max
      }, 0)
    } else {
      const singleData = this.toNumberArray((props.data as any[]) || [])
      return singleData.length ? Math.max(...singleData) : 0
    }
  }

  /**
   * 处理双向柱状图负值数据
   * @param negativeData 负向数据
   * @returns 处理后的负值数据
   */
  static processNegativeData(negativeData: number[]): number[] {
    return negativeData.map((val) => (val > 0 ? -val : val))
  }

  /**
   * 生成饼图中心位置
   * @param showLegend 是否显示图例
   * @param legendPosition 图例位置
   * @returns 中心位置坐标
   */
  static calculatePieCenter(showLegend: boolean, legendPosition: 'top' | 'bottom' | 'left' | 'right' = 'bottom'): [string, string] {
    if (!showLegend) return ['50%', '50%']

    switch (legendPosition) {
      case 'left':
        return ['60%', '50%']
      case 'right':
        return ['40%', '50%']
      case 'top':
        return ['50%', '60%']
      case 'bottom':
        return ['50%', '40%']
      default:
        return ['50%', '50%']
    }
  }

  /**
   * 格式化K线数据
   * @param data K线原始数据
   * @returns 格式化后的数据
   */
  static formatCandlestickData(data: CandlestickDataItem[]) {
    return {
      times: data.map((item) => item.time),
      values: data.map((item) => [item.open, item.close, item.low, item.high])
    }
  }

  /**
   * 生成颜色配置
   * @param customColors 自定义颜色数组
   * @param defaultColors 默认颜色数组
   * @param index 索引
   * @returns 计算后的颜色
   */
  static getColorByIndex(customColors: string[] | undefined, defaultColors: string[], index: number): string {
    const colors = customColors || defaultColors
    return colors[index % colors.length]
  }

  /**
   * 验证图表基础配置
   * @param props 基础属性
   * @returns 验证结果
   */
  static validateBaseProps(props: any) {
    return {
      hasValidHeight: typeof props.height === 'string' && props.height.length > 0,
      hasValidColors: Array.isArray(props.colors) && props.colors.length > 0,
      isLoading: Boolean(props.loading),
      isEmpty: Boolean(props.isEmpty)
    }
  }

  /**
   * 生成默认的网格配置
   * @param showLegend 是否显示图例
   * @param legendPosition 图例位置
   * @param customGrid 自定义网格配置
   * @returns 网格配置
   */
  static generateGridConfig(showLegend: boolean, legendPosition: 'top' | 'bottom' | 'left' | 'right' = 'bottom', customGrid: any = {}) {
    const defaultGrid = {
      top: 15,
      right: 15,
      bottom: 8,
      left: 0,
      containLabel: true,
      ...customGrid
    }

    if (!showLegend) return defaultGrid

    switch (legendPosition) {
      case 'bottom':
        return { ...defaultGrid, bottom: 40 }
      case 'top':
        return { ...defaultGrid, top: 40 }
      case 'left':
        return { ...defaultGrid, left: 120 }
      case 'right':
        return { ...defaultGrid, right: 120 }
      default:
        return defaultGrid
    }
  }
}
