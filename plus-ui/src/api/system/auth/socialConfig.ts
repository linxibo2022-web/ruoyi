/**
 * 社交登录配置
 * 定义所有支持的社交登录类型及其图标、名称
 */

export interface SocialConfig {
  /** 社交登录类型标识 */
  type: string
  /** 显示名称 */
  name: string
  /** 图标代码（Icon组件使用） */
  icon: string
  /** 图标颜色（可选） */
  color?: string
}

/**
 * 所有支持的社交登录类型配置
 * 与后端配置文件中的类型一一对应
 */
export const SOCIAL_CONFIGS: SocialConfig[] = [
  {
    type: 'wechat_open',
    name: '微信开放平台',
    icon: 'wechat-fill',
    color: '#07C160'
  },
  {
    type: 'wechat_mp',
    name: '微信公众号',
    icon: 'wechat-fill',
    color: '#07C160'
  },
  {
    type: 'wechat_enterprise',
    name: '企业微信',
    icon: 'wechat-fill',
    color: '#0099FF'
  },
  {
    type: 'dingtalk',
    name: '钉钉',
    icon: 'mdi:alpha-d-box',
    color: '#0089FF'
  },
  {
    type: 'maxkey',
    name: 'MaxKey',
    icon: 'maxkey'
  },
  {
    type: 'topiam',
    name: 'TopIAM',
    icon: 'topiam'
  },
  {
    type: 'qq',
    name: 'QQ',
    icon: 'mdi:qqchat',
    color: '#12B7F5'
  },
  {
    type: 'weibo',
    name: '微博',
    icon: 'mdi:sina-weibo',
    color: '#E6162D'
  },
  {
    type: 'gitee',
    name: 'Gitee',
    icon: 'gitee',
    color: '#C71D23'
  },
  {
    type: 'github',
    name: 'GitHub',
    icon: 'github',
    color: '#181717'
  },
  {
    type: 'gitlab',
    name: 'GitLab',
    icon: 'mdi:gitlab',
    color: '#FC6D26'
  },
  {
    type: 'baidu',
    name: '百度',
    icon: 'mdi:alpha-b-circle',
    color: '#2319DC'
  },
  {
    type: 'csdn',
    name: 'CSDN',
    icon: 'mdi:alpha-c-box',
    color: '#FC5531'
  },
  {
    type: 'coding',
    name: 'Coding',
    icon: 'mdi:code-braces-box',
    color: '#0066FF'
  },
  {
    type: 'oschina',
    name: '开源中国',
    icon: 'mdi:alpha-o-circle',
    color: '#3DAD48'
  },
  {
    type: 'alipay_wallet',
    name: '支付宝',
    icon: 'mdi:alpha-a-box',
    color: '#1678FF'
  },
  {
    type: 'taobao',
    name: '淘宝',
    icon: 'mdi:alpha-t-box',
    color: '#FF6A00'
  },
  {
    type: 'douyin',
    name: '抖音',
    icon: 'mdi:music-note',
    color: '#000000'
  },
  {
    type: 'linkedin',
    name: 'LinkedIn',
    icon: 'mdi:linkedin',
    color: '#0A66C2'
  },
  {
    type: 'microsoft',
    name: 'Microsoft',
    icon: 'mdi:microsoft',
    color: '#00A4EF'
  },
  {
    type: 'renren',
    name: '人人网',
    icon: 'mdi:account-multiple',
    color: '#005AAA'
  },
  {
    type: 'stack_overflow',
    name: 'Stack Overflow',
    icon: 'mdi:stack-overflow',
    color: '#F58025'
  },
  {
    type: 'huawei',
    name: '华为',
    icon: 'mdi:alpha-h-box',
    color: '#FF0000'
  },
  {
    type: 'aliyun',
    name: '阿里云',
    icon: 'mdi:cloud',
    color: '#FF6A00'
  },
  {
    type: 'gitea',
    name: 'Gitea',
    icon: 'mdi:git',
    color: '#609926'
  }
]

/**
 * 根据类型获取社交登录配置
 * @param type 社交登录类型
 * @returns 配置对象或undefined
 */
export function getSocialConfig(type: string): SocialConfig | undefined {
  return SOCIAL_CONFIGS.find((config) => config.type === type)
}

/**
 * 根据类型列表获取配置列表
 * @param types 逗号分隔的类型字符串或类型数组
 * @returns 配置对象数组
 */
export function getSocialConfigs(types: string | string[]): SocialConfig[] {
  const typeArray = Array.isArray(types) ? types : types.split(',').filter((t) => t.trim())
  return typeArray.map((type) => getSocialConfig(type.trim())).filter((config) => config !== undefined) as SocialConfig[]
}
