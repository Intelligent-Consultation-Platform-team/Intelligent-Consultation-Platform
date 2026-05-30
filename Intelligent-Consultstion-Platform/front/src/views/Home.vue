<template>
  <div class="home-page">
    <el-card shadow="never" class="welcome-card">
      <template #header>
        <div class="card-header">
          <div>
            <div class="section-title">欢迎使用智能咨询平台</div>
            <div class="section-subtitle">今天也请保持专注，合理安排工作与就诊流程</div>
          </div>
          <el-tag type="success" effect="plain">{{ currentDate }}</el-tag>
        </div>
      </template>

      <div class="welcome-content">
        <h3>您好，{{ currentUser.realName || currentUser.username || '访客' }}</h3>
        <p>
          您当前的角色是：
          <el-tag type="primary">{{ getRoleLabel(currentUser.role) }}</el-tag>
        </p>
        <p>{{ roleWelcomeText }}</p>
      </div>
    </el-card>

    <el-row v-if="isDoctor" :gutter="20" class="stats-row">
      <el-col v-for="item in doctorStats" :key="item.label" :xs="24" :sm="8">
        <el-card shadow="never" class="stat-card">
          <div class="stat-icon doctors-icon"></div>
          <div class="stat-content">
            <div class="stat-number">{{ item.value }}</div>
            <div class="stat-label">{{ item.label }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row v-else-if="isPatient" :gutter="20" class="stats-row">
      <el-col v-for="item in patientStats" :key="item.label" :xs="24" :sm="8">
        <el-card shadow="never" class="stat-card">
          <div class="stat-icon appointments-icon"></div>
          <div class="stat-content">
            <div class="stat-number">{{ item.value }}</div>
            <div class="stat-label">{{ item.label }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row v-else :gutter="20" class="stats-row">
      <el-col :xs="24" :sm="8">
        <el-card shadow="never" class="stat-card">
          <div class="stat-icon users-icon"></div>
          <div class="stat-content">
            <div class="stat-number">{{ userCount }}</div>
            <div class="stat-label">总用户数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="8">
        <el-card shadow="never" class="stat-card">
          <div class="stat-icon doctors-icon"></div>
          <div class="stat-content">
            <div class="stat-number">{{ doctorCount }}</div>
            <div class="stat-label">医生数量</div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="8">
        <el-card shadow="never" class="stat-card">
          <div class="stat-icon appointments-icon"></div>
          <div class="stat-content">
            <div class="stat-number">{{ appointmentCount }}</div>
            <div class="stat-label">今日预约</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" class="recent-activities">
      <template #header>
        <div class="card-header">
          <span>{{ isDoctor ? '医生工作台' : '最近活动' }}</span>
          <div class="header-actions">
            <el-button type="primary" plain size="small" @click="goAiConsultation">AI 问诊</el-button>
            <el-button type="primary" plain size="small">查看全部</el-button>
          </div>
        </div>
      </template>

      <template v-if="!isDoctor">
        <el-table v-loading="activitiesLoading" :data="recentActivities" style="width: 100%" empty-text="暂无活动记录">
          <el-table-column prop="time" label="时间" width="180" />
          <el-table-column prop="type" label="类型" width="120">
            <template #default="scope">
              <el-tag :type="getTypeTagType(scope.row.type)">{{ scope.row.type }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="content" label="内容" />
          <el-table-column prop="user" label="操作人" width="120" />
        </el-table>
      </template>

      <template v-else>
        <el-row :gutter="20">
          <el-col v-for="item in doctorDashboard" :key="item.label" :xs="24" :sm="8">
            <el-card shadow="never" class="mini-card">
              <div class="mini-value">{{ item.value }}</div>
              <div class="mini-label">{{ item.label }}</div>
            </el-card>
          </el-col>
        </el-row>
      </template>
    </el-card>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { api } from '../utils/api'

const SESSION_KEY = 'demo_session'
const getSession = () => {
  const raw = localStorage.getItem(SESSION_KEY)
  if (!raw) return null
  try {
    return JSON.parse(raw)
  } catch {
    localStorage.removeItem(SESSION_KEY)
    return null
  }
}

const router = useRouter()
const currentUser = ref({ username: '', realName: '', role: '' })
const currentDate = computed(() =>
  new Date().toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    weekday: 'long',
  }),
)
const roleLabels = { admin: '管理员', doctor: '医生', patient: '患者' }
const getRoleLabel = (role) => roleLabels[role] || '未知'
const isDoctor = computed(() => currentUser.value.role === 'doctor')
const isPatient = computed(() => currentUser.value.role === 'patient')
const roleWelcomeText = computed(() =>
  currentUser.value.role === 'doctor'
    ? '医生您好，请在这里查看今日接诊、待处理预约和排班信息。'
    : currentUser.value.role === 'patient'
      ? '欢迎您，请在这里查看预约、就诊和个人账户信息。'
      : '欢迎您，请使用左侧菜单访问对应功能。',
)

// 统计数据（从API加载）
const userCount = ref(0)
const doctorCount = ref(0)
const appointmentCount = ref(0)
const patientStats = computed(() => [
  { label: '我的预约', value: appointmentCount.value },
  { label: '待就诊', value: 0 },
  { label: '我的记录', value: 0 },
])
const doctorStats = computed(() => [
  { label: '今日接诊', value: 0 },
  { label: '待处理预约', value: 0 },
  { label: '排班时段', value: 0 },
])
const doctorDashboard = computed(() => [
  { label: '今日接诊', value: 0 },
  { label: '待处理预约', value: 0 },
  { label: '已完成就诊', value: 0 },
])

// 最近活动
const recentActivities = ref([])
const activitiesLoading = ref(false)

// 加载统计数据
const loadStatistics = async () => {
  try {
    // 获取用户总数
    const usersData = await api.users.getList()
    userCount.value = Array.isArray(usersData) ? usersData.length : 0

    // 获取医生列表
    const doctorsData = await api.doctors.getList()
    const doctors = Array.isArray(doctorsData) ? doctorsData : []
    doctorCount.value = doctors.length

    // 如果是患者，加载预约数量
    const session = getSession()
    if (session?.userId && isPatient.value) {
      const appointmentsData = await api.appointment.getPatientList(session.userId)
      appointmentCount.value = Array.isArray(appointmentsData) ? appointmentsData.length : 0
    }
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}

// 加载最近活动
const loadRecentActivities = async () => {
  activitiesLoading.value = true
  try {
    const session = getSession()
    if (!session?.userId) {
      recentActivities.value = []
      return
    }

    // 获取患者的预约记录作为最近活动
    const appointmentsData = await api.appointment.getPatientList(session.userId)
    if (Array.isArray(appointmentsData)) {
      recentActivities.value = appointmentsData.slice(0, 5).map(item => ({
        time: item.appointmentDate || item.createdAt || new Date().toLocaleString(),
        type: '预约',
        content: `预约了 ${item.doctorName || '医生'} (${item.deptName || '科室'})`,
        user: currentUser.value.realName || currentUser.value.username || '用户'
      }))
    } else {
      recentActivities.value = []
    }
  } catch (error) {
    console.error('加载最近活动失败:', error)
    recentActivities.value = []
  } finally {
    activitiesLoading.value = false
  }
}

const getTypeTagType = (type) => ({ 登录: 'success', 预约: 'primary', 注册: 'info', 操作: 'warning' }[type] || 'default')
const goAiConsultation = () => router.push('/ai-consultation')

onMounted(() => {
  const session = getSession()
  if (session) {
    currentUser.value = {
      username: session.username || '',
      realName: session.realName || session.username || '',
      role: session.role || ''
    }
  }

  // 加载数据
  loadStatistics()
  loadRecentActivities()
})
</script>

<style scoped>
.home-page {
  padding: 20px 0;
}

.welcome-card,
.recent-activities {
  border-radius: 16px;
}

.welcome-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.section-title {
  font-size: 16px;
  font-weight: 700;
  color: #1f2d3d;
}

.section-subtitle {
  margin-top: 4px;
  font-size: 13px;
  color: #6b7280;
}

.header-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.welcome-content {
  padding: 8px 0 0;
}

.welcome-content h3 {
  margin: 0 0 12px;
  color: #1f2d3d;
}

.welcome-content p {
  margin: 8px 0;
  color: #6b7280;
  line-height: 1.7;
}

.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  min-height: 120px;
  display: flex;
  align-items: center;
  padding: 0 20px;
  border-radius: 16px;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 24px rgba(15, 23, 42, 0.08);
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 16px;
  margin-right: 16px;
}

.users-icon {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.doctors-icon {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.appointments-icon {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.stat-content {
  flex: 1;
}

.stat-number {
  font-size: 24px;
  font-weight: 700;
  color: #1f2d3d;
  line-height: 1.2;
}

.stat-label {
  margin-top: 4px;
  font-size: 13px;
  color: #6b7280;
}

.recent-activities {
  margin-top: 20px;
}

.mini-card {
  text-align: center;
  border-radius: 16px;
}

.mini-value {
  font-size: 24px;
  font-weight: 700;
  color: #1f2d3d;
}

.mini-label {
  margin-top: 6px;
  color: #6b7280;
}

@media (max-width: 768px) {
  .card-header {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>
