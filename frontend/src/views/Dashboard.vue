<template>
  <div class="dashboard">
    <div class="page-title">数据概览</div>

    <!-- 顶部统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6" v-for="item in statsCards" :key="item.label">
        <div class="stat-card-gradient" :class="item.bg">
          <el-icon class="stat-icon"><component :is="item.icon" /></el-icon>
          <div class="stat-label">{{ item.label }}</div>
          <div class="stat-value">{{ item.value }}</div>
          <div class="stat-footer" v-if="item.footer">{{ item.footer }}</div>
        </div>
      </el-col>
    </el-row>
    <!-- 第二行统计卡片 -->
    <el-row :gutter="20" class="stats-row" style="margin-top:20px">
      <el-col :span="6" v-for="item in statsCards2" :key="item.label">
        <div class="stat-card-gradient" :class="item.bg">
          <el-icon class="stat-icon"><component :is="item.icon" /></el-icon>
          <div class="stat-label">{{ item.label }}</div>
          <div class="stat-value">{{ item.value }}</div>
          <div class="stat-footer" v-if="item.footer">{{ item.footer }}</div>
        </div>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="20" class="chart-row">
      <el-col :span="14">
        <el-card shadow="hover" class="chart-card">
          <template #header>
            <div class="chart-header">
              <span class="chart-title">近7日营业额趋势</span>
              <el-tag size="small" type="info">元</el-tag>
            </div>
          </template>
          <v-chart :option="revenueOption" style="height:360px" autoresize />
        </el-card>
      </el-col>
      <el-col :span="10">
        <el-card shadow="hover" class="chart-card">
          <template #header>
            <div class="chart-header">
              <span class="chart-title">热销商品 Top5</span>
              <el-tag size="small" type="info">销售额</el-tag>
            </div>
          </template>
          <v-chart :option="topProductsOption" style="height:360px" autoresize />
        </el-card>
      </el-col>
    </el-row>

    <!-- 快捷入口 -->
    <el-card shadow="hover" class="quick-card">
      <template #header>
        <div class="chart-header">
          <span class="chart-title">快捷操作</span>
        </div>
      </template>
      <el-row :gutter="16">
        <el-col :span="6" v-for="item in quickActions" :key="item.label">
          <el-button class="quick-btn" @click="router.push(item.path)">
            <el-icon :size="24"><component :is="item.icon" /></el-icon>
            <span>{{ item.label }}</span>
          </el-button>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { use } from 'echarts/core'
