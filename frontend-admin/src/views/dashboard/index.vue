<template>
  <div class="page-container dashboard-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div>
        <h2 class="page-title">
          <el-icon><DataAnalysis /></el-icon>
          数据概览
        </h2>
        <p class="page-subtitle">实时掌握卡密运营数据</p>
      </div>
      <div class="header-actions">
        <el-button @click="fetchData(true)" :loading="loading">
          <el-icon><Refresh /></el-icon>
          刷新数据
        </el-button>
      </div>
    </div>
    
    <!-- 核心指标 -->
    <el-row :gutter="16" class="stats-row">
      <el-col :xs="12" :sm="6">
        <div class="stat-card primary">
          <div class="stat-icon">
            <el-icon><Postcard /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ animatedData.totalCards }}</div>
            <div class="stat-label">总发卡数</div>
          </div>
          <div class="stat-decoration"></div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="6">
        <div class="stat-card success">
          <div class="stat-icon">
            <el-icon><CircleCheck /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ animatedData.unusedCards }}</div>
            <div class="stat-label">未使用</div>
          </div>
          <div class="stat-decoration"></div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="6">
        <div class="stat-card warning">
          <div class="stat-icon">
            <el-icon><Select /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ animatedData.usedCards }}</div>
            <div class="stat-label">已核销</div>
          </div>
          <div class="stat-decoration"></div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="6">
        <div class="stat-card danger">
          <div class="stat-icon">
            <el-icon><Delete /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ animatedData.recycledCards }}</div>
            <div class="stat-label">已回收</div>
          </div>
          <div class="stat-decoration"></div>
        </div>
      </el-col>
    </el-row>
    
    <!-- 今日统计 & 其他指标 -->
    <el-row :gutter="16" class="stats-row">
      <el-col :xs="12" :sm="6">
        <div class="mini-stat-card">
          <div class="mini-icon today">
            <el-icon><Calendar /></el-icon>
          </div>
          <div class="mini-content">
            <div class="mini-value">{{ animatedData.todayGeneratedCards }}</div>
            <div class="mini-label">今日发卡</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="6">
        <div class="mini-stat-card">
          <div class="mini-icon verify">
            <el-icon><Finished /></el-icon>
          </div>
          <div class="mini-content">
            <div class="mini-value">{{ animatedData.todayUsedCards }}</div>
            <div class="mini-label">今日核销</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="6">
        <div class="mini-stat-card">
          <div class="mini-icon batch">
            <el-icon><FolderOpened /></el-icon>
          </div>
          <div class="mini-content">
            <div class="mini-value">{{ animatedData.totalBatches }}</div>
            <div class="mini-label">总批次</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="6">
        <div class="mini-stat-card">
          <div class="mini-icon user">
            <el-icon><UserFilled /></el-icon>
          </div>
          <div class="mini-content">
            <div class="mini-value">{{ animatedData.totalUsers }}</div>
            <div class="mini-label">系统用户</div>
          </div>
        </div>
      </el-col>
    </el-row>
    
    <el-row :gutter="16">
      <!-- 使用率分析 -->
      <el-col :xs="24" :lg="14">
        <div class="card chart-card">
          <div class="card-header">
            <h3 class="card-title">
              <el-icon><PieChart /></el-icon>
              卡密使用分析
            </h3>
          </div>
          <div class="usage-analysis">
            <!-- 环形进度图 -->
            <div class="ring-chart">
              <svg viewBox="0 0 100 100" class="ring-svg">
                <circle class="ring-bg" cx="50" cy="50" r="42" />
                <circle 
                  class="ring-progress unused" 
                  cx="50" cy="50" r="42"
                  :stroke-dasharray="`${unusedPercent * 2.64} 264`"
                  :stroke-dashoffset="0"
                />
                <circle 
                  class="ring-progress used" 
                  cx="50" cy="50" r="42"
                  :stroke-dasharray="`${usedPercent * 2.64} 264`"
                  :stroke-dashoffset="`${-unusedPercent * 2.64}`"
                />
                <circle 
                  class="ring-progress recycled" 
                  cx="50" cy="50" r="42"
                  :stroke-dasharray="`${recycledPercent * 2.64} 264`"
                  :stroke-dashoffset="`${-(unusedPercent + usedPercent) * 2.64}`"
                />
              </svg>
              <div class="ring-center">
                <div class="ring-value">{{ total }}</div>
                <div class="ring-label">总数</div>
              </div>
            </div>
            
            <!-- 图例 -->
            <div class="chart-legend">
              <div class="legend-item">
                <span class="legend-dot unused"></span>
                <span class="legend-label">未使用</span>
                <span class="legend-value">{{ dashboardData.unusedCards || 0 }}</span>
                <span class="legend-percent">{{ unusedPercent }}%</span>
              </div>
              <div class="legend-item">
                <span class="legend-dot used"></span>
                <span class="legend-label">已核销</span>
                <span class="legend-value">{{ dashboardData.usedCards || 0 }}</span>
                <span class="legend-percent">{{ usedPercent }}%</span>
              </div>
              <div class="legend-item">
                <span class="legend-dot recycled"></span>
                <span class="legend-label">已回收</span>
                <span class="legend-value">{{ dashboardData.recycledCards || 0 }}</span>
                <span class="legend-percent">{{ recycledPercent }}%</span>
              </div>
            </div>
          </div>
        </div>
      </el-col>
      
      <!-- 快捷操作 -->
      <el-col :xs="24" :lg="10">
        <div class="card quick-card">
          <div class="card-header">
            <h3 class="card-title">
              <el-icon><Grid /></el-icon>
              快捷操作
            </h3>
          </div>
          <div class="quick-actions">
            <div class="quick-action-btn primary" @click="$router.push('/card/generate')">
              <div class="action-icon">
                <el-icon><Plus /></el-icon>
              </div>
              <span class="action-label">一键发卡</span>
            </div>
            <div class="quick-action-btn success" @click="$router.push('/verify/use')">
              <div class="action-icon">
                <el-icon><CircleCheck /></el-icon>
              </div>
              <span class="action-label">卡密核销</span>
            </div>
            <div class="quick-action-btn info" @click="$router.push('/card/list')">
              <div class="action-icon">
                <el-icon><List /></el-icon>
              </div>
              <span class="action-label">卡密列表</span>
            </div>
            <div class="quick-action-btn warning" @click="$router.push('/verify/history')">
              <div class="action-icon">
                <el-icon><Document /></el-icon>
              </div>
              <span class="action-label">核销记录</span>
            </div>
            <div class="quick-action-btn danger" @click="$router.push('/card/batch')">
              <div class="action-icon">
                <el-icon><FolderOpened /></el-icon>
              </div>
              <span class="action-label">批次管理</span>
            </div>
            <div v-if="userStore.isAdmin" class="quick-action-btn" @click="$router.push('/user/list')">
              <div class="action-icon" style="background: linear-gradient(135deg, #8b5cf6 0%, #6d28d9 100%);">
                <el-icon><UserFilled /></el-icon>
              </div>
              <span class="action-label">用户管理</span>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, reactive, watch } from 'vue'
