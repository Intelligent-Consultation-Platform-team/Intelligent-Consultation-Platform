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
      <el-table :data="schedules" style="width: 100%">
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

const filter = reactive({
  doctorId: '',
  date: ''
})

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const doctors = ref([
  { id: 1, name: '张医生', department: '内科' },
  { id: 2, name: '李医生', department: '外科' },
  { id: 3, name: '王医生', department: '儿科' },
  { id: 4, name: '赵医生', department: '妇产科' },
  { id: 5, name: '钱医生', department: '骨科' }
])

const schedules = ref([])
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

const loadData = () => {
  // 模拟数据
  schedules.value = [
    {
      id: 1,
      doctorId: 1,
      doctorName: '张医生',
      department: '内科',
      date: '2026-04-06',
      timeRange: '上午',
      maxNumber: 20,
      remaining: 15,
      status: 1
    },
    {
      id: 2,
      doctorId: 2,
      doctorName: '李医生',
      department: '外科',
      date: '2026-04-06',
      timeRange: '下午',
      maxNumber: 15,
      remaining: 10,
      status: 1
    },
    {
      id: 3,
      doctorId: 3,
      doctorName: '王医生',
      department: '儿科',
      date: '2026-04-07',
      timeRange: '上午',
      maxNumber: 25,
      remaining: 25,
      status: 1
    }
  ]
  pagination.total = schedules.value.length
}

const handleSearch = () => {
  ElMessage.success('查询成功')
  loadData()
}

const resetFilter = () => {
  filter.doctorId = ''
  filter.date = ''
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
  ElMessage.success('删除成功')
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