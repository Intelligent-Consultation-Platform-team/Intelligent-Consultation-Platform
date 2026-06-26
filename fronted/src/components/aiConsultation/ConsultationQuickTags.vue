<template>
  <div class="quick-tags">
    <span class="label">快捷症状：</span>
    <el-space wrap>
      <el-tag v-for="tag in tags" :key="tag" class="tag" effect="plain" @click="handleSelect(tag)">
        {{ tag }}
      </el-tag>
    </el-space>
  </div>
</template>

<script setup>
const props = defineProps({
  tags: {
    type: Array,
    default: () => [],
  },
})

const emit = defineEmits(['select'])

const handleSelect = (tag) => emit('select', tag)
</script>

<style scoped>
.quick-tags { margin-bottom: 16px; }
.label { display: inline-block; margin-right: 8px; color: #6b7280; }
.tag { cursor: pointer; user-select: none; }
</style>
