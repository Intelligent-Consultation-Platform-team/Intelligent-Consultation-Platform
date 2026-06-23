<template>
  <div class="admin">
    <div class="page-header">
      <h3>管理员信息管理</h3>
      <el-button v-if="canManage" type="primary" :loading="submitLoading" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        新增管理员
      </el-button>
    </div>

    <el-card shadow="never" class="filter-card">
      <el-row :gutter="20">
        <el-col :span="8">
          <el-input v-model="filter.username" placeholder="用户名" clearable prefix-icon="el-icon-search" />
        </el-col>
        <el-col :span="8">
          <el-input v-model="filter.realName" placeholder="真实姓名" clearable prefix-icon="el-icon-search" />
        </el-col>
        <el-col :span="8">
          <el-button type="primary" :loading="loading" @click="handleSearch">查询</el-button>
          <el-button @click="resetFilter">重置</el-button>
        </el-col>
      </el-row>
    </el-card>

    <el-card shadow="never" class="table-card">
      <el-table v-loading="loading" :data="pagedAdmins" style="width: 100%" :empty-text="errorText || '暂无数据'">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" />
        <el-table-column prop="realName" label="真实姓名" />
        <el-table-column prop="phone" label="手机号" />
        <el-table-column prop="email" label="邮箱" />
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column v-if="canManage" label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button type="primary" size="small" :loading="rowSubmitting === scope.row.id" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button type="danger" size="small" :loading="rowDeleting === scope.row.id" @click="handleDelete(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!loading && !admins.length" :description="errorText || '暂无管理员数据'" />
      <div class="pagination">
        <el-pagination v-model:current-page="pagination.current" v-model:page-size="pagination.size" :page-sizes="[10, 20, 50, 100]" layout="total, sizes, prev, pager, next, jumper" :total="pagination.total" @size-change="handleSizeChange" @current-change="handleCurrentChange" />
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="用户名" prop="username"><el-input v-model="form.username" placeholder="请输入用户名" /></el-form-item>
        <el-form-item label="真实姓名" prop="realName"><el-input v-model="form.realName" placeholder="请输入真实姓名" /></el-form-item>
        <el-form-item label="手机号" prop="phone"><el-input v-model="form.phone" placeholder="请输入手机号" /></el-form-item>
        <el-form-item label="邮箱" prop="email"><el-input v-model="form.email" placeholder="请输入邮箱" /></el-form-item>
        <el-form-item label="密码" prop="password" v-if="!form.id"><el-input v-model="form.password" type="password" placeholder="请输入密码" show-password /></el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { api } from '../../utils/api'

const getSession = () => { try { return JSON.parse(localStorage.getItem('demo_session') || '{}') } catch { return {} } }
const currentRole = getSession().role || ''
const canManage = computed(() => currentRole === 'admin')
const filter = reactive({ username: '', realName: '' })
const pagination = reactive({ current: 1, size: 10, total: 0 })
const admins = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('新增管理员')
const formRef = ref()
const submitLoading = ref(false)
const loading = ref(false)
const errorText = ref('')
const rowSubmitting = ref('')
const rowDeleting = ref('')
const form = reactive({ userId: '', username: '', realName: '', phone: '', email: '', password: '' })
const rules = { username:[{ required:true,message:'请输入用户名',trigger:'blur' }], realName:[{ required:true,message:'请输入真实姓名',trigger:'blur' }], phone:[{ required:true,message:'请输入手机号',trigger:'blur' },{ pattern:/^1[3-9]\d{9}$/,message:'手机号格式不正确',trigger:['blur','change'] }], email:[{ required:true,message:'请输入邮箱',trigger:'blur' },{ type:'email',message:'邮箱格式不正确',trigger:['blur','change'] }], password:[{ required:true,message:'请输入密码',trigger:'blur' },{ min:6,message:'密码长度不能少于6位',trigger:'blur' }] }

const loadData = async () => {
  loading.value = true
  errorText.value = ''
  try {
    const data = await api.users.getList()
    const adminUsers = (data || []).filter(item => item.role === 'admin')
    const filtered = adminUsers.filter(item => {
      const matchUsername = !filter.username || (item.username && item.username.toLowerCase().includes(filter.username.toLowerCase()))
      const matchRealName = !filter.realName || (item.realName && item.realName.toLowerCase().includes(filter.realName.toLowerCase()))
      return matchUsername && matchRealName
    })
    admins.value = filtered.map(item => ({
      id: item.userId,
      username: item.username,
      realName: item.realName,
      phone: item.phone,
      email: item.email,
      createdAt: item.createdAt
    }))
    pagination.total = admins.value.length
  } catch (e) {
    errorText.value = e?.message || '加载失败'
    ElMessage.error(errorText.value)
  } finally {
    loading.value = false
  }
}
const pagedAdmins = computed(() => admins.value.slice((pagination.current - 1) * pagination.size, pagination.current * pagination.size))
const handleSearch = () => { pagination.current = 1; loadData() }
const resetFilter = () => { filter.username = ''; filter.realName = ''; pagination.current = 1; loadData() }
const resetForm = () => { form.userId = ''; form.username = ''; form.realName = ''; form.phone = ''; form.email = ''; form.password = '' }
const handleAdd = () => { if (!canManage.value) return ElMessage.warning('无操作权限'); resetForm(); dialogTitle.value = '新增管理员'; dialogVisible.value = true }
const handleEdit = (row) => { if (!canManage.value) return ElMessage.warning('无操作权限'); rowSubmitting.value = row.id; resetForm(); form.userId = row.id; form.username = row.username; form.realName = row.realName; form.phone = row.phone; form.email = row.email; dialogTitle.value = '编辑管理员'; dialogVisible.value = true; rowSubmitting.value = '' }
const handleDelete = async (id) => { if (!canManage.value) return ElMessage.warning('无操作权限'); try { rowDeleting.value = id; await api.users.remove(id); ElMessage.success('删除成功'); await loadData() } catch (e) { ElMessage.error(e?.message || '删除失败') } finally { rowDeleting.value = '' } }
const handleSubmit = async () => { if (!formRef.value || submitLoading.value) return; try { await formRef.value.validate(); submitLoading.value = true; if (form.userId) { await api.users.update({ userId: form.userId, username: form.username, realName: form.realName, phone: form.phone, email: form.email, role: 'admin' }); ElMessage.success('编辑成功') } else { await api.auth.register({ username: form.username, password: form.password, realName: form.realName, phone: form.phone, email: form.email, role: 'admin' }); ElMessage.success('新增成功') } dialogVisible.value = false; await loadData() } catch (e) { if (e?.message) ElMessage.error(e.message) } finally { submitLoading.value = false } }
const handleSizeChange = (size) => { pagination.size = size; pagination.current = 1 }
const handleCurrentChange = (current) => { pagination.current = current }
onMounted(loadData)
</script>

<style scoped>
.admin{padding:20px 0}.page-header{display:flex;justify-content:space-between;align-items:center;margin-bottom:20px}.page-header h3{margin:0;color:#1f2d3d}.filter-card{margin-bottom:20px}.table-card{margin-top:20px}.pagination{margin-top:20px;display:flex;justify-content:flex-end}.dialog-footer{display:flex;justify-content:flex-end}
</style>