<template>
  <div class="page-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div>
        <h2 class="page-title">
          <el-icon><FolderOpened /></el-icon>
          批次管理
        </h2>
        <p class="page-subtitle">管理卡密发放批次</p>
      </div>
    </div>
    
    <!-- 表格区域 -->
    <div class="table-card">
      <div class="table-header">
        <div class="table-title">
          批次列表
          <el-tag type="info" size="small" effect="plain">{{ pagination.total }} 个批次</el-tag>
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
        <el-table-column prop="batchNumber" label="批次号" min-width="180">
          <template #default="{ row }">
            <span class="mono-text">{{ row.batchNumber }}</span>
          </template>
        </el-table-column>
        <el-table-column label="发卡数量" min-width="100" align="center">
          <template #default="{ row }">
            <span class="count-badge total">{{ row.totalCount }}</span>
          </template>
        </el-table-column>
        <el-table-column label="未使用" min-width="100" align="center">
          <template #default="{ row }">
            <span class="count-badge success">{{ row.unusedCount }}</span>
          </template>
        </el-table-column>
        <el-table-column label="已核销" min-width="100" align="center">
          <template #default="{ row }">
            <span class="count-badge warning">{{ row.usedCount }}</span>
          </template>
        </el-table-column>
        <el-table-column label="已回收" min-width="100" align="center">
          <template #default="{ row }">
            <span class="count-badge danger">{{ row.recycledCount }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="operatorName" label="操作员" min-width="120" />
        <el-table-column prop="createTime" label="创建时间" min-width="180" />
        <el-table-column label="操作" fixed="right" width="180" align="center">
          <template #default="{ row }">
            <div class="table-action-btns">
              <button class="table-action-btn btn-view" @click="goToCardList(row.batchNumber)">
                <el-icon><List /></el-icon>
                查看
              </button>
              <button
                v-if="row.unusedCount > 0 && userStore.isAdmin"
                class="table-action-btn btn-recycle"
                @click="handleRecycleBatch(row)"
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
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/store/modules/user'
import { getBatchList, recycleBatch } from '@/api/card'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const tableData = ref([])

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0,
})

const fetchData = async () => {
  try {
    loading.value = true
    const res = await getBatchList({ pageNum: pagination.pageNum, pageSize: pagination.pageSize })
    tableData.value = res.data.records
    pagination.total = res.data.total
  } catch (error) {
    console.error('获取数据失败:', error)
  } finally {
    loading.value = false
  }
}

const handleSizeChange = () => { pagination.pageNum = 1; fetchData() }
const handleCurrentChange = () => { fetchData() }

const goToCardList = (batchNumber) => {
  router.push({ path: '/card/list', query: { batchNumber } })
}

const handleRecycleBatch = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要回收批次 ${row.batchNumber} 中所有未使用的卡密吗？共 ${row.unusedCount} 张。`,
      '确认批量回收',
      { type: 'warning' }
    )
    const res = await recycleBatch(row.batchNumber)
    ElMessage.success(res.message)
    fetchData()
  } catch (error) {
    if (error !== 'cancel') console.error('回收失败:', error)
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

.table-card {
  width: 100%;
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

.count-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 40px;
  height: 24px;
  padding: 0 8px;
  border-radius: $radius-full;
  font-size: $font-size-sm;
  font-weight: $font-weight-semibold;
  
  &.total {
    background: $primary-lightest;
    color: $primary-color;
  }
  
  &.success {
    background: $success-bg;
    color: $success-color;
  }
  
  &.warning {
    background: $warning-bg;
    color: $warning-color;
  }
  
  &.danger {
    background: $danger-bg;
    color: $danger-color;
  }
}

.data-table {
  width: 100%;
  :deep(.el-table__header th) {
    background-color: $fill-lighter !important;
  }
}
</style>
