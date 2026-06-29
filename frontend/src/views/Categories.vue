<template>
  <div>
    <div class="page-title">分类管理</div>

    <el-card shadow="hover" class="search-card">
      <el-form :inline="true" size="default">
        <el-form-item label="分类名称">
          <el-input v-model="queryName" placeholder="搜索分类名称" clearable @keyup.enter="search" style="width:200px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" native-type="button" @click="search">查询</el-button>
          <el-button native-type="button" @click="resetQuery">重置</el-button>
          <el-button type="primary" native-type="button" @click="openAdd" plain>+ 新增分类</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="hover" class="page-card">
      <el-table :data="tableData" border stripe style="width:100%" size="default">
        <el-table-column type="index" label="序号" width="70" align="center" />
        <el-table-column prop="name" label="分类名称" min-width="160" />
        <el-table-column prop="sort" label="排序" width="100" align="center" />
        <el-table-column prop="status" label="状态" width="120" align="center">
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
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="openEdit(row)">编辑</el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑分类' : '新增分类'" width="420px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入分类名称" />
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="form.sort" :min="0" style="width:100%" />
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
import { getCategoryList, addCategory, updateCategory, deleteCategory } from '@/api'
import type { Category } from '@/types'

const tableData = ref<Category[]>([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const saving = ref(false)
const formRef = ref()
const queryName = ref('')
const form = reactive({ id: 0, name: '', sort: 0, status: 1 })
const rules = {
  name: [{ required: true, message: '请输入分类名称', trigger: 'blur' }]
}

const getList = async (name?: string) => {
  const params: Record<string, unknown> = {}
  if (name) params.name = name
  const res = await getCategoryList(params)
  if (res.code === 200) tableData.value = res.data
}

const search = () => {
  getList(queryName.value)
}

const resetQuery = () => {
  queryName.value = ''
  getList()
}

const openAdd = () => {
  isEdit.value = false
  Object.assign(form, { id: 0, name: '', sort: 0, status: 1 })
  dialogVisible.value = true
}

const openEdit = (row: Category) => {
  isEdit.value = true
  Object.assign(form, { id: row.id, name: row.name, sort: row.sort, status: row.status })
  dialogVisible.value = true
}

const handleSave = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    const res = isEdit.value ? await updateCategory(form) : await addCategory(form)
    if (res.code === 200) { ElMessage.success(isEdit.value ? '修改成功' : '新增成功'); dialogVisible.value = false; getList(queryName.value) }
    else ElMessage.error(res.msg)
  } finally { saving.value = false }
}

const handleDelete = async (id: number) => {
  await ElMessageBox.confirm('确认删除该分类？', '提示', { type: 'warning', confirmButtonText: '确认删除', cancelButtonText: '取消' })
  const res = await deleteCategory(id)
  if (res.code === 200) { ElMessage.success('删除成功'); getList(queryName.value) }
}

const toggleStatus = async (row: any, val: number) => {
  const res = await updateCategory({ id: row.id, status: val })
  if (res.code === 200) {
    ElMessage.success(val === 1 ? '已启用' : '已禁用')
  } else {
    ElMessage.error(res.msg || '操作失败')
    getList(queryName.value)
  }
}

onMounted(() => getList())
</script>

<style scoped>
.toolbar-right {
  text-align: right;
}
</style>