<template>
  <div>
    <div class="page-title">收银台</div>

    <div class="cashier-container">
      <div class="cashier-left">
        <el-card shadow="hover" class="search-card" style="margin-bottom:12px">
          <el-input
            v-model="barcode"
            placeholder="扫描条码或输入商品名称搜索，回车添加"
            size="large"
            clearable
            @keyup.enter="searchProduct"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </el-card>

        <el-card shadow="hover" style="flex:1; display:flex; flex-direction:column; border-radius:8px">
          <el-table :data="cartItems" border stripe max-height="400" style="width:100%">
            <el-table-column prop="productName" label="商品" min-width="140" />
            <el-table-column label="单价" width="90" align="right">
              <template #default="{ row }">¥{{ row.price }}</template>
            </el-table-column>
            <el-table-column label="数量" width="150" align="center">
              <template #default="{ row, $index }">
                <el-input-number v-model="row.quantity" :min="1" :max="999" size="small" style="width:120px" />
              </template>
            </el-table-column>
            <el-table-column label="小计" width="100" align="right">
              <template #default="{ row }">¥{{ (row.price * row.quantity).toFixed(2) }}</template>
            </el-table-column>
            <el-table-column label="操作" width="70" align="center">
              <template #default="{ row }">
                <el-button type="danger" link size="small" @click="removeItem(row)">×</el-button>
              </template>
            </el-table-column>
          </el-table>

          <div style="margin-top:12px;flex-shrink:0">
            <div style="font-size:13px;color:#909399;margin-bottom:8px;font-weight:500">快速选择：</div>
            <el-row :gutter="8">
              <el-col :span="6" v-for="p in quickProducts" :key="p.id" style="margin-bottom:8px">
                <el-button
                  @click="addToCart(p)"
                  style="width:100%;height:auto;padding:8px;white-space:normal;line-height:1.4"
                  :disabled="p.stock <= 0"
                >
                  <strong>{{ p.name }}</strong><br/>
                  <small style="color:#e4393c">¥{{ p.price }}</small>
                  <small v-if="p.stock > 0" style="color:#999"> / 库存:{{ p.stock }}</small>
                  <small v-else style="color:#e4393c"> / 缺货</small>
                </el-button>
              </el-col>
            </el-row>
          </div>
        </el-card>
      </div>

      <div class="cashier-right">
        <el-card shadow="hover" style="border-radius:8px">
          <template #header>
            <strong style="font-size:15px">结算收银</strong>
          </template>

          <el-form label-width="70px">
            <el-form-item label="会员">
              <div style="display:flex;gap:4px;width:100%">
                <el-input v-model="memberPhone" placeholder="输入手机号" @keyup.enter="searchMember" />
                <el-button @click="searchMember" type="primary" :icon="Search" />
              </div>
            </el-form-item>
            <div v-if="currentMember" class="member-info">
              <el-tag type="success" effect="plain">
                {{ currentMember.name }} | 积分 {{ currentMember.points }}
              </el-tag>
            </div>

            <el-form-item label="支付方式">
              <el-radio-group v-model="payType">
                <el-radio-button value="CASH">现金</el-radio-button>
                <el-radio-button value="ALIPAY">支付宝</el-radio-button>
                <el-radio-button value="WECHAT">微信</el-radio-button>
              </el-radio-group>
            </el-form-item>

            <el-divider />

            <div class="checkout-summary">
              <div class="summary-row">
                <span>商品数量</span>
                <strong>{{ cartCount }}</strong>
              </div>
              <div class="summary-row">
                <span>商品成本</span>
                <span style="color:#909399">¥{{ totalCost.toFixed(2) }}</span>
              </div>
              <div class="summary-row">
                <span>预计盈利</span>
                <span :style="{ color: profit >= 0 ? '#67c23a' : '#f56c6c', fontWeight: 600 }">
                  ¥{{ profit.toFixed(2) }}
                </span>
              </div>
              <div class="summary-row total">
                <span>合计金额</span>
                <span class="total-amount">¥{{ totalAmount.toFixed(2) }}</span>
              </div>
            </div>

            <el-button
              type="danger"
              size="large"
              style="width:100%;margin-top:12px;height:48px;font-size:16px;font-weight:600;border-radius:8px"
              :disabled="cartItems.length === 0"
              @click="handleCheckout"
            >
              结算收款 ¥{{ totalAmount.toFixed(2) }}
            </el-button>
          </el-form>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { getByBarcode, getProductList, getByPhone, createOrder } from '@/api'

