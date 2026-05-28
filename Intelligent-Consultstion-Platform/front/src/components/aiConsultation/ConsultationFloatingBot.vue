<template>
  <div
    v-if="visibleModel"
    ref="botRef"
    class="floating-bot"
    :style="botStyle"
    @pointerdown="handlePointerDown"
  >
    <div class="bot-header">
      <span class="bot-title">AI 助手</span>
      <el-button link type="primary" size="small" @click.stop="toggleVisible">隐藏</el-button>
    </div>

    <ConsultationRobotAvatar size="sm" />

    <div class="bot-bubble">
      <div class="bot-text">我是您的智能问诊助手，拖动我可以放到合适的位置。</div>
      <el-button type="primary" size="small" class="bot-action" @click="goConsultation">开始问诊</el-button>
    </div>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import ConsultationRobotAvatar from './ConsultationRobotAvatar.vue'

const props = defineProps({
  visible: {
    type: Boolean,
    default: true,
  },
})

const emit = defineEmits(['update:visible'])
const router = useRouter()
const botRef = ref(null)
const position = ref({ x: window.innerWidth - 170, y: window.innerHeight - 220 })
const dragging = ref(false)
const offset = ref({ x: 0, y: 0 })

const visibleModel = computed({
  get: () => props.visible,
  set: (val) => emit('update:visible', val),
})

const botStyle = computed(() => ({
  left: `${Math.max(12, position.value.x)}px`,
  top: `${Math.max(12, position.value.y)}px`,
}))

const clampPosition = () => {
  const el = botRef.value
  const width = el?.offsetWidth || 160
  const height = el?.offsetHeight || 220
  position.value.x = Math.min(Math.max(12, position.value.x), window.innerWidth - width - 12)
  position.value.y = Math.min(Math.max(12, position.value.y), window.innerHeight - height - 12)
}

const handlePointerMove = (event) => {
  if (!dragging.value) return
  position.value.x = event.clientX - offset.value.x
  position.value.y = event.clientY - offset.value.y
  clampPosition()
}

const handlePointerUp = () => {
  dragging.value = false
  window.removeEventListener('pointermove', handlePointerMove)
  window.removeEventListener('pointerup', handlePointerUp)
}

const handlePointerDown = (event) => {
  if (event.button !== 0) return
  const el = botRef.value
  if (!el) return
  dragging.value = true
  const rect = el.getBoundingClientRect()
  offset.value = { x: event.clientX - rect.left, y: event.clientY - rect.top }
  window.addEventListener('pointermove', handlePointerMove)
  window.addEventListener('pointerup', handlePointerUp)
}

const toggleVisible = () => {
  visibleModel.value = false
}

const goConsultation = () => {
  router.push('/ai-consultation')
}

const onResize = () => clampPosition()

onMounted(() => {
  window.addEventListener('resize', onResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', onResize)
  handlePointerUp()
})
</script>

<style scoped>
.floating-bot {
  position: fixed;
  z-index: 3000;
  width: 190px;
  padding: 12px;
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.96);
  box-shadow: 0 16px 40px rgba(31, 45, 61, 0.18);
  border: 1px solid rgba(156, 196, 255, 0.45);
  backdrop-filter: blur(8px);
  cursor: grab;
  user-select: none;
}

.floating-bot:active { cursor: grabbing; }
.bot-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 8px; }
.bot-title { font-weight: 600; color: #1f2d3d; }
.bot-bubble { margin-top: 8px; padding: 10px; border-radius: 14px; background: linear-gradient(180deg, #f8fbff 0%, #eef6ff 100%); }
.bot-text { font-size: 13px; color: #526071; line-height: 1.6; margin-bottom: 8px; }
.bot-action { width: 100%; }
</style>
