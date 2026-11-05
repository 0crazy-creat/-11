package com.csp.platform.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.csp.common.exception.BizException;
import com.csp.platform.dto.RoleSaveCmd;
import com.csp.platform.dto.RoleVO;
import com.csp.platform.entity.SysPermission;
import com.csp.platform.entity.SysRole;
import com.csp.platform.entity.SysRolePermission;
import com.csp.platform.entity.SysUserRole;
import com.csp.platform.mapper.SysPermissionMapper;
import com.csp.platform.mapper.SysRoleMapper;
import com.csp.platform.mapper.SysRolePermissionMapper;
import com.csp.platform.mapper.SysUserRoleMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/** 角色与权限管理。 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RoleService {

    private final SysRoleMapper roleMapper;
    private final SysPermissionMapper permissionMapper;
    private final SysRolePermissionMapper rolePermissionMapper;
    private final SysUserRoleMapper userRoleMapper;

    // ---------------- 角色 ----------------

    public List<RoleVO> list() {
        List<SysRole> roles = roleMapper.selectList(Wrappers.<SysRole>lambdaQuery()
                .eq(SysRole::getDeleted, 0)
                .orderByAsc(SysRole::getId));
        if (roles.isEmpty()) return List.of();

        // 一次性取全量关联，避免 N+1
        Map<Long, List<String>> rolePerms = permissionCodesGrouped();
        return roles.stream()
                .map(r -> toVO(r, rolePerms.getOrDefault(r.getId(), List.of())))
                .collect(Collectors.toList());
    }

    public RoleVO detail(Long id) {
        SysRole r = mustGet(id);
        return toVO(r, permissionCodesOfRole(id));
    }

    @Transactional
    public Long create(RoleSaveCmd cmd) {
        String code = cmd.getCode().trim();
        Long exists = roleMapper.selectCount(Wrappers.<SysRole>lambdaQuery()
                .eq(SysRole::getCode, code).eq(SysRole::getDeleted, 0));
        if (exists != null && exists > 0) throw BizException.of("角色码已存在：" + code);

        SysRole r = new SysRole();
        r.setCode(code);
        r.setName(cmd.getName().trim());
        r.setDescription(cmd.getDescription());
        r.setBuiltin(0);
        r.setDeleted(0);
        roleMapper.insert(r);

        replacePermissions(r.getId(), cmd.getPermissions());
        log.info("创建角色 id={} code={}", r.getId(), code);
        return r.getId();
    }

    @Transactional
    public void update(Long id, RoleSaveCmd cmd) {
        SysRole old = mustGet(id);

        String code = cmd.getCode() == null ? null : cmd.getCode().trim();
        if (StringUtils.hasText(code) && !code.equals(old.getCode())) {
            if (Integer.valueOf(1).equals(old.getBuiltin())) {
                throw BizException.of("内置角色不允许修改角色码");
            }
            Long exists = roleMapper.selectCount(Wrappers.<SysRole>lambdaQuery()
                    .eq(SysRole::getCode, code)
                    .eq(SysRole::getDeleted, 0)
                    .ne(SysRole::getId, id));
            if (exists != null && exists > 0) throw BizException.of("角色码已存在：" + code);
        }

        SysRole r = new SysRole();
        r.setId(id);
        if (StringUtils.hasText(code)) r.setCode(code);
        r.setName(cmd.getName() == null ? null : cmd.getName().trim());
        r.setDescription(cmd.getDescription());
        roleMapper.updateById(r);

        if (cmd.getPermissions() != null) replacePermissions(id, cmd.getPermissions());
        log.info("更新角色 id={}", id);
    }

    @Transactional
    public void delete(Long id) {
        SysRole r = mustGet(id);
        if (Integer.valueOf(1).equals(r.getBuiltin())) throw BizException.of("内置角色不可删除");

        Long bound = userRoleMapper.selectCount(Wrappers.<SysUserRole>lambdaQuery()
                .eq(SysUserRole::getRoleId, id));
        if (bound != null && bound > 0) throw BizException.of("该角色下仍有用户，无法删除");

        // MyBatis-Plus 的 updateById 会忽略逻辑删除字段，只能用 deleteById 触发逻辑删除
        roleMapper.deleteById(id);
        rolePermissionMapper.delete(Wrappers.<SysRolePermission>lambdaQuery()
                .eq(SysRolePermission::getRoleId, id));
        log.info("删除角色 id={}", id);
    }

    /** 全量覆盖角色权限点。 */
    @Transactional
    public void replacePermissions(Long roleId, Collection<String> permCodes) {
        mustGet(roleId);
        rolePermissionMapper.delete(Wrappers.<SysRolePermission>lambdaQuery()
                .eq(SysRolePermission::getRoleId, roleId));
        if (permCodes == null || permCodes.isEmpty()) return;

        List<String> codes = permCodes.stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .distinct()
                .collect(Collectors.toList());
        if (codes.isEmpty()) return;

        List<SysPermission> perms = permissionMapper.selectList(Wrappers.<SysPermission>lambdaQuery()
                .in(SysPermission::getCode, codes));
        if (perms.size() != codes.size()) {
            Set<String> found = perms.stream().map(SysPermission::getCode).collect(Collectors.toSet());
            List<String> missing = codes.stream().filter(c -> !found.contains(c)).toList();
            throw BizException.of("不存在的权限点：" + String.join(",", missing));
        }
        for (SysPermission p : perms) {
            rolePermissionMapper.insert(new SysRolePermission(roleId, p.getId()));
        }
    }

    // ---------------- 权限点 ----------------

    /** 全部权限点，按 module 分组，供前端授权树渲染。 */
    public Map<String, List<SysPermission>> permissionTree() {
        List<SysPermission> all = permissionMapper.selectList(Wrappers.<SysPermission>lambdaQuery()
                .orderByAsc(SysPermission::getModule)
                .orderByAsc(SysPermission::getCode));
        Map<String, List<SysPermission>> grouped = new LinkedHashMap<>();
        for (SysPermission p : all) {
            String key = StringUtils.hasText(p.getModule()) ? p.getModule() : "其他";
            grouped.computeIfAbsent(key, k -> new ArrayList<>()).add(p);
        }
        return grouped;
    }

    // ---------------- 内部 ----------------

    private SysRole mustGet(Long id) {
        SysRole r = roleMapper.selectById(id);
        if (r == null || Integer.valueOf(1).equals(r.getDeleted())) throw BizException.notFound("角色");
        return r;
    }

    private List<String> permissionCodesOfRole(Long roleId) {
        List<SysRolePermission> links = rolePermissionMapper.selectList(
                Wrappers.<SysRolePermission>lambdaQuery().eq(SysRolePermission::getRoleId, roleId));
        if (links.isEmpty()) return List.of();
        List<Long> permIds = links.stream().map(SysRolePermission::getPermissionId).toList();
        return permissionMapper.selectBatchIds(permIds).stream()
                .map(SysPermission::getCode)
                .sorted()
                .collect(Collectors.toList());
    }

    /** roleId -> 权限点编码列表。 */
    private Map<Long, List<String>> permissionCodesGrouped() {
        List<SysRolePermission> links = rolePermissionMapper.selectList(null);
        if (links.isEmpty()) return Map.of();

        Set<Long> permIds = links.stream().map(SysRolePermission::getPermissionId).collect(Collectors.toSet());
        Map<Long, String> idToCode = permissionMapper.selectBatchIds(permIds).stream()
                .collect(Collectors.toMap(SysPermission::getId, SysPermission::getCode));

        Map<Long, List<String>> result = new LinkedHashMap<>();
        for (SysRolePermission l : links) {
            String code = idToCode.get(l.getPermissionId());
            if (code == null) continue;
            result.computeIfAbsent(l.getRoleId(), k -> new ArrayList<>()).add(code);
        }
        result.values().forEach(codes -> codes.sort(java.util.Comparator.naturalOrder()));
        return result;
    }

    private RoleVO toVO(SysRole r, List<String> perms) {
        return RoleVO.builder()
                .id(r.getId())
                .code(r.getCode())
                .name(r.getName())
                .description(r.getDescription())
                .builtin(r.getBuiltin())
                .createdAt(r.getCreatedAt())
                .permissions(perms)
                .build();
    }
}