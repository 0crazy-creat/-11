import request from '@/utils/request'

/**
 * AWD 靶机管理（对齐后端 AwdTargetController）
 *
 * 后端真实端点（/api/awd/targets）：
 *   GET    /api/awd/targets?contestId=&teamId=     靶机列表（awd:control 或 contest:view）
 *   GET    /api/awd/targets/mine?contestId=        我方靶机（登录）
 *   POST   /api/awd/targets/mine/start             启动我方靶机（登录）
 *   POST   /api/awd/targets/provision              编排单台（awd:control）
 *   POST   /api/awd/targets/provision-all          全场编排（awd:control）
 *   POST   /api/awd/targets/reset                  重置（awd:control）
 *   POST   /api/awd/targets/destroy                销毁单台（awd:control）
 *   POST   /api/awd/targets/destroy-all            销毁全场（awd:control）
 *   POST   /api/awd/targets/health-check           健康巡检（awd:control）
 *
 * 注意：原文件中的 /awd/targets/{id}/start|stop 等路径在后端不存在，已移除。
 * 选手端应使用 listMine / startMine；裁判端使用 provision/reset/destroy 等。
 */

/** 靶机列表（裁判/管理员视角，可按战队过滤） */
export function getTargets(params) {
  return request({
    url: '/awd/targets',
    method: 'get',
    params
  })
}

/** 我方靶机列表（选手端） */
export function listMyTargets(contestId) {
  return request({
    url: '/awd/targets/mine',
    method: 'get',
    params: { contestId }
  })
}

/** 启动我方靶机（选手端，返回访问地址+随机端口） */
export function startMyTarget(contestId, questionId, roundNo) {
  return request({
    url: '/awd/targets/mine/start',
    method: 'post',
    params: { contestId, ...(questionId ? { questionId } : {}), ...(roundNo ? { roundNo } : {}) }
  })
}

/** 编排单台靶机（裁判） */
export function provisionTarget(params) {
  return request({
    url: '/awd/targets/provision',
    method: 'post',
    params
  })
}

/** 全场编排（裁判） */
export function provisionAllTargets(contestId) {
  return request({
    url: '/awd/targets/provision-all',
    method: 'post',
    params: { contestId }
  })
}

/** 重置靶机（裁判） */
export function resetTarget(params) {
  return request({
    url: '/awd/targets/reset',
    method: 'post',
    params
  })
}

/** 销毁单台靶机（裁判） */
export function destroyTarget(params) {
  return request({
    url: '/awd/targets/destroy',
    method: 'post',
    params
  })
}

/** 销毁全场靶机（裁判） */
export function destroyAllTargets(contestId) {
  return request({
    url: '/awd/targets/destroy-all',
    method: 'post',
    params: { contestId }
  })
}

/** 健康巡检（裁判） */
export function healthCheckTargets(contestId) {
  return request({
    url: '/awd/targets/health-check',
    method: 'post',
    params: { contestId }
  })
}