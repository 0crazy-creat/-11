package com.csp.ctf.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 参赛战队（contest_team）。
 *
 * <p>不继承 BaseEntity：该实体被 csp-platform 与 csp-ctf 两个模块共同引用，
 * 自包含字段可避免跨模块继承导致的主键元数据解析歧义。</p>
 */
@Data
@TableName("contest_team")
public class ContestTeam implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    private Long contestId;
    /** 关联的全局战队；个人参赛为 null */
    private Long teamId;
    private String name;
    private String code;
    private String logo;
    private String orgCode;
    private Long captainId;
    /**
     * 队长昵称（非持久化字段，仅管理端战队列表批量回填后返回给前端展示）。
     */
    @TableField(exist = false)
    private String captainName;
    /** 成员数 */
    private Integer members;
    /** PENDING/APPROVED/REJECTED/QUIT */
    private String status;
    private Long score;
    /**
     * 排名（冗余字段，排行榜刷新时写入）。
     *
     * <p>rank 是 MySQL 8.0 保留字，必须显式加反引号，否则生成的 SQL 会在该列处解析失败。</p>
     */
    @TableField("`rank`")
    private Integer rank;
    
    /** 邀请码（唯一） */
    private String inviteCode;
    /** 邀请码过期时间 */
    private LocalDateTime inviteExpireAt;
    /** 战队人数上限 */
    private Integer maxMembers;
    /** 战队简介 */
    private String description;
    /** 战队口号 */
    private String slogan;

    @TableField(fill = FieldFill.INSERT)
    private Long createdBy;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updatedBy;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    /** 逻辑删除：0 正常 1 已删除 */
    private Integer deleted;
}
