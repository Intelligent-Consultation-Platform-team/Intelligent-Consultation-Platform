import { createRouter, createWebHistory } from 'vue-router'

const AuthPage = () => import('../App.vue')

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      redirect: '/home'
    },
    {
      path: '/auth/login',
      name: 'AuthLogin',
      component: AuthPage,
      meta: {
        title: '登录',
        public: true
      }
    },
    {
      path: '/auth/register',
      name: 'AuthRegister',
      component: AuthPage,
      meta: {
        title: '注册',
        public: true
      }
    },
    {
      path: '/home',
      name: 'Home',
      component: () => import('../views/Home.vue'),
      meta: {
        title: '系统首页'
      }
    },
    {
      path: '/manage',
      name: 'Manage',
      component: () => import('../views/Manage.vue'),
      meta: {
        title: '信息管理',
        roles: ['admin']
      },
      children: [
        {
          path: 'notice',
          name: 'Notice',
          component: () => import('../views/manage/Notice.vue'),
          meta: {
            title: '公告信息',
            roles: ['admin']
          }
        },
        {
          path: 'office',
          name: 'Office',
          component: () => import('../views/manage/Office.vue'),
          meta: {
            title: '科室信息',
            roles: ['admin']
          }
        },
        {
          path: 'schedule',
          name: 'Schedule',
          component: () => import('../views/manage/Schedule.vue'),
          meta: {
            title: '医生排班',
            roles: ['admin']
          }
        }
      ]
    },
    {
      path: '/appoint',
      name: 'Appoint',
      component: () => import('../views/Appoint.vue'),
      meta: {
        title: '预约就诊',
        roles: ['admin', 'doctor', 'patient']
      },
      children: [
        {
          path: 'book',
          name: 'Book',
          component: () => import('../views/appoint/Book.vue'),
          meta: {
            title: '预约挂号',
            roles: ['patient']
          }
        },
        {
          path: 'patient-book',
          name: 'PatientBook',
          component: () => import('../views/appoint/PatientBook.vue'),
          meta: {
            title: '患者挂号',
            roles: ['admin', 'doctor']
          }
        },
        {
          path: 'visit',
          name: 'Visit',
          component: () => import('../views/appoint/Visit.vue'),
          meta: {
            title: '患者就诊',
            roles: ['doctor']
          }
        },
        {
          path: 'hospital',
          name: 'Hospital',
          component: () => import('../views/appoint/Hospital.vue'),
          meta: {
            title: '住院登记',
            roles: ['admin', 'doctor']
          }
        }
      ]
    },
    {
      path: '/user',
      name: 'User',
      component: () => import('../views/User.vue'),
      meta: {
        title: '用户管理',
        roles: ['admin']
      },
      children: [
        {
          path: 'admin',
          name: 'Admin',
          component: () => import('../views/user/Admin.vue'),
          meta: {
            title: '管理员信息',
            roles: ['admin']
          }
        },
        {
          path: 'doctor',
          name: 'Doctor',
          component: () => import('../views/user/Doctor.vue'),
          meta: {
            title: '医生信息',
            roles: ['admin']
          }
        },
        {
          path: 'user',
          name: 'UserInfo',
          component: () => import('../views/user/UserInfo.vue'),
          meta: {
            title: '用户信息',
            roles: ['admin']
          }
        },
        {
          path: 'patient-account',
          name: 'PatientAccount',
          component: () => import('../views/user/PatientAccount.vue'),
          meta: {
            title: '患者账户',
            roles: ['admin', 'patient']
          }
        }
      ]
    }
  ]
})

router.beforeEach((to) => {
  const rawSession = localStorage.getItem('demo_session')
  const isPublicRoute = to.matched.some((record) => record.meta?.public)

  let session = null
  if (rawSession) {
    try {
      session = JSON.parse(rawSession)
    } catch {
      localStorage.removeItem('demo_session')
      session = null
    }
  }

  if (session?.expiresIn && session?.loginAt) {
    const isExpired = Date.now() > session.loginAt + Number(session.expiresIn) * 1000
    if (isExpired) {
      localStorage.removeItem('demo_session')
      session = null
    }
  }

  const role = session?.role
  const requiredRoles = to.matched.flatMap((record) =>
    Array.isArray(record.meta?.roles) ? record.meta.roles : []
  )

  if (!session && !isPublicRoute) {
    return '/auth/login'
  }

  if (session && to.path.startsWith('/auth')) {
    return '/home'
  }

  if (requiredRoles.length > 0) {
    if (!role) {
      return '/auth/login'
    }

    if (!requiredRoles.includes(role)) {
      return '/home'
    }
  }

  return true
})

export default router
