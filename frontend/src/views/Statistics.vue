<template>
  <div>
    <div class="page-title">统计报表</div>

    <el-card shadow="hover" class="search-card">
      <el-form :inline="true" size="small">
        <el-form-item label="统计日期">
          <el-date-picker v-model="currentDate" type="date" value-format="YYYY-MM-DD" @change="loadData" style="width:160px" />
        </el-form-item>
        <el-form-item label="统计月份">
          <el-date-picker v-model="currentMonth" type="month" value-format="YYYY-MM" @change="loadData" style="width:160px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">刷新</el-button>
          <el-button type="success" @click="exportOrders">导出Excel</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-row :gutter="20" style="margin-top:20px">
      <el-col :span="6">
        <div class="stat-card-gradient bg-blue">
          <el-icon class="stat-icon"><Money /></el-icon>
          <div class="stat-label">日营业额</div>
          <div class="stat-value">¥{{ dailyRevenue }}</div>
          <div class="stat-footer" v-if="dailyOrderCount">订单数：{{ dailyOrderCount }}</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card-gradient bg-green">
          <el-icon class="stat-icon"><Coin /></el-icon>
          <div class="stat-label">日成本</div>
          <div class="stat-value">¥{{ dailyCost }}</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card-gradient bg-purple">
          <el-icon class="stat-icon"><Money /></el-icon>
          <div class="stat-label">日利润</div>
          <div class="stat-value">¥{{ dailyProfit }}</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card-gradient bg-orange">
          <el-icon class="stat-icon"><DataAnalysis /></el-icon>
          <div class="stat-label">月营业额</div>
          <div class="stat-value">¥{{ monthlyRevenue }}</div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top:20px">
      <el-col :span="6">
        <div class="stat-card-gradient bg-blue">
          <el-icon class="stat-icon"><Download /></el-icon>
          <div class="stat-label">日采购额</div>
          <div class="stat-value">¥{{ dailyPurchase }}</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card-gradient bg-green">
          <el-icon class="stat-icon"><Coin /></el-icon>
          <div class="stat-label">月采购额</div>
          <div class="stat-value">¥{{ monthlyPurchase }}</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card-gradient bg-purple">
          <el-icon class="stat-icon"><Money /></el-icon>
          <div class="stat-label">净收入（月销-月采）</div>
          <div class="stat-value">¥{{ netIncome }}</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card-gradient bg-orange">
          <el-icon class="stat-icon"><DataAnalysis /></el-icon>
          <div class="stat-label">采购占比</div>
          <div class="stat-value">{{ purchaseRatio }}</div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top:20px">
      <el-col :span="6">
        <div class="stat-card-gradient bg-purple">
          <el-icon class="stat-icon"><Coin /></el-icon>
          <div class="stat-label">月利润</div>
          <div class="stat-value">¥{{ monthlyProfit }}</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card-gradient bg-green">
          <el-icon class="stat-icon"><Tickets /></el-icon>
          <div class="stat-label">月订单数</div>
          <div class="stat-value">{{ monthlyOrderCount }}</div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top:20px">
      <el-col :span="14">
        <el-card shadow="hover" class="chart-card">
          <template #header>
            <div class="chart-header">
              <span class="chart-title">本月每日营业额趋势</span>
              <el-tag size="small" type="info">元</el-tag>
            </div>
          </template>
          <v-chart :option="dailyTrendOption" style="height:380px" autoresize />
        </el-card>
      </el-col>
      <el-col :span="10">
        <el-card shadow="hover" class="chart-card">
          <template #header>
            <div class="chart-header">
              <span class="chart-title">热销商品占比</span>
              <el-tag size="small" type="info">销售额</el-tag>
            </div>
          </template>
          <v-chart :option="pieOption" style="height:380px" autoresize />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { use } from 'echarts/core'
