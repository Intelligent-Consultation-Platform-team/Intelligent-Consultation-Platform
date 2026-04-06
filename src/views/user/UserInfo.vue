<template>
  <div class="user-info">
    <div class="page-header">
      <h3>用户信息</h3>
    </div>

    <el-card shadow="never" class="filter-card">
      <el-row :gutter="20">
        <el-col :span="16">
          <el-input
            v-model="filter.keyword"
            placeholder="请输入关键字查询"
            clearable
            prefix-icon="el-icon-search"
          />
        </el-col>
        <el-col :span="8">
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="resetFilter">重置</el-button>
        </el-col>
      </el-row>
    </el-card>

    <el-card shadow="never" class="table-card">
      <div class="table-header">
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          新增
        </el-button>
        <el-button type="danger" @click="handleBatchDelete">
          批量删除
        </el-button>
      </div>
      <el-table :data="users" style="width: 100%" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" />
        <el-table-column type="index" label="序号" width="80" />
        <el-table-column label="头像" width="100">
          <template #default="scope">
            <el-avatar :size="40" :src="scope.row.avatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'" />
          </template>
        </el-table-column>
        <el-table-column prop="username" label="用户名" />
        <el-table-column prop="realName" label="姓名" />
        <el-table-column prop="role" label="角色标识" width="120">
          <template #default="scope">
            <el-tag type="info">{{ scope.row.role.toUpperCase() }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="电话" />
        <el-table-column prop="email" label="邮箱" />
        <el-table-column prop="level" label="用户等级" width="120">
          <template #default="scope">
            <el-tag type="success">{{ scope.row.level }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="scope">
            <el-button type="primary" size="small" @click="handleEdit(scope.row)">
              编辑
            </el-button>
            <el-button type="danger" size="small" @click="handleDelete(scope.row.id)" style="background-color: #ff7875; border-color: #ff7875;">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination">
        <div class="pagination-info">共 {{ pagination.total }} 条</div>
        <el-pagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 50, 100]"
          layout="prev, pager, next"
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
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="form.realName" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="密码" prop="password" v-if="!form.id">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            show-password
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
  keyword: ''
})

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const users = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('新增用户')
const formRef = ref()
const multipleSelection = ref([])
const form = reactive({
  id: '',
  username: '',
  realName: '',
  phone: '',
  email: '',
  password: '',
  role: 'user',
  level: 100,
  status: 1
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: ['blur', 'change'] }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '邮箱格式不正确', trigger: ['blur', 'change'] }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ]
}

const loadData = () => {
  // 模拟数据
  users.value = [
    {
      id: 1,
      username: 'zhangsan',
      realName: '张三',
      phone: '13877889922',
      email: 'zhangsan@oxrm.com',
      role: 'user',
      level: 110,
      avatar: 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png',
      status: 1
    },
    {
      id: 2,
      username: 'lisi',
      realName: '李四',
      phone: '18899990000',
      email: 'lisi@oxrm.com',
      role: 'user',
      level: 120,
      avatar: 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png',
      status: 1
    }
  ]
  pagination.total = users.value.length
}

const handleSelectionChange = (val) => {
  multipleSelection.value = val
}

const handleBatchDelete = () => {
  if (multipleSelection.value.length === 0) {
    ElMessage.warning('请选择要删除的用户')
    return
  }
  ElMessage.success('批量删除成功')
  loadData()
}

const handleSearch = () => {
  ElMessage.success('查询成功')
  loadData()
}

const resetFilter = () => {
  filter.username = ''
  filter.realName = ''
  loadData()
}

const handleAdd = () => {
  form.id = ''
  form.username = ''
  form.realName = ''
  form.phone = ''
  form.email = ''
  form.password = ''
  form.status = 1
  dialogTitle.value = '新增用户'
  dialogVisible.value = true
}

const handleEdit = (row) => {
  form.id = row.id
  form.username = row.username
  form.realName = row.realName
  form.phone = row.phone
  form.email = row.email
  form.password = ''
  form.status = row.status
  dialogTitle.value = '编辑用户'
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
.user-info {
  padding: 20px 0;
}

.page-header {
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

.table-header {
  display: flex;
  justify-content: flex-start;
  align-items: center;
  margin-bottom: 16px;
  gap: 12px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.pagination-info {
  color: #6b7280;
  font-size: 14px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
}

:deep(.el-button--danger) {
  background-color: #ff7875 !important;
  border-color: #ff7875 !important;
}

:deep(.el-button--danger:hover) {
  background-color: #ff5252 !important;
  border-color: #ff5252 !important;
}
</style>