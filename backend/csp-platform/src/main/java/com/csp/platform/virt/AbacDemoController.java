package com.csp.platform.virt;

import com.csp.common.api.Result;
import com.csp.security.annotation.RequireAttribute;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/** ABAC 演示接口：验证属性级访问控制。 */
@Tag(name = "ABAC 属性访问控制")
@RestController
@RequestMapping("/api/abac")
@RequiredArgsConstructor
public class AbacDemoController {

    /** 要求用户具有 orgCode 属性（管理员豁免）。 */
    @Operation(summary = "需具备院校属性")
    @RequireAttribute(name = "orgCode")
    @GetMapping("/need-org")
    public Result<Map<String, Object>> needOrg() {
        return Result.ok(Map.of("ok", true, "rule", "orgCode 非空"));
    }

    /** 要求 orgCode 精确等于传入值。 */
    @Operation(summary = "院校属性须匹配")
    @RequireAttribute(name = "orgCode", value = "${orgCode}")
    @GetMapping("/match-org")
    public Result<Map<String, Object>> matchOrg(@RequestParam String orgCode) {
        return Result.ok(Map.of("ok", true, "orgCode", orgCode));
    }
}