const barcode = ref('')
const cartItems = ref<Array<{ productId: number; productName: string; price: number; costPrice: number; quantity: number }>>([])
const quickProducts = ref<any[]>([])
const memberPhone = ref('')
const currentMember = ref<any>(null)
const payType = ref('CASH')

const cartCount = computed(() => cartItems.value.reduce((s, i) => s + i.quantity, 0))
const totalAmount = computed(() => cartItems.value.reduce((s, i) => s + i.price * i.quantity, 0))
const totalCost = computed(() => cartItems.value.reduce((s, i) => s + (i.costPrice || 0) * i.quantity, 0))
const profit = computed(() => totalAmount.value - totalCost.value)

const searchProduct = async () => {
  if (!barcode.value) return
  let res = await getByBarcode(barcode.value).catch(() => null)
  if (res && res.code === 200 && res.data) {
    addToCart(res.data)
    barcode.value = ''
    return
  }
  const listRes = await getProductList({ name: barcode.value, page: 1, pageSize: 10 })
  if (listRes.code === 200 && listRes.data.list.length > 0) {
    if (listRes.data.list.length === 1) {
      addToCart(listRes.data.list[0])
    } else {
      quickProducts.value = listRes.data.list
    }
  } else {
    ElMessage.warning('未找到商品')
  }
  barcode.value = ''
}

const addToCart = (product: any) => {
  const exist = cartItems.value.find(i => i.productId === product.id)
  if (exist) {
    exist.quantity++
  } else {
    cartItems.value.push({ productId: product.id, productName: product.name, price: product.price, costPrice: product.costPrice || 0, quantity: 1 })
  }
}

const removeItem = (row: any) => {
  cartItems.value = cartItems.value.filter(i => i.productId !== row.productId)
}

const searchMember = async () => {
  if (!memberPhone.value) { currentMember.value = null; return }
  const res = await getByPhone(memberPhone.value)
  if (res.code === 200 && res.data) {
    currentMember.value = res.data
  } else {
    currentMember.value = null
    ElMessage.warning('未找到该会员')
  }
}

const handleCheckout = async () => {
  const orderData = {
    items: cartItems.value.map(i => ({ productId: i.productId, productName: i.productName, quantity: i.quantity, price: i.price })),
    memberId: currentMember.value?.id || undefined,
    payType: payType.value
  }
  const res = await createOrder(orderData)
  if (res.code === 200) {
    const msg = currentMember.value
      ? `订单创建成功！金额 ¥${totalAmount.value.toFixed(2)}，积分 +${Math.floor(totalAmount.value / 10)}`
      : `订单创建成功！金额 ¥${totalAmount.value.toFixed(2)}`
    ElMessage.success({ message: msg, duration: 4000 })
    // 刷新会员积分
    if (currentMember.value && currentMember.value.id) {
      currentMember.value.points += Math.floor(totalAmount.value / 10)
    }
    setTimeout(() => {
      cartItems.value = []
      currentMember.value = null
      memberPhone.value = ''
      payType.value = 'CASH'
    }, 500)
  } else {
    ElMessage.error(res.msg || '下单失败')
  }
}

onMounted(async () => {
  const res = await getProductList({ page: 1, pageSize: 20 })
  if (res.code === 200) quickProducts.value = res.data.list
})
</script>

<style scoped>
.cashier-container {
  display: flex;
  gap: 16px;
  height: calc(100vh - 160px);
}

.cashier-left {
  flex: 7;
  display: flex;
  flex-direction: column;
}

.cashier-right {
  flex: 3;
  min-width: 320px;
}

.member-info {
  padding: 0 0 12px 0;
}

.checkout-summary {
  padding: 0 4px;
}

.summary-row {
  display: flex;
  justify-content: space-between;
  padding: 6px 0;
  font-size: 14px;
  color: #666;
}

.summary-row.total {
  border-top: 1px dashed #e8e8e8;
  margin-top: 4px;
  padding-top: 12px;
}

.total-amount {
  font-size: 22px;
  font-weight: 700;
  color: #e4393c;
}
</style>