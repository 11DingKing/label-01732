<template>
  <el-container class="layout-container">
    <!-- 侧边栏 -->
    <el-aside :width="isCollapse ? '64px' : '220px'" class="layout-aside" :class="{ collapsed: isCollapse }">
      <!-- Logo -->
      <div class="logo" :class="{ collapsed: isCollapse }">
        <div class="logo-icon">
          <el-icon :size="28"><CreditCard /></el-icon>
        </div>
        <transition name="logo-text">
          <span v-show="!isCollapse" class="logo-text">卡密管理</span>
        </transition>
      </div>
      
      <!-- 导航菜单 -->
      <el-scrollbar class="menu-scrollbar">
        <el-menu
          :default-active="activeMenu"
          :collapse="isCollapse"
          :collapse-transition="false"
          router
          class="layout-menu"
        >
          <el-menu-item index="/dashboard">
            <el-icon><DataAnalysis /></el-icon>
            <template #title>数据统计</template>
          </el-menu-item>
          
          <el-sub-menu index="card">
            <template #title>
              <el-icon><Postcard /></el-icon>
              <span>发卡管理</span>
            </template>
            <el-menu-item index="/card/generate">
              <el-icon><Plus /></el-icon>
              <template #title>一键发卡</template>
            </el-menu-item>
            <el-menu-item index="/card/list">
              <el-icon><List /></el-icon>
              <template #title>卡密列表</template>
            </el-menu-item>
            <el-menu-item index="/card/batch">
              <el-icon><FolderOpened /></el-icon>
              <template #title>批次管理</template>
            </el-menu-item>
          </el-sub-menu>
          
          <el-sub-menu index="verify">
            <template #title>
              <el-icon><CircleCheck /></el-icon>
              <span>核销管理</span>
            </template>
            <el-menu-item index="/verify/use">
              <el-icon><Select /></el-icon>
              <template #title>卡密核销</template>
            </el-menu-item>
            <el-menu-item index="/verify/history">
              <el-icon><Document /></el-icon>
              <template #title>核销记录</template>
            </el-menu-item>
          </el-sub-menu>
          
          <el-menu-item v-if="userStore.isAdmin" index="/user/list">
            <el-icon><UserFilled /></el-icon>
            <template #title>用户管理</template>
          </el-menu-item>
        </el-menu>
      </el-scrollbar>
      
      <!-- 侧边栏底部 -->
      <div class="aside-footer" :class="{ collapsed: isCollapse }">
        <div class="collapse-btn" @click="toggleCollapse">
          <el-icon :size="18">
            <DArrowLeft v-if="!isCollapse" />
            <DArrowRight v-else />
          </el-icon>
        </div>
      </div>
    </el-aside>
    
    <!-- 主内容区 -->
    <el-container class="main-container">
      <!-- 头部 -->
      <el-header class="layout-header">
        <div class="header-left">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/' }">
              <el-icon><HomeFilled /></el-icon>
            </el-breadcrumb-item>
            <el-breadcrumb-item v-if="$route.meta.title">
              {{ $route.meta.title }}
            </el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        
        <div class="header-right">
          <!-- 公开查询入口 -->
          <el-tooltip content="卡密公开查询" placement="bottom">
            <div class="header-action" @click="goPublicQuery">
              <el-icon :size="18"><Search /></el-icon>
            </div>
          </el-tooltip>
          
          <!-- 刷新 -->
          <el-tooltip content="刷新页面" placement="bottom">
            <div class="header-action" :class="{ 'is-spinning': isRefreshing }" @click="refreshPage">
              <el-icon :size="18"><Refresh /></el-icon>
            </div>
          </el-tooltip>
          
          <!-- 用户下拉 -->
          <el-dropdown @command="handleCommand" trigger="click">
            <div class="user-dropdown">
              <el-avatar :size="32" class="user-avatar">
                {{ avatarText }}
              </el-avatar>
              <div class="user-info">
                <span class="user-name">{{ userStore.realName }}</span>
                <span class="user-role">{{ userStore.roleName }}</span>
              </div>
              <el-icon class="dropdown-arrow"><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item disabled>
                  <el-icon><User /></el-icon>
                  账号：{{ userStore.username }}
                </el-dropdown-item>
                <el-dropdown-item divided command="logout">
                  <el-icon><SwitchButton /></el-icon>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      
      <!-- 内容区 -->
      <el-main class="layout-main">
        <router-view v-slot="{ Component }">
          <transition name="slide-fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/store/modules/user'
