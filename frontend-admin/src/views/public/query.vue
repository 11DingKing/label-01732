<template>
  <div class="public-page">
    <!-- 背景 -->
    <div class="page-bg">
      <div class="bg-gradient"></div>
      <div class="bg-grid"></div>
      <!-- 装饰元素 -->
      <div class="bg-orbs">
        <div class="orb orb-1"></div>
        <div class="orb orb-2"></div>
        <div class="orb orb-3"></div>
      </div>
    </div>
    
    <!-- 内容 -->
    <div class="page-content">
      <transition name="zoom" appear>
        <div class="query-card">
          <!-- 头部 -->
          <div class="card-header">
            <div class="header-icon" :class="{ 'is-loading': loading }">
              <el-icon><Search /></el-icon>
              <div class="icon-ring"></div>
            </div>
            <h1 class="header-title">卡密状态查询</h1>
            <p class="header-desc">输入卡号和密码查询卡密使用状态</p>
          </div>
          
          <!-- 表单 -->
          <div class="card-body">
            <el-form
              ref="formRef"
              :model="form"
              :rules="rules"
              class="query-form"
              @keyup.enter="handleQuery"
            >
              <el-form-item prop="cardNumber">
                <el-input
                  v-model="form.cardNumber"
                  placeholder="请输入9位卡号"
                  size="large"
                  maxlength="9"
                  :prefix-icon="Postcard"
                  @focus="inputFocus = 'cardNumber'"
                  @blur="inputFocus = ''"
                  :class="{ 'input-focused': inputFocus === 'cardNumber' }"
                >
                  <template #suffix>
                    <transition name="fade">
                      <span v-if="form.cardNumber" class="input-counter">
                        {{ form.cardNumber.length }}/9
                      </span>
                    </transition>
                  </template>
                </el-input>
              </el-form-item>
              
              <el-form-item prop="cardPassword">
                <el-input
                  v-model="form.cardPassword"
                  placeholder="请输入6位密码"
                  size="large"
                  maxlength="6"
                  :prefix-icon="Lock"
                  @input="handlePasswordInput"
                  @focus="inputFocus = 'cardPassword'"
                  @blur="inputFocus = ''"
                  :class="{ 'input-focused': inputFocus === 'cardPassword' }"
                >
                  <template #suffix>
                    <transition name="fade">
                      <span v-if="form.cardPassword" class="input-counter">
                        {{ form.cardPassword.length }}/6
                      </span>
                    </transition>
                  </template>
                </el-input>
              </el-form-item>
              
              <el-form-item>
                <el-button
                  type="primary"
                  size="large"
                  :loading="loading"
                  :disabled="!canSubmit"
                  class="query-btn ripple-btn"
                  @click="handleQuery"
                >
                  <template v-if="!loading">
                    <el-icon class="btn-icon"><Search /></el-icon>
                    查询状态
                  </template>
                  <template v-else>
                    <span class="loading-spinner"></span>
                    查询中
                  </template>
                </el-button>
              </el-form-item>
            </el-form>
          </div>
          
          <!-- 查询结果 -->
          <transition name="bounce">
            <div v-if="result.visible" class="card-result" :class="getStatusType(result.data.statusName)">
              <div class="result-status">
                <div class="status-icon" :class="getStatusType(result.data.statusName)">
                  <el-icon><component :is="getStatusIcon(result.data.statusName)" /></el-icon>
                </div>
                <div class="status-text">
                  <span class="status-label">当前状态</span>
                  <span class="status-value">{{ result.data.statusName }}</span>
                </div>
              </div>
              
              <div class="result-details">
                <div class="detail-item">
                  <el-icon><Postcard /></el-icon>
                  <span class="detail-label">卡号</span>
                  <span class="detail-value mono">{{ result.data.cardNumber }}</span>
                </div>
                <div class="detail-item">
                  <el-icon><Calendar /></el-icon>
                  <span class="detail-label">生成时间</span>
                  <span class="detail-value">{{ result.data.createTime }}</span>
                </div>
                <div v-if="result.data.useTime" class="detail-item">
                  <el-icon><Clock /></el-icon>
                  <span class="detail-label">核销时间</span>
                  <span class="detail-value">{{ result.data.useTime }}</span>
                </div>
              </div>
            </div>
          </transition>
          
          <!-- 底部 -->
          <div class="card-footer">
            <el-button text type="primary" @click="goLogin" class="login-link">
              <el-icon><User /></el-icon>
              管理员登录
              <el-icon class="link-arrow"><ArrowRight /></el-icon>
            </el-button>
          </div>
        </div>
      </transition>
      
      <!-- 版权信息 -->
      <transition name="fade" appear>
        <div class="copyright">
          <p>© 2024 游戏卡密管理系统 · 安全查询</p>
        </div>
      </transition>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { Postcard, Lock } from '@element-plus/icons-vue'
