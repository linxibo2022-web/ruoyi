// 全局类型声明
import type { ComponentInternalInstance as ComponentInstance } from 'vue'

/**
 * 全局类型定义 (global.d.ts)
 *
 * 为整个应用提供统一的 TypeScript 类型系统，定义项目中通用的接口和类型。
 * 所有类型均为全局声明，在项目任何位置都可直接使用，无需手动导入。
 *
 * 🎯 设计原则:
 * - 全局可用: 避免频繁的 import 操作，提升开发效率
 * - 类型安全: 为 API 调用、UI 组件、数据结构提供完整的类型检查
 * - 统一标准: 确保项目中相同概念使用一致的类型定义
 * - 易于维护: 集中管理所有通用类型，便于统一更新和维护
 *
 * 📋 包含类型分类:
 *
 * 【组件相关】
 * - ComponentInternalInstance: Vue 组件实例类型
 *
 * 【API 交互】
 * - Result<T>: 统一的 API 响应格式 [错误, 数据]
 * - PageResult<T>: 标准化分页响应数据结构
 * - R<T>: 标准 API 响应结构 包含状态码、消息和数据
 * - PageQuery: 分页查询参数接口
 *
 * 【UI 控制】
 * - FieldVisibilityConfig: 字段显示/隐藏控制配置
 * - DialogState: 弹窗状态管理配置
 * - DictItem: 字典数据项配置（下拉选择、标签等）
 *
 */
