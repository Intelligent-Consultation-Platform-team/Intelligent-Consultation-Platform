<template>
  <div :class="['message-item', message.role]">
    <div v-if="message.role !== 'user'" class="avatar">AI</div>
    <div class="bubble">
      <div class="meta">
        <span>{{ roleText }}</span>
        <span>{{ message.createdAt || '' }}</span>
      </div>
      <div class="content">{{ message.content }}</div>
    </div>
    <div v-if="message.role === 'user'" class="avatar user-avatar">我</div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  message: {
    type: Object,
    required: true,
  },
})

const roleText = computed(() => ({ user: '患者', assistant: 'AI 助手', system: '系统' }[props.message.role] || '消息'))
</script>

<style scoped>
.message-item { display: flex; gap: 10px; margin-bottom: 14px; align-items: flex-start; }
.message-item.user { justify-content: flex-end; }
.avatar { width: 36px; height: 36px; border-radius: 50%; background: #409eff; color: #fff; display: flex; align-items: center; justify-content: center; font-size: 12px; flex-shrink: 0; }
.user-avatar { background: #67c23a; }
.bubble { max-width: 72%; background: #f8fafc; border: 1px solid #e5e7eb; border-radius: 12px; padding: 10px 12px; }
.message-item.user .bubble { background: #ecf5ff; }
.meta { display: flex; justify-content: space-between; gap: 12px; color: #9ca3af; font-size: 12px; margin-bottom: 6px; }
.content { white-space: pre-wrap; line-height: 1.8; color: #1f2d3d; }
</style>
