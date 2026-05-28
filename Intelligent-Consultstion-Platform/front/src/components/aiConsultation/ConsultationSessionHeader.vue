<template>
  <el-card shadow="never" class="session-header">
    <div class="header-row">
      <div>
        <div class="title">AI 问诊</div>
        <div class="sub-title">会话编号：{{ sessionId || '-' }}</div>
      </div>
      <div class="actions">
        <el-tag :type="riskTagType" effect="plain">风险：{{ riskText }}</el-tag>
        <el-tag :type="statusTagType" effect="plain">{{ statusText }}</el-tag>
        <el-button type="danger" plain size="small" :disabled="disabled" @click="$emit('close')">
          结束会话
        </el-button>
      </div>
    </div>
  </el-card>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  sessionId: {
    type: String,
    default: '',
  },
  riskLevel: {
    type: String,
    default: 'unknown',
  },
  status: {
    type: String,
    default: 'active',
  },
  disabled: {
    type: Boolean,
    default: false,
  },
})

defineEmits(['close'])

const riskText = computed(() => ({ low: '低', medium: '中', high: '高' }[props.riskLevel] || '未知'))
const riskTagType = computed(() => ({ low: 'success', medium: 'warning', high: 'danger' }[props.riskLevel] || 'info'))
const statusText = computed(() => ({ active: '进行中', closed: '已结束', archived: '已归档' }[props.status] || '未知'))
const statusTagType = computed(() => ({ active: 'success', closed: 'info', archived: 'info' }[props.status] || 'info'))
</script>

<style scoped>
.session-header { margin-bottom: 16px; }
.header-row { display: flex; justify-content: space-between; align-items: center; gap: 16px; }
.title { font-size: 18px; font-weight: 600; color: #1f2d3d; }
.sub-title { margin-top: 6px; color: #6b7280; font-size: 13px; }
.actions { display: flex; align-items: center; gap: 10px; flex-wrap: wrap; }
</style>
