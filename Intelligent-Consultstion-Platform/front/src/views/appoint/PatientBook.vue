<template>
  <div class="patient-book">
    <div class="page-header">
      <h3>患者挂号管理</h3>
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        新增挂号
      </el-button>
    </div>

    <el-card shadow="never" class="filter-card">
      <el-row :gutter="20">
        <el-col :span="8">
          <el-input
            v-model="filter.patientName"
            placeholder="患者姓名"
            clearable
            prefix-icon="el-icon-search"
          />
        </el-col>
        <el-col :span="8">
          <el-select
            v-model="filter.status"
            placeholder="状态"
            clearable
            style="width: 100%"
          >
            <el-option label="待就诊" value="pending" />
            <el-option label="已签到" value="confirmed" />
            <el-option label="就诊中" value="processing" />
            <el-option label="已完成" value="completed" />
            <el-option label="已取消" value="cancelled" />
          </el-select>
        </el-col>
        <el-col :span="8">
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="resetFilter">重置</el-button>
        </el-col>
      </el-row>
    </el-card>

    <el-card shadow="never" class="table-card">
      <el-table v-loading="loading" :data="appointments" style="width: 100%">
        <el-table-column prop="appointmentId" label="挂号ID" width="100" />
        <el-table-column prop="patientName" label="患者姓名" />
        <el-table-column prop="deptName" label="科室" />
        <el-table-column prop="doctorName" label="医生" />
        <el-table-column prop="doctorTitle" label="职称" width="100" />
        <el-table-column prop="appointmentDate" label="预约日期" width="120" />
        <el-table-column prop="appointmentTime" label="预约时间" width="100" />
        <el-table-column prop="symptoms" label="症状描述" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusTagType(scope.row.status)">
              {{ getStatusLabel(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="scope">
            <template v-if="scope.row.status === 'pending' || scope.row.status === 'confirmed'">
              <el-button type="primary" size="small" @click="handleProcess(scope.row)">
                接诊
              </el-button>
            </template>
            <template v-else-if="scope.row.status === 'processing'">
              <el-button type="success" size="small" @click="goToVisit">
                就诊中
              </el-button>
            </template>
            <span v-else-if="scope.row.status === 'unpaid'">待支付</span>
            <span v-else-if="scope.row.status === 'completed'">已完成</span>
            <span v-else-if="scope.row.status === 'cancelled'">已取消</span>
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
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item label="患者" prop="patientId">
          <div style="display: flex; gap: 8px; width: 100%">
            <el-input
              v-model="form.patientName"
              placeholder="输入患者姓名搜索"
              style="flex: 1"
              @keyup.enter="searchPatients"
            />
            <el-button type="primary" @click="searchPatients" :loading="searching">
              查找
            </el-button>
          </div>
        </el-form-item>
        <el-form-item v-if="patientOptions.length > 0" label="">
          <el-radio-group v-model="form.patientId" style="display: flex; flex-direction: column; gap: 8px">
            <el-radio
              v-for="p in patientOptions"
              :key="p.patientId"
              :value="p.patientId"
            >
              {{ p.patientName }} — {{ p.idCard || '无身份证' }} — {{ p.phone || '无电话' }}
            </el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="patientOptions.length === 0 && searched" label="">
          <span style="color: #909399">未找到匹配患者，请先在用户管理中创建患者账户</span>
        </el-form-item>
        <el-form-item label="科室" prop="departmentId">
          <el-select
            v-model="form.departmentId"
            placeholder="选择科室"
            style="width: 100%"
            @change="onDeptChange"
          >
            <el-option
              v-for="dept in departments"
              :key="dept.id"
              :label="dept.name"
              :value="dept.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="医生" prop="doctorId">
          <el-select
            v-model="form.doctorId"
            placeholder="选择医生"
            style="width: 100%"
            :disabled="!form.departmentId"
          >
            <el-option
              v-for="doctor in doctors"
              :key="doctor.id"
              :label="doctor.name"
              :value="doctor.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="预约日期" prop="appointmentDate">
          <el-date-picker
            v-model="form.appointmentDate"
            type="date"
            placeholder="选择日期"
            style="width: 100%"
            value-format="YYYY-MM-DD"
            :disabled-date="disabledDate"
          />
        </el-form-item>
        <el-form-item label="预约时间" prop="scheduleId">
          <el-select
            v-model="form.scheduleId"
            placeholder="请先选择医生和日期"
            style="width: 100%"
            :disabled="!form.doctorId || !form.appointmentDate"
            @change="onScheduleSelect"
          >
            <el-option
              v-for="s in availableSchedules"
              :key="s.scheduleId"
              :label="s.startTime + ' - ' + s.endTime + '（剩余' + s.availableSlots + '号）'"
              :value="s.scheduleId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="症状描述" prop="symptoms">
          <el-input
            v-model="form.symptoms"
            type="textarea"
            :rows="3"
            placeholder="请描述症状"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit" :loading="submitting">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { api } from '../../utils/api'

const router = useRouter()

const filter = reactive({
  patientName: '',
  status: ''
})

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const departments = ref([])
const doctors = ref([])
const appointments = ref([])
const availableSchedules = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('新增挂号')
const formRef = ref()
const loading = ref(false)
const submitting = ref(false)
const searching = ref(false)
const searched = ref(false)
const patientOptions = ref([])
const form = reactive({
  patientId: '',
  patientName: '',
  departmentId: '',
  doctorId: '',
  appointmentDate: '',
  appointmentTime: '',
  scheduleId: '',
  symptoms: ''
})

const rules = {
  patientId: [
    { required: true, message: '请搜索并选择患者', trigger: 'change' }
  ],
  departmentId: [
    { required: true, message: '请选择科室', trigger: 'change' }
  ],
  doctorId: [
    { required: true, message: '请选择医生', trigger: 'change' }
  ],
  appointmentDate: [
    { required: true, message: '请选择预约日期', trigger: 'change' }
  ],
  scheduleId: [
    { required: true, message: '请选择预约时间段', trigger: 'change' }
  ],
  symptoms: [
    { required: true, message: '请描述症状', trigger: 'blur' }
  ]
}

// 日期限制：不能选过去和15天后
const disabledDate = (time) => {
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  const maxDate = new Date(today)
  maxDate.setDate(maxDate.getDate() + 15)
  return time.getTime() < today.getTime() || time.getTime() > maxDate.getTime()
}

const loadDepartments = async () => {
  try {
    const data = await api.departments.getList()
    departments.value = (data || []).map(item => ({
      id: item.deptId ?? item.id,
      name: item.deptName ?? item.name,
      ...item
    }))
  } catch (e) {
    console.error('加载科室失败', e)
  }
}

const loadDoctors = async (deptId) => {
  try {
    const params = deptId ? { deptId } : {}
    const data = await api.doctors.getList(params)
    doctors.value = (data || []).map(item => ({
      id: item.doctorId ?? item.id,
      name: item.realName ?? item.name ?? item.doctorName,
      ...item
    }))
  } catch (e) {
    console.error('加载医生失败', e)
  }
}

const onDeptChange = () => {
  form.doctorId = ''
  form.scheduleId = ''
  form.appointmentTime = ''
  availableSchedules.value = []
  loadDoctors(form.departmentId)
}

// 搜索患者
const searchPatients = async () => {
  if (!form.patientName.trim()) {
    ElMessage.warning('请输入患者姓名')
    return
  }
  searching.value = true
  searched.value = false
  patientOptions.value = []
  form.patientId = ''
  try {
    const data = await api.patient.search(form.patientName.trim())
    patientOptions.value = data || []
    searched.value = true
    if ((data || []).length === 0) {
      ElMessage.warning('未找到匹配患者')
    }
  } catch (e) {
    ElMessage.error(e?.message || '搜索患者失败')
  } finally {
    searching.value = false
  }
}

const loadSchedulesForBooking = async () => {
  if (!form.doctorId || !form.appointmentDate) {
    availableSchedules.value = []
    form.scheduleId = ''
    form.appointmentTime = ''
    return
  }
  try {
    const data = await api.schedules.getList({
      doctorId: form.doctorId,
      date: form.appointmentDate
    })
    availableSchedules.value = (data || []).filter(s => s.status === 'active' && s.availableSlots > 0)
    if (availableSchedules.value.length === 0) {
      ElMessage.warning('该医生在所选日期无可用排班')
    }
  } catch (e) {
    console.error('加载排班失败', e)
  }
}

const onScheduleSelect = (scheduleId) => {
  const selected = availableSchedules.value.find(s => s.scheduleId === scheduleId)
  if (selected) {
    form.appointmentTime = selected.startTime
  }
}

// 医生或日期变化时重新查询排班
watch(() => form.doctorId, () => {
  form.scheduleId = ''
  form.appointmentTime = ''
  loadSchedulesForBooking()
})
watch(() => form.appointmentDate, () => {
  form.scheduleId = ''
  form.appointmentTime = ''
  loadSchedulesForBooking()
})

const loadData = async () => {
  loading.value = true
  try {
    const params = {
      current: pagination.current,
      size: pagination.size
    }
    if (filter.patientName.trim()) params.patientName = filter.patientName.trim()
    if (filter.status) params.status = filter.status

    const result = await api.appointment.getPage(params)
    appointments.value = result?.records || result?.data?.records || []
    pagination.total = result?.total || result?.data?.total || 0
  } catch (e) {
    ElMessage.error('加载预约数据失败')
    console.error(e)
  } finally {
    loading.value = false
  }
}

const getStatusLabel = (status) => ({
  pending: '待就诊',
  confirmed: '已签到',
  processing: '就诊中',
  unpaid: '待支付',
  completed: '已完成',
  cancelled: '已取消'
}[status] || '未知')

const getStatusTagType = (status) => ({
  pending: 'warning',
  confirmed: '',
  processing: 'primary',
  unpaid: 'danger',
  completed: 'success',
  cancelled: 'info'
}[status] || 'default')

const handleSearch = () => {
  pagination.current = 1
  loadData()
}

const resetFilter = () => {
  filter.patientName = ''
  filter.status = ''
  pagination.current = 1
  loadData()
}

const handleProcess = async (row) => {
  try {
    await api.appointment.process(row.appointmentId)
    ElMessage.success('已接诊，跳转至就诊页面')
    router.push('/appoint/visit')
  } catch (e) {
    ElMessage.error(e?.message || '接诊失败')
  }
}

const goToVisit = () => {
  router.push('/appoint/visit')
}

const handleAdd = () => {
  form.patientId = ''
  form.patientName = ''
  form.departmentId = ''
  form.doctorId = ''
  form.appointmentDate = ''
  form.appointmentTime = ''
  form.scheduleId = ''
  form.symptoms = ''
  patientOptions.value = []
  searched.value = false
  availableSchedules.value = []
  dialogTitle.value = '新增挂号'
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
    submitting.value = true
    await api.appointment.create({
      patientId: form.patientId,
      doctorId: form.doctorId,
      scheduleId: form.scheduleId,
      appointmentDate: form.appointmentDate,
      appointmentTime: form.appointmentTime,
      symptoms: form.symptoms
    })
    ElMessage.success('挂号成功')
    dialogVisible.value = false
    loadData()
  } catch (e) {
    ElMessage.error(e?.message || '挂号失败')
  } finally {
    submitting.value = false
  }
}

const handleSizeChange = (size) => {
  pagination.size = size
  pagination.current = 1
  loadData()
}

const handleCurrentChange = (current) => {
  pagination.current = current
  loadData()
}

onMounted(async () => {
  await loadDepartments()
  await loadDoctors()
  await loadData()
})
</script>

<style scoped>
.patient-book {
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

.dialog-footer {
  display: flex;
  justify-content: flex-end;
}
</style>
