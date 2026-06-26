<template>
  <el-card shadow="never" class="chat-window">
    <div ref="scrollRef" class="scroll-area">
      <el-empty v-if="!messages.length && !loading" description="暂无消息，开始你的问诊吧" />
      <div v-else>
        <ConsultationMessageItem v-for="item in messages" :key="item.messageId || item.id" :message="item" />
        <div v-if="loading" class="loading-text">AI 正在思考中...</div>
      </div>
    </div>
  </el-card>
</template>

<script setup>
import { nextTick, onMounted, ref, watch } from 'vue'
import ConsultationMessageItem from './ConsultationMessageItem.vue'

const props = defineProps({
  messages: {
    type: Array,
    default: () => [],
  },
  loading: {
    type: Boolean,
    default: false,
  },
})

const scrollRef = ref(null)

const scrollToBottom = async () => {
  await nextTick()
  const el = scrollRef.value
  if (el) el.scrollTop = el.scrollHeight
}

watch(() => props.messages.length, scrollToBottom)
watch(() => props.loading, scrollToBottom)
onMounted(scrollToBottom)
</script>

<style scoped>
.chat-window { flex: 1; min-height: 420px; }
.scroll-area { height: 100%; max-height: 520px; overflow-y: auto; padding-right: 8px; }
.loading-text { color: #6b7280; font-size: 13px; padding: 8px 0; }
</style>

