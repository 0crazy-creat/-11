import request from '@/utils/request';

/**
 * 获取比赛靶机列表
 */
export function getTargets(contestId) {
  return request({
    url: `/awd/contests/${contestId}/targets`,
    method: 'get'
  });
}

/**
 * 获取靶机详情
 */
export function getTargetDetail(contestId, targetId) {
  return request({
    url: `/awd/contests/${contestId}/targets/${targetId}`,
    method: 'get'
  });
}

/**
 * 创建靶机
 */
export function createTarget(contestId, data) {
  return request({
    url: `/awd/contests/${contestId}/targets`,
    method: 'post',
    data
  });
}

/**
 * 更新靶机
 */
export function updateTarget(contestId, targetId, data) {
  return request({
    url: `/awd/contests/${contestId}/targets/${targetId}`,
    method: 'put',
    data
  });
}

/**
 * 删除靶机
 */
export function deleteTarget(contestId, targetId) {
  return request({
    url: `/awd/contests/${contestId}/targets/${targetId}`,
    method: 'delete'
  });
}

/**
 * 启动靶机容器
 */
export function startTarget(contestId, targetId) {
  return request({
    url: `/awd/contests/${contestId}/targets/${targetId}/start`,
    method: 'post'
  });
}

/**
 * 停止靶机容器
 */
export function stopTarget(contestId, targetId) {
  return request({
    url: `/awd/contests/${contestId}/targets/${targetId}/stop`,
    method: 'post'
  });
}
