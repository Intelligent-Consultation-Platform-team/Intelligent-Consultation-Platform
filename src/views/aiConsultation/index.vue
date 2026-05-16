<template>
  <div class="ai-consultation-entry">
    <el-card shadow="never" class="entry-card">
      <template #header>
        <div class="card-header">
          <span>AI 问诊</span>
          <el-tag type="success" effect="plain">智能初筛</el-tag>
        </div>
      </template>

      <div class="entry-body">
        <div class="hero">
          <ConsultationRobotAvatar size="lg" />
          <div class="hero-text">
            <h3>我是您的智能问诊助手</h3>
            <p>请描述您的症状、持续时间和伴随表现，我会帮助您进行初步分析。</p>
          </div>
        </div>

        <el-alert
          title="提示：AI 结果仅供参考，不能替代医生诊断"
          type="warning"
          :closable="false"
          show-icon
          class="mb16"
        />

        <el-space wrap class="mb16">
          <el-button type="primary" @click="goChat">进入问诊大厅</el-button>
          <el-button @click="goHistory">查看历史问诊</el-button>
        </el-space>

        <ConsultationQuickTags :tags="quickTags" @select="handleTagSelect" />

        <el-input
          v-model="chiefComplaint"
          type="textarea"
          :rows="5"
          maxlength="500"
          show-word-limit
          placeholder="例如：头痛、发热两天，伴随咳嗽和乏力"
          class="mb16"
        />

        <div class="action-row">
          <el-button type="primary" :loading="submitting" @click="handleStartConsultation">
            开始问诊
          </el-button>
          <el-button @click="goHistory">查看历史问诊</el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import ConsultationQuickTags from '../../components/aiConsultation/ConsultationQuickTags.vue'
import ConsultationRobotAvatar from '../../components/aiConsultation/ConsultationRobotAvatar.vue'
import { aiConsultationApi } from '../../api/aiConsultation'

const router = useRouter()
const chiefComplaint = ref('')
const submitting = ref(false)

const quickTags = ['发热', '咳嗽', '头痛', '腹痛', '乏力', '胸闷', '咽痛', '恶心']

const handleTagSelect = (tag) => {
  chiefComplaint.value = chiefComplaint.value ? `${chiefComplaint.value}，${tag}` : tag
}

const handleStartConsultation = async () => {
  if (!chiefComplaint.value.trim()) {
    ElMessage.warning('请先输入主诉或症状')
    return
  }

  submitting.value = true
  try {
    const res = await aiConsultationApi.createSession({
      chiefComplaint: chiefComplaint.value.trim(),
      symptomTags: quickTags.filter((tag) => chiefComplaint.value.includes(tag)),
    })
    const sessionId = res?.sessionId || res?.data?.sessionId
    ElMessage.success('会话创建成功')
    if (sessionId) {
      router.push(`/ai-consultation/chat/${sessionId}`)
    }
  } catch (error) {
    ElMessage.error(error.message || '创建会话失败')
  } finally {
    submitting.value = false
  }
}

const goHistory = () => {
  router.push('/ai-consultation/history')
}
const goChat = () => {
  router.push('/ai-consultation')
}
</script>

<style scoped>
.ai-consultation-entry { padding: 20px 0; }
.entry-card { max-width: 960px; margin: 0 auto; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.entry-body { padding: 8px 0; }
.hero { display: flex; align-items: center; gap: 18px; margin-bottom: 16px; padding: 18px; border-radius: 16px; background: linear-gradient(180deg, #f8fbff 0%, #eef6ff 100%); }
.hero-text h3 { margin: 0 0 8px; color: #1f2d3d; }
.hero-text p { margin: 0; color: #6b7280; line-height: 1.8; }
.mb16 { margin-bottom: 16px; }
.action-row { display: flex; gap: 12px; }
@media (max-width: 640px) { .hero { flex-direction: column; align-items: flex-start; } }
</style>
