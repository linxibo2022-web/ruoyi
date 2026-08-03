<!-- 用户头像 -->
<template>
  <div class="user-info-head" @click="editCropper()">
    <img :src="options.img" :title="t('Click to upload', '点击上传头像')" class="rounded-full w-30 h-30" />
    <AModal v-model="open" :title="title" mode="dialog" size="large" @opened="modalOpened" @close="closeDialog">
      <el-row>
        <el-col :xs="24" :md="12" :style="{ height: '350px' }">
          <vue-cropper
            v-if="visible"
            ref="cropper"
            :img="options.img"
            :info="true"
            :auto-crop="options.autoCrop"
            :auto-crop-width="options.autoCropWidth"
            :auto-crop-height="options.autoCropHeight"
            :fixed-box="options.fixedBox"
            :output-type="options.outputType"
            @real-time="realTime"
          />
        </el-col>
        <el-col :xs="24" :md="12" :style="{ height: '350px' }">
          <div class="avatar-upload-preview">
            <img :src="options.previews.url" :style="options.previews.img" />
          </div>
        </el-col>
      </el-row>
      <br />
      <el-row>
        <el-col :lg="2" :md="2">
          <el-upload action="#" :http-request="requestUpload" :show-file-list="false" :before-upload="beforeUpload">
            <el-button>
              {{ t('Select', '选择') }}
              <el-icon class="el-icon--right">
                <Upload />
              </el-icon>
            </el-button>
          </el-upload>
        </el-col>
        <el-col :lg="{ span: 1, offset: 2 }" :md="2">
          <el-button icon="Plus" @click="changeScale(1)"></el-button>
        </el-col>
        <el-col :lg="{ span: 1, offset: 1 }" :md="2">
          <el-button icon="Minus" @click="changeScale(-1)"></el-button>
        </el-col>
        <el-col :lg="{ span: 1, offset: 1 }" :md="2">
          <el-button icon="RefreshLeft" @click="rotateLeft()"></el-button>
        </el-col>
        <el-col :lg="{ span: 1, offset: 1 }" :md="2">
          <el-button icon="RefreshRight" @click="rotateRight()"></el-button>
        </el-col>
        <el-col :lg="{ span: 2, offset: 6 }" :md="2">
          <el-button type="primary" @click="uploadImg()">{{ t('button.submit') }}</el-button>
        </el-col>
      </el-row>

      <template #footer>
        <el-button @click="closeDialog">{{ t('button.cancel') }}</el-button>
        <el-button type="primary" @click="uploadImg()">{{ t('button.submit') }}</el-button>
      </template>
    </AModal>
  </div>
</template>

<script setup lang="ts" name="UserAvatar">
import 'vue-cropper/dist/index.css'
import { VueCropper } from 'vue-cropper'
import { uploadAvatar } from '@/api/system/core/user/userApi'
import type { UploadRawFile } from 'element-plus'
import { showMsgSuccess, showMsgError } from '@/utils/modal'
const { t } = useI18n()

const userStore = useUserStore()

// =========== 数据定义 ===========
/**裁剪器配置选项接口*/
interface Options {
  img: string | any // 裁剪图片的地址
  autoCrop: boolean // 是否默认生成截图框
  autoCropWidth: number // 默认生成截图框宽度
  autoCropHeight: number // 默认生成截图框高度
  fixedBox: boolean // 固定截图框大小 不允许改变
  fileName: string // 上传文件名
  previews: any // 预览数据
  outputType: string // 输出类型
  loadedFromUrl: boolean // 标记是否从远程URL加载
  originalImg: string // 保存原始头像URL，以便重置
}

/**对话框显示状态*/
const open = ref(false)
/**裁剪器显示状态*/
const visible = ref(false)
/**对话框标题*/
const title = computed(() => t('Edit Avatar', '修改头像'))
/**图片加载状态*/
const loadingImage = ref(false)
/**裁剪器引用*/
const cropper = ref<any>(null)

/**图片裁剪配置数据*/
const options = reactive<Options>({
  img: userStore.userInfo?.avatar,
  autoCrop: true,
  autoCropWidth: 200,
  autoCropHeight: 200,
  fixedBox: true,
  outputType: 'png',
  fileName: 'avatar.png',
  previews: {},
  loadedFromUrl: true,
  originalImg: userStore.userInfo?.avatar
})

// =========== 初始化 ===========
onMounted(() => {
  // 确保组件初始化时保存原始头像URL
  options.originalImg = userStore.userInfo?.avatar
})

// =========== 图片预处理方法 ===========
/**
 * 预加载远程图片并转换为base64
 * @param url 图片URL
 * @returns Promise<string> base64格式的图片数据
 */
