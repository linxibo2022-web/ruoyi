<!-- 表单组件测试 -->
<template>
  <div class="form-container">
    <!-- 表单示例头部 -->
    <div class="flex justify-between items-center">
      <h3>表单组件示例</h3>
    </div>

    <el-divider />

    <!-- 统一示例展示 -->
    <div class="examples-container">
      <!-- 1. 输入框组件 AFormInput -->
      <h2 class="section-title">1. 输入框组件 AFormInput</h2>
      <el-form ref="inputFormRef" :model="inputForm" label-width="100px">
        <div class="examples-grid">
          <div class="example-item">
            <div class="example-header">
              <span class="example-title">基本输入框</span>
              <div class="copy-btn" @click="copy(codeExamples.input1, '复制成功!')">
                <Icon code="copy" size="md" />
              </div>
            </div>
            <AFormInput label="用户名" v-model="inputForm.userName" prop="userName" />
          </div>

          <div class="example-item">
            <div class="example-header">
              <span class="example-title">密码输入框</span>
              <div class="copy-btn" @click="copy(codeExamples.input2, '复制成功!')">
                <Icon code="copy" size="md" />
              </div>
            </div>
            <AFormInput label="密码" v-model="inputForm.password" prop="password" type="password" show-password prevent-autofill />
          </div>

          <div class="example-item">
            <div class="example-header">
              <span class="example-title">数字输入框</span>
              <div class="copy-btn" @click="copy(codeExamples.input3, '复制成功!')">
                <Icon code="copy" size="md" />
              </div>
            </div>
            <AFormInput label="年龄" v-model="inputForm.age" prop="age" type="number" :min="1" :max="120" />
          </div>

          <div class="example-item">
            <div class="example-header">
              <span class="example-title">带提示信息</span>
              <div class="copy-btn" @click="copy(codeExamples.input4, '复制成功!')">
                <Icon code="copy" size="md" />
              </div>
            </div>
            <AFormInput label="邮箱" v-model="inputForm.email" prop="email" tooltip="请输入有效的邮箱地址" />
          </div>
        </div>

        <div class="examples-grid mt-4">
          <div class="example-item" style="grid-column: 1 / -1">
            <div class="example-header">
              <span class="example-title">多行文本框（带字数统计）</span>
              <div class="copy-btn" @click="copy(codeExamples.input5, '复制成功!')">
                <Icon code="copy" size="md" />
              </div>
            </div>
            <AFormInput label="备注" v-model="inputForm.remark" prop="remark" type="textarea" :maxlength="200" show-word-limit />
          </div>
        </div>
      </el-form>

      <!-- 2. 选择器组件 AFormSelect -->
      <h2 class="section-title">2. 选择器组件 AFormSelect</h2>
      <el-form ref="selectFormRef" :model="selectForm" label-width="100px">
        <div class="examples-grid">
          <div class="example-item">
            <div class="example-header">
              <span class="example-title">基础选择器</span>
              <div class="copy-btn" @click="copy(codeExamples.select1, '复制成功!')">
                <Icon code="copy" size="md" />
              </div>
            </div>
            <AFormSelect v-model="selectForm.status" :options="sys_enable_status" label="状态" prop="status" />
          </div>

          <div class="example-item">
            <div class="example-header">
              <span class="example-title">多选选择器</span>
              <div class="copy-btn" @click="copy(codeExamples.select2, '复制成功!')">
                <Icon code="copy" size="md" />
              </div>
            </div>
            <AFormSelect
              v-model="selectForm.roles"
              :options="roleOptions"
              label="角色"
              prop="roles"
              multiple
              collapse-tags
              collapse-tags-tooltip
              value-field="roleId"
              label-field="roleName"
            />
          </div>

          <div class="example-item">
            <div class="example-header">
              <span class="example-title">带提示的选择器</span>
              <div class="copy-btn" @click="copy(codeExamples.select3, '复制成功!')">
                <Icon code="copy" size="md" />
              </div>
            </div>
            <AFormSelect v-model="selectForm.type" :options="typeOptions" label="类型" prop="type" tooltip="请选择应用类型" />
          </div>

          <div class="example-item">
            <div class="example-header">
              <span class="example-title">可创建条目</span>
              <div class="copy-btn" @click="copy(codeExamples.select4, '复制成功!')">
                <Icon code="copy" size="md" />
              </div>
            </div>
            <AFormSelect v-model="selectForm.tags" :options="tagOptions" label="标签" prop="tags" multiple allow-create filterable />
          </div>
        </div>
      </el-form>

      <!-- 3. 开关组件 AFormSwitch -->
      <h2 class="section-title">3. 开关组件 AFormSwitch</h2>
      <el-form ref="switchFormRef" :model="switchForm" label-width="100px">
        <div class="examples-grid">
          <div class="example-item">
            <div class="example-header">
              <span class="example-title">基础开关</span>
              <div class="copy-btn" @click="copy(codeExamples.switch1, '复制成功!')">
                <Icon code="copy" size="md" />
              </div>
            </div>
            <AFormSwitch v-model="switchForm.isEnabled" label="启用状态" prop="isEnabled" />
          </div>

          <div class="example-item">
            <div class="example-header">
              <span class="example-title">自定义文本</span>
              <div class="copy-btn" @click="copy(codeExamples.switch2, '复制成功!')">
                <Icon code="copy" size="md" />
              </div>
            </div>
            <AFormSwitch
              v-model="switchForm.isPublic"
              label="是否公开"
              prop="isPublic"
              active-text="公开"
              inactive-text="私有"
              tooltip="公开后所有用户可见"
            />
          </div>

          <div class="example-item">
            <div class="example-header">
              <span class="example-title">布尔值类型</span>
              <div class="copy-btn" @click="copy(codeExamples.switch3, '复制成功!')">
                <Icon code="copy" size="md" />
              </div>
            </div>
            <AFormSwitch v-model="switchForm.autoSave" label="自动保存" prop="autoSave" :active-value="true" :inactive-value="false" />
          </div>

          <div class="example-item">
            <div class="example-header">
              <span class="example-title">自定义颜色</span>
              <div class="copy-btn" @click="copy(codeExamples.switch4, '复制成功!')">
                <Icon code="copy" size="md" />
              </div>
            </div>
            <AFormSwitch v-model="switchForm.isDanger" label="危险模式" prop="isDanger" active-color="#ff4949" inactive-color="#13ce66" />
          </div>
        </div>
      </el-form>

      <!-- 4. 单选框组件 AFormRadio -->
      <h2 class="section-title">4. 单选框组件 AFormRadio</h2>
      <el-form ref="radioFormRef" :model="radioForm" label-width="100px">
        <div class="examples-grid">
          <div class="example-item">
            <div class="example-header">
              <span class="example-title">基础单选框</span>
              <div class="copy-btn" @click="copy(codeExamples.radio1, '复制成功!')">
                <Icon code="copy" size="md" />
              </div>
            </div>
            <AFormRadio v-model="radioForm.gender" :options="genderOptions" label="性别" prop="gender" />
          </div>

          <div class="example-item">
            <div class="example-header">
              <span class="example-title">按钮样式</span>
              <div class="copy-btn" @click="copy(codeExamples.radio2, '复制成功!')">
                <Icon code="copy" size="md" />
              </div>
            </div>
            <AFormRadio v-model="radioForm.priority" :options="priorityOptions" label="优先级" prop="priority" type="button" />
          </div>

          <div class="example-item">
            <div class="example-header">
              <span class="example-title">带边框样式</span>
              <div class="copy-btn" @click="copy(codeExamples.radio3, '复制成功!')">
                <Icon code="copy" size="md" />
              </div>
            </div>
            <AFormRadio v-model="radioForm.size" :options="sizeOptions" label="尺寸" prop="size" border />
          </div>

          <div class="example-item">
            <div class="example-header">
              <span class="example-title">自定义字段映射</span>
              <div class="copy-btn" @click="copy(codeExamples.radio4, '复制成功!')">
                <Icon code="copy" size="md" />
              </div>
            </div>
            <AFormRadio v-model="radioForm.level" :options="levelOptions" label="级别" prop="level" value-field="id" label-field="name" />
          </div>
        </div>
      </el-form>

      <!-- 5. 复选框组件 AFormCheckbox -->
      <h2 class="section-title">5. 复选框组件 AFormCheckbox</h2>
      <el-form ref="checkboxFormRef" :model="checkboxForm" label-width="100px">
        <div class="examples-grid">
          <div class="example-item">
            <div class="example-header">
              <span class="example-title">基础复选框</span>
              <div class="copy-btn" @click="copy(codeExamples.checkbox1, '复制成功!')">
                <Icon code="copy" size="md" />
              </div>
            </div>
            <AFormCheckbox v-model="checkboxForm.hobbies" :options="hobbyOptions" label="爱好" prop="hobbies" />
          </div>

          <div class="example-item">
            <div class="example-header">
              <span class="example-title">按钮样式</span>
              <div class="copy-btn" @click="copy(codeExamples.checkbox2, '复制成功!')">
                <Icon code="copy" size="md" />
              </div>
            </div>
            <AFormCheckbox
              v-model="checkboxForm.permissions"
              :options="permissionOptions"
              label="权限"
              prop="permissions"
              type="button"
              :return-array="true"
            />
          </div>

          <div class="example-item">
            <div class="example-header">
              <span class="example-title">限制数量</span>
              <div class="copy-btn" @click="copy(codeExamples.checkbox3, '复制成功!')">
                <Icon code="copy" size="md" />
              </div>
            </div>
            <AFormCheckbox v-model="checkboxForm.features" :options="featureOptions" label="功能" prop="features" :min="1" :max="3" />
          </div>

          <div class="example-item">
            <div class="example-header">
              <span class="example-title">单个复选框（记住密码）</span>
              <div class="copy-btn" @click="copy(codeExamples.checkbox4, '复制成功!')">
                <Icon code="copy" size="md" />
              </div>
            </div>
            <AFormCheckbox v-model="checkboxForm.rememberMe" :options="[{ label: '记住密码', value: true }]" label="选项" :single-checkbox="true" />
          </div>
        </div>
      </el-form>

      <!-- 6. 日期选择器组件 AFormDate -->
      <h2 class="section-title">6. 日期选择器组件 AFormDate</h2>
      <el-form ref="dateFormRef" :model="dateForm" label-width="100px">
        <div class="examples-grid">
          <div class="example-item">
            <div class="example-header">
              <span class="example-title">日期选择</span>
              <div class="copy-btn" @click="copy(codeExamples.date1, '复制成功!')">
                <Icon code="copy" size="md" />
              </div>
            </div>
            <AFormDate label="日期" v-model="dateForm.date" prop="date" type="date" />
          </div>

          <div class="example-item">
            <div class="example-header">
              <span class="example-title">日期时间选择</span>
              <div class="copy-btn" @click="copy(codeExamples.date2, '复制成功!')">
                <Icon code="copy" size="md" />
              </div>
            </div>
            <AFormDate label="日期时间" v-model="dateForm.datetime" prop="datetime" type="datetime" />
          </div>

          <div class="example-item">
            <div class="example-header">
              <span class="example-title">日期范围</span>
              <div class="copy-btn" @click="copy(codeExamples.date3, '复制成功!')">
                <Icon code="copy" size="md" />
              </div>
            </div>
            <AFormDate label="日期范围" v-model="dateForm.dateRange" prop="dateRange" type="daterange" />
          </div>

          <div class="example-item">
            <div class="example-header">
              <span class="example-title">月份选择</span>
              <div class="copy-btn" @click="copy(codeExamples.date4, '复制成功!')">
                <Icon code="copy" size="md" />
              </div>
            </div>
            <AFormDate label="月份" v-model="dateForm.month" prop="month" type="month" />
          </div>
        </div>
      </el-form>

      <!-- 7. 树形选择器组件 AFormTreeSelect -->
      <h2 class="section-title">7. 树形选择器组件 AFormTreeSelect</h2>
      <el-form ref="treeFormRef" :model="treeForm" label-width="100px">
        <div class="examples-grid">
          <div class="example-item">
            <div class="example-header">
              <span class="example-title">基础树形选择</span>
              <div class="copy-btn" @click="copy(codeExamples.tree1, '复制成功!')">
                <Icon code="copy" size="md" />
              </div>
            </div>
            <AFormTreeSelect
              label="部门"
              v-model="treeForm.deptId"
              :data="deptTreeData"
              prop="deptId"
              :props="{ value: 'id', label: 'label', children: 'children' }"
            />
          </div>

          <div class="example-item">
            <div class="example-header">
              <span class="example-title">多选树形选择</span>
              <div class="copy-btn" @click="copy(codeExamples.tree2, '复制成功!')">
                <Icon code="copy" size="md" />
              </div>
            </div>
            <AFormTreeSelect label="权限菜单" v-model="treeForm.menuIds" :data="menuTreeData" prop="menuIds" multiple show-checkbox />
          </div>

          <div class="example-item">
            <div class="example-header">
              <span class="example-title">可搜索树形选择</span>
              <div class="copy-btn" @click="copy(codeExamples.tree3, '复制成功!')">
                <Icon code="copy" size="md" />
              </div>
            </div>
            <AFormTreeSelect label="组织架构" v-model="treeForm.orgId" :data="deptTreeData" prop="orgId" filterable />
          </div>

          <div class="example-item">
            <div class="example-header">
              <span class="example-title">默认展开所有</span>
              <div class="copy-btn" @click="copy(codeExamples.tree4, '复制成功!')">
                <Icon code="copy" size="md" />
              </div>
            </div>
            <AFormTreeSelect label="分类" v-model="treeForm.categoryId" :data="menuTreeData" prop="categoryId" default-expand-all />
          </div>
        </div>
      </el-form>

      <!-- 8. 级联选择器组件 AFormCascader -->
      <h2 class="section-title">8. 级联选择器组件 AFormCascader</h2>
      <el-form ref="cascaderFormRef" :model="cascaderForm" label-width="100px">
        <div class="examples-grid">
          <div class="example-item">
            <div class="example-header">
              <span class="example-title">地区选择（内置数据）</span>
              <div class="copy-btn" @click="copy(codeExamples.cascader1, '复制成功!')">
                <Icon code="copy" size="md" />
              </div>
            </div>
            <AFormCascader v-model="cascaderForm.areaCode" mode="region" label="地区" prop="areaCode" />
          </div>

          <div class="example-item">
            <div class="example-header">
              <span class="example-title">自定义数据</span>
              <div class="copy-btn" @click="copy(codeExamples.cascader2, '复制成功!')">
                <Icon code="copy" size="md" />
              </div>
            </div>
            <AFormCascader
              v-model="cascaderForm.categoryId"
              :options="categoryOptions"
              label="分类"
              prop="categoryId"
              mode="custom"
              value-field="id"
              label-field="name"
              children-field="children"
            />
          </div>

          <div class="example-item">
            <div class="example-header">
              <span class="example-title">可搜索级联</span>
              <div class="copy-btn" @click="copy(codeExamples.cascader3, '复制成功!')">
                <Icon code="copy" size="md" />
              </div>
            </div>
            <AFormCascader v-model="cascaderForm.searchArea" mode="region" label="搜索地区" prop="searchArea" filterable />
          </div>

          <div class="example-item">
            <div class="example-header">
              <span class="example-title">多选级联</span>
              <div class="copy-btn" @click="copy(codeExamples.cascader4, '复制成功!')">
                <Icon code="copy" size="md" />
              </div>
            </div>
            <AFormCascader
              v-model="cascaderForm.multiArea"
              mode="region"
              label="多选地区"
              prop="multiArea"
              multiple
              collapse-tags
              collapse-tags-tooltip
            />
          </div>
        </div>
      </el-form>

      <!-- 9. 图片上传组件 AFormImgUpload -->
      <h2 class="section-title">9. 图片上传组件 AFormImgUpload</h2>
      <el-form ref="imgUploadFormRef" :model="imgUploadForm" label-width="100px">
        <div class="examples-grid">
          <div class="example-item">
            <div class="example-header">
              <span class="example-title">单张图片上传</span>
              <div class="copy-btn" @click="copy(codeExamples.imgUpload1, '复制成功!')">
                <Icon code="copy" size="md" />
              </div>
            </div>
            <AFormImgUpload label="头像" v-model="imgUploadForm.avatar" prop="avatar" :limit="1" />
          </div>

          <div class="example-item">
            <div class="example-header">
              <span class="example-title">多张图片上传</span>
              <div class="copy-btn" @click="copy(codeExamples.imgUpload2, '复制成功!')">
                <Icon code="copy" size="md" />
              </div>
            </div>
            <AFormImgUpload label="产品相册" v-model="imgUploadForm.gallery" prop="gallery" :limit="9" list-type="picture-card" />
          </div>

          <div class="example-item">
            <div class="example-header">
              <span class="example-title">带压缩上传</span>
              <div class="copy-btn" @click="copy(codeExamples.imgUpload3, '复制成功!')">
                <Icon code="copy" size="md" />
              </div>
            </div>
            <AFormImgUpload
              label="高清图片"
              v-model="imgUploadForm.hdImages"
              prop="hdImages"
              :compress-support="true"
              :compress-target-size="500"
              :limit="5"
            />
          </div>

          <div class="example-item">
            <div class="example-header">
              <span class="example-title">返回ossId模式</span>
              <div class="copy-btn" @click="copy(codeExamples.imgUpload4, '复制成功!')">
                <Icon code="copy" size="md" />
              </div>
            </div>
            <AFormImgUpload label="产品图片" v-model="imgUploadForm.productImages" prop="productImages" return-mode="ossId" :limit="5" />
          </div>
        </div>
      </el-form>

      <!-- 10. 文件上传组件 AFormFileUpload -->
      <h2 class="section-title">10. 文件上传组件 AFormFileUpload</h2>
      <el-form ref="fileUploadFormRef" :model="fileUploadForm" label-width="100px">
        <div class="examples-grid">
          <div class="example-item">
            <div class="example-header">
              <span class="example-title">基础文件上传</span>
              <div class="copy-btn" @click="copy(codeExamples.fileUpload1, '复制成功!')">
                <Icon code="copy" size="md" />
              </div>
            </div>
            <AFormFileUpload label="附件" v-model="fileUploadForm.files" prop="files" />
          </div>

          <div class="example-item">
            <div class="example-header">
              <span class="example-title">限制文件类型</span>
              <div class="copy-btn" @click="copy(codeExamples.fileUpload2, '复制成功!')">
                <Icon code="copy" size="md" />
              </div>
            </div>
            <AFormFileUpload label="文档" v-model="fileUploadForm.documents" prop="documents" :file-type="['pdf', 'doc', 'docx']" :file-size="10" />
          </div>
        </div>

        <div class="examples-grid mt-4">
          <div class="example-item" style="grid-column: 1 / -1">
            <div class="example-header">
              <span class="example-title">拖拽上传</span>
              <div class="copy-btn" @click="copy(codeExamples.fileUpload3, '复制成功!')">
                <Icon code="copy" size="md" />
              </div>
            </div>
            <AFormFileUpload label="批量文档" v-model="fileUploadForm.batchFiles" prop="batchFiles" :drag="true" :limit="10" />
          </div>
        </div>
      </el-form>

      <!-- 11. AI输入组件 AFormInputWithAi -->
      <h2 class="section-title">11. AI输入组件 AFormInputWithAi</h2>
      <el-form ref="aiFormRef" :model="aiForm" label-width="100px">
        <div class="examples-grid">
          <div class="example-item" style="grid-column: 1 / -1">
            <div class="example-header">
              <span class="example-title">基础AI输入（标题优化）</span>
              <div class="copy-btn" @click="copy(codeExamples.ai1, '复制成功!')">
                <Icon code="copy" size="md" />
              </div>
            </div>
            <AFormInputWithAi label="文章标题" v-model="aiForm.title" prop="title" field-type="title" />
          </div>
        </div>

        <div class="examples-grid mt-4">
          <div class="example-item" style="grid-column: 1 / -1">
            <div class="example-header">
              <span class="example-title">带快捷操作按钮</span>
              <div class="copy-btn" @click="copy(codeExamples.ai2, '复制成功!')">
                <Icon code="copy" size="md" />
              </div>
            </div>
            <AFormInputWithAi
              label="文章内容"
              v-model="aiForm.content"
              prop="content"
              type="textarea"
              field-type="content"
              :show-quick-actions="true"
              :quick-actions="['polish', 'expand', 'shorten']"
            />
          </div>
        </div>

        <div class="examples-grid mt-4">
          <div class="example-item" style="grid-column: 1 / -1">
            <div class="example-header">
              <span class="example-title">自动应用优化结果</span>
              <div class="copy-btn" @click="copy(codeExamples.ai3, '复制成功!')">
                <Icon code="copy" size="md" />
              </div>
            </div>
            <AFormInputWithAi
              label="备注说明"
              v-model="aiForm.remark"
              prop="remark"
              type="textarea"
              field-type="remark"
              :ai-config="{ autoApply: true }"
            />
          </div>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { copy } from '@/utils/function'

