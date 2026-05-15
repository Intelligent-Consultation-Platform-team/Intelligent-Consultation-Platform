<template>
  <div class="schedule">
    <div class="page-header">
      <h3>医生排班管理</h3>
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        新增排班
      </el-button>
    </div>

    <el-card shadow="never" class="filter-card">
      <el-row :gutter="20">
        <el-col :span="8">
          <el-select
            v-model="filter.doctorId"
            placeholder="选择医生"
            clearable
            style="width: 100%"
          >
            <el-option
              v-for="doctor in doctors"
              :key="doctor.id"
              :label="doctor.name"
              :value="doctor.id"
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
        <el-table-column prop="doctorName" label="医生姓名" />
        <el-table-column prop="department" label="所属科室" />
        <el-table-column prop="date" label="日期" width="120" />
        <el-table-column prop="timeRange" label="时间段" width="150" />
        <el-table-column prop="maxNumber" label="最大号数" width="120" />
        <el-table-column prop="remaining" label="剩余号数" width="120" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
              {{ scope.row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button type="primary" size="small" @click="handleEdit(scope.row)">
              编辑
            </el-button>
            <el-button type="danger" size="small" @click="handleDelete(scope.row.id)">
              删除
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
      width="500px"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="80px"
      >
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
        <el-form-item label="日期" prop="date">
          <el-date-picker
            v-model="form.date"
            type="date"
            placeholder="选择日期"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="时间段" prop="timeRange">
          <el-select
            v-model="form.timeRange"
            placeholder="选择时间段"
            style="width: 100%"
          >
            <el-option label="上午 (8:00-12:00)" value="上午" />
            <el-option label="下午 (14:00-18:00)" value="下午" />
            <el-option label="晚上 (18:00-21:00)" value="晚上" />
          </el-select>
        </el-form-item>
        <el-form-item label="最大号数" prop="maxNumber">
          <el-input
            v-model.number="form.maxNumber"
            type="number"
            placeholder="请输入最大号数"
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-switch v-model="form.status" active-value="1" inactive-value="0" />
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
  doctorId: '',
  date: ''
})

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const doctors = ref([])
const schedules = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('新增排班')
const formRef = ref()
const form = reactive({
  id: '',
  doctorId: '',
  date: '',
  timeRange: '',
  maxNumber: 20,
  status: 1
})

const rules = {
  doctorId: [
    { required: true, message: '请选择医生', trigger: 'change' }
  ],
  date: [
    { required: true, message: '请选择日期', trigger: 'change' }
  ],
  timeRange: [
    { required: true, message: '请选择时间段', trigger: 'change' }
  ],
  maxNumber: [
    { required: true, message: '请输入最大号数', trigger: 'blur' },
    { type: 'number', min: 1, message: '最大号数必须大于0', trigger: 'blur' }
  ]
}

const loadDoctors = async () => {
  try {
    const data = await api.doctors.getList()
    doctors.value = (data || []).map(item => ({
      id: item.doctorId,
      name: item.realName,
      department: item.deptName || '-'
    }))
  } catch (error) {
    ElMessage.error('加载医生列表失败')
  }
}

const loadData = async () => {
  loading.value = true
  try {
    const params = {}
    if (filter.doctorId) {
      params.doctorId = filter.doctorId
    }
    if (filter.date) {
      params.date = filter.date
    }
    const data = await api.schedules.getList(params)
    schedules.value = (data || []).map(item => {
      // 查找对应的医生信息
      const doctor = doctors.value.find(d => d.id === item.doctorId) || {}
      return {
        id: item.scheduleId,
        doctorId: item.doctorId,
        doctorName: doctor.name || '-',
        department: doctor.department || '-',
        date: item.date || '-',
        timeRange: `${item.startTime} - ${item.endTime}`,
        maxNumber: item.availableSlots || 0,
        remaining: item.availableSlots || 0,
        status: item.status === 'active' ? 1 : 0
      }
    })
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
  filter.doctorId = ''
  filter.date = ''
  pagination.current = 1
  loadData()
}

const handleAdd = () => {
  form.id = ''
  form.doctorId = ''
  form.date = ''
  form.timeRange = ''
  form.maxNumber = 20
  form.status = 1
  dialogTitle.value = '新增排班'
  dialogVisible.value = true
}

const handleEdit = (row) => {
  form.id = row.id
  form.doctorId = row.doctorId
  form.date = row.date
  form.timeRange = row.timeRange
  form.maxNumber = row.maxNumber
  form.status = row.status
  dialogTitle.value = '编辑排班'
  dialogVisible.value = true
}

const handleDelete = (id) => {
  ElMessage.warning('当前后端暂未提供删除接口')
}

const handleSubmit = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
    ElMessage.warning('当前后端暂未提供新增/编辑接口')
    dialogVisible.value = false
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

onMounted(async () => {
  await loadDoctors()
  await loadData()
})
</script>

<style scoped>
.schedule {
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