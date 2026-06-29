<template>
  <div>
    <div class="page-title">促销活动管理</div>

    <el-card shadow="hover" class="search-card">
      <el-form :inline="true" size="default">
        <el-form-item label="活动名称">
          <el-input v-model="queryName" placeholder="搜索活动名称" clearable @keyup.enter="search" style="width:200px" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryStatus" placeholder="全部" clearable style="width:120px">
            <el-option label="已启用" :value="1" />
            <el-option label="未启用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" native-type="button" @click="search">查询</el-button>
          <el-button native-type="button" @click="resetQuery">重置</el-button>
          <el-button type="primary" native-type="button" @click="openAdd" plain>+ 新增活动</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="hover" class="page-card">
      <el-table :data="tableData" border stripe style="width:100%" size="default">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="name" label="活动名称" min-width="160" />
        <el-table-column prop="type" label="类型" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="row.type === 'full_discount' ? 'success' : row.type === 'discount' ? 'warning' : 'primary'" size="small">
              {{ row.type === 'full_discount' ? '满减' : row.type === 'discount' ? '折扣' : '会员特价' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="startTime" label="开始时间" width="160" />
        <el-table-column prop="endTime" label="结束时间" width="160" />
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
                {{ row.status === 1 ? '已启用' : '未启用' }}
              </el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="openEdit(row)">编辑</el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑活动' : '新增活动'" width="600px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="活动名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入活动名称" />
        </el-form-item>
        <el-form-item label="活动类型" prop="type">
          <el-select v-model="form.type" placeholder="请选择类型" style="width:100%">
            <el-option label="满减活动" value="full_discount" />
            <el-option label="折扣活动" value="discount" />
            <el-option label="会员特价" value="member_discount" />
          </el-select>
        </el-form-item>
        <el-form-item label="开始时间" prop="startTime">
          <el-date-picker v-model="form.startTime" type="datetime" placeholder="选择开始时间" style="width:100%" value-format="YYYY-MM-DD HH:mm:ss" />
        </el-form-item>
        <el-form-item label="结束时间" prop="endTime">
          <el-date-picker v-model="form.endTime" type="datetime" placeholder="选择结束时间" style="width:100%" value-format="YYYY-MM-DD HH:mm:ss" />
        </el-form-item>
        <el-form-item label="规则配置">
          <el-input v-model="form.ruleConfig" placeholder='规则JSON，如 {"minAmount":100,"discountAmount":10}' type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" placeholder="备注" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">已启用</el-radio>
            <el-radio :value="0">未启用</el-radio>
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
import { getPromotionList, addPromotion, updatePromotion, deletePromotion } from '@/api'
import type { Promotion } from '@/types'

const tableData = ref<Promotion[]>([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const saving = ref(false)
const formRef = ref()
const queryName = ref('')
const queryStatus = ref<number | undefined>(undefined)
const form = reactive({ id: 0, name: '', type: '', startTime: '', endTime: '', ruleConfig: '', remark: '', status: 1 })
const rules = {
  name: [{ required: true, message: '请输入活动名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择活动类型', trigger: 'change' }],
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  endTime: [{ required: true, message: '请选择结束时间', trigger: 'change' }]
}

const getList = async () => {
  const params: Record<string, unknown> = {}
  if (queryName.value) params.keyword = queryName.value
  if (queryStatus.value !== undefined) params.status = queryStatus.value
  const res = await getPromotionList(params)
  if (res.code === 200) tableData.value = res.data
}

const search = () => { getList() }
const resetQuery = () => { queryName.value = ''; queryStatus.value = undefined; getList() }

const openAdd = () => {
  isEdit.value = false
  Object.assign(form, { id: 0, name: '', type: '', startTime: '', endTime: '', ruleConfig: '', remark: '', status: 1 })
  dialogVisible.value = true
}

const openEdit = (row: Promotion) => {
  isEdit.value = true
  Object.assign(form, {
    id: row.id, name: row.name, type: row.type,
    startTime: row.startTime, endTime: row.endTime,
    ruleConfig: row.ruleConfig, remark: row.remark, status: row.status
  })
  dialogVisible.value = true
}

const handleSave = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    const res = isEdit.value ? await updatePromotion(form) : await addPromotion(form)
    if (res.code === 200) { ElMessage.success(isEdit.value ? '修改成功' : '新增成功'); dialogVisible.value = false; getList() }
    else ElMessage.error(res.msg)
  } finally { saving.value = false }
}

const handleDelete = async (id: number) => {
  await ElMessageBox.confirm('确认删除该促销活动？', '提示', { type: 'warning', confirmButtonText: '确认删除', cancelButtonText: '取消' })
  const res = await deletePromotion(id)
  if (res.code === 200) { ElMessage.success('删除成功'); getList() }
}

const toggleStatus = async (row: Promotion, val: number) => {
  const res = await updatePromotion({ id: row.id, name: row.name, type: row.type, startTime: row.startTime, endTime: row.endTime, status: val } as Promotion)
  if (res.code === 200) {
    ElMessage.success(val === 1 ? '已启用' : '已禁用')
  } else {
    ElMessage.error(res.msg || '操作失败')
    getList()
  }
}

onMounted(() => getList())
</script>