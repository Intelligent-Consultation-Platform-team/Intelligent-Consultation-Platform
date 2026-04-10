<template>
  <div class="office">
    <div class="page-header">
      <h3>科室信息管理</h3>
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        新增科室
      </el-button>
    </div>

    <el-card shadow="never" class="filter-card">
      <el-row :gutter="20">
        <el-col :span="8">
          <el-input
            v-model="filter.name"
            placeholder="科室名称"
            clearable
          />
        </el-col>
        <el-col :span="8">
          <el-button type="primary" :loading="loading" @click="handleSearch">查询</el-button>
          <el-button @click="resetFilter">重置</el-button>
        </el-col>
      </el-row>
    </el-card>

    <el-card shadow="never" class="table-card">
      <el-table v-loading="loading" :data="pagedOffices" style="width: 100%">
        <el-table-column prop="id" label="ID" width="90" />
        <el-table-column prop="name" label="科室名称" />
        <el-table-column prop="description" label="科室描述" show-overflow-tooltip />
        <el-table-column prop="location" label="位置" width="180" />
        <el-table-column prop="createdAt" label="创建时间" width="180" />
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
        <el-form-item label="科室名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入科室名称" />
        </el-form-item>
        <el-form-item label="科室描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="3"
            placeholder="请输入科室描述"
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
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { request } from '../../utils/request'

const filter = reactive({
  name: '',
})

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0,
})

const offices = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('新增科室')
const formRef = ref()
const form = reactive({
  id: '',
  name: '',
  description: '',
})

const rules = {
  name: [{ required: true, message: '请输入科室名称', trigger: 'blur' }],
  description: [{ required: true, message: '请输入科室描述', trigger: 'blur' }],
}

const filteredOffices = computed(() => {
  return offices.value.filter((item) => {
    if (!filter.name) return true
    return item.name?.includes(filter.name)
  })
})

const pagedOffices = computed(() => {
  const start = (pagination.current - 1) * pagination.size
  const end = start + pagination.size
  return filteredOffices.value.slice(start, end)
})

const formatDate = (value) => {
  if (!value) return '-'
  return String(value).replace('T', ' ').slice(0, 19)
}

const loadData = async () => {
  loading.value = true
  try {
    const data = await request('/departments')
    offices.value = (data || []).map((item) => ({
      id: item.deptId,
      name: item.deptName,
      description: item.description || '-',
      location: item.location || '-',
      createdAt: formatDate(item.createdAt),
    }))
    pagination.total = filteredOffices.value.length
  } catch (error) {
    ElMessage.error(error.message || '加载科室失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.current = 1
  pagination.total = filteredOffices.value.length
}

const resetFilter = () => {
  filter.name = ''
  pagination.current = 1
  pagination.total = filteredOffices.value.length
}

const handleAdd = () => {
  form.id = ''
  form.name = ''
  form.description = ''
  dialogTitle.value = '新增科室'
  dialogVisible.value = true
}

const handleEdit = (row) => {
  form.id = row.id
  form.name = row.name
  form.description = row.description === '-' ? '' : row.description
  dialogTitle.value = '编辑科室'
  dialogVisible.value = true
}

const handleDelete = () => {
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
}

const handleCurrentChange = (current) => {
  pagination.current = current
}

onMounted(async () => {
  await loadData()
})
</script>

<style scoped>
.office {
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