const preloadRemoteImage = (url: string): Promise<string> => {
  return new Promise((resolve, reject) => {
    if (!url || url.startsWith('data:')) {
      resolve(url)
      return
    }

    loadingImage.value = true

    const img = new Image()
    img.crossOrigin = 'anonymous'
    img.onload = () => {
      // 将远程图片转换为base64以便裁剪组件处理
      const canvas = document.createElement('canvas')
      canvas.width = img.width
      canvas.height = img.height
      const ctx = canvas.getContext('2d')
      if (ctx) {
        ctx.drawImage(img, 0, 0)
        const dataURL = canvas.toDataURL('image/png')
        loadingImage.value = false
        resolve(dataURL)
      } else {
        loadingImage.value = false
        reject(new Error('创建画布上下文失败'))
      }
    }

    img.onerror = () => {
      loadingImage.value = false
      reject(new Error('加载图片失败'))
    }

    // 添加时间戳以防止缓存
    img.src = url + (url.includes('?') ? '&' : '?') + 'timestamp=' + new Date().getTime()
  })
}

/**
 * 上传文件预处理
 * @param file 上传的文件
 */
const beforeUpload = (file: UploadRawFile): any => {
  if (file.type.indexOf('image/') == -1) {
    showMsgError(t('Invalid image format', '文件格式错误，请上传图片类型,如：JPG，PNG后缀的文件。'))
  } else {
    const reader = new FileReader()
    reader.readAsDataURL(file)
    reader.onload = () => {
      options.img = reader.result
      options.fileName = file.name
      options.loadedFromUrl = false // 标记为本地文件上传
      // 确保图片加载后重新初始化裁剪器
      if (visible.value && cropper.value) {
        cropper.value.replace(reader.result as string)
      }
    }
  }
}

// =========== 对话框操作 ===========
/**编辑头像 - 打开裁剪对话框*/
const editCropper = async () => {
  try {
    // 当打开编辑器时，预加载远程图片
    if (options.loadedFromUrl && options.img && typeof options.img === 'string' && !options.img.startsWith('data:')) {
      options.img = await preloadRemoteImage(options.img)
    }
    open.value = true
  } catch (error) {
    showMsgError(t('Load avatar failed', '加载头像失败，已清除远程头像，请重新上传'))
    options.img = '' // 清除头像
    options.loadedFromUrl = false // 标记为本地文件上传
    options.originalImg = '' // 清除原始头像URL
    userStore.updateAvatar('') // 更新用户头像为空
    open.value = true // 打开裁剪器
    console.error('加载头像图片失败:', error)
  }
}

/**打开弹出层结束时的回调*/
const modalOpened = () => {
  visible.value = true
}

/**关闭对话框*/
const closeDialog = () => {
  open.value = false // 关闭弹窗
  options.img = options.originalImg // 重置为原始图像
  visible.value = false // 关闭裁剪器
}

// =========== 裁剪器操作 ===========
/**实时预览回调*/
const realTime = (data: any) => {
  options.previews = data
}

/**向左旋转*/
const rotateLeft = () => {
  if (cropper.value) {
    cropper.value.rotateLeft()
  }
}

/**向右旋转*/
const rotateRight = () => {
  if (cropper.value) {
    cropper.value.rotateRight()
  }
}

/**
 * 图片缩放
 * @param num 缩放比例，正数放大，负数缩小
 */
const changeScale = (num: number) => {
  num = num || 1
  if (cropper.value) {
    cropper.value.changeScale(num)
  }
}

// =========== 上传操作 ===========
/**覆盖默认上传行为 - 阻止自动上传*/
const requestUpload = (): any => {}

/**上传裁剪后的图片*/
const uploadImg = async () => {
  if (!cropper.value) {
    showMsgError(t('Cropper not ready', '裁剪组件未初始化，请重试'))
    return
  }

  cropper.value.getCropBlob(async (data: any) => {
    const formData = new FormData()
    formData.append('avatarfile', data, options.fileName)

    const [err, avatarData] = await uploadAvatar(formData)
    if (err) {
      showMsgError(t('Upload failed', '上传失败，请重试'))
      console.error('头像上传失败:', err)
      return
    }
    open.value = false
    options.img = avatarData.imgUrl
    options.originalImg = avatarData.imgUrl // 更新原始图像
    options.loadedFromUrl = true // 重新标记为URL图像
    userStore.updateAvatar(options.img)
    showMsgSuccess(t('message.updateSuccess'))
    visible.value = false
  })

  // =========== 初始化 ===========
  onMounted(() => {
    // 确保组件初始化时保存原始头像URL
    options.originalImg = userStore.userInfo?.avatar
  })
}
</script>

<style lang="scss" scoped>
.user-info-head {
  position: relative;
  display: inline-block;
  height: 120px;
}

.user-info-head:hover:after {
  content: '+';
  position: absolute;
  left: 0;
  right: 0;
  top: 0;
  bottom: 0;
  color: #eee;
  background: rgba(0, 0, 0, 0.5);
  font-size: 24px;
  font-style: normal;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  cursor: pointer;
  line-height: 110px;
  border-radius: 50%;
}

.avatar-upload-preview {
  position: absolute;
  top: 50%;
  transform: translate(50%, -50%);
  width: 200px;
  height: 200px;
  border-radius: 50%;
  box-shadow: 0 0 4px #ccc;
  overflow: hidden;
}
</style>
