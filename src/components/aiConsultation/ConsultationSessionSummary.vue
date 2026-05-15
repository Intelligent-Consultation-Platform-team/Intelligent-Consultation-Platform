<template>
  <el-card shadow="never" class="summary-card">
    <template #header>
      <div class="card-header">
        <span>会话摘要</span>
        <el-tag :type="riskTagType" effect="plain">{{ riskText }}</el-tag>
      </div>
    </template>

    <div class="summary-item"><strong>主诉：</strong>{{ chiefComplaint || '-' }}</div>
    <div class="summary-item"><strong>总结：</strong>{{ summary || '-' }}</div>
    <div class="summary-item"><strong>建议：</strong>{{ suggestion || '-' }}</div>
  </el-card>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  chiefComplaint: { type: String, default: '' },
  summary: { type: String, default: '' },
  suggestion: { type: String, default: '' },
  riskLevel: { type: String, default: 'unknown' },
})

const riskText = computed(() => ({ low: '低风险', medium: '中风险', high: '高风险' }[props.riskLevel] || '未知风险'))
const riskTagType = computed(() => ({ low: 'success', medium: 'warning', high: 'danger' }[props.riskLevel] || 'info'))
</script>

<style scoped>
.summary-card { margin-bottom: 16px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.summary-item { line-height: 1.8; color: #374151; margin-bottom: 8px; }
</style>

