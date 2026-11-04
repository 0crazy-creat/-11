package com.csp.arena.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 靶场模板。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("arena_template")
public class ArenaTemplate extends BaseEntity {
    private String code;
    private String name;
    private String category;
    private String image;
    private Integer containerPort;
    private String difficulty;
    private String description;
    private String defaultFlag;
    private String cpuLimit;
    private String memoryLimit;
    private Integer ttlMinutes;
    private Integer status;
}
