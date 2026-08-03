---
name: i18n-development
description: |
  国际化开发技能，涵盖后端 MessageSource、前端 Vue i18n、移动端 useI18n 完整实现。

  触发场景：
  - 后端国际化配置（MessageSource、property files）
  - PC 端多语言切换（Vue i18n）
  - 移动端国际化（UniApp i18n）
  - 动态语言切换
  - 语言包管理
  - 多语言字段翻译

  触发词：国际化、多语言、i18n、翻译、t()、语言切换、MessageUtils、content-language、LanguageCode、useI18n、messages.properties、locale、$t、zh_CN、en_US
---

# 国际化开发规范

> **核心原则**：三端统一的国际化实现，后端用 MessageSource，前端用 Vue i18n，移动端用增强 Composable！

## 框架对应模块

| 层级 | 模块路径 | 核心文件 |
|------|---------|---------|
| **后端** | `ruoyi-common-web` | `I18nConfiguration.java`<br>`I18nLocaleResolver.java` |
| **后端工具** | `ruoyi-common-core` | `MessageUtils.java`<br>`I18nKeys.java` |
| **后端资源** | `ruoyi-admin/src/main/resources/i18n/` | `messages*.properties` |
| **PC 前端** | `plus-ui/src/locales/` | `i18n.ts`<br>`zh_CN.ts`<br>`en_US.ts` |
| **移动端** | `plus-uniapp/src/composables/` | `useI18n.ts` |
| **移动端资源** | `plus-uniapp/src/locales/` | `zh_CN.ts`<br>`en_US.ts` |

---

## 1. 后端国际化（Spring Boot MessageSource）

### 1.1 配置文件结构

**资源文件位置：** `ruoyi-admin/src/main/resources/i18n/`

```
i18n/
├── messages.properties           # 默认语言（通常是英文）
├── messages_zh_CN.properties     # 简体中文
└── messages_en_US.properties     # 美式英语
```

**messages_zh_CN.properties 示例：**

```properties
############################################################
# 系统消息配置文件
############################################################

################## 通用验证消息 ##################
common.required=* 必须填写
common.length.invalid=长度必须在{min}到{max}个字符之间
common.id.required=主键ID不能为空

################## 通用操作结果消息 ##################
operation.success=操作成功
operation.fail=操作失败
operation.add.success=新增成功
operation.update.success=修改成功
operation.delete.success=删除成功

################## 用户认证相关消息 ##################
user.account.not.exists=对不起, 您的账号：{0} 不存在
user.password.mismatch=用户不存在/密码错误
user.login.success=登录成功
user.logout.success=退出成功

################## 验证码相关消息 ##################
verify.code.captcha.required=图形验证码不能为空
verify.code.captcha.invalid=图形验证码错误

################## 权限控制消息 ##################
permission.no.access=没有访问权限，请联系管理员添加权限 {0} [{1}]

################## 租户相关消息 ##################
tenant.id.required=租户ID不能为空
tenant.not.exists=对不起, 您的租户不存在，请联系管理员
```

### 1.2 类型安全的消息键（I18nKeys）

**位置：** `ruoyi-common-core/src/main/java/plus/ruoyi/common/core/constant/I18nKeys.java`

```java
/**
 * 国际化消息键常量
 * 统一管理系统中所有国际化消息的键常量，按功能模块分类组织
 * 使用方式：I18nKeys.分类.具体消息键
 */
public interface I18nKeys {

    /** 通用验证消息 */
    interface Common {
        String REQUIRED = "common.required";
        String LENGTH_INVALID = "common.length.invalid";
        String ID_REQUIRED = "common.id.required";
        String NUMBER_INVALID = "common.number.invalid";
        String EMAIL_INVALID = "common.email.invalid";
        String PHONE_INVALID = "common.phone.invalid";
    }

    /** 通用操作结果消息 */
    interface Oper {
        String SUCCESS = "operation.success";
        String FAIL = "operation.fail";
        String ADD_SUCCESS = "operation.add.success";
        String UPDATE_SUCCESS = "operation.update.success";
        String DELETE_SUCCESS = "operation.delete.success";
    }

    /** 用户相关消息 */
    interface User {
        String ACCOUNT_NOT_EXISTS = "user.account.not.exists";
        String PASSWORD_MISMATCH = "user.password.mismatch";
        String LOGIN_SUCCESS = "user.login.success";
        String LOGOUT_SUCCESS = "user.logout.success";
    }

    /** 认证相关消息 */
    interface Auth {
        String LOGIN_REQUIRED = "auth.login.required";
        String TOKEN_INVALID = "auth.token.invalid";
        String TOKEN_EXPIRED = "auth.token.expired";
    }
}
```

