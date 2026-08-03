<!-- 个人信息 -->
<template>
  <el-form ref="userFormRef" :model="form" :rules="rules" :label-width="isChinese ? '80px' : '100px'">
    <el-row>
      <AFormInput label="用户昵称" v-model="form.nickName" prop="nickName" :maxlength="30" :span="24"></AFormInput>
      <AFormInput label="手机号码" v-model="form.phone" prop="phone" :maxlength="11" :span="24"></AFormInput>
      <AFormInput label="邮箱" v-model="form.email" prop="email" :maxlength="50" :span="24"></AFormInput>
      <AFormRadio label="性别" v-model="form.gender" prop="gender" :options="sys_user_gender" :span="24"></AFormRadio>
    </el-row>
    <el-form-item>
      <el-button :loading="buttonLoading" type="primary" @click="submitForm">{{ t('button.save') }}</el-button>
      <el-button @click="handleClose">{{ t('button.close') }}</el-button>
    </el-form-item>
  </el-form>
</template>

<script setup lang="ts" name="UserInfo">
import { updateUserProfile } from '@/api/system/core/user/userApi'
import type { SysUserBo } from '@/api/system/core/user/userTypes'
import { closePage } from '@/utils/tab'
import { toValidate } from '@/utils/to'
import { showMsgSuccess, showConfirm } from '@/utils/modal'
const { t, isChinese } = useI18n()
const userStore = useUserStore()

// 字典数据
const { sys_user_gender } = useDict(DictTypes.sys_user_gender)

// =========== Props 定义 ===========
/**
 * 用户资料表单组件Props接口
 */
interface UserProfileFormProps {
  /**
   * 用户信息对象
   */
  user: SysUserBo
}

/**定义 props*/
const props = defineProps<UserProfileFormProps>()

// =========== 表单相关 ===========
/**表单引用*/
const userFormRef = ref<ElFormInstance>()
/**表单提交按钮加载状态*/
const buttonLoading = ref(false)

/**初始表单数据*/
const initFormData: SysUserBo = {
  userId: undefined,
  deptId: undefined,
  userName: '',
  nickName: '',
  password: '',
  phone: '',
  email: '',
  gender: '',
  status: '1',
  remark: '',
  postIds: [],
  roleIds: []
}

/**表单数据对象*/
const form = reactive<SysUserBo>({ ...initFormData })

/**表单校验规则*/
const rules = computed<ElFormRules>(() => ({
  nickName: [{ required: true, message: t('Nickname required', '用户昵称不能为空'), trigger: 'blur' }],
  email: [
    { required: true, message: t('Email required', '邮箱地址不能为空'), trigger: 'blur' },
    { type: 'email', message: t('Invalid email', '请输入正确的邮箱地址'), trigger: ['blur', 'change'] }
  ],
  phone: [
    { required: true, message: t('Phone required', '手机号码不能为空'), trigger: 'blur' },
    { pattern: /^1[3|4|5|6|7|8|9][0-9]\d{8}$/, message: t('Invalid phone', '请输入正确的手机号码'), trigger: 'blur' }
  ]
}))

// =========== 数据同步 ===========
/**监听props变化，同步表单数据*/
watch(
  () => props.user,
  (newUser) => {
    if (newUser) {
      Object.assign(form, newUser)
    }
  },
  { immediate: true, deep: true }
)

// =========== 表单操作 ===========
/**表单重置*/
const reset = () => {
  Object.assign(form, { ...initFormData })
  userFormRef.value?.resetFields()
}

/**提交表单*/
const submitForm = async () => {
  const [validateErr] = await toValidate(userFormRef)
  if (validateErr) return

  buttonLoading.value = true
  const [err] = await updateUserProfile(form)
  if (!err) {
    showMsgSuccess(t('message.updateSuccess'))
    // 更新成功后重新获取用户信息,同步到右上角显示
    await userStore.fetchUserInfo()
  }
  buttonLoading.value = false
}

/**关闭页面*/
const handleClose = () => {
  closePage()
}
</script>
