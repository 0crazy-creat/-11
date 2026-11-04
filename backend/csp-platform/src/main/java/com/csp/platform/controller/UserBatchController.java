package com.csp.platform.controller;

import com.csp.common.api.Result;
import com.csp.common.api.ResultCode;
import com.csp.common.exception.BizException;
import com.csp.platform.entity.SysUser;
import com.csp.platform.mapper.SysUserMapper;
import com.csp.platform.service.AuthService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.csp.security.annotation.RequirePermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * 账号批量导入导出（需求 41 段）。
 * CSV 列：username,nickname,email,phone,password,orgName
 */
@Slf4j
@Tag(name = "账号批量导入导出")
@RestController
@RequestMapping("/api/users/batch")
@RequiredArgsConstructor
public class UserBatchController {

    private final SysUserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Operation(summary = "批量导入账号（CSV）")
    @RequirePermission("user:manage")
    @PostMapping("/import")
    public Result<Map<String, Object>> importCsv(@RequestParam("file") MultipartFile file) throws Exception {
        if (file == null || file.isEmpty()) {
            throw new BizException(ResultCode.BAD_REQUEST, "文件不能为空");
        }
        int ok = 0, skip = 0;
        List<String> errors = new ArrayList<>();
        try (BufferedReader r = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            boolean head = true;
            int row = 0;
            while ((line = r.readLine()) != null) {
                row++;
                if (head) { head = false; continue; }   // 跳过表头
                if (line.isBlank()) continue;
                String[] c = line.split(",", -1);
                if (c.length < 5) { errors.add("第" + row + "行列数不足"); skip++; continue; }
                String username = c[0].trim();
                if (username.isEmpty()) { errors.add("第" + row + "行用户名为空"); skip++; continue; }
                SysUser exist = userMapper.selectOne(Wrappers.<SysUser>lambdaQuery()
                        .eq(SysUser::getUsername, username));
                if (exist != null) { skip++; errors.add("第" + row + "行用户名已存在：" + username); continue; }
                try {
                    AuthService.validatePasswordStrength(c[4].trim());
                } catch (Exception e) {
                    skip++; errors.add("第" + row + "行密码强度不足"); continue;
                }
                SysUser u = new SysUser();
                u.setUsername(username);
                u.setNickname(c[1].trim().isEmpty() ? username : c[1].trim());
                u.setEmail(c[2].trim());
                u.setPhone(c[3].trim());
                u.setPassword(passwordEncoder.encode(c[4].trim()));
                u.setOrgName(c.length > 5 ? c[5].trim() : null);
                u.setStatus(1);
                userMapper.insert(u);
                ok++;
            }
        }
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("imported", ok);
        m.put("skipped", skip);
        m.put("errors", errors.subList(0, Math.min(errors.size(), 10)));
        log.info("[BATCH-IMPORT] 导入完成 ok={} skip={}", ok, skip);
        return Result.ok(m);
    }

    @Operation(summary = "批量导出账号（CSV）")
    @RequirePermission("user:manage")
    @GetMapping("/export")
    public void exportCsv(HttpServletResponse resp) throws Exception {
        List<SysUser> users = userMapper.selectList(Wrappers.<SysUser>lambdaQuery()
                .eq(SysUser::getDeleted, 0).last("LIMIT 5000"));
        String fn = "users-" + System.currentTimeMillis() + ".csv";
        resp.setContentType("text/csv;charset=UTF-8");
        resp.setHeader("Content-Disposition", "attachment; filename=\"" + fn + "\"; filename*=UTF-8''"
                + URLEncoder.encode(fn, StandardCharsets.UTF_8));
        PrintWriter w = resp.getWriter();
        w.write('\uFEFF');
        w.println("username,nickname,email,phone,orgName,status,lastLoginAt");
        for (SysUser u : users) {
            w.println(String.join(",",
                    nvl(u.getUsername()), nvl(u.getNickname()), nvl(u.getEmail()),
                    nvl(u.getPhone()), nvl(u.getOrgName()),
                    u.getStatus() == null ? "" : String.valueOf(u.getStatus()),
                    u.getLastLoginAt() == null ? "" : u.getLastLoginAt().toString()));
        }
        w.flush();
    }

    private static String nvl(String s) { return s == null ? "" : s.replace(",", " "); }
}
