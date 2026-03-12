<template>
  <div class="page-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div>
        <h2 class="page-title">
          <el-icon><List /></el-icon>
          卡密列表
        </h2>
        <p class="page-subtitle">查询和管理所有卡密</p>
      </div>
    </div>
    
    <!-- 搜索区域 -->
    <div class="search-card">
      <el-form :model="searchForm" inline class="search-form">
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
        <el-form-item label="卡号">
          <el-input
            v-model="searchForm.cardNumber"
            placeholder="请输入卡号"
            clearable
            style="width: 160px;"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select
            v-model="searchForm.status"
            placeholder="全部状态"
            clearable
            style="width: 120px;"
          >
            <el-option label="未使用" :value="0" />
            <el-option label="已核销" :value="1" />
            <el-option label="已回收" :value="2" />
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
        </el-form-item>
      </el-form>
    </div>
    
    <!-- 表格区域 -->
    <div class="table-card">
      <div class="table-header">
        <div class="table-title">
          查询结果
          <el-tag type="info" size="small" effect="plain">{{ pagination.total }} 条</el-tag>
        </div>
        <div class="table-toolbar">
          <el-button type="success" @click="handleExport">
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
        <el-table-column prop="cardNumber" label="卡号" min-width="140" fixed>
          <template #default="{ row }">
            <span class="mono-text">{{ row.cardNumber }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="cardPassword" label="密码" min-width="100">
          <template #default="{ row }">
            <span class="mono-text">{{ row.cardPassword }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="batchNumber" label="批次号" min-width="160">
          <template #default="{ row }">
            <el-link type="primary" @click="filterByBatch(row.batchNumber)">
              {{ row.batchNumber }}
            </el-link>
          </template>
        </el-table-column>
        <el-table-column prop="statusName" label="状态" min-width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" effect="light">
              {{ row.statusName }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="生成时间" min-width="180" />
        <el-table-column prop="useTime" label="核销时间" min-width="180">
          <template #default="{ row }">
            <span v-if="row.useTime">{{ row.useTime }}</span>
            <span v-else class="text-placeholder">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="useOperatorName" label="核销操作员" min-width="120">
          <template #default="{ row }">
            <span v-if="row.useOperatorName">{{ row.useOperatorName }}</span>
            <span v-else class="text-placeholder">-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" width="160" align="center">
          <template #default="{ row }">
            <div class="table-action-btns">
              <button class="table-action-btn btn-view" @click="handleView(row)">
                <el-icon><View /></el-icon>
                查看
              </button>
              <button
                v-if="row.status === 0 && userStore.isAdmin"
                class="table-action-btn btn-recycle"
                @click="handleRecycle(row)"
              >
                <el-icon><Delete /></el-icon>
                回收
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
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/store/modules/user'
import { getCardList, getBatchNumbers, recycleSingle, exportCards } from '@/api/card'
import { showSuccess, showError } from '@/utils/toast'

const route = useRoute()
const userStore = useUserStore()

const loading = ref(false)
const tableData = ref([])
const batchNumbers = ref([])

const searchForm = reactive({
  batchNumber: route.query.batchNumber || '',
  cardNumber: '',
  status: null,
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 20,
  total: 0,
})

const getStatusType = (status) => {
  const types = { 0: 'success', 1: 'warning', 2: 'danger' }
  return types[status] || 'info'
}

const fetchData = async (silent = false) => {
  try {
    loading.value = true
    const res = await getCardList({
      ...searchForm,
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
    })
    tableData.value = res.data.records
    pagination.total = res.data.total
    return true
  } catch (error) {
    console.error('获取数据失败:', error)
    if (!silent) showError('获取数据失败，请稍后重试')
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
    console.error('获取批次号失败:', error)
  }
}

const handleSearch = async () => {
  pagination.pageNum = 1
  const ok = await fetchData(true)
  ok ? showSuccess('搜索完成') : showError('获取数据失败，请稍后重试')
}

const handleReset = async () => {
  searchForm.batchNumber = ''
  searchForm.cardNumber = ''
  searchForm.status = null
  pagination.pageNum = 1
  const ok = await fetchData(true)
  ok ? showSuccess('已重置筛选条件') : showError('获取数据失败，请稍后重试')
}

const handleSizeChange = () => {
  pagination.pageNum = 1
  fetchData()
}

const handleCurrentChange = () => {
  fetchData()
}

const filterByBatch = (batchNumber) => {
  searchForm.batchNumber = batchNumber
  handleSearch()
}

const handleView = (row) => {
  const statusColors = { 0: '#52c41a', 1: '#faad14', 2: '#ff4d4f' }
  const statusColor = statusColors[row.status] || '#909399'
  
  ElMessageBox.alert(
    `<div class="card-detail-content">
      <div class="detail-header">
        <div class="card-icon">
          <svg viewBox="0 0 24 24" width="32" height="32" fill="none" stroke="currentColor" stroke-width="2">
            <rect x="2" y="5" width="20" height="14" rx="2"/>
            <line x1="2" y1="10" x2="22" y2="10"/>
          </svg>
        </div>
        <div class="card-main-info">
          <span class="card-number">${row.cardNumber}</span>
          <span class="card-status" style="background: ${statusColor}15; color: ${statusColor}">${row.statusName}</span>
        </div>
      </div>
      <div class="detail-grid">
        <div class="detail-item">
          <span class="detail-label">密码</span>
          <span class="detail-value mono">${row.cardPassword}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">批次号</span>
          <span class="detail-value mono">${row.batchNumber}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">生成时间</span>
          <span class="detail-value">${row.createTime}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">核销时间</span>
          <span class="detail-value">${row.useTime || '-'}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">核销操作员</span>
          <span class="detail-value">${row.useOperatorName || '-'}</span>
        </div>
      </div>
    </div>`,
    '卡密详情',
    {
      dangerouslyUseHTMLString: true,
      confirmButtonText: '关闭',
      customClass: 'card-detail-dialog',
      center: true
    }
  )
}

const handleRecycle = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要回收卡号为 ${row.cardNumber} 的卡密吗？回收后将无法核销。`,
      '确认回收',
      { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
    )
    await recycleSingle(row.cardNumber)
    ElMessage.success('回收成功')
    fetchData()
  } catch (error) {
    if (error !== 'cancel') console.error('回收失败:', error)
  }
}

const handleExport = async () => {
  try {
    const res = await exportCards(searchForm)
    const blob = new Blob([res.data], { 
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' 
    })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `卡密列表_${Date.now()}.xlsx`
    link.click()
    window.URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch (error) {
    console.error('导出失败:', error)
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

.mono-text {
  font-family: $font-family-mono;
  font-weight: $font-weight-medium;
}

.text-placeholder {
  color: $text-disabled;
}

.table-title {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
}

.table-card {
  width: 100%;
}

.data-table {
  width: 100%;
  :deep(.el-table__header th) {
    background-color: $fill-lighter !important;
  }
}

// 操作按钮样式
.action-buttons {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.action-btn {
  padding: 6px 12px;
  font-size: 12px;
  border-radius: 6px;
  transition: all 0.25s ease;
  
  .el-icon {
    margin-right: 4px;
  }
  
  &.view-btn {
    background: linear-gradient(135deg, $primary-color 0%, darken($primary-color, 8%) 100%);
    border: none;
    box-shadow: 0 2px 6px rgba($primary-color, 0.3);
    
    &:hover {
      transform: translateY(-1px);
      box-shadow: 0 4px 12px rgba($primary-color, 0.4);
    }
    
    &:active {
      transform: translateY(0);
    }
  }
  
  &.recycle-btn {
    background: linear-gradient(135deg, $danger-color 0%, darken($danger-color, 8%) 100%);
    border: none;
    box-shadow: 0 2px 6px rgba($danger-color, 0.3);
    
    &:hover {
      transform: translateY(-1px);
      box-shadow: 0 4px 12px rgba($danger-color, 0.4);
    }
    
    &:active {
      transform: translateY(0);
    }
  }
}
</style>

<!-- 全局样式用于弹窗 -->
<style lang="scss">
.card-detail-dialog {
  border-radius: 16px !important;
  width: 560px !important;
  max-width: 90vw !important;
  
  .el-message-box__header {
    padding: 20px 24px 0;
    
    .el-message-box__title {
      font-size: 18px;
      font-weight: 600;
    }
  }
  
  .el-message-box__content {
    padding: 16px 24px 24px;
  }
  
  .el-message-box__btns {
    padding: 0 24px 20px;
    
    .el-button--primary {
      width: 100%;
      height: 44px;
      border-radius: 10px;
      font-size: 15px;
    }
  }
  
  .card-detail-content {
    .detail-header {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 12px;
      padding: 24px;
      background: linear-gradient(135deg, #f6f8fc 0%, #f0f4f8 100%);
      border-radius: 12px;
      margin-bottom: 20px;
      
      .card-icon {
        width: 64px;
        height: 64px;
        background: linear-gradient(135deg, #1890ff 0%, #096dd9 100%);
        border-radius: 16px;
        display: flex;
        align-items: center;
        justify-content: center;
        color: #fff;
      }
      
      .card-main-info {
        display: flex;
        flex-direction: column;
        align-items: center;
        gap: 8px;
        
        .card-number {
          font-size: 28px;
          font-weight: 700;
          font-family: 'SF Mono', Monaco, Consolas, monospace;
          color: #1a1a1a;
          letter-spacing: 3px;
        }
        
        .card-status {
          display: inline-flex;
          align-items: center;
          padding: 6px 16px;
          border-radius: 6px;
          font-size: 14px;
          font-weight: 500;
        }
      }
    }
    
    .detail-grid {
      display: flex;
      flex-direction: column;
      gap: 10px;
      
      .detail-item {
        display: flex;
        align-items: flex-start;
        padding: 14px 20px;
        background: #fafafa;
        border-radius: 10px;
        
        .detail-label {
          font-size: 14px;
          color: #8c8c8c;
          flex-shrink: 0;
          width: 90px;
          padding-top: 1px;
        }
        
        .detail-value {
          flex: 1;
          font-size: 14px;
          color: #262626;
          font-weight: 500;
          word-break: break-all;
          line-height: 1.5;
          
          &.mono {
            font-family: 'SF Mono', Monaco, Consolas, monospace;
            letter-spacing: 1px;
          }
        }
      }
    }
  }
}
</style>
