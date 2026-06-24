<template>
  <div class="recharge-page">
    <div class="page-header">
      <h3>账户充值</h3>
    </div>

    <el-row :gutter="20">
      <el-col :span="12">
        <el-card shadow="never">
          <div class="balance-card">
            <div class="balance-label">当前余额</div>
            <div class="balance-num">¥{{ balance }}</div>
            <div class="balance-hint">可用余额</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never">
          <h4 style="margin-top:0">快速充值</h4>
          <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
            <el-form-item label="充值金额" prop="amount">
              <el-input-number v-model="form.amount" :min="1" :precision="2" style="width:100%" placeholder="请输入充值金额" />
            </el-form-item>
            <el-form-item label="支付方式" prop="method">
              <el-radio-group v-model="form.method">
                <el-radio-button value="alipay">支付宝</el-radio-button>
                <el-radio-button value="wechat">微信支付</el-radio-button>
                <el-radio-button value="bank">银行卡</el-radio-button>
              </el-radio-group>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" size="large" style="width:100%" @click="showRechargeDialog">
                立即充值
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" class="records-card">
      <h4>交易记录</h4>
      <el-table :data="records" v-loading="loading" empty-text="暂无交易记录">
        <el-table-column prop="type" label="类型" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.type === '充值' ? 'success' : 'danger'" size="small">
              {{ scope.row.type }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="金额" width="150">
          <template #default="scope">
            <span :style="{ color: scope.row.amount > 0 ? '#67c23a' : '#f56c6c', fontWeight: 'bold' }">
              {{ scope.row.amount > 0 ? '+' : '' }}{{ scope.row.amount }} 元
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="method" label="方式" width="100" />
        <el-table-column label="时间" width="180">
          <template #default="scope">{{ scope.row.time || '-' }}</template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 模拟充值弹窗 -->
    <el-dialog v-model="dialogVisible" title="模拟充值" width="400px" :close-on-click-modal="false">
      <div class="simulate-pay">
        <div class="pay-amount-display">¥{{ form.amount }}</div>
        <div v-if="!paying && !done" class="pay-status">
          <p>支付方式：{{ methodLabel }}</p>
          <el-image src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='200' height='200'%3E%3Crect width='200' height='200' fill='%23f0f0f0'/%3E%3Ctext x='100' y='100' text-anchor='middle' dy='.3em' font-size='14' fill='%23999'%3E扫码支付(模拟)%3C/text%3E%3C/svg%3E"
            style="width:200px;height:200px;border:1px solid #eee" />
        </div>
        <div v-if="paying" class="paying-animation">
          <el-icon class="spin-icon" :size="60"><Loading /></el-icon>
          <p>正在充值中...</p>
        </div>
        <div v-if="done" class="pay-done-animation">
          <el-icon :size="60" color="#67c23a"><CircleCheckFilled /></el-icon>
          <p>充值成功！</p>
        </div>
      </div>
      <template #footer v-if="!done">
        <el-button @click="dialogVisible = false" :disabled="paying">取消</el-button>
        <el-button type="primary" @click="simulateRecharge" :loading="paying">
          {{ paying ? '充值中...' : '确认充值' }}
        </el-button>
      </template>
      <template #footer v-else>
        <el-button type="primary" @click="onRechargeDone">完成</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Loading, CircleCheckFilled } from '@element-plus/icons-vue'
import { api } from '../../utils/api'
import { getSession } from '../../utils/session'

const balance = ref('0.00')
const records = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const paying = ref(false)
const done = ref(false)
const formRef = ref()
const form = reactive({ amount: 100, method: 'alipay' })
const rules = { amount: [{ required: true, message: '请输入充值金额', trigger: 'blur' }] }
const methodLabel = { alipay: '支付宝', wechat: '微信支付', bank: '银行卡' }

const loadData = async () => {
  loading.value = true
  try {
    const session = getSession()
    const balanceData = await api.patient.getBalance(session.userId)
    balance.value = (balanceData?.balance || 0).toFixed(2)
    const recs = await api.patient.getRecords()
    records.value = (recs || []).map(r => ({
      ...r,
      time: r.time ? r.time.substring(0, 19) : '-'
    }))
  } catch (e) {
    // ignore
  } finally {
    loading.value = false
  }
}

const showRechargeDialog = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
    done.value = false
    paying.value = false
    dialogVisible.value = true
  } catch { /* validate error */ }
}

const simulateRecharge = async () => {
  paying.value = true
  await new Promise(resolve => setTimeout(resolve, 1500))
  paying.value = false
  done.value = true
}

const onRechargeDone = async () => {
  dialogVisible.value = false
  try {
    const session = getSession()
    await api.patient.recharge(session.userId, {
      amount: form.amount,
      paymentMethod: form.method
    })
    ElMessage.success('充值成功')
    await loadData()
  } catch (e) {
    ElMessage.error(e?.message || '充值失败')
  }
}

onMounted(async () => { await loadData() })
</script>

<style scoped>
.recharge-page { padding: 20px 0; max-width: 900px; }
.page-header { margin-bottom: 20px; }
.page-header h3 { margin: 0; color: #1f2d3d; }

.balance-card { text-align: center; padding: 20px 0; }
.balance-label { font-size: 14px; color: #909399; }
.balance-num { font-size: 36px; font-weight: 700; color: #67c23a; margin: 10px 0; }
.balance-hint { font-size: 12px; color: #c0c4cc; }
.records-card { margin-top: 20px; }

.simulate-pay { text-align: center; }
.pay-amount-display { font-size: 32px; font-weight: bold; color: #67c23a; margin-bottom: 20px; }
.pay-status p { color: #606266; margin: 10px 0; }
.paying-animation, .pay-done-animation { padding: 40px 0; }
.paying-animation p, .pay-done-animation p { margin-top: 16px; color: #606266; }
.spin-icon { animation: spin 1s linear infinite; }
@keyframes spin { from { transform: rotate(0deg); } to { transform: rotate(360deg); } }
</style>
