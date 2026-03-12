<template>
  <div class="page-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div>
        <h2 class="page-title">
          <el-icon><Plus /></el-icon>
          一键发卡
        </h2>
        <p class="page-subtitle">快速批量生成卡密</p>
      </div>
    </div>
    
    <el-row :gutter="20">
      <!-- 发卡表单 -->
      <el-col :xs="24" :lg="12">
        <transition name="slide-fade" appear>
          <div class="card generate-card hover-lift">
            <div class="card-header">
              <h3 class="card-title">
                <el-icon><Setting /></el-icon>
                发卡配置
              </h3>
            </div>
            
            <el-form
              ref="formRef"
              :model="form"
              :rules="rules"
              label-position="top"
              class="generate-form"
            >
              <el-form-item label="发卡数量" prop="count">
                <div class="count-input-wrapper">
                  <el-input-number
                    v-model="form.count"
                    :min="1"
                    :max="10000"
                    :step="100"
                    controls-position="right"
                    class="count-input"
                    :disabled="loading"
                  />
                  <span class="count-unit">张</span>
                </div>
                <div class="form-extra">
                  <span class="extra-hint">
                    <el-icon><InfoFilled /></el-icon>
                    单次最多生成 10,000 张
                  </span>
                  <div class="quick-count">
                    <el-button 
                      v-for="count in quickCounts" 
                      :key="count"
                      size="small"
                      :type="form.count === count ? 'primary' : 'default'"
                      :disabled="loading"
                      @click="form.count = count"
                      class="quick-btn"
                    >
                      {{ count }}
                    </el-button>
                  </div>
                </div>
              </el-form-item>
              
              <!-- 进度条 -->
              <transition name="fade">
                <div v-if="loading" class="progress-wrapper">
                  <el-progress 
                    :percentage="Math.min(generateProgress, 100)" 
                    :stroke-width="8"
                    :show-text="false"
                    class="generate-progress"
                  />
                  <span class="progress-text">正在生成 {{ form.count }} 张卡密...</span>
                </div>
              </transition>
              
              <el-form-item>
                <el-button
                  type="primary"
                  size="large"
                  :disabled="loading"
                  class="generate-btn ripple-btn"
                  @click="handleGenerate"
                >
                  <template v-if="!loading">
                    <el-icon class="btn-icon"><Plus /></el-icon>
                    立即发卡
                  </template>
                  <template v-else>
                    <span class="loading-dots">
                      <span></span><span></span><span></span>
                    </span>
                    生成中
                  </template>
                </el-button>
              </el-form-item>
            </el-form>
          </div>
        </transition>
        
        <!-- 发卡说明 -->
        <transition name="slide-fade" appear style="--delay: 0.1s">
          <div class="card tips-card hover-lift">
            <div class="card-header">
              <h3 class="card-title">
                <el-icon><InfoFilled /></el-icon>
                发卡说明
              </h3>
            </div>
            <ul class="tips-list">
              <li v-for="(tip, index) in tips" :key="index" class="tip-item" :style="{ animationDelay: `${index * 0.1}s` }">
                <span class="tip-icon" :class="tip.type">
                  <el-icon><component :is="tip.icon" /></el-icon>
                </span>
                <span class="tip-text">{{ tip.text }}</span>
              </li>
            </ul>
          </div>
        </transition>
      </el-col>
      
      <!-- 发卡结果 -->
      <el-col :xs="24" :lg="12">
        <transition name="bounce" mode="out-in">
          <div v-if="result.visible" class="card result-card success-card" key="result">
            <!-- SVG 成功动画 -->
            <div class="success-animation">
              <svg class="checkmark" viewBox="0 0 52 52">
                <circle class="checkmark-circle" cx="26" cy="26" r="24" fill="none"/>
                <path class="checkmark-check" fill="none" d="M14 27l7 7 16-16"/>
              </svg>
            </div>
            
            <h3 class="result-title">发卡成功！</h3>
            <p class="result-desc">
              成功生成 <strong class="count-highlight">{{ result.count }}</strong> 张卡密
            </p>
            
            <div class="result-info">
              <div class="info-item">
                <span class="info-label">批次号</span>
                <div class="info-value-wrapper">
                  <span class="info-value">{{ result.batchNumber }}</span>
                  <el-tooltip content="复制批次号" placement="top">
                    <el-button 
                      type="primary" 
                      circle
                      size="small"
                      class="copy-btn"
                      @click="copyBatchNumber"
                    >
                      <el-icon><CopyDocument /></el-icon>
                    </el-button>
                  </el-tooltip>
                </div>
              </div>
            </div>
            
            <div class="result-actions">
              <el-button type="primary" size="large" @click="goToCardList" class="action-btn primary">
                <el-icon><List /></el-icon>
                查看卡密列表
              </el-button>
              <el-button size="large" @click="resetForm" class="action-btn">
                <el-icon><RefreshRight /></el-icon>
                继续发卡
              </el-button>
            </div>
          </div>
          
          <!-- 空状态 -->
          <div v-else class="card empty-card" key="empty">
            <div class="empty-illustration">
              <div class="illustration-bg"></div>
              <el-icon class="illustration-icon"><Postcard /></el-icon>
              <div class="illustration-dots">
                <span></span><span></span><span></span>
              </div>
            </div>
            <h3 class="empty-title">等待发卡</h3>
            <p class="empty-desc">设置发卡数量后点击"立即发卡"按钮</p>
            <div class="empty-hint">
              <el-icon><ArrowLeft /></el-icon>
              <span>在左侧配置发卡参数</span>
            </div>
          </div>
        </transition>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { generateCards } from '@/api/card'
