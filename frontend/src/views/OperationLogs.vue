<template>
  <div>
    <div class="page-title">操作日志</div>

    <el-card shadow="hover" class="search-card">
      <el-form :inline="true" size="default">
        <el-form-item label="模块">
          <el-select v-model="query.module" placeholder="全部" clearable style="width:140px">
            <el-option label="商品管理" value="product" />
            <el-option label="分类管理" value="category" />
            <el-option label="订单管理" value="order" />
            <el-option label="会员管理" value="member" />
            <el-option label="用户管理" value="user" />
            <el-option label="供应商管理" value="supplier" />
            <el-option label="采购管理" value="purchase" />
          </el-select>
        </el-form-item>
        <el-form-item label="操作类型">
          <el-select v-model="query.action" placeholder="全部" clearable style="width:120px">
            <el-option label="新增" value="add" />
            <el-option label="修改" value="update" />
            <el-option label="删除" value="delete" />
            <el-option label="入库" value="stock_in" />
            <el-option label="退款" value="refund" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" native-type="button" @click="search">查询</el-button>
          <el-button native-type="button" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="hover" class="page-card">
      <div style="margin-bottom:12px">
        <el-button type="danger" :disabled="selectedIds.length === 0" @click="batchDelete">批量删除</el-button>
        <span style="margin-left:8px;font-size:13px;color:#999" v-if="selectedIds.length > 0">已选择 {{ selectedIds.length }} 项</span>
      </div>
      <el-table :data="tableData" border stripe style="width:100%" size="default" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="45" align="center" />
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="userName" label="操作人" width="100" />
        <el-table-column prop="module" label="模块" width="100" align="center">
          <template #default="{ row }">
            <el-tag size="small">
              {{ { product: '商品', category: '分类', order: '订单', member: '会员', user: '用户', supplier: '供应商', purchase: '采购' }[row.module] || row.module }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="action" label="操作类型" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.action === 'delete' ? 'danger' : row.action === 'add' ? 'success' : 'warning'" size="small">
              {{ { add: '新增', update: '修改', delete: '删除', stock_in: '入库', refund: '退款' }[row.action] || row.action }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="detail" label="操作详情" min-width="300" show-overflow-tooltip />
        <el-table-column prop="createTime" label="操作时间" width="160" />
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
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getOperationLogList, deleteOperationLog } from '@/api'
import type { OperationLog } from '@/types'

const query = reactive({ page: 1, pageSize: 10, module: '', action: '' })
const total = ref(0)
const tableData = ref<OperationLog[]>([])
const selectedIds = ref<number[]>([])

const handleSelectionChange = (rows: OperationLog[]) => {
  selectedIds.value = rows.map(r => r.id)
}

const batchDelete = async () => {
  if (selectedIds.value.length === 0) return
  try {
    await ElMessageBox.confirm(`确定删除已选的 ${selectedIds.value.length} 条操作日志？`, '确认删除', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
    const res = await deleteOperationLog(selectedIds.value)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      selectedIds.value = []
      getList()
    } else {
      ElMessage.error(res.msg || '删除失败')
    }
  } catch {
    // cancelled
  }
}

const getList = async () => {
  const params: Record<string, unknown> = { page: query.page, pageSize: query.pageSize }
  if (query.module) params.module = query.module
  if (query.action) params.action = query.action
  const res = await getOperationLogList(params)
  if (res.code === 200) { tableData.value = res.data.list; total.value = res.data.total }
}

const search = () => { query.page = 1; getList() }
const resetQuery = () => { query.module = ''; query.action = ''; query.page = 1; getList() }

onMounted(() => getList())
</script>