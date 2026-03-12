<template>
  <div class="page-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div>
        <h2 class="page-title">
          <el-icon><CircleCheck /></el-icon>
          卡密核销
        </h2>
        <p class="page-subtitle">支持单张核销和批量Excel导入核销</p>
      </div>
    </div>

    <el-row :gutter="20">
      <!-- 核销表单 -->
      <el-col :xs="24" :lg="12">
        <transition name="slide-fade" appear>
          <div class="card verify-card hover-lift">
            <!-- 顶部状态图标 -->
            <div class="verify-status">
              <div
                class="status-icon"
                :class="{
                  'is-ready': canSubmit || batchFile,
                  'is-loading': loading,
                }"
              >
                <svg
                  v-if="(canSubmit || batchFile) && !loading"
                  class="status-checkmark"
                  viewBox="0 0 52 52"
                >
                  <circle
                    class="status-circle"
                    cx="26"
                    cy="26"
                    r="24"
                    fill="none"
                  />
                  <path class="status-check" fill="none" d="M14 27l7 7 16-16" />
                </svg>
                <div v-else class="status-default">
                  <el-icon><Postcard /></el-icon>
                </div>
              </div>
              <h3 class="verify-title">卡密核销</h3>
              <p class="verify-subtitle">选择核销方式完成操作</p>
            </div>

            <!-- 模式切换标签 -->
            <el-tabs v-model="activeTab" class="verify-tabs">
              <el-tab-pane label="单张核销" name="single">
                <el-form
                  ref="formRef"
                  :model="form"
                  :rules="rules"
                  label-position="top"
                  class="verify-form"
                  @keyup.enter="handleVerify"
                >
                  <el-form-item label="卡号" prop="cardNumber">
                    <el-input
                      v-model="form.cardNumber"
                      placeholder="请输入9位卡号"
                      size="large"
                      maxlength="9"
                      @focus="inputFocus = 'cardNumber'"
                      @blur="inputFocus = ''"
                      :class="{ 'input-focused': inputFocus === 'cardNumber' }"
                    >
                      <template #prefix>
                        <el-icon><Postcard /></el-icon>
                      </template>
                      <template #suffix>
                        <span class="input-counter">
                          {{ form.cardNumber.length }}/9
                        </span>
                      </template>
                    </el-input>
                  </el-form-item>

                  <el-form-item label="密码" prop="cardPassword">
                    <el-input
                      v-model="form.cardPassword"
                      placeholder="请输入6位密码"
                      size="large"
                      maxlength="6"
                      @input="handlePasswordInput"
                      @focus="inputFocus = 'cardPassword'"
                      @blur="inputFocus = ''"
                      :class="{
                        'input-focused': inputFocus === 'cardPassword',
                      }"
                    >
                      <template #prefix>
                        <el-icon><Lock /></el-icon>
                      </template>
                      <template #suffix>
                        <span class="input-counter">
                          {{ form.cardPassword.length }}/6
                        </span>
                      </template>
                    </el-input>
                  </el-form-item>

                  <el-form-item>
                    <el-button
                      type="primary"
                      size="large"
                      :loading="loading"
                      :disabled="!canSubmit"
                      class="verify-btn ripple-btn"
                      @click="handleVerify"
                    >
                      <template v-if="!loading">
                        <el-icon class="btn-icon"><CircleCheck /></el-icon>
                        确认核销
                      </template>
                      <template v-else>
                        <span class="loading-spinner"></span>
                        核销中
                      </template>
                    </el-button>
                  </el-form-item>
                </el-form>
              </el-tab-pane>

              <el-tab-pane label="批量核销" name="batch">
                <div class="batch-upload-section">
                  <el-upload
                    ref="uploadRef"
                    :auto-upload="false"
                    :show-file-list="true"
                    :on-change="handleFileChange"
                    :on-remove="handleFileRemove"
                    accept=".xlsx,.xls"
                    :limit="1"
                    class="batch-upload"
                  >
                    <el-button type="primary" size="large">
                      <el-icon><Upload /></el-icon>
                      选择Excel文件
                    </el-button>
                    <template #tip>
                      <div class="el-upload__tip">
                        支持 .xlsx, .xls 格式，第一列为卡号，第二列为密码
                      </div>
                    </template>
                  </el-upload>

                  <div class="file-info" v-if="batchFile">
                    <el-icon><Document /></el-icon>
                    <span>{{ batchFile.name }}</span>
                  </div>

                  <el-button
                    type="success"
                    size="large"
                    :loading="batchLoading"
                    :disabled="!batchFile"
                    class="batch-verify-btn ripple-btn"
                    @click="handleBatchVerify"
                  >
                    <template v-if="!batchLoading">
                      <el-icon class="btn-icon"><CircleCheck /></el-icon>
                      开始批量核销
                    </template>
                    <template v-else>
                      <span class="loading-spinner"></span>
                      批量核销中
                    </template>
                  </el-button>

                  <div class="batch-tips">
                    <el-alert
                      title="Excel格式说明"
                      type="info"
                      :closable="false"
                      show-icon
                    >
                      <template #default>
                        <p>• 第一列：卡号（9位数字）</p>
                        <p>• 第二列：密码（6位字母数字）</p>
                        <p>• 首行为表头："卡号"、"密码"</p>
                      </template>
                    </el-alert>
                  </div>
                </div>
              </el-tab-pane>
            </el-tabs>
          </div>
        </transition>
      </el-col>

      <!-- 结果区域 -->
      <el-col :xs="24" :lg="12">
        <transition name="bounce" mode="out-in">
          <!-- 单张核销成功 -->
          <div
            v-if="result.visible && result.success && activeTab === 'single'"
            class="card result-card success"
            key="success"
          >
            <div class="success-animation">
              <svg class="checkmark" viewBox="0 0 52 52">
                <circle
                  class="checkmark-circle"
                  cx="26"
                  cy="26"
                  r="24"
                  fill="none"
                />
                <path
                  class="checkmark-check"
                  fill="none"
                  d="M14 27l7 7 16-16"
                />
              </svg>
            </div>
            <h3 class="result-title">核销成功！</h3>
            <p class="result-desc">
              卡号 <strong>{{ form.cardNumber }}</strong> 已成功核销
            </p>
            <div class="result-time">
              <el-icon><Clock /></el-icon>
              {{ new Date().toLocaleString() }}
            </div>
            <el-button
              type="primary"
              size="large"
              @click="resetForm"
              class="continue-btn"
            >
              <el-icon><RefreshRight /></el-icon>
              继续核销
            </el-button>
          </div>

          <!-- 单张核销失败 -->
          <div
            v-else-if="
              result.visible && !result.success && activeTab === 'single'
            "
            class="card result-card error"
            key="error"
          >
            <div class="error-animation">
              <div class="error-circle">
                <div class="background"></div>
                <div class="error-x">
                  <span class="x-line"></span>
                  <span class="x-line"></span>
                </div>
              </div>
            </div>
            <h3 class="result-title">核销失败</h3>
            <p class="result-desc">{{ result.message }}</p>
            <el-button
              type="primary"
              size="large"
              @click="result.visible = false"
              class="retry-btn"
            >
              <el-icon><RefreshRight /></el-icon>
              重新输入
            </el-button>
          </div>

          <!-- 批量核销结果 -->
          <div
            v-if="batchResult.visible"
            class="card batch-result-card"
            key="batch-result"
          >
            <div class="batch-result-header">
              <div
                class="batch-icon"
                :class="{
                  success: batchResult.failCount === 0,
                  warning: batchResult.failCount > 0,
                }"
              >
                <el-icon
                  ><CircleCheckFilled
                    v-if="batchResult.failCount === 0" /><WarningFilled v-else
                /></el-icon>
              </div>
              <h3 class="batch-title">批量核销完成</h3>
            </div>

            <div class="batch-stats">
              <div class="stat-item">
                <span class="stat-value">{{ batchResult.totalCount }}</span>
                <span class="stat-label">总条数</span>
              </div>
              <div class="stat-item success">
                <span class="stat-value">{{ batchResult.successCount }}</span>
                <span class="stat-label">成功</span>
              </div>
              <div class="stat-item error" v-if="batchResult.failCount > 0">
                <span class="stat-value">{{ batchResult.failCount }}</span>
                <span class="stat-label">失败</span>
              </div>
            </div>

            <!-- 失败详情列表 -->
            <div
              v-if="
                batchResult.failDetails && batchResult.failDetails.length > 0
              "
              class="fail-details"
            >
              <h4>失败详情</h4>
              <el-table :data="batchResult.failDetails" size="small" stripe>
                <el-table-column prop="rowNum" label="行号" width="80" />
                <el-table-column prop="cardNumber" label="卡号" width="120" />
                <el-table-column prop="reason" label="失败原因" />
              </el-table>
            </div>

            <el-button
              type="primary"
              size="large"
              @click="resetBatchForm"
              class="continue-btn"
            >
              <el-icon><RefreshRight /></el-icon>
              继续批量核销
            </el-button>
          </div>

          <!-- 说明 -->
          <div v-else class="card tips-card" key="tips">
            <div class="tips-header">
              <div class="tips-icon">
                <el-icon><InfoFilled /></el-icon>
              </div>
              <div class="tips-header-text">
                <h3>核销说明</h3>
                <p>了解核销流程和注意事项</p>
              </div>
            </div>
            <div class="tips-content">
              <div
                v-for="(tip, index) in tips"
                :key="index"
                class="tip-item"
                :style="{ animationDelay: `${index * 0.1}s` }"
              >
                <div class="tip-icon" :class="tip.type">
                  <el-icon><component :is="tip.icon" /></el-icon>
                </div>
                <div class="tip-text">
                  <h4>{{ tip.title }}</h4>
                  <p>{{ tip.desc }}</p>
                </div>
              </div>
            </div>
          </div>
        </transition>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from "vue";
