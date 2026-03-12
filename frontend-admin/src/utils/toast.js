/**
 * 高级 Toast 提示工具
 * 提供更丰富的交互反馈
 */
import { ElMessage, ElNotification, ElMessageBox } from 'element-plus'
import { h } from 'vue'

// 默认配置
const defaultDuration = 3000
const defaultOffset = 60

/**
 * 成功提示
 */
export function showSuccess(message, options = {}) {
  return ElMessage({
    message,
    type: 'success',
    duration: options.duration ?? defaultDuration,
    offset: options.offset ?? defaultOffset,
    showClose: options.showClose ?? true,
    ...options,
  })
}

/**
 * 错误提示
 */
export function showError(message, options = {}) {
  return ElMessage({
    message,
    type: 'error',
    duration: options.duration ?? 4000,
    offset: options.offset ?? defaultOffset,
    showClose: true,
    ...options,
  })
}

/**
 * 警告提示
 */
export function showWarning(message, options = {}) {
  return ElMessage({
    message,
    type: 'warning',
    duration: options.duration ?? defaultDuration,
    offset: options.offset ?? defaultOffset,
    showClose: true,
    ...options,
  })
}

/**
 * 信息提示
 */
export function showInfo(message, options = {}) {
  return ElMessage({
    message,
    type: 'info',
    duration: options.duration ?? defaultDuration,
    offset: options.offset ?? defaultOffset,
    showClose: options.showClose ?? false,
    ...options,
  })
}

/**
 * 加载提示（带 loading 图标）
 */
export function showLoading(message = '加载中...') {
  return ElMessage({
    message: h('span', { style: 'display: flex; align-items: center; gap: 8px;' }, [
      h('i', { class: 'el-icon is-loading', style: 'font-size: 16px;' }, [
        h('svg', {
          viewBox: '0 0 1024 1024',
          style: 'width: 1em; height: 1em; animation: rotate 1s linear infinite;',
        }, [
          h('path', {
            fill: 'currentColor',
            d: 'M512 64a32 32 0 0 1 32 32v192a32 32 0 0 1-64 0V96a32 32 0 0 1 32-32zm0 640a32 32 0 0 1 32 32v192a32 32 0 1 1-64 0V736a32 32 0 0 1 32-32zm448-192a32 32 0 0 1-32 32H736a32 32 0 1 1 0-64h192a32 32 0 0 1 32 32zm-640 0a32 32 0 0 1-32 32H96a32 32 0 0 1 0-64h192a32 32 0 0 1 32 32zM195.2 195.2a32 32 0 0 1 45.248 0L376.32 331.008a32 32 0 0 1-45.248 45.248L195.2 240.448a32 32 0 0 1 0-45.248zm452.544 452.544a32 32 0 0 1 45.248 0L828.8 783.552a32 32 0 0 1-45.248 45.248L647.744 692.992a32 32 0 0 1 0-45.248zM828.8 195.264a32 32 0 0 1 0 45.184L692.992 376.32a32 32 0 0 1-45.248-45.248l135.808-135.808a32 32 0 0 1 45.248 0zm-452.544 452.48a32 32 0 0 1 0 45.248L240.448 828.8a32 32 0 0 1-45.248-45.248l135.808-135.808a32 32 0 0 1 45.248 0z',
          }),
        ]),
      ]),
      message,
    ]),
    type: 'info',
    duration: 0,
    showClose: false,
    offset: defaultOffset,
  })
}

/**
 * 通知提示（右上角）
 */
export function notify(options) {
  const { type = 'info', title, message, duration = 4500, ...rest } = options
  
  return ElNotification({
    title,
    message,
    type,
    duration,
    position: 'top-right',
    ...rest,
  })
}

/**
 * 成功通知
 */
export function notifySuccess(title, message, options = {}) {
  return notify({
    type: 'success',
    title,
    message,
    ...options,
  })
}

/**
 * 错误通知
 */
export function notifyError(title, message, options = {}) {
  return notify({
    type: 'error',
    title,
    message,
    duration: 0, // 错误通知不自动关闭
    ...options,
  })
}

/**
 * 确认对话框
 */
export function confirm(message, title = '提示', options = {}) {
  return ElMessageBox.confirm(message, title, {
    confirmButtonText: options.confirmText ?? '确定',
    cancelButtonText: options.cancelText ?? '取消',
    type: options.type ?? 'warning',
    draggable: true,
    ...options,
  })
}

/**
 * 危险操作确认
 */
export function confirmDanger(message, title = '警告', options = {}) {
  return ElMessageBox.confirm(message, title, {
    confirmButtonText: options.confirmText ?? '确定删除',
    cancelButtonText: options.cancelText ?? '取消',
    type: 'error',
    confirmButtonClass: 'el-button--danger',
    draggable: true,
    ...options,
  })
}

/**
 * 复制成功提示
 */
export function copySuccess(text = '已复制到剪贴板') {
  showSuccess(text, { duration: 2000 })
}

/**
 * 操作成功反馈（带震动）
 */
export function actionSuccess(message = '操作成功') {
  showSuccess(message)
  // 添加触觉反馈（如果浏览器支持）
  if (navigator.vibrate) {
    navigator.vibrate(50)
  }
}

/**
 * 保存成功提示
 */
export function saveSuccess() {
  showSuccess('保存成功', { duration: 2000 })
}

/**
 * 删除成功提示
 */
export function deleteSuccess() {
  showSuccess('删除成功', { duration: 2000 })
}

// 导出默认对象
export default {
  success: showSuccess,
  error: showError,
  warning: showWarning,
  info: showInfo,
  loading: showLoading,
  notify,
  notifySuccess,
  notifyError,
  confirm,
  confirmDanger,
  copySuccess,
  actionSuccess,
  saveSuccess,
  deleteSuccess,
}
