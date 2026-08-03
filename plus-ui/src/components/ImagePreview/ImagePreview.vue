<!--
图片预览组件

单张图片（URL格式）
<ImagePreview src="https://example.com/image.jpg" />

单张图片（ossId格式）
<ImagePreview src="123" />

多张图片预览（只显示第一张，点击预览所有）
<ImagePreview src="https://example.com/1.jpg,https://example.com/2.jpg,https://example.com/3.jpg" />

多张图片预览（ossId格式）
<ImagePreview src="123,456,789" />

多张图片全部显示
<ImagePreview src="https://example.com/1.jpg,https://example.com/2.jpg,https://example.com/3.jpg" :show-all="true" />

多张图片全部显示（ossId格式）
<ImagePreview src="123,456,789" :show-all="true" />

自定义布局
<ImagePreview src="https://example.com/1.jpg,https://example.com/2.jpg,https://example.com/3.jpg" :show-all="true" layout="grid" :columns="3" />

限制显示数量
<ImagePreview src="https://example.com/1.jpg,https://example.com/2.jpg,https://example.com/3.jpg,https://example.com/4.jpg" :show-all="true" :max-show="3" />
-->
<template>
  <div class="image-preview-container">
    <!-- 加载状态 -->
    <div v-if="isLoading" class="loading-container" :style="imageStyle">
      <el-icon class="loading-icon">
        <loading />
      </el-icon>
      <span class="loading-text">{{ t('filePreview.loading') }}</span>
    </div>

    <!-- 单张图片或多张图片只显示第一张 -->
    <div v-else-if="!showAll || realSrcList.length <= 1" class="single-image">
      <el-image
        v-if="realSrc"
        :src="realSrc"
        fit="cover"
        :style="imageStyle"
        :preview-src-list="realSrcList"
        hide-on-click-modal
        preview-teleported
        @error="handleImageError"
      >
        <template #error>
          <div class="image-slot">
            <el-icon>
              <picture-filled />
            </el-icon>
          </div>
        </template>
      </el-image>
      <!-- 显示图片数量提示 -->
      <div v-if="realSrcList.length > 1" class="image-count">+{{ realSrcList.length - 1 }}</div>
    </div>

    <!-- 多张图片全部显示 -->
    <div v-else class="multiple-images" :class="layoutClass">
      <div v-for="(src, index) in displayImages" :key="index" class="image-item" :style="imageItemStyle">
        <el-image
          :src="src"
          fit="cover"
          :style="imageStyle"
          :preview-src-list="realSrcList"
          :initial-index="index"
          hide-on-click-modal
          preview-teleported
          @error="handleImageError"
        >
          <template #error>
            <div class="image-slot">
              <el-icon>
                <picture-filled />
              </el-icon>
            </div>
          </template>
        </el-image>
      </div>
      <!-- 显示更多图片的提示 -->
      <div v-if="hasMoreImages" class="more-images" :style="imageStyle" @click="openPreview">
        <div class="more-text">+{{ realSrcList.length - maxShow }}</div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts" name="ImagePreview">
import { computed, ref, watch } from 'vue'
import { PictureFilled, Loading } from '@element-plus/icons-vue'

const { t } = useI18n()
import { listOssByIds } from '@/api/system/oss/oss/ossApi'
import type { SysOssVo } from '@/api/system/oss/oss/ossTypes'
import { addCacheBuster } from '@/utils/function'

/**
 * 自定义图片组件的属性接口
 */
interface ImagePreviewProps {
  /**
   * 图片源地址
   * 支持单个图片地址或多个图片地址（逗号分隔）
   * @default ''
   */
  src?: string

  /**
   * 图片宽度
   * 可以是数字（默认为像素）或字符串（可包含单位）
   * @default 60
   */
  width?: number | string

  /**
   * 图片高度
   * 可以是数字（默认为像素）或字符串（可包含单位）
   * @default 60
   */
  height?: number | string

