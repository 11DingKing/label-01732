<template>
  <div class="page-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div>
        <h2 class="page-title">
          <el-icon><UserFilled /></el-icon>
          用户管理
        </h2>
        <p class="page-subtitle">管理系统用户账号和权限</p>
      </div>
    </div>
    
    <!-- 搜索区域 -->
    <div class="search-card">
      <el-form :model="searchForm" inline class="search-form">
        <el-form-item label="账号">
          <el-input
            v-model="searchForm.username"
            placeholder="请输入账号"
            clearable
            style="width: 160px;"
          />
        </el-form-item>
        <el-form-item label="姓名">
          <el-input
            v-model="searchForm.realName"
            placeholder="请输入姓名"
            clearable
            style="width: 160px;"
          />
        </el-form-item>
        <el-form-item label="角色">
          <el-select
            v-model="searchForm.role"
            placeholder="全部角色"
            clearable
            style="width: 120px;"
          >
            <el-option label="管理员" value="admin" />
            <el-option label="操作员" value="operator" />
          </el-select>
        </el-form-item>
        <el-form-item class="search-actions">
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><RefreshRight /></el-icon>
            重置
          </el-button>
          <el-button type="success" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            添加用户
          </el-button>
        </el-form-item>
      </el-form>
    </div>
    
    <!-- 表格区域 -->
    <div class="table-card">
      <div class="table-header">
        <div class="table-title">
          用户列表
          <el-tag type="info" size="small" effect="plain">{{ pagination.total }} 人</el-tag>
        </div>
      </div>
      
      <el-table
        v-loading="loading"
        :data="tableData"
        border
        stripe
        class="data-table"
        style="width: 100%"
      >
        <el-table-column prop="username" label="账号" min-width="140">
          <template #default="{ row }">
            <div class="user-cell">
              <el-avatar :size="32" class="user-avatar">
                {{ row.realName?.charAt(0) }}
              </el-avatar>
              <span>{{ row.username }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="realName" label="姓名" min-width="120" />
        <el-table-column prop="roleName" label="角色" min-width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="row.role === 'admin' ? 'danger' : 'primary'" effect="light">
              {{ row.roleName }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="statusName" label="状态" min-width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 0 ? 'success' : 'info'" effect="light">
              {{ row.statusName }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" min-width="180" />
        <el-table-column label="操作" fixed="right" width="160" align="center">
          <template #default="{ row }">
            <div class="table-action-btns">
              <button class="table-action-btn btn-edit" @click="handleEdit(row)">
                <el-icon><Edit /></el-icon>
                编辑
              </button>
              <button
                class="table-action-btn btn-delete"
                :disabled="row.username === 'admin'"
                @click="handleDelete(row)"
              >
                <el-icon><Delete /></el-icon>
                删除
              </button>
            </div>
          </template>
        </el-table-column>
      </el-table>
      
      <div class="table-footer">
        <el-pagination
          v-model:current-page="pagination.pageNum"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>
    
    <!-- 添加/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogType === 'add' ? '添加用户' : '编辑用户'"
      width="500px"
      :close-on-click-modal="false"
      destroy-on-close
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="80px"
      >
        <el-form-item label="账号" prop="username">
          <el-input
            v-model="form.username"
            placeholder="请输入账号"
            :disabled="dialogType === 'edit'"
          />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="form.password"
            type="password"
            :placeholder="dialogType === 'add' ? '请输入密码' : '留空则不修改密码'"
            show-password
          />
        </el-form-item>
        <el-form-item label="姓名" prop="realName">
          <el-input v-model="form.realName" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-radio-group v-model="form.role">
            <el-radio label="admin">管理员</el-radio>
            <el-radio label="operator">操作员</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-switch
            v-model="form.status"
            :active-value="0"
            :inactive-value="1"
            active-text="启用"
            inactive-text="禁用"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getUserList, addUser, updateUser, deleteUser } from '@/api/user'
import { showSuccess, showError } from '@/utils/toast'

const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref([])
const dialogVisible = ref(false)
const dialogType = ref('add')
const formRef = ref(null)

const searchForm = reactive({
  username: '',
  realName: '',
  role: '',
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0,
})

const form = reactive({
  id: null,
  username: '',
  password: '',
  realName: '',
  role: 'operator',
  status: 0,
})

const rules = computed(() => ({
  username: [
    { required: true, message: '请输入账号', trigger: 'blur' },
    { min: 3, max: 20, message: '账号长度为3-20个字符', trigger: 'blur' },
  ],
  password: dialogType.value === 'add' 
    ? [{ required: true, message: '请输入密码', trigger: 'blur' }, { min: 6, max: 20, message: '密码长度为6-20个字符', trigger: 'blur' }]
    : [{ min: 6, max: 20, message: '密码长度为6-20个字符', trigger: 'blur' }],
  realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  role: [{ required: true, message: '请选择角色', trigger: 'change' }],
}))

const fetchData = async (silent = false) => {
  try {
    loading.value = true
    const res = await getUserList({ ...searchForm, pageNum: pagination.pageNum, pageSize: pagination.pageSize })
    tableData.value = res.data.records
    pagination.total = res.data.total
    return true
  } catch (error) {
    console.error('获取数据失败:', error)
    return false
  } finally {
    loading.value = false
  }
}

const handleSearch = async () => {
  pagination.pageNum = 1
  const ok = await fetchData(true)
  ok ? showSuccess('搜索完成') : showError('搜索失败')
}

const handleReset = async () => {
  searchForm.username = ''
  searchForm.realName = ''
  searchForm.role = ''
  pagination.pageNum = 1
  const ok = await fetchData(true)
  ok ? showSuccess('已重置筛选条件') : showError('重置失败')
}
const handleSizeChange = () => { pagination.pageNum = 1; fetchData() }
const handleCurrentChange = () => { fetchData() }

const resetForm = () => {
  form.id = null; form.username = ''; form.password = ''; form.realName = ''; form.role = 'operator'; form.status = 0
}

const handleAdd = () => { resetForm(); dialogType.value = 'add'; dialogVisible.value = true }
const handleEdit = (row) => {
  resetForm(); dialogType.value = 'edit'
  form.id = row.id; form.username = row.username; form.realName = row.realName; form.role = row.role; form.status = row.status
  dialogVisible.value = true
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    submitLoading.value = true
    if (dialogType.value === 'add') {
      await addUser(form)
      ElMessage.success('添加成功')
    } else {
      const updateData = { ...form }
      if (!updateData.password) delete updateData.password
      await updateUser(updateData)
      ElMessage.success('修改成功')
    }
    dialogVisible.value = false
    fetchData()
  } catch (error) {
    console.error('提交失败:', error)
  } finally {
    submitLoading.value = false
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要删除用户 ${row.realName}（${row.username}）吗？`, '确认删除', { type: 'warning' })
    await deleteUser(row.id)
    ElMessage.success('删除成功')
    fetchData()
  } catch (error) {
    if (error !== 'cancel') console.error('删除失败:', error)
  }
}

onMounted(() => { fetchData() })
</script>

<style lang="scss" scoped>
@import '@/assets/styles/variables.scss';

.page-subtitle {
  margin-top: $spacing-xs;
  font-size: $font-size-sm;
  color: $text-secondary;
}

.table-title {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
}

.user-cell {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  
  .user-avatar {
    background: linear-gradient(135deg, $primary-color 0%, $primary-dark 100%);
    color: #fff;
    font-size: 12px;
    flex-shrink: 0;
  }
}

.data-table {
  :deep(.el-table__header th) {
    background-color: $fill-lighter !important;
  }
}
</style>
