<template>
  <div class="app-root">
    <div v-if="!isLoggedIn" class="auth-page">
      <el-card class="auth-card" shadow="hover">
        <template #header>
          <div class="auth-header">
            <h2>欢迎使用牛牛平台</h2>
            <p>请先登录或注册账号</p>
          </div>
        </template>

        <el-segmented
          v-model="activeTab"
          :options="tabOptions"
          block
          class="tab-switch"
        />

        <el-form
          ref="formRef"
          :model="formModel"
          :rules="rules"
          label-position="top"
          class="auth-form"
        >
          <el-form-item label="用户名" prop="username">
            <el-input
              v-model.trim="formModel.username"
              placeholder="请输入用户名"
              clearable
            />
          </el-form-item>

          <el-form-item
            v-if="isRegister"
            label="真实姓名"
            prop="realName"
          >
            <el-input
              v-model.trim="formModel.realName"
              placeholder="请输入真实姓名"
              clearable
            />
          </el-form-item>

          <el-form-item
            v-if="isRegister"
            label="手机号"
            prop="phone"
          >
            <el-input
              v-model.trim="formModel.phone"
              placeholder="请输入手机号"
              clearable
            />
          </el-form-item>

          <el-form-item
            v-if="isRegister"
            label="角色"
            prop="role"
          >
            <el-select
              v-model="formModel.role"
              placeholder="请选择角色"
              clearable
              style="width: 100%"
            >
              <el-option label="患者" value="patient" />
              <el-option label="医生" value="doctor" />
            </el-select>
          </el-form-item>

          <el-form-item
            v-if="isRegister"
            label="邮箱"
            prop="email"
          >
            <el-input
              v-model.trim="formModel.email"
              placeholder="请输入邮箱"
              clearable
            />
          </el-form-item>

          <el-form-item label="密码" prop="password">
            <el-input
              v-model="formModel.password"
              placeholder="请输入密码"
              show-password
              clearable
            />
          </el-form-item>

          <el-form-item
            v-if="isRegister"
            label="确认密码"
            prop="confirmPassword"
          >
            <el-input
              v-model="formModel.confirmPassword"
              placeholder="请再次输入密码"
              show-password
              clearable
            />
          </el-form-item>

          <el-form-item class="submit-row">
            <el-button
              type="primary"
              :loading="submitting"
              class="submit-btn"
              @click="handleSubmit"
            >
              {{ isRegister ? '立即注册' : '立即登录' }}
            </el-button>
          </el-form-item>
        </el-form>

        <p class="switch-tip">
          {{ isRegister ? '已有账号？' : '还没有账号？' }}
          <el-button link type="primary" @click="toggleTab">
            {{ isRegister ? '去登录' : '去注册' }}
          </el-button>
        </p>

        <div class="demo-account">
          <p>默认测试账号：</p>
          <p>邮箱：test@demo.com</p>
          <p>密码：123456</p>
          <el-button link type="primary" @click="fillDemoAccount">
            一键填充
          </el-button>
        </div>
      </el-card>
    </div>

    <div v-else class="inside-page">
      <el-container class="layout">
        <el-aside width="220px" class="aside">
          <div class="brand">牛牛平台</div>
          <el-menu
            :default-active="activeMenu"
            class="menu"
            unique-opened
            router
          >
            <el-menu-item
              v-for="item in menuItems.filter(i => !i.children)"
              :key="item.index"
              :index="item.path"
            >
              {{ item.label }}
            </el-menu-item>
            <el-sub-menu
              v-for="item in menuItems.filter(i => i.children)"
              :key="item.index"
              :index="item.path"
            >
              <template #title>{{ item.label }}</template>
              <el-menu-item
                v-for="child in item.children"
                :key="child.index"
                :index="child.path"
              >
                {{ child.label }}
              </el-menu-item>
            </el-sub-menu>
          </el-menu>
        </el-aside>

        <el-container>
          <el-header class="header">
            <div class="title">欢迎，{{ currentUser.realName || currentUser.username }}</div>
            <div class="header-actions">
              <el-tag type="success">已登录</el-tag>
              <el-tag type="primary">{{ getRoleLabel(currentUser.role) }}</el-tag>
              <el-button type="danger" plain size="small" @click="logout">
                退出登录
              </el-button>
            </div>
          </el-header>

          <el-main class="main">
            <router-view v-slot="{ Component }">
              <transition name="fade" mode="out-in">
                <component :is="Component" />
              </transition>
            </router-view>
          </el-main>
        </el-container>
      </el-container>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { useRoute, useRouter } from 'vue-router'

const activeTab = ref('login')
const formRef = ref()
const submitting = ref(false)
const isLoggedIn = ref(false)
const currentUser = reactive({
  username: '',
  realName: '',
  email: '',
  role: '',
})

const route = useRoute()
const router = useRouter()

const formModel = reactive({
  username: '',
  realName: '',
  phone: '',
  email: '',
  password: '',
  confirmPassword: '',
  role: '',
})

const tabOptions = [
  { label: '登录', value: 'login' },
  { label: '注册', value: 'register' },
]

const isRegister = computed(() => activeTab.value === 'register')

