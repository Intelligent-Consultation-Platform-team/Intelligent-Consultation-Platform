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
      <el-table :data="schedules" style="width: 100%">
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

const filter = reactive({
  department: '',
  date: ''
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

const schedules = ref([])
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

const loadData = () => {
  // 模拟数据
  schedules.value = [
    {
      id: 1,
      department: '内科',
      doctorName: '张医生',
      date: '2026-04-06',
      timeRange: '上午 (8:00-12:00)',
      maxNumber: 20,
      remaining: 15
    },
    {
      id: 2,
      department: '外科',
      doctorName: '李医生',
      date: '2026-04-06',
      timeRange: '下午 (14:00-18:00)',
      maxNumber: 15,
      remaining: 10
    },
    {
      id: 3,
      department: '儿科',
      doctorName: '王医生',
      date: '2026-04-07',
      timeRange: '上午 (8:00-12:00)',
      maxNumber: 25,
      remaining: 25
    },
    {
      id: 4,
      department: '妇产科',
      doctorName: '赵医生',
      date: '2026-04-07',
      timeRange: '下午 (14:00-18:00)',
      maxNumber: 18,
      remaining: 18
    },
    {
      id: 5,
      department: '骨科',
      doctorName: '钱医生',
      date: '2026-04-08',
      timeRange: '上午 (8:00-12:00)',
      maxNumber: 22,
      remaining: 22
    }
  ]
  pagination.total = schedules.value.length
}

const handleSearch = () => {
  ElMessage.success('查询成功')
  loadData()
}

const resetFilter = () => {
  filter.department = ''
  filter.date = ''
  loadData()
}

const refreshData = () => {
  ElMessage.success('数据已刷新')
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
    ElMessage.success('预约成功！请准时前往医院就诊。')
    bookDialogVisible.value = false
    loadData()
  } catch {
    ElMessage.warning('请完善预约信息')
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