  /**
   * 是否启用懒加载
   * @default true
   */
  lazy?: boolean

  /**
   * 是否显示悬停效果
   * @default true
   */
  hoverEffect?: boolean

  /**
   * 是否显示所有图片
   * @default false
   */
  showAll?: boolean

  /**
   * 布局方式
   * @default 'flex'
   */
  layout?: 'flex' | 'grid'

  /**
   * 网格布局列数（仅在layout为grid时生效）
   * @default 3
   */
  columns?: number

  /**
   * 最大显示图片数量
   * @default 9
   */
  maxShow?: number

  /**
   * 图片间距
   * @default 4
   */
  gap?: number
}

// 使用 withDefaults 定义 props
const props = withDefaults(defineProps<ImagePreviewProps>(), {
  src: '',
  width: 60,
  height: 60,
  lazy: true,
  hoverEffect: true,
  showAll: false,
  layout: 'flex',
  columns: 3,
  maxShow: 9,
  gap: 4
})

// 定义 emits
const emits = defineEmits<{
  error: [event: Event]
}>()

// 响应式状态
const resolvedUrls = ref<string[]>([])
const isLoading = ref(false)
const ossDataMap = ref<Map<string, SysOssVo>>(new Map())

/**
 * 处理单位转换的通用函数
 */
const formatSize = (size: number | string): string => {
  if (size === '' || size === 0) return ''
  return typeof size === 'string' ? size : `${size}px`
}

/**
 * 解析图片地址列表
 */
const parseImageList = (src: string): string[] => {
  return src
    .split(',')
    .map((item) => item.trim())
    .filter(Boolean)
}

/**
 * 判断是否为ossId格式
 */
const isOssIdFormat = (src: string): boolean => {
  if (!src) return false
  const values = parseImageList(src)
  return values.length > 0 && values.every((value) => /^\d+$/.test(value))
}

/**
 * 解析ossId并获取URL（保持字符串格式，避免雪花ID精度丢失）
 */
const resolveOssIds = async (ossIds: string[]): Promise<void> => {
  isLoading.value = true

  const [err, data] = await listOssByIds(ossIds)
  if (!err && data) {
    // 更新映射表（ossId保持字符串格式）
    data.forEach((ossItem) => {
      ossDataMap.value.set(String(ossItem.ossId), ossItem)
    })

    // 按原始顺序生成URL列表，并为公开库URL添加缓存破坏参数
    resolvedUrls.value = ossIds
      .map((id) => {
        const ossItem = ossDataMap.value.get(id)
        if (!ossItem?.url) return ''
        // 使用文件更新时间作为缓存破坏参数，确保替换后能刷新预览
        return addCacheBuster(ossItem.url, ossItem.updateTime)
      })
      .filter(Boolean)
  } else {
    console.error('获取OSS文件信息失败:', err)
    resolvedUrls.value = []
  }
  isLoading.value = false
}

/**
 * 智能处理图片源（自动识别URL还是ossId）
 */
const processImageSource = async (): Promise<void> => {
  if (!props.src) {
    resolvedUrls.value = []
    return
  }

  // 智能识别：如果是纯数字格式，当作ossId处理
  if (isOssIdFormat(props.src)) {
    const ossIds = parseImageList(props.src)
    await resolveOssIds(ossIds)
  } else {
    // 否则直接当作URL处理
    resolvedUrls.value = parseImageList(props.src)
  }
}

/**
 * 计算实际图片源地址
 */
const realSrc = computed(() => {
  return resolvedUrls.value[0] || ''
})

/**
 * 计算预览图片源地址列表
 */
const realSrcList = computed(() => {
  return resolvedUrls.value
})

/**
 * 计算要显示的图片列表
 */
const displayImages = computed(() => {
  if (!props.showAll) return realSrcList.value.slice(0, 1)
  return realSrcList.value.slice(0, props.maxShow)
})

