/**
 * 测试路由配置
 *
 * 包含以下功能模块：
 * - 组件中心: UI组件测试和展示
 * - 模板中心: 各种页面模板和布局示例
 *
 * 特点：
 * - 仅开发环境可见: 只在开发模式下显示
 * - 测试隔离: 不影响生产环境
 * - 快速开发: 方便开发者快速测试功能
 */
export const testMenuData: any[] = [
  // 组件中心
  {
    name: 'TestComponent',
    path: 'component',
    hidden: false,
    redirect: 'noRedirect',
    component: 'ParentView',
    alwaysShow: true,
    meta: {
      title: '组件中心',
      icon: 'component',
      noCache: false,
      link: null,
      i18nKey: 'menu.tool.component._self'
    },
    children: [
      {
        name: 'TestForm',
        path: 'form',
        hidden: false,
        component: 'tool/test/component/form',
        meta: {
          title: '表单组件',
          icon: 'form',
          noCache: false,
          link: null,
          i18nKey: 'menu.tool.component.form'
        }
      },
      {
        name: 'TestIcon',
        path: 'icon',
        hidden: false,
        component: 'tool/test/component/icon',
        meta: {
          title: 'Icon图标',
          icon: 'icon',
          noCache: false,
          link: null,
          i18nKey: 'menu.tool.component.icon'
        }
      },
      {
        name: 'TestAi',
        path: 'ai',
        hidden: false,
        component: 'tool/test/component/ai',
        meta: {
          title: 'AI助手',
          icon: 'robot',
          noCache: false,
          link: null,
          i18nKey: 'menu.tool.component.ai'
        }
      },
      {
        name: 'TestAiComponents',
        path: 'aiComponents',
        hidden: false,
        component: 'tool/test/component/aiComponents',
        meta: {
          title: 'AI组件示例',
          icon: 'chip',
          noCache: false,
          link: null,
          i18nKey: 'menu.tool.component.aiComponents'
        }
      },
      {
        name: 'TestFormAi',
        path: 'formAi',
        hidden: false,
        component: 'tool/test/component/formAi',
        meta: {
          title: 'AI表单组件',
          icon: 'magic',
          noCache: false,
          link: null,
          i18nKey: 'menu.tool.component.formAi'
        }
      }
    ]
  },
  // 模板中心
  {
    name: 'TestTemplate',
    path: 'template',
    hidden: false,
    redirect: 'noRedirect',
    component: 'ParentView',
    alwaysShow: true,
    meta: {
      title: '模板中心',
      icon: 'dashboard',
      noCache: false,
      link: null,
      i18nKey: 'menu.tool.template._self'
    },
    children: [
      {
        name: 'TestCard',
        path: 'card',
        hidden: false,
        component: 'tool/test/template/card',
        meta: {
          title: '卡片模板',
          icon: 'button',
          noCache: false,
          link: null,
          i18nKey: 'menu.tool.template.card'
        }
      },
      {
        name: 'TestChart',
        path: 'chart',
        hidden: false,
        component: 'tool/test/template/chart',
        meta: {
          title: '图表模板',
          icon: 'chart',
          noCache: false,
          link: null,
          i18nKey: 'menu.tool.template.chart'
        }
      },
      {
        name: 'TestDataV',
        path: 'datav',
        hidden: false,
        component: 'tool/test/template/datav',
        meta: {
          title: '大屏模板',
          icon: 'monitor',
          noCache: false,
          link: null,
          i18nKey: 'menu.tool.template.datav'
        }
      }
    ]
  },
  // 开发工具
  {
    name: 'TestDev',
    path: 'dev',
    hidden: false,
    redirect: 'noRedirect',
    component: 'ParentView',
    alwaysShow: true,
    meta: {
      title: '开发工具',
      icon: 'bug',
      noCache: false,
      link: null,
      i18nKey: 'menu.tool.dev._self'
    },
    children: [
      {
        name: 'TestLogMonitor',
        path: 'log-monitor',
        hidden: false,
        component: 'tool/test/dev/log-monitor',
        meta: {
          title: '日志监控',
          icon: 'monitor',
          noCache: false,
          link: null,
          i18nKey: 'menu.tool.dev.logMonitor'
        }
      }
    ]
  }
]
