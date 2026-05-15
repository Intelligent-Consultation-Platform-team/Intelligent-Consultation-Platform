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

      <ConsultationHistoryList :items="sessions" @view-detail="handleViewDetail" />
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import ConsultationHistoryList from '../../components/aiConsultation/ConsultationHistoryList.vue'
import { aiConsultationApi } from '../../api/aiConsultation'

const router = useRouter()
const sessions = ref([])

const loadHistory = async () => {
  try {
    const res = await aiConsultationApi.getMySessions({ page: 1, pageSize: 20 })
    sessions.value = res?.list || res?.data?.list || []
  } catch (error) {
    ElMessage.error(error.message || '加载历史记录失败')
  }
}

const handleViewDetail = (row) => {
  router.push(`/ai-consultation/detail/${row.sessionId}`)
}

const goStart = () => router.push('/ai-consultation')

onMounted(loadHistory)
</script>

<style scoped>
.history-page { max-width: 980px; margin: 0 auto; padding: 20px 0; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.actions { display: flex; gap: 8px; }
</style>
