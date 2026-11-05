package com.csp.platform.controller;

import com.csp.common.api.Result;
import com.csp.common.api.ResultCode;
import com.csp.common.exception.BizException;
import com.csp.security.annotation.PublicApi;
import com.csp.security.annotation.RequirePermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * 题目附件上传/下载。
 *
 * <p>附件直接上传到服务器本地目录（默认 /app/uploads，compose 用命名卷持久化），
 * 返回的相对 URL 写入 question.attachment，选手端下载走 {@link #download(String)}。</p>
 */
@Slf4j
@Tag(name = "题目附件")
@RestController
@RequestMapping("/api/files")
public class FileController {

    @Value("${csp.upload.dir:/app/uploads}")
    private String uploadDir;

    /** 与 spring.servlet.multipart.max-file-size 保持一致 */
    private static final long MAX_SIZE = 200L * 1024 * 1024;

    @Operation(summary = "上传题目附件")
    @RequirePermission("question:create")
    @PostMapping("/upload")
    public Result<FileVO> upload(@RequestParam("file") MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new BizException(ResultCode.BAD_REQUEST, "文件为空");
        }
        if (file.getSize() > MAX_SIZE) {
            throw new BizException(ResultCode.BAD_REQUEST, "文件超过 200MB");
        }

        String original = file.getOriginalFilename();
        if (original == null || original.isBlank()) {
            original = "attachment";
        }
        // 去掉路径分隔符与换行，避免目录穿越和响应头注入
        original = original.replaceAll("[\\\\/]", "_").replaceAll("[\\r\\n]", "").trim();
        if (original.length() > 120) {
            original = original.substring(original.length() - 120);
        }

        String stored = UUID.randomUUID().toString().replace("-", "") + "_" + original;
        Path dir = Paths.get(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(dir);
        Path target = dir.resolve(stored).normalize();
        if (!target.startsWith(dir)) {
            throw new BizException(ResultCode.BAD_REQUEST, "非法文件名");
        }
        file.transferTo(target.toFile());
        log.info("附件已上传: {} -> {} ({} bytes)", original, target, file.getSize());

        FileVO vo = new FileVO();
        vo.setName(original);
        vo.setSize(file.getSize());
        vo.setUrl("/api/files/" + UriUtils.encodePathSegment(stored, StandardCharsets.UTF_8));
        return Result.ok(vo);
    }

    @Operation(summary = "下载题目附件")
    @PublicApi
    @GetMapping("/{stored}")
    public ResponseEntity<Resource> download(@PathVariable String stored) throws IOException {
        if (stored == null || stored.isBlank()
                || stored.contains("..") || stored.contains("/") || stored.contains("\\")) {
            throw new BizException(ResultCode.BAD_REQUEST, "非法文件名");
        }
        Path dir = Paths.get(uploadDir).toAbsolutePath().normalize();
        Path target = dir.resolve(stored).normalize();
        if (!target.startsWith(dir) || !Files.exists(target) || !Files.isRegularFile(target)) {
            throw new BizException(ResultCode.NOT_FOUND, "文件不存在");
        }

        // 存储名格式：{32位uuid}_{原始文件名}
        String original = stored.indexOf('_') >= 0 ? stored.substring(stored.indexOf('_') + 1) : stored;
        Resource resource = new FileSystemResource(target);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment()
                                .filename(original, StandardCharsets.UTF_8)
                                .build().toString())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(Files.size(target))
                .body(resource);
    }

    @Data
    public static class FileVO {
        private String name;
        private long size;
        private String url;
    }
}
