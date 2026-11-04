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
 * 后端：CtfCompatController POST /api/ctf/submit，body = { questionId, flag }
 * 兼容层会把提交映射到 SubmissionService.submit()
 */
export function submitFlag(challengeId, flag) {
  return request({
    url: '/ctf/submit',
    method: 'post',
    data: { questionId: challengeId, flag }
  })
}

/**
 * 容器相关接口：练习题不直接持有容器，容器维度是「赛事 + 战队 + 题目」
 * 真实端点在 CTF 靶机：/api/ctf/contests/{contestId}/targets/{start|reset|destroy}
 * 因此这里不再提供 /ctf/challenges/{id}/container/* 死路径。
 *
 * 若后续需要在练习页启动容器，请使用 api/index.js 中的：
 *   - getTargetStatus(contestId, questionId)
 *   - startTarget(contestId, questionId)
 *   - resetTarget(contestId, questionId)
 *   - destroyTarget(contestId, questionId)
 */