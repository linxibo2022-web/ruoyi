<!-- 修改密码 -->
<template>
  <el-form ref="pwdFormRef" :model="form" :rules="rules" :label-width="isChinese ? '80px' : '120px'">
    <el-row>
      <AFormInput
        type="password"
        show-password
        :label="t('Old Password', '旧密码')"
        v-model="form.oldPassword"
        prop="oldPassword"
        :placeholder="t('Enter old password', '请输入旧密码')"
        :span="24"
      ></AFormInput>
    </el-row>
    <el-row>
      <AFormInput
        type="password"
        show-password
        :label="t('New Password', '新密码')"
        v-model="form.newPassword"
        prop="newPassword"
        :placeholder="t('Enter new password', '请输入新密码')"
        :span="24"
      ></AFormInput>
    </el-row>
    <el-row>
      <AFormInput
        type="password"
        show-password
        :label="t('Confirm Password', '确认密码')"
        v-model="form.confirmPassword"
        prop="confirmPassword"
        :placeholder="t('Confirm new password', '请确认新密码')"
        :span="24"
      ></AFormInput>
    </el-row>
    <el-form-item>
      <el-button :loading="buttonLoading" type="primary" @click="submitForm">{{ t('button.save') }}</el-button>
      <el-button @click="handleClose">{{ t('button.close') }}</el-button>
    </el-form-item>
  </el-form>
</template>

<script setup lang="ts" name="ResetPwd">
import { updateUserPwd } from '@/api/system/core/user/userApi'
import type { SysUserPasswordBo } from '@/api/system/core/user/userTypes'
import { closePage } from '@/utils/tab'
import { toValidate } from '@/utils/to'
import { showMsgSuccess, showConfirm } from '@/utils/modal'
const { t, isChinese } = useI18n()

// =========== 表单相关 ===========
/**表单引用*/
const pwdFormRef = ref<ElFormInstance>()
/**表单提交按钮加载状态*/
const buttonLoading = ref(false)

/**初始表单数据*/
const initFormData: SysUserPasswordBo = {
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
}

/**表单数据对象*/
const form = ref<SysUserPasswordBo>({ ...initFormData })

/**密码确认验证器*/
const equalToPassword = (rule: any, value: string, callback: any) => {
  if (form.value.newPassword !== value) {
    callback(new Error(t('Password mismatch', '两次输入的密码不一致')))
  } else {
    callback()
  }
}

/**表单校验规则*/
const rules = computed<ElFormRules>(() => ({
  oldPassword: [{ required: true, message: t('Old password required', '旧密码不能为空'), trigger: 'blur' }],
  newPassword: [
    { required: true, message: t('New password required', '新密码不能为空'), trigger: 'blur' },
    { min: 6, max: 20, message: t('Password length 6-20', '长度在 6 到 20 个字符'), trigger: 'blur' },
    { pattern: /^[^<>"'|\\]+$/, message: t('Invalid characters', '不能包含非法字符：< > " \' \\ |'), trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: t('Confirm password required', '确认密码不能为空'), trigger: 'blur' },
    { required: true, validator: equalToPassword, trigger: 'blur' }
  ]
}))

// =========== 表单操作 ===========
/**表单重置*/
const reset = () => {
  form.value = { ...initFormData }
  pwdFormRef.value?.resetFields()
}

/**提交表单*/
const submitForm = async () => {
  const [validateErr] = await toValidate(pwdFormRef)
  if (validateErr) return

  buttonLoading.value = true
  const [err] = await updateUserPwd(form.value)
  if (!err) {
    showMsgSuccess(t('message.updateSuccess'))
    reset()
  }
  buttonLoading.value = false
}

/**关闭页面*/
const handleClose = () => {
  closePage()
}
</script>
