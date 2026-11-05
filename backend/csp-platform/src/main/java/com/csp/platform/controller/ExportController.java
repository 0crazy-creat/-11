package com.csp.platform.controller;

import com.csp.common.api.ResultCode;
import com.csp.common.exception.BizException;
import com.csp.common.security.LoginUser;
import com.csp.ctf.dto.BoardDetailVO;
import com.csp.ctf.dto.ChallengeMeta;
import com.csp.ctf.dto.FirstBloodItem;
import com.csp.ctf.dto.RankItem;
import com.csp.ctf.dto.SolveCell;
import com.csp.ctf.dto.StageVO;
import com.csp.ctf.entity.Contest;
import com.csp.ctf.entity.Submission;
import com.csp.ctf.mapper.ContestMapper;
import com.csp.ctf.mapper.SubmissionMapper;
import com.csp.ctf.service.ScoreboardService;
import com.csp.platform.entity.Writeup;
import com.csp.platform.mapper.WriteupMapper;
import com.csp.security.annotation.RequirePermission;
import com.csp.security.core.UserContext;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.bind.annotation.*;

import java.io.PrintWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** 数据导出：成绩/题解 CSV 与 XLSX。 */
@Tag(name = "数据导出")
@RestController
@RequestMapping("/api/export")
@RequiredArgsConstructor
public class ExportController {

    private final SubmissionMapper submissionMapper;
    private final WriteupMapper writeupMapper;
    private final ContestMapper contestMapper;
    private final ScoreboardService scoreboardService;
    private static final DateTimeFormatter DT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Operation(summary = "导出提交记录 CSV")
    @RequirePermission("contest:control")
    @GetMapping("/submissions.csv")
    public void submissionsCsv(@RequestParam(required = false) Long contestId,
                               HttpServletResponse resp) throws Exception {
        List<Submission> rows = submissionMapper.selectList(new LambdaQueryWrapper<Submission>()
                .eq(contestId != null, Submission::getContestId, contestId)
                .orderByDesc(Submission::getCreatedAt)
                .last("LIMIT 5000"));
        prep(resp, "submissions", "csv", "text/csv;charset=UTF-8");
        PrintWriter w = resp.getWriter();
        w.write('\uFEFF');  // BOM，Excel 打开中文不乱码
        w.println("ID,赛事ID,战队ID,用户ID,题目ID,类型,是否正确,得分,轮次,IP,耗时ms,提交时间");
        for (Submission s : rows) {
            w.println(csv(s.getId(), s.getContestId(), s.getTeamId(), s.getUserId(), s.getQuestionId(),
                    s.getSubmitType(), Boolean.TRUE.equals(s.getCorrect()) ? "是" : "否",
                    s.getScore(), s.getRoundNo(), s.getIp(), s.getElapsedMs(),
                    s.getCreatedAt() == null ? "" : s.getCreatedAt().format(DT)));
        }
        w.flush();
    }

    @Operation(summary = "导出提交记录 XLSX")
    @RequirePermission("contest:control")
    @GetMapping("/submissions.xlsx")
    public void submissionsXlsx(@RequestParam(required = false) Long contestId,
                                HttpServletResponse resp) throws Exception {
        List<Submission> rows = submissionMapper.selectList(new LambdaQueryWrapper<Submission>()
                .eq(contestId != null, Submission::getContestId, contestId)
                .orderByDesc(Submission::getCreatedAt)
                .last("LIMIT 5000"));
        prep(resp, "submissions", "xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sh = wb.createSheet("提交记录");
            String[] head = {"ID", "赛事ID", "战队ID", "用户ID", "题目ID", "类型", "是否正确", "得分", "轮次", "IP", "耗时ms", "提交时间"};
            Row h = sh.createRow(0);
            for (int i = 0; i < head.length; i++) h.createCell(i).setCellValue(head[i]);
            int r = 1;
            for (Submission s : rows) {
                Row row = sh.createRow(r++);
                row.createCell(0).setCellValue(s.getId() == null ? 0 : s.getId());
                row.createCell(1).setCellValue(s.getContestId() == null ? 0 : s.getContestId());
                row.createCell(2).setCellValue(s.getTeamId() == null ? 0 : s.getTeamId());
                row.createCell(3).setCellValue(s.getUserId() == null ? 0 : s.getUserId());
                row.createCell(4).setCellValue(s.getQuestionId() == null ? 0 : s.getQuestionId());
                row.createCell(5).setCellValue(nvl(s.getSubmitType()));
                row.createCell(6).setCellValue(Boolean.TRUE.equals(s.getCorrect()) ? "是" : "否");
                row.createCell(7).setCellValue(s.getScore() == null ? 0 : s.getScore());
                row.createCell(8).setCellValue(s.getRoundNo() == null ? 0 : s.getRoundNo());
                row.createCell(9).setCellValue(nvl(s.getIp()));
                row.createCell(10).setCellValue(s.getElapsedMs() == null ? 0 : s.getElapsedMs());
                row.createCell(11).setCellValue(s.getCreatedAt() == null ? "" : s.getCreatedAt().format(DT));
            }
            for (int i = 0; i < head.length; i++) sh.autoSizeColumn(i);
            wb.write(resp.getOutputStream());
        }
    }

