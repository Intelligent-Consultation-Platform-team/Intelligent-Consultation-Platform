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
            <el-option label="启用" value="1" />
            <el-option label="禁用" value="0" />
          </el-select>
        </el-col>
        <el-col :span="8">
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="resetFilter">重置</el-button>
        </el-col>
      </el-row>
    </el-card>

    <el-card shadow="never" class="table-card">
      <el-table :data="offices" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="科室名称" />
        <el-table-column prop="description" label="科室描述" show-overflow-tooltip />
        <el-table-column prop="doctorCount" label="医生数量" width="120" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
              {{ scope.row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
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
  status: ''
})

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const offices = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('新增科室')
const formRef = ref()
const form = reactive({
  id: '',
  name: '',
  description: '',
  status: 1
})

const rules = {
  name: [
    { required: true, message: '请输入科室名称', trigger: 'blur' }
  ],
  description: [
    { required: true, message: '请输入科室描述', trigger: 'blur' }
  ]
}

const loadData = () => {
  // 模拟数据
  offices.value = [
    {
      id: 1,
      name: '内科',
      description: '内科是医学的一个重要分支，主要负责诊断和治疗内脏系统疾病。',
      doctorCount: 12,
      status: 1,
      createdAt: '2026-01-01 00:00:00'
    },
    {
      id: 2,
      name: '外科',
      description: '外科主要负责需要手术治疗的疾病，包括各种外科手术。',
      doctorCount: 8,
      status: 1,
      createdAt: '2026-01-01 00:00:00'
    },
    {
      id: 3,
      name: '儿科',
      description: '儿科专门负责儿童的医疗保健和疾病治疗。',
      doctorCount: 6,
      status: 1,
      createdAt: '2026-01-01 00:00:00'
    },
    {
      id: 4,
      name: '妇产科',
      description: '妇产科负责女性生殖系统的健康和疾病治疗。',
      doctorCount: 5,
      status: 1,
      createdAt: '2026-01-01 00:00:00'
    },
    {
      id: 5,
      name: '骨科',
      description: '骨科负责骨骼、关节、肌肉等运动系统疾病的诊断和治疗。',
      doctorCount: 7,
      status: 1,
      createdAt: '2026-01-01 00:00:00'
    }
  ]
  pagination.total = offices.value.length
}

const handleSearch = () => {
  ElMessage.success('查询成功')
  loadData()
}

const resetFilter = () => {
  filter.name = ''
  filter.status = ''
  loadData()
}

const handleAdd = () => {
  form.id = ''
  form.name = ''
  form.description = ''
  form.status = 1
  dialogTitle.value = '新增科室'
  dialogVisible.value = true
}

const handleEdit = (row) => {
  form.id = row.id
  form.name = row.name
  form.description = row.description
  form.status = row.status
  dialogTitle.value = '编辑科室'
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