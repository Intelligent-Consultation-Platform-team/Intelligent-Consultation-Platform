<template>
  <div class="visit">
    <div class="page-header">
      <h3>患者就诊管理</h3>
      <el-button type="primary" @click="refreshData">
        <el-icon><Refresh /></el-icon>
        刷新
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
            <el-option label="就诊中" value="processing" />
            <el-option label="已完成" value="completed" />
          </el-select>
        </el-col>
        <el-col :span="8">
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="resetFilter">重置</el-button>
        </el-col>
      </el-row>
    </el-card>

    <el-card shadow="never" class="table-card">
      <el-table :data="visits" style="width: 100%">
        <el-table-column prop="id" label="就诊ID" width="100" />
        <el-table-column prop="patientName" label="患者姓名" />
        <el-table-column prop="department" label="科室" />
        <el-table-column prop="doctorName" label="医生" />
        <el-table-column prop="visitDate" label="就诊日期" width="120" />
        <el-table-column prop="visitTime" label="就诊时间" width="150" />
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
            <el-button type="primary" size="small" @click="handleDiagnose(scope.row)">
              诊断
            </el-button>
            <el-button type="success" size="small" @click="handleComplete(scope.row.id)" :disabled="scope.row.status === 'completed'">
              完成
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
      v-model="diagnoseDialogVisible"
      title="诊断记录"
      width="600px"
    >
      <div v-if="selectedVisit">
        <h4>患者信息</h4>
        <p><strong>姓名：</strong>{{ selectedVisit.patientName }}</p>
        <p><strong>科室：</strong>{{ selectedVisit.department }}</p>
        <p><strong>症状：</strong>{{ selectedVisit.symptoms }}</p>
        <el-divider />
        <h4>诊断信息</h4>
        <el-form
          ref="diagnoseFormRef"
          :model="diagnoseForm"
          :rules="diagnoseRules"
          label-width="100px"
        >
          <el-form-item label="诊断结果" prop="diagnosis">
            <el-input
              v-model="diagnoseForm.diagnosis"
              type="textarea"
              :rows="3"
              placeholder="请输入诊断结果"
            />
          </el-form-item>
          <el-form-item label="治疗方案" prop="treatment">
            <el-input
              v-model="diagnoseForm.treatment"
              type="textarea"
              :rows="3"
              placeholder="请输入治疗方案"
            />
          </el-form-item>
          <el-form-item label="处方药物" prop="prescription">
            <el-input
              v-model="diagnoseForm.prescription"
              type="textarea"
              :rows="3"
              placeholder="请输入处方药物"
            />
          </el-form-item>
          <el-form-item label="医嘱" prop="advice">
            <el-input
              v-model="diagnoseForm.advice"
              type="textarea"
              :rows="2"
              placeholder="请输入医嘱"
            />
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="diagnoseDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmDiagnosis">保存诊断</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'

const filter = reactive({
  patientName: '',
  status: ''
})

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const visits = ref([])
const diagnoseDialogVisible = ref(false)
const selectedVisit = ref(null)
const diagnoseFormRef = ref()
const diagnoseForm = reactive({
  diagnosis: '',
  treatment: '',
  prescription: '',
  advice: ''
})

const diagnoseRules = {
  diagnosis: [
    { required: true, message: '请输入诊断结果', trigger: 'blur' }
  ],
  treatment: [
    { required: true, message: '请输入治疗方案', trigger: 'blur' }
  ]
}

const loadData = () => {
  // 模拟数据
  visits.value = [
    {
      id: 1,
      patientName: '张三',
      department: '内科',
      doctorName: '张医生',
      visitDate: '2026-04-06',
      visitTime: '09:00',
      symptoms: '发热、咳嗽、咽痛',
      status: 'pending'
    },
    {
      id: 2,
      patientName: '李四',
      department: '外科',
      doctorName: '李医生',
      visitDate: '2026-04-06',
      visitTime: '10:30',
      symptoms: '腹痛、恶心、呕吐',
      status: 'processing'
    },
    {
      id: 3,
      patientName: '王五',
      department: '儿科',
      doctorName: '王医生',
      visitDate: '2026-04-06',
      visitTime: '14:00',
      symptoms: '感冒、发烧、流鼻涕',
      status: 'pending'
    }
  ]
  pagination.total = visits.value.length
}

const getStatusLabel = (status) => {
  const statusMap = {
    pending: '待就诊',
    processing: '就诊中',
    completed: '已完成'
  }
  return statusMap[status] || '未知'
}

const getStatusTagType = (status) => {
  const typeMap = {
    pending: 'warning',
    processing: 'primary',
    completed: 'success'
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

const refreshData = () => {
  ElMessage.success('数据已刷新')
  loadData()
}

const handleDiagnose = (row) => {
  selectedVisit.value = row
  diagnoseForm.diagnosis = ''
  diagnoseForm.treatment = ''
  diagnoseForm.prescription = ''
  diagnoseForm.advice = ''
  diagnoseDialogVisible.value = true
}

const confirmDiagnosis = async () => {
  if (!diagnoseFormRef.value) return
  try {
    await diagnoseFormRef.value.validate()
    ElMessage.success('诊断保存成功')
    diagnoseDialogVisible.value = false
    loadData()
  } catch {
    ElMessage.warning('请完善诊断信息')
  }
}

const handleComplete = (id) => {
  ElMessage.success('就诊已完成')
  loadData()
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
.visit {
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