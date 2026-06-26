<template>
  <el-card v-if="visible" shadow="never" class="risk-card">
    <template #header>
      <div class="card-header">
        <span>AI 风险提示</span>
        <el-tag :type="riskTagType" effect="dark">{{ riskText }}</el-tag>
      </div>
    </template>

    <div class="risk-content">
      <p class="summary">{{ suggestion }}</p>
      <ul v-if="recommendations.length" class="recommend-list">
        <li v-for="item in recommendations" :key="item">{{ item }}</li>
      </ul>
    </div>
  </el-card>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  visible: {
    type: Boolean,
    default: true,
  },
  riskLevel: {
    type: String,
    default: 'unknown',
  },
  suggestion: {
    type: String,
    default: '',
  },
  recommendations: {
    type: Array,
    default: () => [],
  },
})

const riskText = computed(() => ({ low: '低风险', medium: '中风险', high: '高风险' }[props.riskLevel] || '未知风险'))
const riskTagType = computed(() => ({ low: 'success', medium: 'warning', high: 'danger' }[props.riskLevel] || 'info'))
</script>

<style scoped>
.risk-card { margin-top: 16px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.summary { margin: 0 0 10px; color: #374151; line-height: 1.8; }
.recommend-list { margin: 0; padding-left: 18px; color: #6b7280; line-height: 1.8; }
</style>

