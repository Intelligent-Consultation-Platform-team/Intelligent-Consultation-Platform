<template>
  <div class="visit">
    <div class="page-header">
      <h3>患者就诊管理</h3>
      <el-button type="primary" @click="refreshData">
        <el-icon><Refresh /></el-icon>
        刷新
      </el-button>
    </div>

    <el-card shadow="never" class="table-card">
      <el-table v-loading="loading" :data="visits" style="width: 100%">
        <el-table-column prop="consultationId" label="就诊ID" width="100" />
        <el-table-column prop="patientName" label="患者姓名" />
        <el-table-column prop="symptoms" label="症状描述" show-overflow-tooltip />
        <el-table-column prop="diagnosis" label="诊断结果" show-overflow-tooltip>
          <template #default="scope">
            {{ scope.row.diagnosis || '待诊断' }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusTagType(scope.row.status)">
              {{ getStatusLabel(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="scope">
            <template v-if="scope.row.status !== 'completed' && scope.row.status !== 'cancelled' && scope.row.status !== 'unpaid'">
              <el-button type="primary" size="small" @click="handleDiagnose(scope.row)">
                诊断
              </el-button>
              <el-button
                type="success"
                size="small"
                @click="handleComplete(scope.row)"
                :disabled="!scope.row.diagnosis"
              >
                完成
              </el-button>
            </template>
            <template v-if="scope.row.diagnosis && scope.row.status !== 'completed' && scope.row.status !== 'cancelled' && scope.row.status !== 'unpaid'">
              <el-tag v-if="scope.row.admitted" type="danger" size="small">已住院</el-tag>
              <el-button
                v-else
                type="warning"
                size="small"
                @click="handleHospitalize(scope.row)"
              >
                建议住院
              </el-button>
            </template>
            <span v-if="scope.row.status === 'unpaid'">待患者支付</span>
            <span v-if="scope.row.status === 'completed' || scope.row.status === 'cancelled'">已结束</span>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="diagnoseDialogVisible" title="诊断记录" width="600px">
      <div v-if="selectedVisit">
        <h4>患者信息</h4>
        <p><strong>姓名：</strong>{{ selectedVisit.patientName }}</p>
        <p><strong>症状：</strong>{{ selectedVisit.symptoms }}</p>
        <el-divider />
        <h4>诊断信息</h4>
        <el-form ref="diagnoseFormRef" :model="diagnoseForm" :rules="diagnoseRules" label-width="100px">
          <el-form-item label="诊断结果" prop="diagnosis">
            <el-input v-model="diagnoseForm.diagnosis" type="textarea" :rows="3" placeholder="请输入诊断结果" />
          </el-form-item>
          <el-form-item label="治疗方案" prop="treatment">
            <el-input v-model="diagnoseForm.treatment" type="textarea" :rows="3" placeholder="请输入治疗方案" />
          </el-form-item>
          <el-form-item label="处方药物" prop="prescription">
            <el-input v-model="diagnoseForm.prescription" type="textarea" :rows="3" placeholder="请输入处方药物" />
          </el-form-item>
          <el-form-item label="就诊费用" prop="amount">
            <el-input-number v-model="diagnoseForm.amount" :min="0" :precision="2" style="width:100%" placeholder="请输入就诊费用（元）" />
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="diagnoseDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmDiagnosis">保存诊断</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import { api } from '../../utils/api'

const router = useRouter()

const visits = ref([])
const loading = ref(false)
const diagnoseDialogVisible = ref(false)
const selectedVisit = ref(null)
const diagnoseFormRef = ref()
const diagnoseForm = reactive({ diagnosis: '', treatment: '', prescription: '', amount: 0 })

const diagnoseRules = {
  diagnosis: [{ required: true, message: '请输入诊断结果', trigger: 'blur' }],
  treatment: [{ required: true, message: '请输入治疗方案', trigger: 'blur' }]
}

const formatDateTime = (value) => {
  if (!value) return { date: '', time: '' }
  const str = String(value)
  const [date = '', rest = ''] = str.split(' ')
  return { date, time: rest ? rest.substring(0, 5) : '' }
}

const loadData = async () => {
  loading.value = true
  try {
    const data = await api.consultation.getDoctorList()
    const mapped = (data || []).map(item => {
      const { date, time } = formatDateTime(item.consultationDate)
      return {
        consultationId: item.consultationId,
        appointmentId: item.appointmentId,
        patientId: item.patientId,
        doctorId: item.doctorId,
        deptId: item.deptId,
        patientName: item.patientName || '患者' + item.patientId,
        symptoms: item.symptoms || '-',
        diagnosis: item.diagnosis || '',
        treatment: item.treatment || '',
        prescription: item.prescription || '',
        amount: item.amount || 0,
        visitDate: date,
        visitTime: time,
        status: item.status || 'processing',
        admitted: false
      }
    })

    // 批量检查每个患者是否正在住院
    const patientIds = [...new Set(mapped.map(v => v.patientId).filter(Boolean))]
    if (patientIds.length > 0) {
      const results = await Promise.allSettled(
        patientIds.map(pid => api.hospitalization.checkAdmitted(pid))
      )
      const admittedSet = new Set()
      results.forEach((r, i) => {
        if (r.status === 'fulfilled' && r.value?.admitted) {
          admittedSet.add(patientIds[i])
        }
      })
      mapped.forEach(v => {
        if (admittedSet.has(v.patientId)) v.admitted = true
      })
    }

    visits.value = mapped
  } catch (error) {
    ElMessage.error(error.message || '加载就诊记录失败')
  } finally {
    loading.value = false
  }
}

const getStatusLabel = (status) => ({
  pending: '待就诊',
  confirmed: '已签到',
  processing: '就诊中',
  unpaid: '待支付',
  completed: '已完成',
  cancelled: '已取消'
}[status] || '未知')

const getStatusTagType = (status) => ({
  pending: 'warning',
  confirmed: '',
  processing: 'primary',
  unpaid: 'danger',
  completed: 'success',
  cancelled: 'info'
}[status] || 'default')

const refreshData = () => loadData()

const handleDiagnose = (row) => {
  selectedVisit.value = row
  diagnoseForm.diagnosis = row.diagnosis || ''
  diagnoseForm.treatment = row.treatment || ''
  diagnoseForm.prescription = row.prescription || ''
  diagnoseForm.amount = row.amount || 0
  diagnoseDialogVisible.value = true
}

const confirmDiagnosis = async () => {
  if (!diagnoseFormRef.value) return
  try {
    await diagnoseFormRef.value.validate()
    await api.consultation.update(selectedVisit.value.consultationId, {
      diagnosis: diagnoseForm.diagnosis,
      treatment: diagnoseForm.treatment,
      prescription: diagnoseForm.prescription,
      amount: diagnoseForm.amount
    })
    selectedVisit.value.amount = diagnoseForm.amount
    ElMessage.success('诊断已保存')
    diagnoseDialogVisible.value = false
    await loadData()
  } catch (e) {
    if (e?.message) ElMessage.warning(e.message)
  }
}

const handleComplete = async (row) => {
  const fee = row.amount || 0
  try {
    await ElMessageBox.confirm(`确认完成就诊？费用：¥${fee}`, '提示', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await api.consultation.complete(row.consultationId, { amount: fee })
    ElMessage.success('就诊已完成')
    await loadData()
  } catch (e) {
    if (e !== 'cancel' && e?.message) {
      ElMessage.error(e.message)
    }
  }
}

const handleHospitalize = (row) => {
  router.push({
    path: '/appoint/hospital',
    query: {
      consultationId: row.consultationId,
      patientId: row.patientId,
      patientName: row.patientName,
      doctorId: row.doctorId,
      deptId: row.deptId,
      diagnosis: row.diagnosis
    }
  })
}

onMounted(async () => { await loadData() })
</script>

<style scoped>
.visit { padding: 20px 0; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.page-header h3 { margin: 0; color: #1f2d3d; }
.table-card { margin-top: 20px; }
.dialog-footer { display: flex; justify-content: flex-end; }
</style>