**使用优势：**
- ✅ 编译时检查，防止键名拼写错误
- ✅ IDE 自动补全
- ✅ 重构友好（重命名时自动更新）
- ✅ 按模块组织，结构清晰

### 1.3 MessageUtils 工具类

**位置：** `ruoyi-common-core/src/main/java/plus/ruoyi/common/core/utils/MessageUtils.java`

```java
/**
 * 国际化消息工具类
 * 提供便捷的方法来获取i18n资源文件中的消息内容
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MessageUtils {

    private static final MessageSource MESSAGE_SOURCE = SpringUtils.getBean(MessageSource.class);

    /**
     * 根据消息键和参数获取国际化消息
     * @param code 消息键，对应资源文件中的key
     * @param args 消息参数，用于替换消息模板中的占位符
     * @return 国际化翻译后的消息内容
     */
    public static String message(String code, Object... args) {
        try {
            return MESSAGE_SOURCE.getMessage(code, args, LocaleContextHolder.getLocale());
        } catch (NoSuchMessageException e) {
            return code;
        }
    }
}
```

**使用示例：**

```java
// 1. 简单消息
String msg = MessageUtils.message(I18nKeys.Oper.SUCCESS);
// 中文: "操作成功"
// 英文: "Operation successful"

// 2. 带参数的消息
String msg = MessageUtils.message(I18nKeys.User.ACCOUNT_NOT_EXISTS, "admin");
// 中文: "对不起, 您的账号：admin 不存在"
// 英文: "Sorry, your account: admin does not exist"

// 3. ServiceException 中使用
throw ServiceException.of(MessageUtils.message(I18nKeys.User.PASSWORD_MISMATCH));

// 4. 控制器返回消息
return R.ok(MessageUtils.message(I18nKeys.Oper.ADD_SUCCESS));
```

### 1.4 自定义 LocaleResolver

**位置：** `ruoyi-common-web/src/main/java/plus/ruoyi/common/web/core/I18nLocaleResolver.java`

```java
/**
 * 国际化语言环境解析器
 * 从HTTP请求头中获取语言信息来确定用户的语言环境
 * 支持通过content-language请求头进行语言切换
 */
public class I18nLocaleResolver implements LocaleResolver {

    /**
     * 解析请求中的语言环境
     * 从请求头的content-language字段中解析语言信息
     * 支持格式：语言_国家（如：zh_CN、en_US）
     */
    @Override
    public Locale resolveLocale(HttpServletRequest httpServletRequest) {
        String language = httpServletRequest.getHeader("content-language");
        Locale locale = Locale.getDefault();
        if (language != null && !language.isEmpty()) {
            String[] split = language.split("_");
            if (split.length == 2) {
                locale = new Locale(split[0], split[1]);
            }
        }
        return locale;
    }

    @Override
    public void setLocale(HttpServletRequest httpServletRequest,
                          HttpServletResponse httpServletResponse,
                          Locale locale) {
        // 空实现，不支持设置语言环境
    }
}
```

**HTTP 请求头格式：**

```http
GET /api/user/login HTTP/1.1
Host: localhost:8080
Content-Language: zh_CN
```

**支持的语言代码：**
- `zh_CN`: 简体中文
- `en_US`: 美式英语
- `zh_TW`: 繁体中文（需添加 messages_zh_TW.properties）
- `ja_JP`: 日语（需添加 messages_ja_JP.properties）

---

## 2. PC 前端国际化（Vue i18n）

### 2.1 i18n 配置

**位置：** `plus-ui/src/locales/i18n.ts`

```typescript
import { createI18n } from 'vue-i18n'
import zh_CN from '@/locales/zh_CN'
import en_US from '@/locales/en_US'
import el_en from 'element-plus/es/locale/lang/en'
import el_zhCn from 'element-plus/es/locale/lang/zh-cn'
import { LanguageCode } from '@/systemConfig'

/**
 * 获取当前语言
 * @description 从本地存储中获取用户设置的语言，如果没有则使用默认语言（中文）
 */
export const getLanguage = (): LanguageCode => {
  const layout = useLayout()
  if (layout.language.value) {
    return layout.language.value
  }
  return LanguageCode.zh_CN
}

/**
 * 创建 i18n 实例
 * @description 配置国际化实例，设置默认语言和翻译消息
 */
const i18n = createI18n({
  globalInjection: true, // 全局注入 $t, $d 等方法到模板中
  allowComposition: true, // 允许组合式 API
  legacy: false, // 使用 Vue 3 Composition API 模式
  locale: getLanguage(), // 设置当前语言
  messages: {
    zh_CN: {
      ...zh_CN,
      // ⚠️ 只合并 el 命名空间，避免 name 键冲突
      el: el_zhCn.el
    },
    en_US: {
      ...en_US,
      // ⚠️ 只合并 el 命名空间，避免 name 键冲突
      el: el_en.el
    }
  }
})

export default i18n

// 导出语言包类型，用于类型检查和自动补全
export type LanguageType = typeof zh_CN
```

