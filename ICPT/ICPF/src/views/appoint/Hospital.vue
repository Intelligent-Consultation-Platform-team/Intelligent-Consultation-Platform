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
            <el-option label="住院中" value="in" />
            <el-option label="已出院" value="out" />
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
        <el-table-column prop="id" label="住院ID" width="100" />
        <el-table-column prop="patientName" label="患者姓名" />
        <el-table-column prop="department" label="科室" />
        <el-table-column prop="doctorName" label="主治医生" />
        <el-table-column prop="roomNumber" label="病房号" width="120" />
        <el-table-column prop="bedNumber" label="床位号" width="100" />
        <el-table-column prop="admissionDate" label="入院日期" width="150" />
        <el-table-column prop="dischargeDate" label="出院日期" width="150" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 'in' ? 'primary' : 'success'">
              {{ scope.row.status === 'in' ? '住院中' : '已出院' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button type="primary" size="small" @click="handleEdit(scope.row)">
              编辑
            </el-button>
            <el-button v-if="scope.row.status === 'in'" type="success" size="small" @click="handleDischarge(scope.row.id)">
              出院
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
        <el-form-item label="患者姓名" prop="patientName">
          <el-input v-model="form.patientName" placeholder="请输入患者姓名" />
        </el-form-item>
        <el-form-item label="身份证号" prop="idCard">
          <el-input v-model="form.idCard" placeholder="请输入身份证号" />
        </el-form-item>
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="科室" prop="departmentId">
          <el-select
            v-model="form.departmentId"
            placeholder="选择科室"
            style="width: 100%"
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
          >
            <el-option
              v-for="doctor in doctors"
              :key="doctor.id"
              :label="doctor.name"
              :value="doctor.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="病房号" prop="roomNumber">
          <el-input v-model="form.roomNumber" placeholder="请输入病房号" />
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
          <el-button type="primary" @click="handleSubmit">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { api } from '../../utils/api'

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
const hospitals = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('新增住院')
const formRef = ref()
const form = reactive({
  id: '',
  patientName: '',
  idCard: '',
  phone: '',
  departmentId: '',
  doctorId: '',
  roomNumber: '',
  bedNumber: '',
  admissionDate: '',
  dischargeDate: '',
  reason: ''
})

const rules = {
  patientName: [
    { required: true, message: '请输入患者姓名', trigger: 'blur' }
  ],
  idCard: [
    { required: true, message: '请输入身份证号', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' }
  ],
  departmentId: [
    { required: true, message: '请选择科室', trigger: 'change' }
  ],
  doctorId: [
    { required: true, message: '请选择主治医生', trigger: 'change' }
  ],
  roomNumber: [
    { required: true, message: '请输入病房号', trigger: 'blur' }
  ],
  bedNumber: [
    { required: true, message: '请输入床位号', trigger: 'blur' }
  ],
  admissionDate: [
    { required: true, message: '请选择入院日期', trigger: 'change' }
  ],
  reason: [
    { required: true, message: '请输入入院原因', trigger: 'blur' }
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
    const data = await api.doctors.getList()
    doctors.value = (data || []).map(item => ({
      id: item.doctorId,
      name: item.realName
    }))
  } catch (error) {
    ElMessage.error('加载医生失败')
  }
}

const loadData = async () => {
  loading.value = true
  try {
    // 这里应该调用获取住院列表的API，暂时使用模拟数据
    hospitals.value = [
      {
        id: 1,
        patientName: '张三',
        department: '内科',
        doctorName: '张医生',
        roomNumber: '301',
        bedNumber: '2',
        admissionDate: '2026-04-01',
        dischargeDate: '',
        status: 'in'
      },
      {
        id: 2,
        patientName: '李四',
        department: '外科',
        doctorName: '李医生',
        roomNumber: '402',
        bedNumber: '1',
        admissionDate: '2026-04-03',
        dischargeDate: '',
        status: 'in'
      },
      {
        id: 3,
        patientName: '王五',
        department: '骨科',
        doctorName: '钱医生',
        roomNumber: '503',
        bedNumber: '3',
        admissionDate: '2026-03-28',
        dischargeDate: '2026-04-05',
        status: 'out'
      }
    ]
    pagination.total = hospitals.value.length
  } catch (error) {
    ElMessage.error(error.message || '加载住院记录失败')
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

const handleAdd = () => {
  form.id = ''
  form.patientName = ''
  form.idCard = ''
  form.phone = ''
  form.departmentId = ''
  form.doctorId = ''
  form.roomNumber = ''
  form.bedNumber = ''
  form.admissionDate = ''
  form.dischargeDate = ''
  form.reason = ''
  dialogTitle.value = '新增住院'
  dialogVisible.value = true
}

const handleEdit = (row) => {
  form.id = row.id
  form.patientName = row.patientName
  form.idCard = row.idCard || ''
  form.phone = row.phone || ''
  form.departmentId = row.departmentId || ''
  form.doctorId = row.doctorId || ''
  form.roomNumber = row.roomNumber
  form.bedNumber = row.bedNumber
  form.admissionDate = row.admissionDate
  form.dischargeDate = row.dischargeDate
  form.reason = row.reason || ''
  dialogTitle.value = '编辑住院'
  dialogVisible.value = true
}

const handleDischarge = (id) => {
  ElMessage.warning('当前后端暂未提供出院接口')
}

const handleSubmit = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
    // 模拟患者ID，实际应该从患者信息中获取
    const patientId = 1
    await api.hospitalizations.create({
      patientId,
      doctorId: form.doctorId,
      deptId: form.departmentId,
      admissionDate: form.admissionDate,
      reason: form.reason
    })
    ElMessage.success('住院登记成功')
    dialogVisible.value = false
    loadData()
  } catch (error) {
    ElMessage.error(error.message || '住院登记失败')
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
  await loadDoctors()
  await loadData()
})
</script>

<style scoped>
.hospital {
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