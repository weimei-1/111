<template>
  <div>
    <div class="page-title">采购进货管理</div>

    <el-card shadow="hover" class="search-card">
      <el-form :inline="true" size="default">
        <el-form-item label="采购单号">
          <el-input v-model="query.orderNo" placeholder="搜索采购单号" clearable @keyup.enter="search" style="width:180px" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部" clearable style="width:120px">
            <el-option label="待入库" :value="0" />
            <el-option label="已入库" :value="1" />
            <el-option label="已取消" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" native-type="button" @click="search">查询</el-button>
          <el-button native-type="button" @click="resetQuery">重置</el-button>
          <el-button type="primary" native-type="button" @click="openAdd" plain>+ 新增采购单</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="hover" class="page-card">
      <el-table :data="tableData" border stripe style="width:100%" size="default">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="orderNo" label="采购单号" width="180" />
        <el-table-column prop="supplierName" label="供应商" min-width="140" />
        <el-table-column prop="totalAmount" label="采购金额" width="120" align="right">
          <template #default="{ row }">￥{{ row.totalAmount?.toFixed(2) }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 0 ? 'warning' : row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 0 ? '待入库' : row.status === 1 ? '已入库' : '已取消' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="120" show-overflow-tooltip />
        <el-table-column prop="createTime" label="创建时间" width="160" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="viewDetail(row)">详情</el-button>
            <el-button v-if="row.status === 0" type="success" link size="small" @click="handle入库(row.id)">入库</el-button>
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

    <!-- 新增/编辑采购单 -->
    <el-dialog v-model="dialogVisible" title="新增采购单" width="750px" destroy-on-close>
      <el-form ref="formRef" :model="form" label-width="80px">
        <el-form-item label="供应商" prop="supplierId" :rules="[{ required: true, message: '请选择供应商' }]">
          <el-select v-model="form.supplierId" placeholder="请选择供应商" clearable filterable style="width:300px">
            <el-option v-for="s in supplierList" :key="s.id" :label="s.name" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" placeholder="备注" type="textarea" :rows="2" />
        </el-form-item>

        <el-divider>采购商品明细</el-divider>

        <div style="margin-bottom:12px">
          <el-button type="primary" size="small" @click="addItem">+ 添加商品</el-button>
        </div>

        <el-table :data="form.items" border stripe size="small" style="width:100%">
          <el-table-column label="商品" min-width="180">
            <template #default="{ row, $index }">
              <el-select v-model="row.productId" placeholder="搜索商品" filterable remote
                :remote-method="(q: string) => searchProduct(q)"
                @change="(val: number) => onProductChange($index, val)"
                style="width:100%">
                <el-option v-for="p in productOptions" :key="p.id" :label="p.name" :value="p.id" />
              </el-select>
            </template>
          </el-table-column>
          <el-table-column label="数量" width="120">
            <template #default="{ row }">
              <el-input-number v-model="row.quantity" :min="1" style="width:100%" />
            </template>
          </el-table-column>
          <el-table-column label="采购单价" width="140">
            <template #default="{ row }">
              <el-input-number v-model="row.purchasePrice" :min="0" :precision="2" style="width:100%" />
            </template>
          </el-table-column>
          <el-table-column label="小计" width="120" align="right">
            <template #default="{ row }">
              ￥{{ (row.quantity * row.purchasePrice).toFixed(2) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="60" align="center">
            <template #default="{ $index }">
              <el-button type="danger" link size="small" @click="form.items.splice($index, 1)">移除</el-button>
            </template>
          </el-table-column>
        </el-table>

        <div style="text-align:right;margin-top:12px;font-size:16px;font-weight:600">
          合计：￥{{ totalAmount.toFixed(2) }}
        </div>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave" :loading="saving">保存</el-button>
      </template>
    </el-dialog>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="采购单详情" width="650px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="采购单号">{{ detail?.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="detail?.status === 0 ? 'warning' : detail?.status === 1 ? 'success' : 'info'" size="small">
            {{ detail?.status === 0 ? '待入库' : detail?.status === 1 ? '已入库' : '已取消' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="供应商" :span="2">{{ detail?.supplierName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="采购金额" :span="2">￥{{ detail?.totalAmount?.toFixed(2) }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ detail?.remark || '-' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间" :span="2">{{ detail?.createTime }}</el-descriptions-item>
      </el-descriptions>
      <el-divider>商品明细</el-divider>
      <el-table :data="detail?.items || []" border stripe size="small" style="width:100%">
        <el-table-column prop="productName" label="商品名称" min-width="160" />
        <el-table-column prop="quantity" label="数量" width="80" align="center" />
        <el-table-column prop="purchasePrice" label="单价" width="120" align="right">
          <template #default="{ row }">￥{{ row.purchasePrice?.toFixed(2) }}</template>
        </el-table-column>
        <el-table-column prop="subtotal" label="小计" width="120" align="right">
          <template #default="{ row }">￥{{ row.subtotal?.toFixed(2) }}</template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getPurchaseList, addPurchase, deletePurchase, purchaseStockIn, getSupplierList, getProductList } from '@/api'
import type { PurchaseOrder, PurchaseOrderItem, Supplier, Product } from '@/types'

const query = reactive({ page: 1, pageSize: 10, orderNo: '', status: undefined as number | undefined })
const total = ref(0)
const tableData = ref<PurchaseOrder[]>([])
const dialogVisible = ref(false)
const detailVisible = ref(false)
const saving = ref(false)
const formRef = ref()
const supplierList = ref<Supplier[]>([])
const productOptions = ref<Product[]>([])
const detail = ref<PurchaseOrder>()

const form = reactive({
  supplierId: undefined as number | undefined,
  remark: '',
  items: [] as Array<{ productId: number; productName: string; quantity: number; purchasePrice: number }>
})

const totalAmount = computed(() => {
  return form.items.reduce((sum, item) => sum + item.quantity * item.purchasePrice, 0)
})

const getList = async () => {
  const params: Record<string, unknown> = { page: query.page, pageSize: query.pageSize }
  if (query.orderNo) params.orderNo = query.orderNo
  if (query.status !== undefined) params.status = query.status
  const res = await getPurchaseList(params)
  if (res.code === 200) { tableData.value = res.data.list; total.value = res.data.total }
}

const search = () => { query.page = 1; getList() }
const resetQuery = () => { query.orderNo = ''; query.status = undefined; query.page = 1; getList() }

const loadSuppliers = async () => {
  const res = await getSupplierList()
  if (res.code === 200) supplierList.value = res.data
}

const searchProduct = async (q: string) => {
  const res = await getProductList({ keyword: q, page: 1, pageSize: 20 })
  if (res.code === 200) productOptions.value = res.data.list
}

const onProductChange = (index: number, productId: number) => {
  const product = productOptions.value.find(p => p.id === productId)
  if (product) {
    form.items[index].productName = product.name
    form.items[index].purchasePrice = product.costPrice || 0
  }
}

const addItem = () => {
  form.items.push({ productId: 0, productName: '', quantity: 1, purchasePrice: 0 })
}

const openAdd = async () => {
  await loadSuppliers()
  form.supplierId = undefined
  form.remark = ''
  form.items = []
  dialogVisible.value = true
}

const viewDetail = async (row: PurchaseOrder) => {
  detail.value = row
  detailVisible.value = true
}

const handleSave = async () => {
  if (!form.supplierId) { ElMessage.warning('请选择供应商'); return }
  if (form.items.length === 0) { ElMessage.warning('请添加至少一个商品'); return }
  saving.value = true
  try {
    const res = await addPurchase({
      supplierId: form.supplierId,
      remark: form.remark,
      status: 0,
      orderNo: '',
      totalAmount: totalAmount.value,
      items: form.items.map(item => ({
        productId: item.productId,
        productName: item.productName,
        quantity: item.quantity,
        purchasePrice: item.purchasePrice
      }))
    } as PurchaseOrder)
    if (res.code === 200) { ElMessage.success('新增成功'); dialogVisible.value = false; getList() }
    else ElMessage.error(res.msg)
  } finally { saving.value = false }
}

const handle入库 = async (id: number) => {
  await ElMessageBox.confirm('确认入库后，商品库存和成本价将被更新？', '提示', { type: 'warning', confirmButtonText: '确认入库', cancelButtonText: '取消' })
  const res = await purchaseStockIn(id)
  if (res.code === 200) { ElMessage.success('入库成功'); getList() }
  else ElMessage.error(res.msg)
}

const handleDelete = async (id: number) => {
  await ElMessageBox.confirm('确认删除该采购单？', '提示', { type: 'warning', confirmButtonText: '确认删除', cancelButtonText: '取消' })
  const res = await deletePurchase(id)
  if (res.code === 200) { ElMessage.success('删除成功'); getList() }
}

onMounted(() => getList())
</script>