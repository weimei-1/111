<template>
  <div>
    <div class="page-title">订单记录</div>

    <el-card shadow="hover" class="search-card">
      <el-form :inline="true" :model="query" size="default">
        <el-form-item label="日期">
          <el-date-picker v-model="dateRange" type="daterange" value-format="YYYY-MM-DD" range-separator="至" start-placeholder="开始" end-placeholder="结束" style="width:250px" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部" clearable style="width:120px">
            <el-option label="待支付" value="PENDING" />
            <el-option label="已支付" value="PAID" />
            <el-option label="退款中" value="REFUNDING" />
            <el-option label="已退款" value="REFUNDED" />
            <el-option label="已取消" value="CANCELED" />
          </el-select>
        </el-form-item>
        <el-form-item label="支付方式">
          <el-select v-model="query.payType" placeholder="全部" clearable style="width:120px">
            <el-option label="现金" value="CASH" />
            <el-option label="支付宝" value="ALIPAY" />
            <el-option label="微信" value="WECHAT" />
          </el-select>
        </el-form-item>
        <el-form-item label="商品">
          <el-input v-model="query.productName" placeholder="商品名称" clearable style="width:140px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" native-type="button" @click="search">查询</el-button>
          <el-button native-type="button" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="hover" class="page-card">
      <el-table :data="tableData" border stripe style="width:100%" size="default">
        <el-table-column prop="orderNo" label="订单号" width="180" />
        <el-table-column label="总金额" width="100" align="right">
          <template #default="{ row }">¥{{ row.totalAmount }}</template>
        </el-table-column>
        <el-table-column label="实付" width="100" align="right">
          <template #default="{ row }">¥{{ row.payAmount }}</template>
        </el-table-column>
        <el-table-column label="成本" width="90" align="right">
          <template #default="{ row }">¥{{ row.costAmount || '0.00' }}</template>
        </el-table-column>
        <el-table-column label="利润" width="100" align="right">
          <template #default="{ row }">
            <span :style="{ color: (row.payAmount - (row.costAmount || 0)) >= 0 ? '#67c23a' : '#f56c6c' }">
              ¥{{ (row.payAmount - (row.costAmount || 0)).toFixed(2) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="payType" label="支付方式" width="90" align="center">
          <template #default="{ row }">{{ payTypeMap[row.payType] || row.payType }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="statusType[row.status]" effect="dark" size="small">{{ statusMap[row.status] }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="cashierName" label="收银员" width="100" />
        <el-table-column prop="memberName" label="会员" width="100" />
        <el-table-column prop="createTime" label="时间" width="160" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="viewDetail(row)">详情</el-button>
            <el-button type="success" link size="small" @click="handlePrint(row)">小票</el-button>
            <el-button v-if="row.status === 'PAID'" type="warning" link size="small" @click="handleRefund(row)">退款</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        v-model:current-page="query.page"
        v-model:page-size="query.pageSize"
        :total="total"
        :page-sizes="[10,20,50]"
        layout="total,sizes,prev,pager,next"
        @size-change="getList"
        @current-change="getList"
        style="margin-top:16px;justify-content:flex-end"
      />
    </el-card>

    <el-dialog v-model="detailVisible" title="订单详情" width="650px" destroy-on-close>
      <div v-if="currentOrder">
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="订单号">{{ currentOrder.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="statusType[currentOrder.status]" effect="dark" size="small">{{ statusMap[currentOrder.status] }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="总金额">¥{{ currentOrder.totalAmount }}</el-descriptions-item>
          <el-descriptions-item label="优惠金额">¥{{ currentOrder.discountAmount || '0' }}</el-descriptions-item>
          <el-descriptions-item label="实付金额">¥{{ currentOrder.payAmount }}</el-descriptions-item>
          <el-descriptions-item label="成本金额">¥{{ currentOrder.costAmount || '0.00' }}</el-descriptions-item>
          <el-descriptions-item label="利润">
            <span :style="{ color: (currentOrder.payAmount - (currentOrder.costAmount || 0)) >= 0 ? '#67c23a' : '#f56c6c', fontWeight: 'bold' }">
              ¥{{ (currentOrder.payAmount - (currentOrder.costAmount || 0)).toFixed(2) }}
            </span>
          </el-descriptions-item>
          <el-descriptions-item label="支付方式">{{ payTypeMap[currentOrder.payType] }}</el-descriptions-item>
          <el-descriptions-item label="收银员">{{ currentOrder.cashierName }}</el-descriptions-item>
          <el-descriptions-item label="会员">{{ currentOrder.memberName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="时间">{{ currentOrder.createTime }}</el-descriptions-item>
          <el-descriptions-item label="备注" :span="2">{{ currentOrder.remark || '-' }}</el-descriptions-item>
        </el-descriptions>
        <el-divider>商品明细</el-divider>
        <el-table :data="currentOrder.items || []" border size="small">
          <el-table-column prop="productName" label="商品" />
          <el-table-column label="单价" width="100" align="right">
            <template #default="{ row }">¥{{ row.price }}</template>
          </el-table-column>
          <el-table-column prop="quantity" label="数量" width="80" align="center" />
          <el-table-column label="小计" width="100" align="right">
            <template #default="{ row }">¥{{ row.subtotal }}</template>
          </el-table-column>
        </el-table>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getOrderList, getOrderDetail, refundOrder, printOrder } from '@/api'

const query = reactive({ page: 1, pageSize: 10, status: '', payType: '', productName: '', startDate: '', endDate: '', cashierId: undefined as number | undefined })
const dateRange = ref<string[] | null>(null)
const total = ref(0)
const tableData = ref<any[]>([])
const detailVisible = ref(false)
const currentOrder = ref<any>(null)

const statusMap: Record<string, string> = { PENDING: '待支付', PAID: '已支付', REFUNDING: '退款中', REFUNDED: '已退款', CANCELED: '已取消' }
const statusType: Record<string, string> = { PENDING: 'warning', PAID: 'success', REFUNDING: '', REFUNDED: 'info', CANCELED: 'danger' }
const payTypeMap: Record<string, string> = { CASH: '现金', ALIPAY: '支付宝', WECHAT: '微信' }

const getList = async () => {
  if (dateRange.value) {
    query.startDate = dateRange.value[0]
    query.endDate = dateRange.value[1]
  } else {
    query.startDate = ''
    query.endDate = ''
  }
  const res = await getOrderList(query)
  if (res.code === 200) { tableData.value = res.data.list; total.value = res.data.total }
}

const search = () => { query.page = 1; getList() }
const resetQuery = () => {
  query.page = 1; query.status = ''; query.payType = ''; query.productName = ''; query.startDate = ''; query.endDate = ''
  dateRange.value = null; getList()
}

const viewDetail = async (row: any) => {
  const res = await getOrderDetail(row.id)
  if (res.code === 200) { currentOrder.value = res.data; detailVisible.value = true }
}

const handlePrint = async (row: any) => {
  const res = await printOrder(row.id)
  if (res.code === 200) {
    const content = res.data.printContent
    const printWindow = window.open('', '_blank')
    if (printWindow) {
      printWindow.document.write(`<pre style="font-size:14px;line-height:1.8">${content}</pre>`)
      printWindow.document.close()
      printWindow.print()
    }
  }
}

const handleRefund = async (row: any) => {
  await ElMessageBox.confirm(`确认对订单 ${row.orderNo} 发起退款？`, '退款确认', { type: 'warning', confirmButtonText: '确认退款', cancelButtonText: '取消' })
  const res = await refundOrder(row.id)
  if (res.code === 200) { ElMessage.success('退款成功'); getList() }
  else ElMessage.error(res.msg || '退款失败')
}

onMounted(getList)
</script>