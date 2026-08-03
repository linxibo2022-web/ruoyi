// 常量路由
import { type RouteRecordRaw } from 'vue-router'
import Layout from '@/layouts/Layout.vue'
import HomeLayout from '@/layouts/HomeLayout.vue'
import { SystemConfig } from '@/systemConfig'

/**
 * 公共路由配置
 *
 * 包含以下路由类型：
 * - 系统路由: 提供系统功能的基础路由 (重定向、登录、注册等)
 * - 错误页面: 处理异常情况的路由 (404、401等)
 * - 基础页面: 应用的主要入口页面 (首页、个人中心等)
 *
 * 特点：
 * - 所有用户都可以访问，不受权限控制
 * - 不需要动态生成，始终保持固定
 * - 部分路由默认隐藏，不在导航菜单中显示 (hidden: true)
 */
export const constantRoutes: RouteRecordRaw[] = [
  // 重定向路由
  {
    path: '/redirect',
    component: Layout,
    hidden: true,
    children: [
      {
        path: '/redirect/:path(.*)',
        component: () => import('@/views/common/redirect.vue'),
        meta: { title: '页面重定向', noCache: true, i18nKey: 'page.redirect' }
      }
    ]
  },

  // 社交登录回调路由
  {
    path: '/socialCallback',
    hidden: true,
    component: () => import('@/views/system/auth/socialCallback.vue'),
    meta: { title: '社交登录回调', i18nKey: 'page.socialCallback' }
  },

  // 登录页面
  {
    path: '/login',
    component: () => import('@/views/system/auth/login.vue'),
    hidden: true,
    meta: { title: '登录', i18nKey: 'page.login' }
  },

  // 注册页面
  {
    path: '/register',
    component: () => import('@/views/system/auth/register.vue'),
    hidden: true,
    meta: { title: '注册', i18nKey: 'page.register' }
  },

  // 忘记密码页面
  {
    path: '/forgotPassword',
    component: () => import('@/views/system/auth/forgotPassword.vue'),
    hidden: true,
    meta: { title: '忘记密码', i18nKey: 'page.forgotPassword' }
  },

  // 404页面 - 捕获所有未匹配的路由
  {
    path: '/:pathMatch(.*)*',
    component: () => import('@/views/common/404.vue'),
    hidden: true,
    meta: { title: '404 Not Found', i18nKey: 'page.notFound' }
  },

  // 401未授权页面
  {
    path: '/401',
    component: () => import('@/views/common/401.vue'),
    hidden: true,
    meta: { title: '401 Unauthorized', i18nKey: 'page.unauthorized' }
  },

  // 根路径重定向
  {
    path: '/',
    redirect: () => {
      return SystemConfig.app.enableFrontend ? '/home' : '/index'
    }
  },
  // 条件性添加前台首页路由
  ...((SystemConfig.app.enableFrontend
    ? [
        {
          path: '/home',
          component: HomeLayout, // 或者你的前台布局组件
          redirect: '/home',
          children: [
            {
              path: '',
              component: () => import('@/views/common/home.vue'),
              name: 'Home',
              meta: { title: '主页', icon: 'home', affix: true, i18nKey: 'menu.home' }
            }
          ]
        }
      ]
    : []) as RouteRecordRaw[]),

  {
    path: '/index',
    component: Layout,
    redirect: '/index',
    children: [
      {
        path: '',
        component: () => import('@/views/common/index.vue'),
        name: 'Index',
        meta: { title: '首页', icon: 'home3', affix: true, i18nKey: 'menu.index' }
      }
    ]
  },

  // 用户个人中心
  {
    path: '/user',
    component: Layout,
    hidden: true,
    redirect: 'noredirect',
    children: [
      {
        path: 'profile',
        component: () => import('@/views/system/core/user/profile/profile.vue'),
        name: 'Profile',
        meta: { title: '个人中心', icon: 'user', i18nKey: 'page.profile' }
      }
    ]
  },

  // DataV 大屏全屏预览页面
  {
    path: '/datav/fullscreen',
    component: () => import('@/views/tool/test/template/datav-fullscreen.vue'),
    hidden: true,
    meta: { title: '大屏预览', noCache: true, i18nKey: 'page.datavFullscreen' }
  }
]