// 获取字典数据
const { sys_enable_status } = useDict(DictTypes.sys_enable_status)

// ========== 输入框表单 ==========
const inputForm = reactive({
  userName: '',
  password: '',
  age: undefined as number | undefined,
  email: '',
  remark: ''
})
const inputFormRef = ref()

// ========== 选择器表单 ==========
const selectForm = reactive({
  status: '',
  roles: [] as number[],
  type: '',
  tags: [] as string[]
})
const selectFormRef = ref()

const roleOptions = ref([
  { roleId: 1, roleName: '超级管理员' },
  { roleId: 2, roleName: '普通管理员' },
  { roleId: 3, roleName: '普通用户' },
  { roleId: 4, roleName: '访客' }
])

const typeOptions = ref([
  { value: '1', label: 'Web应用' },
  { value: '2', label: '移动应用' },
  { value: '3', label: '桌面应用' }
])

const tagOptions = ref([
  { value: 'vue', label: 'Vue.js' },
  { value: 'react', label: 'React' },
  { value: 'angular', label: 'Angular' }
])

// ========== 开关表单 ==========
const switchForm = reactive({
  isEnabled: '1',
  isPublic: '0',
  autoSave: false,
  isDanger: '0'
})
const switchFormRef = ref()

// ========== 单选框表单 ==========
const radioForm = reactive({
  gender: '1',
  priority: 'medium',
  size: 'default',
  level: ''
})
const radioFormRef = ref()

