<template>
  <div class="error-page">
    <div class="error-content">
      <div class="error-illustration">
        <div class="error-code">404</div>
        <div class="error-icon">
          <el-icon><Warning /></el-icon>
        </div>
      </div>
      <h1 class="error-title">页面不存在</h1>
      <p class="error-desc">抱歉，您访问的页面不存在或已被删除</p>
      <div class="error-actions">
        <el-button type="primary" size="large" @click="goHome">
          <el-icon><HomeFilled /></el-icon>
          返回首页
        </el-button>
        <el-button size="large" @click="goBack">
          <el-icon><Back /></el-icon>
          返回上页
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

const goHome = () => router.push('/')
const goBack = () => router.go(-1)
</script>

<style lang="scss" scoped>
@import '@/assets/styles/variables.scss';

.error-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(180deg, $bg-page 0%, $fill-light 100%);
  padding: 20px;
}

.error-content {
  text-align: center;
  animation: fadeIn 0.6s $ease-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.error-illustration {
  position: relative;
  margin-bottom: 32px;
}

.error-code {
  font-size: 160px;
  font-weight: $font-weight-bold;
  background: linear-gradient(135deg, $primary-color 0%, $primary-light 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  line-height: 1;
  letter-spacing: -8px;
  opacity: 0.2;
}

.error-icon {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: $bg-white;
  box-shadow: $shadow-lg;
  display: flex;
  align-items: center;
  justify-content: center;
  
  .el-icon {
    font-size: 40px;
    color: $warning-color;
  }
}

.error-title {
  font-size: $font-size-2xl;
  font-weight: $font-weight-semibold;
  color: $text-primary;
  margin: 0 0 12px;
}

.error-desc {
  font-size: $font-size-base;
  color: $text-secondary;
  margin: 0 0 32px;
}

.error-actions {
  display: flex;
  justify-content: center;
  gap: 16px;
}
</style>
