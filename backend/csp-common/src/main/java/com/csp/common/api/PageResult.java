package com.csp.common.api;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/** 统一分页结构。 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private long page;
    private long size;
    private long total;
    private long pages;
    private List<T> records;

    public static <T> PageResult<T> of(IPage<T> p) {
        return new PageResult<>(p.getCurrent(), p.getSize(), p.getTotal(), p.getPages(), p.getRecords());
    }

    public static <E, T> PageResult<T> of(IPage<E> p, Function<E, T> mapper) {
        List<T> list = p.getRecords() == null ? Collections.emptyList()
                : p.getRecords().stream().map(mapper).toList();
        return new PageResult<>(p.getCurrent(), p.getSize(), p.getTotal(), p.getPages(), list);
    }

    public static <T> PageResult<T> empty(long page, long size) {
        return new PageResult<>(page, size, 0, 0, Collections.emptyList());
    }
}