**⚠️ Element Plus 集成注意事项：**
- 只合并 `el` 命名空间：`el: el_zhCn.el`
- 不要直接展开：`...el_zhCn`（会导致 `name` 键冲突）

### 2.2 语言文件结构

**位置：** `plus-ui/src/locales/zh_CN.ts`

```typescript
export default {
  // 扁平结构：中文键 → 中文值
  '操作': '操作',
  '新增': '新增',
  '修改': '修改',
  '删除': '删除',
  '确定': '确定',
  '取消': '取消',

  // 嵌套结构：命名空间组织
  /** 按钮权限系统 */
  button: {
    query: '查询',
    add: '新增',
    update: '修改',
    delete: '删除',
    export: '导出',
    import: '导入',
    save: '保存',
    cancel: '取消',
    confirm: '确定'
  },

  /** 弹窗提示 */
  dialog: {
    add: '新增',
    edit: '修改',
    delete: '删除',
    query: '查询',
    detail: '详情'
  },

  /** 消息提示 */
  message: {
    operation: '操作',
    confirmDelete: '是否确认删除下列数据:',
    success: '操作成功',
    error: '操作失败',
    addSuccess: '新增成功',
    updateSuccess: '修改成功',
    deleteSuccess: '删除成功'
  },

  /** 表单验证消息 */
  validation: {
    parentIdRequired: '上级部门不能为空',
    deptNameRequired: '部门名称不能为空',
    emailInvalid: '请输入正确的邮箱地址',
    phoneInvalid: '请输入正确的手机号码'
  },

  /** 菜单国际化 */
  menu: {
    home: '主页',
    system: {
      _self: '系统管理',
      user: '用户管理',
      role: '角色管理',
      menu: '菜单管理'
    }
  }
}
```

**en_US.ts 结构：** 完全镜像 zh_CN.ts 结构

```typescript
export default {
  '操作': 'Operation',
  '新增': 'Add ',
  '修改': 'Edit ',
  '删除': 'Delete',
  '确定': 'Confirm',
  '取消': 'Cancel',

  button: {
    query: 'Query',
    add: 'Add',
    update: 'Edit',
    delete: 'Delete',
    export: 'Export',
    import: 'Import',
    save: 'Save',
    cancel: 'Cancel',
    confirm: 'Confirm'
  },

  dialog: {
    add: 'Add ',
    edit: 'Edit ',
    delete: 'Delete',
    query: 'Query',
    detail: 'Details'
  },

  message: {
    operation: 'Operation',
    confirmDelete: 'Confirm delete the following:',
    success: 'Operation successful',
    error: 'Operation failed',
    addSuccess: 'Added successfully',
    updateSuccess: 'Update successful',
    deleteSuccess: 'Delete successful'
  }
}
```

### 2.3 组件中使用

#### 模板中使用（$t）

```vue
<template>
  <!-- 1. 简单翻译 -->
  <el-button>{{ $t('button.add') }}</el-button>
  <!-- 输出：新增 / Add -->

  <!-- 2. 嵌套键 -->
  <span>{{ $t('menu.system._self') }}</span>
  <!-- 输出：系统管理 / System -->

  <!-- 3. 动态参数 -->
  <p>{{ $t('message.confirmDelete') }}</p>
  <!-- 输出：是否确认删除下列数据: / Confirm delete the following: -->
</template>
```

#### Script 中使用（Composition API）

```typescript
<script setup lang="ts">
import { useI18n } from 'vue-i18n'

const { t } = useI18n()

// 1. 基本使用
const addBtnText = t('button.add')  // "新增" / "Add"

// 2. 消息提示
import modal from '@/utils/modal'
modal.msgSuccess(t('message.addSuccess'))

// 3. 表单验证
const rules = {
  deptName: [
    { required: true, message: t('validation.deptNameRequired'), trigger: 'blur' }
  ]
}
</script>
```

### 2.4 语言切换

