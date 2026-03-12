import request from '@/utils/request'

/**
 * 一键发卡
 */
export function generateCards(data) {
  return request({
    url: '/card/generate',
    method: 'post',
    data,
  })
}

/**
 * 查询卡密列表
 */
export function getCardList(params) {
  return request({
    url: '/card/list',
    method: 'get',
    params,
  })
}

/**
 * 导出卡密
 */
export function exportCards(params) {
  return request({
    url: '/card/export',
    method: 'get',
    params,
    responseType: 'blob',
  })
}

/**
 * 按批次回收卡密
 */
export function recycleBatch(batchNumber) {
  return request({
    url: '/card/recycle/batch',
    method: 'put',
    params: { batchNumber },
  })
}

/**
 * 单独回收卡密
 */
export function recycleSingle(cardNumber) {
  return request({
    url: '/card/recycle/single',
    method: 'put',
    params: { cardNumber },
  })
}

/**
 * 查询批次列表
 */
export function getBatchList(params) {
  return request({
    url: '/card/batch/list',
    method: 'get',
    params,
  })
}

/**
 * 获取所有批次号列表
 */
export function getBatchNumbers() {
  return request({
    url: '/card/batch/numbers',
    method: 'get',
  })
}
