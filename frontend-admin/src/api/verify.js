import request from "@/utils/request";

/**
 * 核销卡密
 */
export function verifyCard(data) {
  return request({
    url: "/verify/use",
    method: "post",
    data,
  });
}

/**
 * 查询核销记录
 */
export function getVerifyHistory(params) {
  return request({
    url: "/verify/history",
    method: "get",
    params,
  });
}

/**
 * 导出核销记录
 */
export function exportVerifyHistory(params) {
  return request({
    url: "/verify/export",
    method: "get",
    params,
    responseType: "blob",
  });
}

/**
 * 批量核销卡密
 */
export function verifyCardBatch(data) {
  return request({
    url: "/verify/batch",
    method: "post",
    data,
    headers: {
      "Content-Type": "multipart/form-data",
    },
  });
}