```typescript
import { useI18n } from 'vue-i18n'
import { LanguageCode } from '@/systemConfig'

const { locale } = useI18n()

// 切换到中文
locale.value = LanguageCode.zh_CN

// 切换到英文
locale.value = LanguageCode.en_US
```

---

## 3. 移动端国际化（UniApp + useI18n Composable）

### 3.1 useI18n Composable

**位置：** `plus-uniapp/src/composables/useI18n.ts`

**核心特性：**
- 支持 4 种翻译模式
- 响应式状态管理
- 智能降级策略
- wot-ui 兼容

**接口定义：**

```typescript
interface UseI18nReturn {
  // 翻译函数
  t: (
    key: string,
    fieldInfoOrValue?: string | {
      field?: string
      comment?: string
      [LanguageCode.zh_CN]?: string
      [LanguageCode.en_US]?: string
      [key: string]: any
    }
  ) => string

  // 检查翻译键是否存在
  te: (key: string) => boolean

  // 响应式状态
  currentLanguage: ComputedRef<LanguageCode>
  currentLanguageName: ComputedRef<string>
  languageOptions: ComputedRef<Array<{
    value: LanguageCode
    label: string
    name: string
  }>>
  isChinese: ComputedRef<boolean>
  isEnglish: ComputedRef<boolean>

  // 方法
  setLanguage: (lang: LanguageCode) => boolean
  translateRouteTitle: (title: string) => string
  translate: (key: string, ...args: unknown[]) => string  // wot-ui 兼容
}
```

### 3.2 四种翻译模式

#### 模式 1：简单用法（推荐用于字段标签）

```typescript
const { t } = useI18n()

// 中文环境: 返回第二个参数（中文）
// 英文环境: 返回第一个参数（英文键）
t('userName', '用户名')
// zh_CN: "用户名"
// en_US: "userName"

t('userPhone', '手机号')
// zh_CN: "手机号"
// en_US: "userPhone"
```

**适用场景：** 表单字段标签、列表列名

#### 模式 2：字段信息（推荐用于数据库字段）

```typescript
// 使用数据库注释
t('', {
  field: 'UserName',      // 英文字段名
  comment: '用户名'        // 中文注释
})
// zh_CN: "用户名"（优先 comment）
// en_US: "UserName"（优先 field）

t('', {
  field: 'PhoneNumber',
  comment: '手机号码'
})
// zh_CN: "手机号码"
// en_US: "PhoneNumber"
```

**适用场景：** 根据 Entity 字段生成表单

#### 模式 3：显式语言映射（推荐用于复杂翻译）

```typescript
t('', {
  [LanguageCode.zh_CN]: '提交订单',
  [LanguageCode.en_US]: 'Submit Order'
})
// zh_CN: "提交订单"
// en_US: "Submit Order"

t('', {
  [LanguageCode.zh_CN]: '支付成功',
  [LanguageCode.en_US]: 'Payment Successful',
  amount: 100  // 额外参数
})
```

**适用场景：** 业务逻辑消息、复杂提示

#### 模式 4：传统 i18n 键（标准用法）

```typescript
// 与 Vue i18n 完全兼容
t('button.add')        // "新增" / "Add"
t('message.success')   // "操作成功" / "Operation successful"

// 支持占位符替换
t('user.welcome', { name: '张三' })
// 配置: "欢迎 {name}" / "Welcome {name}"
// 输出: "欢迎 张三" / "Welcome 张三"
```

**适用场景：** 按钮文本、系统消息

### 3.3 移动端使用示例

#### 页面中使用

```vue
<!-- 移动端国际化表单页面示例 -->
<template>
  <view>
    <!-- 模式1: 简单字段标签 -->
    <wd-input :label="t('userName', '用户名')" v-model="form.userName" />
    <wd-input :label="t('userPhone', '手机号')" v-model="form.phone" />

    <!-- 模式4: 按钮文本 -->
    <wd-button @click="handleSubmit">{{ t('button.submit') }}</wd-button>
  </view>
</template>

<script setup lang="ts">
import { useI18n, useToast } from '@/wd'

const { t } = useI18n()
const toast = useToast()

const handleSubmit = async () => {
  const [err] = await submitForm(form)
  if (!err) {
    // 模式4: 系统消息
    toast.success(t('message.addSuccess'))
  }
}
</script>
```

#### WD 组件选项翻译

```typescript
// 下拉选项国际化
const statusOptions = [
  {
    label: t('', {
      [LanguageCode.zh_CN]: '启用',
      [LanguageCode.en_US]: 'Enabled'
    }),
    value: '1'
  },
  {
    label: t('', {
      [LanguageCode.zh_CN]: '停用',
      [LanguageCode.en_US]: 'Disabled'
    }),
    value: '0'
  }
]
```