/**
 * 是否有更多图片
 */
const hasMoreImages = computed(() => {
  return props.showAll && realSrcList.value.length > props.maxShow
})

/**
 * 计算图片样式
 */
const imageStyle = computed(() => {
  const width = formatSize(props.width)
  const height = formatSize(props.height)

  return {
    width,
    height,
    ...(props.hoverEffect && {
      '--hover-scale': '1.05'
    })
  }
})

/**
 * 计算图片项样式
 */
const imageItemStyle = computed(() => {
  if (props.layout === 'grid') {
    return {
      flex: `0 0 calc((100% - ${(props.columns - 1) * props.gap}px) / ${props.columns})`
    }
  }
  return {}
})

/**
 * 计算布局类名
 */
const layoutClass = computed(() => {
  return `layout-${props.layout}`
})

/**
 * 处理图片加载错误
 */
const handleImageError = (event: Event) => {
  emits('error', event)
}

/**
 * 打开预览
 */
const openPreview = () => {
  // 这里可以通过编程方式打开预览
  // 暂时使用点击第一张图片的方式
  const firstImage = document.querySelector('.image-item .el-image') as HTMLElement
  if (firstImage) {
    firstImage.click()
  }
}

// 监听props变化，重新处理图片源
watch(
  () => props.src,
  () => {
    processImageSource()
  },
  { immediate: true }
)
</script>

<style lang="scss" scoped>
.image-preview-container {
  .single-image {
    position: relative;
    display: inline-block;

    .el-image {
      border-radius: 5px;
      background-color: #ebeef5;
      box-shadow: 0 0 5px 1px #ccc;

      :deep(.el-image__inner) {
        transition: transform 0.3s ease;
        cursor: pointer;

        &:hover {
          transform: scale(var(--hover-scale, 1));
        }
      }
    }

    .image-count {
      position: absolute;
      top: 4px;
      right: 4px;
      background: rgba(0, 0, 0, 0.6);
      color: white;
      padding: 2px 6px;
      border-radius: 10px;
      font-size: 12px;
      line-height: 1;
    }
  }

  .multiple-images {
    display: flex;
    gap: v-bind('props.gap + "px"');

    &.layout-flex {
      flex-wrap: wrap;
    }

    &.layout-grid {
      display: grid;
      grid-template-columns: repeat(v-bind('props.columns'), 1fr);
    }

    .image-item {
      .el-image {
        border-radius: 5px;
        background-color: #ebeef5;
        box-shadow: 0 0 5px 1px #ccc;

        :deep(.el-image__inner) {
          transition: transform 0.3s ease;
          cursor: pointer;

          &:hover {
            transform: scale(var(--hover-scale, 1));
          }
        }
      }
    }

    .more-images {
      border-radius: 5px;
      background-color: rgba(0, 0, 0, 0.6);
      display: flex;
      align-items: center;
      justify-content: center;
      cursor: pointer;
      transition: all 0.3s ease;

      &:hover {
        background-color: rgba(0, 0, 0, 0.8);
        transform: scale(1.05);
      }

      .more-text {
        color: white;
        font-size: 16px;
        font-weight: bold;
      }
    }
  }

  :deep(.image-slot) {
    display: flex;
    justify-content: center;
    align-items: center;
    width: 100%;
    height: 100%;
    color: #909399;
    font-size: 30px;
  }
}

// 当不需要悬停效果时
.el-image:not([style*='--hover-scale']) {
  :deep(.el-image__inner:hover) {
    transform: none;
  }
}

// 加载状态样式
.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background-color: #f5f7fa;
  border-radius: 5px;
  border: 1px dashed #d9d9d9;

  .loading-icon {
    font-size: 20px;
    color: #409eff;
    animation: rotate 1s linear infinite;
    margin-bottom: 5px;
  }

  .loading-text {
    font-size: 12px;
    color: #909399;
  }
}

@keyframes rotate {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}
</style>
