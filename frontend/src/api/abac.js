import request from '@/utils/request';

/**
 * 获取 ABAC 策略列表
 */
export function getPolicies(params) {
  return request({
    url: '/api/admin/abac/policies',
    method: 'get',
    params
  });
}

/**
 * 获取单个策略详情
 */
export function getPolicy(id) {
  return request({
    url: `/api/admin/abac/policies/${id}`,
    method: 'get'
  });
}

/**
 * 创建策略
 */
export function createPolicy(data) {
  return request({
    url: '/api/admin/abac/policies',
    method: 'post',
    data
  });
}

/**
 * 更新策略
 */
export function updatePolicy(id, data) {
  return request({
    url: `/api/admin/abac/policies/${id}`,
    method: 'put',
    data
  });
}

/**
 * 删除策略
 */
export function deletePolicy(id) {
  return request({
    url: `/api/admin/abac/policies/${id}`,
    method: 'delete'
  });
}

/**
 * 测试策略评估
 */
export function testPolicy(data) {
  return request({
    url: '/api/admin/abac/test',
    method: 'post',
    data
  });
}
