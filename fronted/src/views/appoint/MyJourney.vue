<template>
  <div class="my-journey">
    <div class="page-header">
      <h3>我的流程</h3>
      <el-button type="primary" @click="loadData">
        <el-icon><Refresh /></el-icon>
        刷新
      </el-button>
    </div>

    <!-- 步骤概览卡片 -->
    <el-row :gutter="16" class="step-cards">
      <el-col :span="6" v-for="step in steps" :key="step.key">
        <el-card
          shadow="hover"
          :class="['step-card', { active: activeStep === step.key }]"
          @click="activeStep = step.key"
        >
          <div class="step-num">{{ step.count }}</div>
          <div class="step-label">{{ step.label }}</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 数据列表 -->
    <el-card shadow="never" class="table-card">
      <el-table v-loading="loading" :data="filteredItems" style="width: 100%" empty-text="暂无记录">
        <el-table-column prop="appointmentId" label="挂号ID" width="90" />
        <el-table-column label="科室" width="120">
          <template #default="scope">{{ scope.row.deptName || '-' }}</template>
        </el-table-column>
        <el-table-column label="医生" width="100">
          <template #default="scope">
            {{ scope.row.doctorName || '-' }}
            <span v-if="scope.row.doctorTitle" class="doctor-title">({{ scope.row.doctorTitle }})</span>
          </template>
        </el-table-column>
        <el-table-column prop="appointmentDate" label="日期" width="110" />
        <el-table-column prop="appointmentTime" label="时间" width="80" />
        <el-table-column prop="symptoms" label="症状" show-overflow-tooltip width="150" />
        <el-table-column label="诊断结果" show-overflow-tooltip width="150">
          <template #default="scope">
            {{ scope.row.diagnosis || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusTagType(getDisplayStatus(scope.row))">
              {{ getStatusLabel(getDisplayStatus(scope.row)) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="scope">
            <template v-if="scope.row.status === 'pending' || scope.row.status === 'confirmed'">
              <el-button type="danger" size="small" @click="handleCancel(scope.row.appointmentId)">
                取消
              </el-button>
            </template>
            <template v-else-if="scope.row.paymentStatus === 'paid'">
              <el-tag type="success" size="small">已完成</el-tag>
            </template>
            <template v-else-if="scope.row.status === 'unpaid' || (scope.row.status === 'processing' && scope.row.paymentStatus === 'unpaid')">
              <el-button type="warning" size="small" @click="handlePay(scope.row)">
                立即缴费
              </el-button>
            </template>
            <template v-else-if="scope.row.status === 'processing' && scope.row.paymentStatus !== 'unpaid'">
              <el-button type="primary" size="small" plain disabled>
                就诊中
              </el-button>
            </template>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 缴费对话框 -->
    <el-dialog
      v-model="payDialogVisible"
      title="缴纳就诊费用"
      width="520px"
      :close-on-click-modal="false"
      @closed="onPayDialogClosed"
    >
      <div v-if="payLoading" class="pay-dialog-loading">
        <el-icon class="spin-icon" :size="40"><Loading /></el-icon>
        <p>加载缴费信息...</p>
      </div>

      <div v-else-if="payPaying" class="pay-dialog-processing">
        <el-icon class="spin-icon" :size="40"><Loading /></el-icon>
        <p>正在处理支付...</p>
      </div>

      <div v-else-if="payDone" class="pay-dialog-success">
        <el-result icon="success" title="支付成功" sub-title="就诊费用已缴纳完毕">
          <template #extra>
            <el-button type="primary" @click="onPayDone">完成</el-button>
          </template>
        </el-result>
      </div>

      <div v-else-if="payRechargeMode" class="pay-dialog-recharge">
        <div class="recharge-info">
          <p>当前余额：<strong class="balance-display">¥{{ currentBalance.toFixed(2) }}</strong></p>
          <p>需缴费：<strong class="amount-display">¥{{ payingAmount.toFixed(2) }}</strong></p>
          <p>还需充值：<strong class="need-amount">¥{{ Math.max(0, payingAmount - currentBalance).toFixed(2) }}</strong></p>
        </div>
        <el-form ref="rechargeFormRef" :model="rechargeForm" :rules="rechargeRules" label-width="80px">
          <el-form-item label="充值金额" prop="amount">
            <el-input-number v-model="rechargeForm.amount" :min="1" :precision="2" style="width: 100%" />
          </el-form-item>
          <el-form-item label="支付方式" prop="method">
            <el-radio-group v-model="rechargeForm.method">
              <el-radio-button value="alipay">支付宝</el-radio-button>
              <el-radio-button value="wechat">微信支付</el-radio-button>
              <el-radio-button value="bank">银行卡</el-radio-button>
            </el-radio-group>
          </el-form-item>
        </el-form>
      </div>

      <div v-else>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="就诊ID">{{ payInfo.consultationId }}</el-descriptions-item>
          <el-descriptions-item label="挂号ID">{{ payInfo.appointmentId }}</el-descriptions-item>
          <el-descriptions-item label="科室">{{ payInfo.deptName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="医生">{{ payInfo.doctorName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="诊断结果" :span="2">{{ payInfo.diagnosis || '-' }}</el-descriptions-item>
          <el-descriptions-item label="就诊费用">
            <strong style="color: #f56c6c; font-size: 20px">¥{{ payingAmount.toFixed(2) }}</strong>
          </el-descriptions-item>
          <el-descriptions-item label="支付状态">
            <el-tag type="danger">待缴费</el-tag>
          </el-descriptions-item>
        </el-descriptions>

        <el-divider />
        <h4 style="text-align: center">选择支付方式</h4>
        <el-radio-group v-model="payMethod" class="pay-radio-group">
          <el-radio-button value="alipay">
            <span class="pay-icon-circle">支</span> 支付宝
          </el-radio-button>
          <el-radio-button value="wechat">
            <span class="pay-icon-circle wechat">微</span> 微信支付
          </el-radio-button>
          <el-radio-button value="account">
            <span class="pay-icon-circle account">余</span> 余额支付
          </el-radio-button>
        </el-radio-group>

        <div class="pay-action">
          <el-button type="primary" size="large" class="pay-btn" @click="handleConfirmPay">
            确认支付 ¥{{ payingAmount.toFixed(2) }}
          </el-button>
          <p v-if="payMethod === 'account'" class="recharge-hint">
            余额不足？可以直接切换支付方式，或
            <el-link type="primary" @click="enterRechargeMode">在此充值</el-link>
          </p>
        </div>
      </div>

      <template #footer>
        <template v-if="payRechargeMode">
          <el-button @click="payRechargeMode = false">返回</el-button>
          <el-button type="primary" @click="handleRechargeAndPay" :loading="rechargePaying">
            {{ rechargePaying ? '充值中...' : '确认充值并缴费' }}
          </el-button>
        </template>
        <template v-else-if="!payLoading && !payPaying && !payDone">
          <el-button @click="payDialogVisible = false">取消</el-button>
        </template>
      </template>
    </el-dialog>

  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh, Loading } from '@element-plus/icons-vue'
import { api } from '../../utils/api'
import { getSession } from '../../utils/session'

const router = useRouter()
const items = ref([])
const loading = ref(false)
const activeStep = ref('all')

// 缴费对话框状态
const payDialogVisible = ref(false)
const payLoading = ref(false)
const payPaying = ref(false)
const payDone = ref(false)
const payRechargeMode = ref(false)
const rechargePaying = ref(false)
const payMethod = ref('account')
const payInfo = ref({})
const payingAmount = ref(0)
const currentBalance = ref(0)
const rechargeFormRef = ref()
const rechargeForm = reactive({ amount: 0, method: 'alipay' })
const rechargeRules = {
  amount: [{ required: true, message: '请输入充值金额', trigger: 'blur' }]
}
const payMethodLabel = computed(() => ({
  alipay: '支付宝', wechat: '微信支付', account: '余额支付'
}[payMethod.value] || '余额支付'))

const steps = computed(() => {
  const counts = { pending: 0, processing: 0, unpaid: 0, completed: 0 }
  items.value.forEach(item => {
    if (item.status === 'pending' || item.status === 'confirmed') counts.pending++
    else if (item.status === 'unpaid' || (item.status === 'processing' && item.paymentStatus === 'unpaid')) counts.unpaid++
    else if (item.paymentStatus === 'paid' || item.status === 'completed') counts.completed++
    else if (item.status === 'processing') counts.processing++
  })
  return [
    { key: 'all', label: '全部', count: items.value.length },
    { key: 'pending', label: '预约中', count: counts.pending },
    { key: 'processing', label: '就诊中', count: counts.processing },
    { key: 'unpaid', label: '待缴费', count: counts.unpaid },
    { key: 'completed', label: '已完成', count: counts.completed }
  ]
})

const filteredItems = computed(() => {
  if (activeStep.value === 'all') return items.value
  if (activeStep.value === 'pending') return items.value.filter(i => i.status === 'pending' || i.status === 'confirmed')
  if (activeStep.value === 'processing') return items.value.filter(i => i.status === 'processing' && i.paymentStatus !== 'unpaid' && i.paymentStatus !== 'paid')
  if (activeStep.value === 'unpaid') return items.value.filter(i => i.status === 'unpaid' || (i.status === 'processing' && i.paymentStatus === 'unpaid'))
  if (activeStep.value === 'completed') return items.value.filter(i => i.paymentStatus === 'paid' || i.status === 'completed')
  return items.value
})

const getDisplayStatus = (row) => {
  if (row.paymentStatus === 'paid') return 'completed'
  if (row.status === 'unpaid' || (row.status === 'processing' && row.paymentStatus === 'unpaid')) return 'unpaid'
  return row.status
}

const getStatusLabel = (status) => ({
  pending: '待就诊',
  confirmed: '已签到',
  processing: '就诊中',
  unpaid: '待缴费',
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

const loadData = async () => {
  loading.value = true
  try {
    const session = getSession()
    if (!session?.userId) {
      items.value = []
      return
    }
    const data = await api.patient.getJourney()
    items.value = (data?.items || []).map(item => ({
      ...item,
      appointmentDate: item.appointmentDate || '',
      appointmentTime: item.appointmentTime || ''
    }))
  } catch (e) {
    ElMessage.error(e?.message || '加载失败')
  } finally {
    loading.value = false
  }
}

const handleCancel = async (id) => {
  try {
    await ElMessageBox.confirm('确认取消该预约？', '提示', {
      confirmButtonText: '确认取消',
      cancelButtonText: '返回',
      type: 'warning'
    })
    await api.appointment.cancel(id)
    ElMessage.success('已取消')
    await loadData()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error(e?.message || '取消失败')
  }
}

const handlePay = async (row) => {
  payDialogVisible.value = true
  payLoading.value = true
  payDone.value = false
  payPaying.value = false
  payRechargeMode.value = false

  try {
    const data = await api.consultation.getInfo(row.consultationId)
    payInfo.value = data
    payingAmount.value = parseFloat(data.amount || 0)
    payMethod.value = 'account'

    const session = getSession()
    if (session?.userId) {
      try {
        const balanceData = await api.patient.getBalance(session.userId)
        currentBalance.value = parseFloat(balanceData?.balance ?? balanceData ?? 0)
      } catch { currentBalance.value = 0 }
    }
  } catch (e) {
    ElMessage.error(e?.message || '加载缴费信息失败')
    payDialogVisible.value = false
  } finally {
    payLoading.value = false
  }
}

const handleConfirmPay = async () => {
  if (payMethod.value === 'account' && currentBalance.value < payingAmount.value) {
    ElMessage.warning(`余额不足（当前：¥${currentBalance.value.toFixed(2)}，需缴：¥${payingAmount.value.toFixed(2)}）`)
    return
  }

  try {
    await ElMessageBox.confirm(
      `确认使用「${payMethodLabel.value}」支付 ¥${payingAmount.value.toFixed(2)}？`,
      '确认支付',
      { confirmButtonText: '确认', cancelButtonText: '取消', type: 'warning' }
    )
  } catch {
    return
  }

  payPaying.value = true
  try {
    if (payMethod.value !== 'account') {
      await new Promise(resolve => setTimeout(resolve, 1500))
    }

    const session = getSession()
    await api.patient.payment(session.userId, {
      consultationId: payInfo.value.consultationId,
      amount: payingAmount.value,
      paymentMethod: payMethod.value
    })

    payPaying.value = false
    payDone.value = true
  } catch (e) {
    ElMessage.error(e?.message || '缴费失败')
    payPaying.value = false
  }
}

const enterRechargeMode = () => {
  const shortfall = payingAmount.value - currentBalance.value
  rechargeForm.amount = Math.max(1, Math.ceil(shortfall))
  payRechargeMode.value = true
}

const handleRechargeAndPay = async () => {
  if (!rechargeFormRef.value) return
  try {
    await rechargeFormRef.value.validate()
  } catch {
    return
  }

  rechargePaying.value = true
  try {
    await new Promise(resolve => setTimeout(resolve, 1500))

    const session = getSession()
    await api.patient.recharge(session.userId, {
      amount: rechargeForm.amount,
      paymentMethod: rechargeForm.method
    })

    try {
      const balanceData = await api.patient.getBalance(session.userId)
      currentBalance.value = parseFloat(balanceData?.balance ?? balanceData ?? 0)
    } catch { }

    ElMessage.success(`充值成功，当前余额：¥${currentBalance.value.toFixed(2)}`)
    payRechargeMode.value = false

    if (currentBalance.value >= payingAmount.value) {
      ElMessage.success('余额充足，可以支付了')
    }
  } catch (e) {
    ElMessage.error(e?.message || '充值失败')
  } finally {
    rechargePaying.value = false
  }
}

const onPayDone = async () => {
  payDialogVisible.value = false
  ElMessage.success('缴费成功，就诊已完成')
  await loadData()
}

const onPayDialogClosed = () => {
  payLoading.value = false
  payPaying.value = false
  payDone.value = false
  payRechargeMode.value = false
  rechargePaying.value = false
}

onMounted(async () => { await loadData() })
</script>

<style scoped>
.my-journey { padding: 20px 0; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.page-header h3 { margin: 0; color: #1f2d3d; }

.step-cards { margin-bottom: 20px; }
.step-card { text-align: center; cursor: pointer; transition: 0.2s; }
.step-card.active { border-color: #409eff; background: #ecf5ff; }
.step-card:hover { border-color: #409eff; }
.step-num { font-size: 28px; font-weight: 700; color: #409eff; }
.step-label { margin-top: 4px; font-size: 14px; color: #606266; }

.table-card { margin-top: 0; }
.doctor-title { font-size: 12px; color: #909399; }

/* 缴费对话框 */
.pay-dialog-loading,
.pay-dialog-processing { text-align: center; padding: 40px 0; }
.pay-dialog-loading p,
.pay-dialog-processing p { margin-top: 16px; color: #909399; }
.pay-dialog-success { padding: 20px 0; }
.pay-radio-group { display: flex; justify-content: center; gap: 12px; margin: 12px 0 24px; }
.pay-icon-circle { display: inline-block; width: 20px; height: 20px; line-height: 20px;
  border-radius: 50%; background: #1677ff; color: #fff;
  font-size: 11px; font-weight: bold; margin-right: 4px; }
.pay-icon-circle.wechat { background: #07c160; }
.pay-icon-circle.account { background: #e6a23c; }
.pay-action { text-align: center; }
.pay-btn { width: 280px; font-size: 16px; }
.recharge-hint { margin-top: 12px; font-size: 13px; color: #909399; }

.pay-dialog-recharge .recharge-info { text-align: center; margin-bottom: 20px; }
.pay-dialog-recharge .recharge-info p { margin: 6px 0; color: #606266; }
.pay-dialog-recharge .balance-display { color: #67c23a; }
.pay-dialog-recharge .amount-display { color: #f56c6c; }
.pay-dialog-recharge .need-amount { color: #e6a23c; font-size: 18px; }

.spin-icon { animation: spin 1s linear infinite; }
@keyframes spin { from { transform: rotate(0deg); } to { transform: rotate(360deg); } }
</style>
