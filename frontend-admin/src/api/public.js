import request from '@/utils/request'

/**
 * 公开查询卡密状态
 */
export function queryCardStatus(params) {
  return request({
    url: '/public/query',
    method: 'get',
    params,
  })
}
