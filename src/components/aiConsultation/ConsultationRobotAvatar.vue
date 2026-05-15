<template>
  <div class="robot-avatar" :class="sizeClass">
    <div class="robot-head">
      <div class="antenna"></div>
      <div class="eyes">
        <span></span>
        <span></span>
      </div>
      <div class="mouth"></div>
    </div>
    <div class="robot-body">
      <div class="panel"></div>
    </div>
    <div class="robot-shadow"></div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  size: {
    type: String,
    default: 'md',
  },
})

const sizeClass = computed(() => `robot-${props.size}`)
</script>

<style scoped>
.robot-avatar {
  position: relative;
  display: inline-flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-start;
  width: 112px;
  height: 132px;
}

.robot-sm { width: 80px; height: 96px; }
.robot-lg { width: 144px; height: 168px; }

.robot-head {
  position: relative;
  z-index: 2;
  width: 70px;
  height: 64px;
  border-radius: 22px;
  background: linear-gradient(180deg, #f8fbff 0%, #dfeeff 100%);
  border: 2px solid #9cc4ff;
  box-shadow: 0 10px 24px rgba(64, 158, 255, 0.18);
}

.robot-sm .robot-head { width: 52px; height: 48px; border-radius: 18px; }
.robot-lg .robot-head { width: 88px; height: 80px; border-radius: 26px; }

.antenna {
  position: absolute;
  top: -14px;
  left: 50%;
  width: 4px;
  height: 16px;
  background: #9cc4ff;
  transform: translateX(-50%);
  border-radius: 999px;
}

.antenna::before {
  content: '';
  position: absolute;
  top: -6px;
  left: 50%;
  width: 10px;
  height: 10px;
  transform: translateX(-50%);
  border-radius: 50%;
  background: #67c23a;
  box-shadow: 0 0 0 4px rgba(103, 194, 58, 0.15);
}

.eyes {
  position: absolute;
  top: 20px;
  left: 50%;
  display: flex;
  gap: 10px;
  transform: translateX(-50%);
}

.robot-sm .eyes { top: 15px; gap: 8px; }
.robot-lg .eyes { top: 26px; gap: 12px; }

.eyes span {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #2b6cb0;
  box-shadow: 0 0 0 4px rgba(64, 158, 255, 0.12);
}

.robot-sm .eyes span { width: 6px; height: 6px; }
.robot-lg .eyes span { width: 10px; height: 10px; }

.mouth {
  position: absolute;
  left: 50%;
  bottom: 14px;
  width: 22px;
  height: 6px;
  transform: translateX(-50%);
  border-radius: 999px;
  background: #6b7280;
}

.robot-sm .mouth { bottom: 11px; width: 18px; height: 4px; }
.robot-lg .mouth { bottom: 16px; width: 28px; height: 7px; }

.robot-body {
  position: relative;
  z-index: 1;
  margin-top: -6px;
  width: 54px;
  height: 40px;
  border-radius: 18px 18px 16px 16px;
  background: linear-gradient(180deg, #eaf4ff 0%, #cfe2ff 100%);
  border: 2px solid #9cc4ff;
}

.robot-sm .robot-body { width: 40px; height: 30px; border-radius: 14px; }
.robot-lg .robot-body { width: 70px; height: 50px; border-radius: 20px; }

.panel {
  position: absolute;
  top: 10px;
  left: 50%;
  width: 18px;
  height: 10px;
  transform: translateX(-50%);
  border-radius: 6px;
  background: rgba(64, 158, 255, 0.22);
}

.robot-shadow {
  width: 58px;
  height: 12px;
  margin-top: 6px;
  border-radius: 50%;
  background: radial-gradient(ellipse at center, rgba(31, 45, 61, 0.18) 0%, rgba(31, 45, 61, 0.05) 60%, transparent 100%);
}

.robot-sm .robot-shadow { width: 42px; height: 10px; }
.robot-lg .robot-shadow { width: 76px; height: 14px; }
</style>
