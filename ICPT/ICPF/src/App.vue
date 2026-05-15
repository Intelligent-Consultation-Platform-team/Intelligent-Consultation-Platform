<template>
  <div class="app-shell">
    <div v-if="!isAuthenticated" class="auth-screen">
      <el-card class="auth-card" shadow="hover">
        <div class="auth-hero">
          <div class="auth-badge">智能咨询平台</div>
          <h1>{{ isRegister ? '创建账号' : '欢迎回来' }}</h1>
          <p>{{ isRegister ? '请填写信息完成注册' : '请使用账号密码登录系统' }}</p>
        </div>

        <el-tabs v-model="activeTab" class="auth-tabs" stretch>
          <el-tab-pane label="登录" name="login" />
          <el-tab-pane label="注册" name="register" />
        </el-tabs>

        <el-form ref="formRef" :model="formModel" :rules="rules" label-position="top" class="auth-form">
          <el-form-item label="用户名" prop="username">
            <el-input v-model.trim="formModel.username" placeholder="请输入用户名" clearable />
          </el-form-item>

          <template v-if="isRegister">
            <el-form-item label="真实姓名" prop="realName">
              <el-input v-model.trim="formModel.realName" placeholder="请输入真实姓名" clearable />
            </el-form-item>
            <el-form-item label="手机号" prop="phone">
              <el-input v-model.trim="formModel.phone" placeholder="请输入手机号" clearable />
            </el-form-item>
            <el-form-item label="邮箱" prop="email">
              <el-input v-model.trim="formModel.email" placeholder="请输入邮箱" clearable />
            </el-form-item>
            <el-form-item label="角色" prop="role">
              <el-select v-model="formModel.role" placeholder="请选择角色" style="width: 100%">
                <el-option label="患者" value="patient" />
                <el-option label="医生" value="doctor" />
              </el-select>
            </el-form-item>
          </template>

          <el-form-item label="密码" prop="password">
            <el-input v-model="formModel.password" placeholder="请输入密码" show-password clearable />
          </el-form-item>

          <el-form-item v-if="isRegister" label="确认密码" prop="confirmPassword">
            <el-input v-model="formModel.confirmPassword" placeholder="请再次输入密码" show-password clearable />
          </el-form-item>

          <el-button type="primary" :loading="loading" class="submit-btn" @click="handleSubmit">
            {{ isRegister ? '立即注册' : '立即登录' }}
          </el-button>
        </el-form>

        <div class="auth-footer">
          <span>{{ isRegister ? '已有账号？' : '没有账号？' }}</span>
          <el-button link type="primary" @click="toggleTab">
            {{ isRegister ? '去登录' : '去注册' }}
          </el-button>
        </div>

        <el-divider content-position="center">测试账号</el-divider>
        <div class="demo-box">
          <p>注册后即可使用对应角色登录。</p>
          <el-button size="small" @click="fillDemo">一键填充</el-button>
        </div>
      </el-card>
    </div>

    <div v-else class="app-layout">
      <el-container class="layout-container">
        <el-aside width="220px" class="sidebar">
          <div class="brand">智能咨询平台</div>
          <el-menu :default-active="route.path" class="menu" router>
            <template v-for="item in filteredMenu" :key="item.index">
              <el-menu-item v-if="!item.children" :index="item.path">{{ item.label }}</el-menu-item>
              <el-sub-menu v-else :index="item.path">
                <template #title>{{ item.label }}</template>
                <el-menu-item v-for="child in item.children" :key="child.index" :index="child.path">
                  {{ child.label }}
                </el-menu-item>
              </el-sub-menu>
            </template>
          </el-menu>
        </el-aside>

        <el-container>
          <el-header class="header">
            <div>
              <div class="welcome">欢迎，{{ currentUser.realName || currentUser.username }}</div>
              <div class="role">当前角色：{{ roleText[currentUser.role] || '未知' }}</div>
            </div>
            <el-button type="danger" plain @click="logout">退出登录</el-button>
          </el-header>

          <el-main class="main">
            <router-view />
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
import { api } from './utils/api'

const SESSION_KEY = 'demo_session'
const route = useRoute()
const router = useRouter()
const formRef = ref()
const loading = ref(false)
const activeTab = ref(route.path === '/auth/register' ? 'register' : 'login')
const isAuthenticated = ref(false)

const currentUser = reactive({ username: '', realName: '', role: '' })
const formModel = reactive({ username: '', realName: '', phone: '', email: '', password: '', confirmPassword: '', role: '' })
const roleText = { admin: '管理员', doctor: '医生', patient: '患者' }
const isRegister = computed(() => activeTab.value === 'register')
const isDoctor = computed(() => currentUser.role === 'doctor')
const isPatient = computed(() => currentUser.role === 'patient')

const validateConfirmPassword = (_, value, callback) => {
  if (!isRegister.value) return callback()
  if (!value) return callback(new Error('请再次输入密码'))
  if (value !== formModel.password) return callback(new Error('两次输入的密码不一致'))
  callback()
}

const menuConfig = [
  { index: 'home', label: '系统首页', path: '/home', roles: ['admin', 'doctor', 'patient'] },
  {
    index: 'manage',
    label: '信息管理',
    path: '/manage',
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
    path: '/appoint',
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
    path: '/user',
    roles: ['admin'],
    children: [
      { index: 'admin', label: '管理员信息', path: '/user/admin', roles: ['admin'] },
      { index: 'doctor', label: '医生信息', path: '/user/doctor', roles: ['admin'] },
      { index: 'user', label: '用户信息', path: '/user/user', roles: ['admin'] },
    ],
  },
]