import { getDashboardData } from '@/api/statistics'
import { useUserStore } from '@/store/modules/user'
import { showSuccess, showError } from '@/utils/toast'

const userStore = useUserStore()
const loading = ref(false)
const dashboardData = ref({})

// 动画数据
const animatedData = reactive({
  totalCards: 0,
  unusedCards: 0,
  usedCards: 0,
  recycledCards: 0,
  todayGeneratedCards: 0,
  todayUsedCards: 0,
  totalBatches: 0,
  totalUsers: 0,
})

const total = computed(() => dashboardData.value.totalCards || 0)

const unusedPercent = computed(() => {
  if (total.value === 0) return 0
  return Math.round((dashboardData.value.unusedCards || 0) / total.value * 100)
})

const usedPercent = computed(() => {
  if (total.value === 0) return 0
  return Math.round((dashboardData.value.usedCards || 0) / total.value * 100)
})

const recycledPercent = computed(() => {
  if (total.value === 0) return 0
  return Math.round((dashboardData.value.recycledCards || 0) / total.value * 100)
})

// 数字动画
const animateNumber = (key, target) => {
  const duration = 1000
  const start = animatedData[key]
  const diff = target - start
  const startTime = performance.now()
  
  const animate = (currentTime) => {
    const elapsed = currentTime - startTime
    const progress = Math.min(elapsed / duration, 1)
    const easeProgress = 1 - Math.pow(1 - progress, 3) // ease-out
    
    animatedData[key] = Math.round(start + diff * easeProgress)
    
    if (progress < 1) {
      requestAnimationFrame(animate)
    }
  }
  
  requestAnimationFrame(animate)
}

