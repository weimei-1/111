<template>
  <div>
    <div class="page-title">商品管理</div>

    <el-card shadow="hover" class="search-card">
      <el-form :inline="true" :model="query" size="default">
        <el-form-item label="商品名称">
          <el-input v-model="query.name" placeholder="搜索" clearable @keyup.enter="search" style="width:160px" />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="query.categoryId" placeholder="全部" clearable style="width:140px">
            <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部" clearable style="width:100px">
            <el-option label="上架" :value="1" />
            <el-option label="下架" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" native-type="button" @click="search">查询</el-button>
          <el-button native-type="button" @click="resetQuery">重置</el-button>
          <el-button type="primary" native-type="button" @click="openAdd" plain>+ 新增商品</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="hover" class="page-card">
      <el-table :data="tableData" border stripe style="width:100%" size="default" max-height="500">
        <el-table-column prop="name" label="商品名称" min-width="130" />
        <el-table-column label="条码" width="140">
          <template #default="{ row }">
            <span style="white-space:nowrap">{{ row.barcode }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="categoryName" label="分类" width="90" />
        <el-table-column label="进价" width="90" align="right">
          <template #default="{ row }">¥{{ row.costPrice }}</template>
        </el-table-column>
        <el-table-column label="售价" width="90" align="right">
          <template #default="{ row }">¥{{ row.price }}</template>
        </el-table-column>
        <el-table-column label="库存" width="70" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.stock <= row.warningStock" type="danger" effect="dark" size="small">{{ row.stock }}</el-tag>
            <span v-else>{{ row.stock }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="warningStock" label="预警" width="60" align="center" />
        <el-table-column prop="unit" label="单位" width="55" align="center" />
        <el-table-column prop="status" label="状态" width="90" align="center">
          <template #default="{ row }">
            <div style="display:flex;align-items:center;gap:6px">
              <el-switch
                v-model="row.status"
                :active-value="1"
                :inactive-value="0"
                @change="(val: number) => toggleStatus(row, val)"
                size="small"
              />
              <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small" effect="dark">
                {{ row.status === 1 ? '上架' : '下架' }}
              </el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="145" />
        <el-table-column label="操作" width="130" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="openEdit(row)">编辑</el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row.id)">删除</el-button>
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

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑商品' : '新增商品'" width="520px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="商品名称" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="条码" prop="barcode">
          <el-input v-model="form.barcode" />
        </el-form-item>
        <el-form-item label="分类" prop="categoryId">
          <el-select v-model="form.categoryId" style="width:100%">
            <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="进价" prop="costPrice">
              <el-input-number v-model="form.costPrice" :precision="2" :min="0" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="售价" prop="price">
              <el-input-number v-model="form.price" :precision="2" :min="0" style="width:100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="库存" prop="stock">
              <el-input-number v-model="form.stock" :min="0" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="预警库存" prop="warningStock">
              <el-input-number v-model="form.warningStock" :min="0" style="width:100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="单位" prop="unit">
          <el-input v-model="form.unit" placeholder="如：斤/个/箱" />
        </el-form-item>
        <el-form-item label="图片" prop="image">
          <el-input v-model="form.image" placeholder="商品图片URL（可选）" clearable />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">上架</el-radio>
            <el-radio :value="0">下架</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave" :loading="saving">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getProductList, addProduct, updateProduct, deleteProduct, getCategoryList } from '@/api'
import type { Category } from '@/types'

const query = reactive({ page: 1, pageSize: 10, name: '', categoryId: undefined as number | undefined, status: undefined as number | undefined })
const total = ref(0)
const tableData = ref<any[]>([])
const categories = ref<Category[]>([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const saving = ref(false)
const formRef = ref()
const form = reactive({ id: 0, name: '', barcode: '', categoryId: undefined as number | undefined, costPrice: 0, price: 0, stock: 0, warningStock: 0, unit: '', image: '', status: 1 })
const rules = {
  name: [{ required: true, message: '请输入名称', trigger: 'blur' }],
  barcode: [{ required: true, message: '请输入条码', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  price: [{ required: true, message: '请输入售价', trigger: 'blur' }]
}

const getList = async () => {
  const res = await getProductList(query)
  if (res.code === 200) { tableData.value = res.data.list; total.value = res.data.total }
}

const getCategories = async () => {
  const res = await getCategoryList()
  if (res.code === 200) categories.value = res.data
}

const search = () => { query.page = 1; getList() }
const resetQuery = () => { Object.assign(query, { page: 1, name: '', categoryId: undefined, status: undefined }); getList() }
const openAdd = () => {
  isEdit.value = false
  Object.assign(form, { id: 0, name: '', barcode: '', categoryId: undefined, costPrice: 0, price: 0, stock: 0, warningStock: 0, unit: '', image: '', status: 1 })
  dialogVisible.value = true
}
const openEdit = (row: any) => {
  isEdit.value = true
  Object.assign(form, { id: row.id, name: row.name, barcode: row.barcode, categoryId: row.categoryId, costPrice: row.costPrice, price: row.price, stock: row.stock, warningStock: row.warningStock, unit: row.unit, image: row.image || '', status: row.status })
  dialogVisible.value = true
}
const handleSave = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    const res = isEdit.value ? await updateProduct(form) : await addProduct(form)
    if (res.code === 200) { ElMessage.success(isEdit.value ? '修改成功' : '新增成功'); dialogVisible.value = false; getList() }
    else ElMessage.error(res.msg)
  } finally { saving.value = false }
}
const handleDelete = async (id: number) => {
  await ElMessageBox.confirm('确认删除该商品？', '提示', { type: 'warning', confirmButtonText: '确认删除', cancelButtonText: '取消' })
  const res = await deleteProduct(id)
  if (res.code === 200) { ElMessage.success('删除成功'); getList() }
}

const toggleStatus = async (row: any, val: number) => {
  const res = await updateProduct({ id: row.id, status: val })
  if (res.code === 200) {
    ElMessage.success(val === 1 ? '已上架' : '已下架')
  } else {
    ElMessage.error(res.msg || '操作失败')
    getList()
  }
}

onMounted(() => { getCategories(); getList() })
</script>