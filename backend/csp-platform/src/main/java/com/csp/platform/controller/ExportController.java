package com.csp.platform.controller;

import com.csp.ctf.entity.Submission;
import com.csp.ctf.mapper.SubmissionMapper;
import com.csp.platform.entity.Writeup;
import com.csp.platform.mapper.WriteupMapper;
import com.csp.security.annotation.RequirePermission;
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
import java.time.format.DateTimeFormatter;
import java.util.List;

/** 数据导出：成绩/题解 CSV 与 XLSX。 */
@Tag(name = "数据导出")
@RestController
@RequestMapping("/api/export")
@RequiredArgsConstructor
public class ExportController {

    private final SubmissionMapper submissionMapper;
    private final WriteupMapper writeupMapper;
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