const fetchData = async (showToast = false) => {
  try {
    loading.value = true
    const res = await getDashboardData()
    dashboardData.value = res.data
    
    // 触发数字动画
    Object.keys(animatedData).forEach(key => {
      if (res.data[key] !== undefined) {
        animateNumber(key, res.data[key])
      }
    })
    
    // 手动刷新时显示成功提示
    if (showToast) {
      showSuccess('数据刷新成功')
    }
  } catch (error) {
    console.error('获取统计数据失败:', error)
    showError('数据刷新失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchData()
})
</script>

<style lang="scss" scoped>
@import '@/assets/styles/variables.scss';

.dashboard-page {
  .page-subtitle {
    margin-top: $spacing-xs;
    font-size: $font-size-sm;
    color: $text-secondary;
  }
}

.header-actions {
  display: flex;
  gap: $spacing-sm;
}

// ==================== 统计卡片 ====================

.stats-row {
  margin-bottom: $spacing-base;
}

.stat-card {
  background: $bg-white;
  border-radius: $radius-xl;
  padding: $spacing-xl;
  position: relative;
  overflow: hidden;
  box-shadow: $shadow-sm;
  transition: all $transition-base $ease-in-out;
  display: flex;
  align-items: flex-start;
  gap: $spacing-base;

  &:hover {
    transform: translateY(-4px);
    box-shadow: $shadow-md;
  }

  .stat-icon {
    width: 56px;
    height: 56px;
    border-radius: $radius-lg;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 28px;
    color: $bg-white;
    flex-shrink: 0;
  }

  .stat-content {
    flex: 1;
    min-width: 0;
  }

  .stat-value {
    font-size: 32px;
    font-weight: $font-weight-bold;
    color: $text-primary;
    line-height: 1.2;
    font-family: $font-family-mono;
  }

  .stat-label {
    font-size: $font-size-sm;
    color: $text-secondary;
    margin-top: $spacing-xs;
  }

  .stat-decoration {
    position: absolute;
    top: -20px;
    right: -20px;
    width: 100px;
    height: 100px;
    border-radius: 50%;
    opacity: 0.1;
  }

  &.primary {
    .stat-icon {
      background: linear-gradient(135deg, $primary-color 0%, $primary-dark 100%);
    }
    .stat-decoration {
      background: $primary-color;
    }
  }

  &.success {
    .stat-icon {
      background: linear-gradient(135deg, $success-color 0%, darken($success-color, 10%) 100%);
    }
    .stat-value { color: $success-color; }
    .stat-decoration { background: $success-color; }
  }

  &.warning {
    .stat-icon {
      background: linear-gradient(135deg, $warning-color 0%, darken($warning-color, 10%) 100%);
    }
    .stat-value { color: $warning-color; }
    .stat-decoration { background: $warning-color; }
  }

  &.danger {
    .stat-icon {
      background: linear-gradient(135deg, $danger-color 0%, darken($danger-color, 10%) 100%);
    }
    .stat-value { color: $danger-color; }
    .stat-decoration { background: $danger-color; }
  }
}

// ==================== 迷你统计卡片 ====================

.mini-stat-card {
  background: $bg-white;
  border-radius: $radius-lg;
  padding: $spacing-lg;
  display: flex;
  align-items: center;
  gap: $spacing-md;
  box-shadow: $shadow-sm;
  transition: all $transition-base $ease-in-out;

  &:hover {
    box-shadow: $shadow-base;
  }

  .mini-icon {
    width: 44px;
    height: 44px;
    border-radius: $radius-md;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 20px;
    color: $bg-white;

    &.today { background: linear-gradient(135deg, #6366f1 0%, #4f46e5 100%); }
    &.verify { background: linear-gradient(135deg, #10b981 0%, #059669 100%); }
    &.batch { background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%); }
    &.user { background: linear-gradient(135deg, #8b5cf6 0%, #7c3aed 100%); }
  }

  .mini-value {
    font-size: $font-size-xl;
    font-weight: $font-weight-bold;
    color: $text-primary;
    font-family: $font-family-mono;
  }

  .mini-label {
    font-size: $font-size-xs;
    color: $text-secondary;
  }
}

// ==================== 图表卡片 ====================

.chart-card {
  height: 100%;
}

.usage-analysis {
  display: flex;
  align-items: center;
  gap: $spacing-2xl;
  padding: $spacing-lg 0;

  @media screen and (max-width: $breakpoint-md) {
    flex-direction: column;
  }
}

.ring-chart {
  position: relative;
  width: 180px;
  height: 180px;
  flex-shrink: 0;

  .ring-svg {
    width: 100%;
    height: 100%;
    transform: rotate(-90deg);
  }

  .ring-bg {
    fill: none;
    stroke: $fill-light;
    stroke-width: 12;
  }

  .ring-progress {
    fill: none;
    stroke-width: 12;
    stroke-linecap: round;
    transition: stroke-dasharray 1s $ease-out;

    &.unused { stroke: $success-color; }
    &.used { stroke: $warning-color; }
    &.recycled { stroke: $danger-color; }
  }

  .ring-center {
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    text-align: center;

    .ring-value {
      font-size: $font-size-2xl;
      font-weight: $font-weight-bold;
      color: $text-primary;
      font-family: $font-family-mono;
    }

    .ring-label {
      font-size: $font-size-xs;
      color: $text-secondary;
    }
  }
}

.chart-legend {
  flex: 1;

  .legend-item {
    display: flex;
    align-items: center;
    padding: $spacing-md 0;
    border-bottom: 1px solid $border-light;

    &:last-child {
      border-bottom: none;
    }

    .legend-dot {
      width: 10px;
      height: 10px;
      border-radius: 50%;
      margin-right: $spacing-md;

      &.unused { background: $success-color; }
      &.used { background: $warning-color; }
      &.recycled { background: $danger-color; }
    }

    .legend-label {
      flex: 1;
      font-size: $font-size-sm;
      color: $text-regular;
    }

    .legend-value {
      font-size: $font-size-md;
      font-weight: $font-weight-semibold;
      color: $text-primary;
      margin-right: $spacing-md;
      font-family: $font-family-mono;
    }

    .legend-percent {
      font-size: $font-size-sm;
      color: $text-secondary;
      min-width: 48px;
      text-align: right;
    }
  }
}

// ==================== 快捷操作 ====================

.quick-card {
  height: 100%;
}

.quick-actions {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: $spacing-md;

  @media screen and (max-width: $breakpoint-sm) {
    grid-template-columns: repeat(2, 1fr);
  }
}

.quick-action-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: $spacing-lg;
  background: $fill-lighter;
  border-radius: $radius-lg;
  cursor: pointer;
  transition: all $transition-base $ease-in-out;

  &:hover {
    background: $fill-light;
    transform: translateY(-2px);

    .action-icon {
      transform: scale(1.1);
    }
  }

  .action-icon {
    width: 48px;
    height: 48px;
    border-radius: $radius-lg;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 24px;
    color: $bg-white;
    margin-bottom: $spacing-sm;
    transition: transform $transition-base $ease-in-out;
  }

  .action-label {
    font-size: $font-size-sm;
    color: $text-regular;
    font-weight: $font-weight-medium;
  }

  &.primary .action-icon {
    background: linear-gradient(135deg, $primary-color 0%, $primary-dark 100%);
  }

  &.success .action-icon {
    background: linear-gradient(135deg, $success-color 0%, darken($success-color, 10%) 100%);
  }

  &.warning .action-icon {
    background: linear-gradient(135deg, $warning-color 0%, darken($warning-color, 10%) 100%);
  }

  &.danger .action-icon {
    background: linear-gradient(135deg, $danger-color 0%, darken($danger-color, 10%) 100%);
  }

  &.info .action-icon {
    background: linear-gradient(135deg, $info-color 0%, darken($info-color, 10%) 100%);
  }
}
</style>
