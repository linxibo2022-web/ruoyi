<!-- API密钥管理 -->
<template>
  <div>
    <!-- 操作按钮 -->
    <div class="mb-4">
      <el-button type="primary" plain icon="Plus" @click="handleGenerate" :disabled="apiKeys.length >= maxKeys">
        {{ t('Generate Key', '生成密钥') }} ({{ apiKeys.length }}/{{ maxKeys }})
      </el-button>
      <el-button type="success" plain icon="List" @click="handleViewOpenApis"> {{ t('View APIs', '查看可用接口') }} </el-button>
      <el-alert v-if="apiKeys.length >= maxKeys" type="warning" :closable="false" class="mt-2">
        {{ t('Max keys reached', '您已达到最大密钥数量限制') }}({{ maxKeys }}{{ t('keys', '个') }}),{{
          t('delete unused keys', '请删除不使用的密钥后再创建新密钥')
        }}
      </el-alert>
    </div>

    <!-- API密钥列表 -->
    <el-table :data="apiKeys" border max-height="400">
      <el-table-column :label="t('App Name', '应用名称')" prop="appName" align="center" min-width="120" />
      <el-table-column label="AppKey" prop="appKey" align="center" min-width="200">
        <template #default="{ row }">
          <el-text truncated class="cursor-pointer" @click="copy(row.appKey)" :title="clickToCopyTitle(row.appKey)">
            {{ row.appKey }}
          </el-text>
        </template>
      </el-table-column>
      <el-table-column :label="t('Status', '状态')" prop="status" align="center" width="80">
        <template #default="{ row }">
          <DictTag :options="sys_enable_status" :value="row.status" />
        </template>
      </el-table-column>
      <el-table-column :label="t('Expire Time', '过期时间')" prop="expireTime" align="center" width="160" />
      <el-table-column :label="t('Call Count', '调用次数')" prop="callCount" align="center" width="100" />
      <el-table-column :label="t('Create Time', '创建时间')" prop="createTime" align="center" width="160" />
      <el-table-column :label="t('操作')" align="center" fixed="right" width="180">
        <template #default="{ row }">
          <el-tooltip :content="t('查看')" placement="top">
            <el-button link type="primary" icon="View" @click="handleView(row)"></el-button>
          </el-tooltip>
          <el-tooltip :content="t('Edit Whitelist', '编辑白名单')" placement="top">
            <el-button link type="success" icon="Edit" @click="handleEditWhiteIps(row)"></el-button>
          </el-tooltip>
          <el-tooltip :content="t('Reset Secret', '重置密钥')" placement="top">
            <el-button link type="warning" icon="RefreshRight" @click="handleResetSecret(row)"></el-button>
          </el-tooltip>
          <el-tooltip :content="t('删除')" placement="top">
            <el-button link type="danger" icon="Delete" @click="handleDelete(row)"></el-button>
          </el-tooltip>
        </template>
      </el-table-column>
    </el-table>

    <!-- 生成密钥对话框 -->
    <AModal
      v-model="generateDialog.visible"
      size="small"
      :title="generateDialog.title"
      :loading="buttonLoading"
      @confirm="submitGenerate"
      @cancel="cancelGenerate"
    >
      <el-form ref="generateFormRef" :model="generateForm" :rules="generateRules" label-width="120px">
        <AFormInput label="应用名称" v-model="generateForm.appName" prop="appName" :placeholder="t('Enter app name', '请输入应用名称')" />
        <AFormDate
          label="过期时间"
          v-model="generateForm.expireTime"
          prop="expireTime"
          type="datetime"
          :placeholder="t('No expiry if empty', '不设置则永久有效')"
        />
        <AFormInput
          :label="t('IP Whitelist', 'IP白名单')"
          v-model="generateForm.whiteIps"
          type="textarea"
          :rows="3"
          prop="whiteIps"
          :placeholder="t('One IP per line', '每行一个IP，不填则不限制') + '\n' + t('Formats', '支持格式') + ':\n192.168.1.1\n192.168.1.*'"
        />
        <AFormInput :label="t('Remark', '备注')" v-model="generateForm.remark" type="textarea" prop="remark" />
      </el-form>
    </AModal>

    <!-- 密钥详情对话框(仅生成时显示) -->
    <AModal v-model="secretDialog.visible" :title="secretDialog.title" size="small" :loading="false" @confirm="secretDialog.visible = false">
      <el-alert type="warning" :closable="false" show-icon>
        <template #title>
          <span style="color: #e6a23c">{{ t('Key generated! Save it now, shown only once', '密钥生成成功！请妥善保管，密钥只显示一次') }}</span>
        </template>
      </el-alert>
      <el-descriptions :column="1" border class="mt-4">
        <el-descriptions-item :label="t('App Name', '应用名称')">{{ secretData.appName }}</el-descriptions-item>
        <el-descriptions-item label="AppKey">
          <el-input v-model="secretData.appKey" readonly>
            <template #append>
              <el-button @click="copy(secretData.appKey)">{{ t('Copy', '复制') }}</el-button>
            </template>
          </el-input>
        </el-descriptions-item>
        <el-descriptions-item label="AppSecret">
          <el-input v-model="secretData.appSecret" readonly>
            <template #append>
              <el-button @click="copy(secretData.appSecret)">{{ t('Copy', '复制') }}</el-button>
            </template>
          </el-input>
        </el-descriptions-item>
      </el-descriptions>
    </AModal>

    <!-- 编辑白名单对话框 -->
    <AModal
      v-model="editWhiteIpsDialog.visible"
      size="small"
      :title="editWhiteIpsDialog.title"
      :loading="buttonLoading"
      @confirm="submitEditWhiteIps"
      @cancel="cancelEditWhiteIps"
    >
      <el-form ref="editWhiteIpsFormRef" :model="editWhiteIpsForm" label-width="120px">
        <AFormInput
          :label="t('IP Whitelist', 'IP白名单')"
          v-model="editWhiteIpsForm.whiteIps"
          type="textarea"
          :rows="5"
          prop="whiteIps"
          :placeholder="t('One IP per line', '每行一个IP，不填则不限制') + '\n' + t('Formats', '支持格式') + ':\n192.168.1.1\n192.168.1.*'"
        />
      </el-form>
    </AModal>

    <!-- 查看详情对话框 -->
    <ADetail
      v-model="viewDialog.visible"
      :title="viewDialog.title"
      :data="viewData"
      :fields="[
        { prop: 'appName', label: t('App Name', '应用名称') },
        { prop: 'appKey', label: 'AppKey', type: 'copyable' },
        { prop: 'whiteIps', label: t('IP Whitelist', 'IP白名单'), slot: 'whiteIps' },
        { prop: 'expireTime', label: t('Expire Time', '过期时间'), type: 'datetime' },
        { prop: 'status', label: t('Status', '状态'), type: 'dict', dictOptions: sys_enable_status },
        { prop: 'callCount', label: t('Call Count', '调用次数') },
        { prop: 'lastCallTime', label: t('Last Call Time', '最后调用时间'), type: 'datetime' },
        { prop: 'createTime', label: t('Create Time', '创建时间'), type: 'datetime' },
        { prop: 'remark', label: t('Remark', '备注') }
      ]"
    >
      <!-- IP白名单插槽 -->
      <template #whiteIps="{ value }">
        <div v-if="value" class="flex flex-wrap gap-1">
          <el-tag v-for="ip in value.split(',')" :key="ip" size="small">{{ ip }}</el-tag>
        </div>
        <el-text v-else type="info">{{ t('No IP restriction', '不限制IP') }}</el-text>
      </template>
    </ADetail>

    <!-- 开放接口列表对话框 -->
    <AModal v-model="openApiDialog.visible" :title="openApiDialog.title" size="large" :show-footer="false">
      <!-- 搜索 -->
      <el-input
        v-model="searchText"
        :placeholder="t('Search API path or description', '搜索接口路径、说明...')"
        clearable
        class="mb-4"
        prefix-icon="Search"
      />

      <!-- 按模块分组的折叠面板 -->
      <el-collapse v-model="activeModules" class="open-api-collapse">
        <el-collapse-item v-for="(apis, module) in groupedOpenApis" :key="module" :name="module">
          <template #title>
            <div class="module-title">
              <el-tag type="primary">{{ module }}</el-tag>
              <span class="ml-2">{{ apis.length }} {{ t('APIs', '个接口') }}</span>
            </div>
          </template>

          <el-table :data="apis" border stripe>
            <el-table-column :label="t('Description', '接口说明')" prop="description" min-width="150" />
            <el-table-column :label="t('Method', '请求方法')" prop="method" width="100" align="center">
              <template #default="{ row }">
                <el-tag :type="getMethodTagType(row.method) as ElTagType" size="small">
                  {{ row.method }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column :label="t('API Path', '接口路径')" prop="path" min-width="200">
              <template #default="{ row }">
                <el-text class="cursor-pointer" @click="copy(row.path)" :title="clickToCopyTitle(row.path)">
                  {{ row.path }}
                </el-text>
              </template>
            </el-table-column>
            <el-table-column :label="t('Permission', '权限要求')" prop="permission" width="180">
              <template #default="{ row }">
                <el-tag v-if="row.noAuth" type="success" size="small">{{ t('No Auth', '无需权限') }}</el-tag>
                <div v-else-if="row.permission">
                  <el-tag size="small">{{ row.permission }}</el-tag>
                  <el-tag v-if="row.permissionMode === 'AND'" type="danger" size="small" class="ml-1">AND</el-tag>
                  <el-tag v-else-if="row.permissionMode === 'OR'" type="warning" size="small" class="ml-1">OR</el-tag>
                </div>
                <div v-else-if="row.roleCode">
                  <el-tag type="warning" size="small">{{ row.roleCode }}</el-tag>
                  <el-tag v-if="row.roleMode === 'AND'" type="danger" size="small" class="ml-1">AND</el-tag>
                  <el-tag v-else-if="row.roleMode === 'OR'" type="warning" size="small" class="ml-1">OR</el-tag>
                </div>
                <el-tag v-else type="info" size="small">{{ t('Public', '公开') }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column :label="t('操作')" width="100" align="center" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" @click="handleViewApiDetail(row)">{{ t('Detail', '详情') }}</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-collapse-item>
      </el-collapse>
    </AModal>

    <!-- 接口详情 -->
    <ADetail
      v-model="apiDetailDialog.visible"
      :title="apiDetailDialog.title"
      :data="currentApiData"
      :fields="apiDetailFields"
      mode="drawer"
      size="large"
      :column="2"
      :show-footer="false"
    >
      <!-- 权限要求插槽 -->
      <template #permission="{ data }">
        <div v-if="data?.noAuth">
          <el-tag type="success">{{ t('No Auth', '无需权限') }}</el-tag>
        </div>
        <div v-else-if="data?.permission">
          <el-tag>{{ data.permission }}</el-tag>
          <el-tag v-if="data.permissionMode === 'AND'" type="danger" class="ml-1">{{ t('AND Mode', 'AND模式') }}</el-tag>
          <el-tag v-else-if="data.permissionMode === 'OR'" type="warning" class="ml-1">{{ t('OR Mode', 'OR模式') }}</el-tag>
        </div>
        <div v-else-if="data?.roleCode">
          <el-tag type="warning">{{ data.roleCode }}</el-tag>
          <el-tag v-if="data.roleMode === 'AND'" type="danger" class="ml-1">{{ t('AND Mode', 'AND模式') }}</el-tag>
          <el-tag v-else-if="data.roleMode === 'OR'" type="warning" class="ml-1">{{ t('OR Mode', 'OR模式') }}</el-tag>
        </div>
        <el-tag v-else type="info">{{ t('Public', '公开') }}</el-tag>
      </template>

      <!-- 请求方法插槽 -->
      <template #method="{ value }">
        <el-tag :type="getMethodTagType(value) as ElTagType">{{ value }}</el-tag>
      </template>

      <!-- 响应说明 -->
      <template #beforeContent>
        <el-divider content-position="left">{{ t('Response Info', '响应信息') }}</el-divider>
        <el-descriptions :column="1" border class="mb-4">
          <el-descriptions-item :label="t('Full Response Type', '完整响应类型')">
            {{ currentApi?.responseInfo?.fullType || currentApi?.responseType || '-' }}
          </el-descriptions-item>
          <el-descriptions-item :label="t('Response Data Type', '响应数据类型')">
            {{ currentApi?.responseInfo?.dataType || '-' }}
          </el-descriptions-item>
        </el-descriptions>

        <!-- 响应体字段 -->
        <template v-if="currentApi?.responseInfo?.fields && currentApi.responseInfo.fields.length > 0">
          <el-divider content-position="left">{{ t('Response Fields', '响应体字段') }}</el-divider>
          <el-table :data="currentApi.responseInfo.fields" border stripe>
            <el-table-column :label="t('Field Name', '字段名')" prop="name" width="150" />
            <el-table-column :label="t('Type', '类型')" prop="type" width="120" />
            <el-table-column :label="t('Required', '必填')" prop="required" width="80" align="center">
              <template #default="{ row }">
                <el-tag v-if="row.required" type="danger" size="small">{{ t('Yes', '是') }}</el-tag>
                <el-tag v-else type="info" size="small">{{ t('No', '否') }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column :label="t('Description', '说明')" min-width="300">
              <template #default="{ row }">
                {{ row.description || '-' }}
              </template>
            </el-table-column>
            <el-table-column :label="t('Example', '示例值')" min-width="200">
              <template #default="{ row }">
                <div v-if="row.example" class="text-gray-600">{{ row.example }}</div>
                <span v-else class="text-gray-400">-</span>
              </template>
            </el-table-column>
          </el-table>
        </template>
      </template>

      <!-- 额外内容 -->
      <template #content>
        <!-- 参数位置说明 -->
        <el-divider content-position="left">{{ t('Param Location', '参数位置说明') }}</el-divider>
        <div class="mb-4 flex flex-wrap gap-4">
          <div class="flex items-center gap-2">
            <el-tag type="danger" size="small">PATH</el-tag>
            <span class="text-sm text-gray-600">{{ t('Path param in URL', '路径参数，拼接在URL中') }}</span>
          </div>
          <div class="flex items-center gap-2">
            <el-tag type="success" size="small">QUERY</el-tag>
            <span class="text-sm text-gray-600">{{ t('Query param after ?', '查询参数，跟在?后面') }}</span>
          </div>
          <div class="flex items-center gap-2">
            <el-tag type="warning" size="small">BODY</el-tag>
            <span class="text-sm text-gray-600">{{ t('Request body JSON', '请求体，JSON格式') }}</span>
          </div>
          <div class="flex items-center gap-2">
            <el-tag type="info" size="small">FORM</el-tag>
            <span class="text-sm text-gray-600">{{ t('Form multipart', '表单参数，multipart格式') }}</span>
          </div>
          <div class="flex items-center gap-2">
            <el-tag type="primary" size="small">HEADER</el-tag>
            <span class="text-sm text-gray-600">{{ t('HTTP header', '请求头，HTTP Header') }}</span>
          </div>
        </div>

        <!-- 请求参数 -->
        <el-divider content-position="left">{{ t('Request Params', '请求参数') }}</el-divider>
        <el-table v-if="currentApi?.parameters?.length > 0" :data="currentApi.parameters" border>
          <el-table-column :label="t('Param Name', '参数名')" prop="name" />
          <el-table-column :label="t('Location', '位置')" prop="location" align="center">
            <template #default="{ row }">
              <el-tag :type="getLocationTagType(row.location)" size="small">
                {{ row.location }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column :label="t('Type', '类型')" prop="type">
            <template #default="{ row }">
              <div class="flex items-center gap-2">
                <span>{{ row.type }}</span>
                <el-tag v-if="row.fields && row.fields.length > 0" type="info" size="small">{{ t('Object', '对象') }}</el-tag>
              </div>
            </template>
          </el-table-column>
          <el-table-column :label="t('Required', '必填')" prop="required" align="center">
            <template #default="{ row }">
              <el-tag v-if="row.required" type="danger" size="small">{{ t('Yes', '是') }}</el-tag>
              <el-tag v-else type="info" size="small">{{ t('No', '否') }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column :label="t('Description', '说明')" min-width="200">
            <template #default="{ row }">
              <div v-if="row.description" class="text-gray-500">{{ row.description }}</div>
              <div v-else-if="row.fields && row.fields.length > 0" class="relative">
                <el-button
                  class="absolute top-2 right-2 z-10"
                  size="small"
                  type="primary"
                  link
                  icon="CopyDocument"
                  :title="t('Copy', '复制内容')"
                  @click="copy(JSON.stringify(generateFieldsDescription(row.fields), null, 2))"
                  >{{ t('Copy', '复制') }}</el-button
                >
                <div class="scrollbar-y max-h-200px">
                  <VueJsonPretty :data="generateFieldsDescription(row.fields)" />
                </div>
              </div>
              <span v-else class="text-gray-400">-</span>
            </template>
          </el-table-column>
          <el-table-column :label="t('Example', '示例')" min-width="300">
            <template #default="{ row }">
              <div v-if="row.fields && row.fields.length > 0" class="relative">
                <el-button
                  class="absolute top-2 right-2 z-10"
                  size="small"
                  type="primary"
                  link
                  icon="CopyDocument"
                  :title="t('Copy', '复制内容')"
                  @click="copy(JSON.stringify(generateFieldsExample(row.fields), null, 2))"
                  >{{ t('Copy', '复制') }}</el-button
                >
                <div class="scrollbar-y max-h-200px">
                  <VueJsonPretty :data="generateFieldsExample(row.fields)" />
                </div>
              </div>
              <span v-else class="text-gray-400">-</span>
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-else :description="t('No params', '无参数')" :image-size="60" />

        <!-- 响应示例 -->
        <template v-if="currentApi?.responseInfo?.fields && currentApi.responseInfo.fields.length > 0">
          <el-divider content-position="left">{{ t('Response Example', '响应示例') }}</el-divider>
          <div class="relative mb-4">
            <el-button
              class="absolute top-2 right-2 z-10"
              size="small"
              type="primary"
              link
              icon="CopyDocument"
              :title="t('Copy', '复制内容')"
              @click="copy(generateResponseJsonWithComments(currentApi.responseInfo.fields))"
              >{{ t('Copy', '复制') }}</el-button
            >
            <div class="scrollbar-y max-h-300px border rounded p-3 bg-gray-50 whitespace-pre-wrap font-mono text-sm">
              {{ generateResponseJsonWithComments(currentApi.responseInfo.fields) }}
            </div>
          </div>
        </template>

        <!-- 调用示例 -->
        <el-divider content-position="left">{{ t('Call Example', '调用示例') }}</el-divider>
        <el-tabs>
          <el-tab-pane label="cURL">
            <div class="relative">
              <el-button
                class="absolute top-2 right-2 z-10"
                size="small"
                type="primary"
                link
                icon="CopyDocument"
                :title="t('Copy', '复制内容')"
                @click="copy(curlExample)"
                >{{ t('Copy', '复制') }}</el-button
              >
              <el-input v-model="curlExample" type="textarea" :rows="10" readonly />
            </div>
          </el-tab-pane>
          <el-tab-pane label="Java">
            <div class="relative">
              <el-button
                class="absolute top-2 right-2 z-10"
                size="small"
                type="primary"
                link
                icon="CopyDocument"
                :title="t('Copy', '复制内容')"
                @click="copy(javaExample)"
                >{{ t('Copy', '复制') }}</el-button
              >
              <el-input v-model="javaExample" type="textarea" :rows="18" readonly />
            </div>
          </el-tab-pane>
        </el-tabs>
      </template>
    </ADetail>
  </div>
</template>

<script setup lang="ts" name="ApiKeyManage">
import {
  listMyApiKeys,
  generateMyApiKey,
  deleteMyApiKey,
  resetMyApiKey,
  updateMyApiKeyWhiteIps,
  getMaxKeys,
  listMyOpenApis
} from '@/api/system/core/user/profile/profileOpenApiApi'
import type { SysApiKeyBo, SysApiKeyVo, OpenApiSecretVo, OpenApiInfoVo, FieldInfo, ResponseInfo } from '@/api/system/openApi/openApiTypes'
import { ParameterLocation } from '@/api/system/openApi/openApiTypes'
import { toValidate } from '@/utils/to'
import { showMsgSuccess, showConfirm } from '@/utils/modal'
import { copy } from '@/utils/function'
import { SystemConfig } from '@/systemConfig'
import VueJsonPretty from 'vue-json-pretty'
import 'vue-json-pretty/lib/styles.css'

const { t, isChinese } = useI18n()

/** 点击复制提示文本 */
const clickToCopyTitle = (text: string) => {
  return isChinese.value ? `点击复制: ${text}` : `Click to copy: ${text}`
}
const { sys_enable_status } = useDict(DictTypes.sys_enable_status)

// =========== 数据列表 ===========
const apiKeys = ref<SysApiKeyVo[]>([])
const maxKeys = ref(5) // 最大密钥数量

// =========== 生成密钥表单 ===========
const generateFormRef = ref<ElFormInstance>()
const buttonLoading = ref(false)
const generateDialog = ref<DialogState>({
  visible: false,
  title: ''
})
const generateForm = ref<SysApiKeyBo>({
  appName: undefined,
  expireTime: undefined,
  status: '1',
  whiteIps: undefined,
  remark: undefined
})
const generateRules = computed<ElFormRules>(() => ({
  appName: [{ required: true, message: t('App name required', '应用名称不能为空'), trigger: 'blur' }]
}))

// =========== 密钥详情对话框 ===========
const secretDialog = ref<DialogState>({
  visible: false,
  title: ''
})
const secretData = ref<OpenApiSecretVo>({
  id: 0,
  appName: '',
  appKey: '',
  appSecret: '',
  tips: ''
})

// =========== 编辑白名单 ===========
const editWhiteIpsFormRef = ref<ElFormInstance>()
const editWhiteIpsDialog = ref<DialogState>({
  visible: false,
  title: ''
})
const editWhiteIpsForm = ref<{ id?: number | string; whiteIps?: string }>({
  id: undefined,
  whiteIps: undefined
})

// =========== 查看详情 ===========
const viewDialog = ref<DialogState>({
  visible: false,
  title: ''
})
const viewData = ref<SysApiKeyVo>({} as SysApiKeyVo)

// =========== 开放接口列表 ===========
const openApiDialog = ref<DialogState>({
  visible: false,
  title: ''
})
const openApis = ref<OpenApiInfoVo[]>([])
const searchText = ref('')
const activeModules = ref<string[]>([])

// =========== 接口详情抽屉 ===========
const apiDetailDialog = ref<DialogState>({
  visible: false,
  title: ''
})
const currentApi = ref<OpenApiInfoVo | null>(null)

// ADetail 字段配置
const apiDetailFields = computed<FieldConfig[]>(() => [
  { prop: 'description', label: t('Description', '接口说明') },
  { prop: 'method', label: t('Method', '请求方法'), slot: 'method' },
  { prop: 'path', label: t('Path', '接口路径'), type: 'copyable' },
  { prop: 'module', label: t('Module', '所属模块') },
  { prop: 'className', label: 'Controller' },
  { prop: 'methodName', label: t('Method Name', '方法名') },
  { prop: 'permission', label: t('Permission', '权限要求'), slot: 'permission' },
  { prop: 'responseType', label: t('Response Type', '响应类型') }
])

// 当前API数据(处理null值)
const currentApiData = computed(() => currentApi.value || {})

// =========== 计算属性 ===========
/** 过滤后的接口列表 */
const filteredOpenApis = computed(() => {
  if (!searchText.value) return openApis.value
  const keyword = searchText.value.toLowerCase()
  return openApis.value.filter((api) => api.path.toLowerCase().includes(keyword) || api.description.toLowerCase().includes(keyword))
})

/** 按模块分组的接口 */
const groupedOpenApis = computed(() => {
  const groups: Record<string, OpenApiInfoVo[]> = {}
  filteredOpenApis.value.forEach((api) => {
    if (!groups[api.module]) {
      groups[api.module] = []
    }
    groups[api.module].push(api)
  })
  return groups
})

/** 获取完整的API URL */
const getApiUrl = () => {
  if (!currentApi.value) return ''
  const { path } = currentApi.value

  // 开发环境：使用 http://127.0.0.1:端口/path
  if (SystemConfig.app.env === 'development') {
    const port = SystemConfig.api.port
    return `http://127.0.0.1:${port}${path}`
  }

  // 生产环境：使用浏览器地址栏的域名 + baseUrl + path
  const protocol = window.location.protocol
  const host = window.location.host
  const baseUrl = SystemConfig.api.baseUrl
  return `${protocol}//${host}${baseUrl}${path}`
}

/** cURL 调用示例 */
const curlExample = computed(() => {
  if (!currentApi.value) return ''
  const { method } = currentApi.value
  const url = getApiUrl()
  return `curl -X ${method} "${url}" \\
  -H "Content-Type: application/json" \\
  -H "X-App-Key: your_app_key" \\
  -H "X-Timestamp: \${timestamp}" \\
  -H "X-Sign: \${sign}"${method === 'POST' || method === 'PUT' ? ' \\\n  -d \'{"key": "value"}\'' : ''}`
})

/** Java 调用示例 */
const javaExample = computed(() => {
  if (!currentApi.value) return ''
  const { method } = currentApi.value
  const httpMethod = method.toLowerCase()
  const url = getApiUrl()

  const comment1 = isChinese.value ? '// 1. 生成签名' : '// 1. Generate signature'
  const comment2 = isChinese.value ? '// 2. 发送请求' : '// 2. Send request'
  const comment3 = isChinese.value ? '// 3. 输出结果' : '// 3. Print result'
  const statusLabel = isChinese.value ? '响应状态码' : 'Status code'
  const bodyLabel = isChinese.value ? '响应内容' : 'Response body'

  return `${comment1}
String appKey = "your_app_key";
String appSecret = "your_app_secret";
long timestamp = System.currentTimeMillis();
String sign = DigestUtil.md5Hex(appKey + timestamp + appSecret);

${comment2}
HttpResponse response = HttpRequest.${httpMethod}("${url}")
    .header("Content-Type", "application/json")
    .header("X-App-Key", appKey)
    .header("X-Timestamp", String.valueOf(timestamp))
    .header("X-Sign", sign)${method === 'POST' || method === 'PUT' ? '\n    .body("{\\"key\\": \\"value\\"}")' : ''}
    .execute();

${comment3}
System.out.println("${statusLabel}: " + response.getStatus());
System.out.println("${bodyLabel}: " + response.body());`
})

// =========== 方法 ===========
/** 加载密钥列表 */
const loadApiKeys = async () => {
  const [err, data] = await listMyApiKeys()
  if (!err) {
    apiKeys.value = data
  }
}

/** 加载最大密钥数量配置 */
const loadMaxKeys = async () => {
  const [err, data] = await getMaxKeys()
  if (!err) {
    maxKeys.value = data
  }
}

/** 生成密钥 */
const handleGenerate = () => {
  generateForm.value = {
    appName: undefined,
    expireTime: undefined,
    status: '1',
    whiteIps: undefined,
    remark: undefined
  }
  generateDialog.value.title = t('Generate API Key', '生成API密钥')
  generateDialog.value.visible = true
}

/** 提交生成 */
const submitGenerate = async () => {
  const [validateErr] = await toValidate(generateFormRef)
  if (validateErr) return

  buttonLoading.value = true
  const [err, data] = await generateMyApiKey(generateForm.value)
  if (!err) {
    showMsgSuccess(t('message.generateSuccess', '生成成功'))
    generateDialog.value.visible = false

    // 显示密钥详情
    secretData.value = data
    secretDialog.value.title = t('Secret Details', '密钥详情(请妥善保管)')
    secretDialog.value.visible = true

    await loadApiKeys()
  }
  buttonLoading.value = false
}

/** 取消生成 */
const cancelGenerate = () => {
  generateDialog.value.visible = false
}

/** 编辑白名单 */
const handleEditWhiteIps = (row: SysApiKeyVo) => {
  editWhiteIpsForm.value = {
    id: row.id,
    whiteIps: row.whiteIps
  }
  editWhiteIpsDialog.value.title = isChinese.value ? `编辑IP白名单 - ${row.appName}` : `Edit IP Whitelist - ${row.appName}`
  editWhiteIpsDialog.value.visible = true
}

/** 提交编辑白名单 */
const submitEditWhiteIps = async () => {
  buttonLoading.value = true
  const [err] = await updateMyApiKeyWhiteIps(editWhiteIpsForm.value.id!, {
    whiteIps: editWhiteIpsForm.value.whiteIps
  } as SysApiKeyBo)
  if (!err) {
    showMsgSuccess(t('message.updateSuccess'))
    editWhiteIpsDialog.value.visible = false
    await loadApiKeys()
  }
  buttonLoading.value = false
}

/** 取消编辑白名单 */
const cancelEditWhiteIps = () => {
  editWhiteIpsDialog.value.visible = false
}

/** 查看详情 */
const handleView = (row: SysApiKeyVo) => {
  viewData.value = row
  viewDialog.value.title = t('View API Key', '查看API密钥')
  viewDialog.value.visible = true
}

/** 重置密钥 */
const handleResetSecret = async (row: SysApiKeyVo) => {
  const confirmMsg = isChinese.value ? `是否确认重置${row.appName}的密钥?` : `Are you sure to reset the secret of ${row.appName}?`
  const [confirmErr] = await showConfirm(confirmMsg)
  if (confirmErr) return

  const [err, data] = await resetMyApiKey(row.id)
  if (!err) {
    showMsgSuccess(t('message.resetSuccess', '重置成功'))

    // 显示新密钥
    secretData.value = data
    secretDialog.value.visible = true
    secretDialog.value.title = t('New Secret Details', '新密钥详情(请妥善保管)')

    await loadApiKeys()
  }
}

/** 删除密钥 */
const handleDelete = async (row: SysApiKeyVo) => {
  const confirmMsg = isChinese.value ? `是否确认删除${row.appName}?` : `Are you sure to delete ${row.appName}?`
  const [confirmErr] = await showConfirm(confirmMsg)
  if (confirmErr) return

  const [err] = await deleteMyApiKey(row.id)
  if (!err) {
    showMsgSuccess(t('message.deleteSuccess'))
    await loadApiKeys()
  }
}

/** 查看开放接口列表 */
const handleViewOpenApis = async () => {
  const [err, data] = await listMyOpenApis()
  if (!err) {
    openApis.value = data
    // 默认展开所有模块
    activeModules.value = Object.keys(groupedOpenApis.value)
    openApiDialog.value.title = isChinese.value ? `可用开放接口列表 (${data.length})` : `Available APIs (${data.length})`
    openApiDialog.value.visible = true
  }
}

/** 查看接口详情 */
const handleViewApiDetail = (api: OpenApiInfoVo) => {
  currentApi.value = api
  apiDetailDialog.value.title = isChinese.value ? `接口详情 - ${api.description}` : `API Details - ${api.description}`
  apiDetailDialog.value.visible = true
}

/** 获取请求方法的标签类型 */
const getMethodTagType = (method: string) => {
  const typeMap: Record<string, string> = {
    GET: '',
    POST: 'success',
    PUT: 'warning',
    DELETE: 'danger',
    PATCH: 'info'
  }
  return typeMap[method] || 'info'
}

/** 获取参数位置的标签类型 */
const getLocationTagType = (location: string) => {
  const typeMap: Record<string, string> = {
    PATH: 'danger',
    QUERY: 'success',
    BODY: 'warning',
    FORM: 'info',
    HEADER: 'primary',
    UNDEFINED: ''
  }
  return (typeMap[location] || 'info') as ElTagType
}

/** 根据字段列表生成示例JSON */
const generateFieldsExample = (fields: FieldInfo[]) => {
  const example: Record<string, any> = {}
  fields.forEach((field) => {
    // 使用示例值,如果没有则根据类型生成默认值
    if (field.example) {
      // 根据类型转换示例值
      if (field.type === 'Integer' || field.type === 'Long' || field.type === 'int' || field.type === 'long') {
        example[field.name] = parseInt(field.example) || 0
      } else if (field.type === 'Double' || field.type === 'Float' || field.type === 'double' || field.type === 'float') {
        example[field.name] = parseFloat(field.example) || 0.0
      } else if (field.type === 'Boolean' || field.type === 'boolean') {
        example[field.name] = field.example === 'true'
      } else if (field.type === 'BigDecimal') {
        example[field.name] = field.example
      } else {
        example[field.name] = field.example
      }
    } else {
      // 没有示例值时使用类型默认值
      if (field.type === 'String') {
        example[field.name] = ''
      } else if (field.type === 'Integer' || field.type === 'Long' || field.type === 'int' || field.type === 'long') {
        example[field.name] = 0
      } else if (field.type === 'Double' || field.type === 'Float' || field.type === 'double' || field.type === 'float') {
        example[field.name] = 0.0
      } else if (field.type === 'Boolean' || field.type === 'boolean') {
        example[field.name] = false
      } else if (field.type === 'BigDecimal') {
        example[field.name] = '0.00'
      } else {
        example[field.name] = null
      }
    }
  })
  return example
}

/** 根据字段列表生成说明JSON */
const generateFieldsDescription = (fields: FieldInfo[]) => {
  const description: Record<string, any> = {}
  const typeLabel = isChinese.value ? '类型' : 'type'
  const requiredLabel = isChinese.value ? '必填' : 'required'
  const descLabel = isChinese.value ? '说明' : 'description'
  const yesText = isChinese.value ? '是' : 'yes'
  const noText = isChinese.value ? '否' : 'no'

  fields.forEach((field) => {
    const info: any = {
      [typeLabel]: field.type,
      [requiredLabel]: field.required ? yesText : noText
    }
    if (field.description) {
      info[descLabel] = field.description
    }
    description[field.name] = info
  })
  return description
}

/** 根据响应体字段列表生成示例JSON */
const generateResponseExample = (fields: FieldInfo[]) => {
  const example: Record<string, any> = {}
  fields.forEach((field) => {
    // 使用示例值,如果没有则根据类型生成默认值
    if (field.example) {
      // 根据类型转换示例值
      if (field.type === 'Integer' || field.type === 'Long' || field.type === 'int' || field.type === 'long') {
        example[field.name] = parseInt(field.example) || 0
      } else if (field.type === 'Double' || field.type === 'Float' || field.type === 'double' || field.type === 'float') {
        example[field.name] = parseFloat(field.example) || 0.0
      } else if (field.type === 'Boolean' || field.type === 'boolean') {
        example[field.name] = field.example === 'true'
      } else if (field.type === 'BigDecimal') {
        example[field.name] = field.example
      } else {
        example[field.name] = field.example
      }
    } else {
      // 没有示例值时使用类型默认值
      if (field.type === 'String') {
        example[field.name] = ''
      } else if (field.type === 'Integer' || field.type === 'Long' || field.type === 'int' || field.type === 'long') {
        example[field.name] = 0
      } else if (field.type === 'Double' || field.type === 'Float' || field.type === 'double' || field.type === 'float') {
        example[field.name] = 0.0
      } else if (field.type === 'Boolean' || field.type === 'boolean') {
        example[field.name] = false
      } else if (field.type === 'BigDecimal') {
        example[field.name] = '0.00'
      } else {
        example[field.name] = null
      }
    }
  })
  return example
}

/** 生成带注释的响应JSON示例（所有值转换为字符串，包含类型和说明） */
const generateResponseJsonWithComments = (fields: FieldInfo[]) => {
  const lines: string[] = ['{']

  fields.forEach((field, index) => {
    // 获取示例值并转换为字符串
    let exampleValue: string
    if (field.example) {
      exampleValue = field.example.toString()
    } else {
      // 没有示例值时使用类型默认值
      if (field.type === 'String') {
        exampleValue = ''
      } else if (field.type === 'Integer' || field.type === 'Long' || field.type === 'int' || field.type === 'long') {
        exampleValue = '0'
      } else if (field.type === 'Double' || field.type === 'Float' || field.type === 'double' || field.type === 'float') {
        exampleValue = '0.0'
      } else if (field.type === 'Boolean' || field.type === 'boolean') {
        exampleValue = 'false'
      } else if (field.type === 'BigDecimal') {
        exampleValue = '0.00'
      } else if (field.type === 'Date' || field.type === 'LocalDateTime') {
        exampleValue = '2025-01-01 12:00:00'
      } else {
        exampleValue = 'null'
      }
    }

    // 组合类型和说明信息
    const typeInfo = field.type
    const description = field.description ? ` - ${field.description}` : ''
    const commentInfo = `(${typeInfo}${description})`

    // 生成JSON行 - 值为字符串形式，包含类型和说明的注释
    const isLast = index === fields.length - 1
    const comma = isLast ? '' : ','
    lines.push(`  "${field.name}": "${exampleValue}"${comma} ${commentInfo}`)
  })

  lines.push('}')
  return lines.join('\n')
}

// =========== 生命周期 ===========
onMounted(() => {
  loadMaxKeys()
  loadApiKeys()
})
</script>

<style scoped lang="scss">
.module-title {
  display: flex;
  align-items: center;
}

.open-api-collapse {
  :deep(.el-collapse-item__content) {
    padding: 0 20px 20px;
  }
}
</style>
