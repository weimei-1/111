<template>
  <div>
    <div class="page-title">会员管理</div>

    <el-card shadow="hover" class="search-card">
      <el-form :inline="true" size="default">
        <el-form-item label="手机号">
          <el-input v-model="query.keyword" placeholder="搜索姓名/手机号" clearable @keyup.enter="search" style="width:180px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" native-type="button" @click="search">查询</el-button>
          <el-button native-type="button" @click="resetQuery">重置</el-button>
          <el-button type="success" native-type="button" @click="openAdd" plain>+ 新增会员</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="hover" class="page-card">
      <el-table :data="tableData" border stripe style="width:100%" size="default">
        <el-table-column prop="name" label="姓名" width="120" />
        <el-table-column prop="phone" label="手机号" width="150" />
        <el-table-column prop="points" label="积分" width="100" align="center" />
        <el-table-column prop="level" label="等级" width="120" align="center" />
        <el-table-column prop="createTime" label="注册时间" width="160" />
        <el-table-column label="操作" width="160" fixed="right">
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

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑会员' : '新增会员'" width="450px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="姓名" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" maxlength="11" />
        </el-form-item>
        <el-form-item label="积分" prop="points">
          <el-input-number v-model="form.points" :min="0" style="width:100%" />
        </el-form-item>
        <el-form-item label="等级" prop="level">
          <el-select v-model="form.level" style="width:100%">
            <el-option label="普通会员" value="普通会员" />
            <el-option label="银卡会员" value="银卡会员" />
            <el-option label="金卡会员" value="金卡会员" />
            <el-option label="钻石会员" value="钻石会员" />
          </el-select>
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
import { getMemberList, addMember, updateMember, deleteMember } from '@/api'

const query = reactive({ page: 1, pageSize: 10, keyword: '' })
const total = ref(0)
const tableData = ref<any[]>([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const saving = ref(false)
const formRef = ref()
const form = reactive({ id: 0, name: '', phone: '', points: 0, level: '' })
const rules = {
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }]
}

const getList = async () => {
  const res = await getMemberList(query)
  if (res.code === 200) { tableData.value = res.data.list; total.value = res.data.total }
}

const search = () => { query.page = 1; getList() }
const resetQuery = () => { query.keyword = ''; query.page = 1; getList() }
const openAdd = () => {
  isEdit.value = false
  Object.assign(form, { id: 0, name: '', phone: '', points: 0, level: '' })
  dialogVisible.value = true
}
const openEdit = (row: any) => {
  isEdit.value = true
  Object.assign(form, { id: row.id, name: row.name, phone: row.phone, points: row.points, level: row.level })
  dialogVisible.value = true
}
const handleSave = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    const res = isEdit.value ? await updateMember(form) : await addMember(form)
    if (res.code === 200) { ElMessage.success(isEdit.value ? '修改成功' : '新增成功'); dialogVisible.value = false; getList() }
    else ElMessage.error(res.msg)
  } finally { saving.value = false }
}
const handleDelete = async (id: number) => {
  await ElMessageBox.confirm('确认删除该会员？', '提示', { type: 'warning', confirmButtonText: '确认删除', cancelButtonText: '取消' })
  const res = await deleteMember(id)
  if (res.code === 200) { ElMessage.success('删除成功'); getList() }
}

onMounted(getList)
</script>