import { BarChart, PieChart } from 'echarts/charts'
import { GridComponent, TooltipComponent, LegendComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import VChart from 'vue-echarts'
import dayjs from 'dayjs'
import { getDailyRevenue, getMonthlyRevenue, getTopProducts, getPurchaseStats } from '@/api'
import { Money, DataAnalysis, Tickets, Coin, Download } from '@element-plus/icons-vue'

use([BarChart, PieChart, GridComponent, TooltipComponent, LegendComponent, CanvasRenderer])

const currentDate = ref(dayjs().format('YYYY-MM-DD'))
const currentMonth = ref(dayjs().format('YYYY-MM'))
const dailyRevenue = ref(0)
const dailyCost = ref(0)
const dailyProfit = ref(0)
const dailyOrderCount = ref(0)
const monthlyRevenue = ref(0)
const monthlyProfit = ref(0)
const monthlyOrderCount = ref(0)
const dailyPurchase = ref(0)
const monthlyPurchase = ref(0)
const netIncome = ref(0)
const purchaseRatio = ref('0%')
const dailyTrendData = ref<{ dates: string[]; amounts: number[]; orderCounts: number[] }>({ dates: [], amounts: [], orderCounts: [] })
const pieData = ref<{ name: string; value: number }[]>([])

const dailyTrendOption = computed(() => ({
  tooltip: {
    trigger: 'axis',
    axisPointer: { type: 'shadow' },
    backgroundColor: 'rgba(255,255,255,0.95)',
    borderWidth: 0,
    boxShadow: '0 4px 12px rgba(0,0,0,0.1)',
    formatter: (params: any) => {
      const item = params[0]
      return `<div style="font-size:14px;font-weight:600">${item.name}</div>
              <div style="font-size:18px;color:#1890ff;font-weight:700;margin-top:4px">¥${item.value}</div>`
    }
  },
  grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
  xAxis: {
    type: 'category',
    data: dailyTrendData.value.dates,
    axisLine: { lineStyle: { color: '#e8e8e8' } },
    axisLabel: { rotate: 45, color: '#8c8c8c', fontSize: 12 }
  },
  yAxis: {
    type: 'value',
    name: '营业额 (¥)',
    splitLine: { lineStyle: { color: '#f0f0f0', type: 'dashed' } },
    axisLabel: { color: '#8c8c8c' }
  },
  series: [{
    type: 'bar',
    data: dailyTrendData.value.amounts,
    barWidth: '60%',
    itemStyle: {
      color: {
        type: 'linear', x: 0, y: 0, x2: 0, y2: 1,
        colorStops: [
          { offset: 0, color: '#1890ff' },
          { offset: 1, color: '#096dd9' }
        ]
      },
      borderRadius: [4, 4, 0, 0]
    }
  }]
}))

const pieOption = computed(() => ({
  tooltip: {
    trigger: 'item',
    formatter: '{b}: ¥{c} ({d}%)',
    backgroundColor: 'rgba(255,255,255,0.95)',
    borderWidth: 0,
    boxShadow: '0 4px 12px rgba(0,0,0,0.1)'
  },
  legend: {
    orient: 'vertical',
    left: 'left',
    bottom: 10
  },
  series: [{
    type: 'pie',
    radius: ['35%', '60%'],
    center: ['50%', '50%'],
    avoidLabelOverlap: true,
    itemStyle: {
      borderRadius: 4,
      borderColor: '#fff',
      borderWidth: 2
    },
    label: {
      show: true,
      formatter: '{b}\n¥{c}',
      fontSize: 12
    },
    emphasis: {
      label: {
        show: true,
        fontSize: 14,
        fontWeight: 'bold'
      }
    },
    data: pieData.value
  }]
}))

const loadData = async () => {
  try {
    // 日营业额
    const dailyRes = await getDailyRevenue(currentDate.value)
    if (dailyRes.code === 200) {
      dailyRevenue.value = dailyRes.data.revenue || 0
      dailyCost.value = dailyRes.data.cost || 0
      dailyProfit.value = dailyRes.data.profit || 0
      dailyOrderCount.value = dailyRes.data.orderCount || 0
    }

    // 月营业额
    const [year, month] = (currentMonth.value || dayjs().format('YYYY-MM')).split('-').map(Number)
    const monthlyRes = await getMonthlyRevenue(year, month)
    if (monthlyRes.code === 200) {
      monthlyRevenue.value = monthlyRes.data.revenue || 0
      monthlyProfit.value = monthlyRes.data.profit || 0
    }

    // 采购统计
    const purchaseRes = await getPurchaseStats(currentDate.value)
    if (purchaseRes.code === 200) {
      dailyPurchase.value = purchaseRes.data.dailyPurchase || 0
      monthlyPurchase.value = purchaseRes.data.monthlyPurchase || 0
      netIncome.value = monthlyRevenue.value - monthlyPurchase.value
      purchaseRatio.value = monthlyRevenue.value > 0
        ? (monthlyPurchase.value / monthlyRevenue.value * 100).toFixed(1) + '%'
        : '0%'
    }

    // 当月每日营业额
    const dates: string[] = []
    const amounts: number[] = []
    const orderCounts: number[] = []
    const daysInMonth = dayjs(`${year}-${month}`).daysInMonth()
    for (let d = 1; d <= daysInMonth; d++) {
      const dateStr = `${year}-${String(month).padStart(2, '0')}-${String(d).padStart(2, '0')}`
      dates.push(`${d}日`)
      const res = await getDailyRevenue(dateStr)
      amounts.push(res.code === 200 ? (res.data.revenue || 0) : 0)
      orderCounts.push(res.code === 200 ? (res.data.orderCount || 0) : 0)
    }
    dailyTrendData.value = { dates, amounts, orderCounts }
    // 当月订单总数
    monthlyOrderCount.value = orderCounts.reduce((a, b) => a + b, 0)

    // 热销商品
    const topRes = await getTopProducts({ date: currentDate.value, limit: 5 })
    if (topRes.code === 200) {
      pieData.value = topRes.data.map((item: any) => ({ name: item.productName, value: item.totalAmount }))
    }
  } catch (e) {
    console.error(e)
  }
}

const exportOrders = () => {
  const token = localStorage.getItem('token') || ''
  const params = new URLSearchParams()
  if (currentDate.value) {
    params.append('startDate', currentDate.value)
    params.append('endDate', currentDate.value)
  }
  const url = `/api/export/orders?${params.toString()}`
  const a = document.createElement('a')
  a.href = url
  a.style.display = 'none'
  document.body.appendChild(a)
  a.click()
  document.body.removeChild(a)
}

onMounted(loadData)
</script>

<style scoped>
.stat-card-gradient {
  min-height: 130px;
}

.chart-card {
  border-radius: 10px;
}

.chart-card :deep(.el-card__header) {
  padding: 14px 20px;
  border-bottom: 1px solid #f0f0f0;
}

.chart-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.chart-title {
  font-size: 15px;
  font-weight: 600;
  color: #1a1a2e;
}
</style>