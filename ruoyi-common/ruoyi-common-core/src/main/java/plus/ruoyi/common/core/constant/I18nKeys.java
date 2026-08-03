package plus.ruoyi.common.core.constant;

/**
 * 国际化消息键常量
 * <p>
 * 统一管理系统中所有国际化消息的键常量，按功能模块分类组织
 * 使用方式：I18nKeys.分类.具体消息键
 *
 * @author 抓蛙师
 * @since 2025-06-29
 */
public interface I18nKeys {

    /**
     * 通用验证消息
     * <p>
     * 包含系统中通用的验证提示消息
     */
    interface Common {
        /** 必须填写 */
        String REQUIRED = "common.required";
        /** 长度必须在{min}到{max}个字符之间 */
        String LENGTH_INVALID = "common.length.invalid";
        /** 主键ID不能为空 */
        String ID_REQUIRED = "common.id.required";
    }

    /**
     * 通用操作结果消息
     * <p>
     * 包含系统中通用的操作成功提示消息
     */
    interface Oper {
        /** 操作成功 */
        String SUCCESS = "operation.success";
        /** 操作失败 */
        String FAIL = "operation.fail";
        /** 查询成功 */
        String QUERY_SUCCESS = "operation.query.success";
        /** 新增成功 */
        String ADD_SUCCESS = "operation.add.success";
        /** 修改成功 */
        String UPDATE_SUCCESS = "operation.update.success";
        /** 删除成功 */
        String DELETE_SUCCESS = "operation.delete.success";
        /** 导入成功 */
        String IMPORT_SUCCESS = "operation.import.success";
        /** 导出成功 */
        String EXPORT_SUCCESS = "operation.export.success";
    }

    /**
     * 用户相关消息
     * <p>
     * 包含用户认证、登录注册、个人信息验证等相关消息
     */
    interface User {
        // ========== 账号状态 ==========
        /** 对不起, 您的账号：{0} 不存在 */
        String ACCOUNT_NOT_EXISTS = "user.account.not.exists";
        /** 用户不存在/密码错误 */
        String PASSWORD_MISMATCH = "user.password.mismatch";
        /** 对不起，您的账号：{0} 已禁用，请联系管理员 */
        String ACCOUNT_DISABLED = "user.account.disabled";

        // ========== 登录注册 ==========
        /** 登录成功 */
        String LOGIN_SUCCESS = "user.login.success";
        /** 退出成功 */
        String LOGOUT_SUCCESS = "user.logout.success";
        /** 注册成功 */
        String REGISTER_SUCCESS = "user.register.success";
        /** 保存用户 {0} 失败，注册账号已存在 */
        String REGISTER_ACCOUNT_EXISTS = "user.register.account.exists";
        /** 注册失败，请联系系统管理人员 */
        String REGISTER_FAILED = "user.register.failed";

        // ========== 会话状态 ==========
        /** 会话已过期，请重新登录 */
        String SESSION_EXPIRED = "user.session.expired";
        /** 管理员强制退出，请重新登录 */
        String ADMIN_FORCE_LOGOUT = "user.admin.force.logout";
        /** 系统异常，请重新登录 */
        String SYSTEM_ERROR_RELOGIN = "user.system.error.relogin";

        // ========== 密码重试限制 ==========
        /** 密码输入错误{0}次 */
        String PASSWORD_RETRY_COUNT = "user.password.retry.count";
        /** 密码输入错误{0}次，帐户锁定{1}分钟 */
        String PASSWORD_RETRY_LOCKED = "user.password.retry.locked";

        // ========== 用户名验证 ==========
        /** 用户名不能为空 */
        String USERNAME_REQUIRED = "user.username.required";
        /** 2到20个汉字、字母、数字或下划线组成，且必须以非数字开头 */
        String USERNAME_FORMAT_INVALID = "user.username.format.invalid";
        /** 账户长度必须在{min}到{max}个字符之间 */
        String USERNAME_LENGTH_INVALID = "user.username.length.invalid";

        // ========== 密码验证 ==========
        /** 用户密码不能为空 */
        String PASSWORD_REQUIRED = "user.password.required";
        /** 用户密码长度必须在{min}到{max}个字符之间 */
        String PASSWORD_LENGTH_INVALID = "user.password.length.invalid";
        /** 密码格式不正确，5-50个字符 */
        String PASSWORD_FORMAT_INVALID = "user.password.format.invalid";
        /** 密码必须同时包含字母和数字 */
        String PASSWORD_COMPLEXITY_INVALID = "user.password.complexity.invalid";

        // ========== 邮箱验证 ==========
        /** 邮箱格式错误 */
        String EMAIL_FORMAT_INVALID = "user.email.format.invalid";
        /** 邮箱不能为空 */
        String EMAIL_REQUIRED = "user.email.required";

        // ========== 手机号验证 ==========
        /** 用户手机号不能为空 */
        String PHONE_REQUIRED = "user.phone.required";
        /** 手机号格式错误 */
        String PHONE_FORMAT_INVALID = "user.phone.format.invalid";


    }

    /**
     * 认证方式相关消息
     * <p>
     * 包含认证方式和角色相关的提示消息
     */
    interface Auth {
        /** 认证方式不支持 */
        String TYPE_UNSUPPORTED = "auth.type.unsupported";
        /** 认证方式已禁用 */
        String TYPE_DISABLED = "auth.type.disabled";
        /** 认证方式不能为空 */
        String TYPE_REQUIRED = "auth.type.required";
    }

