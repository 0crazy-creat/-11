import request from '@/utils/request'

/**
 * 获取公告列表
 */
export function getAnnouncements(params) {
  return request({
    url: '/announcements',
    method: 'get',
    params
  })
}

/**
 * 获取公告详情
 */
export function getAnnouncementDetail(id) {
  return request({
    url: `/announcements/${id}`,
    method: 'get'
  })
}

/**
 * 创建公告（管理员）
 */
export function createAnnouncement(data) {
  return request({
    url: '/announcements',
    method: 'post',
    data
  })
}

/**
 * 更新公告（管理员）
 */
export function updateAnnouncement(id, data) {
  return request({
    url: `/announcements/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除公告（管理员）
 */
export function deleteAnnouncement(id) {
  return request({
    url: `/announcements/${id}`,
    method: 'delete'
  })
}

/**
 * 标记公告为已读
 */
export function markAnnouncementRead(id) {
  return request({
    url: `/announcements/${id}/read`,
    method: 'post'
  })
}

/**
 * 获取最新公告
 */
export function getLatestAnnouncement() {
  return request({
    url: '/announcements/latest',
    method: 'get'
  })
}