import { queryCardStatus } from '@/api/public'
import toast from '@/utils/toast'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)
const inputFocus = ref('')

const form = reactive({
  cardNumber: '',
  cardPassword: '',
})

const rules = {
  cardNumber: [
    { required: true, message: '请输入卡号', trigger: 'blur' },
    { pattern: /^\d{9}$/, message: '卡号必须为9位数字', trigger: 'blur' },
  ],
  cardPassword: [
    { required: true, message: '请输入密码', trigger: 'blur' },
  ],
}

const result = reactive({
  visible: false,
  data: {},
})

// 是否可以提交
const canSubmit = computed(() => {
  return form.cardNumber.length === 9 && form.cardPassword.length > 0
})

// 自动转大写
const handlePasswordInput = (value) => {
  form.cardPassword = value.toUpperCase()
}

const getStatusType = (statusName) => {
  const types = {
    '未使用': 'success',
    '已核销': 'warning',
    '已回收': 'danger',
  }
  return types[statusName] || 'info'
}

const getStatusIcon = (statusName) => {
  const icons = {
    '未使用': 'CircleCheck',
    '已核销': 'WarningFilled',
    '已回收': 'Delete',
  }
  return icons[statusName] || 'InfoFilled'
}

const handleQuery = async () => {
  try {
    await formRef.value.validate()
    loading.value = true
    result.visible = false
    
    const res = await queryCardStatus(form)
    
    // 短暂延迟以显示加载动画
    await new Promise(resolve => setTimeout(resolve, 300))
    
    result.data = res.data
    result.visible = true
    
    // 根据状态显示不同提示
    if (res.data.statusName === '未使用') {
      toast.success('查询成功，卡密可用')
    } else if (res.data.statusName === '已核销') {
      toast.warning('该卡密已被使用')
    } else {
      toast.info('查询成功')
    }
  } catch (error) {
    result.visible = false
    // 错误提示已在请求拦截器中处理，这里不再重复
  } finally {
    loading.value = false
  }
}

const goLogin = () => {
  router.push('/login')
}
</script>

<style lang="scss" scoped>
@import '@/assets/styles/variables.scss';

.public-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  padding: 20px;
}

// ==================== 背景 ====================

.page-bg {
  position: absolute;
  inset: 0;
  z-index: 0;
  overflow: hidden;
}

.bg-gradient {
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, #0f172a 0%, #1e293b 50%, #334155 100%);
}

.bg-grid {
  position: absolute;
  inset: 0;
  background-image: 
    linear-gradient(rgba(255, 255, 255, 0.03) 1px, transparent 1px),
    linear-gradient(90deg, rgba(255, 255, 255, 0.03) 1px, transparent 1px);
  background-size: 60px 60px;
  animation: gridMove 20s linear infinite;
}

@keyframes gridMove {
  0% { transform: translate(0, 0); }
  100% { transform: translate(60px, 60px); }
}

.bg-orbs {
  position: absolute;
  inset: 0;
  
  .orb {
    position: absolute;
    border-radius: 50%;
    filter: blur(60px);
    opacity: 0.3;
    animation: orbFloat 15s ease-in-out infinite;
  }
  
  .orb-1 {
    width: 300px;
    height: 300px;
    background: $primary-color;
    top: -100px;
    left: -100px;
  }
  
  .orb-2 {
    width: 200px;
    height: 200px;
    background: #764ba2;
    bottom: -50px;
    right: -50px;
    animation-delay: -5s;
  }
  
  .orb-3 {
    width: 150px;
    height: 150px;
    background: $success-color;
    top: 50%;
    right: 20%;
    animation-delay: -10s;
  }
}

@keyframes orbFloat {
  0%, 100% { transform: translate(0, 0) scale(1); }
  33% { transform: translate(30px, -30px) scale(1.1); }
  66% { transform: translate(-20px, 20px) scale(0.9); }
}

