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
            label="昵称"
            prop="nickname"
          >
            <el-input
              v-model.trim="formModel.nickname"
              placeholder="请输入昵称"
              clearable
            />
          </el-form-item>

          <el-form-item label="邮箱" prop="email">
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
          <el-menu default-active="home" class="menu" unique-opened>
            <el-menu-item index="home">系统首页</el-menu-item>
            <el-sub-menu index="manage">
              <template #title>信息管理</template>
              <el-menu-item index="notice">公告信息</el-menu-item>
              <el-menu-item index="office">科室信息</el-menu-item>
              <el-menu-item index="schedule">医生排班</el-menu-item>
            </el-sub-menu>
            <el-sub-menu index="appoint">
              <template #title>预约就诊</template>
              <el-menu-item index="book">预约挂号</el-menu-item>
              <el-menu-item index="patient-book">患者挂号</el-menu-item>
              <el-menu-item index="visit">患者就诊</el-menu-item>
              <el-menu-item index="hospital">住院登记</el-menu-item>
            </el-sub-menu>
            <el-sub-menu index="user">
              <template #title>用户管理</template>
              <el-menu-item index="admin">管理员信息</el-menu-item>
              <el-menu-item index="doctor">医生信息</el-menu-item>
              <el-menu-item index="user">用户信息</el-menu-item>
            </el-sub-menu>
          </el-menu>
        </el-aside>

        <el-container>
          <el-header class="header">
            <div class="title">欢迎，{{ currentUser.nickname || currentUser.username }}</div>
            <div class="header-actions">
              <el-tag type="success">已登录</el-tag>
              <el-button type="danger" plain size="small" @click="logout">
                退出登录
              </el-button>
            </div>
          </el-header>

          <el-main class="main">
            <el-card shadow="never">
              <h3>页面完成情况预览</h3>
              <p>你已成功进入系统内部页面，这里可以展示各模块完成情况。</p>
              <el-divider />
              <el-row :gutter="16">
                <el-col :span="12">
                  <el-card shadow="never" class="status-card">
                    <div class="status-title">用户管理</div>
                    <div class="status-desc">已完成基础布局与列表展示</div>
                  </el-card>
                </el-col>
                <el-col :span="12">
                  <el-card shadow="never" class="status-card">
                    <div class="status-title">预约就诊</div>
                    <div class="status-desc">待对接后端接口</div>
                  </el-card>
                </el-col>
                <el-col :span="12">
                  <el-card shadow="never" class="status-card">
                    <div class="status-title">信息管理</div>
                    <div class="status-desc">页面结构已完成，数据待补充</div>
                  </el-card>
                </el-col>
                <el-col :span="12">
                  <el-card shadow="never" class="status-card">
                    <div class="status-title">系统首页</div>
                    <div class="status-desc">可添加统计卡片与图表</div>
                  </el-card>
                </el-col>
              </el-row>
            </el-card>
          </el-main>
        </el-container>
      </el-container>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'

const activeTab = ref('login')
const formRef = ref()
const submitting = ref(false)
const isLoggedIn = ref(false)
const currentUser = reactive({
  username: '',
  nickname: '',
  email: '',
})

const formModel = reactive({
  username: '',
  nickname: '',
  email: '',
  password: '',
  confirmPassword: '',
})

const tabOptions = [
  { label: '登录', value: 'login' },
  { label: '注册', value: 'register' },
]

const isRegister = computed(() => activeTab.value === 'register')

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
  nickname: isRegister.value
    ? [
        { required: true, message: '请输入昵称', trigger: 'blur' },
        { min: 2, max: 20, message: '昵称长度为 2-20 位', trigger: 'blur' },
      ]
    : [],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '邮箱格式不正确', trigger: ['blur', 'change'] },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 32, message: '密码长度为 6-32 位', trigger: 'blur' },
  ],
  confirmPassword: isRegister.value
    ? [{ validator: validateConfirmPassword, trigger: ['blur', 'change'] }]
    : [],
}))

const storageKeys = {
  users: 'demo_users',
  session: 'demo_session',
}

const seedDemoAccount = () => {
  const users = loadUsers()
  if (users.length === 0) {
    users.push({
      id: 1,
      username: 'test',
      nickname: '测试用户',
      email: 'test@demo.com',
      password: '123456',
    })
    saveUsers(users)
  }
}

const loadUsers = () => {
  const raw = localStorage.getItem(storageKeys.users)
  return raw ? JSON.parse(raw) : []
}

const saveUsers = (users) => {
  localStorage.setItem(storageKeys.users, JSON.stringify(users))
}

const saveSession = (user) => {
  localStorage.setItem(storageKeys.session, JSON.stringify(user))
}

const restoreSession = () => {
  const raw = localStorage.getItem(storageKeys.session)
  if (!raw) return
  const user = JSON.parse(raw)
  currentUser.username = user.username
  currentUser.nickname = user.nickname
  currentUser.email = user.email
  isLoggedIn.value = true
}

watch(activeTab, () => {
  resetForm()
})

const resetForm = () => {
  formModel.username = ''
  formModel.nickname = ''
  formModel.email = ''
  formModel.password = ''
  formModel.confirmPassword = ''
  formRef.value?.clearValidate()
}

const toggleTab = () => {
  activeTab.value = isRegister.value ? 'login' : 'register'
}

const fillDemoAccount = () => {
  formModel.username = 'test'
  formModel.email = 'test@demo.com'
  formModel.password = '123456'
}

const handleSubmit = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    submitting.value = true

    await new Promise((resolve) => setTimeout(resolve, 700))

    if (isRegister.value) {
      const users = loadUsers()
      const usernameExists = users.some((u) => u.username === formModel.username)
      const emailExists = users.some((u) => u.email === formModel.email)

      if (usernameExists) {
        ElMessage.error('用户名已存在')
        return
      }
      if (emailExists) {
        ElMessage.error('邮箱已注册')
        return
      }

      const newUser = {
        id: Date.now(),
        username: formModel.username,
        nickname: formModel.nickname,
        email: formModel.email,
        password: formModel.password,
      }
      users.push(newUser)
      saveUsers(users)

      ElMessage.success(`注册成功，欢迎你：${formModel.nickname}`)
      activeTab.value = 'login'
      return
    }

    const users = loadUsers()
    const matched = users.find(
      (u) =>
        (u.username === formModel.username || u.email === formModel.email) &&
        u.password === formModel.password
    )

    if (!matched) {
      ElMessage.error('账号或密码错误')
      return
    }

    currentUser.username = matched.username
    currentUser.nickname = matched.nickname
    currentUser.email = matched.email
    isLoggedIn.value = true
    saveSession({
      username: matched.username,
      nickname: matched.nickname,
      email: matched.email,
    })

    ElMessage.success(`登录成功，欢迎回来：${matched.nickname || matched.username}`)
  } catch {
    ElMessage.warning('请先完成表单信息')
  } finally {
    submitting.value = false
  }
}

const logout = () => {
  localStorage.removeItem(storageKeys.session)
  isLoggedIn.value = false
  currentUser.username = ''
  currentUser.nickname = ''
  currentUser.email = ''
  activeTab.value = 'login'
  resetForm()
}

onMounted(() => {
  seedDemoAccount()
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