const genderOptions = ref([
  { value: '1', label: '男' },
  { value: '0', label: '女' }
])

const priorityOptions = ref([
  { value: 'low', label: '低' },
  { value: 'medium', label: '中' },
  { value: 'high', label: '高' }
])

const sizeOptions = ref([
  { value: 'small', label: '小' },
  { value: 'default', label: '中' },
  { value: 'large', label: '大' }
])

const levelOptions = ref([
  { id: 'A', name: 'A级' },
  { id: 'B', name: 'B级' },
  { id: 'C', name: 'C级' }
])

// ========== 复选框表单 ==========
const checkboxForm = reactive({
  hobbies: '',
  permissions: [] as string[],
  features: '',
  rememberMe: false
})
const checkboxFormRef = ref()

const hobbyOptions = ref([
  { value: 'reading', label: '阅读' },
  { value: 'music', label: '音乐' },
  { value: 'sports', label: '运动' },
  { value: 'travel', label: '旅行' }
])

const permissionOptions = ref([
  { value: 'read', label: '查看' },
  { value: 'write', label: '编辑' },
  { value: 'delete', label: '删除' },
  { value: 'export', label: '导出' }
])

const featureOptions = ref([
  { value: 'f1', label: '功能1' },
  { value: 'f2', label: '功能2' },
  { value: 'f3', label: '功能3' },
  { value: 'f4', label: '功能4' },
  { value: 'f5', label: '功能5' }
])