    /**
     * 验证码相关消息
     * <p>
     * 包含短信验证码和邮箱验证码相关的提示消息
     */
    interface VerifyCode {
        // ========== 图形验证码 ==========
        /** 图形验证码不能为空 */
        String CAPTCHA_REQUIRED = "verify.code.captcha.required";
        /** 图形验证码错误 */
        String CAPTCHA_INVALID = "verify.code.captcha.invalid";
        /** 图形验证码已失效 */
        String CAPTCHA_EXPIRED = "verify.code.captcha.expired";

        // ========== 短信验证码 ==========
        /** 短信验证码不能为空 */
        String SMS_REQUIRED = "verify.code.sms.required";
        /** 短信验证码输入错误{0}次 */
        String SMS_RETRY_COUNT = "verify.code.sms.retry.count";
        /** 短信验证码输入错误{0}次，帐户锁定{1}分钟 */
        String SMS_RETRY_LOCKED = "verify.code.sms.retry.locked";
        /** 短信验证码错误 */
        String SMS_INVALID = "verify.code.sms.invalid";
        /** 短信验证码已过期 */
        String SMS_EXPIRED = "verify.code.sms.expired";

        // ========== 邮箱验证码 ==========
        /** 邮箱验证码不能为空 */
        String EMAIL_REQUIRED = "verify.code.email.required";
        /** 邮箱验证码输入错误{0}次 */
        String EMAIL_RETRY_COUNT = "verify.code.email.retry.count";
        /** 邮箱验证码输入错误{0}次，帐户锁定{1}分钟 */
        String EMAIL_RETRY_LOCKED = "verify.code.email.retry.locked";
        /** 邮箱验证码错误 */
        String EMAIL_INVALID = "verify.code.email.invalid";
        /** 邮箱验证码已过期 */
        String EMAIL_EXPIRED = "verify.code.email.expired";
    }

    /**
     * 第三方登录相关消息
     * <p>
     * 包含小程序登录和社交平台登录相关的提示消息
     */
    interface SocialLogin {
        /** 第三方登录平台不能为空 */
        String SOURCE_REQUIRED = "social.login.source.required";
        /** 第三方登录授权码不能为空 */
        String AUTH_CODE_REQUIRED = "social.login.auth.code.required";
        /** 第三方登录状态参数不能为空 */
        String STATE_REQUIRED = "social.login.state.required";
    }

    /**
     * 文件上传相关消息
     * <p>
     * 包含文件上传过程中的各种验证和错误提示消息
     */
    interface FileUpload {
        /** 上传的文件大小超出限制的文件大小！<br/>允许的文件最大大小是：{0}MB！ */
        String SIZE_EXCEED_LIMIT = "file.upload.size.exceed.limit";
        /** 上传的文件名最长{0}个字符 */
        String FILENAME_TOO_LONG = "file.upload.filename.too.long";
        /** 文件类型不支持 */
        String TYPE_NOT_SUPPORTED = "file.upload.type.not.supported";
        /** 文件上传失败 */
        String FAILED = "file.upload.failed";
    }

    /**
     * 权限控制相关消息
     * <p>
     * 包含各种操作权限不足的提示消息
     */
    interface Permission {
        /** 没有访问权限，请联系管理员添加权限 {0} [{1}] */
        String NO_ACCESS = "permission.no.access";
        /** 您没有创建数据的权限，请联系管理员添加权限 [{0}] */
        String NO_CREATE = "permission.no.create";
        /** 您没有修改数据的权限，请联系管理员添加权限 [{0}] */
        String NO_UPDATE = "permission.no.update";
        /** 您没有删除数据的权限，请联系管理员添加权限 [{0}] */
        String NO_DELETE = "permission.no.delete";
        /** 您没有导出数据的权限，请联系管理员添加权限 [{0}] */
        String NO_EXPORT = "permission.no.export";
        /** 您没有查看数据的权限，请联系管理员添加权限 [{0}] */
        String NO_VIEW = "permission.no.view";
    }

    /**
     * 请求控制相关消息
     * <p>
     * 包含重复提交和访问频率限制相关的提示消息
     */
    interface Request {
        /** 不允许重复提交，请稍候再试 */
        String DUPLICATE_SUBMIT = "request.control.duplicate.submit";
        /** 访问过于频繁，请稍候再试 */
        String RATE_LIMIT_EXCEEDED = "request.control.rate.limit.exceeded";
    }

    /**
     * 租户管理相关消息
     * <p>
     * 包含多租户模式下租户相关的验证和状态提示消息
     */
    interface Tenant {
        /** 租户ID不能为空 */
        String ID_REQUIRED = "tenant.id.required";
        /** 套餐ID不能为空 */
        String PACKAGE_ID_REQUIRED = "tenant.package.id.required";
        /** 对不起, 您的租户不存在，请联系管理员 */
        String NOT_EXISTS = "tenant.not.exists";
        /** 对不起，您的租户已禁用，请联系管理员 */
        String DISABLED = "tenant.disabled";
        /** 对不起，您的租户已过期，请联系管理员 */
        String EXPIRED = "tenant.expired";
        /** 当前未开启租户模式 */
        String MODE_NOT_ENABLED = "tenant.mode.not.enabled";
        /** 同步租户套餐成功 */
        String SYNC_PACKAGE_SUCCESS = "tenant.sync.package.success";
        /** 同步租户角色成功 */
        String SYNC_ROLES_SUCCESS = "tenant.sync.roles.success";
        /** 同步租户字典成功 */
        String SYNC_DICTS_SUCCESS = "tenant.sync.dicts.success";
    }
}
