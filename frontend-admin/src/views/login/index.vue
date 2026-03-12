<template>
  <div class="login-page">
    <!-- 背景装饰 -->
    <div class="login-bg">
      <div class="bg-gradient"></div>
      <div class="bg-pattern"></div>
      <div class="bg-circles">
        <div class="circle circle-1"></div>
        <div class="circle circle-2"></div>
        <div class="circle circle-3"></div>
        <div class="circle circle-4"></div>
        <div class="circle circle-5"></div>
      </div>
      <!-- 粒子效果 -->
      <div class="particles">
        <span v-for="i in 20" :key="i" class="particle" :style="getParticleStyle(i)"></span>
      </div>
    </div>
    
    <!-- 登录卡片 -->
    <div class="login-wrapper">
      <transition name="zoom" appear>
        <div class="login-card">
          <!-- Logo 区域 -->
          <div class="login-header">
            <div class="logo-wrapper">
              <div class="logo-icon" :class="{ 'animate-pulse': loading }">
                <el-icon :size="32"><CreditCard /></el-icon>
                <div class="logo-ring"></div>
              </div>
              <div class="logo-text">
                <h1>游戏卡密管理系统</h1>
                <p class="typing-text">Card Management System</p>
              </div>
            </div>
          </div>
          
          <!-- 表单区域 -->
          <div class="login-body">
            <el-form
              ref="loginFormRef"
              :model="loginForm"
              :rules="loginRules"
              class="login-form"
              @keyup.enter="handleLogin"
            >
              <transition name="slide-fade" appear>
                <el-form-item prop="username" class="form-item-animated" style="--delay: 0.1s">
                  <el-input
                    v-model="loginForm.username"
                    placeholder="请输入账号"
                    size="large"
                    :prefix-icon="User"
                    @focus="inputFocus = 'username'"
                    @blur="inputFocus = ''"
                    :class="{ 'input-focused': inputFocus === 'username' }"
                  />
                </el-form-item>
              </transition>
              
              <transition name="slide-fade" appear>
                <el-form-item prop="password" class="form-item-animated" style="--delay: 0.2s">
                  <el-input
                    v-model="loginForm.password"
                    type="password"
                    placeholder="请输入密码"
                    size="large"
                    :prefix-icon="Lock"
                    show-password
                    @focus="inputFocus = 'password'"
                    @blur="inputFocus = ''"
                    :class="{ 'input-focused': inputFocus === 'password' }"
                  />
                </el-form-item>
              </transition>
              
              <transition name="slide-fade" appear>
                <el-form-item class="form-item-animated" style="--delay: 0.3s">
                  <el-button
                    type="primary"
                    size="large"
                    :loading="loading"
                    class="login-btn ripple-btn"
                    @click="handleLogin"
                  >
                    <transition name="fade" mode="out-in">
                      <span v-if="!loading" key="login">
                        <el-icon class="btn-icon"><Right /></el-icon>
                        登 录
                      </span>
                      <span v-else key="loading" class="loading-text">
                        <span class="dot-loader">
                          <span></span><span></span><span></span>
                        </span>
                        登录中
                      </span>
                    </transition>
                  </el-button>
                </el-form-item>
              </transition>
            </el-form>
            
            <!-- 快捷登录提示 -->
            <transition name="fade" appear>
              <div class="quick-tips">
                <el-tooltip content="管理员: admin / admin123" placement="top">
                  <el-tag size="small" type="info" effect="plain" class="tip-tag">
                    <el-icon><InfoFilled /></el-icon>
                    演示账号
                  </el-tag>
                </el-tooltip>
              </div>
            </transition>
            
            <!-- 其他入口 -->
            <div class="login-footer">
              <el-divider>
                <span class="divider-text">其他入口</span>
              </el-divider>
              <div class="footer-links">
                <el-button text type="primary" @click="goPublicQuery" class="public-link">
                  <el-icon><Search /></el-icon>
                  卡密查询（无需登录）
                  <el-icon class="link-arrow"><ArrowRight /></el-icon>
                </el-button>
              </div>
            </div>
          </div>
        </div>
      </transition>
      
      <!-- 底部信息 -->
      <transition name="fade" appear>
        <div class="login-info">
          <div class="info-badges">
            <span class="info-badge"><el-icon><Lock /></el-icon> 安全</span>
            <span class="info-badge"><el-icon><Timer /></el-icon> 高效</span>
            <span class="info-badge"><el-icon><Promotion /></el-icon> 便捷</span>
          </div>
          <p>© 2024 游戏卡密管理系统</p>
        </div>
      </transition>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { User, Lock, Right, ArrowRight, InfoFilled, Timer, Promotion } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/modules/user'
