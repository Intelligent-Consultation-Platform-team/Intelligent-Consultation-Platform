<template>
  <div class="doctor">
    <div class="page-header">
      <h3>医生信息管理</h3>
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        新增医生
      </el-button>
    </div>

    <el-card shadow="never" class="filter-card">
      <el-row :gutter="20">
        <el-col :span="8">
          <el-input
            v-model="filter.name"
            placeholder="医生姓名"
            clearable
            prefix-icon="el-icon-search"
          />
        </el-col>
        <el-col :span="8">
          <el-select
            v-model="filter.department"
            placeholder="所属科室"
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
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="resetFilter">重置</el-button>
        </el-col>
      </el-row>
    </el-card>

    <el-card shadow="never" class="table-card">
      <el-table :data="doctors" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="姓名" />
        <el-table-column prop="department" label="所属科室" />
        <el-table-column prop="title" label="职称" />
        <el-table-column prop="specialty" label="专长" show-overflow-tooltip />
        <el-table-column prop="phone" label="联系电话" />
        <el-table-column prop="email" label="邮箱" />
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
        label-width="100px"
      >
        <el-form-item label="姓名" prop="name">
          <el-input v-model="form.name" placeholder="请输入医生姓名" />
        </el-form-item>
        <el-form-item label="所属科室" prop="departmentId">
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
        <el-form-item label="职称" prop="title">
          <el-input v-model="form.title" placeholder="请输入职称" />
        </el-form-item>
        <el-form-item label="专长" prop="specialty">
          <el-input
            v-model="form.specialty"
            type="textarea"
            :rows="2"
            placeholder="请输入专长"
          />
        </el-form-item>
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="请输入邮箱" />
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
  name: '',
  department: ''
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

const doctors = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('新增医生')
const formRef = ref()
const form = reactive({
  id: '',
  name: '',
  departmentId: '',
  title: '',
  specialty: '',
  phone: '',
  email: '',
  status: 1
})

const rules = {
  name: [
    { required: true, message: '请输入医生姓名', trigger: 'blur' }
  ],
  departmentId: [
    { required: true, message: '请选择所属科室', trigger: 'change' }
  ],
  title: [
    { required: true, message: '请输入职称', trigger: 'blur' }
  ],
  specialty: [
    { required: true, message: '请输入专长', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: ['blur', 'change'] }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '邮箱格式不正确', trigger: ['blur', 'change'] }
  ]
}

const loadData = () => {
  // 模拟数据
  doctors.value = [
    {
      id: 1,
      name: '张医生',
      department: '内科',
      title: '主任医师',
      specialty: '心血管疾病',
      phone: '13800138001',
      email: 'zhang@example.com',
      status: 1
    },
    {
      id: 2,
      name: '李医生',
      department: '外科',
      title: '副主任医师',
      specialty: '普外科',
      phone: '13800138002',
      email: 'li@example.com',
      status: 1
    },
    {
      id: 3,
      name: '王医生',
      department: '儿科',
      title: '主治医师',
      specialty: '小儿内科',
      phone: '13800138003',
      email: 'wang@example.com',
      status: 1
    },
    {
      id: 4,
      name: '赵医生',
      department: '妇产科',
      title: '主任医师',
      specialty: '产科',
      phone: '13800138004',
      email: 'zhao@example.com',
      status: 1
    },
    {
      id: 5,
      name: '钱医生',
      department: '骨科',
      title: '副主任医师',
      specialty: '骨关节外科',
      phone: '13800138005',
      email: 'qian@example.com',
      status: 1
    }
  ]
  pagination.total = doctors.value.length
}

const handleSearch = () => {
  ElMessage.success('查询成功')
  loadData()
}

const resetFilter = () => {
  filter.name = ''
  filter.department = ''
  loadData()
}

const handleAdd = () => {
  form.id = ''
  form.name = ''
  form.departmentId = ''
  form.title = ''
  form.specialty = ''
  form.phone = ''
  form.email = ''
  form.status = 1
  dialogTitle.value = '新增医生'
  dialogVisible.value = true
}

const handleEdit = (row) => {
  form.id = row.id
  form.name = row.name
  form.departmentId = row.departmentId || ''
  form.title = row.title
  form.specialty = row.specialty
  form.phone = row.phone
  form.email = row.email
  form.status = row.status
  dialogTitle.value = '编辑医生'
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
.doctor {
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