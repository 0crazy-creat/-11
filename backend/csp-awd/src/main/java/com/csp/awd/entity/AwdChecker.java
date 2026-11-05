package com.csp.awd.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.csp.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** AWD checker 配置：按题目定义如何检测靶机服务是否正常。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("awd_checker")
public class AwdChecker extends BaseEntity {

    private Long contestId;
    private Long questionId;
    private String name;
    /** HTTP / TCP */
    private String checkType;
    private String targetPath;
    private Integer expectStatus;
    private Integer timeoutMs;
    private Integer enabled;
}