// ========== 日期表单 ==========
const dateForm = reactive({
  date: '',
  datetime: '',
  dateRange: [] as string[],
  month: ''
})
const dateFormRef = ref()

// ========== 树形选择器表单 ==========
const treeForm = reactive({
  deptId: null as number | null,
  menuIds: [] as number[],
  orgId: null as number | null,
  categoryId: null as number | null
})
const treeFormRef = ref()

const deptTreeData = ref([
  {
    id: 1,
    label: '总公司',
    children: [
      {
        id: 2,
        label: '技术部',
        children: [
          { id: 4, label: '前端组' },
          { id: 5, label: '后端组' }
        ]
      },
      {
        id: 3,
        label: '市场部',
        children: [
          { id: 6, label: '销售组' },
          { id: 7, label: '推广组' }
        ]
      }
    ]
  }
])

const menuTreeData = ref([
  {
    id: 1,
    label: '系统管理',
    children: [
      { id: 2, label: '用户管理' },
      { id: 3, label: '角色管理' },
      { id: 4, label: '菜单管理' }
    ]
  },
  {
    id: 5,
    label: '业务管理',
    children: [
      { id: 6, label: '订单管理' },
      { id: 7, label: '商品管理' }
    ]
  }
])

// ========== 级联选择器表单 ==========
const cascaderForm = reactive({
  areaCode: '',
  categoryId: '',
  searchArea: '',
  multiArea: ''
})
const cascaderFormRef = ref()

