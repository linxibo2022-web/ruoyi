<!--
价格卡片组件 APricingCard

使用示例:

1. 基本用法
<el-row :gutter="20">
  <el-col :xs="24" :sm="12" :md="8">
    <APricingCard
      plan="基础版"
      :price="99"
      period="月"
      :features="['基础功能', '5个项目', '邮件支持']"
    />
  </el-col>
</el-row>

2. 带推荐标签
<el-row :gutter="20">
  <el-col :xs="24" :sm="12" :md="8">
    <APricingCard
      plan="专业版"
      :price="199"
      period="月"
      description="最受欢迎的选择"
      :features="['所有基础功能', '无限项目', '高级功能', '优先支持']"
      recommended
    />
  </el-col>
</el-row>

3. 年付优惠价格
<el-row :gutter="20">
  <el-col :xs="24" :sm="12" :md="8">
    <APricingCard
      plan="企业版"
      :price="999"
      :original-price="1188"
      period="年"
      saving-text="节省 ¥189"
      :features="['所有专业功能', '企业级功能', '专属客服', '定制开发']"
      button-text="联系销售"
    />
  </el-col>
</el-row>

4. 免费版本
<el-row :gutter="20">
  <el-col :xs="24" :sm="12" :md="8">
    <APricingCard
      plan="免费版"
      price="免费"
      description="适合个人开发者"
      :features="['基础功能', '1个项目', '社区支持']"
      :disabled-features="['高级功能', '优先支持', '定制开发']"
    />
  </el-col>
</el-row>

5. 完整多列布局
<el-row :gutter="20">
  <el-col :xs="24" :sm="12" :md="6">
    <APricingCard
      plan="入门版"
      :price="49"
      period="月"
      :features="['基础功能', '3个项目']"
    />
  </el-col>
  <el-col :xs="24" :sm="12" :md="6">
    <APricingCard
      plan="专业版"
      :price="99"
      period="月"
      :features="['所有入门功能', '10个项目', '高级功能']"
      recommended
    />
  </el-col>
  <el-col :xs="24" :sm="12" :md="6">
    <APricingCard
      plan="团队版"
      :price="299"
      period="月"
      :features="['所有专业功能', '无限项目', '团队协作']"
      icon="team"
    />
  </el-col>
  <el-col :xs="24" :sm="12" :md="6">
    <APricingCard
      plan="旗舰版"
      :price="2999"
      period="年"
      :features="['所有功能', '专属服务', 'API访问']"
      icon="crown"
      plan-color="#f56c6c"
      button-type="danger"
    />
  </el-col>
</el-row>
-->
<template>
  <div class="pricing-card" :class="{ 'is-recommended': recommended }">
    <!-- 推荐标签 -->
    <div v-if="recommended" class="recommended-badge">
      <Icon code="star" :size="14" />
      <span>{{ t('card.pricing.recommended') }}</span>
    </div>

    <!-- 卡片头部 -->
    <div class="card-header">
      <div v-if="icon" class="plan-icon" :style="{ color: planColor }">
        <Icon :code="icon" :size="32" />
      </div>
      <h3 class="plan-name" :style="{ color: planColor }">{{ plan }}</h3>
      <p v-if="description" class="plan-description">{{ description }}</p>
    </div>

    <!-- 价格区域 -->
    <div class="card-pricing">
      <div class="price-wrapper">
        <span v-if="typeof price === 'number'" class="price-currency">¥</span>
        <span class="price-value">{{ price }}</span>
        <span v-if="period" class="price-period">/{{ period }}</span>
      </div>
      <div v-if="originalPrice" class="original-price">{{ t('card.pricing.originalPrice') }} ¥{{ originalPrice }}</div>
      <div v-if="savingText" class="saving-text">{{ savingText }}</div>
    </div>

    <!-- 功能列表 -->
    <div class="card-features">
      <ul class="feature-list">
        <li v-for="(feature, index) in features" :key="index" class="feature-item">
          <Icon code="check" :size="16" class="feature-icon" />
          <span class="feature-text">{{ feature }}</span>
        </li>
        <li v-for="(feature, index) in disabledFeatures" :key="`disabled-${index}`" class="feature-item is-disabled">
          <Icon code="close-circle" :size="16" class="feature-icon" />
          <span class="feature-text">{{ feature }}</span>
        </li>
      </ul>
    </div>

    <!-- 选择按钮 -->
    <div class="card-action">
      <el-button :type="buttonType" :size="buttonSize" :disabled="disabled" style="width: 100%" @click="handleSelect">
        {{ computedButtonText }}
      </el-button>
    </div>

    <!-- 额外信息 -->
    <div v-if="$slots.footer || footerText" class="card-footer">
      <slot name="footer">
        <p class="footer-text">{{ footerText }}</p>
      </slot>
    </div>
  </div>
</template>

<script setup lang="ts" name="APricingCard">
const { t } = useI18n()

