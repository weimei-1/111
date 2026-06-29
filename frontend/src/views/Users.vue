<template>
  <div>
    <div class="page-title">用户管理</div>

    <el-card shadow="hover" class="search-card">
      <el-form :inline="true" size="default">
        <el-form-item label="用户名">
          <el-input v-model="query.keyword" placeholder="搜索用户名" clearable @keyup.enter="search" style="width:180px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" native-type="button" @click="search">查询</el-button>
          <el-button native-type="button" @click="resetQuery">重置</el-button>
          <el-button type="primary" native-type="button" @click="openAdd" plain>+ 新增用户</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="hover" class="page-card">
      <el-table :data="tableData" border stripe style="width:100%" size="default">
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="realName" label="真实姓名" width="120" />
        <el-table-column prop="role" label="角色" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.role === 'ADMIN' ? 'danger' : 'warning'" effect="dark" size="small">
              {{ row.role === 'ADMIN' ? '管理员' : '收银员' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="手机号" width="140" />
        <el-table-column prop="status" label="状态" width="110" align="center">
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
        <el-table-column prop="updateTime" label="更新时间" width="160" />
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

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑用户' : '新增用户'" width="480px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="密码" prop="password" v-if="!isEdit">
          <el-input v-model="form.password" type="password" show-password />
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="form.realName" />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="form.role" style="width:100%">
            <el-option label="管理员" value="ADMIN" />
            <el-option label="收银员" value="CASHIER" />
          </el-select>
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" maxlength="11" />
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
import { getUserList, addUser, updateUser, deleteUser } from '@/api'

const query = reactive({ page: 1, pageSize: 10, keyword: '' })
const total = ref(0)
const tableData = ref<any[]>([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const saving = ref(false)
const formRef = ref()
const form = reactive({ id: 0, username: '', password: '', realName: '', role: 'CASHIER', phone: '', status: 1 })
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  role: [{ required: true, message: '请选择角色', trigger: 'change' }]
}

const getList = async () => {
  const res = await getUserList(query)
  if (res.code === 200) { tableData.value = res.data.list; total.value = res.data.total }
}

const search = () => { query.page = 1; getList() }
const resetQuery = () => { query.keyword = ''; query.page = 1; getList() }
const openAdd = () => {
  isEdit.value = false
  Object.assign(form, { id: 0, username: '', password: '', realName: '', role: 'CASHIER', phone: '', status: 1 })
  dialogVisible.value = true
}
const openEdit = (row: any) => {
  isEdit.value = true
  Object.assign(form, { id: row.id, username: row.username, password: '', realName: row.realName, role: row.role, phone: row.phone, status: row.status })
  dialogVisible.value = true
}
const handleSave = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    const res = isEdit.value ? await updateUser(form) : await addUser(form)
    if (res.code === 200) { ElMessage.success(isEdit.value ? '修改成功' : '新增成功'); dialogVisible.value = false; getList() }
    else ElMessage.error(res.msg)
  } finally { saving.value = false }
}
const handleDelete = async (id: number) => {
  await ElMessageBox.confirm('确认删除该用户？', '提示', { type: 'warning', confirmButtonText: '确认删除', cancelButtonText: '取消' })
  const res = await deleteUser(id)
  if (res.code === 200) { ElMessage.success('删除成功'); getList() }
}

const toggleStatus = async (row: any, val: number) => {
  const res = await updateUser({ id: row.id, status: val })
  if (res.code === 200) {
    ElMessage.success(val === 1 ? '已启用' : '已禁用')
  } else {
    ElMessage.error(res.msg || '操作失败')
    getList()
  }
}

onMounted(getList)
</script>