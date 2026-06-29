import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import { useUserStore } from '@/stores/user'

/** 路由配置 */
const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    component: () => import('@/views/Login.vue'),
    meta: { noAuth: true }
  },
  {
    path: '/',
    component: () => import('@/views/Layout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        component: () => import('@/views/Dashboard.vue'),
        meta: { title: '首页看板', icon: 'Odometer' }
      },
      {
        path: 'products',
        component: () => import('@/views/Products.vue'),
        meta: { title: '商品管理', icon: 'Goods' }
      },
      {
        path: 'categories',
        component: () => import('@/views/Categories.vue'),
        meta: { title: '分类管理', icon: 'Collection' }
      },
      {
        path: 'member-levels',
        component: () => import('@/views/MemberLevels.vue'),
        meta: { title: '会员等级', icon: 'Star' }
      },
      {
        path: 'cashier',
        component: () => import('@/views/Cashier.vue'),
        meta: { title: '收银台', icon: 'ShoppingCart' }
      },
      {
        path: 'orders',
        component: () => import('@/views/Orders.vue'),
        meta: { title: '订单记录', icon: 'List' }
      },
      {
        path: 'users',
        component: () => import('@/views/Users.vue'),
        meta: { title: '用户管理', icon: 'User' }
      },
      {
        path: 'members',
        component: () => import('@/views/Members.vue'),
        meta: { title: '会员管理', icon: 'UserFilled' }
      },
      {
        path: 'suppliers',
        component: () => import('@/views/Suppliers.vue'),
        meta: { title: '供应商管理', icon: 'Van' }
      },
      {
        path: 'purchases',
        component: () => import('@/views/Purchases.vue'),
        meta: { title: '采购进货', icon: 'Download' }
      },
      {
        path: 'inventory-counts',
        component: () => import('@/views/InventoryCounts.vue'),
        meta: { title: '库存盘点', icon: 'List' }
      },
      {
        path: 'promotions',
        component: () => import('@/views/Promotions.vue'),
        meta: { title: '促销活动', icon: 'Present' }
      },
      {
        path: 'operation-logs',
        component: () => import('@/views/OperationLogs.vue'),
        meta: { title: '操作日志', icon: 'Document' }
      },
      {
        path: 'statistics',
        component: () => import('@/views/Statistics.vue'),
        meta: { title: '统计报表', icon: 'DataAnalysis' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

/** 路由守卫：未登录时跳转登录页 */
router.beforeEach((to, _from, next) => {
  if (to.meta.noAuth) {
    next()
    return
  }
  const userStore = useUserStore()
  if (!userStore.getToken()) {
    next('/login')
    return
  }
  next()
})

export default router