package com.csp.cfs.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/** CFS 新建分层拓扑入参。 */
@Data
public class CfsTopologySaveCmd {

    /** 所属赛事（可空，空=通用模板） */
    private Long contestId;

    @NotBlank(message = "拓扑编码不能为空")
    private String code;

    @NotBlank(message = "拓扑名称不能为空")
    private String name;

    private String description;

    /** 层定义：至少 1 层，最多 3 层；越深层 baseScore 越高 */
    @NotEmpty(message = "至少需要一层定义")
    private List<LayerCmd> layers;

    /** 节点定义（可空，后续再补） */
    private List<NodeCmd> nodes;

    @Data
    public static class LayerCmd {
        /** 1/2/3 */
        @NotNull
        @Min(1)
        private Integer layerNo;
        private String name;
        /** 解锁本层所需的前置层号，0=无需前置；为空时按 layerNo-1 自动推导 */
        private Integer prevLayerNo;
        /** 本层基础分；为空时按层号自动递增 */
        private Integer baseScore;
        private Integer scoreStep;
        private String networkCidr;
        /** 1=暴露宿主端口 */
        private Integer exposePort;
    }

    @Data
    public static class NodeCmd {
        @NotNull
        private Integer layerNo;
        @NotBlank
        private String nodeCode;
        @NotBlank
        private String name;
        /** 关联题库题目（允许跨 bank_type） */
        private Long questionId;
        private String imageName;
        private Integer containerPort;
        private Integer isEntry;
        private Integer score;
        private String flag;
        private Integer dynamicFlag;
    }
}