import toast from '@/utils/toast'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const isCollapse = ref(false)
const isRefreshing = ref(false)

const activeMenu = computed(() => route.path)
const avatarText = computed(() => userStore.realName?.charAt(0) || 'U')

const toggleCollapse = () => {
  isCollapse.value = !isCollapse.value
}

const goPublicQuery = () => {
  window.open('/public/query', '_blank')
}

const refreshPage = async () => {
  if (isRefreshing.value) return
  
  isRefreshing.value = true
  toast.info('刷新中...')
  
  // 添加小延迟以显示动画
  await new Promise(resolve => setTimeout(resolve, 300))
  router.go(0)
}

const handleCommand = async (command) => {
  if (command === 'logout') {
    try {
      await toast.confirm('确定要退出登录吗？', '退出确认', {
        confirmText: '确定退出',
        type: 'warning',
      })
      userStore.logout()
      router.push('/login')
      toast.success('已安全退出登录')
    } catch (e) {
      // 取消退出
    }
  }
}
</script>

<style lang="scss" scoped>
@import '@/assets/styles/variables.scss';

.layout-container {
  height: 100vh;
  overflow: hidden;
}

// ==================== 侧边栏 ====================

.layout-aside {
  background: linear-gradient(180deg, #1e293b 0%, #0f172a 100%);
  display: flex;
  flex-direction: column;
  transition: width $transition-slow $ease-in-out;
  overflow: hidden;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.15);

  &.collapsed {
    overflow-x: visible;
  }
}

.logo {
  height: 64px;
  display: flex;
  align-items: center;
  padding: 0 20px;
  gap: 12px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
  flex-shrink: 0;
  
  &.collapsed {
    justify-content: center;
    padding: 0;
  }

  .logo-icon {
    width: 40px;
    height: 40px;
    border-radius: $radius-lg;
    background: linear-gradient(135deg, $primary-color 0%, $primary-dark 100%);
    display: flex;
    align-items: center;
    justify-content: center;
    color: #fff;
    flex-shrink: 0;
  }

  .logo-text {
    font-size: 18px;
    font-weight: $font-weight-bold;
    color: #fff;
    white-space: nowrap;
    letter-spacing: 1px;
  }
}

.logo-text-enter-active,
.logo-text-leave-active {
  transition: all $transition-base $ease-in-out;
}

.logo-text-enter-from,
.logo-text-leave-to {
  opacity: 0;
  transform: translateX(-10px);
}

.menu-scrollbar {
  flex: 1;
  overflow: hidden;
  min-height: 0;
}

.layout-aside.collapsed .menu-scrollbar {
  padding-bottom: 4px;
}

.layout-menu {
  border-right: none;
  background: transparent;
  padding: 12px 8px;

  &:not(.el-menu--collapse) {
    width: 100%;
  }

  :deep(.el-menu-item),
  :deep(.el-sub-menu__title) {
    height: 44px;
    line-height: 44px;
    margin-bottom: 4px;
    border-radius: $radius-md;
    color: rgba(255, 255, 255, 0.7);
    transition: all $transition-base $ease-in-out;

    &:hover {
      background-color: rgba(255, 255, 255, 0.08);
      color: #fff;
    }

    .el-icon {
      font-size: 18px;
    }
  }

  :deep(.el-menu-item.is-active) {
    background: linear-gradient(90deg, $primary-color 0%, $primary-dark 100%);
    color: #fff;
    box-shadow: 0 4px 12px rgba($primary-color, 0.4);
  }

  :deep(.el-sub-menu.is-active > .el-sub-menu__title) {
    color: #fff;
  }

  :deep(.el-sub-menu .el-menu) {
    background: transparent;
    
    .el-menu-item {
      padding-left: 52px !important;
      height: 40px;
      line-height: 40px;
    }
  }

  /* 折叠态：图标居中、加大间距、弱化阴影 */
  &.el-menu--collapse {
    padding: 12px 10px;

    :deep(.el-menu-item),
    :deep(.el-sub-menu__title) {
      margin-bottom: 8px;
      height: 40px;
      line-height: 40px;
      padding-left: 0 !important;
      padding-right: 0 !important;
      display: flex;
      justify-content: center;
      align-items: center;
    }

    :deep(.el-menu-item .el-icon),
    :deep(.el-sub-menu__title .el-icon) {
      margin-right: 0 !important;
    }

    :deep(.el-sub-menu__title .el-sub-menu__icon-arrow) {
      display: none;
    }

    :deep(.el-menu-item.is-active),
    :deep(.el-sub-menu.is-active > .el-sub-menu__title) {
      box-shadow: 0 2px 8px rgba($primary-color, 0.35);
    }

    :deep(.el-sub-menu .el-menu-item) {
      margin-bottom: 4px;
    }
  }
}