import toast from '@/utils/toast'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)
const generateProgress = ref(0)

const quickCounts = [100, 500, 1000, 2000, 5000]

// 提示列表
const tips = [
  { icon: 'Check', type: 'success', text: '每次发卡会自动生成一个批次号，格式为：YYYYMMDDHHmmss' },
  { icon: 'Check', type: 'success', text: '卡号为 9 位唯一数字，系统自动生成并校验唯一性' },
  { icon: 'Check', type: 'success', text: '密码为 6 位随机字符（包含数字和大写字母）' },
  { icon: 'Check', type: 'success', text: '新生成的卡密默认状态为"未使用"' },
  { icon: 'Warning', type: 'warning', text: '大批量发卡（>5000张）可能需要等待几秒钟' },
]

const form = reactive({
  count: 100,
})

const rules = {
  count: [
    { required: true, message: '请输入发卡数量', trigger: 'blur' },
  ],
}

const result = reactive({
  visible: false,
  batchNumber: '',
  count: 0,
})

// 预估时间
const estimatedTime = computed(() => {
  if (form.count <= 100) return '< 1 秒'
  if (form.count <= 1000) return '约 1-2 秒'
  if (form.count <= 5000) return '约 3-5 秒'
  return '约 5-10 秒'
})

const handleGenerate = async () => {
  try {
    await formRef.value.validate()
    loading.value = true
    generateProgress.value = 0
    
    // 模拟进度（更平滑）
    const progressInterval = setInterval(() => {
      if (generateProgress.value < 85) {
        generateProgress.value += Math.random() * 8 + 2
      }
    }, 150)
    
    // 最小展示时间，让用户看到进度动画
    const minDelay = new Promise(resolve => setTimeout(resolve, 1500))
    
    const [res] = await Promise.all([
      generateCards({ count: form.count }),
      minDelay
    ])
    
    clearInterval(progressInterval)
    
    // 平滑完成进度条
    const completeProgress = () => {
      return new Promise(resolve => {
        const step = () => {
          if (generateProgress.value < 100) {
            generateProgress.value = Math.min(generateProgress.value + 5, 100)
            requestAnimationFrame(step)
          } else {
            resolve()
          }
        }
        step()
      })
    }
    
    await completeProgress()
    
    // 延迟显示结果，让进度条完成动画
    setTimeout(() => {
      result.batchNumber = res.data
      result.count = form.count
      result.visible = true
      
      // 成功通知
      toast.notifySuccess(
        '发卡成功！', 
        `已生成 ${form.count} 张卡密，批次号: ${res.data}`
      )
    }, 300)
    
  } catch (error) {
    console.error('发卡失败:', error)
    toast.error('发卡失败，请重试')
  } finally {
    loading.value = false
    generateProgress.value = 0
  }
}

const goToCardList = () => {
  router.push({
    path: '/card/list',
    query: { batchNumber: result.batchNumber },
  })
}

const resetForm = () => {
  result.visible = false
  result.batchNumber = ''
  result.count = 0
  form.count = 100
}

const copyBatchNumber = async () => {
  try {
    await navigator.clipboard.writeText(result.batchNumber)
    toast.copySuccess('批次号已复制到剪贴板')
  } catch {
    toast.error('复制失败')
  }
}
</script>

<style lang="scss" scoped>
@import '@/assets/styles/variables.scss';

