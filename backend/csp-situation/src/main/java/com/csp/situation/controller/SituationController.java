package com.csp.situation.controller;

import com.csp.awd.dto.TargetVO;
import com.csp.common.api.Result;
import com.csp.ctf.dto.RankItem;
import com.csp.security.annotation.PublicApi;
import com.csp.situation.dto.OverviewVO;
import com.csp.situation.dto.ScreenVO;
import com.csp.situation.dto.TeamHomeVO;
import com.csp.situation.service.SituationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 态势感知接口：排行榜、战队主页、3D 大屏、靶机监控。
 *
 * <p>均为只读接口；大屏类接口标注 {@link PublicApi}，便于现场投屏免登录访问。</p>
 */
@RestController
@RequestMapping("/api/situation")
@RequiredArgsConstructor
public class SituationController {

    private final SituationService situationService;

    /** 赛事总览指标。 */
    @PublicApi
    @GetMapping("/overview/{contestId}")
    public Result<OverviewVO> overview(@PathVariable Long contestId) {
        return Result.ok(situationService.overview(contestId));
    }

    /** 排行榜 TopN。 */
    @PublicApi
    @GetMapping("/ranking/{contestId}")
    public Result<List<RankItem>> ranking(@PathVariable Long contestId,
                                          @RequestParam(required = false) Integer topN) {
        return Result.ok(situationService.ranking(contestId, topN));
    }

    /** 3D 态势大屏数据包。 */
    @PublicApi
    @GetMapping("/screen/{contestId}")
    public Result<ScreenVO> screen(@PathVariable Long contestId,
                                   @RequestParam(required = false) Integer topN) {
        return Result.ok(situationService.screen(contestId, topN));
    }

    /** 战队主页。 */
    @PublicApi
    @GetMapping("/team/{contestId}/{teamId}")
    public Result<TeamHomeVO> teamHome(@PathVariable Long contestId, @PathVariable Long teamId) {
        return Result.ok(situationService.teamHome(contestId, teamId));
    }

    /** 靶机监控（teamId 可选）。 */
    @GetMapping("/targets/{contestId}")
    public Result<List<TargetVO>> targets(@PathVariable Long contestId,
                                          @RequestParam(required = false) Long teamId) {
        return Result.ok(situationService.targets(contestId, teamId));
    }

    /* ================= 前端兼容端点（无 contestId 路径参数版本） =================
     * 前端 Situation3D.vue / Ranking.vue / TeamHome.vue 调用的是
     *   /situation/overview?contestId=..  /situation/ranking?contestId=..
     *   /situation/3d?contestId=..        /situation/monitor?contestId=..
     *   /situation/teams/{teamId}
     * 旧版只有 /overview/{contestId} 这种路径变量形式，导致上述请求 404 -> 被当静态资源
     * -> 前端弹“警告 400”。这里补齐查询参数版本，contestId 缺省时自动选最近进行中/最近赛事。
     */

    /** 赛事总览（查询参数版）。 */
    @PublicApi
    @GetMapping("/overview")
    public Result<OverviewVO> overviewByQuery(@RequestParam(required = false) Long contestId) {
        return Result.ok(situationService.overview(resolveContestId(contestId)));
    }

    /** 排行榜（查询参数版）。 */
    @PublicApi
    @GetMapping("/ranking")
    public Result<List<RankItem>> rankingByQuery(@RequestParam(required = false) Long contestId,
                                                 @RequestParam(required = false) Integer topN,
                                                 @RequestParam(required = false) Integer limit) {
        Integer n = topN != null ? topN : limit;
        return Result.ok(situationService.ranking(resolveContestId(contestId), n));
    }

    /** 3D 大屏数据包（查询参数版）。 */
    @PublicApi
    @GetMapping("/3d")
    public Result<ScreenVO> threeDByQuery(@RequestParam(required = false) Long contestId,
                                          @RequestParam(required = false) Integer topN) {
        return Result.ok(situationService.screen(resolveContestId(contestId), topN));
    }

    /** 靶机监控（查询参数版，供“屏幕管理/靶机监控”页面使用）。 */
    @PublicApi
    @GetMapping("/monitor")
    public Result<List<TargetVO>> monitor(@RequestParam(required = false) Long contestId,
                                          @RequestParam(required = false) Long teamId) {
        return Result.ok(situationService.targets(resolveContestId(contestId), teamId));
    }

    /** 战队主页（无 contestId 版本：按 teamId 自动反查所属赛事）。 */
    @PublicApi
    @GetMapping("/teams/{teamId}")
    public Result<TeamHomeVO> teamProfile(@PathVariable Long teamId) {
        return Result.ok(situationService.teamHomeByTeamId(teamId));
    }

    /** contestId 缺省时挑一个“可展示”的赛事：优先 RUNNING，其次最近创建。 */
    private Long resolveContestId(Long contestId) {
        if (contestId != null) return contestId;
        return situationService.pickDisplayContestId();
    }
}
