package com.csp.platform.virt;

import com.csp.common.api.Result;
import com.csp.security.annotation.RequirePermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/** LVP 轻量虚拟化管理接口。 */
@Tag(name = "LVP 轻量虚拟化")
@RestController
@RequestMapping("/api/lvp/vms")
@RequiredArgsConstructor
public class LvpController {

    private final LvpVirtualizationService lvp;

    public record CreateVmCmd(String name, String image, Integer vcpu, Integer memoryMb) {}

    @Operation(summary = "创建虚拟机")
    @RequirePermission("contest:control")
    @PostMapping
    public Result<LvpVirtualizationService.VmInstance> create(@RequestBody CreateVmCmd cmd) {
        int vcpu = cmd.vcpu() == null ? 1 : cmd.vcpu();
        int mem = cmd.memoryMb() == null ? 512 : cmd.memoryMb();
        return Result.ok(lvp.create(cmd.name(), cmd.image(), vcpu, mem));
    }

    @Operation(summary = "虚拟机列表")
    @GetMapping
    public Result<List<LvpVirtualizationService.VmInstance>> list() {
        return Result.ok(lvp.list());
    }

    @Operation(summary = "虚拟机详情")
    @GetMapping("/{id}")
    public Result<LvpVirtualizationService.VmInstance> detail(@PathVariable Long id) {
        return Result.ok(lvp.get(id));
    }

    @Operation(summary = "启动虚拟机")
    @RequirePermission("contest:control")
    @PostMapping("/{id}/start")
    public Result<LvpVirtualizationService.VmInstance> start(@PathVariable Long id) {
        return Result.ok(lvp.start(id));
    }

    @Operation(summary = "停止虚拟机")
    @RequirePermission("contest:control")
    @PostMapping("/{id}/stop")
    public Result<LvpVirtualizationService.VmInstance> stop(@PathVariable Long id) {
        return Result.ok(lvp.stop(id));
    }

    @Operation(summary = "创建快照")
    @RequirePermission("contest:control")
    @PostMapping("/{id}/snapshot")
    public Result<Map<String, String>> snapshot(@PathVariable Long id,
                                                @RequestParam(required = false) String tag) {
        return Result.ok(lvp.snapshot(id, tag));
    }

    @Operation(summary = "销毁虚拟机")
    @RequirePermission("contest:control")
    @DeleteMapping("/{id}")
    public Result<Void> destroy(@PathVariable Long id) {
        lvp.destroy(id);
        return Result.ok();
    }

    @Operation(summary = "资源汇总")
    @GetMapping("/summary")
    public Result<Map<String, Object>> summary() {
        return Result.ok(lvp.summary());
    }
}
