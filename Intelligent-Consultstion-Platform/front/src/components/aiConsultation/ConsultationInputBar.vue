<template>
  <el-card shadow="never" class="input-bar">
    <el-input
      v-model="text"
      type="textarea"
      :rows="4"
      maxlength="500"
      show-word-limit
      placeholder="请输入你的症状、持续时间、伴随症状等信息"
      :disabled="disabled"
      @keydown.enter.exact.prevent="handleSend"
    />
    <div class="toolbar">
      <div class="left-tools">
        <el-button size="small" plain :disabled="disabled" @click="$emit('upload')">上传图片</el-button>
        <el-button size="small" plain :disabled="disabled" @click="$emit('voice')">语音输入</el-button>
      </div>
      <el-button type="primary" :loading="loading" :disabled="disabled || !text.trim()" @click="handleSend">
        发送
      </el-button>
    </div>
  </el-card>
</template>

<script setup>
import { ref, watch } from 'vue'

const props = defineProps({
  modelValue: {
    type: String,
    default: '',
  },
  loading: {
    type: Boolean,
    default: false,
  },
  disabled: {
    type: Boolean,
    default: false,
  },
})

const emit = defineEmits(['update:modelValue', 'send', 'upload', 'voice'])
const text = ref(props.modelValue)

watch(() => props.modelValue, (val) => {
  text.value = val
})

watch(text, (val) => emit('update:modelValue', val))

const handleSend = () => {
  if (!text.value.trim()) return
  emit('send', text.value.trim())
  text.value = ''
}
</script>

<style scoped>
.input-bar { margin-top: 16px; }
.toolbar { margin-top: 12px; display: flex; justify-content: space-between; align-items: center; gap: 12px; }
.left-tools { display: flex; gap: 8px; flex-wrap: wrap; }
</style>

