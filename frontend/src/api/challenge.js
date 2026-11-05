import request from '@/utils/request'

/**
 * 获取题目列表（练习模式）
 * 后端：PracticeController GET /api/practice/challenges（公开，隐藏 flag）
 */
export function getChallenges(params) {
  return request({
    url: '/practice/challenges',
    method: 'get',
    params
  })
}

/**
 * 获取题目详情（练习模式）
 * 后端：PracticeController GET /api/practice/challenges/{id}（公开，隐藏 flag/answer）
 * 注意：不要走 /ctf/challenges/{id}（那是 CTF 赛事兼容层，返回 Map 且不含 practice 语义）
 */
export function getChallengeDetail(id) {
  return request({
    url: `/practice/challenges/${id}`,
    method: 'get'
  })
}

/**
 * 提交 Flag（练习模式）
 * 后端：PracticeController POST /api/practice/challenges/{id}/submit，body = { flag }
 * 只做 flag 比对，不写赛事提交表，不影响排行榜。
 */
export function submitPracticeFlag(challengeId, flag) {
  return request({
    url: `/practice/challenges/${challengeId}/submit`,
    method: 'post',
    data: { flag }
  })
}

/**
 * 练习容器接口（无赛事上下文，后端 PracticeController）
 * 后端：/api/practice/challenges/{id}/container*（需登录）
 */
export function getPracticeContainer(id) {
  return request({ url: `/practice/challenges/${id}/container`, method: 'get' })
}
export function startPracticeContainer(id) {
  return request({ url: `/practice/challenges/${id}/container/start`, method: 'post' })
}
export function resetPracticeContainer(id) {
  return request({ url: `/practice/challenges/${id}/container/reset`, method: 'post' })
}
export function destroyPracticeContainer(id) {
  return request({ url: `/practice/challenges/${id}/container/destroy`, method: 'post' })
}
export function extendPracticeContainer(id) {
  return request({ url: `/practice/challenges/${id}/container/extend`, method: 'post' })
}