const roleLabels = {
  admin: '管理员',
  doctor: '医生',
  patient: '患者',
}

const getRoleLabel = (role) => roleLabels[role] || '未知'

const activeMenu = computed(() => {
  return route.path
})

const menuItems = computed(() => {
  const role = currentUser.role
  console.log('当前用户角色:', role)
  const items = [
    { index: 'home', label: '系统首页', path: '/home', roles: ['admin', 'doctor', 'patient'] },
    {
      index: 'manage',
      label: '信息管理',
      path: '/manage/notice',
      roles: ['admin'],
      children: [
        { index: 'notice', label: '公告信息', path: '/manage/notice', roles: ['admin'] },
        { index: 'office', label: '科室信息', path: '/manage/office', roles: ['admin'] },
        { index: 'schedule', label: '医生排班', path: '/manage/schedule', roles: ['admin'] },
      ],
    },
    {
      index: 'appoint',
      label: '预约就诊',
      path: '/appoint/book',
      roles: ['admin', 'doctor', 'patient'],
      children: [
        { index: 'book', label: '预约挂号', path: '/appoint/book', roles: ['patient'] },
        { index: 'patient-book', label: '患者挂号', path: '/appoint/patient-book', roles: ['admin', 'doctor'] },
        { index: 'visit', label: '患者就诊', path: '/appoint/visit', roles: ['doctor'] },
        { index: 'hospital', label: '住院登记', path: '/appoint/hospital', roles: ['admin', 'doctor'] },
      ],
    },
    {
      index: 'user',
      label: '用户管理',
      path: '/user/admin',
      roles: ['admin'],
      children: [
        { index: 'admin', label: '管理员信息', path: '/user/admin', roles: ['admin'] },
        { index: 'doctor', label: '医生信息', path: '/user/doctor', roles: ['admin'] },
        { index: 'user', label: '用户信息', path: '/user/user', roles: ['admin'] },
      ],
    },
  ]

  const filterByRole = (items) => {
    return items.filter((item) => item.roles.includes(role)).map((item) => {
      if (item.children) {
        const filteredChildren = filterByRole(item.children)
        if (filteredChildren.length > 0) {
          return { ...item, children: filteredChildren }
        }
        return null
      }
      return item
    }).filter(Boolean)
  }

  const result = filterByRole(items)
  console.log('生成的菜单:', result)
  return result
})

const validateConfirmPassword = (_, value, callback) => {
  if (!isRegister.value) {
    callback()
    return
  }
  if (!value) {
    callback(new Error('请再次输入密码'))
    return
  }
  if (value !== formModel.password) {
    callback(new Error('两次输入的密码不一致'))
    return
  }
  callback()
}

const rules = computed(() => ({
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度为 3-20 位', trigger: 'blur' },
  ],
  realName: isRegister.value
    ? [
        { required: true, message: '请输入真实姓名', trigger: 'blur' },
        { min: 2, max: 20, message: '真实姓名长度为 2-20 位', trigger: 'blur' },
      ]
    : [],
  phone: isRegister.value
    ? [
        { required: true, message: '请输入手机号', trigger: 'blur' },
        { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: ['blur', 'change'] },
      ]
    : [],
  role: isRegister.value
    ? [{ required: true, message: '请选择角色', trigger: 'change' }]
    : [],
  email: isRegister.value
    ? [
        { required: true, message: '请输入邮箱', trigger: 'blur' },
        { type: 'email', message: '邮箱格式不正确', trigger: ['blur', 'change'] },
      ]
    : [],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 32, message: '密码长度为 6-32 位', trigger: 'blur' },
  ],
  confirmPassword: isRegister.value
    ? [{ validator: validateConfirmPassword, trigger: ['blur', 'change'] }]
    : [],
}))

const storageKeys = {
  session: 'demo_session',
}


const saveSession = (user) => {
  localStorage.setItem(
    storageKeys.session,
    JSON.stringify({
      ...user,
      loginAt: Date.now(),
    })
  )
}

const getSession = () => {
  const raw = localStorage.getItem(storageKeys.session)
  if (!raw) return null

  try {
    return JSON.parse(raw)
  } catch {
    localStorage.removeItem(storageKeys.session)
    return null
  }
}

const isSessionExpired = (session) => {
  if (!session?.expiresIn || !session?.loginAt) return false
  return Date.now() > session.loginAt + Number(session.expiresIn) * 1000
}

const restoreSession = () => {
  const user = getSession()
  if (!user) return

  if (isSessionExpired(user)) {
    localStorage.removeItem(storageKeys.session)
    return
  }

  currentUser.username = user.username
  currentUser.realName = user.realName
  currentUser.email = user.email
  currentUser.role = user.role
  isLoggedIn.value = true
}

const authFetch = async (url, options = {}) => {
  const session = getSession()
  const token = session?.token
  const tokenType = session?.tokenType || 'Bearer'

  const response = await fetch(url, {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      ...(token ? { Authorization: `${tokenType} ${token}` } : {}),
      ...(options.headers || {}),
    },
  })

  if (response.status === 401) {
    localStorage.removeItem(storageKeys.session)
    isLoggedIn.value = false
    router.push('/auth/login')
    throw new Error('登录已过期，请重新登录')
  }

  return response
}

