import request from '@/utils/request'

/**
 * 查询用户列表
 */
export function getUserList(params) {
  return request({
    url: '/user/list',
    method: 'get',
    params,
  })
}

/**
 * 添加用户
 */
export function addUser(data) {
  return request({
    url: '/user/add',
    method: 'post',
    data,
  })
}

/**
 * 修改用户
 */
export function updateUser(data) {
  return request({
    url: '/user/update',
    method: 'put',
    data,
  })
}

/**
 * 删除用户
 */
export function deleteUser(id) {
  return request({
    url: `/user/delete/${id}`,
    method: 'delete',
  })
}

/**
 * 根据ID查询用户
 */
export function getUserById(id) {
  return request({
    url: `/user/${id}`,
    method: 'get',
  })
}
