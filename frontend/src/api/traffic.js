import request from '@/utils/request';

/**
 * 启动流量捕获
 */
export function startCapture(contestId, data) {
  return request({
    url: `/awd/contests/${contestId}/traffic/capture/start`,
    method: 'post',
    params: data
  });
}

/**
 * 停止流量捕获
 */
export function stopCapture(contestId, captureId) {
  return request({
    url: `/awd/contests/${contestId}/traffic/capture/${captureId}/stop`,
    method: 'post'
  });
}

/**
 * 查询捕获列表
 */
export function listCaptures(contestId, params) {
  return request({
    url: `/awd/contests/${contestId}/traffic/captures`,
    method: 'get',
    params
  });
}

/**
 * 查询捕获详情
 */
export function getCapture(contestId, captureId) {
  return request({
    url: `/awd/contests/${contestId}/traffic/capture/${captureId}`,
    method: 'get'
  });
}

/**
 * 查询分析结果
 */
export function getAnalysis(contestId, captureId) {
  return request({
    url: `/awd/contests/${contestId}/traffic/capture/${captureId}/analysis`,
    method: 'get'
  });
}

/**
 * 手动触发分析
 */
export function triggerAnalysis(contestId, captureId) {
  return request({
    url: `/awd/contests/${contestId}/traffic/capture/${captureId}/analyze`,
    method: 'post'
  });
}

/**
 * 下载 pcap 文件
 */
export function downloadPcap(contestId, captureId) {
  return request({
    url: `/awd/contests/${contestId}/traffic/capture/${captureId}/download`,
    method: 'get',
    responseType: 'blob'
  }).then(res => {
    const url = window.URL.createObjectURL(new Blob([res.data]));
    return url;
  });
}

/**
 * 清理过期文件
 */
export function cleanupExpired(contestId) {
  return request({
    url: `/awd/contests/${contestId}/traffic/cleanup`,
    method: 'delete'
  });
}