import toast from '@/utils/toast'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const loginFormRef = ref(null)
const loading = ref(false)
const inputFocus = ref('')

const loginForm = reactive({
  username: '',
  password: '',
})

const loginRules = {
  username: [
    { required: true, message: '请输入账号', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
  ],
}

// 生成粒子样式
const getParticleStyle = (index) => {
  const size = Math.random() * 4 + 2
  return {
    width: `${size}px`,
    height: `${size}px`,
    left: `${Math.random() * 100}%`,
    top: `${Math.random() * 100}%`,
    animationDelay: `${Math.random() * 5}s`,
    animationDuration: `${Math.random() * 10 + 10}s`,
  }
}

const handleLogin = async () => {
  try {
    await loginFormRef.value.validate()
    loading.value = true
    
    await userStore.login(loginForm)
    
    // 使用自定义 toast
    toast.notifySuccess('登录成功', `欢迎回来，${userStore.userInfo?.realName || '用户'}！`)
    
    const redirect = route.query.redirect || '/'
    router.push(redirect)
  } catch (error) {
    console.error('登录失败:', error)
    // 错误消息已经由 axios 拦截器显示，这里不再重复提示
  } finally {
    loading.value = false
  }
}

const goPublicQuery = () => {
  router.push('/public/query')
}
</script>

<style lang="scss" scoped>
@import '@/assets/styles/variables.scss';

.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
}

// ==================== 背景 ====================

.login-bg {
  position: absolute;
  inset: 0;
  z-index: 0;
}

.bg-gradient {
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 50%, #6B8DD6 100%);
  animation: gradientShift 10s ease-in-out infinite;
}

@keyframes gradientShift {
  0%, 100% {
    background-position: 0% 50%;
  }
  50% {
    background-position: 100% 50%;
  }
}

.bg-pattern {
  position: absolute;
  inset: 0;
  background-image: url("data:image/svg+xml,%3Csvg width='60' height='60' viewBox='0 0 60 60' xmlns='http://www.w3.org/2000/svg'%3E%3Cg fill='none' fill-rule='evenodd'%3E%3Cg fill='%23ffffff' fill-opacity='0.05'%3E%3Cpath d='M36 34v-4h-2v4h-4v2h4v4h2v-4h4v-2h-4zm0-30V0h-2v4h-4v2h4v4h2V6h4V4h-4zM6 34v-4H4v4H0v2h4v4h2v-4h4v-2H6zM6 4V0H4v4H0v2h4v4h2V6h4V4H6z'/%3E%3C/g%3E%3C/g%3E%3C/svg%3E");
  opacity: 0.5;
}

.bg-circles {
  position: absolute;
  inset: 0;
  overflow: hidden;
  
  .circle {
    position: absolute;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.08);
    animation: float 20s infinite ease-in-out;
  }
  
  .circle-1 {
    width: 600px;
    height: 600px;
    top: -200px;
    left: -200px;
    animation-delay: 0s;
  }
  
  .circle-2 {
    width: 400px;
    height: 400px;
    bottom: -100px;
    right: -100px;
    animation-delay: -5s;
  }
  
  .circle-3 {
    width: 200px;
    height: 200px;
    top: 50%;
    left: 15%;
    animation-delay: -10s;
  }
  
  .circle-4 {
    width: 150px;
    height: 150px;
    top: 20%;
    right: 20%;
    animation-delay: -3s;
  }
  
  .circle-5 {
    width: 100px;
    height: 100px;
    bottom: 20%;
    left: 10%;
    animation-delay: -7s;
  }
}

// 粒子效果
.particles {
  position: absolute;
  inset: 0;
  overflow: hidden;
  
  .particle {
    position: absolute;
    background: rgba(255, 255, 255, 0.5);
    border-radius: 50%;
    animation: particleFloat 15s infinite linear;
    
    &:nth-child(odd) {
      animation-direction: reverse;
    }
  }
}

@keyframes particleFloat {
  0% {
    transform: translateY(100vh) rotate(0deg);
    opacity: 0;
  }
  10% {
    opacity: 1;
  }
  90% {
    opacity: 1;
  }
  100% {
    transform: translateY(-100vh) rotate(720deg);
    opacity: 0;
  }
}

@keyframes float {
  0%, 100% {
    transform: translate(0, 0) scale(1);
  }
  25% {
    transform: translate(10px, -20px) scale(1.02);
  }
  50% {
    transform: translate(-5px, 15px) scale(0.98);
  }
  75% {
    transform: translate(15px, 5px) scale(1.01);
  }
}

