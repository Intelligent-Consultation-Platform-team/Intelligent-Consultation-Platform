<template>
  <div class="patient-account">
    <div class="page-header">
      <h3>患者账户管理</h3>
    </div>

    <el-card shadow="never" class="balance-card">
      <div class="balance-header">
        <span>账户余额</span>
        <div>
          <el-button type="primary" @click="handleRecharge">
            <el-icon><Plus /></el-icon>
            充值
          </el-button>
          <el-button type="danger" @click="handlePayment" style="margin-left: 10px;">
            缴费
          </el-button>
        </div>
      </div>
      <div class="balance-amount">
        ¥{{ balance }}
      </div>
    </el-card>

    <el-card shadow="never" class="transaction-card">
      <template #header>
        <div class="card-header">
          <span>交易记录</span>
        </div>
      </template>
      <el-table v-loading="loading" :data="transactions" style="width: 100%">
        <el-table-column prop="id" label="交易ID" width="120" />
        <el-table-column prop="type" label="类型" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.type === 'recharge' ? 'success' : 'danger'">
              {{ scope.row.type === 'recharge' ? '充值' : '支付' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="amount" label="金额" width="120">
          <template #default="scope">
            <span :class="scope.row.type === 'recharge' ? 'text-success' : 'text-danger'">
              {{ scope.row.type === 'recharge' ? '+' : '-' }}¥{{ scope.row.amount }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" />
        <el-table-column prop="createdAt" label="交易时间" width="180" />
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
      v-model="rechargeDialogVisible"
      title="账户充值"
      width="400px"
    >
      <el-form
        ref="rechargeFormRef"
        :model="rechargeForm"
        :rules="rechargeRules"
        label-width="80px"
      >
        <el-form-item label="充值金额" prop="amount">
          <el-input v-model.number="rechargeForm.amount" placeholder="请输入充值金额" />
        </el-form-item>
        <el-form-item label="支付方式" prop="paymentMethod">
          <el-select v-model="rechargeForm.paymentMethod" placeholder="选择支付方式" style="width: 100%">
            <el-option label="支付宝" value="alipay" />
            <el-option label="微信支付" value="wechat" />
            <el-option label="银行卡" value="bank" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="rechargeDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleRechargeSubmit">确定充值</el-button>
        </span>
      </template>
    </el-dialog>

    <el-dialog
      v-model="paymentDialogVisible"
      title="账户缴费"
      width="400px"
    >
      <el-form
        ref="paymentFormRef"
        :model="paymentForm"
        :rules="paymentRules"
        label-width="80px"
      >
        <el-form-item label="缴费金额" prop="amount">
          <el-input v-model.number="paymentForm.amount" placeholder="请输入缴费金额" />
        </el-form-item>
        <el-form-item label="就诊ID" prop="consultationId">
          <el-input v-model.number="paymentForm.consultationId" placeholder="请输入就诊ID" />
        </el-form-item>
        <el-form-item label="支付方式" prop="paymentMethod">
          <el-select v-model="paymentForm.paymentMethod" placeholder="选择支付方式" style="width: 100%">
            <el-option label="支付宝" value="alipay" />
            <el-option label="微信支付" value="wechat" />
            <el-option label="银行卡" value="bank" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="paymentDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handlePaymentSubmit">确定缴费</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { api } from '../../utils/api'
import { getSession } from '../../utils/session'

const session = getSession()
const patientId = session?.userId || null

const balance = ref(0)
const transactions = ref([])
const loading = ref(false)
const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const rechargeDialogVisible = ref(false)
const rechargeFormRef = ref()
const rechargeForm = reactive({
  amount: '',
  paymentMethod: 'alipay'
})

const rechargeRules = {
  amount: [
    { required: true, message: '请输入充值金额', trigger: 'blur' },
    { type: 'number', min: 1, message: '充值金额至少为1元', trigger: 'blur' }
  ],
  paymentMethod: [
    { required: true, message: '请选择支付方式', trigger: 'change' }
  ]
}

const paymentDialogVisible = ref(false)
const paymentFormRef = ref()
const paymentForm = reactive({
  amount: '',
  consultationId: '',
  paymentMethod: 'alipay'
})

const paymentRules = {
  amount: [
    { required: true, message: '请输入缴费金额', trigger: 'blur' },
    { type: 'number', min: 1, message: '缴费金额至少为1元', trigger: 'blur' }
  ],
  paymentMethod: [
    { required: true, message: '请选择支付方式', trigger: 'change' }
  ]
}

// 获取余额
const loadBalance = async () => {
  try {
    const data = await api.patient.getBalance(patientId)
    if (data !== undefined && data !== null) {
      balance.value = parseFloat(data.balance ?? data) || 0
    }
  } catch (error) {
    console.error('获取余额失败', error)
    balance.value = 0
  }
}

// 加载交易记录（充值和缴费）
const loadData = async () => {
  loading.value = true
  try {
    transactions.value = []
    pagination.total = 0
  } catch (error) {
    console.error('加载交易记录失败', error)
    transactions.value = []
  } finally {
    loading.value = false
  }
}

const handleRecharge = () => {
  rechargeForm.amount = ''
  rechargeForm.paymentMethod = 'alipay'
  rechargeDialogVisible.value = true
}

const handleRechargeSubmit = async () => {
  if (!rechargeFormRef.value) return
  try {
    await rechargeFormRef.value.validate()
    if (!patientId) {
      ElMessage.error('请先登录')
      return
    }
    const result = await api.patient.recharge(patientId, {
      amount: rechargeForm.amount,
      paymentMethod: rechargeForm.paymentMethod
    })
    ElMessage.success('充值成功')
    if (result?.balance !== undefined) {
      balance.value = parseFloat(result.balance) || balance.value
    }
    rechargeDialogVisible.value = false
    loadData()
  } catch (error) {
    ElMessage.error(error?.message || '充值失败')
  }
}

const handlePayment = () => {
  paymentForm.amount = ''
  paymentForm.consultationId = ''
  paymentForm.paymentMethod = 'alipay'
  paymentDialogVisible.value = true
}

const handlePaymentSubmit = async () => {
  if (!paymentFormRef.value) return
  try {
    await paymentFormRef.value.validate()
    if (paymentForm.amount > balance.value) {
      ElMessage.error('账户余额不足')
      return
    }
    if (!patientId) {
      ElMessage.error('请先登录')
      return
    }
    const result = await api.patient.payment(patientId, {
      consultationId: paymentForm.consultationId || null,
      amount: paymentForm.amount,
      paymentMethod: paymentForm.paymentMethod
    })
    ElMessage.success('缴费成功')
    if (result?.balance !== undefined) {
      balance.value = parseFloat(result.balance) || balance.value
    }
    paymentDialogVisible.value = false
    loadData()
  } catch (error) {
    ElMessage.error(error?.message || '缴费失败')
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
  await loadBalance()
  await loadData()
})
</script>

<style scoped>
.patient-account {
  padding: 20px 0;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h3 {
  margin: 0;
  color: #1f2d3d;
}

.balance-card {
  margin-bottom: 20px;
}

.balance-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.balance-amount {
  font-size: 32px;
  font-weight: 600;
  color: #1f2d3d;
  text-align: center;
  padding: 20px 0;
}

.transaction-card {
  margin-top: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
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

.text-success {
  color: #67c23a;
}

.text-danger {
  color: #f56c6c;
}
</style>