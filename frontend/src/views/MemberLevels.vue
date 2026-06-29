<template>
  <div>
    <div class="page-title">会员等级管理</div>

    <el-card shadow="hover" class="search-card">
      <el-form :inline="true" size="default">
        <el-form-item label="等级名称">
          <el-input v-model="queryName" placeholder="搜索等级名称" clearable @keyup.enter="search" style="width:200px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" native-type="button" @click="search">查询</el-button>
          <el-button native-type="button" @click="resetQuery">重置</el-button>
          <el-button type="primary" native-type="button" @click="openAdd" plain>+ 新增等级</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="hover" class="page-card">
      <el-table :data="tableData" border stripe style="width:100%" size="default">
        <el-table-column type="index" label="序号" width="70" align="center" />
        <el-table-column prop="name" label="等级名称" min-width="120" />
        <el-table-column label="升级门槛(元)" width="130" align="right">
          <template #default="{ row }">¥{{ row.minSpent }}</template>
        </el-table-column>
        <el-table-column label="折扣比例" width="110" align="center">
          <template #default="{ row }">{{ (row.discountRate * 100).toFixed(0) }}%</template>
        </el-table-column>
        <el-table-column label="积分倍率" width="100" align="center">
          <template #default="{ row }">{{ row.pointsMultiplier }}x</template>
        </el-table-column>
        <el-table-column prop="sort" label="排序" width="80" align="center" />
        <el-table-column label="状态" width="140" align="center">
          <template #default="{ row }">
            <div style="display:flex;align-items:center;gap:6px;justify-content:center">
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
            <el-button type="danger" link size="small" @click="handleDelete(row.id!)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑会员等级' : '新增会员等级'" width="520px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="等级名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入等级名称" />
        </el-form-item>
        <el-form-item label="升级门槛" prop="minSpent">
          <el-input-number v-model="form.minSpent" :precision="2" :min="0" :step="100" style="width:100%" />
        </el-form-item>
        <el-form-item label="折扣比例" prop="discountRate">
          <el-input-number v-model="form.discountRate" :precision="2" :step="0.05" :min="0" :max="1" style="width:100%" />
        </el-form-item>
        <el-form-item label="积分倍率" prop="pointsMultiplier">
          <el-input-number v-model="form.pointsMultiplier" :precision="1" :step="0.1" :min="0" style="width:100%" />
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
import { getMemberLevelList, addMemberLevel, updateMemberLevel, deleteMemberLevel } from '@/api'
import type { MemberLevel } from '@/types'

const tableData = ref<MemberLevel[]>([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const saving = ref(false)
const formRef = ref()
const queryName = ref('')
const form = reactive({ id: 0, name: '', minSpent: 0, discountRate: 0, pointsMultiplier: 0, sort: 0, status: 1 })
const rules = {
  name: [{ required: true, message: '请输入等级名称', trigger: 'blur' }]
}

const getList = async (name?: string) => {
  const params: Record<string, unknown> = {}
  if (name) params.name = name
  const res = await getMemberLevelList(params)
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
  Object.assign(form, { id: 0, name: '', minSpent: 0, discountRate: 0, pointsMultiplier: 0, sort: 0, status: 1 })
  dialogVisible.value = true
}

const openEdit = (row: MemberLevel) => {
  isEdit.value = true
  Object.assign(form, { id: row.id, name: row.name, minSpent: row.minSpent, discountRate: row.discountRate, pointsMultiplier: row.pointsMultiplier, sort: row.sort, status: row.status })
  dialogVisible.value = true
}

const handleSave = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    const res = isEdit.value ? await updateMemberLevel(form) : await addMemberLevel(form)
    if (res.code === 200) { ElMessage.success(isEdit.value ? '修改成功' : '新增成功'); dialogVisible.value = false; getList(queryName.value) }
    else ElMessage.error(res.msg)
  } finally { saving.value = false }
}

const handleDelete = async (id: number) => {
  await ElMessageBox.confirm('确认删除该会员等级？', '提示', { type: 'warning', confirmButtonText: '确认删除', cancelButtonText: '取消' })
  const res = await deleteMemberLevel(id)
  if (res.code === 200) { ElMessage.success('删除成功'); getList(queryName.value) }
}

const toggleStatus = async (row: any, val: number) => {
  const res = await updateMemberLevel({ id: row.id, status: val })
  if (res.code === 200) {
    ElMessage.success(val === 1 ? '已启用' : '已禁用')
  } else {
    ElMessage.error(res.msg || '操作失败')
    getList(queryName.value)
  }
}

onMounted(() => getList())
</script>