const categoryOptions = ref([
  {
    id: '1',
    name: '电子产品',
    children: [
      {
        id: '1-1',
        name: '手机',
        children: [
          { id: '1-1-1', name: '智能手机' },
          { id: '1-1-2', name: '功能机' }
        ]
      },
      {
        id: '1-2',
        name: '电脑',
        children: [
          { id: '1-2-1', name: '笔记本' },
          { id: '1-2-2', name: '台式机' }
        ]
      }
    ]
  },
  {
    id: '2',
    name: '服装',
    children: [
      { id: '2-1', name: '男装' },
      { id: '2-2', name: '女装' }
    ]
  }
])

// ========== 图片上传表单 ==========
const imgUploadForm = reactive({
  avatar: '',
  gallery: '',
  hdImages: '',
  productImages: ''
})
const imgUploadFormRef = ref()

// ========== 文件上传表单 ==========
const fileUploadForm = reactive({
  files: '',
  documents: '',
  batchFiles: ''
})
const fileUploadFormRef = ref()

// ========== AI表单 ==========
const aiForm = reactive({
  title: '',
  content: '',
  remark: ''
})
const aiFormRef = ref()

// ========== 代码示例 ==========
const codeExamples = {
  // 输入框
  input1: `<AFormInput label="用户名" v-model="form.userName" prop="userName" />`,
  input2: `<AFormInput label="密码" v-model="form.password" prop="password" type="password" show-password prevent-autofill />`,
  input3: `<AFormInput label="年龄" v-model="form.age" prop="age" type="number" :min="1" :max="120" />`,
  input4: `<AFormInput label="邮箱" v-model="form.email" prop="email" tooltip="请输入有效的邮箱地址" />`,
  input5: `<AFormInput label="备注" v-model="form.remark" prop="remark" type="textarea" :maxlength="200" show-word-limit />`,

  // 选择器
  select1: `<AFormSelect v-model="form.status" :options="sys_enable_status" label="状态" prop="status" />`,
  select2: `<AFormSelect v-model="form.roles" :options="roleOptions" label="角色" prop="roles" multiple collapse-tags collapse-tags-tooltip value-field="roleId" label-field="roleName" />`,
  select3: `<AFormSelect v-model="form.type" :options="typeOptions" label="类型" prop="type" tooltip="请选择应用类型" />`,
  select4: `<AFormSelect v-model="form.tags" :options="tagOptions" label="标签" prop="tags" multiple allow-create filterable />`,

  // 开关
  switch1: `<AFormSwitch v-model="form.isEnabled" label="启用状态" prop="isEnabled" />`,
  switch2: `<AFormSwitch v-model="form.isPublic" label="是否公开" prop="isPublic" active-text="公开" inactive-text="私有" tooltip="公开后所有用户可见" />`,
  switch3: `<AFormSwitch v-model="form.autoSave" label="自动保存" prop="autoSave" :active-value="true" :inactive-value="false" />`,
  switch4: `<AFormSwitch v-model="form.isDanger" label="危险模式" prop="isDanger" active-color="#ff4949" inactive-color="#13ce66" />`,

  // 单选框
  radio1: `<AFormRadio v-model="form.gender" :options="genderOptions" label="性别" prop="gender" />`,
  radio2: `<AFormRadio v-model="form.priority" :options="priorityOptions" label="优先级" prop="priority" type="button" />`,
  radio3: `<AFormRadio v-model="form.size" :options="sizeOptions" label="尺寸" prop="size" border />`,
  radio4: `<AFormRadio v-model="form.level" :options="levelOptions" label="级别" prop="level" value-field="id" label-field="name" />`,

  // 复选框
  checkbox1: `<AFormCheckbox v-model="form.hobbies" :options="hobbyOptions" label="爱好" prop="hobbies" />`,
  checkbox2: `<AFormCheckbox v-model="form.permissions" :options="permissionOptions" label="权限" prop="permissions" type="button" :return-array="true" />`,
  checkbox3: `<AFormCheckbox v-model="form.features" :options="featureOptions" label="功能" prop="features" :min="1" :max="3" />`,
  checkbox4: `<AFormCheckbox v-model="form.rememberMe" :options="[{ label: '记住密码', value: true }]" label="选项" :single-checkbox="true" />`,

  // 日期选择器
  date1: `<AFormDate label="日期" v-model="form.date" prop="date" type="date" />`,
  date2: `<AFormDate label="日期时间" v-model="form.datetime" prop="datetime" type="datetime" />`,
  date3: `<AFormDate label="日期范围" v-model="form.dateRange" prop="dateRange" type="daterange" />`,
  date4: `<AFormDate label="月份" v-model="form.month" prop="month" type="month" />`,

  // 树形选择器
  tree1: `<AFormTreeSelect label="部门" v-model="form.deptId" :data="deptTreeData" prop="deptId" :props="{ value: 'id', label: 'label', children: 'children' }" />`,
  tree2: `<AFormTreeSelect label="权限菜单" v-model="form.menuIds" :data="menuTreeData" prop="menuIds" multiple show-checkbox />`,
  tree3: `<AFormTreeSelect label="组织架构" v-model="form.orgId" :data="deptTreeData" prop="orgId" filterable />`,
  tree4: `<AFormTreeSelect label="分类" v-model="form.categoryId" :data="menuTreeData" prop="categoryId" default-expand-all />`,

  // 级联选择器
  cascader1: `<AFormCascader v-model="form.areaCode" mode="region" label="地区" prop="areaCode" />`,
  cascader2: `<AFormCascader v-model="form.categoryId" :options="categoryOptions" label="分类" prop="categoryId" mode="custom" value-field="id" label-field="name" children-field="children" />`,
  cascader3: `<AFormCascader v-model="form.searchArea" mode="region" label="搜索地区" prop="searchArea" filterable />`,
  cascader4: `<AFormCascader v-model="form.multiArea" mode="region" label="多选地区" prop="multiArea" multiple collapse-tags collapse-tags-tooltip />`,

  // 图片上传
  imgUpload1: `<AFormImgUpload label="头像" v-model="form.avatar" prop="avatar" :limit="1" />`,
  imgUpload2: `<AFormImgUpload label="产品相册" v-model="form.gallery" prop="gallery" :limit="9" list-type="picture-card" />`,
  imgUpload3: `<AFormImgUpload label="高清图片" v-model="form.hdImages" prop="hdImages" :compress-support="true" :compress-target-size="500" :limit="5" />`,
  imgUpload4: `<AFormImgUpload label="产品图片" v-model="form.productImages" prop="productImages" return-mode="ossId" :limit="5" />`,

  // 文件上传
  fileUpload1: `<AFormFileUpload label="附件" v-model="form.files" prop="files" />`,
  fileUpload2: `<AFormFileUpload label="文档" v-model="form.documents" prop="documents" :file-type="['pdf', 'doc', 'docx']" :file-size="10" />`,
  fileUpload3: `<AFormFileUpload label="批量文档" v-model="form.batchFiles" prop="batchFiles" :drag="true" :limit="10" />`,

  // AI输入
  ai1: `<AFormInputWithAi label="文章标题" v-model="form.title" prop="title" field-type="title" />`,
  ai2: `<AFormInputWithAi label="文章内容" v-model="form.content" prop="content" type="textarea" field-type="content" :show-quick-actions="true" :quick-actions="['polish', 'expand', 'shorten']" />`,
  ai3: `<AFormInputWithAi label="备注说明" v-model="form.remark" prop="remark" type="textarea" field-type="remark" :ai-config="{ autoApply: true }" />`
}
</script>