// ==================== 登录卡片 ====================

.login-wrapper {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 100%;
  max-width: 420px;
  padding: 20px;
}

.login-card {
  width: 100%;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-radius: $radius-2xl;
  box-shadow: 
    0 25px 50px -12px rgba(0, 0, 0, 0.25),
    0 0 0 1px rgba(255, 255, 255, 0.1) inset;
  overflow: hidden;
  transition: transform 0.3s $ease-smooth, box-shadow 0.3s $ease-smooth;
  
  &:hover {
    transform: translateY(-5px);
    box-shadow: 
      0 30px 60px -15px rgba(0, 0, 0, 0.3),
      0 0 0 1px rgba(255, 255, 255, 0.15) inset;
  }
}

// 缩放动画
.zoom-enter-active {
  animation: zoomIn 0.6s $ease-spring;
}

@keyframes zoomIn {
  from {
    opacity: 0;
    transform: scale(0.8) translateY(30px);
  }
  to {
    opacity: 1;
    transform: scale(1) translateY(0);
  }
}

.login-header {
  padding: 40px 40px 0;
  text-align: center;
}

.logo-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}

.logo-icon {
  width: 72px;
  height: 72px;
  border-radius: $radius-xl;
  background: linear-gradient(135deg, $primary-color 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  box-shadow: 0 8px 24px rgba($primary-color, 0.4);
  position: relative;
  transition: all 0.3s $ease-spring;
  
  &:hover {
    transform: scale(1.05) rotate(5deg);
  }
  
  &.animate-pulse {
    animation: logoPulse 1s ease-in-out infinite;
  }
  
  .logo-ring {
    position: absolute;
    inset: -4px;
    border-radius: inherit;
    border: 2px solid rgba($primary-color, 0.3);
    animation: ringPulse 2s ease-in-out infinite;
  }
}

@keyframes logoPulse {
  0%, 100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.05);
  }
}

@keyframes ringPulse {
  0%, 100% {
    transform: scale(1);
    opacity: 1;
  }
  50% {
    transform: scale(1.2);
    opacity: 0;
  }
}

