import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('../views/Home.vue')
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue')
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/Register.vue')
  },
  {
    path: '/search',
    name: 'Search',
    component: () => import('../views/Search.vue')
  },
  {
    path: '/book/:id',
    name: 'BookDetail',
    component: () => import('../views/BookDetail.vue')
  },
  {
    path: '/profile',
    name: 'Profile',
    component: () => import('../views/Profile.vue')
  },
  {
    path: '/favorites',
    name: 'Favorites',
    component: () => import('../views/Favorites.vue')
  },
  {
    path: '/categories',
    name: 'Categories',
    component: () => import('../views/Categories.vue')
  },
  {
    path: '/collection',
    name: 'Collection',
    component: () => import('../views/Collection.vue')
  },
  {
    path: '/services',
    name: 'Services',
    component: () => import('../views/Services.vue')
  },
  {
    path: '/news',
    name: 'News',
    component: () => import('../views/News.vue')
  },
  {
    path: '/admin',
    name: 'AdminBooks',
    component: () => import('../views/AdminBooks.vue'),
    meta: { requiresAdmin: true }
  },
  {
    path: '/admin/borrows',
    name: 'AdminBorrows',
    component: () => import('../views/AdminBorrows.vue'),
    meta: { requiresAdmin: true }
  },
  {
    path: '/admin/users',
    name: 'AdminUsers',
    component: () => import('../views/AdminUsers.vue'),
    meta: { requiresAdmin: true }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫：管理员页面需要验证角色
router.beforeEach((to, from, next) => {
  if (to.meta.requiresAdmin) {
    const role = localStorage.getItem('role')
    if (role !== 'ADMIN') {
      next('/login')
    } else {
      next()
    }
  } else {
    next()
  }
})

export default router
