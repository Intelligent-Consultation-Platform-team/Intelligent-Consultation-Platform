<template>
  <div class="history-page">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>AI 问诊历史</span>
          <div class="actions">
            <el-button plain @click="loadHistory">刷新</el-button>
            <el-button type="primary" plain @click="goStart">新建问诊</el-button>
          </div>
        </div>
      </template>

      <el-empty v-if="!sessions.length && !loading" description="暂无历史问诊记录" />
      <el-table v-else v-loading="loading" :data="sessions" style="width: 100%">
        <el-table-column prop="sessionId" label="会话编号" width="180" show-overflow-tooltip />
        <el-table-column v-if="isAdmin" prop="username" label="用户" width="100">
          <template #default="scope">
            {{ scope.row.username || scope.row.userId || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="chiefComplaint" label="主诉" min-width="150" show-overflow-tooltip>
          <template #default="scope">
            {{ scope.row.chiefComplaint || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="riskLevel" label="风险" width="80">
          <template #default="scope">
            <el-tag v-if="scope.row.riskLevel" :type="scope.row.riskLevel === 'high' ? 'danger' : scope.row.riskLevel === 'medium' ? 'warning' : 'success'" size="small">
              {{ scope.row.riskLevel === 'high' ? '高' : scope.row.riskLevel === 'medium' ? '中' : '低' }}
            </el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90">
          <template #default="scope">
            <el-tag :type="scope.row.status === 'active' ? 'success' : 'info'" size="small">
              {{ scope.row.status === 'active' ? '进行中' : '已结束' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="170" />
        <el-table-column label="操作" width="80">
          <template #default="scope">
            <el-button type="primary" link @click="handleViewDetail(scope.row)">查看</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { aiConsultationApi } from '../../api/aiConsultation'

const router = useRouter()
const sessions = ref([])
const loading = ref(false)

const getSession = () => {
  try {
    return JSON.parse(localStorage.getItem('demo_session') || '{}')
  } catch {
    return {}
  }
}
const isAdmin = computed(() => getSession().role === 'admin')

const loadHistory = async () => {
  loading.value = true
  try {
    const params = { page: 1, pageSize: 200 }
    const res = isAdmin.value
      ? await aiConsultationApi.getAllSessions(params)
      : await aiConsultationApi.getMySessions(params)
    sessions.value = res || []
  } catch (error) {
    ElMessage.error(error.message || '加载历史记录失败')
  } finally {
    loading.value = false
  }
}

const handleViewDetail = (row) => {
  router.push(`/ai-consultation/detail/${row.sessionId}`)
}

const goStart = () => router.push('/ai-consultation')

onMounted(loadHistory)
</script>

<style scoped>
.history-page { max-width: 1100px; margin: 0 auto; padding: 20px 0; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.actions { display: flex; gap: 8px; }
</style>