import { verifyCard, batchVerifyCard } from "@/api/verify";
import toast from "@/utils/toast";
import {
  CircleCheck,
  Postcard,
  Lock,
  Clock,
  RefreshRight,
  InfoFilled,
  EditPen,
  Warning,
  Document,
  Upload,
  CircleCheckFilled,
  WarningFilled,
} from "@element-plus/icons-vue";

const formRef = ref(null);
const uploadRef = ref(null);
const loading = ref(false);
const batchLoading = ref(false);
const inputFocus = ref("");
const activeTab = ref("single");

const batchFile = ref(null);

const batchResult = reactive({
  visible: false,
  totalCount: 0,
  successCount: 0,
  failCount: 0,
  failDetails: [],
});

const form = reactive({
  cardNumber: "",
  cardPassword: "",
});

const rules = {
  cardNumber: [
    { required: true, message: "请输入卡号", trigger: "blur" },
    { pattern: /^\d{9}$/, message: "卡号必须为9位数字", trigger: "blur" },
  ],
  cardPassword: [
    { required: true, message: "请输入密码", trigger: "blur" },
    {
      pattern: /^[A-Z0-9]{6}$/,
      message: "密码必须为6位字母数字",
      trigger: "blur",
    },
  ],
};

const result = reactive({
  visible: false,
  success: false,
  message: "",
});

