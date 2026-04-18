<template>
  <div class="book">
    <div class="page-header">
      <h3>预约挂号</h3>
      <el-button type="primary" @click="refreshData">
        <el-icon><Refresh /></el-icon>
        刷新
      </el-button>
    </div>

    <el-card shadow="never" class="filter-card">
      <el-row :gutter="20">
        <el-col :span="8">
          <el-select
            v-model="filter.department"
            placeholder="选择科室"
            clearable
            style="width: 100%"
          >
            <el-option
              v-for="dept in departments"
              :key="dept.id"
              :label="dept.name"
              :value="dept.id"
            />
          </el-select>
        </el-col>
        <el-col :span="8">
          <el-date-picker
            v-model="filter.date"
            type="date"
            placeholder="选择日期"
            style="width: 100%"
          />
        </el-col>
        <el-col :span="8">
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="resetFilter">重置</el-button>
        </el-col>
      </el-row>
    </el-card>

    <el-card shadow="never" class="table-card">
      <el-table v-loading="loading" :data="schedules" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="department" label="科室" />
        <el-table-column prop="doctorName" label="医生姓名" />
        <el-table-column prop="date" label="日期" width="120" />
        <el-table-column prop="timeRange" label="时间段" width="150" />
        <el-table-column prop="maxNumber" label="最大号数" width="120" />
        <el-table-column prop="remaining" label="剩余号数" width="120">
          <template #default="scope">
            <el-tag :type="scope.row.remaining > 0 ? 'success' : 'danger'">
              {{ scope.row.remaining }}
            </el-tag>
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
          :total="pagination.total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <el-dialog
      v-model="bookDialogVisible"
      title="确认预约"
      width="400px"
    >
      <div v-if="selectedSchedule">
        <p><strong>科室：</strong>{{ selectedSchedule.department }}</p>
        <p><strong>医生：</strong>{{ selectedSchedule.doctorName }}</p>
        <p><strong>日期：</strong>{{ selectedSchedule.date }}</p>
        <p><strong>时间：</strong>{{ selectedSchedule.timeRange }}</p>
        <p><strong>剩余号数：</strong>{{ selectedSchedule.remaining }}</p>
        <el-form
          ref="bookFormRef"
          :model="bookForm"
          :rules="bookRules"
          label-width="80px"
          class="book-form"
        >
          <el-form-item label="症状描述" prop="symptoms">
            <el-input
              v-model="bookForm.symptoms"
              type="textarea"
              :rows="3"
              placeholder="请简要描述您的症状"
            />
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="bookDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmBook">确认预约</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import { api } from '../../utils/api'

const filter = reactive({
  department: '',
  date: ''
})

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const departments = ref([])
const doctors = ref([])
const schedules = ref([])
const loading = ref(false)
const bookDialogVisible = ref(false)
const selectedSchedule = ref(null)
const bookFormRef = ref()
const bookForm = reactive({
  symptoms: ''
})

const bookRules = {
  symptoms: [
    { required: true, message: '请描述您的症状', trigger: 'blur' }
  ]
}

const loadDepartments = async () => {
  try {
    const data = await api.departments.getList()
    departments.value = (data || []).map(item => ({
      id: item.deptId,
      name: item.deptName
    }))
  } catch (error) {
    ElMessage.error('加载科室失败')
  }
}

const loadDoctors = async () => {
  try {
    const params = {}
    if (filter.department) {
      params.deptId = filter.department
    }
    const data = await api.doctors.getList(params)
    doctors.value = (data || []).map(item => ({
      id: item.doctorId,
      name: item.realName,
      deptId: item.deptId
    }))
  } catch (error) {
    ElMessage.error('加载医生失败')
  }
}

const loadData = async () => {
  loading.value = true
  try {
    await loadDoctors()
    const schedulePromises = doctors.value.map(async (doctor) => {
      const params = {
        doctorId: doctor.id
      }
      if (filter.date) {
        params.date = filter.date
      }
      try {
        const data = await api.schedules.getList(params)
        return (data || []).map(item => ({
          id: item.scheduleId,
          doctorId: item.doctorId,
          doctorName: doctor.name,
          department: departments.value.find(d => d.id === doctor.deptId)?.name || '-',
          date: filter.date || '-',
          timeRange: `${item.startTime} - ${item.endTime}`,
          maxNumber: item.availableSlots || 0,
          remaining: item.availableSlots || 0
        }))
      } catch (error) {
        return []
      }
    })
    const results = await Promise.all(schedulePromises)
    schedules.value = results.flat()
    pagination.total = schedules.value.length
  } catch (error) {
    ElMessage.error(error.message || '加载排班失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.current = 1
  loadData()
}

const resetFilter = () => {
  filter.department = ''
  filter.date = ''
  pagination.current = 1
  loadData()
}

const refreshData = () => {
  loadData()
}

const handleBook = (row) => {
  selectedSchedule.value = row
  bookForm.symptoms = ''
  bookDialogVisible.value = true
}

const confirmBook = async () => {
  if (!bookFormRef.value) return
  try {
    await bookFormRef.value.validate()
    if (!selectedSchedule.value) {
      ElMessage.error('请选择排班信息')
      return
    }
    // 模拟患者ID，实际应该从登录信息中获取
    const patientId = 1
    await api.appointments.create({
      patientId,
      doctorId: selectedSchedule.value.doctorId,
      scheduleId: selectedSchedule.value.id,
      appointmentDate: selectedSchedule.value.date,
      appointmentTime: selectedSchedule.value.timeRange.split(' - ')[0]
    })
    ElMessage.success('预约成功！请准时前往医院就诊。')
    bookDialogVisible.value = false
    loadData()
  } catch (error) {
    ElMessage.error(error.message || '预约失败')
  }
}

const handleSizeChange = (size) => {
  pagination.size = size
  loadData()
}

const handleCurrentChange = (current) => {
  pagination.current = current
  loadData()
}

onMounted(async () => {
  await loadDepartments()
  await loadData()
})
</script>

<style scoped>
.book {
  padding: 20px 0;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h3 {
  margin: 0;
  color: #1f2d3d;
}

.filter-card {
  margin-bottom: 20px;
}

.table-card {
  margin-top: 20px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.book-form {
  margin-top: 20px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
}
</style>