.page-subtitle {
  margin-top: $spacing-xs;
  font-size: $font-size-sm;
  color: $text-secondary;
}

// ==================== 发卡表单 ====================

.generate-card {
  margin-bottom: $spacing-base;
  transition: all 0.3s $ease-smooth;
  
  .card-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
  }
}

.generate-form {
  :deep(.el-form-item__label) {
    font-weight: $font-weight-semibold;
    font-size: $font-size-base;
    padding-bottom: $spacing-sm;
  }
}

.count-input-wrapper {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
}

.count-input {
  width: 200px;
  
  :deep(.el-input__inner) {
    font-size: $font-size-lg;
    font-weight: $font-weight-semibold;
  }
  
  :deep(.el-input-number__increase),
  :deep(.el-input-number__decrease) {
    transition: all 0.2s $ease-smooth;
    
    &:hover {
      color: $primary-color;
    }
  }
}

.count-unit {
  font-size: $font-size-md;
  color: $text-secondary;
  font-weight: $font-weight-medium;
}

.form-extra {
  margin-top: $spacing-md;
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: $spacing-sm;
}

.extra-hint {
  font-size: $font-size-xs;
  color: $text-secondary;
  display: flex;
  align-items: center;
  gap: 4px;
  
  .el-icon {
    font-size: 14px;
  }
}

.quick-count {
  display: flex;
  gap: $spacing-xs;
  
  .quick-btn {
    position: relative;
    overflow: hidden;
    transition: all 0.25s $ease-spring;
    
    // 点击波纹效果
    &::after {
      content: '';
      position: absolute;
      top: 50%;
      left: 50%;
      width: 0;
      height: 0;
      background: rgba(255, 255, 255, 0.4);
      border-radius: 50%;
      transform: translate(-50%, -50%);
      opacity: 0;
    }
    
    &:hover {
      transform: translateY(-2px);
    }
    
    &:active {
      transform: translateY(0) scale(0.95);
      
      &::after {
        animation: quickBtnRipple 0.4s ease-out;
      }
    }
    
    &.el-button--primary {
      box-shadow: 0 4px 12px rgba($primary-color, 0.3);
      animation: quickBtnSelect 0.3s $ease-spring;
      
      &:active {
        box-shadow: 0 2px 8px rgba($primary-color, 0.4);
      }
    }
  }
}

@keyframes quickBtnRipple {
  0% {
    width: 0;
    height: 0;
    opacity: 0.5;
  }
  100% {
    width: 200%;
    height: 200%;
    opacity: 0;
  }
}

@keyframes quickBtnSelect {
  0% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.08);
  }
  100% {
    transform: scale(1);
  }
}

// 进度条
.progress-wrapper {
  margin-bottom: $spacing-lg;
  padding: $spacing-base;
  background: $fill-lighter;
  border-radius: $radius-lg;
  
  .generate-progress {
    margin-bottom: $spacing-sm;
    
    :deep(.el-progress-bar__inner) {
      background: linear-gradient(90deg, $primary-color, $primary-light);
      transition: width 0.3s $ease-smooth;
    }
  }
  
  .progress-text {
    font-size: $font-size-xs;
    color: $text-secondary;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: $spacing-xs;
  }
}

.generate-btn {
  width: 100%;
  height: 52px;
  font-size: $font-size-md;
  font-weight: $font-weight-semibold;
  background: linear-gradient(135deg, $primary-color 0%, $primary-dark 100%);
  border: none;
  position: relative;
  overflow: hidden;
  transition: all 0.3s $ease-smooth;
  
  // 光泽扫过效果
  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: -100%;
    width: 100%;
    height: 100%;
    background: linear-gradient(
      90deg,
      transparent,
      rgba(255, 255, 255, 0.2),
      transparent
    );
    transition: left 0.5s ease;
  }
  
  // 点击波纹效果
  &::after {
    content: '';
    position: absolute;
    top: 50%;
    left: 50%;
    width: 0;
    height: 0;
    background: rgba(255, 255, 255, 0.3);
    border-radius: 50%;
    transform: translate(-50%, -50%);
    opacity: 0;
  }
  
  .btn-icon {
    margin-right: 8px;
    transition: transform 0.3s $ease-spring;
  }
  
  &:hover:not(:disabled) {
    transform: translateY(-2px);
    box-shadow: 0 8px 25px rgba($primary-color, 0.4);
    
    &::before {
      left: 100%;
    }
    
    .btn-icon {
      transform: rotate(90deg);
    }
  }
  
  &:active:not(:disabled) {
    transform: translateY(0) scale(0.98);
    box-shadow: 0 4px 15px rgba($primary-color, 0.3);
    
    &::after {
      animation: generateBtnRipple 0.6s ease-out;
    }
    
    .btn-icon {
      transform: rotate(180deg) scale(1.1);
    }
  }
  
  .loading-dots {
    display: inline-flex;
    gap: 4px;
    margin-right: 8px;
    
    span {
      width: 6px;
      height: 6px;
      background: #fff;
      border-radius: 50%;
      animation: dotPulse 1.4s infinite ease-in-out;
      
      &:nth-child(1) { animation-delay: -0.32s; }
      &:nth-child(2) { animation-delay: -0.16s; }
    }
  }
}

