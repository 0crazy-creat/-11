package com.csp.arena.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.csp.arena.config.Result;
import com.csp.arena.dto.TemplateSaveCmd;
import com.csp.arena.entity.ArenaTemplate;
import com.csp.arena.service.TemplateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/** 靶场模板管理。 */
@RestController
@RequestMapping("/api/templates")
@RequiredArgsConstructor
public class TemplateController {

    private final TemplateService templateService;

    @GetMapping
    public Result<Page<ArenaTemplate>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        return Result.ok(templateService.page(page, size, category, keyword, status));
    }

    /** 启用模板下拉列表。 */
    @GetMapping("/enabled")
    public Result<List<ArenaTemplate>> enabled() {
        return Result.ok(templateService.listEnabled());
    }

    @GetMapping("/{id}")
    public Result<ArenaTemplate> get(@PathVariable Long id) {
        return Result.ok(templateService.get(id));
    }

    @GetMapping("/code/{code}")
    public Result<ArenaTemplate> getByCode(@PathVariable String code) {
        return Result.ok(templateService.getByCode(code));
    }

    @PostMapping
    public Result<Map<String, Long>> create(@Valid @RequestBody TemplateSaveCmd cmd) {
        Long id = templateService.create(cmd, 1L);
        return Result.ok(Map.of("id", id));
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody TemplateSaveCmd cmd) {
        templateService.update(id, cmd, 1L);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        templateService.delete(id);
        return Result.ok();
    }

    @PutMapping("/{id}/status")
    public Result<Void> toggle(@PathVariable Long id, @RequestParam Integer status) {
        templateService.toggleStatus(id, status);
        return Result.ok();
    }
}