    @Operation(summary = "导出题解 CSV")
    @RequirePermission("writeup:review")
    @GetMapping("/writeups.csv")
    public void writeupsCsv(HttpServletResponse resp) throws Exception {
        List<Writeup> rows = writeupMapper.selectList(new LambdaQueryWrapper<Writeup>()
                .orderByDesc(Writeup::getCreatedAt).last("LIMIT 5000"));
        prep(resp, "writeups", "csv", "text/csv;charset=UTF-8");
        PrintWriter w = resp.getWriter();
        w.write('\uFEFF');
        w.println("ID,题目ID,赛事ID,作者ID,标题,状态,浏览量,点赞数,创建时间");
        for (Writeup x : rows) {
            w.println(csv(x.getId(), x.getQuestionId(), x.getContestId(), x.getUserId(),
                    x.getTitle(), x.getStatus(), x.getViewCount(), x.getLikeCount(),
                    x.getCreatedAt() == null ? "" : x.getCreatedAt().format(DT)));
        }
        w.flush();
    }

    @Operation(summary = "导出排行榜 CSV")
    @GetMapping("/scoreboard.csv")
    public void scoreboardCsv(@RequestParam Long contestId,
                              @RequestParam(defaultValue = "total") String board,
                              @RequestParam(defaultValue = "0") int stage,
                              @RequestParam(defaultValue = "200") int topN,
                              HttpServletResponse resp) throws Exception {
        requireExportable(contestId);
        TableData t = leaderboard(contestId, board, stage, topN);
        prep(resp, t.fileName, "csv", "text/csv;charset=UTF-8");
        PrintWriter w = resp.getWriter();
        w.write('\uFEFF');  // BOM，Excel 打开中文不乱码
        w.println(csv(t.headers.toArray()));
        for (List<Object> row : t.rows) w.println(csv(row.toArray()));
        w.flush();
    }

    @Operation(summary = "导出排行榜 XLSX")
    @GetMapping("/scoreboard.xlsx")
    public void scoreboardXlsx(@RequestParam Long contestId,
                               @RequestParam(defaultValue = "total") String board,
                               @RequestParam(defaultValue = "0") int stage,
                               @RequestParam(defaultValue = "200") int topN,
                               HttpServletResponse resp) throws Exception {
        requireExportable(contestId);
        TableData t = leaderboard(contestId, board, stage, topN);
        prep(resp, t.fileName, "xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sh = wb.createSheet(t.sheet);
            Row h = sh.createRow(0);
            for (int i = 0; i < t.headers.size(); i++) h.createCell(i).setCellValue(t.headers.get(i));
            int r = 1;
            for (List<Object> row : t.rows) {
                Row line = sh.createRow(r++);
                for (int i = 0; i < row.size(); i++) {
                    Object v = row.get(i);
                    if (v instanceof Number num) line.createCell(i).setCellValue(num.doubleValue());
                    else line.createCell(i).setCellValue(v == null ? "" : String.valueOf(v));
                }
            }
            for (int i = 0; i < t.headers.size(); i++) sh.autoSizeColumn(i);
            wb.write(resp.getOutputStream());
        }
    }

