import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/LoginView.vue'),
    meta: { requiresGuest: true }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/RegisterView.vue'),
    meta: { requiresGuest: true }
  },
  {
    path: '/',
    component: () => import('@/layouts/MainLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      {
        path: '',
        name: 'Dashboard',
        component: () => import('@/views/DashboardView.vue')
      },
      {
        path: 'journals',
        name: 'Journals',
        component: () => import('@/views/JournalListView.vue')
      },
      {
        path: 'journals/new',
        name: 'NewJournal',
        component: () => import('@/views/JournalEditView.vue')
      },
      {
        path: 'journals/:id/edit',
        name: 'EditJournal',
        component: () => import('@/views/JournalEditView.vue')
      },
      {
        path: 'journals/:id',
        name: 'JournalDetail',
        component: () => import('@/views/JournalDetailView.vue')
      },
      {
        path: 'categories',
        name: 'Categories',
        component: () => import('@/views/CategoryView.vue')
      },
      {
        path: 'tags',
        name: 'Tags',
        component: () => import('@/views/TagView.vue')
      },
      {
        path: 'settings',
        name: 'Settings',
        component: () => import('@/views/SettingsView.vue')
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// Navigation guards
router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()
  
  if (to.meta.requiresAuth && !authStore.isAuthenticated) {
    next({ name: 'Login' })
  } else if (to.meta.requiresGuest && authStore.isAuthenticated) {
    next({ name: 'Dashboard' })
  } else {
    next()
  }
})

export default router
