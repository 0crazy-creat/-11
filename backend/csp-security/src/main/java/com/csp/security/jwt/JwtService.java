package com.csp.security.jwt;

import com.csp.common.security.LoginUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/** JWT 签发与解析。 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtProperties props;

    private SecretKey key() {
        return Keys.hmacShaKeyFor(props.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public String issueAccessToken(LoginUser user) {
        return build(user, props.getExpireSeconds(), "access");
    }

    public String issueRefreshToken(LoginUser user) {
        return build(user, props.getRefreshExpireSeconds(), "refresh");
    }

    private String build(LoginUser user, long ttlSeconds, String type) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .subject(String.valueOf(user.getUserId()))
                .issuer(props.getIssuer())
                .issuedAt(new Date(now))
                .expiration(new Date(now + ttlSeconds * 1000))
                .claim("typ", type)
                .claim("username", user.getUsername())
                .claim("nickname", user.getNickname())
                .claim("roles", user.getRoles() == null ? List.of() : List.copyOf(user.getRoles()))
                .claim("perms", user.getPermissions() == null ? List.of() : List.copyOf(user.getPermissions()))
                .claim("contestId", user.getContestId())
                .claim("orgCode", user.getOrgCode())
                .signWith(key())
                .compact();
    }

    /** 解析并校验 token，失败返回 null。 */
    public LoginUser parse(String token) {
        if (token == null || token.isBlank()) return null;
        try {
            Claims c = Jwts.parser().verifyWith(key()).requireIssuer(props.getIssuer())
                    .build().parseSignedClaims(token).getPayload();

            if ("refresh".equals(c.get("typ", String.class))) return null;

            @SuppressWarnings("unchecked")
            List<String> roles = (List<String>) c.get("roles", List.class);
            @SuppressWarnings("unchecked")
            List<String> perms = (List<String>) c.get("perms", List.class);
            Number contestId = c.get("contestId", Number.class);

            return LoginUser.builder()
                    .userId(Long.valueOf(c.getSubject()))
                    .username(c.get("username", String.class))
                    .nickname(c.get("nickname", String.class))
                    .roles(roles == null ? Set.of() : roles.stream().collect(Collectors.toSet()))
                    .permissions(perms == null ? Set.of() : perms.stream().collect(Collectors.toSet()))
                    .contestId(contestId == null ? null : contestId.longValue())
                    .orgCode(c.get("orgCode", String.class))
                    .build();
        } catch (Exception e) {
            log.debug("JWT 解析失败: {}", e.getMessage());
            return null;
        }
    }

    /** 刷新场景：校验 refresh token 并回填用户。 */
    public LoginUser parseRefresh(String token) {
        try {
            Claims c = Jwts.parser().verifyWith(key()).requireIssuer(props.getIssuer())
                    .build().parseSignedClaims(token).getPayload();
            if (!"refresh".equals(c.get("typ", String.class))) return null;
            @SuppressWarnings("unchecked")
            List<String> roles = (List<String>) c.get("roles", List.class);
            @SuppressWarnings("unchecked")
            List<String> perms = (List<String>) c.get("perms", List.class);
            Number contestId = c.get("contestId", Number.class);
            return LoginUser.builder()
                    .userId(Long.valueOf(c.getSubject()))
                    .username(c.get("username", String.class))
                    .nickname(c.get("nickname", String.class))
                    .roles(roles == null ? Set.of() : roles.stream().collect(Collectors.toSet()))
                    .permissions(perms == null ? Set.of() : perms.stream().collect(Collectors.toSet()))
                    .contestId(contestId == null ? null : contestId.longValue())
                    .orgCode(c.get("orgCode", String.class))
                    .build();
        } catch (Exception e) {
            return null;
        }
    }

    public Map<String, Object> describe(String token) {
        LoginUser u = parse(token);
        if (u == null) return Map.of();
        return Map.of("userId", u.getUserId(), "username", u.getUsername(), "roles", u.getRoles());
    }
}
