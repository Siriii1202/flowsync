/**
 * Vue Router 路由配置文件
 *
 * 路由（Router）就像一张"地图"，
 * 它告诉应用：当用户访问某个 URL 时，应该显示哪个页面组件。
 *
 * 例如：
 *   /login   → 显示登录页面
 *   /overview → 显示总览页面
 */

import { createRouter, createWebHistory } from 'vue-router'
import { getUser } from '../utils/storage'

// 路由配置表：URL 路径和页面组件的对应关系
const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/LoginView.vue')
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/RegisterView.vue')
  },
  {
    path: '/',
    component: () => import('../views/HomeView.vue'),
    // 路由守卫：进入首页前检查是否已登录
    beforeEnter: (to, from, next) => {
      const user = getUser()
      if (!user) {
        // 没登录就踢回登录页
        next('/login')
      } else {
        next()
      }
    },
    // 子路由：在 HomeView 内部切换显示
    children: [
      {
        path: '',
        redirect: '/overview' // 默认跳转到总览
      },
      {
        path: 'overview',
        name: 'Overview',
        component: () => import('../views/OverviewView.vue'),
        meta: { title: '总览' }
      },
      {
        path: 'projects',
        name: 'Projects',
        component: () => import('../views/ProjectView.vue'),
        meta: { title: '项目管理' }
      },
      {
        path: 'ai-tasks',
        name: 'AiTasks',
        component: () => import('../views/AiTaskView.vue'),
        meta: { title: 'AI 任务拆解', role: 'leader' }
      },
      {
        path: 'tasks',
        name: 'Tasks',
        component: () => import('../views/TaskView.vue'),
        meta: { title: '任务管理' }
      },
      {
        path: 'progress',
        name: 'Progress',
        component: () => import('../views/ProgressView.vue'),
        meta: { title: '进度跟踪' }
      },
      {
        path: 'summaries',
        name: 'Summaries',
        component: () => import('../views/SummaryView.vue'),
        meta: { title: '总结中心' }
      },
      {
        path: 'members',
        name: 'Members',
        component: () => import('../views/MemberView.vue'),
        meta: { title: '成员列表' }
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('../views/ProfileView.vue'),
        meta: { title: '个人信息' }
      }
    ]
  }
]

// 创建路由器实例
const router = createRouter({
  // createWebHistory 使用 HTML5 的 History 模式
  // 这样 URL 中就不会有 # 号了，更美观
  history: createWebHistory(),
  routes
})

// 全局路由守卫：每次路由切换前都会执行
router.beforeEach((to, from, next) => {
  const user = getUser()
  // 如果要去登录页或注册页，直接放行
  if (to.path === '/login' || to.path === '/register') {
    next()
    return
  }
  // 如果没登录，跳转到登录页
  if (!user) {
    next('/login')
    return
  }
  next()
})

export default router
