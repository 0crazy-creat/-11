package com.csp.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.csp.platform.entity.SysUserRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SysUserRoleMapper extends BaseMapper<SysUserRole> {

    /** 用户拥有的角色码。 */
    @Select("""
            SELECT r.code
              FROM sys_user_role ur
              JOIN sys_role r ON r.id = ur.role_id AND r.deleted = 0
             WHERE ur.user_id = #{userId}
            """)
    List<String> selectRoleCodesByUserId(@Param("userId") Long userId);
}