### 3.4 语言切换

```typescript
const { setLanguage, currentLanguage, isChinese } = useI18n()

// 切换到英文
setLanguage(LanguageCode.en_US)

// 切换到中文
setLanguage(LanguageCode.zh_CN)

// 获取当前语言
console.log(currentLanguage.value)  // "zh_CN" or "en_US"

// 判断是否中文
if (isChinese.value) {
  console.log('当前是中文环境')
}
```

---

## 4. 完整使用示例

### 4.1 后端 Service 层

```java
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements IUserService {

    @Override
    public void login(LoginBo loginBo) {
        // 验证用户存在
        SysUser user = findByUserName(loginBo.getUserName());
        if (user == null) {
            throw ServiceException.of(
                MessageUtils.message(I18nKeys.User.ACCOUNT_NOT_EXISTS, loginBo.getUserName())
            );
        }

        // 验证密码
        if (!passwordMatches(loginBo.getPassword(), user.getPassword())) {
            throw ServiceException.of(
                MessageUtils.message(I18nKeys.User.PASSWORD_MISMATCH)
            );
        }

        // 登录成功
        log.info(MessageUtils.message(I18nKeys.User.LOGIN_SUCCESS));
    }
}
```

### 4.2 后端 Controller 层

```java
@RestController
@RequestMapping("/system/user")
@RequiredArgsConstructor
public class SysUserController {

    private final IUserService userService;

    @PostMapping("/addUser")
    public R<Void> add(@Validated @RequestBody UserBo userBo) {
        userService.add(userBo);
        return R.ok(MessageUtils.message(I18nKeys.Oper.ADD_SUCCESS));
    }

    @PutMapping("/updateUser")
    public R<Void> update(@Validated @RequestBody UserBo userBo) {
        userService.update(userBo);
        return R.ok(MessageUtils.message(I18nKeys.Oper.UPDATE_SUCCESS));
    }
}
```

### 4.3 PC 前端页面

```vue
<!-- PC端用户管理国际化页面示例 -->
<template>
  <div>
    <!-- 搜索表单 -->
    <ASearchForm>
      <AFormInput v-model="queryParams.userName" :label="t('user.userName')" />
      <AFormSelect v-model="queryParams.status" :label="t('status')" :options="statusOptions" />
    </ASearchForm>

    <!-- 操作按钮 -->
    <el-button type="primary" @click="handleAdd">{{ t('button.add') }}</el-button>
    <el-button @click="handleExport">{{ t('button.export') }}</el-button>

    <!-- 表格 -->
    <el-table :data="dataList">
      <el-table-column prop="userName" :label="t('user.userName')" />
      <el-table-column prop="phone" :label="t('phone')" />
      <el-table-column :label="t('button.operation')">
        <template #default="scope">
          <el-button link @click="handleUpdate(scope.row)">{{ t('button.update') }}</el-button>
          <el-button link @click="handleDelete(scope.row)">{{ t('button.delete') }}</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 表单弹窗 -->
    <AModal v-model="visible" :title="modalTitle">
      <AForm ref="formRef" :model="form" :rules="rules">
        <AFormInput v-model="form.userName" :label="t('user.userName')" prop="userName" />
        <AFormInput v-model="form.phone" :label="t('phone')" prop="phone" />
      </AForm>
    </AModal>
  </div>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import modal from '@/utils/modal'

const { t } = useI18n()

// 状态选项国际化
const statusOptions = computed(() => [
  { label: t('common.enable'), value: '1' },
  { label: t('common.disable'), value: '0' }
])

// 表单验证规则
const rules = {
  userName: [
    { required: true, message: t('validation.userNameRequired'), trigger: 'blur' }
  ],
  phone: [
    { required: true, message: t('validation.phoneRequired'), trigger: 'blur' },
    { pattern: /^1\d{10}$/, message: t('validation.phoneInvalid'), trigger: 'blur' }
  ]
}

// 弹窗标题
const modalTitle = computed(() =>
  form.value.id ? t('dialog.edit') : t('dialog.add')
)

// 提交成功
const handleSubmit = async () => {
  const [err] = await addUser(form.value)
  if (!err) {
    modal.msgSuccess(t('message.addSuccess'))
  }
}
</script>
```

### 4.4 移动端页面

