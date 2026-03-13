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

    <el-tabs v-model="activeTab" class="verify-tabs">
      <!-- 单张核销 -->
      <el-tab-pane label="单张核销" name="single">
        <el-row :gutter="20">
          <!-- 核销表单 -->
          <el-col :xs="24" :lg="12">
            <transition name="slide-fade" appear>
              <div class="card verify-card hover-lift">
                <!-- 顶部状态图标 -->
                <div class="verify-status">
                  <div
                    class="status-icon"
                    :class="{ 'is-ready': canSubmit, 'is-loading': loading }"
                  >
                    <svg
                      v-if="canSubmit && !loading"
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
                      <path
                        class="status-check"
                        fill="none"
                        d="M14 27l7 7 16-16"
                      />
                    </svg>
                    <div v-else class="status-default">
                      <el-icon><Postcard /></el-icon>
                    </div>
                  </div>
                  <h3 class="verify-title">卡密核销</h3>
                  <p class="verify-subtitle">输入卡号和密码完成核销</p>
                </div>

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
              </div>
            </transition>
          </el-col>

          <!-- 结果区域 -->
          <el-col :xs="24" :lg="12">
            <transition name="bounce" mode="out-in">
              <!-- 核销成功 -->
              <div
                v-if="result.visible && result.success"
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

              <!-- 核销失败 -->
              <div
                v-else-if="result.visible && !result.success"
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
      </el-tab-pane>

      <!-- 批量核销 -->
      <el-tab-pane label="批量核销" name="batch">
        <el-row :gutter="20">
          <!-- 上传区域 -->
          <el-col :xs="24" :lg="12">
            <div class="card batch-card hover-lift">
              <div class="batch-header">
                <div class="batch-icon">
                  <el-icon><UploadFilled /></el-icon>
                </div>
                <div class="batch-header-text">
                  <h3>Excel批量核销</h3>
                  <p>上传Excel文件，批量核销卡密</p>
                </div>
              </div>

              <div class="upload-area">
                <el-upload
                  ref="uploadRef"
                  :auto-upload="false"
                  :show-file-list="true"
                  :limit="1"
                  accept=".xlsx,.xls"
                  :on-change="handleFileChange"
                  :on-remove="handleFileRemove"
                  drag
                >
                  <el-icon class="upload-icon"><Upload /></el-icon>
                  <p class="upload-text">将文件拖到此处，或<em>点击上传</em></p>
                  <p class="upload-hint">支持 .xlsx, .xls 格式，最大 10MB</p>
                </el-upload>
              </div>

              <div class="batch-actions">
                <el-button type="default" @click="downloadTemplate">
                  <el-icon><Download /></el-icon>
                  下载模板
                </el-button>
                <el-button
                  type="primary"
                  :loading="batchLoading"
                  :disabled="!selectedFile"
                  @click="handleBatchVerify"
                  class="batch-verify-btn"
                >
                  <template v-if="!batchLoading">
                    <el-icon><CircleCheck /></el-icon>
                    开始核销
                  </template>
                  <template v-else>
                    <span class="loading-spinner"></span>
                    核销中
                  </template>
                </el-button>
              </div>
            </div>
          </el-col>

          <!-- 结果区域 -->
          <el-col :xs="24" :lg="12">
            <transition name="fade" mode="out-in">
              <!-- 批量结果 -->
              <div
                v-if="batchResult.visible"
                class="card batch-result-card"
                key="batch-result"
              >
                <div class="result-stats">
                  <div class="stat-item total">
                    <div class="stat-value">{{ batchResult.totalCount }}</div>
                    <div class="stat-label">总条数</div>
                  </div>
                  <div class="stat-item success">
                    <div class="stat-value">{{ batchResult.successCount }}</div>
                    <div class="stat-label">成功</div>
                  </div>
                  <div class="stat-item fail">
                    <div class="stat-value">{{ batchResult.failCount }}</div>
                    <div class="stat-label">失败</div>
                  </div>
                </div>

                <div
                  v-if="
                    batchResult.successCards &&
                    batchResult.successCards.length > 0
                  "
                  class="success-section"
                >
                  <h4>
                    <el-icon class="success-icon"><CircleCheck /></el-icon>
                    成功核销的卡号
                  </h4>
                  <div class="success-list">
                    <el-tag
                      v-for="card in batchResult.successCards"
                      :key="card"
                      type="success"
                      size="small"
                      class="success-tag"
                    >
                      {{ card }}
                    </el-tag>
                  </div>
                </div>

                <div
                  v-if="
                    batchResult.failRecords &&
                    batchResult.failRecords.length > 0
                  "
                  class="fail-section"
                >
                  <h4>
                    <el-icon class="fail-icon"><CloseCircle /></el-icon>
                    核销失败记录
                  </h4>
                  <div class="fail-table-container">
                    <el-table
                      :data="batchResult.failRecords"
                      size="small"
                      border
                    >
                      <el-table-column
                        prop="rowNum"
                        label="行号"
                        width="80"
                        align="center"
                      />
                      <el-table-column
                        prop="cardNumber"
                        label="卡号"
                        min-width="120"
                      />
                      <el-table-column
                        prop="errorMessage"
                        label="错误原因"
                        min-width="200"
                      />
                    </el-table>
                  </div>
                  <el-button
                    type="danger"
                    size="small"
                    class="export-fail-btn"
                    @click="exportFailRecords"
                  >
                    <el-icon><Download /></el-icon>
                    导出失败记录
                  </el-button>
                </div>

                <el-button
                  type="primary"
                  @click="resetBatchForm"
                  class="reset-batch-btn"
                >
                  <el-icon><RefreshRight /></el-icon>
                  继续批量核销
                </el-button>
              </div>

              <!-- 批量说明 -->
              <div v-else class="card tips-card" key="batch-tips">
                <div class="tips-header">
                  <div class="tips-icon">
                    <el-icon><Document /></el-icon>
                  </div>
                  <div class="tips-header-text">
                    <h3>批量核销说明</h3>
                    <p>了解批量核销的使用方法</p>
                  </div>
                </div>
                <div class="tips-content">
                  <div
                    v-for="(tip, index) in batchTips"
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
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from "vue";
import {
  verifyCard,
  batchVerifyCard,
  downloadVerifyTemplate,
  exportFailRecords as exportFailRecordsApi,
} from "@/api/verify";
import toast from "@/utils/toast";