// ==================== 内容 ====================

.page-content {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: 440px;
}

.query-card {
  background: rgba(255, 255, 255, 0.98);
  backdrop-filter: blur(20px);
  border-radius: $radius-2xl;
  box-shadow: 
    0 25px 50px -12px rgba(0, 0, 0, 0.4),
    0 0 0 1px rgba(255, 255, 255, 0.1) inset;
  overflow: hidden;
  transition: transform 0.3s $ease-smooth, box-shadow 0.3s $ease-smooth;
  
  &:hover {
    transform: translateY(-5px);
    box-shadow: 
      0 30px 60px -15px rgba(0, 0, 0, 0.5),
      0 0 0 1px rgba(255, 255, 255, 0.15) inset;
  }
}

// 缩放进入动画
.zoom-enter-active {
  animation: zoomIn 0.5s $ease-spring;
}

@keyframes zoomIn {
  from {
    opacity: 0;
    transform: scale(0.9) translateY(20px);
  }
  to {
    opacity: 1;
    transform: scale(1) translateY(0);
  }
}

// ==================== 头部 ====================

.card-header {
  text-align: center;
  padding: 40px 40px 0;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.header-icon {
  width: 72px;
  height: 72px;
  border-radius: $radius-xl;
  background: linear-gradient(135deg, $primary-color 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 20px;
  position: relative;
  transition: all 0.3s $ease-spring;
  
  &:hover {
    transform: scale(1.05) rotate(5deg);
  }
  
  .el-icon {
    font-size: 32px;
    color: #fff;
    transition: transform 0.3s $ease-spring;
  }
  
  .icon-ring {
    position: absolute;
    inset: -4px;
    border-radius: inherit;
    border: 2px solid rgba($primary-color, 0.3);
    animation: ringPulse 2s ease-in-out infinite;
  }
  
  &.is-loading {
    animation: iconPulse 0.8s ease-in-out infinite;
    
    .el-icon {
      animation: iconSpin 1s linear infinite;
    }
  }
}

@keyframes ringPulse {
  0%, 100% { transform: scale(1); opacity: 1; }
  50% { transform: scale(1.15); opacity: 0; }
}

@keyframes iconPulse {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.05); }
}

@keyframes iconSpin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.header-title {
  font-size: $font-size-xl;
  font-weight: $font-weight-bold;
  color: $text-primary;
  margin: 0 0 8px;
}

.header-desc {
  font-size: $font-size-sm;
  color: $text-secondary;
  margin: 0;
}

// ==================== 表单 ====================

.card-body {
  padding: 32px 40px;
}

.query-form {
  .el-form-item {
    margin-bottom: 20px;
  }
  
  :deep(.el-input__wrapper) {
    padding: 4px 16px;
    border-radius: $radius-lg;
    background-color: $fill-light;
    box-shadow: none;
    border: 2px solid transparent;
    transition: all 0.3s $ease-smooth;
    
    &:hover {
      border-color: rgba($primary-color, 0.3);
    }
  }
  
  :deep(.el-input.input-focused .el-input__wrapper) {
    border-color: $primary-color;
    background-color: #fff;
    box-shadow: 0 0 0 4px rgba($primary-color, 0.1);
  }
  
  :deep(.el-input__inner) {
    height: 44px;
    font-size: $font-size-base;
    font-family: $font-family-mono;
    letter-spacing: 2px;
  }
  
  .input-counter {
    font-size: $font-size-xs;
    color: $text-disabled;
    font-family: $font-family-mono;
  }
}

