import request from '@/utils/request'

/**
 * 获取赛事列表
 */
export function getContests(params) {
  return request({
    url: '/contests',
    method: 'get',
    params
  })
}

/**
 * 获取赛事详情
 */
export function getContestDetail(id) {
  return request({
    url: `/contests/${id}`,
    method: 'get'
  })
}

/**
 * 创建赛事（管理员）
 */
export function createContest(data) {
  return request({
    url: '/contests',
    method: 'post',
    data
  })
}

/**
 * 更新赛事（管理员）
 */
export function updateContest(id, data) {
  return request({
    url: `/contests/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除赛事（管理员）
 */
export function deleteContest(id) {
  return request({
    url: `/contests/${id}`,
    method: 'delete'
  })
}

/**
 * 变更赛事状态（管理员）
 */
export function changeContestStatus(id, status) {
  return request({
    url: `/contests/${id}/status`,
    method: 'post',
    data: { status }
  })
}

/**
 * 获取我的赛事
 */
export function getMyContests() {
  return request({
    url: '/contests/mine',
    method: 'get'
  })
}

/**
 * 报名赛事
 */
export function signUpContest(id, data) {
  return request({
    url: `/contests/${id}/signup`,
    method: 'post',
    data
  })
}

/**
 * 获取赛事排行榜
 */
export function getContestRanking(id, params) {
  return request({
    url: `/contests/${id}/ranking`,
    method: 'get',
    params
  })
}
