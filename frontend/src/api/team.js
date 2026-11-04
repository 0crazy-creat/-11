import request from '@/utils/request';

/**
 * 创建战队
 */
export function createTeam(contestId, data) {
  return request({
    url: `/ctf/contests/${contestId}/teams`,
    method: 'post',
    data
  });
}

/**
 * 加入战队
 */
export function joinTeam(contestId, teamId) {
  return request({
    url: `/ctf/contests/${contestId}/teams/join`,
    method: 'post',
    data: { teamId }
  });
}

/**
 * 通过邀请码加入战队
 */
export function joinByInviteCode(contestId, inviteCode) {
  return request({
    url: `/ctf/contests/${contestId}/teams/join-by-code`,
    method: 'post',
    data: { inviteCode }
  });
}

/**
 * 退出/取消报名
 */
export function quitTeam(contestId) {
  return request({
    url: `/ctf/contests/${contestId}/teams/quit`,
    method: 'delete'
  });
}

/**
 * 查询我的战队
 */
export function getMyTeam(contestId) {
  return request({
    url: `/ctf/contests/${contestId}/teams/mine`,
    method: 'get'
  });
}

/**
 * 查询战队成员列表
 */
export function getTeamMembers(contestId, teamId) {
  return request({
    url: `/ctf/contests/${contestId}/teams/${teamId}/members`,
    method: 'get'
  });
}

/**
 * 生成邀请码
 */
export function generateInviteCode(contestId, teamId) {
  return request({
    url: `/ctf/contests/${contestId}/teams/${teamId}/invite-code`,
    method: 'post'
  });
}

/**
 * 刷新邀请码
 */
export function refreshInviteCode(contestId, teamId) {
  return request({
    url: `/ctf/contests/${contestId}/teams/${teamId}/invite-code`,
    method: 'put'
  });
}

/**
 * 审核战队（管理员/裁判）
 */
export function reviewTeam(contestId, teamId, status) {
  return request({
    url: `/ctf/contests/${contestId}/teams/${teamId}/review`,
    method: 'post',
    params: { status }
  });
}

/**
 * 获取战队列表
 */
export function getTeams(contestId) {
  return request({
    url: `/ctf/contests/${contestId}/teams`,
    method: 'get'
  });
}