// 是否可以提交
const canSubmit = computed(() => {
  return form.cardNumber.length === 9 && form.cardPassword.length === 6;
});

// 提示数据
const tips = [
  {
    icon: "EditPen",
    type: "primary",
    title: "验证信息",
    desc: "输入正确的卡号和密码即可完成核销",
  },
  {
    icon: "CircleCheck",
    type: "success",
    title: "状态要求",
    desc: '只有"未使用"状态的卡密才能核销',
  },
  {
    icon: "Warning",
    type: "warning",
    title: "不可逆操作",
    desc: '核销成功后状态变更为"已核销"，无法撤回',
  },
  {
    icon: "Document",
    type: "info",
    title: "记录追溯",
    desc: "核销记录将保存操作员信息和核销时间",
  },
];

// 自动转大写
const handlePasswordInput = (value) => {
  form.cardPassword = value.toUpperCase();
};

const handleVerify = async () => {
  try {
    await formRef.value.validate();
    loading.value = true;

    await verifyCard(form);

    result.visible = true;
    result.success = true;
    result.message = "";

    // 振动反馈
    if (navigator.vibrate) {
      navigator.vibrate([50, 30, 50]);
    }

    toast.notifySuccess("核销成功", `卡号 ${form.cardNumber} 已成功核销`);
  } catch (error) {
    result.visible = true;
    result.success = false;
    result.message = error.message || "核销失败，请检查卡号和密码";
    // 错误已在请求拦截器中显示，这里不再重复提示
  } finally {
    loading.value = false;
  }
};

