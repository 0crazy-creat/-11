package com.csp.platform.format;

import com.csp.common.api.ResultCode;
import com.csp.common.exception.BizException;
import com.csp.platform.entity.ContestInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 赛制处理器路由：按 {@code contest.type} 选择唯一 {@link ContestFormatHandler}。
 *
 * <p>实现类由 Spring 注入，路由表在构造时一次性建立（启动即校验六赛制全覆盖），
 * 运行时 O(1) 查表，杜绝 ContestService 里散落的 type 判断。</p>
 */
@Slf4j
@Component
public class ContestFormatRouter {

    private final Map<String, ContestFormatHandler> table;

    public ContestFormatRouter(List<ContestFormatHandler> handlers) {
        this.table = handlers.stream().collect(Collectors.toUnmodifiableMap(
                h -> h.type().toUpperCase(Locale.ROOT), Function.identity()));
        log.info("ContestFormatRouter 已装载赛制处理器: {}", table.keySet());
    }

    /** 已注册的赛制类型码（大写）。 */
    public java.util.Set<String> types() {
        return table.keySet();
    }

    /** 按类型取处理器；未知赛制直接拒绝（避免静默走错分支）。 */
    public ContestFormatHandler of(String type) {
        ContestFormatHandler h = type == null ? null : table.get(type.trim().toUpperCase(Locale.ROOT));
        if (h == null) {
            throw new BizException(ResultCode.BAD_REQUEST,
                    "未支持的赛制类型：" + type + "（已注册：" + table.keySet() + "）");
        }
        return h;
    }

    public ContestFormatHandler of(ContestInfo c) {
        if (c == null) throw new BizException(ResultCode.BAD_REQUEST, "赛事不存在");
        return of(c.getType());
    }
}
