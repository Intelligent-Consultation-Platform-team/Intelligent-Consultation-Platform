<template>
  <div class="book-page">
    <div class="page-header">
      <div>
        <h3>预约挂号</h3>
        <p>选择科室和日期，查看医生排班并提交预约</p>
      </div>
      <div class="header-actions">
        <el-button @click="loadSchedules">刷新</el-button>
      </div>
    </div>

    <el-card shadow="never" class="filter-card">
      <el-form :inline="true" :model="filterForm" @submit.prevent>
        <el-form-item label="科室">
          <el-select v-model="filterForm.deptId" placeholder="全部科室" clearable style="width: 180px">
            <el-option v-for="dept in departments" :key="dept.deptId" :label="dept.deptName" :value="dept.deptId" />
          </el-select>
        </el-form-item>
        <el-form-item label="日期">
          <el-date-picker v-model="filterForm.date" value-format="YYYY-MM-DD" type="date" placeholder="选择日期" style="width: 180px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="loadSchedules">查询</el-button>
          <el-button @click="clearFilter">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="table-card">
      <el-table v-loading="loading" :data="scheduleList" style="width: 100%" empty-text="暂无可预约的排班">
        <el-table-column prop="deptName" label="科室" min-width="100" />
        <el-table-column prop="doctorName" label="医生" min-width="100" />
        <el-table-column prop="title" label="职称" width="100" />
        <el-table-column prop="dayOfWeek" label="星期" width="80">
          <template #default="{ row }">
            {{ formatDayOfWeek(row.dayOfWeek) }}
          </template>
        </el-table-column>
        <el-table-column prop="startTime" label="开始时间" width="100" />
        <el-table-column prop="endTime" label="结束时间" width="100" />
        <el-table-column prop="availableSlots" label="剩余号源" width="100">
          <template #default="{ row }">
            <el-tag :type="row.availableSlots > 5 ? 'success' : row.availableSlots > 0 ? 'warning' : 'danger'">
              {{ row.availableSlots }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" :disabled="row.availableSlots <= 0" @click="openBookDialog(row)">
              预约
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" title="确认预约" width="450px">
      <div v-if="selectedSchedule" class="schedule-info">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="科室">{{ selectedSchedule.deptName }}</el-descriptions-item>
          <el-descriptions-item label="医生">{{ selectedSchedule.doctorName }} ({{ selectedSchedule.title }})</el-descriptions-item>
          <el-descriptions-item label="时间">{{ selectedSchedule.dayOfWeekText }} {{ selectedSchedule.startTime }} - {{ selectedSchedule.endTime }}</el-descriptions-item>
          <el-descriptions-item label="剩余号源">
            <el-tag :type="selectedSchedule.availableSlots > 5 ? 'success' : 'warning'">{{ selectedSchedule.availableSlots }}</el-tag>
          </el-descriptions-item>
        </el-descriptions>
      </div>
      <el-form ref="formRef" :model="bookForm" :rules="rules" label-width="80px" style="margin-top: 20px">
        <el-form-item label="预约日期" prop="date">
          <el-date-picker v-model="bookForm.date" value-format="YYYY-MM-DD" type="date" placeholder="选择日期" style="width: 100%" />
        </el-form-item>
        <el-form-item label="症状描述" prop="symptoms">
          <el-input v-model="bookForm.symptoms" type="textarea" :rows="3" maxlength="200" show-word-limit placeholder="请简要描述您的症状（2-200字）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitBooking">确认预约</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { api } from '../../utils/api'
import { getSession } from '../../utils/session'

const loading = ref(false)
const submitting = ref(false)
const departments = ref([])
const scheduleList = ref([])
const dialogVisible = ref(false)
const selectedSchedule = ref(null)
const formRef = ref()

const filterForm = reactive({
  deptId: '',
  date: ''
})

const bookForm = reactive({
  date: '',
  symptoms: ''
})

const rules = {
  date: [{ required: true, message: '请选择预约日期', trigger: 'change' }],
  symptoms: [
    { required: true, message: '请描述症状', trigger: 'blur' },
    { min: 2, max: 200, message: '症状描述长度为2-200字', trigger: 'blur' }
  ]
}

const dayNames = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']

const formatDayOfWeek = (day) => {
  return dayNames[parseInt(day) - 1] || '未知'
}

const loadDepartments = async () => {
  try {
    const data = await api.departments.getList()
    departments.value = data || []
  } catch (e) {
    console.error('加载科室失败', e)
  }
}

const loadSchedules = async () => {
  loading.value = true
  try {
    const params = {}
    if (filterForm.deptId) params.deptId = filterForm.deptId
    if (filterForm.date) params.date = filterForm.date

    const data = await api.schedules.getList(params)
    scheduleList.value = (data || []).map(item => ({
      ...item,
      dayOfWeekText: formatDayOfWeek(item.dayOfWeek)
    }))
  } catch (e) {
    ElMessage.error(e?.message || '加载排班失败')
  } finally {
    loading.value = false
  }
}

const clearFilter = () => {
  filterForm.deptId = ''
  filterForm.date = ''
  loadSchedules()
}

const openBookDialog = (row) => {
  selectedSchedule.value = row
  bookForm.date = filterForm.date || ''
  bookForm.symptoms = ''
  dialogVisible.value = true
}

const submitBooking = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
  } catch {
    return
  }

  if (!selectedSchedule.value) return

  const session = getSession()
  if (!session?.userId) {
    ElMessage.error('请先登录')
    return
  }

  submitting.value = true
  try {
    await api.appointments.create({
      patientId: session.userId,
      doctorId: selectedSchedule.value.doctorId,
      scheduleId: selectedSchedule.value.scheduleId,
      appointmentDate: bookForm.date,
      appointmentTime: selectedSchedule.value.startTime,
      symptoms: bookForm.symptoms.trim()
    })
    ElMessage.success('预约成功！请按时就诊')
    dialogVisible.value = false
    loadSchedules()
  } catch (e) {
    ElMessage.error(e?.message || '预约失败')
  } finally {
    submitting.value = false
  }
}

onMounted(async () => {
  await loadDepartments()
  await loadSchedules()
})
</script>

<style scoped>
.book-page { padding: 20px 0; }
.page-header { display: flex; justify-content: space-between; align-items: flex-end; margin-bottom: 20px; }
.page-header h3 { margin: 0; color: #1f2d3d; }
.page-header p { margin: 6px 0 0; color: #6b7280; }
.filter-card, .table-card { margin-bottom: 20px; }
.schedule-info { margin-bottom: 0; }
</style>
