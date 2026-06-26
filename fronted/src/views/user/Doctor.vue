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
      <el-table v-loading="loading" :data="pagedDoctors" style="width: 100%">
        <el-table-column prop="name" label="姓名" />
        <el-table-column prop="username" label="用户名" />
        <el-table-column prop="department" label="所属科室" />
        <el-table-column prop="title" label="职称" />
        <el-table-column prop="specialty" label="专长" show-overflow-tooltip />
        <el-table-column prop="phone" label="联系电话" />
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
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" :placeholder="form.id ? '' : '用于登录系统，默认密码123456'" :disabled="!!form.id" />
        </el-form-item>
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
import { ref, reactive, computed, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { api } from '../../utils/api'

const filter = reactive({
  name: '',
  department: ''
})

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const departments = ref([])
const doctors = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('新增医生')
const formRef = ref()

const pagedDoctors = computed(() => doctors.value.slice((pagination.current - 1) * pagination.size, pagination.current * pagination.size))
const form = reactive({
  id: '',
  userId: '',
  username: '',
  name: '',
  departmentId: '',
  title: '',
  specialty: '',
  phone: ''
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
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

const loadData = async () => {
  loading.value = true
  try {
    const params = {}
    if (filter.department) {
      params.deptId = filter.department
    }
    const data = await api.doctors.getList(params)
    let list = (data || []).map(item => ({
      id: item.doctorId ?? item.id,
      userId: item.userId,
      name: item.realName ?? item.name,
      username: item.username || '-',
      department: item.deptName || '-',
      departmentId: item.deptId ?? item.departmentId ?? '',
      title: item.title || '-',
      specialty: item.specialty || '-',
      phone: item.phone || '-'
    }))
    if (filter.name) {
      const keyword = filter.name.toLowerCase()
      list = list.filter(item => (item.name || '').toLowerCase().includes(keyword))
    }
    doctors.value = list
    pagination.total = doctors.value.length
  } catch (error) {
    ElMessage.error(error.message || '加载医生失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.current = 1
  loadData()
}

const resetFilter = () => {
  filter.name = ''
  filter.department = ''
  pagination.current = 1
  loadData()
}

const handleAdd = () => {
  form.id = ''
  form.userId = ''
  form.username = ''
  form.name = ''
  form.departmentId = ''
  form.title = ''
  form.specialty = ''
  form.phone = ''
  dialogTitle.value = '新增医生'
  dialogVisible.value = true
}

const handleEdit = (row) => {
  nextTick(() => {
    form.id = row.id
    form.userId = row.userId || ''
    form.username = row.username || ''
    form.name = row.name
    form.departmentId = row.departmentId || ''
    form.title = row.title === '-' ? '' : row.title
    form.specialty = row.specialty === '-' ? '' : row.specialty
    form.phone = row.phone === '-' ? '' : row.phone
    dialogTitle.value = '编辑医生'
    dialogVisible.value = true
  })
}

const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确定删除该医生吗？', '提示', { type: 'warning' })
    await api.doctors.remove(id)
    ElMessage.success('删除成功')
    await loadData()
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') {
      ElMessage.error(error?.message || '删除失败')
    }
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
    const payload = {
      doctorId: form.id,
      userId: form.userId,
      deptId: form.departmentId,
      title: form.title,
      specialty: form.specialty,
      realName: form.name,
      phone: form.phone
    }
    if (form.id) {
      await api.doctors.update(payload)
      ElMessage.success('编辑成功')
    } else {
      await api.doctors.create({ ...payload, username: form.username })
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    await loadData()
  } catch (error) {
    if (error?.message) {
      ElMessage.error(error.message)
    } else {
      ElMessage.warning('请完善表单信息')
    }
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