```vue
<!-- 移动端用户表单国际化页面示例 -->
<template>
  <view>
    <!-- 表单 -->
    <wd-form :model="form">
      <!-- 模式1: 简单字段标签 -->
      <wd-input :label="t('userName', '用户名')" v-model="form.userName" required />
      <wd-input :label="t('userPhone', '手机号')" v-model="form.phone" />

      <!-- 模式4: 标准按钮文本 -->
      <wd-button type="primary" @click="handleSubmit">
        {{ t('button.submit') }}
      </wd-button>
    </wd-form>
  </view>
</template>

<script setup lang="ts">
import { useI18n, useToast } from '@/wd'

const { t, isChinese } = useI18n()
const toast = useToast()

// 模式3: 显式语言映射（业务消息）
const confirmMessage = t('', {
  [LanguageCode.zh_CN]: '确认提交此表单？',
  [LanguageCode.en_US]: 'Confirm to submit this form?'
})

const handleSubmit = async () => {
  const [err] = await submitUserForm(form)
  if (!err) {
    // 模式4: 系统消息
    toast.success(t('message.addSuccess'))
  }
}
</script>
```

---

## 5. 新增语言支持

### 5.1 后端添加新语言

**步骤 1：** 添加资源文件

```bash
# 在 ruoyi-admin/src/main/resources/i18n/ 目录下
cp messages_zh_CN.properties messages_ja_JP.properties
# 然后翻译为日语
```

**步骤 2：** 无需修改代码

Spring Boot 的 `ResourceBundleMessageSource` 会自动加载 `messages_*.properties` 文件。

**步骤 3：** 客户端发送对应的 `content-language` 头

```http
Content-Language: ja_JP
```

### 5.2 PC 前端添加新语言

**步骤 1：** 创建语言文件

```typescript
// plus-ui/src/locales/ja_JP.ts
export default {
  '操作': '操作',
  '新增': '追加',
  '修改': '編集',
  // ...
  button: {
    query: 'クエリ',
    add: '追加',
    update: '編集'
  }
}
```

**步骤 2：** 注册到 i18n

```typescript
// plus-ui/src/locales/i18n.ts
import ja_JP from '@/locales/ja_JP'
import el_ja from 'element-plus/es/locale/lang/ja'

const i18n = createI18n({
  // ...
  messages: {
    zh_CN: { ...zh_CN, el: el_zhCn.el },
    en_US: { ...en_US, el: el_en.el },
    ja_JP: { ...ja_JP, el: el_ja.el }  // 新增
  }
})
```

**步骤 3：** 在 systemConfig 中添加

```typescript
// plus-ui/src/systemConfig.ts
export enum LanguageCode {
  zh_CN = 'zh_CN',
  en_US = 'en_US',
  ja_JP = 'ja_JP'  // 新增
}
```

### 5.3 移动端添加新语言

**步骤 1：** 创建语言文件

```typescript
// plus-uniapp/src/locales/ja_JP.ts
export default {
  button: {
    submit: '提出',
    cancel: 'キャンセル'
  },
  message: {
    success: '操作が成功しました'
  }
}
```

**步骤 2：** 注册到系统配置

```typescript
// plus-uniapp/src/systemConfig.ts
export enum LanguageCode {
  zh_CN = 'zh_CN',
  en_US = 'en_US',
  ja_JP = 'ja_JP'  // 新增
}

export const LANGUAGE_OPTIONS = [
  { value: LanguageCode.zh_CN, label: '简体中文', name: '中文' },
  { value: LanguageCode.en_US, label: 'English', name: 'English' },
  { value: LanguageCode.ja_JP, label: '日本語', name: '日语' }  // 新增
]
```

**步骤 3：** 导入语言包（如果需要在 setup 中使用）

```typescript
// plus-uniapp/src/composables/useI18n.ts
import zh_CN from '@/locales/zh_CN'
import en_US from '@/locales/en_US'
import ja_JP from '@/locales/ja_JP'  // 新增

const languagePackages: Record<LanguageCode, any> = {
  [LanguageCode.zh_CN]: zh_CN,
  [LanguageCode.en_US]: en_US,
  [LanguageCode.ja_JP]: ja_JP  // 新增
}
```

---

## 6. 开发检查清单

### 后端 i18n 检查

- [ ] **消息键定义在 properties 文件中**（`messages_zh_CN.properties`）
- [ ] **类型安全常量添加到 I18nKeys.java**（编译时检查）
- [ ] **使用 MessageUtils.message()**（而非直接使用 MessageSource）
- [ ] **消息键按模块分类**（Common/Oper/User/Auth）
- [ ] **占位符格式正确**（`{0}`, `{1}` 或 `{name}`）
- [ ] **异常消息国际化**（`ServiceException.of(MessageUtils.message(...))`）
- [ ] **英文 messages_en_US.properties 同步更新**

