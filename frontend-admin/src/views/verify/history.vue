<template>
  <div class="page-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div>
        <h2 class="page-title">
          <el-icon><Document /></el-icon>
          核销记录
        </h2>
        <p class="page-subtitle">查看卡密核销历史记录</p>
      </div>
    </div>
    
    <!-- 搜索区域 -->
    <div class="search-card">
      <el-form :model="searchForm" inline class="search-form">
        <el-form-item label="卡号">
          <el-input
            v-model="searchForm.cardNumber"
            placeholder="请输入卡号"
            clearable
            style="width: 160px;"
          />
        </el-form-item>
        <el-form-item label="批次号">
          <el-select
            v-model="searchForm.batchNumber"
            placeholder="全部批次"
            clearable
            filterable
            style="width: 200px;"
          >
            <el-option
              v-for="item in batchNumbers"
              :key="item"
              :label="item"
              :value="item"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="核销日期">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            style="width: 260px;"
            @change="handleDateChange"
          />
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
        </el-form-item>
      </el-form>
    </div>
    
    <!-- 表格区域 -->
    <div class="table-card">
      <div class="table-header">
        <div class="table-title">
          核销记录
          <el-tag type="info" size="small" effect="plain">{{ pagination.total }} 条</el-tag>
        </div>
        <div class="table-actions">
          <el-button type="success" :loading="exporting" @click="handleExport">
            <el-icon><Download /></el-icon>
            导出Excel
          </el-button>
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
        <el-table-column prop="cardNumber" label="卡号" min-width="140">
          <template #default="{ row }">
            <span class="mono-text">{{ row.cardNumber }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="batchNumber" label="批次号" min-width="160">
          <template #default="{ row }">
            <span class="mono-text">{{ row.batchNumber }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="useTime" label="核销时间" min-width="180">
          <template #default="{ row }">
            <div class="time-cell">
              <el-icon><Clock /></el-icon>
              <span>{{ row.useTime }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="useOperatorName" label="操作员" min-width="120" align="center">
          <template #default="{ row }">
            <el-tag effect="plain" size="small" class="operator-tag">{{ row.useOperatorName }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="生成时间" min-width="180" />
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
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getVerifyHistory, exportVerifyHistory } from '@/api/verify'
import { getBatchNumbers } from '@/api/card'
import { ElMessage } from 'element-plus'
import { showSuccess, showError } from '@/utils/toast'

const loading = ref(false)
const exporting = ref(false)
const tableData = ref([])
const batchNumbers = ref([])
const dateRange = ref(null)

const searchForm = reactive({
  cardNumber: '',
  batchNumber: '',
  startDate: '',
  endDate: '',
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 20,
  total: 0,
})

const handleDateChange = (val) => {
  if (val) {
    searchForm.startDate = val[0]
    searchForm.endDate = val[1]
  } else {
    searchForm.startDate = ''
    searchForm.endDate = ''
  }
}

const fetchData = async (silent = false) => {
  try {
    loading.value = true
    const res = await getVerifyHistory({ ...searchForm, pageNum: pagination.pageNum, pageSize: pagination.pageSize })
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

const fetchBatchNumbers = async () => {
  try {
    const res = await getBatchNumbers()
    batchNumbers.value = res.data
  } catch (error) {
    // 错误已在拦截器中处理，这里只记录日志
    console.error('获取批次号失败:', error)
  }
}

const handleSearch = async () => {
  pagination.pageNum = 1
  const ok = await fetchData(true)
  ok ? showSuccess('搜索完成') : showError('搜索失败')
}

const handleReset = async () => {
  searchForm.cardNumber = ''
  searchForm.batchNumber = ''
  searchForm.startDate = ''
  searchForm.endDate = ''
  dateRange.value = null
  pagination.pageNum = 1
  const ok = await fetchData(true)
  ok ? showSuccess('已重置筛选条件') : showError('重置失败')
}

const handleSizeChange = () => { pagination.pageNum = 1; fetchData() }
const handleCurrentChange = () => { fetchData() }

const handleExport = async () => {
  try {
    exporting.value = true
    const res = await exportVerifyHistory(searchForm)
    // 检查响应是否为错误
    if (res.data && res.data.type && res.data.type.includes('json')) {
      // 如果返回的是JSON而不是文件，说明有错误
      const text = await res.data.text()
      const error = JSON.parse(text)
      throw new Error(error.message || '导出失败')
    }
    // 创建下载链接
    const blob = new Blob([res.data], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `核销记录_${new Date().toISOString().slice(0,10).replace(/-/g, '')}.xlsx`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch (error) {
    console.error('导出失败:', error)
    // 错误已在拦截器中处理，这里不再重复提示
  } finally {
    exporting.value = false
  }
}

onMounted(() => {
  fetchData()
  fetchBatchNumbers()
})
</script>

<style lang="scss" scoped>
@import '@/assets/styles/variables.scss';

.page-subtitle {
  margin-top: $spacing-xs;
  font-size: $font-size-sm;
  color: $text-secondary;
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.table-title {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
}

.mono-text {
  font-family: $font-family-mono;
  font-weight: $font-weight-medium;
}

.time-cell {
  display: flex;
  align-items: center;
  gap: $spacing-xs;
  color: $text-regular;
  
  .el-icon {
    color: $text-secondary;
  }
}

.operator-tag {
  min-width: 80px;
  text-align: center;
}

.data-table {
  :deep(.el-table__header th) {
    background-color: $fill-lighter !important;
  }
}
</style>