declare global {
  /** Vue 组件实例类型 */
  declare type ComponentInternalInstance = ComponentInstance

  /**
   * 统一 API 响应类型
   * @description 定义所有 API 请求的统一返回格式 [错误, 数据]
   * @template T 响应数据的类型
   */
  declare type Result<T = any> = Promise<[Error | null, T | null]>

  /**
   * 分页响应数据类型
   * @description 标准化的分页数据结构，与后端 PageResult 保持一致
   * @template T 列表项的类型
   */
  declare interface PageResult<T = any> {
    /** 数据记录列表 */
    records: T[]
    /** 总记录数 */
    total: number
    /** 总页数 */
    pages: number
    /** 当前页码 */
    current: number
    /** 每页大小 */
    size: number
    /** 是否为最后一页 */
    last: boolean
  }

  /**
   * 标准 API 响应结构
   * @description 后端返回的标准响应格式，包含状态码、消息和数据
   * @template T 数据字段的类型
   */
  declare interface R<T = any> {
    /** 响应状态码 */
    code: number
    /** 响应消息 */
    msg: string
    /** 响应数据 */
    data: T
  }

  /**
   * 分页查询基础参数
   * @description 用于后端分页查询的通用参数接口
   */
  declare interface PageQuery {
    /** 当前页码，从1开始 */
    pageNum?: number
    /** 每页显示记录数 */
    pageSize?: number
    /** 排序字段 */
    orderByColumn?: string
    /** 排序方向 asc/desc */
    isAsc?: string
    /** 模糊搜索关键词 */
    searchValue?: string
    /** 扩展查询参数 */
    params?: Record<string, any>
  }

  /**
   * 字段可见性配置
   * @description 用于控制界面字段的显示/隐藏状态，支持层级结构
   */
  declare interface FieldVisibilityConfig {
    /** 字段唯一标识 */
    key: string | number
    /** 字段名称 */
    field: string
    /** 字段显示标签 */
    label: string
    /** 是否可见 */
    visible: boolean
    /** 子字段配置，支持层级结构 */
    children?: Array<FieldVisibilityConfig>
  }

  /**
   * 弹窗状态配置
   * @description 用于管理弹窗的显示状态和基本属性
   */
  declare interface DialogState {
    /** 弹窗标题 */
    title?: string
    /** 弹窗是否显示 */
    visible: boolean
  }

  /**
   * 字典项配置
   * @description 用于下拉选择、标签等组件的选项数据，支持 Element UI 标签样式
   */
  declare interface DictItem {
    /** 显示标签文本 */
    label: string
    /** 实际存储的值 */
    value: string
    /** 状态标识 */
    status?: string
    /** Element UI Tag 组件的类型 */
    elTagType?: ElTagType
    /** Element UI Tag 组件的自定义类名 */
    elTagClass?: string
  }

  /**
   * 字段配置接口
   * @description 用于详情展示、表单等组件的字段配置，支持多种显示类型和自定义渲染
   */
  declare interface FieldConfig {
    /** 字段属性名，支持嵌套如 'user.name' */
    prop: string
    /** 字段显示标签 */
    label: string
    /** 字段占用列数 */
    span?: number
    /** 自定义插槽名称，用于自定义渲染 */
    slot?: string
    /** 自定义格式化函数 */
    formatter?: (value: any, data: any) => string
    /** 数据类型，用于自动格式化 */
    type?: 'text' | 'copyable' | 'date' | 'datetime' | 'currency' | 'boolean' | 'array' | 'dict' | 'image' | 'password' | 'html' | 'file' | 'region'
    /** 字典选项，当type为dict时使用，支持普通数组或响应式引用 */
    dictOptions?: DictItem[] | import('vue').Ref<DictItem[]>
    /** 图片预览配置，当type为image时使用 */
    imageConfig?: {
      /** 宽度 */
      width?: number | string
      /** 高度 */
      height?: number | string
      /** 是否显示所有图片 */
      showAll?: boolean
      /** 布局方式 */
      layout?: 'flex' | 'grid'
      /** 网格布局列数（仅在layout为grid时生效）3 */
      columns?: number
      /** 最大显示图片数量 9 */
      maxShow?: number
      /** 图片间距 4 */
      gap?: number
    }
    /** 是否隐藏字段，支持函数动态判断 */
    hidden?: boolean | ((data: any) => boolean)
    /** 分组名称，设置后会按组分块显示 */
    group?: string
    /** 是否不参与打印，设为true时该字段不会在打印中显示 */
    noPrint?: boolean
  }

  /**
   * 表格操作按钮配置
   * @description 用于 ATable 组件操作列的按钮配置
   */
  declare interface TableActionConfig {
    /** 按钮图标名称 */
    icon?: string
    /** 按钮文本 */
    text?: string
    /** 按钮类型 */
    type?: 'primary' | 'success' | 'warning' | 'danger' | 'info'
    /** 权限标识，支持单个或多个权限 */
    permission?: string | string[]
    /** 鼠标悬停提示文字 */
    tooltip?: string
    /** 是否显示，支持函数动态判断 */
    show?: boolean | ((row: any) => boolean)
    /** 是否禁用，支持函数动态判断 */
    disabled?: boolean | ((row: any) => boolean)
    /** 点击事件回调 */
    onClick: (row: any, index: number) => void
  }

  /**
   * 表格列配置接口
   * @description 用于 ATable 组件的列配置，扩展自 FieldConfig 的部分属性
   */
  declare interface TableColumnConfig {
    /** 字段属性名，支持嵌套如 'user.name' */
    prop: string
    /** 列标签 */
    label: string
    /** 列宽度 */
    width?: number | string
    /** 最小宽度 */
    minWidth?: number | string
    /** 对齐方式 */
    align?: 'left' | 'center' | 'right'
    /** 是否固定列 */
    fixed?: 'left' | 'right' | boolean
    /** 是否可排序 */
    sortable?: boolean | 'custom'
    /** 是否超出隐藏并显示 tooltip */
    showOverflowTooltip?: boolean
    /** 自定义插槽名称 */
    slot?: string
    /** 自定义格式化函数 */
    formatter?: (value: any, row: any) => string
    /** 数据类型，用于自动渲染 */
    type?: 'text' | 'dict' | 'switch' | 'image' | 'datetime' | 'date' | 'actions' | 'copyable' | 'currency' | 'boolean'
    /** 字典选项，当 type 为 dict 时使用，支持普通数组或响应式引用 */
    dictOptions?: DictItem[] | import('vue').Ref<DictItem[]>
    /** 图片预览配置，当 type 为 image 时使用 */
    imageConfig?: {
      /** 宽度 */
      width?: number | string
      /** 高度 */
      height?: number | string
      /** 是否显示所有图片 */
      showAll?: boolean
    }
    /** 是否隐藏列 */
    hidden?: boolean
    /** 操作按钮配置，当 type 为 actions 时使用 */
    actions?: TableActionConfig[]
    /** 开关变化回调，当 type 为 switch 时使用 */
    onSwitchChange?: (row: any, value: any) => void
    /** 开关禁用条件，当 type 为 switch 时使用 */
    switchDisabled?: boolean | ((row: any) => boolean)
  }

  /**
   * 响应式 Span 配置接口
   * @description 用于栅格布局的响应式配置，支持不同屏幕尺寸下的列数设置
   */
  declare interface ResponsiveSpan {
    /** 超小屏幕 <768px */
    xs?: number
    /** 小屏幕 ≥768px */
    sm?: number
    /** 中等屏幕 ≥992px */
    md?: number
    /** 大屏幕 ≥1200px */
    lg?: number
    /** 超大屏幕 ≥1920px */
    xl?: number
  }

  /**
   * Span 属性类型
   * @description 支持固定数字、数字字符串、响应式对象或自动模式
   * - number: 固定数值，如 12
   * - string: 数字字符串如 "12"，或预设值 "auto"
   * - ResponsiveSpan: 响应式配置对象
   * - undefined: 不使用栅格
   */
  declare type SpanType = number | string | ResponsiveSpan | undefined
}

/**
 * 空导出语句
 * 确保此文件被 TypeScript 视为模块而非全局脚本
 */
export {}
