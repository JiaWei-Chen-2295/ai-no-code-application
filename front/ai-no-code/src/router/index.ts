import { createRouter, createWebHistory } from 'vue-router'
import BasicLayout from '@/layouts/BasicLayout.vue'
import { useUserStore } from '@/stores/userStore'
import { message } from 'ant-design-vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      component: BasicLayout,
      children: [
        {
          path: '',
          name: 'Home',
          component: () => import('@/page/HomePage.vue'),
        },
        {
          path: 'apps',
          name: 'Apps',
          component: () => import('@/page/AppsPage.vue'),
        },
        {
          path: 'featured',
          name: 'Featured',
          component: () => import('@/page/FeaturedAppsPage.vue'),
        },
        {
          path: 'app/detail/:id',
          name: 'AppDetail',
          component: () => import('@/page/AppDetailPage.vue'),
        },
        {
          path: 'app/edit/:id',
          name: 'AppEdit',
          component: () => import('@/page/AppEditPage.vue'),
        },
        {
          path: 'admin/apps',
          name: 'AdminApps',
          component: () => import('@/page/AdminAppsPage.vue'),
          meta: { requiresAdmin: true },
        },
        {
          path: 'admin/chat-history',
          name: 'AdminChatHistory',
          component: () => import('@/page/AdminChatHistoryPage.vue'),
          meta: { requiresAdmin: true },
        },
      ],
    },
    {
      path: '/app/chat/:id',
      name: 'AppChat',
      component: () => import('@/page/AppChatPage.vue'),
    },
    {
      path: '/user/login',
      name: 'UserLogin',
      component: () => import('@/page/UserLoginPage.vue'),
    },
    {
      path: '/user/register',
      name: 'UserRegister',
      component: () => import('@/page/UserRegisterPage.vue'),
    },
    {
      path: '/:pathMatch(.*)*',
      redirect: '/',
    },
  ],
})

// 路由守卫
router.beforeEach(async (to, from, next) => {
  // 获取用户存储
  const userStore = useUserStore()
  
  // 如果路由需要admin权限
  if (to.meta.requiresAdmin) {
    // 确保用户信息已加载
    if (!userStore.loginUser) {
      try {
        await userStore.fetchLoginUser()
      } catch (error) {
        console.error('获取用户信息失败:', error)
      }
    }
    
    // 检查用户是否为admin
    if (!userStore.loginUser || userStore.loginUser.userRole !== 'admin') {
      message.error('您没有访问此页面的权限')
      next('/')
      return
    }
  }
  
  next()
})

export default router