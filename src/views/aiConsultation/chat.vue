<template>
  <div class="chat-page">
    <ConsultationSessionHeader
      :session-id="sessionId"
      :risk-level="riskLevel"
      :status="status"
      :disabled="status !== 'active'"
      @close="handleCloseSession"
    />

    <div class="chat-layout">
      <div class="main-panel">
        <ConsultationChatWindow :messages="messages" :loading="sending" />

        <ConsultationRiskCard
          :visible="Boolean(suggestion)"
          :risk-level="riskLevel"
          :suggestion="suggestion"
          :recommendations="recommendations"
        />
      </div>

      <div class="side-panel">
        <el-card shadow="never" class="side-card">
          <template #header>
            <span>问诊引导</span>
          </template>
          <el-space direction="vertical" fill>
            <el-tag effect="plain">请描述症状出现时间</el-tag>
            <el-tag effect="plain">说明是否发热、咳嗽、胸闷</el-tag>
            <el-tag effect="plain">补充既往病史与用药情况</el-tag>
          </el-space>
        </el-card>

        <el-card shadow="never" class="side-card">
          <template #header>
            <span>快捷操作</span>
          </template>
          <el-space direction="vertical" fill>
            <el-button plain @click="handleUpload">上传图片</el-button>
            <el-button plain @click="handleVoice">语音输入</el-button>
            <el-button plain @click="goHistory">历史记录</el-button>
          </el-space>
        </el-card>
      </div>
    </div>

    <ConsultationInputBar
      v-model="draft"
      :loading="sending"
      :disabled="status !== 'active'"
      @send="handleSendMessage"
      @upload="handleUpload"
      @voice="handleVoice"
    />

    <ConsultationUploadPanel v-model="uploadVisible" @change="handleFileChange" />
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import ConsultationSessionHeader from '../../components/aiConsultation/ConsultationSessionHeader.vue'
import ConsultationRiskCard from '../../components/aiConsultation/ConsultationRiskCard.vue'
import ConsultationChatWindow from '../../components/aiConsultation/ConsultationChatWindow.vue'
import ConsultationInputBar from '../../components/aiConsultation/ConsultationInputBar.vue'
import ConsultationUploadPanel from '../../components/aiConsultation/ConsultationUploadPanel.vue'
import { aiConsultationApi } from '../../api/aiConsultation'

const route = useRoute()
const router = useRouter()
const sessionId = route.params.sessionId || ''
const messages = ref([])
const draft = ref('')
const sending = ref(false)
const uploadVisible = ref(false)
const riskLevel = ref('medium')
const status = ref('active')
const suggestion = ref('建议继续观察症状变化，如出现持续高热、胸痛、气促，请及时就医。')
const recommendations = ref(['建议就诊科室：呼吸内科', '如症状加重，请尽快线下就诊'])

const loadSession = async () => {
  if (!sessionId) return
  try {
    const detail = await aiConsultationApi.getSessionDetail(sessionId)
    const data = detail?.data || detail
    riskLevel.value = data?.riskLevel || 'medium'
    status.value = data?.status || 'active'
    suggestion.value = data?.summary || suggestion.value

    const list = await aiConsultationApi.getSessionMessages(sessionId, { page: 1, pageSize: 50 })
    messages.value = list?.list || list?.data?.list || []
  } catch (error) {
    ElMessage.error(error.message || '加载会话失败')
  }
}

const handleSendMessage = async (content) => {
  if (!content.trim() || sending.value || status.value !== 'active') return
  sending.value = true
  try {
    const res = await aiConsultationApi.sendMessage({ sessionId, content, contentType: 'text' })
    const data = res?.data || res
    if (data?.userMessage) messages.value.push(data.userMessage)
    if (data?.assistantMessage) messages.value.push(data.assistantMessage)
    if (data?.riskLevel) riskLevel.value = data.riskLevel
    if (data?.suggestion) suggestion.value = data.suggestion
  } catch (error) {
    ElMessage.error(error.message || '发送消息失败')
  } finally {
    sending.value = false
  }
}

const handleCloseSession = async () => {
  try {
    await aiConsultationApi.closeSession(sessionId, { closeReason: '用户主动结束问诊' })
    status.value = 'closed'
    ElMessage.success('会话已结束')
  } catch (error) {
    ElMessage.error(error.message || '结束会话失败')
  }
}

const handleUpload = () => {
  uploadVisible.value = true
}
const handleVoice = () => ElMessage.info('语音功能待后续扩展')
const handleFileChange = async (file) => {
  if (!file) return
  ElMessage.success(`已选择文件：${file.name}`)
  try {
    await aiConsultationApi.uploadFile({ fileName: file.name })
  } catch {
    ElMessage.info('文件上传接口待后端联调')
  }
}
const goHistory = () => router.push('/ai-consultation/history')

onMounted(loadSession)
</script>

<style scoped>
.chat-page { max-width: 1200px; margin: 0 auto; padding: 20px 0; display: flex; flex-direction: column; gap: 16px; }
.chat-layout { display: grid; grid-template-columns: 1fr 280px; gap: 16px; align-items: start; }
.main-panel { min-width: 0; }
.side-panel { display: flex; flex-direction: column; gap: 16px; }
.side-card :deep(.el-card__body) { display: flex; flex-direction: column; gap: 10px; }
@media (max-width: 992px) {
  .chat-layout { grid-template-columns: 1fr; }
  .side-panel { order: -1; }
}
</style>
