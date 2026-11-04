package com.csp.arena.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.csp.arena.config.BizException;
import com.csp.arena.dto.TemplateSaveCmd;
import com.csp.arena.entity.ArenaTemplate;
import com.csp.arena.mapper.ArenaTemplateMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/** 靶场模板服务。 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TemplateService {

    private final ArenaTemplateMapper templateMapper;

    public Page<ArenaTemplate> page(int page, int size, String category, String keyword, Integer status) {
        LambdaQueryWrapper<ArenaTemplate> q = new LambdaQueryWrapper<>();
        if (category != null && !category.isBlank()) q.eq(ArenaTemplate::getCategory, category);
        if (status != null) q.eq(ArenaTemplate::getStatus, status);
        if (keyword != null && !keyword.isBlank()) {
            q.and(w -> w.like(ArenaTemplate::getName, keyword).or().like(ArenaTemplate::getCode, keyword));
        }
        q.orderByDesc(ArenaTemplate::getCreatedAt);
        return templateMapper.selectPage(new Page<>(page, size), q);
    }

    public List<ArenaTemplate> listEnabled() {
        return templateMapper.selectList(new LambdaQueryWrapper<ArenaTemplate>()
                .eq(ArenaTemplate::getStatus, 1).orderByAsc(ArenaTemplate::getCategory));
    }

    public ArenaTemplate get(Long id) {
        ArenaTemplate t = templateMapper.selectById(id);
        if (t == null) throw BizException.notFound("靶场模板");
        return t;
    }

    public ArenaTemplate getByCode(String code) {
        ArenaTemplate t = templateMapper.selectOne(new LambdaQueryWrapper<ArenaTemplate>()
                .eq(ArenaTemplate::getCode, code).last("LIMIT 1"));
        if (t == null) throw BizException.notFound("靶场模板");
        return t;
    }

    public Long create(TemplateSaveCmd cmd, Long actorId) {
        Long dup = templateMapper.selectCount(new LambdaQueryWrapper<ArenaTemplate>()
                .eq(ArenaTemplate::getCode, cmd.getCode()));
        if (dup != null && dup > 0) throw new BizException(409, "模板编码已存在：" + cmd.getCode());

        ArenaTemplate t = new ArenaTemplate();
        fill(t, cmd);
        t.setCreatedBy(actorId);
        t.setUpdatedBy(actorId);
        templateMapper.insert(t);
        log.info("创建靶场模板 id={} code={}", t.getId(), t.getCode());
        return t.getId();
    }

    public void update(Long id, TemplateSaveCmd cmd, Long actorId) {
        ArenaTemplate t = get(id);
        fill(t, cmd);
        t.setUpdatedBy(actorId);
        templateMapper.updateById(t);
    }

    public void delete(Long id) {
        get(id);
        templateMapper.deleteById(id);
    }

    public void toggleStatus(Long id, Integer status) {
        ArenaTemplate t = get(id);
        t.setStatus(status);
        templateMapper.updateById(t);
    }

    private void fill(ArenaTemplate t, TemplateSaveCmd c) {
        t.setCode(c.getCode());
        t.setName(c.getName());
        t.setCategory(c.getCategory() == null ? "WEB" : c.getCategory());
        t.setImage(c.getImage());
        t.setContainerPort(c.getContainerPort() == null ? 80 : c.getContainerPort());
        t.setDifficulty(c.getDifficulty() == null ? "EASY" : c.getDifficulty());
        t.setDescription(c.getDescription());
        t.setDefaultFlag(c.getDefaultFlag());
        t.setCpuLimit(c.getCpuLimit() == null ? "1.0" : c.getCpuLimit());
        t.setMemoryLimit(c.getMemoryLimit() == null ? "512m" : c.getMemoryLimit());
        t.setTtlMinutes(c.getTtlMinutes() == null ? 120 : c.getTtlMinutes());
        t.setStatus(c.getStatus() == null ? 1 : c.getStatus());
    }
}
