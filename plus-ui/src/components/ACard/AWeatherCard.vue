<!--
天气卡片组件 AWeatherCard

使用示例:

1. 基本用法
<el-row :gutter="20">
  <el-col :xs="24" :sm="12" :md="8">
    <AWeatherCard
      :weather="{
        city: '北京',
        temperature: 25,
        condition: '晴',
        icon: 'sunny'
      }"
    />
  </el-col>
</el-row>

2. 完整天气信息
<el-row :gutter="20">
  <el-col :xs="24" :sm="12" :md="8">
    <AWeatherCard
      :weather="{
        city: '上海',
        temperature: 22,
        condition: '多云',
        icon: 'cloudy',
        humidity: 60,
        windSpeed: 12,
        windDirection: '东南风',
        airQuality: 85,
        feelLike: 24
      }"
    />
  </el-col>
</el-row>

3. 带未来天气预报
<el-row :gutter="20">
  <el-col :xs="24" :md="12">
    <AWeatherCard
      :weather="{
        city: '广州',
        temperature: 28,
        condition: '晴转多云',
        icon: 'sunny',
        humidity: 65,
        airQuality: 72
      }"
      :forecast="[
        { date: '明天', high: 30, low: 22, condition: '晴', icon: 'sunny' },
        { date: '后天', high: 28, low: 20, condition: '多云', icon: 'cloudy' },
        { date: '周四', high: 26, low: 19, condition: '小雨', icon: 'rainy' }
      ]"
      show-forecast
    />
  </el-col>
</el-row>

4. 带刷新按钮
<el-row :gutter="20">
  <el-col :xs="24" :sm="12" :md="8">
    <AWeatherCard
      :weather="{
        city: '深圳',
        temperature: 27,
        condition: '阵雨',
        icon: 'rainy',
        humidity: 75,
        windSpeed: 15,
        airQuality: 68
      }"
      show-action
    />
  </el-col>
</el-row>

5. 多城市天气展示
<el-row :gutter="20">
  <el-col :xs="24" :sm="12" :md="6">
    <AWeatherCard
      :weather="{ city: '杭州', temperature: 24, condition: '晴', icon: 'sunny' }"
    />
  </el-col>
  <el-col :xs="24" :sm="12" :md="6">
    <AWeatherCard
      :weather="{ city: '成都', temperature: 20, condition: '多云', icon: 'cloudy' }"
    />
  </el-col>
  <el-col :xs="24" :sm="12" :md="6">
    <AWeatherCard
      :weather="{ city: '西安', temperature: 18, condition: '阴', icon: 'foggy' }"
    />
  </el-col>
  <el-col :xs="24" :sm="12" :md="6">
    <AWeatherCard
      :weather="{ city: '武汉', temperature: 23, condition: '小雨', icon: 'rainy' }"
    />
  </el-col>
</el-row>
-->
<template>
  <div class="weather-card">
    <!-- 卡片头部 -->
    <div class="card-header">
      <div class="header-content">
        <h3 class="card-title">
          <Icon code="location" :size="16" />
          {{ weather.city }}
        </h3>
        <p v-if="currentTime" class="card-subtitle">{{ currentTime }}</p>
      </div>
      <div v-if="showAction || $slots['header-action']" class="header-action">
        <slot name="header-action">
          <el-button text @click="handleAction">
            <Icon code="refresh" :size="16" />
          </el-button>
        </slot>
      </div>
    </div>

    <!-- 主要天气信息 -->
    <div class="card-body">
      <div class="weather-main">
        <!-- 天气图标 -->
        <div class="weather-icon" :style="{ color: getWeatherColor() }">
          <Icon :code="getWeatherIcon()" :size="64" />
        </div>

        <!-- 温度和天气状况 -->
        <div class="weather-info">
          <div class="temperature">
            <span class="temp-value">{{ weather.temperature }}</span>
            <span class="temp-unit">°C</span>
          </div>
          <p class="weather-condition">{{ weather.condition }}</p>
          <p v-if="weather.feelLike" class="weather-feel">{{ t('card.weather.feelsLike') }} {{ weather.feelLike }}°C</p>
        </div>
      </div>

      <!-- 详细信息 -->
      <div v-if="showDetails && hasWeatherDetails" class="weather-details">
        <div v-if="weather.humidity !== undefined" class="detail-item">
          <Icon code="water" :size="16" />
          <span class="detail-label">{{ t('card.weather.humidity') }}</span>
          <span class="detail-value">{{ weather.humidity }}%</span>
        </div>
        <div v-if="weather.windSpeed !== undefined" class="detail-item">
          <Icon code="wind" :size="16" />
          <span class="detail-label">{{ t('card.weather.windSpeed') }}</span>
          <span class="detail-value">{{ weather.windSpeed }} km/h</span>
        </div>
        <div v-if="weather.windDirection" class="detail-item">
          <Icon code="compass" :size="16" />
          <span class="detail-label">{{ t('card.weather.windDirection') }}</span>
          <span class="detail-value">{{ weather.windDirection }}</span>
        </div>
        <div v-if="weather.airQuality !== undefined" class="detail-item">
          <Icon code="cloud" :size="16" />
          <span class="detail-label">{{ t('card.weather.airQuality') }}</span>
          <span class="detail-value" :class="getAirQualityClass()">
            {{ getAirQualityText() }}
          </span>
        </div>
      </div>

      <!-- 未来天气预报 -->
      <div v-if="showForecast && forecast && forecast.length > 0" class="weather-forecast">
        <div class="forecast-title">{{ t('card.weather.forecast') }}</div>
        <div class="forecast-list">
          <div v-for="(item, index) in forecast" :key="index" class="forecast-item">
            <span class="forecast-date">{{ item.date }}</span>
            <Icon :code="getWeatherIcon(item.icon)" :size="24" />
            <span class="forecast-temp"> {{ item.high }}° / {{ item.low }}° </span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts" name="AWeatherCard">