### PC 前端 i18n 检查

- [ ] **中英文语言文件结构一致**（zh_CN.ts 和 en_US.ts）
- [ ] **使用命名空间组织**（button/dialog/message）
- [ ] **Element Plus 只合并 el 命名空间**（避免 name 键冲突）
- [ ] **类型导出**（`export type LanguageType = typeof zh_CN`）
- [ ] **组件中使用 t() 方法**（不硬编码中文）
- [ ] **表单验证消息国际化**
- [ ] **弹窗标题国际化**（computed 根据编辑/新增切换）

### 移动端 i18n 检查

- [ ] **使用 useI18n composable**（从 `@/wd` 导入）
- [ ] **字段标签使用模式1**（`t('userName', '用户名')`）
- [ ] **业务消息使用模式3**（显式语言映射）
- [ ] **系统消息使用模式4**（标准 i18n 键）
- [ ] **Toast/Message 消息国际化**
- [ ] **WD 组件选项国际化**

---

## 7. 常见错误

| 错误写法 | 正确写法 | 原因 |
|---------|---------|------|
| **后端** |||
| `throw new RuntimeException("用户不存在")` | `throw ServiceException.of(MessageUtils.message(I18nKeys.User.ACCOUNT_NOT_EXISTS))` | 硬编码中文 |
| `return R.ok("操作成功")` | `return R.ok(MessageUtils.message(I18nKeys.Oper.SUCCESS))` | 硬编码中文 |
| 直接使用字符串键：`MessageUtils.message("user.login.success")` | `MessageUtils.message(I18nKeys.User.LOGIN_SUCCESS)` | 缺少类型安全 |
| **PC 前端** |||
| `...el_zhCn` | `el: el_zhCn.el` | 会导致 name 键冲突 |
| `<el-button>新增</el-button>` | `<el-button>{{ t('button.add') }}</el-button>` | 硬编码中文 |
| `legacy: true` | `legacy: false` | Vue 3 应使用 Composition API 模式 |
| **移动端** |||
| `<wd-input label="用户名" />` | `<wd-input :label="t('userName', '用户名')" />` | 硬编码中文 |
| `import { useI18n } from 'vue-i18n'` | `import { useI18n } from '@/wd'` | 移动端用项目封装 |
| `toast.success('操作成功')` | `toast.success(t('message.success'))` | 硬编码中文 |

---

## 8. 最佳实践

### 后端最佳实践

1. **消息键命名规范**
   ```properties
   # 格式：模块.子模块.具体消息
   common.required=* 必须填写
   user.login.success=登录成功
   operation.add.success=新增成功
   ```

2. **占位符使用**
   ```properties
   # 位置占位符（按顺序）
   user.account.not.exists=对不起, 您的账号：{0} 不存在

   # 命名占位符（推荐）
   file.upload.size.exceed=文件大小超出限制！允许的最大大小是：{maxSize}MB
   ```

3. **类型安全常量同步**
   ```java
   // 每次添加新消息键时，同步更新 I18nKeys.java
   interface User {
       String NEW_MESSAGE = "user.new.message";  // 新增
   }
   ```

### PC 前端最佳实践

1. **结构一致性**
   ```typescript
   // zh_CN.ts 和 en_US.ts 必须结构完全一致
   export default {
     button: {
       add: '新增'  // zh_CN
       add: 'Add'   // en_US
     }
   }
   ```

2. **命名空间组织**
   ```typescript
   // 按功能模块分类
   button: { /* 按钮文本 */ },
   dialog: { /* 弹窗标题 */ },
   message: { /* 消息提示 */ },
   validation: { /* 表单验证 */ },
   menu: { /* 菜单路由 */ }
   ```

3. **动态消息**
   ```typescript
   // 支持参数插值
   const msg = t('message.welcome', { name: userName })
   // 配置: "欢迎 {name}"
   // 输出: "欢迎 张三"
   ```

### 移动端最佳实践

1. **选择合适的翻译模式**
   ```typescript
   // 字段标签 → 模式1（简单）
   t('userName', '用户名')

   // 数据库字段 → 模式2（字段信息）
   t('', { field: 'UserName', comment: '用户名' })

   // 业务消息 → 模式3（显式映射）
   t('', { zh_CN: '支付成功', en_US: 'Payment Successful' })

   // 系统按钮 → 模式4（标准键）
   t('button.submit')
   ```

