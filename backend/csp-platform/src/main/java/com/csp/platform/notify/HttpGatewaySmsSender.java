package com.csp.platform.notify;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 通用 HTTP 短信网关通道：POST JSON {mobile, content}，可选 Bearer Token。
 *
 * <p>适配自建短信网关、云函数、或第三方聚合平台的转发接口。</p>
 */
@Slf4j
@Component
public class HttpGatewaySmsSender implements SmsSender {

    private final NotifyProperties props;
    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5)).build();

    public HttpGatewaySmsSender(NotifyProperties props) {
        this.props = props;
    }

    @Override
    public String provider() {
        return "http";
    }

    @Override
    public void sendCode(String phone, String code) throws Exception {
        String content = "【网络安全竞赛平台】您的登录验证码为 " + code + "，5 分钟内有效，请勿泄露。";
        sendContent(phone, content);
    }

    @Override
    public void sendText(String phone, String text) throws Exception {
        sendContent(phone, text);
    }

    private void sendContent(String phone, String content) throws Exception {
        if (isBlank(props.getSmsGateway())) {
            throw new IllegalStateException("短信网关未配置 csp.notify.sms-gateway");
        }
        Map<String, String> body = new LinkedHashMap<>();
        body.put("mobile", phone);
        body.put("content", content);

        HttpRequest.Builder b = HttpRequest.newBuilder(URI.create(props.getSmsGateway()))
                .timeout(Duration.ofSeconds(8))
                .header("Content-Type", "application/json;charset=UTF-8")
                .POST(HttpRequest.BodyPublishers.ofString(toJson(body), StandardCharsets.UTF_8));
        if (!isBlank(props.getSmsToken())) {
            b.header("Authorization", "Bearer " + props.getSmsToken());
        }
        HttpResponse<String> r = client.send(b.build(),
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (r.statusCode() < 200 || r.statusCode() >= 300) {
            throw new IllegalStateException("短信网关返回 " + r.statusCode() + ": " + r.body());
        }
        log.info("[SMS-HTTP] 已下发 phone={} status={}", mask(phone), r.statusCode());
    }

    private String toJson(Map<String, String> map) {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, String> e : map.entrySet()) {
            if (!first) sb.append(',');
            first = false;
            sb.append('"').append(escape(e.getKey())).append("\":\"")
              .append(escape(e.getValue())).append('"');
        }
        return sb.append('}').toString();
    }

    private static String escape(String s) {
        return s == null ? "" : s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private static String mask(String phone) {
        return phone != null && phone.length() >= 7
                ? phone.substring(0, 3) + "****" + phone.substring(7) : "***";
    }
}