const resetForm = () => {
  result.visible = false;
  form.cardNumber = "";
  form.cardPassword = "";
  formRef.value?.resetFields();
};

// 批量核销相关方法
const handleFileChange = (file, fileList) => {
  if (fileList.length > 0) {
    batchFile.value = file.raw;
  } else {
    batchFile.value = null;
  }
  // 隐藏之前的结果
  batchResult.visible = false;
};

const handleFileRemove = () => {
  batchFile.value = null;
  batchResult.visible = false;
};

const handleBatchVerify = async () => {
  if (!batchFile.value) {
    toast.notifyError("请先选择Excel文件");
    return;
  }

  try {
    batchLoading.value = true;
    const response = await batchVerifyCard(batchFile.value);

    // 更新结果
    batchResult.visible = true;
    batchResult.totalCount = response.data.totalCount;
    batchResult.successCount = response.data.successCount;
    batchResult.failCount = response.data.failCount;
    batchResult.failDetails = response.data.failDetails || [];

    // 显示成功提示
    if (batchResult.failCount === 0) {
      toast.notifySuccess(
        "批量核销成功",
        `成功核销 ${batchResult.successCount} 张卡密`,
      );
    } else {
      toast.notifyWarning(
        "批量核销完成",
        `成功 ${batchResult.successCount} 张，失败 ${batchResult.failCount} 张`,
      );
    }

    // 振动反馈
    if (navigator.vibrate) {
      navigator.vibrate([50, 30, 50]);
    }
  } catch (error) {
    toast.notifyError(
      "批量核销失败",
      error.message || "请检查文件格式是否正确",
    );
  } finally {
    batchLoading.value = false;
  }
};

const resetBatchForm = () => {
  batchResult.visible = false;
  batchFile.value = null;
  uploadRef.value?.clearFiles();
};
</script>

<style lang="scss" scoped>
@import "@/assets/styles/variables.scss";

.page-subtitle {
  margin-top: $spacing-xs;
  font-size: $font-size-sm;
  color: $text-secondary;
}

// ==================== 核销卡片 ====================

