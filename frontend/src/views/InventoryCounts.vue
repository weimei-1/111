<template>
  <div>
    <div class="page-title">库存盘点</div>

    <el-card shadow="hover" class="search-card">
      <el-form :inline="true" size="default">
        <el-form-item label="盘点单号">
          <el-input v-model="query.countNo" placeholder="搜索单号" clearable @keyup.enter="search" style="width:180px" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部" clearable style="width:120px">
            <el-option label="待盘点" :value="0" />
            <el-option label="已完成" :value="1" />
            <el-option label="已审核" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" native-type="button" @click="search">查询</el-button>
          <el-button native-type="button" @click="resetQuery">重置</el-button>
          <el-button type="primary" native-type="button" @click="openAdd" plain>+ 新增盘点</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="hover" class="page-card">
      <el-table :data="tableData" border stripe style="width:100%" size="default">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="countNo" label="盘点单号" width="180" />
        <el-table-column prop="countDate" label="盘点日期" width="120" />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 0 ? 'warning' : row.status === 1 ? 'success' : 'primary'" size="small">
              {{ row.status === 0 ? '待盘点' : row.status === 1 ? '已完成' : '已审核' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="160" show-overflow-tooltip />
        <el-table-column prop="createTime" label="创建时间" width="160" />
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="viewDetail(row)">详情</el-button>
            <el-button v-if="row.status === 0" type="success" link size="small" @click="handleComplete(row.id)">完成</el-button>
            <el-button v-if="row.status === 1" type="primary" link size="small" @click="handleAudit(row.id)">审核</el-button>
            <el-button v-if="row.status === 0" type="danger" link size="small" @click="handleDelete(row.id)">删除</el-button>
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

    <!-- 新增盘点 -->
    <el-dialog v-model="dialogVisible" title="新增盘点单" width="750px" destroy-on-close>
      <el-form ref="formRef" :model="form" label-width="80px">
        <el-form-item label="盘点日期">
          <el-date-picker v-model="form.countDate" type="date" placeholder="选择日期" style="width:200px" value-format="YYYY-MM-DD" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" placeholder="备注" type="textarea" :rows="2" />
        </el-form-item>
        <el-divider>盘点商品明细</el-divider>
        <div style="margin-bottom:12px">
          <el-button type="primary" size="small" @click="addItem">+ 添加商品</el-button>
        </div>
        <el-table :data="form.items" border stripe size="small" style="width:100%">
          <el-table-column label="商品" min-width="180">
            <template #default="{ row, $index }">
              <el-select v-model="row.productId" placeholder="搜索商品" filterable remote
                :remote-method="searchProduct"
                @change="(val) => onProductChange($index, val)"
                style="width:100%">
                <el-option v-for="p in productOptions" :key="p.id" :label="`${p.name} (库存:${p.stock})`" :value="p.id" />
              </el-select>
            </template>
          </el-table-column>
          <el-table-column label="账面库存" width="100" align="center">
            <template #default="{ row }">{{ row.bookStock }}</template>
          </el-table-column>
          <el-table-column label="实际库存" width="120">
            <template #default="{ row }">
              <el-input-number v-model="row.actualStock" :min="0" style="width:100%" />
            </template>
          </el-table-column>
          <el-table-column label="差异" width="100" align="center">
            <template #default="{ row }">
              <span :style="{ color: (row.actualStock - row.bookStock) !== 0 ? '#f56c6c' : '#67c23a' }">
                {{ row.actualStock - row.bookStock > 0 ? '+' : '' }}{{ row.actualStock - row.bookStock }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="60" align="center">
            <template #default="{ $index }">
              <el-button type="danger" link size="small" @click="form.items.splice($index, 1)">移除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave" :loading="saving">保存</el-button>
      </template>
    </el-dialog>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="盘点单详情" width="650px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="盘点单号">{{ detail?.countNo }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="detail?.status === 0 ? 'warning' : detail?.status === 1 ? 'success' : 'primary'" size="small">
            {{ detail?.status === 0 ? '待盘点' : detail?.status === 1 ? '已完成' : '已审核' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="盘点日期">{{ detail?.countDate }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ detail?.createTime }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ detail?.remark || '-' }}</el-descriptions-item>
      </el-descriptions>
      <el-divider>商品明细</el-divider>
      <el-table :data="detail?.items || []" border stripe size="small" style="width:100%">
        <el-table-column prop="productName" label="商品名称" min-width="160" />
        <el-table-column prop="bookStock" label="账面库存" width="100" align="center" />
        <el-table-column prop="actualStock" label="实际库存" width="100" align="center" />
        <el-table-column prop="difference" label="差异" width="100" align="center">
          <template #default="{ row }">
            <span :style="{ color: row.difference !== 0 ? '#f56c6c' : '#67c23a', fontWeight: 'bold' }">
              {{ row.difference > 0 ? '+' : '' }}{{ row.difference }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="120" />
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getInventoryCountList, addInventoryCount, completeInventoryCount, auditInventoryCount, deleteInventoryCount, getProductList } from '@/api'
import type { InventoryCount, Product } from '@/types'

const query = reactive({ page: 1, pageSize: 10, countNo: '', status: undefined as number | undefined })
const total = ref(0)
const tableData = ref<InventoryCount[]>([])
const dialogVisible = ref(false)
const detailVisible = ref(false)
const saving = ref(false)
const formRef = ref()
const productOptions = ref<Product[]>([])
const detail = ref<InventoryCount>()

const form = reactive({
  countDate: '',
  remark: '',
  items: [] as Array<{ productId: number; productName: string; bookStock: number; actualStock: number }>
})

const getList = async () => {
  const params: Record<string, unknown> = { page: query.page, pageSize: query.pageSize }
  if (query.countNo) params.countNo = query.countNo
  if (query.status !== undefined) params.status = query.status
  const res = await getInventoryCountList(params)
  if (res.code === 200) { tableData.value = res.data.list; total.value = res.data.total }
}

const search = () => { query.page = 1; getList() }
const resetQuery = () => { query.countNo = ''; query.status = undefined; query.page = 1; getList() }

const searchProduct = async (q: string) => {
  const res = await getProductList({ keyword: q, page: 1, pageSize: 20 })
  if (res.code === 200) productOptions.value = res.data.list
}

const onProductChange = (index: number, productId: number) => {
  const product = productOptions.value.find(p => p.id === productId)
  if (product) {
    form.items[index].productName = product.name
    form.items[index].bookStock = product.stock || 0
  }
}

const addItem = () => {
  form.items.push({ productId: 0, productName: '', bookStock: 0, actualStock: 0 })
}

const openAdd = async () => {
  form.countDate = ''
  form.remark = ''
  form.items = []
  dialogVisible.value = true
}

const viewDetail = (row: InventoryCount) => {
  detail.value = row
  detailVisible.value = true
}

const handleSave = async () => {
  if (form.items.length === 0) { ElMessage.warning('请添加至少一个商品'); return }
  saving.value = true
  try {
    const res = await addInventoryCount({
      countDate: form.countDate,
      remark: form.remark,
      status: 0,
      countNo: '',
      createTime: '',
      items: form.items.map(item => ({
        productId: item.productId,
        productName: item.productName,
        bookStock: item.bookStock,
        actualStock: item.actualStock,
        difference: item.actualStock - item.bookStock,
        remark: ''
      }))
    } as InventoryCount)
    if (res.code === 200) { ElMessage.success('新增成功'); dialogVisible.value = false; getList() }
    else ElMessage.error(res.msg)
  } finally { saving.value = false }
}

const handleComplete = async (id: number) => {
  await ElMessageBox.confirm('确认完成该盘点单？', '提示', { type: 'info', confirmButtonText: '确认', cancelButtonText: '取消' })
  const res = await completeInventoryCount(id)
  if (res.code === 200) { ElMessage.success('盘点完成'); getList() }
  else ElMessage.error(res.msg)
}

const handleAudit = async (id: number) => {
  await ElMessageBox.confirm('审核通过后，商品库存将按实际盘点数量更新，确认审核？', '提示', { type: 'warning', confirmButtonText: '确认审核', cancelButtonText: '取消' })
  const res = await auditInventoryCount(id)
  if (res.code === 200) { ElMessage.success('审核通过，库存已更新'); getList() }
  else ElMessage.error(res.msg)
}

const handleDelete = async (id: number) => {
  await ElMessageBox.confirm('确认删除该盘点单？', '提示', { type: 'warning', confirmButtonText: '确认删除', cancelButtonText: '取消' })
  const res = await deleteInventoryCount(id)
  if (res.code === 200) { ElMessage.success('删除成功'); getList() }
}

onMounted(() => getList())
</script>