import { LineChart, BarChart } from 'echarts/charts'
import { GridComponent, TooltipComponent, LegendComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import VChart from 'vue-echarts'
import dayjs from 'dayjs'
import { getDailyRevenue, getMonthlyRevenue, getTopProducts, getWarningList } from '@/api'
import {
  Goods, ShoppingCart, Money, WarningFilled, Coin,
  List, UserFilled, DataAnalysis, Tickets
} from '@element-plus/icons-vue'

use([LineChart, BarChart, GridComponent, TooltipComponent, LegendComponent, CanvasRenderer])

const router = useRouter()

const statsCards = ref([
  { label: '今日订单数', value: 0, icon: 'Tickets', bg: 'bg-blue', footer: '' },
  { label: '今日营业额', value: '¥0', icon: 'Money', bg: 'bg-green', footer: '' },
  { label: '今日利润', value: '¥0', icon: 'Coin', bg: 'bg-purple', footer: '' },
  { label: '本月营业额', value: '¥0', icon: 'DataAnalysis', bg: 'bg-purple', footer: '' }
])

const statsCards2 = ref([
  { label: '本月利润', value: '¥0', icon: 'Coin', bg: 'bg-green', footer: '' },
  { label: '库存预警', value: 0, icon: 'WarningFilled', bg: 'bg-orange', footer: '' }
])

const quickActions = [
  { label: '商品管理', path: '/products', icon: 'Goods' },
  { label: '收银台', path: '/cashier', icon: 'ShoppingCart' },
  { label: '订单记录', path: '/orders', icon: 'List' },
  { label: '会员管理', path: '/members', icon: 'UserFilled' }
]

const chartData = ref<{ dates: string[]; amounts: number[] }>({ dates: [], amounts: [] })
const topProducts = ref<{ names: string[]; amounts: number[] }>({ names: [], amounts: [] })

const revenueOption = computed(() => ({
  tooltip: {
    trigger: 'axis',
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
    data: chartData.value.dates,
    axisLine: { lineStyle: { color: '#e8e8e8' } },
    axisLabel: { color: '#8c8c8c', fontSize: 12 }
  },
  yAxis: {
    type: 'value',
    splitLine: { lineStyle: { color: '#f0f0f0', type: 'dashed' } },
    axisLabel: { color: '#8c8c8c' }
  },
  series: [{
    type: 'line',
    data: chartData.value.amounts,
    smooth: true,
    lineStyle: { width: 3, color: '#1890ff' },
    itemStyle: { color: '#1890ff' },
    areaStyle: {
      color: {
        type: 'linear', x: 0, y: 0, x2: 0, y2: 1,
        colorStops: [
          { offset: 0, color: 'rgba(24,144,255,0.25)' },
          { offset: 1, color: 'rgba(24,144,255,0.02)' }
        ]
      }
    },
    symbol: 'circle',
    symbolSize: 8
  }]
}))

const topProductsOption = computed(() => ({
  tooltip: {
    trigger: 'axis',
    axisPointer: { type: 'shadow' },
    formatter: (params: any) => {
      const item = params[0]
      return `<div style="font-size:14px;font-weight:600">${item.name}</div>
              <div style="font-size:16px;color:#1890ff;font-weight:700;margin-top:4px">¥${item.value}</div>`
    }
  },
  grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
  xAxis: {
    type: 'category',
    data: topProducts.value.names,
    axisLabel: { color: '#8c8c8c', fontSize: 11 }
  },
  yAxis: {
    type: 'value',
    splitLine: { lineStyle: { color: '#f0f0f0', type: 'dashed' } }
  },
  series: [{
    type: 'bar',
    data: topProducts.value.amounts,
    barWidth: '50%',
    itemStyle: {
      color: {
        type: 'linear', x: 0, y: 0, x2: 0, y2: 1,
        colorStops: [
          { offset: 0, color: '#1890ff' },
          { offset: 1, color: '#096dd9' }
        ]
      },
      borderRadius: [6, 6, 0, 0]
    }
  }]
}))

onMounted(async () => {
  const today = dayjs().format('YYYY-MM-DD')
  const month = dayjs().format('YYYY-MM')
  const [year, monthNum] = month.split('-')

  try {
    const [dailyRes, monthlyRes, topRes, warnRes] = await Promise.all([
      getDailyRevenue(today),
      getMonthlyRevenue(Number(year), Number(monthNum)),
      getTopProducts({ date: today, limit: 5 }),
      getWarningList()
    ])

    if (dailyRes.code === 200) {
      statsCards.value[0].value = dailyRes.data.orderCount ?? 0
      statsCards.value[1].value = '¥' + (dailyRes.data.revenue ?? 0).toFixed(2)
      statsCards.value[2].value = '¥' + (dailyRes.data.profit ?? 0).toFixed(2)
    }
    if (monthlyRes.code === 200) {
      statsCards.value[3].value = '¥' + (monthlyRes.data.revenue ?? 0).toFixed(2)
      statsCards2.value[0].value = '¥' + (monthlyRes.data.profit ?? 0).toFixed(2)
    }
    if (topRes.code === 200) {
      topProducts.value.names = topRes.data.map((item: any) => item.productName)
      topProducts.value.amounts = topRes.data.map((item: any) => item.totalAmount)
    }
    if (warnRes.code === 200) {
      statsCards2.value[1].value = warnRes.data.length ?? 0
    }

    // 近7日趋势
    const dates: string[] = []
    const amounts: number[] = []
    for (let i = 6; i >= 0; i--) {
      const d = dayjs().subtract(i, 'day').format('YYYY-MM-DD')
      dates.push(dayjs().subtract(i, 'day').format('MM-DD'))
      const r = await getDailyRevenue(d)
      amounts.push(r.code === 200 ? (r.data.revenue ?? 0) : 0)
    }
    chartData.value = { dates, amounts }
  } catch (e) {
    console.error(e)
  }
})
</script>

<style scoped>
.dashboard {
  max-width: 1400px;
  margin: 0 auto;
}

.stats-row {
  margin-bottom: 20px;
}

.chart-row {
  margin-bottom: 20px;
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

.quick-card {
  border-radius: 10px;
}

.quick-card :deep(.el-card__header) {
  padding: 14px 20px;
  border-bottom: 1px solid #f0f0f0;
}

.quick-btn {
  width: 100%;
  height: 80px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  border-radius: 10px;
  border: 1px solid #e8e8e8;
  background: #fff;
  transition: all 0.3s;
  font-size: 13px;
  color: #333;
}

.quick-btn:hover {
  border-color: #1890ff;
  color: #1890ff;
  background: rgba(24, 144, 255, 0.04);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(24, 144, 255, 0.1);
}
</style>