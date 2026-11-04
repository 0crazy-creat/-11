package com.csp.common.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HexFormat;

/** Flag 生成与比对工具：支持静态 flag 与动态（每队独立）flag。 */
public final class FlagUtils {
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String ALPHABET = "abcdefghijklmnopqrstuvwxyz0123456789";

    private FlagUtils() {}

    /** 生成指定长度的随机 flag 主体。 */
    public static String randomToken(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));
        }
        return sb.toString();
    }

    /**
     * 生成动态 flag：flag{prefix_teamId_round_random}
     */
    public static String dynamicFlag(String prefix, long teamId, int round) {
        return "flag{" + prefix + "_" + teamId + "_" + round + "_" + randomToken(16) + "}";
    }

    /** 常量时间比较，避免时序攻击。 */
    public static boolean safeEquals(String a, String b) {
        if (a == null || b == null) return false;
        return MessageDigest.isEqual(a.getBytes(StandardCharsets.UTF_8), b.getBytes(StandardCharsets.UTF_8));
    }

    /** 提交内容归一化：去除首尾空白。 */
    public static String normalize(String submitted) {
        return submitted == null ? "" : submitted.trim();
    }

    /** SHA-256 摘要（用于 flag 落库脱敏存储）。 */
    public static String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(md.digest(input.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 不可用", e);
        }
    }

    /** HMAC-SHA256 十六进制摘要（用于动态 flag / 每队种子派生）。 */
    public static String hmacSha256Hex(String key, String input) {
        try {
            javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA256");
            mac.init(new javax.crypto.spec.SecretKeySpec(
                    (key == null ? "" : key).getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return HexFormat.of().formatHex(mac.doFinal(input.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("HmacSHA256 不可用", e);
        }
    }
}
