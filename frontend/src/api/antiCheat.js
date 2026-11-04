import request from '@/utils/request';

/**
 * 查询用户风险摘要
 */
export function getUserRiskSummary(contestId, userId) {
  return request({
    url: `/api/ctf/contests/${contestId}/anti-cheat/users/${userId}/risk`,
    method: 'get'
  });
}

/**
 * 查询赛事高风险用户列表
 */
export function getHighRiskUsers(contestId, minRiskScore = 60) {
  return request({
    url: `/api/ctf/contests/${contestId}/anti-cheat/high-risk`,
    method: 'get',
    params: { minRiskScore }
  });
}

/**
 * 暂停用户（管理员操作）
 */
export function suspendUser(contestId, userId, reason) {
  return request({
    url: `/api/ctf/contests/${contestId}/anti-cheat/users/${userId}/suspend`,
    method: 'post',
    data: { reason }
  });
}

/**
 * 标记为误判（管理员操作）
 */
export function clearUser(contestId, userId, note) {
  return request({
    url: `/api/ctf/contests/${contestId}/anti-cheat/users/${userId}/clear`,
    method: 'post',
    data: { note }
  });
}

/**
 * 提交设备指纹（前端自动调用）
 */
export function submitFingerprint(contestId, deviceId, fingerprint, userAgent) {
  return request({
    url: `/api/ctf/contests/${contestId}/anti-cheat/fingerprint`,
    method: 'post',
    data: {
      deviceId,
      fingerprint,
      userAgent
    }
  });
}
