<template>
  <div class="profile">
    <div class="page-header">
      <h3>个人信息</h3>
      <el-button type="primary" @click="handleEdit" v-if="!editing">
        <el-icon><Edit /></el-icon>
        编辑
      </el-button>
    </div>

    <el-card shadow="never" v-loading="loading">
      <el-descriptions :column="2" border>
        <!-- 公共信息 -->
        <el-descriptions-item label="用户名">{{ profile.username || '-' }}</el-descriptions-item>
        <el-descriptions-item label="角色">
          <el-tag>{{ roleText[profile.role] || profile.role }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="真实姓名">
          <template v-if="editing">
            <el-input v-model="form.realName" placeholder="真实姓名" />
          </template>
          <template v-else>{{ profile.realName || '-' }}</template>
        </el-descriptions-item>
        <el-descriptions-item label="手机号">
          <template v-if="editing">
            <el-input v-model="form.phone" placeholder="手机号" />
          </template>
          <template v-else>{{ profile.phone || '-' }}</template>
        </el-descriptions-item>
        <el-descriptions-item label="邮箱" :span="2">
          <template v-if="editing">
            <el-input v-model="form.email" placeholder="邮箱" />
          </template>
          <template v-else>{{ profile.email || '-' }}</template>
        </el-descriptions-item>

        <!-- 医生字段 -->
        <template v-if="profile.role === 'doctor'">
          <el-descriptions-item label="职称">
            <template v-if="editing">
              <el-input v-model="form.title" placeholder="如：主任医师" />
            </template>
            <template v-else>{{ profile.title || '-' }}</template>
          </el-descriptions-item>
          <el-descriptions-item label="专长">
            <template v-if="editing">
              <el-input v-model="form.specialty" placeholder="如：心血管内科" />
            </template>
            <template v-else>{{ profile.specialty || '-' }}</template>
          </el-descriptions-item>
          <el-descriptions-item label="简介" :span="2">
            <template v-if="editing">
              <el-input v-model="form.bio" type="textarea" :rows="3" placeholder="个人简介" />
            </template>
            <template v-else>{{ profile.bio || '-' }}</template>
          </el-descriptions-item>
        </template>

        <!-- 患者字段 -->
        <template v-if="profile.role === 'patient'">
          <el-descriptions-item label="性别">
            <template v-if="editing">
              <el-select v-model="form.gender" placeholder="选择性别" style="width:100%">
                <el-option label="男" value="male" />
                <el-option label="女" value="female" />
              </el-select>
            </template>
            <template v-else>{{ profile.gender === 'male' ? '男' : profile.gender === 'female' ? '女' : '-' }}</template>
          </el-descriptions-item>
          <el-descriptions-item label="年龄">
            <template v-if="editing">
              <el-input-number v-model="form.age" :min="0" :max="150" style="width:100%" />
            </template>
            <template v-else>{{ profile.age || '-' }}</template>
          </el-descriptions-item>
          <el-descriptions-item label="身份证号">
            {{ profile.idCard || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="地址" :span="2">
            <template v-if="editing">
              <el-input v-model="form.address" placeholder="家庭地址" />
            </template>
            <template v-else>{{ profile.address || '-' }}</template>
          </el-descriptions-item>
          <el-descriptions-item label="账户余额">
            <strong style="color:#67c23a">¥{{ profile.balance || '0.00' }}</strong>
          </el-descriptions-item>
        </template>

        <!-- 管理员字段 -->
        <template v-if="profile.role === 'admin'">
          <el-descriptions-item label="所属科室">
            <template v-if="editing">
              <el-input v-model="form.department" placeholder="如：信息科" />
            </template>
            <template v-else>{{ profile.department || '-' }}</template>
          </el-descriptions-item>
          <el-descriptions-item label="岗位">
            <template v-if="editing">
              <el-input v-model="form.position" placeholder="如：系统管理员" />
            </template>
            <template v-else>{{ profile.position || '-' }}</template>
          </el-descriptions-item>
        </template>
      </el-descriptions>

      <div v-if="editing" class="edit-actions">
        <el-button @click="cancelEdit">取消</el-button>
        <el-button type="primary" @click="saveProfile">保存</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Edit } from '@element-plus/icons-vue'
import { api } from '../../utils/api'

const profile = ref({})
const loading = ref(false)
const editing = ref(false)
const form = reactive({})

const roleText = { admin: '管理员', doctor: '医生', patient: '患者' }

const loadProfile = async () => {
  loading.value = true
  try {
    profile.value = await api.auth.getMe()
  } catch (e) {
    ElMessage.error(e?.message || '加载个人信息失败')
  } finally {
    loading.value = false
  }
}

const handleEdit = () => {
  form.realName = profile.value.realName || ''
  form.phone = profile.value.phone || ''
  form.email = profile.value.email || ''
  if (profile.value.role === 'doctor') {
    form.title = profile.value.title || ''
    form.specialty = profile.value.specialty || ''
    form.bio = profile.value.bio || ''
  } else if (profile.value.role === 'patient') {
    form.gender = profile.value.gender || ''
    form.age = profile.value.age || null
    form.address = profile.value.address || ''
  } else if (profile.value.role === 'admin') {
    form.department = profile.value.department || ''
    form.position = profile.value.position || ''
  }
  editing.value = true
}

const cancelEdit = () => {
  editing.value = false
}

const saveProfile = async () => {
  try {
    await api.auth.updateProfile(form)
    ElMessage.success('个人信息已更新')
    editing.value = false
    await loadProfile()
  } catch (e) {
    ElMessage.error(e?.message || '保存失败')
  }
}

onMounted(async () => { await loadProfile() })
</script>

<style scoped>
.profile { padding: 20px 0; max-width: 800px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.page-header h3 { margin: 0; color: #1f2d3d; }
.edit-actions { margin-top: 20px; display: flex; justify-content: flex-end; gap: 12px; }
</style>