.verify-card {
  text-align: center;
  padding: $spacing-2xl;
  height: 100%;
  min-height: 400px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

// 标签页样式
.verify-tabs {
  margin-top: $spacing-lg;

  :deep(.el-tabs__nav-wrap) {
    margin-bottom: $spacing-xl;
  }

  :deep(.el-tabs__item) {
    font-size: $font-size-md;
    font-weight: $font-weight-semibold;
  }
}

// 批量上传区域样式
.batch-upload-section {
  text-align: center;
  padding: 0 $spacing-xl;

  .batch-upload {
    margin-bottom: $spacing-lg;
  }

  .file-info {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: $spacing-xs;
    margin-bottom: $spacing-lg;
    color: $text-secondary;
    font-size: $font-size-sm;

    .el-icon {
      color: $primary-color;
    }
  }

  .batch-verify-btn {
    width: 100%;
    height: 48px;
    font-size: $font-size-md;
    font-weight: $font-weight-semibold;
    border-radius: $radius-lg;
    background: linear-gradient(
      135deg,
      $success-color 0%,
      darken($success-color, 10%) 100%
    );
    border: none;
    transition: all 0.3s $ease-smooth;
    margin-bottom: $spacing-lg;

    .btn-icon {
      margin-right: 8px;
      transition: transform 0.3s $ease-spring;
    }

    &:not(:disabled):hover {
      box-shadow: 0 8px 25px rgba($success-color, 0.4);

      .btn-icon {
        transform: scale(1.1);
      }
    }

    &:disabled {
      background: $fill-light;
      color: $text-disabled;
      box-shadow: none;
    }

    .loading-spinner {
      width: 18px;
      height: 18px;
      border: 2px solid rgba(255, 255, 255, 0.3);
      border-top-color: #fff;
      border-radius: 50%;
      animation: spin 0.8s linear infinite;
      margin-right: 8px;
    }
  }

  .batch-tips {
    text-align: left;
  }
}

// 批量结果卡片样式
.batch-result-card {
  text-align: center;
  padding: $spacing-2xl;
  height: 100%;
  min-height: 400px;
  display: flex;
  flex-direction: column;
  align-items: center;

  .batch-result-header {
    margin-bottom: $spacing-xl;

    .batch-icon {
      width: 64px;
      height: 64px;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      margin: 0 auto $spacing-lg;

      &.success {
        background: rgba($success-color, 0.1);
        color: $success-color;
      }

      &.warning {
        background: rgba($warning-color, 0.1);
        color: $warning-color;
      }

      .el-icon {
        font-size: 32px;
      }
    }

    .batch-title {
      font-size: $font-size-xl;
      font-weight: $font-weight-bold;
      color: $text-primary;
    }
  }

  .batch-stats {
    display: flex;
    justify-content: center;
    gap: $spacing-2xl;
    margin-bottom: $spacing-xl;

    .stat-item {
      text-align: center;

      .stat-value {
        display: block;
        font-size: $font-size-2xl;
        font-weight: $font-weight-bold;
        color: $text-primary;
      }

      .stat-label {
        font-size: $font-size-sm;
        color: $text-secondary;
      }

      &.success .stat-value {
        color: $success-color;
      }

      &.error .stat-value {
        color: $danger-color;
      }
    }
  }

  .fail-details {
    width: 100%;
    text-align: left;
    margin-bottom: $spacing-xl;

    h4 {
      font-size: $font-size-base;
      font-weight: $font-weight-semibold;
      color: $text-primary;
      margin-bottom: $spacing-sm;
    }

    :deep(.el-table) {
      font-size: $font-size-sm;
    }
  }
}

// 顶部状态图标
.verify-status {
  margin-bottom: $spacing-xl;
}

.status-icon {
  width: 72px;
  height: 72px;
  margin: 0 auto $spacing-base;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: linear-gradient(
    135deg,
    $primary-lightest 0%,
    rgba($primary-color, 0.1) 100%
  );
  transition: all 0.4s $ease-spring;

  .status-default {
    width: 100%;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;

    .el-icon {
      font-size: 32px;
      color: $primary-color;
      transition: all 0.3s ease;
    }
  }

  &.is-ready {
    background: linear-gradient(
      135deg,
      rgba($success-color, 0.15) 0%,
      rgba($success-color, 0.08) 100%
    );

    .status-checkmark {
      animation: statusPop 0.4s $ease-spring;
    }
  }

  &.is-loading {
    background: linear-gradient(
      135deg,
      rgba($primary-color, 0.15) 0%,
      rgba($primary-color, 0.08) 100%
    );

    .status-default .el-icon {
      animation: iconSpin 1s linear infinite;
    }
  }
}

.verify-title {
  font-size: $font-size-lg;
  font-weight: $font-weight-bold;
  color: $text-primary;
  margin: 0 0 $spacing-xs;
}

.verify-subtitle {
  font-size: $font-size-sm;
  color: $text-secondary;
  margin: 0;
}

.status-checkmark {
  width: 40px;
  height: 40px;
  stroke-width: 3;
  stroke: $success-color;
  stroke-miterlimit: 10;
}

.status-circle {
  stroke-dasharray: 166;
  stroke-dashoffset: 0;
  stroke-width: 3;
  stroke: $success-color;
  fill: transparent;
}

.status-check {
  stroke-dasharray: 48;
  stroke-dashoffset: 0;
  stroke-width: 3;
  stroke-linecap: round;
  stroke-linejoin: round;
}

@keyframes statusPop {
  0% {
    transform: scale(0.5);
    opacity: 0;
  }
  50% {
    transform: scale(1.1);
  }
  100% {
    transform: scale(1);
    opacity: 1;
  }
}

@keyframes iconSpin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

.verify-form {
  max-width: 360px;
  margin: 0 auto;
  text-align: left;

  :deep(.el-form-item__label) {
    font-weight: $font-weight-semibold;
    color: $text-primary;
    padding-bottom: 8px;
  }

  :deep(.el-input__wrapper) {
    padding: 8px 16px;
    border-radius: $radius-lg;
    box-shadow: 0 0 0 1px $border-light inset;
    transition: all 0.3s $ease-smooth;

    &:hover {
      box-shadow: 0 0 0 1px $border-color inset;
    }
  }

  :deep(.el-input.is-focus .el-input__wrapper) {
    box-shadow: 0 0 0 2px rgba($primary-color, 0.2) inset;
  }

  :deep(.el-input__prefix .el-icon) {
    font-size: 18px;
    color: $text-secondary;
  }

  :deep(.el-input__inner) {
    font-size: $font-size-md;
    font-family: $font-family-mono;
    letter-spacing: 2px;

    &::placeholder {
      letter-spacing: 0;
      font-family: $font-family;
    }
  }

  .input-counter {
    font-size: $font-size-xs;
    color: $text-disabled;
    font-family: $font-family-mono;
  }
}

.verify-btn {
  width: 100%;
  height: 48px;
  font-size: $font-size-md;
  font-weight: $font-weight-semibold;
  border-radius: $radius-lg;
  background: linear-gradient(135deg, $primary-color 0%, $primary-dark 100%);
  border: none;
  transition: all 0.3s $ease-smooth;
  margin-top: $spacing-sm;

  .btn-icon {
    margin-right: 8px;
    transition: transform 0.3s $ease-spring;
  }

  &:not(:disabled):hover {
    box-shadow: 0 8px 25px rgba($primary-color, 0.4);

    .btn-icon {
      transform: scale(1.1);
    }
  }

  &:disabled {
    background: $fill-light;
    color: $text-disabled;
    box-shadow: none;
  }

  .loading-spinner {
    width: 18px;
    height: 18px;
    border: 2px solid rgba(255, 255, 255, 0.3);
    border-top-color: #fff;
    border-radius: 50%;
    animation: spin 0.8s linear infinite;
    margin-right: 8px;
  }
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

// ==================== 结果卡片 ====================

.result-card {
  text-align: center;
  padding: $spacing-3xl;
  height: 100%;
  min-height: 400px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;

  &.success {
    background: linear-gradient(135deg, #f0fff4 0%, #ffffff 100%);
    border: 1px solid rgba($success-color, 0.2);

    .result-title {
      color: $success-color;
    }
  }

  &.error {
    background: linear-gradient(135deg, #fff5f5 0%, #ffffff 100%);
    border: 1px solid rgba($danger-color, 0.2);

    .result-title {
      color: $danger-color;
    }
  }
}

// 成功动画 - SVG 样式
.success-animation {
  margin-bottom: $spacing-xl;
}

.checkmark {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  display: block;
  stroke-width: 3;
  stroke: #fff;
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
  fill: $success-color;
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
  0%,
  100% {
    transform: none;
  }
  50% {
    transform: scale3d(1.1, 1.1, 1);
  }
}

// 失败动画
.error-animation {
  margin-bottom: $spacing-lg;
}

.error-circle {
  width: 80px;
  height: 80px;
  position: relative;
  margin: 0 auto;

  .background {
    width: 80px;
    height: 80px;
    border-radius: 50%;
    background: $danger-color;
    position: absolute;
    animation: circleIn 0.3s ease-out;
  }

  .error-x {
    position: absolute;
    inset: 0;
    display: flex;
    align-items: center;
    justify-content: center;

    .x-line {
      position: absolute;
      width: 4px;
      height: 36px;
      background: #fff;
      border-radius: 2px;

      &:first-child {
        transform: rotate(45deg);
        animation: xLineIn 0.2s ease-out 0.2s forwards;
        transform-origin: center;
        opacity: 0;
      }

      &:last-child {
        transform: rotate(-45deg);
        animation: xLineIn 0.2s ease-out 0.3s forwards;
        transform-origin: center;
        opacity: 0;
      }
    }
  }
}

@keyframes xLineIn {
  from {
    opacity: 0;
    height: 0;
  }
  to {
    opacity: 1;
    height: 36px;
  }
}

.result-title {
  font-size: $font-size-xl;
  font-weight: $font-weight-bold;
  margin: 0 0 $spacing-sm;
  animation: fadeInUp 0.4s ease-out 0.4s both;
}

.result-desc {
  font-size: $font-size-base;
  color: $text-secondary;
  margin: 0 0 $spacing-base;
  animation: fadeInUp 0.4s ease-out 0.5s both;

  strong {
    color: $primary-color;
    font-family: $font-family-mono;
    font-size: $font-size-lg;
    margin: 0 4px;
  }
}

.result-time {
  font-size: $font-size-sm;
  color: $text-secondary;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: $spacing-xs;
  margin-bottom: $spacing-xl;
  animation: fadeInUp 0.4s ease-out 0.6s both;

  .el-icon {
    font-size: 14px;
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

.continue-btn,
.retry-btn {
  min-width: 160px;
  animation: fadeInUp 0.4s ease-out 0.7s both;
  transition: all 0.3s $ease-smooth;

  &:hover {
    box-shadow: 0 6px 16px rgba(0, 0, 0, 0.15);
  }
}

.continue-btn {
  background: linear-gradient(
    135deg,
    $success-color 0%,
    darken($success-color, 10%) 100%
  );
  border: none;

  &:hover {
    box-shadow: 0 6px 16px rgba($success-color, 0.35);
  }
}

// ==================== 说明卡片 ====================

.tips-card {
  height: 100%;
  min-height: 400px;
  display: flex;
  flex-direction: column;
}

.tips-header {
  display: flex;
  align-items: center;
  gap: $spacing-base;
  padding-bottom: $spacing-lg;
  margin-bottom: $spacing-lg;
  border-bottom: 1px solid $border-light;

  .tips-icon {
    width: 48px;
    height: 48px;
    border-radius: $radius-lg;
    background: linear-gradient(135deg, $primary-color 0%, $primary-dark 100%);
    display: flex;
    align-items: center;
    justify-content: center;

    .el-icon {
      font-size: 24px;
      color: #fff;
    }
  }

  .tips-header-text {
    h3 {
      font-size: $font-size-lg;
      font-weight: $font-weight-bold;
      color: $text-primary;
      margin: 0 0 4px;
    }

    p {
      font-size: $font-size-sm;
      color: $text-secondary;
      margin: 0;
    }
  }
}

.tips-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: $spacing-sm;
}

.tip-item {
  display: flex;
  align-items: flex-start;
  gap: $spacing-md;
  padding: $spacing-base;
  border-radius: $radius-lg;
  background: $fill-lighter;
  transition: all 0.3s $ease-smooth;
  animation: tipSlideIn 0.4s $ease-out both;

  &:hover {
    background: $fill-light;
    transform: translateX(4px);

    .tip-icon {
      transform: scale(1.1);
    }
  }
}

.tip-icon {
  width: 36px;
  height: 36px;
  border-radius: $radius-md;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  transition: transform 0.3s $ease-spring;

  .el-icon {
    font-size: 18px;
  }

  &.primary {
    background: rgba($primary-color, 0.1);
    color: $primary-color;
  }

  &.success {
    background: rgba($success-color, 0.1);
    color: $success-color;
  }

  &.warning {
    background: rgba($warning-color, 0.1);
    color: $warning-color;
  }

  &.info {
    background: rgba($info-color, 0.1);
    color: $info-color;
  }
}

@keyframes tipSlideIn {
  from {
    opacity: 0;
    transform: translateX(-15px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

.tip-text {
  flex: 1;

  h4 {
    font-size: $font-size-base;
    font-weight: $font-weight-semibold;
    color: $text-primary;
    margin: 0 0 4px;
  }

  p {
    font-size: $font-size-sm;
    color: $text-secondary;
    margin: 0;
    line-height: 1.5;
  }
}

// ==================== 动画 ====================

.slide-fade-enter-active {
  transition: all 0.4s $ease-smooth;
}

.slide-fade-leave-active {
  transition: all 0.2s $ease-in;
}

.slide-fade-enter-from {
  transform: translateX(20px);
  opacity: 0;
}

.slide-fade-leave-to {
  transform: translateX(-20px);
  opacity: 0;
}

.bounce-enter-active {
  animation: bounceIn 0.5s $ease-spring;
}

.bounce-leave-active {
  animation: bounceOut 0.3s $ease-in;
}

@keyframes bounceIn {
  0% {
    transform: scale(0.8);
    opacity: 0;
  }
  50% {
    transform: scale(1.02);
  }
  100% {
    transform: scale(1);
    opacity: 1;
  }
}

@keyframes bounceOut {
  0% {
    transform: scale(1);
    opacity: 1;
  }
  100% {
    transform: scale(0.9);
    opacity: 0;
  }
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s $ease-smooth;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
