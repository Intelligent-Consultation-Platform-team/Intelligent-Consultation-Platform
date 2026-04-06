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
            <el-option label="已就诊" value="completed" />
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
      <el-table :data="appointments" style="width: 100%">
        <el-table-column prop="id" label="挂号ID" width="100" />
        <el-table-column prop="patientName" label="患者姓名" />
        <el-table-column prop="department" label="科室" />
        <el-table-column prop="doctorName" label="医生" />
        <el-table-column prop="appointmentDate" label="预约日期" width="120" />
        <el-table-column prop="appointmentTime" label="预约时间" width="150" />
        <el-table-column prop="symptoms" label="症状描述" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusTagType(scope.row.status)">
              {{ getStatusLabel(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button type="primary" size="small" @click="handleEdit(scope.row)">
              编辑
            </el-button>
            <el-button type="danger" size="small" @click="handleCancel(scope.row.id)">
              取消
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
        <el-form-item label="医生" prop="doctorId">
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
        <el-form-item label="预约日期" prop="appointmentDate">
          <el-date-picker
            v-model="form.appointmentDate"
            type="date"
            placeholder="选择日期"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="预约时间" prop="appointmentTime">
          <el-select
            v-model="form.appointmentTime"
            placeholder="选择时间"
            style="width: 100%"
          >
            <el-option label="上午 (8:00-12:00)" value="上午" />
            <el-option label="下午 (14:00-18:00)" value="下午" />
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

const filter = reactive({
  patientName: '',
  status: ''
})

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const departments = ref([
  { id: 1, name: '内科' },
  { id: 2, name: '外科' },
  { id: 3, name: '儿科' },
  { id: 4, name: '妇产科' },
  { id: 5, name: '骨科' }
])

const doctors = ref([
  { id: 1, name: '张医生', department: '内科' },
  { id: 2, name: '李医生', department: '外科' },
  { id: 3, name: '王医生', department: '儿科' },
  { id: 4, name: '赵医生', department: '妇产科' },
  { id: 5, name: '钱医生', department: '骨科' }
])

const appointments = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('新增挂号')
const formRef = ref()
const form = reactive({
  id: '',
  patientName: '',
  idCard: '',
  phone: '',
  departmentId: '',
  doctorId: '',
  appointmentDate: '',
  appointmentTime: '',
  symptoms: ''
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
    { required: true, message: '请选择医生', trigger: 'change' }
  ],
  appointmentDate: [
    { required: true, message: '请选择预约日期', trigger: 'change' }
  ],
  appointmentTime: [
    { required: true, message: '请选择预约时间', trigger: 'change' }
  ],
  symptoms: [
    { required: true, message: '请描述症状', trigger: 'blur' }
  ]
}

const loadData = () => {
  // 模拟数据
  appointments.value = [
    {
      id: 1,
      patientName: '张三',
      department: '内科',
      doctorName: '张医生',
      appointmentDate: '2026-04-06',
      appointmentTime: '上午',
      symptoms: '发热、咳嗽',
      status: 'pending'
    },
    {
      id: 2,
      patientName: '李四',
      department: '外科',
      doctorName: '李医生',
      appointmentDate: '2026-04-06',
      appointmentTime: '下午',
      symptoms: '腹痛',
      status: 'completed'
    },
    {
      id: 3,
      patientName: '王五',
      department: '儿科',
      doctorName: '王医生',
      appointmentDate: '2026-04-07',
      appointmentTime: '上午',
      symptoms: '感冒、发烧',
      status: 'pending'
    }
  ]
  pagination.total = appointments.value.length
}

const getStatusLabel = (status) => {
  const statusMap = {
    pending: '待就诊',
    completed: '已就诊',
    cancelled: '已取消'
  }
  return statusMap[status] || '未知'
}

const getStatusTagType = (status) => {
  const typeMap = {
    pending: 'warning',
    completed: 'success',
    cancelled: 'danger'
  }
  return typeMap[status] || 'default'
}

const handleSearch = () => {
  ElMessage.success('查询成功')
  loadData()
}

const resetFilter = () => {
  filter.patientName = ''
  filter.status = ''
  loadData()
}

const handleAdd = () => {
  form.id = ''
  form.patientName = ''
  form.idCard = ''
  form.phone = ''
  form.departmentId = ''
  form.doctorId = ''
  form.appointmentDate = ''
  form.appointmentTime = ''
  form.symptoms = ''
  dialogTitle.value = '新增挂号'
  dialogVisible.value = true
}

const handleEdit = (row) => {
  form.id = row.id
  form.patientName = row.patientName
  form.idCard = row.idCard || ''
  form.phone = row.phone || ''
  form.departmentId = row.departmentId || ''
  form.doctorId = row.doctorId || ''
  form.appointmentDate = row.appointmentDate
  form.appointmentTime = row.appointmentTime
  form.symptoms = row.symptoms
  dialogTitle.value = '编辑挂号'
  dialogVisible.value = true
}

const handleCancel = (id) => {
  ElMessage.success('挂号已取消')
  loadData()
}

const handleSubmit = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
    ElMessage.success('保存成功')
    dialogVisible.value = false
    loadData()
  } catch {
    ElMessage.warning('请完善表单信息')
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

onMounted(() => {
  loadData()
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