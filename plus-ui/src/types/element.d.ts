/**
 * Element Plus 类型全局声明
 * @description 将 Element Plus 组件及其实例类型定义为全局类型，简化在项目中的使用
 * @author 项目团队
 * @example 以下声明不需要引入ElUploadInstance直接使用
 * const uploadRef = ref<ElUploadInstance>()
 * 现在可以安全地调用上传组件的方法
 * uploadRef.value?.submit()
 */
import type * as ep from 'element-plus'

declare global {
  /**
   * Element Plus 样式类型
   * @description 定义各种组件的样式类型变体，用于控制组件的外观样式
   */
  /**
   * 标签类型，用于 el-tag 组件的 type 属性
   * @component el-tag
   * @property type
   * @example <el-tag type="success">标签</el-tag>
   */
  declare type ElTagType = 'primary' | 'success' | 'info' | 'warning' | 'danger'

  /**
   * 按钮类型，用于 el-button 组件的 type 属性
   * @component el-button
   * @property type
   * @example <el-button type="primary">按钮</el-button>
   */
  declare type ElButtonType = '' | 'default' | 'primary' | 'success' | 'warning' | 'danger' | 'info' | 'text'

  /**
   * 消息类型，用于 ElMessage 的类型
   * @function ElMessage
   * @property type
   * @example ElMessage.success('操作成功')
   */
  declare type ElMessageType = 'success' | 'warning' | 'info' | 'error'

  /**
   * 通知类型，用于 ElNotification 的类型
   * @function ElNotification
   * @property type
   * @example ElNotification({ type: 'success', message: '通知内容' })
   */
  declare type ElNotificationType = 'success' | 'warning' | 'info' | 'error'

  /**
   * 组件尺寸，用于控制组件的大小
   * @global
   * @property size
   * @example <el-input size="small" />
   */
  declare type ElSize = 'large' | 'default' | 'small'

  /**
   * 效果类型，用于标签等显示效果
   * @global
   * @property effect
   * @example <el-tag effect="light" />
   */
  declare type ElEffect = 'light' | 'dark' | 'plain'

  /**
   * Element Plus 表单组件实例类型
   * @description 表单相关组件的实例类型，用于模板引用和组件方法调用
   */
  /**
   * Form 表单组件实例，用于表单验证、重置等操作
   * @component el-form
   * @instance
   * @example const formRef = ref<ElFormInstance>(); formRef.value?.validate()
   */
  declare type ElFormInstance = ep.FormInstance

  /**
   * Input 输入框组件实例，用于获取焦点、选中内容等
   * @component el-input
   * @instance
   * @example const inputRef = ref<ElInputInstance>(); inputRef.value?.focus()
   */
  declare type ElInputInstance = ep.InputInstance

  /**
   * InputNumber 数字输入框组件实例
   * @component el-input-number
   * @instance
   * @example const inputNumberRef = ref<ElInputNumberInstance>(); inputNumberRef.value?.focus()
   */
  declare type ElInputNumberInstance = ep.InputNumberInstance

  /**
   * Select 选择器组件实例
   * @component el-select
   * @instance
   * @example const selectRef = ref<ElSelectInstance>(); selectRef.value?.focus()
   */
  declare type ElSelectInstance = InstanceType<typeof ep.ElSelect>

  /**
   * Option 选项组件实例
   * @component el-option
   * @instance
   * @example const optionRef = ref<ElOptionInstance>()
   */
  declare type ElOptionInstance = InstanceType<typeof ep.ElOption>

  /**
   * OptionGroup 选项分组组件实例
   * @component el-option-group
   * @instance
   * @example const optionGroupRef = ref<ElOptionGroupInstance>()
   */
  declare type ElOptionGroupInstance = InstanceType<typeof ep.ElOptionGroup>

  /**
   * Radio 单选框组件实例
   * @component el-radio
   * @instance
   * @example const radioRef = ref<ElRadioInstance>()
   */
  declare type ElRadioInstance = ep.RadioInstance

  /**
   * RadioGroup 单选框组组件实例
   * @component el-radio-group
   * @instance
   * @example const radioGroupRef = ref<ElRadioGroupInstance>()
   */
  declare type ElRadioGroupInstance = ep.RadioGroupInstance

  /**
   * RadioButton 单选按钮组件实例
   * @component el-radio-button
   * @instance
   * @example const radioButtonRef = ref<ElRadioButtonInstance>()
   */
  declare type ElRadioButtonInstance = ep.RadioButtonInstance

  /**
   * Checkbox 复选框组件实例
   * @component el-checkbox
   * @instance
   * @example const checkboxRef = ref<ElCheckboxInstance>()
   */
  declare type ElCheckboxInstance = ep.CheckboxInstance

  /**
   * CheckboxGroup 复选框组组件实例
   * @component el-checkbox-group
   * @instance
   * @example const checkboxGroupRef = ref<ElCheckboxGroupInstance>()
   */
  declare type ElCheckboxGroupInstance = InstanceType<typeof ep.ElCheckboxGroup>

  /**
   * Switch 开关组件实例
   * @component el-switch
   * @instance
   * @example const switchRef = ref<ElSwitchInstance>()
   */
  declare type ElSwitchInstance = ep.SwitchInstance

  /**
   * Cascader 级联选择器组件实例
   * @component el-cascader
   * @instance
   * @example const cascaderRef = ref<ElCascaderInstance>()
   */
  declare type ElCascaderInstance = ep.CascaderInstance

  /**
   * Slider 滑块组件实例
   * @component el-slider
   * @instance
   * @example const sliderRef = ref<ElSliderInstance>()
   */
  declare type ElSliderInstance = ep.SliderInstance

  /**
   * TimePicker 时间选择器组件实例
   * @component el-time-picker
   * @instance
   * @example const timePickerRef = ref<ElTimePickerInstance>()
   */
  declare type ElTimePickerInstance = InstanceType<typeof ep.ElTimePicker>

  /**
   * TimeSelect 时间选择器组件实例
   * @component el-time-select
   * @instance
   * @example const timeSelectRef = ref<ElTimeSelectInstance>()
   */
  declare type ElTimeSelectInstance = InstanceType<typeof ep.ElTimeSelect>

  /**
   * DatePicker 日期选择器组件实例
   * @component el-date-picker
   * @instance
   * @example const datePickerRef = ref<ElDatePickerInstance>()
   */
  declare type ElDatePickerInstance = InstanceType<typeof ep.ElDatePicker>

  /**
   * Rate 评分组件实例
   * @component el-rate
   * @instance
   * @example const rateRef = ref<ElRateInstance>()
   */
  declare type ElRateInstance = ep.RateInstance

  /**
   * ColorPicker 颜色选择器组件实例
   * @component el-color-picker
   * @instance
   * @example const colorPickerRef = ref<ElColorPickerInstance>()
   */
  declare type ElColorPickerInstance = ep.ColorPickerInstance

  /**
   * Transfer 穿梭框组件实例
   * @component el-transfer
   * @instance
   * @example const transferRef = ref<ElTransferInstance>()
   */
  declare type ElTransferInstance = InstanceType<typeof ep.ElTransfer>

  /**
   * Element Plus 数据展示组件实例类型
   * @description 用于数据展示的组件实例类型
   */
  /**
   * Table 表格组件实例，用于表格刷新、排序、选择等操作
   * @component el-table
   * @instance
   * @example const tableRef = ref<ElTableInstance>(); tableRef.value?.clearSelection()
   */
  declare type ElTableInstance = ep.TableInstance

  /**
   * TableColumn 表格列组件实例
   * @component el-table-column
   * @instance
   * @example const tableColumnRef = ref<ElTableColumnInstance>()
   */
  declare type ElTableColumnInstance = InstanceType<typeof ep.ElTableColumn>

  /**
   * Pagination 分页组件实例
   * @component el-pagination
   * @instance
   * @example const paginationRef = ref<ElPaginationInstance>()
   */
  declare type ElPaginationInstance = InstanceType<typeof ep.ElPagination>

  /**
   * Tree 树形控件组件实例
   * @component el-tree
   * @instance
   * @example const treeRef = ref<ElTreeInstance>(); treeRef.value?.filter('关键字')
   */
  declare type ElTreeInstance = InstanceType<typeof ep.ElTree>

  /**
   * TreeSelect 树选择组件实例
   * @component el-tree-select
   * @instance
   * @example const treeSelectRef = ref<ElTreeSelectInstance>()
   */
  declare type ElTreeSelectInstance = InstanceType<typeof ep.ElTreeSelect>

  /**
   * Tag 标签组件实例
   * @component el-tag
   * @instance
   * @example const tagRef = ref<ElTagInstance>()
   */
  declare type ElTagInstance = InstanceType<typeof ep.ElTag>

  /**
   * Badge 徽章组件实例
   * @component el-badge
   * @instance
   * @example const badgeRef = ref<ElBadgeInstance>()
   */
  declare type ElBadgeInstance = InstanceType<typeof ep.ElBadge>

  /**
   * Skeleton 骨架屏组件实例
   * @component el-skeleton
   * @instance
   * @example const skeletonRef = ref<ElSkeletonInstance>()
   */
  declare type ElSkeletonInstance = InstanceType<typeof ep.ElSkeleton>

  /**
   * SkeletonItem 骨架屏子项组件实例
   * @component el-skeleton-item
   * @instance
   * @example const skeletonItemRef = ref<ElSkeletonItemInstance>()
   */
  declare type ElSkeletonItemInstance = InstanceType<typeof ep.ElSkeletonItem>

  /**
   * Empty 空状态组件实例
   * @component el-empty
   * @instance
   * @example const emptyRef = ref<ElEmptyInstance>()
   */
  declare type ElEmptyInstance = InstanceType<typeof ep.ElEmpty>

  /**
   * Descriptions 描述列表组件实例
   * @component el-descriptions
   * @instance
   * @example const descriptionsRef = ref<ElDescriptionsInstance>()
   */
  declare type ElDescriptionsInstance = InstanceType<typeof ep.ElDescriptions>

  /**
   * DescriptionsItem 描述列表项组件实例
   * @component el-descriptions-item
   * @instance
   * @example const descriptionsItemRef = ref<ElDescriptionsItemInstance>()
   */
  declare type ElDescriptionsItemInstance = InstanceType<typeof ep.ElDescriptionsItem>

  /**
   * Result 结果组件实例
   * @component el-result
   * @instance
   * @example const resultRef = ref<ElResultInstance>()
   */
  declare type ElResultInstance = InstanceType<typeof ep.ElResult>

  /**
   * Statistic 统计数值组件实例
   * @component el-statistic
   * @instance
   * @example const statisticRef = ref<ElStatisticInstance>()
   */
  declare type ElStatisticInstance = InstanceType<typeof ep.ElStatistic>

  /**
   * Element Plus 导航组件实例类型
   * @description 用于导航的组件实例类型
   */
  /**
   * Menu 菜单组件实例
   * @component el-menu
   * @instance
   * @example const menuRef = ref<ElMenuInstance>()
   */
  declare type ElMenuInstance = InstanceType<typeof ep.ElMenu>

  /**
   * MenuItem 菜单项组件实例
   * @component el-menu-item
   * @instance
   * @example const menuItemRef = ref<ElMenuItemInstance>()
   */
  declare type ElMenuItemInstance = InstanceType<typeof ep.ElMenuItem>

  /**
   * SubMenu 子菜单组件实例
   * @component el-sub-menu
   * @instance
   * @example const subMenuRef = ref<ElSubMenuInstance>()
   */
  declare type ElSubMenuInstance = InstanceType<typeof ep.ElSubMenu>

  /**
   * MenuItemGroup 菜单项分组组件实例
   * @component el-menu-item-group
   * @instance
   * @example const menuItemGroupRef = ref<ElMenuItemGroupInstance>()
   */
  declare type ElMenuItemGroupInstance = InstanceType<typeof ep.ElMenuItemGroup>

  /**
   * Breadcrumb 面包屑组件实例
   * @component el-breadcrumb
   * @instance
   * @example const breadcrumbRef = ref<ElBreadcrumbInstance>()
   */
  declare type ElBreadcrumbInstance = InstanceType<typeof ep.ElBreadcrumb>

  /**
   * BreadcrumbItem 面包屑项组件实例
   * @component el-breadcrumb-item
   * @instance
   * @example const breadcrumbItemRef = ref<ElBreadcrumbItemInstance>()
   */
  declare type ElBreadcrumbItemInstance = InstanceType<typeof ep.ElBreadcrumbItem>

  /**
   * PageHeader 页头组件实例
   * @component el-page-header
   * @instance
   * @example const pageHeaderRef = ref<ElPageHeaderInstance>()
   */
  declare type ElPageHeaderInstance = InstanceType<typeof ep.ElPageHeader>

  /**
   * Dropdown 下拉菜单组件实例
   * @component el-dropdown
   * @instance
   * @example const dropdownRef = ref<ElDropdownInstance>()
   */
  declare type ElDropdownInstance = InstanceType<typeof ep.ElDropdown>

  /**
   * DropdownItem 下拉菜单项组件实例
   * @component el-dropdown-item
   * @instance
   * @example const dropdownItemRef = ref<ElDropdownItemInstance>()
   */
  declare type ElDropdownItemInstance = InstanceType<typeof ep.ElDropdownItem>

  /**
   * DropdownMenu 下拉菜单组件实例
   * @component el-dropdown-menu
   * @instance
   * @example const dropdownMenuRef = ref<ElDropdownMenuInstance>()
   */
  declare type ElDropdownMenuInstance = InstanceType<typeof ep.ElDropdownMenu>

  /**
   * Steps 步骤条组件实例
   * @component el-steps
   * @instance
   * @example const stepsRef = ref<ElStepsInstance>()
   */
  declare type ElStepsInstance = InstanceType<typeof ep.ElSteps>

  /**
   * Step 步骤项组件实例
   * @component el-step
   * @instance
   * @example const stepRef = ref<ElStepInstance>()
   */
  declare type ElStepInstance = InstanceType<typeof ep.ElStep>

  /**
   * Tabs 标签页组件实例
   * @component el-tabs
   * @instance
   * @example const tabsRef = ref<ElTabsInstance>()
   */
  declare type ElTabsInstance = InstanceType<typeof ep.ElTabs>

  /**
   * TabPane 标签页面板组件实例
   * @component el-tab-pane
   * @instance
   * @example const tabPaneRef = ref<ElTabPaneInstance>()
   */
  declare type ElTabPaneInstance = InstanceType<typeof ep.ElTabPane>

  /**
   * Element Plus 反馈组件实例类型
   * @description 用于用户反馈的组件实例类型
   */
  /**
   * Alert 警告组件实例
   * @component el-alert
   * @instance
   * @example const alertRef = ref<ElAlertInstance>()
   */
  declare type ElAlertInstance = InstanceType<typeof ep.ElAlert>

  /**
   * Dialog 对话框组件实例
   * @component el-dialog
   * @instance
   * @example const dialogRef = ref<ElDialogInstance>()
   */
  declare type ElDialogInstance = InstanceType<typeof ep.ElDialog>

  /**
   * Drawer 抽屉组件实例
   * @component el-drawer
   * @instance
   * @example const drawerRef = ref<ElDrawerInstance>()
   */
  declare type ElDrawerInstance = InstanceType<typeof ep.ElDrawer>

  /**
   * Popover 弹出框组件实例
   * @component el-popover
   * @instance
   * @example const popoverRef = ref<ElPopoverInstance>()
   */
  declare type ElPopoverInstance = InstanceType<typeof ep.ElPopover>

  /**
   * Popconfirm 气泡确认框组件实例
   * @component el-popconfirm
   * @instance
   * @example const popconfirmRef = ref<ElPopconfirmInstance>()
   */
  declare type ElPopconfirmInstance = InstanceType<typeof ep.ElPopconfirm>

  /**
   * Progress 进度条组件实例
   * @component el-progress
   * @instance
   * @example const progressRef = ref<ElProgressInstance>()
   */
  declare type ElProgressInstance = InstanceType<typeof ep.ElProgress>

  /**
   * Tooltip 文字提示组件实例
   * @component el-tooltip
   * @instance
   * @example const tooltipRef = ref<ElTooltipInstance>()
   */
  declare type ElTooltipInstance = InstanceType<typeof ep.ElTooltip>

  /**
   * Image 图片组件实例
   * @component el-image
   * @instance
   * @example const imageRef = ref<ElImageInstance>()
   */
  declare type ElImageInstance = InstanceType<typeof ep.ElImage>

  /**
   * Carousel 走马灯组件实例
   * @component el-carousel
   * @instance
   * @example const carouselRef = ref<ElCarouselInstance>()
   */
  declare type ElCarouselInstance = InstanceType<typeof ep.ElCarousel>

  /**
   * CarouselItem 走马灯项组件实例
   * @component el-carousel-item
   * @instance
   * @example const carouselItemRef = ref<ElCarouselItemInstance>()
   */
  declare type ElCarouselItemInstance = InstanceType<typeof ep.ElCarouselItem>

  /**
   * Collapse 折叠面板组件实例
   * @component el-collapse
   * @instance
   * @example const collapseRef = ref<ElCollapseInstance>()
   */
  declare type ElCollapseInstance = InstanceType<typeof ep.ElCollapse>

  /**
   * CollapseItem 折叠面板项组件实例
   * @component el-collapse-item
   * @instance
   * @example const collapseItemRef = ref<ElCollapseItemInstance>()
   */
  declare type ElCollapseItemInstance = InstanceType<typeof ep.ElCollapseItem>

  /**
   * Timeline 时间线组件实例
   * @component el-timeline
   * @instance
   * @example const timelineRef = ref<ElTimelineInstance>()
   */
  declare type ElTimelineInstance = InstanceType<typeof ep.ElTimeline>

  /**
   * TimelineItem 时间线项组件实例
   * @component el-timeline-item
   * @instance
   * @example const timelineItemRef = ref<ElTimelineItemInstance>()
   */
  declare type ElTimelineItemInstance = InstanceType<typeof ep.ElTimelineItem>

  /**
   * Element Plus 布局组件实例类型
   * @description 用于页面布局的组件实例类型
   */
  /**
   * Divider 分割线组件实例
   * @component el-divider
   * @instance
   * @example const dividerRef = ref<ElDividerInstance>()
   */
  declare type ElDividerInstance = InstanceType<typeof ep.ElDivider>

  /**
   * Card 卡片组件实例
   * @component el-card
   * @instance
   * @example const cardRef = ref<ElCardInstance>()
   */
  declare type ElCardInstance = InstanceType<typeof ep.ElCard>

  /**
   * Calendar 日历组件实例
   * @component el-calendar
   * @instance
   * @example const calendarRef = ref<ElCalendarInstance>()
   */
  declare type ElCalendarInstance = InstanceType<typeof ep.ElCalendar>

  /**
   * CollapseTransition 折叠过渡组件实例
   * @component el-collapse-transition
   * @instance
   * @example const collapseTransitionRef = ref<ElCollapseTransitionInstance>()
   */
  declare type ElCollapseTransitionInstance = InstanceType<typeof ep.ElCollapseTransition>

  /**
   * Container 布局容器组件实例
   * @component el-container
   * @instance
   * @example const containerRef = ref<ElContainerInstance>()
   */
  declare type ElContainerInstance = InstanceType<typeof ep.ElContainer>

  /**
   * Header 页头容器组件实例
   * @component el-header
   * @instance
   * @example const headerRef = ref<ElHeaderInstance>()
   */
  declare type ElHeaderInstance = InstanceType<typeof ep.ElHeader>

  /**
   * Main 主要区域容器组件实例
   * @component el-main
   * @instance
   * @example const mainRef = ref<ElMainInstance>()
   */
  declare type ElMainInstance = InstanceType<typeof ep.ElMain>

  /**
   * Aside 侧边栏容器组件实例
   * @component el-aside
   * @instance
   * @example const asideRef = ref<ElAsideInstance>()
   */
  declare type ElAsideInstance = InstanceType<typeof ep.ElAside>

  /**
   * Footer 页脚容器组件实例
   * @component el-footer
   * @instance
   * @example const footerRef = ref<ElFooterInstance>()
   */
  declare type ElFooterInstance = InstanceType<typeof ep.ElFooter>

  /**
   * Row 行布局组件实例
   * @component el-row
   * @instance
   * @example const rowRef = ref<ElRowInstance>()
   */
  declare type ElRowInstance = InstanceType<typeof ep.ElRow>

  /**
   * Col 列布局组件实例
   * @component el-col
   * @instance
   * @example const colRef = ref<ElColInstance>()
   */
  declare type ElColInstance = InstanceType<typeof ep.ElCol>

  /**
   * Space 间距组件实例
   * @component el-space
   * @instance
   * @example const spaceRef = ref<ElSpaceInstance>()
   */
  declare type ElSpaceInstance = InstanceType<typeof ep.ElSpace>

  /**
   * Affix 固钉组件实例
   * @component el-affix
   * @instance
   * @example const affixRef = ref<ElAffixInstance>()
   */
  declare type ElAffixInstance = InstanceType<typeof ep.ElAffix>

  /**
   * Element Plus 其他组件实例类型
   * @description 不属于上述分类的其他组件实例类型
   */
  /**
   * Button 按钮组件实例
   * @component el-button
   * @instance
   * @example const buttonRef = ref<ElButtonInstance>()
   */
  declare type ElButtonInstance = InstanceType<typeof ep.ElButton>

  /**
   * ButtonGroup 按钮组组件实例
   * @component el-button-group
   * @instance
   * @example const buttonGroupRef = ref<ElButtonGroupInstance>()
   */
  declare type ElButtonGroupInstance = InstanceType<typeof ep.ElButtonGroup>

  /**
   * Link 链接组件实例
   * @component el-link
   * @instance
   * @example const linkRef = ref<ElLinkInstance>()
   */
  declare type ElLinkInstance = InstanceType<typeof ep.ElLink>

  /**
   * Text 文本组件实例
   * @component el-text
   * @instance
   * @example const textRef = ref<ElTextInstance>()
   */
  declare type ElTextInstance = InstanceType<typeof ep.ElText>

  /**
   * Icon 图标组件实例
   * @component el-icon
   * @instance
   * @example const iconRef = ref<ElIconInstance>()
   */
  declare type ElIconInstance = InstanceType<typeof ep.ElIcon>

  /**
   * Avatar 头像组件实例
   * @component el-avatar
   * @instance
   * @example const avatarRef = ref<ElAvatarInstance>()
   */
  declare type ElAvatarInstance = InstanceType<typeof ep.ElAvatar>

  /**
   * Backtop 回到顶部组件实例
   * @component el-backtop
   * @instance
   * @example const backtopRef = ref<ElBacktopInstance>()
   */
  declare type ElBacktopInstance = InstanceType<typeof ep.ElBacktop>

  /**
   * InfiniteScroll 无限滚动组件实例
   * @component el-infinite-scroll
   * @instance
   * @example const infiniteScrollRef = ref<ElInfiniteScrollInstance>()
   */
  declare type ElInfiniteScrollInstance = InstanceType<typeof ep.ElInfiniteScroll>

  /**
   * Loading 加载组件实例
   * @component el-loading
   * @instance
   * @example const loadingInstance = ElLoading.service()
   */
  declare type ElLoadingInstance = ep.LoadingInstance

  /**
   * Scrollbar 滚动条组件实例
   * @component el-scrollbar
   * @instance
   * @example const scrollbarRef = ref<ElScrollbarInstance>(); scrollbarRef.value?.setScrollTop(0)
   */
  declare type ElScrollbarInstance = ep.ScrollbarInstance

  /**
   * Upload 上传组件实例
   * @component el-upload
   * @instance
   * @example const uploadRef = ref<ElUploadInstance>(); uploadRef.value?.submit()
   */
  declare type ElUploadInstance = ep.UploadInstance

  /**
   * Element Plus 属性和配置类型
   * @description 组件的属性类型、配置选项等
   */
  /**
   * Transfer 穿梭框的 key 类型
   * @component el-transfer
   * @property key
   * @example const keys: ElTransferKey[] = ['key1', 'key2']
   */
  declare type ElTransferKey = ep.TransferKey

  /**
   * Checkbox 复选框的值类型
   * @component el-checkbox
   * @property value/modelValue
   * @example const value: ElCheckboxValueType = true
   */
  declare type ElCheckboxValueType = ep.CheckboxValueType

  /**
   * Form 表单验证规则类型
   * @component el-form
   * @property rules
   * @example const rules: ElFormRules = { name: [{ required: true, message: '必填项' }] }
   */
  declare type ElFormRules = ep.FormRules

  /**
   * DatePicker 日期选择器模型类型
   * @component el-date-picker
   * @property modelValue
   * @example const date: ElDateModelType = new Date()
   */
  declare type ElDateModelType = ep.ElDateModelType

  /**
   * Upload 上传文件类型
   * @component el-upload
   * @property file
   * @example const file: ElUploadFile = { name: 'file.jpg', url: '...' }
   */
  declare type ElUploadFile = ep.UploadFile

  /**
   * Upload 上传文件列表类型
   * @component el-upload
   * @property fileList
   * @example const files: ElUploadFiles = [{ name: 'file.jpg', url: '...' }]
   */
  declare type ElUploadFiles = ep.UploadFiles

  /**
   * Upload 用户上传文件类型
   * @component el-upload
   * @property internal
   * @example const userFile: ElUploadUserFile = { name: 'file.jpg' }
   */
  declare type ElUploadUserFile = ep.UploadUserFile

  /**
   * Upload 原始上传文件类型
   * @component el-upload
   * @property internal
   * @example const rawFile: ElUploadRawFile
   */
  declare type ElUploadRawFile = ep.UploadRawFile

  /**
   * Table 表格属性类型
   * @component el-table
   * @property props
   * @example const tableProps: ElTableProps<User> = { border: true }
   */
  declare type ElTableProps = ep.TableProps

  /**
   * TableColumn 表格列属性类型
   * @component el-table-column
   * @property props
   * @example const columnProps: ElTableColumnProps = { width: '100px' }
   */
  declare type ElTableColumnProps = ep.TableColumnProps

  /**
   * Form 表单属性类型
   * @component el-form
   * @property props
   * @example const formProps: ElFormProps = { labelWidth: '100px' }
   */
  declare type ElFormProps = ep.FormProps

  /**
   * Input 输入框属性类型
   * @component el-input
   * @property props
   * @example const inputProps: ElInputProps = { placeholder: '请输入' }
   */
  declare type ElInputProps = ep.InputProps

  /**
   * Select 选择器属性类型
   * @component el-select
   * @property props
   * @example const selectProps: ElSelectProps = { multiple: true }
   */
  declare type ElSelectProps = ep.SelectProps

  /**
   * DatePicker 日期选择器属性类型
   * @component el-date-picker
   * @property props
   * @example const dateProps: ElDatePickerProps = { type: 'date' }
   */
  declare type ElDatePickerProps = ep.DatePickerProps

  /**
   * Tree 树形控件属性类型
   * @component el-tree
   * @property props
   * @example const treeProps: ElTreeProps = { nodeKey: 'id' }
   */
  declare type ElTreeProps = ep.TreeProps

  /**
   * Tree 树节点数据类型
   * @component el-tree
   * @property data item
   * @example const node: ElTreeNodeData = { label: '节点', children: [] }
   */
  declare type ElTreeNodeData = ep.TreeNodeData

  /**
   * Tree 树节点类型
   * @component el-tree
   * @property node
   * @example const node: ElTreeNode
   */
  declare type ElTreeNode = ep.TreeNode

  /**
   * Loading 加载配置选项类型
   * @function ElLoading.service
   * @property options
   * @example const options: ElLoadingOptions = { text: '加载中' }
   */
  declare type ElLoadingOptions = ep.LoadingOptions

  /**
   * Notification 通知配置选项类型
   * @function ElNotification
   * @property options
   * @example const options: ElNotificationOptions = { title: '通知', message: '内容' }
   */
  declare type ElNotificationOptions = ep.NotificationOptions

  /**
   * Message 消息配置选项类型
   * @function ElMessage
   * @property options
   * @example const options: ElMessageOptions = { message: '提示内容' }
   */
  declare type ElMessageOptions = ep.MessageOptions

  /**
   * MessageBox 消息框配置选项类型
   * @function ElMessageBox.confirm
   * @property options
   * @example const options: ElMessageBoxOptions = { title: '提示' }
   */
  declare type ElMessageBoxOptions = ep.ElMessageBoxOptions

  /**
   * TableColumn 列上下文类型
   * @component el-table-column
   * @property internal
   * @example function formatter(row, column: TableColumnCtx<User>, cellValue) {...}
   */
  declare type ElTableColumnCtx<T> = ep.TableColumnCtx<T>

  /**
   * 排序顺序类型
   * @component el-table
   * @property sort
   * @example const order: ElSortOrder = 'ascending'
   */
  declare type ElSortOrder = ep.SortOrder

  /**
   * Popper 弹出配置选项类型
   * @component el-popper
   * @property options
   * @example const popperOptions: ElPopperOptions = { modifiers: [...] }
   */
  declare type ElPopperOptions = ep.ElPopperOptions

  /**
   * Element Plus 全局配置和尺寸类型
   * @description 全局配置和组件尺寸相关类型
   */
  /**
   * 组件尺寸类型
   * @global component size
   * @property size
   * @example const size: ElComponentSize = 'small'
   */
  declare type ElComponentSize = ep.ComponentSize

  /**
   * DatePicker 日期选择器类型，用于控制日期选择器的展示模式
   * @component el-date-picker
   * @property type
   * @example <el-date-picker type="date" />
   */
  declare type ElDatePickerType =
    | 'year'
    | 'years'
    | 'month'
    | 'months'
    | 'date'
    | 'dates'
    | 'week'
    | 'datetime'
    | 'datetimerange'
    | 'daterange'
    | 'monthrange'
    | 'yearrange'

  /**
   * 全局配置类型
   * @component el-config-provider
   * @property props
   * @example const config: ElConfig = { size: 'small', zIndex: 3000 }
   */
  declare type ElConfig = ep.ConfigProviderProps
}