.query-btn {
  width: 100%;
  height: 52px;
  font-size: $font-size-md;
  font-weight: $font-weight-semibold;
  border-radius: $radius-lg;
  background: linear-gradient(135deg, $primary-color 0%, #764ba2 100%);
  border: none;
  transition: all 0.3s $ease-smooth;
  
  // 按钮内容水平居中排列
  :deep(.el-button__text) {
    display: inline-flex;
    align-items: center;
    justify-content: center;
  }
  
  .btn-icon {
    margin-right: 8px;
    transition: transform 0.3s $ease-spring;
  }
  
  &:not(:disabled):hover {
    transform: translateY(-2px);
    box-shadow: 0 10px 30px rgba($primary-color, 0.4);
    
    .btn-icon {
      transform: scale(1.2);
    }
  }
  
  &:disabled {
    background: $fill-light;
    color: $text-disabled;
  }
  
  .loading-spinner {
    width: 18px;
    height: 18px;
    border: 2px solid rgba(255, 255, 255, 0.3);
    border-top-color: #fff;
    border-radius: 50%;
    animation: spin 0.8s linear infinite;
    margin-right: 8px;
    display: inline-block;
  }
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

// ==================== 结果 ====================

.card-result {
  margin: 0 40px 32px;
  border-radius: $radius-xl;
  overflow: hidden;
  
  &.success {
    background: linear-gradient(135deg, #f0fff4 0%, #dcfce7 100%);
    border: 1px solid rgba($success-color, 0.2);
  }
  
  &.warning {
    background: linear-gradient(135deg, #fffbeb 0%, #fef3c7 100%);
    border: 1px solid rgba($warning-color, 0.2);
  }
  
  &.danger {
    background: linear-gradient(135deg, #fef2f2 0%, #fecaca 100%);
    border: 1px solid rgba($danger-color, 0.2);
  }
}

.result-status {
  display: flex;
  align-items: center;
  gap: $spacing-base;
  padding: $spacing-lg $spacing-xl;
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
  
  .status-icon {
    width: 48px;
    height: 48px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    
    .el-icon {
      font-size: 24px;
      color: #fff;
    }
    
    &.success {
      background: linear-gradient(135deg, $success-color 0%, darken($success-color, 10%) 100%);
    }
    
    &.warning {
      background: linear-gradient(135deg, $warning-color 0%, darken($warning-color, 10%) 100%);
    }
    
    &.danger {
      background: linear-gradient(135deg, $danger-color 0%, darken($danger-color, 10%) 100%);
    }
  }
  
  .status-text {
    display: flex;
    flex-direction: column;
    
    .status-label {
      font-size: $font-size-xs;
      color: $text-secondary;
    }
    
    .status-value {
      font-size: $font-size-lg;
      font-weight: $font-weight-bold;
      color: $text-primary;
    }
  }
}

.result-details {
  padding: $spacing-base $spacing-xl $spacing-lg;
  
  .detail-item {
    display: flex;
    align-items: center;
    gap: $spacing-sm;
    padding: $spacing-sm 0;
    
    .el-icon {
      font-size: 14px;
      color: $text-secondary;
    }
    
    .detail-label {
      font-size: $font-size-sm;
      color: $text-secondary;
      min-width: 60px;
    }
    
    .detail-value {
      font-size: $font-size-sm;
      color: $text-primary;
      font-weight: $font-weight-medium;
      
      &.mono {
        font-family: $font-family-mono;
        letter-spacing: 1px;
      }
    }
  }
}

// ==================== 底部 ====================

.card-footer {
  padding: 0 40px 32px;
  text-align: center;
  
  .login-link {
    display: inline-flex;
    align-items: center;
    gap: 4px;
    transition: all 0.3s $ease-smooth;
    
    .link-arrow {
      opacity: 0;
      transform: translateX(-8px);
      transition: all 0.3s $ease-spring;
    }
    
    &:hover {
      .link-arrow {
        opacity: 1;
        transform: translateX(0);
      }
    }
  }
}

.copyright {
  text-align: center;
  margin-top: 24px;
  
  p {
    font-size: $font-size-xs;
    color: rgba(255, 255, 255, 0.5);
    margin: 0;
  }
}

// ==================== 动画 ====================

.bounce-enter-active {
  animation: bounceIn 0.5s $ease-spring;
}

.bounce-leave-active {
  animation: bounceOut 0.3s $ease-in;
}

@keyframes bounceIn {
  0% { transform: scale(0.8); opacity: 0; }
  50% { transform: scale(1.02); }
  100% { transform: scale(1); opacity: 1; }
}

@keyframes bounceOut {
  0% { transform: scale(1); opacity: 1; }
  100% { transform: scale(0.9); opacity: 0; }
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s $ease-smooth;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

// ==================== 响应式 ====================

@media screen and (max-width: $breakpoint-sm) {
  .card-header,
  .card-body,
  .card-footer {
    padding-left: 24px;
    padding-right: 24px;
  }
  
  .card-result {
    margin-left: 24px;
    margin-right: 24px;
  }
}
</style>