<style lang="scss" scoped>
.form-container {
  @apply p-5;
}

.examples-container {
  @apply flex flex-col gap-8 w-full;
}

.section-title {
  @apply text-16px font-600 mb-4 mt-6;
  color: var(--el-text-color-primary);
  border-left: 4px solid var(--el-color-primary);
  padding-left: 12px;

  &:first-child {
    @apply mt-0;
  }
}

.examples-grid {
  @apply grid grid-cols-[repeat(auto-fill,minmax(280px,1fr))] gap-5 w-full;
}

.example-item {
  @apply flex flex-col gap-3;

  .example-header {
    @apply flex items-center justify-between px-3 py-2 rounded-md transition-all duration-300;
    background: var(--el-fill-color-light);

    .example-title {
      @apply text-13px font-500;
      color: var(--el-text-color-regular);
    }

    .copy-btn {
      @apply flex items-center justify-center w-7 h-7 rounded-md cursor-pointer transition-all duration-300;
      color: var(--el-text-color-secondary);
      background: transparent;

      &:hover {
        color: var(--el-color-primary);
        background: var(--el-color-primary-light-9);
        @apply scale-110;
      }

      &:active {
        @apply scale-95;
      }
    }
  }
}

.mt-4 {
  margin-top: 16px;
}
</style>