    /**
     * 组装导出表格。
     * <p>total=总分榜，stage=阶段榜（需 stage 序号），firstblood=首杀榜；
     * 总榜与阶段榜带每题解题列（一血/二血/三血特殊标识）。</p>
     */
    private TableData leaderboard(Long contestId, String board, int stage, int topN) {
        Contest contest = contestMapper.selectById(contestId);
        String kind = board == null ? "total" : board.trim().toLowerCase();
        TableData t = new TableData();
        t.fileName = "leaderboard-" + (contest == null ? contestId : contest.getCode())
                + "-" + kind + (stage > 0 ? "-stage" + stage : "");
        t.sheet = switch (kind) {
            case "firstblood", "first", "fb" -> "首杀榜";
            case "stage" -> "阶段榜";
            default -> "总分榜";
        };

        if ("firstblood".equals(kind) || "first".equals(kind) || "fb".equals(kind)) {
            t.headers = List.of("序号", "题目", "分类", "难度", "分值", "战队", "解题者", "首杀时间", "解出队伍数");
            int i = 1;
            for (FirstBloodItem f : scoreboardService.firstBloodBoard(contestId)) {
                t.rows.add(Arrays.asList(i++, nvl(f.getTitle()), nvl(f.getCategory()), nvl(f.getDifficulty()),
                        f.getScore(), nvl(f.getTeamName()), nvl(f.getUserNickname()),
                        f.getSolvedAt() == null ? "" : f.getSolvedAt().format(DT),
                        f.getSolveTeamCount() == null ? 0 : f.getSolveTeamCount()));
            }
            return t;
        }

        LocalDateTime asOf = null;
        if ("stage".equals(kind) && stage > 0) {
            List<StageVO> stages = scoreboardService.stages(contestId, scoreboardService.defaultSlices(contestId));
            asOf = stages.stream().filter(s -> s.getIndex() == stage).findFirst()
                    .map(StageVO::getEndAt).orElse(null);
        }
        BoardDetailVO detail = scoreboardService.detail(contestId, topN, asOf);

        Map<Long, Map<Long, SolveCell>> byTeam = new HashMap<>();
        for (SolveCell c : detail.getCells()) {
            byTeam.computeIfAbsent(c.getTeamId(), k -> new HashMap<>()).put(c.getQuestionId(), c);
        }
        t.headers = new ArrayList<>(List.of("排名", "战队", "院校", "得分", "解题数", "一血数", "最近解题"));
        for (ChallengeMeta q : detail.getChallenges()) {
            t.headers.add(q.getTitle() + "(" + (q.getSolvedCount() == null ? 0 : q.getSolvedCount()) + "解)");
        }
        for (RankItem r : detail.getRanks()) {
            List<Object> row = new ArrayList<>();
            row.add(r.getRank());
            row.add(nvl(r.getTeamName()));
            row.add(nvl(r.getOrgName()));
            row.add(r.getScore());
            row.add(r.getSolved());
            row.add(r.getFirstBloodCount() == null ? 0 : r.getFirstBloodCount());
            row.add(r.getLastSolveAt() == null ? "" : r.getLastSolveAt().format(DT));
            Map<Long, SolveCell> cells = byTeam.getOrDefault(r.getTeamId(), Map.of());
            for (ChallengeMeta q : detail.getChallenges()) {
                SolveCell c = cells.get(q.getQuestionId());
                row.add(c == null ? "" : bloodLabel(c.getBloodRank()) + " " + (c.getScore() == null ? 0 : c.getScore()));
            }
            t.rows.add(row);
        }
        return t;
    }

    /** 赛事结束后全员可导出；进行中仅管理员/裁判。 */
    private void requireExportable(Long contestId) {
        Contest contest = contestMapper.selectById(contestId);
        if (contest == null) throw BizException.notFound("赛事");
        String status = contest.getStatus();
        boolean ended = "FINISHED".equals(status) || "ARCHIVED".equals(status);
        LoginUser u = UserContext.get();
        boolean control = u != null && u.hasPermission("contest:control");
        if (!ended && !control) {
            throw new BizException(ResultCode.FORBIDDEN, "赛事结束后开放榜单导出，当前状态：" + status);
        }
    }

    private static String bloodLabel(Integer rank) {
        int r = rank == null ? 0 : rank;
        return switch (r) {
            case 1 -> "一血";
            case 2 -> "二血";
            case 3 -> "三血";
            default -> "解出";
        };
    }

    /** 导出用的扁平表格。 */
    private static final class TableData {
        private String fileName;
        private String sheet;
        private List<String> headers = new ArrayList<>();
        private final List<List<Object>> rows = new ArrayList<>();
    }

    private static void prep(HttpServletResponse resp, String name, String ext, String ct) {
        String fn = name + "-" + System.currentTimeMillis() + "." + ext;
        resp.setContentType(ct);
        resp.setCharacterEncoding("UTF-8");
        resp.setHeader("Content-Disposition", "attachment; filename=\"" + fn + "\"; filename*=UTF-8''"
                + URLEncoder.encode(fn, StandardCharsets.UTF_8));
    }

    private static String nvl(String s) { return s == null ? "" : s; }

    private static String csv(Object... vals) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < vals.length; i++) {
            if (i > 0) sb.append(',');
            String v = vals[i] == null ? "" : String.valueOf(vals[i]);
            if (v.contains(",") || v.contains("\"") || v.contains("\n")) {
                v = "\"" + v.replace("\"", "\"\"") + "\"";
            }
            sb.append(v);
        }
        return sb.toString();
    }
}
