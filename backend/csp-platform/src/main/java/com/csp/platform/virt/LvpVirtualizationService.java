package com.csp.platform.virt;

import com.csp.common.api.ResultCode;
import com.csp.common.exception.BizException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * LVP 轻量虚拟化（Lightweight Virtualization Platform）：
 * 基于容器/命名空间的轻量隔离，提供"虚拟机"生命周期管理（创建/启动/停止/快照/销毁）。
 * 本地验证用内存实现；生产可对接 docker-java 或 libvirt。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LvpVirtualizationService {

    public enum State { CREATED, RUNNING, STOPPED, DESTROYED }

    public static class VmInstance {
        public Long id;
        public String name;
        public String image;
        public int vcpu;
        public int memoryMb;
        public State state = State.CREATED;
        public String ip;
        public long createdAt;
        public Map<String, String> snapshots = new LinkedHashMap<>();
    }

    private final Map<Long, VmInstance> store = new ConcurrentHashMap<>();
    private final AtomicLong seq = new AtomicLong(7000000000000000000L);

    /** 创建虚拟机（分配 IP、记录规格）。 */
    public VmInstance create(String name, String image, int vcpu, int memoryMb) {
        if (name == null || name.isBlank()) throw new BizException(ResultCode.BAD_REQUEST, "虚拟机名称不能为空");
        if (vcpu <= 0 || vcpu > 8) throw new BizException(ResultCode.BAD_REQUEST, "vcpu 需在 1~8 之间");
        if (memoryMb < 128 || memoryMb > 4096) throw new BizException(ResultCode.BAD_REQUEST, "内存需在 128~4096MB 之间");
        VmInstance vm = new VmInstance();
        vm.id = seq.incrementAndGet();
        vm.name = name;
        vm.image = image == null || image.isBlank() ? "csp/lvp-base:1.0" : image;
        vm.vcpu = vcpu;
        vm.memoryMb = memoryMb;
        vm.ip = "10.66." + (vm.id % 250) + ".2";
        vm.createdAt = System.currentTimeMillis();
        store.put(vm.id, vm);
        log.info("[LVP] 虚拟机已创建 id={} name={} {}", vm.id, vm.name, vm.ip);
        return vm;
    }

    public VmInstance get(Long id) {
        VmInstance vm = store.get(id);
        if (vm == null) throw new BizException(ResultCode.NOT_FOUND, "虚拟机不存在");
        return vm;
    }

    /** 启动：CREATED/STOPPED → RUNNING。 */
    public VmInstance start(Long id) {
        VmInstance vm = get(id);
        if (vm.state == State.RUNNING) return vm;
        if (vm.state == State.DESTROYED) throw new BizException(ResultCode.CONFLICT, "虚拟机已销毁");
        vm.state = State.RUNNING;
        log.info("[LVP] 虚拟机已启动 id={} ip={}", id, vm.ip);
        return vm;
    }

    public VmInstance stop(Long id) {
        VmInstance vm = get(id);
        if (vm.state == State.DESTROYED) throw new BizException(ResultCode.CONFLICT, "虚拟机已销毁");
        vm.state = State.STOPPED;
        return vm;
    }

    /** 快照。 */
    public Map<String, String> snapshot(Long id, String tag) {
        VmInstance vm = get(id);
        if (vm.state != State.RUNNING) throw new BizException(ResultCode.CONFLICT, "仅运行中的虚拟机可快照");
        String key = tag == null || tag.isBlank() ? "snap-" + (vm.snapshots.size() + 1) : tag;
        vm.snapshots.put(key, "state=" + vm.state + ";mem=" + vm.memoryMb + ";at=" + System.currentTimeMillis());
        log.info("[LVP] 快照已创建 id={} tag={}", id, key);
        return vm.snapshots;
    }

    public void destroy(Long id) {
        VmInstance vm = get(id);
        vm.state = State.DESTROYED;
        store.remove(id);
        log.info("[LVP] 虚拟机已销毁 id={}", id);
    }

    public List<VmInstance> list() {
        return new ArrayList<>(store.values());
    }

    /** 资源汇总（供 SLA/容量看板）。 */
    public Map<String, Object> summary() {
        int running = 0, totalVcpu = 0, totalMem = 0;
        for (VmInstance vm : store.values()) {
            if (vm.state == State.RUNNING) running++;
            totalVcpu += vm.vcpu;
            totalMem += vm.memoryMb;
        }
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("total", store.size());
        m.put("running", running);
        m.put("totalVcpu", totalVcpu);
        m.put("totalMemoryMb", totalMem);
        return m;
    }
}
