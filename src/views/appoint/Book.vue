<template>
  <div class="book-page">
    <div class="page-header">
      <div>
        <h3>预约挂号</h3>
        <p>支持按科室与日期筛选，可直接提交预约</p>
      </div>
      <div class="header-actions">
        <el-button @click="resetFilter">重置</el-button>
        <el-button type="primary" :loading="loading" @click="refreshData">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
      </div>
    </div>

    <el-card shadow="never" class="filter-card">
      <el-form :inline="true" :model="filter" @submit.prevent>
        <el-form-item label="科室">
          <el-select v-model="filter.departmentId" placeholder="选择科室" clearable style="width: 200px">
            <el-option v-for="dept in departments" :key="dept.id" :label="dept.name" :value="dept.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="日期">
          <el-date-picker v-model="filter.date" value-format="YYYY-MM-DD" type="date" placeholder="选择日期" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleSearch">查询</el-button>
          <el-button @click="resetFilter">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="table-card">
      <el-skeleton v-if="loading && !hasLoadedOnce" :rows="6" animated />
      <el-empty v-else-if="!loading && !visibleSchedules.length" :description="emptyText">
        <el-button type="primary" @click="refreshData">重新加载</el-button>
      </el-empty>
      <template v-else>
        <el-table :data="paginatedSchedules" v-loading="loading" style="width: 100%">
          <el-table-column prop="departmentName" label="科室" min-width="140" />
          <el-table-column prop="doctorName" label="医生姓名" min-width="120" />
          <el-table-column prop="scheduleDate" label="日期" width="120" />
          <el-table-column prop="timeRange" label="时间段" width="160" />
          <el-table-column prop="maxNumber" label="最大号数" width="100" />
          <el-table-column prop="remaining" label="剩余号数" width="100">
            <template #default="scope">
              <el-tag :type="scope.row.remaining > 0 ? 'success' : 'danger'">{{ scope.row.remaining }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120" fixed="right">
            <template #default="scope">
              <el-button
                type="primary"
                size="small"
                :disabled="scope.row.remaining <= 0"
                @click="handleBook(scope.row)"
              >
                预约
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        <div class="pagination">
          <el-pagination
            v-model:current-page="pagination.current"
            v-model:page-size="pagination.size"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next, jumper"
            :total="visibleSchedules.length"
          />
        </div>
      </template>
    </el-card>

    <el-dialog v-model="bookDialogVisible" title="确认预约" width="420px" @closed="resetBookForm">
      <div v-if="selectedSchedule" class="schedule-summary">
        <p><strong>科室：</strong>{{ selectedSchedule.departmentName }}</p>
        <p><strong>医生：</strong>{{ selectedSchedule.doctorName }}</p>
        <p><strong>日期：</strong>{{ selectedSchedule.scheduleDate }}</p>
        <p><strong>时间：</strong>{{ selectedSchedule.timeRange }}</p>
        <p><strong>剩余号数：</strong>{{ selectedSchedule.remaining }}</p>
      </div>
      <el-form ref="bookFormRef" :model="bookForm" :rules="bookRules" label-width="90px" class="book-form">
        <el-form-item label="症状描述" prop="symptoms">
          <el-input v-model.trim="bookForm.symptoms" type="textarea" :rows="3" maxlength="200" show-word-limit placeholder="请简要描述您的症状" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="bookDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="booking" :disabled="booking" @click="confirmBook">确认预约</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import { api } from '../../utils/api'

const filter = reactive({ departmentId: '', date: '' })
const pagination = reactive({ current: 1, size: 10 })
const departments = ref([])
const schedules = ref([])
const loading = ref(false)
const booking = ref(false)
const bookDialogVisible = ref(false)
const selectedSchedule = ref(null)
const bookFormRef = ref()
const bookForm = reactive({ symptoms: '' })
const hasLoadedOnce = ref(false)

const bookRules = {
  symptoms: [
    { required: true, message: '请描述您的症状', trigger: 'blur' },
    { min: 2, max: 200, message: '症状描述长度为 2-200 字', trigger: 'blur' },
  ],
}

const departmentMap = computed(() => new Map(departments.value.map((item) => [item.id, item.name])))
const visibleSchedules = computed(() => schedules.value)
const paginatedSchedules = computed(() => {
  const start = (pagination.current - 1) * pagination.size
  return visibleSchedules.value.slice(start, start + pagination.size)
})
const emptyText = computed(() => (hasLoadedOnce.value ? '暂无匹配的排班数据' : '暂无排班数据'))

const normalizeSchedule = (item) => {
  const startTime = item.startTime || item.beginTime || item.amStartTime || '--:--'
  const endTime = item.endTime || item.finishTime || item.pmEndTime || '--:--'
  const maxNumber = Number(item.maxNumber ?? item.totalNumber ?? item.capacity ?? item.availableSlots ?? 0)
  const remaining = Number(item.remaining ?? item.remainingNumber ?? item.availableSlots ?? maxNumber)
  return {
    id: item.scheduleId ?? item.id,
    doctorId: item.doctorId,
    doctorName: item.doctorName || item.realName || '-',
    departmentId: item.deptId ?? item.departmentId ?? '',
    departmentName: item.deptName || departmentMap.value.get(item.deptId ?? item.departmentId) || '-',
    scheduleDate: item.date || item.scheduleDate || filter.date || '-',
    timeRange: `${startTime} - ${endTime}`,
    maxNumber,
    remaining,
  }
}

const loadDepartments = async () => {
  const data = await api.departments.getList()
  departments.value = (data || []).map((item) => ({
    id: item.deptId ?? item.id,
    name: item.deptName ?? item.name ?? '-',
  }))
}

const loadData = async () => {
  loading.value = true
  try {
    await loadDepartments()
    const params = {}
    if (filter.departmentId) params.deptId = filter.departmentId
    if (filter.date) params.date = filter.date
    const data = await api.schedules.getList(params)
    schedules.value = (data || []).map(normalizeSchedule)
    pagination.current = 1
    hasLoadedOnce.value = true
  } catch (error) {
    ElMessage.error(error?.message || '加载排班失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = async () => {
  await loadData()
}

const resetFilter = async () => {
  filter.departmentId = ''
  filter.date = ''
  await loadData()
}

const refreshData = async () => {
  await loadData()
}

const resetBookForm = () => {
  selectedSchedule.value = null
  bookForm.symptoms = ''
  bookFormRef.value?.clearValidate?.()
}

const handleBook = (row) => {
  selectedSchedule.value = row
  bookForm.symptoms = ''
  bookDialogVisible.value = true
}

const confirmBook = async () => {
  if (!bookFormRef.value || booking.value || !selectedSchedule.value) return
  try {
    await bookFormRef.value.validate()
    booking.value = true
    await api.appointments.create({
      doctorId: selectedSchedule.value.doctorId,
      scheduleId: selectedSchedule.value.id,
      appointmentDate: selectedSchedule.value.scheduleDate,
      appointmentTime: selectedSchedule.value.timeRange,
      symptoms: bookForm.symptoms.trim(),
    })
    ElMessage.success('预约成功，请按时就诊')
    bookDialogVisible.value = false
    await loadData()
  } catch (error) {
    if (error?.message) ElMessage.error(error.message)
  } finally {
    booking.value = false
  }
}

onMounted(loadData)
</script>

<style scoped>
.book-page { padding: 20px 0; }
.page-header { display: flex; justify-content: space-between; align-items: flex-end; gap: 16px; margin-bottom: 20px; }
.page-header h3 { margin: 0; color: #1f2d3d; }
.page-header p { margin: 6px 0 0; color: #6b7280; }
.header-actions { display: flex; gap: 12px; }
.filter-card, .table-card { margin-bottom: 20px; }
.pagination { margin-top: 20px; display: flex; justify-content: flex-end; }
.book-form { margin-top: 12px; }
.dialog-footer { display: flex; justify-content: flex-end; gap: 12px; }
.schedule-summary { line-height: 1.9; color: #374151; }
</style>
