// 中文语言包
export default {
  '操作': '操作',
  '新增': '新增',
  '修改': '修改',
  '删除': '删除',
  '查看': '查看',
  '导出': '导出',
  '导入': '导入',
  '刷新': '刷新',
  '确定': '确定',
  '取消': '取消',
  '启用': '启用',
  '停用': '停用',
  '成功': '成功',
  '是否确认': '是否确认',
  '折叠': '折叠',
  '展开': '展开',
  '共': '共',
  '条': '条',
  '搜索': '搜索...',
  '是否确认删除': '是否确认删除下列数据:',
  '请先保存主表': '请先保存主表信息后，子表功能将自动开启',
  /** 按钮权限系统 */
  button: {
    // 通用按钮
    query: '查询',
    add: '新增',
    update: '修改',
    delete: '删除',
    export: '导出',
    import: '导入',
    list: '列表',
    confirm: '确定',
    cancel: '取消',
    save: '保存',
    upload: '上传',
    download: '下载',
    preview: '预览',
    view: '查看',
    generate: '生成',
    more: '更多',
    downloadTemplate: '下载模板',
    importData: '导入数据',
    exportData: '导出数据',
    collapse: '收起',
    expand: '展开',
    close: '关闭',
    sync: '同步',
    submit: '提交',

    // 系统管理特殊按钮
    resetPwd: '重置密码',
    unlock: '账户解锁',
    clean: '清空',
    refreshCache: '刷新缓存',
    syncTenantRoles: '同步租户角色',
    syncTenantDicts: '同步租户字典',
    syncTenantConfigs: '同步租户参数配置',
    unbind: '解绑',

    // 监控特殊按钮
    batchLogout: '批量强退',
    forceLogout: '单条强退',

    // 开发工具特殊按钮
    importCode: '导入代码',
    previewCode: '预览代码',
    generateCode: '生成代码'
  },

  /** 弹窗提示 */
  dialog: {
    add: '新增',
    edit: '修改',
    delete: '删除',
    query: '查询',
    detail: '详情',
    import: '导入',
    export: '导出',
    config: '配置',
    close: '关闭'
  },

  /** 消息提示 */
  message: {
    operation: '操作',

    // 操作确认类消息
    confirmDelete: '是否确认删除下列数据:',
    confirmCancel: '是否确认取消操作?',
    confirmSubmit: '是否确认提交?',
    confirm: '是否确认',
    enable: '启用',
    disable: '禁用',

    // 操作结果类消息
    success: '操作成功',
    error: '操作失败',

    // 具体操作结果消息
    saveSuccess: '保存成功',
    saveError: '保存失败',
    addSuccess: '新增成功',
    addError: '新增失败',
    updateSuccess: '修改成功',
    updateError: '修改失败',
    deleteSuccess: '删除成功',
    deleteError: '删除失败',
    importSuccess: '导入成功',
    importError: '导入失败',
    exportSuccess: '导出成功',
    exportError: '导出失败',
    confirmCleanAll: '确认删除所有数据?',
    confirmUnlock: '确认账户解锁 ',
    unlockSuccess: '账户解锁成功 ',
    unbindSuccess: '解绑成功',
    visible: '显示',
    invisible: '隐藏',
    assignSuccess: '分配成功',
    cancelSuccess: '取消授权成功',
    confirmCancelSelected: '是否取消选中用户授权数据项?',
    generateSuccess: '生成成功',
    resetSuccess: '重置成功',

    // 提示类消息
    noData: '暂无数据',
    invalidParams: '参数无效',
    networkError: '网络错误，请稍后重试',
    unauthorized: '您没有操作权限',
    sessionExpired: '会话已过期，请重新登录',

    saveMainFirst: '请先保存主表信息后，子表功能将自动开启',
    total: '共',
    items: '条',
    search: '搜索...',

    confirmForceLogout: '确认强制退出',
    confirmBatchForceLogout: '确认批量强制退出'
  },

  /** 弹窗/通知 (modal.ts) */
  modal: {
    systemPrompt: '系统提示',
    notification: '通知',
    error: '错误',
    success: '成功',
    warning: '警告',
    ok: '确定',
    cancel: '取消'
  },

  /** 占位符 */
  placeholder: {
    input: '请输入',
    select: '请选择'
  },

  /** 提示*/
  tooltip: {
    showSearch: '显示搜索',
    print: '打印',
    hideSearch: '隐藏搜索',
    resetSearch: '重置搜索',
    refresh: '刷新',
    columns: '显示/隐藏列',
    columnSettings: '列设置'
  },

  /** 列设置 */
  columnSettings: {
    title: '列设置',
    reset: '重置',
    dragHint: '拖拽可排序',
    resetSuccess: '列配置已重置'
  },

  /** 文件预览组件 */
  filePreview: {
    videoNotSupported: '您的浏览器不支持视频标签',
    audioNotSupported: '您的浏览器不支持音频标签',
    officeDocument: 'Office 文档',
    officePreviewRequiresPublicUrl: 'Office 在线预览需要公网可访问的文件地址',
    localNetworkTip: '当前为本地/内网环境，Microsoft Office Viewer 无法访问文件。',
    downloadToOpen: '请下载后使用本地 Office 软件打开。',
    downloadFile: '下载文件',
    loading: '加载中...',
    previewNotSupported: '该文件类型暂不支持在线预览',
    loadFailed: '加载文件内容失败',
    loadFailedWithError: '加载文件内容失败: {error}'
  },

  /** 图标选择组件 */
  iconSelect: {
    placeholder: '点击选择图标',
    searchPlaceholder: '搜索图标 (代码或名称)',
    totalIcons: '共 {count} 个图标'
  },

  /** 充值组件 */
  recharge: {
    title: '在线充值(系统自动充值)',
    paymentCompleted: '我已完成支付',
    rechargeAmount: '充值金额',
    yuan: '元',
    paymentMethod: '支付方式',
    wechatPay: '微信支付',
    alipay: '支付宝支付',
    wechat: '微信',
    alipayShort: '支付宝',
    scanToPay: '{method}扫码，支付{amount}元',
    refreshQrCode: '重新获取',
    paymentProblem: '支付遇到问题',
    refund: '退款',
    rechargePrefix: '充值',
    onlineRecharge: '在线充值',
    generateQrCodeFailed: '生成支付二维码失败',
    createOrderFailed: '创建订单失败',
    selectRechargeAmount: '请选择充值金额',
    paymentSuccess: '支付成功',
    copiedContact: '已复制qq/wx: 770492966, 请添加联系~',
    noRefundableOrder: '没有可退款的订单',
    confirmRefund: '确定要退款吗？退款后订单将关闭',
    refundSuccess: '退款成功'
  },

  /** 用户选择组件 */
  userSelect: {
    title: '用户选择',
    selectUser: '选择用户',
    deptFilter: '部门筛选',
    deptPlaceholder: '请输入部门名称',
    userName: '用户名称',
    phone: '手机号码',
    createTime: '创建时间',
    userId: '用户ID',
    nickName: '用户昵称',
    dept: '部门',
    status: '状态',
    defaultUserName: '用户{id}'
  },

  /** OSS媒体管理器组件 */
  ossMediaManager: {
    title: '素材库',
    fileTypeHint: '当前仅支持选择 {types} 格式的文件',
    hideDirectory: '隐藏目录',
    showDirectory: '显示目录',
    folder: '文件夹',
    add: '新建',
    rename: '重命名',
    delete: '删除',
    closeDirectory: '关闭目录',
    directoryPlaceholder: '请输入文件夹名称',
    uploadFile: '上传文件',
    replace: '替换',
    moveTo: '移动到',
    batchDelete: '批量删除',
    searchPlaceholder: '搜索文件',
    selectedCount: '已选择 {count} 项',
    noSelection: '未选择任何文件',
    fileType: '文件类型',
    allFiles: '全部文件',
    images: '图片',
    documents: '文档',
    videos: '视频',
    audios: '音频',
    others: '其他',
    noFiles: '暂无文件',
    unknownSize: '未知大小',
    loading: '加载中...',
    loadingMore: '更多文件加载中...',
    allFilesLoaded: '已加载全部文件',
    filePreview: '文件预览',
    videoNotSupported: '您的浏览器不支持视频标签',
    audioNotSupported: '您的浏览器不支持音频标签',
    loadTextContent: '加载文本内容',
    downloadFile: '下载文件',
    moveToFolder: '移动到文件夹',
    cancel: '取消',
    confirm: '确认',
    directoryName: '目录名称',
    directoryNamePlaceholder: '请输入目录名称',
    selectFile: '选择文件',
    replaceUploadTip: '请选择一个文件替换当前文件',
    uploadTip: '可上传{types}文件，单文件不超过{size}MB',
    close: '关闭',
    confirmSelection: '确认选择',
    sortBy: '排序方式',
    sortUpdateTimeDesc: '按更新时间（最新）',
    sortUpdateTimeAsc: '按更新时间（最旧）',
    sortFileNameAsc: '按文件名（A-Z）',
    sortFileNameDesc: '按文件名（Z-A）',
    sortFileSizeAsc: '按文件大小（从小到大）',
    sortFileSizeDesc: '按文件大小（从大到小）',
    sortFileTypeAsc: '按文件类型（A-Z）',
    sortFileTypeDesc: '按文件类型（Z-A）',
    directoryNameRequired: '请输入目录名称',
    directoryNameLength: '长度在 1 到 50 个字符',
    directoryNameInvalid: '目录名不能包含特殊字符 \\ / : * ? " < > |',
    loadDirectoryFailed: '加载目录失败',
    loadingText: '加载中...',
    loadTextFailed: '无法加载文件内容，请下载后查看。',
    requestFailed: '请求失败: {status}',
    addDirectory: '添加目录',
    addSubDirectory: '添加子目录',
    renameDirectory: '重命名目录',
    hasChildDirectory: '该目录包含子目录，请先删除子目录',
    confirmDeleteDirectory: '是否确认删除目录"{name}"？',
    warning: '警告',
    deleteSuccess: '删除成功',
    addDirectorySuccess: '添加目录成功',
    updateDirectorySuccess: '修改目录成功',
    selectOneFileToReplace: '请选择一个文件进行替换',
    replaceFile: '替换文件',
    fileSizeLimit: '文件大小不能超过{size}MB',
    fileTypeMustMatch: '替换文件类型必须与原文件类型一致，原文件类型：{oldType}，新文件类型：{newType}',
    fileTypeMustBe: '文件类型必须是 {types} 格式',
    fileReplaceSuccess: '文件替换成功',
    uploadSuccess: '上传成功',
    uploadFailed: '上传失败',
    selectFilesToMove: '请选择要移动的文件',
    selectTargetDirectory: '请选择目标目录',
    moveSuccess: '移动成功',
    confirmDeleteFile: '是否确认删除该文件？',
    deleteFileFailed: '删除文件失败',
    selectFilesToDelete: '请选择要删除的文件',
    confirmBatchDelete: '是否确认删除选中的 {count} 个文件？',
    batchDeleteFailed: '批量删除文件失败',
    batchDeleteSuccess: '批量删除成功',
    pleaseSelectFile: '请选择文件',
    anyType: '任意类型的',
    imageType: '图片',
    documentType: '文档',
    videoType: '视频',
    audioType: '音频',
    otherType: '其他'
  },

  /** 表单上传组件 */
  formUpload: {
    selectFromLibrary: '从素材库选择',
    clearAll: '清空全部',
    downloadImage: '下载图片',
    previewTitle: '图片预览',
    // 默认值
    defaultLabel: '图片',
    selectImage: '选择图片',
    // 拖拽提示
    dragHint: '将图片拖到此处，或',
    clickToUpload: '点击上传',
    // 上传提示模板
    uploadHintPrefix: '请上传',
    sizeLimit: '大小不超过',
    formatHint: '格式为',
    imageSuffix: '的图片',
    countLimit: '，最多上传',
    countUnit: '张',
    compressHint: '，超过',
    willCompress: '将自动压缩',
    directMode: '（直传模式）',
    // 加载提示
    loadingImageInfo: '正在加载图片信息...',
    uploadingImage: '正在上传图片，请稍候...',
    deletingFiles: '正在删除 {count} 个云端文件，请稍候...',
    // 清空确认消息
    noImageToClear: '没有图片可以清空',
    confirmClearAll: '确认要清空所有图片吗？',
    confirmClearCloud: '{count} 个云端文件将被删除',
    confirmClearLocal: '{count} 个本地文件将被清空',
    cloudFilesDeleted: '{count} 个云端文件删除成功',
    localFilesCleared: '{count} 个本地文件已清空',
    cloudDeleteFailed: '以下云端文件删除失败：',
    clearCompleted: '清空完成：',
    partialClearCompleted: '部分清空完成：',
    // 错误消息
    formatError: '图片格式不正确, 请上传{formats}图片格式文件!',
    filenameError: '图片名不正确，不能包含英文逗号!',
    sizeError: '上传图片大小不能超过 {size} MB!',
    countError: '上传图片数量不能超过 {limit} 张!',
    uploadFailed: '图片上传失败',
    fileFailed: '文件上传失败',
    confirmFailed: '确认上传失败',
    compressFailed: '图片压缩失败: ',
    directUploadFailed: '直传图片失败: ',
    selectImageFile: '请选择图片文件!',
    confirmDeleteCloud: '此操作会删除云端图片,是否确认删除',
    // 成功消息
    replaceSuccess: '图片替换成功',
    uploadSuccess: '图片上传成功',
    directUploadSuccess: '图片直传成功',
    selectSuccess: '选择图片成功',
    // 混合格式警告
    mixedFormatWarning: '检测到混合格式的文件（部分有ossId，部分没有），将返回URL格式以保持一致性',

    // ========== 文件上传组件专用 ==========
    // 默认值
    defaultFileLabel: '文件',
    selectFile: '选择文件',
    // 拖拽提示
    fileDragHint: '将文件拖到此处，或',
    // 上传提示
    fileSuffix: '的文件',
    fileCountUnit: '个',
    // 预览弹窗
    filePreviewTitle: '文件预览',
    downloadFile: '下载文件',
    // 加载提示
    loadingFileInfo: '正在加载文件信息...',
    uploadingFile: '正在上传文件，请稍候...',
    // 清空确认消息
    noFileToClear: '没有文件可以清空',
    confirmClearAllFiles: '确认要清空所有文件吗？',
    // 错误消息
    fileFormatError: '文件格式不正确, 请上传{formats}格式文件!',
    filenameHasCommaError: '文件名不正确，不能包含英文逗号!',
    fileSizeError: '上传文件大小不能超过 {size} MB!',
    fileCountError: '上传文件数量不能超过 {limit} 个!',
    directUploadFileFailed: '直传文件失败: ',
    uploadFileFailed: '上传文件失败: ',
    // 成功消息
    fileReplaceSuccess: '文件替换成功',
    fileUploadSuccess: '文件上传成功',
    fileDirectUploadSuccess: '文件直传成功',
    selectFileSuccess: '选择文件成功',
    confirmDeleteCloudFile: '此操作会删除云端文件,是否确认删除',
    // 清空操作相关
    clearConfirmCloudAndLocal: '确认要清空所有文件吗？\n• {cloudCount} 个云端文件将被删除\n• {localCount} 个本地文件将被清空',
    clearConfirmCloud: '确认要清空所有文件吗？\n{count} 个云端文件将被删除。',
    clearConfirmLocal: '确认要清空所有文件吗？\n{count} 个本地文件将被清空。',
    deletingCloudFiles: '正在删除 {count} 个云端文件，请稍候...',
    deleteCloudFileFailed: '以下云端文件删除失败：',
    unknownError: '未知错误'
  },

  /** 富文本编辑器组件 */
  formEditor: {
    // 默认值
    defaultLabel: '富文本',
    placeholder: '请输入内容...',
    // 图片上传
    imageUploadSuccess: '图片上传成功',
    imageUploadFailed: '图片上传失败',
    imageUploadResponseError: '图片上传失败：响应格式异常',
    // 视频上传
    videoUploadSuccess: '视频上传成功',
    videoUploadFailed: '视频上传失败',
    videoUploadResponseError: '视频上传失败：响应格式异常',
    // 错误消息
    uploadFailed: '上传失败',
    invalidBase64Data: '无效的Base64数据'
  },

  /** 地图选点组件 */
  formMap: {
    // 弹窗与按钮
    dialogTitle: '选择位置',
    buttonText: '选择位置',
    confirmButton: '确定选择',
    // 搜索
    searchPlaceholder: '请输入地点名称进行搜索',
    searchButton: '搜索',
    searchKeywordRequired: '请输入搜索关键词',
    searchNoResult: '未找到相关地点，请尝试其他关键词',
    searchLocated: '已定位到：{name}',
    // 地图加载
    mapLoading: '地图加载中...',
    mapLoadFailed: '高德地图加载失败，请检查配置和网络',
    mapNotConfigured: '高德地图未配置，请配置Key',
    geocoderNotInit: '地理编码器未初始化',
    getAddressFailed: '获取地址信息失败',
    // 位置信息标签
    longitude: '经度',
    latitude: '纬度',
    province: '省份',
    city: '城市',
    district: '区县',
    adcode: '行政区划代码',
    detailAddress: '详细地址',
    // 提示与校验
    emptyTip: '请在地图上点击选择位置',
    selectFirst: '请先选择位置',
    areaHint: '提示：请在当前行政区域内选择位置（区域代码：{code}）',
    validationMessage: '所选位置不在指定行政区域内，请重新选择！',
    requiredLocation: '要求位置：{name} (区域代码: {code})',
    requiredAreaCode: '要求区域代码：{code}',
    currentLocation: '当前位置：{name} (区域代码: {code})',
    currentAreaCode: '当前区域代码：{code}'
  },

  /** 表格选择组件 */
  formTableSelect: {
    modalTitle: '选择数据',
    searchPlaceholder: '请输入关键词搜索'
  },

  /** 日期选择组件 */
  formDate: {
    startDate: '开始日期',
    endDate: '结束日期',
    weekFormat: 'YYYY 第 ww 周'
  },

  /** Excel导入组件 */
  importExcel: {
    dragHint: '将文件拖到此处，或',
    clickToUpload: '点击上传',
    autoUpdateHint: '导入时将自动更新已存在的数据',
    formatHint: '仅允许导入 .xls、.xlsx 格式文件',
    downloadTemplate: '下载模板',
    excelOnlyWarning: '只能上传 Excel 文件!',
    noUrlWarning: '需提供导入地址/模板地址',
    importSuffix: '导入',
    importResult: '导入结果',
    templateSuffix: '模板',
    selectFileFirst: '请先选择要导入的文件',
    noFileAvailable: '没有可上传的文件'
  },

  /** 表格组件 */
  table: {
    emptyText: '暂无数据',
    indexLabel: '序号',
    booleanYes: '是',
    booleanNo: '否',
    currencySymbol: '¥',
    copySuccess: '复制成功',
    copyFieldSuccess: '复制{field}成功',
    // 打印相关
    printTitle: '数据列表',
    printFailed: '打印失败，请稍后重试',
    actionsColumn: '操作',
    switchEnabled: '启用',
    switchDisabled: '禁用'
  },

  /** 详情组件 */
  detail: {
    title: '详情',
    show: '显示',
    hide: '隐藏',
    copy: '复制',
    copyContent: '复制内容',
    viewAttachment: '查看附件',
    basicInfo: '基本信息',
    print: '打印',
    printFailed: '打印失败'
  },

  /** 路由国际化配置 */
  route: {
    dashboard: '首页',
    document: '项目文档'
  },

  label: {
    large: '较大',
    default: '默认',
    small: '较小'
  },

  /** 登录页面国际化 */
  login: {
    selectPlaceholder: '请选择/输入公司名称',
    tenantId: '租户id',
    userName: '用户名',
    password: '密码',
    login: '登 录',
    logging: '登 录 中...',
    code: '验证码',
    rememberPassword: '记住我',
    switchRegisterPage: '立即注册',
    forgotPassword: '忘记密码?',
    noAccount: '还没有账号?',
    registerNow: '立即注册',
    thirdPartyConfigError: '第三方登录配置错误',
    ruoyiStudio: '若依工作室',
    // 左侧品牌展示区域
    leftView: {
      title: '欢迎使用',
      subtitle: '简洁、高效、现代化的管理系统'
    },
    // 表单验证规则
    rule: {
      tenantId: { required: '请输入您的租户id' },
      userName: { required: '请输入您的账号' },
      password: { required: '请输入您的密码' },
      code: { required: '请输入验证码' }
    },
    // 第三方登录方式
    social: {
      wechat_open: '微信开放平台',
      wechat_mp: '微信公众号',
      wechat_enterprise: '企业微信',
      dingtalk: '钉钉',
      maxkey: 'MaxKey',
      topiam: 'TopIAM',
      qq: 'QQ',
      weibo: '微博',
      gitee: 'Gitee',
      github: 'GitHub',
      gitlab: 'GitLab',
      baidu: '百度',
      csdn: 'CSDN',
      coding: 'Coding',
      oschina: '开源中国',
      alipay_wallet: '支付宝',
      taobao: '淘宝',
      douyin: '抖音',
      linkedin: 'LinkedIn',
      microsoft: 'Microsoft',
      renren: '人人网',
      stack_overflow: 'Stack Overflow',
      huawei: '华为',
      aliyun: '阿里云',
      gitea: 'Gitea'
    }
  },

  /** 注册页面国际化 */
  register: {
    selectPlaceholder: '请选择/输入公司名称',
    userName: '用户名',
    password: '密码',
    confirmPassword: '确认密码',
    register: '注 册',
    registering: '注 册 中...',
    registerSuccess: '恭喜你，您的账号 {userName} 注册成功！',
    code: '验证码',
    hasAccount: '已有账号？',
    switchLoginPage: '立即登录',
    subtitle: '创建您的账户',
    // 邀请注册相关
    inviteRole: '邀请角色',
    department: '所属部门',
    invitedRole: '受邀注册角色',
    approvalRequired: '注册后需要管理员审核',
    inviteCodeSuccess: '通过邀请码注册，将自动分配到角色',
    inviteCodeInvalid: '邀请码无效或已过期，将按正常注册流程进行',
    // 注册成功消息
    congratsInvite: '恭喜您通过邀请成功注册！',
    waitApproval: '请等待管理员审核后方可登录',
    loginImmediately: '可以立即登录使用',
    registrationSuccess: '注册成功！',
    usernameLabel: '用户名',
    roleLabel: '角色',
    systemNotice: '系统提示',
    thirdPartyConfigError: '第三方注册配置错误',
    // 表单验证规则（包含动态参数示例）
    rule: {
      tenantId: { required: '请输入您的租户id' },
      userName: {
        required: '请输入您的账号',
        length: '用户账号长度必须介于 {min} 和 {max} 之间'
      },
      password: {
        required: '请输入您的密码',
        length: '用户密码长度必须介于 {min} 和 {max} 之间',
        pattern: '不能包含非法字符：{strings}'
      },
      code: { required: '请输入验证码' },
      confirmPassword: {
        required: '请再次输入您的密码',
        equalToPassword: '两次输入的密码不一致'
      }
    }
  },

  /** 忘记密码页面国际化 */
  forgotPassword: {
    title: '重置密码',
    subtitle: '请输入您的邮箱地址，我们将发送重置密码链接',
    emailPlaceholder: '请输入邮箱地址',
    codePlaceholder: '请输入验证码',
    submitBtn: '发送重置链接',
    submitting: '发送中...',
    backToLogin: '返回登录',
    success: '重置密码链接已发送到您的邮箱，请查收',
    // 表单验证规则
    rule: {
      emailRequired: '请输入邮箱地址',
      emailFormat: '请输入正确的邮箱格式',
      codeRequired: '请输入验证码'
    }
  },

  /** 导航栏国际化 */
  navbar: {
    searchMenu: '搜索菜单',
    full: '全屏',
    exitFull: '退出全屏',
    language: '语言',
    dashboard: '首页',
    document: '项目文档',
    message: '消息',
    git: '仓库地址',
    layoutSize: '布局大小',
    sizeChangeSuccess: '布局大小切换成功',
    selectTenant: '选择租户',
    layoutSetting: '布局设置',
    personalCenter: '个人中心',
    logout: '退出登录',
    aiChat: 'AI助手',
    // 通知公告
    notice: {
      title: '通知公告',
      markAllAsRead: '全部已读',
      tabs: {
        notice: '通知公告'
      },
      unread: '未读',
      empty: '暂无通知公告',
      publisher: '发布人',
      publishTime: '发布时间',
      close: '关闭',
      getDetailFailed: '获取通知详情失败',
      markedAsRead: '已全部标记为已读'
    },
    // AI 智能助手
    aiAssistant: {
      title: 'AI 智能助手',
      newChat: '新建对话',
      newSessionTitle: '新对话',
      noHistory: '暂无对话历史',
      startNewChat: '开始新对话',
      clearMessages: '清空对话',
      settings: '设置',
      startChatTitle: '开始与 AI 对话',
      startChatDesc: '输入您的问题，让 AI 助手帮助您',
      you: '你',
      assistant: 'AI 助手',
      editPlaceholder: '修改您的消息...',
      cancel: '取消',
      saveAndResend: '保存并重新发送',
      generating: '正在生成中...',
      copy: '复制',
      edit: '编辑',
      regenerate: '重新生成',
      tokenInput: '输入',
      tokenOutput: '输出',
      tokenTotal: '总计',
      // 深度思考（推理模型）
      thinkingInProgress: '正在深度思考...',
      thinkingDone: '已深度思考',
      expandThinking: '展开思考过程',
      collapseThinking: '收起思考过程',
      // 深度思考档位（仅后端开启深度思考时显示该控件）
      deepThinking: '深度思考',
      thinkingLevelStandard: '标准',
      thinkingLevelDeep: '深度',
      thinkingLevelOff: '本次关闭',
      modelProvider: '模型提供商',
      selectProvider: '选择提供商',
      inputPlaceholder: '输入消息... (Enter 发送，Shift+Enter 换行)',
      send: '发送',
      // 时间格式
      time: {
        justNow: '刚刚',
        minutesAgo: '{n} 分钟前'
      },
      // 状态文本
      status: {
        sending: '发送中',
        streaming: '生成中',
        complete: '完成',
        error: '错误'
      },
      // 消息提示
      message: {
        wsNotConnected: 'WebSocket 未连接，请刷新页面重试',
        sessionCreated: '已创建新对话',
        sessionDeleted: '已删除对话',
        messagesCleared: '已清空消息',
        copiedToClipboard: '已复制到剪贴板',
        copyFailed: '复制失败',
        confirmDeleteSession: '确定要删除这个对话吗？',
        confirmClearMessages: '确定要清空当前对话的所有消息吗？'
      }
    }
  },

  /**页签*/
  tagsView: {
    refresh: '刷新页面',
    closeCurrent: '关闭当前',
    closeOthers: '关闭其他',
    closeLeft: '关闭左侧',
    closeRight: '关闭右侧',
    closeAll: '全部关闭'
  },

  /** 页面标题（用于浏览器标签页） */
  page: {
    redirect: '页面重定向',
    socialCallback: '社交登录回调',
    login: '登录',
    register: '注册',
    forgotPassword: '忘记密码',
    notFound: '404 页面不存在',
    unauthorized: '401 未授权',
    profile: '个人中心',
    datavFullscreen: '大屏预览'
  },

  /** 菜单系统配置（核心结构） */
  menu: {
    // 首页菜单
    home: '主页',
    index: '首页',

    // 系统管理模块
    system: {
      _self: '系统管理',
      user: '用户管理',
      role: '角色管理',
      menu: '菜单管理',
      dept: '部门管理',
      post: '岗位管理',
      dict: '字典管理',
      config: '参数设置',
      notice: '通知公告',
      // 日志管理子模块
      log: {
        _self: '日志管理',
        operLog: '操作日志',
        loginLog: '登录日志'
      },
      oss: '文件管理',
      sysOssDirectory: 'OSS目录'
    },

    // 系统监控模块
    monitor: {
      _self: '系统监控',
      online: '在线用户',
      cache: '缓存监控',
      admin: 'admin监控',
      snailjob: '任务调度'
    },

    // 系统工具模块
    tool: {
      _self: '系统工具',
      gen: '代码生成'
    },

    // 租户管理模块
    tenant: {
      _self: '租户管理',
      tenant: '租户管理',
      tenantPackage: '租户套餐'
    },

    // 应用配置模块
    appConfiguration: {
      _self: 'APP配置',
      appConfig: '多端配置'
    },

    // 商城管理
    mallManage: {
      _self: '商城管理',
      goods: '商品管理'
    }
  },

  /** 图表组件 */
  chart: {
    noData: '暂无数据',
    map: {
      name: '名称',
      code: '代码',
      level: '级别',
      unknownRegion: '未知区域'
    },
    bidirectional: {
      positiveData: '正向数据',
      negativeData: '负向数据'
    },
    pie: {
      dataRatio: '数据占比'
    },
    candlestick: {
      time: '时间：',
      open: '开盘：',
      close: '收盘：',
      low: '最低：',
      high: '最高：'
    }
  },

  /** 通用字段翻译（根级别，供 AForm* 组件使用） */
  // 部门管理
  dept: '部门',
  parentId: '上级部门',
  deptName: '部门名称',
  deptCategory: '类别编码',
  areaCode: '地区',
  orderNum: '显示排序',
  leader: '负责人',
  // 通用字段
  searchValue: '模糊搜索',
  phone: '联系电话',
  email: '邮箱',
  status: '状态',
  createTime: '创建时间',
  updateTime: '更新时间',
  remark: '备注',
  createBy: '创建人',
  updateBy: '更新人',

  /** 表单验证消息 */
  validation: {
    // 部门管理
    parentIdRequired: '上级部门不能为空',
    deptNameRequired: '部门名称不能为空',
    orderNumRequired: '显示排序不能为空',
    emailInvalid: '请输入正确的邮箱地址',
    phoneInvalid: '请输入正确的手机号码'
  },

  /** 通用文本 */
  common: {
    empty: '暂无',
    cancel: '取消',
    apply: '应用',
    copy: '复制',
    enable: '启用',
    disable: '禁用',
    operation: '操作',
    edit: '编辑',
    delete: '删除',
    addSuccess: '添加成功'
  },

  /** 卡片组件 */
  card: {
    // 统计卡片
    stats: {
      completed: '完成'
    },
    // 社交卡片
    social: {
      edit: '编辑',
      delete: '删除',
      report: '举报',
      like: '点赞',
      comment: '评论',
      share: '分享'
    },
    // 数据列表卡片
    dataList: {
      view: '查看',
      viewMore: '查看更多',
      noData: '暂无数据'
    },
    // 用户卡片
    user: {
      follow: '关注',
      following: '已关注',
      message: '私信'
    },
    // 资料卡片
    profile: {
      edit: '编辑资料',
      viewProfile: '查看主页'
    },
    // 表单卡片
    form: {
      submit: '提交',
      reset: '重置'
    },
    // 表格卡片
    tableCard: {
      noData: '暂无数据',
      viewAll: '查看全部'
    },
    // 图片卡片
    image: {
      preview: '预览'
    },
    // 活动卡片
    activity: {
      viewDetails: '查看详情',
      join: '参加'
    },
    // 时间线卡片
    timeline: {
      viewMore: '查看更多',
      noData: '暂无记录'
    },
    // 天气卡片
    weather: {
      humidity: '湿度',
      wind: '风速',
      feelsLike: '体感',
      forecast: '预报',
      today: '今天',
      tomorrow: '明天',
      celsius: '°C',
      fahrenheit: '°F',
      kmh: 'km/h',
      mph: 'mph',
      sunny: '晴',
      cloudy: '多云',
      rainy: '雨',
      snowy: '雪',
      windy: '大风',
      foggy: '雾'
    },
    // 价格卡片
    pricing: {
      perMonth: '/月',
      perYear: '/年',
      selectPlan: '选择套餐',
      currentPlan: '当前套餐',
      popular: '热门'
    },
    // 通知卡片
    notification: {
      markAsRead: '标记已读',
      markAllAsRead: '全部已读',
      delete: '删除',
      noNotifications: '暂无通知',
      viewAll: '查看全部',
      justNow: '刚刚',
      minutesAgo: '{n}分钟前',
      hoursAgo: '{n}小时前',
      daysAgo: '{n}天前',
      info: '信息',
      success: '成功',
      warning: '警告',
      error: '错误'
    }
  },
  /** AI 组件国际化 */
  ai: {
    // AI 文本优化器
    textOptimizer: {
      currentContent: '当前内容',
      charCount: '{count} 字',
      inputPlaceholder: '请输入需要优化的文本内容...',
      optimizeMethod: '优化方式',
      advancedOptions: '高级选项',
      styleRequirement: '风格要求',
      selectStyle: '选择风格',
      styles: {
        professional: '专业正式',
        casual: '轻松随意',
        creative: '创意新颖',
        concise: '简洁精炼'
      },
      targetLength: '目标长度',
      minLength: '最少',
      maxLength: '最多',
      keywords: '关键词保留',
      keywordsPlaceholder: '输入需要保留的关键词，用逗号分隔',
      translateLanguage: '翻译语言',
      languages: {
        en: '英语',
        ja: '日语',
        ko: '韩语',
        fr: '法语',
        de: '德语',
        es: '西班牙语'
      },
      customRequirement: '自定义要求',
      customPlaceholder: '请输入自定义优化要求...',
      processing: '处理中...',
      startOptimize: '开始优化',
      optimizeResult: '优化结果',
      compare: '对比',
      generating: '正在生成优化内容...',
      tokenUsage: {
        totalTokens: '总 Token',
        inputTokens: '输入',
        outputTokens: '输出'
      },
      otherSuggestions: '其他建议',
      contentCompare: '内容对比',
      originalText: '原文',
      optimizedText: '优化后',
      wordCount: '{count} 字',
      inputAndSelectRequired: '请输入内容并选择优化方式',
      optimizeFailed: '优化失败，请重试',
      optimizeComplete: '优化完成',
      copySuccess: '已复制到剪贴板',
      types: {
        polish: '润色',
        expand: '扩写',
        shorten: '精简',
        formal: '正式化',
        casual: '口语化',
        marketing: '营销化',
        seo: 'SEO优化'
      }
    },
    // AI 助手
    assistant: {
      features: {
        optimize: '文本优化',
        generate: '内容生成',
        review: '内容审核',
        translate: '翻译'
      },
      history: '历史记录',
      inputLabel: '输入',
      outputLabel: '输出',
      useThisResult: '使用此结果',
      advancedSettings: '高级设置',
      modelSelect: '模型选择',
      selectProvider: '选择提供商',
      providers: {
        qianwen: '通义千问'
      },
      creativity: '创造力',
      roleSettings: '角色设定',
      rolePlaceholder: '为 AI 设定角色，如：你是一位专业的文案编辑...',
      collapseSettings: '收起设置',
      applyResult: '应用结果',
      quickActions: {
        optimizeContent: '优化内容',
        regenerate: '重新生成',
        translate: '翻译'
      },
      processing: '处理中...',
      buttons: {
        optimize: '开始优化',
        generate: '开始生成',
        review: '开始审核',
        translate: '开始翻译',
        suggest: '获取建议'
      },
      title: 'AI 助手',
      titles: {
        optimize: '文本优化',
        generate: '内容生成',
        review: '内容审核',
        translate: '翻译助手',
        suggest: '智能建议'
      },
      processFailed: '处理失败，请重试',
      resultApplied: '已应用结果'
    },
    // AI 数据生成器
    dataGenerator: {
      generateConfig: '生成配置',
      fieldConfig: '字段配置',
      generateCount: '生成数量',
      countUnit: '条',
      dataLanguage: '数据语言',
      languages: {
        zh: '中文',
        en: '英文',
        mixed: '混合'
      },
      valueRange: '数值范围',
      min: '最小值',
      max: '最大值',
      relatedFields: '关联字段',
      select: '选择',
      selectRelatedField: '选择关联字段',
      addField: '添加字段',
      selectGenerateFields: '请选择要生成的字段',
      generating: '生成中...',
      startGenerate: '开始生成',
      generateResult: '生成结果',
      generated: '已生成',
      records: '条数据',
      edit: '编辑',
      delete: '删除',
      copyAll: '复制全部',
      exportJson: '导出JSON',
      exportExcel: '导出Excel',
      clearAll: '清空',
      editRecord: '编辑记录',
      selectFieldsFirst: '请先选择要生成的字段',
      generateFailed: '生成失败，请重试',
      generateSuccess: '成功生成 {count} 条数据',
      saveSuccess: '保存成功',
      deleteSuccess: '删除成功',
      exportSuccess: '导出成功',
      exportExcelDeveloping: '导出Excel功能需要集成导出工具',
      copySuccess: '复制成功',
      clearSuccess: '已清空'
    },
    // AI 内容审核器
    contentReviewer: {
      reviewConfig: '审核配置',
      reviewLevel: '审核级别',
      levels: {
        loose: '宽松',
        normal: '标准',
        strict: '严格'
      },
      checkItems: '审核项',
      items: {
        compliance: '合规性检查',
        sensitive: '敏感词检测',
        quality: '质量评估',
        completeness: '完整性检查',
        format: '格式验证'
      },
      autoFix: '自动修复',
      autoFixTip: 'AI会尝试自动修复发现的问题',
      contentToReview: '待审核内容',
      loadFromForm: '从表单加载',
      inputPlaceholder: '请输入{field}',
      customRules: '自定义审核规则',
      field: '字段',
      selectField: '选择字段',
      ruleType: '规则类型',
      ruleTypes: {
        required: '必填',
        length: '长度限制',
        format: '格式要求',
        keywords: '关键词',
        regex: '正则表达式'
      },
      ruleValue: '规则值',
      tip: '提示',
      errorTip: '错误提示',
      addRule: '添加规则',
      reviewing: '审核中...',
      startReview: '开始审核',
      reviewResult: '审核结果',
      overallScore: '综合评分',
      scores: {
        compliance: '合规性：',
        quality: '质量分：',
        completeness: '完整度：'
      },
      foundIssues: '发现 {count} 个问题',
      issueLocation: '问题位置：',
      entireField: '整个字段',
      issueContent: '问题内容：',
      fixSuggestion: '修复建议：',
      afterFix: '修复后：',
      applyFix: '应用修复',
      ignore: '忽略',
      reviewPassed: '审核通过',
      contentMeetsStandards: '内容符合所有审核标准',
      exportReport: '导出报告',
      reviewSummary: '审核摘要',
      summary: {
        reviewTime: '审核时间',
        reviewLevel: '审核级别',
        reviewFields: '审核字段',
        foundIssues: '发现问题',
        severeIssues: '严重问题',
        suggestedOptimizations: '建议优化',
        countUnit: '{count} 个'
      },
      reReview: '重新审核',
      applyAllFixes: '应用所有修复',
      status: {
        pass: '通过',
        warning: '警告',
        fail: '不通过'
      },
      severity: {
        low: '建议',
        medium: '警告',
        high: '严重'
      },
      loadFromFormRequiresContext: '从表单加载功能需要传入context',
      reviewFailed: '审核失败，请重试',
      reviewPassedMsg: '内容审核通过',
      foundWarningIssues: '发现 {count} 个需要注意的问题',
      foundSevereIssues: '发现 {count} 个严重问题',
      fixApplied: '已应用修复',
      issueIgnored: '已忽略该问题',
      fixesApplied: '已应用 {count} 个修复',
      report: {
        reviewTime: '审核时间',
        reviewLevel: '审核级别',
        reviewStatus: '审核状态',
        overallScore: '综合评分',
        scoreDetails: '评分详情',
        issuesList: '问题列表',
        reviewContent: '审核内容'
      },
      reportExported: '报告导出成功'
    },
    // 带AI的输入框
    inputWithAi: {
      polish: '润色',
      expand: '扩写',
      shorten: '精简',
      formal: '正式化',
      casual: '口语化',
      translate: '翻译',
      custom: '自定义',
      moreFeatures: '更多功能',
      textOptimize: '文本优化',
      contentRequired: '请先输入内容',
      optimizeFailed: '优化失败',
      emptyResult: '优化结果为空',
      optimizeSuccess: '优化成功',
      applyResult: '是否应用优化结果？',
      applied: '已应用',
      optimizeError: '优化出错'
    }
  },

  /** 字典数据国际化 */
  dict: {
    // 商品规格类型
    m_goods_spec_type: {
      '0': '单规格',
      '1': '多规格'
    },
    // 审核状态
    sys_audit_status: {
      '0': '待审核',
      '1': '通过',
      '2': '驳回',
      '3': '拒绝'
    },
    // 布尔标志
    sys_boolean_flag: {
      '0': '否',
      '1': '是'
    },
    // 数据权限范围
    sys_data_scope: {
      '1': '全部数据权限',
      '2': '自定义数据权限',
      '3': '本部门数据权限',
      '4': '本部门及以下数据权限',
      '5': '仅本人数据权限',
      '6': '部门及以下或本人数据权限'
    },
    // 显示设置
    sys_display_setting: {
      '0': '隐藏',
      '1': '显示'
    },
    // 启用状态
    sys_enable_status: {
      '0': '禁用',
      '1': '启用'
    },
    // 文件类型
    sys_file_type: {
      image: '图片',
      document: '文档',
      video: '视频',
      audio: '音频',
      archive: '压缩包',
      other: '其他'
    },
    // 消息类型
    sys_message_type: {
      system: '系统消息',
      activity: '活动消息',
      audit: '审核消息',
      account: '账户消息',
      private: '私信'
    },
    // 通知状态
    sys_notice_status: {
      '0': '保存为草稿',
      '1': '立即发送'
    },
    // 通知类型
    sys_notice_type: {
      '1': '通知',
      '2': '公告'
    },
    // 操作结果
    sys_oper_result: {
      '0': '失败',
      '1': '成功'
    },
    // 操作类型
    sys_oper_type: {
      '1': '新增',
      '2': '修改',
      '3': '删除',
      '4': '授权',
      '5': '导出',
      '6': '导入',
      '7': '强退',
      '8': '生成代码',
      '9': '清空数据',
      '99': '其他'
    },
    // 订单状态
    sys_order_status: {
      pending: '待支付',
      paid: '已支付',
      delivered: '已发货',
      completed: '已完成',
      cancelled: '已取消',
      refunded: '已退款'
    },
    // 支付方式
    sys_payment_method: {
      wechat: '微信支付',
      alipay: '支付宝',
      unionpay: '银联支付',
      balance: '余额支付',
      points: '积分支付'
    },
    // 平台类型
    sys_platform_type: {
      'mp-weixin': '微信小程序',
      'mp-official-account': '微信公众号',
      'mp-qq': 'QQ小程序',
      'mp-alipay': '支付宝小程序',
      'mp-baidu': '百度小程序',
      'mp-toutiao': '头条小程序',
      'mp-lark': '飞书小程序',
      'mp-kuaishou': '快手小程序',
      'mp-jd': '京东小程序',
      'mp-360': '360小程序',
      'app-android': 'Android App',
      'app-ios': 'iOS App',
      'h5': 'H5网页',
      'web': 'PC网页'
    },
    // 用户性别
    sys_user_gender: {
      '0': '女',
      '1': '男',
      '2': '未知'
    }
  }
}
