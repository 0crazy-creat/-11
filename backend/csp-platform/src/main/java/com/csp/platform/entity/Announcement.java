package com.csp.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.csp.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 平台公告。scope=GLOBAL 全平台；scope=CONTEST 绑定指定赛事。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("announcement")
public class Announcement extends BaseEntity {

    private String title;
    private String content;
    /** GLOBAL / CONTEST */
    private String scope;
    private Long contestId;
    /** PUBLISHED / DRAFT */
    private String status;
}