const filteredMenu = computed(() => {
  const role = currentUser.role
  const filterItems = (items) =>
    items
      .filter((item) => item.roles.includes(role))
      .map((item) => {
        if (!item.children) return item
        const children = filterItems(item.children)
        return children.length ? { ...item, children } : null
      })
      .filter(Boolean)
  return filterItems(menuConfig)
})

const rules = computed(() => ({
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度为 3-20 位', trigger: 'blur' },
  ],
  realName: isRegister.value ? [
    { required: true, message: '请输入真实姓名', trigger: 'blur' },
    { min: 2, max: 20, message: '真实姓名长度为 2-20 位', trigger: 'blur' }
  ] : [],
  phone: isRegister.value ? [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ] : [],
  email: isRegister.value
    ? [
        { required: true, message: '请输入邮箱', trigger: 'blur' },
        { type: 'email', message: '邮箱格式不正确', trigger: ['blur', 'change'] },
      ]
    : [],
  role: isRegister.value ? [{ required: true, message: '请选择角色', trigger: 'change' }] : [],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 32, message: '密码长度为 6-32 位', trigger: 'blur' },
  ],
  confirmPassword: isRegister.value ? [{ validator: validateConfirmPassword, trigger: ['blur', 'change'] }] : [],
}))

const getSession = () => {
  const raw = localStorage.getItem(SESSION_KEY)
  if (!raw) return null
  try { return JSON.parse(raw) } catch { localStorage.removeItem(SESSION_KEY); return null }
}

const saveSession = (data) => {
  localStorage.setItem(SESSION_KEY, JSON.stringify({ ...data, loginAt: Date.now() }))
}

const restoreSession = () => {
  const session = getSession()
  if (!session) return
  if (session.expiresIn && session.loginAt && Date.now() > session.loginAt + Number(session.expiresIn) * 1000) {
    localStorage.removeItem(SESSION_KEY)
    return
  }
  currentUser.username = session.username || ''
  currentUser.realName = session.realName || ''
  currentUser.role = session.role || ''
  isAuthenticated.value = true
}

const clearForm = () => {
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

const fillDemo = () => {
  formModel.username = 'zhangsan'
  formModel.password = '123456'
}

const handleSubmit = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
    loading.value = true

    if (isRegister.value) {
      // 确保所有必要的参数都被正确传递
      const registerData = {
        username: formModel.username.trim(),
        nickname: formModel.realName.trim(), // 后端期望的字段名是nickname
        phone: formModel.phone.trim(),
        email: formModel.email.trim(),
        password: formModel.password,
        confirmPassword: formModel.confirmPassword,
        role: formModel.role
      }
      // 打印参数，便于调试
      console.log('注册参数:', registerData)
      await api.auth.register(registerData)
      ElMessage.success('注册成功，请登录')
      activeTab.value = 'login'
      router.push('/auth/login')
      clearForm()
      return
    }

    const data = await api.auth.login({
      username: formModel.username,
      password: formModel.password,
    })

    const user = data?.user || {}
    currentUser.username = user.username || formModel.username
    currentUser.realName = user.realName || user.nickname || user.username || formModel.username
    currentUser.role = user.role || 'patient'

    saveSession({
      username: currentUser.username,
      realName: currentUser.realName,
      role: currentUser.role,
      token: data?.token || '',
      tokenType: data?.tokenType || 'Bearer',
      expiresIn: data?.expiresIn,
    })

    isAuthenticated.value = true
    ElMessage.success('登录成功')
    router.push('/home')
  } catch (error) {
    ElMessage.error(error?.message || '请求失败')
  } finally {
    loading.value = false
  }
}

const logout = () => {
  localStorage.removeItem(SESSION_KEY)
  isAuthenticated.value = false
  currentUser.username = ''
  currentUser.realName = ''
  currentUser.role = ''
  clearForm()
  router.push('/auth/login')
}

watch(
  () => route.path,
  (path) => {
    if (path === '/auth/register') activeTab.value = 'register'
    else if (path === '/auth/login') activeTab.value = 'login'
  },
  { immediate: true }
)

watch(activeTab, () => clearForm())

onMounted(() => {
  restoreSession()
})
</script>

<style scoped>
.app-shell { min-height: 100vh; }
.auth-screen {
  min-height: 100vh;
  display: grid;
  place-items: center;
  padding: 24px;
  background: linear-gradient(135deg, #edf4ff 0%, #f7faff 45%, #eef7ff 100%);
}
.auth-card { width: 100%; max-width: 480px; border-radius: 20px; }
.auth-hero { text-align: center; margin-bottom: 16px; }
.auth-badge { display: inline-block; padding: 4px 12px; border-radius: 999px; background: #ecf5ff; color: #409eff; font-size: 12px; margin-bottom: 12px; }
.auth-hero h1 { margin: 0; font-size: 28px; color: #1f2d3d; }
.auth-hero p { margin: 8px 0 0; color: #6b7280; }
.auth-form { margin-top: 8px; }
.submit-btn { width: 100%; margin-top: 8px; }
.auth-footer { display: flex; align-items: center; justify-content: center; gap: 4px; margin-top: 14px; color: #6b7280; }
.demo-box { text-align: center; color: #6b7280; font-size: 13px; }
.demo-box p { margin: 0 0 8px; }
.app-layout { min-height: 100vh; background: #f5f7fb; }
.layout-container { min-height: 100vh; }
.sidebar { background: #1f2d3d; color: #fff; }
.brand { height: 56px; display: flex; align-items: center; justify-content: center; font-size: 18px; font-weight: 600; background: #16202b; }
.menu { border-right: none; }
.header { display: flex; align-items: center; justify-content: space-between; background: #fff; padding: 0 20px; box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05); }
.welcome { font-size: 16px; font-weight: 600; }
.role { font-size: 13px; color: #6b7280; margin-top: 4px; }
.main { padding: 20px; }
</style>
