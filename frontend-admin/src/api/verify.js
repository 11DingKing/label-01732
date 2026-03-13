import request from '@/utils/request'

/**
 * 核销卡密
 */
export function verifyCard(data) {
  return request({
    url: '/verify/use',
    method: 'post',
    data,
  })
}

/**
 * 批量核销卡密
 */
export function batchVerifyCard(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/verify/batch',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  })
}

/**
 * 下载批量核销模板
 */
export function downloadVerifyTemplate() {
  return request({
    url: '/verify/template',
    method: 'get',
    responseType: 'blob',
  })
}

/**
 * 导出核销失败记录
 */
export function exportFailRecords(data) {
  return request({
    url: '/verify/export-fail',
    method: 'post',
    data,
    responseType: 'blob',
  })
}

/**
 * 查询核销记录
 */
export function getVerifyHistory(params) {
  return request({
    url: '/verify/history',
    method: 'get',
    params,
  })
}

/**
 * 导出核销记录
 */
export function exportVerifyHistory(params) {
  return request({
    url: '/verify/export',
    method: 'get',
    params,
    responseType: 'blob',
  })
}