2. **响应式状态使用**
   ```typescript
   const { isChinese, isEnglish, currentLanguage } = useI18n()

   // 条件渲染
   <view v-if="isChinese">中文专属内容</view>
   <view v-else>English exclusive content</view>
   ```

3. **wot-ui 组件兼容**
   ```typescript
   // WD UI 组件自动支持 translate() 方法
   const { translate } = useI18n()
   // WD 组件内部会调用 translate('xxx')
   ```

---

## 9. FAQ

### Q1: 后端如何切换语言？

**A:** 客户端在 HTTP 请求头中设置 `Content-Language`:

```http
Content-Language: zh_CN  # 中文
Content-Language: en_US  # 英文
```

后端 `I18nLocaleResolver` 会自动解析此头，并设置到 `LocaleContextHolder`。

### Q2: PC 前端为什么不能直接展开 Element Plus 语言包？

**A:** 会导致键名冲突：

```typescript
// ❌ 错误：name 键冲突
messages: {
  zh_CN: {
    ...zh_CN,
    ...el_zhCn  // el_zhCn 里有 name 属性
  }
}

// ✅ 正确：只合并 el 命名空间
messages: {
  zh_CN: {
    ...zh_CN,
    el: el_zhCn.el  // 避免冲突
  }
}
```

### Q3: 移动端的 4 种翻译模式如何选择？

**A:**

| 场景 | 模式 | 示例 |
|------|------|------|
| 表单字段标签 | 模式1（简单） | `t('userName', '用户名')` |
| 根据 Entity 生成 | 模式2（字段信息） | `t('', { field: 'UserName', comment: '用户名' })` |
| 业务逻辑消息 | 模式3（显式映射） | `t('', { zh_CN: '支付成功', en_US: 'Payment Success' })` |
| 系统按钮/通用消息 | 模式4（标准键） | `t('button.submit')` |

### Q4: 如何处理占位符参数？

**A:**

**后端：**
```java
// properties 文件
user.account.not.exists=账号 {0} 不存在

// Java 代码
MessageUtils.message(I18nKeys.User.ACCOUNT_NOT_EXISTS, "admin")
// 输出: "账号 admin 不存在"
```

**PC 前端：**
```typescript
// zh_CN.ts
export default {
  user: {
    welcome: '欢迎 {name}'
  }
}

// 使用
t('user.welcome', { name: '张三' })
// 输出: "欢迎 张三"
```

**移动端：**
```typescript
// 自动支持占位符
t('user.welcome', { name: '张三' })
// 内部会替换 {name} 为实际值
```

### Q5: 消息键找不到时会发生什么？

**A:** 优雅降级：

- **后端**: `MessageUtils.message()` 返回键名本身
  ```java
  MessageUtils.message("not.exist.key")  // 返回: "not.exist.key"
  ```

- **PC 前端**: `t()` 返回键名本身
  ```typescript
  t('not.exist.key')  // 返回: "not.exist.key"
  ```

- **移动端**: `useI18n` 会尝试多种降级策略，最终返回键名

### Q6: 如何测试国际化是否生效？

**A:**

**后端测试：**
```bash
# Postman/curl 测试
curl -H "Content-Language: en_US" http://localhost:8080/api/user/login
```

**PC 前端测试：**
```typescript
// 浏览器控制台
import { useI18n } from 'vue-i18n'
const { locale } = useI18n()
locale.value = 'en_US'  // 切换语言
```

**移动端测试：**
```typescript
// 页面代码
const { setLanguage } = useI18n()
setLanguage(LanguageCode.en_US)  // 切换语言
```

---

## 10. 参考代码位置

### 后端参考

```
ruoyi-common/ruoyi-common-core/src/main/java/plus/ruoyi/common/core/
├── constant/I18nKeys.java                    # 类型安全常量
└── utils/MessageUtils.java                   # 消息工具

ruoyi-common/ruoyi-common-web/src/main/java/plus/ruoyi/common/web/
├── config/I18nConfiguration.java             # 配置类
└── core/I18nLocaleResolver.java              # 自定义解析器

ruoyi-admin/src/main/resources/i18n/
├── messages.properties
├── messages_zh_CN.properties
└── messages_en_US.properties
```

### PC 前端参考

```
plus-ui/src/locales/
├── i18n.ts                                    # i18n 配置
├── zh_CN.ts                                   # 中文语言包
└── en_US.ts                                   # 英文语言包
```

### 移动端参考

```
plus-uniapp/src/
├── composables/useI18n.ts                     # i18n Composable
└── locales/
    ├── zh_CN.ts
    └── en_US.ts
```