.logo-text {
  h1 {
    font-size: $font-size-xl;
    font-weight: $font-weight-bold;
    color: $text-primary;
    margin: 0 0 4px;
    letter-spacing: 1px;
    animation: fadeInUp 0.6s $ease-out 0.2s both;
  }
  
  .typing-text {
    font-size: $font-size-xs;
    color: $text-secondary;
    margin: 0;
    text-transform: uppercase;
    letter-spacing: 2px;
    animation: fadeInUp 0.6s $ease-out 0.3s both;
  }
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

// ==================== 表单 ====================

.login-body {
  padding: 32px 40px 40px;
}

.login-form {
  .el-form-item {
    margin-bottom: 24px;
  }
  
  .form-item-animated {
    animation: slideInUp 0.5s $ease-out calc(var(--delay, 0s)) both;
  }
  
  @keyframes slideInUp {
    from {
      opacity: 0;
      transform: translateY(20px);
    }
    to {
      opacity: 1;
      transform: translateY(0);
    }
  }
  
  :deep(.el-input) {
    --el-input-bg-color: #{$fill-light};
    
    .el-input__wrapper {
      padding: 4px 16px;
      border-radius: $radius-lg;
      box-shadow: none;
      border: 2px solid transparent;
      transition: all 0.3s $ease-smooth;
      
      &:hover {
        border-color: rgba($primary-color, 0.3);
        background-color: rgba($fill-light, 0.8);
      }
      
      &.is-focus {
        border-color: $primary-color;
        background-color: #fff;
        box-shadow: 0 0 0 4px rgba($primary-color, 0.1);
      }
    }
    
    .el-input__inner {
      height: 44px;
      font-size: $font-size-base;
    }
    
    .el-input__prefix {
      font-size: 18px;
      color: $text-secondary;
      transition: color 0.3s;
    }
    
    &.input-focused {
      .el-input__prefix {
        color: $primary-color;
      }
    }
  }
}

.login-btn {
  width: 100%;
  height: 52px;
  font-size: $font-size-md;
  font-weight: $font-weight-semibold;
  border-radius: $radius-lg;
  background: linear-gradient(135deg, $primary-color 0%, #764ba2 100%);
  background-size: 200% 200%;
  border: none;
  letter-spacing: 2px;
  transition: all 0.3s $ease-smooth;
  position: relative;
  overflow: hidden;
  
  // 按钮内容水平居中排列
  > span {
    display: inline-flex;
    align-items: center;
    justify-content: center;
  }
  
  .btn-icon {
    margin-right: 8px;
    transition: transform 0.3s $ease-spring;
  }
  
  &:hover {
    transform: translateY(-3px);
    box-shadow: 0 10px 30px rgba($primary-color, 0.4);
    background-position: 100% 0;
    
    .btn-icon {
      transform: translateX(3px);
    }
  }
  
  &:active {
    transform: translateY(-1px);
  }
  
  .loading-text {
    display: inline-flex;
    align-items: center;
    gap: 8px;
  }
  
  .dot-loader {
    display: flex;
    gap: 4px;
    
    span {
      width: 6px;
      height: 6px;
      background: #fff;
      border-radius: 50%;
      animation: dotBounce 1.4s infinite ease-in-out both;
      
      &:nth-child(1) { animation-delay: -0.32s; }
      &:nth-child(2) { animation-delay: -0.16s; }
      &:nth-child(3) { animation-delay: 0s; }
    }
  }
}

@keyframes dotBounce {
  0%, 80%, 100% {
    transform: scale(0.6);
    opacity: 0.5;
  }
  40% {
    transform: scale(1);
    opacity: 1;
  }
}

// 快捷提示
.quick-tips {
  display: flex;
  justify-content: center;
  margin-top: -8px;
  margin-bottom: 16px;
  
  .tip-tag {
    cursor: pointer;
    transition: all 0.3s $ease-smooth;
    padding: 6px 12px;
    font-size: $font-size-xs;
    border-radius: $radius-full;
    background: rgba($primary-color, 0.08);
    border-color: rgba($primary-color, 0.2);
    color: $primary-color;
    
    &:hover {
      transform: scale(1.05);
      background: rgba($primary-color, 0.12);
      border-color: rgba($primary-color, 0.3);
    }
    
    // 修复 el-tag 内部图标和文字的排列
    :deep(.el-tag__content) {
      display: inline-flex;
      align-items: center;
      gap: 6px;
      
      .el-icon {
        font-size: 14px;
      }
    }
  }
}

// ==================== 底部 ====================

.login-footer {
  margin-top: 8px;
  
  :deep(.el-divider) {
    margin: 24px 0;
    
    .el-divider__text {
      background-color: #fff;
      padding: 0 16px;
    }
  }
  
  .divider-text {
    font-size: $font-size-xs;
    color: $text-disabled;
  }
}

.footer-links {
  display: flex;
  justify-content: center;
  
  .public-link {
    font-size: $font-size-sm;
    display: flex;
    align-items: center;
    gap: 4px;
    transition: all 0.3s $ease-smooth;
    
    .link-arrow {
      transition: transform 0.3s $ease-spring;
      opacity: 0;
      margin-left: -8px;
    }
    
    &:hover {
      .link-arrow {
        opacity: 1;
        margin-left: 0;
        transform: translateX(3px);
      }
    }
  }
}

.login-info {
  margin-top: 32px;
  text-align: center;
  
  .info-badges {
    display: flex;
    justify-content: center;
    gap: 16px;
    margin-bottom: 12px;
    
    .info-badge {
      display: flex;
      align-items: center;
      gap: 4px;
      font-size: $font-size-xs;
      color: rgba(255, 255, 255, 0.8);
      padding: 4px 10px;
      background: rgba(255, 255, 255, 0.1);
      border-radius: $radius-full;
      backdrop-filter: blur(10px);
      transition: all 0.3s $ease-smooth;
      
      &:hover {
        background: rgba(255, 255, 255, 0.2);
        transform: translateY(-2px);
      }
      
      .el-icon {
        font-size: 12px;
      }
    }
  }
  
  p {
    font-size: $font-size-xs;
    color: rgba(255, 255, 255, 0.6);
    margin: 0;
  }
}

// ==================== 响应式 ====================

@media screen and (max-width: $breakpoint-sm) {
  .login-header {
    padding: 32px 24px 0;
  }
  
  .login-body {
    padding: 24px;
  }
  
  .logo-icon {
    width: 60px;
    height: 60px;
  }
  
  .logo-text h1 {
    font-size: $font-size-lg;
  }
  
  .info-badges {
    flex-wrap: wrap;
    gap: 8px !important;
  }
}

// ==================== 动画类 ====================

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s $ease-smooth;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.slide-fade-enter-active {
  transition: all 0.4s $ease-smooth;
}

.slide-fade-leave-active {
  transition: all 0.2s $ease-in;
}

.slide-fade-enter-from {
  opacity: 0;
  transform: translateY(15px);
}

.slide-fade-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}
</style>
