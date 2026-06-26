<template>
  <div class="hospital">
    <div class="page-header">
      <h3>住院登记管理</h3>
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        新增住院
      </el-button>
    </div>

    <el-card shadow="never" class="filter-card">
      <el-row :gutter="20">
        <el-col :span="8">
          <el-input
            v-model="filter.patientName"
            placeholder="患者姓名"
            clearable
          />
        </el-col>
        <el-col :span="8">
          <el-select
            v-model="filter.status"
            placeholder="状态"
            clearable
            style="width: 100%"
          >
            <el-option label="住院中" value="admitted" />
            <el-option label="已出院" value="discharged" />
          </el-select>
        </el-col>
        <el-col :span="8">
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="resetFilter">重置</el-button>
        </el-col>
      </el-row>
    </el-card>

    <el-card shadow="never" class="table-card">
      <el-table v-loading="loading" :data="hospitals" style="width: 100%">
        <el-table-column prop="hospitalizationId" label="住院ID" width="100" />
        <el-table-column prop="patientName" label="患者姓名" />
        <el-table-column prop="deptName" label="科室" />
        <el-table-column prop="doctorName" label="主治医生" />
        <el-table-column prop="reason" label="入院原因" show-overflow-tooltip />
        <el-table-column prop="wardNumber" label="病房号" width="100" />
        <el-table-column prop="bedNumber" label="床位号" width="100" />
        <el-table-column prop="admissionDate" label="入院日期" width="120">
          <template #default="scope">
            {{ formatDate(scope.row.admissionDate) }}
          </template>
        </el-table-column>
        <el-table-column prop="dischargeDate" label="出院日期" width="120">
          <template #default="scope">
            {{ scope.row.dischargeDate ? formatDate(scope.row.dischargeDate) : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 'admitted' ? 'primary' : 'success'">
              {{ scope.row.status === 'admitted' ? '住院中' : '已出院' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="scope">
            <el-button
              v-if="scope.row.status === 'admitted'"
              type="success"
              size="small"
              @click="handleDischarge(scope.row)"
            >
              出院
            </el-button>
            <span v-else>-</span>
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="患者" prop="patientId">
          <div style="display: flex; gap: 8px; width: 100%">
            <el-input
              v-model="form.patientName"
              placeholder="输入患者姓名搜索"
              style="flex: 1"
              :disabled="!!prefillPatientId"
              @keyup.enter="searchPatients"
            />
            <el-button v-if="!prefillPatientId" type="primary" @click="searchPatients" :loading="searching">
              查找
            </el-button>
          </div>
        </el-form-item>
        <el-form-item v-if="patientOptions.length > 0 && !prefillPatientId" label="">
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
        <el-form-item v-if="patientOptions.length === 0 && searched && !prefillPatientId" label="">
          <span style="color: #909399">未找到匹配患者</span>
        </el-form-item>
        <el-form-item label="科室" prop="deptId">
          <el-select
            v-model="form.deptId"
            placeholder="选择科室"
            style="width: 100%"
            :disabled="!!prefillDeptId"
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
        <el-form-item label="主治医生" prop="doctorId">
          <el-select
            v-model="form.doctorId"
            placeholder="选择医生"
            style="width: 100%"
            :disabled="!!prefillDoctorId || !form.deptId"
          >
            <el-option
              v-for="doctor in doctors"
              :key="doctor.id"
              :label="doctor.name"
              :value="doctor.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="病房号" prop="wardNumber">
          <el-input v-model="form.wardNumber" placeholder="请输入病房号" />
        </el-form-item>
        <el-form-item label="床位号" prop="bedNumber">
          <el-input v-model="form.bedNumber" placeholder="请输入床位号" />
        </el-form-item>
        <el-form-item label="入院日期" prop="admissionDate">
          <el-date-picker
            v-model="form.admissionDate"
            type="date"
            placeholder="选择入院日期"
            style="width: 100%"
            value-format="YYYY-MM-DD"
            :disabled-date="disabledDate"
          />
        </el-form-item>
        <el-form-item label="入院原因" prop="reason">
          <el-input
            v-model="form.reason"
            type="textarea"
            :rows="3"
            placeholder="请输入入院原因"
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
import { ref, reactive, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { api } from '../../utils/api'

const route = useRoute()

const filter = reactive({ patientName: '', status: '' })
const pagination = reactive({ current: 1, size: 10, total: 0 })
const departments = ref([])
const doctors = ref([])
const hospitals = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('新增住院')
const formRef = ref()
const submitting = ref(false)
const searching = ref(false)
const searched = ref(false)
const patientOptions = ref([])

const prefillPatientId = ref(null)
const prefillDeptId = ref(null)
const prefillDoctorId = ref(null)

const form = reactive({
  patientId: '',
  patientName: '',
  deptId: '',
  doctorId: '',
  wardNumber: '',
  bedNumber: '',
  admissionDate: '',
  reason: ''
})

const rules = {
  patientId: [{ required: true, message: '请搜索并选择患者', trigger: 'change' }],
  deptId: [{ required: true, message: '请选择科室', trigger: 'change' }],
  doctorId: [{ required: true, message: '请选择主治医生', trigger: 'change' }],
  wardNumber: [{ required: true, message: '请输入病房号', trigger: 'blur' }],
  bedNumber: [{ required: true, message: '请输入床位号', trigger: 'blur' }],
  admissionDate: [{ required: true, message: '请选择入院日期', trigger: 'change' }],
  reason: [{ required: true, message: '请输入入院原因', trigger: 'blur' }]
}

const formatDate = (value) => {
  if (!value) return ''
  const str = String(value)
  const tIdx = str.indexOf('T')
  const spaceIdx = str.indexOf(' ')
  if (spaceIdx > 0) return str.substring(0, spaceIdx)
  if (tIdx > 0) return str.substring(0, tIdx)
  return str
}

const disabledDate = (time) => {
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  const maxDate = new Date(today)
  maxDate.setDate(maxDate.getDate() + 30)
  return time.getTime() > maxDate.getTime()
}

const loadDepartments = async () => {
  try {
    const data = await api.departments.getList()
    departments.value = (data || []).map(item => ({
      id: item.deptId ?? item.id,
      name: item.deptName ?? item.name
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
      name: item.realName ?? item.name ?? item.doctorName
    }))
  } catch (e) {
    console.error('加载医生失败', e)
  }
}

const onDeptChange = () => {
  form.doctorId = ''
  loadDoctors(form.deptId)
}

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

const loadData = async () => {
  loading.value = true
  try {
    const params = {
      current: pagination.current,
      size: pagination.size
    }
    if (filter.patientName.trim()) params.patientName = filter.patientName.trim()
    if (filter.status) params.status = filter.status

    const result = await api.hospitalization.getPage(params)
    hospitals.value = result?.records || result?.data?.records || []
    pagination.total = result?.total || result?.data?.total || 0
  } catch (e) {
    ElMessage.error('加载住院数据失败')
    console.error(e)
  } finally {
    loading.value = false
  }
}

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

const resetForm = () => {
  form.patientId = ''
  form.patientName = ''
  form.deptId = ''
  form.doctorId = ''
  form.wardNumber = ''
  form.bedNumber = ''
  form.admissionDate = ''
  form.reason = ''
  patientOptions.value = []
  searched.value = false
}

const handleAdd = () => {
  resetForm()
  prefillPatientId.value = null
  prefillDeptId.value = null
  prefillDoctorId.value = null
  dialogTitle.value = '新增住院'
  dialogVisible.value = true
}

const handleDischarge = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确认患者 ${row.patientName} 出院？`,
      '出院确认',
      { confirmButtonText: '确认出院', cancelButtonText: '取消', type: 'warning' }
    )
    await api.hospitalization.discharge(row.hospitalizationId)
    ElMessage.success('出院成功')
    await loadData()
  } catch (e) {
    if (e !== 'cancel' && e?.message) {
      ElMessage.error(e.message)
    }
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
    submitting.value = true
    const payload = {
      patientId: form.patientId,
      doctorId: form.doctorId,
      deptId: form.deptId,
      wardNumber: form.wardNumber,
      bedNumber: form.bedNumber,
      admissionDate: form.admissionDate,
      reason: form.reason
    }
    if (route.query.consultationId) {
      payload.consultationId = Number(route.query.consultationId)
    }
    await api.hospitalization.create(payload)
    ElMessage.success('住院登记成功')
    dialogVisible.value = false
    await loadData()
  } catch (e) {
    ElMessage.error(e?.message || '住院登记失败')
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

const applyPrefill = () => {
  const q = route.query
  if (!q.patientId) return

  prefillPatientId.value = Number(q.patientId)
  prefillDeptId.value = q.deptId ? Number(q.deptId) : null
  prefillDoctorId.value = q.doctorId ? Number(q.doctorId) : null

  form.patientId = prefillPatientId.value
  form.patientName = q.patientName || ''
  form.deptId = prefillDeptId.value || ''
  form.doctorId = prefillDoctorId.value || ''
  form.admissionDate = new Date().toISOString().split('T')[0]
  if (q.diagnosis) {
    form.reason = q.diagnosis
  }

  if (prefillDeptId.value) {
    loadDoctors(prefillDeptId.value)
  }

  dialogTitle.value = '新增住院'
  dialogVisible.value = true
}

onMounted(async () => {
  await loadDepartments()
  await loadDoctors()
  await loadData()
  applyPrefill()
})
</script>

<style scoped>
.hospital { padding: 20px 0; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.page-header h3 { margin: 0; color: #1f2d3d; }
.filter-card { margin-bottom: 20px; }
.table-card { margin-top: 20px; }
.pagination { margin-top: 20px; display: flex; justify-content: flex-end; }
.dialog-footer { display: flex; justify-content: flex-end; }
</style>