const activeTab = ref("single");

// ==================== 单张核销 ====================
const formRef = ref(null);
const loading = ref(false);
const inputFocus = ref("");

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

const canSubmit = computed(() => {
  return form.cardNumber.length === 9 && form.cardPassword.length === 6;
});

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

    if (navigator.vibrate) {
      navigator.vibrate([50, 30, 50]);
    }

    toast.notifySuccess("核销成功", `卡号 ${form.cardNumber} 已成功核销`);
  } catch (error) {
    result.visible = true;
    result.success = false;
    result.message = error.message || "核销失败，请检查卡号和密码";
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

// ==================== 批量核销 ====================
const uploadRef = ref(null);
const batchLoading = ref(false);
const selectedFile = ref(null);

const batchResult = reactive({
  visible: false,
  totalCount: 0,
  successCount: 0,
  failCount: 0,
  successCards: [],
  failRecords: [],
});

const batchTips = [
  {
    icon: "Download",
    type: "primary",
    title: "下载模板",
    desc: "请先下载Excel模板，按模板格式填写卡号和密码",
  },
  {
    icon: "Upload",
    type: "success",
    title: "上传文件",
    desc: "支持.xlsx和.xls格式，文件大小不超过10MB",
  },
  {
    icon: "DataLine",
    type: "info",
    title: "数据格式",
    desc: "第一列为卡号(9位数字)，第二列为密码(6位大写字母和数字)",
  },
  {
    icon: "Warning",
    type: "warning",
    title: "注意事项",
    desc: "单次建议不超过1000条，核销过程中请勿关闭页面",
  },
];

const handleFileChange = (file) => {
  selectedFile.value = file.raw;
};

const handleFileRemove = () => {
  selectedFile.value = null;
};

const downloadTemplate = async () => {
  try {
    const response = await downloadVerifyTemplate();
    const blob = new Blob([response.data], {
      type: "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
    });
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement("a");
    link.href = url;
    link.download = "卡密核销模板.xlsx";
    link.click();
    window.URL.revokeObjectURL(url);
    toast.notifySuccess("下载成功", "模板文件已下载");
  } catch (error) {
    toast.notifyError("下载失败", error.message || "模板下载失败");
  }
};

const handleBatchVerify = async () => {
  if (!selectedFile.value) {
    toast.notifyWarning("请选择文件", "请先选择要上传的Excel文件");
    return;
  }

  try {
    batchLoading.value = true;

    const result = await batchVerifyCard(selectedFile.value);

    batchResult.visible = true;
    batchResult.totalCount = result.data.totalCount;
    batchResult.successCount = result.data.successCount;
    batchResult.failCount = result.data.failCount;
    batchResult.successCards = result.data.successCards || [];
    batchResult.failRecords = result.data.failRecords || [];

    if (batchResult.failCount > 0) {
      toast.notifyWarning(
        "核销完成",
        `成功: ${batchResult.successCount} 条，失败: ${batchResult.failCount} 条`,
      );
    } else {
      toast.notifySuccess(
        "核销完成",
        `成功核销 ${batchResult.successCount} 条记录`,
      );
    }
  } catch (error) {
    toast.notifyError("批量核销失败", error.message || "请检查文件格式");
  } finally {
    batchLoading.value = false;
  }
};

const exportFailRecords = async () => {
  try {
    const response = await exportFailRecordsApi(batchResult);
    const blob = new Blob([response.data], {
      type: "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
    });
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement("a");
    link.href = url;
    link.download = `核销失败记录_${new Date().toLocaleDateString()}.xlsx`;
    link.click();
    window.URL.revokeObjectURL(url);
    toast.notifySuccess("导出成功", "失败记录已导出");
  } catch (error) {
    toast.notifyError("导出失败", error.message || "导出失败");
  }
};

const resetBatchForm = () => {
  batchResult.visible = false;
  batchResult.totalCount = 0;
  batchResult.successCount = 0;
  batchResult.failCount = 0;
  batchResult.successCards = [];
  batchResult.failRecords = [];
  selectedFile.value = null;
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

.verify-tabs {
  :deep(.el-tabs__nav-wrap) {
    margin-bottom: $spacing-lg;
  }

  :deep(.el-tabs__item) {
    font-size: $font-size-md;
    font-weight: $font-weight-semibold;
  }
}

// ==================== 单张核销样式 ====================

.verify-card {
  text-align: center;
  padding: $spacing-2xl;
  height: 100%;
  min-height: 400px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

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

// ==================== 批量核销样式 ====================

.batch-card {
  padding: $spacing-2xl;
  height: 100%;
  min-height: 400px;
  display: flex;
  flex-direction: column;
}

.batch-header {
  display: flex;
  align-items: center;
  gap: $spacing-base;
  margin-bottom: $spacing-xl;

  .batch-icon {
    width: 56px;
    height: 56px;
    border-radius: $radius-lg;
    background: linear-gradient(135deg, $primary-color 0%, $primary-dark 100%);
    display: flex;
    align-items: center;
    justify-content: center;

    .el-icon {
      font-size: 28px;
      color: #fff;
    }
  }

  .batch-header-text {
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

.upload-area {
  flex: 1;
  margin-bottom: $spacing-lg;

  :deep(.el-upload) {
    width: 100%;
  }

  :deep(.el-upload-dragger) {
    width: 100%;
    min-height: 200px;
    display: flex;
    flex-direction: column;
    justify-content: center;
    border: 2px dashed $border-color;
    border-radius: $radius-lg;
    transition: all 0.3s $ease-smooth;

    &:hover {
      border-color: $primary-color;
      background: rgba($primary-color, 0.02);
    }

    &.is-dragover {
      border-color: $primary-color;
      background: rgba($primary-color, 0.05);
    }
  }

  .upload-icon {
    font-size: 48px;
    color: $primary-color;
    margin-bottom: $spacing-base;
  }

  .upload-text {
    font-size: $font-size-md;
    color: $text-primary;
    margin: 0 0 $spacing-xs;

    em {
      color: $primary-color;
      font-style: normal;
    }
  }

  .upload-hint {
    font-size: $font-size-sm;
    color: $text-secondary;
    margin: 0;
  }
}

.batch-actions {
  display: flex;
  gap: $spacing-base;
  justify-content: space-between;
  align-items: center;
}

.batch-verify-btn {
  min-width: 140px;
  height: 40px;
  background: linear-gradient(
    135deg,
    $success-color 0%,
    darken($success-color, 10%) 100%
  );
  border: none;

  &:not(:disabled):hover {
    box-shadow: 0 6px 16px rgba($success-color, 0.35);
  }

  &:disabled {
    background: $fill-light;
    color: $text-disabled;
  }
}

// ==================== 批量结果卡片 ====================

.batch-result-card {
  padding: $spacing-2xl;
  height: 100%;
  min-height: 400px;
  display: flex;
  flex-direction: column;
}

.result-stats {
  display: flex;
  gap: $spacing-base;
  margin-bottom: $spacing-xl;

  .stat-item {
    flex: 1;
    text-align: center;
    padding: $spacing-base;
    border-radius: $radius-lg;

    &.total {
      background: rgba($primary-color, 0.1);

      .stat-value {
        color: $primary-color;
      }
    }

    &.success {
      background: rgba($success-color, 0.1);

      .stat-value {
        color: $success-color;
      }
    }

    &.fail {
      background: rgba($danger-color, 0.1);

      .stat-value {
        color: $danger-color;
      }
    }

    .stat-value {
      font-size: $font-size-2xl;
      font-weight: $font-weight-bold;
      font-family: $font-family-mono;
      margin-bottom: $spacing-xs;
    }

    .stat-label {
      font-size: $font-size-sm;
      color: $text-secondary;
    }
  }
}

.success-section,
.fail-section {
  margin-bottom: $spacing-lg;

  h4 {
    display: flex;
    align-items: center;
    gap: $spacing-xs;
    font-size: $font-size-base;
    font-weight: $font-weight-semibold;
    color: $text-primary;
    margin: 0 0 $spacing-base;

    .success-icon {
      color: $success-color;
    }

    .fail-icon {
      color: $danger-color;
    }
  }
}

.success-list {
  display: flex;
  flex-wrap: wrap;
  gap: $spacing-xs;
  max-height: 120px;
  overflow-y: auto;
  padding: $spacing-xs;
  background: $fill-lighter;
  border-radius: $radius-md;
}

.success-tag {
  font-family: $font-family-mono;
}

.fail-table-container {
  max-height: 200px;
  overflow: auto;
  border-radius: $radius-md;
  border: 1px solid $border-light;
  margin-bottom: $spacing-base;

  :deep(.el-table) {
    font-size: $font-size-sm;
  }

  :deep(.el-table__row) {
    td {
      padding: 8px 12px;
    }
  }
}

.export-fail-btn {
  margin-bottom: $spacing-lg;
}

.reset-batch-btn {
  margin-top: auto;
  align-self: center;
  min-width: 160px;
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
  transition: opacity 0.3s $ease-smooth;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

@keyframes circleIn {
  from {
    transform: scale(0);
    opacity: 0;
  }
  to {
    transform: scale(1);
    opacity: 1;
  }
}
</style>
