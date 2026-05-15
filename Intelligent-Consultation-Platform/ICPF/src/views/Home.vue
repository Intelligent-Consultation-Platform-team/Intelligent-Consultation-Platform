<template>
  <div class="home-page">
    <el-card shadow="never" class="welcome-card">
      <template #header>
        <div class="card-header">
          <span>欢迎使用智能咨询平台</span>
          <el-tag type="success" effect="plain">{{ currentDate }}</el-tag>
        </div>
      </template>
      <div class="welcome-content">
        <h3>您好，{{ currentUser.realName || currentUser.username || '访客' }}</h3>
        <p>您当前的角色是：<el-tag type="primary">{{ getRoleLabel(currentUser.role) }}</el-tag></p>
        <p>{{ roleWelcomeText }}</p>
      </div>
    </el-card>

    <el-row v-if="isDoctor" :gutter="20" class="stats-row">
      <el-col v-for="item in doctorStats" :key="item.label" :span="8">
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
      <el-col v-for="item in patientStats" :key="item.label" :span="8">
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
      <el-col :span="8">
        <el-card shadow="never" class="stat-card">
          <div class="stat-icon users-icon"></div>
          <div class="stat-content">
            <div class="stat-number">{{ userCount }}</div>
            <div class="stat-label">总用户数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="never" class="stat-card">
          <div class="stat-icon doctors-icon"></div>
          <div class="stat-content">
            <div class="stat-number">{{ doctorCount }}</div>
            <div class="stat-label">医生数量</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
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
          <el-button type="primary" plain size="small">查看全部</el-button>
        </div>
      </template>

      <template v-if="!isDoctor">
        <el-table :data="recentActivities" style="width: 100%">
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
          <el-col v-for="item in doctorDashboard" :key="item.label" :span="8">
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
import { ElMessage } from 'element-plus'

const SESSION_KEY = 'demo_session'
const getSession = () => {
  const raw = localStorage.getItem(SESSION_KEY)
  if (!raw) return null
  try { return JSON.parse(raw) } catch { localStorage.removeItem(SESSION_KEY); return null }
}

const currentUser = ref({ username: '', realName: '', role: '' })
const currentDate = computed(() => new Date().toLocaleDateString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric', weekday: 'long' }))
const roleLabels = { admin: '管理员', doctor: '医生', patient: '患者' }
const getRoleLabel = (role) => roleLabels[role] || '未知'
const isDoctor = computed(() => currentUser.value.role === 'doctor')
const isPatient = computed(() => currentUser.value.role === 'patient')
const roleWelcomeText = computed(() => currentUser.value.role === 'doctor' ? '医生您好，请在这里查看今日接诊、待处理预约和排班信息。' : currentUser.value.role === 'patient' ? '欢迎您，请在这里查看预约、就诊和个人账户信息。' : '欢迎您，请使用左侧菜单访问对应功能。')
const userCount = ref(128)
const doctorCount = ref(32)
const appointmentCount = ref(45)
const doctorStats = computed(() => [{ label: '今日接诊', value: 18 }, { label: '待处理预约', value: 6 }, { label: '排班时段', value: 4 }])
const patientStats = computed(() => [{ label: '我的预约', value: 8 }, { label: '待就诊', value: 2 }, { label: '我的记录', value: 15 }])
const doctorDashboard = computed(() => [{ label: '今日接诊', value: 18 }, { label: '待处理预约', value: 6 }, { label: '已完成就诊', value: 24 }])
const recentActivities = ref([{ time: '2026-04-06 09:30', type: '登录', content: '用户登录系统', user: '测试用户' }, { time: '2026-04-06 08:45', type: '预约', content: '预约了内科张医生', user: '测试用户' }, { time: '2026-04-05 16:20', type: '注册', content: '新用户注册', user: '系统' }])
const getTypeTagType = (type) => ({ 登录: 'success', 预约: 'primary', 注册: 'info', 操作: 'warning' }[type] || 'default')

onMounted(() => {
  const session = getSession()
  if (session) currentUser.value = { username: session.username || '', realName: session.realName || session.username || '', role: session.role || '' }
  setTimeout(() => ElMessage.success('数据加载成功'), 1000)
})
</script>

<style scoped>
.home-page { padding: 20px 0; }
.welcome-card { margin-bottom: 20px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.welcome-content { padding: 20px 0; }
.welcome-content h3 { margin: 0 0 12px 0; color: #1f2d3d; }
.welcome-content p { margin: 8px 0; color: #6b7280; }
.stats-row { margin-bottom: 20px; }
.stat-card { height: 120px; display: flex; align-items: center; padding: 0 20px; transition: all 0.3s ease; }
.stat-card:hover { transform: translateY(-2px); box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1); }
.stat-icon { width: 60px; height: 60px; border-radius: 50%; display: flex; align-items: center; justify-content: center; margin-right: 20px; font-size: 24px; color: white; }
.users-icon { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); }
.doctors-icon { background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%); }
.appointments-icon { background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%); }
.stat-content { flex: 1; }
.stat-number { font-size: 24px; font-weight: 600; color: #1f2d3d; margin-bottom: 4px; }
.stat-label { font-size: 14px; color: #6b7280; }
.recent-activities { margin-top: 20px; }
.mini-card { text-align: center; }
.mini-value { font-size: 24px; font-weight: 600; color: #1f2d3d; }
.mini-label { margin-top: 6px; color: #6b7280; }
</style>
