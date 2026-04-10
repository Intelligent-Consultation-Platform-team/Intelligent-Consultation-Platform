<template>
  <div class="notice">
    <div class="page-header">
      <h3>公告信息管理</h3>
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        新增公告
      </el-button>
    </div>

    <el-card shadow="never" class="filter-card">
      <el-row :gutter="20">
        <el-col :span="8">
          <el-input
            v-model="filter.title"
            placeholder="公告标题"
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
      <el-table v-loading="loading" :data="pagedNotices" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="标题" />
        <el-table-column prop="content" label="内容" show-overflow-tooltip />
        <el-table-column prop="statusText" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 'active' ? 'success' : 'danger'">
              {{ scope.row.statusText }}
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
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入公告标题" />
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="4"
            placeholder="请输入公告内容"
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
  title: '',
})

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0,
})

const notices = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('新增公告')
const formRef = ref()
const form = reactive({
  id: '',
  title: '',
  content: '',
})

const rules = {
  title: [{ required: true, message: '请输入公告标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入公告内容', trigger: 'blur' }],
}

const filteredNotices = computed(() => {
  return notices.value.filter((item) => {
    if (!filter.title) return true
    return item.title?.includes(filter.title)
  })
})

const pagedNotices = computed(() => {
  const start = (pagination.current - 1) * pagination.size
  const end = start + pagination.size
  return filteredNotices.value.slice(start, end)
})

const formatDate = (value) => {
  if (!value) return '-'
  return String(value).replace('T', ' ').slice(0, 19)
}

const loadData = async () => {
  loading.value = true
  try {
    const data = await request('/notices')
    notices.value = (data || []).map((item) => ({
      id: item.noticeId,
      title: item.title,
      content: item.content,
      status: item.status,
      statusText: item.status === 'active' ? '启用' : '禁用',
      createdAt: formatDate(item.createdAt),
    }))
    pagination.total = filteredNotices.value.length
  } catch (error) {
    ElMessage.error(error.message || '加载公告失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.current = 1
  pagination.total = filteredNotices.value.length
}

const resetFilter = () => {
  filter.title = ''
  pagination.current = 1
  pagination.total = filteredNotices.value.length
}

const handleAdd = () => {
  form.id = ''
  form.title = ''
  form.content = ''
  dialogTitle.value = '新增公告'
  dialogVisible.value = true
}

const handleEdit = (row) => {
  form.id = row.id
  form.title = row.title
  form.content = row.content
  dialogTitle.value = '编辑公告'
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
.notice {
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
