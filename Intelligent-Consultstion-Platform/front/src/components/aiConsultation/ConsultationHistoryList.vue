<template>
  <div>
    <el-empty v-if="!items.length" description="暂无历史问诊记录" />
    <el-table v-else :data="items" style="width: 100%">
      <el-table-column prop="sessionId" label="会话编号" width="180" />
      <el-table-column prop="chiefComplaint" label="主诉摘要" />
      <el-table-column prop="riskLevel" label="风险等级" width="120">
        <template #default="scope">
          <el-tag :type="getRiskType(scope.row.riskLevel)" effect="plain">{{ scope.row.riskLevel || '未知' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100" />
      <el-table-column prop="createdAt" label="创建时间" width="180" />
      <el-table-column label="操作" width="120">
        <template #default="scope">
          <el-button type="primary" link @click="$emit('view-detail', scope.row)">查看</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
defineProps({
  items: {
    type: Array,
    default: () => [],
  },
})

defineEmits(['view-detail'])

const getRiskType = (risk) => ({ low: 'success', medium: 'warning', high: 'danger' }[risk] || 'info')
</script>

