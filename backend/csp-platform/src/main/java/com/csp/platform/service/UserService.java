package com.csp.platform.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.csp.common.api.PageResult;
import com.csp.common.exception.BizException;
import com.csp.platform.dto.UserProfileVO;
import com.csp.platform.dto.UserQuery;
import com.csp.platform.dto.UserSaveCmd;
import com.csp.platform.entity.SysRole;
import com.csp.platform.entity.SysUser;
import com.csp.platform.entity.SysUserRole;
import com.csp.platform.mapper.SysRoleMapper;
import com.csp.platform.mapper.SysUserMapper;
import com.csp.platform.mapper.SysUserRoleMapper;
import com.csp.security.core.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/** 用户管理（管理员侧）。 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    /** 新建用户的默认初始密码，首次登录后应强制修改 */
    private static final String DEFAULT_PASSWORD = "Csp@123456";

    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final PasswordEncoder passwordEncoder;

    // ---------------- 查询 ----------------

    public PageResult<UserProfileVO> page(UserQuery q) {
        LambdaQueryWrapper<SysUser> w = Wrappers.<SysUser>lambdaQuery()
                .eq(SysUser::getDeleted, 0);

        if (StringUtils.hasText(q.getKeyword())) {
            String kw = q.getKeyword().trim();
            w.and(x -> x.like(SysUser::getUsername, kw)
                    .or().like(SysUser::getNickname, kw)
                    .or().like(SysUser::getRealName, kw)
                    .or().like(SysUser::getStudentNo, kw)
                    .or().like(SysUser::getPhone, kw));
        }
        if (StringUtils.hasText(q.getOrgCode())) w.eq(SysUser::getOrgCode, q.getOrgCode());
        if (q.getStatus() != null) w.eq(SysUser::getStatus, q.getStatus());

        w.orderByDesc(SysUser::getCreatedAt);

        Page<SysUser> page = userMapper.selectPage(new Page<>(q.getPage(), q.getSize()), w);
        return PageResult.of(page, AuthService::toProfile);
    }

    public UserProfileVO detail(Long id) {
        SysUser u = mustGet(id);
        return AuthService.toProfile(u);
    }

    /** 用户当前角色码。 */
    public Set<String> rolesOf(Long userId) {
        List<String> codes = userRoleMapper.selectRoleCodesByUserId(userId);
        return codes == null ? Set.of() : Set.copyOf(codes);
    }

    // ---------------- 写操作 ----------------

    @Transactional
    public Long create(UserSaveCmd cmd) {
        String username = cmd.getUsername().trim();
        Long exists = userMapper.selectCount(Wrappers.<SysUser>lambdaQuery()
                .eq(SysUser::getUsername, username).eq(SysUser::getDeleted, 0));
        if (exists != null && exists > 0) throw BizException.of("用户名已存在");

        if (StringUtils.hasText(cmd.getPhone())) {
            Long phoneExists = userMapper.selectCount(Wrappers.<SysUser>lambdaQuery()
                    .eq(SysUser::getPhone, cmd.getPhone().trim()).eq(SysUser::getDeleted, 0));
            if (phoneExists != null && phoneExists > 0) throw BizException.of("手机号已被占用");
        }

        String rawPwd = StringUtils.hasText(cmd.getPassword()) ? cmd.getPassword() : DEFAULT_PASSWORD;
        AuthService.validatePasswordStrength(rawPwd);

        SysUser u = new SysUser();
        u.setUsername(username);
        u.setPassword(passwordEncoder.encode(rawPwd));
        u.setNickname(cmd.getNickname());
        u.setRealName(cmd.getRealName());
        u.setStudentNo(cmd.getStudentNo());
        u.setPhone(StringUtils.hasText(cmd.getPhone()) ? cmd.getPhone().trim() : null);
        u.setEmail(cmd.getEmail());
        u.setAvatar(cmd.getAvatar());
        u.setOrgCode(cmd.getOrgCode());
        u.setOrgName(cmd.getOrgName());
        u.setClassName(cmd.getClassName());
        u.setStatus(cmd.getStatus() == null ? 1 : cmd.getStatus());
        u.setMfaEnabled(0);
        u.setDeleted(0);
        u.setCreatedBy(UserContext.get() == null ? null : UserContext.get().getUserId());
        userMapper.insert(u);

        assignRoles(u.getId(), cmd.getRoles());
        log.info("创建用户 id={} username={}", u.getId(), username);
        return u.getId();
    }

    /**
     * 自助注册专用：创建用户并按角色码分配默认角色。
     *
     * <p>与管理员建号的区别：不读取请求体的 roles（防越权注册管理员），
     * 角色码由调用方传入固定值；注册来源的用户 createdBy 为空。</p>
     */
    @Transactional
    public Long createWithDefaultRole(UserSaveCmd cmd, String roleCode) {
        Long userId = create(cmd);
        if (StringUtils.hasText(roleCode)) {
            assignRoles(userId, java.util.List.of(roleCode));
        }
        return userId;
    }

    @Transactional
    public void update(Long id, UserSaveCmd cmd) {
        SysUser old = mustGet(id);

        String username = cmd.getUsername() == null ? null : cmd.getUsername().trim();
        if (StringUtils.hasText(username) && !username.equals(old.getUsername())) {
            Long exists = userMapper.selectCount(Wrappers.<SysUser>lambdaQuery()
                    .eq(SysUser::getUsername, username)
                    .eq(SysUser::getDeleted, 0)
                    .ne(SysUser::getId, id));
            if (exists != null && exists > 0) throw BizException.of("用户名已存在");
        }

        SysUser u = new SysUser();
        u.setId(id);
        if (StringUtils.hasText(username)) u.setUsername(username);
        u.setNickname(cmd.getNickname());
        u.setRealName(cmd.getRealName());
        u.setStudentNo(cmd.getStudentNo());
        u.setPhone(cmd.getPhone());
        u.setEmail(cmd.getEmail());
        u.setAvatar(cmd.getAvatar());
        u.setOrgCode(cmd.getOrgCode());
        u.setOrgName(cmd.getOrgName());
        u.setClassName(cmd.getClassName());
        u.setStatus(cmd.getStatus());
        u.setUpdatedBy(UserContext.get() == null ? null : UserContext.get().getUserId());
        userMapper.updateById(u);

        if (cmd.getRoles() != null) assignRoles(id, cmd.getRoles());
        log.info("更新用户 id={}", id);
    }

    /** 管理员重置他人密码。 */
    @Transactional
    public void resetPassword(Long id, String newPassword) {
        mustGet(id);
        String raw = StringUtils.hasText(newPassword) ? newPassword : DEFAULT_PASSWORD;
        AuthService.validatePasswordStrength(raw);

        SysUser u = new SysUser();
        u.setId(id);
        u.setPassword(passwordEncoder.encode(raw));
        u.setUpdatedBy(UserContext.get() == null ? null : UserContext.get().getUserId());
        userMapper.updateById(u);
        log.info("重置用户密码 id={}", id);
    }

    /** 启用/禁用/锁定。 */
    @Transactional
    public void changeStatus(Long id, Integer status) {
        if (status == null || status < 0 || status > 2) throw BizException.of("非法的状态值");
        mustGet(id);
        SysUser u = new SysUser();
        u.setId(id);
        u.setStatus(status);
        u.setUpdatedBy(UserContext.get() == null ? null : UserContext.get().getUserId());
        userMapper.updateById(u);
    }

    /** 逻辑删除。 */
    @Transactional
    public void delete(Long id) {
        mustGet(id);
        Long me = UserContext.get() == null ? null : UserContext.get().getUserId();
        if (id.equals(me)) throw BizException.of("不能删除当前登录账号");

        // MyBatis-Plus 的 updateById 会忽略逻辑删除字段，只能用 deleteById 触发逻辑删除
        userMapper.deleteById(id);
        userRoleMapper.delete(Wrappers.<SysUserRole>lambdaQuery().eq(SysUserRole::getUserId, id));
        log.info("删除用户 id={}", id);
    }

    // ---------------- 内部 ----------------

    private SysUser mustGet(Long id) {
        SysUser u = userMapper.selectById(id);
        if (u == null || Integer.valueOf(1).equals(u.getDeleted())) {
            throw BizException.notFound("用户");
        }
        return u;
    }

    /** 全量覆盖用户角色：先删后插。 */
    private void assignRoles(Long userId, Collection<String> roleCodes) {
        userRoleMapper.delete(Wrappers.<SysUserRole>lambdaQuery().eq(SysUserRole::getUserId, userId));
        if (roleCodes == null || roleCodes.isEmpty()) return;

        List<String> codes = roleCodes.stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .distinct()
                .collect(Collectors.toList());
        if (codes.isEmpty()) return;

        List<SysRole> roles = roleMapper.selectList(Wrappers.<SysRole>lambdaQuery()
                .in(SysRole::getCode, codes)
                .eq(SysRole::getDeleted, 0));
        if (roles.size() != codes.size()) {
            Set<String> found = roles.stream().map(SysRole::getCode).collect(Collectors.toSet());
            List<String> missing = codes.stream().filter(c -> !found.contains(c)).toList();
            throw BizException.of("不存在的角色码：" + String.join(",", missing));
        }
        for (SysRole r : roles) {
            userRoleMapper.insert(new SysUserRole(userId, r.getId()));
        }
    }
}