const { t } = useI18n()

/**天气数据接口*/
interface WeatherData {
  /**城市*/
  city: string
  /**温度*/
  temperature: number
  /**天气状况*/
  condition: string
  /**天气图标*/
  icon?: string
  /**湿度*/
  humidity?: number
  /**风速*/
  windSpeed?: number
  /**风向*/
  windDirection?: string
  /**空气质量指数*/
  airQuality?: number
  /**体感温度*/
  feelLike?: number
}

/**天气预报数据接口*/
interface ForecastData {
  /**日期*/
  date: string
  /**最高温度*/
  high: number
  /**最低温度*/
  low: number
  /**天气状况*/
  condition: string
  /**天气图标*/
  icon?: string
}

/**天气卡片属性接口*/
interface AWeatherCardProps {
  /**天气数据*/
  weather: WeatherData
  /**未来天气预报*/
  forecast?: ForecastData[]
  /**是否显示详细信息*/
  showDetails?: boolean
  /**是否显示天气预报*/
  showForecast?: boolean
  /**是否显示操作按钮*/
  showAction?: boolean
  /**是否显示当前时间*/
  showTime?: boolean
}

/**组件属性定义*/
const props = withDefaults(defineProps<AWeatherCardProps>(), {
  forecast: () => [],
  showDetails: true,
  showForecast: false,
  showAction: false,
  showTime: true
})

/**组件事件定义*/
const emit = defineEmits<{
  /**操作按钮点击*/
  action: []
}>()

