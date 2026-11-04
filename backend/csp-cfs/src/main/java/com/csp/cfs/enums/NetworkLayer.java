package com.csp.cfs.enums;

import java.util.List;

/**
 * 网络分层（CFS 商业靶场核心维度）。
 *
 * <ul>
 *   <li>{@link #LAYER1} 外网：唯一对外暴露入口，初始即解锁；</li>
 *   <li>{@link #LAYER2} 内网：需拿下 LAYER1 任意 flag 才解锁；</li>
 *   <li>{@link #LAYER3} 深网：需拿下 LAYER2 任意 flag 才解锁，分值最高。</li>
 * </ul>
 *
 * 层内靶机只在本层 Docker network 中可达，跨层不可直连——模拟真实纵深。
 */
public enum NetworkLayer {
    LAYER1(1, "外网", 0),
    LAYER2(2, "内网", 1),
    LAYER3(3, "深网", 2);

    private final int no;
    private final String label;
    private final int prevNo;

    NetworkLayer(int no, String label, int prevNo) {
        this.no = no;
        this.label = label;
        this.prevNo = prevNo;
    }

    public int getNo() { return no; }
    public String getLabel() { return label; }
    /** 解锁本层所需的前置层号；0 表示无需前置。 */
    public int getPrevNo() { return prevNo; }

    /** 是否需要前置层解锁。 */
    public boolean requiresPrev() { return prevNo > 0; }

    /** 全部层级，按由外到内排序。 */
    public static List<NetworkLayer> ordered() { return List.of(LAYER1, LAYER2, LAYER3); }

    /** 按层号解析；非法层号抛 {@link IllegalArgumentException}。 */
    public static NetworkLayer ofNo(int no) {
        for (NetworkLayer l : values()) {
            if (l.no == no) return l;
        }
        throw new IllegalArgumentException("非法网络层号：" + no + "（允许 1-3）");
    }

    /** 按编码解析（LAYER1/LAYER2/LAYER3），大小写不敏感。 */
    public static NetworkLayer ofCode(String code) {
        if (code == null) throw new IllegalArgumentException("网络层编码不能为空");
        return valueOf(code.trim().toUpperCase());
    }
}