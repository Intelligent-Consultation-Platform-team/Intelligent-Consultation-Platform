<template>
  <div class="payment-redirect" v-loading="true" element-loading-text="正在跳转到我的流程..."></div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
onMounted(() => {
  router.replace('/appoint/my-journey')
})
</script>

<style scoped>
.payment-redirect { min-height: 200px; }
</style>