watch(activeTab, (tab) => {
  resetForm()

  const targetPath = tab === 'register' ? '/auth/register' : '/auth/login'
  if (route.path !== targetPath) {
    router.push(targetPath)
  }
})

watch(
  () => route.path,
  (path) => {
    if (path === '/auth/register') {
      activeTab.value = 'register'
    } else if (path === '/auth/login') {
      activeTab.value = 'login'
    }
  },
  { immediate: true }
)

const resetForm = () => {
  formModel.username = ''
  formModel.realName = ''
  formModel.phone = ''
  formModel.email = ''
  formModel.password = ''
  formModel.confirmPassword = ''
  formModel.role = ''
  formRef.value?.clearValidate()
}

const toggleTab = () => {
  router.push(isRegister.value ? '/auth/login' : '/auth/register')
}

const fillDemoAccount = () => {
  formModel.username = 'test'
  formModel.password = '123456'
}

const handleSubmit = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    submitting.value = true

    if (isRegister.value) {
      const response = await authFetch('/auth/register', {
        method: 'POST',
        body: JSON.stringify({
          username: formModel.username,
          nickname: formModel.realName,
          phone: formModel.phone,
          email: formModel.email,
          password: formModel.password,
          confirmPassword: formModel.confirmPassword,
          role: formModel.role,
        }),
      })

      const result = await response.json()

      if (!response.ok || result.code !== 0) {
        ElMessage.error(result.message || '注册失败')
        return
      }

      ElMessage.success('注册成功，请登录')
      router.push('/auth/login')
      return
    }

    const response = await authFetch('/auth/login', {
      method: 'POST',
      body: JSON.stringify({
        username: formModel.username,
        password: formModel.password,
      }),
    })

    const result = await response.json()

    if (!response.ok || result.code !== 0) {
      ElMessage.error(result.message || '账号或密码错误')
      return
    }

    const user = result?.data?.user || {}

    currentUser.username = user.username || formModel.username
    currentUser.realName = user.nickname || user.realName || formModel.username
    currentUser.email = user.email || formModel.email
    if (!user.role) {
      ElMessage.error('登录返回缺少角色信息，请联系后端检查 /auth/login 返回数据')
      return
    }
    currentUser.role = user.role
    isLoggedIn.value = true

    saveSession({
      username: currentUser.username,
      realName: currentUser.realName,
      email: currentUser.email,
      role: currentUser.role,
      token: result?.data?.token || '',
      tokenType: result?.data?.tokenType || 'Bearer',
      expiresIn: result?.data?.expiresIn,
    })

    ElMessage.success(`登录成功，欢迎回来：${currentUser.realName || currentUser.username}`)
    router.push('/home')
  } catch (error) {
    ElMessage.error('网络异常或后端服务未启动')
  } finally {
    submitting.value = false
  }
}

const logout = () => {
  localStorage.removeItem(storageKeys.session)
  isLoggedIn.value = false
  currentUser.username = ''
  currentUser.realName = ''
  currentUser.email = ''
  currentUser.role = ''
  router.push('/auth/login')
  resetForm()
}

onMounted(() => {
  restoreSession()
})
</script>

<style scoped>
.app-root {
  min-height: 100vh;
}

.auth-page {
  min-height: 100vh;
  display: grid;
  place-items: center;
  padding: 20px;
  background: linear-gradient(135deg, #f0f5ff 0%, #f9fbff 45%, #eef6ff 100%);
}

.auth-card {
  width: 100%;
  max-width: 460px;
  border-radius: 18px;
}

.auth-header {
  text-align: center;
}

.auth-header h2 {
  margin: 0;
  font-size: 24px;
  color: #1f2d3d;
}

.auth-header p {
  margin: 8px 0 0;
  color: #7b8794;
}

.tab-switch {
  margin-bottom: 18px;
}

.auth-form {
  margin-top: 8px;
}

.submit-row {
  margin-top: 8px;
  margin-bottom: 8px;
}

.submit-btn {
  width: 100%;
}

.switch-tip {
  margin: 0;
  text-align: center;
  color: #6b7280;
  font-size: 14px;
}

.demo-account {
  margin-top: 16px;
  padding: 12px;
  border-radius: 12px;
  background: #f5f7ff;
  text-align: center;
  color: #5b6470;
  font-size: 13px;
}

.demo-account p {
  margin: 4px 0;
}

.inside-page {
  min-height: 100vh;
  background: #f5f7fb;
}

.layout {
  min-height: 100vh;
}

.aside {
  background: #1f2d3d;
  color: #fff;
}

.brand {
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  font-weight: 600;
  background: #16202b;
}

.menu {
  border-right: none;
}

.header {
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
}

.header .title {
  font-size: 16px;
  font-weight: 600;
  color: #1f2d3d;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.main {
  padding: 20px;
}

.status-card {
  margin-bottom: 16px;
}

.status-title {
  font-weight: 600;
  margin-bottom: 6px;
}

.status-desc {
  color: #6b7280;
}
</style>
