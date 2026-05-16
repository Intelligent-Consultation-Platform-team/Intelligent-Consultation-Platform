<template>
  <div class="office">
    <div class="page-header">
      <div>
        <h3>科室信息管理</h3>
        <p>支持筛选、分页和弹窗编辑</p>
      </div>
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        新增科室
      </el-button>
    </div>

    <el-card shadow="never" class="filter-card">
      <el-row :gutter="20">
        <el-col :span="8">
          <el-input v-model="filter.name" placeholder="科室名称" clearable />
        </el-col>
        <el-col :span="8">
          <el-button type="primary" :loading="loading" @click="handleSearch">查询</el-button>
          <el-button @click="resetFilter">重置</el-button>
        </el-col>
      </el-row>
    </el-card>

    <el-card shadow="never" class="table-card">
      <el-skeleton v-if="loading && !hasLoadedOnce" :rows="6" animated />
      <el-empty v-else-if="!loading && !pagedOffices.length" description="暂无科室数据">
        <el-button type="primary" @click="loadData">重新加载</el-button>
      </el-empty>
      <template v-else>
        <el-table v-loading="loading" :data="pagedOffices" style="width: 100%">
          <el-table-column prop="id" label="ID" width="90" />
          <el-table-column prop="name" label="科室名称" />
          <el-table-column prop="description" label="科室描述" show-overflow-tooltip />
          <el-table-column prop="location" label="位置" width="180" />
          <el-table-column prop="createdAt" label="创建时间" width="180" />
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="scope">
              <el-button type="primary" size="small" @click="handleEdit(scope.row)">编辑</el-button>
              <el-button type="danger" size="small" @click="handleDelete(scope.row.id)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <div class="pagination">
          <el-pagination
            v-model:current-page="pagination.current"
            v-model:page-size="pagination.size"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next, jumper"
            :total="filteredOffices.length"
          />
        </div>
      </template>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px" @closed="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="科室名称" prop="name"><el-input v-model.trim="form.name" placeholder="请输入科室名称" /></el-form-item>
        <el-form-item label="科室描述" prop="description"><el-input v-model.trim="form.description" type="textarea" :rows="3" placeholder="请输入科室描述" /></el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="submitLoading" :disabled="submitLoading" @click="handleSubmit">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { api } from '../../utils/api'

const filter = reactive({ name: '' })
const pagination = reactive({ current: 1, size: 10 })
const offices = ref([])
const loading = ref(false)
const submitLoading = ref(false)
const hasLoadedOnce = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('新增科室')
const formRef = ref()
const form = reactive({ id: '', name: '', description: '' })
const rules = { name: [{ required: true, message: '请输入科室名称', trigger: 'blur' }], description: [{ required: true, message: '请输入科室描述', trigger: 'blur' }] }
const filteredOffices = computed(() => offices.value.filter((item) => !filter.name || item.name?.includes(filter.name)))
const pagedOffices = computed(() => filteredOffices.value.slice((pagination.current - 1) * pagination.size, pagination.current * pagination.size))
const formatDate = (value) => (value ? String(value).replace('T', ' ').slice(0, 19) : '-')
const buildDepartmentPayload = () => ({ deptName: form.name.trim(), description: form.description.trim() })

const loadData = async () => {
  loading.value = true
  try {
    const data = await api.departments.getList()
    offices.value = (data || []).map((item) => ({ id: item.deptId ?? item.id, name: item.deptName ?? item.name, description: item.description || '-', location: item.location || '-', createdAt: formatDate(item.createdAt) }))
    hasLoadedOnce.value = true
    pagination.current = 1
  } catch (error) {
    ElMessage.error(error?.message || '加载科室失败')
  } finally {
    loading.value = false
  }
}
const handleSearch = () => { pagination.current = 1; loadData() }
const resetFilter = () => { filter.name = ''; pagination.current = 1; loadData() }
const resetForm = () => { form.id = ''; form.name = ''; form.description = ''; formRef.value?.clearValidate?.() }
const handleAdd = () => { dialogTitle.value = '新增科室'; resetForm(); dialogVisible.value = true }
const handleEdit = (row) => { form.id = row.id; form.name = row.name; form.description = row.description === '-' ? '' : row.description; dialogTitle.value = '编辑科室'; dialogVisible.value = true }

const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确定删除该科室吗？', '提示', { type: 'warning' })
    await api.departments.remove(id)
    ElMessage.success('删除成功')
    await loadData()
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') {
      ElMessage.error(error?.message || '删除失败')
    }
  }
}
const handleSubmit = async () => {
  if (!formRef.value || submitLoading.value) return
  try {
    await formRef.value.validate()
    submitLoading.value = true
    const payload = buildDepartmentPayload()
    if (form.id) {
      await api.departments.update({ deptId: form.id, ...payload })
      ElMessage.success('编辑成功')
    } else {
      await api.departments.create(payload)
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
  } finally {
    submitLoading.value = false
  }
}
onMounted(loadData)
</script>

<style scoped>
.office{padding:20px 0}.page-header{display:flex;justify-content:space-between;align-items:center;margin-bottom:20px}.page-header h3{margin:0;color:#1f2d3d}.page-header p{margin:6px 0 0;color:#6b7280}.filter-card{margin-bottom:20px}.table-card{margin-top:20px}.pagination{margin-top:20px;display:flex;justify-content:flex-end}.dialog-footer{display:flex;justify-content:flex-end}
</style>
