<template>
  <el-drawer v-model="visibleModel" title="上传资料" size="360px">
    <el-upload
      drag
      action="#"
      :auto-upload="false"
      :show-file-list="false"
      :before-upload="beforeUpload"
      :on-change="handleChange"
    >
      <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
      <div class="el-upload__text">将图片或文件拖到此处，或<em>点击上传</em></div>
    </el-upload>

    <div v-if="previewName" class="preview-box">
      <el-tag type="info" effect="plain">{{ previewName }}</el-tag>
    </div>
  </el-drawer>
</template>

<script setup>
import { computed, ref } from 'vue'
import { UploadFilled } from '@element-plus/icons-vue'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
})
const emit = defineEmits(['update:modelValue', 'change'])

const previewName = ref('')
const visibleModel = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val),
})
const beforeUpload = () => false
const handleChange = (file) => {
  previewName.value = file?.name || ''
  emit('change', file?.raw || file)
}
</script>

<style scoped>
.preview-box { margin-top: 16px; }
</style>

