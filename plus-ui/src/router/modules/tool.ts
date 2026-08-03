// 工具路由
import { type RouteRecordRaw } from 'vue-router'
import Layout from '@/layouts/Layout.vue'

/**
 * 开发工具相关路由
 *
 * 包含以下功能模块：
 * - 代码生成: 自动生成代码的配置与管理 (代码生成编辑)
 *
 * 特点：
 * - 权限控制: 这些路由需要相应的开发工具权限才能访问
 * - 隐藏路由: 默认在导航菜单中隐藏 (hidden: true)
 * - 关联菜单: 使用activeMenu指定关联的主菜单项
 * - 无缓存: 配置页面设置noCache防止缓存，确保配置实时生效
 */
export const toolRoutes: RouteRecordRaw[] = [
  // 代码生成编辑页面
  {
    path: '/tool/genEdit',
    component: Layout,
    hidden: true,
    permissions: ['tool:gen:update'],
    children: [
      {
        path: 'genEdit/:tableId(\\d+)',
        component: () => import('@/views/tool/gen/editTable.vue'),
        name: 'GenEdit',
        meta: {
          title: '修改生成配置',
          activeMenu: '/tool/gen', // 高亮显示的菜单项
          icon: '',
          noCache: true // 不缓存页面
        }
      }
    ]
  }
  // 可以在此添加其他工具相关路由 但是只能是动态路由
]
