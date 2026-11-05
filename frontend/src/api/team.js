import request from '@/utils/request';

/**
 * 赛事参赛记录（管理端 ContestTeamController：/api/contests/{contestId}/teams）。
 *
 * 战队本身已改为全局模块，建队/入队请使用 @/api 里的 teamApi。
 */
export function getTeams(contestId) {
  return request({ url: `/contests/${contestId}/teams`, method: 'get' });
}
