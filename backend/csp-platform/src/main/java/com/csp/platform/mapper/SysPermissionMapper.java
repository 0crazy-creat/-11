package com.csp.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.csp.platform.entity.SysPermission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SysPermissionMapper extends BaseMapper<SysPermission> {

    /** 按用户聚合其角色下的全部权限点编码。 */
    @Select("""
            SELECT DISTINCT p.code
              FROM sys_user_role ur
              JOIN sys_role r            ON r.id = ur.role_id AND r.deleted = 0
              JOIN sys_role_permission rp ON rp.role_id = r.id
              JOIN sys_permission p      ON p.id = rp.permission_id
             WHERE ur.user_id = #{userId}
            """)
    List<String> selectCodesByUserId(@Param("userId") Long userId);
}