@keyframes generateBtnRipple {
  0% {
    width: 0;
    height: 0;
    opacity: 0.6;
  }
  100% {
    width: 300%;
    height: 300%;
    opacity: 0;
  }
}

@keyframes dotPulse {
  0%, 80%, 100% { transform: scale(0.6); opacity: 0.5; }
  40% { transform: scale(1); opacity: 1; }
}

// ==================== 发卡说明 ====================

.tips-card {
  .card-header {
    margin-bottom: $spacing-base;
  }
}

.tips-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.tip-item {
  display: flex;
  align-items: flex-start;
  gap: $spacing-md;
  padding: $spacing-sm 0;
  animation: tipFadeIn 0.4s $ease-out both;
  
  .tip-icon {
    width: 24px;
    height: 24px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
    margin-top: 2px;
    
    &.success {
      background: $success-bg;
      color: $success-color;
    }
    
    &.warning {
      background: $warning-bg;
      color: $warning-color;
    }
    
    .el-icon {
      font-size: 12px;
    }
  }
  
  .tip-text {
    color: $text-regular;
    font-size: $font-size-sm;
    line-height: 1.6;
  }
}

@keyframes tipFadeIn {
  from {
    opacity: 0;
    transform: translateX(-10px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

// ==================== 发卡结果 ====================

.result-card {
  text-align: center;
  padding: $spacing-3xl;
}

.success-card {
  background: linear-gradient(135deg, #f0fff4 0%, #ffffff 100%);
  border: 1px solid rgba($success-color, 0.2);
}

// SVG 成功动画
.success-animation {
  margin-bottom: $spacing-xl;
}

.checkmark {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  display: block;
  stroke-width: 3;
  stroke: $success-color;
  stroke-miterlimit: 10;
  margin: 0 auto;
  box-shadow: 0 0 0 rgba($success-color, 0.25);
  animation: checkmarkScale 0.3s ease-in-out 0.9s both;
}

.checkmark-circle {
  stroke-dasharray: 166;
  stroke-dashoffset: 166;
  stroke-width: 3;
  stroke-miterlimit: 10;
  stroke: $success-color;
  fill: rgba($success-color, 0.08);
  animation: checkmarkStroke 0.6s cubic-bezier(0.65, 0, 0.45, 1) forwards;
}

.checkmark-check {
  transform-origin: 50% 50%;
  stroke-dasharray: 48;
  stroke-dashoffset: 48;
  stroke-width: 3;
  stroke-linecap: round;
  stroke-linejoin: round;
  animation: checkmarkStroke 0.3s cubic-bezier(0.65, 0, 0.45, 1) 0.8s forwards;
}

@keyframes checkmarkStroke {
  100% {
    stroke-dashoffset: 0;
  }
}

@keyframes checkmarkScale {
  0%, 100% {
    transform: none;
  }
  50% {
    transform: scale3d(1.1, 1.1, 1);
  }
}

.result-title {
  font-size: $font-size-xl;
  font-weight: $font-weight-bold;
  color: $success-color;
  margin: 0 0 $spacing-sm;
  animation: fadeInUp 0.5s $ease-out 0.8s both;
}

.result-desc {
  font-size: $font-size-base;
  color: $text-secondary;
  margin: 0 0 $spacing-xl;
  animation: fadeInUp 0.5s $ease-out 0.9s both;
  
  .count-highlight {
    color: $success-color;
    font-size: $font-size-2xl;
    font-weight: $font-weight-bold;
    margin: 0 4px;
  }
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(15px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.result-info {
  background: rgba($success-color, 0.08);
  border: 1px solid rgba($success-color, 0.2);
  border-radius: $radius-xl;
  padding: $spacing-lg $spacing-xl;
  margin-bottom: $spacing-xl;
  animation: fadeInUp 0.5s $ease-out 1s both;
}

.info-item {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: $spacing-md;
  flex-wrap: wrap;
  
  .info-label {
    font-size: $font-size-sm;
    color: $text-secondary;
  }
  
  .info-value-wrapper {
    display: inline-flex;
    align-items: center;
    gap: $spacing-sm;
    min-height: 40px;
  }
  
  .info-value {
    font-size: $font-size-lg;
    font-weight: $font-weight-bold;
    color: $text-primary;
    font-family: $font-family-mono;
    background: $bg-white;
    padding: $spacing-xs $spacing-md;
    border-radius: $radius-md;
    border: 1px solid $border-light;
    line-height: 1.5;
    display: inline-flex;
    align-items: center;
  }
  
  .copy-btn {
    flex-shrink: 0;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    transition: all 0.3s $ease-spring;
    
    :deep(.el-icon) {
      display: flex;
      align-items: center;
      justify-content: center;
    }
    
    &:hover {
      transform: scale(1.1);
    }
  }
}

.result-actions {
  display: flex;
  justify-content: center;
  gap: $spacing-md;
  animation: fadeInUp 0.5s $ease-out 1.1s both;
  
  .action-btn {
    min-width: 140px;
    transition: all 0.3s $ease-smooth;
    
    &.primary {
      background: linear-gradient(135deg, $success-color 0%, darken($success-color, 10%) 100%);
      border: none;
      
      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 8px 20px rgba($success-color, 0.4);
      }
    }
  }
}

// ==================== 空状态 ====================

.empty-card {
  text-align: center;
  padding: $spacing-4xl $spacing-xl;
  min-height: 450px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
}

.empty-illustration {
  position: relative;
  width: 140px;
  height: 140px;
  margin-bottom: $spacing-xl;
  
  .illustration-bg {
    position: absolute;
    inset: 0;
    border-radius: 50%;
    background: linear-gradient(135deg, $fill-light 0%, $fill-lighter 100%);
    animation: bgPulse 3s ease-in-out infinite;
  }
  
  .illustration-icon {
    position: relative;
    z-index: 1;
    width: 100%;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 64px;
    color: $border-color;
    animation: iconFloat 3s ease-in-out infinite;
  }
  
  .illustration-dots {
    position: absolute;
    bottom: 10px;
    left: 50%;
    transform: translateX(-50%);
    display: flex;
    gap: 6px;
    
    span {
      width: 8px;
      height: 8px;
      border-radius: 50%;
      background: $border-light;
      animation: dotBounce 1.4s ease-in-out infinite;
      
      &:nth-child(1) { animation-delay: 0s; }
      &:nth-child(2) { animation-delay: 0.2s; }
      &:nth-child(3) { animation-delay: 0.4s; }
    }
  }
}

@keyframes bgPulse {
  0%, 100% { transform: scale(1); opacity: 1; }
  50% { transform: scale(1.05); opacity: 0.8; }
}

@keyframes iconFloat {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-8px); }
}

@keyframes dotBounce {
  0%, 80%, 100% { transform: translateY(0); }
  40% { transform: translateY(-8px); }
}

.empty-title {
  font-size: $font-size-lg;
  font-weight: $font-weight-semibold;
  color: $text-regular;
  margin: 0 0 $spacing-sm;
}

.empty-desc {
  font-size: $font-size-sm;
  color: $text-secondary;
  margin: 0 0 $spacing-lg;
}

.empty-hint {
  display: flex;
  align-items: center;
  gap: $spacing-xs;
  font-size: $font-size-xs;
  color: $text-disabled;
  animation: hintPulse 2s ease-in-out infinite;
  
  .el-icon {
    animation: arrowBounce 1s ease-in-out infinite;
  }
}

@keyframes hintPulse {
  0%, 100% { opacity: 0.6; }
  50% { opacity: 1; }
}

@keyframes arrowBounce {
  0%, 100% { transform: translateX(0); }
  50% { transform: translateX(-5px); }
}

// ==================== 动画 ====================

.slide-fade-enter-active {
  transition: all 0.4s $ease-smooth;
  transition-delay: var(--delay, 0s);
}

.slide-fade-leave-active {
  transition: all 0.2s $ease-in;
}

.slide-fade-enter-from {
  transform: translateY(20px);
  opacity: 0;
}

.slide-fade-leave-to {
  transform: translateY(-10px);
  opacity: 0;
}

.bounce-enter-active {
  animation: bounceIn 0.6s $ease-spring;
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
</style>