/**当前时间*/
const currentTime = computed(() => {
  if (!props.showTime) return ''
  const now = new Date()
  return now.toLocaleString('zh-CN', {
    month: 'long',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
})

/**是否有详细天气信息*/
const hasWeatherDetails = computed(() => {
  return (
    props.weather.humidity !== undefined ||
    props.weather.windSpeed !== undefined ||
    props.weather.windDirection !== undefined ||
    props.weather.airQuality !== undefined
  )
})

/**天气图标映射*/
const weatherIconMap: Record<string, IconCode> = {
  'sunny': 'sun', // 晴天 - 使用太阳图标
  'cloudy': 'cloud', // 多云 - 使用云端图标
  'rainy': 'rain', // 雨天 - 使用雨图标
  'snowy': 'snow', // 雪天 - 使用雪图标
  'windy': 'wind', // 大风 - 使用风图标
  'foggy': 'cloud', // 雾天 - 使用云端图标
  'thunder': 'lightning', // 雷电 - 使用闪电图标
  'clear': 'moon' // 晴朗夜晚 - 使用月亮图标
}

/**天气颜色映射*/
const weatherColorMap: Record<string, string> = {
  'sunny': '#f39c12', // 橙黄色
  'cloudy': '#95a5a6', // 灰色
  'rainy': '#3498db', // 蓝色
  'snowy': '#ecf0f1', // 浅灰白色
  'windy': '#16a085', // 青绿色
  'foggy': '#7f8c8d', // 深灰色
  'thunder': '#9b59b6', // 紫色
  'clear': '#34495e' // 深蓝灰色
}

/**获取天气图标*/
const getWeatherIcon = (icon?: string): IconCode => {
  const iconKey = icon || props.weather.icon || 'sun'
  return weatherIconMap[iconKey] || 'sun'
}

/**获取天气颜色*/
const getWeatherColor = (): string => {
  const iconKey = props.weather.icon || 'sunny'
  return weatherColorMap[iconKey] || '#f39c12'
}

/**获取空气质量等级*/
const getAirQualityClass = (): string => {
  const aqi = props.weather.airQuality || 0
  if (aqi <= 50) return 'aqi-excellent'
  if (aqi <= 100) return 'aqi-good'
  if (aqi <= 150) return 'aqi-moderate'
  if (aqi <= 200) return 'aqi-poor'
  return 'aqi-bad'
}

/**获取空气质量文本*/
const getAirQualityText = (): string => {
  const aqi = props.weather.airQuality || 0
  if (aqi <= 50) return t('card.weather.aqi.excellent')
  if (aqi <= 100) return t('card.weather.aqi.good')
  if (aqi <= 150) return t('card.weather.aqi.moderate')
  if (aqi <= 200) return t('card.weather.aqi.poor')
  return t('card.weather.aqi.bad')
}

/**处理操作按钮点击*/
const handleAction = () => {
  emit('action')
}

/**暴露方法*/
defineExpose({
  /**城市*/
  city: computed(() => props.weather.city),
  /**温度*/
  temperature: computed(() => props.weather.temperature)
})
</script>

<style lang="scss" scoped>
.weather-card {
  background-color: var(--bg-level-1);
  border-radius: var(--radius-md);
  border: 1px solid var(--el-border-color);
  overflow: hidden;

  .card-header {
    display: flex;
    align-items: flex-start;
    justify-content: space-between;
    padding: 20px;
    border-bottom: 1px solid var(--el-border-color);

    .header-content {
      flex: 1;
    }

    .card-title {
      display: flex;
      align-items: center;
      gap: 6px;
      margin: 0 0 4px;
      font-size: 16px;
      font-weight: 500;
      color: var(--el-text-color-primary);
    }

    .card-subtitle {
      margin: 0;
      font-size: 13px;
      color: var(--el-text-color-secondary);
    }

    .header-action {
      margin-left: 16px;

      :deep(.el-button) {
        color: var(--el-text-color-regular);

        &:hover {
          color: var(--el-color-primary);
        }
      }
    }
  }

  .card-body {
    padding: 20px;
  }

  .weather-main {
    display: flex;
    align-items: center;
    gap: 20px;
    margin-bottom: 20px;

    .weather-icon {
      flex-shrink: 0;
    }

    .weather-info {
      flex: 1;
    }

    .temperature {
      display: flex;
      align-items: baseline;
      margin-bottom: 8px;

      .temp-value {
        font-size: 48px;
        font-weight: 600;
        color: var(--el-text-color-primary);
        line-height: 1;
      }

      .temp-unit {
        margin-left: 4px;
        font-size: 24px;
        color: var(--el-text-color-secondary);
      }
    }

    .weather-condition {
      margin: 0 0 4px;
      font-size: 16px;
      color: var(--el-text-color-regular);
    }

    .weather-feel {
      margin: 0;
      font-size: 13px;
      color: var(--el-text-color-secondary);
    }
  }

  .weather-details {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
    padding: 16px;
    background-color: var(--el-fill-color-light);
    border-radius: var(--radius-md);
    margin-bottom: 20px;

    .detail-item {
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 13px;

      :deep(.icon) {
        color: var(--el-text-color-secondary);
      }

      .detail-label {
        color: var(--el-text-color-secondary);
      }

      .detail-value {
        margin-left: auto;
        font-weight: 500;
        color: var(--el-text-color-primary);

        &.aqi-excellent {
          color: var(--el-color-success);
        }

        &.aqi-good {
          color: #67c23a;
        }

        &.aqi-moderate {
          color: var(--el-color-warning);
        }

        &.aqi-poor {
          color: #e6a23c;
        }

        &.aqi-bad {
          color: var(--el-color-danger);
        }
      }
    }
  }

  .weather-forecast {
    .forecast-title {
      margin-bottom: 12px;
      font-size: 14px;
      font-weight: 500;
      color: var(--el-text-color-primary);
    }

    .forecast-list {
      display: flex;
      gap: 12px;
      overflow-x: auto;
    }

    .forecast-item {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 8px;
      padding: 12px;
      min-width: 80px;
      background-color: var(--el-fill-color-light);
      border-radius: var(--radius-md);

      .forecast-date {
        font-size: 12px;
        color: var(--el-text-color-secondary);
      }

      .forecast-temp {
        font-size: 13px;
        font-weight: 500;
        color: var(--el-text-color-primary);
      }
    }
  }
}
</style>
