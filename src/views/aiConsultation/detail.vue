<template>
  <div class="detail-page">
    <div class="top-actions">
      <el-button plain @click="goBack">返回历史</el-button>
      <el-button type="primary" plain @click="goStart">新建问诊</el-button>
    </div>

    <ConsultationSessionHeader
      :session-id="sessionId"
      :risk-level="riskLevel"
      :status="status"
      :disabled="true"
    />

    <ConsultationSessionSummary
      :chief-complaint="summary.chiefComplaint"
      :summary="summary.text"
      :risk-level="riskLevel"
      :suggestion="summary.suggestion"
    />

    <ConsultationChatWindow :messages="messages" :loading="false" />
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import ConsultationSessionHeader from '../../components/aiConsultation/ConsultationSessionHeader.vue'
import ConsultationSessionSummary from '../../components/aiConsultation/ConsultationSessionSummary.vue'
import ConsultationChatWindow from '../../components/aiConsultation/ConsultationChatWindow.vue'
import { aiConsultationApi } from '../../api/aiConsultation'

const route = useRoute()
const router = useRouter()
const sessionId = route.params.sessionId || ''
const riskLevel = ref('medium')
const status = ref('closed')
const messages = ref([])
const summary = reactive({ chiefComplaint: '', text: '', suggestion: '' })

const loadDetail = async () => {
  try {
    const detail = await aiConsultationApi.getSessionDetail(sessionId)
    riskLevel.value = detail?.riskLevel || 'medium'
    status.value = detail?.status || 'closed'
    summary.chiefComplaint = detail?.chiefComplaint || ''
    summary.text = detail?.summary || ''
    summary.suggestion = detail?.suggestion || ''

    const list = await aiConsultationApi.getSessionMessages(sessionId, { page: 1, pageSize: 200 })
    messages.value = Array.isArray(list) ? list : (list?.records || list?.list || [])
  } catch (error) {
    ElMessage.error(error.message || '加载详情失败')
  }
}

const goBack = () => router.push('/ai-consultation/history')
const goStart = () => router.push('/ai-consultation')

onMounted(loadDetail)
</script>

<style scoped>
.detail-page { max-width: 980px; margin: 0 auto; padding: 20px 0; display: flex; flex-direction: column; gap: 16px; }
.top-actions { display: flex; justify-content: space-between; gap: 12px; }
</style>