.aside-footer {
  padding: 12px;
  border-top: 1px solid rgba(255, 255, 255, 0.08);
  flex-shrink: 0;
  
  &.collapsed {
    padding: 12px 0;
    display: flex;
    justify-content: center;
  }
}

.collapse-btn {
  width: 100%;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  border-radius: $radius-md;
  color: rgba(255, 255, 255, 0.6);
  transition: all $transition-base $ease-in-out;

  &:hover {
    background-color: rgba(255, 255, 255, 0.08);
    color: #fff;
  }

  .collapsed & {
    width: 36px;
  }
}

// ==================== 主容器 ====================

.main-container {
  flex: 1;
  overflow: hidden;
  background-color: $bg-page;
}

// ==================== 头部 ====================

.layout-header {
  height: 56px;
  background-color: #fff;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  z-index: 10;
}

.header-left {
  display: flex;
  align-items: center;
  
  :deep(.el-breadcrumb) {
    font-size: $font-size-sm;
    
    .el-breadcrumb__inner {
      color: $text-secondary;
      
      &.is-link:hover {
        color: $primary-color;
      }
    }
    
    .el-breadcrumb__item:last-child .el-breadcrumb__inner {
      color: $text-primary;
      font-weight: $font-weight-medium;
    }
  }
}

.header-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.header-action {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: $radius-md;
  cursor: pointer;
  color: $text-secondary;
  transition: all $transition-base $ease-in-out;

  &:hover {
    background-color: $fill-light;
    color: $primary-color;
    transform: scale(1.05);
  }
  
  &:active {
    transform: scale(0.95);
  }
  
  &.is-spinning .el-icon {
    animation: spinAnimation 1s linear infinite;
  }
}

@keyframes spinAnimation {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.user-dropdown {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 6px 12px;
  border-radius: $radius-lg;
  cursor: pointer;
  transition: all $transition-base $ease-in-out;

  &:hover {
    background-color: $fill-light;
  }

  .user-avatar {
    background: linear-gradient(135deg, $primary-color 0%, $primary-dark 100%);
    color: #fff;
    font-weight: $font-weight-semibold;
    font-size: 14px;
  }

  .user-info {
    display: flex;
    flex-direction: column;
    line-height: 1.3;

    .user-name {
      font-size: $font-size-sm;
      font-weight: $font-weight-medium;
      color: $text-primary;
    }

    .user-role {
      font-size: $font-size-xs;
      color: $text-secondary;
    }
  }

  .dropdown-arrow {
    color: $text-secondary;
    font-size: 12px;
    transition: transform $transition-base;
  }

  &:hover .dropdown-arrow {
    transform: rotate(180deg);
  }
}

// ==================== 内容区 ====================

.layout-main {
  padding: 0;
  overflow-y: scroll; // 始终显示滚动条，防止页面跳动
  overflow-x: hidden;
}

// ==================== 动画 ====================

.slide-fade-enter-active {
  transition: all $transition-slow $ease-out;
}

.slide-fade-leave-active {
  transition: all $transition-base $ease-in;
}

.slide-fade-enter-from {
  transform: translateY(10px);
  opacity: 0;
}

.slide-fade-leave-to {
  transform: translateY(-10px);
  opacity: 0;
}

// ==================== 响应式 ====================

@media screen and (max-width: $breakpoint-md) {
  .user-info {
    display: none !important;
  }
  
  .user-dropdown {
    padding: 6px;
  }
}
</style>