/**价格卡片属性接口*/
interface APricingCardProps {
  /**方案名称*/
  plan: string
  /**价格 (数字或文本如"免费")*/
  price: number | string
  /**原价*/
  originalPrice?: number
  /**周期*/
  period?: string
  /**描述*/
  description?: string
  /**功能列表*/
  features: string[]
  /**不可用功能列表*/
  disabledFeatures?: string[]
  /**是否推荐*/
  recommended?: boolean
  /**图标*/
  icon?: IconCode
  /**方案颜色*/
  planColor?: string
  /**按钮文本*/
  buttonText?: string
  /**按钮类型*/
  buttonType?: 'primary' | 'success' | 'warning' | 'danger' | 'info'
  /**按钮尺寸*/
  buttonSize?: 'large' | 'default' | 'small'
  /**是否禁用*/
  disabled?: boolean
  /**节省金额文本*/
  savingText?: string
  /**底部文本*/
  footerText?: string
}

/**组件属性定义*/
const props = withDefaults(defineProps<APricingCardProps>(), {
  period: '',
  description: '',
  disabledFeatures: () => [],
  recommended: false,
  planColor: 'var(--el-color-primary)',
  buttonText: '选择方案',
  buttonType: 'primary',
  buttonSize: 'default',
  disabled: false,
  savingText: '',
  footerText: ''
})

/**组件事件定义*/
const emit = defineEmits<{
  /**选择方案*/
  select: []
}>()

/**计算按钮文本*/
const computedButtonText = computed(() => props.buttonText || t('card.pricing.selectPlan'))

/**处理选择*/
const handleSelect = () => {
  if (!props.disabled) {
    emit('select')
  }
}

/**暴露方法*/
defineExpose({
  /**方案名称*/
  planName: computed(() => props.plan),
  /**价格*/
  planPrice: computed(() => props.price)
})
</script>

<style lang="scss" scoped>
.pricing-card {
  position: relative;
  display: flex;
  flex-direction: column;
  padding: 32px 24px;
  background-color: var(--bg-level-1);
  border-radius: var(--radius-md);
  border: 2px solid var(--el-border-color);
  transition: all 0.3s ease;

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  }

  &.is-recommended {
    border-color: var(--el-color-primary);
    box-shadow: 0 4px 16px rgba(64, 158, 255, 0.2);

    &:hover {
      box-shadow: 0 8px 24px rgba(64, 158, 255, 0.3);
    }
  }

  .recommended-badge {
    position: absolute;
    top: -12px;
    left: 50%;
    transform: translateX(-50%);
    display: flex;
    align-items: center;
    gap: 4px;
    padding: 4px 16px;
    background: linear-gradient(135deg, var(--el-color-primary), var(--el-color-primary-light-3));
    color: #fff;
    font-size: 12px;
    font-weight: 600;
    border-radius: 12px;
    box-shadow: 0 2px 8px rgba(64, 158, 255, 0.3);
  }

  .card-header {
    text-align: center;
    margin-bottom: 24px;

    .plan-icon {
      margin-bottom: 12px;
    }

    .plan-name {
      margin: 0 0 8px;
      font-size: 24px;
      font-weight: 600;
      color: var(--el-text-color-primary);
    }

    .plan-description {
      margin: 0;
      font-size: 14px;
      color: var(--el-text-color-secondary);
    }
  }

  .card-pricing {
    text-align: center;
    margin-bottom: 32px;
    padding-bottom: 24px;
    border-bottom: 1px solid var(--el-border-color-lighter);

    .price-wrapper {
      display: flex;
      align-items: baseline;
      justify-content: center;
      margin-bottom: 8px;

      .price-currency {
        font-size: 20px;
        font-weight: 500;
        color: var(--el-text-color-regular);
        margin-right: 4px;
      }

      .price-value {
        font-size: 48px;
        font-weight: 700;
        color: var(--el-text-color-primary);
        line-height: 1;
      }

      .price-period {
        font-size: 16px;
        color: var(--el-text-color-secondary);
        margin-left: 4px;
      }
    }

    .original-price {
      font-size: 14px;
      color: var(--el-text-color-placeholder);
      text-decoration: line-through;
      margin-bottom: 4px;
    }

    .saving-text {
      font-size: 13px;
      font-weight: 500;
      color: var(--el-color-success);
    }
  }

  .card-features {
    flex: 1;
    margin-bottom: 24px;

    .feature-list {
      margin: 0;
      padding: 0;
      list-style: none;

      .feature-item {
        display: flex;
        align-items: flex-start;
        gap: 8px;
        padding: 10px 0;
        font-size: 14px;
        color: var(--el-text-color-regular);
        line-height: 1.6;

        &.is-disabled {
          opacity: 0.4;

          .feature-icon {
            color: var(--el-text-color-placeholder);
          }
        }

        .feature-icon {
          flex-shrink: 0;
          margin-top: 2px;
          color: var(--el-color-success);
        }

        .feature-text {
          flex: 1;
        }
      }
    }
  }

  .card-action {
    margin-bottom: 16px;
  }

  .card-footer {
    text-align: center;

    .footer-text {
      margin: 0;
      font-size: 12px;
      color: var(--el-text-color-placeholder);
      line-height: 1.5;
    }
  }
}
</style>
