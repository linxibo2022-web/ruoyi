// 英文语言包
export default {
  '操作': 'Operation',
  '新增': 'Add ',
  '修改': 'Edit ',
  '删除': 'Delete',
  '查看': 'View ',
  '导出': 'Export',
  '导入': 'Import',
  '刷新': 'Refresh',
  '确定': 'Confirm',
  '取消': 'Cancel',
  '启用': 'Enable',
  '停用': 'Disable',
  '成功': 'Success',
  '是否确认': 'Confirm ',
  '折叠': 'Collapse',
  '展开': 'Expand',
  '共': 'Total ',
  '条': ' items',
  '搜索': 'Search...',
  '是否确认删除': 'Confirm delete the following:',
  '请先保存主表': 'Please save the main table information first, then the sub-table function will be automatically enabled',
  /** 按钮权限系统 */
  button: {
    // 通用按钮
    query: 'Query',
    add: 'Add',
    update: 'Edit',
    delete: 'Delete',
    export: 'Export',
    import: 'Import',
    list: 'List ',
    confirm: 'Confirm',
    cancel: 'Cancel',
    save: 'Save',
    upload: 'Upload',
    download: 'Download',
    preview: 'Preview',
    view: 'View',
    generate: 'Generate',
    more: 'More',
    downloadTemplate: 'Download Template',
    importData: 'Import Data',
    exportData: 'Export Data',
    collapse: 'Collapse',
    expand: 'Expand',
    close: 'Close',
    sync: 'Sync',
    submit: 'Submit',

    // 系统管理特殊按钮
    resetPwd: 'Reset Password',
    unlock: 'Unlock Account',
    clean: 'Clean',
    refreshCache: 'Refresh Cache',
    syncTenantRoles: 'Sync Tenant Roles',
    syncTenantDicts: 'Sync Tenant Dicts',
    syncTenantConfigs: 'Sync Tenant Configs',
    unbind: 'Unbind',

    // 监控特殊按钮
    batchLogout: 'Batch Logout',
    forceLogout: 'Force Logout',

    // 开发工具特殊按钮
    importCode: 'Import Code',
    previewCode: 'Preview Code',
    generateCode: 'Generate Code'
  },

  /** Dialog Prompts */
  dialog: {
    add: 'Add ',
    edit: 'Edit ',
    delete: 'Delete',
    query: 'Query',
    detail: 'Details',
    import: 'Import',
    export: 'Export',
    config: 'Configure',
    close: 'Close'
  },

  /** Message Prompts */
  message: {
    operation: 'Operation',

    // Operation confirmation messages
    confirmDelete: 'Confirm delete the following:',
    confirmCancel: 'Confirm cancel?',
    confirmSubmit: 'Confirm submit?',
    confirm: 'Confirm ',
    enable: 'Enable ',
    disable: 'Disable ',

    // Operation result messages
    success: 'Operation successful',
    error: 'Operation failed',

    // Specific operation result messages
    saveSuccess: 'Save successful',
    saveError: 'Save failed',
    addSuccess: 'Added successfully',
    addError: 'Add failed',
    updateSuccess: 'Update successful',
    updateError: 'Update failed',
    deleteSuccess: 'Delete successful',
    deleteError: 'Delete failed',
    importSuccess: 'Import successful',
    importError: 'Import failed',
    exportSuccess: 'Export successful',
    exportError: 'Export failed',
    confirmCleanAll: 'Confirm clear all data?',
    confirmUnlock: 'Confirm unlock account ',
    unlockSuccess: 'Account unlocked successfully',
    unbindSuccess: 'Unbind successfully',
    visible: 'Visible',
    invisible: 'Hidden',
    assignSuccess: 'Assignment successful',
    cancelSuccess: 'Authorization cancelled successfully',
    confirmCancelSelected: 'Are you sure you want to cancel authorization for selected users?',
    generateSuccess: 'Generation successful',
    resetSuccess: 'Reset successful',

    // Notification messages
    noData: 'No data available',
    invalidParams: 'Invalid parameters',
    networkError: 'Network error, please try again later',
    unauthorized: 'You do not have permission for this operation',
    sessionExpired: 'Session expired, please log in again',

    saveMainFirst: 'Please save the main table information first, then the sub-table function will be automatically enabled',
    total: 'Total',
    items: 'items',
    search: 'Search...',

    confirmForceLogout: 'Confirm force logout ',
    confirmBatchForceLogout: 'Confirm batch force logout'
  },

  /** Modal/Notification (modal.ts) */
  modal: {
    systemPrompt: 'System Prompt',
    notification: 'Notification',
    error: 'Error',
    success: 'Success',
    warning: 'Warning',
    ok: 'OK',
    cancel: 'Cancel'
  },

  /** 占位符 */
  placeholder: {
    input: 'Please enter ',
    select: 'Please select '
  },

  /** 提示*/
  tooltip: {
    showSearch: 'Show Search',
    print: 'Print',
    hideSearch: 'Hide Search',
    resetSearch: 'Reset Search',
    refresh: 'Refresh',
    columns: 'Show/Hide Columns',
    columnSettings: 'Column Settings'
  },

  /** Column Settings */
  columnSettings: {
    title: 'Column Settings',
    reset: 'Reset',
    dragHint: 'Drag to reorder',
    resetSuccess: 'Column settings have been reset'
  },

  /** File Preview Component */
  filePreview: {
    videoNotSupported: 'Your browser does not support the video tag',
    audioNotSupported: 'Your browser does not support the audio tag',
    officeDocument: 'Office Document',
    officePreviewRequiresPublicUrl: 'Office online preview requires a publicly accessible file URL',
    localNetworkTip: 'Current environment is local/intranet, Microsoft Office Viewer cannot access the file.',
    downloadToOpen: 'Please download and open with local Office software.',
    downloadFile: 'Download File',
    loading: 'Loading...',
    previewNotSupported: 'This file type does not support online preview',
    loadFailed: 'Failed to load file content',
    loadFailedWithError: 'Failed to load file content: {error}'
  },

  /** Icon Select Component */
  iconSelect: {
    placeholder: 'Click to select icon',
    searchPlaceholder: 'Search icons (code or name)',
    totalIcons: '{count} icons in total'
  },

  /** Recharge Component */
  recharge: {
    title: 'Online Recharge (Auto)',
    paymentCompleted: 'Payment Completed',
    rechargeAmount: 'Amount',
    yuan: '',
    paymentMethod: 'Payment Method',
    wechatPay: 'WeChat Pay',
    alipay: 'Alipay',
    wechat: 'WeChat',
    alipayShort: 'Alipay',
    scanToPay: 'Scan with {method} to pay {amount}',
    refreshQrCode: 'Refresh',
    paymentProblem: 'Payment Issue',
    refund: 'Refund',
    rechargePrefix: 'Recharge ',
    onlineRecharge: 'Online Recharge',
    generateQrCodeFailed: 'Failed to generate payment QR code',
    createOrderFailed: 'Failed to create order',
    selectRechargeAmount: 'Please select recharge amount',
    paymentSuccess: 'Payment successful',
    copiedContact: 'Copied QQ/WeChat: 770492966, please contact~',
    noRefundableOrder: 'No refundable order',
    confirmRefund: 'Confirm refund? The order will be closed after refund.',
    refundSuccess: 'Refund successful'
  },

  /** User Select Component */
  userSelect: {
    title: 'User Selection',
    selectUser: 'Select User',
    deptFilter: 'Department Filter',
    deptPlaceholder: 'Please enter department name',
    userName: 'User Name',
    phone: 'Phone',
    createTime: 'Created Time',
    userId: 'User ID',
    nickName: 'Nickname',
    dept: 'Department',
    status: 'Status',
    defaultUserName: 'User {id}'
  },

  /** OSS Media Manager Component */
  ossMediaManager: {
    title: 'Media Library',
    fileTypeHint: 'Currently only supports selecting {types} format files',
    hideDirectory: 'Hide Directory',
    showDirectory: 'Show Directory',
    folder: 'Folder',
    add: 'New',
    rename: 'Rename',
    delete: 'Delete',
    closeDirectory: 'Close Directory',
    directoryPlaceholder: 'Please enter directory name',
    uploadFile: 'Upload File',
    replace: 'Replace',
    moveTo: 'Move To',
    batchDelete: 'Batch Delete',
    searchPlaceholder: 'Search file name',
    selectedCount: '{count} selected',
    noSelection: 'No selection',
    fileType: 'File Type',
    allFiles: 'All Files',
    images: 'Images',
    documents: 'Documents',
    videos: 'Videos',
    audios: 'Audios',
    others: 'Others',
    noFiles: 'No files',
    unknownSize: 'Unknown size',
    loading: 'Loading...',
    loadingMore: 'Loading more...',
    allFilesLoaded: 'All files loaded',
    filePreview: 'File Preview',
    videoNotSupported: 'Your browser does not support video playback',
    audioNotSupported: 'Your browser does not support audio playback',
    loadTextContent: 'Load Text Content',
    downloadFile: 'Download File',
    moveToFolder: 'Move to Folder',
    cancel: 'Cancel',
    confirm: 'Confirm',
    directoryName: 'Directory Name',
    directoryNamePlaceholder: 'Please enter directory name',
    selectFile: 'Select File',
    replaceUploadTip: 'Click or drag file here to replace, only supports same type files',
    uploadTip: 'Click or drag files here to upload',
    close: 'Close',
    confirmSelection: 'Confirm Selection',
    sortBy: 'Sort By',
    sortUpdateTimeDesc: 'Update Time (Newest First)',
    sortUpdateTimeAsc: 'Update Time (Oldest First)',
    sortFileNameAsc: 'File Name (A-Z)',
    sortFileNameDesc: 'File Name (Z-A)',
    sortFileSizeAsc: 'File Size (Smallest First)',
    sortFileSizeDesc: 'File Size (Largest First)',
    sortFileTypeAsc: 'File Type (A-Z)',
    sortFileTypeDesc: 'File Type (Z-A)',
    directoryNameRequired: 'Directory name is required',
    directoryNameLength: 'Directory name length must be between 1 and 50 characters',
    directoryNameInvalid: 'Directory name cannot contain special characters \\ / : * ? " < > |',
    loadDirectoryFailed: 'Failed to load directory',
    loadingText: 'Loading text content...',
    loadTextFailed: 'Failed to load text content',
    requestFailed: 'Request failed: {status}',
    addDirectory: 'Add Directory',
    addSubDirectory: 'Add Subdirectory',
    renameDirectory: 'Rename Directory',
    hasChildDirectory: 'Has Subdirectory',
    confirmDeleteDirectory: 'Are you sure to delete directory "{name}"?',
    warning: 'Warning',
    deleteSuccess: 'Deleted successfully',
    addDirectorySuccess: 'Directory added successfully',
    updateDirectorySuccess: 'Directory updated successfully',
    selectOneFileToReplace: 'Please select one file to replace',
    replaceFile: 'Replace File',
    fileSizeLimit: 'File size cannot exceed {size}MB',
    fileTypeMustMatch: 'File type must match original file',
    fileTypeMustBe: 'File type must be {type}',
    fileReplaceSuccess: 'File replaced successfully',
    uploadSuccess: 'Uploaded successfully',
    uploadFailed: 'Upload failed',
    selectFilesToMove: 'Please select files to move',
    selectTargetDirectory: 'Please select target directory',
    moveSuccess: 'Moved successfully',
    confirmDeleteFile: 'Are you sure to delete file "{name}"?',
    deleteFileFailed: 'Failed to delete file',
    selectFilesToDelete: 'Please select files to delete',
    confirmBatchDelete: 'Are you sure to delete {count} selected files?',
    batchDeleteFailed: 'Failed to batch delete',
    batchDeleteSuccess: 'Batch deleted successfully',
    pleaseSelectFile: 'Please select a file',
    anyType: 'any type',
    imageType: 'image',
    documentType: 'document',
    videoType: 'video',
    audioType: 'audio',
    otherType: 'other'
  },

  /** Form Upload Component */
  formUpload: {
    selectFromLibrary: 'Select from Library',
    clearAll: 'Clear All',
    downloadImage: 'Download Image',
    previewTitle: 'Image Preview',
    // Default values
    defaultLabel: 'Image',
    selectImage: 'Select Image',
    // Drag hints
    dragHint: 'Drag image here, or ',
    clickToUpload: 'click to upload',
    // Upload hint templates
    uploadHintPrefix: 'Please upload',
    sizeLimit: ' size not exceeding ',
    formatHint: ' format ',
    imageSuffix: ' images',
    countLimit: ', max ',
    countUnit: ' images',
    compressHint: ', exceeding ',
    willCompress: ' will be auto-compressed',
    directMode: '(Direct Upload)',
    // Loading hints
    loadingImageInfo: 'Loading image info...',
    uploadingImage: 'Uploading image, please wait...',
    deletingFiles: 'Deleting {count} cloud files, please wait...',
    // Clear confirmation messages
    noImageToClear: 'No images to clear',
    confirmClearAll: 'Are you sure to clear all images?',
    confirmClearCloud: '{count} cloud files will be deleted',
    confirmClearLocal: '{count} local files will be cleared',
    cloudFilesDeleted: '{count} cloud files deleted successfully',
    localFilesCleared: '{count} local files cleared',
    cloudDeleteFailed: 'Failed to delete cloud files: ',
    clearCompleted: 'Clear completed: ',
    partialClearCompleted: 'Partial clear completed: ',
    // Error messages
    formatError: 'Invalid image format, please upload {formats} format images!',
    filenameError: 'Invalid filename, cannot contain commas!',
    sizeError: 'Image size cannot exceed {size} MB!',
    countError: 'Cannot upload more than {limit} images!',
    uploadFailed: 'Image upload failed',
    fileFailed: 'File upload failed',
    confirmFailed: 'Upload confirmation failed',
    compressFailed: 'Image compression failed: ',
    directUploadFailed: 'Direct upload failed: ',
    selectImageFile: 'Please select an image file!',
    confirmDeleteCloud: 'This will delete the cloud image, confirm?',
    // Success messages
    replaceSuccess: 'Image replaced successfully',
    uploadSuccess: 'Image uploaded successfully',
    directUploadSuccess: 'Image direct upload successful',
    selectSuccess: 'Image selected successfully',
    // Mixed format warning
    mixedFormatWarning: 'Mixed format files detected, will return URL format for consistency',

    // ========== File Upload Component ==========
    // Default values
    defaultFileLabel: 'File',
    selectFile: 'Select File',
    // Drag hints
    fileDragHint: 'Drag file here, or ',
    // Upload hints
    fileSuffix: ' files',
    fileCountUnit: ' files',
    // Preview modal
    filePreviewTitle: 'File Preview',
    downloadFile: 'Download File',
    // Loading hints
    loadingFileInfo: 'Loading file info...',
    uploadingFile: 'Uploading file, please wait...',
    // Clear confirmation messages
    noFileToClear: 'No files to clear',
    confirmClearAllFiles: 'Are you sure to clear all files?',
    // Error messages
    fileFormatError: 'Invalid file format, please upload {formats} format files!',
    filenameHasCommaError: 'Invalid filename, cannot contain commas!',
    fileSizeError: 'File size cannot exceed {size} MB!',
    fileCountError: 'Cannot upload more than {limit} files!',
    directUploadFileFailed: 'Direct upload file failed: ',
    uploadFileFailed: 'Upload file failed: ',
    // Success messages
    fileReplaceSuccess: 'File replaced successfully',
    fileUploadSuccess: 'File uploaded successfully',
    fileDirectUploadSuccess: 'File direct upload successful',
    selectFileSuccess: 'File selected successfully',
    confirmDeleteCloudFile: 'This will delete the cloud file, confirm?',
    // Clear operation related
    clearConfirmCloudAndLocal:
      'Are you sure to clear all files?\n• {cloudCount} cloud files will be deleted\n• {localCount} local files will be cleared',
    clearConfirmCloud: 'Are you sure to clear all files?\n{count} cloud files will be deleted.',
    clearConfirmLocal: 'Are you sure to clear all files?\n{count} local files will be cleared.',
    deletingCloudFiles: 'Deleting {count} cloud files, please wait...',
    deleteCloudFileFailed: 'Failed to delete cloud files: ',
    unknownError: 'Unknown error'
  },

  /** Rich Text Editor Component */
  formEditor: {
    // Default values
    defaultLabel: 'Rich Text',
    placeholder: 'Please enter content...',
    // Image upload
    imageUploadSuccess: 'Image uploaded successfully',
    imageUploadFailed: 'Image upload failed',
    imageUploadResponseError: 'Image upload failed: Invalid response format',
    // Video upload
    videoUploadSuccess: 'Video uploaded successfully',
    videoUploadFailed: 'Video upload failed',
    videoUploadResponseError: 'Video upload failed: Invalid response format',
    // Error messages
    uploadFailed: 'Upload failed',
    invalidBase64Data: 'Invalid Base64 data'
  },

  /** Map Location Picker Component */
  formMap: {
    // Dialog and buttons
    dialogTitle: 'Select Location',
    buttonText: 'Select Location',
    confirmButton: 'Confirm Selection',
    // Search
    searchPlaceholder: 'Enter place name to search',
    searchButton: 'Search',
    searchKeywordRequired: 'Please enter search keyword',
    searchNoResult: 'No places found, please try other keywords',
    searchLocated: 'Located: {name}',
    // Map loading
    mapLoading: 'Loading map...',
    mapLoadFailed: 'Failed to load Amap, please check configuration and network',
    mapNotConfigured: 'Amap not configured, please configure the Key',
    geocoderNotInit: 'Geocoder not initialized',
    getAddressFailed: 'Failed to get address information',
    // Location info labels
    longitude: 'Longitude',
    latitude: 'Latitude',
    province: 'Province',
    city: 'City',
    district: 'District',
    adcode: 'Admin Code',
    detailAddress: 'Detail Address',
    // Tips and validation
    emptyTip: 'Click on the map to select a location',
    selectFirst: 'Please select a location first',
    areaHint: 'Tip: Please select a location within the current administrative area (Area code: {code})',
    validationMessage: 'Selected location is not within the specified area, please select again!',
    requiredLocation: 'Required location: {name} (Area code: {code})',
    requiredAreaCode: 'Required area code: {code}',
    currentLocation: 'Current location: {name} (Area code: {code})',
    currentAreaCode: 'Current area code: {code}'
  },

  /** Table Select Component */
  formTableSelect: {
    modalTitle: 'Select Data',
    searchPlaceholder: 'Enter keyword to search'
  },

  /** Date Picker Component */
  formDate: {
    startDate: 'Start Date',
    endDate: 'End Date',
    weekFormat: 'YYYY Week ww'
  },

  /** Excel Import Component */
  importExcel: {
    dragHint: 'Drag file here, or ',
    clickToUpload: 'click to upload',
    autoUpdateHint: 'Import will automatically update existing data',
    formatHint: 'Only .xls, .xlsx format files are allowed',
    downloadTemplate: 'Download Template',
    excelOnlyWarning: 'Can only upload Excel files!',
    noUrlWarning: 'Import URL or template URL is required',
    importSuffix: ' Import',
    importResult: 'Import Result',
    templateSuffix: ' Template',
    selectFileFirst: 'Please select a file to import first',
    noFileAvailable: 'No file available to upload'
  },

  /** Table Component */
  table: {
    emptyText: 'No data',
    indexLabel: 'No.',
    booleanYes: 'Yes',
    booleanNo: 'No',
    currencySymbol: '$',
    copySuccess: 'Copied successfully',
    copyFieldSuccess: '{field} copied successfully',
    // Print related
    printTitle: 'Data List',
    printFailed: 'Print failed, please try again',
    actionsColumn: 'Actions',
    switchEnabled: 'Enabled',
    switchDisabled: 'Disabled'
  },

  /** Detail Component */
  detail: {
    title: 'Details',
    show: 'Show',
    hide: 'Hide',
    copy: 'Copy',
    copyContent: 'Copy content',
    viewAttachment: 'View attachment',
    basicInfo: 'Basic Info',
    print: 'Print',
    printFailed: 'Print failed'
  },

  /** 路由国际化配置 */
  route: {
    dashboard: 'Dashboard',
    document: 'Document'
  },

  label: {
    large: 'Large',
    default: 'Default',
    small: 'Small'
  },

  /** 登录页面国际化 */
  login: {
    selectPlaceholder: 'Please select/enter a company name',
    tenantId: 'TenantId',
    userName: 'UserName',
    password: 'Password',
    login: 'Login',
    logging: 'Logging...',
    code: 'Verification Code',
    rememberPassword: 'Remember me',
    switchRegisterPage: 'Sign up now',
    forgotPassword: 'Forgot Password?',
    noAccount: "Don't have an account?",
    registerNow: 'Register Now',
    thirdPartyConfigError: 'Third-party login configuration error',
    ruoyiStudio: 'Ruoyi Studio',
    // 左侧品牌展示区域
    leftView: {
      title: 'Welcome',
      subtitle: 'Simple, Efficient, Modern Management System'
    },
    // 表单验证规则
    rule: {
      tenantId: { required: 'Please enter your tenant id' },
      userName: { required: 'Please enter your account' },
      password: { required: 'Please enter your password' },
      code: { required: 'Please enter a verification code' }
    },
    // 第三方登录方式
    social: {
      wechat_open: 'WeChat Open Platform',
      wechat_mp: 'WeChat Official Account',
      wechat_enterprise: 'WeChat Work',
      dingtalk: 'DingTalk',
      maxkey: 'MaxKey',
      topiam: 'TopIAM',
      qq: 'QQ',
      weibo: 'Weibo',
      gitee: 'Gitee',
      github: 'GitHub',
      gitlab: 'GitLab',
      baidu: 'Baidu',
      csdn: 'CSDN',
      coding: 'Coding',
      oschina: 'OSChina',
      alipay_wallet: 'Alipay',
      taobao: 'Taobao',
      douyin: 'Douyin',
      linkedin: 'LinkedIn',
      microsoft: 'Microsoft',
      renren: 'Renren',
      stack_overflow: 'Stack Overflow',
      huawei: 'Huawei',
      aliyun: 'Alibaba Cloud',
      gitea: 'Gitea'
    }
  },

  /**注册页面国际化 */
  register: {
    selectPlaceholder: 'Please select/enter a company name',
    userName: 'UserName',
    password: 'Password',
    confirmPassword: 'Confirm Password',
    register: 'Register',
    registering: 'Registering...',
    registerSuccess: 'Congratulations, your {userName} account has been registered!',
    code: 'Verification Code',
    hasAccount: 'Already have an account?',
    switchLoginPage: 'Sign in',
    subtitle: 'Create your account',
    inviteRole: 'Invited Role',
    department: 'Department',
    invitedRole: 'Invited Registration Role',
    approvalRequired: 'Admin approval required after registration',
    inviteCodeSuccess: 'Register via invitation code, will be automatically assigned to role',
    inviteCodeInvalid: 'Invitation code is invalid or expired, will proceed with normal registration',
    congratsInvite: 'Congratulations on successful registration via invitation!',
    waitApproval: 'Please wait for admin approval before logging in',
    loginImmediately: 'You can log in immediately',
    registrationSuccess: 'Registration Successful!',
    usernameLabel: 'Username',
    roleLabel: 'Role',
    systemNotice: 'System Notice',
    thirdPartyConfigError: 'Third-party registration configuration error',
    // 表单验证规则（包含动态参数示例）
    rule: {
      tenantId: { required: 'Please enter your tenant id' },
      userName: {
        required: 'Please enter your account',
        length: 'The length of the user account must be between {min} and {max}'
      },
      password: {
        required: 'Please enter your password',
        length: 'The user password must be between {min} and {max} in length',
        pattern: "Can't contain illegal characters: {strings}"
      },
      code: { required: 'Please enter a verification code' },
      confirmPassword: {
        required: 'Please enter your password again',
        equalToPassword: 'The password entered twice is inconsistent'
      }
    }
  },

  /** Forgot Password Page i18n */
  forgotPassword: {
    title: 'Reset Password',
    subtitle: 'Please enter your email address and we will send you a reset link',
    emailPlaceholder: 'Please enter your email address',
    codePlaceholder: 'Please enter the verification code',
    submitBtn: 'Send Reset Link',
    submitting: 'Sending...',
    backToLogin: 'Back to Login',
    success: 'Reset password link has been sent to your email, please check',
    // Form validation rules
    rule: {
      emailRequired: 'Please enter your email address',
      emailFormat: 'Please enter a valid email format',
      codeRequired: 'Please enter the verification code'
    }
  },

  /**导航栏国际化 */
  navbar: {
    searchMenu: 'Search Menu',
    full: 'Full Screen',
    exitFull: 'Exit Full Screen',
    language: 'Language',
    dashboard: 'Dashboard',
    document: 'Document',
    git: 'Git',
    message: 'Message',
    layoutSize: 'Layout Size',
    sizeChangeSuccess: 'SizeChangeSuccess',
    selectTenant: 'Select Tenant',
    layoutSetting: 'Layout Setting',
    personalCenter: 'Personal Center',
    logout: 'Logout',
    aiChat: 'AiChat',
    // Notice
    notice: {
      title: 'Notifications',
      markAllAsRead: 'Mark all as read',
      tabs: {
        notice: 'Notifications'
      },
      unread: 'Unread',
      empty: 'No notifications',
      publisher: 'Publisher',
      publishTime: 'Publish Time',
      close: 'Close',
      getDetailFailed: 'Failed to get notification details',
      markedAsRead: 'All marked as read'
    },
    // AI Assistant
    aiAssistant: {
      title: 'AI Assistant',
      newChat: 'New Chat',
      newSessionTitle: 'New Chat',
      noHistory: 'No chat history',
      startNewChat: 'Start New Chat',
      clearMessages: 'Clear Chat',
      settings: 'Settings',
      startChatTitle: 'Start Chat with AI',
      startChatDesc: 'Enter your question and let AI assistant help you',
      you: 'You',
      assistant: 'AI Assistant',
      editPlaceholder: 'Edit your message...',
      cancel: 'Cancel',
      saveAndResend: 'Save and Resend',
      generating: 'Generating...',
      copy: 'Copy',
      edit: 'Edit',
      regenerate: 'Regenerate',
      tokenInput: 'Input',
      tokenOutput: 'Output',
      tokenTotal: 'Total',
      // Deep thinking (reasoning models)
      thinkingInProgress: 'Thinking...',
      thinkingDone: 'Thought process',
      expandThinking: 'Show thinking',
      collapseThinking: 'Hide thinking',
      // Deep-thinking level (control only shows when enabled on the backend)
      deepThinking: 'Deep Thinking',
      thinkingLevelStandard: 'Standard',
      thinkingLevelDeep: 'Deep',
      thinkingLevelOff: 'Off this time',
      modelProvider: 'Model Provider',
      selectProvider: 'Select Provider',
      inputPlaceholder: 'Enter message... (Enter to send, Shift+Enter for new line)',
      send: 'Send',
      // Time format
      time: {
        justNow: 'Just now',
        minutesAgo: '{n} minutes ago'
      },
      // Status text
      status: {
        sending: 'Sending',
        streaming: 'Generating',
        complete: 'Complete',
        error: 'Error'
      },
      // Messages
      message: {
        wsNotConnected: 'WebSocket not connected, please refresh the page',
        sessionCreated: 'New chat created',
        sessionDeleted: 'Chat deleted',
        messagesCleared: 'Messages cleared',
        copiedToClipboard: 'Copied to clipboard',
        copyFailed: 'Copy failed',
        confirmDeleteSession: 'Are you sure you want to delete this chat?',
        confirmClearMessages: 'Are you sure you want to clear all messages in this chat?'
      }
    }
  },

  /**页签*/
  tagsView: {
    refresh: 'Refresh',
    closeCurrent: 'Close Current',
    closeOthers: 'Close Others',
    closeLeft: 'Close Left',
    closeRight: 'Close Right',
    closeAll: 'Close All'
  },

  /** Page titles (for browser tab) */
  page: {
    redirect: 'Page Redirect',
    socialCallback: 'Social Login Callback',
    login: 'Login',
    register: 'Register',
    forgotPassword: 'Forgot Password',
    notFound: '404 Not Found',
    unauthorized: '401 Unauthorized',
    profile: 'Profile',
    datavFullscreen: 'DataV Fullscreen'
  },

  /**菜单系统配置（核心结构） */
  menu: {
    // 首页菜单
    home: 'Home',
    index: 'Index',

    // 系统管理模块
    system: {
      _self: 'System', // 父级菜单名称
      user: 'Users',
      role: 'Roles',
      menu: 'Menus',
      dept: 'Departments',
      post: 'Posts',
      dict: 'Dictionary',
      config: 'Parameters',
      notice: 'Notices',
      // 日志管理子模块
      log: {
        _self: 'Logs',
        operLog: 'Operation Log',
        loginLog: 'Login Log'
      },
      oss: 'Files',
      sysOssDirectory: 'OSS Directory'
    },

    // 系统监控模块
    monitor: {
      _self: 'Monitor',
      online: 'Online Users',
      cache: 'Cache',
      admin: 'Admin',
      snailjob: 'Tasks'
    },

    // 工具模块
    tool: {
      _self: 'Tools',
      gen: 'Code Gen'
    },

    // 租户管理模块
    tenant: {
      _self: 'Tenant',
      tenant: 'Tenants',
      tenantPackage: 'Packages'
    },

    // 应用配置模块
    appConfiguration: {
      _self: 'APP Config',
      appConfig: 'Multi-end'
    },

    // 商城管理
    mallManage: {
      _self: 'Mall',
      goods: 'Products'
    }
  },

  /** Chart components */
  chart: {
    noData: 'No Data',
    map: {
      name: 'Name',
      code: 'Code',
      level: 'Level',
      unknownRegion: 'Unknown Region'
    },
    bidirectional: {
      positiveData: 'Positive Data',
      negativeData: 'Negative Data'
    },
    pie: {
      dataRatio: 'Data Ratio'
    },
    candlestick: {
      time: 'Time: ',
      open: 'Open: ',
      close: 'Close: ',
      low: 'Low: ',
      high: 'High: '
    }
  },

  /** Field translations (root level, for AForm* components) */
  // Department
  dept: 'Department',
  parentId: 'Parent Department',
  deptName: 'Department Name',
  deptCategory: 'Category Code',
  areaCode: 'Region',
  orderNum: 'Sort Order',
  leader: 'Leader',
  // Common fields
  searchValue: 'Search',
  phone: 'Phone',
  email: 'Email',
  status: 'Status',
  createTime: 'Create Time',
  updateTime: 'Update Time',
  remark: 'Remark',
  createBy: 'Created By',
  updateBy: 'Updated By',

  /** Form validation messages */
  validation: {
    // Department
    parentIdRequired: 'Parent department is required',
    deptNameRequired: 'Department name is required',
    orderNumRequired: 'Sort order is required',
    emailInvalid: 'Please enter a valid email address',
    phoneInvalid: 'Please enter a valid phone number'
  },

  /** Common text */
  common: {
    empty: 'N/A',
    cancel: 'Cancel',
    apply: 'Apply',
    copy: 'Copy',
    enable: 'Enable',
    disable: 'Disable',
    operation: 'Operation',
    edit: 'Edit',
    delete: 'Delete',
    addSuccess: 'Added successfully'
  },

  /** Card components */
  card: {
    // Stats card
    stats: {
      completed: 'Completed'
    },
    // Social card
    social: {
      edit: 'Edit',
      delete: 'Delete',
      report: 'Report',
      like: 'Like',
      comment: 'Comment',
      share: 'Share'
    },
    // Data list card
    dataList: {
      view: 'View',
      viewMore: 'View More',
      noData: 'No Data'
    },
    // User card
    user: {
      follow: 'Follow',
      following: 'Following',
      message: 'Message'
    },
    // Profile card
    profile: {
      edit: 'Edit Profile',
      viewProfile: 'View Profile'
    },
    // Form card
    form: {
      submit: 'Submit',
      reset: 'Reset'
    },
    // Table card
    tableCard: {
      noData: 'No Data',
      viewAll: 'View All'
    },
    // Image card
    image: {
      preview: 'Preview'
    },
    // Activity card
    activity: {
      viewDetails: 'View Details',
      join: 'Join'
    },
    // Timeline card
    timeline: {
      viewMore: 'View More',
      noData: 'No Records'
    },
    // Weather card
    weather: {
      humidity: 'Humidity',
      wind: 'Wind',
      feelsLike: 'Feels Like',
      forecast: 'Forecast',
      today: 'Today',
      tomorrow: 'Tomorrow',
      celsius: '°C',
      fahrenheit: '°F',
      kmh: 'km/h',
      mph: 'mph',
      sunny: 'Sunny',
      cloudy: 'Cloudy',
      rainy: 'Rainy',
      snowy: 'Snowy',
      windy: 'Windy',
      foggy: 'Foggy'
    },
    // Pricing card
    pricing: {
      perMonth: '/mo',
      perYear: '/yr',
      selectPlan: 'Select Plan',
      currentPlan: 'Current Plan',
      popular: 'Popular'
    },
    // Notification card
    notification: {
      markAsRead: 'Mark as Read',
      markAllAsRead: 'Mark All as Read',
      delete: 'Delete',
      noNotifications: 'No Notifications',
      viewAll: 'View All',
      justNow: 'Just now',
      minutesAgo: '{n} minutes ago',
      hoursAgo: '{n} hours ago',
      daysAgo: '{n} days ago',
      info: 'Info',
      success: 'Success',
      warning: 'Warning',
      error: 'Error'
    }
  },
  /** AI Components */
  ai: {
    // AI Text Optimizer
    textOptimizer: {
      currentContent: 'Current Content',
      charCount: '{count} characters',
      inputPlaceholder: 'Enter text content to optimize...',
      optimizeMethod: 'Optimization Method',
      advancedOptions: 'Advanced Options',
      styleRequirement: 'Style Requirement',
      selectStyle: 'Select Style',
      styles: {
        professional: 'Professional',
        casual: 'Casual',
        creative: 'Creative',
        concise: 'Concise'
      },
      targetLength: 'Target Length',
      minLength: 'Min',
      maxLength: 'Max',
      keywords: 'Keywords to Keep',
      keywordsPlaceholder: 'Enter keywords to preserve, separated by commas',
      translateLanguage: 'Translation Language',
      languages: {
        en: 'English',
        ja: 'Japanese',
        ko: 'Korean',
        fr: 'French',
        de: 'German',
        es: 'Spanish'
      },
      customRequirement: 'Custom Requirement',
      customPlaceholder: 'Enter custom optimization requirements...',
      processing: 'Processing...',
      startOptimize: 'Start Optimization',
      optimizeResult: 'Optimization Result',
      compare: 'Compare',
      generating: 'Generating optimized content...',
      tokenUsage: {
        totalTokens: 'Total Tokens',
        inputTokens: 'Input',
        outputTokens: 'Output'
      },
      otherSuggestions: 'Other Suggestions',
      contentCompare: 'Content Comparison',
      originalText: 'Original',
      optimizedText: 'Optimized',
      wordCount: '{count} words',
      inputAndSelectRequired: 'Please enter content and select optimization method',
      optimizeFailed: 'Optimization failed, please try again',
      optimizeComplete: 'Optimization complete',
      copySuccess: 'Copied to clipboard',
      types: {
        polish: 'Polish',
        expand: 'Expand',
        shorten: 'Shorten',
        formal: 'Formalize',
        casual: 'Casualize',
        marketing: 'Marketing',
        seo: 'SEO Optimize'
      }
    },
    // AI Assistant
    assistant: {
      features: {
        optimize: 'Text Optimization',
        generate: 'Content Generation',
        review: 'Content Review',
        translate: 'Translation'
      },
      history: 'History',
      inputLabel: 'Input',
      outputLabel: 'Output',
      useThisResult: 'Use This Result',
      advancedSettings: 'Advanced Settings',
      modelSelect: 'Model Selection',
      selectProvider: 'Select Provider',
      providers: {
        qianwen: 'Qianwen'
      },
      creativity: 'Creativity',
      roleSettings: 'Role Settings',
      rolePlaceholder: 'Set a role for AI, e.g.: You are a professional copywriter...',
      collapseSettings: 'Collapse Settings',
      applyResult: 'Apply Result',
      quickActions: {
        optimizeContent: 'Optimize Content',
        regenerate: 'Regenerate',
        translate: 'Translate'
      },
      processing: 'Processing...',
      buttons: {
        optimize: 'Start Optimization',
        generate: 'Start Generation',
        review: 'Start Review',
        translate: 'Start Translation',
        suggest: 'Get Suggestions'
      },
      title: 'AI Assistant',
      titles: {
        optimize: 'Text Optimization',
        generate: 'Content Generation',
        review: 'Content Review',
        translate: 'Translation Assistant',
        suggest: 'Smart Suggestions'
      },
      processFailed: 'Processing failed, please try again',
      resultApplied: 'Result applied'
    },
    // AI Data Generator
    dataGenerator: {
      generateConfig: 'Generation Config',
      fieldConfig: 'Field Config',
      generateCount: 'Generate Count',
      countUnit: 'records',
      dataLanguage: 'Data Language',
      languages: {
        zh: 'Chinese',
        en: 'English',
        mixed: 'Mixed'
      },
      valueRange: 'Value Range',
      min: 'Min',
      max: 'Max',
      relatedFields: 'Related Fields',
      select: 'Select',
      selectRelatedField: 'Select related field',
      addField: 'Add Field',
      selectGenerateFields: 'Please select fields to generate',
      generating: 'Generating...',
      startGenerate: 'Start Generation',
      generateResult: 'Generation Result',
      generated: 'Generated',
      records: 'records',
      edit: 'Edit',
      delete: 'Delete',
      copyAll: 'Copy All',
      exportJson: 'Export JSON',
      exportExcel: 'Export Excel',
      clearAll: 'Clear All',
      editRecord: 'Edit Record',
      selectFieldsFirst: 'Please select fields to generate first',
      generateFailed: 'Generation failed, please try again',
      generateSuccess: 'Successfully generated {count} records',
      saveSuccess: 'Saved successfully',
      deleteSuccess: 'Deleted successfully',
      exportSuccess: 'Exported successfully',
      exportExcelDeveloping: 'Export Excel requires integration with export tool',
      copySuccess: 'Copied successfully',
      clearSuccess: 'Cleared'
    },
    // AI Content Reviewer
    contentReviewer: {
      reviewConfig: 'Review Config',
      reviewLevel: 'Review Level',
      levels: {
        loose: 'Loose',
        normal: 'Normal',
        strict: 'Strict'
      },
      checkItems: 'Check Items',
      items: {
        compliance: 'Compliance Check',
        sensitive: 'Sensitive Words Detection',
        quality: 'Quality Assessment',
        completeness: 'Completeness Check',
        format: 'Format Validation'
      },
      autoFix: 'Auto Fix',
      autoFixTip: 'AI will attempt to automatically fix discovered issues',
      contentToReview: 'Content to Review',
      loadFromForm: 'Load from Form',
      inputPlaceholder: 'Please enter {field}',
      customRules: 'Custom Review Rules',
      field: 'Field',
      selectField: 'Select field',
      ruleType: 'Rule Type',
      ruleTypes: {
        required: 'Required',
        length: 'Length Limit',
        format: 'Format Requirement',
        keywords: 'Keywords',
        regex: 'Regular Expression'
      },
      ruleValue: 'Rule Value',
      tip: 'Tip',
      errorTip: 'Error message',
      addRule: 'Add Rule',
      reviewing: 'Reviewing...',
      startReview: 'Start Review',
      reviewResult: 'Review Result',
      overallScore: 'Overall Score',
      scores: {
        compliance: 'Compliance:',
        quality: 'Quality:',
        completeness: 'Completeness:'
      },
      foundIssues: 'Found {count} issues',
      issueLocation: 'Issue Location:',
      entireField: 'Entire field',
      issueContent: 'Issue Content:',
      fixSuggestion: 'Fix Suggestion:',
      afterFix: 'After Fix:',
      applyFix: 'Apply Fix',
      ignore: 'Ignore',
      reviewPassed: 'Review Passed',
      contentMeetsStandards: 'Content meets all review standards',
      exportReport: 'Export Report',
      reviewSummary: 'Review Summary',
      summary: {
        reviewTime: 'Review Time',
        reviewLevel: 'Review Level',
        reviewFields: 'Review Fields',
        foundIssues: 'Issues Found',
        severeIssues: 'Severe Issues',
        suggestedOptimizations: 'Suggested Optimizations',
        countUnit: '{count}'
      },
      reReview: 'Re-review',
      applyAllFixes: 'Apply All Fixes',
      status: {
        pass: 'Pass',
        warning: 'Warning',
        fail: 'Fail'
      },
      severity: {
        low: 'Suggestion',
        medium: 'Warning',
        high: 'Severe'
      },
      loadFromFormRequiresContext: 'Load from form requires context parameter',
      reviewFailed: 'Review failed, please try again',
      reviewPassedMsg: 'Content review passed',
      foundWarningIssues: 'Found {count} issues that need attention',
      foundSevereIssues: 'Found {count} severe issues',
      fixApplied: 'Fix applied',
      issueIgnored: 'Issue ignored',
      fixesApplied: 'Applied {count} fixes',
      report: {
        reviewTime: 'Review Time',
        reviewLevel: 'Review Level',
        reviewStatus: 'Review Status',
        overallScore: 'Overall Score',
        scoreDetails: 'Score Details',
        issuesList: 'Issues List',
        reviewContent: 'Review Content'
      },
      reportExported: 'Report exported successfully'
    },
    // Input with AI
    inputWithAi: {
      polish: 'Polish',
      expand: 'Expand',
      shorten: 'Shorten',
      formal: 'Formalize',
      casual: 'Casualize',
      translate: 'Translate',
      custom: 'Custom',
      moreFeatures: 'More Features',
      textOptimize: 'Text Optimization',
      contentRequired: 'Please enter content first',
      optimizeFailed: 'Optimization failed',
      emptyResult: 'Optimization result is empty',
      optimizeSuccess: 'Optimization successful',
      applyResult: 'Apply optimization result?',
      applied: 'Applied',
      optimizeError: 'Optimization error'
    }
  },

  /** Dictionary Data i18n */
  dict: {
    // Goods Spec Type
    m_goods_spec_type: {
      '0': 'Single Spec',
      '1': 'Multiple Specs'
    },
    // Audit Status
    sys_audit_status: {
      '0': 'Pending',
      '1': 'Approved',
      '2': 'Rejected',
      '3': 'Refused'
    },
    // Boolean Flag
    sys_boolean_flag: {
      '0': 'No',
      '1': 'Yes'
    },
    // Data Scope
    sys_data_scope: {
      '1': 'All Data',
      '2': 'Custom Data',
      '3': 'Department Data',
      '4': 'Department & Sub-departments',
      '5': 'Personal Data Only',
      '6': 'Department & Sub-departments or Personal'
    },
    // Display Setting
    sys_display_setting: {
      '0': 'Hidden',
      '1': 'Visible'
    },
    // Enable Status
    sys_enable_status: {
      '0': 'Disabled',
      '1': 'Enabled'
    },
    // File Type
    sys_file_type: {
      image: 'Image',
      document: 'Document',
      video: 'Video',
      audio: 'Audio',
      archive: 'Archive',
      other: 'Other'
    },
    // Message Type
    sys_message_type: {
      system: 'System',
      activity: 'Activity',
      audit: 'Audit',
      account: 'Account',
      private: 'Private'
    },
    // Notice Status
    sys_notice_status: {
      '0': 'Draft',
      '1': 'Send Now'
    },
    // Notice Type
    sys_notice_type: {
      '1': 'Notification',
      '2': 'Announcement'
    },
    // Operation Result
    sys_oper_result: {
      '0': 'Failed',
      '1': 'Success'
    },
    // Operation Type
    sys_oper_type: {
      '1': 'Add',
      '2': 'Edit',
      '3': 'Delete',
      '4': 'Authorize',
      '5': 'Export',
      '6': 'Import',
      '7': 'Force Logout',
      '8': 'Generate Code',
      '9': 'Clear Data',
      '99': 'Other'
    },
    // Order Status
    sys_order_status: {
      pending: 'Pending',
      paid: 'Paid',
      delivered: 'Delivered',
      completed: 'Completed',
      cancelled: 'Cancelled',
      refunded: 'Refunded'
    },
    // Payment Method
    sys_payment_method: {
      wechat: 'WeChat Pay',
      alipay: 'Alipay',
      unionpay: 'UnionPay',
      balance: 'Balance',
      points: 'Points'
    },
    // Platform Type
    sys_platform_type: {
      'mp-weixin': 'WeChat Mini Program',
      'mp-official-account': 'WeChat Official Account',
      'mp-qq': 'QQ Mini Program',
      'mp-alipay': 'Alipay Mini Program',
      'mp-baidu': 'Baidu Mini Program',
      'mp-toutiao': 'ByteDance Mini Program',
      'mp-lark': 'Lark Mini Program',
      'mp-kuaishou': 'Kuaishou Mini Program',
      'mp-jd': 'JD Mini Program',
      'mp-360': '360 Mini Program',
      'app-android': 'Android App',
      'app-ios': 'iOS App',
      'h5': 'H5 Web',
      'web': 'PC Web'
    },
    // User Gender
    sys_user_gender: {
      '0': 'Female',
      '1': 'Male',
      '2': 'Unknown'
    }
  }
}
