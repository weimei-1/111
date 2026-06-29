<template>
  <div>
    <div class="page-title">供应商管理</div>

    <el-card shadow="hover" class="search-card">
      <el-form :inline="true" size="default">
        <el-form-item label="供应商名称">
          <el-input v-model="queryName" placeholder="搜索供应商" clearable @keyup.enter="search" style="width:200px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" native-type="button" @click="search">查询</el-button>
          <el-button native-type="button" @click="resetQuery">重置</el-button>
          <el-button type="primary" native-type="button" @click="openAdd" plain>+ 新增供应商</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="hover" class="page-card">
      <el-table :data="tableData" border stripe style="width:100%" size="default">
        <el-table-column type="index" label="序号" width="70" align="center" />
        <el-table-column prop="name" label="供应商名称" min-width="140" />
        <el-table-column prop="contactPerson" label="联系人" width="100" />
        <el-table-column prop="phone" label="联系电话" width="130" />
        <el-table-column prop="address" label="地址" min-width="180" show-overflow-tooltip />
        <el-table-column prop="remark" label="备注" min-width="140" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="140" align="center">
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
                {{ row.status === 1 ? '启用' : '禁用' }}
              </el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160" />
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="openEdit(row)">编辑</el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑供应商' : '新增供应商'" width="550px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="供应商名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入供应商名称" />
        </el-form-item>
        <el-form-item label="联系人" prop="contactPerson">
          <el-input v-model="form.contactPerson" placeholder="请输入联系人" />
        </el-form-item>
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="地址" prop="address">
          <el-input v-model="form.address" placeholder="请输入地址" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" placeholder="请输入备注" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
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
import { getSupplierList, addSupplier, updateSupplier, deleteSupplier } from '@/api'
import type { Supplier } from '@/types'

const tableData = ref<Supplier[]>([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const saving = ref(false)
const formRef = ref()
const queryName = ref('')
const form = reactive({ id: 0, name: '', contactPerson: '', phone: '', address: '', remark: '', status: 1 })
const rules = {
  name: [{ required: true, message: '请输入供应商名称', trigger: 'blur' }]
}

const getList = async (keyword?: string) => {
  const params: Record<string, unknown> = {}
  if (keyword) params.keyword = keyword
  const res = await getSupplierList(params)
  if (res.code === 200) tableData.value = res.data
}

const search = () => { getList(queryName.value) }
const resetQuery = () => { queryName.value = ''; getList() }

const openAdd = () => {
  isEdit.value = false
  Object.assign(form, { id: 0, name: '', contactPerson: '', phone: '', address: '', remark: '', status: 1 })
  dialogVisible.value = true
}

const openEdit = (row: Supplier) => {
  isEdit.value = true
  Object.assign(form, { id: row.id, name: row.name, contactPerson: row.contactPerson, phone: row.phone, address: row.address, remark: row.remark, status: row.status })
  dialogVisible.value = true
}

const handleSave = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    const res = isEdit.value ? await updateSupplier(form) : await addSupplier(form)
    if (res.code === 200) { ElMessage.success(isEdit.value ? '修改成功' : '新增成功'); dialogVisible.value = false; getList(queryName.value) }
    else ElMessage.error(res.msg)
  } finally { saving.value = false }
}

const handleDelete = async (id: number) => {
  await ElMessageBox.confirm('确认删除该供应商？', '提示', { type: 'warning', confirmButtonText: '确认删除', cancelButtonText: '取消' })
  const res = await deleteSupplier(id)
  if (res.code === 200) { ElMessage.success('删除成功'); getList(queryName.value) }
}

const toggleStatus = async (row: Supplier, val: number) => {
  const res = await updateSupplier({ id: row.id, name: row.name, status: val } as Supplier)
  if (res.code === 200) {
    ElMessage.success(val === 1 ? '已启用' : '已禁用')
  } else {
    ElMessage.error(res.msg || '操作失败')
    getList(queryName.value)
  }
}

onMounted(() => getList())
</script>